# Screens (wireframes)

Wireframe-level descriptions, written before each screen is implemented. All user-facing
text is Indonesian and lives in `strings.xml`. Each screen has loading, empty and error
states where they can occur. Touch targets are at least 48 dp. Text uses `sp`, and layouts
scroll instead of clipping at 200% font scale.

Adaptive layout rule: content that is read (text, forms, lists of settings) is capped at
640 dp wide and centered. The navigation is a bottom bar on compact widths and a
navigation rail on medium/expanded widths (tablets, unfolded foldables). There is no
navigation chrome on first-run screens and detail screens.

## Phase 1

### Splash (system splash, Android 12+ API via `core-splashscreen`)

```
┌───────────────────────────┐
│                           │
│         ┌──┐              │   brand teal background (#006A6A)
│       ┌─┤  │              │   app icon: two overlapping cards
│       │ └──┘              │
│       └──┘                │
│                           │
└───────────────────────────┘
```
- Stays only until preferences are read (normally < 100 ms), then hands over to the start screen.
- No text, no spinner.

### Onboarding (4 slides, horizontal pager)

```
┌───────────────────────────┐
│                  [Lewati] │  text button, hidden on last slide
│                           │
│          ( icon )         │  120 dp circle, primaryContainer
│                           │
│   Satu aplikasi, banyak   │  headlineSmall, heading for TalkBack
│           akun            │
│  Kembar bikin salinan ... │  bodyLarge, onSurfaceVariant, centered
│                           │
│          ● ○ ○ ○          │  page dots; TalkBack: "Halaman 1 dari 4"
│  [        Lanjut        ] │  primary button; "Mulai" on the last slide
└───────────────────────────┘
```
Slides:
1. Satu aplikasi, banyak akun
2. Datanya terpisah
3. Clone selalu kelihatan beda
4. Nggak semua aplikasi bisa

States: none (static content). "Lewati" and "Mulai" both mark onboarding done and open the
Disclaimer. The current slide survives rotation (pager state is saveable).

### Disclaimer ("Sebelum mulai, baca ini dulu ya")

```
┌───────────────────────────┐
│ [←]                       │  only when re-read from Tentang (read-only)
│ Sebelum mulai, baca ini   │  headlineSmall
│ dulu ya                   │
│ Kembar membuat clone ...  │  intro
│                           │
│ (!) Nggak semua aplikasi  │  6 points: icon + title (heading) + body
│     mau jalan             │
│     ...                   │
│ (🔕) Login Google dan ... │
│ (👤) Ada risiko akun ...  │
│ (⚖) Kamu yang menanggung  │
│ (☁) Data di dalam clone   │
│ (✔) Kembar main jujur     │
├───────────────────────────┤  bottom bar (first run only)
│ [☐] Aku sudah baca dan    │  whole row toggles the checkbox (48 dp+)
│     paham risikonya       │
│ [   Setuju dan lanjut   ] │  disabled until checked
└───────────────────────────┘
```
- Error state: if saving fails, a snackbar says "Persetujuanmu belum tersimpan karena
  penyimpanan HP sedang bermasalah. Coba lagi sebentar lagi." The button stays enabled for retry.
- The checkbox state survives process death (SavedStateHandle).
- Bumping `CURRENT_DISCLAIMER_VERSION` shows this screen again to existing users.

### Beranda (Home)

```
┌───────────────────────────┐
│ Beranda                   │  top app bar
│                           │
│         ( ⧉ icon )        │  empty state
│      Belum ada clone      │
│ Clone yang kamu buat ...  │
│                           │
├───────────────────────────┤
│  [⌂ Beranda] [⚙ Pengaturan] │  bottom bar (rail on tablets)
└───────────────────────────┘
```
- Phase 1 only has the empty state. The clone list, the loading state and the
  "Buat clone" FAB come with the app picker (Phase 2) and the clone registry (Phases 3–4).

### Pengaturan (Settings)

```
┌───────────────────────────┐
│ Pengaturan                │
│ TAMPILAN                  │  section header (heading)
│ (•) Ikuti sistem          │  radio group, selectableGroup
│     Terang atau gelap ... │
│ ( ) Terang                │
│ ( ) Gelap                 │
│ 🎨 Warna dinamis     [⏻]  │  only on Android 12+
│    Ambil warna dari ...   │
│ INFO                      │
│ ⓘ Tentang Kembar       >  │
└───────────────────────────┘
```
- Loading state while preferences are read. Changes apply immediately (no save button).

### Tentang (About)

```
┌───────────────────────────┐
│ [←] Tentang Kembar        │
│ Versi 0.1.0               │
│ Kembar bikin salinan ...  │
│ Privasi                   │  heading
│ Kembar nggak mengumpulkan │
│ Bukan aplikasi resmi      │  heading
│ Kembar nggak ada ...      │
│ [  Baca lagi disclaimer ] │  opens Disclaimer read-only
└───────────────────────────┘
```

## Visual clone distinction (design system rule)

- `CloneBadge`: pill "Kembar n" in a fixed magenta (`cloneBadge` token). It is
  intentionally **not** dynamic, so it looks the same on every wallpaper and never blends
  into the app's own colors.
- `BadgedAppIcon`: the app icon with a numbered magenta dot in the bottom-end corner.
  TalkBack reads it as "{nama aplikasi}, clone ke-{n}".
- On-device, the clone's system label is patched to "{label} · Kembar {n}" (Phase 3).
