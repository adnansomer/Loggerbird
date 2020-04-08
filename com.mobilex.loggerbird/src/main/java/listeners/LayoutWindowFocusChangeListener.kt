package listeners

import android.os.Build
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class LayoutWindowFocusChangeListener(private val initializeFloatingActionButton: Runnable,private val takeOldCoordinates:Runnable) :
    ViewTreeObserver.OnWindowFocusChangeListener {
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if(!hasFocus){
            takeOldCoordinates.run()
        }
        initializeFloatingActionButton.run()
    }
}