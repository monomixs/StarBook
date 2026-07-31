# StarBook

A modern, privacy-first audiobook player for Android.

StarBook is designed to make listening to local audiobooks simple, beautiful, and distraction-free. It focuses on a polished Material 3 experience, powerful playback features, and complete user privacy without ads, accounts, or cloud services.

> StarBook is free and open-source software licensed under the GNU General Public License v3.0.

---

## Credits

StarBook would not exist without the incredible work of the open-source community.

### Voice

**Voice** by Paul Woitaschek served as the foundation of this project.

StarBook began as a fork of Voice and has since evolved into a heavily modified application with its own interface, features, and user experience while continuing to build upon Voice's reliable playback engine, architecture, and privacy-first philosophy.

https://github.com/PaulWoitaschek/Voice

### PixelPlayer

**PixelPlayer** by PixelPlayerHQ inspired portions of StarBook's mini player and full player interface.

The inspiration is limited to visual design only. No PixelPlayer source code is included in StarBook.

https://github.com/PixelPlayerHQ/PixelPlayer

---

## Features

- 🎨 **Modern Material 3 Interface**
  - Material You support
  - Dynamic colors extracted from audiobook covers
  - Clean, distraction-free design

- 📚 **Library Management**
  - Import local audiobook folders
  - Automatic library scanning
  - Metadata editing
  - Chapter navigation

- ▶️ **Powerful Playback**
  - Resume exactly where you left off
  - Playback speed adjustment
  - Silence skipping
  - Auto-rewind after pauses
  - Sleep timer with optional fade-out

- 🔒 **Privacy First**
  - No advertisements
  - No user accounts
  - No analytics or tracking
  - No cloud dependency
  - Your library stays on your device

- 🚗 **Android Integration**
  - Android Auto support
  - Home screen widgets
  - Media notifications
  - Background playback

---

## Project Architecture

StarBook uses a modular architecture to keep the project maintainable, scalable, and fast to build.

```
:app
 ├── :navigation
 ├── :core
 └── :features
```

### Infrastructure

Application entry point, dependency wiring, and navigation.

### Core

Reusable services shared throughout the application, including:

- Playback (Media3 / ExoPlayer)
- Database (Room)
- Library scanning
- Search
- Settings
- Business logic

### Features

Independent screen modules that compose the core services into user-facing functionality.

Examples include:

- `:features:playbackScreen`
- `:features:bookOverview`
- `:features:library`

Dependencies follow a single direction:

```
Infrastructure
      ↓
     Core
      ↓
   Features
```

---

## Tech Stack

| Technology | Used For |
|------------|----------|
| Kotlin | Application language |
| Jetpack Compose | User interface |
| Material 3 Expressive | Design system |
| Media3 / ExoPlayer | Audio playback |
| Room | Local database |
| Metro | Dependency injection |
| Navigation3 | Navigation |
| Coil | Image loading |

---

## Building

Clone the repository:

```bash
git clone <repository-url>
```

Build the debug application:

```bash
./gradlew :app:assembleDebug
```

---

## Testing

StarBook focuses on testing domain logic and UI state.

Testing tools include:

- Molecule
- Turbine

Run all unit tests:

```bash
./gradlew starbookUnitTest
```

---

## License

StarBook is licensed under the GNU General Public License v3.0.

See the `LICENSE` file for the full license text.
