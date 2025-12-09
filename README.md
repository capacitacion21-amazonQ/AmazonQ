# Sistema Ticketero Digital

Sistema de gestión de tickets para atención en sucursales bancarias con notificaciones en tiempo real vía Telegram.

## 📋 Descripción

Sistema digital que moderniza la experiencia de atención presencial mediante:
- Digitalización del proceso de tickets
- Notificaciones automáticas vía Telegram
- Movilidad del cliente durante la espera
- Asignación automática a ejecutivos
- Panel de monitoreo en tiempo real

## 🎯 Beneficios Esperados

- Mejora de NPS de 45 a 65 puntos
- Reducción de abandonos de 15% a 5%
- Incremento de 20% en tickets atendidos
- Trazabilidad completa

## 📁 Estructura del Proyecto

```
.
├── .amazonq/
│   └── rules/              # Reglas para Amazon Q
├── docs/
│   ├── project-requirements.md
│   ├── REQUERIMIENTOS-FUNCIONALES.md
│   └── propuesta-tecnica.md
└── README.md
```

## 🛠️ Stack Tecnológico Propuesto

- Java 21
- Spring Boot 3.2
- PostgreSQL 15
- Redis 7
- Telegram Bot API
- Docker + Docker Compose

## 📖 Documentación

- **Requerimientos de Negocio:** `docs/project-requirements.md`
- **Requerimientos Funcionales:** `docs/REQUERIMIENTOS-FUNCIONALES.md` (51 escenarios Gherkin)
- **Propuesta Técnica:** `docs/propuesta-tecnica.md`

## 🚀 Fases de Implementación

### Fase 1: Piloto (1 sucursal)
- 500-800 tickets/día
- 1 instancia aplicación

### Fase 2: Expansión (5 sucursales)
- 2,500-3,000 tickets/día
- Load balancer + replicas

### Fase 3: Nacional (50+ sucursales)
- 25,000+ tickets/día
- Auto-scaling en cloud

## 📊 Requerimientos Funcionales

- **RF-001:** Crear Ticket Digital
- **RF-002:** Enviar Notificaciones Automáticas
- **RF-003:** Calcular Posición y Tiempo Estimado
- **RF-004:** Asignar Ticket a Ejecutivo Automáticamente
- **RF-005:** Gestionar Múltiples Colas
- **RF-006:** Consultar Estado del Ticket
- **RF-007:** Panel de Monitoreo para Supervisor
- **RF-008:** Registrar Auditoría de Eventos

## 🔒 Seguridad

- Encriptación AES-256 para datos sensibles
- HTTPS obligatorio
- Rate limiting
- Auditoría completa de eventos

## 📝 Licencia

Proyecto propietario - Institución Financiera

## 👥 Equipo

- Analista: Amazon Q Developer
- Cliente: Institución Financiera
- Versión: 1.0
- Fecha: Diciembre 2025
