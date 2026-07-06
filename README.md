# PivotFit

PivotFit is a SoftSmith native Android workout app MVP.

## Repository Status

This repository now contains the Android MVP source.

- Package: `com.pivotfit.app`
- Stack: Kotlin, Jetpack Compose, Material 3, Room, DataStore
- Version: `0.1.1-adaptive-tests`, `versionCode = 2`
- Local installer artifact: `PivotFit-release.apk` when built locally

## MVP Scope

- Today check-in for time, location, energy, soreness, goal, equipment, crowded gym, quiet mode, and low-sweat mode.
- Local adaptive workout generator with warmup, main exercises, cooldown, and plain-language reasoning.
- Active workout flow with large controls, RPE, pain flag, skip, completion, and the Pivot button.
- Local exercise library with 80+ original exercises.
- Room history for completed workouts, exercise logs, pivots, soreness, notes, duration, and generated reason.
- Progress, flexible plan, equipment, recovery, preferences, privacy, safety, and settings screens.

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
