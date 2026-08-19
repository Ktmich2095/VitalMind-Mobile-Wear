"""
generate_vitalmind_data.py
============================================================================
Generador de datos sinteticos para la base de datos `vitalmind`.

Crea usuarios (con edad, genero, rol, etc.) y datos relacionados realistas:
habitos diarios, registros de sintomas, medicamentos, citas y notificaciones.
Todo se genera con datos FALSOS (nombres, correos) pero con la MISMA
estructura que usan Back_VitalMind y vitalmind-api, para que los dashboards
tengan suficiente volumen de datos para graficar.

No borra nada de tu base: solo hace INSERT. Los usuarios nuevos se generan
con correos @vitalmind.test para que puedas identificarlos y borrarlos
despues facilmente si quieres (ver el DELETE de ejemplo al final).

------------------------------------------------------------------------
COMO USARLO
------------------------------------------------------------------------
1) Instala la unica dependencia necesaria:
     pip install pymysql

2) Ajusta la seccion CONFIGURACION de abajo con los datos de tu MySQL
   (los mismos que usas en el .env de tus backends).

3) Corre:
     python generate_vitalmind_data.py

4) Todos los usuarios sinteticos quedan con la MISMA contraseña de prueba:
     Demo123!
   (ya viene guardada como hash bcrypt valido, compatible con bcryptjs)
============================================================================
"""

import random
import datetime
import pymysql

# ============================================================================
# CONFIGURACION -- ajusta esto a tu entorno
# ============================================================================
DB_HOST = "127.0.0.1"
DB_PORT = 3306
DB_USER = "root"
DB_PASSWORD = "1234"       # <-- pon aqui tu password real de MySQL
DB_NAME = "vitalmind"

NUM_USERS = 5000                    # cuantos usuarios nuevos generar
HABIT_LOG_DAYS = 30                 # dias hacia atras de registro de habitos
SYMPTOM_LOG_DAYS = 30                # dias hacia atras de registro de sintomas
MEDICATION_LOG_DAYS = 14             # dias hacia atras de tomas de medicamento
BATCH_SIZE = 1000                    # tamaño de lote para cada INSERT masivo

# Hash bcrypt valido de la contraseña "Demo123!" (compatible con bcryptjs)
DEMO_PASSWORD_HASH = "$2b$10$Ir5/p5hYcGOjN9UwgwZft.COOZOymQxFOjkUiyquha/f9okPlzhum"

# ============================================================================
# DATOS BASE PARA GENERAR NOMBRES / CATALOGOS REALISTAS
# ============================================================================
NOMBRES_M = [
    "Carlos", "Luis", "Miguel", "Jose", "Juan", "Jorge", "Ricardo", "Fernando",
    "Eduardo", "Alejandro", "Diego", "Roberto", "Francisco", "Manuel", "Pedro",
    "Antonio", "Javier", "Raul", "Sergio", "Arturo", "Daniel", "Angel", "Mario",
]
NOMBRES_F = [
    "Maria", "Ana", "Laura", "Sara", "Marta", "Sofia", "Andrea", "Gabriela",
    "Fernanda", "Alejandra", "Patricia", "Monica", "Claudia", "Veronica",
    "Jennifer", "Karla", "Diana", "Paola", "Lucia", "Adriana", "Beatriz",
]
APELLIDOS = [
    "Garcia", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
    "Perez", "Sanchez", "Ramirez", "Torres", "Flores", "Rivera", "Gomez",
    "Diaz", "Reyes", "Cruz", "Morales", "Ortiz", "Gutierrez", "Chavez",
    "Ramos", "Vazquez", "Castillo", "Jimenez", "Mendoza", "Ruiz", "Aguilar",
]
BLOOD_TYPES = ["O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"]
COLORS = ["#0F766E", "#2563EB", "#8B5CF6", "#F59E0B", "#22C55E", "#EF4444", "#EC4899", "#14B8A6"]
MOODS = ["Muy bien", "Bien", "Regular", "Mal", "Muy mal"]
MED_NAMES = [
    ("Metformina", "850mg", "pastilla"), ("Losartan", "50mg", "pastilla"),
    ("Salbutamol", "100mcg", "inyeccion"), ("Vitamina D", "1000UI", "capsula"),
    ("Omega 3", "1g", "capsula"), ("Paracetamol", "500mg", "pastilla"),
    ("Ibuprofeno", "400mg", "pastilla"), ("Amoxicilina", "500mg", "capsula"),
    ("Loratadina", "10mg", "pastilla"), ("Jarabe para la tos", "15ml", "jarabe"),
]
SPECIALTIES = ["Cardiologia", "Endocrinologia", "Medicina general", "Oftalmologia",
               "Dermatologia", "Nutricion", "Psicologia", "Traumatologia"]
