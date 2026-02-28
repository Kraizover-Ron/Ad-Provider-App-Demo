# Backend API (cookieclicker-server)

Location: `cookieclicker-server`

## Run locally

1. Create `.env` from `.env.example`.
2. Provide Firebase Realtime Database URL:
   - `FIREBASE_DATABASE_URL`
3. Provide Firebase Admin credentials (one of the options):
   - Option A: set `FIREBASE_SERVICE_ACCOUNT_JSON` (recommended)
   - Option B: place the file at `cookieclicker-server/secrets/firebase-service-account.json`

4. Install and run:
   - `npm install`
   - `npm start`

## Environment variables

- `PORT` (default 5000)
- `FIREBASE_DATABASE_URL` (required)
- `ADMIN_KEY` (optional; if set, protects admin endpoints)
- `FIREBASE_SERVICE_ACCOUNT_JSON` (recommended; JSON string)
- `LOG_REQUESTS` (optional; set to 1 to log requests)

## Data storage

The server uses Firebase Realtime Database and stores:
- sessions
- recent click timestamps per user
- eligibility and cooldown state
- aggregate metrics for the dashboard
