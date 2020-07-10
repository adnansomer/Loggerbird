package observers

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import constants.Constants
import loggerbird.LoggerBird
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//This class is unused because of there is already fragment and activity life cycle listeners.Although there might be certain cases that this class might be useful.
internal class LogLifeCycleObserver() :
    LifecycleObserver {
    //Global variables.
    private var stringBuilderLifeCycleObserver: StringBuilder = StringBuilder()
    private lateinit var context: Context
    private var classList: ArrayList<String> = ArrayList()

    //Static global variables.
    companion object {
        private var currentLifeCycleState: String? = null
        private var formattedTime: String? = null
        internal var returnLifeCycleClassName: String? = null
    }

    init {
        stringBuilderLifeCycleObserver.append("\n" + "Life Cycle Details:" + "\n")
    }

    /**
     * This Method Register A LifeCycle Observer For The Current Activity.
     * @param context is for getting reference from the application context , you must deploy this parameter.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    fun registerLifeCycle(context: Context) {
        try {
            deRegisterLifeCycle()
            this.context = context
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            returnLifeCycleClassName = context.javaClass.simpleName
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method DeRegister A LifeCycle Observer From The Current Activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    fun deRegisterLifeCycle() {
        try {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnCreate State In The Current Activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreateListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onCreate"
            stringBuilderLifeCycleObserver.append(Constants.activityTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
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
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnStart State In The Current Activity Or Fragment.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStartListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStart"
            stringBuilderLifeCycleObserver.append(Constants.activityTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnResume State In The Current Activity Or Fragment.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResumeListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onResume"
            stringBuilderLifeCycleObserver.append(Constants.activityTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnPause State In The Current Activity Or Fragment.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPauseListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onPause"
            stringBuilderLifeCycleObserver.append(Constants.activityTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityLifeCycleObserverTag
            )
        }
    }


    /**
     * This Method Called When LifeCycle Observer Detect's OnDestroy State In The Current Activity Or Fragment.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStopListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStop"
            stringBuilderLifeCycleObserver.append(Constants.activityTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnDestroy State In The Current Activity Or Fragment.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun onDestroyListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onDestroy"
            stringBuilderLifeCycleObserver.append(Constants.activityTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityLifeCycleObserverTag
            )
        }
    }


    /**
     * This Method Is Used For Printing Observer Outcome.
     * @return String value.
     */
    internal fun returnActivityLifeCycleState(): String {
        return stringBuilderLifeCycleObserver.toString()
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderLifeCycleObserver.
     */
    internal fun refreshLifeCycleObserverState() {
        stringBuilderLifeCycleObserver = StringBuilder()
    }

    /**
     * This Method Is Used For Getting Activity List.
     * @return ArrayList<String>.
     */
    internal fun returnClassList(): ArrayList<String> {
        return classList
    }

}


