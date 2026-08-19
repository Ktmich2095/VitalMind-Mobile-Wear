"""
vitalmind_seed.py
============================================================================
Generador FLEXIBLE de datos para la base de datos `vitalmind`.

A diferencia de generate_vitalmind_data.py (que llena todo de una vez),
esta herramienta te deja generar la cantidad EXACTA que quieras, en la
tabla que quieras, especificando campos o dejando que se generen solos.

No borra nada: solo hace INSERT.

------------------------------------------------------------------------
INSTALACION
------------------------------------------------------------------------
pip install pymysql

Ajusta DB_HOST / DB_USER / DB_PASSWORD / DB_NAME abajo (mismos datos
que usas en el .env de tus backends).

------------------------------------------------------------------------
EJEMPLOS DE USO (exactamente los que pidieron)
------------------------------------------------------------------------
# 30 alergias diferentes para pacientes (usuarios al azar)
python vitalmind_seed.py medical_history_items --count 30 --set category=allergies

# 200 usuarios mujer
python vitalmind_seed.py users --count 200 --set gender=Mujer

# 7 usuarios admin
python vitalmind_seed.py users --count 7 --set role=admin

# 100 dias de habitos completos para el usuario con id 18
python vitalmind_seed.py habit_logs --count 100 --user-id 18

# 1, 10, 100, 1000... la cantidad que sea, en cualquier tabla soportada:
python vitalmind_seed.py medications --count 1000 --set type=pastilla
python vitalmind_seed.py symptom_logs --count 500 --user-id 18 --set mood="Muy bien"
python vitalmind_seed.py appointments --count 50 --set status=proxima
python vitalmind_seed.py notifications --count 300 --set kind=alert

# Ver que tablas y campos soporta:
python vitalmind_seed.py --list
============================================================================
"""

import argparse
import datetime
import random
import sys
import pymysql

# ============================================================================
# CONFIGURACION -- ajusta esto a tu entorno
# ============================================================================
DB_HOST = "127.0.0.1"
DB_PORT = 3306
DB_USER = "root"
DB_PASSWORD = "1234"     # <-- tu password real de MySQL
DB_NAME = "vitalmind"

DEMO_PASSWORD_HASH = "$2b$10$Ir5/p5hYcGOjN9UwgwZft.COOZOymQxFOjkUiyquha/f9okPlzhum"  # "Demo123!"

# ============================================================================
# CATALOGOS PARA GENERAR VALORES REALISTAS POR DEFECTO
# ============================================================================
NOMBRES_M = ["Carlos", "Luis", "Miguel", "Jose", "Juan", "Jorge", "Ricardo",
             "Fernando", "Eduardo", "Alejandro", "Diego", "Roberto", "Manuel"]
NOMBRES_F = ["Maria", "Ana", "Laura", "Sara", "Marta", "Sofia", "Andrea",
             "Gabriela", "Fernanda", "Alejandra", "Patricia", "Monica"]
APELLIDOS = ["Garcia", "Rodriguez", "Martinez", "Hernandez", "Lopez",
             "Gonzalez", "Perez", "Sanchez", "Ramirez", "Torres", "Flores",
             "Rivera", "Gomez", "Diaz", "Reyes", "Cruz", "Morales", "Ortiz"]
BLOOD_TYPES = ["O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"]
COLORS = ["#0F766E", "#2563EB", "#8B5CF6", "#F59E0B", "#22C55E", "#EF4444", "#EC4899"]
MOODS = ["Muy bien", "Bien", "Regular", "Mal", "Muy mal"]
MED_NAMES = [("Metformina", "850mg", "pastilla"), ("Losartan", "50mg", "pastilla"),
             ("Salbutamol", "100mcg", "inyeccion"), ("Vitamina D", "1000UI", "capsula"),
             ("Omega 3", "1g", "capsula"), ("Paracetamol", "500mg", "pastilla"),
             ("Amoxicilina", "500mg", "capsula"), ("Jarabe para la tos", "15ml", "jarabe")]
SPECIALTIES = ["Cardiologia", "Endocrinologia", "Medicina general", "Oftalmologia",
               "Dermatologia", "Nutricion", "Psicologia", "Traumatologia"]
DOCTORS = ["Dr. Martinez Lopez", "Dra. Garcia Ruiz", "Dr. Sanchez Vega",
           "Dra. Torres Ramos", "Dr. Flores Diaz", "Dra. Morales Cruz"]
PLACES = ["Clinica San Rafael", "Hospital Central", "Centro de Salud Norte",
          "Optica Vision Salud", "Consultorio 204", "Hospital ABC"]
CATEGORIES = ["diseases", "allergies", "medications", "surgeries",
              "consultations", "vaccines", "results"]
