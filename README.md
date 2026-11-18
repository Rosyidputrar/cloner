# Kembar

Kembar is an Android app for running more copies of apps you already have installed (for
example a second Telegram account). Each clone is a separate app with its own package name,
signing key, data, login, cache and notifications, and is always visibly marked as a clone.

Status: **Phase 1 done** (project setup + design system). The clone engine arrives in Phase 3.

## Build

Requirements: JDK 17 (Temurin), Android SDK platform 34 + build-tools 34.0.0.

```bash
echo "sdk.dir=/path/to/Android/sdk" > local.properties
./gradlew assembleDebug testDebugUnitTest lintDebug detekt ktlintCheck
```

Details, device test steps and the checklist are in [docs/phase-1.md](docs/phase-1.md).

## Documents

- [ADR-0000: Product decisions](docs/adr/0000-product-decisions.md)
- [ADR-0001: Clone engine](docs/adr/0001-clone-engine.md) (accepted: repackaging engine)
- [Architecture: modules, pipeline, data model](docs/architecture.md)
- [Screens (wireframes)](docs/screens.md)
- [Risk register](docs/risk-register.md)
- [Roadmap](docs/roadmap.md)
- [Phase 1 report](docs/phase-1.md)
