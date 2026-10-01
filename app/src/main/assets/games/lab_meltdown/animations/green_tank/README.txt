GREEN TANK - COMPLETE SET (fitted to lab_meltdown_bg.png, 851x1849)
All frames are transparent PNGs, 12 fps, and ALL THREE sequences use the same size and position:
    frame size 186x486, draw with top-left at x=369, y=557 (background pixels; apply your background scale/offset).

tank_unstable/     48 frames, loop   boiling green slime, bubbles, leak, puddle
tank_settle/       24 frames, ONCE   green cools to your purple, bubbles fade, leak dries up, puddle shrinks
tank_stable_idle/  48 frames, loop   calm purple tank, faint shimmer, a few slow pale bubbles

Flow: unstable -> (250 ms crossfade) -> settle -> stable_idle.
The settle's last frame equals your original purple tank, so you can also simply stop drawing after it.
