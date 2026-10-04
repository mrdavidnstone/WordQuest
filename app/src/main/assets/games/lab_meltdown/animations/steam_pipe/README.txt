LAB MELTDOWN - RIGHT PIPE STEAM LEAK, v2: REALISTIC TEXTURE (on YOUR lab_meltdown_bg.png)
=============================================================================================
Revision of steam_pipe_android.zip based on two rounds of feedback:

1. "Too jumpy at the loop" - root cause found and fixed: elements with a CSS
   animation-delay render at full, untransformed visibility during their one-time
   start-up delay window (no fill-mode was set). My original frame capture sampled
   that first cycle directly, baking the startup artifact into "frame 0." Frames are
   now sampled from the animation's SECOND cycle, once every element is in steady
   repeating motion - verified seamless by comparing the wrap-frame pixel difference
   against every other frame transition (no spike, same magnitude as normal steps).

2. "Looks like little balls" / "make it more realistic" - replaced flat-gradient
   circles with: an SVG feTurbulence + feDisplacementMap filter on every puff (ragged,
   irregular organic edges instead of smooth round blobs), elongated vertical wisp
   shapes (upward-streaking, not round), per-blob randomized opacity, and a mix of
   warm-white / cool-grey toned puffs for a sense of depth. No crack glow (per earlier
   feedback) - steam only.

Frames are FULL CANVAS SIZE (851x1849), matching your background exactly, fully
transparent except the active steam area - draw_at_xy is [0,0], no offset math needed.
Nothing extends past the pipe's existing painted shape - steam only, in front of it.

LAYERS (bottom -> top):
  1. lab_meltdown_bg.png   your plain background
  2. steam frame           transparent PNG, 851x1849, draw at (0,0)

SEQUENCES
  stable_idle   2 frames @1fps,  loop       - nothing, default calm state.
  unstable      24 frames @10fps, loop       - realistic, seamless, chaotic billowing steam.
  settle        24 frames @8fps,  ONE-SHOT   - dies down to nothing, hands off cleanly to stable_idle.

SUGGESTED FLOW
  stable_idle (loop) -> fault -> unstable (loop) -> resolved -> settle (once) -> stable_idle.

PREVIEWS
  preview_full_cycle.gif - all three states back to back, on your actual background.
  preview_unstable.gif   - just the unstable loop.
  preview_settle.gif     - just the settle one-shot.

PNGs are straight (non-premultiplied) alpha, native pixel quality.
ANDROID MEMORY TIP: full-canvas frames are large (851x1849 RGBA, ~6.3 MB decoded each).
Load only the sequence currently playing.
