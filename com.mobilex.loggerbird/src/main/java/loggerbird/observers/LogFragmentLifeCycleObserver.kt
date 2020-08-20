package loggerbird.observers

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import loggerbird.constants.Constants
import loggerbird.LoggerBird
import loggerbird.utils.other.TimeFormatter
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

//LogFragmentCycleObserver class is used for attaching lifecycle observer for your current fragment.
internal class LogFragmentLifeCycleObserver(
) :
    FragmentManager.FragmentLifecycleCallbacks() {
    //Global variables.
    private var classList: ArrayList<String> = ArrayList()
    private val logComponentObserver = LogComponentObserver()
    private var fragmentStartTime: Long? = null
    private var fragmentPauseTime: Long? = null
    private var totalFragmentTime: Long? = 0
    private var totalTimeSpentInFragment: Long? = 0
    private val timeFormatter:TimeFormatter = TimeFormatter()
    //    private var totalTimeSpentInApplication: Long? = 0
    //Static global variables.
    companion object {
        internal var stringBuilderFragmentLifeCycleObserver: StringBuilder = StringBuilder()
        private var currentLifeCycleState: String? = null
        private var formattedTime: String? = null
        internal var hashMapFragmentComponents: HashMap<Fragment, ArrayList<View>> = HashMap()
    }

    //Constructor.
    init {
        stringBuilderFragmentLifeCycleObserver.append("\n" + "Fragment Details:" + "\n")
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnViewCreate State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @param v takes view of current Fragment.
     * @param savedInstanceState takes saved instances of current fragment.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentViewCreated(fm, f, v, savedInstanceState)
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentViewCreated"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
            if (classList.isEmpty()) {
                classList.add(className)
            }
            while (classList.iterator().hasNext()) {
                if (classList.contains(className)) {
                    break;
                } else {
                    classList.add(className)
                }
            }
            if (LoggerBird.classPathList.size > 20) {
                LoggerBird.classPathList.removeAt(LoggerBird.classPathCounter)
                LoggerBird.classPathListCounter.removeAt(LoggerBird.classPathCounter)
                LoggerBird.classPathList.add(
                    LoggerBird.classPathCounter,
                    className
                )
                LoggerBird.classPathListCounter.add(
                    LoggerBird.classPathCounter,
                    LoggerBird.classPathTotalCounter
                )
                LoggerBird.classPathCounter++
                if (LoggerBird.classPathCounter >= LoggerBird.classPathList.size) {
                    LoggerBird.classPathCounter = 0
                }
            } else {
                LoggerBird.classPathList.add(className)
                LoggerBird.classPathListCounter.add(LoggerBird.classPathTotalCounter)
            }
            LoggerBird.classPathTotalCounter++
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnAttach State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @param context takes Context of the current Fragment.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        try {
            super.onFragmentAttached(fm, f, context)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentAttached"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnPreAttach State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @param context takes Context of the current Fragment.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentPreAttached(fm, f, context)
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentPreAttached"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnSaveInstance State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @param outState takes saved instance of the current fragmemt.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        super.onFragmentSaveInstanceState(fm, f, outState)
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onSaveInstanceState"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack onViewDestroy State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        super.onFragmentViewDestroyed(fm, f)
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentViewDestroyed"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnPreCreate State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @param savedInstanceState takes saved instance of the current fragmemt.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentPreCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentPreCreated(fm, f, savedInstanceState)
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentPreCreated"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnActivityCreate State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @param savedInstanceState takes saved instance of the current fragmemt.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentActivityCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentActivityCreated(fm, f, savedInstanceState)
        try {
            super.onFragmentCreated(fm, f, savedInstanceState)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentActivityCreated"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnCreate State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @param savedInstanceState takes saved instance of the current fragmemt.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        try {
            super.onFragmentCreated(fm, f, savedInstanceState)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentCreate"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnStart State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentStarted(fm, f)
            val date = Calendar.getInstance().time
            fragmentStartTime = date.time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentStarted"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
            logComponentObserver.initializeLoggerBirdCoordinatorLayout(fragment = f)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnResume State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentResumed(fm, f)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentResumed"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnPause State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentPaused(fm, f)
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            val date = Calendar.getInstance().time
            fragmentPauseTime = date.time
            totalFragmentTime = totalFragmentTime!! + fragmentPauseTime!! - fragmentStartTime!!
            totalTimeSpentInFragment = totalTimeSpentInFragment!! + totalFragmentTime!!
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "Total time In This Fragment:" + timeFormatter.timeString(
                    totalFragmentTime!!
                ) + "\n"
            )
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentPaused"
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnStop State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentStopped(fm, f)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentStopped"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }

    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnDestroy State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentDestroyed(fm, f)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentDestroyed"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Called When LifeCycle Observer Detect's An FragmentCallBack OnDetach State In The Current Fragment.
     * @param fm takes FragmentManager Instance.
     * @param f takes current Fragment from FragmentManager.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        try {
            super.onFragmentDetached(fm, f)
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onFragmentDetached"
            val className: String = if (f.tag != null) {
                f.tag!!
            } else {
                f.javaClass.simpleName
            }
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "$formattedTime:$currentLifeCycleState\n"
            )
            stringBuilderFragmentLifeCycleObserver.append(
                Constants.fragmentTag + ":" + className + " " + "Total fragment time:" + timeFormatter.timeString(
                    totalTimeSpentInFragment!!
                ) + "\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.fragmentLifeCycleObserverTag
            )
        }
    }

    /**
     * This Method Is Used For Printing Observer Outcome.
     * @return String value.
     */
    internal fun returnFragmentLifeCycleState(): String {
        return stringBuilderFragmentLifeCycleObserver.toString()
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderFragmentLifeCycleObserver.
     */
    internal fun refreshLifeCycleObserverState() {
        stringBuilderFragmentLifeCycleObserver = StringBuilder()
    }

    /**
     * This Method Is Used For Getting Fragment List.
     * @return ArrayList<String>.
     */
    internal fun returnClassList(): ArrayList<String> {
        return classList
    }
}
