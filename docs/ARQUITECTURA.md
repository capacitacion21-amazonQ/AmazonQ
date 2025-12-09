# Arquitectura - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Arquitecto:** Amazon Q Developer  
**Versión:** 1.0  
**Fecha:** Diciembre 2025

---

## 1. Stack Tecnológico

### 1.1 Backend
- **Java 21** - LTS, Virtual Threads para concurrencia
- **Spring Boot 3.2** - Framework principal
- **Spring Data JPA** - Persistencia
- **Spring WebSocket** - Comunicación tiempo real
- **Spring Scheduler** - Tareas asíncronas

### 1.2 Persistencia
- **PostgreSQL 15** - Base de datos principal
- **Redis 7** - Cache + Sesiones + Cola de mensajes

### 1.3 Integraciones
- **Telegram Bot API** - Notificaciones push

### 1.4 Infraestructura
- **Docker** - Contenedores
- **Docker Compose** - Orquestación local

---

## 2. Diagrama de Contexto (C4 Level 1)

```
┌─────────────┐
│   Cliente   │──(1. Crea ticket)──┐
│  (Terminal) │                    │
└─────────────┘                    ▼
                          ┌──────────────────┐
┌─────────────┐           │  Sistema         │
│  Ejecutivo  │◄─(4)──────│  Ticketero       │──(2. Notifica)──► [Telegram API]
│ (Dashboard) │           │                  │
└─────────────┘           └──────────────────┘
                                   │
┌─────────────┐                   │
│  Supervisor │◄─(5. Monitorea)───┘
│  (Monitor)  │
└─────────────┘
```

**Flujos principales:**
1. Cliente crea ticket en terminal autoservicio
2. Sistema envía notificaciones vía Telegram
3. Sistema asigna ticket a ejecutivo disponible
4. Ejecutivo atiende cliente en módulo
5. Supervisor monitorea operación en tiempo real

---

## 3. Diagrama de Secuencia End-to-End

**Flujo: Crear Ticket y Notificar (Happy Path)**

```
Cliente    Controller    Service    Repository    DB    TelegramService    Redis    Telegram API
  │            │            │            │          │           │            │           │
  │─POST /api/tickets──────►│            │          │           │            │           │
  │            │            │            │          │           │            │           │
  │            │─crearTicket(request)───►│          │           │            │           │
  │            │            │            │          │           │            │           │
  │            │            │─validar unicidad─────►│◄─SELECT──►│           │           │
  │            │            │            │          │           │            │           │
  │            │            │─save(ticket)─────────►│◄─INSERT──►│           │            │
  │            │            │            │          │           │            │           │
  │            │            │─programarMensajes()──────────────►│           │           │
  │            │            │            │          │           │            │           │
  │            │            │            │          │           │◄─LPUSH────►│           │
  │            │            │            │          │           │  (cola)    │           │
  │            │            │            │          │           │            │           │
  │            │◄─TicketResponse─────────│          │           │            │           │
  │            │            │            │          │           │            │           │
  │◄─201 Created───────────│            │          │           │            │           │
  │            │            │            │          │           │            │           │
  │            │            │            │          │           │            │           │
  │            │      [Proceso Asíncrono - Scheduler cada 5s]   │            │           │
  │            │            │            │          │           │            │           │
  │            │            │            │          │           │◄─RPOP─────►│           │
  │            │            │            │          │           │            │           │
  │            │            │            │          │           │─sendMessage()─────────►│
  │            │            │            │          │           │            │           │
  │            │            │            │          │           │◄─200 OK────────────────│
  │            │            │            │          │           │            │           │
  │            │            │            │          │           │─update(ENVIADO)───────►│
```

**Componentes:**
- **Controller**: Validación HTTP, mapeo DTO
- **Service**: Lógica de negocio, orquestación
- **Repository**: Acceso a datos (JPA)
- **TelegramService**: Integración con Telegram API
- **Redis**: Cola de mensajes pendientes

