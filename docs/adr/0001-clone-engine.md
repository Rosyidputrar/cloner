# ADR-0001: Clone engine

- Status: **Proposed** (awaiting owner confirmation)
- Date: 2026-11-18
- Deciders: project owner
- Related: [0000 product decisions](0000-product-decisions.md), [architecture](../architecture.md), [risk register](../risk-register.md)

## 1. Context

Kembar lets a user run a second, isolated instance of an app that is already installed
(separate data, login, cache, notification identity). The engine is the part that creates
and runs that second instance. It decides compatibility, security, policy exposure and most
of the maintenance cost, so it is decided before any code is written.

Constraints that come from the brief and from [ADR-0000](0000-product-decisions.md):

- Android 10 (API 29) to the latest stable release; no root.
- No bypassing of licensing, payments, ads, DRM, root/emulator detection, anti-tamper or
  anti-cheat; no device-identifier spoofing.
- A clone must always be visibly a clone.
- Distribution starts as a sideloaded release (GitHub Releases), but the engine should not
  make a later Google Play listing impossible.

## 2. Options

| ID | Engine | One-line description |
|----|--------|----------------------|
| A | Virtualization / sandbox | Guest APK is loaded inside Kembar's own process; framework services (AMS, PMS, ContentResolver, etc.) are proxied and file paths are redirected (VirtualApp / VirtualXposed style). |
| B | Repackaging | Copy the installed base + split APKs, rewrite the package name, provider authorities, custom permissions and `sharedUserId`, re-sign with a Kembar key, and install as a separate app. |
| C | Hybrid B + A | Repackage by default, fall back to virtualization when repackaging fails. |
| D | Managed (work) profile | Kembar provisions an Android managed profile and becomes its profile owner. The **unmodified, originally signed** app is made available in that profile with `DevicePolicyManager.installExistingPackage()` (API 28). Android itself provides a separate user ID, data directory, accounts, notifications and launcher badge. |

Option D was not in the original brief. It is added because it is the only approach that
uses a public, documented platform mechanism for running a second, isolated copy of an app.
Shelter and Insular (and Island before them) are long-running open-source apps that use it.

## 3. Analysis

Legend: ✅ works by design · ⚠️ works with caveats · ❌ does not work or not acceptable.

