package listeners

import android.os.Build
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird
import observers.LogActivityLifeCycleObserver

class FloatingActionButtonGlobalLayoutListener(
    private val floatingActionButton: FloatingActionButton? = null
) : ViewTreeObserver.OnGlobalLayoutListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onGlobalLayout() {
        try {
            if (LogActivityLifeCycleObserver.floatingActionButtonLastDx != null && LogActivityLifeCycleObserver.floatingActionButtonLastDy != null) {
                floatingActionButton?.x = LogActivityLifeCycleObserver.floatingActionButtonLastDx!!
                floatingActionButton?.y = LogActivityLifeCycleObserver.floatingActionButtonLastDy!!
            }
            floatingActionButton?.viewTreeObserver?.removeOnGlobalLayoutListener(
                this
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.floatingActionButtonTag)
        }
    }
}