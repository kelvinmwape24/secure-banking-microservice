package com.banking.microservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_records")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String status;
    private LocalDateTime timestamp = LocalDateTime.now();

    public TransactionRecord() {}

    public TransactionRecord(String transactionId, String sourceAccount, String destinationAccount, BigDecimal amount, String status) {
        this.transactionId = transactionId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public String getSourceAccount() { return sourceAccount; }
    public String getDestinationAccount() { return destinationAccount; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
