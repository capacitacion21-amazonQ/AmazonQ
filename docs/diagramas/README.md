# Diagramas UML - Sistema Ticketero Digital

Este directorio contiene los diagramas UML en formato PlantUML para el Sistema Ticketero Digital.

## 📋 Diagramas Disponibles

### 1. Diagrama de Contexto (C4 Level 1)
**Archivo:** `01-contexto.puml`  
**Propósito:** Mostrar el sistema en su entorno con actores externos  
**Elementos:** 5 (Cliente, Ejecutivo, Supervisor, Sistema, Telegram API)  
**Tiempo de explicación:** ~1 minuto

### 2. Diagrama de Secuencia - Crear Ticket
**Archivo:** `02-secuencia-crear-ticket.puml`  
**Propósito:** Flujo completo de creación de ticket y notificación  
**Interacciones:** 8 principales  
**Tiempo de explicación:** ~2 minutos

### 3. Modelo de Datos (ER)
**Archivo:** `03-modelo-datos.puml`  
**Propósito:** Estructura de base de datos con relaciones  
**Tablas:** 4 principales (ticket, mensaje, advisor, audit_event)  
**Tiempo de explicación:** ~2 minutos

### 4. Diagrama de Componentes
**Archivo:** `04-componentes.puml`  
**Propósito:** Arquitectura en capas del sistema  
**Capas:** Presentation, Business, Data Access, Scheduled Tasks  
**Tiempo de explicación:** ~2 minutos

### 5. Diagrama de Secuencia - Asignación Automática
**Archivo:** `05-secuencia-asignacion.puml`  
**Propósito:** Flujo de asignación automática de tickets a ejecutivos  
**Interacciones:** 10 principales  
**Tiempo de explicación:** ~2 minutos

### 6. Diagrama de Deployment
**Archivo:** `06-deployment.puml`  
**Propósito:** Arquitectura de contenedores Docker  
**Contenedores:** 3 (app, postgres, redis)  
**Tiempo de explicación:** ~1 minuto

---

## 🛠️ Cómo Visualizar los Diagramas

### Opción 1: PlantUML Online (Más Rápido)
1. Ir a: https://www.plantuml.com/plantuml/uml/
2. Copiar el contenido del archivo `.puml`
3. Pegar en el editor
4. Ver el diagrama renderizado

### Opción 2: VS Code (Recomendado para Desarrollo)
1. Instalar extensión: **PlantUML** (jebbs.plantuml)
2. Instalar Java (requerido por PlantUML)
3. Abrir archivo `.puml`
4. Presionar `Alt+D` para preview

### Opción 3: IntelliJ IDEA
1. Instalar plugin: **PlantUML Integration**
2. Abrir archivo `.puml`
3. Click derecho → "Show PlantUML Diagram"

### Opción 4: Exportar a Imagen
```bash
# Instalar PlantUML CLI
npm install -g node-plantuml

# Generar PNG
puml generate 01-contexto.puml -o output.png

# Generar SVG (mejor calidad)
puml generate 01-contexto.puml -o output.svg
```

---

## 📊 Uso en Presentaciones

### Para PowerPoint/Google Slides:
1. Renderizar diagrama en PlantUML Online
2. Click derecho → "Guardar imagen como PNG"
3. Insertar en presentación

### Para Confluence/Notion:
1. Copiar código PlantUML
2. Usar macro de PlantUML integrado
3. El diagrama se renderiza automáticamente

### Para Documentación Markdown:
```markdown
![Diagrama de Contexto](./diagramas/01-contexto.png)
```

---

## 🎯 Cumplimiento de Rule #1

| Diagrama | Elementos | Tiempo Explicación | Estado |
|----------|-----------|-------------------|--------|
| Contexto | 5 | ~1 min | ✅ |
| Secuencia Crear | 8 | ~2 min | ✅ |
| Modelo Datos | 4 tablas | ~2 min | ✅ |
| Componentes | 3 capas | ~2 min | ✅ |
| Secuencia Asignar | 10 | ~2 min | ✅ |
| Deployment | 3 containers | ~1 min | ✅ |

**Total diagramas:** 6  
**Diagramas core (obligatorios):** 3 (Contexto, Secuencia, ER)  
**Diagramas complementarios:** 3 (Componentes, Asignación, Deployment)

---

## 📝 Convenciones Usadas

### Colores
- **LightBlue:** Componentes del sistema
- **LightGreen:** Actores humanos / Servicios
- **LightYellow:** Repositorios / Datos
- **LightGray:** Sistemas externos

### Notación
- `PK`: Primary Key (rojo)
- `FK`: Foreign Key (azul)
- `<<stereotype>>`: Tipo de componente
- `-->`: Dependencia / Llamada
- `--`: Relación de datos

---

## 🔄 Actualización de Diagramas

**Cuándo actualizar:**
- Cambios en arquitectura de componentes
- Nuevas entidades en modelo de datos
- Modificación de flujos principales

**Quién puede modificar:**
- Arquitecto de solución
- Tech Lead con aprobación

**Proceso:**
1. Editar archivo `.puml`
2. Validar que se renderiza correctamente
3. Actualizar este README si es necesario
4. Commit con mensaje descriptivo

---

## 📚 Referencias

- **PlantUML:** https://plantuml.com/
- **C4 Model:** https://c4model.com/
- **UML Distilled:** Martin Fowler
- **Rule #1:** Ver `../.amazonq/rules/simplicidad-verificable.md`

---

**Versión:** 1.0  
**Última actualización:** Diciembre 2025  
**Mantenedor:** Equipo de Arquitectura
