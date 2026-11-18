# Kembar: architecture

Engine: **B, repackaging** ([ADR-0001](adr/0001-clone-engine.md)).

## 1. Runtime picture

```mermaid
flowchart LR
  subgraph Device["Main user profile"]
    ORIG["Original app<br/>com.example.chat<br/>(Play signature)"]
    subgraph K["Kembar (io.github.rosyidputrar.kembar)"]
      UI["Compose UI + ViewModels"]
      DB[("Room: clone registry")]
      WM["WorkManager<br/>CloneWorker (foreground, dataSync)<br/>HealthCheckWorker, UpdateCheckWorker"]
      ENG["CloneEngine<br/>(:engine:impl)"]
      KS[("FileKeystore<br/>PKCS#12, one key per clone")]
    end
    C1["Clone 1<br/>com.example.chat.kembar1<br/>(Kembar key #1)"]
    C2["Clone 2<br/>com.example.chat.kembar2<br/>(Kembar key #2)"]
    PI["System PackageInstaller"]
  end
  ORIG -- "read base.apk + splits<br/>(world-readable)" --> ENG
  UI --> DB
  UI -- "enqueue / observe / cancel" --> WM --> ENG
  ENG --> KS
  ENG -- "session install<br/>(user confirms)" --> PI --> C1 & C2
  UI -- "launch via getLaunchIntentForPackage" --> C1
```

Every clone is an ordinary app with its own UID, data directory, notification channels,
WebView data and FCM identity (which may not work, see ADR-0001 L2).

## 2. Module graph

Modules are created in the phase that first needs them, so no module is ever empty.

```mermaid
flowchart TD
  app[":app"]
  fa[":feature:applist (P2)"]
  fc[":feature:clone (P3)"]
  fm[":feature:manager (P1)"]
  fs[":feature:settings (P1)"]
  cui[":core:ui (P1)"]
  cc[":core:common (P1)"]
  cd[":core:data (P1)"]
  cdb[":core:database (P3)"]
  ea[":engine:api (P3)"]
  ei[":engine:impl (P3)"]
  bl["build-logic<br/>(convention plugins, P1)"]
  bp[":baselineprofile (P7)"]

  app --> fa & fc & fm & fs
  app --> ei
  app --> cd
  fa & fc & fm & fs --> cui
  fa & fc & fm & fs --> cd
  fa & fc & fm & fs --> cc
  cui --> cc
  cd --> cdb
  cd --> ea
  cd --> cc
  cdb --> cc
  ei --> ea
  ei --> cc
  ea --> cc
  bp -.-> app
  bl -. "applies plugins" .-> app & fa & fc & fm & fs & cui & cd & cdb & ei
```

Rules:

- Feature modules never depend on each other or on `:engine:impl`. Navigation is wired in `:app`.
- `:core:data` exposes repository **interfaces and domain models**. Implementations are
  `internal` and bound with Hilt. Use cases live in the feature module that owns them.
- Onboarding, disclaimer and splash live in `:app`, because they decide the start
  destination and belong to no single feature.
- `:engine:api` is the contract (`CloneEngine`, `CloneRequest`, `CloneProgress`, `EngineError`).
- `:engine:impl` holds the pipeline. Its binary-format code (`StringPool`, `AxmlPatcher`,
  `ArscPatcher`, `AlignedZipWriter`) uses no Android APIs, so it is unit-tested on the
  host JVM against fixtures built with `aapt2`/`d8` and verified with
  `apksigner`/`zipalign` from build-tools 34.0.0.
- `:core:testing` holds test-only helpers (fake repositories, the JUnit 5 main-dispatcher
  extension). Only `testImplementation` depends on it, so it never ships in the APK.
- No NDK module.

## 3. Engine internals (`:engine:impl`)

