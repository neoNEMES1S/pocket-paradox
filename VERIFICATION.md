# Verification — 2026-09-12

Build 0.1.0, package `com.pocketparadox.game`, Android 8.0+ (minimum API 26), target/compile API 36.

| Check | Result |
| --- | --- |
| Debug APK assembly | Passed with JDK 21, Java 17 source target, Gradle 8.14.3, AGP 8.13.0 |
| APK signature | Valid APK Signature Scheme v2 debug signature |
| Requested Android permissions | None |
| Engine checks | Passed: pushing, transactional rollback, centered doors, nested containment, undo, goals, persistence validation |
| Seeded invariant checks | 24,576 moves across all 12 puzzles; failed moves preserve state, undo is exact, save/restore and replay agree |
| Campaign solutions | All 12 recorded solutions replay successfully, 3–36 moves each |
| Android lint | Passed: 0 errors, 5 warnings |
| Emulator campaign | All 12 levels solved through actual Android input, with sequential unlocking and best scores |
| UI/lifecycle | Passed touch controls, swipes, keyboard arrows, Back, current-level resume, restart confirmation, rotation retaining undo, process restart restoring positions/progress |
| Layout | Inspected portrait, landscape, nested-room overview, completion, and 360×568dp with 200% font scale; small/enlarged layouts scroll |

Runtime checks used an Android 15 / API 35 ARM64 AOSP emulator. Screenshots are in `dist/screenshots`. Android 16's platform Back callback is implemented and lint-checked, but an API 36 runtime and physical devices were not tested.

Remaining lint warnings concern a newer available Gradle patch, modern Android backup configuration, and English strings not extracted for localization. They do not block the development build. The app is English-only.

This build is a finite-nesting foundation. Cyclic/self-containing boxes, infinity spaces, audio, a larger campaign, release signing, and store publication remain outside this milestone. No external game code, levels, music, or artwork were copied.

Reproduce the engine/campaign checks with `./check.sh`, static checks with `./gradlew :app:lintDebug`, and the emulator checks with `tests/android_smoke.py` as described in the README.
