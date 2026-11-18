# ADR-0001: Clone engine

- Status: **Accepted**, engine B (repackaging)
- Date: 2026-11-18
- Deciders: project owner
- History: first proposed D (managed work profile). The owner chose clones that live
  **outside an isolated profile**, built with a repackaging toolchain (AxmlPatcher,
  ArscPatcher, StringPool, AlignedZipWriter, CloneSigner, FileKeystore, apksig).
  The original analysis is kept below so the trade-offs stay on record.
- Related: [0000 product decisions](0000-product-decisions.md), [architecture](../architecture.md), [risk register](../risk-register.md)

## 1. Context

Kembar lets a user run more instances of an app that is already installed (separate data,
login, cache, notification identity). The engine creates those instances. It decides
compatibility, security, policy exposure and most of the maintenance cost.

Constraints from the brief and [ADR-0000](0000-product-decisions.md):

- Android 10 (API 29) and newer; no root.
- No bypassing of licensing, payments, ads, DRM, root/emulator detection, anti-tamper or
  anti-cheat; no device-identifier spoofing; **no modification of app code (DEX)**.
- A clone must always be visibly a clone.
- Several clones of the same app must be possible.
- Clones are normal apps in the user's main profile, not inside a work profile.

## 2. Options

| ID | Engine | One-line description |
|----|--------|----------------------|
| A | Virtualization / sandbox | Guest APK runs inside Kembar's process with hooked framework services (VirtualApp style). |
| B | Repackaging | Copy the installed base + split APKs, rewrite package name, provider authorities, custom permissions and `sharedUserId` in the binary manifest and resource table, re-sign with a Kembar-generated key, install as a separate app. |
| C | Hybrid B + A | Repackage by default, virtualization fallback. |
| D | Managed (work) profile | Run the unmodified app in an Android managed profile owned by Kembar (`installExistingPackage`). |

## 3. Analysis

Legend: ✅ works by design · ⚠️ works with caveats · ❌ does not work or not acceptable.

| Dimension | A. Virtualization | B. Repackaging | C. Hybrid | D. Managed profile |
|---|---|---|---|---|
| **Feasibility on Android 10–15** | ❌ Hidden-API reflection into `ActivityThread`/`IActivityManager`; hidden-API restrictions tighten every release; Android 14 requires dynamically loaded code to be read-only. | ⚠️ Feasible. Installed APKs (`ApplicationInfo.sourceDir`, `splitSourceDirs`) are world-readable. Every first install needs user confirmation via `PackageInstaller`. Android 14 refuses apps with `targetSdkVersion` < 23 (`INSTALL_FAILED_DEPRECATED_SDK_VERSION`). | ❌ | ✅ Public APIs. |
| **Split APK / app bundles** | ⚠️ | ⚠️ Every installed split is patched and signed with the same key, and all go into one `PackageInstaller` session. Splits not on the device (on-demand features) are missing. | ❌ | ✅ |
| **Native libs (ABI, 16 KB pages)** | ⚠️ | ⚠️ Libraries stored uncompressed (`extractNativeLibs=false`) must stay STORED and page-aligned. `AlignedZipWriter` aligns `.so` entries to 16 KB (valid for 4 KB and 16 KB page devices). All ABIs are kept as they are. | ❌ | ✅ |
| **ContentProvider authority conflicts** | ⚠️ | ⚠️ Authorities are renamed, otherwise install fails with `INSTALL_FAILED_CONFLICTING_PROVIDER`. Code that builds authorities from `getPackageName()` keeps working. Code that uses a compile-time constant (`BuildConfig.APPLICATION_ID`) breaks, because Kembar does not rewrite DEX. | ❌ | ✅ |
| **FileProvider** | ⚠️ | ⚠️ Works when the app derives the authority from `getPackageName()`. Breaks when it is hardcoded (sharing photos/files fails). | ❌ | ✅ |
| **Firebase / Google Play services** | ❌ | ❌ usually. GMS sees the new package + new certificate. API keys / OAuth clients restricted to the original package + SHA-1 reject it: Google Sign-In fails (`DEVELOPER_ERROR`), Firebase calls can be refused. | ❌ | ✅ |
| **Apps that verify their own signature** | ❌ | ❌ They refuse to run or crash. Some (WhatsApp, Meta apps) may treat it as an unofficial client: **account-ban risk**. Bypassing is out of scope. | ❌ | ✅ |
| **Play Integrity** | ❌ | ❌ `appRecognitionVerdict = UNRECOGNIZED_VERSION`. | ❌ | ✅ |
| **Push (FCM identity)** | ❌ | ❌ usually. The token belongs to the new package and cert, which the app's Firebase project doesn't know, so pushes may not arrive. | ❌ | ✅ |
| **WebView data directory** | ⚠️ needs `setDataDirectorySuffix` | ✅ separate app | ⚠️ | ✅ |
| **Scoped storage** | ⚠️ | ✅ native per-app | ⚠️ | ✅ |
| **Background limits** | ❌ shared UID | ✅ native per-app | ❌ | ✅ |
| **Foreground service types (14+)** | ❌ | ✅ the app's own declarations are kept | ❌ | ✅ |
| **Battery / performance** | ⚠️ | ✅ at runtime. Creating a clone costs CPU/IO for 30 s–3 min, and each clone stores a full APK copy (often 100–300 MB). | ⚠️ | ✅ |
| **Isolation** | ❌ shared UID | ✅ own UID per clone, plus a separate signing key per clone | ❌ | ✅ |
| **Maintainability** | ❌ | ⚠️ Binary formats (AXML, ARSC, ZIP, APK signing) are stable and well documented. Per-app breakage is reported, not patched. | ❌ | ✅ |
| **Google Play policy risk** | ❌ high | ❌ high (`REQUEST_INSTALL_PACKAGES` purpose, distribution of modified third-party apps). **Sideload only.** | ❌ | ⚠️ |
| **Multiple clones of one app** | ✅ | ✅ one package name per clone | ✅ | ❌ one profile per device |
| **Clone data backup/export** | ✅ | ❌ Kembar can't read another app's private data | ⚠️ | ❌ |
| **Native code (NDK) needed** | Yes | **No** | Yes | No |

