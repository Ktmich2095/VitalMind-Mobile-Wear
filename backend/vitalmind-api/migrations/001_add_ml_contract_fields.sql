-- Migración: columnas requeridas por el "Contrato de integración del microservicio ML"
-- (sección 6 y 9.1: stress_level, energy_level y sleep_quality son campos indispensables).
-- Es una migración ADITIVA: no borra ni renombra nada existente, todas las columnas
-- nuevas son NULL-ables, así que no rompe datos ni queries actuales.

ALTER TABLE symptom_logs
  ADD COLUMN stress_level TINYINT UNSIGNED NULL COMMENT 'Escala 1-10, requerido por el contrato ML' AFTER heart_rate,
  ADD COLUMN energy_level TINYINT UNSIGNED NULL COMMENT 'Escala 1-10, requerido por el contrato ML' AFTER stress_level,
  ADD COLUMN sleep_quality TINYINT UNSIGNED NULL COMMENT 'Escala 1-10, requerido por el contrato ML' AFTER energy_level;

-- =========================================================
-- VitalMind AI
-- Ajustes de esquema requeridos por integración ML
-- =========================================================


-- ---------------------------------------------------------
-- 1. Agregar log_date a symptom_logs
-- ---------------------------------------------------------

ALTER TABLE symptom_logs
ADD COLUMN log_date DATE NULL AFTER user_id;

UPDATE symptom_logs
SET log_date = DATE(created_at)
WHERE log_date IS NULL;



CREATE INDEX idx_symptom_logs_user_date
ON symptom_logs (user_id, log_date);


-- ---------------------------------------------------------
-- 2. Crear emotional_logs
-- ---------------------------------------------------------

CREATE TABLE IF NOT EXISTS emotional_logs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    user_id BIGINT UNSIGNED NOT NULL,

    mood ENUM(
        'Muy bien',
        'Bien',
        'Regular',
        'Mal',
        'Muy mal'
    ) NOT NULL,

    stress_level TINYINT UNSIGNED NOT NULL,
    energy_level TINYINT UNSIGNED NOT NULL,
    sleep_quality TINYINT UNSIGNED NOT NULL,

    notes TEXT NULL,

    log_date DATE NOT NULL,

    created_at DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    INDEX idx_emotional_logs_user_date (
        user_id,
        log_date
    ),

    CONSTRAINT fk_emotional_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_emotional_stress
        CHECK (
            stress_level BETWEEN 1 AND 10
        ),

    CONSTRAINT chk_emotional_energy
        CHECK (
            energy_level BETWEEN 1 AND 10
        ),

    CONSTRAINT chk_emotional_sleep_quality
        CHECK (
            sleep_quality BETWEEN 1 AND 10
        )
);