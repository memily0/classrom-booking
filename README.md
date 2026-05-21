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

## Run Backend

```bash
cd backend
chmod +x ./gradlew
./gradlew bootRun
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
