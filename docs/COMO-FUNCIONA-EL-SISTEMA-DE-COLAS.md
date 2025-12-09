# Cómo Funciona el Sistema de Colas

**Proyecto:** Sistema Ticketero Digital  
**Versión:** 1.0  
**Fecha:** Diciembre 2025

---

## 🎯 Concepto General

El sistema gestiona **4 colas virtuales simultáneas**, cada una con características específicas de prioridad y tiempo de atención. Los tickets se asignan automáticamente a ejecutivos siguiendo reglas de negocio estrictas.

---

## 📊 Las 4 Colas del Sistema

| Cola | Prefijo | Prioridad | Tiempo Promedio | Uso |
|------|---------|-----------|-----------------|-----|
| **GERENCIA** | G | 4 (máxima) | 30 min | Casos especiales, VIP |
| **EMPRESAS** | E | 3 (alta) | 20 min | Clientes corporativos |
| **PERSONAL_BANKER** | P | 2 (media) | 15 min | Productos financieros |
| **CAJA** | C | 1 (baja) | 5 min | Transacciones básicas |

---

## 🔄 Ciclo de Vida de un Ticket

### 1️⃣ **Creación** (Estado: EN_ESPERA)

```
Cliente en terminal → Selecciona tipo de cola → Sistema genera ticket
```

**Ejemplo:**
- Cliente selecciona "Caja"
- Sistema genera: **C05** (5to en cola de Caja)
- Posición: 5
- Tiempo estimado: 25 minutos (5 × 5min)

**Reglas aplicadas:**
- **RN-001**: Valida que cliente no tenga ticket activo
- **RN-005**: Genera número con formato [Prefijo][01-99]
- **RN-010**: Calcula tiempo = posición × tiempo_promedio

---

### 2️⃣ **Pre-aviso** (Estado: PROXIMO)

```
Posición ≤ 3 → Sistema envía notificación → Cliente se acerca
```

**Ejemplo:**
- Ticket C05 avanza a posición 3
- Sistema detecta umbral (RN-012)
- Envía Mensaje 2: "⏰ ¡Pronto será tu turno!"
- Cambia estado a PROXIMO

---

### 3️⃣ **Asignación** (Estado: ATENDIENDO)

```
Ejecutivo disponible → Sistema asigna ticket → Cliente es llamado
```

**Ejemplo:**
- Ejecutivo "María González" termina atención
- Sistema busca siguiente ticket según prioridad
- Asigna C05 a María en módulo 3
- Envía Mensaje 3: "🔔 ¡ES TU TURNO C05! Módulo: 3"

---

### 4️⃣ **Finalización** (Estado: COMPLETADO)

```
Ejecutivo completa atención → Sistema libera ejecutivo → Ciclo termina
```

---

## ⚙️ Algoritmo de Asignación Automática

### Paso a Paso

```
1. TRIGGER: Ejecutivo cambia a AVAILABLE
   ↓
2. BUSCAR TICKETS: Filtrar todos EN_ESPERA
   ↓
3. ORDENAR POR PRIORIDAD:
   - GERENCIA (4) primero
   - EMPRESAS (3)
   - PERSONAL_BANKER (2)
   - CAJA (1) último
   ↓
4. DENTRO DE MISMA PRIORIDAD: Orden FIFO (más antiguo primero)
   ↓
5. SELECCIONAR EJECUTIVO:
   - Que pueda atender ese tipo de cola
   - Con menor assignedTicketsCount (balanceo)
   ↓
6. ASIGNAR:
   - Ticket → ATENDIENDO
   - Ejecutivo → BUSY
   - Incrementar contador
   ↓
7. NOTIFICAR:
   - Cliente vía Telegram
   - Ejecutivo en su terminal
```

---

## 🎲 Ejemplos Prácticos

### Ejemplo 1: Cola Simple (Solo CAJA)

**Situación inicial:**
```
Cola CAJA:
- C01 (creado 10:00) → EN_ESPERA
- C02 (creado 10:05) → EN_ESPERA
- C03 (creado 10:10) → EN_ESPERA

Ejecutivos:
- María (módulo 1) → AVAILABLE
```

**Proceso:**
1. Sistema detecta María AVAILABLE
2. Busca tickets EN_ESPERA en CAJA
3. Selecciona C01 (más antiguo, FIFO)
4. Asigna C01 a María
5. C01 → ATENDIENDO, María → BUSY
6. C02 y C03 recalculan posiciones (ahora 1 y 2)

---

### Ejemplo 2: Múltiples Colas con Prioridad

