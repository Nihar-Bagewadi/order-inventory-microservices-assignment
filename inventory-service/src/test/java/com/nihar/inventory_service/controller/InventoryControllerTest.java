package com.nihar.inventory_service.controller;

import com.nihar.inventory_service.dto.InventoryUpdateRequest;
import com.nihar.inventory_service.dto.ProductBatchDto;
import com.nihar.inventory_service.dto.ProductDto;
import com.nihar.inventory_service.service.InventoryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@ExtendWith(SpringExtension.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void testGetProducts() throws Exception {

        List<ProductDto> mockProducts = List.of(
                new ProductDto(1L, "Product A", "description A"),
                new ProductDto(2L, "Product B","description B")
        );

        Mockito.when(inventoryService.getProducts()).thenReturn(mockProducts);

        mockMvc.perform(get("/inventory/get-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Product A"));
    }

    @Test
    void testGetInventoryByProduct() throws Exception {

        List<ProductBatchDto> mockBatches = List.of(
                new ProductBatchDto(100L,1L,"001",34, LocalDate.of(2024,03,23)),
                new ProductBatchDto(101L,1L,"002",45, LocalDate.of(2025,07,20))
        );

        Mockito.when(inventoryService.getBatchesByProduct(1L)).thenReturn(mockBatches);

        mockMvc.perform(get("/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].batchNumber").value("001"))
                .andExpect(jsonPath("$[0].quantity").value(34));
    }

    @Test
    void testUpdateInventorySuccess() throws Exception {

        InventoryUpdateRequest req =
                new InventoryUpdateRequest(1L, 5);

        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Inventory updated successfully"));

        Mockito.verify(inventoryService, times(1)).updateInventory(any());
    }

    @Test
    void testUpdateInventoryValidationFailure() throws Exception {

        InventoryUpdateRequest invalidReq =
                new InventoryUpdateRequest(1L, -1);

        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }
}
