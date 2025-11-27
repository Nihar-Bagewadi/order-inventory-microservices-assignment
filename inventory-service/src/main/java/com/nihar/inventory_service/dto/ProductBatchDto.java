package com.nihar.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductBatchDto {
    private Long id;
    private Long productId;
    private String batchNumber;
    private Integer quantity;
    private LocalDate expiryDate;
}
