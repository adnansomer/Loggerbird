package observers

import android.app.Activity
import android.app.Application
import android.os.Bundle
import constants.Constants
import loggerbird.LoggerBird
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LogActivityLifeCycleObserver : Application.ActivityLifecycleCallbacks {
    //Global variables.
    private var stringBuilderActivityLifeCycleObserver: StringBuilder = StringBuilder()
    private var stringBuilderBundle: StringBuilder = StringBuilder()
    private var classList: ArrayList<String> = ArrayList()

    //Static global variables.
    companion object {
        private var currentLifeCycleState: String? = null
        private var formattedTime: String? = null
        internal var returnActivityLifeCycleClassName: String? = null
    }

    init {
        stringBuilderActivityLifeCycleObserver.append("\n" + "Life Cycle Details:" + "\n")
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
            stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n" + stringBuilderBundle.toString() + "\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        try {
            returnActivityLifeCycleClassName = activity.javaClass.simpleName
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onCreate"
            stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            if (classList.isEmpty()) {
                classList.add(activity.javaClass.simpleName)
            }
            while (classList.iterator().hasNext()) {
                if (classList.contains(activity.javaClass.simpleName)) {
                    break
                } else {
                    classList.add(activity.javaClass.simpleName)
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

    override fun onActivityStarted(activity: Activity) {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStart"
            stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    override fun onActivityResumed(activity: Activity) {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onResume"
            stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    override fun onActivityPaused(activity: Activity) {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onPause"
            stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
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
            stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
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
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onDestroy"
            stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
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
        stringBuilderActivityLifeCycleObserver = StringBuilder()
    }

    /**
     * This Method Is Used For Getting Activity List.
     * Variables:
     * @var classList takes list of activities that are called with this observer.
     * @return ArrayList<String>.
     */
    internal fun returnClassList(): ArrayList<String> {
        return classList
    }

    /**
     * This Method Is Used For Printing Observer Outcome.
     * Variables:
     * @var stringBuilderLifeCycleObserver will print activity details.
     * @return String value.
     */
    internal fun returnActivityLifeCycleState(): String {
        return stringBuilderActivityLifeCycleObserver.toString()
    }
}