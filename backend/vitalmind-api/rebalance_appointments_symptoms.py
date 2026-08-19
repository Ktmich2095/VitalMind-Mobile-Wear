"""
rebalance_appointments_symptoms.py
============================================================================
Arregla el problema de "todas las categorias salen con la misma cantidad"
en las graficas de CITAS (appointments) y SINTOMAS / ESTADO DE ANIMO
(symptom_logs).

Que hace:
  1) Te muestra la distribucion ACTUAL (para que veas el problema:
     ej. proxima=333, completada=333, cancelada=334 -> parejo/sospechoso).
  2) Borra esos registros (solo de usuarios sinteticos @vitalmind.test,
     para no tocar datos reales que hayas metido a mano).
  3) Los vuelve a generar con proporciones REALISTAS y no uniformes:
       - Citas por estado:     completada 55%, proxima 30%, cancelada 15%
       - Citas por especialidad: Medicina general es la mas comun, las
         demas van bajando (no reparto parejo entre las 8 especialidades)
       - Estado de animo:      "Bien" es lo mas comun, "Muy mal" lo menos
         (distribucion tipo campana, no pareja entre las 5 categorias)
       - Ademas correlaciona un poco el dolor/mood: si el mood es malo,
         el dolor tiende a ser mas alto (mas realista que puro azar).
  4) Te vuelve a mostrar la distribucion nueva, para que compares.

------------------------------------------------------------------------
USO
------------------------------------------------------------------------
pip install pymysql
python rebalance_appointments_symptoms.py
============================================================================
"""

import datetime
import random
import pymysql

# ============================================================================
DB_HOST = "127.0.0.1"
DB_PORT = 3306
DB_USER = "root"
DB_PASSWORD = "Vitalmind2026"      # <-- tu password real de MySQL
DB_NAME = "vitalmind"

# Alcance seguro: solo toca datos de usuarios sinteticos (@vitalmind.test).
# Si quieres que tambien incluya TODOS los usuarios (incluyendo los que
# creaste tu a mano), cambia esto a False.
SOLO_USUARIOS_SINTETICOS = True

APPOINTMENTS_PER_USER = (0, 3)     # rango de citas nuevas por usuario
SYMPTOMS_PER_USER = (0, 8)         # rango de sintomas nuevos por usuario

# ---------------------------------------------------------------------------
# Distribuciones REALISTAS (no uniformes) -----------------------------------
STATUS_WEIGHTS = {"completada": 55, "proxima": 30, "cancelada": 15}

SPECIALTY_WEIGHTS = {
    "Medicina general": 30, "Nutricion": 15, "Cardiologia": 12,
    "Endocrinologia": 10, "Dermatologia": 10, "Psicologia": 10,
    "Traumatologia": 8, "Oftalmologia": 5,
}

MOOD_WEIGHTS = {"Bien": 35, "Regular": 25, "Muy bien": 20, "Mal": 15, "Muy mal": 5}

DOCTORS = ["Dr. Martinez Lopez", "Dra. Garcia Ruiz", "Dr. Sanchez Vega",
           "Dra. Torres Ramos", "Dr. Flores Diaz", "Dra. Morales Cruz"]
PLACES = ["Clinica San Rafael", "Hospital Central", "Centro de Salud Norte",
          "Optica Vision Salud", "Consultorio 204", "Hospital ABC"]
COLORS = ["#0F766E", "#2563EB", "#8B5CF6", "#F59E0B", "#22C55E", "#EF4444", "#EC4899"]


def weighted_choice(weight_dict):
    keys = list(weight_dict.keys())
    weights = list(weight_dict.values())
    return random.choices(keys, weights=weights, k=1)[0]


def get_connection():
    return pymysql.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASSWORD,
        database=DB_NAME, charset="utf8mb4", autocommit=False,
    )


def show_distribution(cur, label, table, column, where=""):
    cur.execute(f"SELECT {column}, COUNT(*) as total FROM {table} {where} GROUP BY {column} ORDER BY total DESC")
    rows = cur.fetchall()
    total = sum(r[1] for r in rows) or 1
    print(f"\n{label}:")
    if not rows:
        print("  (sin registros)")
    for value, count in rows:
        pct = round(count / total * 100, 1)
        bar = "#" * int(pct / 2)
        print(f"  {str(value):18s} {count:5d}  ({pct:5.1f}%)  {bar}")


def get_target_user_ids(cur):
    if SOLO_USUARIOS_SINTETICOS:
        cur.execute("SELECT id FROM users WHERE email LIKE %s AND role IN ('patient','caregiver')",
                     ("%@vitalmind.test",))
    else:
        cur.execute("SELECT id FROM users WHERE role IN ('patient','caregiver')")
    return [r[0] for r in cur.fetchall()]


def delete_old_data(cur, user_ids):
    if not user_ids:
        print("No hay usuarios en el alcance definido (SOLO_USUARIOS_SINTETICOS). Nada que borrar.")
        return
    placeholders = ",".join(["%s"] * len(user_ids))
    cur.execute(f"DELETE FROM appointments WHERE user_id IN ({placeholders})", user_ids)
    deleted_appts = cur.rowcount
    cur.execute(f"DELETE FROM symptom_logs WHERE user_id IN ({placeholders})", user_ids)
    deleted_symptoms = cur.rowcount
    print(f"Borrados: {deleted_appts} citas, {deleted_symptoms} registros de sintomas.")


