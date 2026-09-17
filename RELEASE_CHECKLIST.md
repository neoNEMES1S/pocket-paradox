# Pocket Paradox release preparation

Version 0.2.0 is a polish preview, not a Play Store release approval.

## Implemented

- Native movement, undo, room zoom, blocked feedback, goal particles, completion feedback.
- Restrained gradients, raised pieces, persistent outside preview, inline results.
- Eight original synthesized effects; independent audio, haptics, and reduced-motion preferences.
- Three optional authored hints per puzzle and contextual teaching copy.
- Existing 12-level order/geometry and PP1 saves preserved; explicit chapter boundaries.
- In-app explanation of local-only data handling.

## Human playtest gate

Observe new players on the original campaign before adding chapters. Record where they confuse pushing with entering, lose track of the outside room, miss goals, or need the strongest hint. Measure completion time without imposing a target on players. Then add distinct puzzles in small batches toward 24–36; every new puzzle needs a verified winning transcript. Append levels to preserve index-based progress, and test the unlock migration for players who completed the old campaign.

## Device gate

- Physical slower phone: rapid taps/swipes, smooth animation, readable nested-room details, no excessive battery drain while idle.
- High-refresh phone: consistent movement duration and no queued-input lag.
- Android 8 and current Android: Back, system bars, background/resume, process recreation, installation over the previous version.
- Small screen, landscape, 200% font scale, TalkBack, system animations disabled, reduced motion.
- Headphones/speaker: chime loudness, audio focus interruptions, silence when muted/backgrounded.
- Restart, undo across room boundaries, repeated completion, preferences and progress after upgrade.

## Publishing gate — owner information required

- Confirm `com.pocketparadox.game` as the permanent application ID before publishing.
- Supply and securely back up an upload signing key; configure signing outside tracked files. The generated release bundle is unsigned until this is done.
- Decide pricing and intended audience; complete content rating and Play Console declarations.
- Publish a privacy policy with the developer identity and contact address. The in-app explanation alone does not replace the public policy URL.
- Declare the actual data behavior: no network, analytics, ads, accounts, collection, or sharing; local progress/settings only. Revisit declarations if SDKs are added.
- Prepare final icon, feature graphic, description, and truthful screenshots from the approved build.
- Run internal testing, then the closed testing required by the developer account, before applying for production access.

Official references: [testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465), [Data safety](https://support.google.com/googleplay/android-developer/answer/10787469), [target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878).

No upload, public hosting, signing-key creation, or Play Console submission is performed by this preview.
