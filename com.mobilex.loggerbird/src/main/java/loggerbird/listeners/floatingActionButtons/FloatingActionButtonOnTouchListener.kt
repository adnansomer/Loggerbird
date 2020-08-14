package loggerbird.listeners.floatingActionButtons

import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.google.android.material.circularreveal.CircularRevealLinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import loggerbird.LoggerBird
import loggerbird.constants.Constants

//This class is used for making floating action buttons movable.
internal class FloatingActionButtonOnTouchListener(
    private val windowManager: WindowManager,
    private val windowManagerView: View,
    private val windowManagerParams: WindowManager.LayoutParams,
    private val floatingActionButton: FloatingActionButton,
    private val floatingActionButtonScreenShot: FloatingActionButton,
    private val floatingActionButtonVideo: FloatingActionButton,
    private val floatingActionButtonAudio: FloatingActionButton,
    private val textViewCounterVideo: TextView,
    private val textViewCounterAudio: TextView,
    private val textViewVideoSize: TextView,
    private val textViewAudioSize: TextView,
    private val revealLinearLayoutShare: CircularRevealLinearLayout
) : View.OnTouchListener {
    private var windowManagerDx: Float = 0F
    private var windowManagerDy: Float = 0F
    private var lastAction: Int = 0
    private val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
    private val deviceHeight = Resources.getSystem().displayMetrics.heightPixels
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    floatingActionButtonScreenShot.visibility = View.GONE
                    floatingActionButtonVideo.visibility = View.GONE
                    floatingActionButtonAudio.visibility = View.GONE
                    textViewCounterVideo.visibility = View.GONE
                    textViewCounterAudio.visibility = View.GONE
                    textViewVideoSize.visibility = View.GONE
                    textViewAudioSize.visibility = View.GONE
                    windowManagerDx = windowManagerParams.x - event.rawX
                    windowManagerDy = windowManagerParams.y - event.rawY
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
                    if (deviceWidth < (event.rawX + (floatingActionButton.width))) {
                        Log.d("corner", "a")
                            windowManagerParams.x = deviceWidth / 2
                        (floatingActionButton.parent as LinearLayout).orientation = LinearLayout.HORIZONTAL
                        (floatingActionButton.parent as LinearLayout).layoutDirection = View.LAYOUT_DIRECTION_RTL
                    } else if (event.rawX - (floatingActionButton.width) < 0) {
                        Log.d("corner", "b")
                            windowManagerParams.x = -(deviceWidth / 2)
                        (floatingActionButton.parent as LinearLayout).orientation = LinearLayout.HORIZONTAL
                        (floatingActionButton.parent as LinearLayout).layoutDirection = View.LAYOUT_DIRECTION_LTR
                    }
                    if (deviceHeight < (event.rawY + (floatingActionButton.height))) {
                        Log.d("corner", "c")
                            windowManagerParams.y = (deviceHeight / 2)
                        reverseLayout(viewGroup = floatingActionButton.parent as LinearLayout)
                        (floatingActionButton.parent as LinearLayout).orientation = LinearLayout.VERTICAL
                    } else if (event.rawY - (floatingActionButton.height) < 0) {
                        Log.d("corner", "d")
                            windowManagerParams.y = -(deviceHeight / 2)
                        (floatingActionButton.parent as LinearLayout).orientation = LinearLayout.VERTICAL
                    }
//                    floatingActionButton.setImageResource(R.drawable.ic_close_black_24dp)
//                    floatingActionButtonScreenShot.visibility = View.VISIBLE
//                    floatingActionButtonVideo.visibility = View.VISIBLE
//                    floatingActionButtonAudio.visibility = View.VISIBLE

                    if (!revealLinearLayoutShare.isVisible) {
                        floatingActionButton.performClick()
                    }
                    lastAction = MotionEvent.ACTION_UP
                        windowManager.updateViewLayout(
                            windowManagerView,
                            windowManagerParams
                        )
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
        return true
    }
    private fun reverseLayout(viewGroup: ViewGroup){
        for (child in (viewGroup).childCount - 1 downTo 0) {
            val item: View = (viewGroup).getChildAt(child)
            (viewGroup).removeViewAt(child)
            (viewGroup).addView(item)
        }
    }
}
