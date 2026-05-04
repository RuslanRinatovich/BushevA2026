package com.furniture.repository;

import com.furniture.entity.FinishedProduct;
import com.furniture.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long>, JpaSpecificationExecutor<ProductionOrder> {

    List<ProductionOrder> findByStatus(String status);

    List<ProductionOrder> findByProduct(FinishedProduct product);

    List<ProductionOrder> findByOrderByOrderNumberDesc();

    List<ProductionOrder> findByStatusAndPlannedDateBefore(String status, LocalDate date);

    @Query("SELECT o FROM ProductionOrder o WHERE o.status IN ('planned', 'in_progress')")
    List<ProductionOrder> findActiveOrders();

    Optional<ProductionOrder> findTopByOrderByIdDesc();

    // Поиск по номеру заказа или названию продукции
    @Query("SELECT o FROM ProductionOrder o WHERE " +
            "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.product.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<ProductionOrder> searchOrders(@Param("search") String search);

    // Фильтр по статусу и поиску
    @Query("SELECT o FROM ProductionOrder o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:search IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.product.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ProductionOrder> filterOrders(@Param("status") String status, @Param("search") String search);
}