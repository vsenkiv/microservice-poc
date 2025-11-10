
## 🏗️ Architecture Overview

This is a microservices-based e-commerce PoC built with Spring Boot 3.5.6 and Java 21, implementing advanced patterns including Event-Driven Architecture, CQRS/Event Sourcing with Axon Framework, Saga Pattern, Circuit Breaker, and Redis Caching.

## 📦 Services Overview

### 1. Order Service (Port: 8081)

**Purpose:** Central orchestrator for order management, handling order creation, validation, and coordination with other services.

**Technology Stack:**
- Spring Boot with JPA (PostgreSQL)
- Axon Framework (CQRS/Event Sourcing)
- Kafka (Event Publishing)
- Redis (Caching)
- GraphQL + REST APIs
- OAuth2 Security (Google)
- Resilience4j (Circuit Breaker)

**Key Components:**
- `OrderController` - REST endpoints for order operations
- `OrderGraphQLController` - GraphQL query interface
- `OrderService` - Business logic with caching
- `OrderProcessingSaga` - Orchestrates distributed transactions
- `OrderEventPublisher` - Publishes events to Kafka
- `InventoryService` - Validates inventory with circuit breaker

**Entry Points:**
- **REST API:** `POST /api/orders` - Create order
- **REST API:** `POST /api/orders/saga` - Create order with Saga orchestration
- **REST API:** `GET /api/orders/{orderId}` - Get order by ID
- **REST API:** `GET /api/orders/me` - Get current authenticated user
- **GraphQL:** `POST /graphql` - GraphQL queries (getOrder, getOrders)
- **GraphiQL:** `GET /graphiql` - Interactive GraphQL interface

**Database:** PostgreSQL (orders, order_items tables)

### 2. User Service (Port: 8083)

**Purpose:** Manages user profiles, addresses, and provides user validation for other services.

**Technology Stack:**
- Spring Boot with JPA (PostgreSQL)
- OpenTelemetry (Distributed Tracing)
- REST APIs

**Key Components:**
- `UserController` - REST endpoints for user management
- `UserService` - User business logic
- `UserRepository` - Data persistence

**Entry Points:**
- **REST API:** `POST /api/users` - Create user
- **REST API:** `GET /api/users/{userId}` - Get user by ID
- **REST API:** `GET /api/users/username/{username}` - Get user by username
- **REST API:** `GET /api/users` - Get all users
- **REST API:** `PUT /api/users/{userId}` - Update user
- **REST API:** `DELETE /api/users/{userId}` - Delete user
- **REST API:** `POST /api/users/{userId}/addresses` - Add address
- **REST API:** `GET /api/users/search?name={name}` - Search users
- **REST API:** `GET /api/users/{userId}/exists` - Check if user exists

**Database:** PostgreSQL (users, addresses tables)

### 3. Inventory Service (Port: 8084)

**Purpose:** Manages product catalog, stock levels, and inventory reservations.

**Technology Stack:**
- Spring Boot with JPA (PostgreSQL)
- OpenTelemetry (Distributed Tracing)
- REST APIs

**Key Components:**
- `InventoryController` - REST endpoints for inventory management
- `InventoryService` - Inventory business logic
- `Product`, `StockReservation`, `InventoryTransaction` - Domain entities

**Entry Points:**
- **REST API:** `GET /api/inventory/check/{productId}?quantity={qty}` - Check stock availability
- **REST API:** `POST /api/inventory/products` - Create product
- **REST API:** `GET /api/inventory/products/{productId}` - Get product
- **REST API:** `GET /api/inventory/products` - Get all products
- **REST API:** `GET /api/inventory/products/category/{category}` - Get products by category
- **REST API:** `GET /api/inventory/products/search?keyword={keyword}` - Search products
- **REST API:** `PUT /api/inventory/products/{productId}` - Update product
- **REST API:** `POST /api/inventory/products/{productId}/adjust` - Adjust stock
- **REST API:** `POST /api/inventory/reservations` - Reserve stock
- **REST API:** `DELETE /api/inventory/reservations/order/{orderId}/product/{productId}` - Release reservation
- **REST API:** `POST /api/inventory/reservations/order/{orderId}/product/{productId}/fulfill` - Fulfill reservation
- **REST API:** `GET /api/inventory/products/reorder` - Get products needing reorder

**Database:** PostgreSQL (products, stock_reservations, inventory_transactions tables)

### 4. Payment Service (Port: 8085)

**Purpose:** Handles payment processing asynchronously using reactive programming, consumes order events from Kafka.

**Technology Stack:**
- Spring Boot with Reactive MongoDB
- Spring WebFlux (Reactive Programming)
- Kafka (Event Consumer)
- Server-Sent Events (SSE) for real-time streaming

**Key Components:**
- `PaymentController` - Reactive REST endpoints
- `PaymentProcessingService` - Async payment processing
- `PaymentQueryService` - Reactive queries
- `OrderEventConsumer` - Kafka listener for order events

