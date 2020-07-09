package observers

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mobilex.loggerbird.R
import constants.Constants
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import services.LoggerBirdService
import java.util.concurrent.TimeUnit

internal class LogActivityLifeCycleObserver() :
    Activity(),
    Application.ActivityLifecycleCallbacks {
    //Global variables.
    private var stringBuilderBundle: StringBuilder = StringBuilder()
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var intentService: Intent
    private var coroutineCallService: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var activityStartTime: Long? = null
    private var activityPauseTime: Long? = null
    private var totalActivityTime: Long? = 0
    private var totalTimeSpentInApplication:Long? = 0

    //Static global variables.
    internal companion object {
        private var currentLifeCycleState: String? = null
        private var formattedTime: String? = null
        internal var returnActivityLifeCycleClassName: String? = null
        internal lateinit var logActivityLifeCycleObserverInstance: LogActivityLifeCycleObserver
    }
    //Constructor.
    init {
        LoggerBird.stringBuilderActivityLifeCycleObserver.append("\n" + "Life Cycle Details:" + "\n")
    }

    /**
     * This method is called when activity is in onSaveInstance state.
     */
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        try {
            stringBuilderBundle = StringBuilder()
            for (outStateItem in outState.keySet()) {
                stringBuilderBundle.append(
                    "$outStateItem:" + outState.get(
                        outStateItem
                    ) + "\n"
                )
            }
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onSaveInstanceState"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n" + stringBuilderBundle.toString() + "\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    /**
     * This method is called when activity is in onCreate state.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        try {
            this.activity = activity
            this.context = activity
            logActivityLifeCycleObserverInstance = this
            if (!this::intentService.isInitialized) {
                coroutineCallService.async {
                    intentService = Intent(context, LoggerBirdService::class.java)
                    context.startService(intentService)
                }
            }
            LoggerBird.fragmentLifeCycleObserver =
                LogFragmentLifeCycleObserver()
            if ((activity is AppCompatActivity)) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    LoggerBird.fragmentLifeCycleObserver,
                    true
                )
            }
            returnActivityLifeCycleClassName = activity.javaClass.simpleName
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onCreate"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            if (LoggerBird.classList.isEmpty()) {
                LoggerBird.classList.add(activity.javaClass.simpleName)
            }
            while (LoggerBird.classList.iterator().hasNext()) {
                if (LoggerBird.classList.contains(activity.javaClass.simpleName)) {
                    break
                } else {
                    LoggerBird.classList.add(activity.javaClass.simpleName)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    /**
     * This method is called when activity is in onStart state.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityStarted(activity: Activity) {
        try {
            if (LoggerBirdService.controlLoggerBirdServiceInit()) {
                LoggerBirdService.loggerBirdService.initializeNewActivity(activity = activity)
            }
            val date = Calendar.getInstance().time
            activityStartTime = date.time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStart"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    /**
     * This method is called when activity is in onResume state.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResumed(activity: Activity) {
        try {
            if (LoggerBirdService.controlPermissionRequest) {
                when {
                    LoggerBirdService.controlWriteExternalPermission -> {
                        checkWriteExternalStoragePermissionResult()
                    }
                    LoggerBirdService.controlAudioPermission -> {
                        checkAudioPermissionResult()
                    }
                    LoggerBirdService.controlDrawableSettingsPermission -> {
                        checkDrawOtherAppPermissionResult(activity = activity)
                    }
                }
            }
            LoggerBirdService.controlPermissionRequest = false
            LoggerBirdService.controlWriteExternalPermission = false
            LoggerBirdService.controlAudioPermission = false
            LoggerBirdService.controlVideoPermission = false
            LoggerBirdService.controlDrawableSettingsPermission = false
            val date = Calendar.getInstance().time
            activityStartTime = date.time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onResume"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    /**
     * This method is called when activity is in onPause state.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onActivityPaused(activity: Activity) {
        try {
            if (LoggerBirdService.controlPermissionRequest) {
                if (LoggerBirdService.controlIntentForegroundServiceVideo()) {
                    (context as Activity).stopService(LoggerBirdService.intentForegroundServiceVideo)
                }
            }
            val date = Calendar.getInstance().time
            activityPauseTime = date.time
            totalActivityTime = totalActivityTime!! +  activityPauseTime!! - activityStartTime!!
            totalTimeSpentInApplication = totalTimeSpentInApplication!! + totalActivityTime!!
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onPause"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName +" "+"Total time In This Activity:"+ timeString(totalActivityTime!!) +"\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    /**
     * This method is called when activity is in onStop state.
     */
    override fun onActivityStopped(activity: Activity) {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStop"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    /**
     * This method is called when activity is in onDestroy state.
     */
    override fun onActivityDestroyed(activity: Activity) {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onDestroy"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName +" "+ "Total activity time:"+ timeString(totalTimeSpentInApplication!!)+"\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderLifeCycleObserver.
     */
    internal fun refreshLifeCycleObserverState() {
        LoggerBird.stringBuilderActivityLifeCycleObserver = StringBuilder()
    }

    /**
     * This Method Is Used For Getting Activity List.
     * @return ArrayList<String>.
     */
    internal fun returnClassList(): ArrayList<String> {
        return LoggerBird.classList
    }

    /**
     * This Method Is Used For Printing Observer Outcome.
     * @return String value.
     */
    internal fun returnActivityLifeCycleState(): String {
        return LoggerBird.stringBuilderActivityLifeCycleObserver.toString()
    }

    /**
     * This method is used for checking for audio permission result.
     */
    private fun checkAudioPermissionResult() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            Toast.makeText(context, R.string.permission_audio_denied, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, R.string.permission_audio_granted, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * This method is used for checking for write-external storage permission result.
     */
    private fun checkWriteExternalStoragePermissionResult() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Toast.makeText(
                context,
                R.string.permission_write_external_storage_denied,
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            Toast.makeText(
                context,
                R.string.permission_write_external_storage_granted,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * This method is used for checking for draw other application permission result.
     * @param activity is used for getting reference of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkDrawOtherAppPermissionResult(activity: Activity) {
        LoggerBirdService.sd.start(sensorManager = LoggerBirdService.sensorManager)
        if (!Settings.canDrawOverlays(activity)) {
            Toast.makeText(activity, R.string.permission_draw_other_apps_denied, Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                activity,
                R.string.permission_draw_other_apps_granted,
                Toast.LENGTH_SHORT
            ).show()
            LoggerBirdService.loggerBirdService.initializeFloatingActionButton(activity = this.activity)
        }
    }

    /**
     * This method is used for getting current activity.
     */
    internal fun activityInstance(): Activity {
        return this.activity
    }

    /**
     * This method is used for formatting certain time value in day/hour/second/millisecond format.
     * @param remainingSeconds is for getting reference of time value.
     * @return String value.
     */
    private fun timeString(remainingSeconds: Long): String {
        return String.format(
            Locale.getDefault(), "%02d:%02d:%02d:%02d", TimeUnit.MILLISECONDS.toDays(remainingSeconds),
            TimeUnit.MILLISECONDS.toHours(remainingSeconds) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(remainingSeconds)),
            TimeUnit.MILLISECONDS.toMinutes(remainingSeconds) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    remainingSeconds
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(remainingSeconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    remainingSeconds
                )
            )
        )
    }

}