| Class | Responsibility |
|-------|----------------|
| `CloneAnalyzer` | Reads `PackageInfo`, base + split paths, `targetSdkVersion`, `sharedUserId`, native-lib layout, Google-services markers. Checks free space. Applies the bundled compatibility list. Returns `Compatibility` with Indonesian-mappable reason codes. |
| `StringPool` | Parses and serializes `ResStringPool` chunks (UTF-8 and UTF-16, styles preserved). Appends strings. |
| `AxmlPatcher` | Parses binary XML chunks (string pool, resource map, namespaces, elements, CDATA). Applies `ManifestRewriteRules` (section 4.1 of ADR-0001), matching attributes by **resource ID** (e.g. `android:name` = `0x01010003`) rather than by name. Re-serializes the result. |
| `ArscPatcher` | Renames the `ResTable_package.name` field (`char16[128]`) of the app's package in `resources.arsc` (base and config splits). Leaves everything else alone. |
| `AlignedZipWriter` | Streams entries from the source APK into a new ZIP. Swaps in the patched manifest/arsc and drops `META-INF` v1 signature files. Keeps STORED entries STORED: `resources.arsc` 4-byte aligned (required for targetSdk ≥ 30), `.so` 16 KB aligned, others 4-byte aligned. Never loads a whole APK into memory. |
| `FileKeystore` | PKCS#12 file in `noBackupFilesDir`. Random password, encrypted by an Android Keystore AES-256-GCM key. One EC P-256 key + self-signed certificate per clone (alias = clone id). Passphrase-encrypted export/import. |
| `CloneSigner` | apksig 8.5.2 `ApkSigner`: v2 + v3, v1 off, `minSdkVersion` 29, `setAlignmentPreserved(true)`. |
| `ApkInstaller` | `PackageInstaller` session (`MODE_FULL_INSTALL`, `setPackageSource(PACKAGE_SOURCE_LOCAL_FILE)` on API 33+, `setRequireUserAction(USER_ACTION_NOT_REQUIRED)` for updates on API 31+). Writes base + splits in one session, commits with a mutable `PendingIntent`, and surfaces `STATUS_PENDING_USER_ACTION` to the UI or a notification. |
| `CloneEngine` (impl) | Orchestrates the stages, emits `CloneProgress`, and cleans up the work directory on success, failure or cancellation. |

## 4. Clone pipeline

Stages shown in the UI: **Analisis → Salin → Modifikasi → Tanda tangan → Pasang**.

```mermaid
sequenceDiagram
  autonumber
  actor U as User
  participant UI as Proses Clone screen
  participant VM as CloneProgressViewModel
  participant REPO as CloneRepository
  participant WM as WorkManager / CloneWorker
  participant AN as CloneAnalyzer
  participant ZW as AlignedZipWriter + AxmlPatcher + ArscPatcher
  participant KS as FileKeystore
  participant SG as CloneSigner (apksig)
  participant PI as ApkInstaller / PackageInstaller
  participant DB as Room

  U->>UI: Pick app, confirm warnings, tap "Buat clone"
  UI->>VM: UiEvent.Start(packageName)
  VM->>REPO: startClone(packageName)
  REPO->>DB: insert clones(status=CREATING, slot=n, clone_package=pkg.kembarN)<br/>insert clone_jobs(state=QUEUED)
  REPO->>WM: enqueueUniqueWork("clone-<id>", CloneWorker, expedited)
  WM->>WM: setForeground(dataSync notification "Membuat clone…")
  Note over WM,AN: 1. Analisis
  WM->>AN: analyze(packageName)
  AN-->>WM: base + splits, targetSdk, warnings, free space OK
  Note over WM,ZW: 2. Salin
  WM->>WM: stream-copy base.apk + splits to cache/clone-work/<jobId>/
  Note over WM,ZW: 3. Modifikasi
  WM->>ZW: patch manifest (package, authorities, permissions, label), patch arsc name,<br/>rewrite ZIP with 16 KB/4 B alignment, drop v1 signature files
  Note over WM,SG: 4. Tanda tangan
  WM->>KS: getOrCreateKey(cloneId)
  KS-->>WM: PrivateKey + X509Certificate
  WM->>SG: sign(each APK, v2+v3)
  Note over WM,PI: 5. Pasang
  WM->>PI: createSession, write base + splits, commit
  PI-->>WM: STATUS_PENDING_USER_ACTION(confirmIntent)
  WM-->>VM: progress(INSTALL, needsUserAction = true)
  alt UI in foreground
    VM-->>UI: UiEffect.LaunchInstallConfirm(intent)
  else UI in background
    WM->>U: Notification "Ketuk untuk memasang clone"
  end
  U->>PI: Confirm "Instal"
  PI-->>WM: STATUS_SUCCESS
  WM->>DB: clones(status=ACTIVE, version), clone_jobs(SUCCEEDED)
  WM->>WM: delete work directory
  WM-->>VM: WorkInfo SUCCEEDED
  VM-->>UI: UiState(success): "Clone siap dipakai"
  opt User taps "Batal" (any stage before the install commit)
    UI->>VM: UiEvent.Cancel
    VM->>REPO: cancel(jobId) → WorkManager.cancelWorkById
    WM->>WM: coroutine cancelled → abandon session, delete work directory
    WM->>DB: clone_jobs(CANCELLED), delete clones row (status CREATING)
  end
```

