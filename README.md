# PivotFit

PivotFit is a planned SoftSmith Android app.

## Repository Status

This repository is initialized as a placeholder so the app can be developed and
later connected to SoftSmith DevHub.

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

