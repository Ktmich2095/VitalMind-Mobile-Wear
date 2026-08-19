# VitalMind API — arreglos aplicados (resumen)

Este paquete es tu proyecto `vitalmind-api` con TODO lo que hemos corregido
hasta ahora ya integrado en el código. Solo te faltan pasos de configuración
de tu lado (que no puedo hacer yo porque corren en tu máquina/tu MySQL).

## Orden exacto para dejarlo funcionando desde cero

### 1) Instala dependencias
```powershell
npm install
pip install pymysql
```

### 2) Configura tu .env
Copia `.env.example` a `.env` y pon tu password real de MySQL. Asegúrate de
que `PORT` no choque con otro proceso que ya esté corriendo (ej. si tienes
`Back_VitalMind` corriendo en 4000, usa 4001 aquí).

### 3) Corre los scripts SQL, EN ESTE ORDEN
```powershell
mysql -u root -p vitalmind -e "source fix_schema_vitalmind_v3.sql"
mysql -u root -p vitalmind -e "source fix_add_gender.sql"
```
Esto crea/completa las tablas `habit_logs`, `symptom_logs`,
`medical_history_items`, `medication_logs`, agrega columnas que faltaban
(`water`, `stress_level`, `days_duration`, `blood_type`, etc.) y la columna
`gender`. Es 100% aditivo: no borra nada de lo que ya tengas.

### 4) (Opcional) Genera datos de prueba
```powershell
# Todo de una vez (5000 usuarios + datos relacionados):
python generate_vitalmind_data.py

# O generación puntual, cualquier cantidad, cualquier tabla:
python vitalmind_seed.py --list
python vitalmind_seed.py users --count 200 --set gender=Mujer
python vitalmind_seed.py habit_logs --count 100 --user-id 18
```

### 4.1) (Opcional) Si tus gráficas de citas/síntomas salen todas parejas
Esto pasa si generaste los datos con distribución uniforme (todas las
categorías con la misma cantidad, que se ve poco realista). Este script
borra esos registros sintéticos y los regenera con proporciones reales
(no parejas): más citas completadas que canceladas, "Medicina general"
más común que las demás especialidades, estado de ánimo en forma de
campana, etc. Te muestra la distribución antes y después en la terminal.
```powershell
python rebalance_appointments_symptoms.py
```

### 5) Levanta la API
```powershell
npm run dev
```

### 6) Abre el dashboard
`http://localhost:<tu-puerto>/dashboard/` — recarga con Ctrl+Shift+R la
primera vez.

---

## Qué se corrigió en el código (ya viene incluido, no tienes que hacer nada)

- **`dashboard/index.html`**: Chart.js ya no depende de un CDN externo —
  se sirve localmente desde `dashboard/vendor/chart.umd.js`. Antes fallaba
  con "Chart is not defined" si no había internet.
- **Gráficas de "Usuarios por edad" y "Usuarios por género"** agregadas al
  dashboard (necesitan que corras `fix_add_gender.sql`, paso 3).
- **`src/repositories/userRepository.js`**: nuevas funciones
  `countByAgeGroup()` y `countByGender()`.
- **`src/dashboard/dashboardService.js`** y **`src/controllers/dashboardController.js`**:
  nuevos métodos `usersByAge` / `usersByGender`.
- **`src/routes/dashboardRoutes.js`**: nuevas rutas `/users-by-age` y
  `/users-by-gender`.
- **El contrato de ML de tu compañera** (`mlController.js`,
  `mlIntegrationService.js`, `mlRoutes.js`) — revisado, sin cambios, sigue
  intacto y coincide con el documento del contrato.

## Pendiente (NO incluido todavía, requiere que tú decidas)

**Reconciliar `Back_VitalMind` (tu app) con este esquema**, específicamente:
- `medication_logs`: tu app usa `taken_date` + `taken_time`; esta API usa
  `taken_at` (un solo campo).
- Estado de ánimo/estrés/energía/sueño: tu app tiene una tabla separada
  `emotional_logs`; esta API los guarda dentro de `symptom_logs`.

Esto solo importa el día que quieras que las dos apps compartan datos en
tiempo real. Mientras tanto, `vitalmind-api` funciona sola sin problema.
Cuando quieras resolverlo, súbeme los controladores de `Back_VitalMind`
que tocan medicamentos y estado de ánimo, y lo dejamos reconciliado.
