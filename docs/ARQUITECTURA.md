# Arquitectura - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Arquitecto:** Amazon Q Developer  
**Versión:** 2.0  
**Fecha:** Diciembre 2025

---

## 1. Stack Tecnológico

### 1.1 Backend
- **Java 21 LTS** - Records, Pattern Matching, Virtual Threads
- **Spring Boot 3.2** - Framework principal con arquitectura en capas
- **Spring Data JPA** - Persistencia con constructor injection
- **Spring Scheduler** - Tareas asíncronas con Virtual Threads
- **Lombok** - Reducción de boilerplate (@RequiredArgsConstructor, @Slf4j)

### 1.2 Persistencia
- **PostgreSQL 15** - Base de datos principal con índices optimizados
- **Redis 7** - Cola de mensajes + Cache de sesiones
- **Flyway** - Migraciones de base de datos versionadas

### 1.3 Integraciones
- **Telegram Bot API** - Notificaciones push asíncronas

### 1.4 Infraestructura
- **Docker** - Contenedores
- **Docker Compose** - Orquestación local (3 servicios máximo)

---

## 2. Diagrama de Contexto (C4 Level 1)

**Elementos:** 5 | **Tiempo explicación:** ~1 minuto | **Cumple Rule #1:** ✅

```
┌─────────────┐
│   Cliente   │──(1. Crea ticket)──┐
│  (Terminal) │                    │
└─────────────┘                    ▼
                          ┌──────────────────┐
┌─────────────┐           │  Sistema         │
│  Ejecutivo  │◄─(3)──────│  Ticketero       │──(2. Notifica)──► [Telegram API]
│ (Dashboard) │           │                  │
└─────────────┘           └──────────────────┘
                                   │
┌─────────────┐                   │
│  Supervisor │◄─(4. Monitorea)───┘
│  (Monitor)  │
└─────────────┘
```

**Flujos principales:**
1. Cliente crea ticket → Sistema valida y persiste
2. Sistema notifica → Telegram API (asíncrono)
3. Sistema asigna → Ejecutivo disponible (automático)
4. Supervisor monitorea → Dashboard tiempo real

---

## 3. Diagrama de Secuencia End-to-End

**Flujo:** Crear Ticket y Notificar (Happy Path)  
**Interacciones:** 8 | **Tiempo explicación:** ~2 minutos | **Cumple Rule #1:** ✅

```
Cliente    Controller    Service    Repository    DB    TelegramService    Redis
  │            │            │            │          │           │            │
  │─POST /api/tickets──────►│            │          │           │            │
  │            │            │            │          │           │            │
  │            │─crearTicket(request)───►│          │           │            │
  │            │            │            │          │           │            │
  │            │            │─save(ticket)─────────►│◄─INSERT──►│           │            │
  │            │            │            │          │           │            │
  │            │            │─programarMensajes()──────────────►│           │
  │            │            │            │          │           │            │
  │            │            │            │          │           │◄─LPUSH────►│
  │            │            │            │          │           │            │
  │            │◄─TicketResponse─────────│          │           │            │
  │            │            │            │          │           │            │
  │◄─201 Created───────────│            │          │           │            │
```

**Arquitectura en Capas (Spring Boot Pattern):**
- **Controller**: `@RestController` + `@Valid` + `ResponseEntity<T>`
- **Service**: `@Service` + `@Transactional` + lógica de negocio
- **Repository**: `JpaRepository<Entity, ID>` + queries derivadas
- **TelegramService**: `@Service` + Virtual Threads para I/O
- **Redis**: Cola asíncrona para desacoplar notificaciones

---

## 4. Modelo de Datos (ER)

**Tablas:** 4 | **Tiempo explicación:** ~2 minutos | **Cumple Rule #1:** ✅

