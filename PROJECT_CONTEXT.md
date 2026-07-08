# PivotFit Project Context

## Repo

- GitHub: `https://github.com/BadBagger/pivotfit`
- Status: Android MVP source added
- Android package: `com.pivotfit.app`
- App scope: local-first adaptive workout app for real-life schedule, energy, equipment, soreness, and location changes
- Latest APK release: `v0.1.8-image-zoom`
- Local APK artifact: `PivotFit-release.apk` may exist locally after build, but it is ignored by git

## Recent Updates

- `v0.1.6-generated-guidance` replaces the abstract movement diagrams with generated exercise visual assets, including clearer bear crawl, squat, core, arm, machine, kettlebell, cardio, and mobility examples plus movement-pattern fallbacks for every workout.
- `v0.1.7-rest-timer-ui` modernizes the active workout screen with set-based controls, a large Finish set button, rest countdown timer, cleaner exercise hero card, and thumb-friendly action layout.
- `v0.1.8-image-zoom` adds tap-to-enlarge exercise images on the active workout and instruction cards so form examples are easier to inspect on small screens.

## DevHub

PivotFit is not connected to the DevHub Android app yet because no GitHub
Release with an APK asset exists and the store listing assets are not finalized.

When those are defined, update the DevHub repo:

- `apps.yml`
- `android-app/app/src/main/AndroidManifest.xml`
- `android-app/app/src/main/java/com/softsmith/devhub/MainActivity.java`
- Store icon and preview assets
- DevHub `PROJECT_CONTEXT.md`
