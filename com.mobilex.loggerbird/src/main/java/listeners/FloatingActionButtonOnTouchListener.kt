package listeners

import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.view.marginStart
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird
import services.LoggerBirdService
import javax.mail.Quota


class FloatingActionButtonOnTouchListener(
    private val windowManager: WindowManager,
    private val windowManagerView: View,
    private val windowManagerParams: WindowManager.LayoutParams,
    private val floatingActionButton: FloatingActionButton,
    private val floatingActionButtonScreenShot: FloatingActionButton,
    private val floatingActionButtonVideo: FloatingActionButton,
    private val floatingActionButtonAudio: FloatingActionButton
) : View.OnTouchListener {
    private var windowManagerDx: Float = 0F
    private var windowManagerDy: Float = 0F
    private var floatingActionButtonDx: Float = 0F
    private var floatingActionButtonDy: Float = 0F
    private var floatingActionButtonScreenShotDx: Float = 0F
    private var floatingActionButtonScreenShotDy: Float = 0F
    private var floatingActionButtonVideoDx: Float = 0F
    private var floatingActionButtonVideoDy: Float = 0F
    private var floatingActionButtonAudioDx: Float = 0F
    private var floatingActionButtonAudioDy: Float = 0F
    private var lastAction: Int = 0
    private val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
    private val deviceHeight = Resources.getSystem().displayMetrics.heightPixels
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            resetOldCoordinates()
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    floatingActionButtonScreenShot.visibility = View.GONE
                    floatingActionButtonVideo.visibility = View.GONE
                    floatingActionButtonAudio.visibility = View.GONE
                    windowManagerDx = windowManagerParams.x - event.rawX
                    windowManagerDy = windowManagerParams.y - event.rawY
                    floatingActionButtonDx = floatingActionButton.x - event.rawX
                    floatingActionButtonDy = floatingActionButton.y - event.rawY
                    floatingActionButtonScreenShotDx = floatingActionButtonScreenShot.x - event.rawX
                    floatingActionButtonScreenShotDy = floatingActionButtonScreenShot.y - event.rawY
                    floatingActionButtonVideoDx = floatingActionButtonVideo.x - event.rawX
                    floatingActionButtonVideoDy = floatingActionButtonVideo.y - event.rawY
                    floatingActionButtonAudioDx = floatingActionButtonAudio.x - event.rawX
                    floatingActionButtonAudioDy = floatingActionButtonAudio.y - event.rawY
                    lastAction = MotionEvent.ACTION_DOWN
                }
                MotionEvent.ACTION_MOVE -> {
                    windowManagerParams.x = event.rawX.toInt() + windowManagerDx.toInt()
                    windowManagerParams.y = event.rawY.toInt() + windowManagerDy.toInt()
                    lastAction = MotionEvent.ACTION_MOVE
                    windowManager.updateViewLayout(
                        windowManagerView,
                        windowManagerParams
                    )
                }
                MotionEvent.ACTION_UP -> {
                    if (deviceWidth < (event.rawX +  (floatingActionButton.width))) {
                        windowManagerParams.x = deviceWidth / 2
                        Log.d("corner", "a")
//                        floatingActionButtonScreenShot.x =
//                            (floatingActionButton.x - floatingActionButtonScreenShot.width).toFloat()
//                        floatingActionButtonVideo.x =
//                            (floatingActionButton.x- floatingActionButtonVideo.width).toFloat()
//                        floatingActionButtonAudio.x =
//                            (floatingActionButton.x - floatingActionButtonAudio.width).toFloat()
//                        floatingActionButtonScreenShot.y = windowManagerParams.y.toFloat()
//                        floatingActionButtonVideo.y =  windowManagerParams.y.toFloat()
//                        floatingActionButtonAudio.y = windowManagerParams.y.toFloat()
                    } else if (event.rawX -  (floatingActionButton.width) < 0) {
                        Log.d("corner", "b")
                        windowManagerParams.x = -(deviceWidth / 2)
                      //  ((floatingActionButton.parent as FrameLayout).layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START
                        (floatingActionButton.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START
                        (floatingActionButtonScreenShot.layoutParams as FrameLayout.LayoutParams).marginStart = 150
                        (floatingActionButtonVideo.layoutParams as FrameLayout.LayoutParams).marginStart= 300
                        (floatingActionButtonAudio.layoutParams as FrameLayout.LayoutParams).marginStart = 450
//                        floatingActionButton.x = 0F
//                        floatingActionButtonScreenShot.x =
//                            (windowManagerParams.x + 50).toFloat()
//                        floatingActionButtonVideo.x =
//                            (windowManagerParams.x + 100).toFloat()
//                        floatingActionButtonAudio.x =
//                            (windowManagerParams.x + 150).toFloat()
//                        floatingActionButtonScreenShot.y =  windowManagerParams.y.toFloat()
//                        floatingActionButtonVideo.y = windowManagerParams.y.toFloat()
//                        floatingActionButtonAudio.y = windowManagerParams.y.toFloat()
                    }
                    if (deviceHeight < (event.rawY + (floatingActionButton.height))) {
                        Log.d("corner", "c")
                        windowManagerParams.y = (deviceHeight / 2)
//                        floatingActionButton.y = (deviceHeight - floatingActionButton.height).toFloat()
//                        floatingActionButtonScreenShot.y =
//                            (windowManagerParams.y - floatingActionButtonScreenShot.height).toFloat()
//                        floatingActionButtonVideo.y =
//                            (windowManagerParams.y - floatingActionButtonVideo.height).toFloat()
//                        floatingActionButtonAudio.y =
//                            (windowManagerParams.y - floatingActionButtonAudio.height).toFloat()
//                        floatingActionButtonScreenShot.x =  windowManagerParams.x.toFloat()
//                        floatingActionButtonVideo.x =  windowManagerParams.x.toFloat()
//                        floatingActionButtonAudio.x =  windowManagerParams.x.toFloat()
                    } else if (event.rawY -  (floatingActionButton.height) < 0) {
                        Log.d("corner", "d")
                        windowManagerParams.y = -(deviceHeight / 2)
                        (floatingActionButton.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.START
                        (floatingActionButtonScreenShot.layoutParams as FrameLayout.LayoutParams).marginEnd = 150
                        (floatingActionButtonVideo.layoutParams as FrameLayout.LayoutParams).marginEnd= 300
                        (floatingActionButtonAudio.layoutParams as FrameLayout.LayoutParams).marginEnd = 450
////                        floatingActionButton.y = 0F
////                        floatingActionButtonScreenShot.y =
////                            floatingActionButtonScreenShot.height.toFloat()
////                        floatingActionButtonVideo.y =
////                            floatingActionButtonVideo.height.toFloat()
////                        floatingActionButtonAudio.y =
////                            floatingActionButtonAudio.height.toFloat()
//                        floatingActionButtonScreenShot.x =  windowManagerParams.x.toFloat()
//                        floatingActionButtonVideo.x =  windowManagerParams.x.toFloat()
//                        floatingActionButtonAudio.x =  windowManagerParams.x.toFloat()
                    }
                    lastAction = MotionEvent.ACTION_UP
                    windowManager.updateViewLayout(
                        windowManagerView,
                        windowManagerParams
                    )
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

    private fun resetOldCoordinates() {
        LoggerBirdService.floatingActionButtonLastDx = null
        LoggerBirdService.floatingActionButtonLastDy = null
        LoggerBirdService.floatingActionButtonScreenShotLastDx = null
        LoggerBirdService.floatingActionButtonScreenShotLastDy = null
        LoggerBirdService.floatingActionButtonAudioLastDx = null
        LoggerBirdService.floatingActionButtonAudioLastDy = null
        LoggerBirdService.floatingActionButtonVideoLastDx = null
        LoggerBirdService.floatingActionButtonVideoLastDy = null
    }
}