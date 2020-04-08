package listeners

import android.os.Build
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird
import observers.LogActivityLifeCycleObserver

class FloatingActionButtonScreenshotGlobalLayoutListener(private val floatingActionButtonScreenshot: FloatingActionButton? = null) :
    ViewTreeObserver.OnGlobalLayoutListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onGlobalLayout() {
        try {
            if (LogActivityLifeCycleObserver.floatingActionButtonScreenShotLastDx != null && LogActivityLifeCycleObserver.floatingActionButtonScreenShotLastDy != null) {
                floatingActionButtonScreenshot?.x =
                    LogActivityLifeCycleObserver.floatingActionButtonScreenShotLastDx!!
                floatingActionButtonScreenshot?.y =
                    LogActivityLifeCycleObserver.floatingActionButtonScreenShotLastDy!!
            }
            floatingActionButtonScreenshot?.viewTreeObserver?.removeOnGlobalLayoutListener(
                this
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.floatingActionButtonScreenshotTag
            )
        }
    }
}