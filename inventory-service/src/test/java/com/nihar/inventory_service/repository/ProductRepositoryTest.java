package com.nihar.inventory_service.repository;

import com.nihar.inventory_service.model.Product;
import com.nihar.inventory_service.model.ProductBatch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Should save and fetch Product by ID")
    void testFindById() {

        Product product = new Product();
        product.setName("Paracetamol");
        product.setDescription("Pain relief");

        Product savedProduct = productRepository.save(product);


        Optional<Product> found = productRepository.findById(savedProduct.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Paracetamol");
        assertThat(found.get().getDescription()).isEqualTo("Pain relief");
    }

    @Test
    @DisplayName("Should save product with batches and retrieve them (OneToMany)")
    void testProductWithBatches() {

        Product product = new Product();
        product.setName("Vitamin C");
        product.setDescription("Supplement");

        ProductBatch batch1 = new ProductBatch();
        batch1.setBatchNumber("BATCH-001");
        batch1.setQuantity(50);
        batch1.setExpiryDate(LocalDate.now().plusMonths(6));
        batch1.setProduct(product);

        ProductBatch batch2 = new ProductBatch();
        batch2.setBatchNumber("BATCH-002");
        batch2.setQuantity(30);
        batch2.setExpiryDate(LocalDate.now().plusMonths(3));
        batch2.setProduct(product);

        product.getBatches().add(batch1);
        product.getBatches().add(batch2);

        Product savedProduct = productRepository.save(product);

        Optional<Product> found = productRepository.findById(savedProduct.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getBatches()).hasSize(2);

        assertThat(found.get().getBatches().get(0).getBatchNumber()).isEqualTo("BATCH-001");
        assertThat(found.get().getBatches().get(1).getBatchNumber()).isEqualTo("BATCH-002");
    }
}
