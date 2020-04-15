package listeners

import android.os.Build
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class LayoutWindowAttachListener(private val runnable: Runnable) :ViewTreeObserver.OnWindowAttachListener {
    companion object{
        internal var controlAttachListener:Boolean = false
    }
    override fun onWindowDetached() {
        controlAttachListener = false
        runnable.run()
    }

    override fun onWindowAttached() {
        controlAttachListener = true
        runnable.run()
    }
}