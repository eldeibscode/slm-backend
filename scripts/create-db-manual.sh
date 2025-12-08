#!/bin/bash

# =============================================================================
# Manual Database Creation Script for Existing MySQL Container
# =============================================================================
# This script creates the slmDev database in your existing mysql-container
# without recreating the container.
# =============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}==============================================================================${NC}"
echo -e "${GREEN}MySQL Database Creation Script${NC}"
echo -e "${GREEN}==============================================================================${NC}"
echo ""

# Check if container is running
echo -e "${YELLOW}Checking if mysql-container is running...${NC}"
if ! podman ps | grep -q mysql-container; then
    echo -e "${RED}Error: mysql-container is not running!${NC}"
    echo -e "${YELLOW}Please start the container first with: podman start mysql-container${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Container is running${NC}"
echo ""

# Prompt for root password
echo -e "${YELLOW}Enter MySQL root password:${NC}"
read -s MYSQL_ROOT_PASSWORD
echo ""

# Create SQL commands
SQL_COMMANDS="
-- Create slmDev database
CREATE DATABASE IF NOT EXISTS slmDev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user if not exists
CREATE USER IF NOT EXISTS 'slm_user'@'%' IDENTIFIED BY 'slm_password';

-- Grant privileges on slmDev
GRANT ALL PRIVILEGES ON slmDev.* TO 'slm_user'@'%';

-- Flush privileges
FLUSH PRIVILEGES;

-- Show created databases
SHOW DATABASES LIKE 'slm%';

-- Success message
SELECT 'Database slmDev created successfully!' AS Status;
"

echo -e "${YELLOW}Creating slmDev database...${NC}"

# Execute SQL commands in container
if echo "$SQL_COMMANDS" | podman exec -i mysql-container mysql -u root -p"$MYSQL_ROOT_PASSWORD" 2>/dev/null; then
    echo ""
    echo -e "${GREEN}==============================================================================${NC}"
    echo -e "${GREEN}✓ Success! Database slmDev has been created.${NC}"
    echo -e "${GREEN}==============================================================================${NC}"
    echo ""
    echo -e "${YELLOW}Next steps:${NC}"
    echo -e "1. Update application-dev.properties with correct password"
    echo -e "2. Run: ./gradlew bootRun --args=\"--spring.profiles.active=dev\""
    echo ""
    echo -e "${YELLOW}To verify the database:${NC}"
    echo -e "podman exec -it mysql-container mysql -u slm_user -p"
    echo -e "Then run: SHOW DATABASES;"
    echo ""
else
    echo ""
    echo -e "${RED}==============================================================================${NC}"
    echo -e "${RED}Error: Failed to create database${NC}"
    echo -e "${RED}==============================================================================${NC}"
    echo -e "${YELLOW}Please check:${NC}"
    echo -e "1. MySQL root password is correct"
    echo -e "2. Container has enough permissions"
    echo -e "3. MySQL is fully started (wait a few seconds and try again)"
    echo ""
    exit 1
fi
