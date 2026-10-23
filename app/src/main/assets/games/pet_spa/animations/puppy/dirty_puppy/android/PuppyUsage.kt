// 1) Copy frames_256 (or frames_512 for big/tablet screens) into res/drawable-nodpi/
//    and puppy_idle_anim.xml into res/drawable/.
// 2) Put an ImageView in your layout, e.g. over the tub:
//      <ImageView android:id="@+id/puppy" android:layout_width="wrap_content"
//          android:layout_height="wrap_content" android:adjustViewBounds="true" />

val puppy = findViewById<ImageView>(R.id.puppy)
puppy.setBackgroundResource(R.drawable.puppy_idle_anim)
val idle = puppy.background as AnimationDrawable
idle.start()                       // loops forever (oneshot=false)

// Pause/resume with the screen so it doesn't burn battery in the background
override fun onPause() { super.onPause(); idle.stop() }
override fun onResume() { super.onResume(); idle.start() }

// Tip: every frame shares one canvas and is bottom-aligned, so just keep the
// ImageView in one place. Don't resize it per frame.
// Memory: the 256px set (~5 MB decoded) is plenty for phones; the 512px set
// is ~20 MB decoded, so prefer it only where you really need the detail.
