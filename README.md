# SpellArcade

Cross-platform educational arcade game for children (ages 6–14) built with **Kotlin Multiplatform** and **Compose Multiplatform**.

## Targets

| Platform | Minimum Version |
|----------|-----------------|
| Android  | 10 (API 29)     |
| iOS      | 16              |

## Project Structure

```
SpellArcade/
├── composeApp/                 # Main Compose Multiplatform application
├── core/
│   ├── common/                 # Shared utilities
│   ├── domain/                 # Pure domain models & use-case interfaces
│   ├── data/                   # Repositories, SQLDelight, Settings
│   ├── game-engine/            # Shared game state machines & scoring
│   └── design-system/          # Theme, typography, reusable components
├── feature/
│   ├── home/
│   ├── games/
│   ├── vocabulary/
│   ├── parent-portal/
│   ├── settings/
│   └── player-profile/
├── iosApp/                     # Xcode project (generated / maintained separately)
└── gradle/libs.versions.toml   # Version catalog
```

## Tech Stack

- Kotlin 2.4.21
- Compose Multiplatform 1.12.2
- Koin (DI)
- SQLDelight (persistence)
- Kotlin Coroutines + StateFlow
- Multiplatform Settings

## Getting Started

### Prerequisites

- Android Studio (latest stable / Otter or newer recommended)
- Xcode 16+ (for iOS)
- JDK 17+

### Open & Build (Android)

1. Open the `SpellArcade` folder in Android Studio.
2. Let Gradle sync.
3. Select the `composeApp` run configuration.
4. Run on an emulator or device (API 29+).

### iOS

1. After a successful Gradle sync, the iOS framework is produced under `composeApp/build/bin/ios*/`.
2. Open or create the Xcode project in `iosApp/`.
3. Link the generated `ComposeApp.framework`.
4. Build & run on Simulator (iOS 16+) or device.

## Development Phases

See the master development prompt for the full phased plan.

**Current status:** Phase 1 — Project skeleton & version catalog complete.

## License

Proprietary — All rights reserved.
