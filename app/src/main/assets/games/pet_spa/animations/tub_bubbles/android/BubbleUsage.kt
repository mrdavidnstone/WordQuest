// Float loop, then pop on tap or when the bubble reaches its height.
// Put the PNGs from frames_256 (or frames_128) in res/drawable-nodpi/.
val bubble = ImageView(context)
bubble.setBackgroundResource(R.drawable.bubble_float_anim)
(bubble.background as AnimationDrawable).start()

fun popBubble(view: ImageView, onDone: () -> Unit) {
    view.setBackgroundResource(R.drawable.bubble_pop_anim)
    val anim = view.background as AnimationDrawable
    anim.start()
    val total = (0 until anim.numberOfFrames).sumOf { anim.getDuration(it) }.toLong()
    view.postDelayed({ onDone() }, total)   // remove or recycle the view here
}

// For floating up: animate translationY (plus a small sine translationX) on the view
view.animate().translationYBy(-400f).setDuration(3000).withEndAction { popBubble(view) { /* recycle */ } }.start()
