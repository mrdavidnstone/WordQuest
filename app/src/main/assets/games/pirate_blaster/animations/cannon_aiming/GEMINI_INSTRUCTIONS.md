# Pirate Blaster: Cannon Aiming Sprites, Instructions for Gemini

You are implementing cannon aiming in an existing Android game, **Pirate Blaster**. Read this whole file, then read `animation.json`, then implement. Adapt the code to the game's existing language, engine, and structure (Kotlin/Java Canvas, libGDX, Unity, etc.). The snippets are illustrative, not mandatory.

## What you were given

| Item | Purpose |
|---|---|
| `sprites/` | 15 separate transparent PNGs (center + left side), all **308 x 308**, RGBA. **There are no right-side files. You mirror the left ones in code.** |
| `animation.json` | The data contract: grid, filenames, pivot, muzzle points, aim mapping, sequences |
| `cannon_rotation_preview.gif` | Visual reference of every angle in order. Preview only, never a game asset |

---

# PART 1: How the angles work (left AND right)

## The core idea
The cannon is **pre-rendered**. Aiming = **choosing the right whole sprite**. Each sprite already shows the entire cannon (barrel, carriage, wheels) turned and raised to that angle as one rigid object.

**Never** do any of the following in code (the only allowed transforms are uniform scale and the horizontal mirror described below):
- rotate the sprite (`canvas.rotate`, `rotation`, matrix rotate) to aim
- skew, shear, or non-uniformly scale it
- rotate the barrel separately from the base
- shift it sideways to imitate turning
- tween or morph between sprites (swap instantly)

## The 5 x 5 grid

Yaw (horizontal) has 5 steps and pitch (elevation) has 5 steps, giving 25 logical angles. Only 15 are real files; the 10 right-side angles reuse the left files, mirrored.

```
              far_left  mid_left  center  mid_right  far_right
high           ...       ...      ...      ...        ...
upper_mid
center
lower_mid
low
```

Files on disk: `cannon_<yaw>_<pitch>.png` for yaw in `far_left`, `mid_left`, `center` (15 files).
Logical yaw steps: `far_left`, `mid_left`, `center`, `mid_right`, `far_right`
- pitch: `high`, `upper_mid`, `center`, `lower_mid`, `low`

## Left vs right (mirror in code)
- **Left** and **center** sprites are real files.
- **Right is the same left sprite drawn horizontally flipped.** `mid_right` = `cannon_mid_left_<pitch>.png` flipped, `far_right` = `cannon_far_left_<pitch>.png` flipped. `center` is symmetric and never flipped.
- `animation.json` already encodes this: every right-side frame lists the left file with `"flip_x": true`. Do not look for `*_right_*` files; they do not exist.
- **Flip around the pivot's x, which is the canvas centre column**, so the base does not move. On Android Canvas this is one call: `canvas.scale(-1f, 1f, mountX, mountY)` before drawing, then `canvas.restore()`. In libGDX use `flip(true, false)` with the origin at the pivot; in Unity use `flipX` or `localScale.x = -1` on a sprite whose pivot is the base centre.
- A mirror is the **only** transform allowed (plus uniform scale). It is not rotation or skew.
- `muzzle_px` for flipped frames is already mirrored into the final displayed orientation, so use the normal formula in Step 4 with no extra flip math.

## Choosing the sprite from an aim value

Normalize the aim into two numbers:

- `aim_x` in [-1, 1]: -1 = far left, 0 = straight ahead, +1 = far right
- `aim_y` in [-1, 1]: +1 = barrel highest, 0 = level, -1 = barrel lowest

```
yaw_index   = clamp(round(aim_x * 2) + 2, 0, 4)   // 0 far_left ... 4 far_right
pitch_index = clamp(2 - round(aim_y * 2), 0, 4)   // 0 high ... 4 low
sprite      = grid[pitch_index][yaw_index]
```

From a touch/target position:

```
aim_x = clamp((targetX - pivotScreenX) / yawFullScalePx, -1, 1)      // default yawFullScalePx = screenWidth / 2
aim_y = clamp((horizonY - targetY) / pitchFullScalePx, -1, 1)        // above horizon = raise, below = lower
```

`horizonY` and `pitchFullScalePx` depend on the game's camera. Expose them as constants and tune them so that a target at the horizon uses `pitch center`, and the closest on-screen ocean targets use `low`.

**Hysteresis:** keep the current yaw/pitch bin until the aim has passed the bin edge by ~0.08 (see `aim_space.hysteresis`). Without it, the sprite flickers when the aim sits on a boundary.

---

# PART 2: How to implement it

## Step 1: Load everything once
Parse `animation.json`. Decode the 15 PNGs once at start-up. Build a lookup from each of the 25 logical keys `"yaw/pitch"` (the keys in `frames`) to `(bitmap, flipX)`; right-side keys point to the left bitmap with `flipX = true`. Never decode during gameplay.

## Step 2: Anchor with the pivot
Every sprite shares the same pivot pixel, `pivot_px = (154, 298)` in the 308 x 308 canvas. It is the centre of the cannon's base at the bottom edge. Place that point at the cannon's mount position on the ship deck. Because the pivot is the same in all 15 files and sits on the canvas centre column, swapping sprites (flipped or not) causes **no jumping**.

```
scale   = desiredCannonScale                 // one uniform scale for ALL sprites
left    = mountX - pivot.x * scale
top     = mountY - pivot.y * scale
drawBitmap(sprite, left, top, scale)         // identical origin math every frame
```