**Entry Points:**
- **SSE Stream:** `GET /api/payments/orders/stream` - Stream all order events
- **SSE Stream:** `GET /api/payments/orders/stream/user/{userId}` - Stream user order events
- **SSE Stream:** `GET /api/payments/transactions/stream` - Stream payment transactions
- **REST API:** `GET /api/payments/orders` - Get all order events (paginated)
- **REST API:** `GET /api/payments/orders/user/{userId}` - Get order events by user
- **REST API:** `GET /api/payments/orders/{orderId}` - Get order events by order ID
- **REST API:** `GET /api/payments/orders/date-range?start={}&end={}` - Get events by date range
- **REST API:** `GET /api/payments/transactions` - Get all transactions
- **REST API:** `GET /api/payments/transactions/{transactionId}` - Get transaction by ID
- **REST API:** `GET /api/payments/transactions/order/{orderId}` - Get transactions by order
- **REST API:** `POST /api/payments/transactions/{transactionId}/retry` - Retry failed payment
- **REST API:** `GET /api/payments/stats` - Get payment statistics

**Database:** MongoDB (paymentTransactions, orderEvents collections)

## 🔄 Business Workflow

### Standard Order Creation Flow:
1. **User Request** → Order Service receives `POST /api/orders`
2. **User Validation** → Order Service checks Redis cache, then calls User Service
3. **Inventory Check** → Order Service validates stock via Inventory Service (with Circuit Breaker)
4. **Order Creation** → Order persisted in PostgreSQL with status PENDING
5. **Event Publishing** → OrderCreatedEvent published to Kafka topic
6. **Cache Eviction** → Redis cache evicted for affected data
7. **Order Confirmation** → Status updated to CONFIRMED
8. **Payment Processing** → Payment Service consumes Kafka event asynchronously
9. **Payment Persistence** → Payment transaction saved to MongoDB
10. **Payment Event** → PaymentProcessedEvent published to Kafka
11. **Order Update** → Order Service consumes payment event and updates order status

### Saga-Based Order Flow (Distributed Transaction):
1. **User Request** → `POST /api/orders/saga`
2. **Command Dispatch** → CreateOrderCommand sent to Axon Server
3. **Saga Initiation** → OrderProcessingSaga starts
4. **User Validation** → ValidateUserCommand → UserValidatedEvent
5. **Inventory Reservation** → ReserveInventoryCommand → InventoryReservedEvent
6. **Payment Processing** → ProcessPaymentCommand → PaymentProcessedEvent
7. **Order Completion** → CompleteOrderCommand → OrderCompletedEvent
8. **Saga End** → Transaction complete

### Failure Handling (Compensating Transactions):
- **Payment Fails** → Release inventory + Fail order
- **Inventory Unavailable** → Fail order immediately
- **User Invalid** → Fail order immediately
- **Order Cancelled** → Release inventory + Refund payment (if processed)

## 🔌 Service Communication Patterns

### Synchronous Communication (REST/HTTP):
- **Order → User:** User validation
- **Order → Inventory:** Stock checking (with Circuit Breaker & Retry)

### Asynchronous Communication (Kafka):
- **Order → Payment:** OrderCreatedEvent
- **Payment → Order:** PaymentProcessedEvent

### Event Sourcing (Axon Server):
- Order Saga orchestration
- Command/Event handling
- Event store persistence

### Caching (Redis):
- User data cached by Order Service
- Order data cached for reads
- Cache eviction on updates

## 🛠️ Infrastructure Components

### Databases:
- **PostgreSQL** (port 5432): Orders, Users, Inventory
- **MongoDB** (port 27017): Payments (reactive)
- **Redis** (port 6379): Distributed cache

### Messaging & Events:
- **Kafka** (port 9092): Event streaming
- **Zookeeper** (port 2181): Kafka coordination
- **Axon Server** (port 8124): CQRS/Event Sourcing

### Observability:
- **Prometheus** (port 9090): Metrics collection
- **Grafana** (port 3000): Dashboards
- **Jaeger** (port 16686): Distributed tracing
- **Elasticsearch** (port 9200): Log storage
- **Kibana** (port 5601): Log visualization
- **OpenTelemetry Collector** (port 4318): Telemetry aggregation

### Monitoring UIs:
- **Kafka UI** (port 8090): Kafka monitoring
- **Mongo Express** (port 8091): MongoDB UI
- **Redis Commander** (port 8092): Redis UI

### Load Testing:
- **JMeter:** Performance testing container

## 🔐 Security Features

- **OAuth2 with Google:** Authentication in Order Service
- **JWT Tokens:** Resource server validation
- **Security Configuration:** Protected endpoints

## 🛡️ Resilience Patterns

### Circuit Breaker (Resilience4j):
- Applied to Inventory Service calls
- Failure threshold: 50%
- Wait duration: 5s in open state
- Sliding window: 10 calls

### Retry:
- Max attempts: 3
- Exponential backoff (2x multiplier)
- 100ms initial wait

### Timeout:
- 3s timeout for inventory calls
- Cancellable futures

## 📊 Key Technical Features

- **CQRS/Event Sourcing:** Axon Framework for order sagas
- **Reactive Programming:** WebFlux in Payment Service
- **Event-Driven Architecture:** Kafka for async communication
- **Distributed Caching:** Redis with TTL
- **Distributed Tracing:** OpenTelemetry + Jaeger
- **Metrics & Monitoring:** Prometheus + Grafana
- **Centralized Logging:** ELK Stack
- **API Gateway Pattern:** GraphQL in Order Service
- **Service Mesh Ready:** Docker Compose orchestration
- **Load Testing:** JMeter integration

## 🚀 Getting Started

All services are containerized and orchestrated via `docker-compose.yml`. The system uses a shared PostgreSQL instance with separate schemas/databases per service for data isolation while maintaining deployment simplicity.
