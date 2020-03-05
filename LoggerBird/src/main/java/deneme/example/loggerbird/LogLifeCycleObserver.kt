package deneme.example.loggerbird

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//LogLifeCycleObserver class is used for attaching lifecycle observer for your current activity or fragment.
class LogLifeCycleObserver() : LifecycleObserver {
    private var stringBuilderLifeCycleObserver: StringBuilder = StringBuilder()
    private lateinit var context: Context
    private var fragmentManager: FragmentManager? = null
    private var classList: ArrayList<String> = ArrayList()
    /**
     * This Method Register A LifeCycle Observer For The Current Activity Or Fragment.
     * Parameters:
     * @param context is for getting reference from the application context , you must deploy this parameter.
     * @param fragmentManager is used for getting details from FragmentManager which is used for  tracking life cycle of Fragments rather than activity.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     * @throws exception if logInit method return value is false.
     * @return String value.
     */
    fun registerLifeCycle(context: Context, fragmentManager: FragmentManager?): Boolean {
        try {
            deRegisterLifeCycle()
            this.context = context
            this.fragmentManager = fragmentManager
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
        return false
    }

    /**
     * This Method DeRegister A LifeCycle Observer From The Current Activity Or Fragment.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     * @throws exception if logInit method return value is false.
     * @return String value.
     */
    fun deRegisterLifeCycle(): Boolean {
        try {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
        return false
    }

    companion object {
        private var currentLifeCycleState: String? = null
        private var formattedTime: String? = null
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnCreate State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager classList will take fragment's in the fragment manager otherwise it will take all activity list's.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     * @return String value.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreateListener(): String {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onCreate"
            if (fragmentManager != null) {
                stringBuilderLifeCycleObserver.append(" " + Constants.fragmentTag + ":" + fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex].tag + " " + "$formattedTime:$currentLifeCycleState\n")
                if (classList.isEmpty()) {
                    classList.add(fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex].tag!!)
                }
                while (classList.iterator().hasNext()) {
                    if (classList.contains(fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex].tag)) {
                        break;
                    } else {
                        classList.add(fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex].tag!!)
                    }
                }
            } else {
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
            }
            return stringBuilderLifeCycleObserver.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
        return stringBuilderLifeCycleObserver.toString()
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnStart State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     * @return String value.
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStartListener(): String {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStart"
            if (fragmentManager != null) {
                stringBuilderLifeCycleObserver.append(
                    " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments.get(
                        fragmentManager!!.fragments.lastIndex
                    ).tag + " " + "$formattedTime:$currentLifeCycleState\n"
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
            return stringBuilderLifeCycleObserver.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stringBuilderLifeCycleObserver.toString()
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnResume State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     * @return String value.
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResumeListener(): String {
        try {
            val date= Calendar.getInstance().time
            val formatter= SimpleDateFormat.getDateTimeInstance()
            formattedTime =formatter.format(date)
            currentLifeCycleState = "onResume"
            if (fragmentManager != null) {
                stringBuilderLifeCycleObserver.append(
                    " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments.get(
                        fragmentManager!!.fragments.lastIndex
                    ).tag + " " + "$formattedTime:$currentLifeCycleState\n"
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
            return stringBuilderLifeCycleObserver.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
        return stringBuilderLifeCycleObserver.toString()
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnPause State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     * @return String value.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPauseListener(): String {
        try {
            val date= Calendar.getInstance().time
            val formatter= SimpleDateFormat.getDateTimeInstance()
            formattedTime =formatter.format(date)
            currentLifeCycleState = "onPause"
            if (fragmentManager != null) {
                stringBuilderLifeCycleObserver.append(
                    " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments.get(
                        fragmentManager!!.fragments.lastIndex
                    ).tag + " " + "$formattedTime:$currentLifeCycleState\n"
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
            return stringBuilderLifeCycleObserver.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
        return stringBuilderLifeCycleObserver.toString()
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnDestroy State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     * @return String value.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroyListener(): String {
        try {
            val date= Calendar.getInstance().time
            val formatter= SimpleDateFormat.getDateTimeInstance()
            formattedTime =formatter.format(date)
            currentLifeCycleState = "onDestroy"
            if (fragmentManager != null) {
                stringBuilderLifeCycleObserver.append(
                    " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments.get(
                        fragmentManager!!.fragments.lastIndex
                    ).tag + " " + "$formattedTime:$currentLifeCycleState\n"
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
            return stringBuilderLifeCycleObserver.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
        return stringBuilderLifeCycleObserver.toString()
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnDestroy State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     * @return String value.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStopListener(): String {
        try {
            val date= Calendar.getInstance().time
            val formatter= SimpleDateFormat.getDateTimeInstance()
            formattedTime =formatter.format(date)
            currentLifeCycleState = "onStop"
            if (fragmentManager != null) {
                stringBuilderLifeCycleObserver.append(
                    " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments.get(
                        fragmentManager!!.fragments.lastIndex
                    ).tag + " " + "$formattedTime:$currentLifeCycleState\n"
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
            return stringBuilderLifeCycleObserver.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
        return stringBuilderLifeCycleObserver.toString()
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


