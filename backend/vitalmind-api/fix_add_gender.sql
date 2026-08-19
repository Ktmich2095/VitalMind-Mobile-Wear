-- ============================================================================
-- VitalMind · Agregar columna `gender` a `users` (SEGURO / ADITIVO)
-- Corre esto una vez:  mysql -u root -p vitalmind -e "source fix_add_gender.sql"
-- ============================================================================

USE `vitalmind`;

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

CALL vm_add_column_if_missing('users', 'gender', "`gender` enum('Mujer','Hombre','Otro') DEFAULT NULL AFTER `age`");

DROP PROCEDURE IF EXISTS `vm_add_column_if_missing`;

SHOW COLUMNS FROM users;
