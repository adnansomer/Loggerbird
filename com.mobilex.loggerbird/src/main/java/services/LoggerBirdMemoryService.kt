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

/**
 * This class is a service for obtaning memory consumption.
 */
internal class LoggerBirdMemoryService : Service() {
    private var memoryThreshold: Long = 4180632L
    private var formattedTime: String? = null
    private var memoryOverused: Boolean = false
    private var coroutineCallMemoryUsageDetails: CoroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private var timer: Timer? = null
        private var timerTask: TimerTask? = null
        internal var stringBuilderMemoryUsage = StringBuilder()
    }

    /**
     * This method called when service in onBind state.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * This method called when service detect's an OnCreate state in the current activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onCreate() {
        super.onCreate()
        try {
            coroutineCallMemoryUsageDetails.async {
                startMemoryUsage()
            }
        } catch (e: Exception) {
            e.printStackTrace()

            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.memoryServiceTag)
        }
    }

    /**
     * This method called when service in onStartCommand state.
     * @return START_NOT_STICKY not to stick into device as a service
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    /**
     * This Method Called When Service In onTaskRemoved State and stops observing memory consumption
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            stopMemoryUsage()
        } catch (e: Exception) {
            e.printStackTrace()

            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.memoryServiceTag)
        }
    }

    /**
     * This method called when service in onDestroy State.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onDestroy() {
        super.onDestroy()
        try {
            stopMemoryUsage()
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()

            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.memoryServiceTag)
        }
    }

    /**
     * This method starts time to get memory usage data in every 5 seconds.
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
     * This method runs the timer to see instant memory usage
     */
    private fun initializeMemoryUsage() {
        timerTask = object : TimerTask() {
            override fun run() {
                takeMemoryUsageDetails()
            }
        }
    }

    /**
     * This method stops timer for counting memory usage.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun stopMemoryUsage() {
        try {
            if (timer != null) {
                timer!!.cancel()
                timer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.memoryServiceTag)
        }
    }

    /**
     * This method gives memory
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                tag = Constants.memoryServiceTag
            )
        }
    }
}
