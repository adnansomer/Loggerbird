package deneme.example.loggerbird

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
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
class LogLifeCycleObserver() : LifecycleObserver, FragmentManager.FragmentLifecycleCallbacks() {
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
     * @return Boolean value.
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
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreateListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onCreate"
            if (fragmentManager != null) {
                onFragmentAttached(
                    fm = fragmentManager!!,
                    f = fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex],
                    context = context
                )
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
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnStart State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStartListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStart"
            if (fragmentManager != null) {
                onFragmentStarted(
                    fm = fragmentManager!!,
                    f = fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex]
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnResume State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResumeListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onResume"
            if (fragmentManager != null) {
                onFragmentResumed(
                    fm = fragmentManager!!,
                    f = fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex]
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnPause State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPauseListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onPause"
            if (fragmentManager != null) {
                onFragmentPaused(
                    fm = fragmentManager!!,
                    f = fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex]
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnDestroy State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroyListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onDestroy"
            if (fragmentManager != null) {
                onFragmentDestroyed(
                    fm = fragmentManager!!,
                    f = fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex]
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's OnDestroy State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * @var if method is called with fragmentManager then it will take current state's according to the fragment and it's life cycle otherwise it will take current state's according to the activity and it's life cycle.
     * @var if method is called with fragmentManager then stringBuilderLifeCycleObserver will print fragment detail's otherwise it will print activity details.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStopListener() {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStop"
            if (fragmentManager != null) {
                onFragmentStopped(
                    fm = fragmentManager!!,
                    f = fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex]
                )
            } else {
                stringBuilderLifeCycleObserver.append(" " + Constants.lifeCycleTag + ":" + context.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(e)
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnAttached State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateTimeInstance()
        formattedTime = formatter.format(date)
        currentLifeCycleState = "onFragmentAttached"
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
        onFragmentCreated(
            fm = fragmentManager!!,
            f = fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex],
            savedInstanceState = null
        )
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnCreate State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        try {
            super.onFragmentCreated(fm, f, savedInstanceState)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentCreate"
            stringBuilderLifeCycleObserver.append(
                " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex].tag + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(exception = e)
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnStarted State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentStarted(fm, f)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentStarted"
            stringBuilderLifeCycleObserver.append(
                " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments.get(
                    fragmentManager!!.fragments.lastIndex
                ).tag + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(exception = e)
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnResumed State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentResumed(fm, f)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentResumed"
            stringBuilderLifeCycleObserver.append(
                " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments.get(
                    fragmentManager!!.fragments.lastIndex
                ).tag + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(exception = e)
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnPaused State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentPaused(fm, f)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentPaused"
            stringBuilderLifeCycleObserver.append(
                " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments.get(
                    fragmentManager!!.fragments.lastIndex
                ).tag + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(exception = e)
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnStop State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentStopped(fm, f)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentStopped"
            stringBuilderLifeCycleObserver.append(
                " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex].tag + " " + "$formattedTime:$currentLifeCycleState\n"
            )
            onFragmentDestroyed(
                fm = fragmentManager!!,
                f = fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex]
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(exception = e)
        }

    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnDestroyed State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentDestroyed(fm, f)
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateTimeInstance()
        formattedTime = formatter.format(date)
        currentLifeCycleState = "onFragmentDestroyed"
        stringBuilderLifeCycleObserver.append(
            " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex].tag + " " + "$formattedTime:$currentLifeCycleState\n"
        )
        onFragmentDetached(
            fm = fragmentManager!!,
            f = fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex]
        )
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnDeAttached State In The Current Activity Or Fragment.
     * Variables:
     * @var currentLifeCycle states takes current state as a String in the life cycle.
     * Exceptions:
     * @throws exception if error occurs and saves the exception in logExceptionDetails.
     */
    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentDetached(fm, f)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentDetached"
            stringBuilderLifeCycleObserver.append(
                " " + Constants.fragmentTag + ":" + fragmentManager!!.fragments[fragmentManager!!.fragments.lastIndex].tag + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LogDeneme.logExceptionDetails(exception = e)
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