**Situación inicial:**
```
Tickets EN_ESPERA:
- C01 (CAJA, prioridad 1, creado 09:00)
- P01 (PERSONAL_BANKER, prioridad 2, creado 09:15)
- G01 (GERENCIA, prioridad 4, creado 09:30)

Ejecutivos:
- Juan (puede atender todas) → AVAILABLE
```

**Proceso:**
1. Sistema detecta Juan AVAILABLE
2. Busca tickets EN_ESPERA
3. Ordena por prioridad:
   - G01 (prioridad 4) ← GANA
   - P01 (prioridad 2)
   - C01 (prioridad 1)
4. Asigna G01 a Juan (aunque es el más nuevo)
5. C01 y P01 siguen esperando

**Conclusión:** La prioridad de cola supera el orden de llegada.

---

### Ejemplo 3: Balanceo de Carga

**Situación inicial:**
```
Tickets EN_ESPERA:
- P01, P02, P03 (todos PERSONAL_BANKER)

Ejecutivos AVAILABLE:
- María (assignedTicketsCount = 10)
- Juan (assignedTicketsCount = 5)
- Ana (assignedTicketsCount = 8)
```

**Proceso:**
1. Sistema busca siguiente ticket: P01
2. Filtra ejecutivos AVAILABLE que atienden PERSONAL_BANKER
3. Ordena por assignedTicketsCount:
   - Juan (5) ← GANA
   - Ana (8)
   - María (10)
4. Asigna P01 a Juan
5. Juan.assignedTicketsCount = 6

**Conclusión:** Distribuye carga equitativamente.

---

### Ejemplo 4: Ejecutivo Especializado

**Situación inicial:**
```
Tickets EN_ESPERA:
- G01 (GERENCIA)
- C01 (CAJA)

Ejecutivos AVAILABLE:
- María (puede: CAJA, PERSONAL_BANKER)
- Juan (puede: GERENCIA, EMPRESAS)
```

**Proceso:**
1. Sistema busca siguiente ticket: G01 (prioridad 4)
2. Filtra ejecutivos que pueden atender GERENCIA
3. Solo Juan califica
4. Asigna G01 a Juan
5. María NO recibe asignación (no puede atender GERENCIA)
6. Cuando Juan termine, María recibirá C01

---

## 📈 Recálculo de Posiciones

### Trigger: Cada vez que un ticket cambia de estado

**Ejemplo:**

**Estado inicial:**
```
Cola EMPRESAS:
- E01 (posición 1) → EN_ESPERA
- E02 (posición 2) → EN_ESPERA
- E03 (posición 3) → EN_ESPERA
- E04 (posición 4) → EN_ESPERA
```

**Evento:** E01 es asignado (cambia a ATENDIENDO)

**Recálculo automático:**
```
- E01 (posición 0) → ATENDIENDO
- E02 (posición 1) → EN_ESPERA (antes era 2)
- E03 (posición 2) → EN_ESPERA (antes era 3)
- E04 (posición 3) → EN_ESPERA (antes era 4) ← Ahora PROXIMO
```

**Resultado:**
- E04 recibe Mensaje 2 (pre-aviso) porque posición ≤ 3
- Tiempos estimados se actualizan automáticamente

---

## 🔢 Cálculo de Tiempo Estimado

### Fórmula Simple

```
tiempo_estimado = posición_en_cola × tiempo_promedio_cola
```

### Ejemplos por Cola

**CAJA (5 min promedio):**
- Posición 1 → 5 min
- Posición 5 → 25 min
- Posición 10 → 50 min

**PERSONAL_BANKER (15 min promedio):**
- Posición 1 → 15 min
- Posición 3 → 45 min
- Posición 5 → 75 min

**EMPRESAS (20 min promedio):**
- Posición 1 → 20 min
- Posición 4 → 80 min

**GERENCIA (30 min promedio):**
- Posición 1 → 30 min
- Posición 3 → 90 min

---

## 🚨 Casos Especiales

### Caso 1: No hay ejecutivos disponibles

```
Situación: Todos los ejecutivos están BUSY u OFFLINE
Comportamiento: Tickets permanecen EN_ESPERA
Acción: Sistema espera hasta que alguien se libere
```

### Caso 2: Cliente con ticket activo intenta crear otro

```
Situación: Cliente tiene P05 EN_ESPERA
Intento: Crear ticket en CAJA
Resultado: HTTP 409 Conflict
Mensaje: "Ya tienes un ticket activo: P05"
```

### Caso 3: Cola crítica (>15 esperando)

