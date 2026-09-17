# Verification — 2026-09-17 polish preview

Version 0.2.0, version code 2. Original engine, level geometry/order, and PP1 save format remain unchanged.

| Check | Result |
| --- | --- |
| Plain Java checks | Passed all 12 solution transcripts and 24,576 seeded moves |
| Presentation regression checks | Passed interrupted movement, goal event deduplication, undo, cross-room coordinates, room-boundary undo, and instant/reduced-motion positions |
| Debug APK and release AAB | Built with the checked-in fresh-output verification script |
| APK signature | Verified APK Signature Scheme v2 debug signature |
| Release bundle signing | Confirmed unsigned; owner upload credentials still required |
| Android lint | 0 errors, 12 warnings; remaining warnings concern SDK/toolchain freshness, backup metadata, and English-only UI text |
| Emulator campaign | All 12 levels completed through Android input on API 37 ARM64 |
| UI/lifecycle | Hints, independent settings, rotation retaining undo, process restart, restart confirmation, nested-room undo burst, saved progress, and preference persistence passed |
| Visual review | Updated home, portrait play, landscape, nested preview, results, selection, and enlarged-text screenshots |

The emulator test used `com.pocketparadox.game.polishtest`, preserving the existing normal-package installation and its progress. The final layout has Undo, Restart, and Hint in one row, allowing the direction pad to fit in the standard portrait viewport. Small screens with enlarged text remain scrollable.

Reproduce (with a local JDK and Android SDK):

```sh
./check.sh
./gradlew -I tools/verification.gradle :app:assembleDebug :app:lintDebug :app:bundleRelease
./gradlew -I tools/verification.gradle -PisolatedUiTest :app:assembleDebug
adb -s emulator-5554 install -r build/polish-verification/app-ui/outputs/apk/debug/app-debug.apk
POCKET_TEST_PACKAGE=com.pocketparadox.game.polishtest python3 tests/android_smoke.py /path/to/adb emulator-5554 --reset-test-data
```

The existing default `app/build` cache contains an unreadable `merger 2.xml`; the default build still fails on that pre-existing cache. An attempted recoverable move timed out without moving it. Fresh outputs under `build/polish-verification/` avoid the issue; the verification script is checked in so the successful build is reproducible.

Deliverables: `dist/pocket-paradox-0.2.0-debug.apk`, `dist/pocket-paradox-0.2.0-unsigned.aab`, and updated `dist/screenshots/`.

Not verified: physical-device frame pacing/battery use, audible sound quality (emulator ran without host audio), audio interruption behavior on a phone, TalkBack usability, runtime on API 26/36, or human difficulty/onboarding feedback. Campaign expansion remains behind that human playtest gate. See `RELEASE_CHECKLIST.md` for publishing requirements; nothing has been submitted to Google Play.

## Historical verification — 2026-09-12

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
