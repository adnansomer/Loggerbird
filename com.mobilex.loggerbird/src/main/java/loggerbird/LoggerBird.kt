package loggerbird

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleObserver
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.android.billingclient.api.*
import com.google.gson.GsonBuilder
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import utils.EmailUtil
import io.realm.Realm
import io.realm.RealmModel
import kotlinx.coroutines.*
import listeners.LogRecyclerViewChildAttachStateChangeListener
import listeners.LogRecyclerViewItemTouchListener
import listeners.LogRecyclerViewScrollListener
import observers.*
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import services.LoggerBirdService
import utils.LinkedBlockingQueueUtil
import utils.WorkerUtil
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
        private var controlLogInit: Boolean = false
        private lateinit var context: Context
        private var filePathName: String? = null
        private lateinit var fileDirectory: File
        private lateinit var filePath: File
        private var stringBuilderComponent: StringBuilder = StringBuilder()
        private var stringBuilderLifeCycle: StringBuilder = StringBuilder()
        private var stringBuilderFragmentManager: StringBuilder = StringBuilder()
        private var stringBuilderAnalyticsManager: StringBuilder = StringBuilder()
        private var stringBuilderHttp: StringBuilder = StringBuilder()
        private var stringBuilderInAPurchase: StringBuilder = StringBuilder()
        private var stringBuilderSkuDetailList: StringBuilder = StringBuilder()
        private var stringBuilderRetrofit: StringBuilder = StringBuilder()
        private var stringBuilderQuery: StringBuilder = StringBuilder()
        private var stringBuilderRealm: StringBuilder = StringBuilder()
        private var stringBuilderBuild: StringBuilder = StringBuilder()
        private var stringBuilderException: StringBuilder = StringBuilder()
        private var stringBuilderAll: StringBuilder = StringBuilder()
        private var stringBuilderExceedFileWriterLimit: StringBuilder = StringBuilder()
        private var coroutineCallComponent: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallLifeCycle: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallAnalytics: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallFragmentManager: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallHttpRequest: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallInAPurchase: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallRetrofit: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallRealm: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallException: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineRetrofitTask: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallEmailTask = CoroutineScope(Dispatchers.IO)
        private var formattedTime: String? = null
        private var fileLimit: Long = 2097152
        private lateinit var lifeCycleObserver: LogLifeCycleObserver
        private lateinit var fragmentLifeCycleObserver: LogFragmentLifeCycleObserver
        private var recyclerViewAdapterDataObserver: LogRecyclerViewAdapterDataObserver =
            LogRecyclerViewAdapterDataObserver()
        private var recyclerViewScrollListener: LogRecyclerViewScrollListener =
            LogRecyclerViewScrollListener()
        private var recyclerViewChildAttachStateChangeListener: LogRecyclerViewChildAttachStateChangeListener =
            LogRecyclerViewChildAttachStateChangeListener()
        private var recyclerViewItemTouchListener: LogRecyclerViewItemTouchListener =
            LogRecyclerViewItemTouchListener()
        private lateinit var workQueueLinked: LinkedBlockingQueueUtil
        private lateinit var recyclerViewItemObserver: LogDataSetObserver
        private lateinit var intentService: Intent
        private lateinit var textViewFileReader: TextView
        private lateinit var buttonFileReader: Button
        //private var corePoolSize: Int = 1000
        //private var maximumPoolSize: Int = 1000
        //private var keepAliveTime: Long = 100000
        //private var timeUnit: TimeUnit = TimeUnit.MILLISECONDS
        //private lateinit var threadPoolExecutor: ThreadPoolExecutor
        private var runnableList: ArrayList<Runnable> = ArrayList()
        internal var uncaughtExceptionHandlerController = false
        private lateinit var defaultProgressBar: ProgressBar
        private var defaultProgressBarView: View? = null


        //---------------Public Methods:---------------


        /**
         * Call This Method Before Calling Any Other Methods.
         * Parameters:
         * @param context is for getting reference from the application context , you must deploy this parameter.
         * @param filePathName allow user modify the file name they want to create for saving their details method , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name of "logger_bird_details"
         * @param fragmentManager is used for getting details from FragmentManager which is used for tracking life cycle of Fragments rather than activity.
         * Variables:
         * @var controlLogInit is used for tracking the logInit return value which is used in other methods in this class.
         * @var intentService start's service class for listening some event's in the application.
         * @var workQueueLinked is used for adding called saving method's into a queue.
         * @var logcatObserver is used for adding and initializing unhandled exception observer into application.
         * @return Boolean value.
         */
        fun logInit(
            context: Context,
            filePathName: String? = null,
            fragmentManager: FragmentManager? = null
        ): Boolean {
            if (!controlLogInit) {
                fileDirectory = context.filesDir
                if (filePathName != null) {
                    filePath = File(fileDirectory, "$filePathName.txt")
                    if (filePath.exists()) {
                        filePath.delete()
                    }
                } else {
                    filePath = File(fileDirectory, "logger_bird_details.txt")
                    if (filePath.exists()) {
                        filePath.delete()
                    }
                }
                intentService = Intent(context, LoggerBirdService::class.java)
                context.startService(intentService)
                workQueueLinked = LinkedBlockingQueueUtil(context = context)
                val logcatObserver = LogcatObserver()
                Thread.setDefaultUncaughtExceptionHandler(logcatObserver)
            }
            this.context = context
            this.filePathName = filePathName
            controlLogInit =
                logAttach(
                    context,
                    fragmentManager
                )
//            threadPoolExecutor= LogThreadPoolExecutorUtil(
//                corePoolSize = corePoolSize,
//                maximumPoolSize = maximumPoolSize,
//                keepAliveTime = keepAliveTime,
//                workQueue = workQueueLinked,
//                unit = timeUnit
//            )
            return controlLogInit
        }

        /**
         * This Method Used For Checking logInit State.
         */
        fun isLogInitAttached(): Boolean {
            return controlLogInit
        }

        /**
         * This Method Detaches logInit from Your Application.
         */
        fun logDetach() {
            controlLogInit = false
        }

        /**
         * This Method Detaches A LifeCycle Observer From The Current Activity.
         */
        fun logDetachObserver() {
            if (Companion::lifeCycleObserver.isInitialized) {
                lifeCycleObserver.deRegisterLifeCycle()
            }
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
         * This Method Re-Creates Instantiation For StringBuilders.
         */
        fun logRefreshInstance() {
            stringBuilderComponent = StringBuilder()
            stringBuilderLifeCycle = StringBuilder()
            stringBuilderFragmentManager = StringBuilder()
            stringBuilderAnalyticsManager = StringBuilder()
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
         * This Method Used For Re-Instantation Of The File Path Name  You Have Specified.
         */
        fun changeFilePathName(filePathName: String?) {
            this.filePathName = filePathName
        }

        /**
         * This Method Attaches A LifeCycle Observer For The Current Activity.
         * Parameters:
         * @param context is for getting reference from the application context , you must deploy this parameter.
         * @param fragmentManager is used for getting details from FragmentManager which is used for  tracking life cycle of Fragments rather than activity.
         * Variables:
         * @var If fragmentManager is not null then fragmentLifeCycleObserver is used for initializing the fragment observer class and adding fragment observer into fragment.
         * @var If fragmentManager is null then lifeCycleobserver is used for initializing the activity observer class and adding activity observer into activity.
         * @return Boolean value.
         */
        private fun logAttach(context: Context, fragmentManager: FragmentManager? = null): Boolean {
            if (fragmentManager != null) {
                fragmentLifeCycleObserver =
                    LogFragmentLifeCycleObserver(
                        fragmentManager = fragmentManager
                    )
                fragmentManager.registerFragmentLifecycleCallbacks(fragmentLifeCycleObserver, true)
            } else {
                lifeCycleObserver = LogLifeCycleObserver()
                lifeCycleObserver.registerLifeCycle(context)
            }
            recyclerViewItemObserver = LogDataSetObserver(context)
            return true
        }

        //dummy method probably removed.
        private fun attachRootView(rootView: ViewGroup) {
            val view: View = LayoutInflater.from(context)
                .inflate(R.layout.default_file_text_reader, rootView, true)
            textViewFileReader = view.findViewById(R.id.textView_file_reader)
            buttonFileReader = view.findViewById(R.id.button_file_reader)
            textViewFileReader.movementMethod = ScrollingMovementMethod()
            when {
                stringBuilderComponent.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderComponent
                stringBuilderLifeCycle.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderLifeCycle
                stringBuilderFragmentManager.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderFragmentManager
                stringBuilderAnalyticsManager.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderAnalyticsManager
                stringBuilderHttp.isNotEmpty() -> textViewFileReader.text = stringBuilderHttp
                stringBuilderInAPurchase.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderInAPurchase
                stringBuilderRetrofit.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderRetrofit
                stringBuilderQuery.isNotEmpty() -> textViewFileReader.text = stringBuilderQuery
                stringBuilderRealm.isNotEmpty() -> textViewFileReader.text = stringBuilderRealm
                stringBuilderException.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderException
                stringBuilderAll.isNotEmpty() -> textViewFileReader.text = stringBuilderAll
            }
            buttonFileReader.setOnClickListener {
                //                rootView.removeView(view.rootView)
            }
        }

        /**
         * This Method used for when a saving method exceed's 2mb file limit and delete's old entries at the start and add's new entries to the end of the file.
         * Parameters:
         * @param stringBuilder is used for getting the reference of stringBuilder of called saving method.
         * @param file is used for getting the reference of stringBuilder of called saving method.
         * Variables:
         * @var scannerFile is used for getting file content to the scanner instance.
         * @var scannerStringBuilder is used for getting used saving method content to the scanner instance.
         * @var scannerTempStringBuilder is used for getting used saving method content to the temporary scanner instance.
         * @var temFile is used for opening temporary file that will be used for getting the deleted content's from real file(only have 2mb file limit).
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun exceededFileLimitWriter(
            stringBuilder: StringBuilder,
            file: File
        ) {
            try {
                val scannerFile = Scanner(file)
                val scannerStringBuilder =
                    Scanner(stringBuilder.toString())
                val scannerTempStringBuilder =
                    Scanner(stringBuilder.toString())
                val tempFile =
                    File(file.toString().substringBeforeLast("/"), "logger_bird_details_temp.txt")
                file.delete()
                file.createNewFile()
                if (!tempFile.exists()) {
                    tempFile.createNewFile()
                }
                if (tempFile.length() > fileLimit) {
                    tempFile.delete()
                    tempFile.createNewFile()
                }
                do {
                    if (scannerTempStringBuilder.hasNextLine()) {
                        scannerTempStringBuilder.nextLine()
                        tempFile.appendText(scannerFile.nextLine() + "\n")
                    } else {
                        file.appendText(scannerFile.nextLine() + "\n")
                    }
                } while (scannerFile.hasNextLine())
                do {
                    if (scannerStringBuilder.hasNextLine()) {
                        file.appendText(
                            scannerStringBuilder.nextLine() + "\n"
                        )
                    }
                } while (scannerStringBuilder.hasNextLine())
            } catch (e: Exception) {
                e.printStackTrace()
                callExceptionDetails(exception = e, tag = Constants.exceedFileLimitTag)
            }
            stringBuilderExceedFileWriterLimit = StringBuilder()
        }

        /**
         * This Method Saves Component Details To Txt File.
         * Parameters:
         * Variables:
         * @var file is used for getting reference of file details that are desired given in logInit method.
         * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
         * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
         * @var stringBuilderComponent prints component details.
         * @var stringBuilderExceedFileWriterLimit is used for printing deleted content from real file to temporary file.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveComponentDetails() {
            if (stringBuilderComponent.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory, "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory, "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        filePath.appendText(
                            takeBuilderDetails()
                        )
                        filePath.appendText(stringBuilderComponent.toString())
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(stringBuilderComponent.toString())
                        } else {
                            filePath.appendText(
                                stringBuilderComponent.toString()
                            )
                        }
                    }
//                            if (rootView != null) {
//                                withContext(Dispatchers.Main) {
//                                    attachRootView(rootView)
//                                }
//                            }
                    workQueueLinked.controlRunnable = false
                    if (runnableList.size > 0) {
                        runnableList.removeAt(0)
                        if (runnableList.size > 0) {
                            workQueueLinked.put(runnableList[0])
                        }
                    }
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.componentTag
                    )
                }

            } else {
                throw LoggerBirdException(
                    Constants.saveErrorMessage + Constants.componentMethodTag
                )
            }
        }

        /**
         * This Method Saves Life-Cycle Details To Txt File.
         * Variables:
         * @var LoggerBirdService.onDestroyMessage used for getting onDestroy state for lifecycle via service.
         * @var file is used for getting reference of file details that are desired given in logInit method.
         * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
         * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
         * @var stringBuilderLifeCycle prints life-cycle details.
         * @var stringBuilderExceedFileWriterLimit is used for printing deleted content from real file to temporary file.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveLifeCycleDetails() {
            if (stringBuilderLifeCycle.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    if (filePathName != null) {
                        filePath = File(
                            fileDirectory, "$filePathName.txt"
                        )
                    } else {
                        filePath = File(
                            fileDirectory, "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        filePath.appendText(
                            takeBuilderDetails()
                        )
                        filePath.appendText(
                            stringBuilderLifeCycle.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(stringBuilderLifeCycle.toString())
                        } else {
                            filePath.appendText(
                                stringBuilderLifeCycle.toString()
                            )
                        }
                    }
//                               if (rootView != null) {
//                                    attachRootView(rootView)
//                                }
                    workQueueLinked.controlRunnable = false
                    if (runnableList.size > 0) {
                        runnableList.removeAt(0)
                        if (runnableList.size > 0) {
                            workQueueLinked.put(runnableList[0])
                        }
                    }
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                    if (LoggerBirdService.onDestroyMessage != null) {
                        saveSessionIntoOldSessionFile()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.lifeCycleTag
                    )
                }
            } else {
                throw LoggerBirdException(
                    Constants.saveErrorMessage + Constants.lifeCycleMethodTag
                )
            }
        }

        /**
         * This Method Saves FragmentManager Details To Txt File.
         * Variables:
         * @var file is used for getting reference of file details that are desired given in logInit method.
         * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files
         * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details")
         * @var stringBuilderFragmentManager prints fragment manager details.
         * @var stringBuilderExceedFileWriterLimit is used for printing deleted content from real file to temporary file.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveFragmentManagerDetails() {
            if (stringBuilderFragmentManager.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    if (filePathName != null) {
                        filePath = File(
                            fileDirectory, "$filePathName.txt"
                        )
                    } else {
                        filePath = File(
                            fileDirectory, "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        filePath.appendText(
                            takeBuilderDetails()
                        )
                        filePath.appendText(
                            stringBuilderFragmentManager.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderFragmentManager.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderFragmentManager.toString()
                            )
                        }
                    }
//                            if (rootView != null) {
//                                withContext(Dispatchers.Main) {
//                                    attachRootView(rootView)
//                                }
//                            }
                    workQueueLinked.controlRunnable = false
                    if (runnableList.size > 0) {
                        runnableList.removeAt(0)
                        if (runnableList.size > 0) {
                            workQueueLinked.put(runnableList[0])
                        }
                    }
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.fragmentManagerTag
                    )
                }
            } else {
                throw LoggerBirdException(
                    Constants.saveErrorMessage + Constants.fragmentManagerMethodTag
                )
            }
        }

        /**
         * This Method Saves Analytics Details To Txt File.
         * Variables:
         * @var file is used for getting reference of file details that are desired given in logInit method.
         * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
         * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
         * @var stringBuilderAnalyticsManager prints analytics details.
         * @var stringBuilderExceedFileWriterLimit is used for printing deleted content from real file to temporary file.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveAnalyticsDetails() {
            if (stringBuilderAnalyticsManager.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    if (filePathName != null) {
                        filePath = File(
                            fileDirectory, "$filePathName.txt"
                        )
                    } else {
                        filePath = File(
                            fileDirectory, "logger_bird.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        filePath.appendText(
                            takeBuilderDetails()
                        )
                        filePath.appendText(
                            stringBuilderAnalyticsManager.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderAnalyticsManager.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderAnalyticsManager.toString()
                            )
                        }
                    }
//                            if (rootView != null) {
//                                withContext(Dispatchers.Main) {
//                                    attachRootView(rootView)
//                                }
//                            }
                    workQueueLinked.controlRunnable = false
                    if (runnableList.size > 0) {
                        runnableList.removeAt(0)
                        if (runnableList.size > 0) {
                            workQueueLinked.put(runnableList[0])
                        }
                        if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                            exceededFileLimitWriter(
                                stringBuilder = stringBuilderExceedFileWriterLimit,
                                file = filePath
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.analyticsTag
                    )
                }

            } else {
                throw LoggerBirdException(
                    Constants.saveErrorMessage + Constants.analyticsMethodTag
                )
            }
        }

        /**
         * This Method Saves HttpRequest Details To Txt File.
         * Variables:
         * @var file is used for getting reference of file details that are desired given in logInit method.
         * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
         * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
         * @var stringBuilderHttp prints http details.
         * @var stringBuilderExceedFileWriterLimit is used for printing deleted content from real file to temporary file.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveHttpRequestDetails() {
            if (stringBuilderHttp.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    if (filePathName != null) {
                        filePath = File(
                            fileDirectory, "$filePathName.txt"
                        )
                    } else {
                        filePath = File(
                            fileDirectory, "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        filePath.appendText(
                            takeBuilderDetails()
                        )
                        filePath.appendText(
                            stringBuilderHttp.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(stringBuilderHttp.toString())
                        } else {
                            filePath.appendText(
                                stringBuilderHttp.toString()
                            )
                        }
                    }
//                            if (rootView != null) {
//                                withContext(Dispatchers.Main) {
//                                    attachRootView(rootView)
//                                }
//                            }
                    workQueueLinked.controlRunnable = false
                    if (runnableList.size > 0) {
                        runnableList.removeAt(0)
                        if (runnableList.size > 0) {
                            workQueueLinked.put(runnableList[0])
                        }
                    }
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.httpTag
                    )
                }
            } else {
                throw LoggerBirdException(
                    Constants.saveErrorMessage + Constants.htppRequestMethodTag
                )
            }
        }

        /**
         * This Method Saves Android In A Purchase Details To Txt File.
         * Variables:
         * @var file is used for getting reference of file details that are desired given in logInit method.
         * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
         * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
         * @var stringBuilderInAPurchase prints android In A Purchase details.
         * @var stringBuilderExceedFileWriterLimit is used for printing deleted content from real file to temporary file.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveInAPurchaseDetails() {
            if (stringBuilderInAPurchase.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory, "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory, "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        filePath.appendText(
                            takeBuilderDetails()
                        )
                        filePath.appendText(
                            stringBuilderInAPurchase.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(stringBuilderInAPurchase.toString())
                        } else {
                            filePath.appendText(
                                stringBuilderInAPurchase.toString()
                            )
                        }
                    }
//                            if (rootView != null) {
//                                withContext(Dispatchers.Main) {
//                                    attachRootView(rootView)
//                                }
//                            }
                    workQueueLinked.controlRunnable = false
                    if (runnableList.size > 0) {
                        runnableList.removeAt(0)
                        if (runnableList.size > 0) {
                            workQueueLinked.put(runnableList[0])
                        }
                    }
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.inAPurchaseTag
                    )
                }
            } else {
                throw LoggerBirdException(
                    Constants.saveErrorMessage + Constants.inAPurchaseMethodTag
                )
            }
        }

        /**
         * This Method Saves RetrofitRequest Details To Txt File.
         * Variables:
         * @var file is used for getting reference of file details that are desired given in logInit method.
         * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
         * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
         * @var stringBuilderRetrofit prints retrofit details.
         * @var stringBuilderExceedFileWriterLimit is used for printing deleted content from real file to temporary file.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveRetrofitRequestDetails() {
            if (stringBuilderRetrofit.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory, "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory, "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        filePath.appendText(
                            takeBuilderDetails()
                        )
                        filePath.appendText(
                            stringBuilderRetrofit.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(stringBuilderRetrofit.toString())
                        } else {
                            filePath.appendText(
                                stringBuilderRetrofit.toString()
                            )
                        }
                    }
//                            if (rootView != null) {
//                                withContext(Dispatchers.Main) {
//                                    attachRootView(rootView)
//                                }
//                            }
                    workQueueLinked.controlRunnable = false
                    if (runnableList.size > 0) {
                        runnableList.removeAt(0)
                        if (runnableList.size > 0) {
                            workQueueLinked.put(runnableList[0])
                        }
                    }
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.retrofitTag
                    )
                }
            } else {
                throw LoggerBirdException(
                    Constants.saveErrorMessage + Constants.retrofitMethodTag
                )
            }
        }

        /**
         * This Method Saves Realm Details To Txt File.
         * Variables:
         * @var file is used for getting reference of file details that are desired given in logInit method.
         * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
         * @var defaultFilePath is used for getting defaultFileDirectory and default file name("logger_bird_details").
         * @var stringBuilderRealm prints realm details.
         * @var stringBuilderExceedFileWriterLimit is used for printing deleted content from real file to temporary file.
         * Exception:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveRealmDetails() {
            if (stringBuilderRealm.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory, "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory, "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        filePath.appendText(
                            takeBuilderDetails()
                        )
                        filePath.appendText(
                            stringBuilderRealm.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(stringBuilderRealm.toString())
                        }
                    }
//                            if (rootView != null) {
//                                withContext(Dispatchers.Main) {
//                                    attachRootView(rootView)
//                                }
//                            }
                    workQueueLinked.controlRunnable = false
                    if (runnableList.size > 0) {
                        runnableList.removeAt(0)
                        if (runnableList.size > 0) {
                            workQueueLinked.put(runnableList[0])
                        }
                    }
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.realmTag
                    )
                }
            } else {
                throw LoggerBirdException(
                    Constants.saveErrorMessage + Constants.realmMethodTag
                )
            }
        }

        /**
         * This Method Saves Exception Details To Txt File.
         * Variables:
         * @var file is used for getting reference of file details that are desired given in logInit method.
         * @var defaultFileDirectory is used for getting default file path which is data->data->your project package name->files.
         * @var defaultFilePath is used for getting defaultFileDirectory and default file name("exception_details").
         * @var stringBuilderException prints com.mobilex.loggerbird.exception details.
         * @var stringBuilderExceedFileWriterLimit is used for printing deleted content from real file to temporary file.
         * Exception:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveExceptionDetails() {
            if (stringBuilderException.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    if (filePathName != null) {
                        filePath = File(
                            fileDirectory, "$filePathName.txt"
                        )
                    } else {
                        filePath = File(
                            fileDirectory, "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        filePath.appendText(
                            takeBuilderDetails()
                        )
                        filePath.appendText(
                            stringBuilderException.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(stringBuilderException.toString())
                        } else {
                            filePath.appendText(
                                stringBuilderException.toString()
                            )
                        }
                    }
//                        if (rootView != null) {
//                            withContext(Dispatchers.Main) {
//                                attachRootView(rootView)
//                            }
//                        }
                    workQueueLinked.controlRunnable = false
                    if (runnableList.size > 0) {
                        runnableList.removeAt(0)
                        if (runnableList.size > 0) {
                            workQueueLinked.put(runnableList[0])
                        }
                    }
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                    if (uncaughtExceptionHandlerController) {
                        EmailUtil.sendUnhandledException(file = filePath,context = context)
                        saveSessionIntoOldSessionFile()
                        android.os.Process.killProcess(android.os.Process.myPid());
                        exitProcess(0);
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                throw LoggerBirdException(
                    Constants.saveErrorMessage + Constants.exceptionMethodTag
                )
            }
        }

        //dummy worker method probably deleted.
        private fun callEnqueue() {
            val saveFileWorkRequest = OneTimeWorkRequestBuilder<WorkerUtil>().build()
            WorkManager.getInstance(context).enqueue(saveFileWorkRequest)
        }

        //in progress method.
        fun callComponentDetails(view: View?, resources: Resources?) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put(Runnable {
                        takeComponentDetails(
                            view = view,
                            resources = resources
                        )
                    })
                }
                runnableList.add(Runnable {
                    takeComponentDetails(
                        view = view,
                        resources = resources
                    )
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        //in progress method.
        fun callLifeCycleDetails() {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put(Runnable { takeLifeCycleDetails() })
                }
                runnableList.add(Runnable { takeLifeCycleDetails() })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        //in progress method.
        fun callAnalyticsDetails(bundle: Bundle?) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put(Runnable {
                        takeAnalyticsDetails(bundle = bundle)
                    })
                }
                runnableList.add(Runnable { takeAnalyticsDetails(bundle = bundle) })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        //in progress method.
        fun callFragmentManagerDetails(fragmentManager: FragmentManager?) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put(Runnable {
                        takeFragmentManagerDetails(fragmentManager = fragmentManager)
                    })
                }
                runnableList.add(Runnable { takeFragmentManagerDetails(fragmentManager = fragmentManager) })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        //in progress method.
        fun callHttpRequestDetails(httpUrlConnection: HttpURLConnection?) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put(Runnable {
                        takeHttpRequestDetails(httpUrlConnection = httpUrlConnection)
                    })
                }
                runnableList.add(Runnable { takeHttpRequestDetails(httpUrlConnection = httpUrlConnection) })

            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        //in progress method.
        fun callInAPurchase(
            billingClient: BillingClient? = null,
            billingResult: BillingResult? = null,
            skuDetailsParams: SkuDetailsParams? = null,
            billingFlowParams: BillingFlowParams? = null,
            acknowledgePurchaseParams: AcknowledgePurchaseParams? = null
        ) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put(Runnable {
                        takeInAPurchaseDetails(
                            billingClient = billingClient,
                            billingResult = billingResult,
                            skuDetailsParams = skuDetailsParams,
                            billingFlowParams = billingFlowParams,
                            acknowledgePurchaseParams = acknowledgePurchaseParams
                        )
                    })
                }
                runnableList.add(Runnable {
                    takeInAPurchaseDetails(
                        billingClient = billingClient,
                        billingResult = billingResult,
                        skuDetailsParams = skuDetailsParams,
                        billingFlowParams = billingFlowParams,
                        acknowledgePurchaseParams = acknowledgePurchaseParams
                    )
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        //in progress method.
        fun callRetrofitRequestDetails(
            retrofit: Retrofit? = null,
            response: Response? = null,
            request: Request? = null
        ) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put(Runnable {
                        takeRetrofitRequestDetails(
                            retrofit = retrofit,
                            response = response,
                            request = request
                        )
                    })
                }
                runnableList.add(Runnable {
                    takeRetrofitRequestDetails(
                        retrofit = retrofit,
                        response = response,
                        request = request
                    )
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        //in progress method.
        fun callRealmDetails(realm: Realm? = null, realmModel: RealmModel? = null) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put(Runnable {
                        takeRealmDetails(realm = realm, realmModel = realmModel)
                    })
                }
                runnableList.add(Runnable {
                    takeRealmDetails(
                        realm = realm,
                        realmModel = realmModel
                    )
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        //in progress method.
        fun callExceptionDetails(
            exception: Exception? = null,
            tag: String? = null,
            throwable: Throwable? = null
        ) {
            if (runnableList.isEmpty()) {
                workQueueLinked.put(Runnable {
                    takeExceptionDetails(exception = exception, tag = tag, throwable = throwable)
                })
            }
            runnableList.add(Runnable {
                takeExceptionDetails(
                    exception = exception,
                    tag = tag,
                    throwable = throwable
                )
            })
        }

        //In progress method.
        fun callOldSecessionFile(tag: String?) {
            if (runnableList.isEmpty()) {
                workQueueLinked.put(Runnable { saveSessionIntoOldSessionFile() })
            }
            runnableList.add(Runnable { saveSessionIntoOldSessionFile() })
        }

        //In progress method.
        private fun saveSessionIntoOldSessionFile() {
            try {
                val scannerOldSecessionFile = Scanner(filePath)
                val oldSecessionFile = if (filePathName != null) {
                    File(filePath.path.substringBeforeLast("/"), filePathName + "_old_session.txt")
                } else {
                    File(
                        filePath.path.substringBeforeLast("/"),
                        "logger_bird_details_old_session.txt"
                    )
                }
                if (oldSecessionFile.exists()) {
                    oldSecessionFile.delete()
                    oldSecessionFile.createNewFile()
                } else {
                    oldSecessionFile.createNewFile()
                }
                do {
                    oldSecessionFile.appendText(scannerOldSecessionFile.nextLine() + "\n")
                } while (scannerOldSecessionFile.hasNextLine())
                filePath.delete()
                filePath=oldSecessionFile
            } catch (e: Exception) {
                e.printStackTrace()
                callExceptionDetails(exception = e, tag = Constants.saveSessionOldFileTag)
            }
        }

        /**
         * //in progress method.
         * This Method Takes Component Details.
         * Parameters:
         * @param view parameter used for getting id of the component.
         * @param resources parameter used for getting component name with view parameter.
         * Variables:
         * @var workQueueLinked is used for locking queue class when there is a execution of transaction in the queue.
         * @var controlLogInit is used for getting logInit method return value.
         * @var coroutineCallComponent is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var current time used for getting local time of your devices.
         * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second)
         * @var stringBuilderComponent used for printing details.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeComponentDetails(view: View?, resources: Resources?) {
            workQueueLinked.controlRunnable = true
            coroutineCallComponent.async {
                if (controlLogInit) {
                    try {
//                         throw NullPointerException("button is null")
                        stringBuilderComponent = StringBuilder()
                        recyclerViewAdapterDataObserver.refreshRecyclerViewObserverState()
                        recyclerViewChildAttachStateChangeListener.refreshRecyclerViewObserverState()
                        recyclerViewItemTouchListener.refreshRecyclerViewObserverState()
                        recyclerViewScrollListener.refreshRecyclerViewObserverState()
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)
                        if (view is RecyclerView) {
                            takeRecyclerViewDetails(recyclerView = view, resources = resources)
                        } else {
                            if (view != null) {
                                stringBuilderComponent.append(
                                    formattedTime + ":" + Constants.componentTag + "\n" + "Component Name:" + (resources?.getResourceName(
                                        view.id
                                    )) + " " + "Component Id:" + view.id + "\n" + "Component Type:" + view.findViewById<View>(
                                        view.id
                                    ).toString() + "\n"
                                )
                            } else {
                                stringBuilderComponent.append(
                                    formattedTime + ":" + Constants.componentTag + "\n" + "Component Name:" + "null" + " " + "Component Id:" + view?.id + "\n" + "Component Type:" + "null" + "\n"
                                )
                            }
                        }
                        saveComponentDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.componentTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
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
        private fun takeRecyclerViewDetails(recyclerView: RecyclerView, resources: Resources?) {
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
                    ).toString() + "\n" + "RecyclerView Layout:" + recyclerView.layoutManager + "\n" + "RecyclerView Adapter:" + recyclerView.adapter + "\n" + "RecyclerView Item Size:" + recyclerView.adapter?.itemCount + "\n" + "RecyclerView Item list:" + "\n" + recyclerViewList.toString() + "\n"
                )
                stringBuilderComponent.append(recyclerViewAdapterDataObserver.returnRecyclerViewState())
                stringBuilderComponent.append(recyclerViewScrollListener.returnRecyclerViewState())
                stringBuilderComponent.append(recyclerViewChildAttachStateChangeListener.returnRecyclerViewState())
            } catch (e: Exception) {
                e.printStackTrace()
                callExceptionDetails(exception = e, tag = Constants.componentTag)
            }
        }

        /**
         * This Method Takes Life-Cycle Details.
         * Variables:
         * @var workQueueLinked is used for locking queue class when there is a execution of transaction in the queue.
         * @var controlLogInit is used for getting logInit method return value.
         * @var coroutineCallLifeCycle is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var stringBuilderLifeCycle used for printing details.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        internal fun takeLifeCycleDetails() {
            workQueueLinked.controlRunnable = true
            if (LoggerBirdService.onDestroyMessage != null) {
                if (controlLogInit) {
                    stringBuilderLifeCycle = StringBuilder()
                    try {
                        if (Companion::fragmentLifeCycleObserver.isInitialized) {
                            if (fragmentLifeCycleObserver.returnFragmentLifeCycleState().isNotEmpty()) {
                                for (classList in fragmentLifeCycleObserver.returnClassList()) {
                                    stringBuilderLifeCycle.append(classList + ":" + "\n")
                                    for (stateList in fragmentLifeCycleObserver.returnFragmentLifeCycleState().split(
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
                                for (stateList in lifeCycleObserver.returnActivityLifeCycleState().split(
                                    "\n"
                                )) {
                                    if (stateList.contains(classList)) {
                                        stringBuilderLifeCycle.append(stateList + "\n")
                                    }
                                }
                            }
                        }
                        stringBuilderLifeCycle.append(LoggerBirdService.onDestroyMessage)
                        saveLifeCycleDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.lifeCycleTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            } else {
                coroutineCallLifeCycle.async {
                    if (controlLogInit) {
                        stringBuilderLifeCycle = StringBuilder()
                        try {
                            if (Companion::fragmentLifeCycleObserver.isInitialized) {
                                if (fragmentLifeCycleObserver.returnFragmentLifeCycleState().isNotEmpty()) {
                                    for (classList in fragmentLifeCycleObserver.returnClassList()) {
                                        stringBuilderLifeCycle.append(classList + ":" + "\n")
                                        for (stateList in fragmentLifeCycleObserver.returnFragmentLifeCycleState().split(
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
                                    for (stateList in lifeCycleObserver.returnActivityLifeCycleState().split(
                                        "\n"
                                    )) {
                                        if (stateList.contains(classList)) {
                                            stringBuilderLifeCycle.append(stateList + "\n")
                                        }
                                    }
                                }
                            }
                            saveLifeCycleDetails()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            callExceptionDetails(
                                exception = e,
                                tag = Constants.lifeCycleTag
                            )
                        }

                    } else {
                        throw LoggerBirdException(Constants.logInitErrorMessage)
                    }
                }
            }
        }

        //adnan improved this method probably will be deleted.
        private fun takeBuilderDetails(): String {
            stringBuilderBuild = StringBuilder()
            stringBuilderBuild.append("Device Information:" + "\n" + "ID:" + Build.ID + "\n" + "DEVICE:" + Build.DEVICE + "\n" + "DEVICE MODEL:" + Build.MODEL + "\n" + "DEVICE TYPE:" + Build.TYPE + "\n" + "USER:" + Build.USER + "\n" + "SDK VERSION:" + Build.VERSION.SDK_INT + "\n" + "MANUFACTURER:" + Build.MANUFACTURER + "\n" + "HOST:" + Build.HOST + "HARDWARE:" + Build.HARDWARE + "\n")
            return stringBuilderBuild.toString()
        }

        /**
         * This Method Takes Analytics Details.
         * Parameters:
         * @param bundle parameter used for getting details from analytic bundle.
         * Variables:
         * @var workQueueLinked is used for locking queue class when there is a execution of transaction in the queue.
         * @var controlLogInit is used for getting logInit method return value.
         * @var coroutineCallAnalytic is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var current time used for getting local time of your devices.
         * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
         * @var stringBuilderAnalyticsManager used for printing the details.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeAnalyticsDetails(bundle: Bundle? = null) {
            workQueueLinked.controlRunnable = true
            coroutineCallAnalytics.async {
                if (controlLogInit) {
                    try {
                        stringBuilderAnalyticsManager = StringBuilder()
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
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.analyticsTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }
        }

        /**
         * This Method Takes FragmentManager Details.
         * Parameters:
         * @param fragmentManager parameter used for getting details from FragmentManager and printing all fragments in FragmentManager.
         * Variables:
         * @var workQueueLinked is used for locking queue class when there is a execution of transaction in the queue.
         * @var controlLogInit is used for getting logInit method return value.
         * @var coroutineCallFragment is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var stringBuilderFragmentManager used for printing the details.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeFragmentManagerDetails(
            fragmentManager: FragmentManager? = null
        ) {
            workQueueLinked.controlRunnable = true
            coroutineCallFragmentManager.async {
                if (controlLogInit) {
                    try {
                        stringBuilderFragmentManager = StringBuilder()
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
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.fragmentManagerTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }
        }

        /**
         * This Method Takes HttpRequest Details.
         * Parameters:
         * @param httpUrlConnection parameter used for getting details from HttpUrlConnection which is used for printing response code and response message.
         * Variables:
         * @var workQueueLinked is used for locking queue class when there is a execution of transaction in the queue.
         * @var controlLogInit is used for getting logInit method return value.
         * @var coroutineCallHttpRequest is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var current time used for getting local time of your devices.
         * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
         * @var stringBuilderHttp used for printing the details.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeHttpRequestDetails(
            httpUrlConnection: HttpURLConnection? = null
        ) {
            workQueueLinked.controlRunnable = true
            coroutineCallHttpRequest.async {
                if (controlLogInit) {
                    try {
                        stringBuilderHttp = StringBuilder()
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)
                        stringBuilderHttp.append("\n" + formattedTime + ":" + Constants.httpTag + "\n" + "Http Request Code:" + httpUrlConnection?.responseCode + " " + "Http Response Message:" + httpUrlConnection?.responseMessage)
                        saveHttpRequestDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.httpTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
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
         * @var workQueueLinked is used for locking queue class when there is a execution of transaction in the queue.
         * @var controlLogInit is used for getting logInit method return value.
         * @var coroutineCallInAPurchase is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var current time used for getting local time of your devices.
         * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
         * @var gson used for converting a json format to the String format.
         * @var prettyJson is the value of billingFlowParams sku's details from json format to the String format.
         * @var responseMessage is the outcome message according to the response code comes from billingResult.
         * @var stringBuilderSkuDetailList used for printing the details.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeInAPurchaseDetails(
            billingClient: BillingClient? = null,
            billingResult: BillingResult? = null,
            skuDetailsParams: SkuDetailsParams? = null,
            billingFlowParams: BillingFlowParams? = null,
            acknowledgePurchaseParams: AcknowledgePurchaseParams? = null
        ) {
            workQueueLinked.controlRunnable = true
            coroutineCallInAPurchase.async {
                if (controlLogInit) {
                    try {
                        stringBuilderInAPurchase = StringBuilder()
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
                                3 -> responseMessage =
                                    "The Google Play Billing AIDL version is not supported for the type requested"
                                4 -> responseMessage =
                                    "Requested product is not available for purchase"
                                5 -> responseMessage =
                                    "Invalid arguments provided to the API.This error can also indicate that the application was not correctly signed or properly \n set up for Google Play Billing , or does not have the neccessary permissions in the manifest"
                                6 -> responseMessage = "Fatal error during the API action"
                                7 -> responseMessage =
                                    "Failure to purchase since item is already owned"
                                8 -> responseMessage = "Failure to consume since item is not owned"
                            }
                        }
                        stringBuilderInAPurchase.append("\n" + formattedTime + ":" + Constants.inAPurchaseTag + "\n" + "Billing Flow Item Consumed:" + billingFlowParams?.skuDetails?.isRewarded + "\n" + "Billing Response Code:" + billingResult?.responseCode + "\n" + "Billing Response Message:" + responseMessage + "\n" + "Billing Client Is Ready:" + billingClient?.isReady + "\n" + "Sku Type:" + skuDetailsParams?.skuType + "\n" + "Sku List:" + stringBuilderSkuDetailList.toString() + "\n" + "Billing Flow Sku Details:" + prettyJson + "\n" + "Billing Flow Sku:" + billingFlowParams?.sku + "\n" + "Billing Flow Account Id:" + billingFlowParams?.accountId + "\n" + "Billing Flow Developer Id:" + billingFlowParams?.developerId + "\n" + "Billing flow Old Sku:" + billingFlowParams?.oldSku + "\n" + "Billing Flow Old Sku Purchase Token:" + billingFlowParams?.oldSkuPurchaseToken + "\n" + "Acknowledge Params:" + acknowledgePurchaseParams?.developerPayload + "\n" + "Acknowledge Purchase Token:" + acknowledgePurchaseParams?.purchaseToken)
                        saveInAPurchaseDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.inAPurchaseTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }
        }

        /**
         * This Method Takes Retrofit Request Details.
         * Parameters:
         * @param retrofit parameter used for getting details from Retrofit which is used for getting base url of request.
         * @param response parameter used for getting details from Response which is used for getting response code ,response message ,response success and response body.
         * @param request parameter used for getting details from Request which is used for getting request query and request method.
         * Variables:
         * @var workQueueLinked is used for locking queue class when there is a execution of transaction in the queue.
         * @var controlLogInit is used for getting logInit method return value.
         * @var coroutineCallRetrofit is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var current time used for getting local time of your devices.
         * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
         * @var stringBuilderRetrofit used for printing the details.
         * @var stringBuilderQuery used for printing the details of query that gathered from request.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeRetrofitRequestDetails(
            retrofit: Retrofit? = null,
            response: Response? = null,
            request: Request? = null
        ) {
            workQueueLinked.controlRunnable = true
            coroutineCallRetrofit.async {
                if (controlLogInit) {
                    try {
                        stringBuilderRetrofit = StringBuilder()
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
                                    ) + "," + request.url.queryParameterValue(parameterQueryCounter) + "\n"
                                )
                                parameterQueryCounter++
                            }
                        }
                        withContext(Dispatchers.IO) {
                            coroutineRetrofitTask.async {
                                withContext(Dispatchers.IO) {
                                    stringBuilderRetrofit.append("\n" + formattedTime + ":" + Constants.retrofitTag + "\n" + "Retrofit Request Code:" + response?.code + " " + "Response Message:" + response?.message + "\n" + "Retrofit Url:" + retrofit?.baseUrl() + " " + "Request Url:" + request?.url + "\n" + "Response Success:" + response?.isSuccessful + "\n" + "Request Method:" + request?.method + "\n" + stringBuilderQuery.toString() + "Response Value:" + response?.body?.string())
                                    saveRetrofitRequestDetails()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.retrofitTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }
        }
        //return asyncLogRetrofitTask(request = request,response = response,retrofit = retrofit).execute().get()
        /**
         * This Method Takes Realm Details.
         * Parameters:
         * @param realm parameter used for getting details from Realm which is used for getting permissions,privileges and copy realm data.
         * @param realm model parameter used for getting details from RealmModel which is used for giving realm data to the Realm method which is copyFromRealm().
         * Variables:
         * @var workQueueLinked is used for locking queue class when there is a execution of transaction in the queue.
         * @var controlLogInit is used for getting logInit method return value.
         * @var coroutineCallRealm is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var current time used for getting local time of your devices.
         * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
         * @var stringBuilderRealm used for printing the details.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeRealmDetails(
            realm: Realm? = null,
            realmModel: RealmModel? = null
        ) {
            workQueueLinked.controlRunnable = true
            coroutineCallRealm.async {
                if (controlLogInit) {
                    try {
                        stringBuilderRealm = StringBuilder()
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)
                        stringBuilderRealm.append(
                            "\n" + formattedTime + ":" + Constants.realmTag + "Realm Details:" + "\n" + "Permissions:" + realm?.permissions + " " + "Privileges:" + realm?.privileges + "\n" + "Realm Model:" + realm?.copyFromRealm(
                                realmModel
                            )
                        )
                        saveRealmDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.realmTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }
        }

        /**
         * This Method Takes Exception Details.
         * Parameters:
         * @param exception parameter used for getting details from Exception which is used for getting deneme.example.loggerbird.exception messages.
         * @param tag parameter used for getting details of which method caused this deneme.example.loggerbird.exception.
         * Variables:
         * @var workQueueLinked is used for locking queue class when there is a execution of transaction in the queue.
         * @var controlLogInit is used for getting logInit method return value.
         * @var coroutineCallException is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var current time used for getting local time of your devices.
         * @var formatted time used for formatting time as "HH:mm:ss.SSS".(hour,minute,second,split second).
         * @var stringBuilderException used for printing the details.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeExceptionDetails(
            exception: Exception? = null,
            tag: String? = null,
            throwable: Throwable? = null
        ) {
            workQueueLinked.controlRunnable = true
            try {
                coroutineCallException.async {
                    stringBuilderException = StringBuilder()
                    val date = Calendar.getInstance().time
                    val formatter = SimpleDateFormat.getDateTimeInstance()
                    formattedTime = formatter.format(date)
                    if (exception != null) {
                        if (Log.getStackTraceString(exception).isNotEmpty()) {
                            stringBuilderException.append(
                                "\n" + "Method Tag:" + tag +
                                        "\n" + formattedTime + ":" + Constants.exceptionTag + "\n" + "Exception:" + Log.getStackTraceString(
                                    exception
                                )
                            )
                        } else {
                            stringBuilderException.append(
                                "\n" + "Method Tag:" + tag +
                                        "\n" + formattedTime + ":" + Constants.exceptionTag + "\n" + "Exception:" + exception.message
                            )
                        }
                    } else if (throwable != null) {
                        if (Log.getStackTraceString(throwable).isNotEmpty()) {
                            stringBuilderException.append(
                                "\n" + "Method Tag:" + tag +
                                        "\n" + formattedTime + ":" + Constants.unHandledExceptionTag + "\n" + "Throwable:" + Log.getStackTraceString(
                                    throwable
                                )
                            )
                        } else {
                            stringBuilderException.append(
                                "\n" + "Method Tag:" + tag +
                                        "\n" + formattedTime + ":" + Constants.unHandledExceptionTag + "\n" + "Throwable:" + throwable.message
                            )
                        }

                    }
                    saveExceptionDetails()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callExceptionDetails(exception = e, tag = Constants.exceptionTag)
            }
        }

        fun callEmailSender(
            file: File? = null,
            context: Context,
            progressBar: ProgressBar? = null,
            rootView: ViewGroup? = null
        ) {
            if (runnableList.isEmpty()) {
                workQueueLinked.put(Runnable {
                    if (file != null) {
                        sendDetailsAsEmail(
                            file = file,
                            context = context,
                            progressBar = progressBar,
                            rootView = rootView
                        )
                    } else {
                        sendDetailsAsEmail(
                            file = this.filePath,
                            context = context,
                            progressBar = progressBar,
                            rootView = rootView
                        )
                    }
                })
            }
            runnableList.add(Runnable {
                if (file != null) {
                    sendDetailsAsEmail(
                        file = file,
                        context = context,
                        progressBar = progressBar,
                        rootView = rootView
                    )
                } else {
                    sendDetailsAsEmail(
                        file = this.filePath,
                        context = context,
                        progressBar = progressBar,
                        rootView = rootView
                    )
                }
            })
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
         * @var coroutineCallEmailTask is used for call the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * @var file limit is a constant which is equals to 2mb in byte format.
         * @var scanner is used for reading current desired file.
         * @var fileTemp is used for creating temp files which is used for holding the parts of original file which exceeds 2mb size.
         * @var withContext code block calls ui thread for using progressbar.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun sendDetailsAsEmail(
            file: File,
            context: Context,
            progressBar: ProgressBar? = null,
            rootView: ViewGroup? = null
        ) {
            workQueueLinked.controlRunnable = true
            coroutineCallEmailTask.async {
                try {
                    if (controlLogInit) {
                        withContext(Dispatchers.Main) {
                            if (defaultProgressBarView != null) {
                                defaultProgressBar.visibility = View.VISIBLE
                            } else {
                                progressBar?.let {
                                    it.visibility = View.VISIBLE
                                }.run {
                                    defaultProgressBarView = LayoutInflater.from(context)
                                        .inflate(R.layout.default_progressbar, rootView, true)
                                    defaultProgressBar = ProgressBar(context)
                                    defaultProgressBar =
                                        defaultProgressBarView!!.findViewById(R.id.progressBar)
                                }
                            }
                        }
                        if (progressBar != null) {
                            EmailUtil.sendEmail(
                                file = file,
                                context = context,
                                progressBar = progressBar,
                                workQueueLinked = workQueueLinked,
                                runnableList = runnableList
                            )
                        } else {
                            EmailUtil.sendEmail(
                                file = file,
                                context = context,
                                progressBar = defaultProgressBar,
                                workQueueLinked = workQueueLinked,
                                runnableList = runnableList
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    callExceptionDetails(exception = e, tag = Constants.emailTag)
                }
            }
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderComponent instance.
        internal fun returnStringBuilderComponent(): StringBuilder {
            return stringBuilderComponent
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderLifeCycle instance.
        internal fun returnStringBuilderLifecycle(): StringBuilder {
            return stringBuilderLifeCycle
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderFragmentManager instance.
        internal fun returnStringBuilderFragmentManager(): StringBuilder {
            return stringBuilderFragmentManager
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderAnalyticsManager instance.
        internal fun returnStringBuilderAnalyticsManager(): StringBuilder {
            return stringBuilderAnalyticsManager
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderHttp instance.
        internal fun returnStringBuilderHttp(): StringBuilder {
            return stringBuilderHttp
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderInAPurchase instance.
        internal fun returnStringBuilderInAPurchase(): StringBuilder {
            return stringBuilderInAPurchase
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderRetrofit instance.
        internal fun returnStringBuilderRetrofit(): StringBuilder {
            return stringBuilderRetrofit
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderRealm instance.
        internal fun returnStringBuilderRealm(): StringBuilder {
            return stringBuilderRealm
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderException instance.
        internal fun returnStringBuilderException(): StringBuilder {
            return stringBuilderException
        }

        //dummy method might be useful for future implementation and changes for taking stringBuilderAll instance.
        internal fun returnStringBuilderAll(): StringBuilder {
            return stringBuilderAll
        }
    }
}
