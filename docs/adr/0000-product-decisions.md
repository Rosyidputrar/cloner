# ADR-0000: Product decisions

- Status: **Accepted** (owner delegated these choices: "pilihkan yang terbaik")
- Date: 2026-11-18

These five decisions answer the clarifying questions asked before architecture work.

| # | Topic | Decision | Why |
|---|-------|----------|-----|
| 1 | Distribution | **Sideload first** (signed APK on GitHub Releases). Keep the codebase Play-compatible (no hidden APIs, no `QUERY_ALL_PACKAGES`, no `REQUEST_INSTALL_PACKAGES`) so a Play listing can be attempted later with the compliance checklist from Phase 7. | Play review of profile-owner consumer apps is uncertain. Sideloading lets real users test the product now without boxing in the architecture. |
| 2 | Engine | **Managed (work) profile engine**, see [ADR-0001](0001-clone-engine.md). One clone per app in v1. | Only option where the apps users want keep push, Sign-In and Play Integrity, without modifying or bypassing anything. |
| 3 | Platform range | **minSdk 29 (Android 10)**, targetSdk/compileSdk = latest stable API level (verified when the project is created in Phase 1). Test matrix: Pixel/AOSP, Samsung One UI, Xiaomi HyperOS, Oppo ColorOS, Vivo. Must-check apps: WhatsApp, WhatsApp Business, Instagram, Telegram, Facebook Lite, Shopee, Tokopedia, one banking app (expected to fail; used to test the incompatibility explainer). | Brief targets Android 10+. `installExistingPackage` needs API 28; API 29 removes several pre-Q special cases. |
| 4 | Root | **Never required, never used.** | Simpler threat model, no SafetyNet/Integrity side effects, no policy issues. |
| 5 | Branding | App name **Kembar**. `applicationId` **`io.github.rosyidputrar.kembar`**. Material 3 with dynamic color on Android 12+ and a teal seed (`#00696D`) fallback palette. License: decided in Phase 7 (Apache-2.0 suggested). | "Kembar" (twin) is short, Indonesian, and says what the app does. The `io.github.<owner>` namespace is collision-free and needs no domain. |

## Language

- Default `values/strings.xml` is Indonesian (casual "kamu" register).
- Code, comments, commit messages and documentation are English.

## Repository conventions

- Commits are authored by the repository owner only. No AI co-author trailers or
  generated-by lines in commits or pull requests.
