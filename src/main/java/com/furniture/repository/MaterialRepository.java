package com.furniture.repository;

import com.furniture.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    Optional<Material> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Material> findByOrderByNameAsc();

    // Материалы с остатком меньше или равным минимальному
    @Query("SELECT m FROM Material m WHERE m.currentBalance <= m.minBalance")
    List<Material> findLowStockMaterials();

    // Материалы с остатком ниже минимального
    @Query("SELECT m FROM Material m WHERE m.currentBalance < m.minBalance")
    List<Material> findBelowMinStock();
}