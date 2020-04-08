package listeners

import android.os.Build
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird
import observers.LogActivityLifeCycleObserver

class FloatingActionButtonVideoGlobalLayoutListener(private val floatingActionButtonVideo: FloatingActionButton? = null) :
    ViewTreeObserver.OnGlobalLayoutListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onGlobalLayout() {
        try {
            if (LogActivityLifeCycleObserver.floatingActionButtonVideoLastDx != null && LogActivityLifeCycleObserver.floatingActionButtonVideoLastDy != null) {
                floatingActionButtonVideo?.x =
                    LogActivityLifeCycleObserver.floatingActionButtonVideoLastDx!!
                floatingActionButtonVideo?.y =
                    LogActivityLifeCycleObserver.floatingActionButtonVideoLastDy!!
            }
            floatingActionButtonVideo?.viewTreeObserver?.removeOnGlobalLayoutListener(
                this
            )
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