```
┌─────────────────────────┐
│       ticket            │
├─────────────────────────┤
│ id (PK) BIGSERIAL       │
│ codigo_referencia UUID  │◄──┐
│ numero VARCHAR(10)      │   │
│ national_id VARCHAR(20) │   │
│ telefono VARCHAR(15)    │   │
│ queue_type VARCHAR(20)  │   │
│ status VARCHAR(20)      │   │
│ position_in_queue INT   │   │
│ created_at TIMESTAMP    │   │
│ assigned_advisor_id FK  │───┼──┐
└─────────────────────────┘   │  │
                              │  │
┌─────────────────────────┐   │  │
│       mensaje           │   │  │
├─────────────────────────┤   │  │
│ id (PK) BIGSERIAL       │   │  │
│ ticket_id (FK)          │───┘  │
│ plantilla VARCHAR(50)   │      │
│ estado_envio VARCHAR(20)│      │
│ fecha_programada        │      │
│ telegram_message_id     │      │
└─────────────────────────┘      │
                                 │
┌─────────────────────────┐      │
│       advisor           │      │
├─────────────────────────┤      │
│ id (PK) BIGSERIAL       │◄─────┘
│ name VARCHAR(100)       │
│ status VARCHAR(20)      │
│ module_number INT       │
│ queue_types TEXT[]      │
└─────────────────────────┘
```

**Relaciones JPA:**
- `@OneToMany(mappedBy = "ticket")` + `@ToString.Exclude`
- `@ManyToOne(fetch = LAZY)` + `@JoinColumn`
- Enums con `@Enumerated(EnumType.STRING)`

**Índices (Flyway):**
```sql
CREATE INDEX idx_ticket_codigo ON ticket(codigo_referencia);
CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_mensaje_ticket ON mensaje(ticket_id);
```

---

## 5. Estructura de Paquetes (Spring Boot Pattern)

```
src/main/java/com/banco/ticketero/
│
├── controller/                  # @RestController
│   ├── TicketController.java    # @RequiredArgsConstructor + @Slf4j
│   └── AdminController.java     # @Valid + ResponseEntity<T>
│
├── service/                     # @Service + @Transactional
│   ├── TicketService.java       # Lógica de negocio principal
│   ├── TelegramService.java     # Virtual Threads para I/O
│   └── AssignmentService.java   # Asignación automática
│
├── repository/                  # @Repository + JpaRepository
│   ├── TicketRepository.java    # Query derivadas + @Query
│   ├── AdvisorRepository.java   # findByStatus, countBy...
│   └── MessageRepository.java   # JPQL con @Param
│
├── model/
│   ├── entity/                  # @Entity + Lombok
│   │   ├── Ticket.java          # @Builder + @ToString.Exclude
│   │   ├── Advisor.java         # @NoArgsConstructor + @AllArgsConstructor
│   │   └── Message.java         # @PrePersist + @PreUpdate
│   └── dto/                     # Records (Java 21)
│       ├── TicketRequest.java   # @NotBlank + @Valid
│       └── TicketResponse.java  # Inmutable + constructor desde Entity
│
├── config/                      # @Configuration
│   ├── RedisConfig.java         # @Bean para RedisTemplate
│   └── AsyncConfig.java         # Virtual Threads executor
│
├── scheduler/                   # @Scheduled + @Component
│   └── MessageScheduler.java    # @Async con Virtual Threads
│
└── exception/                   # @ControllerAdvice
    ├── TicketActivoException.java
    └── GlobalExceptionHandler.java  # @ExceptionHandler + ErrorResponse
```

---

## 6. Componentes Clave (Spring Boot Patterns)

### 6.1 TicketService

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TelegramService telegramService;
    
    @Transactional
    public TicketResponse crearTicket(TicketRequest request) {
        // Validación RN-001 (unicidad)
        validateNoActiveTicket(request.nationalId());
        
        // Crear entity con Builder pattern
        Ticket ticket = Ticket.builder()
            .nationalId(request.nationalId())
            .telefono(request.telefono())
            .queueType(request.queueType())
            .status(TicketStatus.EN_ESPERA)
            .build();
            
        Ticket saved = ticketRepository.save(ticket);
        
        // Programar mensajes asíncronos
        telegramService.programarMensajes(saved);
        
        return new TicketResponse(saved);
    }
}
```

### 6.2 TelegramService (Virtual Threads)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {
    private final ExecutorService virtualThreadExecutor;
    
    @Async
    public void programarMensajes(Ticket ticket) {
        virtualThreadExecutor.submit(() -> {
            // I/O bloqueante en Virtual Thread
            sendTelegramMessage(ticket);
        });
    }
}
```

### 6.3 Repository Pattern (JPA)

