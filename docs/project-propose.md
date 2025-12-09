
# Propuesta Técnica - Sistema Ticketero

## Arquitectura: Monolito Modular

**Justificación (Test 3 minutos):**
- 1 aplicación Spring Boot con 4 módulos internos
- PostgreSQL + Redis para cache
- Telegram Bot API como única integración externa
- Docker Compose para deployment inicial

## Stack Tecnológico

```
Java 21 + Spring Boot 3.2
PostgreSQL 15 (persistencia)
Redis 7 (cache + sessions)
Telegram Bot API (notificaciones)
Docker + Docker Compose
```

## Módulos del Sistema (4 core)

### 1. ticket-core
- Crear/consultar tickets
- Calcular posiciones y tiempos
- Gestionar colas FIFO

### 2. asignacion-service  
- Asignar tickets a ejecutivos
- Balanceo de carga automático
- Liberar ejecutivos

### 3. notificacion-service
- Envío vía Telegram Bot
- Reintentos automáticos (30s, 60s, 120s)
- Templates de mensajes

### 4. dashboard-service
- WebSocket para tiempo real
- Métricas básicas
- Alertas críticas

## Modelo de Datos (5 tablas máximo)

```sql
-- Tabla principal
tickets (id, rut_cliente, tipo_cola, estado, posicion, tiempo_estimado, created_at)

-- Gestión operativa  
ejecutivos (id, nombre, modulo, disponible, tipo_cola)
colas (tipo, tiempo_promedio, prioridad, ejecutivos_activos)

-- Auditoría simple
eventos (id, ticket_id, tipo_evento, timestamp, datos_json)

-- Configuración
sucursales (id, nombre, configuracion_json)
```

## API REST Minimalista

```java
// Endpoints esenciales
POST /api/tickets              // Crear ticket
GET  /api/tickets/{id}         // Consultar estado  
PUT  /api/ejecutivos/{id}/libre // Liberar ejecutivo
GET  /api/dashboard            // Métricas dashboard
```

## Flujo de Datos Simplificado

```
Cliente → Controller → Service → Repository → DB
                   ↓
            NotificationService → Telegram API
                   ↓  
            DashboardService → WebSocket → Frontend
```

## Configuración de Deployment

### docker-compose.yml
```yaml
services:
  app:
    image: ticketero:latest
    ports: ["8080:8080"]
    depends_on: [postgres, redis]
    
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ticketero
      
  redis:
    image: redis:7-alpine
```

## Estrategia de Escalabilidad

### Fase 1 (Piloto): 1 sucursal
- 1 instancia aplicación
- PostgreSQL single instance
- Redis local

### Fase 2 (Expansión): 5 sucursales  
- 2 instancias aplicación + Load Balancer
- PostgreSQL con read replica
- Redis cluster

### Fase 3 (Nacional): 50+ sucursales
- Auto-scaling en ECS/EKS
- RDS Multi-AZ + ElastiCache
- CDN para dashboard

## Métricas de Performance

```java
// Targets según requerimientos
- Crear ticket: < 3 segundos
- Calcular posición: < 1 segundo  
- Envío Telegram: < 5 segundos
- Update dashboard: cada 5 segundos
```

## Seguridad Básica

- Encriptación AES-256 para RUT/teléfonos
- HTTPS obligatorio
- Rate limiting: 10 req/min por IP
- Logs de auditoría en tabla eventos

## Plan de Implementación (3 sprints)

### Sprint 1: Core + DB
- Modelo de datos
- CRUD tickets básico
- Cálculo de posiciones

### Sprint 2: Notificaciones + Asignación
- Integración Telegram
- Lógica de asignación automática
- Reintentos y fallbacks

### Sprint 3: Dashboard + Deployment
- WebSocket dashboard
- Métricas tiempo real
- Docker + CI/CD

## Estimación de Esfuerzo

- **Desarrollo:** 6-8 semanas (2 developers)
- **Testing:** 2 semanas
- **Deployment:** 1 semana
- **Total:** 9-11 semanas

## Riesgos Técnicos

1. **Latencia Telegram API:** Implementar circuit breaker
2. **Concurrencia en asignaciones:** Locks optimistas en DB
3. **Escalabilidad dashboard:** WebSocket con Redis pub/sub

Esta propuesta cumple el "Test de 3 minutos": arquitectura simple, componentes claros, y enfoque pragmático sin sobre-ingeniería.