| Dimension | A. Virtualization | B. Repackaging | C. Hybrid | D. Managed profile |
|---|---|---|---|---|
| **Feasibility on Android 10–16** | ❌ Relies on reflection into hidden framework classes (`ActivityThread`, `IActivityManager`, `IPackageManager` proxies). Hidden-API restrictions (since API 28) get stricter every release, so every OS release breaks it. Android 14 requires dynamically loaded code files to be read-only (`DCL` restriction for targetSdk ≥ 34). | ⚠️ Technically feasible (APK files under `ApplicationInfo.sourceDir` / `splitSourceDirs` are world-readable). Every install needs user confirmation via `PackageInstaller`. Android 14 blocks installing apps with `targetSdkVersion` < 23. | ❌ Inherits A's breakage. | ✅ Public APIs: `ACTION_PROVISION_MANAGED_PROFILE` (API 21), `installExistingPackage` (API 28), `LauncherApps` multi-profile (API 21/28), `CrossProfileApps` (API 28). Needs `PackageManager.FEATURE_MANAGED_USERS`, which most Android Go / low-RAM builds do not have. |
| **Split APK / app bundles** | ⚠️ Must merge splits into the guest ClassLoader/resources by hand. | ⚠️ Every split's binary manifest must be rewritten. All splits must be re-signed with the same key and installed in one `PackageInstaller` session. Only splits already on the device exist; on-demand feature modules are missing. | ❌ | ✅ Not applicable. The same installed package (all splits) is shared with the profile user. |
| **Native libs (ABI, 16 KB pages)** | ⚠️ Host must load guest `.so` files. Host ABI must match guest ABI. Guest libs that are not 16 KB-aligned fail on 16 KB-page devices (Android 15+). | ⚠️ Must keep `extractNativeLibs=false` libs **stored uncompressed and page-aligned** in the rewritten zip (zipalign `-P 16`). Getting this wrong gives `INSTALL_FAILED_INVALID_APK` or load failures. | ❌ | ✅ Not applicable. The original install is untouched. |
| **ContentProvider authority conflicts** | ⚠️ Host must fake providers. | ⚠️ Authorities must be renamed or install fails with `INSTALL_FAILED_CONFLICTING_PROVIDER`. But app code calls hardcoded authority strings (`BuildConfig.APPLICATION_ID + ".provider"` is compiled in as a constant), so the manifest rename alone breaks runtime calls unless the DEX is rewritten too. | ❌ | ✅ No conflict. Authorities are unique per user. |
| **FileProvider** | ⚠️ Same as above. | ❌ Usually breaks: `FileProvider.getUriForFile(ctx, "<old authority>", f)` throws because the authority no longer exists. Fixing it means rewriting DEX string constants, which is code modification of a third-party app. | ❌ | ✅ Works unchanged. |
| **Firebase / Google Play services** | ❌ GMS sees Kembar's UID and signature, not the guest's. | ⚠️/❌ GMS checks package name + signing cert. Firebase API keys and OAuth clients are often restricted to the original package + SHA-1, so Google Sign-In fails (`DEVELOPER_ERROR`) and Firebase calls can be rejected. | ❌ | ✅ Same package and signature. GMS runs in the profile. The user may need to add a Google account inside the profile for Sign-In. |
| **Apps that verify their own signature** | ❌ Detect the host. Bypassing is out of scope (and forbidden by the brief). | ❌ Refuse to run, crash, or (for WhatsApp, Meta apps) may flag the account as using an unofficial client. **Risk of account bans for the user.** | ❌ | ✅ Signature is the original one. |
| **Play Integrity** | ❌ `appIntegrity` fails. | ❌ `appRecognitionVerdict` = `UNRECOGNIZED_VERSION`. Apps that enforce it will refuse to work. | ❌ | ✅ App is the Play-installed copy, so `PLAY_RECOGNIZED` is expected. To verify in spike S4. |
| **Push notifications (FCM identity)** | ❌ Token is bound to the host package. Every guest shares one identity. | ⚠️/❌ Token is requested for the new package and cert. The app's server-side Firebase project usually doesn't know that package, so pushes don't arrive. | ❌ | ✅ Each profile gets its own FCM token for the original package. Enterprise work apps rely on this. |
| **WebView data directory** | ⚠️ All guests share one process family. Needs `WebView.setDataDirectorySuffix()` (API 28) per guest process or WebView crashes. | ✅ Separate app, separate data dir. | ⚠️ | ✅ Separate user, separate data dir. |
| **Scoped storage** | ⚠️ Guest file paths must be redirected. MediaStore entries are attributed to the host. | ✅ Native per-app behaviour. | ⚠️ | ✅ Native. Shared storage is per user, so the clone sees the profile's own `/sdcard`. File sharing with the personal side goes through the system's cross-profile pickers. |
| **Background execution limits** | ❌ All guests run under the host UID. Standby buckets, Doze and OEM killers treat them as one app. | ✅ Native per-app. | ❌ | ✅ Native per-app, per-user. |
| **Foreground service types (Android 14+)** | ❌ Host would have to declare every FGS type any guest uses. Play requires a justification for each. | ✅ Guest manifest declarations are kept. | ❌ | ✅ Untouched. |
| **Battery / performance** | ⚠️ Low memory overhead. Hooking costs CPU, and one guest crash can kill all of them. | ✅ Same as a normal app, plus one extra APK on disk per clone (often 100–300 MB). | ⚠️ | ✅ No extra APK copy (code is shared between users). The extra user adds a small fixed RAM cost. |
| **Isolation / security** | ❌ All guests share Kembar's UID. A malicious guest can read every other guest's data and Kembar's own data. | ✅ Separate UID. | ❌ | ✅ Separate Linux user and UID, enforced by the kernel and framework. |
| **Maintainability** | ❌ Breaks every Android release and with OEM framework changes. Needs a native IO-redirect layer (NDK). | ⚠️ Large surface: AXML/ARSC rewriting, zip alignment, apksig, per-app fixes. Breaks whenever target apps change their manifest or self-checks. | ❌ Both. | ✅ Small, documented API surface. The main maintenance work is OEM quirks. |
| **Google Play policy risk** | ❌ High. Device and Network Abuse policy covers apps that interfere with other apps or run code not in the APK. | ❌ High. Needs `REQUEST_INSTALL_PACKAGES` + `QUERY_ALL_PACKAGES` declarations; installs modified third-party apps (IP/ToS issues). | ❌ | ⚠️ Low to medium. No sensitive permissions; uses public enterprise APIs. Profile-owner apps for consumers exist, but Play review is never guaranteed. |
| **Multiple clones of one app** | ✅ | ✅ (with a different package name per clone) | ✅ | ❌ **At most one extra instance per app.** Android allows one managed profile per device. |
| **Access to clone data (backup/export)** | ✅ Data lives in Kembar's own directory. | ❌ The clone is a separate app; Kembar can't read its private data. | ⚠️ | ❌ Kembar's profile instance can clear an app's data (`clearApplicationUserData`, API 28) but cannot read it. |
| **Native code (NDK) needed** | Yes (syscall / libc hooks for path redirection) | No (apksig is pure Java) | Yes | **No** |

