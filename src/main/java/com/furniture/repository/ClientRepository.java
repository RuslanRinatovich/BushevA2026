package com.furniture.repository;

import com.furniture.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByInn(String inn);
    List<Client> findByNameContainingIgnoreCase(String name);
    List<Client> findByOrderByNameAsc();
}