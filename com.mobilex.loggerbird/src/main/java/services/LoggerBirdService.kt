package services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import constants.Constants
import loggerbird.LoggerBird
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

internal class LoggerBirdService() : Service() {
    //Global variables:
    private var intentService: Intent? = null
    private var currentLifeCycleState: String? = null
    private var formattedTime: String? = null

    //Static global variables:
    companion object {
        var onDestroyMessage: String? = null
    }

    /**
     * This Method Called When Service Detect's An  OnBind State In The Current Activity.
     * Parameters:
     * @param intent used for getting context reference from the Activity.
     * @return IBinder value.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * This Method Called When Service Detect's An  OnBind State In The Current Activity.
     * Parameters:
     * @param intent used for getting context reference from the Activity.
     * @param flags (no idea).
     * @param startId (no idea).
     * @return IBinder value.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intentService = intent
        return START_STICKY
    }

    /**
     * This Method Called When Service Detect's An  OnBind State In The Current Activity.
     * Parameters:
     * @param rootIntent used for getting context reference from the Activity.
     * Variables:
     * @var currentLifeCycleState states takes current state as a String in the Activity life cycle.
     * @var onDestroyMessage used for providing detail's for stringBuilder in LoggerBird.takelifeCycleDetails.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     * @return IBinder value.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onDestroy"
            onDestroyMessage =
                " " + Constants.lifeCycleTag + ":" + intentService!!.component!!.className + " " + "${formattedTime}:${currentLifeCycleState}\n"
            LoggerBird.takeLifeCycleDetails()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.serviceTag)
        }
    }

    /**
     * This Method Called When Service Detect's An  OnCreate State In The Current Activity.
     */
    override fun onCreate() {
        super.onCreate()
    }

    /**
     * This Method Called When Service Detect's An  OnDestroy State In The Current Activity.
     */
    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
    }
}