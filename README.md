# Cold Snake Launcher

A "dumb launcher" for Android built around timed sessions rather than always-on restrictions.
Set a duration, the phone drops into a minimal fixed-app-set "dumb mode," and reverts
automatically when the timer runs out. Points are scored for staying in dumb mode.

Full concept and design notes live outside this repo, in the project's Obsidian vault entry.

## Status

Early scaffold — v1 goal is a working default-launcher app with dumb-mode sessions,
published to the Google Play Store.

## Release notes

### 0.1.0

Early preview. Pick a session length (2 sec / 10 sec / 1 min — short durations for testing) and start a session — you'll see a fixed grid of essential apps (Phone, Messages, Camera, Clock, Calendar, Maps) plus a countdown. When time's up, the app closes automatically. Active work-in-progress; expect frequent updates.

## Permissions

| Permission | Why |
|---|---|
| `com.android.alarm.permission.SET_ALARM` | Required by some clock apps (e.g. MIUI's) to open the alarms screen from the dumb-mode app grid's Clock tile. |

## Stack

- Kotlin
- Android Gradle Plugin, min SDK 26, target/compile SDK 35

## Getting started

```
./gradlew assembleDebug
```

Open the project root in Android Studio to build and run.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
