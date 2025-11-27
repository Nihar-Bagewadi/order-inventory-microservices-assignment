package com.nihar.order_service.service;

import com.nihar.order_service.dto.OrderRequestDto;
import com.nihar.order_service.dto.OrderResponseDto;
import com.nihar.order_service.model.Order;
import com.nihar.order_service.repository.OrderRepository;

import com.nihar.order_service.service.impl.OrderServiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceServiceImpl orderService;

    @BeforeEach
    void setup() {

        // since inventoryBaseUrl cannot set via mock
        orderService = new OrderServiceServiceImpl(restTemplate, orderRepository);
        ReflectionTestUtils.setField(orderService, "inventoryBaseUrl", "http://inventory-service");
    }

    @Test
    void testCreateOrder_Success() {

        OrderRequestDto request = new OrderRequestDto(1L, 10);

        ResponseEntity<String> inventoryResponse =
                new ResponseEntity<>("OK", HttpStatus.ACCEPTED);

        when(restTemplate.postForEntity(
                eq("http://inventory-service/update"),
                eq(request),
                eq(String.class)
        )).thenReturn(inventoryResponse);

        Order savedOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD123")
                .productId(1L)
                .quantity(10)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponseDto response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD123");
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getQuantity()).isEqualTo(10);

        verify(orderRepository).save(any(Order.class));
        verify(restTemplate).postForEntity(anyString(), eq(request), eq(String.class));
    }

    @Test
    void testCreateOrder_FailureFromInventory() {

        OrderRequestDto request = new OrderRequestDto(2L, 5);

        ResponseEntity<String> badResponse =
                new ResponseEntity<>("FAIL", HttpStatus.BAD_REQUEST);

        when(restTemplate.postForEntity(
                eq("http://inventory-service/update"),
                eq(request),
                eq(String.class)
        )).thenReturn(badResponse);

        OrderResponseDto response = orderService.createOrder(request);

        assertThat(response.getOrderNumber()).isNull(); // empty DTO returned
        assertThat(response.getProductId()).isNull();

        verify(orderRepository, never()).save(any());
    }

    @Test
    void testGetOrderInfo_Found() {

        Order order = Order.builder()
                .id(10L)
                .orderNumber("ORDER101")
                .productId(3L)
                .quantity(20)
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        OrderResponseDto dto = orderService.getOrderInfo(10L);

        assertThat(dto).isNotNull();
        assertThat(dto.getOrderNumber()).isEqualTo("ORDER101");
        assertThat(dto.getProductId()).isEqualTo(3L);
        assertThat(dto.getQuantity()).isEqualTo(20);
    }

    @Test
    void testGetOrderInfo_NotFound() {

        when(orderRepository.findById(5L)).thenReturn(Optional.empty());

        OrderResponseDto dto = orderService.getOrderInfo(5L);

        assertThat(dto).isNull();
    }

    @Test
    void testGetAllOrders() {

        List<Order> orders = List.of(
                Order.builder().orderNumber("A1").productId(1L).quantity(10).build(),
                Order.builder().orderNumber("B2").productId(2L).quantity(20).build()
        );

        when(orderRepository.findAll()).thenReturn(orders);

        List<OrderResponseDto> response = orderService.getAllOrders();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getOrderNumber()).isEqualTo("A1");
        assertThat(response.get(1).getOrderNumber()).isEqualTo("B2");

        verify(orderRepository).findAll();
    }
}
