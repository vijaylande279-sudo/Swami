# 🏨 Swami Hotel — OMS Backend Guide
### Spring Boot 3.x | Java 21 | PostgreSQL

> Read this before writing any code. Every rule exists because a mistake happened.

---

## 📐 Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL 15+ |
| Auth | Spring Security + JWT (jjwt 0.12.3) |
| Real-time | Spring WebSocket + STOMP |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| QR Code | ZXing 3.5.3 |
| Docs | Springdoc OpenAPI |
| Migration | Flyway |

---

## 🗂️ Project Structure

```
src/main/java/com/hotel/oms/
├── HotelOmsApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   └── CorsConfig.java
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   └── RegisterRequest.java
├── module/
│   ├── user/
│   │   ├── User.java
│   │   ├── UserRepository.java
│   │   ├── UserService.java
│   │   └── UserController.java
│   ├── table/
│   │   ├── DiningTable.java
│   │   ├── TableRepository.java
│   │   ├── TableService.java
│   │   └── TableController.java
│   ├── menu/
│   │   ├── MenuCategory.java
│   │   ├── MenuItem.java
│   │   ├── MenuCategoryRepository.java
│   │   ├── MenuItemRepository.java
│   │   ├── MenuService.java
│   │   └── MenuController.java
│   ├── order/
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── OrderStatus.java           ← enum
│   │   ├── OrderItemStatus.java       ← enum
│   │   ├── OrderRepository.java
│   │   ├── OrderItemRepository.java
│   │   ├── OrderService.java
│   │   ├── OrderController.java
│   │   ├── OrderNotification.java     ← WebSocket payload
│   │   └── TableStatusEvent.java      ← WebSocket payload
│   └── bill/
│       ├── Bill.java
│       ├── BillRepository.java
│       ├── BillService.java
│       ├── BillController.java
│       └── QRCodeService.java
├── exception/
│   ├── AppException.java
│   └── GlobalExceptionHandler.java
└── util/
    └── ApiResponse.java
```

---

## 🗄️ Database Schema

```sql
-- V1__create_users.sql
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) UNIQUE NOT NULL,
    password   TEXT NOT NULL,           -- bcrypt only, NEVER plain
    role       VARCHAR(20) NOT NULL DEFAULT 'WAITER',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- V2__create_tables.sql
CREATE TABLE dining_tables (
    id           BIGSERIAL PRIMARY KEY,
    table_number VARCHAR(10) UNIQUE NOT NULL,
    capacity     INT NOT NULL,
    status       VARCHAR(20) DEFAULT 'AVAILABLE'
    -- AVAILABLE | OCCUPIED | RESERVED | ORDER_READY | ORDER_COMPLETED
);

-- V3__create_menu.sql
CREATE TABLE menu_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0
);

CREATE TABLE menu_items (
    id           BIGSERIAL PRIMARY KEY,
    category_id  BIGINT REFERENCES menu_categories(id) ON DELETE SET NULL,
    name         VARCHAR(150) NOT NULL,
    description  TEXT,
    price        NUMERIC(10,2) NOT NULL,   -- NEVER float/double
    is_available BOOLEAN DEFAULT TRUE,
    image_url    TEXT,
    created_at   TIMESTAMPTZ DEFAULT NOW()
);

-- V4__create_orders.sql
CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    table_id     BIGINT REFERENCES dining_tables(id),
    waiter_id    BIGINT REFERENCES users(id),
    session_type VARCHAR(20),              -- LUNCH | DINNER
    status       VARCHAR(30) DEFAULT 'OPEN',
    notes        TEXT,
    created_at   TIMESTAMPTZ DEFAULT NOW(),
    closed_at    TIMESTAMPTZ
);

CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id BIGINT REFERENCES menu_items(id),
    quantity     INT NOT NULL DEFAULT 1,
    unit_price   NUMERIC(10,2) NOT NULL,   -- snapshot at order time
    notes        TEXT,
    status       VARCHAR(30) DEFAULT 'PENDING',
    created_at   TIMESTAMPTZ DEFAULT NOW()
);

-- V5__create_bills.sql
CREATE TABLE bills (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT UNIQUE REFERENCES orders(id),
    subtotal        NUMERIC(10,2) NOT NULL,
    tax_percent     NUMERIC(5,2) DEFAULT 5,
    tax_amount      NUMERIC(10,2) DEFAULT 0,
    total           NUMERIC(10,2) NOT NULL,
    payment_mode    VARCHAR(30),
    payment_url     TEXT,           -- UPI deep link
    qr_code_base64  TEXT,           -- Base64 PNG for QR
    is_paid         BOOLEAN DEFAULT FALSE,
    generated_at    TIMESTAMPTZ DEFAULT NOW(),
    paid_at         TIMESTAMPTZ
);
```

---

## 🔄 Order Status Flow (State Machine)

```
OPEN → SENT_TO_KITCHEN → PREPARING → READY → SERVED → BILLED → CLOSED
```

```java
public enum OrderStatus {
    OPEN, SENT_TO_KITCHEN, PREPARING, READY, SERVED, BILLED, CLOSED
}
```

