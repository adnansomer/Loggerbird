package listeners.floatingActionButtons

import android.os.Build
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird
import services.LoggerBirdService

//This class is not used at the moment.
internal class FloatingActionButtonScreenshotGlobalLayoutListener(private val floatingActionButtonScreenshot: FloatingActionButton? = null) :
    ViewTreeObserver.OnGlobalLayoutListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onGlobalLayout() {
        try {
            if (LoggerBirdService.floatingActionButtonScreenShotLastDx != null && LoggerBirdService.floatingActionButtonScreenShotLastDy != null) {
                floatingActionButtonScreenshot?.x =
                    LoggerBirdService.floatingActionButtonScreenShotLastDx!!
                floatingActionButtonScreenshot?.y =
                    LoggerBirdService.floatingActionButtonScreenShotLastDy!!
            }
//            floatingActionButtonScreenshot?.viewTreeObserver?.removeOnGlobalLayoutListener(
//                this
//            )
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