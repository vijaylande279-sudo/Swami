# Swami — Hotel Order Management System

A real-time order management system for a restaurant: waiters take orders on tablets, the
kitchen sees live order tickets, and admins manage the menu, tables, and billing (with UPI
QR-code payment).

## Structure

This is a monorepo with two independently deployed apps:

```
backend/   Spring Boot 3 (Java 17) REST API + WebSocket (STOMP) + PostgreSQL
frontend/  Angular 21 SPA (SSR-capable) — waiter, kitchen, and admin views
```

Each has its own `Dockerfile` and deploys as a separate service.

## Local development

**Backend** — needs a local PostgreSQL database named `hotel_oms`:

```bash
cd backend
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. See `backend/src/main/resources/application.yml` for
configuration (all overridable via environment variables).

**Frontend**:

```bash
cd frontend
npm install
npm start
```

Runs on `http://localhost:4200` and expects the backend at `http://localhost:8080`
(see `frontend/src/environments/environment.ts`).

## Deploying to Railway

Both apps deploy as separate Railway services from this one repo, each with its **Root
Directory** set to `backend` or `frontend` in the Railway dashboard (Settings → Root
Directory). Railway auto-detects each `Dockerfile`.

### 1. Database

Add a **PostgreSQL** plugin to your Railway project (New → Database → PostgreSQL). Railway
provisions it and exposes `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` variables
automatically.

### 2. Backend service

- Root Directory: `backend`
- Environment variables:

  | Variable | Value |
  |---|---|
  | `DATABASE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
  | `DB_USER` | `${{Postgres.PGUSER}}` |
  | `DB_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
  | `JWT_SECRET` | a random string, 32+ characters (required — no default in prod) |
  | `FRONTEND_URL` | the frontend service's public URL (for CORS), e.g. `https://swami-frontend.up.railway.app` |
  | `HOTEL_NAME` | `Swami Hotel` (or your own) |
  | `HOTEL_UPI_ID` | your UPI ID for the payment QR code |
  | `HOTEL_TAX_PERCENT` | e.g. `5` |

  Railway's `${{ServiceName.VAR}}` syntax references another service/plugin's variables —
  use the reference picker in the Railway dashboard rather than typing it by hand.

  `SPRING_PROFILES_ACTIVE=prod` and `PORT` are already handled by the Dockerfile/app config —
  no need to set them manually.

- Once deployed, note the backend's public URL (Settings → Networking → Generate Domain).

### 3. Frontend service

- Root Directory: `frontend`
- **Create a local `frontend/src/environments/environment.prod.ts`** (not in git — it's in `.gitignore`) with your backend's public URL:

  ```ts
  export const environment = {
    production: true,
    apiUrl: 'https://your-backend-url.up.railway.app',
    wsUrl: 'https://your-backend-url.up.railway.app',
  };
  ```

  **⚠️ Important:** This file is ignored by git and never committed. You must create it locally before building for production:
  1. Copy the template above
  2. Replace `your-backend-url.up.railway.app` with the actual backend URL from step 2
  3. Save to `frontend/src/environments/environment.prod.ts` on your local machine
  4. Run `npm run build` to create the production bundle
  5. Push to deploy on Railway (the build artifact will include your environment config)

- Railway sets `PORT` automatically; the Angular SSR server (`frontend/src/server.ts`)
  already reads it.
- Generate a public domain for this service too (Settings → Networking).

### 4. Wire them together

Once both have public URLs, double check:
- Backend's `FRONTEND_URL` env var matches the frontend's actual domain (CORS).
- Your local `frontend/src/environments/environment.prod.ts` `apiUrl`/`wsUrl` match the backend's actual domain.

Redeploy either service after changing either of these.

## First admin user

There's no self-service admin signup. After the backend is deployed and migrations have
run, insert an admin user directly via Railway's Postgres data tab (or `psql`), with a
bcrypt-hashed password (strength 12) — see `backend/BACKEND_GUIDE (2).md` for the exact
schema and role values.