```java
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    // Query derivada (Spring genera SQL)
    Optional<Ticket> findByCodigoReferencia(UUID codigo);
    
    List<Ticket> findByStatusAndQueueType(TicketStatus status, QueueType type);
    
    boolean existsByNationalIdAndStatusIn(String nationalId, List<TicketStatus> statuses);
    
    // @Query solo para casos complejos
    @Query("""
        SELECT t FROM Ticket t 
        WHERE t.status = :status 
        ORDER BY t.createdAt ASC
        """)
    List<Ticket> findNextInQueue(@Param("status") TicketStatus status);
}
```

---

## 7. DTOs con Records (Java 21)

### 7.1 Request DTOs

```java
public record TicketRequest(
    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "Invalid national ID")
    String nationalId,
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone")
    String telefono,
    
    @NotNull(message = "Queue type is required")
    QueueType queueType
) {}
```

### 7.2 Response DTOs

```java
public record TicketResponse(
    UUID codigoReferencia,
    String numero,
    QueueType queueType,
    TicketStatus status,
    Integer posicionEnCola,
    Integer tiempoEstimadoMinutos,
    LocalDateTime createdAt
) {
    // Constructor desde Entity
    public TicketResponse(Ticket ticket) {
        this(
            ticket.getCodigoReferencia(),
            ticket.getNumero(),
            ticket.getQueueType(),
            ticket.getStatus(),
            ticket.getPositionInQueue(),
            ticket.getEstimatedWaitMinutes(),
            ticket.getCreatedAt()
        );
    }
}
```

---

## 8. Exception Handling

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
            
        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse("Validation failed", 400, errors));
    }
}

public record ErrorResponse(
    String message,
    int status,
    LocalDateTime timestamp,
    List<String> errors
) {
    public ErrorResponse(String message, int status, List<String> errors) {
        this(message, status, LocalDateTime.now(), errors);
    }
}
```

---

## 9. Configuración (Java 21 + Virtual Threads)

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public Executor getAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
```

---

## 10. Flyway Migrations

### V1__create_tickets_table.sql
```sql
CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    codigo_referencia UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    numero VARCHAR(10) NOT NULL,
    national_id VARCHAR(20) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    queue_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    position_in_queue INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    assigned_advisor_id BIGINT
);

CREATE INDEX idx_ticket_codigo ON ticket(codigo_referencia);
CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_national_id ON ticket(national_id);
```

---

## 11. Docker Compose (Simplificado)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ticketero
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_PASSWORD: ${DB_PASSWORD}
      TELEGRAM_BOT_TOKEN: ${TELEGRAM_BOT_TOKEN}
    depends_on:
      - postgres
      - redis
```

---

## 12. Endpoints REST API

### 12.1 Tickets (Público)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | /api/tickets | Crear ticket |
| GET | /api/tickets/{uuid} | Consultar ticket |

### 12.2 Admin (Protegido)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/admin/dashboard | Dashboard completo |
| GET | /api/admin/queues | Estado de colas |

---

## 13. Cumplimiento de Reglas

### ✅ Rule #1: Simplicidad Verificable
- **Diagramas core:** 3 (Contexto, Secuencia, ER)
- **Elementos por diagrama:** ≤10
- **Tiempo explicación:** ≤3 minutos cada uno

### ✅ Spring Boot Patterns
- **Arquitectura en capas:** Controller → Service → Repository
- **Constructor injection:** `@RequiredArgsConstructor`
- **Transacciones:** `@Transactional(readOnly = true)` por defecto
- **Validación:** `@Valid` en controllers

### ✅ JPA Best Practices
- **Entities:** `@ToString.Exclude` en relaciones
- **Repositories:** Query derivadas + `@Query` solo si necesario
- **Índices:** Definidos en Flyway migrations

### ✅ Java 21 Features
- **Records:** Para todos los DTOs
- **Virtual Threads:** Para operaciones I/O (Telegram API)
- **Pattern Matching:** En switch expressions
- **Text Blocks:** Para queries JPQL multilinea

### ✅ Lombok Usage
- **Services:** `@RequiredArgsConstructor` + `@Slf4j`
- **Entities:** `@Builder` + `@ToString.Exclude`
- **NO @Data:** En entities con relaciones

---

**Versión:** 2.0  
**Última actualización:** Diciembre 2025  
**Cumple reglas:** ✅ Todas