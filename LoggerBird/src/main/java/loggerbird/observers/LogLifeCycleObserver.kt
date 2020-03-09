package loggerbird.observers

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import loggerbird.constants.Constants
import loggerbird.LoggerBird
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//LogLifeCycleObserver class is used for attaching lifecycle observer for your current activity.
class LogLifeCycleObserver() : LifecycleObserver {
    //Global variables.
    private var stringBuilderLifeCycleObserver: StringBuilder = StringBuilder()
    private lateinit var context: Context
    private var classList: ArrayList<String> = ArrayList()

    //Static global variables.
    companion object {
        private var currentLifeCycleState: String? = null
        private var formattedTime: String? = null
    }

    /**
     * This Method Register A LifeCycle Observer For The Current Activity.
     * Parameters:
     * @param context is for getting reference from the application context , you must deploy this parameter.
     * Exceptions:
     * @throws exception if error occurs then deneme.example.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    fun registerLifeCycle(context: Context) {
        try {
            deRegisterLifeCycle()
            this.context = context
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.takeExceptionDetails(e)
            LoggerBird.saveExceptionDetails()
        }
    }

    /**
     * This Method DeRegister A LifeCycle Observer From The Current Activity.
     * Exceptions:
     * @throws exception if error occurs then deneme.example.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    fun deRegisterLifeCycle() {
        try {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.takeExceptionDetails(e)
            LoggerBird.saveExceptionDetails()
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnCreate State In The Current Activity.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var classList will take list of activities.
     * @var stringBuilderLifeCycleObserver used for printing fragment detail's.
     * Exceptions:
     * @throws exception if error occurs and saves the deneme.example.loggerbird.exception in logExceptionDetails.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreateListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onCreate"
            stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            if (classList.isEmpty()) {
                classList.add(context.javaClass.simpleName)
            }
            while (classList.iterator().hasNext()) {
                if (classList.contains(context.javaClass.simpleName)) {
                    break
                } else {
                    classList.add(context.javaClass.simpleName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.takeExceptionDetails(e)
            LoggerBird.saveExceptionDetails()
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnStart State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var classList will take list of activities.
     * @var stringBuilderLifeCycleObserver used for printing fragment detail's.
     * Exceptions:
     * @throws exception if error occurs and saves the deneme.example.loggerbird.exception in logExceptionDetails.
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStartListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStart"
            stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.takeExceptionDetails(
                exception = e
            )
            LoggerBird.saveExceptionDetails()
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnResume State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var classList will take list of activities.
     * @var stringBuilderLifeCycleObserver used for printing fragment detail's.
     * Exceptions:
     * @throws exception if error occurs and saves the deneme.example.loggerbird.exception in logExceptionDetails.
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResumeListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onResume"
            stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.takeExceptionDetails(e)
            LoggerBird.saveExceptionDetails()
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnPause State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var classList will take list of activities.
     * @var stringBuilderLifeCycleObserver used for printing fragment detail's.
     * Exceptions:
     * @throws exception if error occurs and saves the deneme.example.loggerbird.exception in logExceptionDetails.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPauseListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onPause"
            stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.takeExceptionDetails(e)
            LoggerBird.saveExceptionDetails()
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnDestroy State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var classList will take list of activities.
     * @var stringBuilderLifeCycleObserver used for printing fragment detail's.
     * Exceptions:
     * @throws exception if error occurs and saves the deneme.example.loggerbird.exception in logExceptionDetails.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroyListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onDestroy"
            stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.takeExceptionDetails(e)
            LoggerBird.saveExceptionDetails()
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnDestroy State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var classList will take list of activities.
     * @var stringBuilderLifeCycleObserver used for printing fragment detail's.
     * Exceptions:
     * @throws exception if error occurs and saves the deneme.example.loggerbird.exception in logExceptionDetails.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStopListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStop"
            stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.takeExceptionDetails(e)
            LoggerBird.saveExceptionDetails()
        }
    }

    /**
     * This Method Is Used For Printing Observer Outcome.
     * Variables:
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * @return String value.
     */
    fun returnActivityLifeCycleState(): String {
        return stringBuilderLifeCycleObserver.toString()
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderLifeCycleObserver.
     */
    fun refreshLifeCycleObserverState() {
        stringBuilderLifeCycleObserver = StringBuilder()
    }

    /**
     * This Method Is Used For Getting Activity Or Fragment List.
     * Variables:
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * @return ArrayList<String>.
     */
    fun returnClassList(): ArrayList<String> {
        return classList
    }

}


