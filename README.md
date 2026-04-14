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

