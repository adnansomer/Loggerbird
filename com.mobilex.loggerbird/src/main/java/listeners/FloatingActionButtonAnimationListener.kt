package listeners

import android.content.Context
import android.view.animation.Animation
import com.google.android.material.floatingactionbutton.FloatingActionButton


class FloatingActionButtonAnimationListener(
    private val floatingActionButtonAudio: FloatingActionButton,
    private val context: Context
) : Animation.AnimationListener {
    override fun onAnimationRepeat(animation: Animation?) {
    }

    override fun onAnimationEnd(animation: Animation?) {
        animation?.cancel()
    }

    override fun onAnimationStart(animation: Animation?) {
    }
}