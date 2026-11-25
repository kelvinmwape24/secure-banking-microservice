package com.banking.microservice.controller;

import com.banking.microservice.model.Account;
import com.banking.microservice.model.User;
import com.banking.microservice.service.BankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/banking")
public class BankingController {

    private final BankingService bankingService;

    public BankingController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> payload) {
        try {
            String username = (String) payload.get("username");
            String password = (String) payload.get("password");
            String accountNumber = (String) payload.get("accountNumber");
            BigDecimal initialDeposit = new BigDecimal(payload.get("initialDeposit").toString());

            User user = bankingService.registerUserWithAccount(username, password, accountNumber, initialDeposit);
            return ResponseEntity.ok(Map.of("message", "Registered successfully", "userId", user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            User user = bankingService.authenticateUser(username, password);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", user.getUsername());
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/accounts")
    public ResponseEntity<List>> getAccounts() {
        List> accounts = bankingService.getAllAccounts().stream().map(acc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("accountNumber", acc.getAccountNumber());
            map.put("balance", acc.getBalance());
            map.put("owner", acc.getUser() != null ? acc.getUser().getUsername() : "N/A");
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> payload) {
        try {
            String fromAccount = (String) payload.get("fromAccount");
            String toAccount = (String) payload.get("toAccount");
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            bankingService.transferFunds(fromAccount, toAccount, amount);
            return ResponseEntity.ok(Map.of("message", "Transferred $" + amount + " from " + fromAccount + " to " + toAccount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