DOCTORS = ["Dr. Martinez Lopez", "Dra. Garcia Ruiz", "Dr. Sanchez Vega",
           "Dra. Torres Ramos", "Dr. Flores Diaz", "Dra. Morales Cruz"]
PLACES = ["Clinica San Rafael", "Hospital Central", "Centro de Salud Norte",
          "Optica Vision Salud", "Consultorio 204", "Hospital ABC"]
CATEGORIES = ["diseases", "allergies", "medications", "surgeries",
              "consultations", "vaccines", "results"]
HISTORY_DESCRIPTIONS = {
    "diseases": ["Hipertension arterial", "Diabetes tipo 2", "Asma", "Migraña cronica"],
    "allergies": ["Alergia a la penicilina", "Alergia al polen", "Alergia a mariscos"],
    "medications": ["Tratamiento con metformina", "Tratamiento con losartan"],
    "surgeries": ["Apendicectomia (2019)", "Cirugia de rodilla (2021)"],
    "consultations": ["Consulta de rutina anual", "Consulta por dolor de espalda"],
    "vaccines": ["Vacuna influenza", "Vacuna COVID-19 refuerzo", "Vacuna tetanos"],
    "results": ["Perfil lipidico dentro de rango", "Glucosa en ayuno: 95 mg/dL"],
}


def month_es(m):
    return ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"][m - 1]


def random_date_label(days_back_max=700):
    d = datetime.date.today() - datetime.timedelta(days=random.randint(0, days_back_max))
    return f"{d.day:02d} {month_es(d.month)} {d.year}", d


def gen_users(n):
    rows = []
    used_emails = set()
    for i in range(n):
        gender = random.choices(["Mujer", "Hombre", "Otro"], weights=[47, 47, 6])[0]
        first = random.choice(NOMBRES_F if gender == "Mujer" else NOMBRES_M) if gender != "Otro" else random.choice(NOMBRES_F + NOMBRES_M)
        last1, last2 = random.sample(APELLIDOS, 2)
        full_name = f"{first} {last1} {last2}"
        email = f"{first.lower()}.{last1.lower()}{i}@vitalmind.test"
        while email in used_emails:
            email = f"{first.lower()}.{last1.lower()}{i}.{random.randint(1,999)}@vitalmind.test"
        used_emails.add(email)

        age = random.choices(
            population=[random.randint(12, 17), random.randint(18, 24), random.randint(25, 34),
                        random.randint(35, 44), random.randint(45, 54), random.randint(55, 64),
                        random.randint(65, 90)],
            weights=[5, 18, 24, 20, 16, 10, 7],
        )[0]

        joined_label, joined_date = random_date_label(700)
        last_active_label = f"{random.randint(1,23):02d}:{random.randint(0,59):02d}" if random.random() < 0.5 else "Hace unos dias"
        status = random.choices(["active", "inactive", "pending"], weights=[70, 20, 10])[0]
        role = random.choices(["patient", "caregiver", "admin"], weights=[92, 7, 1])[0]

        height = round(random.uniform(150, 190), 1)
        weight = round(random.uniform(45, 110), 1)

        rows.append((
            full_name, email, DEMO_PASSWORD_HASH, age, gender,
            joined_label, last_active_label, status,
            random.randint(0, 80), random.randint(0, 40),
            random.choice(COLORS), role,
            random.choice(BLOOD_TYPES), f"222{random.randint(1000000,9999999)}",
            weight, height,
        ))
    return rows


def batched(iterable, size):
    for i in range(0, len(iterable), size):
        yield iterable[i:i + size]


