# ScoreeBoard

A minimalist Android score-tracking app for turn-by-turn games (Scrabble, Belote, Uno, etc.).

## Features

- 2–6 configurable players
- Game title (optional)
- Round-by-round score entry, including negative values
- Cumulative score table with auto-scroll
- Edit any previously submitted round (tap the row)
- End Game → ranked summary with winner highlight
- Abort Game → back to home with confirmation
- Local game history (stored on device, no account needed)

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM — single ViewModel + StateFlow |
| Navigation | Navigation Compose 2.8 (type-safe routes) |
| Persistence | kotlinx.serialization → JSON file on internal storage |
| Min SDK | 26 (Android 8.0) |

## Project structure

```
app/src/main/kotlin/com/scoreeboard/app/
├── MainActivity.kt
├── ScoreboardApp.kt          # NavHost
├── navigation/Routes.kt
├── model/                    # GameState, GameRecord
├── data/GamesRepository.kt   # JSON persistence
├── viewmodel/GameViewModel.kt
└── ui/
    ├── welcome/WelcomeScreen.kt
    ├── setup/SetupScreen.kt
    ├── game/GameScreen.kt
    ├── summary/SummaryScreen.kt
    ├── history/HistoryScreen.kt
    └── theme/
```

## Build & run

**Android Studio**

1. Open `C:\Dev\scoreeboard`
2. Sync Gradle
3. Run on emulator or device

**Command line (PowerShell)**

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## Install on a friend's phone (no USB)

1. **Build → Build APK(s)** in Android Studio
2. Find the APK at `app/build/outputs/apk/debug/app-debug.apk`
3. Send it via WhatsApp, Drive, email, etc.
4. On their phone: open the file and allow installation from unknown sources when prompted
