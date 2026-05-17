package com.tissugest.repository;

import com.tissugest.entity.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {
    List<MessageLog> findByShopIdOrderByCreatedAtDesc(Long shopId);
    long countByShopId(Long shopId);
}
