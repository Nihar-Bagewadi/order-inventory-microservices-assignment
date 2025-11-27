package com.nihar.inventory_service.service;

import com.nihar.inventory_service.dto.InventoryUpdateRequest;
import com.nihar.inventory_service.dto.ProductBatchDto;
import com.nihar.inventory_service.dto.ProductDto;
import com.nihar.inventory_service.exception.InsufficientInventoryException;
import com.nihar.inventory_service.exception.ProductNotFoundException;
import com.nihar.inventory_service.model.Product;
import com.nihar.inventory_service.model.ProductBatch;
import com.nihar.inventory_service.repository.ProductBatchRepository;
import com.nihar.inventory_service.repository.ProductRepository;
import com.nihar.inventory_service.service.factory.InventoryAllocatorFactory;
import com.nihar.inventory_service.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductBatchRepository batchRepository;

    @Mock
    private InventoryAllocatorFactory allocatorFactory;

    @Mock
    private InventoryAllocator allocator;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product product;
    private ProductBatch batch1;
    private ProductBatch batch2;

    @BeforeEach
    void setup() {

        product = new Product();
        product.setId(1L);
        product.setName("Paracetamol");

        batch1 = new ProductBatch();
        batch1.setId(101L);
        batch1.setBatchNumber("B1");
        batch1.setQuantity(50);
        batch1.setExpiryDate(LocalDate.now().plusDays(30));
        batch1.setProduct(product);

        batch2 = new ProductBatch();
        batch2.setId(102L);
        batch2.setBatchNumber("B2");
        batch2.setQuantity(30);
        batch2.setExpiryDate(LocalDate.now().plusDays(60));
        batch2.setProduct(product);
    }

    @Test
    void testGetBatchesByProduct_Success() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(batchRepository.findByProductIdOrderByExpiryDateAsc(1L))
                .thenReturn(List.of(batch1, batch2));

        List<ProductBatchDto> result = inventoryService.getBatchesByProduct(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBatchNumber()).isEqualTo("B1");
        verify(productRepository).findById(1L);
        verify(batchRepository).findByProductIdOrderByExpiryDateAsc(1L);
    }

    @Test
    void testGetBatchesByProduct_ProductNotFound() {

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getBatchesByProduct(1L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(batchRepository, never()).findByProductIdOrderByExpiryDateAsc(anyLong());
    }

    @Test
    void testUpdateInventory_Success() {

        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1L);
        request.setQuantity(60);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(batchRepository.findByProductIdOrderByExpiryDateAsc(1L))
                .thenReturn(List.of(batch1, batch2));

        when(allocatorFactory.getAllocator("FEFO"))
                .thenReturn(allocator);

        inventoryService.updateInventory(request);

        verify(allocator).allocate(List.of(batch1, batch2), 60);
        verify(batchRepository).saveAll(List.of(batch1, batch2));
    }

    @Test
    void testUpdateInventory_ProductNotFound() {
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(99L);
        request.setQuantity(10);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.updateInventory(request))
                .isInstanceOf(ProductNotFoundException.class);

        verify(batchRepository, never()).saveAll(any());
    }

    @Test
    void testUpdateInventory_InsufficientQuantity() {

        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1L);
        request.setQuantity(200); // more than available 80

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(batchRepository.findByProductIdOrderByExpiryDateAsc(1L))
                .thenReturn(List.of(batch1, batch2));

        assertThatThrownBy(() -> inventoryService.updateInventory(request))
                .isInstanceOf(InsufficientInventoryException.class);

        verify(allocator, never()).allocate(any(), anyInt());
        verify(batchRepository, never()).saveAll(any());
    }

    @Test
    void testGetProducts() {

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Vitamin C");
        product2.setDescription("Immunity booster");

        when(productRepository.findAll())
                .thenReturn(List.of(product, product2));

        List<ProductDto> result = inventoryService.getProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getName()).isEqualTo("Vitamin C");

        verify(productRepository).findAll();
    }
}
