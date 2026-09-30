LAB MELTDOWN - ROBOT ARM (mounted on the left stand of YOUR lab_meltdown_bg.png)
==============================================================================
Arm art is cut from your original arm image at native pixel quality (no redraw, no resize).
The background is your plain lab_meltdown_bg.png, UNCHANGED (851x1849). The arm's column plugs into the
socket on the hazard-striped stand at the left, so nothing in the background needs editing.

LAYERS (bottom -> top), game canvas 851x1849:
  1. lab_meltdown_bg.png       your plain background
  2. arm frames                transparent PNGs, 496x528, draw with top-left at x=-98, y=428
                               (x is negative on purpose: the arm can swing past the left screen edge; let the screen clip it)
  3. sparks frames (optional)  transparent PNGs, same size / same position / same frame number as the arm frame

SEQUENCES (reverse-hangman trouble levels)
  L3_meltdown      24 frames @12fps, loop  - violent spasms, claw snapping, heavy sparks (start of round)
  L2_struggling    24 frames @12fps, loop  - big jerks, some sparks
  L1_unstable      24 frames @10fps, loop  - small twitches, one spark burst
  settle_to_normal 16 frames @12fps, ONCE  - shudders and damps out, ends exactly on the rest pose
  stable_idle      48 frames @12fps, loop  - CALM state: slow gentle sway (about 1-3 degrees), claws drift slightly, no sparks.
                                             Frame 0 is exactly the rest pose, so it follows settle_to_normal / arm_normal.png with no jump.
  arm_normal.png   1 frame                 - arm fixed / at rest (your original pose)

Suggested flow: L3 -> L2 -> L1 as the player gets letters right -> settle_to_normal -> hold arm_normal.png for a beat -> stable_idle (or just keep holding arm_normal.png if you want a fully still win state).
Loops start/end seamlessly. PNGs are straight (non-premultiplied) alpha.

ANDROID MEMORY TIP: each frame is 496x528 RGBA (~1.0 MB decoded). Load only the sequence that is playing
(or pack each sequence into one atlas) instead of all ~160 PNGs at once.
