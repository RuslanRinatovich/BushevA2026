package com.furniture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@Table(name = "finished_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinishedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(length = 20)
    private String unit = "шт";

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "current_balance", precision = 12, scale = 3)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Lob
    @JdbcTypeCode(Types.BINARY)
    @Column(columnDefinition = "bytea")
    private byte[] image;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}