## Step 3: Update loop
Each frame:
1. Convert the current aim (touch, joystick, or auto-target) to `aim_x`, `aim_y`.
2. Apply hysteresis, then compute `yaw_index` and `pitch_index`.
3. If the key changed, swap to that sprite **instantly** (no fade, no tween).
4. Draw the cannon above the deck and below projectiles/effects, applying the horizontal mirror when `flip_x` is true.

## Step 4: Fire from the muzzle
Each frame entry has `muzzle_px` (x, y in canvas pixels in the *final, displayed* orientation, estimated from the art). Spawn cannonballs, muzzle flash, and smoke there. For flipped frames the stored value is the mirrored canvas position, so use the formula below with the non-flipped draw origin (left/top), not the flipped transform:

```
muzzleScreenX = left + muzzle.x * scale      // muzzle_px for flipped frames is already mirrored
muzzleScreenY = top  + muzzle.y * scale
```

The projectile's flight direction should come from the actual target, not from the sprite's drawn angle, because the sprite is quantized to 5 x 5 steps. Check the muzzle points visually and adjust any that sit off the barrel tip.

## Step 5: Illustrative Kotlin (Android Canvas)

```kotlin
val yaws   = listOf("far_left","mid_left","center","mid_right","far_right")
val pitches= listOf("high","upper_mid","center","lower_mid","low")

class CannonSprites(ctx: Context, json: JSONObject) {
    val bitmaps = HashMap<String, Bitmap>()          // "yaw/pitch" -> bitmap (right keys share the left bitmap)
    val flipX   = HashMap<String, Boolean>()         // "yaw/pitch" -> mirror horizontally?
    val cache   = HashMap<String, Bitmap>()          // file -> bitmap, so 15 files serve 25 keys
    val muzzle  = HashMap<String, PointF>()
    val pivot   = PointF(
        json.getJSONObject("pivot_px").getDouble("x").toFloat(),
        json.getJSONObject("pivot_px").getDouble("y").toFloat())
    init {
        val frames = json.getJSONObject("frames")
        for (key in frames.keys()) {
            val f = frames.getJSONObject(key)
            val file = f.getString("file")
            bitmaps[key] = cache.getOrPut(file) { ctx.assets.open(file).use { BitmapFactory.decodeStream(it) } }
            flipX[key] = f.optBoolean("flip_x", false)
            val m = f.getJSONArray("muzzle_px")
            muzzle[key] = PointF(m.getDouble(0).toFloat(), m.getDouble(1).toFloat())
        }
    }
}

var yawI = 2; var pitchI = 2
fun updateAim(aimX: Float, aimY: Float, hyst: Float = 0.08f) {
    val rawYaw   = (aimX.coerceIn(-1f, 1f) * 2f) + 2f           // 0..4
    val rawPitch = 2f - (aimY.coerceIn(-1f, 1f) * 2f)           // 0..4
    if (kotlin.math.abs(rawYaw   - yawI)   > 0.5f + hyst) yawI   = rawYaw.roundToInt().coerceIn(0, 4)
    if (kotlin.math.abs(rawPitch - pitchI) > 0.5f + hyst) pitchI = rawPitch.roundToInt().coerceIn(0, 4)
}

fun drawCannon(c: Canvas, s: CannonSprites, mountX: Float, mountY: Float, scale: Float, paint: Paint) {
    val key = "${yaws[yawI]}/${pitches[pitchI]}"
    val bmp = s.bitmaps[key] ?: return
    val left = mountX - s.pivot.x * scale
    val top  = mountY - s.pivot.y * scale
    val dst  = RectF(left, top, left + bmp.width * scale, top + bmp.height * scale)
    c.save()
    if (s.flipX[key] == true) c.scale(-1f, 1f, mountX, mountY)   // mirror around the pivot column; no other transform
    c.drawBitmap(bmp, null, dst, paint)          // uniform scale only; paint.isFilterBitmap = true
    c.restore()
}
```

Put `sprites/` and `animation.json` in `assets/` (or the engine's equivalent). Keep the relative paths from the JSON.

---

# PART 3: Acceptance tests (verify before reporting done)

1. Aiming straight ahead shows `center/center`.
2. Sweeping the aim from far left to far right passes through `far_left`, `mid_left`, `center`, `mid_right`, `far_right`, in that order. The right side must be the mirrored left art: a cannon aimed right visibly points right, with its base in the same screen position as on the left.
3. Sweeping up and down passes `high` through `low`, with no skipped steps.
4. The base of the cannon does **not** move on screen when sprites swap (the pivot test). If it moves, the origin math is wrong.
5. The only transform on the cannon draw is uniform scale plus the horizontal mirror for right-side frames. No rotation or skew.
6. Cannonballs leave from the barrel tip for all 25 logical angles (check both left and right).
7. No frame hitches when aiming changes (bitmaps preloaded).
8. Compare against `cannon_rotation_preview.gif` to confirm the orders match.

# Known limits (do not "fix" these in code)
- The `mid_*` and `far_*` sprites look close to each other: far is only slightly more turned. If the difference is hard to see in play, you may collapse yaw to 3 steps (`far_*` and `mid_*` share one bin) by changing the bin thresholds. Do not generate extra rotation in code.
- There is a visible jump from `center` (true rear view) to `mid_*` (three-quarter view). That comes from the art, so keep the swap instant and, if needed, widen the center bin slightly (e.g. +-0.35) so the jump occurs less often.
- Mirroring also mirrors the painted lighting, so highlights land on the opposite side on the right. This is rarely noticeable.
- The source art is about 250 px wide per cannon. Keep the uniform on-screen scale near 1x to 1.5x for best sharpness.
