package listeners

import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi

class FloatingActionButtonPaintGlobalLayoutListener(private val paintRunnable: Runnable,private val view:View) : ViewTreeObserver.OnGlobalLayoutListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onGlobalLayout() {
       paintRunnable.run()
        view.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }
}