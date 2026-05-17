package com.tissugest.repository;

import com.tissugest.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByShopId(Long shopId);
    Optional<Client> findByIdAndShopId(Long id, Long shopId);
}