---

## 4. Modelo de Datos (ER)

```
┌─────────────────────────┐
│       ticket            │
├─────────────────────────┤
│ id (PK)                 │
│ codigo_referencia (UUID)│◄──┐
│ numero (C01, P15)       │   │
│ national_id             │   │
│ telefono                │   │
│ branch_office           │   │
│ queue_type (ENUM)       │   │
│ status (ENUM)           │   │
│ position_in_queue       │   │
│ estimated_wait_minutes  │   │
│ created_at              │   │
│ assigned_advisor_id (FK)│───┼──┐
│ assigned_module_number  │   │  │
└─────────────────────────┘   │  │
                              │  │
                              │  │
┌─────────────────────────┐   │  │
│       mensaje           │   │  │
├─────────────────────────┤   │  │
│ id (PK)                 │   │  │
│ ticket_id (FK)          │───┘  │
│ plantilla (ENUM)        │      │
│ estado_envio (ENUM)     │      │
│ fecha_programada        │      │
│ fecha_envio             │      │
│ telegram_message_id     │      │
│ intentos                │      │
└─────────────────────────┘      │
                                 │
                                 │
┌─────────────────────────┐      │
│       advisor           │      │
├─────────────────────────┤      │
│ id (PK)                 │◄─────┘
│ name                    │
│ email                   │
│ status (ENUM)           │
│ module_number (1-5)     │
│ assigned_tickets_count  │
│ queue_types (ARRAY)     │
└─────────────────────────┘

┌─────────────────────────┐
│     audit_event         │
├─────────────────────────┤
│ id (PK)                 │
│ timestamp               │
│ event_type              │
│ actor                   │
│ entity_type             │
│ entity_id               │
│ previous_state (JSON)   │
│ new_state (JSON)        │
│ metadata (JSON)         │
└─────────────────────────┘
```

**Relaciones:**
- ticket (1) ──< (N) mensaje
- advisor (1) ──< (N) ticket
- audit_event: tabla independiente (log inmutable)

**Índices principales:**
- ticket: codigo_referencia (UNIQUE), national_id, status, queue_type
- mensaje: ticket_id, estado_envio
- advisor: status, module_number

---

## 5. Estructura de Paquetes

```
src/main/java/com/banco/ticketero/
│
├── config/                      # Configuraciones
│   ├── RedisConfig.java
│   ├── TelegramConfig.java
│   └── WebSocketConfig.java
│
├── controller/                  # REST Controllers
│   ├── TicketController.java
│   └── AdminController.java
│
├── service/                     # Lógica de negocio
│   ├── TicketService.java
│   ├── QueueService.java
│   ├── AssignmentService.java
│   ├── TelegramService.java
│   └── AuditService.java
│
├── repository/                  # Acceso a datos
│   ├── TicketRepository.java
│   ├── AdvisorRepository.java
│   ├── MessageRepository.java
│   └── AuditEventRepository.java
│
├── domain/                      # Entidades JPA
│   ├── Ticket.java
│   ├── Advisor.java
│   ├── Message.java
│   └── AuditEvent.java
│
├── dto/                         # Data Transfer Objects
│   ├── TicketRequest.java
│   ├── TicketResponse.java
│   └── DashboardResponse.java
│
├── enums/                       # Enumeraciones
│   ├── QueueType.java
│   ├── TicketStatus.java
│   ├── AdvisorStatus.java
│   └── MessageTemplate.java
│
├── scheduler/                   # Tareas programadas
│   ├── MessageScheduler.java
│   └── PositionRecalculator.java
│
└── exception/                   # Manejo de errores
    ├── TicketActivoException.java
    └── GlobalExceptionHandler.java
```

---

## 6. Componentes Clave

### 6.1 TicketService

**Responsabilidades:**
- Crear tickets con validación RN-001 (unicidad)
- Calcular posición y tiempo estimado (RN-010)
- Generar número de ticket (RN-005, RN-006)
- Programar 3 mensajes en Redis

