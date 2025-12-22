# Docker Deployment Guide for Hostinger

This guide explains how to deploy the SLM Backend application on Hostinger using Docker Compose.

## Prerequisites

- Hostinger VPS with Docker installed
- SSH access to your Hostinger server
- Git installed on the server
- Domain name (optional but recommended)

## Files Overview

The following files have been created for Docker deployment:

- `Dockerfile` - Multi-stage build configuration for the Spring Boot application
- `docker-compose.yml` - Orchestrates MySQL and Backend services
- `.dockerignore` - Excludes unnecessary files from Docker image
- `.env.example` - Template for environment variables

## Deployment Steps

### 1. Prepare Your Environment

SSH into your Hostinger VPS:

```bash
ssh your-username@your-server-ip
```

### 2. Clone Your Repository

```bash
cd /var/www  # or your preferred directory
git clone https://github.com/your-username/slm-backend.git
cd slm-backend
```

### 3. Configure Environment Variables

Create a `.env` file from the example:

```bash
cp .env.example .env
nano .env  # or use vim, vi, etc.
```

Update the following variables in `.env`:

```env
# MySQL Configuration
MYSQL_ROOT_PASSWORD=your-strong-root-password-here
MYSQL_DATABASE=slmdb
MYSQL_USER=slm_user
MYSQL_PASSWORD=your-strong-user-password-here

# JWT Configuration - IMPORTANT: Generate a new secret!
JWT_SECRET=your-unique-jwt-secret-key-here
JWT_EXPIRATION=86400000

# Application Configuration
# Replace with your actual domain
APP_UPLOAD_URL_PREFIX=https://yourdomain.com
```

**Security Note:** Generate a secure JWT secret:

```bash
openssl rand -base64 64
```

### 4. Build and Deploy

Build and start the containers:

```bash
# Build and start in detached mode
docker-compose up -d

# View logs to ensure everything is working
docker-compose logs -f
```

### 5. Verify Deployment

Check that containers are running:

```bash
docker-compose ps
```

You should see both `slm-mysql` and `slm-backend` containers running.

Test the API:

```bash
curl http://localhost:3000/api/
```

### 6. Configure Reverse Proxy (Recommended)

For production, you should use Nginx as a reverse proxy. Create an Nginx configuration:

```bash
sudo nano /etc/nginx/sites-available/slm-backend
```

Add the following configuration:

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    client_max_body_size 50M;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
}
```

Enable the site and restart Nginx:

```bash
sudo ln -s /etc/nginx/sites-available/slm-backend /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 7. Setup SSL with Let's Encrypt (Recommended)

```bash
sudo apt update
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```

## Management Commands

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f mysql
```

### Restart Services

```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart backend
```

### Stop Services

```bash
docker-compose down
```

### Update Application

```bash
# Pull latest changes
git pull origin main

# Rebuild and restart
docker-compose up -d --build
```

### Database Backup

```bash
# Backup database
docker-compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} slmdb > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore database
docker-compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} slmdb < backup_file.sql
```

### Access MySQL Shell

```bash
docker-compose exec mysql mysql -u root -p
```

## Hostinger Docker Manager

You can also manage your containers through Hostinger's Docker Manager interface:

1. Log in to your Hostinger panel
2. Navigate to VPS > Docker Manager
3. You should see your `slm-backend` project with 2 containers
4. Use the interface to view logs, restart containers, etc.

## Troubleshooting

### Container Won't Start

Check logs:

```bash
docker-compose logs backend
```

### Database Connection Issues

Verify MySQL is healthy:

```bash
docker-compose exec mysql mysqladmin ping -h localhost -u root -p
```

### Port Already in Use

If port 3000 or 3306 is already in use, you can change them in `docker-compose.yml`:

```yaml
ports:
  - "8080:3000"  # Change 8080 to any available port
```

### Permission Issues with Uploads

Ensure the upload volume has correct permissions:

```bash
docker-compose exec backend ls -la /app/uploads
```

## Environment Variables Reference

| Variable | Description | Default |
|----------|-------------|---------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | rootpassword |
| `MYSQL_DATABASE` | Database name | slmdb |
| `MYSQL_USER` | MySQL user | slm_user |
| `MYSQL_PASSWORD` | MySQL user password | slm_password |
| `JWT_SECRET` | JWT signing secret | (default - change in production!) |
| `JWT_EXPIRATION` | JWT expiration in ms | 86400000 (24 hours) |
| `APP_UPLOAD_URL_PREFIX` | Base URL for uploaded files | http://localhost:3000 |

## Security Checklist

- [ ] Changed default MySQL passwords
- [ ] Generated unique JWT secret
- [ ] Configured firewall rules
- [ ] Setup SSL/TLS certificate
- [ ] Regular database backups scheduled
- [ ] Nginx reverse proxy configured
- [ ] Docker containers auto-restart enabled
- [ ] Application logs monitored

## Support

For issues related to:
- Application: Check application logs and code repository
- Docker: Check Docker documentation
- Hostinger: Contact Hostinger support
