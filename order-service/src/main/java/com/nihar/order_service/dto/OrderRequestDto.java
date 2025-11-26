package com.nihar.order_service.dto;

import lombok.Data;

@Data
public class OrderRequestDto {

    private Long productId;
    private int quantity;
}
