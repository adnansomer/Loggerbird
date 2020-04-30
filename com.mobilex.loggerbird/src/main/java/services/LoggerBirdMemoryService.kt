package services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

internal class LoggerBirdMemoryService : Service() {
    private var memoryThreshold: Long = 4180632L
    private var formattedTime: String? = null
    private var memoryOverused: Boolean = false
    private var coroutineCallMemoryUsageDetails: CoroutineScope = CoroutineScope(Dispatchers.IO)

    //Static variables
    companion object {
        private var timer: Timer? = null
        private var timerTask: TimerTask? = null
        internal var stringBuilderMemoryUsage = StringBuilder()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * This Method Called When Service Detect's An OnCreate State In The Current Activity.
     */
    override fun onCreate() {
        super.onCreate()
        try {
            coroutineCallMemoryUsageDetails.async {
                startMemoryUsage()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This Method Called When Service In onStartCommand State
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    /**
     * This Method Called When Service In onTaskRemoved State and stops observing memory consumption
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            stopMemoryUsage()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This Method Called When Service In onDestroy State.
     */
    override fun onDestroy() {
        super.onDestroy()
        try {
            stopMemoryUsage()
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This function starts time to get Memory Usage data every 5 seconds.
     * @var timer starts timer to count.
     */
    private fun startMemoryUsage() {
        timer = Timer()
        initializeMemoryUsage()
        timer!!.schedule(
            timerTask, 0, 20000
        )
    }

    /**
     * This Method Runs the Timer to See Memory Usage
     */
    private fun initializeMemoryUsage() {
        timerTask = object : TimerTask() {
            override fun run() {
                takeMemoryUsageDetails()
            }
        }
    }

    /**
     * This Method Stops the Timer
     */
    private fun stopMemoryUsage() {
        try {
            if (timer != null) {
                timer!!.cancel()
                timer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun takeMemoryUsageDetails() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            val runtime: Runtime = Runtime.getRuntime()
            val runtimeTotalMemory = runtime.totalMemory()
            val runtimeFreeMemory = runtime.freeMemory()
            val usedMemorySize = (runtimeTotalMemory - runtimeFreeMemory)
            if (usedMemorySize > memoryThreshold) {
                memoryOverused = true
                stringBuilderMemoryUsage.append("Memory Overused: $memoryOverused\nMemory Usage: $usedMemorySize Bytes\n")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.serviceTag
            )
        }
    }
}
