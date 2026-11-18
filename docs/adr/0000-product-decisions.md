# ADR-0000: Product decisions

- Status: **Accepted**
- Date: 2026-11-18
- History: the owner delegated the first set of choices ("pilihkan yang terbaik"), then
  changed the engine to repackaging (clones outside an isolated profile) and pinned the toolchain.

| # | Topic | Decision | Why |
|---|-------|----------|-----|
| 1 | Distribution | **Sideload only** (signed APK on GitHub Releases). | A repackaging engine needs `REQUEST_INSTALL_PACKAGES` and installs modified copies of third-party apps. Google Play's policy does not allow that. |
| 2 | Engine | **Repackaging (B)**, see [ADR-0001](0001-clone-engine.md). Multiple clones per app. | Owner requirement: clones are normal apps, not inside a work profile. |
| 3 | Platform range | **minSdk 29 (Android 10)**, **compileSdk/targetSdk 34**. Must-check apps: Telegram, Facebook Lite, Shopee, Tokopedia, Instagram, WhatsApp (warning only), one banking app (expected to fail). | Brief targets Android 10+. Target 34 is the highest that build-tools 34.0.0 / AGP 8.5.2 fully support. |
| 4 | Root | **Never required, never used.** | Simpler threat model, no policy or integrity side effects. |
| 5 | Branding | App name **Kembar**, `applicationId` **`io.github.rosyidputrar.kembar`**, Material 3 with dynamic color on Android 12+ and a teal fallback palette (primary `#006A6A`). License decided in Phase 7. | Short Indonesian name ("twin"). Collision-free namespace. |

## Toolchain (pinned by owner)

| Tool | Version |
|------|---------|
| JDK | Eclipse Temurin 17 |
| Gradle | 8.7 (wrapper) |
| Android Gradle Plugin | 8.5.2 (requires Gradle ≥ 8.7; matches apksig 8.5.2) |
| Android SDK | platform 34, build-tools 34.0.0 |
| Kotlin | 2.0.20 (+ Compose compiler plugin, KSP 2.0.20-1.0.25) |
| apksig | 8.5.2 |
| AndroidX / Material | Compose BOM 2024.09.03, Material Components 1.12.0 (window themes only) |
| Build-tools CLIs | aapt2, apksigner, zipalign, d8 (tests/verification only), keytool (release key) |

## Language

- Default `values/strings.xml` is Indonesian (casual "kamu" register).
- Code, comments, commit messages and documentation are English.

## Repository conventions

- Commits are authored by the repository owner only. No AI co-author trailers or
  generated-by lines in commits or pull requests.
