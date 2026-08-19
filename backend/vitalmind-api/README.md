# VitalMind API

API REST profesional para **VitalMind IA**, construida sobre el esquema MySQL existente (`vitalmind`), sin modificar su estructura.

## Estado del desarrollo

- [x] Fase 0 — Configuración base (Express, MySQL2 pool, seguridad, Swagger, manejo de errores)
- [x] Fase 1 — Autenticación (registro, login, JWT + refresh token, logout, middlewares de rol)
- [x] Fase 2 — Usuarios (CRUD + perfil: edad, peso, altura, grupo sanguíneo, teléfono, rol)
- [x] Fase 3 — Medicamentos (CRUD + marcar tomado + historial + filtros)
- [x] Fase 4 — Citas (CRUD + próximas/canceladas/completadas + búsqueda por fecha/especialidad)
- [x] Fase 5 — Historial médico (CRUD unificado: enfermedades, alergias, cirugías, vacunas, resultados)
- [x] Fase 6 — Hábitos (agua, sueño, ejercicio, alimentación, meditación)
- [x] Fase 7 — Síntomas (dolor, temperatura, presión, glucosa, peso, FC, ánimo, notas)
- [x] Fase 8 — Notificaciones
- [x] Fase 9 — Dashboard (KPIs y datos para gráficas, 100% en tiempo real desde MySQL, sin mocks)
- [x] Fase 10 — Reportes (PDF / Excel / CSV)
- [x] Fase 11 — Documentación (Swagger, README, colección de Postman)
- [x] Catálogo de los 20 mecanismos de ML (documentación + heurística de prioridad de síntomas)
- [ ] Fase 12 — Dashboard frontend con gráficas dinámicas (Chart.js/Recharts) — siguiente entrega

## Requisitos

- Node.js 18+
- MySQL 8+ / MariaDB 10.11+ con la base de datos `vitalmind` ya importada (`DB_VitalMind.sql`)

## Instalación

```bash
npm install
cp .env.example .env
# Edita .env con tus credenciales reales de MySQL
npm run dev
```

Documentación interactiva: `http://localhost:4000/api/docs`

## Módulos y endpoints

### Auth (`/api/auth`)
| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `/register` | Registrar usuario | No |
| POST | `/login` | Iniciar sesión | No |
| POST | `/refresh` | Renovar access token | No |
| POST | `/logout` | Cerrar sesión | No |
| GET | `/me` | Perfil propio | Sí |

### Usuarios (`/api/users`)
| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| GET | `/` | Listar (con filtros role/status/search, paginado) | admin/caregiver |
| GET | `/:id` | Detalle | Sí |
| PUT/PATCH | `/:id` | Actualizar perfil (propio o cualquiera si admin) | Sí |
| POST | `/me/change-password` | Cambiar contraseña | Sí |
| DELETE | `/:id` | Eliminar | admin |

### Medicamentos (`/api/medications`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Listar (filtros type/taken, paginado) |
| GET | `/:id` | Detalle |
| GET | `/:id/logs` | Historial de tomas |
| POST | `/` | Crear |
| PUT | `/:id` | Editar |
| PATCH | `/:id/taken` | Marcar como tomado/no tomado |
| DELETE | `/:id` | Eliminar |

### Citas (`/api/appointments`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Listar (filtros status/specialty/fromDate/toDate) |
| GET | `/:id` | Detalle |
| POST | `/` | Crear |
| PUT | `/:id` | Editar (incluye cambio de status) |
| DELETE | `/:id` | Eliminar |

### Historial médico (`/api/medical-history`)
CRUD unificado por `category`: `diseases`, `allergies`, `medications`, `surgeries`, `consultations`, `vaccines`, `results`.

### Hábitos (`/api/habits`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/?fromDate&toDate` | Rango de registros |
| GET | `/:date` | Registro de un día |
| POST | `/` | Crear/actualizar (upsert) del día |
| POST | `/increment` | Incrementar un hábito puntual |
| DELETE | `/:date` | Eliminar registro del día |

