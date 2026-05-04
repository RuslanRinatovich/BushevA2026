package com.furniture.repository;

import com.furniture.entity.Material;
import com.furniture.entity.MaterialConsumption;
import com.furniture.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface MaterialConsumptionRepository extends JpaRepository<MaterialConsumption, Long> {
    List<MaterialConsumption> findByOrder(ProductionOrder order);
    List<MaterialConsumption> findByOrderAndWrittenOffFalse(ProductionOrder order);
    List<MaterialConsumption> findByMaterialAndWrittenOffTrue(Material material);

    @Query("SELECT mc FROM MaterialConsumption mc WHERE mc.order.id = :orderId")
    List<MaterialConsumption> findByOrderId(Long orderId);

    @Modifying
    @Transactional
    @Query("UPDATE MaterialConsumption mc SET mc.writtenOff = true, mc.writtenOffAt = CURRENT_TIMESTAMP WHERE mc.order.id = :orderId")
    void markAllAsWrittenOffByOrderId(Long orderId);
}