Process death and configuration changes: the job is a unique WorkManager work item and
the UI only observes `WorkInfo`, so rotating the screen or killing the process doesn't
lose it. If the worker is killed mid-pipeline, WorkManager retries it. The retry starts
again from **Analisis** with a clean work directory, because the stages are idempotent.
A pending install session found at retry is abandoned first.

## 5. Data model (Room, `:core:database`, Phase 3)

Schema export to `core/database/schemas`. Every version bump ships an explicit `Migration`
and a `MigrationTestHelper` test.

```mermaid
erDiagram
  clones ||--|| clone_settings : has
  clones ||--o{ clone_jobs : "created/updated by"
  clones ||--o{ health_checks : "checked by"

  clones {
    TEXT id PK "UUID, also the FileKeystore alias"
    TEXT source_package "NOT NULL, e.g. com.example.chat"
    TEXT clone_package "NOT NULL UNIQUE, e.g. com.example.chat.kembar1"
    INTEGER slot "NOT NULL, n in .kembarN"
    TEXT display_name "NOT NULL, Kembar-side name"
    TEXT custom_icon_file "NULL, app-private file"
    TEXT badge_color "NOT NULL, token name"
    TEXT status "NOT NULL: CREATING | ACTIVE | UPDATE_AVAILABLE | NEEDS_ATTENTION | BROKEN | REMOVING"
    INTEGER source_version_code "NOT NULL, original version the clone was built from"
    TEXT source_version_name "NULL"
    TEXT signer_cert_sha256 "NULL until signed, hex"
    INTEGER created_at "NOT NULL, epoch ms"
    INTEGER updated_at "NOT NULL"
    INTEGER last_launched_at "NULL"
    INTEGER sort_order "NOT NULL"
  }
  clone_settings {
    TEXT clone_id PK "FK -> clones.id ON DELETE CASCADE"
    INTEGER lock_enabled "NOT NULL DEFAULT 0"
    INTEGER network_blocked "NOT NULL DEFAULT 0 (spike S7)"
    INTEGER update_alerts "NOT NULL DEFAULT 1"
  }
  clone_jobs {
    TEXT id PK "UUID = WorkManager work id"
    TEXT clone_id "NOT NULL, FK -> clones.id ON DELETE CASCADE"
    TEXT type "NOT NULL: CREATE | UPDATE | REPAIR"
    TEXT stage "NOT NULL: ANALYZE | COPY | MODIFY | SIGN | INSTALL"
    TEXT state "NOT NULL: QUEUED | RUNNING | WAITING_USER | SUCCEEDED | FAILED | CANCELLED"
    TEXT error_code "NULL, EngineError name"
    INTEGER created_at "NOT NULL"
    INTEGER updated_at "NOT NULL"
  }
  health_checks {
    INTEGER id PK "AUTOINCREMENT"
    TEXT clone_id "NOT NULL, FK -> clones.id ON DELETE CASCADE"
    INTEGER checked_at "NOT NULL"
    TEXT verdict "NOT NULL: HEALTHY | NEEDS_ATTENTION | BROKEN"
    TEXT issues "NOT NULL, JSON array of IssueCode (TypeConverter)"
  }
```

