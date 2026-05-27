# Classroom Booking

Classroom Booking is a service for booking classrooms in a lyceum.

## Project Structure

```text
classroom-booking/
  frontend/  React + TypeScript + Vite application
  backend/   Spring Boot Kotlin application
```

## Frontend Stack

- React
- TypeScript
- Vite
- npm

## Backend Stack

- Kotlin
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL Driver
- Flyway Migration
- Validation
- Gradle
- Java 21

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

## Run backend locally

```bash
cd backend
chmod +x ./gradlew
./gradlew bootRun
```

## Run with Docker

```bash
docker compose up --build
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## Backend API notes

Room responses include location fields for frontend filters:

```text
buildingName
floor
```

Availability supports both point-in-time and interval checks:

```text
GET /api/rooms/availability?datetime=2026-05-21T14:00:00
GET /api/rooms/availability?start=2026-05-21T12:00:00&end=2026-05-21T13:00:00
```

Each availability item also contains a `schedule` object for the selected date.

For local development, backend CORS allows the Vite frontend origin:

```text
http://localhost:5173
```

## Build

```bash
cd frontend
npm install
npm run build
```

```bash
cd backend
chmod +x ./gradlew
./gradlew build
```

## Git Workflow

- Direct commits to `master` are forbidden.
- All changes must go through Pull Requests.
- At least 1 approval is required before merge.
- CI must pass before merge.
- Force push to `master` is forbidden.
