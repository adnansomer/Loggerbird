package listeners

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import observers.LogActivityLifeCycleObserver


class FloatingActionButtonOnTouchListener(
    private val floatingActionButtonScreenShot: FloatingActionButton,
    private val floatingActionButtonVideo: FloatingActionButton,
    private val floatingActionButtonAudio: FloatingActionButton
) : View.OnTouchListener {
    private var viewDx: Float = 0F
    private var viewDy: Float = 0F
    private var floatingActionButtonScreenShotDx: Float = 0F
    private var floatingActionButtonScreenShotDy: Float = 0F
    private var floatingActionButtonVideoDx: Float = 0F
    private var floatingActionButtonVideoDy: Float = 0F
    private var floatingActionButtonAudioDx: Float = 0F
    private var floatingActionButtonAudioDy: Float = 0F
    private var lastAction: Int = 0
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            resetOldCoordinates()
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    floatingActionButtonScreenShot.visibility = View.GONE
                    floatingActionButtonVideo.visibility = View.GONE
                    floatingActionButtonAudio.visibility = View.GONE
                    viewDx = view.x - event.rawX
                    viewDy = view.y - event.rawY
                    floatingActionButtonScreenShotDx = floatingActionButtonScreenShot.x - event.rawX
                    floatingActionButtonScreenShotDy = floatingActionButtonScreenShot.y - event.rawY
                    floatingActionButtonVideoDx = floatingActionButtonVideo.x - event.rawX
                    floatingActionButtonVideoDy = floatingActionButtonVideo.y - event.rawY
                    floatingActionButtonAudioDx = floatingActionButtonAudio.x - event.rawX
                    floatingActionButtonAudioDy = floatingActionButtonAudio.y - event.rawY
                    lastAction = MotionEvent.ACTION_DOWN
                }
                MotionEvent.ACTION_MOVE -> {
                    if ((view.parent as ViewGroup).height > (event.rawY + viewDy + view.height) && (event.rawY + viewDy) > 0) {
                        view.y = event.rawY + viewDy
                        floatingActionButtonScreenShot.y =
                            event.rawY + floatingActionButtonScreenShotDy
                        floatingActionButtonVideo.y = event.rawY + floatingActionButtonVideoDy
                        floatingActionButtonAudio.y = event.rawY + floatingActionButtonAudioDy
                    }
                    if ((view.parent as ViewGroup).width > (event.rawX + viewDx + view.width) && (event.rawX + viewDx) > 0) {
                        view.x = event.rawX + viewDx
                        floatingActionButtonScreenShot.x =
                            event.rawX + floatingActionButtonScreenShotDx
                        floatingActionButtonVideo.x = event.rawX + floatingActionButtonVideoDx
                        floatingActionButtonAudio.x = event.rawX + floatingActionButtonAudioDx
                    }

                    lastAction = MotionEvent.ACTION_MOVE

                }
                MotionEvent.ACTION_UP -> {
                    if ((view.parent as ViewGroup).width < (event.rawX + viewDx + 3 * (view.width))) {
                        view.x = ((view.parent as ViewGroup).width.toFloat() - view.width)
                        Log.d("corner", "a")
                        floatingActionButtonScreenShot.x =
                            (view.x - floatingActionButtonScreenShot.width)
                        floatingActionButtonVideo.x =
                            (view.x - floatingActionButtonVideo.width) - 150
                        floatingActionButtonAudio.x =
                            (view.x - floatingActionButtonAudio.width) - 300
                        floatingActionButtonScreenShot.y = view.y
                        floatingActionButtonVideo.y = view.y
                        floatingActionButtonAudio.y = view.y
                    } else if (event.rawX + 3 * (viewDx) < 0) {
                        Log.d("corner", "b")
                        view.x = 0F
                        floatingActionButtonScreenShot.x =
                            floatingActionButtonScreenShot.width.toFloat()
                        floatingActionButtonVideo.x =
                            floatingActionButtonVideo.width.toFloat() + 150
                        floatingActionButtonAudio.x =
                            floatingActionButtonAudio.width.toFloat() + 300
                        floatingActionButtonScreenShot.y = view.y
                        floatingActionButtonVideo.y = view.y
                        floatingActionButtonAudio.y = view.y
                    }
                    if ((view.parent as ViewGroup).height < (event.rawY + viewDy + 3 * (view.height))) {
                        Log.d("corner", "c")
                        view.y = ((view.parent as ViewGroup).height.toFloat() - view.height)
                        floatingActionButtonScreenShot.y =
                            (view.y - floatingActionButtonScreenShot.height)
                        floatingActionButtonVideo.y =
                            (view.y - floatingActionButtonVideo.height) - 150
                        floatingActionButtonAudio.y =
                            (view.y - floatingActionButtonAudio.height) - 300
                        floatingActionButtonScreenShot.x = view.x
                        floatingActionButtonVideo.x = view.x
                        floatingActionButtonAudio.x = view.x
                    } else if (event.rawY + 3 * (viewDy) < 0) {
                        Log.d("corner", "d")
                        view.y = 0F
                        floatingActionButtonScreenShot.y =
                            floatingActionButtonScreenShot.height.toFloat()
                        floatingActionButtonVideo.y =
                            floatingActionButtonVideo.height.toFloat() + 150
                        floatingActionButtonAudio.y =
                            floatingActionButtonAudio.height.toFloat() + 300
                        floatingActionButtonScreenShot.x = view.x
                        floatingActionButtonVideo.x = view.x
                        floatingActionButtonAudio.x = view.x
                    }
                    lastAction = MotionEvent.ACTION_UP
                    floatingActionButtonScreenShot.visibility = View.VISIBLE
                    floatingActionButtonVideo.visibility = View.VISIBLE
                    floatingActionButtonAudio.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.floatingActionButtonOnTouchTag
            )
        }
        return false
    }
    private fun resetOldCoordinates(){
        LogActivityLifeCycleObserver.floatingActionButtonLastDx = null
        LogActivityLifeCycleObserver.floatingActionButtonLastDy = null
        LogActivityLifeCycleObserver.floatingActionButtonScreenShotLastDx = null
        LogActivityLifeCycleObserver.floatingActionButtonScreenShotLastDy = null
        LogActivityLifeCycleObserver.floatingActionButtonAudioLastDx = null
        LogActivityLifeCycleObserver.floatingActionButtonAudioLastDy = null
        LogActivityLifeCycleObserver.floatingActionButtonVideoLastDx = null
        LogActivityLifeCycleObserver.floatingActionButtonVideoLastDy = null
    }
}