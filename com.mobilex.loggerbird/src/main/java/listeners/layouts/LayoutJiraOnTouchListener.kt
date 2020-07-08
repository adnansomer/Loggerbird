package listeners.layouts

import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobilex.loggerbird.R
import constants.Constants
import loggerbird.LoggerBird


//This class is used for making jira layout movable.
internal class LayoutJiraOnTouchListener(
    private val windowManager: WindowManager,
    private val windowManagerView: View,
    private val windowManagerParams: WindowManager.LayoutParams
) : View.OnTouchListener {
    private var windowManagerDx: Float = 0F
    private var windowManagerDy: Float = 0F
    private var lastAction: Int = 0
//    private val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
//    private val deviceHeight = Resources.getSystem().displayMetrics.heightPixels
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
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
                tag = Constants.feedbackOnTouchTag
            )
        }
        return false
    }
}