### Síntomas (`/api/symptoms`)
CRUD completo. Cada respuesta incluye `priorityScore`/`priorityLevel`, una heurística (no modelo entrenado) del mecanismo #1 del catálogo de ML.

### Notificaciones (`/api/notifications`)
CRUD + `/unread-count`.

### Dashboard (`/api/dashboard`) — todo en tiempo real desde MySQL
```
GET /statistics                  → KPIs generales
GET /users-by-role
GET /users-by-status
GET /medications-by-type
GET /medications-taken-by-day?days=14
GET /appointments-by-specialty
GET /appointments-status
GET /mood-statistics
GET /pain-statistics?days=30
GET /glucose-history?days=30
GET /blood-pressure?days=30
GET /temperature-history?days=30
GET /water-consumption?days=7
GET /sleep-history?days=7
GET /physical-activity?days=7
GET /habits-weekly?days=7
```

### Reportes (`/api/reports`) — admin/caregiver
```
GET /:entity/csv
GET /:entity/excel
GET /:entity/pdf
```
`entity` ∈ `users, medications, appointments, symptoms, medicalHistory`

### Mecanismos de ML (`/api/mechanisms`)
```
GET /            → catálogo completo (10 supervisados + 10 no supervisados)
GET /:id         → detalle de un mecanismo (1-20)
```
**Nota:** este catálogo es documentación de referencia del proyecto académico. Los modelos entrenados (Random Forest, K-Means, etc.) requieren un pipeline aparte (Python/scikit-learn); esta API expone una heurística SQL/JS del mecanismo #1 en `/api/symptoms`.

### Integración con microservicio ML (`/api/ml`)
Implementa el lado "Backend principal" del `Contrato de integración del microservicio ML`.
```
POST /analyze        → arma el payload desde users/habit_logs/symptom_logs y llama a {ML_SERVICE_URL}/api/v1/analyze
GET  /health          → proxy de {ML_SERVICE_URL}/health
GET  /models/info      → proxy de {ML_SERVICE_URL}/api/v1/models/info
```
**Importante — migración requerida:** el contrato marca `stress_level`, `energy_level` y `sleep_quality` como campos indispensables (sección 9.1), pero no existían en `symptom_logs`. Se agregaron de forma aditiva (NULL-ables, no rompen nada existente) en `migrations/001_add_ml_contract_fields.sql`. Corre esa migración antes de usar `/api/ml/analyze`:
```bash
mysql -u root -p vitalmind < migrations/001_add_ml_contract_fields.sql
```
El endpoint de síntomas (`POST /api/symptoms`) ya acepta `stressLevel`, `energyLevel`, `sleepQuality` (escala 1-10) para poder registrarlos.

El Backend nunca calcula BMI, nunca imputa datos y nunca corre los modelos — eso es responsabilidad exclusiva del microservicio (secciones 2 y 18.1 del contrato). Este módulo solo arma el JSON, llama, y traduce sus respuestas (200/422/500) sin exponer detalles internos.

## Arquitectura

```
src/
├── config/         # env, conexión MySQL2
├── controllers/     # Manejo de req/res, delegan a services
├── middleware/       # auth, roles, validación, errores
├── routes/          # Definición de endpoints por módulo
├── services/        # Lógica de negocio
├── repositories/     # Acceso a datos (SQL puro vía mysql2)
├── utils/           # ApiError, asyncHandler, jwt
├── validations/      # Reglas de express-validator
├── dashboard/        # Queries agregadas para KPIs/gráficas + catálogo ML
├── docs/            # Configuración de Swagger
├── app.js           # Configuración de Express
└── server.js         # Punto de entrada
```

## Seguridad

- `bcrypt` (10 salt rounds) para contraseñas.
- Refresh tokens con hash SHA-256 y rotación en cada uso.
- `helmet`, `cors` restringido por `.env`, `morgan` para logs.
- Manejo centralizado de errores, sin fuga de detalles internos en producción.
- Autorización por rol (`admin`, `patient`, `caregiver`) vía middleware.

## Postman

Colección incluida en `docs/VitalMind.postman_collection.json`. Importa en Postman y define la variable `baseUrl = http://localhost:4000/api` y `accessToken` tras hacer login.
