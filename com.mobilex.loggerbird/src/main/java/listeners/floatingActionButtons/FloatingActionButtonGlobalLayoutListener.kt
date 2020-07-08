package listeners.floatingActionButtons

import android.os.Build
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird
import services.LoggerBirdService

//This class is not used at the moment.
internal class FloatingActionButtonGlobalLayoutListener(
        private val floatingActionButton: FloatingActionButton? = null
) : ViewTreeObserver.OnGlobalLayoutListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onGlobalLayout() {
        try {
            if (LoggerBirdService.floatingActionButtonLastDx != null && LoggerBirdService.floatingActionButtonLastDy != null) {
                floatingActionButton?.x = LoggerBirdService.floatingActionButtonLastDx!!
                floatingActionButton?.y = LoggerBirdService.floatingActionButtonLastDy!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.floatingActionButtonTag)
        }
    }
}