HISTORY_BY_CATEGORY = {
    "diseases": ["Hipertension arterial", "Diabetes tipo 2", "Asma", "Migrana cronica",
                 "Hipotiroidismo", "Artritis reumatoide", "Gastritis cronica"],
    "allergies": ["Alergia a la penicilina", "Alergia al polen", "Alergia a mariscos",
                  "Alergia a la lactosa", "Alergia al latex", "Alergia a la aspirina",
                  "Alergia a los frutos secos", "Alergia al polvo", "Rinitis alergica",
                  "Alergia a la sulfa", "Alergia al niquel", "Dermatitis por contacto",
                  "Alergia a los acaros", "Alergia al huevo", "Alergia a la soya"],
    "medications": ["Tratamiento con metformina", "Tratamiento con losartan",
                    "Tratamiento con levotiroxina"],
    "surgeries": ["Apendicectomia (2019)", "Cirugia de rodilla (2021)", "Cesarea (2018)"],
    "consultations": ["Consulta de rutina anual", "Consulta por dolor de espalda"],
    "vaccines": ["Vacuna influenza", "Vacuna COVID-19 refuerzo", "Vacuna tetanos"],
    "results": ["Perfil lipidico dentro de rango", "Glucosa en ayuno: 95 mg/dL"],
}


def month_es(m):
    return ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep",
            "oct", "nov", "dic"][m - 1]


def rand_joined_label():
    d = datetime.date.today() - datetime.timedelta(days=random.randint(0, 700))
    return f"{d.day:02d} {month_es(d.month)} {d.year}"


def rand_email(seed):
    first = random.choice(NOMBRES_F + NOMBRES_M)
    last = random.choice(APELLIDOS)
    return f"{first.lower()}.{last.lower()}{seed}@vitalmind.test"


def get_connection():
    return pymysql.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASSWORD,
        database=DB_NAME, charset="utf8mb4", autocommit=False,
    )


def existing_user_ids(cur, role=None, gender=None):
    """Trae IDs de usuarios ya existentes en la base, para usarlos como FK."""
    query = "SELECT id FROM users WHERE 1=1"
    params = []
    if role:
        query += " AND role = %s"
        params.append(role)
    if gender:
        query += " AND gender = %s"
        params.append(gender)
    cur.execute(query, params)
    return [r[0] for r in cur.fetchall()]


# ============================================================================
# UN GENERADOR DE FILA POR TABLA
# Cada funcion recibe (index, overrides, ctx) y devuelve un dict campo->valor.
# `overrides` son los --set que puso el usuario (tienen prioridad absoluta).
# `ctx` trae cosas compartidas (ids de usuarios existentes, etc.)
# ============================================================================

def row_users(i, overrides, ctx):
    gender = overrides.get("gender") or random.choices(
        ["Mujer", "Hombre", "Otro"], weights=[47, 47, 6])[0]
    first = random.choice(NOMBRES_F if gender == "Mujer" else NOMBRES_M) \
        if gender != "Otro" else random.choice(NOMBRES_F + NOMBRES_M)
    last1, last2 = random.sample(APELLIDOS, 2)
    row = {
        "full_name": f"{first} {last1} {last2}",
        "email": rand_email(f"{ctx['run_tag']}{i}"),
        "password_hash": DEMO_PASSWORD_HASH,
        "age": random.randint(12, 90),
        "gender": gender,
        "joined_label": rand_joined_label(),
        "last_active_label": "Hace unos dias",
        "status": random.choices(["active", "inactive", "pending"], weights=[70, 20, 10])[0],
        "registros": random.randint(0, 80),
        "consultas": random.randint(0, 40),
        "color": random.choice(COLORS),
        "role": random.choices(["patient", "caregiver", "admin"], weights=[85, 10, 5])[0],
        "blood_type": random.choice(BLOOD_TYPES),
        "phone": f"222{random.randint(1000000, 9999999)}",
        "weight_kg": round(random.uniform(45, 110), 1),
        "height_cm": round(random.uniform(150, 190), 1),
    }
    row.update(overrides)
    return row


def row_medications(i, overrides, ctx):
    name, dose, mtype = random.choice(MED_NAMES)
    row = {
        "user_id": overrides.get("user_id") or random.choice(ctx["patient_ids"]),
        "name": name, "dose": dose,
        "frequency": random.choice(["Diario", "Cada 8h", "Cada 12h", "Semanal", "PRN"]),
        "time_label": f"{random.randint(6, 22):02d}:00",
        "color": random.choice(COLORS),
        "taken": random.choice([0, 1]),
        "type": mtype,
        "days_duration": random.randint(5, 90),
    }
    row.update(overrides)
    return row


