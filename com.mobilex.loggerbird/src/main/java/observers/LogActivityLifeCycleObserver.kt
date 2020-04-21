package observers

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import constants.Constants
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import services.LoggerBirdService

internal class LogActivityLifeCycleObserver(private val loggerBirdService: LoggerBirdService) :
    Activity(),
    Application.ActivityLifecycleCallbacks {
    //Global variables.
    private var stringBuilderBundle: StringBuilder = StringBuilder()
    private lateinit var context: Context
    private lateinit var intentService: Intent
    private var coroutineCallService: CoroutineScope = CoroutineScope(Dispatchers.IO)

    //Static global variables.
    internal companion object {
        private var currentLifeCycleState: String? = null
        private var formattedTime: String? = null
        internal var returnActivityLifeCycleClassName: String? = null
    }

    init {
        LoggerBird.stringBuilderActivityLifeCycleObserver.append("\n" + "Life Cycle Details:" + "\n")
    }

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        try {
            this.context = activity
            if (!this::intentService.isInitialized) {
                loggerBirdService.initializeActivity(activity = activity)
                coroutineCallService.async {
                    intentService = Intent(context, loggerBirdService.javaClass)
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityStarted(activity: Activity) {
        try {
            loggerBirdService.initializeNewActivity(activity = activity)
            val date = Calendar.getInstance().time
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
            val date = Calendar.getInstance().time
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onActivityPaused(activity: Activity) {
        try {
            if (activity is AppCompatActivity) {
//               LoggerBirdService.takeOldCoordinates()
            }
            if (LoggerBirdService.controlPermissionRequest) {
                (context as Activity).stopService(LoggerBirdService.intentForegroundServiceVideo)
            }
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onPause"
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

    override fun onActivityDestroyed(activity: Activity) {
        try {
//            if(coroutineCallService.isActive){
//                coroutineCallService.cancel()
//            }
            if (!this::intentService.isInitialized) {
                stopService(intentService)
            }
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onDestroy"
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
     * This Method Re-Creates Instantiation For stringBuilderLifeCycleObserver.
     */
    internal fun refreshLifeCycleObserverState() {
        LoggerBird.stringBuilderActivityLifeCycleObserver = StringBuilder()
    }

    /**
     * This Method Is Used For Getting Activity List.
     * Variables:
     * @var classList takes list of activities that are called with this observer.
     * @return ArrayList<String>.
     */
    internal fun returnClassList(): ArrayList<String> {
        return LoggerBird.classList
    }

    /**
     * This Method Is Used For Printing Observer Outcome.
     * Variables:
     * @var stringBuilderLifeCycleObserver will print activity details.
     * @return String value.
     */
    internal fun returnActivityLifeCycleState(): String {
        return LoggerBird.stringBuilderActivityLifeCycleObserver.toString()
    }

    private fun checkAudioPermissionResult() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            Toast.makeText(context, "Permission Audio Denied!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission Audio Granted!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkWriteExternalStoragePermissionResult() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "Permission Write External Storage Granted!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkDrawOtherAppPermissionResult(activity: Activity) {
        if (!Settings.canDrawOverlays(activity)) {
            Toast.makeText(activity, "Permission DrawOtherApp Settings Denied!", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                activity,
                "Permission DrawOtherApp Settings Granted!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}