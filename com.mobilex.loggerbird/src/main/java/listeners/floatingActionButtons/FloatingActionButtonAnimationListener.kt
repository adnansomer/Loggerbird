package listeners.floatingActionButtons

import android.content.Context
import android.view.animation.Animation
import com.google.android.material.floatingactionbutton.FloatingActionButton

//Dummy class might be useful in future.
internal class FloatingActionButtonAnimationListener(
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