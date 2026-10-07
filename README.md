# Spelling Games — Android App

A fun, offline-first Android spelling practice app for kids.  
One shared spelling-list system powers multiple different games.

## Features (MVP)

- **Shared Spelling Lists** — create, edit, paste multiple words, duplicate, set active list
- **Active List** is remembered and used by every game automatically
- **Progress & Mastery** tracking per word (New → Learning → Practicing → Mastered)
- **Parent Area** protected by a simple 4-digit PIN
- **Games included**:
  1. **Spell It** — see/hear the word and type it
  2. **Missing Letters** — fill in the blank
  3. **Scrambled Word** — unscramble letters
  4. **Word Builder** — tap letters in order
  5. **Listen & Spell** — pure audio (Text-to-Speech)
  6. **Which One Is Correct?** — multiple choice with realistic distractors
  7. **Word Hunt** — find the correctly spelled word
  8. **Spelling Challenge** — placeholder ready for expansion
- Fully offline (Room + DataStore)
- Child-friendly colorful Compose UI
- Portrait orientation, empty-state handling, encouraging feedback

## How to Build the APK

1. Open the project in **Android Studio** (Ladybug / 2024.2+ recommended).
2. Let Gradle sync (it will download dependencies).
3. Connect a phone or start an emulator (API 26+).
4. Click **Run** or build a release APK:

```bash
./gradlew assembleRelease
```

The APK will be at:
`app/build/outputs/apk/release/app-release-unsigned.apk`

(Sign it with your keystore for distribution.)

## First-time use

1. Open the app → go to **Lists** (or Parent Area).
2. Create a list (e.g. “Week 1”) and paste words (one per line or comma-separated).
3. The new list becomes the **Active** list.
4. Go to **Games** and start playing any game — they all use the active list.
5. Progress is saved automatically.

## Parent PIN

- First time: open Parent Area → “Set a new PIN”.
- Later: enter the 4-digit PIN to manage lists, add/delete words, or reset progress.

## Architecture

- **Kotlin + Jetpack Compose + Material 3**
- **MVVM** + Hilt
- **Room** for lists, words, progress
- **DataStore** for PIN / points
- Shared `SpellingRepository` — games never own their own lists
- Easy to add new games later

## Project structure

```
app/src/main/java/com/SpellArcade/app/
├── data/          # Room entities, DAOs, repositories
├── di/            # Hilt modules
├── domain/        # (ready for use-cases)
├── ui/
│   ├── components/
│   ├── navigation/
│   ├── screens/
│   │   ├── home/
│   │   ├── lists/
│   │   ├── games/     # all 8 games
│   │   ├── progress/
│   │   └── parent/
│   └── theme/
└── util/          # TTS helper
```

Enjoy helping your son practice spelling through games!
