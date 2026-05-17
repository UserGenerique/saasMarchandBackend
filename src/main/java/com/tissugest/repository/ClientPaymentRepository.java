package com.tissugest.repository;

import com.tissugest.entity.ClientPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientPaymentRepository extends JpaRepository<ClientPayment, Long> {
    List<ClientPayment> findBySaleIdOrderByCreatedAtDesc(Long saleId);
    List<ClientPayment> findByClientIdOrderByCreatedAtDesc(Long clientId);
}
