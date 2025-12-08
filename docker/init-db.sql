-- =============================================================================
-- MySQL Database Initialization Script
-- =============================================================================
-- This script automatically creates both slmdb and slmDev databases
-- It runs only on the first container startup (when mysql-data volume is empty)
-- =============================================================================

-- Create slmdb database (Production)
CREATE DATABASE IF NOT EXISTS slmdb
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Create slmDev database (Development)
CREATE DATABASE IF NOT EXISTS slmDev
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Grant privileges to slm_user on both databases
-- Note: slm_user is created automatically by MYSQL_USER env variable
GRANT ALL PRIVILEGES ON slmdb.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON slmDev.* TO '${MYSQL_USER}'@'%';

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

-- Verify databases were created
SELECT SCHEMA_NAME
FROM INFORMATION_SCHEMA.SCHEMATA
WHERE SCHEMA_NAME IN ('slmdb', 'slmDev');

-- Show success message
SELECT 'Databases slmdb and slmDev created successfully!' AS Status;