## 4. Decision

**Engine B, repackaging, is the only engine.** It was chosen by the owner.

It meets the hard requirements D can't meet: several clones per app, and clones that are
ordinary apps outside an isolated profile. It needs no NDK and no hidden APIs. It does not
touch app code: only `AndroidManifest.xml` (binary XML), `resources.arsc` (package name),
signature files and the ZIP layout are changed.

A and C stay rejected (shared-UID isolation, hidden-API fragility, NDK hooks).

### 4.1 What Kembar modifies, and what it never modifies

| Modified | Never modified |
|---|---|
| `<manifest package>` → clone package name | DEX bytecode (`classes*.dex`) |
| `android:authorities` of every `<provider>` | Native libraries |
| Custom `<permission>` / `<permission-group>` names and every reference to them (`uses-permission`, `android:permission`, `readPermission`, `writePermission`) | Assets, images, layouts |
| `android:sharedUserId` / `sharedUserLabel` / `sharedUserMaxSdkVersion` (removed) | Version code / version name |
| `android:taskAffinity` values that start with the original package | Anything related to licensing, ads, integrity, root detection |
| `<category>` / `<action>` values equal to the original package (legacy GCM) | `android:debuggable` (never added) |
| Application and launcher-activity `android:label` → "{label} · Kembar {n}" (visible clone marking) | |
| Relative component class names → fully qualified with the **original** package (code stays in its original Java package) | |
| `resources.arsc` package name (so `getIdentifier(…, getPackageName())` resolves) | |
| `META-INF` v1 signature files (removed), new v2 + v3 signature | |

Rename rule for authorities, permissions and affinities:

- If the value starts with the original package name, that prefix is replaced by the
  clone package name.
- Otherwise, the suffix `.kembar{n}` is added.

Clone package name: `{original}.kembar{n}`, with `n` = 1, 2, 3, ….

## 5. Consequences: what will NOT work (honest list)

| # | Limitation | Cause |
|---|-----------|-------|
| L1 | **Apps that check their own signature** refuse to run or crash. | Kembar never bypasses tamper checks. |
| L2 | **Google Sign-In, many Firebase features and FCM push likely fail** in clones of apps whose Google/Firebase credentials are bound to package + SHA-1. This includes most big apps. | GMS verifies package name and certificate. |
| L3 | **Apps that enforce Play Integrity** (banking, e-wallets, some games) refuse to run. | Clone is not the Play-recognized binary. |
| L4 | **WhatsApp / WhatsApp Business / Meta apps: account-ban risk**, because a re-signed client can look like an unofficial client. The app warns before cloning them and recommends their built-in multi-account features. | App provider policy. |
| L5 | **Hardcoded authorities/FileProvider** break file sharing in some clones. | No DEX rewriting. |
| L6 | **Each install and uninstall shows a system confirmation**, and the user must allow "Instal aplikasi tidak dikenal" for Kembar. Android 12+ can skip the prompt for *updates* of clones Kembar installed (`setRequireUserAction(USER_ACTION_NOT_REQUIRED)`); see spike S5. | Android installer security. |
| L7 | **Play Protect may warn about or block** a repackaged copy of a well-known app. Kembar explains this and never tells users to turn Play Protect off. | Play Protect scans sideloaded installs. |
| L8 | **Clone data can't be backed up or exported** by Kembar. Backup = Kembar's settings and clone signing keys (encrypted). | No API to read another app's private data. |
| L9 | **Clearing a clone's data** opens the system App Info page. Kembar can't clear it silently. | `CLEAR_APP_USER_DATA` is a signature permission. |
| L10 | **Updating a clone means rebuilding and reinstalling it** from the new original. That costs time, disk and an install confirmation unless L6's exemption applies. A clone can't be updated after the clone's signing key is lost: it must be uninstalled, and its data is lost with it. | Android update rule: same package + same signer. |
| L11 | **Disk usage**: each clone stores its own APK copy. Creating one needs about 2× the APK size in free space temporarily. | APK files are per package. |
| L12 | **System apps and apps with `targetSdkVersion` < 23 can't be cloned.** | Install rules + privileged permissions. |
| L13 | **On-demand feature modules and asset packs** that aren't installed are missing in the clone. | Only installed splits can be copied. |
| L14 | **Not distributable on Google Play.** | Policy (see table). |

