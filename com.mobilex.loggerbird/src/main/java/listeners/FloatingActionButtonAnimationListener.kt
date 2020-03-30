package listeners

import android.content.Context
import android.os.Handler
import android.view.animation.Animation
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FloatingActionButtonAnimationListener(private val floatingActionButtonAudio:FloatingActionButton,private val context:Context):Animation.AnimationListener {
   private val handler:Handler = Handler()
    override fun onAnimationRepeat(animation: Animation?) {
    }

    override fun onAnimationEnd(animation: Animation?) {
       animation?.cancel()
    }

    override fun onAnimationStart(animation: Animation?) {
    }
}