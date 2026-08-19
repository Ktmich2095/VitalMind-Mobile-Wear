-- ============================================================================
-- VitalMind · Migración de reparación de esquema (SEGURA / ADITIVA) — v3
-- ----------------------------------------------------------------------------
-- Compatible con MySQL estándar (no MariaDB): usa un procedimiento temporal
-- que revisa INFORMATION_SCHEMA antes de agregar cada columna, en vez de la
-- sintaxis "ADD COLUMN IF NOT EXISTS" (que solo existe en MariaDB).
--
-- NO usa DROP TABLE, NO borra filas, NO renombra nada. Se puede correr las
-- veces que quieras: si una tabla o columna ya existe, simplemente la salta.
--
-- Cómo correrlo:
--   mysql -u root -p vitalmind -e "source fix_schema_vitalmind_v3.sql"
-- ============================================================================

USE `vitalmind`;

-- Procedimiento auxiliar: agrega una columna solo si no existe todavía.
DROP PROCEDURE IF EXISTS `vm_add_column_if_missing`;
DELIMITER $$
CREATE PROCEDURE `vm_add_column_if_missing`(
  IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column
  ) THEN
    SET @vm_sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_definition);
    PREPARE vm_stmt FROM @vm_sql;
    EXECUTE vm_stmt;
    DEALLOCATE PREPARE vm_stmt;
  END IF;
END$$
DELIMITER ;

-- 1) habit_logs -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `habit_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `log_date` date NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_habit_user_date` (`user_id`,`log_date`),
  CONSTRAINT `fk_habit_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CALL vm_add_column_if_missing('habit_logs', 'water', '`water` decimal(6,2) NOT NULL DEFAULT ''0.00'' AFTER `log_date`');
CALL vm_add_column_if_missing('habit_logs', 'exercise', '`exercise` decimal(6,2) NOT NULL DEFAULT ''0.00'' AFTER `water`');
CALL vm_add_column_if_missing('habit_logs', 'sleep', '`sleep` decimal(6,2) NOT NULL DEFAULT ''0.00'' AFTER `exercise`');
CALL vm_add_column_if_missing('habit_logs', 'nutrition', '`nutrition` decimal(6,2) NOT NULL DEFAULT ''0.00'' AFTER `sleep`');
CALL vm_add_column_if_missing('habit_logs', 'meditation', '`meditation` decimal(6,2) NOT NULL DEFAULT ''0.00'' AFTER `nutrition`');

-- 2) symptom_logs (incluye ya las columnas del contrato ML) -----------------
CREATE TABLE IF NOT EXISTS `symptom_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned DEFAULT NULL,
  `pain` int NOT NULL DEFAULT '0',
  `temperature` decimal(4,1) DEFAULT NULL,
  `systolic` int DEFAULT NULL,
  `diastolic` int DEFAULT NULL,
  `glucose` int DEFAULT NULL,
  `weight` decimal(5,1) DEFAULT NULL,
  `heart_rate` int DEFAULT NULL,
  `mood` enum('Muy bien','Bien','Regular','Mal','Muy mal') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_symptom_logs_user` (`user_id`),
  CONSTRAINT `fk_symptom_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CALL vm_add_column_if_missing('symptom_logs', 'stress_level', '`stress_level` tinyint unsigned DEFAULT NULL AFTER `heart_rate`');
CALL vm_add_column_if_missing('symptom_logs', 'energy_level', '`energy_level` tinyint unsigned DEFAULT NULL AFTER `stress_level`');
CALL vm_add_column_if_missing('symptom_logs', 'sleep_quality', '`sleep_quality` tinyint unsigned DEFAULT NULL AFTER `energy_level`');

-- 3) medical_history_items ---------------------------------------------------
CREATE TABLE IF NOT EXISTS `medical_history_items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `category` enum('diseases','allergies','medications','surgeries','consultations','vaccines','results') COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_medical_history_user` (`user_id`),
  CONSTRAINT `fk_medical_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) medication_logs (requiere que la tabla `medications` ya exista) --------
CREATE TABLE IF NOT EXISTS `medication_logs` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `medication_id` bigint unsigned NOT NULL,
  `user_id` bigint unsigned DEFAULT NULL,
  `taken` tinyint(1) NOT NULL DEFAULT '0',
  `taken_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_medication_logs_medication` (`medication_id`),
  KEY `fk_medication_logs_user` (`user_id`),
  CONSTRAINT `fk_medication_logs_medication` FOREIGN KEY (`medication_id`) REFERENCES `medications` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_medication_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5) Columnas adicionales que también usa el código --------------------------
CALL vm_add_column_if_missing('medications', 'days_duration', '`days_duration` int NOT NULL DEFAULT ''0''');
CALL vm_add_column_if_missing('users', 'blood_type', '`blood_type` varchar(5) DEFAULT NULL');
CALL vm_add_column_if_missing('users', 'phone', '`phone` varchar(30) DEFAULT NULL');
CALL vm_add_column_if_missing('users', 'weight_kg', '`weight_kg` decimal(5,1) DEFAULT NULL');
CALL vm_add_column_if_missing('users', 'height_cm', '`height_cm` decimal(5,1) DEFAULT NULL');

-- Limpieza: borra el procedimiento auxiliar, ya no se necesita
DROP PROCEDURE IF EXISTS `vm_add_column_if_missing`;

-- Verificación rápida: deberías ver las columnas de cada tabla listadas.
SHOW COLUMNS FROM habit_logs;
SHOW COLUMNS FROM symptom_logs;
SHOW COLUMNS FROM medical_history_items;
SHOW COLUMNS FROM medication_logs;
SHOW COLUMNS FROM medications;
SHOW COLUMNS FROM users;