def row_medication_logs(i, overrides, ctx):
    med_id = overrides.get("medication_id") or random.choice(ctx["medication_ids"])
    row = {
        "medication_id": med_id,
        "user_id": overrides.get("user_id") or ctx["medication_to_user"].get(med_id),
        "taken": random.choice([0, 1]),
        "taken_at": datetime.datetime.now() - datetime.timedelta(days=random.randint(0, 30)),
    }
    row.update(overrides)
    return row


def row_appointments(i, overrides, ctx):
    future = random.random() < 0.4
    offset = random.randint(1, 60) if future else -random.randint(1, 180)
    appt_date = datetime.date.today() + datetime.timedelta(days=offset)
    row = {
        "user_id": overrides.get("user_id") or random.choice(ctx["patient_ids"]),
        "specialty": random.choice(SPECIALTIES),
        "doctor": random.choice(DOCTORS),
        "appointment_date": appt_date.isoformat(),
        "appointment_time": f"{random.randint(8, 18):02d}:{random.choice(['00', '30'])}",
        "place": random.choice(PLACES),
        "color": random.choice(COLORS),
        "status": "proxima" if future else random.choice(["completada", "completada", "cancelada"]),
    }
    row.update(overrides)
    return row


def row_habit_logs(i, overrides, ctx):
    """Para --user-id + --count N: genera N dias CONSECUTIVOS hacia atras
    para ese usuario (ej. 100 dias completos al usuario 18)."""
    log_date = datetime.date.today() - datetime.timedelta(days=i)
    row = {
        "user_id": overrides.get("user_id") or random.choice(ctx["patient_ids"]),
        "habit_key": "water",
        "log_date": log_date.isoformat(),
        "water": round(random.uniform(1, 12), 2),
        "exercise": round(random.uniform(0, 90), 2),
        "sleep": round(random.uniform(4, 10), 2),
        "nutrition": round(random.uniform(1, 5), 2),
        "meditation": round(random.uniform(0, 30), 2),
        "value": round(random.uniform(1, 12), 2),
        "goal": round(random.uniform(4, 12), 2),
    }
    row.update(overrides)
    # log_date siempre se calcula por indice, aunque haya overrides (para que
    # --count 100 con --user-id genere 100 dias distintos, no 100 veces el mismo dia)
    if "log_date" not in overrides:
        row["log_date"] = log_date.isoformat()
    return row


def row_symptom_logs(i, overrides, ctx):
    row = {
        "user_id": overrides.get("user_id") or random.choice(ctx["patient_ids"]),
        "pain": random.randint(0, 10),
        "temperature": round(random.uniform(35.5, 39.0), 1),
        "systolic": random.randint(95, 150),
        "diastolic": random.randint(60, 100),
        "glucose": random.randint(70, 180),
        "weight": round(random.uniform(45, 110), 1),
        "heart_rate": random.randint(55, 110),
        "stress_level": random.randint(1, 10),
        "energy_level": random.randint(1, 10),
        "sleep_quality": random.randint(1, 10),
        "mood": random.choice(MOODS),
        "notes": None,
        "created_at": datetime.datetime.now() - datetime.timedelta(days=random.randint(0, 60)),
    }
    row.update(overrides)
    return row


def row_medical_history_items(i, overrides, ctx):
    category = overrides.get("category") or random.choice(CATEGORIES)
    description = overrides.get("description") or random.choice(HISTORY_BY_CATEGORY[category])
    row = {
        "user_id": overrides.get("user_id") or random.choice(ctx["patient_ids"]),
        "category": category,
        "description": description,
    }
    row.update(overrides)
    return row


def row_notifications(i, overrides, ctx):
    kind = overrides.get("kind") or random.choice(["tip", "reminder", "ai", "alert"])
    titles = {"tip": "Consejo de bienestar", "reminder": "Recordatorio",
              "ai": "Insight de IA", "alert": "Alerta de salud"}
    default_user = random.choice(ctx["patient_ids"] + [None])
    row = {
        "user_id": overrides.get("user_id", default_user),
        "kind": kind,
        "title": titles.get(kind, "Notificacion"),
        "body": "Notificacion generada automaticamente.",
        "time_label": "Hace unos dias",
        "is_read": random.choice([0, 1]),
    }
    row.update(overrides)
    return row


TABLES = {
    "users": {
        "generator": row_users,
        "needs_patients": False,
    },
    "medications": {
        "generator": row_medications,
        "needs_patients": True,
    },
    "medication_logs": {
        "generator": row_medication_logs,
        "needs_patients": True,
        "needs_medications": True,
    },
    "appointments": {
        "generator": row_appointments,
        "needs_patients": True,
    },
    "habit_logs": {
        "generator": row_habit_logs,
        "needs_patients": True,
    },
    "symptom_logs": {
        "generator": row_symptom_logs,
        "needs_patients": True,
    },
    "medical_history_items": {
        "generator": row_medical_history_items,
        "needs_patients": True,
    },
    "notifications": {
        "generator": row_notifications,
        "needs_patients": True,
    },
}


