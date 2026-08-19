/**
 * Script de datos de demostración.
 * Inserta registros REALES (no mocks del frontend) directo en MySQL para
 * que el dashboard tenga algo que graficar. Usa el primer usuario que
 * encuentre en la tabla `users` (o el que se pase por argumento --userId).
 *
 * Uso:
 *   node scripts/seedDemoData.js
 *   node scripts/seedDemoData.js --userId=3
 */
require('dotenv').config();
const { pool } = require('../src/config/db');

function arg(name, fallback) {
  const found = process.argv.find((a) => a.startsWith(`--${name}=`));
  return found ? found.split('=')[1] : fallback;
}

function daysAgo(n) {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d.toISOString().slice(0, 10);
}

async function main() {
  const explicitUserId = arg('userId', null);

  let userId = explicitUserId;
  if (!userId) {
    const [rows] = await pool.query('SELECT id FROM users ORDER BY id ASC LIMIT 1');
    if (!rows.length) {
      console.error('❌ No hay ningún usuario en la tabla `users`. Registra uno primero (POST /api/auth/register).');
      process.exit(1);
    }
    userId = rows[0].id;
  }
  console.log(`→ Sembrando datos de demostración para user_id = ${userId}`);

  // --- Medicamentos ---
  const medications = [
    { name: 'Metformina', dose: '850mg', frequency: 'Diario', time_label: '08:00', type: 'pastilla', color: '#0F766E' },
    { name: 'Losartán', dose: '50mg', frequency: 'Diario', time_label: '20:00', type: 'tableta', color: '#6D5DD3' },
    { name: 'Vitamina D', dose: '2000UI', frequency: 'Diario', time_label: '09:00', type: 'capsula', color: '#D9A441' },
  ];
  const medIds = [];
  for (const m of medications) {
    const [r] = await pool.query(
      `INSERT INTO medications (user_id, name, dose, frequency, time_label, type, color, taken, days_duration)
       VALUES (:userId, :name, :dose, :frequency, :time_label, :type, :color, :taken, 30)`,
      { userId, ...m, taken: Math.random() > 0.4 ? 1 : 0 }
    );
    medIds.push(r.insertId);
  }
  console.log(`✔ ${medications.length} medicamentos creados`);

  // --- Historial de tomas (últimos 14 días) ---
  let logsCount = 0;
  for (let d = 0; d < 14; d++) {
    for (const medId of medIds) {
      if (Math.random() > 0.25) {
        await pool.query(
          `INSERT INTO medication_logs (medication_id, user_id, taken, taken_at) VALUES (:medId, :userId, 1, :takenAt)`,
          { medId, userId, takenAt: `${daysAgo(d)} 08:30:00` }
        );
        logsCount++;
      }
    }
  }
  console.log(`✔ ${logsCount} registros de medicamentos tomados (14 días)`);

  // --- Citas ---
  const appointments = [
    { specialty: 'Cardiología', doctor: 'Dra. Ramírez', place: 'Hospital General', status: 'proxima', daysOffset: 10 },
    { specialty: 'Endocrinología', doctor: 'Dr. Torres', place: 'Clínica Xicotepec', status: 'completada', daysOffset: -20 },
    { specialty: 'Nutrición', doctor: 'Lic. Pérez', place: 'Consultorio 3', status: 'cancelada', daysOffset: -5 },
    { specialty: 'Cardiología', doctor: 'Dra. Ramírez', place: 'Hospital General', status: 'completada', daysOffset: -45 },
  ];
  for (const a of appointments) {
    const date = new Date();
    date.setDate(date.getDate() + a.daysOffset);
    await pool.query(
      `INSERT INTO appointments (user_id, specialty, doctor, appointment_date, appointment_time, place, status)
       VALUES (:userId, :specialty, :doctor, :appointmentDate, '10:00', :place, :status)`,
      { userId, specialty: a.specialty, doctor: a.doctor, appointmentDate: date.toISOString().slice(0, 10), place: a.place, status: a.status }
    );
  }
  console.log(`✔ ${appointments.length} citas creadas`);

  // --- Hábitos (últimos 7 días) ---
  for (let d = 0; d < 7; d++) {
    await pool.query(
      `INSERT INTO habit_logs (user_id, log_date, water, exercise, sleep, nutrition, meditation)
       VALUES (:userId, :logDate, :water, :exercise, :sleep, :nutrition, :meditation)
       ON DUPLICATE KEY UPDATE water=VALUES(water), exercise=VALUES(exercise), sleep=VALUES(sleep), nutrition=VALUES(nutrition), meditation=VALUES(meditation)`,
      {
        userId, logDate: daysAgo(d),
        water: (5 + Math.random() * 3).toFixed(1),
        exercise: Math.floor(15 + Math.random() * 40),
        sleep: (5.5 + Math.random() * 3).toFixed(1),
        nutrition: Math.floor(2 + Math.random() * 3),
        meditation: Math.floor(Math.random() * 20),
      }
    );
  }
  console.log('✔ 7 días de hábitos creados');

  // --- Síntomas (últimos 30 días, cada 3 días) ---
  const moods = ['Muy bien', 'Bien', 'Regular', 'Mal', 'Muy mal'];
  let symptomCount = 0;
  for (let d = 0; d < 30; d += 3) {
    await pool.query(
      `INSERT INTO symptom_logs
        (user_id, pain, temperature, systolic, diastolic, glucose, weight, heart_rate, stress_level, energy_level, sleep_quality, mood, notes, created_at)
       VALUES (:userId, :pain, :temperature, :systolic, :diastolic, :glucose, :weight, :heartRate, :stress, :energy, :sleepQ, :mood, :notes, :createdAt)`,
      {
        userId,
        pain: Math.floor(Math.random() * 8),
        temperature: (36.2 + Math.random() * 1.5).toFixed(1),
        systolic: 110 + Math.floor(Math.random() * 25),
        diastolic: 70 + Math.floor(Math.random() * 15),
        glucose: 85 + Math.floor(Math.random() * 40),
        weight: (65 + Math.random() * 6).toFixed(1),
        heartRate: 65 + Math.floor(Math.random() * 30),
        stress: 1 + Math.floor(Math.random() * 10),
        energy: 1 + Math.floor(Math.random() * 10),
        sleepQ: 1 + Math.floor(Math.random() * 10),
        mood: moods[Math.floor(Math.random() * moods.length)],
        notes: 'Registro de demostración generado por seedDemoData.js',
        createdAt: `${daysAgo(d)} 09:00:00`,
      }
    );
    symptomCount++;
  }
  console.log(`✔ ${symptomCount} registros de síntomas creados (30 días)`);

  // --- Notificaciones ---
  const notifications = [
    { kind: 'reminder', title: 'Toma tu medicamento', body: 'No olvides tomar tu Metformina de las 08:00' },
    { kind: 'tip', title: 'Consejo del día', body: 'Recuerda tomar al menos 6 vasos de agua hoy' },
    { kind: 'ai', title: 'Análisis disponible', body: 'Tu análisis de bienestar semanal ya está listo' },
  ];
  for (const n of notifications) {
    await pool.query(
      `INSERT INTO notifications (user_id, kind, title, body) VALUES (:userId, :kind, :title, :body)`,
      { userId, ...n }
    );
  }
  console.log(`✔ ${notifications.length} notificaciones creadas`);

  console.log('\n🎉 Listo. Recarga http://localhost:4000/dashboard y las gráficas ya deberían mostrar datos.');
  await pool.end();
}

main().catch((err) => {
  console.error('❌ Error sembrando datos:', err.message);
  process.exit(1);
});
