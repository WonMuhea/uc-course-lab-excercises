# Secure PHP + MySQL User Management

A lightweight user-management app using PHP, MySQL in Docker, and environment-based configuration.

---

## Requirements

- PHP 8+
- Docker & Docker Compose
- Web browser

---

## Setup

```bash
git clone <repo>
cd my-secure-app
cp .env.example .env   # rename example to .env
docker compose up -d
cd public
php -S localhost:8000
