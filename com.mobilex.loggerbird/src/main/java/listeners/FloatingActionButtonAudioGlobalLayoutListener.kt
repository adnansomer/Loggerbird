package listeners

import android.os.Build
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird
import observers.LogActivityLifeCycleObserver

class FloatingActionButtonAudioGlobalLayoutListener(private val floatingActionButtonAudio: FloatingActionButton? = null) :
    ViewTreeObserver.OnGlobalLayoutListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onGlobalLayout() {
        try {
            if (LogActivityLifeCycleObserver.floatingActionButtonAudioLastDx != null && LogActivityLifeCycleObserver.floatingActionButtonAudioLastDy != null) {
                floatingActionButtonAudio?.x =
                    LogActivityLifeCycleObserver.floatingActionButtonAudioLastDx!!
                floatingActionButtonAudio?.y =
                    LogActivityLifeCycleObserver.floatingActionButtonAudioLastDy!!
            }
            floatingActionButtonAudio?.viewTreeObserver?.removeOnGlobalLayoutListener(
                this
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.floatingActionButtonAudioTag
            )
        }
    }
}