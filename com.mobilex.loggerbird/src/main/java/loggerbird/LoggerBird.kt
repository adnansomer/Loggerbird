package loggerbird

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.HttpAuthHandler
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.util.rangeTo
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.google.gson.GsonBuilder
import constants.Constants
import constants.Constants.Companion.cpuInfoErrorTag
import constants.Constants.Companion.cpuInfoTag
import constants.Constants.Companion.deviceInfoTag
import constants.Constants.Companion.devicePerformanceTag
import deneme.example.loggerbird.R
import exception.LoggerBirdException
import interceptors.*
import io.realm.Realm
import io.realm.RealmModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import listeners.LogRecyclerViewChildAttachStateChangeListener
import listeners.LogRecyclerViewItemTouchListener
import listeners.LogRecyclerViewScrollListener
import observers.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import services.LoggerBirdMemoryService
import services.LoggerBirdService
import utils.EmailUtil
import java.io.File
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

//LoggerBird class is the general logging class for this library.
class LoggerBird : LifecycleObserver {
    companion object {
        //Static global variables.
        var controlLogInit: Boolean = false
        private var stringBuilderComponent: StringBuilder = StringBuilder()
        private var stringBuilderLifeCycle: StringBuilder = StringBuilder()
        private var stringBuilderFragmentManager: StringBuilder = StringBuilder()
        private var stringBuilderAnalyticsManager: StringBuilder = StringBuilder()
        private var stringBuilderHttp: StringBuilder = StringBuilder()
        private var stringBuilderOkHttp : StringBuilder = StringBuilder()
        private var stringBuilderInAPurchase: StringBuilder = StringBuilder()
        private var stringBuilderSkuDetailList: StringBuilder = StringBuilder()
        private var stringBuilderRetrofit: StringBuilder = StringBuilder()
        private var stringBuilderQuery: StringBuilder = StringBuilder()
        private var stringBuilderRealm: StringBuilder = StringBuilder()
        private var stringBuilderBuild: StringBuilder = StringBuilder()
        private var stringBuilderException: StringBuilder = StringBuilder()
        private var stringBuilderAll: StringBuilder = StringBuilder()
        private var stringBuilderPerformanceDetails: StringBuilder = StringBuilder()
        private var stringBuilderDeviceInfoDetails: StringBuilder = StringBuilder()
        private var stringBuilderMemoryUsageDetails: StringBuilder = StringBuilder()
        private var coroutineCallRetrofit = CoroutineScope(Dispatchers.IO)
        private var coroutineCallAnalytic = CoroutineScope(Dispatchers.IO)
        private var coroutineCallHttp = CoroutineScope(Dispatchers.IO)
        private var coroutineCallInAPurchase = CoroutineScope(Dispatchers.IO)
        private var coroutineCallComponent = CoroutineScope(Dispatchers.IO)
        private var coroutineCallEmailTask = CoroutineScope(Dispatchers.IO)
        private var coroutineCallEmail = CoroutineScope(Dispatchers.IO)
        private var coroutineCallActivity = CoroutineScope(Dispatchers.IO)
        private var coroutineCallFragment = CoroutineScope(Dispatchers.IO)
        private var coroutineCallDevicePerformance = CoroutineScope(Dispatchers.IO)
        private var coroutineCallRealm = CoroutineScope(Dispatchers.IO)
        private var coroutineCallException = CoroutineScope(Dispatchers.IO)
        private var coroutineCallAll = CoroutineScope(Dispatchers.IO)
        private lateinit var defaultFileDirectory: File
        private lateinit var defaultFilePath: File
        private var formattedTime: String? = null
        private val lifeCycleObserver = LogLifeCycleObserver()
        private lateinit var fragmentLifeCycleObserver: LogFragmentLifeCycleObserver
        private lateinit var context: Context
        private var file: File? = null
        private var fileLimit: Long = 2097152
        private var memoryThreshold: Long = 1897152L
        private var arrayListFile: ArrayList<File> = ArrayList()
        private lateinit var fileTemp: File
        private lateinit var intentService: Intent
        private lateinit var intentServiceMemory: Intent
        private var recyclerViewAdapterDataObserver: LogRecyclerViewAdapterDataObserver = LogRecyclerViewAdapterDataObserver()
        private var recyclerViewScrollListener: LogRecyclerViewScrollListener = LogRecyclerViewScrollListener()
        private var recyclerViewChildAttachStateChangeListener: LogRecyclerViewChildAttachStateChangeListener = LogRecyclerViewChildAttachStateChangeListener()
        private var recyclerViewItemTouchListener: LogRecyclerViewItemTouchListener = LogRecyclerViewItemTouchListener()
        private lateinit var recyclerViewItemObserver: LogDataSetObserver


        //---------------Public Methods:---------------//

        /**
         * Call This Method Before Calling Any Other Methods.
         * Parameters:
         * @param context is for getting reference from the application context , you must deploy this parameter.
         * @param fragmentManager is used for getting details from FragmentManager which is used for tracking life cycle of Fragments rather than activity.
         * Variables:
         * controlLogInit is used for tracking the logInit return value which is used in other methods in this class.
         * @return Boolean value.
         */


        fun logInit(
                context: Context,
                file: File? = null,
                fragmentManager: FragmentManager? = null
            ): Boolean {
                intentService = Intent(context, LoggerBirdService::class.java)
                context.startService(intentService)

                intentServiceMemory = Intent(context, LoggerBirdMemoryService::class.java)
                context.startService(intentServiceMemory)

                this.context = context
                this.file = file

                Companion.context = context
                controlLogInit = logAttach(context, fragmentManager)
                val logcatObserver: LogcatObserver = LogcatObserver()
                Thread.setDefaultUncaughtExceptionHandler(logcatObserver)

                return controlLogInit
            }


            /**
             * This Method Used For Checking LogInit State.
             */
            fun isLogInitAttached(): Boolean {
                return controlLogInit
            }

            /**
             * This Method Used For Refreshing LogInit State.
             */
            fun refreshLogInitInstance() {
                controlLogInit = false
            }

            /**
             * This Method Detaches A LifeCycle Observer From The Current Activity.
             */
            fun logDetachObserver() {
                lifeCycleObserver.deRegisterLifeCycle()
            }

            /**
             * This Method Detaches A Fragment Observer From The Current Fragment.
             */
            fun logDetachFragmentObserver(fragmentManager: FragmentManager) {
                if (Companion::fragmentLifeCycleObserver.isInitialized) {
                    fragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifeCycleObserver)
                }
            }

            /**
             * This Method Detaches library from Your Application.
             */
            fun logDetach() {
                if (controlLogInit) {
                    controlLogInit = false
                }
            }

            /**
             * This Method Re-Creates Instantiation For StringBuilders.
             */
            fun logRefreshInstance() {
                stringBuilderComponent = StringBuilder()
                stringBuilderLifeCycle = StringBuilder()
                stringBuilderFragmentManager = StringBuilder()
                stringBuilderAnalyticsManager = StringBuilder()
                stringBuilderMemoryUsageDetails = StringBuilder()
                stringBuilderDeviceInfoDetails = StringBuilder()
                stringBuilderPerformanceDetails = StringBuilder()
                stringBuilderHttp = StringBuilder()
                stringBuilderInAPurchase = StringBuilder()
                stringBuilderSkuDetailList = StringBuilder()
                stringBuilderRetrofit = StringBuilder()
                stringBuilderQuery = StringBuilder()
                stringBuilderRealm = StringBuilder()
                stringBuilderException = StringBuilder()
                stringBuilderAll = StringBuilder()
            }


            /**
             * This Method Attaches A LifeCycle Observer For The Current Activity.
             * Parameters:
             * @param context is for getting reference from the application context , you must deploy this parameter.
             * @param fragmentManager is used for getting details from FragmentManager which is used for  tracking life cycle of Fragments rather than activity.
             * @return Boolean value.
             */
            fun logAttach(context: Context, fragmentManager: FragmentManager? = null): Boolean {
                if (fragmentManager != null) {
                    fragmentLifeCycleObserver =
                        LogFragmentLifeCycleObserver(
                            fragmentManager = fragmentManager
                        )
                    fragmentManager.registerFragmentLifecycleCallbacks(
                        fragmentLifeCycleObserver,
                        true
                    )

                } else {
                    lifeCycleObserver.registerLifeCycle(context)
                }
                recyclerViewItemObserver = LogDataSetObserver(context)
                return true
            }

