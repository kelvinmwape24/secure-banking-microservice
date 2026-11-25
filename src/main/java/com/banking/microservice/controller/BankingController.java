package com.banking.microservice.controller;

import com.banking.microservice.model.User;
import com.banking.microservice.service.BankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/banking")
public class BankingController {

    private final BankingService bankingService;

    public BankingController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");
        String accountNumber = (String) payload.get("accountNumber");
        BigDecimal initialDeposit = new BigDecimal(payload.get("initialDeposit").toString());

        User user = bankingService.registerUser(username, password, accountNumber, initialDeposit);
        return ResponseEntity.ok(Map.of("message", "User registered successfully", "userId", user.getId()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> payload) {
        String fromAcc = (String) payload.get("fromAccount");
        String toAcc = (String) payload.get("toAccount");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        bankingService.transferFunds(fromAcc, toAcc, amount);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Transfer completed safely with row-level locks."));
    }
}
