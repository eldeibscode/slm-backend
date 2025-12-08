# SLM Backend

Backend application for SLM built with Java, Spring Boot, Hibernate, and QueryDSL.

## Technologies

- **Java 17**
- **Spring Boot 3.2.0**
- **Hibernate** (via Spring Data JPA)
- **QueryDSL 5.0.0** - Type-safe query construction
- **MySQL** (production database)
- **H2 Database** (development & testing)
- **Lombok** - Reduces boilerplate code
- **Gradle 8.5** - Build tool

## What is H2 and Why Do We Use It?

### H2 Database Explained

**H2** is a lightweight, in-memory database written in Java that runs embedded in your application.

### Key Features:
- **Zero Installation** - No database server to install or configure
- **Lightning Fast** - Data stored in RAM for maximum speed
- **Temporary Data** - Resets on every restart (perfect for testing!)
- **Built-in Console** - Web UI to view and query data during development
- **Java-based** - Runs anywhere Java runs

### Why Use H2 for Development?

1. **Instant Setup** - Just run `./gradlew bootRun` and you're ready!
2. **Fast Development Cycle** - No database state to manage or clean up
3. **Isolated Testing** - Each test starts with a clean slate
4. **No Production Conflicts** - Develop without touching production DB
5. **Works Offline** - No external database server required
6. **Perfect for CI/CD** - Automated tests run without database setup

### H2 vs MySQL

| Feature | H2 (Development) | MySQL (Production) |
|---------|------------------|-------------------|
| Installation | None required | Requires MySQL server |
| Speed | Very fast (in-memory) | Fast (disk-based) |
| Data Persistence | Temporary (resets on restart) | Permanent |
| Use Case | Local dev & testing | Production environment |
| Setup Time | 0 seconds | 5-10 minutes |

## Prerequisites

