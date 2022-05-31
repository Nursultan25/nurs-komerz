package com.example.moneytransfer.repository;

import com.example.moneytransfer.entity.Transaction;
import com.example.moneytransfer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByCode(String code);
    Page<Transaction> findByCode(String code, Pageable pageRequest);
    Page<Transaction> findAllByDateCreatedBetween(Date date1, Date date2, Pageable pageRequest);
}
