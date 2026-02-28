# Android SDK (engagementsdk)

The SDK is located at: `CookieClickerApp/engagementsdk`

## What the SDK provides

- Session lifecycle helpers: `startSession`, `endSession`
- Event tracking: `trackClick`, `trackScreen`
- Eligibility state: `isEligible`, callbacks via `setOnEligibilityChanged`
- Ad request helper: `requestAd`
- A reusable UI component: `AdPlaceholderFragment`

## Usage inside the example app

Initialization:

- Create a config:
  - `baseUrl`: API base URL
  - `appKey`: app identifier string

Main calls:
- `EngagementSdk.init(context, config)`
- `EngagementSdk.get().startSession(...)`
- `EngagementSdk.get().trackClick()`
- `EngagementSdk.get().endSession()`

The example app uses:
- `BuildConfig.SDK_BASE_URL`
- `BuildConfig.SDK_APP_KEY`

## Publishing (JitPack)

The project uses `maven-publish` and defines a `MavenPublication` named `release` in `engagementsdk`.

To publish via JitPack:

1. Add `jitpack.yml` in the repository root.
2. Push the repository to GitHub.
3. Create a version tag (for example `1.0.0`) and push the tag.
4. Add the dependency from JitPack in the consuming project.

You must set these Gradle properties (either in `CookieClickerApp/gradle.properties` or via CI):

- `PUBLISH_GROUP_ID`  (recommended: `com.github.<your-github-username>`)
- `PUBLISH_ARTIFACT_ID` (example: `engagementsdk`)
- `PUBLISH_VERSION` (example: `1.0.0`)

## Local development integration

Since the SDK is included as a module in this repository, the example app already consumes it without publishing.
