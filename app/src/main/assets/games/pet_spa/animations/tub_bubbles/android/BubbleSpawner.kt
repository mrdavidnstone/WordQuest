import android.animation.ValueAnimator
import android.graphics.RectF
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Spawns floating bubbles over the tub and pops them at the top of their rise.
 *
 * container : FrameLayout stacked on top of the tub image
 * waterArea : the tub's water surface in container pixels (the full width of the tub rim)
 *
 * All the "how often / where / how big" knobs are constructor parameters,
 * so you can tune the feel without touching the art.
 */
class BubbleSpawner(
    private val container: FrameLayout,
    private val waterArea: RectF,
    private val minIntervalMs: Long = 1200,      // gap between spawns (raise both for fewer bubbles)
    private val maxIntervalMs: Long = 2600,
    private val maxBubbles: Int = 5,             // cap on bubbles alive at once
    private val bubbleSizePx: IntRange = 40..110,  // visible bubble diameter
    private val riseDistancePx: IntRange = 160..320,
    private val riseDurationMs: LongRange = 3000L..4500L
) {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var alive = 0

    // The bubble art fills about 52% of each sprite frame (room is left for the pop spray)
    private val spriteScale = 1f / 0.52f

    fun start() { running = true; scheduleNext() }
    fun stop() { running = false; handler.removeCallbacksAndMessages(null) }

    private fun scheduleNext() {
        if (!running) return
        handler.postDelayed({
            if (alive < maxBubbles) spawn()
            scheduleNext()
        }, Random.nextLong(minIntervalMs, maxIntervalMs + 1))
    }

    private fun randomPointInWater(): Pair<Float, Float> {
        // uniform point inside the ellipse so bubbles come from the whole tub, not just the middle
        val cx = waterArea.centerX(); val cy = waterArea.centerY()
        val rx = waterArea.width() / 2f; val ry = waterArea.height() / 2f
        val a = Random.nextDouble(0.0, 2 * PI)
        val r = sqrt(Random.nextDouble())
        return Pair((cx + rx * r * kotlin.math.cos(a)).toFloat(), (cy + ry * r * sin(a)).toFloat())
    }

    private fun spawn() {
        val diameter = Random.nextInt(bubbleSizePx.first, bubbleSizePx.last + 1)
        val size = (diameter * spriteScale).toInt()
        val (x0, y0) = randomPointInWater()
        val rise = Random.nextInt(riseDistancePx.first, riseDistancePx.last + 1).toFloat()
        val duration = Random.nextLong(riseDurationMs.first, riseDurationMs.last + 1)
        val swayPx = Random.nextInt(8, 22).toFloat()
        val swayCycles = Random.nextInt(2, 5) / 2f
        val phase = Random.nextFloat() * (2 * PI).toFloat()

        val view = ImageView(container.context).apply {
            layoutParams = FrameLayout.LayoutParams(size, size)
            setBackgroundResource(R.drawable.bubble_float_anim)
            (background as AnimationDrawable).start()
            alpha = 0f
        }
        container.addView(view)
        alive++

        ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener { va ->
                val t = va.animatedFraction
                val eased = 1f - (1f - t) * (1f - t)                       // slows as it rises
                val sway = swayPx * sin(phase + t * swayCycles * 2 * PI.toFloat()) * (0.4f + t)
                view.translationX = x0 + sway - size / 2f
                view.translationY = y0 - rise * eased - size / 2f
                val grow = (t / 0.12f).coerceAtMost(1f)                    // grows in over first 12%
                view.scaleX = 0.35f + 0.65f * grow; view.scaleY = view.scaleX
                view.alpha = grow
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: android.animation.Animator) = pop(view)
            })
            start()
        }
        view.setOnClickListener { pop(view) }   // optional: tap to pop early
    }

    private fun pop(view: ImageView) {
        if (view.tag == "popping") return
        view.tag = "popping"
        view.setOnClickListener(null)
        view.setBackgroundResource(R.drawable.bubble_pop_anim)
        val anim = view.background as AnimationDrawable
        anim.start()
        val total = (0 until anim.numberOfFrames).sumOf { anim.getDuration(it) }.toLong()
        view.postDelayed({ container.removeView(view); alive-- }, total)
    }
}

// Usage (e.g. in onViewCreated, after the tub has been laid out):
//   val water = RectF(tub.left + tub.width * 0.07f, tub.top + tub.height * 0.38f,
//                     tub.left + tub.width * 0.93f, tub.top + tub.height * 0.58f)
//   spawner = BubbleSpawner(bubbleLayer, water).also { it.start() }
// and call spawner.stop() in onPause / onDestroyView.