**Métodos principales:**
```java
TicketResponse crearTicket(TicketRequest request)
TicketResponse consultarTicket(UUID codigoReferencia)
void recalcularPosiciones(QueueType queueType)
```

### 6.2 AssignmentService

**Responsabilidades:**
- Asignar tickets automáticamente (RN-002, RN-003, RN-004)
- Balanceo de carga entre ejecutivos
- Actualizar estados de ticket y advisor

**Métodos principales:**
```java
void asignarAutomaticamente()
void liberarEjecutivo(Long advisorId)
```

### 6.3 TelegramService

**Responsabilidades:**
- Enviar mensajes vía Telegram Bot API
- Implementar reintentos con backoff exponencial (RN-007, RN-008)
- Actualizar estado de mensajes

**Métodos principales:**
```java
void enviarMensaje(Message mensaje)
void procesarColaReintentos()
```

### 6.4 MessageScheduler

**Responsabilidades:**
- Procesar cola de mensajes en Redis cada 5 segundos
- Ejecutar reintentos con backoff exponencial
- Marcar mensajes como FALLIDO después de 3 reintentos

**Configuración:**
```java
@Scheduled(fixedDelay = 5000)
public void procesarMensajesPendientes()
```

### 6.5 QueueService

**Responsabilidades:**
- Gestionar estadísticas de colas (RF-005)
- Calcular métricas en tiempo real
- Generar alertas de colas críticas

**Métodos principales:**
```java
List<QueueStats> obtenerEstadoColas()
QueueStats obtenerEstadoCola(QueueType type)
```

---

## 7. Flujos de Datos

### 7.1 Cache Strategy (Redis)

**Datos en Cache:**
- Posiciones de tickets (TTL: 30s)
- Estado de ejecutivos (TTL: 10s)
- Estadísticas de colas (TTL: 5s)

**Cola de Mensajes:**
- Key: `telegram:messages:pending`
- Estructura: Lista (LPUSH/RPOP)
- Contenido: JSON con datos del mensaje

### 7.2 Persistencia (PostgreSQL)

**Escritura:**
- Tickets: INSERT al crear, UPDATE al asignar/completar
- Mensajes: INSERT al programar, UPDATE al enviar
- Auditoría: INSERT only (inmutable)

**Lectura:**
- Consultas de tickets: por UUID o número
- Dashboard: agregaciones en tiempo real
- Auditoría: consultas por filtros

---

## 8. Configuración de Entorno

### 8.1 application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/ticketero
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  data:
    redis:
      host: redis
      port: 6379

telegram:
  bot:
    token: ${TELEGRAM_BOT_TOKEN}
    username: ${TELEGRAM_BOT_USERNAME}

scheduler:
  message-processor:
    fixed-delay: 5000
  position-recalculator:
    fixed-delay: 10000
```

### 8.2 docker-compose.yml

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
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_USER: admin
      DB_PASSWORD: ${DB_PASSWORD}
      TELEGRAM_BOT_TOKEN: ${TELEGRAM_BOT_TOKEN}
      TELEGRAM_BOT_USERNAME: ${TELEGRAM_BOT_USERNAME}
    depends_on:
      - postgres
      - redis

volumes:
  postgres_data:
  redis_data:
```

---

## 9. Endpoints REST API

### 9.1 Tickets (Público)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | /api/tickets | Crear ticket |
| GET | /api/tickets/{uuid} | Consultar ticket |
| GET | /api/tickets/{uuid}/position | Consultar posición |

### 9.2 Admin (Protegido)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/admin/dashboard | Dashboard completo |
| GET | /api/admin/queues | Estado de colas |
| GET | /api/admin/advisors | Lista ejecutivos |
| PUT | /api/admin/advisors/{id}/status | Cambiar estado ejecutivo |
| GET | /api/admin/audit/ticket/{uuid} | Auditoría de ticket |

