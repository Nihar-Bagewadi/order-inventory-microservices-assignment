package com.nihar.inventory_service.integration;

import com.nihar.inventory_service.dto.InventoryUpdateRequest;
import com.nihar.inventory_service.dto.ProductBatchDto;
import com.nihar.inventory_service.dto.ProductDto;
import com.nihar.inventory_service.model.Product;
import com.nihar.inventory_service.model.ProductBatch;
import com.nihar.inventory_service.repository.ProductBatchRepository;
import com.nihar.inventory_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // ensure fresh DB per test class
class InventoryIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    private String baseUrl() {
        return "http://localhost:" + port + "/inventory";
    }

    private final ObjectMapper objectMapper = new ObjectMapper();


    private final ProductRepository productRepository;
    private final ProductBatchRepository batchRepository;

    InventoryIntegrationTest(@Autowired ProductRepository productRepository,
                             @Autowired ProductBatchRepository batchRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
    }

    @BeforeEach
    void setUp() {
        // clean DB
        batchRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void getProducts_returnsSavedProducts() {
        // Seed products
        Product p1 = new Product();
        p1.setName("Prod A");
        p1.setDescription("desc A");
        p1 = productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("Prod B");
        p2.setDescription("desc B");
        p2 = productRepository.save(p2);

        // Call GET /inventory/get-products
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/get-products", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // parse response body to list of ProductDto
        List<ProductDto> dtos = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(ProductDto::getName).containsExactlyInAnyOrder("Prod A", "Prod B");
    }

    @Test
    void getInventoryByProduct_returnsBatchesSortedByExpiry() throws Exception {
        // Create product + batches
        Product p = new Product();
        p.setName("Milk");
        p.setDescription("Dairy");
        p = productRepository.save(p);

        ProductBatch b1 = new ProductBatch();
        b1.setProduct(p);
        b1.setBatchNumber("B1");
        b1.setQuantity(50);
        b1.setExpiryDate(LocalDate.now().plusDays(10));
        batchRepository.save(b1);

        ProductBatch b2 = new ProductBatch();
        b2.setProduct(p);
        b2.setBatchNumber("B2");
        b2.setQuantity(20);
        b2.setExpiryDate(LocalDate.now().plusDays(2));
        batchRepository.save(b2);

        // Call GET /inventory/{productId}
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/" + p.getId(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ProductBatchDto> dtos = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getBatchNumber()).isEqualTo("B2");
        assertThat(dtos.get(1).getBatchNumber()).isEqualTo("B1");
    }

    @Test
    void updateInventory_success_reducesBatches() {
        // Seed product with batches
        Product p = new Product();
        p.setName("Tablet");
        p.setDescription("Med");
        p = productRepository.save(p);

        ProductBatch b1 = new ProductBatch();
        b1.setProduct(p);
        b1.setBatchNumber("X1");
        b1.setQuantity(5);
        b1.setExpiryDate(LocalDate.now().plusDays(30));
        b1 = batchRepository.save(b1);

        ProductBatch b2 = new ProductBatch();
        b2.setProduct(p);
        b2.setBatchNumber("X2");
        b2.setQuantity(10);
        b2.setExpiryDate(LocalDate.now().plusDays(60));
        b2 = batchRepository.save(b2);

        InventoryUpdateRequest req = new InventoryUpdateRequest(p.getId(), 7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryUpdateRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl() + "/update", entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Reload batches from DB and assert quantities updated
        ProductBatch refreshed1 = batchRepository.findById(b1.getId()).orElseThrow();
        ProductBatch refreshed2 = batchRepository.findById(b2.getId()).orElseThrow();

        assertThat(refreshed1.getQuantity()).isEqualTo(0); // used fully
        assertThat(refreshed2.getQuantity()).isEqualTo(8); // 10 - 2 = 8
    }

    @Test
    void updateInventory_insufficientInventory_returnsBadRequest() {
        Product p = new Product();
        p.setName("Syrup");
        p.setDescription("Cough");
        p = productRepository.save(p);

        ProductBatch b = new ProductBatch();
        b.setProduct(p);
        b.setBatchNumber("S1");
        b.setQuantity(3);
        b.setExpiryDate(LocalDate.now().plusDays(10));
        b = batchRepository.save(b);


        InventoryUpdateRequest request = new InventoryUpdateRequest(p.getId(), 5);

        try {
            restTemplate.postForEntity(baseUrl() + "/update", request, String.class);
            fail("Expected HttpClientErrorException.BadRequest");
        } catch (HttpClientErrorException.BadRequest ex) {

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());

            String body = ex.getResponseBodyAsString();

            assertTrue(body.contains("Not enough stock for product Syrup"));
        }
    }

    @Test
    void updateInventory_productNotFound_returnsNotFound() {

        // Using a product id that does not exist
        InventoryUpdateRequest req = new InventoryUpdateRequest(9999L, 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryUpdateRequest> entity = new HttpEntity<>(req, headers);

        try {
            restTemplate.postForEntity(baseUrl() + "/update", entity, String.class);
            fail("Expected for RestTemplate to get 404 but passed");
        } catch (HttpClientErrorException.NotFound ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Test
    void updateInventory_validationFails_returnsBadRequest() {
        // Missing productId or invalid quantity (quantity < 1)
        InventoryUpdateRequest invalid = new InventoryUpdateRequest(null, 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<InventoryUpdateRequest> entity = new HttpEntity<>(invalid, headers);

        try {
            restTemplate.postForEntity(baseUrl() + "/update", entity, String.class);
            fail("Expected for RestTemplate to fail but passed");
        } catch (HttpClientErrorException.BadRequest ex) {

            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());

            String body = ex.getResponseBodyAsString();

            assertTrue(body.contains("Product ID cannot be Null"));
            assertTrue(body.contains("Quantity must be atleast 1"));

        }
    }
}
