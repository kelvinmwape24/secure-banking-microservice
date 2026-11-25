package com.banking.microservice.service;

import com.banking.microservice.model.Account;
import com.banking.microservice.model.TransactionRecord;
import com.banking.microservice.model.User;
import com.banking.microservice.repository.AccountRepository;
import com.banking.microservice.repository.TransactionRepository;
import com.banking.microservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class BankingService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public BankingService(UserRepository userRepository, AccountRepository accountRepository, TransactionRepository transactionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUserWithAccount(String username, String rawPassword, String accountNumber, BigDecimal initialDeposit) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            throw new RuntimeException("Account number already exists");
        }
        if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Initial deposit cannot be negative");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(initialDeposit);
        account.setUser(savedUser);
        accountRepository.save(account);

        return savedUser;
    }

    public User authenticateUser(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return user;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<TransactionRecord> getAllTransactions() {
        return transactionRepository.findAllByOrderByTimestampDesc();
    }

    @Transactional
    public String transferFunds(String fromAccountNum, String toAccountNum, BigDecimal amount) {
        String txId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }
        if (fromAccountNum.equals(toAccountNum)) {
            throw new RuntimeException("Cannot transfer funds to the same account");
        }

        Account fromAccount = accountRepository.findByAccountNumberWithLock(fromAccountNum)
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        Account toAccount = accountRepository.findByAccountNumberWithLock(toAccountNum)
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            transactionRepository.save(new TransactionRecord(txId, fromAccountNum, toAccountNum, amount, "FAILED_INSUFFICIENT_FUNDS"));
            throw new RuntimeException("Insufficient funds in source account");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transactionRepository.save(new TransactionRecord(txId, fromAccountNum, toAccountNum, amount, "SUCCESS"));
        return txId;
    }
}