## 4. Decision

**Adopt D (managed profile) as the only engine for v1.**

Keep an `:engine:api` abstraction (`CloneEngine`) so a second engine could be added later,
but only if a spike shows it would actually work for real target apps without breaking the
ethical constraints. On current evidence B would not pass that bar for the apps users care
about most.

Rationale:

1. **It's the only option where the apps users want (WhatsApp, Instagram, Telegram, Google-backed apps)
   are expected to work fully**, including push, Sign-In and Play Integrity. A, B and C break
   these by design, and fixing them would need exactly the bypasses the brief forbids.
2. **It doesn't modify third-party apps.** The clone runs the original, signed APK. That
   avoids IP/ToS problems and the account-ban risk of looking like a modded client.
3. **Isolation is enforced by the OS** (separate Linux user), not by hooks in our process.
4. **Smallest maintenance surface and no NDK.**
5. **Lowest policy exposure:** no `QUERY_ALL_PACKAGES`, no `REQUEST_INSTALL_PACKAGES`, no hidden APIs.
6. **Visibility is built in:** Android adds the work-profile badge to the clone's icon,
   notifications and recents. Kembar adds its own badge on top for shortcuts.

## 5. Consequences: what will NOT work (honest list)

| # | Limitation | Cause | Impact on brief |
|---|-----------|-------|-----------------|
| L1 | **Only one clone per app** (no clone 2, 3, …). | Android supports one managed profile per device. `DevicePolicyManager.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE)` returns false when one exists. | Feature 4 (multiple clones per app) is **out of v1**. The schema keeps a `slot` column so a future engine can add more. |
| L2 | **Not available if the device already has a work profile** (employer MDM, Shelter, Insular, some OEM features). | Same one-profile limit. | Kembar detects this and explains it on an Indonesian error screen. |
| L3 | **Not available on devices without `FEATURE_MANAGED_USERS`** (most Android Go / low-RAM devices, some OEM builds). | OEM/platform build choice. | Detected on first run. |
| L4 | **Clone app data cannot be backed up or exported.** | No API lets a profile owner read another app's private data. Root would be needed. | Feature 3 "export/backup clone data" becomes **"backup Kembar settings"** (clone list, names, icons, per-clone settings) in a passphrase-encrypted file. Users are pointed to each app's own backup (e.g. WhatsApp chat backup). |
| L5 | **Renaming/re-iconing only changes Kembar's list and pinned shortcuts**, not the label in the system launcher's work tab. | Labels come from the app's own manifest. | Feature 3 partially met. Said plainly in the UI. |
| L6 | **App lock can be bypassed by opening the clone from the launcher's work tab.** | Kembar is not the launcher. | Kembar offers to turn on Android's own **work profile lock** (`DevicePolicyManager` work challenge) as the strong option. Kembar's biometric lock only guards launches through Kembar and its shortcuts. |
| L7 | **Per-clone notification toggle can't be done silently.** | No profile-owner API toggles another app's notification channels. | Kembar opens the clone's system notification settings inside the profile. |
| L8 | **Per-clone network toggle needs an in-profile VPN** (`VpnService` that black-holes selected apps, set as always-on by the profile owner). Not confirmed yet; see spike S5. | No direct per-app firewall API without root. | Feature 5 network toggle is **conditional on S5**. Fallback: suspend the clone (`setPackagesSuspended`). |
| L9 | **Pausing the work profile turns off all clones at once.** | `UserManager.requestQuietModeEnabled` is restricted to the default launcher / privileged callers. | Kembar shows the paused state and links to system settings. |
| L10 | **Original app updates apply to the clone automatically** (shared code). There is no separate "update clone" step, and the clone **cannot stay on an older version**. | Package code is shared across users. | Feature 7 becomes "notify that the clone was updated and run a health check". |
| L11 | **Removing the profile (or Kembar's profile owner) deletes every clone and its data.** | User removal wipes the user. | Shown with a strong confirmation dialog. |
| L12 | **Some apps deliberately refuse to run in a work profile** (some banking/e-wallet apps check `UserManager.isManagedProfile()`). | App policy. Bypassing is forbidden. | Listed on "Aplikasi yang tidak bisa di-clone" and flagged as "kemungkinan tidak kompatibel" where known. |
| L13 | **Some OEM builds have unreliable work-profile support** (historically some Xiaomi MIUI/HyperOS builds, Huawei builds without GMS). | OEM modifications. | Covered by spike S1 on real devices. A per-OEM help article. |
| L14 | **Clone-creation stages differ from the brief.** There is no copy / modify / sign step. | No APK is rewritten. | Stages become: **Analisis → Siapkan ruang → Pasang → Atur → Verifikasi**. |

## 6. Uncertainties and spikes

Each spike is a short, throwaway experiment on real devices. It has a pass/fail criterion
and runs before the feature that depends on it is built.

| Spike | Question | Pass criterion | Blocks |
|-------|----------|---------------|--------|
| S1 | Does managed-profile provisioning by Kembar succeed on target devices (Pixel/AOSP, Samsung One UI, Xiaomi HyperOS, Oppo ColorOS, Vivo FuntouchOS/OriginOS) on Android 10–16? | Profile created, Kembar is profile owner, survives reboot. | Phase 3 |
| S2 | What is the most reliable command channel from the personal-side Kembar to its profile-side instance? Options: (a) `addCrossProfileIntentFilter` + translucent activity returning a result, (b) `DevicePolicyManager.setCrossProfilePackages` (API 30) + `CrossProfileApps.startActivity`. | Round-trip command with result in < 1 s on S1 devices. Works on API 29 via (a). | Phase 3 |
| S3 | Does `installExistingPackage` work for Play-installed apps that use split APKs and 16 KB-aligned native libs (WhatsApp, Instagram, Telegram, Shopee, Tokopedia)? | Returns true. App launches from the profile. | Phase 3 |
| S4 | Do push notifications, Google Sign-In and Play Integrity work for those apps in the profile? | Push arrives on a test account. Sign-In succeeds after adding a Google account in the profile. | Phase 3 sign-off (documented results only) |
| S5 | Can the profile owner run an always-on, black-hole `VpnService` in the profile to cut network for selected clones, without a consent dialog? | Selected clone has no network; others unaffected. | Phase 5 network toggle |
| S6 | Can the profile-side instance read per-app storage stats (`StorageStatsManager.queryStatsForPackage`) without asking the user for usage access? | Returns sizes. Otherwise fall back to a "Lihat di Pengaturan" link. | Phase 5 storage usage |
| S7 | Does uninstalling a package from inside the profile (`PackageInstaller.uninstall`) by the profile owner run silently on API 29–36? | Silent uninstall, or a clear user prompt as fallback. | Phase 4 |

## 7. Rejected alternatives (summary)

- **A (virtualization)** is rejected. It has no real isolation, breaks with every OS release,
  needs NDK hooks and hidden-API workarounds, carries high Play policy risk, and breaks
  push/Sign-In/Integrity by design.
- **B (repackaging)** is rejected for v1. It breaks FileProvider/authorities unless DEX code
  is rewritten. It fails signature-checking apps, Play Integrity and Firebase/Sign-In
  restrictions. It risks bans for users of WhatsApp/Meta apps, needs install confirmation
  per clone, and carries high policy and IP risk. It might be revisited only as an
  explicitly experimental engine for apps that pass a compatibility spike.
- **C (hybrid)** is rejected. It combines the weaknesses of A and B.

## 8. Signing-key strategy

With D, **Kembar never re-signs any third-party app.** Clones keep the original developer
signature. Kembar's own release key is the only key in scope:

- Kept outside the repository. CI reads it from encrypted GitHub Actions secrets in Phase 7.
- APK signature schemes v2 + v3 (v3 enables future key rotation). v1 is not needed because minSdk ≥ 24.
- **Losing the key means users can't update Kembar.** Because Kembar is the profile owner,
  installing a differently signed Kembar would mean removing the profile and every clone.
  The key must be backed up offline. Key rotation uses the APK Signature Scheme v3
  lineage, never "uninstall and reinstall".
