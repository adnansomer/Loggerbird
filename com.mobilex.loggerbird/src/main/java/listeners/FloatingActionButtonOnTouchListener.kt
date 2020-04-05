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
            val  coroutineScopeOnTouch = CoroutineScope(Dispatchers.IO)
            coroutineScopeOnTouch.async {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
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
    //                        if (view.y > (view.parent as ViewGroup).height / 2) {
    //                            Log.d("y", "im invoked")
    //                        } else {
    //
    //                        }
                            
                            if ((view.parent as ViewGroup).height > (event.rawY + floatingActionButtonAudioDy + floatingActionButtonAudio.height) && (event.rawY + floatingActionButtonAudioDy) > 0) {
                                floatingActionButtonScreenShot.y = event.rawY + floatingActionButtonScreenShotDy
                                floatingActionButtonVideo.y = event.rawY + floatingActionButtonVideoDy
                                floatingActionButtonAudio.y = event.rawY + floatingActionButtonAudioDy
                            }

                        }
                        if ((view.parent as ViewGroup).width > (event.rawX + viewDx + view.width) && (event.rawX + viewDx) > 0) {
                            view.x = event.rawX + viewDx
                            if ((view.parent as ViewGroup).width > (event.rawX + floatingActionButtonAudioDx + view.width) && (event.rawX +floatingActionButtonAudioDx) > 0) {
                                floatingActionButtonScreenShot.x = event.rawX + floatingActionButtonScreenShotDx
                                floatingActionButtonVideo.x = event.rawX + floatingActionButtonVideoDx
                                floatingActionButtonAudio.x = event.rawX + floatingActionButtonAudioDx
                            }
                        }

                        lastAction = MotionEvent.ACTION_MOVE

                    }
                    MotionEvent.ACTION_UP -> {
                        if ((view.parent as ViewGroup).width < (event.rawX + viewDx + 3 * (view.width))) {
                            view.x = ((view.parent as ViewGroup).width.toFloat() - view.width)
                            Log.d("corner", "a")
                            floatingActionButtonScreenShot.x =
                                (view.x - floatingActionButtonScreenShot.width)
                            floatingActionButtonVideo.x = (view.x - floatingActionButtonVideo.width) - 100
                            floatingActionButtonAudio.x = (view.x - floatingActionButtonAudio.width) - 200
                            floatingActionButtonScreenShot.y = view.y
                            floatingActionButtonVideo.y = view.y
                            floatingActionButtonAudio.y = view.y
                        } else if (event.rawX + 3 * (viewDx) < 0) {
                            Log.d("corner", "b")
                            view.x = 0F
                            floatingActionButtonScreenShot.x =
                                floatingActionButtonScreenShot.width.toFloat()
                            floatingActionButtonVideo.x = floatingActionButtonVideo.width.toFloat() + 100
                            floatingActionButtonAudio.x = floatingActionButtonAudio.width.toFloat() + 200
                            floatingActionButtonScreenShot.y = view.y
                            floatingActionButtonVideo.y = view.y
                            floatingActionButtonAudio.y = view.y
                        }
                        if ((view.parent as ViewGroup).height < (event.rawY + viewDy + 3 * (view.height))) {
                            Log.d("corner", "c")
                            view.y = ((view.parent as ViewGroup).height.toFloat() - view.height)
                            floatingActionButtonScreenShot.y =
                                (view.y - floatingActionButtonScreenShot.height)
                            floatingActionButtonVideo.y = (view.y - floatingActionButtonVideo.height) - 100
                            floatingActionButtonAudio.y = (view.y - floatingActionButtonAudio.height) - 200
                            floatingActionButtonScreenShot.x = view.x
                            floatingActionButtonVideo.x = view.x
                            floatingActionButtonAudio.x = view.x
                        } else if (event.rawY + 3 * (viewDy) < 0) {
                            Log.d("corner", "d")
                            view.y = 0F
                            floatingActionButtonScreenShot.y =
                                floatingActionButtonScreenShot.height.toFloat()
                            floatingActionButtonVideo.y = floatingActionButtonVideo.height.toFloat() + 100
                            floatingActionButtonAudio.y = floatingActionButtonAudio.height.toFloat() + 200
                            floatingActionButtonScreenShot.x = view.x
                            floatingActionButtonVideo.x = view.x
                            floatingActionButtonAudio.x = view.x
                        }
                        lastAction = MotionEvent.ACTION_UP
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e , tag = Constants.floatingActionButtonOnTouchTag)
        }
        return false
    }
}