def main():
    conn = pymysql.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASSWORD,
        database=DB_NAME, charset="utf8mb4", autocommit=False,
    )
    cur = conn.cursor()

    print(f"Generando {NUM_USERS} usuarios sinteticos...")
    users = gen_users(NUM_USERS)
    insert_user_sql = """
        INSERT INTO users
          (full_name, email, password_hash, age, gender, joined_label, last_active_label,
           status, registros, consultas, color, role, blood_type, phone, weight_kg, height_cm)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
    """
    for batch in batched(users, BATCH_SIZE):
        cur.executemany(insert_user_sql, batch)
    conn.commit()
    print(f"  -> {len(users)} usuarios insertados.")

    # Recuperar los IDs de los usuarios recien creados (rol patient/caregiver, para datos clinicos)
    cur.execute("SELECT id, role, age FROM users WHERE email LIKE %s", ("%@vitalmind.test",))
    created = cur.fetchall()
    patient_ids = [uid for uid, role, age in created if role in ("patient", "caregiver")]
    print(f"  -> {len(patient_ids)} usuarios elegibles para datos clinicos (paciente/cuidador).")

    # ---------------- medications ----------------
    print("Generando medicamentos...")
    med_rows = []
    user_meds = {}  # user_id -> [medication placeholder index]
    for uid in patient_ids:
        if random.random() < 0.6:
            n_meds = random.randint(1, 3)
            for _ in range(n_meds):
                name, dose, mtype = random.choice(MED_NAMES)
                med_rows.append((
                    uid, name, dose, random.choice(["Diario", "Cada 8h", "Cada 12h", "Semanal", "PRN"]),
                    f"{random.randint(6,22):02d}:00", random.choice(COLORS),
                    random.choice([0, 1]), mtype, random.randint(5, 90),
                ))
    insert_med_sql = """
        INSERT INTO medications (user_id, name, dose, frequency, time_label, color, taken, type, days_duration)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)
    """
    for batch in batched(med_rows, BATCH_SIZE):
        cur.executemany(insert_med_sql, batch)
    conn.commit()
    print(f"  -> {len(med_rows)} medicamentos insertados.")

    medication_rows = []
    if patient_ids:
        placeholders = ",".join(["%s"] * len(patient_ids))
        cur.execute(
            f"SELECT id, user_id FROM medications WHERE user_id IN ({placeholders})",
            patient_ids,
        )
        medication_rows = cur.fetchall()

    # ---------------- medication_logs ----------------
    print("Generando historial de tomas de medicamento...")
    med_log_rows = []
    today = datetime.date.today()
    for med_id, uid in medication_rows:
        for d in range(MEDICATION_LOG_DAYS):
            if random.random() < 0.75:
                day = today - datetime.timedelta(days=d)
                taken_at = datetime.datetime.combine(day, datetime.time(random.randint(6, 22), random.randint(0, 59)))
                med_log_rows.append((med_id, uid, random.choice([0, 1]), taken_at))
    insert_medlog_sql = "INSERT INTO medication_logs (medication_id, user_id, taken, taken_at) VALUES (%s,%s,%s,%s)"
    for batch in batched(med_log_rows, BATCH_SIZE):
        cur.executemany(insert_medlog_sql, batch)
    conn.commit()
    print(f"  -> {len(med_log_rows)} registros de toma de medicamento insertados.")

    # ---------------- appointments ----------------
    print("Generando citas...")
    appt_rows = []
    for uid in patient_ids:
        for _ in range(random.randint(0, 3)):
            future = random.random() < 0.4
            offset = random.randint(1, 60) if future else -random.randint(1, 180)
            appt_date = today + datetime.timedelta(days=offset)
            status = "proxima" if future else random.choice(["completada", "completada", "cancelada"])
            appt_rows.append((
                uid, random.choice(SPECIALTIES), random.choice(DOCTORS),
                appt_date.isoformat(), f"{random.randint(8,18):02d}:{random.choice(['00','30'])}",
                random.choice(PLACES), random.choice(COLORS), status,
            ))
    insert_appt_sql = """
        INSERT INTO appointments (user_id, specialty, doctor, appointment_date, appointment_time, place, color, status)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s)
    """
    for batch in batched(appt_rows, BATCH_SIZE):
        cur.executemany(insert_appt_sql, batch)
    conn.commit()
    print(f"  -> {len(appt_rows)} citas insertadas.")

    # ---------------- habit_logs ----------------
    print("Generando registros de habitos (esto puede tardar un poco)...")
    habit_rows = []
    for uid in patient_ids:
        active_days = random.sample(range(HABIT_LOG_DAYS), k=random.randint(0, HABIT_LOG_DAYS))
        for d in active_days:
            log_date = today - datetime.timedelta(days=d)
            habit_rows.append((
                uid, "water", log_date.isoformat(),
                round(random.uniform(1, 12), 2), round(random.uniform(0, 90), 2),
                round(random.uniform(4, 10), 2), round(random.uniform(1, 5), 2),
                round(random.uniform(0, 30), 2),
                round(random.uniform(1, 12), 2), round(random.uniform(4, 12), 2),
            ))
    insert_habit_sql = """
        INSERT INTO habit_logs (user_id, habit_key, log_date, water, exercise, sleep, nutrition, meditation, value, goal)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
    """
    for batch in batched(habit_rows, BATCH_SIZE):
        cur.executemany(insert_habit_sql, batch)
    conn.commit()
    print(f"  -> {len(habit_rows)} registros de habitos insertados.")

    # ---------------- symptom_logs ----------------
    print("Generando registros de sintomas...")
    symptom_rows = []
    for uid in patient_ids:
        n_logs = random.randint(0, 8)
        for _ in range(n_logs):
            days_ago = random.randint(0, SYMPTOM_LOG_DAYS)
            created_at = datetime.datetime.combine(
                today - datetime.timedelta(days=days_ago),
                datetime.time(random.randint(6, 22), random.randint(0, 59)),
            )
            symptom_rows.append((
                uid, random.randint(0, 10), round(random.uniform(35.5, 39.0), 1),
                random.randint(95, 150), random.randint(60, 100), random.randint(70, 180),
                round(random.uniform(45, 110), 1), random.randint(55, 110),
                random.randint(1, 10), random.randint(1, 10), random.randint(1, 10),
                random.choice(MOODS), None, created_at,
            ))
    insert_symptom_sql = """
        INSERT INTO symptom_logs
          (user_id, pain, temperature, systolic, diastolic, glucose, weight, heart_rate,
           stress_level, energy_level, sleep_quality, mood, notes, created_at)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
    """
    for batch in batched(symptom_rows, BATCH_SIZE):
        cur.executemany(insert_symptom_sql, batch)
    conn.commit()
    print(f"  -> {len(symptom_rows)} registros de sintomas insertados.")

    # ---------------- medical_history_items ----------------
    print("Generando historial medico...")
    history_rows = []
    for uid in patient_ids:
        for _ in range(random.randint(0, 4)):
            cat = random.choice(CATEGORIES)
            desc = random.choice(HISTORY_DESCRIPTIONS[cat])
            history_rows.append((uid, cat, desc))
    insert_history_sql = "INSERT INTO medical_history_items (user_id, category, description) VALUES (%s,%s,%s)"
    for batch in batched(history_rows, BATCH_SIZE):
        cur.executemany(insert_history_sql, batch)
    conn.commit()
    print(f"  -> {len(history_rows)} entradas de historial medico insertadas.")

    # ---------------- notifications ----------------
    print("Generando notificaciones...")
    notif_rows = []
    kinds = ["tip", "reminder", "ai", "alert"]
    titles = {
        "tip": "Consejo de bienestar", "reminder": "Recordatorio", "ai": "Insight de IA", "alert": "Alerta de salud",
    }
    for uid in random.sample(patient_ids, k=min(len(patient_ids), max(1, NUM_USERS // 3))):
        for _ in range(random.randint(1, 4)):
            kind = random.choice(kinds)
            notif_rows.append((
                uid, kind, titles[kind],
                "Notificacion generada automaticamente para pruebas de dashboard.",
                "Hace unos dias", random.choice([0, 1]),
            ))
    insert_notif_sql = """
        INSERT INTO notifications (user_id, kind, title, body, time_label, is_read)
        VALUES (%s,%s,%s,%s,%s,%s)
    """
    for batch in batched(notif_rows, BATCH_SIZE):
        cur.executemany(insert_notif_sql, batch)
    conn.commit()
    print(f"  -> {len(notif_rows)} notificaciones insertadas.")

    cur.close()
    conn.close()
    print("\nListo. Todos los usuarios sinteticos usan la contraseña: Demo123!")
    print("Para borrarlos despues si quieres, puedes correr en MySQL:")
    print("  DELETE FROM users WHERE email LIKE '%@vitalmind.test';")
    print("  (las tablas relacionadas se borran solas por ON DELETE CASCADE / SET NULL)")


if __name__ == "__main__":
    main()
