# PivotFit

PivotFit is a SoftSmith native Android workout app MVP.

## Repository Status

This repository now contains the Android MVP source.

- Package: `com.pivotfit.app`
- Stack: Kotlin, Jetpack Compose, Material 3, Room, DataStore
- Version: `0.1.9-time-slider`, `versionCode = 10`
- Local installer artifact: `PivotFit-release.apk` when built locally

## MVP Scope

- Today check-in for time, location, energy, soreness, goal, equipment, crowded gym, quiet mode, and low-sweat mode.
- Local adaptive workout generator with warmup, main exercises, cooldown, and plain-language reasoning.
- Active workout flow with large controls, RPE, pain flag, skip, completion, and the Pivot button.
- Local exercise library with 80+ original exercises.
- Room history for completed workouts, exercise logs, pivots, soreness, notes, duration, and generated reason.
- Progress, flexible plan, equipment, recovery, preferences, privacy, safety, and settings screens.
- First-run onboarding for goal, experience, preferred length, equipment, beginner mode, quiet workouts, and low-sweat defaults.
- Workout completion summary with exercises completed, pivots, skipped exercises, soreness flags, minutes, RPE, and next recommendation.
- App icon refreshed from supplied artwork with outer background removed.
- Active workouts and exercise details include instructions, common mistakes, and generated visual guidance for every exercise through specific, family-specific, and movement-pattern image assets.
- Active workout screen has a cleaner set-based flow with a large Finish set button, visible set target, and rest timer.
- Exercise images can be enlarged from the active workout and instruction cards for easier form checks.
- Today Check-In uses a 5-60 minute slider for available workout time instead of fixed interval chips.

## Build

Use the local Gradle wrapper:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:testDebugUnitTest
```

Release signing uses ignored local files:

- `release-keystore.jks`
- `local.properties` values `pivotfit.storePassword`, `pivotfit.keyAlias`, and `pivotfit.keyPassword`

## Publishing Rule

DevHub detects Android app updates from GitHub Releases with APK assets
attached. Pushing source code alone is not enough.

When PivotFit becomes an Android app, publish updates by:

1. Bumping `versionCode`.
2. Setting a new `versionName`.
3. Running tests.
4. Building the APK.
5. Committing and pushing source.
6. Creating a GitHub Release with an APK attached.
