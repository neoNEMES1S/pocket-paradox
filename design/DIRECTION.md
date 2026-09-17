# Pocket Paradox — Android visual direction

An original, quiet spatial puzzle: small rooms contain smaller rooms. The interface should feel precise and tactile, with the board taking priority over decoration. All artwork is drawn from simple geometry; no Patrick’s Parabox art, names, or level layouts are reused.

## Tokens

| Role | Value |
| --- | --- |
| Window / main background | `#101820` |
| Raised surface | `#1A2733` |
| Board floor | `#223047` |
| Wall | `#35465F` |
| Primary text | `#F3F6FC` |
| Secondary text | `#AFBCD0` |
| Explorer / primary action | `#FF947F` |
| Room box / success | `#9FE3C2` |
| Crate | `#AFA7ED` |
| Goal / selected state | `#FFD789` |
| Text on bright fills | `#101820` |

Use the Android system serif for the wordmark and puzzle titles, and system sans for controls and supporting text. Keep main text at least 4.5:1 contrast. Spacing uses 4dp increments; screen padding 24dp, control gaps 8–12dp, card padding 16–20dp. Surfaces use 16dp corner radii; actions 14dp; board cells 5–8% of their cell size.

The 0.2 polish pass adds a restrained background gradient from `#101820` to `#1B2440`. Movable pieces have a small contact shadow and a fine upper highlight: explorer `#FFB39A` → `#FF887E`, room frames `#B9F2D6` → `#79CFAE`, crates `#C3B8F5` → `#998BDD`. Keep board tiles flat for readability. Gold activation rings and six small square particles appear briefly on newly filled goals; no continuous particles or screen shake.

## Home

Top: small uppercase “A LITTLE SPACE TO THINK” eyebrow, then two-line “Pocket / Paradox” wordmark. A compact nested-square motif in mint, violet, and coral sits in the middle of generous negative space. Beneath: one-line promise “Find a way in. Think your way out.”

Bottom action stack: mint **Continue exploring** (or **Begin exploring** for fresh progress), secondary **Choose a puzzle**, then **How to play** and **Settings**. Derive completion counts from the campaign. No currency, daily streaks, or account controls.

## Puzzle selection

Top bar: 48dp Back control, **Your discoveries**, completion count. Group levels under short chapter labels **First steps**, **Rooms within rooms**, **Deeper thinking**. Each chapter has an understated subtitle explaining its mechanic. Use a three-column numbered grid with cells at least 80dp high; current level has a warm outline, completed levels have a visible checkmark, locked levels have a lock glyph and a muted number. Color never carries the status alone. Scroll vertically; never shrink the grid to fit every level at once.

## Play

Portrait order: Back / chapter label / level count; level title; movement count and a compact “Room 1 → Room 2” breadcrumb when nested; square board; a short contextual hint; controls. Use the available area to fit the board with generous separation from controls, and scale the board down before reducing touch targets.

The floor is navy, walls are slate, crates are violet solids, and enterable room boxes are mint frames with a visible miniature room. The coral explorer has two dark square eyes. Box goals use a gold hollow square with four inset corner ticks; the player goal uses a gold ring plus central dot. Completed goals gain a small checkmark. Show the active room’s outer edge clearly. Miniatures render only enough depth to make nesting legible; avoid tiny full-detail grids.

Place **Undo** and **Restart** in one row immediately below the board or hint. Use four separate 56–64dp native buttons in a cross for movement, with a blank center: Up above Left / blank / Right above Down. Controls should work through taps, swipes, and keyboard arrows; undo and restart stay available throughout play. All controls need content descriptions, visible pressed states, at least 48dp touch targets, and readable disabled states.

## Completion and settings

Completion uses an inline result panel: **A way through.**, move count and personal best, then mint **Next puzzle**. Keep the final board visible. On the final puzzle, the action returns home and the panel reads **Every room explored.**

Settings include independent vibration and sound toggles, reduced motion, local privacy information, and confirmed progress reset. Reset preserves these preferences. Respect system font scale, safe areas, disabled system animations, and landscape by placing board and controls side by side or permitting scrolling.

## Motion

Tile movement takes 130ms with cubic ease-out; room entry/exit takes 210ms. Rules update immediately; rapid same-room input starts from the currently displayed position. Undo uses the same path. Room changes zoom between the containing box and the active board. Cargo crossing a room boundary fades between its two valid positions rather than interpolating unrelated coordinates. A blocked attempt produces a 120ms character bump. Goal feedback ends by 450ms, completion feedback by 500ms. Rendering is event-driven and stops when an effect finishes.

Reduced motion uses immediate positions and persistent goal checks, doorway marks, breadcrumbs, and an outlined outside preview. Audio uses eight original synthesized chimes (regenerated with `tools/make_sounds.py`); no ambient music. Stop audio and settle animation when backgrounded.

Room changes also trigger a 480ms portal reveal: mint and violet square halos expand from the containing box on entry (contract on exit), with twelve small orbiting sparks and a faint mint wash. This effect has its own short animation so the next ordinary move does not erase it. Another room change replaces it; undo, backgrounding, navigation, and reduced motion cancel it immediately.

## Teaching and hints

The existing 12 puzzles retain their titles, geometry, order, and save fingerprints. Show brief teaching text at the start of an unsolved puzzle, then let the player request three authored hints: nudge, existing level hint, concrete guidance. Chapter boundaries are explicit rather than assuming four puzzles per chapter. Expand toward 24–36 puzzles after observing new players complete this polish preview.
