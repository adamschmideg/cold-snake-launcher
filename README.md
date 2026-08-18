# Cold Snake Launcher

A "dumb launcher" for Android built around timed sessions rather than always-on restrictions.
Set a duration, the phone drops into a minimal fixed-app-set "dumb mode," and reverts
automatically when the timer runs out. Points are scored for staying in dumb mode.

Full concept and design notes live outside this repo, in the project's Obsidian vault entry.

## Status

Early scaffold — v1 goal is a working default-launcher app with dumb-mode sessions,
published to the Google Play Store.

## Stack

- Kotlin
- Android Gradle Plugin, min SDK 26, target/compile SDK 34

## Getting started

```
./gradlew assembleDebug
```

Open the project root in Android Studio to build and run.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
