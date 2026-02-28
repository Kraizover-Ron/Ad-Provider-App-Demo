# Deployment

This repository is designed to be deployable on common PaaS providers.

## Backend

Deploy the `cookieclicker-server` as a Node service and configure environment variables:

- `FIREBASE_DATABASE_URL`
- `FIREBASE_SERVICE_ACCOUNT_JSON`
- `ADMIN_KEY` (optional)

## Dashboard

Deploy `cookieclicker-dashboard-react` as a static site.

Set:
- `VITE_API_BASE_URL` to the deployed backend URL
- `VITE_ADMIN_KEY` (if needed)

## Android

For local development, point `SDK_BASE_URL` to your local backend and run the app.

For a submission/demo, point `SDK_BASE_URL` to the deployed backend URL.
