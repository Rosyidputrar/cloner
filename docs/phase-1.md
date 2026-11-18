# Phase 1 report: project setup + design system

## What's included

| Area | Content |
|------|---------|
| Build | Gradle 8.7 wrapper, version catalog (`gradle/libs.versions.toml`), `build-logic` convention plugins (`kembar.android.application`, `…application.compose`, `…library`, `…library.compose`, `…feature`, `kembar.hilt`), AGP 8.5.2, Kotlin 2.0.20, KSP, compile/target SDK 34, minSdk 29, JDK 17 |
| Quality | detekt 1.23.7 (`config/detekt/detekt.yml`), ktlint 1.3.1 (`.editorconfig`), Android lint (abort on error), JUnit 5 + MockK + Turbine for local tests |
| Modules | `:app`, `:core:common`, `:core:data`, `:core:ui`, `:core:testing` (test helpers), `:feature:manager`, `:feature:settings`. Engine, database, app list and clone modules are added in the phases that need them, so no module is empty. |
| Design system (`:core:ui`) | Light/dark teal color tokens + dynamic color (Android 12+), semantic extended colors (clone badge, success, warning), type scale, 4 dp spacing grid, shapes, motion tokens, buttons, loading/empty/error states, `CloneBadge`, `BadgedAppIcon`, `StageIndicator`, settings rows, `readableWidth()` |
| Screens | Splash, Onboarding (4 slides), Disclaimer (accept / read-only), Beranda (empty state), Pengaturan (theme + dynamic color), Tentang |
| Architecture | MVVM + UDF (`UiState` / `UiEvent` / `UiEffect`), Hilt, DataStore preferences, type-safe Navigation Compose, adaptive `NavigationSuiteScaffold` |
| Security | `allowBackup=false`, `dataExtractionRules` excluding everything from cloud backup and device transfer, no permissions, no network, no analytics |

## How to build

Requirements: JDK 17 (Temurin), Android SDK with platform 34 and build-tools 34.0.0.

```bash
# local.properties (not committed)
echo "sdk.dir=/path/to/Android/sdk" > local.properties

./gradlew assembleDebug                 # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest             # JUnit 5 local tests (19 tests)
./gradlew lintDebug detekt ktlintCheck  # static analysis
./gradlew assembleRelease               # R8-minified, unsigned until Phase 7
./gradlew connectedDebugAndroidTest     # Compose UI tests, needs a device/emulator
```

In Android Studio: open the project folder, let Gradle sync, choose the `app` run configuration.

## How to test on a real device

1. Turn on Developer options and USB debugging, then connect the phone.
2. `./gradlew installDebug` (installs `io.github.rosyidputrar.kembar.debug`, labelled "Kembar").
3. First launch: the teal splash screen appears briefly, then onboarding.
4. Swipe or tap **Lanjut** through 4 slides, then **Mulai**. Or tap **Lewati** on any slide.
5. On the disclaimer, **Setuju dan lanjut** is disabled until you tick the checkbox.
   Rotate the phone after ticking: the tick must survive.
6. Beranda shows "Belum ada clone". Switch to **Pengaturan** in the bottom bar.
7. Change **Tema** to Gelap/Terang: the app and status bar icons update at once.
   On Android 12+, toggle **Warna dinamis** and compare against your wallpaper colors.
8. **Tentang Kembar** → **Baca lagi disclaimer** opens a read-only copy with a back arrow.
9. Kill the app (swipe away) and reopen: you land on Beranda directly.
10. Accessibility: turn on TalkBack and walk through onboarding and settings. Set
    *Font size* to the maximum and *Display size* to large: nothing should be clipped.
11. On a tablet or foldable (or the resizable emulator), Beranda/Pengaturan show a
    navigation rail instead of a bottom bar.
12. `./gradlew connectedDebugAndroidTest` runs the Compose UI tests on the connected device.

## Known limitations

- Beranda only has its empty state. The "Buat clone" action arrives in Phase 2/3.
- The release APK is unsigned (signing config comes in Phase 7 from CI secrets).
- `targetSdk` is 34, as pinned by the toolchain (AGP 8.5.2 / build-tools 34.0.0). That is
  fine for sideloading, but below Google Play's current minimum for new apps.
- Compose UI tests were compiled but not run in CI yet (no emulator until Phase 7).
- detekt 1.23.7 runs on its embedded Kotlin 2.0.10 compiler; the root build pins its
  classpath to that version on purpose.
- The splash screen icon reuses the launcher foreground. Android 12+ masks it to a circle,
  and the two cards stay inside the visible area.

## Manual checklist

- [ ] `./gradlew assembleDebug testDebugUnitTest lintDebug detekt ktlintCheck` passes locally.
- [ ] App installs and starts on an Android 10 (API 29) device or emulator.
- [ ] App installs and starts on the newest Android version you have.
- [ ] Onboarding → Disclaimer → Beranda works, and doesn't come back after a restart.
- [ ] Disclaimer checkbox survives rotation.
- [ ] Theme switch (Ikuti sistem / Terang / Gelap) works, including status bar icon colors.
- [ ] Dynamic color toggle visible only on Android 12+.
- [ ] Navigation rail appears on a tablet/foldable width.
- [ ] TalkBack reads headings, page position ("Halaman 1 dari 4") and checkbox state.
- [ ] 200% font scale: every screen scrolls, nothing clipped.
- [ ] Indonesian copy reviewed: casual "kamu" register, no stiff phrasing.
