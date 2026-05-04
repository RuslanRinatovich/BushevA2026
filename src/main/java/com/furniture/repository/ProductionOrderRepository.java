package com.furniture.repository;

import com.furniture.entity.FinishedProduct;
import com.furniture.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
    List<ProductionOrder> findByStatus(String status);
    List<ProductionOrder> findByStatusAndPlannedDateBefore(String status, LocalDate date);
    List<ProductionOrder> findByProduct(FinishedProduct product);

    @Query("SELECT o FROM ProductionOrder o WHERE o.status = 'planned' AND o.plannedDate <= CURRENT_DATE")
    List<ProductionOrder> findOverdueOrders();

    List<ProductionOrder> findByOrderByOrderNumberDesc();
}