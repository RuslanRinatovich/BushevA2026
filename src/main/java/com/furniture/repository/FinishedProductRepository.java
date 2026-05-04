package com.furniture.repository;

import com.furniture.entity.FinishedProduct;
import com.furniture.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FinishedProductRepository extends JpaRepository<FinishedProduct, Long> {

    Optional<FinishedProduct> findBySku(String sku);

    boolean existsBySku(String sku);

    List<FinishedProduct> findByCategory(ProductCategory category);

    // Товары с положительным остатком
    @Query("SELECT p FROM FinishedProduct p WHERE p.currentBalance > 0")
    List<FinishedProduct> findProductsWithStock();

    // Товары с остатком больше указанного значения
    List<FinishedProduct> findByCurrentBalanceGreaterThan(BigDecimal balance);

    @Query("SELECT p FROM FinishedProduct p ORDER BY p.name")
    List<FinishedProduct> findAllOrderByName();
}