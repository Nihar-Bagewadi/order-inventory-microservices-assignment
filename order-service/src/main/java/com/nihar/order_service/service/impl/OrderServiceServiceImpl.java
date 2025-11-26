package com.nihar.order_service.service.impl;

import com.nihar.order_service.dto.OrderRequestDto;
import com.nihar.order_service.dto.OrderResponseDto;
import com.nihar.order_service.model.Order;
import com.nihar.order_service.repository.OrderRepository;
import com.nihar.order_service.service.OrderServiceService;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceServiceImpl implements OrderServiceService {

    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;

    @Value("${inventory-service.base-url}")
    private String inventoryBaseUrl;

    @Override
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {

        String url = inventoryBaseUrl.concat("/update");

        ResponseEntity<String> inventoryResponse = restTemplate.postForEntity(url, requestDto, String.class);

        if(inventoryResponse.getStatusCode().is2xxSuccessful()) {
            Order order = Order.builder()
                    .orderNumber(UUID.randomUUID().toString())
                    .productId(requestDto.getProductId())
                    .quantity(requestDto.getQuantity())
                    .build();
            Order savedOrder = orderRepository.save(order);
            return OrderResponseDto.builder()
                    .orderNumber(savedOrder.getOrderNumber())
                    .quantity(savedOrder.getQuantity())
                    .productId(savedOrder.getProductId())
                    .build();
        }

        return OrderResponseDto.builder().build();
    }

    @Override
    public OrderResponseDto getOrderInfo(Long id) {
        Optional<Order> orderInfoPredicate = orderRepository.findById(id);
        if(orderInfoPredicate.isPresent()) {
            Order orderInfo = orderInfoPredicate.get();
            return OrderResponseDto.builder()
                    .orderNumber(orderInfo.getOrderNumber())
                    .productId(orderInfo.getProductId())
                    .quantity(orderInfo.getQuantity())
                    .build();
        }
        else {
            return null;
        }
    }

    @Override
    public List<OrderResponseDto> getAllOrders() {
        List<Order> ordersList = orderRepository.findAll();

        return ordersList.stream().
                map(order -> OrderResponseDto.builder()
               .orderNumber(order.getOrderNumber())
               .productId(order.getProductId())
               .quantity(order.getQuantity())
               .build()
                ).toList();
    }
}
