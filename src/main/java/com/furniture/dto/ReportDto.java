package com.furniture.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReportDto {
    private String name;
    private BigDecimal value;
    private String unit;
}