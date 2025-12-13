# Finance App Backend

A robust, production-ready Kotlin backend for a Personal Finance Mobile Application built with Ktor, Exposed ORM, and PostgreSQL.

## Tech Stack

- **Language:** Kotlin 1.9.22
- **Framework:** Ktor 2.3.7
- **Database:** PostgreSQL with Exposed ORM
- **DI:** Koin 3.5.3
- **Authentication:** JWT
- **Build Tool:** Gradle Kotlin DSL

## Prerequisites

- JDK 17+
- PostgreSQL database (Supabase recommended)

## Configuration

Set the following environment variables or update `application.conf`:

```bash
export DB_URL="jdbc:postgresql://your-host:5432/your-db"
export DB_USER="your-username"
export DB_PASSWORD="your-password"
export JWT_SECRET="your-super-secret-key"
export PORT=8080
```

## Running Locally

```bash
# Build the project
./gradlew build

# Run the server
./gradlew run
```

The server will start at `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /auth/register` - Register a new user
- `POST /auth/login` - Login and get JWT token

### Categories (Protected)
- `GET /categories` - Get all categories (system + user)
- `POST /categories` - Create a custom category

### Transactions (Protected)
- `GET /transactions` - List transactions (with filters)
- `POST /transactions` - Create transaction
- `GET /transactions/{id}` - Get transaction by ID
- `PUT /transactions/{id}` - Update transaction
- `DELETE /transactions/{id}` - Delete transaction
- `GET /transactions/summary` - Get income/expense summary
- `GET /transactions/recent` - Get recent transactions

## Deployment on Render.com

1. Push your code to GitHub
2. Create a new Web Service on Render
3. Connect your repository
4. Set environment variables:
   - `DB_URL`
   - `DB_USER`
   - `DB_PASSWORD`
   - `JWT_SECRET`
5. Render will automatically build using the Dockerfile

## Project Structure

```
src/main/kotlin/com/financeapp/
├── Application.kt          # Entry point
├── database/
│   ├── DatabaseFactory.kt  # DB connection & seeding
│   └── tables/             # Exposed table definitions
├── di/
│   └── AppModule.kt        # Koin DI configuration
├── models/                 # Data classes & DTOs
├── plugins/                # Ktor plugins
├── repositories/           # Data access layer
├── routes/                 # API route handlers
├── services/               # Business logic
└── utils/                  # Helpers & utilities
```
