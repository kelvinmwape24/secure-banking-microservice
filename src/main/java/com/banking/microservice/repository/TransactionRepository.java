package com.banking.microservice.repository;

import com.banking.microservice.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findAllByOrderByTimestampDesc();
}
