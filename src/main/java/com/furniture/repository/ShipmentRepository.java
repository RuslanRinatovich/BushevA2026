package com.furniture.repository;

import com.furniture.entity.Client;
import com.furniture.entity.FinishedProduct;
import com.furniture.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByShipmentDateBetween(LocalDate startDate, LocalDate endDate);
    List<Shipment> findByClient(Client client);
    List<Shipment> findByProduct(FinishedProduct product);
    List<Shipment> findByOrderByShipmentDateDesc();
    Optional<Shipment> findTopByOrderByIdDesc();

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Shipment s WHERE s.shipmentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueForPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}