# CookieClicker — Android App + Engagement SDK + Admin Dashboard

A small end-to-end project that connects an **Android app** to a **backend API**, and exposes an **Admin Dashboard** for monitoring users, sessions, clicks, ad eligibility, and ad serving.

---

## Documentation

The full documentation is in `docs/` and can be published with GitHub Pages.

- Docs entry point: `docs/index.md`
- GitHub Pages link (after enabling Pages): `https://<github-username>.github.io/<repo-name>/`

---

## Demo

A quick demo showing the full flow: **taps → eligibility → ad → cooldown → score**.

[![App Demo (YouTube)](https://img.youtube.com/vi/0KwZL-KGmOw/0.jpg)](https://www.youtube.com/shorts/0KwZL-KGmOw)

---

## Project structure

- **`CookieClickerApp/`** — Android app (Kotlin)
  - includes **`engagementsdk`** module (a reusable mini-SDK)
- **`cookieclicker-server/`** — Node.js (Express) backend + Firebase Realtime Database
- **`cookieclicker-dashboard-react/`** — React + Vite + Tailwind admin dashboard

---

## How it works

### Core concepts (short explanations)

- **Session**: a play session started when the app comes to foreground and ended when it stops.
- **Events**: user activity sent to the server (mainly `click`, but also `screen_view`).
- **Eligibility**: a temporary state where the user is allowed to receive an ad.
- **Cooldown**: a waiting period after an ad is served, during which the user cannot receive another ad.

### Flow in this project

1. The Android app starts a **session** (`/v1/session/start`).
2. Every tap calls `EngagementSdk.trackClick()`.
3. The SDK batches events and sends them to the backend (`/v1/events/batch`).
4. The backend maintains **recent clicks** per user and checks whether the user reached the threshold:
   - default window: **10 seconds**
   - default threshold: **20 clicks**
5. When the threshold is reached, the backend marks the user as **eligible** for a short period (TTL).
6. The SDK/UI shows **“Eligible”**, then requests the next ad (`/v1/ads/next`).
7. If the user is eligible, the backend returns a demo ad payload and immediately starts a **cooldown** (and clears `recentClicks`).

**Default timings (server-side):**
- Click window: **10s**
- Threshold: **20 clicks**
- Eligibility TTL: **3 minutes**
- Cooldown: **3 minutes**

---

## Admin Dashboard

The dashboard is a lightweight web UI that reads **admin endpoints** from the backend.

Link - Link - https://ad-provider-dashboard.onrender.com/

### Overview page

- Total users / sessions / events
- How many users are **eligible now** vs **in cooldown**
- Ads served (total + last 24h)
- Charts (per minute): **clicks** and **ads served**
- Auto refresh (every ~5 seconds)

![Dashboard Home](https://i.imgur.com/LB7s5Il.png)

### Users page

- Search users by **userId / name / device**
- Table with:
  - sessions, clicks, avg session duration
  - eligibility state and cooldown state
  - ads served count, last ad served time, last seen time
- Click a user row to expand a per-user analytics card:
  - sessions/clicks/ads in the selected range
  - charts: clicks per minute + ads per minute
- Auto refresh (every ~5 seconds)

![Dashboard Users](https://i.imgur.com/3Xm5h4p.png)

---

## API quick reference

### Public (used by the Android SDK)

- `GET /health` — health check
- `POST /v1/session/start` — start a session
- `POST /v1/session/end` — end a session
- `POST /v1/events/batch` — send events (clicks are counted here)
- `POST /v1/engagement/evaluate` — server-side eligibility evaluation (throttled by the SDK)
- `GET /v1/engagement/status?userId=...` — eligibility status
- `GET /v1/ads/next?userId=...` — returns **204** if not eligible, otherwise returns an ad and starts cooldown

### Admin (used by the Dashboard)

> If `ADMIN_KEY` is set in the server, the dashboard must send it as `X-ADMIN-KEY`.

- `GET /admin/overview`
- `GET /admin/users`
- `GET /admin/charts/clicks?range=hour|day|week`
- `GET /admin/charts/ads?range=hour|day|week`
- `GET /admin/users/:userId/metrics?range=hour|day|week`
- `GET /admin/users/:userId/charts/clicks?range=hour|day|week`
- `GET /admin/users/:userId/charts/ads?range=hour|day|week`

---

## Run locally

### 1) Backend (Node.js + Firebase)

**Requirements:** Node.js + a Firebase Realtime Database project.

1. Go to the server folder:
   ```bash
   cd cookieclicker-server


## JitPack (Library publishing)

The `engagementsdk` module is configured with `maven-publish`.

To publish with JitPack:

1. Push this repository to GitHub.
2. Create and push a tag (example: `1.0.0`).
3. Enable JitPack and add the dependency:

```gradle
repositories {
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  implementation("com.github.<github-username>:<repo-name>:1.0.0")
}
```

You may set publishing properties:
- `PUBLISH_GROUP_ID`
- `PUBLISH_ARTIFACT_ID`
- `PUBLISH_VERSION`

---
