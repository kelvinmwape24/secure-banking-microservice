package com.banking.microservice.service;

import com.banking.microservice.model.Account;
import com.banking.microservice.model.User;
import com.banking.microservice.repository.AccountRepository;
import com.banking.microservice.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class BankingService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public BankingService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public User registerUser(String username, String rawPassword, String initialAccountNumber, BigDecimal initialDeposit) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }
        User user = new User(username, passwordEncoder.encode(rawPassword));
        user = userRepository.save(user);

        Account account = new Account(initialAccountNumber, initialDeposit, user);
        accountRepository.save(account);

        return user;
    }

    @Transactional
    public void transferFunds(String fromAccNumber, String toAccNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        Account fromAccount = accountRepository.findByAccountNumberForUpdate(fromAccNumber)
                .orElseThrow(() -> new RuntimeException("Source account not found: " + fromAccNumber));

        Account toAccount = accountRepository.findByAccountNumberForUpdate(toAccNumber)
                .orElseThrow(() -> new RuntimeException("Destination account not found: " + toAccNumber));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in source account.");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }
}
