package com.nihar.inventory_service.repository;

import com.nihar.inventory_service.model.Product;
import com.nihar.inventory_service.model.ProductBatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductBatchRepositoryTest {

    @Autowired
    private ProductBatchRepository productBatchRepository;

    @Autowired
    private EntityManager entityManager;

    private Product savedProduct;

    @BeforeEach
    void setup() {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");

        entityManager.persist(product);
        savedProduct = product;

        ProductBatch b1 = buildBatch(savedProduct, "B1", 10, LocalDate.now().plusDays(5));
        ProductBatch b2 = buildBatch(savedProduct, "B2", 20, LocalDate.now().plusDays(2));
        ProductBatch b3 = buildBatch(savedProduct, "B3", 0, LocalDate.now().plusDays(1));

        entityManager.persist(b1);
        entityManager.persist(b2);
        entityManager.persist(b3);

        entityManager.flush();
    }

    private ProductBatch buildBatch(Product product, String batchNum, int qty, LocalDate expiry) {
        ProductBatch batch = new ProductBatch();
        batch.setProduct(product);
        batch.setBatchNumber(batchNum);
        batch.setQuantity(qty);
        batch.setExpiryDate(expiry);
        return batch;
    }

    @Test
    void testFindByProductIdOrderByExpiryDateAsc() {
        List<ProductBatch> batches =
                productBatchRepository.findByProductIdOrderByExpiryDateAsc(savedProduct.getId());

        assertThat(batches).hasSize(3);

        assertThat(batches.get(0).getBatchNumber()).isEqualTo("B3"); // earliest expiry
        assertThat(batches.get(1).getBatchNumber()).isEqualTo("B2");
        assertThat(batches.get(2).getBatchNumber()).isEqualTo("B1");
    }

    @Test
    void testFindAvailableByProductIdOrderByExpiryDateAsc() {
        LocalDate today = LocalDate.now();

        List<ProductBatch> batches =
                productBatchRepository.findAvailableByProductIdOrderByExpiryDateAsc(
                        savedProduct.getId(), today);

        assertThat(batches).hasSize(2);

        assertThat(batches.get(0).getBatchNumber()).isEqualTo("B2");
        assertThat(batches.get(1).getBatchNumber()).isEqualTo("B1");

        assertThat(batches.stream().anyMatch(b -> b.getBatchNumber().equals("B3"))).isFalse();
    }
}
