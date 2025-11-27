package com.nihar.order_service.service;

import com.nihar.order_service.dto.OrderRequestDto;
import com.nihar.order_service.dto.OrderResponseDto;

import java.util.List;

public interface OrderServiceService {

    OrderResponseDto createOrder(OrderRequestDto requestDto);

    OrderResponseDto getOrderInfo(Long id);

    List<OrderResponseDto> getAllOrders();
}
