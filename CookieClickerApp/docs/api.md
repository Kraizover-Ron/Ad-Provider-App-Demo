# API Reference

Base URL is configured in:
- Dashboard: `cookieclicker-dashboard-react/.env` via `VITE_API_BASE_URL`
- Android app: `CookieClickerApp/app` via `BuildConfig.SDK_BASE_URL`

## Conventions

- JSON request/response for all endpoints (unless explicitly stated).
- Admin endpoints require an **optional** header: `X-ADMIN-KEY` (only enforced if `ADMIN_KEY` is set on the server).

## Health

### GET `/health`
Returns a simple health response.

## Diagnostics

### GET `/whoami`
Returns basic request info (useful for debugging deployments).

## Sessions

### POST `/v1/session/start`
Starts a session and stores it in Firebase.

Body:
- `userId` (string, required)
- `displayName` (string, optional)
- `appVersion` (string, optional)
- `device` (object, optional)

Response:
- `sessionId` (string)
- `serverTime` (number)

### POST `/v1/session/end`
Ends a session.

Body:
- `sessionId` (string, required)

## Events

### POST `/v1/events/batch`
Sends a batch of events.

Body:
- `sessionId` (string, required)
- `userId` (string, optional)
- `events` (array, required)
  - event objects may include `type`, `at` (ms timestamp), and additional fields

Server-side effects:
- updates `users/{userId}/recentClicks` for click events
- updates metrics fields

## Engagement / Eligibility

### POST `/v1/engagement/evaluate`
Evaluates eligibility.

Body:
- `sessionId` (string, optional if `userId` is provided)
- `userId` (string, optional if `sessionId` is provided)
- `windowMs` (number, optional; defaults to 10000)
- `thresholdClicks` (number, optional; defaults to 20)

Response:
- `eligible` (boolean)
- `reason` (string)
- `eligibleUntil` (number|null)
- `cooldownUntil` (number|null)
- `score` (number)  number of clicks in current window
- `serverTime` (number)

### GET `/v1/ad/eligibility`
### GET `/v1/engagement/status`
Query params:
- `userId` (string, required)

Response:
- `eligible`, `reason`, `eligibleUntil`, `cooldownUntil`, `serverTime`, etc.

## Ads

### GET `/v1/ads/next`
Query params:
- `userId` (string, required)

If the user is not eligible, returns **204 No Content**.

If eligible, returns a demo ad object:
- `adId`, `title`, `imageUrl`, `clickUrl`
- `eligibleUntil`, `cooldownUntil`, `serverTime`

Server-side effects:
- starts cooldown for the user

## Admin (Dashboard)

All endpoints below are used by the admin dashboard:

- GET `/admin/overview`
- GET `/admin/users`
- GET `/admin/charts/clicks`
- GET `/admin/charts/ads`
- GET `/admin/users/:userId/metrics`
- GET `/admin/users/:userId/charts/clicks`
- GET `/admin/users/:userId/charts/ads`

Authentication:
- If `ADMIN_KEY` is set on the server, include header `X-ADMIN-KEY` (dashboard uses `VITE_ADMIN_KEY`).
