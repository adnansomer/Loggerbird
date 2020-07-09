package listeners.layouts

import android.os.Build
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi

//Dummy class might be useful in future.
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
internal class LayoutWindowAttachListener(private val runnable: Runnable) :ViewTreeObserver.OnWindowAttachListener {
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