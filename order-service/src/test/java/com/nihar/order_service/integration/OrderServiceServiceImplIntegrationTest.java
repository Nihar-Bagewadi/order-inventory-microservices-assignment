package com.nihar.order_service.integration;

import com.nihar.order_service.dto.OrderRequestDto;
import com.nihar.order_service.dto.OrderResponseDto;
import com.nihar.order_service.model.Order;
import com.nihar.order_service.repository.OrderRepository;
import com.nihar.order_service.service.impl.OrderServiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class OrderServiceServiceImplIntegrationTest {

    @Autowired
    private OrderServiceServiceImpl orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    private OrderRequestDto orderRequestDto;

    @BeforeEach
    void setUp() {
        orderRequestDto = new OrderRequestDto(1L, 5);
    }

    @Test
    void testCreateOrder_SuccessfulInventoryUpdate() {
        // Mock the RestTemplate call to inventory-service
        Mockito.when(restTemplate.postForEntity(
                        any(String.class),
                        any(OrderRequestDto.class),
                        eq(String.class)))
                .thenReturn(new ResponseEntity<>("Inventory updated successfully", HttpStatus.ACCEPTED));

        OrderResponseDto response = orderService.createOrder(orderRequestDto);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(orderRequestDto.getProductId());
        assertThat(response.getQuantity()).isEqualTo(orderRequestDto.getQuantity());
        assertThat(response.getOrderNumber()).isNotNull();

        // Verify that the order is persisted in H2 DB
        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getProductId()).isEqualTo(orderRequestDto.getProductId());
    }

    @Test
    void testCreateOrder_InventoryServiceFails() {

        Mockito.when(restTemplate.postForEntity(
                        any(String.class),
                        any(OrderRequestDto.class),
                        eq(String.class)))
                .thenReturn(new ResponseEntity<>("Failure", HttpStatus.BAD_REQUEST));

        OrderResponseDto response = orderService.createOrder(orderRequestDto);

        // Should return empty response DTO
        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isNull();
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    void testGetOrderInfo() {
        // First, create an order in DB directly
        Order savedOrder = orderRepository.save(Order.builder()
                .productId(1L)
                .quantity(10)
                .orderNumber("ORD-123")
                .build());

        OrderResponseDto response = orderService.getOrderInfo(savedOrder.getId());

        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD-123");
        assertThat(response.getQuantity()).isEqualTo(10);
        assertThat(response.getProductId()).isEqualTo(1L);
    }

    @Test
    void testGetAllOrders() {

        orderRepository.save(Order.builder().productId(1L).quantity(5).orderNumber("ORD-001").build());
        orderRepository.save(Order.builder().productId(2L).quantity(3).orderNumber("ORD-002").build());

        List<OrderResponseDto> allOrders = orderService.getAllOrders();

        assertThat(allOrders).hasSize(2);
        assertThat(allOrders).extracting("orderNumber").containsExactlyInAnyOrder("ORD-001", "ORD-002");
    }
}
