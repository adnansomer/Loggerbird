package loggerbird.listeners.layouts

import android.os.Build
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import loggerbird.constants.Constants
import loggerbird.LoggerBird


//This class is used for observing components in the current view.
internal class LayoutOnTouchListener(
) : View.OnTouchListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            Log.d("touch_clicked","Id:" +view.id.toString() + "\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.layoutOnTouchTag
            )
        }
        return false
    }
}