Constraints and indexes:

- `clones`: `UNIQUE(clone_package)`, `UNIQUE(source_package, slot)`, index on `status`.
- `clone_jobs`: index on `clone_id`, `state`.
- `health_checks`: index on `(clone_id, checked_at)`. Keep only the last 10 per clone.

DataStore (`:core:data`): `onboarding_completed`, `disclaimer_accepted_version`,
`theme_mode`, `dynamic_color` (Phase 1). Added later: app lock config (PIN as a PBKDF2 hash
with salt, the record encrypted with a Keystore key), `last_package_sequence` for
`PackageManager.getChangedPackages()`, and `key_backup_reminder_dismissed_at`.

Kembar ships **no analytics SDK**. Crash reports are opt-in and local (share sheet).

The Room DB holds metadata only, so file-based encryption is enough at rest. The secrets
(clone signing keys) are in `FileKeystore`, which is Keystore-wrapped. Exported backups
(settings + keys) are passphrase-encrypted so they can be restored on another device.

## 6. Feature feasibility

| # | Feature | v1 status | Notes |
|---|---------|-----------|-------|
| 1 | App picker + incompatibility hints | ✅ | `<queries>` launcher intent, no `QUERY_ALL_PACKAGES`. Hints: system app, targetSdk < 23, `sharedUserId`, Google services markers, known list. |
| 2 | Staged creation + cancel | ✅ | Matches brief stages. Cancel works until the user confirms the system install dialog. |
| 3 | Launch, rename, icon/badge, pin shortcut, clear data, uninstall, backup | ⚠️ | Rename/icon change Kembar's list + shortcuts. The system label is fixed at creation ("… · Kembar n"). Clear data → App Info (L9). Uninstall → system confirm. Backup = settings + keys (L8). |
| 4 | Multiple clones per app | ✅ | `.kembar1`, `.kembar2`, … each with its own key. |
| 5 | Notifications, network, storage, permissions | ⚠️ | Notifications → system settings for the clone package. Storage via `StorageStatsManager` (needs usage access, optional). Permissions via `PackageInfo.requestedPermissionsFlags` (Kembar can see packages it installed). Network depends on S7. |
| 6 | Health check + repair | ✅ | Checks: clone installed, signer matches the stored key, original still installed, launch intent resolvable, key present in `FileKeystore`. Repair = rebuild + reinstall as an update. |
| 7 | Update awareness | ✅ | `getChangedPackages(sequence)` in a periodic worker → original `versionCode` > clone's `source_version_code` → notification + "Perbarui clone" (rebuild as update, data kept). |
| 8 | App lock | ⚠️ | Guards launches through Kembar and its shortcuts only. A clone opened from the launcher can't be intercepted (stated in UI). |
| 9 | Onboarding, help, explainer, disclaimer | ✅ | |

## 7. Permissions (full rationale table in Phase 6)

| Permission | Why | If denied |
|---|---|---|
| `<queries>` MAIN/LAUNCHER | List launchable apps (Android 11+ package visibility) | n/a |
| `REQUEST_INSTALL_PACKAGES` + user's "Instal aplikasi tidak dikenal" toggle | Install clones | Cloning is impossible; Kembar explains and opens the toggle (`ACTION_MANAGE_UNKNOWN_APP_SOURCES`) |
| `REQUEST_DELETE_PACKAGES` | Uninstall clones from inside Kembar | Falls back to `ACTION_DELETE` |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` | Keep a long clone job alive with a visible notification (local file processing) | n/a (normal permissions) |
| `POST_NOTIFICATIONS` (13+) | Progress, install prompts in background, update alerts | Job continues. Install prompt waits until the user reopens Kembar |
| `USE_BIOMETRIC` | Optional app lock | PIN only |
| `PACKAGE_USAGE_STATS` (optional special access) | App sizes / storage usage | Sizes hidden |
| **Not used:** `QUERY_ALL_PACKAGES`, `INTERNET` (until S7 needs it), any location/contacts/storage permissions | | |
