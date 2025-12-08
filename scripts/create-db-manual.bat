@echo off
REM =============================================================================
REM Manual Database Creation Script for Existing MySQL Container (Windows)
REM =============================================================================
REM This script creates the slmDev database in your existing mysql-container
REM without recreating the container.
REM =============================================================================

setlocal enabledelayedexpansion

echo ==============================================================================
echo MySQL Database Creation Script (Windows)
echo ==============================================================================
echo.

REM Check if container is running
echo Checking if mysql-container is running...
podman ps | findstr mysql-container >nul 2>&1
if errorlevel 1 (
    echo Error: mysql-container is not running!
    echo Please start the container first with: podman start mysql-container
    exit /b 1
)

echo [OK] Container is running
echo.

REM Prompt for root password
set /p MYSQL_ROOT_PASSWORD="Enter MySQL root password: "
echo.

REM Create temporary SQL file
set TEMP_SQL=%TEMP%\slm-init.sql
echo -- Create slmDev database > %TEMP_SQL%
echo CREATE DATABASE IF NOT EXISTS slmDev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; >> %TEMP_SQL%
echo. >> %TEMP_SQL%
echo -- Create user if not exists >> %TEMP_SQL%
echo CREATE USER IF NOT EXISTS 'slm_user'@'%%' IDENTIFIED BY 'slm_password'; >> %TEMP_SQL%
echo. >> %TEMP_SQL%
echo -- Grant privileges on slmDev >> %TEMP_SQL%
echo GRANT ALL PRIVILEGES ON slmDev.* TO 'slm_user'@'%%'; >> %TEMP_SQL%
echo. >> %TEMP_SQL%
echo -- Flush privileges >> %TEMP_SQL%
echo FLUSH PRIVILEGES; >> %TEMP_SQL%
echo. >> %TEMP_SQL%
echo -- Show created databases >> %TEMP_SQL%
echo SHOW DATABASES LIKE 'slm%%'; >> %TEMP_SQL%
echo. >> %TEMP_SQL%
echo -- Success message >> %TEMP_SQL%
echo SELECT 'Database slmDev created successfully!' AS Status; >> %TEMP_SQL%

echo Creating slmDev database...
echo.

REM Execute SQL commands in container
type %TEMP_SQL% | podman exec -i mysql-container mysql -u root -p%MYSQL_ROOT_PASSWORD% 2>nul
if errorlevel 1 (
    echo ==============================================================================
    echo Error: Failed to create database
    echo ==============================================================================
    echo Please check:
    echo 1. MySQL root password is correct
    echo 2. Container has enough permissions
    echo 3. MySQL is fully started (wait a few seconds and try again^)
    echo.
    del %TEMP_SQL%
    exit /b 1
)

REM Clean up
del %TEMP_SQL%

echo.
echo ==============================================================================
echo [SUCCESS] Database slmDev has been created.
echo ==============================================================================
echo.
echo Next steps:
echo 1. Update application-dev.properties with correct password
echo 2. Run: gradlew.bat bootRun --args="--spring.profiles.active=dev"
echo.
echo To verify the database:
echo podman exec -it mysql-container mysql -u slm_user -p
echo Then run: SHOW DATABASES;
echo.

endlocal