```
Situación: Cola PERSONAL_BANKER tiene 16 tickets EN_ESPERA
Acción automática:
- Sistema genera alerta
- Notifica supervisor en dashboard
- Alerta: "Cola PERSONAL_BANKER crítica: 16 tickets esperando"
```

### Caso 4: Cliente sin teléfono

```
Situación: Cliente crea ticket sin proporcionar teléfono
Comportamiento:
- Ticket se crea normalmente
- NO se programan mensajes Telegram
- Cliente debe consultar estado manualmente
```

---

## 🔄 Flujo Completo Integrado

### Escenario Real: Día Normal en Sucursal

**09:00 - Apertura**
```
- 3 ejecutivos cambian a AVAILABLE
- Sistema listo para recibir tickets
```

**09:05 - Primeros clientes**
```
- Cliente A crea C01 (CAJA) → posición 1, espera 5 min
- Cliente B crea P01 (PERSONAL_BANKER) → posición 1, espera 15 min
- Cliente C crea C02 (CAJA) → posición 2, espera 10 min
```

**09:06 - Primera asignación**
```
- Ejecutivo María (AVAILABLE) recibe C01
- C01 → ATENDIENDO
- C02 recalcula: ahora posición 1, espera 5 min
- Cliente A recibe: "🔔 ¡ES TU TURNO C01! Módulo: 1"
```

**09:11 - María termina con C01**
```
- C01 → COMPLETADO
- María → AVAILABLE
- Sistema asigna automáticamente C02 a María
- Cliente C recibe: "🔔 ¡ES TU TURNO C02! Módulo: 1"
```

**09:15 - Llega cliente VIP**
```
- Cliente D crea G01 (GERENCIA) → posición 1, espera 30 min
- Hay 5 tickets EN_ESPERA en otras colas
```

**09:16 - Ejecutivo Juan se libera**
```
- Sistema busca siguiente ticket
- Encuentra G01 (prioridad 4) aunque hay tickets más antiguos
- Asigna G01 a Juan (prioridad supera FIFO)
- Cliente D recibe notificación inmediata
```

**10:00 - Cola PERSONAL_BANKER crece**
```
- 16 tickets EN_ESPERA en PERSONAL_BANKER
- Sistema genera alerta automática
- Supervisor ve alerta en dashboard
- Supervisor puede asignar más ejecutivos a esa cola
```

---

## 📊 Métricas en Tiempo Real

### Dashboard del Supervisor

**Resumen General:**
```
- Tickets activos: 23
- Tickets completados hoy: 87
- Clientes esperando: 18
- Tiempo promedio real: 17 min
```

**Por Cola:**
```
CAJA:
- Esperando: 8
- Ejecutivos disponibles: 2
- Tiempo promedio: 6 min

PERSONAL_BANKER:
- Esperando: 7
- Ejecutivos disponibles: 3
- Tiempo promedio: 14 min

EMPRESAS:
- Esperando: 2
- Ejecutivos disponibles: 1
- Tiempo promedio: 19 min

GERENCIA:
- Esperando: 1
- Ejecutivos disponibles: 1
- Tiempo promedio: 28 min
```

---

## 🎯 Reglas de Negocio Clave

### RN-002: Prioridad de Colas
```
GERENCIA (4) > EMPRESAS (3) > PERSONAL_BANKER (2) > CAJA (1)
```

### RN-003: FIFO Dentro de Cola
```
Dentro de misma prioridad: más antiguo primero
```

### RN-004: Balanceo de Carga
```
Seleccionar ejecutivo con menor assignedTicketsCount
```

### RN-012: Umbral de Pre-aviso
```
Si posición ≤ 3 → Enviar Mensaje 2 (pre-aviso)
```

---

## 💡 Ventajas del Sistema

1. **Justicia**: FIFO dentro de cada cola
2. **Priorización**: Casos urgentes primero
3. **Eficiencia**: Balanceo automático de carga
4. **Transparencia**: Cliente sabe su posición y tiempo
5. **Movilidad**: Cliente puede salir de sucursal
6. **Automatización**: Sin intervención manual

---

## 🔧 Tecnología Subyacente

### PostgreSQL
- Almacena tickets, ejecutivos, mensajes
- Queries con ORDER BY para FIFO y prioridad
- Transacciones ACID para consistencia

### Redis
- Cache de posiciones (TTL 30s)
- Cola de mensajes Telegram
- Estadísticas en tiempo real

### Spring Scheduler
- Recalcula posiciones cada 10s
- Procesa mensajes cada 5s
- Detecta colas críticas

---

**Versión:** 1.0  
**Última actualización:** Diciembre 2025  
**Tiempo de lectura:** ~8 minutos