- **JDK 17 or higher** - [Download here](https://adoptium.net/)
- **MySQL Server** (for production only) - [Download here](https://dev.mysql.com/downloads/mysql/)
- **Gradle** (optional - wrapper included)

## Getting Started

### 1. Navigate to Project Directory

```bash
cd C:\dev\slm\lp\slm-backend
```

### 2. Build the Project

```bash
# Windows
gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

This will:
- Download all dependencies
- Generate QueryDSL Q-classes from your entities
- Compile the code
- Run tests
- Build the application JAR

### 3. Run the Application (Development Mode)

```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### 4. Access H2 Console (Development Only)

When running in development mode, you can access the H2 web console:

- **URL**: `http://localhost:8080/api/h2-console`
- **JDBC URL**: `jdbc:h2:mem:slmdb`
- **Username**: `sa`
- **Password**: (leave empty)

Here you can:
- View all tables
- Run SQL queries
- Inspect data
- Test database operations

## Project Structure

```
src/
├── main/
│   ├── java/com/slm/backend/
│   │   ├── SlmBackendApplication.java    # Main Spring Boot application
│   │   ├── config/                       # Configuration classes
│   │   │   ├── JpaConfig.java           # JPA auditing (createdAt, updatedAt)
│   │   │   ├── QueryDslConfig.java      # QueryDSL JPAQueryFactory bean
│   │   │   └── WebConfig.java           # CORS configuration
│   │   ├── controller/                   # REST API endpoints
│   │   │   └── UserController.java      # User CRUD operations
│   │   ├── service/                      # Business logic layer
│   │   │   └── UserService.java         # User business logic
│   │   ├── repository/                   # Data access layer
│   │   │   ├── UserRepository.java      # JPA repository interface
│   │   │   ├── UserRepositoryCustom.java # Custom query interface
│   │   │   └── UserRepositoryImpl.java  # QueryDSL implementation
│   │   ├── entity/                       # JPA entities
│   │   │   ├── BaseEntity.java          # Base entity with audit fields
│   │   │   └── User.java                # User entity
│   │   └── dto/                          # Data Transfer Objects
│   │       ├── UserDto.java             # User response DTO
│   │       └── CreateUserRequest.java   # User creation request DTO
│   └── resources/
│       ├── application.properties        # H2 configuration (development)
│       └── application-prod.properties   # MySQL configuration (production)
└── test/                                 # Test classes
    └── java/com/slm/backend/
        └── SlmBackendApplicationTests.java
```

## API Endpoints

All endpoints are prefixed with `/api`

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Create a new user |
| GET | `/api/users` | Get all users (paginated) |
| GET | `/api/users/{id}` | Get user by ID |
| GET | `/api/users/username/{username}` | Get user by username |
| GET | `/api/users/search` | Search users with filters |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |
| PATCH | `/api/users/{id}/activate` | Activate user |
| PATCH | `/api/users/{id}/deactivate` | Deactivate user |

### Example API Requests

#### Create a User

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### Get All Users (Paginated)

```bash
curl "http://localhost:8080/api/users?page=0&size=10&sortBy=username&sortDir=asc"
```

#### Search Users

```bash
curl "http://localhost:8080/api/users/search?searchTerm=john&active=true&page=0&size=10"
```

#### Update User

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "password": "newpassword123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

## QueryDSL Usage

QueryDSL provides type-safe database queries. This project includes a complete example in `UserRepositoryImpl.java`.

### How QueryDSL Works

1. **Q-Classes** - Gradle automatically generates query classes (e.g., `QUser`) from your entities
2. **Type-Safe** - Compile-time checking prevents SQL errors
3. **Dynamic Queries** - Build complex queries programmatically
4. **IDE Support** - Full autocomplete and refactoring support

### Example: UserRepositoryImpl.java

```java
@Override
public Page<User> searchUsers(String searchTerm, Boolean active, Pageable pageable) {
    QUser user = QUser.user;

    BooleanBuilder builder = new BooleanBuilder();

    if (searchTerm != null && !searchTerm.isEmpty()) {
        builder.and(
            user.username.containsIgnoreCase(searchTerm)
                .or(user.email.containsIgnoreCase(searchTerm))
                .or(user.firstName.containsIgnoreCase(searchTerm))
                .or(user.lastName.containsIgnoreCase(searchTerm))
        );
    }

    if (active != null) {
        builder.and(user.active.eq(active));
    }

    JPAQuery<User> query = queryFactory
        .selectFrom(user)
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize());

    return new PageImpl<>(query.fetch(), pageable, query.fetchCount());
}
```

### Generate Q-Classes After Adding New Entities

```bash
# Windows
gradlew.bat clean compileJava

# Linux/Mac
./gradlew clean compileJava
```

Q-classes will be generated in `build/generated/sources/annotationProcessor/java/main/`

## Database Configuration

The SLM Backend supports **three database profiles** for different use cases:

1. **H2 (Default)** - In-memory database for quick testing
2. **MySQL Development (slmDev)** - Persistent MySQL database for local development
3. **MySQL Production (slmdb)** - Production MySQL database

### Profile Overview

| Profile | Database | Use Case | Command |
|---------|----------|----------|---------|
| Default | H2 (in-memory) | Quick testing, CI/CD | `./gradlew bootRun` |
| dev | MySQL (slmDev) | Local development | `./gradlew bootRun --args="--spring.profiles.active=dev"` |
| prod | MySQL (slmdb) | Production | `./gradlew bootRun --args="--spring.profiles.active=prod"` |

---

### Option 1: H2 Database (Default - Fastest Setup)

**Zero configuration required!** Just run the application:

```bash
./gradlew bootRun
```

H2 will automatically:
- Create an in-memory database
- Generate tables from your entities
- Provide a web console at `http://localhost:8080/api/h2-console`

**Access H2 Console:**
- URL: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:slmdb`
- Username: `sa`
- Password: (leave empty)

**When to use:** Quick testing, running automated tests, no persistence needed.

Configuration: `src/main/resources/application.properties`

---

### Option 2: MySQL Development with slmDev Database (Recommended for Development)

Use a persistent MySQL database for local development with Podman/Docker.

#### Method A: Using Podman Compose (Recommended)

This method automatically creates both `slmdb` and `slmDev` databases.

**Step 1: Configure Environment Variables**

```bash
# Copy the example file
cp .env.example .env

# Edit .env and set secure passwords
# Make sure MYSQL_PASSWORD matches the password in application-dev.properties
```

Example `.env` file:
```env
MYSQL_ROOT_PASSWORD=secure_root_password
MYSQL_DATABASE=slmdb
MYSQL_USER=slm_user
MYSQL_PASSWORD=slm_password
```

**Step 2: Start MySQL Container**

```bash
# Start MySQL container with both databases
podman-compose up -d

# Check logs
podman-compose logs -f mysql

# Verify databases were created
podman exec -it mysql-container mysql -u root -p
# Then run: SHOW DATABASES;
```

**Step 3: Update Spring Boot Configuration**

Edit `src/main/resources/application-dev.properties` and ensure the password matches:

```properties
spring.datasource.password=slm_password
```

**Step 4: Run Application with Dev Profile**

```bash
# Windows
gradlew.bat bootRun --args="--spring.profiles.active=dev"

# Linux/Mac
./gradlew bootRun --args="--spring.profiles.active=dev"
```

**Podman Compose Commands:**

```bash
# Start container
podman-compose up -d

# Stop container
podman-compose down

# View logs
podman-compose logs -f mysql

# Restart container
podman-compose restart

# Remove container and volumes (WARNING: Deletes all data!)
podman-compose down -v
```

#### Method B: Using Existing MySQL Container (Quick)

If you already have a MySQL container running, use the provided script to add the `slmDev` database:

**For Linux/Mac:**

```bash
# Make script executable (first time only)
chmod +x scripts/create-db-manual.sh

# Run the script
./scripts/create-db-manual.sh

# Follow the prompts to enter MySQL root password
```

**For Windows:**

```cmd
scripts\create-db-manual.bat
```

The script will:
1. Check if `mysql-container` is running
2. Create `slmDev` database with utf8mb4 charset
3. Create/update `slm_user` with privileges
4. Display success message

**Manual SQL Commands (Alternative):**

```bash
# Connect to your MySQL container
podman exec -it mysql-container mysql -u root -p

# Create database
CREATE DATABASE IF NOT EXISTS slmDev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Create user (if not exists)
CREATE USER IF NOT EXISTS 'slm_user'@'%' IDENTIFIED BY 'slm_password';

# Grant privileges
GRANT ALL PRIVILEGES ON slmDev.* TO 'slm_user'@'%';

# Apply changes
FLUSH PRIVILEGES;

# Verify
SHOW DATABASES LIKE 'slm%';

# Exit
exit;
```

---

### Option 3: MySQL Production with slmdb Database

For production deployment with the `slmdb` database.

#### Step 1: Install MySQL Server

Download and install MySQL Server from [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/)

#### Step 2: Create Database and User

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database
CREATE DATABASE slmdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'slm_user'@'localhost' IDENTIFIED BY 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON slmdb.* TO 'slm_user'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Exit MySQL
exit;
```

#### Step 3: Update Configuration

Edit `src/main/resources/application-prod.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/slmdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=slm_user
spring.datasource.password=your_secure_password
```

#### Step 4: Run with Production Profile

```bash
# Windows
gradlew.bat bootRun --args="--spring.profiles.active=prod"

# Linux/Mac
./gradlew bootRun --args="--spring.profiles.active=prod"
```

---

### Database Profile Comparison

| Feature | H2 (Default) | MySQL Dev (slmDev) | MySQL Prod (slmdb) |
|---------|--------------|-------------------|-------------------|
| **Setup Time** | 0 seconds | 2 minutes | 10 minutes |
| **Persistence** | No (resets on restart) | Yes | Yes |
| **Data Isolation** | Perfect | Per database | Per database |
| **Use Case** | Testing, CI/CD | Local development | Production |
| **Configuration** | application.properties | application-dev.properties | application-prod.properties |
| **Tables** | Auto-created | Auto-created | Manual migration |
| **SQL Logging** | Enabled | Enabled | Disabled |

---

### Connecting to MySQL Databases

Once MySQL is running, you can connect using any MySQL client:

**Command Line:**
```bash
# Connect to slmDev
podman exec -it mysql-container mysql -u slm_user -p slmDev

# Connect to slmdb
podman exec -it mysql-container mysql -u slm_user -p slmdb
```

**GUI Tools:**
- Host: `localhost`
- Port: `3306`
- Username: `slm_user`
- Password: (from .env file)
- Database: `slmDev` or `slmdb`

Popular GUI tools: MySQL Workbench, DBeaver, DataGrip, phpMyAdmin

---

### Troubleshooting Database Issues

#### "Could not connect to database"

```bash
# Check if container is running
podman ps | grep mysql-container

# Start if not running
podman-compose up -d

# Or start existing container
podman start mysql-container

# Check logs for errors
podman-compose logs mysql
```

#### "Access denied for user"

- Verify password matches in `.env` and `application-dev.properties`
- Check user exists: `SELECT User, Host FROM mysql.user;`
- Recreate user with correct password

#### "Unknown database"

```bash
# List databases
podman exec -it mysql-container mysql -u root -p -e "SHOW DATABASES;"

# Recreate database
./scripts/create-db-manual.sh
```

#### "Port 3306 already in use"

- Another MySQL instance is running
- Stop it or change port in `podman-compose.yml`

#### Init script didn't run

The `init-db.sql` script only runs on first startup when the mysql-data volume is empty.

To force re-initialization:
```bash
# WARNING: This deletes all data!
podman-compose down -v
podman-compose up -d
```

---


## Gradle Commands

### Build Commands

```bash
# Clean build directory
./gradlew clean

# Compile Java code
./gradlew compileJava

# Build JAR (skip tests)
./gradlew build -x test

# Build JAR (with tests)
./gradlew build
```

### Run Commands

```bash
# Run application (development mode with H2)
./gradlew bootRun

# Run with production profile (MySQL)
./gradlew bootRun --args="--spring.profiles.active=prod"

# Run with custom port
./gradlew bootRun --args="--server.port=9090"
```

### Test Commands

```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests UserServiceTest
```

### Dependency Commands

```bash
# View dependency tree
./gradlew dependencies

# Check for dependency updates
./gradlew dependencyUpdates
```

## Building for Production

### Build Executable JAR

```bash
./gradlew clean build -x test
```

The JAR will be created at: `build/libs/slm-backend-0.0.1-SNAPSHOT.jar`

### Run the JAR

```bash
# Development mode (H2)
java -jar build/libs/slm-backend-0.0.1-SNAPSHOT.jar

# Production mode (MySQL)
java -jar build/libs/slm-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Testing

### Run All Tests

```bash
./gradlew test
```

### Test with H2

All tests automatically use H2 in-memory database, so:
- No database setup needed
- Tests run fast
- Each test gets a clean database
- No cleanup required

## Additional Configuration

### CORS Settings

The backend is configured to accept requests from:
- `http://localhost:4200` (Angular)
- `http://localhost:3000` (React)

To add more origins, edit `WebConfig.java`:

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:4200",
                "http://localhost:3000",
                "https://your-frontend-domain.com"  // Add your domain
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true);
}
```

### Audit Fields

All entities extending `BaseEntity` automatically get:
- `id` - Auto-generated primary key
- `createdAt` - Timestamp when entity was created
- `updatedAt` - Timestamp when entity was last modified

These are managed by JPA Auditing (configured in `JpaConfig.java`).

## Important Notes

### Security Warning

The current implementation stores passwords in plain text. **For production, you MUST**:

1. Add Spring Security dependency
2. Implement password hashing (BCrypt)
3. Add authentication/authorization
4. Use JWT or OAuth2 for API security

### Development vs Production

| Aspect | Development | Production |
|--------|-------------|------------|
| Database | H2 (in-memory) | MySQL (persistent) |
| Data | Temporary | Permanent |
| DDL Mode | `update` (auto-creates tables) | `validate` (requires migration) |
| SQL Logging | Enabled | Disabled |
| H2 Console | Enabled | Disabled |

## Next Steps

### Essential (Production-Ready)

1. Add Spring Security
2. Implement password hashing (BCrypt)
3. Add JWT authentication
4. Implement proper exception handling with `@ControllerAdvice`
5. Add database migrations (Flyway or Liquibase)
6. Add API documentation (Swagger/OpenAPI)

### Nice-to-Have

1. Add MapStruct for DTO mapping
2. Add integration tests
3. Add Docker support
4. Set up CI/CD pipeline
5. Add logging with SLF4J
6. Add caching (Redis)
7. Add metrics and monitoring (Actuator)

## Troubleshooting

### "java: command not found"

Install JDK 17 or higher and set JAVA_HOME:

```bash
# Windows
setx JAVA_HOME "C:\Program Files\Java\jdk-17"

# Linux/Mac
export JAVA_HOME=/path/to/jdk-17
```

### "Could not connect to H2 database"

H2 runs in-memory, so:
- Make sure the application is running
- Use the correct JDBC URL: `jdbc:h2:mem:slmdb`
- Check the H2 console at `http://localhost:8080/api/h2-console`

### "Table not found"

Run:

```bash
./gradlew clean build
./gradlew bootRun
```

This regenerates Q-classes and recreates tables.

### QueryDSL Q-classes not generated

```bash
# Clean and rebuild
./gradlew clean compileJava

# Check generated files
ls build/generated/sources/annotationProcessor/java/main/com/slm/backend/entity/
```

## Project Links

- Spring Boot Docs: [https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- QueryDSL Docs: [http://querydsl.com/static/querydsl/latest/reference/html/](http://querydsl.com/static/querydsl/latest/reference/html/)
- Hibernate Docs: [https://hibernate.org/orm/documentation/](https://hibernate.org/orm/documentation/)
- Gradle Docs: [https://docs.gradle.org/](https://docs.gradle.org/)

## License

This project is part of the SLM application suite.
