package com.furniture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private FinishedProduct product;

    @Column(name = "planned_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal plannedQuantity;

    @Column(name = "actual_quantity", precision = 10, scale = 2)
    private BigDecimal actualQuantity = BigDecimal.ZERO;

    @Column(length = 20)
    private String status = "planned"; // planned, in_progress, completed, cancelled

    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}