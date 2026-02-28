# Android Example App

Location: `CookieClickerApp/app`

## Run

1. Open `CookieClickerApp` in Android Studio.
2. Sync Gradle.
3. Run the `app` configuration on an emulator or a physical device.

## Configuration

The app reads these values from `BuildConfig`:

- `SDK_BASE_URL`  
  Defaults to the deployed API, but can be overridden by setting `SDK_BASE_URL` in `CookieClickerApp/gradle.properties`.

- `SDK_APP_KEY`  
  Defaults to `demo-app`, can be overridden by setting `SDK_APP_KEY` in `CookieClickerApp/gradle.properties`.

## Flow (what to demonstrate)

- Tap the cookie button to generate clicks.
- Once eligibility is reached, an ad is fetched and shown in the placeholder fragment.
- Closing the ad starts a cooldown timer.
- Dashboard updates reflect clicks and ad serving.
