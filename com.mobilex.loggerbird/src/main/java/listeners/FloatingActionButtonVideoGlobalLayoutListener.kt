package listeners

import android.os.Build
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird
import services.LoggerBirdService

class FloatingActionButtonVideoGlobalLayoutListener(private val floatingActionButtonVideo: FloatingActionButton? = null) :
    ViewTreeObserver.OnGlobalLayoutListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onGlobalLayout() {
        try {
            if (LoggerBirdService.floatingActionButtonVideoLastDx != null && LoggerBirdService.floatingActionButtonVideoLastDy != null) {
                floatingActionButtonVideo?.x =
                    LoggerBirdService.floatingActionButtonVideoLastDx!!
                floatingActionButtonVideo?.y =
                    LoggerBirdService.floatingActionButtonVideoLastDy!!
            }
//            floatingActionButtonVideo?.viewTreeObserver?.removeOnGlobalLayoutListener(
//                this
//            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.floatingActionButtonVideoTag
            )
        }
    }
}