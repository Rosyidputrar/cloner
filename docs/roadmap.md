# Roadmap

Each phase ends with: build instructions, real-device test steps, known limitations, and a
manual checklist. Work stops after each phase until the owner replies **"lanjut"**.

| Phase | Scope | Exit criteria |
|-------|-------|---------------|
| **1. Project setup + design system** | Gradle multi-module skeleton (all modules from the [module graph](architecture.md#2-module-graph)), version catalog, convention plugins, Hilt, Room/DataStore wiring, detekt + ktlint. `:core:ui` design system: color tokens (dynamic + teal fallback, light/dark), type scale, spacing, shapes, motion, base components (buttons, list items, empty/loading/error states, stage indicator). Splash, Onboarding (4 slides), Disclaimer, Navigation shell with adaptive layout (`NavigationSuiteScaffold`). **Plus a debug-only "Engine Lab" screen** that provisions the profile and runs `installExistingPackage`, so spikes S1–S3 can run on your devices while Phase 2 is built. | `./gradlew assembleDebug detekt ktlintCheck` green. App runs on API 29 and latest. Engine Lab provisions a profile on at least one real device. |
| **2. App list (Pilih Aplikasi)** | Installed launchable apps via `LauncherApps`, search, filter (user/system, cloned/not), sort (name, install date, size), icon/version/size, "kemungkinan tidak kompatibel" hints from the bundled compatibility list + engine `assess()`. All UI states. | Picker usable at 200% font scale and with TalkBack. Unit + Compose tests for ViewModel and screen. |
| **3. Clone engine core** | `:engine:api` contracts, `WorkProfileEngine`, profile agent, cross-profile command protocol (S2 result), provisioning flow, Proses Clone screen with 5 stages and cancel/rollback, `clone_jobs` reconciliation after process death. S3/S4 results documented. | A clone of a Play-installed app is created, launched and receives push on a test device. Instrumented engine tests pass on an emulator with managed-profile support. |
| **4. Clone manager + shortcuts** | Beranda (clone list), Detail Clone: launch, rename, custom icon + badge, pin shortcut (`ShortcutManagerCompat.requestPinShortcut`, launches via a lock-aware trampoline), clear data, remove clone (S7), remove whole profile (typed confirmation). | All actions work on a real device, and each destructive action has a confirmation and an undo or explanation. |
| **5. Health check, updates, backup** | Periodic `HealthCheckWorker` + on-open check, repair flow. `UpdateWatcher` using `getChangedPackages`. Notifications. Per-clone settings: notifications deep link, permission overview, storage (S6), network toggle (S5) or suspend fallback. Encrypted settings backup/restore. | Health check detects: profile removed, app removed from profile, app suspended, Kembar no longer profile owner. Backup restores on a second device. |
| **6. Security hardening + polish** | Full threat model doc, exported-component audit, signature-permission on commands, App lock (Kunci Aplikasi: biometric/PIN + work-profile lock guidance), Pengaturan, Bantuan, Tentang, "Aplikasi yang tidak bisa di-clone" page, permission rationale table, copy review, accessibility audit (contrast, 48dp, TalkBack, 200% font). | Threat-model mitigations implemented or explicitly accepted. Accessibility checklist passes. |
| **7. Tests + CI + release** | Test coverage gaps, Room migration tests, baseline profile module, R8 keep rules verified on release build, GitHub Actions (build, detekt, ktlint, unit tests, assembleRelease, optional emulator tests), signing via secrets, release checklist, Play compliance checklist, license. | Tagged release APK built by CI. Release checklist complete. |

## Spike schedule

| Spike | When | Owner action needed |
|-------|------|---------------------|
| S1 provisioning on OEMs | During Phase 1–2 (Engine Lab) | Install the debug APK on every device you have and report the result |
| S2 command channel | Start of Phase 3 | None (emulator + one device) |
| S3 `installExistingPackage` on split/16 KB apps | Phase 1–2 (Engine Lab) | Report which apps clone and launch |
| S4 push / Sign-In / Integrity | Phase 3 | Log in to test accounts in the clone |
| S5 in-profile VPN network toggle | Start of Phase 5 | None |
| S6 storage stats from profile | Start of Phase 5 | None |
| S7 silent uninstall in profile | Start of Phase 4 | None |
