# Pocket Paradox

An original Android puzzle-game foundation inspired by the boxes-within-boxes idea in [Patrick's Parabox](https://www.patricksparabox.com/). All code, puzzles, and drawn artwork in this project are new.

## Play

Build the debug APK using the instructions below, then install `app/build/outputs/apk/debug/app-debug.apk` on Android 8.0 or newer. In Android Studio, select the `app` Android App run configuration and a device, then click Run to build, install, and launch. Debug APKs are signed with a development key; they are not Play Store releases. Generated APKs and source archives are not tracked in this repository.

Move the coral explorer with the direction buttons, board swipes, arrow keys, or WASD. Push crates or room boxes onto square-ring goals, then finish on the coral cross. A room box moves first; when blocked, its open center doorway lets a piece enter. Walk through a room's centered opening to exit beside its box in the parent room. Moving a room box carries its contents. Undo is also available with Z.

The app includes a home screen, three chapters, 12 original puzzles, sequential unlocking, replay, best move counts, saved in-progress play, touch-feedback settings, and help. It works offline and requests no permissions. No accounts, network services, ads, or analytics.

Progress and current positions persist across app restarts. Undo history survives rotation and other configuration changes, but starts fresh after the process is closed. Selecting the current unsolved puzzle resumes it; Restart explicitly resets it. Reset all progress has a confirmation.

## Build

Use Android SDK platform 36 and an Android SDK location in `ANDROID_HOME` or an untracked `local.properties` file. The checked-in Gradle wrapper uses Gradle 9.6.0 and Android Gradle Plugin 9.4.0; daemon JVM criteria select JetBrains JDK 21, while Java source targets 17. Android Studio can open this directory directly. The first build may download the JDK and build tools.

```sh
export JAVA_HOME=/path/to/your/jdk
export ANDROID_HOME=/path/to/your/android/sdk
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

Build an unsigned release bundle using `./gradlew :app:bundleRelease`; configure your own release signing before publishing. A production application ID, release key, store listing, wider device testing, and a longer campaign are release tasks.

## Check the rules

```sh
./check.sh
./gradlew :app:lintDebug
```

The plain-Java checks exercise pushes, centered entry/exit, blocked-move rollback, containment rejection, goals, undo, and malformed-save validation. They replay a winning transcript for every level and run 24,576 seeded moves, checking save/restore, undo, and deterministic replay. No test framework or runtime dependencies are needed.

An optional UI check runs on a connected **emulator only**, with the APK already installed. It resets this game's data in that emulator and captures screenshots under `dist/screenshots`:

```sh
python3 tests/android_smoke.py /path/to/adb emulator-5580 --reset-test-data
```

## Where to work

| File | Responsibility |
| --- | --- |
| `app/src/main/java/com/pocketparadox/game/Engine.java` | Android-independent movement, room ownership, undo, goals, persistence |
| `app/src/main/java/com/pocketparadox/game/Levels.java` | Original campaign, teaching hints, and solution transcripts |
| `app/src/main/java/com/pocketparadox/game/MainActivity.java` | Native screens, Canvas board, controls, lifecycle, progress |
| `tests/EngineCheck.java` and `tests/LevelCheck.java` | Runnable rule and campaign checks |

Rooms use `#` for walls, `.` for floor, `o` for a box goal, and `@` for the single player goal. Piece 0 is the player. A piece's `inside` field identifies its interior room, or is -1 for an ordinary crate/player. Each nonroot room belongs to exactly one box. Save files include a level fingerprint and reject invalid geometry or containment.

## Scope

This is a playable **finite-nesting foundation**, not full gameplay parity with Patrick's Parabox. It supports movable rooms, crates entering/exiting rooms, and two nested depths in the campaign. Self-containing boxes, eating/squeezing behaviors, duplicate room references, and infinity spaces are not implemented. Those need explicit cyclic spatial rules and their own teaching campaign. Rendering shows three preview depths; it does not impose that limit on finite engine nesting.

Native buttons have accessibility labels, goals use distinct glyphs, and the board describes player/piece/goal coordinates to accessibility services. Portrait pages can scroll on small screens or enlarged fonts, and landscape uses side-by-side play and controls. A dedicated nonvisual puzzle-navigation mode remains future work.

The [official game description](https://www.patricksparabox.com/) and [creator interview](https://www.gamedeveloper.com/design/patrick-s-parabox-/) informed the mechanical scope. No original game files or levels were used.
