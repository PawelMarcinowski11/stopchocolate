# StopChocolate 🍫

A modern web application for tracking and overcoming chocolate addiction, built with Spring Boot, Angular, and Keycloak.

## 🚀 Features

- Secure authentication (Keycloak)
- Email notifications for password reset and important events
- PostgreSQL database for reliable data storage
- Docker-based deployment for development and production
- Modern Angular frontend _(in progress)_

## 🛠 Tech Stack

- **Backend:** Spring Boot
- **Frontend:** Angular 17
- **Authentication:** Keycloak 26.1
- **Database:** PostgreSQL 17.2
- **Containerization:** Docker & Docker Compose
- **Email Service:** Postmark

## 🏃‍♂️ Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 17+ (for backend development)
- Node.js 18+ (for frontend development)

### Setup & Run

1. Copy the example environment file and adjust configuration as needed:
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```
2. Start all services:
   ```bash
   docker compose up -d
   ```
3. Access the app:
   - Frontend: http://localhost:4200 _(if running frontend separately)_
   - Backend API: http://localhost:8080
   - Keycloak Admin: http://localhost:8085

### Local Development

**Backend (Spring Boot):**

```bash
cd backend
./gradlew bootRun
```

**Frontend (Angular):**

```bash
cd frontend
npm install
npm start
```

## 🏗 Project Structure

```
.
├── backend/      # Spring Boot application
├── frontend/     # Angular application
├── keycloak/     # Keycloak configuration
│   └── realms/
├── db/           # Database initialization scripts
└── compose.yaml  # Docker Compose file
```

## 🔒 Security

- JWT-based authentication _(implemented)_
- Password policy enforcement _(implemented)_
- Rate limiting _(planned)_
- Secure email communication _(implemented)_

## 🤝 Contributing

Suggestions and improvements are welcome. To propose a change:

- Fork the repository
- Create a branch for your feature or fix
- Commit and push your changes
- Open a Pull Request
