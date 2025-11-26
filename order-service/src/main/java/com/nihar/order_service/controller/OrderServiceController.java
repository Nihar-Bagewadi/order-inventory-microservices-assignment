package com.nihar.order_service.controller;

import com.nihar.order_service.dto.OrderRequestDto;
import com.nihar.order_service.dto.OrderResponseDto;
import com.nihar.order_service.service.OrderServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order-service/api/")
@RequiredArgsConstructor
public class OrderServiceController {

    private final OrderServiceService orderServiceService;


    @PostMapping("/order")
    ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto requestDto) {

        OrderResponseDto orderResponse = orderServiceService.createOrder(requestDto);

        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/order/{id}")
    ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long id) {

        OrderResponseDto orderResponse = orderServiceService.getOrderInfo(id);

        if(null != orderResponse) {
            return new ResponseEntity<>(orderResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/orders")
    ResponseEntity<List<OrderResponseDto>> getOrder() {

        List<OrderResponseDto> orderResponse = orderServiceService.getAllOrders();

        if(!orderResponse.isEmpty()) {
            return new ResponseEntity<>(orderResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
