package utils.other

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.mobilex.loggerbird.R
import constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird
import java.util.*

/**
 * This class is used for create custom toast.
 */
internal class DefaultToast {
    private lateinit var windowManagerParamsToast: WindowManager.LayoutParams
    private var windowManagerToast: Any? = null
    private var toastView: View? = null
    private var textViewToast: TextView? = null
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)

    /**
     * This method is used for attach custom toast
     * @param activity is used for getting reference of current activity.
     * @param toastMessage is used for getting text that used entered.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun attachToast(activity: Activity, toastMessage: String) {
        try {
            detachToast()
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            toastView =
                LayoutInflater.from(activity).inflate(R.layout.default_toast, rootView, false)
            windowManagerParamsToast = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            } else {
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            }
            windowManagerParamsToast.gravity = Gravity.BOTTOM or Gravity.CENTER
            windowManagerToast = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerToast as WindowManager).addView(
                toastView,
                windowManagerParamsToast
            )
            if (toastView != null) {
                var controlGlobalLayoutListener = false
                textViewToast = toastView!!.findViewById(R.id.textView_default_toast)
                textViewToast!!.viewTreeObserver.addOnGlobalLayoutListener {
                    if (!controlGlobalLayoutListener) {
                        controlGlobalLayoutListener = true
                        val textViewLayout = FrameLayout.LayoutParams(
                            3 * (textViewToast!!.width),
                            3 * textViewToast!!.height
                        )
                        textViewLayout.setMargins(0, 0, 0, 100)
                        textViewToast!!.layoutParams = textViewLayout
                        textViewToast!!.text = toastMessage
                        toastTimer(activity = activity)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            detachToast()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.loggerBirdToastTag)
        }
    }

    /**
     * This method is used for detach custom toast.
     */
    private  fun detachToast() {
        if (this.textViewToast != null && this.toastView != null) {
                    (windowManagerToast as WindowManager).removeViewImmediate(toastView)
                    toastView = null
                    textViewToast = null
        }
    }

    /**
     * This method is used for arrange the time that toast will shown.
     * @param activity is used for getting reference of current activity.
     */
    private fun  toastTimer(activity: Activity) {
        val timerToast = Timer()
        val timerTaskToast = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    detachToast()
                }
            }
        }
        timerToast.schedule(timerTaskToast, 2000)
    }
}