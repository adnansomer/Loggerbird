package loggerbird.listeners.layouts

import android.os.Build
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import loggerbird.constants.Constants
import loggerbird.LoggerBird
import loggerbird.observers.LogActivityLifeCycleObserver
import loggerbird.services.LoggerBirdService


//This class is used for observing components in the current view.
internal class LayoutOnTouchListener(
) : View.OnTouchListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            Log.d("touch_x", event.rawX.toString())
            Log.d("touch_y", event.rawY.toString())
            LogActivityLifeCycleObserver.hashMapActivityComponents[LoggerBirdService.loggerBirdService.returnActivity()]?.forEach {
                val locations = IntArray(2)
                it.getLocationOnScreen(locations)
                if ((event.rawX.toInt() <= (locations[0] + it.width ) && (event.rawX.toInt() >= (locations[0] - it.width)))
                    && ((event.rawY.toInt() <= (locations[1] + it.height) && (event.rawY.toInt() >= (locations[1] - it.height))))){
                    Log.d("touch_clicked", "Id:" + it.id + "\n" + it.toString())
                }else{
                    Log.d("touch_false","false")
                }
            }
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
