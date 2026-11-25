package com.banking.microservice.service;

import com.banking.microservice.model.Account;
import com.banking.microservice.model.User;
import com.banking.microservice.repository.AccountRepository;
import com.banking.microservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankingService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public BankingService(UserRepository userRepository, AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
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

    @Transactional
    public void transferFunds(String fromAccountNum, String toAccountNum, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }
        if (fromAccountNum.equals(toAccountNum)) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        Account fromAccount = accountRepository.findByAccountNumberWithLock(fromAccountNum)
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        Account toAccount = accountRepository.findByAccountNumberWithLock(toAccountNum)
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }
}
