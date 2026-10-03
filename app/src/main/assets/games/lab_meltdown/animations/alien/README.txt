LAB MELTDOWN - ALIEN STORY ANIMATION
====================================
40 transparent PNG frames, 340x380 each (frames/alien_00.png .. alien_39.png) and the same frames as an 8x5 sprite sheet
(alien_story_sheet_8x5.png, 2720x1900, read left-to-right, top-to-bottom). Plays once through the whole beat sheet and loops.

Beats (frame ranges are inclusive):
  1 idle .......................... 0-4     happy, relaxed, default smile (frame 0 = your art, pixel for pixel)
  2 looks left, then right ........ 5-10    nervous glancing, the eye highlights shift a little more than the eyes
  3 eyes widen, antennae snap up .. 11-14
  4 body shakes rapidly ........... 15-19
  5 ducks down, eyes shut ......... 20-24   squashes down, > < eyes, tight wobbly mouth, a few sparks fly past
  6 electrical surge flash ........ 25-27   soft warm glow + little electric arcs; the alien stays visible
  7 purple slime blob hits it ..... 28-33   blob flies in from the upper left, hits the forehead, leaves a splat with two drips
  8 surprised ..................... 34-37   huge eyes, wide open mouth, antennae straight up, little hop
  9 relaxes back to idle .......... 38-39   the splat slides down and fades; frame 39 is almost exactly frame 0, so the loop restarts cleanly

Timing: animation.json has durations_ms per frame (about 4 seconds total). If your engine only does a fixed frame rate, use 10 fps.
Position: top-left of the frame at (484, 498) in the 784x1168 concept art puts the alien exactly over the original.

What is from your art and what is new: the body, puddle, droplet, eyes, highlights, mouth (smile) and antennae are cut from your image.
The shut-eye > <, the tight mouth, the open shocked mouth, the purple blob and the splat, the sparks and the electric arcs were drawn
new, matched to your art's colours and outlines (there was nothing in the image to copy for those).
