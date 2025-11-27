package com.nihar.order_service.integration;

import com.nihar.order_service.dto.OrderRequestDto;
import com.nihar.order_service.dto.OrderResponseDto;
import com.nihar.order_service.model.Order;
import com.nihar.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private static RestTemplate testRestTemplate;

    // Since we are using h2 database for both actual implementation and
    // Testing we are autowiring the same repo else we can have a test
    // repository interface as well
    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    private String baseUrl;

    @BeforeAll
    static void init() {
        testRestTemplate = new RestTemplate();
    }

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/order-service/api/";
        // clearing db before each test
        orderRepository.deleteAll();
    }

    @Test
    void testCreateOrder_success() {

        OrderRequestDto requestDto = new OrderRequestDto(1L, 5);
        Mockito.when(restTemplate.postForEntity(
                Mockito.anyString(),
                Mockito.any(),
                Mockito.eq(String.class)
        )).thenReturn(new ResponseEntity<>("Inventory updated successfully", HttpStatus.ACCEPTED));

        ResponseEntity<OrderResponseDto> response = testRestTemplate.postForEntity(
                baseUrl + "/order",
                requestDto,
                OrderResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductId()).isEqualTo(1L);
        assertThat(response.getBody().getQuantity()).isEqualTo(5);

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    void testGetOrderById_success() {

        Order order = Order.builder()
                .orderNumber("ORD-123")
                .productId(2L)
                .quantity(10)
                .build();
        order = orderRepository.save(order);

        ResponseEntity<OrderResponseDto> response = testRestTemplate.getForEntity(
                baseUrl + "/order/" + order.getId(),
                OrderResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductId()).isEqualTo(2L);
        assertThat(response.getBody().getQuantity()).isEqualTo(10);
        assertThat(response.getBody().getOrderNumber()).isEqualTo("ORD-123");
    }

    @Test
    void testGetAllOrders_success() {

        orderRepository.save(Order.builder().orderNumber("ORD-1").productId(1L).quantity(5).build());
        orderRepository.save(Order.builder().orderNumber("ORD-2").productId(2L).quantity(10).build());

        ResponseEntity<OrderResponseDto[]> response = testRestTemplate.getForEntity(
                baseUrl + "/orders",
                OrderResponseDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}