def generate_appointments(user_ids):
    today = datetime.date.today()
    rows = []
    for uid in user_ids:
        for _ in range(random.randint(*APPOINTMENTS_PER_USER)):
            status = weighted_choice(STATUS_WEIGHTS)
            if status == "proxima":
                appt_date = today + datetime.timedelta(days=random.randint(1, 60))
            else:
                appt_date = today - datetime.timedelta(days=random.randint(1, 180))
            rows.append((
                uid, weighted_choice(SPECIALTY_WEIGHTS), random.choice(DOCTORS),
                appt_date.isoformat(), f"{random.randint(8,18):02d}:{random.choice(['00','30'])}",
                random.choice(PLACES), random.choice(COLORS), status,
            ))
    return rows


def generate_symptoms(user_ids):
    today = datetime.date.today()
    rows = []
    for uid in user_ids:
        for _ in range(random.randint(*SYMPTOMS_PER_USER)):
            mood = weighted_choice(MOOD_WEIGHTS)
            # Correlaciona el dolor y las constantes vitales un poco con el mood,
            # para que no sea puro azar sin relacion (mas realista para graficar).
            mood_pain_bias = {"Muy bien": 0, "Bien": 1, "Regular": 3, "Mal": 5, "Muy mal": 7}[mood]
            pain = max(0, min(10, mood_pain_bias + random.randint(-1, 2)))
            stress_bias = {"Muy bien": 2, "Bien": 3, "Regular": 5, "Mal": 7, "Muy mal": 9}[mood]
            stress = max(1, min(10, stress_bias + random.randint(-1, 1)))
            energy_bias = {"Muy bien": 8, "Bien": 7, "Regular": 5, "Mal": 3, "Muy mal": 2}[mood]
            energy = max(1, min(10, energy_bias + random.randint(-1, 1)))

            days_ago = random.randint(0, 60)
            created_at = datetime.datetime.combine(
                today - datetime.timedelta(days=days_ago),
                datetime.time(random.randint(6, 22), random.randint(0, 59)),
            )
            rows.append((
                uid, pain, round(random.uniform(35.5, 39.0), 1),
                random.randint(95, 150), random.randint(60, 100), random.randint(70, 180),
                round(random.uniform(45, 110), 1), random.randint(55, 110),
                stress, energy, random.randint(1, 10),
                mood, None, created_at,
            ))
    return rows


def insert_batched(cur, sql, rows, batch_size=1000):
    for start in range(0, len(rows), batch_size):
        cur.executemany(sql, rows[start:start + batch_size])


def main():
    conn = get_connection()
    cur = conn.cursor()

    where_clause = ""
    if SOLO_USUARIOS_SINTETICOS:
        where_clause = "WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@vitalmind.test')"

    print("=" * 70)
    print("DISTRIBUCION ACTUAL (antes de arreglar)")
    print("=" * 70)
    show_distribution(cur, "Citas por estado", "appointments", "status", where_clause)
    show_distribution(cur, "Citas por especialidad", "appointments", "specialty", where_clause)
    show_distribution(cur, "Estado de animo (symptom_logs.mood)", "symptom_logs", "mood", where_clause)

    user_ids = get_target_user_ids(cur)
    print(f"\nUsuarios en el alcance (SOLO_USUARIOS_SINTETICOS={SOLO_USUARIOS_SINTETICOS}): {len(user_ids)}")

    delete_old_data(cur, user_ids)

    print("\nGenerando citas nuevas con distribucion realista...")
    appt_rows = generate_appointments(user_ids)
    insert_batched(cur, """
        INSERT INTO appointments (user_id, specialty, doctor, appointment_date, appointment_time, place, color, status)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s)
    """, appt_rows)
    print(f"  -> {len(appt_rows)} citas nuevas insertadas.")

    print("Generando registros de sintomas nuevos con distribucion realista...")
    symptom_rows = generate_symptoms(user_ids)
    insert_batched(cur, """
        INSERT INTO symptom_logs
          (user_id, pain, temperature, systolic, diastolic, glucose, weight, heart_rate,
           stress_level, energy_level, sleep_quality, mood, notes, created_at)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
    """, symptom_rows)
    print(f"  -> {len(symptom_rows)} registros de sintomas nuevos insertados.")

    conn.commit()

    print("\n" + "=" * 70)
    print("DISTRIBUCION NUEVA (despues de arreglar)")
    print("=" * 70)
    show_distribution(cur, "Citas por estado", "appointments", "status", where_clause)
    show_distribution(cur, "Citas por especialidad", "appointments", "specialty", where_clause)
    show_distribution(cur, "Estado de animo (symptom_logs.mood)", "symptom_logs", "mood", where_clause)

    cur.close()
    conn.close()
    print("\nListo. Recarga el dashboard (Ctrl+Shift+R) para ver las graficas actualizadas.")


if __name__ == "__main__":
    main()
