# Guitar Tuner App Icon Design

Date: 2026-05-16

## Goal
Design a new Android app icon for the guitar tuning app that feels modern, minimal, and tool-like while still clearly signaling music and tuning.

The icon should replace the current default launcher assets and give the app a stronger identity in the launcher, app switcher, and store listings.

## Approved Direction
- Primary style: modern minimal
- Chosen color direction: dark digital
- Core concept: combine a guitar pick, tuning fork, and simplified guitar headstock in one compact symbol

## Success Criteria
- The icon reads as a tuning tool first, not a generic music player.
- It remains recognizable at small launcher sizes.
- It avoids visual clutter even though three symbols are combined.
- It fits Android adaptive icon constraints cleanly.
- It feels more like a contemporary audio utility than a decorative instrument badge.

## Explored Directions

### Option 1: Pick-Wrapped Symbol
- Outer silhouette uses a guitar pick.
- Center axis uses a simplified tuning fork.
- Bottom section uses a minimal guitar headstock with tuning pegs.
- Strengths: best balance of recognition, function clarity, and small-size legibility.
- Weaknesses: needs strict simplification so the headstock details do not become noise.

### Option 2: Headstock-Led Symbol
- Main emphasis is the guitar headstock.
- Tuning fork becomes a hidden internal structure.
- Pick only appears as a faint enclosing contour.
- Strengths: strongest instrument association.
- Weaknesses: weaker "tuning" signal and more risk of looking like a generic guitar app.

### Option 3: Geometric Badge
- Compress all three ideas into a more abstract badge-like composition.
- Stronger symmetry and more software-tool styling.
- Strengths: clean and modern.
- Weaknesses: more abstract, so music and tuning meaning may be less immediate.

## Recommendation
Use Option 1: Pick-Wrapped Symbol.

It is the clearest way to merge all three requested elements without overloading the icon. The pick shape makes the app feel musical, the tuning fork makes the function explicit, and the small headstock cue keeps the instrument context grounded.

## Final Composition
- Outer form: rounded guitar pick silhouette
- Primary vertical element: simplified tuning fork centered in the icon
- Secondary bottom element: minimal guitar headstock block with three peg cues
- Supporting detail: subtle string lines aligned to the tuning fork stem

### Hierarchy
- First read: guitar pick silhouette
- Second read: tuning fork
- Third read: guitar headstock and peg details

This hierarchy is intentional. The icon must still work if the smallest details become visually soft at launcher size.

## Color System

### Chosen Palette: Dark Digital
- Background gradient: deep indigo to near-black
- Symbol color: bright white with cool highlights
- Optional micro-accent: very restrained soft neon green reflection, only if it improves depth without changing the primary identity

### Mood
- precise
- modern
- digital audio tool
- cool, focused, and professional

### Avoid
- warm wood or amber-dominant treatment
- playful rainbow accents
- purple-heavy fantasy gradients
- flat monochrome without depth

## Shape Rules
- Keep the outer pick silhouette large and stable.
- Keep the tuning fork thick enough to survive downscaling.
- Use the headstock as a supporting cue, not the focal point.
- Limit visible peg cues to a simplified set so the bottom does not become crowded.
- Do not add note glyphs, waveform decorations, or extra chrome.

## Android Adaptive Icon Guidance
- Build the symbol for adaptive icon usage, not only for static square export.
- The foreground should stay centered inside the safe zone.
- The pick silhouette and tuning fork should survive common OEM masks, including rounded square and circular crops.
- The bottom headstock cue must remain inside the crop-safe area.
- Round icon output should reuse the same composition instead of inventing a separate symbol.

## Production Notes
- Prefer vector construction for the foreground artwork.
- Background can be a simple gradient layer rather than a detailed illustration.
- If implementation requires raster output, export from the same master composition for all mipmap densities.
- Test the icon at small sizes before finalizing peg spacing and string detail thickness.

## Handoff Rules For Implementation
- Replace the default launcher foreground asset with the approved composition.
- Keep one icon concept only; do not ship both warm and dark variants together.
- If a fallback static icon preview is needed for documentation or store use, it should match the adaptive icon foreground and background exactly.

## Out of Scope
- Rebranding the app name
- Changing in-app theme colors
- Creating multiple seasonal icon variants
- Updating store screenshots or marketing art
