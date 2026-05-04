package com.furniture.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ShipmentDto {
    private Long id;
    private String shipmentNumber;
    private Long clientId;
    private String clientName;
    private Long productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private LocalDate shipmentDate;
}