### 9.3 WebSocket

| Endpoint | Descripción |
|----------|-------------|
| WS /ws/dashboard | Actualizaciones tiempo real |

---

## 10. Seguridad

### 10.1 Autenticación
- Endpoints públicos: /api/tickets/**
- Endpoints admin: Spring Security + JWT
- WebSocket: Token en handshake

### 10.2 Validaciones
- DTO validation con Bean Validation
- Sanitización de inputs
- Rate limiting en Redis

---

## 11. Monitoreo y Observabilidad

### 11.1 Métricas (Spring Actuator)
- /actuator/health
- /actuator/metrics
- /actuator/prometheus

### 11.2 Logs
- Nivel INFO para operaciones normales
- Nivel ERROR para fallos críticos
- Formato JSON para agregación

### 11.3 Alertas
- Cola crítica (>15 tickets)
- Mensajes fallidos consecutivos
- Ejecutivos sin actividad >30min

---

## 12. Decisiones de Arquitectura

### 12.1 ¿Por qué Redis para mensajes?
- **Ventaja**: Persistencia + velocidad + simplicidad
- **Alternativa descartada**: RabbitMQ (over-engineering para MVP)

### 12.2 ¿Por qué Scheduler en lugar de eventos?
- **Ventaja**: Control de backoff exponencial, reintentos predecibles
- **Alternativa descartada**: Event-driven (complejidad innecesaria)

### 12.3 ¿Por qué PostgreSQL en lugar de MongoDB?
- **Ventaja**: Transacciones ACID, relaciones claras, índices eficientes
- **Caso de uso**: Datos estructurados con relaciones 1:N

### 12.4 ¿Por qué WebSocket para dashboard?
- **Ventaja**: Actualizaciones push en tiempo real sin polling
- **Alternativa descartada**: Server-Sent Events (menor soporte navegadores)

---

## 13. Escalabilidad Futura

### 13.1 Horizontal Scaling
- Múltiples instancias de app con load balancer
- Redis como sesión compartida
- PostgreSQL con read replicas

### 13.2 Optimizaciones
- Cache de consultas frecuentes (posiciones)
- Índices compuestos en queries complejas
- Connection pooling (HikariCP)

---

## 14. Cumplimiento de Rule #1

### ✅ Test de los 3 Minutos

**Pregunta 1: ¿Comunica el 80% del valor?**
- ✅ Sí: 3 diagramas core cubren flujo completo

**Pregunta 2: ¿Explicable sin documentación adicional?**
- ✅ Sí: Diagramas con notación simple, sin UML complejo

**Pregunta 3: ¿El código puede explicarse mejor sin diagramas?**
- ❌ No: Diagramas muestran interacciones que código no evidencia

**Pregunta 4: ¿Menos de 10 elementos por diagrama?**
- ✅ Contexto: 5 elementos
- ✅ Secuencia: 8 interacciones
- ✅ ER: 4 tablas principales

### ✅ Límites Cuantitativos

| Aspecto | Límite | Real | Estado |
|---------|--------|------|--------|
| Diagramas totales | 3 | 3 | ✅ |
| Elementos por diagrama | 5-10 | 4-8 | ✅ |
| Niveles de profundidad | 2 | 2 | ✅ |

---

## 15. Comandos de Ejecución

### 15.1 Desarrollo Local

```bash
# Levantar infraestructura
docker-compose up -d postgres redis

# Ejecutar aplicación
./mvnw spring-boot:run

# Ejecutar tests
./mvnw test
```

### 15.2 Producción

```bash
# Build
./mvnw clean package -DskipTests

# Deploy con Docker Compose
docker-compose up -d

# Ver logs
docker-compose logs -f app
```

---

**Versión:** 1.0  
**Última actualización:** Diciembre 2025  
**Estado:** Activa  
**Tiempo de explicación:** ~3 minutos ✅
