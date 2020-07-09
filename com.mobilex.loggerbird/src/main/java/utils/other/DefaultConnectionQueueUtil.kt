package utils.other

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.*

internal class DefaultConnectionQueueUtil {
    private lateinit var timerTask: TimerTask
    internal fun checkQueueTimeMainThreadAction(activity: Activity? = null, runnable: Runnable,delay:Long) {
        val timerQueue = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    runnable.run()
                }
            }
        }
        timerQueue.schedule(timerTask, delay)
    }
    internal fun checkQueueTimeCoroutineThreadAction(runnable: Runnable,delay:Long) {
        val timerQueue = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                val coroutineCallQueue = CoroutineScope(Dispatchers.IO)
                coroutineCallQueue.async {
                    runnable
                }
            }
        }
        timerQueue.schedule(timerTask, delay)
    }
    internal fun cancelTimer(){
        if(this::timerTask.isInitialized){
            timerTask.cancel()
        }
    }
}