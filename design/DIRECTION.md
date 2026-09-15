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

Use Android system sans: 34sp bold for the home title, 24sp medium for screen titles, 18sp medium for primary buttons, 16sp regular for explanatory text, 13sp medium for metadata. Keep main text at least 4.5:1 contrast. Spacing uses 4dp increments; screen padding 24dp, control gaps 8–12dp, card padding 20dp. Surfaces use 16dp corner radii; large actions 14dp; board cells 5–8% of their cell size. Use flat color and an occasional one-pixel inset stroke; avoid glow, gradients, and decorative card stacks.

## Home

Top: small uppercase “A LITTLE SPACE TO THINK” eyebrow, then two-line “Pocket / Paradox” wordmark. A compact nested-square motif in mint, violet, and coral sits in the middle of generous negative space. Beneath: one-line promise “Find a way in. Think your way out.”

Bottom action stack: coral **Continue · 03** (or **Start exploring** for fresh progress), outlined **Choose a puzzle**, then a small row containing **How to play** and **Settings**. Show a plain completion count such as “2 of 18 puzzles complete” near the primary action. No currency, daily streaks, or account controls.

## Puzzle selection

Top bar: 48dp Back control, **Your discoveries**, completion count. Group levels under short chapter labels **First steps**, **Rooms within rooms**, **Deeper thinking**. Each chapter has an understated subtitle explaining its mechanic. Use a three-column numbered grid with cells at least 80dp high; current level has a warm outline, completed levels have a visible checkmark, locked levels have a lock glyph and a muted number. Color never carries the status alone. Scroll vertically; never shrink the grid to fit every level at once.

## Play

Portrait order: Back / chapter label / level count; level title; movement count and a compact “Room 1 → Room 2” breadcrumb when nested; square board; a short contextual hint; controls. Use the available area to fit the board with generous separation from controls, and scale the board down before reducing touch targets.

The floor is navy, walls are slate, crates are violet solids, and enterable room boxes are mint frames with a visible miniature room. The coral explorer has two dark square eyes. Box goals use a gold hollow square with four inset corner ticks; the player goal uses a gold ring plus central dot. Completed goals gain a small checkmark. Show the active room’s outer edge clearly. Miniatures render only enough depth to make nesting legible; avoid tiny full-detail grids.

Place **Undo** and **Restart** in one row immediately below the board or hint. Use four separate 56–64dp native buttons in a cross for movement, with a blank center: Up above Left / blank / Right above Down. Controls should work through taps, swipes, and keyboard arrows; undo and restart stay available throughout play. All controls need content descriptions, visible pressed states, at least 48dp touch targets, and readable disabled states.

## Completion and settings

Completion uses a compact native dialog: **A way through.**, “Puzzle 03 complete · 12 moves”, coral **Next puzzle**, and **Choose puzzle**. Keep final-board state visible behind it. On the final puzzle, primary action returns to selection and copy reads **Every room explored.**

Settings remain a short native screen/dialog with vibration toggle, motion preference if animation exists, and progress reset. Progress reset requires a native destructive confirmation; changing settings never erases progress. Respect system font scale, safe areas, reduce-motion preference where available, and landscape by placing board and controls side by side or permitting scrolling.

## Motion

Optional board transitions are 100–140ms ease-out; room focus changes at most 180ms. Never delay accepted movement while animating and never require motion to understand a room transition. One subtle vibration on a successful push is sufficient when enabled. Start with native ripple feedback and immediate movement; additional animation can follow once the mechanic is proven.
