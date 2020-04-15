package listeners

import android.os.Build
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi

class LayoutGlobalLayoutListener(
    private val rootView: ViewGroup? = null,
    private val runnable: Runnable? = null
) : ViewTreeObserver.OnGlobalLayoutListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onGlobalLayout() {
        if(!LayoutWindowFocusChangeListener.controlFocusChangeListener){
            runnable?.run()
        }
    }
}