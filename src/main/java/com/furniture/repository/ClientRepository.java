package com.furniture.repository;

import com.furniture.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByInn(String inn);
    boolean existsByName(String name);

    @Query("SELECT c FROM Client c ORDER BY c.name")
    List<Client> findAllOrderByName();

    List<Client> findByNameContainingIgnoreCase(String name);
}