**Transition rules:**
- `SENT_TO_KITCHEN` → WebSocket push to `/topic/kitchen` (kitchen sees new card)
- `PREPARING` → Chef clicked "Start Cooking" — card turns blue
- `READY` → Chef clicked "Order Complete" — push to `/topic/waiter` + `/topic/tables` (green border)
- `SERVED` → Waiter served food — table resets
- `BILLED` → Admin generated bill — no more item changes allowed
- Any illegal transition → throw `AppException(400)`

---

## 🔌 REST API Endpoints

```
POST   /api/auth/register
POST   /api/auth/login
GET    /api/auth/me

GET    /api/tables
POST   /api/tables                      [ADMIN]
PATCH  /api/tables/{id}/status

GET    /api/menu/categories
GET    /api/menu/items
POST   /api/menu/items                  [ADMIN]
PUT    /api/menu/items/{id}             [ADMIN]
DELETE /api/menu/items/{id}             [ADMIN] soft-delete only

POST   /api/orders
GET    /api/orders/{id}
GET    /api/orders/kitchen              ← Kitchen: SENT_TO_KITCHEN + PREPARING orders
GET    /api/orders/ready                ← Waiter: READY orders notification list
POST   /api/orders/{id}/items
DELETE /api/orders/{id}/items/{itemId}
PATCH  /api/orders/{id}/send            ← Waiter sends → kitchen notified
PATCH  /api/orders/{id}/preparing       ← Chef starts cooking
PATCH  /api/orders/{id}/ready           ← Chef done → waiter + table notified
PATCH  /api/orders/{id}/served          ← Waiter served → table resets

POST   /api/bills/generate/{orderId}    [ADMIN]
GET    /api/bills/{id}
GET    /api/bills/{id}/print
PATCH  /api/bills/{id}/pay
```

---

## 📡 WebSocket Events

| Topic | When emitted | Payload | Receiver |
|---|---|---|---|
| `/topic/kitchen` | Waiter sends order | `OrderNotification` | Kitchen screen |
| `/topic/waiter` | Chef marks READY | `OrderNotification` | All waiter devices |
| `/topic/tables` | Any status change | `TableStatusEvent` | All clients |
| `/topic/admin` | Bill generated | `AdminNotification` | Admin dashboard |

```java
// OrderNotification.java
public record OrderNotification(
    Long orderId,
    String tableNumber,
    String sessionType,
    String status,
    String message,
    List<OrderItemInfo> items,
    String notes,
    String createdAt
) {}

// TableStatusEvent.java  
public record TableStatusEvent(
    Long tableId,
    String tableNumber,
    String status,
    String borderColor   // "green" | "orange" | "gray" | "red"
) {}
```

---

## 🧾 QR Code — Bill Payment

### pom.xml
```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
</dependency>
```

### QRCodeService.java
```java
@Service
public class QRCodeService {
    public String generateBase64(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new AppException("QR generation failed", 500);
        }
    }
}
```

### Bill generation
```java
// BillService.java — always BigDecimal, never double
BigDecimal subtotal = items.stream()
    .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
    .reduce(BigDecimal.ZERO, BigDecimal::add);

BigDecimal taxPercent = new BigDecimal("5");
BigDecimal taxAmount = subtotal
    .multiply(taxPercent)
    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

BigDecimal total = subtotal.add(taxAmount);

String upiUrl = String.format(
    "upi://pay?pa=%s&pn=SwamiHotel&am=%s&cu=INR&tn=Table%s",
    hotelUpiId, total.toPlainString(), tableNumber
);

String qrBase64 = qrCodeService.generateBase64(upiUrl, 300);
```

---

## 🔐 Security Config

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/ws/**").permitAll()
            .requestMatchers(POST, "/api/menu/**").hasRole("ADMIN")
            .requestMatchers(PUT, "/api/menu/**").hasRole("ADMIN")
            .requestMatchers(DELETE, "/api/menu/**").hasRole("ADMIN")
            .requestMatchers("/api/bills/**").hasAnyRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

---

## ⚙️ application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hotel_oms
spring.datasource.username=postgres
spring.datasource.password=12345
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0

app.jwt.secret=hotelOmsSecretKeyMustBeAtLeast32CharactersLong
app.jwt.expiration-ms=28800000

hotel.name=Swami Hotel
hotel.upi.id=swamihotel@upi
hotel.tax.percent=5
```

---

## 🚫 Never Do These

| ❌ | ✅ |
|---|---|
| `double`/`float` for money | `BigDecimal` always |
| Hard-delete menu items | `is_available = false` |
| Skip state transition validation | Always validate in service |
| Expose `@Entity` from controller | Map to DTO always |
| Hardcode UPI ID | `application.properties` |
| Emit WebSocket to all | Target specific topics |
| Expose stack traces | Log server-side, generic message to client |
| JWT in DB | Stateless only |

---

## 📋 Pre-commit Checklist

- [ ] All money fields `BigDecimal`
- [ ] All state transitions validated
- [ ] WebSocket fires on every status change
- [ ] Kitchen endpoint returns only `SENT_TO_KITCHEN` + `PREPARING`
- [ ] QR code generated on bill creation
- [ ] No secrets in source code
- [ ] `./mvnw test` passes

---
*Swami Hotel OMS — updated 2026-08-18*
