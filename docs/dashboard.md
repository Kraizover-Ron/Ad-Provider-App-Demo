# Admin Dashboard

Location: `cookieclicker-dashboard-react`

## Run locally

1. Create a `.env` file based on `.env.example`.
2. Install dependencies:
   - `npm install`
3. Start dev server:
   - `npm run dev`

## Configuration

`.env`:
- `VITE_API_BASE_URL` (example: `http://localhost:5000`)
- `VITE_ADMIN_KEY` (optional; required only if the server enforces `ADMIN_KEY`)

## Notes about refresh and hosting

If deploying the dashboard as static files, configure the host to redirect all routes to `index.html` for client-side routing.

A `_redirects` file is included for hosts that support it.
