# Architecture

This project is split into three components:

1. **Android Example App** (`CookieClickerApp/app`)  
   A demo game-like app that generates user interaction (clicks) and displays an ad placeholder.

2. **Android SDK / Library** (`CookieClickerApp/engagementsdk`)  
   A reusable SDK that handles:
   - session lifecycle
   - event batching and delivery
   - eligibility evaluation (click threshold inside a time window)
   - ad fetching and cooldown tracking
   - a drop-in UI fragment (`AdPlaceholderFragment`) that demonstrates the ad flow

3. **Backend API + Database** (`cookieclicker-server`)  
   A REST API built with Express, using Firebase Realtime Database as the persistent store.

4. **Admin Dashboard** (`cookieclicker-dashboard-react`)  
   A React + Vite portal to monitor users, sessions, clicks, eligibility, cooldown, and charts.

## High-level data flow

Android App
  -> Engagement SDK
     -> Backend API
        -> Firebase Realtime Database
Admin Dashboard
  -> Backend API
     -> Firebase Realtime Database

## Eligibility algorithm (summary)

The server maintains a rolling list of recent click timestamps per user and evaluates:

- **Window**: 10 seconds
- **Threshold**: 20 clicks in the window
- **Eligibility TTL**: 3 minutes
- **Cooldown after ad served**: 3 minutes

If the user reaches the threshold, the server marks the user as eligible until the TTL expires, unless an ad is served earlier (which starts cooldown).

## Firebase data model (simplified)

- `users/{userId}`
  - `recentClicks`: number[] (timestamps)
  - `adEligibility`:
    - `eligible`: boolean
    - `reason`: string
    - `eligibleUntil`: number | null
    - `cooldownUntil`: number | null
    - `lastUpdatedAt`: number
  - `metrics`:
    - `totalClicks`: number
    - `totalAdsServed`: number
    - `lastSeenAt`: number

- `sessions/{sessionId}`
  - `userId`: string
  - `startedAt`: number
  - `endedAt`: number | null

## Components and responsibilities

- **SDK**: queues events locally, sends batches to the API, triggers evaluation, and exposes eligibility/cooldown status to the app UI.
- **API**: is the source of truth for eligibility and cooldown, persists data, and provides read endpoints for the admin dashboard.
- **Dashboard**: read-only monitoring UI (overview, users list, charts).