## 6. Uncertainties and spikes

| Spike | Question | Pass criterion | Blocks |
|-------|----------|---------------|--------|
| S1 | Do `AxmlPatcher` + `ArscPatcher` + `AlignedZipWriter` + `CloneSigner` produce APKs that pass `apksigner verify`, `zipalign -c -P 16 -v 4` and install on API 29–35 for fixture apps (single APK, split APKs, `extractNativeLibs=false`)? | Host JVM tests green with build-tools 34 tools. Fixtures install on an emulator. | Phase 3 |
| S2 | Does apksig 8.5.2 keep the 16 KB alignment written by `AlignedZipWriter` (`setAlignmentPreserved(true)`)? | `zipalign -c -P 16` passes after signing. | Phase 3 |
| S3 | Which real apps run as clones? Telegram, Facebook Lite, Shopee, Tokopedia, Instagram, WhatsApp (warn only, test account), one banking app (expected to fail). | Results recorded in `docs/compat-results.md` and used for the in-app compatibility list. | Phase 3 sign-off |
| S4 | Does BouncyCastle `bcpkix` (self-signed certificate generation) work under R8 on API 29–35, or is a small hand-written X.509 DER encoder simpler? | Key + cert generated in < 1 s and accepted by apksig. | Phase 3 |
| S5 | When does Android 12+ skip the confirmation for clone updates (`USER_ACTION_NOT_REQUIRED`)? It depends on the installer and the clone's targetSdk. | Behaviour documented per API level. | Phase 5 |
| S6 | Can a launcher-icon badge be added by `ArscPatcher` (new drawable entry + layer-list), or should v1 mark clones by label only? | Badge visible on API 29–35 launchers without breaking resources. | Phase 4 |
| S7 | Per-clone network toggle via a local `VpnService` that black-holes selected UIDs: acceptable UX given it occupies the single VPN slot? | Works, and the conflict with a user VPN is explained. | Phase 5 |

## 7. Signing-key strategy

- **One key per clone** (EC P-256, alias = clone id), generated on the device when the clone is
  created. Separate keys mean clones can't use signature-level permissions to reach each
  other, and losing one key affects one clone only.
- Keys live in **`FileKeystore`**: a PKCS#12 file in `noBackupFilesDir`. Its password is
  random (32 bytes) and stored encrypted with a non-exportable **Android Keystore**
  AES-256-GCM key. The PKCS#12 file is needed because apksig needs a signing key and an
  X.509 certificate chain. A file is also exportable, while keys inside Android Keystore are not.
- **Losing the key = the clone can no longer be updated** (L10). This happens when Kembar
  is uninstalled or its data is cleared. So Kembar offers an **encrypted key backup**
  (PKCS#12 re-wrapped with a user passphrase → AES-256-GCM, PBKDF2-HMAC-SHA256, 310 000
  iterations). The key backup is shown as important, with an explanation.
- Signatures: APK Signature Scheme v2 + v3, `minSdkVersion` for signing = 29 (no v1 needed).
  Old `META-INF/*.SF|*.RSA|*.DSA|*.EC|MANIFEST.MF` entries are dropped.
- Kembar's **own** release key is separate: kept offline, provided to CI through encrypted
  secrets in Phase 7, signed v2 + v3.
- Re-signing risks are shown to the user before the first clone: Google services may not
  work, accounts may be flagged by some providers, and updates depend on the backup.

## 8. Tool usage

| Tool | Where it runs | Purpose |
|------|--------------|---------|
| JDK Temurin 17, Gradle 8.7 (wrapper), AGP 8.5.2, Kotlin 2.0.20 | Build host / CI | Build Kembar |
| Android SDK platform 34, build-tools 34.0.0 | Build host / CI | compile/target SDK 34 |
| `aapt2` | Build host (tests) | Compile fixture APKs for engine tests |
| `apksigner`, `zipalign` | Build host (tests) | Independently verify the engine's output (`apksigner verify -v`, `zipalign -c -P 16 -v 4`) |
| `d8` | Build host (tests) | Compile fixture classes into DEX for fixture APKs. **Never used on-device; Kembar does not rewrite DEX.** |
| `keytool` | Build host | Create Kembar's own release keystore |
| apksig 8.5.2 | **On device** (library) | Sign clones (inside `CloneSigner`) |
| AxmlPatcher, ArscPatcher, StringPool, AlignedZipWriter, CloneSigner, FileKeystore, CloneEngine | **On device** (`:engine:impl`) | The pipeline, see [architecture](../architecture.md) |
