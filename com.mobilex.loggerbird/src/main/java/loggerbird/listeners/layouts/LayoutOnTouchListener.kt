package loggerbird.listeners.layouts

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import loggerbird.constants.Constants
import loggerbird.LoggerBird
import loggerbird.observers.LogActivityLifeCycleObserver
import loggerbird.observers.LogFragmentLifeCycleObserver
import loggerbird.services.LoggerBirdService


//This class is used for observing components in the current view.
internal class LayoutOnTouchListener(
    private val activity: Activity? = null, private val fragment: Fragment? = null
) : View.OnTouchListener {
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            Log.d("touch_x", event.rawX.toString())
            Log.d("touch_y", event.rawY.toString())
            if(activity != null){
                LogActivityLifeCycleObserver.hashMapActivityComponents[activity]?.forEach {
//                    val locations = IntArray(2)
//                    it.getLocationOnScreen(locations)
//                    if ((event.rawX.toInt() <= (locations[0] + it.width) && (event.rawX.toInt() >= (locations[0] - it.width)))
//                        && ((event.rawY.toInt() <= (locations[1] + it.height) && (event.rawY.toInt() >= (locations[1] - it.height))))
//                    ) {
//                        Log.d("touch_clicked_activity", "Id:" + it.id + "\n" + it.toString())
//                    } else {
//                        Log.d("touch_false_activity", "false")
//                    }
                    val rect = Rect(it.left,it.top,it.right,it.bottom)
                    if(rect.contains(event.x.toInt(),event.y.toInt())){
                     Log.d("touch_clicked_activity", "Id:" + it.id + "\n" + it.toString())
                    }
                }
            }else if(fragment != null){
                LogFragmentLifeCycleObserver.hashMapFragmentComponents[fragment]?.forEach {
//                    val locations = IntArray(2)
//                    it.getLocationOnScreen(locations)
//                    if ((event.rawX.toInt() <= (locations[0] + it.width) && (event.rawX.toInt() >= (locations[0] - it.width)))
//                        && ((event.rawY.toInt() <= (locations[1] + it.height) && (event.rawY.toInt() >= (locations[1] - it.height))))
//                    ) {
//                        Log.d("touch_clicked_fragment", "Id:" + it.id + "\n" + it.toString())
//                    } else {
//                        Log.d("touch_false_fragment", "false")
//                    }
                    val rect = Rect(it.left,it.top,it.right,it.bottom)
                    if(rect.contains(event.x.toInt(),event.y.toInt())){
                        Log.d("touch_clicked_fragment", "Id:" + it.id + "\n" + it.toString())
                    }
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
