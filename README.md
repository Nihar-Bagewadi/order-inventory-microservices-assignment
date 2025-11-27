# order-inventory-microservices-assignment

## Note

I have made few assumptions in the assignment for simplicity.

- **Each Order constitutes of only one Product** : In practical scenarios we can have multiple products in a single order, In such case we have have another entity of Product and Quantity mapped to the Order as List of ProductBatches.

- **Dispatch the batches which expires first** : Based on the factory pattern I have created a InventoryAllocator which follows `FEFO` (First Expiry First Out). The batch which expires first would be updated.

- **Dispatching batch even if expired** : In practical scenarios we don't dispatch expired products but for sake of simplicity I am assuming this is okay, if not there is another query which will query the available batches in sorted expiry date which can be used.

## Project Setup

This project is structured as two independent Spring Boot microservices:

1. order_service

2. inventory_service

### Technologies Used

- Java 17

- Spring Boot 3.x

- Spring Data JPA

- H2 Database (for tests)

- Lombok

- RestTemplate-based Integration Tests

- JUnit 5 & SpringBootTest

- ControllerAdvice-based Exception Handling

- Factory Design Pattern in Inventory Service for future Extensibility

-

Each service can run standalone and exposes its own REST API. Both services follow the same structure:

### Inventory Service

```
    src/
├── main/
│   ├── java/
│   │   ├── com/
│   │       ├── nihar/
│   │           ├── inventory_service/
│   │               ├── controller/ -> controller Classes
│   │               ├── dto/ -> All DTO for Requests and Response
│   │               ├── exception/ -> for Exception Handling
│   │               ├── model/ -> for storing JPA entities
│   │               ├── repository/ -> for storing repositories
│   │               ├── service/ -> business Logic
│   │               │   ├── factory/  -> for storing factory Classes
│   │               │   └── impl/ -> Implementations of Service Interfaces
│   │               └── InventoryServiceApplication.java -> Main Application Class
│   ├── resources/
│       ├── application.yaml -> application configurations
│       └── data.sql -> Adding seed data
├── test/
    ├── java/
        ├── com/
            ├── nihar/
                ├── inventory_service/
                    ├── controller/ -> controller Tests
                    ├── integration/ -> Integration Tests
                    ├── repository/ -> respository Tests
                    ├── service/ -> Service Tests
                    └── InventoryServiceApplicationTests.java

```

### Order Service

```
src/
├── main/
│   ├── java/
│   │   ├── com/
│   │       ├── nihar/
│   │           ├── order_service/
│   │               ├── config/
│   │               ├── controller/ -> controller Classes
│   │               ├── dto/ -> All DTO for Requests and Response
│   │               ├── exception/ -> for Exception Handling
│   │               ├── model/ -> for storing JPA entities
│   │               ├── repository/ -> for storing repositories
│   │               ├── service/ -> business Logic
│   │               │   └── impl/ -> Implementations of Service Interfaces
│   │               └── OrderServiceApplication.java -> Main Application Class
│   ├── resources/
│       └── application.yaml -> application configurations
├── test/
    ├── java/
        ├── com/
            ├── nihar/
                ├── order_service/
                    ├── integration/ -> Integration Tests
                    ├── service/ -> Service Tests
                    └── OrderServiceApplicationTests.java

```

You can run both of the service with the command

```
mvn spring-boot:run
```

Port Configuration

By default:

- Order Service → localhost:9008

- Inventory Service → localhost:8080

## API documentation

### Order Service

---

#### 1. Create Order

**POST** `/order`

Request Body :

```
    {
        "productId": 1,
        "quantity": 5
    }
```

Sample Response :

```
    {
        "orderNumber": "57a5426d-e83c-4d62-bd98-ab9a9bc8d467",
        "productId": 1,
        "quantity": 35
    }
```

---

#### 2. Get Order

**GET** `/order/{id}`

Sample Response :

```
    {
        "orderNumber": "57a5426d-e83c-4d62-bd98-ab9a9bc8d467",
        "productId": 1,
        "quantity": 35
    }
```

---

#### 3. Get Orders

**GET** `/orders`

```
[
    {
        "orderNumber": "57a5426d-e83c-4d62-bd98-ab9a9bc8d467",
        "productId": 1,
        "quantity": 35
    },
    {
        "orderNumber": "eec45302-4acb-4ab6-9a8b-4a3ac4b5486b",
        "productId": 2,
        "quantity": 30
    }
]
```

### Inventory Service

---

#### 1. Get Product Batch

**GET** `/inventory/{productId}`

Sample Response :

```
    [
        {
            "id": 2,
            "productId": 1,
            "batchNumber": "PCM-002",
            "quantity": 15,
            "expiryDate": "2025-06-05"
        },
        {
            "id": 1,
            "productId": 1,
            "batchNumber": "PCM-001",
            "quantity": 100,
            "expiryDate": "2026-01-10"
        }
    ]
```

---

#### 2. Update Inventory

Ideally since we are updating the inventory it should be a PUT call we can consider this when we are scaling.

**POST** `/inventory/update`

Request Body :

```
    {
        "productId": 1,
        "quantity": 10
    }
```

Sample Response :
Status code `202 Accepted`

```
Inventory updated successfully
```

---

#### 3. Get Products (Additional)

**GET** `/inventory/get-products`

Sample Response :

```
    [
        {
            "id": 1,
            "name": "Paracetamol",
            "description": "Pain relief / fever reducer"
        },
        {
            "id": 2,
            "name": "Vitamin C",
            "description": "Immunity booster tablets"
        }
]
```

---

## Testing Instructions

### Testing Setup

- Unit Tests use JUnit 5 and Mockito.

- Integration Tests use:

  - @SpringBootTest(webEnvironment = RANDOM_PORT)

  - H2 in-memory DB

  - RestTemplate/WebClient for real HTTP calls

  - Database auto-cleanup before each test

- Each service has isolated Spring context and test database.