def build_context(cur, table_key, user_id_filter=None):
    ctx = {"run_tag": random.randint(10000, 99999)}
    spec = TABLES[table_key]
    if spec.get("needs_patients"):
        if user_id_filter:
            ctx["patient_ids"] = [user_id_filter]
        else:
            ids = existing_user_ids(cur)
            if not ids:
                print("ERROR: no hay usuarios todavia en la tabla `users`. "
                      "Genera usuarios primero, ej:\n"
                      "  python vitalmind_seed.py users --count 50")
                sys.exit(1)
            ctx["patient_ids"] = ids
    if spec.get("needs_medications"):
        cur.execute("SELECT id, user_id FROM medications")
        rows = cur.fetchall()
        if not rows:
            print("ERROR: no hay medicamentos todavia. Genera medicamentos primero, ej:\n"
                  "  python vitalmind_seed.py medications --count 50")
            sys.exit(1)
        ctx["medication_ids"] = [r[0] for r in rows]
        ctx["medication_to_user"] = {r[0]: r[1] for r in rows}
    return ctx


def parse_set_args(set_list):
    """Convierte ['category=allergies', 'mood=Muy bien'] en {'category':'allergies', ...}"""
    overrides = {}
    for item in set_list or []:
        if "=" not in item:
            print(f"ERROR: --set '{item}' invalido, usa el formato campo=valor")
            sys.exit(1)
        key, value = item.split("=", 1)
        # Convierte tipos basicos automaticamente
        if value.lower() in ("null", "none"):
            value = None
        elif value.isdigit():
            value = int(value)
        else:
            try:
                value = float(value)
            except ValueError:
                pass  # se queda como string
        overrides[key.strip()] = value
    return overrides


def main():
    parser = argparse.ArgumentParser(
        description="Generador flexible de datos para vitalmind (cualquier cantidad, cualquier tabla).")
    parser.add_argument("table", nargs="?", choices=list(TABLES.keys()),
                         help="Tabla donde insertar (users, medications, medication_logs, "
                              "appointments, habit_logs, symptom_logs, medical_history_items, notifications)")
    parser.add_argument("--count", type=int, default=1,
                         help="Cuantos registros generar (1, 10, 100, 1000... lo que sea)")
    parser.add_argument("--user-id", type=int, default=None,
                         help="Aplica todos los registros a este user_id existente "
                              "(ej: 100 dias de habitos al usuario 18)")
    parser.add_argument("--set", action="append", default=[],
                         help="Fuerza un campo a un valor fijo: --set campo=valor "
                              "(se puede repetir varias veces)")
    parser.add_argument("--list", action="store_true",
                         help="Muestra las tablas y campos soportados y termina")
    args = parser.parse_args()

    if args.list or not args.table:
        print("Tablas soportadas y campos disponibles para --set:\n")
        print("  users                  -> full_name, email, age, gender, status, role, blood_type, phone, weight_kg, height_cm, color")
        print("  medications            -> user_id, name, dose, frequency, time_label, color, taken, type, days_duration")
        print("  medication_logs        -> medication_id, user_id, taken, taken_at")
        print("  appointments           -> user_id, specialty, doctor, appointment_date, appointment_time, place, status")
        print("  habit_logs             -> user_id, log_date, water, exercise, sleep, nutrition, meditation")
        print("  symptom_logs           -> user_id, pain, temperature, systolic, diastolic, glucose, heart_rate, stress_level, energy_level, sleep_quality, mood")
        print("  medical_history_items  -> user_id, category (diseases/allergies/medications/surgeries/consultations/vaccines/results), description")
        print("  notifications          -> user_id, kind (tip/reminder/ai/alert), title, body, is_read")
        return

    overrides = parse_set_args(args.set)
    if args.user_id is not None:
        overrides["user_id"] = args.user_id

    conn = get_connection()
    cur = conn.cursor()
    ctx = build_context(cur, args.table, user_id_filter=overrides.get("user_id"))

    generator = TABLES[args.table]["generator"]
    rows = [generator(i, overrides, ctx) for i in range(args.count)]

    columns = list(rows[0].keys())
    placeholders = ",".join(["%s"] * len(columns))
    col_list = ",".join(f"`{c}`" for c in columns)
    sql = f"INSERT INTO `{args.table}` ({col_list}) VALUES ({placeholders})"
    values = [tuple(row[c] for c in columns) for row in rows]

    BATCH = 1000
    for start in range(0, len(values), BATCH):
        cur.executemany(sql, values[start:start + BATCH])
    conn.commit()
    cur.close()
    conn.close()

    print(f"Listo: {args.count} registro(s) insertado(s) en `{args.table}`.")
    if overrides:
        print(f"Campos forzados: {overrides}")


if __name__ == "__main__":
    main()
