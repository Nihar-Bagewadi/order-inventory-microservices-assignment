package com.nihar.inventory_service.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryUpdateRequest {

    private Long productId;
    private int quantity;
    private LocalDateTime expiryDate;
}
