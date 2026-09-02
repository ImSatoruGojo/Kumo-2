# Kumo — Native Android

**Milestone 1** — Compiling debug APK with core screens.

## What this build has

- Real Kotlin + Jetpack Compose project
- Dark theme (black / purple)
- Bottom navigation: Home · Search · Library · Settings
- Beta notice badge
- Demo catalog (Anime, Movies, TV Shows, Cartoons, Manga)
- Search with 250 ms debounce + ranking
- Title detail page + episode / chapter lists
- Ready for GitHub Actions APK build

## Target

- minSdk 24
- Optimized direction for low-end devices (Vivo Y12 / Android 11)

## How to build the APK

### On a PC with Android Studio

1. Open this folder in Android Studio
2. Let it sync Gradle
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. Install the debug APK on your phone

### Or with command line

```bash
./gradlew assembleDebug
```

APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

## Next milestones (from handoff)

2. Room + unified catalog + duplicate merging  
3. Real player (Media3 ExoPlayer)  
4. Provider architecture  
5. Manga reader  
etc.

---

Kumo is still in beta — expect some bugs.