            /**
             * This Method Saves Component Details To Txt File.
             * Parameters:
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallComponent is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var file allow user modify the file they want to create for saving their Component Details , otherwise it will save your file to the devices data->data->your project package name->files->component_details with an default name of "logger_bird_details"
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
             * @var stringBuilderComponent prints component details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             * @throws exception if log instance is empty.
             */
            private fun saveComponentDetails() {
                if (controlLogInit) {
                    if (stringBuilderComponent.isNotEmpty()) {
                        coroutineCallComponent.async {
                            try {
                                if (file != null) {
                                    if (!file!!.exists()) {
                                        withContext(Dispatchers.IO) {
                                            file!!.createNewFile()
                                            file!!.appendText(takeDeviceInformationDetails())
                                        }
                                    }
                                    file!!.appendText(
                                        stringBuilderComponent.toString()
                                    )
                                } else {
                                    defaultFileDirectory = context.filesDir
                                    defaultFilePath = File(
                                        defaultFileDirectory, "logger_bird_details.txt"
                                    )
                                    if (!defaultFilePath.exists()) {
                                        withContext(Dispatchers.IO) {
                                            defaultFilePath.createNewFile()
                                            defaultFilePath.appendText(
                                                takeDeviceInformationDetails()
                                            )
                                        }
                                    }
                                    defaultFilePath.appendText(
                                        stringBuilderComponent.toString()
                                    )
                                }
                                stringBuilderComponent = StringBuilder()
                                recyclerViewAdapterDataObserver.refreshRecyclerViewObserverState()
                                recyclerViewChildAttachStateChangeListener.refreshRecyclerViewObserverState()
                                recyclerViewItemTouchListener.refreshRecyclerViewObserverState()
                                recyclerViewScrollListener.refreshRecyclerViewObserverState()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                takeExceptionDetails(
                                    e,
                                    Constants.componentTag
                                )
                            }
                        }
                    } else {
                        throw LoggerBirdException(
                            Constants.saveErrorMessage + Constants.componentMethodTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }

            }

            /**
             * This Method Saves Life-Cycle Details To Txt File.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var LoggerBirdService.onDestroyMessage used for getting onDestroy state for lifecycle via service.
             * @var coroutineCallActivity is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var file allow user modify the file they want to create for saving their Activity Details , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name of life_cycle_details.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
             * @var stringBuilderLifeCycle prints life-cycle details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             * @throws exception if log instance is empty.
             */
            private fun saveLifeCycleDetails() {
                if (controlLogInit) {
                    if (stringBuilderLifeCycle.isNotEmpty()) {
                        if (LoggerBirdService.onDestroyMessage != null) {
                            try {
                                if (file != null) {
                                    if (!file!!.exists()) {
                                            file!!.createNewFile()
                                            file!!.appendText(takeDeviceInformationDetails())

                                        if (!file!!.exists()) {
                                            file!!.createNewFile()
                                            file!!.appendText(takeDeviceInformationDetails())

                                        }
                                        file!!.appendText(stringBuilderLifeCycle.toString())
                                    } else {
                                        defaultFileDirectory = context.filesDir
                                        defaultFilePath = File(
                                            defaultFileDirectory, "logger_bird_details.txt"
                                        )
                                        if (!defaultFilePath.exists()) {

                                                defaultFilePath.createNewFile()
                                                defaultFilePath.appendText(
                                                    takeDeviceInformationDetails()
                                                )

                                            defaultFilePath.createNewFile()
                                            defaultFilePath.appendText(
                                                takeDeviceInformationDetails()
                                            )
                                        }
                                        defaultFilePath.appendText(
                                            stringBuilderLifeCycle.toString()
                                        )
                                    }
                                    stringBuilderLifeCycle = StringBuilder()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                takeExceptionDetails(
                                    e,
                                    Constants.lifeCycleTag
                                )
                            }
                        } else {
                            coroutineCallActivity.async {
                                try {
                                    if (file != null) {
                                        if (!file!!.exists()) {
                                            withContext(Dispatchers.IO) {
                                                file!!.createNewFile()
                                                file!!.appendText(takeDeviceInformationDetails())
                                            }
                                        }
                                        file!!.appendText(stringBuilderLifeCycle.toString())
                                    } else {
                                        defaultFileDirectory = context.filesDir
                                        defaultFilePath = File(
                                            defaultFileDirectory, "logger_bird_details.txt"
                                        )
                                        if (!defaultFilePath.exists()) {
                                            withContext(Dispatchers.IO) {
                                                defaultFilePath.createNewFile()
                                                defaultFilePath.appendText(
                                                    takeDeviceInformationDetails()
                                                )
                                            }
                                        }
                                        defaultFilePath.appendText(
                                            stringBuilderLifeCycle.toString()
                                        )
                                    }
                                    stringBuilderLifeCycle = StringBuilder()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    takeExceptionDetails(
                                        e,
                                        Constants.lifeCycleTag
                                    )
                                }

                            }
                        }
                    } else {
                        throw LoggerBirdException(
                            Constants.saveErrorMessage + Constants.lifeCycleMethodTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }


            /**
             * This Method Saves DevicePerformance Details To Txt File.
             * Parameters:
             * @param file allow user modify the file they want to create for saving their Device Performance Details , otherwise it will save your file to the devices data->data->your project package name->files->device_performance_details with an default name device_performance_details.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallDevicePerformance is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("device_performance_details")
             * Exceptions:
             * @throws exception if error occurs then deneme.example.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             * @throws exception if log instance is empty.
             */
            fun saveDevicePerformanceDetails(
                file: File? = null
            ) {
                if (controlLogInit) {
                    if (stringBuilderPerformanceDetails.isNotEmpty()) {
                        coroutineCallDevicePerformance.async {
                            try {
                                if (file != null) {
                                    if (!file.exists()) {
                                        withContext(Dispatchers.IO) {
                                            file.createNewFile()
                                            file.appendText(takeDeviceInformationDetails())
                                        }
                                    }
                                    file.appendText(
                                        stringBuilderPerformanceDetails.toString()
                                    )
                                } else {
                                    defaultFileDirectory = context.filesDir
                                    defaultFilePath = File(
                                        defaultFileDirectory, "device_performance_details.txt"
                                    )
                                    if (!defaultFilePath.exists()) {
                                        withContext(Dispatchers.IO) {
                                            defaultFilePath.createNewFile()
                                            defaultFilePath.appendText(
                                                takeDeviceInformationDetails()
                                            )
                                        }
                                    }
                                    defaultFilePath.appendText(
                                        stringBuilderPerformanceDetails.toString()
                                    )
                                }
                                stringBuilderPerformanceDetails = StringBuilder()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                takeExceptionDetails(
                                    e,
                                    Constants.devicePerformanceTag
                                )
                                saveExceptionDetails()
                            }
                        }
                    } else {
                        throw LoggerBirdException(
                            Constants.saveErrorMessage + Constants.devicePerformanceTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }

            }

            /**
             * This Method Saves FragmentManager Details To Txt File.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallFragment is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var file allow user modify the file they want to create for saving their FragmentManager Details , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name logger_bird_details.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details")
             * @var stringBuilderFragmentManager prints fragment manager details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             * @throws exception if log instance is empty.
             */
            private fun saveFragmentManagerDetails() {
                if (controlLogInit) {
                    if (stringBuilderFragmentManager.isNotEmpty()) {
                        coroutineCallFragment.async {
                            try {
                                if (file != null) {
                                    if (!file!!.exists()) {
                                        withContext(Dispatchers.IO) {
                                            file!!.createNewFile()
                                            file!!.appendText(takeDeviceInformationDetails())
                                        }
                                    }
                                    file!!.appendText(
                                        stringBuilderFragmentManager.toString()
                                    )
                                } else {
                                    defaultFileDirectory = context.filesDir
                                    defaultFilePath = File(
                                        defaultFileDirectory, "logger_bird_details.txt"
                                    )
                                    if (!defaultFilePath.exists()) {
                                        withContext(Dispatchers.IO) {
                                            defaultFilePath.createNewFile()
                                            defaultFilePath.appendText(
                                                takeDeviceInformationDetails()
                                            )
                                        }
                                    }
                                    defaultFilePath.appendText(
                                        stringBuilderFragmentManager.toString()
                                    )
                                }
                                stringBuilderFragmentManager = StringBuilder()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                takeExceptionDetails(
                                    e,
                                    Constants.fragmentManagerTag
                                )
                            }
                        }
                    } else {
                        throw LoggerBirdException(
                            Constants.saveErrorMessage + Constants.fragmentManagerMethodTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }

            }

            /**
             * This Method Saves Analytics Details To Txt File.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallAnalytic is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var file allow user modify the file they want to create for saving their Analytics Details , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name of logger_bird_details.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
             * @var stringBuilderAnalyticsManager prints analytics details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             * @throws exception if log instance is empty.
             */
            private fun saveAnalyticsDetails() {
                if (controlLogInit) {
                    if (stringBuilderAnalyticsManager.isNotEmpty()) {
                        coroutineCallAnalytic.async {
                            try {
                                if (file != null) {
                                    if (!file!!.exists()) {
                                        withContext(Dispatchers.IO) {
                                            file!!.createNewFile()
                                            file!!.appendText(takeDeviceInformationDetails())
                                        }
                                    }
                                    file!!.appendText(
                                        stringBuilderAnalyticsManager.toString()
                                    )
                                } else {
                                    defaultFileDirectory = context.filesDir
                                    defaultFilePath = File(
                                        defaultFileDirectory, "logger_bird.txt"
                                    )
                                    if (!defaultFilePath.exists()) {
                                        withContext(Dispatchers.IO) {
                                            defaultFilePath.createNewFile()
                                            defaultFilePath.appendText(
                                                takeDeviceInformationDetails()
                                            )
                                        }
                                    }
                                    defaultFilePath.appendText(
                                        stringBuilderAnalyticsManager.toString()
                                    )
                                }
                                stringBuilderAnalyticsManager = StringBuilder()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                takeExceptionDetails(
                                    e,
                                    Constants.analyticsTag
                                )
                            }
                        }
                    } else {
                        throw LoggerBirdException(
                            Constants.saveErrorMessage + Constants.analyticsMethodTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }

            /**
             * This Method Saves HttpRequest Details To Txt File.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallHttp is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var file allow user modify the file they want to create for saving their HttpRequest Details , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name of logger_bird_details.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
             * @var stringBuilderHttp prints http details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             * @throws exception if log instance is empty.
             */
            private fun saveHttpRequestDetails() {
                if (controlLogInit) {
                    if (stringBuilderHttp.isNotEmpty()) {
                        coroutineCallHttp.async {
                            try {
                                if (file != null) {
                                    if (!file!!.exists()) {
                                        withContext(Dispatchers.IO) {
                                            file!!.createNewFile()
                                            file!!.appendText(takeDeviceInformationDetails())
                                        }
                                    }
                                    file!!.appendText(
                                        stringBuilderHttp.toString()
                                    )
                                } else {
                                    defaultFileDirectory = context.filesDir
                                    defaultFilePath = File(
                                        defaultFileDirectory, "logger_bird_details.txt"
                                    )
                                    if (!defaultFilePath.exists()) {
                                        withContext(Dispatchers.IO) {
                                            defaultFilePath.createNewFile()
                                            defaultFilePath.appendText(
                                                takeDeviceInformationDetails()
                                            )
                                        }
                                    }
                                    defaultFilePath.appendText(
                                        stringBuilderHttp.toString()
                                    )
                                }
                                stringBuilderHttp = StringBuilder()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                takeExceptionDetails(
                                    e,
                                    Constants.httpTag
                                )
                            }
                        }
                    } else {
                        throw LoggerBirdException(
                            Constants.saveErrorMessage + Constants.htppRequestMethodTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }

            /**
             * This Method Saves Android In A Purchase Details To Txt File.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallInAPurchase is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var file allow user modify the file  they want to create for saving their InAPurchase Details , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name of logger_bird_details.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
             * @var stringBuilderInAPurchase prints android In A Purchase details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             * @throws exception if log instance is empty.
             */
            private fun saveInAPurchaseDetails() {
                if (controlLogInit) {
                    if (stringBuilderInAPurchase.isNotEmpty()) {
                        coroutineCallInAPurchase.async {
                            try {
                                if (file != null) {
                                    if (!file!!.exists()) {
                                        withContext(Dispatchers.IO) {
                                            file!!.createNewFile()
                                            file!!.appendText(takeDeviceInformationDetails())

                                        }
                                    }
                                    file!!.appendText(
                                        stringBuilderInAPurchase.toString()
                                    )
                                } else {
                                    defaultFileDirectory = context.filesDir
                                    defaultFilePath = File(
                                        defaultFileDirectory, "logger_bird_details.txt"
                                    )
                                    if (!defaultFilePath.exists()) {
                                        withContext(Dispatchers.IO) {
                                            defaultFilePath.createNewFile()
                                            defaultFilePath.appendText(
                                                takeDeviceInformationDetails()
                                            )
                                        }
                                    }
                                    defaultFilePath.appendText(
                                        stringBuilderInAPurchase.toString()
                                    )
                                }
                                stringBuilderInAPurchase = StringBuilder()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                takeExceptionDetails(
                                    e,
                                    Constants.inAPurchaseTag
                                )
                            }
                        }
                    } else {
                        throw LoggerBirdException(
                            Constants.saveErrorMessage + Constants.inAPurchaseMethodTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }

            /**
             * This Method Saves RetrofitRequest Details To Txt File.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallRetrofit is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var file allow user to modify the file they want to create for their RetrofitRequest Details , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name of logger_bird_details.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
             * @var stringBuilderRetrofit prints retrofit details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             * @throws exception if log instance is empty.
             */
            private fun saveRetrofitRequestDetails() {
                if (controlLogInit) {
                    if (stringBuilderRetrofit.isNotEmpty()) {
                        coroutineCallRetrofit.async {
                            try {
                                if (file != null) {
                                    if (!file!!.exists()) {
                                        withContext(Dispatchers.IO) {
                                            file!!.createNewFile()
                                            file!!.appendText(takeDeviceInformationDetails())

                                        }
                                    }
                                    file!!.appendText(
                                        stringBuilderRetrofit.toString()
                                    )
                                } else {
                                    defaultFileDirectory = context.filesDir
                                    defaultFilePath = File(
                                        defaultFileDirectory, "logger_bird_details.txt"
                                    )
                                    if (!defaultFilePath.exists()) {
                                        withContext(Dispatchers.IO) {
                                            defaultFilePath.createNewFile()
                                            defaultFilePath.appendText(
                                                takeDeviceInformationDetails()
                                            )
                                        }
                                    }
                                    defaultFilePath.appendText(
                                        stringBuilderRetrofit.toString()
                                    )
                                }
                                stringBuilderRetrofit = StringBuilder()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                takeExceptionDetails(
                                    e,
                                    Constants.retrofitTag
                                )
                            }
                        }
                    } else {
                        throw LoggerBirdException(
                            Constants.saveErrorMessage + Constants.retrofitMethodTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }

            /**
             * This Method Saves Realm Details To Txt File.
             * Parameters:
             * @param file allow user modify the file they want to create for saving their Realm Details , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name of logger_bird_details.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallRealm is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
             * @var stringBuilderRealm prints realm details.
             * Exception:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             * @throws exception if log instance is empty.
             */
            private fun saveRealmDetails() {
                if (controlLogInit) {
                    if (stringBuilderRealm.isNotEmpty()) {
                        coroutineCallRealm.async {
                            try {
                                if (file != null) {
                                    if (!file!!.exists()) {
                                        withContext(Dispatchers.IO) {

                                            file!!.createNewFile()
                                            file!!.appendText(takeDeviceInformationDetails())

                                        }
                                    }
                                    file!!.appendText(
                                        stringBuilderRealm.toString()
                                    )
                                } else {
                                    defaultFileDirectory = context.filesDir
                                    defaultFilePath = File(
                                        defaultFileDirectory, "logger_bird_details.txt"
                                    )
                                    if (!defaultFilePath.exists()) {
                                        withContext(Dispatchers.IO) {
                                            defaultFilePath.createNewFile()
                                            defaultFilePath.appendText(
                                                takeDeviceInformationDetails()
                                            )
                                        }
                                    }
                                    defaultFilePath.appendText(
                                        stringBuilderRealm.toString()
                                    )
                                }
                                stringBuilderRealm = StringBuilder()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                takeExceptionDetails(
                                    e,
                                    Constants.realmTag
                                )
                                saveExceptionDetails()
                            }
                        }
                    } else {
                        throw LoggerBirdException(
                            Constants.saveErrorMessage + Constants.realmMethodTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }

            /**
             * This Method Saves Exception Details To Txt File.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallAnalytic is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var file allow user to modify the file they want to create for saving their Exception Details , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name of logger_bird_details.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("exception_details").
             * @var stringBuilderException prints com.mobilex.loggerbird.exception details.
             * Exception:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            fun saveExceptionDetails() {
                if (stringBuilderException.isNotEmpty()) {
                    coroutineCallException.async {
                        try {
                            if (file != null) {
                                if (!file!!.exists()) {
                                    withContext(Dispatchers.IO) {

                                        file!!.createNewFile()
                                        file!!.appendText(takeDeviceInformationDetails())

                                    }
                                }
                                file!!.appendText(
                                    stringBuilderException.toString()
                                )
                            } else {
                                defaultFileDirectory = context.filesDir
                                defaultFilePath = File(
                                    defaultFileDirectory, "logger_bird_details.txt"
                                )
                                if (!defaultFilePath.exists()) {
                                    withContext(Dispatchers.IO) {
                                        defaultFilePath.createNewFile()
                                        defaultFilePath.appendText(
                                            takeDeviceInformationDetails()
                                        )
                                    }
                                }
                                defaultFilePath.appendText(
                                    stringBuilderException.toString()
                                )
                            }
                            stringBuilderException = StringBuilder()
                            exitProcess(0)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    throw LoggerBirdException(
                        Constants.saveErrorMessage + Constants.exceptionMethodTag
                    )
                }
            }

            //This method isnt updated needs to be modified or deleted.
            /**
             * This Method Saves All Details To Txt File.
             * Parameters:
             * @param file allow user to modify the file they want to create for saving their All Details , otherwise it will save your file to the devices data->data->your project package name->files->all_details with an default name of all_details.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
             * @var coroutineCallAll is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
             * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
             * @var defaultFilePath is used for getting defaultFileDirectory and default file name("all_details").
             * @var stringBuilderLifeCycle prints life-cycle details.
             * @var stringBuilderFragmentManager prints fragment manager details.
             * @var stringBuilderAnalyticsManager prints analytics details.
             * @var stringBuilderHttp prints http request details.
             * @var stringBuilderInAPurchase prints android in a purchase details.
             * @var stringBuilderRetrofit prints retrofit details.
             * @var stringBuilderRealm prints realm details.
             * @var stringBuilderException prints com.mobilex.exception details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            fun saveAllDetails(
                file: File? = null
            ) {
                if (controlLogInit) {
                    coroutineCallAll.async {
                        try {
                            if (file != null) {
                                if (!file.exists()) {
                                    withContext(Dispatchers.IO) {
                                        file.createNewFile()
                                        file.appendText(takeDeviceInformationDetails())
                                    }
                                }
                                file.appendText(
                                    stringBuilderComponent.toString()
                                            + "\n" + stringBuilderLifeCycle.toString()
                                            + "\n" + stringBuilderFragmentManager.toString()
                                            + "\n" + stringBuilderAnalyticsManager.toString()
                                            + "\n" + stringBuilderHttp.toString()
                                            + "\n" + stringBuilderInAPurchase.toString()
                                            + "\n" + stringBuilderRetrofit.toString()
                                            + "\n" + stringBuilderRealm.toString()
                                            + "\n" + stringBuilderException.toString()
                                )
                            } else {
                                defaultFileDirectory = context.filesDir
                                defaultFilePath = File(
                                    defaultFileDirectory, "all_details.txt"
                                )
                                if (!defaultFilePath.exists()) {
                                    withContext(Dispatchers.IO) {
                                        defaultFilePath.createNewFile()
                                        defaultFilePath.appendText(
                                            takeDeviceInformationDetails()
                                        )
                                    }
                                }
                                defaultFilePath.appendText(
                                    stringBuilderComponent.toString()
                                            + "\n" + stringBuilderLifeCycle.toString()
                                            + "\n" + stringBuilderFragmentManager.toString()
                                            + "\n" + stringBuilderAnalyticsManager.toString()
                                            + "\n" + stringBuilderHttp.toString()
                                            + "\n" + stringBuilderInAPurchase.toString()
                                            + "\n" + stringBuilderRetrofit.toString()
                                            + "\n" + stringBuilderRealm.toString()
                                            + "\n" + stringBuilderException.toString()
                                )
                            }
                            stringBuilderAll = StringBuilder()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            takeExceptionDetails(
                                e,
                                Constants.allTag
                            )
                        }
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }

            /**
             * This Method Takes Component Details.
             * Parameters:
             * @param view parameter used for getting id of the component.
             * @param resources parameter used for getting component name with view parameter.
             * Variables:
             * @var current time used for getting local time of your devices.
             * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second)
             * @var stringBuilderComponent used for printing details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            fun takeComponentDetails(
                view: View? = null,
                resources: Resources? = null
            ) {
                if (controlLogInit) {
                    try {
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)
                        if (view is RecyclerView) {
                            takeRecyclerViewDetails(recyclerView = view, resources = resources)
                        } else {
                            stringBuilderComponent.append(
                                formattedTime + ":" + Constants.componentTag + "\n" + "Component Name:" + (resources?.getResourceName(
                                    view!!.id
                                )) + " " + "Component Id:" + view?.id + "\n" + "Component Type:" + view!!.findViewById<View>(
                                    view.id
                                ).toString() + "\n"
                            )
                        }
                        saveComponentDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        takeExceptionDetails(e, Constants.componentTag)
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }

            //In progress method.
            fun registerRecyclerViewObservers(recyclerView: RecyclerView) {
                recyclerView.adapter?.registerAdapterDataObserver(recyclerViewAdapterDataObserver)
                recyclerView.addOnScrollListener(recyclerViewScrollListener)
                recyclerView.addOnChildAttachStateChangeListener(
                    recyclerViewChildAttachStateChangeListener
                )
                recyclerView.addOnItemTouchListener(recyclerViewItemTouchListener)
            }

            //In progress method.
            fun unRegisterRecyclerViewObservers(recyclerView: RecyclerView) {
                recyclerView.adapter?.unregisterAdapterDataObserver(recyclerViewAdapterDataObserver)
                recyclerView.removeOnScrollListener(recyclerViewScrollListener)
                recyclerView.removeOnChildAttachStateChangeListener(
                    recyclerViewChildAttachStateChangeListener
                )
                recyclerView.removeOnItemTouchListener(recyclerViewItemTouchListener)
            }

            //In progress method.
            fun takeRecyclerViewDetails(recyclerView: RecyclerView, resources: Resources?) {
                try {
                    val stringBuilderRecyclerViewItem: StringBuilder = StringBuilder()
                    val recyclerViewList: ArrayList<Any> = ArrayList()
                    var tempView: View
                    var tempViewGroup: ViewGroup
                    var tempTextView: TextView
                    recyclerViewItemObserver.takeObserverList()
                    for (recyclerViewItem in 0..recyclerView.adapter!!.itemCount) {
                        if (recyclerView.getChildAt(recyclerViewItem) != null) {
                            tempView = recyclerView.getChildAt(recyclerViewItem)
                            tempViewGroup = tempView as ViewGroup
                            do {
                                if (tempView is ViewGroup) {
                                    tempViewGroup = tempView as ViewGroup
                                    if (tempViewGroup.getChildAt(0) != null) {
                                        tempView = tempViewGroup.getChildAt(0)
                                    } else if (tempViewGroup.getChildAt(recyclerViewItem) != null) {
                                        tempView = tempViewGroup.getChildAt(recyclerViewItem)
                                    }
                                } else {
                                    if (tempView is TextView) {
                                        tempTextView = tempView
                                        recyclerViewList.add(tempTextView.text)
                                        break
                                    }
                                }
                            } while (true)
                        }
                    }
                    stringBuilderRecyclerViewItem.append(recyclerViewList.toString() + "\n")
                    stringBuilderComponent.append(
                        "\n" + formattedTime + ":" + Constants.componentTag + "\n" + "Component Name:" + (resources?.getResourceName(
                            recyclerView.id
                        )) + " " + "Component Id:" + recyclerView.id + "\n" + "Component Type:" + recyclerView.findViewById<View>(
                                recyclerView.id
                            )
                            .toString() + "\n" + "RecyclerView Layout:" + recyclerView.layoutManager + "\n" + "RecyclerView Adapter:" + recyclerView.adapter + "\n" + "RecyclerView Item Size:" + recyclerView.adapter?.itemCount + "\n" + "RecyclerView Item list:" + "\n" + recyclerViewList.toString() + "\n"
                    )
                    stringBuilderComponent.append(recyclerViewAdapterDataObserver.returnRecyclerViewState())
                    stringBuilderComponent.append(recyclerViewScrollListener.returnRecyclerViewState())
                    stringBuilderComponent.append(recyclerViewChildAttachStateChangeListener.returnRecyclerViewState())
                } catch (e: Exception) {
                    e.printStackTrace()
                    takeExceptionDetails(e, Constants.componentTag)
                }
            }

            /**
             * This Method Takes Life-Cycle Details.
             * printing LifeCycleObserver.returnActivityLifeCycleState outcomes.
             * Variables:
             * @var stringBuilderLifeCycle used for printing details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            fun takeLifeCycleDetails() {
                if (controlLogInit) {
                    try {
                        if (Companion::fragmentLifeCycleObserver.isInitialized) {
                            if (fragmentLifeCycleObserver.returnFragmentLifeCycleState().isNotEmpty()
                            ) {
                                for (classList in fragmentLifeCycleObserver.returnClassList()) {
                                    stringBuilderLifeCycle.append(classList + ":" + "\n")
                                    for (stateList in fragmentLifeCycleObserver.returnFragmentLifeCycleState()
                                        .split(
                                            "\n"
                                        )) {
                                        if (stateList.contains(classList)) {
                                            stringBuilderLifeCycle.append(stateList + "\n")
                                        }
                                    }
                                }
                            }
                        } else if (lifeCycleObserver.returnActivityLifeCycleState().isNotEmpty()) {
                            for (classList in lifeCycleObserver.returnClassList()) {
                                stringBuilderLifeCycle.append(classList + ":" + "\n")
                                for (stateList in lifeCycleObserver.returnActivityLifeCycleState()
                                    .split(
                                        "\n"
                                    )) {
                                    if (stateList.contains(classList)) {
                                        stringBuilderLifeCycle.append(stateList + "\n")
                                    }
                                }
                            }
                        }
                        if (LoggerBirdService.onDestroyMessage != null) {
                            stringBuilderLifeCycle.append(LoggerBirdService.onDestroyMessage)
                        }
                        saveLifeCycleDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        takeExceptionDetails(
                            e,
                            Constants.lifeCycleTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }


            /**
             * This Method Takes CPU and Processor Details of Android Device
             * Variables:
             * @var stringBuffer is used for getting cpu information from /proc/cpuinfo
             * @var file is used for get cpu info to the file
             * Excepitons:
             * @throws exception if logInit method return value is false.
             */
            fun takeDeviceCpuDetails(): String {

                val stringBuffer = StringBuffer()

                if (controlLogInit) {
                    try {
                        stringBuffer.append("abi: ").append(Build.CPU_ABI).append("\n")

                        if (File("/proc/cpuinfo").exists()) {
                            try {
                                val file = File("/proc/cpuinfo")
                                file.bufferedReader().forEachLine { stringBuffer.append(it + "\n") }

                            } catch (e: java.lang.Exception) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(cpuInfoErrorTag, "Cpu Information directiory not found")
                        }

                        Log.d(cpuInfoTag, stringBuffer.toString())
                        return stringBuffer.toString()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        saveExceptionDetails()
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
                return stringBuffer.toString()
            }


            /**
             * This Method Determines Whether Memory is Overused
             * Parameters:
             * @param memoryThreshold takes threshold value to determine whether memory is overused.
             * Variables:
             * @var runtimeTotalMemory returns total amount of memory in runtime.
             * @var runtimeFreeMemory returns free amount of memory in runtime.
             * @var usedMemorySize returns extraction freeMemory from totalMemory that gives exact usage of application in memory
             * @var memoryOverused returns true if memory usage exceeds the threshold value.
             * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
             * Exceptions:
             * @throws exception if error occurs then deneme.example.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            fun takeMemoryUsageDetails(threshold: Long?): String {

                if (controlLogInit) {

                    try {
                        stringBuilderMemoryUsageDetails = StringBuilder()

                        val runtime: Runtime = Runtime.getRuntime()
                        val runtimeTotalMemory = runtime.totalMemory()
                        val runtimeFreeMemory = runtime.freeMemory()
                        val usedMemorySize = (runtimeTotalMemory - runtimeFreeMemory)
                        val memoryOverused: Boolean

                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)

                        if (threshold != null) {
                            memoryThreshold = threshold
                        }

                        if (usedMemorySize > memoryThreshold) {
                            memoryOverused = true
                            stringBuilderMemoryUsageDetails.append("\n\nMemory Overused: $memoryOverused\nMemory Usage: $usedMemorySize Bytes")
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        saveExceptionDetails()
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
                return stringBuilderMemoryUsageDetails.toString()
            }

            /**
             * This Method Takes Device Performance Details
             * Variables:
             * @var availableMemory returns available memory on device.
             * @var totalMemory returns total memory on device.
             * @var lowMemory returns true if system considers memory is low.
             * @var runtimeMaxMemory returns maximum amount of memory that application uses in runtime.
             * @var runtimeTotalMemory returns total amount of memory in runtime.
             * @var runtimeFreeMemory returns free amount of memory in runtime.
             * @var availableProcessors returns number of available processors.
             * @var usedMemorySize returns extraction freeMemory from totalMemory.
             * @var cpuAbi returns cpu abi.
             * @var sendNetworkUsage returns number of bytes that ransmitted across mobile networks.
             * @var receivedNetworkUsage returns number of bytes that received across mobile networks.
             * @var battery status gets information of battery percentage with using receiver.
             * @var battery level is used for getting battery level information.
             * @var battery scale is used for getting battery level information.
             * @var battery gives the battery percentage of device.
             * @return device information with a string builder.
             * Exceptions:
             * @throws exception if error occurs then deneme.example.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            fun takeDevicePerformanceDetails() {
                if (controlLogInit) {

                    try {
                        val memoryInfo = ActivityManager.MemoryInfo()
                        val activityManager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        activityManager.getMemoryInfo(memoryInfo)
                        val runtime: Runtime = Runtime.getRuntime()

                        val availableMemory = memoryInfo.availMem / 1048576L
                        val totalMemory = memoryInfo.totalMem / 1048576L
                        val lowMemory = memoryInfo.lowMemory
                        val runtimeMaxMemory = runtime.maxMemory() / 1048576L
                        val runtimeTotalMemory = runtime.totalMemory() / 1048576L
                        val runtimeFreeMemory = runtime.freeMemory() / 1048576L
                        val availableProcessors = runtime.availableProcessors()
                        val usedMemorySize = (runtimeTotalMemory - runtimeFreeMemory)
                        val cpuAbi = Build.CPU_ABI.toString()
                        val sendNetworkUsage = android.net.TrafficStats.getMobileTxBytes()
                        val receivedNetworkUsage = android.net.TrafficStats.getMobileRxBytes()

                        val batteryStatus = context.registerReceiver(
                            null,
                            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                        )
                        var batteryLevel = -1
                        var batteryScale = 1
                        if (batteryStatus != null) {
                            batteryLevel =
                                batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, batteryLevel)
                            batteryScale =
                                batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, batteryScale)
                        }
                        val battery = batteryLevel / batteryScale.toFloat() * 100

                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)

                        stringBuilderPerformanceDetails.append(
                            "\nFormatted Time : $formattedTime\nAvailable Memory: $availableMemory MB\nTotal Memory: $totalMemory MB\nRuntime Max Memory: $runtimeMaxMemory MB\n" +
                                    "Runtime Total Memory: $runtimeTotalMemory MB\nRuntime Free Memmory: $runtimeFreeMemory MB\nLow Memory: $lowMemory\nAvilable Processors: $availableProcessors\n"
                                    + "Used Memory Size: $usedMemorySize MB \nCPU ABI: $cpuAbi\nNetwork Usage(Send): $sendNetworkUsage Bytes\nNetwork Usage(Received): $receivedNetworkUsage Bytes\n"
                                    + "Battery: $battery\n "
                        )

                        Log.d(devicePerformanceTag, stringBuilderPerformanceDetails.toString())

                    } catch (e: Exception) {
                        e.printStackTrace()
                        saveExceptionDetails()
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }

            /**
             * This Method Takes Device Information Details
             * Variables:
             * @var deviceId is used for obtain device id of user.
             * @var deviceSerial is used for getting device serial.
             * @var device is used for getting device info.
             * @var deviceModel is used for getting device model.
             * @var deviceType is used for getting device type.
             * @var deviceUser is used to get user of device.
             * @var sdkVersion is used for getting sdk version of device.
             * @var manufacturer is used for getting manufacturer of device.
             * @var host is used for getting host of device.
             * @var hardware is used for getting hardware details of device.
             * @var deviceBrand is used to get brand name of device.
             * @var product is used for getting product info.
             * @return device information with a string builder.
             * Exceptions:
             * @throws exception if error occurs then deneme.example.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            fun takeDeviceInformationDetails(): String {

                if (controlLogInit) {

                    try {
                        val deviceId = Build.ID
                        val deviceSerial = Build.FINGERPRINT
                        val device = Build.DEVICE
                        val deviceModel = Build.MODEL
                        val deviceType = Build.TYPE
                        val deviceUser = Build.USER
                        val sdkVersion = Build.VERSION.SDK_INT
                        val manufacturer = Build.MANUFACTURER
                        val host = Build.HOST
                        val hardware = Build.HARDWARE
                        val deviceBrand = Build.BRAND
                        val product = Build.PRODUCT

                        stringBuilderBuild = StringBuilder()
                        stringBuilderBuild.append(
                            "Device Information:" + "\n"
                                    + "ID:" + deviceId + "\n"
                                    + "SERIAL: " + deviceSerial + "\n"
                                    + "DEVICE:" + device + "\n"
                                    + "DEVICE MODEL:" + deviceModel + "\n"
                                    + "DEVICE TYPE:" + deviceType + "\n"
                                    + "USER:" + deviceUser + "\n"
                                    + "SDK VERSION:" + sdkVersion + "\n"
                                    + "MANUFACTURER:" + manufacturer + "\n"
                                    + "HOST:" + host + "\n"
                                    + "HARDWARE:" + hardware + "\n"
                                    + "BRAND:" + deviceBrand + "\n"
                                    + "PRODUCT:" + product + "\n"
                        )

                        Log.d(deviceInfoTag, stringBuilderBuild.toString())

                    } catch (e: Exception) {
                        e.printStackTrace()
                        takeExceptionDetails(e, Constants.deviceInfoTag)
                        saveExceptionDetails()
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }

                return stringBuilderBuild.toString()
            }

            /**
            * This method creates a HttpClient to Intercept Retrofit Logs
            * @var builder creates a new OkHttp Client to call in Retrofit Builder for applying interception
            * @return builder to use it as an OkHttpClient
            */
            fun LoggerBirdHttpClient() : OkHttpClient {

                if(controlLogInit){

                    val LoggerBirdHttpClient = OkHttpClient().newBuilder()
                        .addInterceptor(LogOkHttpInterceptor())
                        .addInterceptor(LogOkHttpAuthTokenInterceptor())
                        .addInterceptor(LogOkHttpErrorInterceptor())
                        .addInterceptor(LogOkHttpCacheInterceptor())

                    return LoggerBirdHttpClient.build()

                    } else {

                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }

             /**
              *
              *
              *
              */
            fun takeOkHttpDetails(okhttpClient: OkHttpClient? = null, okhttpRequest: Request? = null, okHttpURLConnection: HttpURLConnection?){

                if(controlLogInit){
                    try{
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateInstance()
                        formattedTime = formatter.format(date)

                        var okHttpClientInterceptors = okhttpClient?.interceptors
                        var okHttpClientNetworkInterceptors = okhttpClient?.networkInterceptors
                        var okHttpClientAuth = okhttpClient?.authenticator
                        var okHttpClientTimeOut = okhttpClient?.connectTimeoutMillis
                        var okHttpClientProtocols = okhttpClient?.protocols
                        var okHttpClientCache = okhttpClient?.cache

                        var okHttpIsRequest = okhttpRequest?.isHttps
                        var okHttpRequestBody = okhttpRequest?.body
                        var okHttpRequestHeaders = okhttpRequest?.headers
                        var okHttpRequestUrl = okhttpRequest?.url
                        var okHttpRequestMethod = okhttpRequest?.method

                        var okHttpConnectionMethod = okHttpURLConnection?.requestMethod
                        var okHttpConnectionResponseCode = okHttpURLConnection?.responseCode
                        var okHttpConnectionError = okHttpURLConnection?.errorStream
                        var okHttpConnectionResponse = okHttpURLConnection?.responseMessage

                        stringBuilderOkHttp.append("\n"
                            + formattedTime + " " + Constants.okHttpTag + "\n")


                    }catch (e : Exception){
                        e.printStackTrace()
                        takeExceptionDetails(e, Constants.okHttpTag)
                        }
                    } else{
                        throw LoggerBirdException(Constants.logInitErrorMessage)
                    }
                }

            /**
             * This Method Takes Analytics Details.
             * Parameters:
             * @param bundle parameter used for getting details from analytic bundle.
             * Variables:
             * @var current time used for getting local time of your devices.
             * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
             * @var stringBuilderAnalyticsManager used for printing the details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            fun takeAnalyticsDetails(bundle: Bundle? = null) {

                if (controlLogInit) {
                    try {
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)
                        stringBuilderAnalyticsManager.append("\n" + formattedTime + ":" + Constants.analyticsTag + "\n")
                        if (bundle != null) {
                            for (bundleItem in bundle.keySet()) {
                                stringBuilderAnalyticsManager.append(
                                    "$bundleItem:" + bundle.get(
                                        bundleItem
                                    ) + "\n"
                                )
                            }
                        }
                            saveAnalyticsDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        takeExceptionDetails(
                            e,
                            Constants.analyticsTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                    }
                }

            /**
             * This Method Takes FragmentManager Details.
             * Parameters:
             * @param fragmentManager parameter used for getting details from FragmentManager and printing all fragments in FragmentManager.
             * Variables:
             * @var stringBuilderFragmentManager used for printing the details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            fun takeFragmentManagerDetails(fragmentManager: FragmentManager? = null) {
                    if (controlLogInit) {
                        try {
                            stringBuilderFragmentManager.append("\n" + Constants.fragmentTag + " " + "list:")
                            if (fragmentManager != null) {
                                var fragmentCounter: Int = 1
                                for (fragmentList in fragmentManager.fragments) {
                                    stringBuilderFragmentManager.append("\n" + fragmentCounter + ")" + fragmentList.tag)
                                    fragmentCounter++
                                }
                            }
                            saveFragmentManagerDetails()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            takeExceptionDetails(
                                e,
                                Constants.fragmentManagerTag
                            )
                        }
                    } else {
                        throw LoggerBirdException(Constants.logInitErrorMessage)
                    }
                }





            /**
             * This Method Takes Android In A Purchase Details.
             * Parameters:
             * @param billingClient parameter used for getting status of BillingClient.
             * @param billingResult parameter used for getting the response code and message of Billing flow .
             * @param skuDetailsParams parameter used for getting the skusList and sku type of Billing flow.
             * @param billingFlowParams parameter used for getting the details of the sku's in the Billing flow.
             * @param acknowledgePurchaseParams parameter used for getting the details developer payload and purchase token.
             * Variables:
             * @var current time used for getting local time of your devices.
             * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
             * @var gson used for converting a json format to the String format.
             * @var prettyJson is the value of billingFlowParams sku's details from json format to the String format.
             * @var responseMessage is the outcome message according to the response code comes from billingResult.
             * @var stringBuilderSkuDetailList used for printing the details.
             * Exceptions:
             * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
             * @throws exception if logInit method return value is false.
             */
            fun takeInAPurchaseDetails(
                billingClient: BillingClient? = null,
                billingResult: BillingResult? = null,
                skuDetailsParams: SkuDetailsParams? = null,
                billingFlowParams: BillingFlowParams? = null,
                acknowledgePurchaseParams: AcknowledgePurchaseParams? = null
            ) {
                if (controlLogInit) {
                    try {
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson: String = gson.toJson(billingFlowParams?.skuDetails);
                        var responseMessage: String = ""
                            if (skuDetailsParams != null) {
                                if (skuDetailsParams.skusList != null) {
                                    var skuListCounter: Int = 0
                                    do {
                                        if (skuDetailsParams.skusList.size > skuListCounter) {
                                            stringBuilderSkuDetailList.append(
                                                "\n" + skuDetailsParams.skusList[skuListCounter]
                                            )
                                        } else {
                                            break
                                        }
                                        skuListCounter++
                                    } while (skuDetailsParams.skusList.iterator().hasNext())
                                }
                            }
                        if (billingResult != null) {
                            when (billingResult.responseCode) {
                                0 -> responseMessage = "Success"
                                1 -> responseMessage = "User pressed back or canceled a dialog"
                                2 -> responseMessage = "Network connection is down"
                                3 -> responseMessage = "The Google Play Billing AIDL version is not supported for the type requested"
                                4 -> responseMessage = "Requested product is not available for purchase"
                                5 -> responseMessage = "Invalid arguments provided to the API.This error can also indicate that the application was not correctly signed or properly \n set up for Google Play Billing , or does not have the neccessary permissions in the manifest"
                                6 -> responseMessage = "Fatal error during the API action"
                                7 -> responseMessage = "Failure to purchase since item is already owned"
                                8 -> responseMessage = "Failure to consume since item is not owned"

                                }
                            }
                            stringBuilderInAPurchase.append("\n" + formattedTime + ":" + Constants.inAPurchaseTag + "\n" + "Billing Flow Item Consumed:" + billingFlowParams?.skuDetails?.isRewarded + "\n" + "Billing Response Code:" + billingResult?.responseCode + "\n" + "Billing Response Message:" + responseMessage + "\n" + "Billing Client Is Ready:" + billingClient?.isReady + "\n" + "Sku Type:" + skuDetailsParams?.skuType + "\n" + "Sku List:" + stringBuilderSkuDetailList.toString() + "\n" + "Billing Flow Sku Details:" + prettyJson + "\n" + "Billing Flow Sku:" + billingFlowParams?.sku + "\n" + "Billing Flow Account Id:" + billingFlowParams?.accountId + "\n" + "Billing Flow Developer Id:" + billingFlowParams?.developerId + "\n" + "Billing flow Old Sku:" + billingFlowParams?.oldSku + "\n" + "Billing Flow Old Sku Purchase Token:" + billingFlowParams?.oldSkuPurchaseToken + "\n" + "Acknowledge Params:" + acknowledgePurchaseParams?.developerPayload + "\n" + "Acknowledge Purchase Token:" + acknowledgePurchaseParams?.purchaseToken)
                            saveInAPurchaseDetails()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            takeExceptionDetails(
                                e,
                                Constants.inAPurchaseTag
                            )
                        }
                    } else {
                        throw LoggerBirdException(Constants.logInitErrorMessage)
                    }
                }

                /**
                 * This Method Takes Retrofit Request Details.
                 * Parameters:
                 * @param retrofit parameter used for getting details from Retrofit which is used for getting base url of request.
                 * @param response parameter used for getting details from Response which is used for getting response code ,response message ,response success and response body.
                 * @param request parameter used for getting details from Request which is used for getting request query and request method.
                 * Variables:
                 * @var current time used for getting local time of your devices.
                 * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
                 * @var stringBuilderRetrofit used for printing the details.
                 * @var stringBuilderQuery used for printing the details of query that gathered from request.
                 * Exceptions:
                 * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
                 * @throws exception if logInit method return value is false.
                 */
                fun takeRetrofitRequestDetails(
                    retrofit: Retrofit? = null,
                    response: Response? = null,
                    request: Request? = null
                ) {
                    if (controlLogInit) {
                        try {
                            stringBuilderQuery = StringBuilder()
                            val date = Calendar.getInstance().time
                            val formatter = SimpleDateFormat.getDateTimeInstance()
                            formattedTime = formatter.format(date)

                            var parameterQueryCounter: Int = 0
                            if (request != null) {
                                while (request.url.querySize > parameterQueryCounter) {
                                    stringBuilderQuery.append(
                                        "Response Query Parameter:" + request.url.queryParameterName(
                                            parameterQueryCounter
                                        ) + "," + request.url.queryParameterValue(
                                            parameterQueryCounter
                                        ) + "\n"
                                    )
                                    parameterQueryCounter++
                                }
                            }
                            stringBuilderRetrofit.append("\n" + formattedTime + ":" + Constants.retrofitTag + "\n" + "Retrofit Request Code:" + response?.code + " " + "Response Message:" + response?.message + "\n" + "Retrofit Url:" + retrofit?.baseUrl() + " " + "Request Url:" + request?.url + "\n" + "Response Success:" + response?.isSuccessful + "\n" + "Request Method:" + request?.method + "\n" + stringBuilderQuery.toString() + "Response Value:" + response?.body?.string())
                            saveRetrofitRequestDetails()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            takeExceptionDetails(
                                e,
                                Constants.retrofitTag
                            )
                        }
                    } else {
                        throw LoggerBirdException(Constants.logInitErrorMessage)
                    }
                }
                //return asyncLogRetrofitTask(request = request,response = response,retrofit = retrofit).execute().get()

                /**
                 * This Method Takes Realm Details.
                 * Parameters:
                 * @param realm parameter used for getting details from Realm which is used for getting permissions,privileges and copy realm data.
                 * @param realm model parameter used for getting details from RealmModel which is used for giving realm data to the Realm method which is copyFromRealm().
                 * Variables:
                 * @var current time used for getting local time of your devices.
                 * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
                 * @var stringBuilderRealm used for printing the details.
                 * Exceptions:
                 * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
                 * @throws exception if logInit method return value is false.
                 */
                fun takeRealmDetails(realm: Realm? = null, realmModel: RealmModel? = null) {
                    if (controlLogInit) {
                        try {
                            if (controlLogInit) {
                                val date = Calendar.getInstance().time
                                val formatter = SimpleDateFormat.getDateTimeInstance()
                                formattedTime = formatter.format(date)
                                stringBuilderRealm.append(
                                    "\n" + formattedTime + ":" + Constants.realmTag + "Realm Details:" + "\n" + "Permissions:" + realm?.permissions + " " + "Privileges:" + realm?.privileges + "\n" + "Realm Model:" + realm?.copyFromRealm(
                                        realmModel
                                    )
                                )
                                saveRealmDetails()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            takeExceptionDetails(e, Constants.realmTag)
                        }
                    } else {
                        throw LoggerBirdException(Constants.logInitErrorMessage)
                    }
                }

                /**
                 * This Method Takes Exception Details.
                 * Parameters:
                 * @param exception parameter used for getting details from Exception which is used for getting deneme.example.loggerbird.exception messages.
                 * @param tag parameter used for getting details of which method caused this deneme.example.loggerbird.exception.
                 * Variables:
                 * @var current time used for getting local time of your devices.
                 * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
                 * @var stringBuilderException used for printing the details.
                 * Exceptions:
                 * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
                 * @throws exception if logInit method return value is false.
                 */
                fun takeExceptionDetails(
                    exception: Exception? = null,
                    tag: String? = null,
                    throwable: Throwable? = null
                ) {
                    try {
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)
                        if (exception != null) {
                            stringBuilderException.append(
                                "\n" + formattedTime + ":" + Constants.exceptionTag + "\n" + "Exception:" + Log.getStackTraceString(
                                    exception
                                ) + "Method Tag:" + tag
                            )
                        } else if (throwable != null) {
                            stringBuilderException.append(
                                "\n" + formattedTime + ":" + Constants.unHandledExceptionTag + "\n" + "Throwable:" + Log.getStackTraceString(
                                    throwable
                                ) + "Method Tag:" + tag
                            )
                        }
                        saveExceptionDetails()
                    } catch (e: Exception) {
                        takeExceptionDetails(exception = e, tag = Constants.exceptionTag)
                    }
                }

            /**
             * This Method Sends Desired File As Email.
             * If desired file is greater than 2mb size then it will create temp files and stores 2mb parts of the current file in these fill which will be send as chunks for sending email.
             * At the end of transaction these files will be deleted or if there are certain failure in transaction it will automatically deletes temp files created by previous failed transaction.
             * Parameters:
             * @param file parameter used for getting file details for sending as email.
             * @param context parameter used for getting context of the current activity or fragment.
             * @param progressBar parameter used for getting custom progressbar that provided by method caller , if progressbar is null there will be default progressbar with default layout and you need to provide rootview in order to not get deneme.example.loggerbird.exception from default progressbar.
             * @param rootView parameter used for getting the current view of activity or fragment.
             * Variables:
             * @var controlLogInit is used for getting logInit method return value.
                 * @var defaultProgressBar is used for providing default progressBar if user doesn't provides one.
                 * @var arraylistFile is used for holding temp files for sending Email.
                 * @var coroutineCallEmail is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
                 * @var file limit is a constant which is equals to 2mb in byte format.
                 * @var scanner is used for reading current desired file.
                 * @var fileTemp is used for creating temp files which is used for holding the parts of original file which exceeds 2mb size.
                 * @var withContext code block calls ui thread for using progressbar.
                 * Exceptions:
                 * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
                 */
                fun sendDetailsAsEmail(
                    file: File,
                    context: Context,
                    progressBar: ProgressBar? = null,
                    rootView: ViewGroup? = null
                ) {
                    try {
                        if (controlLogInit) {
                            var defaultProgressBar: ProgressBar = ProgressBar(context)
                            arrayListFile = ArrayList()
                            coroutineCallEmail.async {
                                withContext(Dispatchers.Main) {
                                    progressBar?.let {
                                        it.isShown
                                        it.visibility = View.VISIBLE
                                    }.run {
                                        val view: View = LayoutInflater.from(context)
                                            .inflate(R.layout.default_progressbar, rootView, true)
                                        defaultProgressBar = view.findViewById(R.id.progressBar)
                                        defaultProgressBar.isShown
                                    }


                                }
                                if (file.length() > fileLimit) {
                                    val scanner: Scanner = Scanner(file)
                                    var fileCounter: Int = 0
                                    do {
                                        fileTemp =
                                            File(context.filesDir, file.name + fileCounter + ".txt")
                                        fileCounter++
                                        if (fileTemp.exists()) {
                                            fileTemp.delete()
                                        }
                                    } while (fileTemp.exists())
                                    fileCounter = 0
                                    fileTemp =
                                        File(context.filesDir, file.name + fileCounter + ".txt")
                                    withContext(Dispatchers.IO) {
                                        fileTemp.createNewFile()
                                    }
                                    do {
                                        fileTemp.appendText(scanner.nextLine() + "\n")
                                        if (fileTemp.length() > fileLimit) {
                                            fileCounter++
                                            val fileEmail: File =
                                                fileTemp
                                            arrayListFile.add(fileEmail)
                                            fileTemp =
                                                File(
                                                    context.filesDir,
                                                    file.name + fileCounter + ".txt"
                                                )
                                            withContext(Dispatchers.IO) {
                                                fileTemp.createNewFile()
                                            }
                                        }
                                    } while (scanner.hasNext())
                                    arrayListFile.add(
                                        fileTemp
                                    )
                                    coroutineCallEmailTask.async {
                                        if (progressBar != null) {
                                            EmailUtil.sendEmail(
                                                arrayListFile = arrayListFile,
                                                context = context,
                                                progressBar = progressBar,
                                                coroutinecallEmail = coroutineCallEmailTask
                                            )
                                        } else {
                                            EmailUtil.sendEmail(
                                                arrayListFile = arrayListFile,
                                                context = context,
                                                progressBar = defaultProgressBar,
                                                coroutinecallEmail = coroutineCallEmailTask
                                            )
                                        }
                                    }
                                } else {
                                    coroutineCallEmailTask.async {
                                        if (progressBar != null) {
                                            EmailUtil.sendEmail(
                                                file = file,
                                                context = context,
                                                progressBar = progressBar,
                                                coroutinecallEmail = coroutineCallEmailTask
                                            )
                                        } else {
                                            EmailUtil.sendEmail(
                                                file = file,
                                                context = context,
                                                progressBar = defaultProgressBar,
                                                coroutinecallEmail = coroutineCallEmailTask
                                            )
                                        }
                                    }
                                }
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        takeExceptionDetails(e, Constants.emailTag)
                    }
                }


                //dummy method probably will be deleted.
                fun takeAllDetails(
                    view: View?,
                    resources: Resources?,
                    okHttpURLConnection: HttpURLConnection,
                    okhttpClient: OkHttpClient,
                    okhttpRequest: Request,
                    realm: Realm?,
                    realmModel: RealmModel?,
                    retrofit: Retrofit?,
                    response: Response?,
                    request: Request?,
                    billingClient: BillingClient?,
                    billingResult: BillingResult?,
                    skuDetailsParams: SkuDetailsParams?,
                    billingFlowParams: BillingFlowParams?,
                    acknowledgePurchaseParams: AcknowledgePurchaseParams?,
                    exception: Exception?
                ): String {
                    val date = Calendar.getInstance().time
                    val formatter = SimpleDateFormat.getDateTimeInstance()
                    formattedTime = formatter.format(date)
                    stringBuilderAll.append(
                        "$formattedTime:" + takeComponentDetails(
                            view,
                            resources
                        ) + "\n" + "$formattedTime:" + takeOkHttpDetails(okhttpClient,
                            okhttpRequest,
                            okHttpURLConnection
                        ) + "\n" + "$formattedTime:" + takeRealmDetails(
                            realm,
                            realmModel
                        ) + "\n" + "$formattedTime:" + takeRetrofitRequestDetails(
                            retrofit,
                            response,
                            request
                        ) + "\n" + "$formattedTime:" + takeInAPurchaseDetails(
                            billingClient,
                            billingResult,
                            skuDetailsParams,
                            billingFlowParams,
                            acknowledgePurchaseParams
                        ) + "\n" + "$formattedTime:" + takeExceptionDetails(
                            exception
                        )
                    )
                    return stringBuilderAll.toString()
                }
            }
        }