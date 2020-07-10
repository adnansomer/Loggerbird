package loggerbird

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.BatteryManager
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleObserver
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.google.gson.GsonBuilder
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import interceptors.LogOkHttpAuthenticationInterceptor
import interceptors.LogOkHttpCacheInterceptor
import interceptors.LogOkHttpErrorInterceptor
import interceptors.LogOkHttpInterceptor
import io.realm.Realm
import io.realm.RealmModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import listeners.recyclerViews.LogRecyclerViewChildAttachStateChangeListener
import listeners.recyclerViews.LogRecyclerViewItemTouchListener
import listeners.recyclerViews.LogRecyclerViewScrollListener
import observers.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import services.LoggerBirdMemoryService
import services.LoggerBirdService
import utils.other.DefaultToast
import utils.email.EmailUtil
import utils.other.InternetConnectionUtil
import utils.other.LinkedBlockingQueueUtil
import java.io.File
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


/**
 * Loggerbird class is the general logging class for this library.
 */
class LoggerBird : LifecycleObserver {

    companion object {
        private var controlLogInit: Boolean = false
        internal lateinit var context: Context
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
        private var stringBuilderOkHttp: StringBuilder = StringBuilder()
        internal var stringBuilderInterceptor: StringBuilder = StringBuilder()
        private var stringBuilderQuery: StringBuilder = StringBuilder()
        private var stringBuilderRealm: StringBuilder = StringBuilder()
        private var stringBuilderBuild: StringBuilder = StringBuilder()
        private var stringBuilderPerformance: StringBuilder = StringBuilder()
        private var stringBuilderMemoryUsage: StringBuilder = StringBuilder()
        private var stringBuilderCpu: StringBuilder = StringBuilder()
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
        private var coroutineCallOkHttpClient: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallRealm: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallMemoryUsageDetails = CoroutineScope(Dispatchers.IO)
        private var coroutineCallCpu = CoroutineScope(Dispatchers.IO)
        private var coroutineCallException: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallRetrofitTask: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var coroutineCallEmailTask = CoroutineScope(Dispatchers.IO)
        private var coroutineCallMemoryService: CoroutineScope = CoroutineScope(Dispatchers.IO)
        private var formattedTime: String? = null
        private var fileLimit: Long = 2097152
        internal lateinit var fragmentLifeCycleObserver: LogFragmentLifeCycleObserver
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
        private lateinit var textViewFileReader: TextView
        private lateinit var buttonFileReader: Button
        private var memoryOverused: Boolean = false
        private var runnableList: ArrayList<Runnable> = ArrayList()
        internal var uncaughtExceptionHandlerController = false
        private lateinit var defaultProgressBar: ProgressBar
        private var defaultProgressBarView: View? = null
        private var memoryThreshold: Long = 4180632L
        private lateinit var intentServiceMemory: Intent
        private lateinit var activityLifeCycleObserver: LogActivityLifeCycleObserver
        internal var stringBuilderActivityLifeCycleObserver: StringBuilder = StringBuilder()
        internal var classList: ArrayList<String> = ArrayList()
        internal lateinit var filePathSecessionName: File
        internal lateinit var jiraDomainName: String
        internal lateinit var jiraUserName: String
        internal lateinit var jiraApiToken: String
        internal lateinit var slackApiToken: String
        internal lateinit var githubUserName: String
        internal lateinit var githubPassword: String
        internal lateinit var trelloUserName: String
        internal lateinit var trelloPassword: String
        internal lateinit var trelloKey: String
        internal lateinit var trelloToken: String
        internal lateinit var gitlabApiToken: String
        internal lateinit var pivotalUserName: String
        internal lateinit var pivotalApiToken: String
        internal lateinit var basecampApiToken: String
        internal lateinit var asanaApiToken: String
        internal lateinit var clubhouseApiToken: String

        //---------------Public Methods:---------------//

        /**
         * Call This Method Before Calling Any Other Methods.
         * @param context is for getting reference from the application context , you must deploy this parameter.
         * @param filePathName allow user modify the file name they want to create for saving their details method , otherwise it will save your file to the devices data->data->your project package name->files->logger_bird_details with an default name of "logger_bird_details".
         * @return Boolean value.
         */
        fun logInit(
            context: Context,
            //jiraDomainName: String,
            //jiraUserName: String,
            //jiraApiToken: String,
            //slackApiToken: String,
            //githubUserName: String,
            //githubPassword: String,
            //gitlabApiToken: String,
            //trelloUserName: String,
            //trelloPassword: String,
            //trelloKey: String,
            //trelloToken: String,
            //pivotalApiToken: String,
            //basecampApiToken: String,
            //asanaApiToken: String,
            //clubhouseApiToken: String,
            filePathName: String? = null
        ): Boolean {
            this.context = context
            this.filePathName = filePathName
            if (!controlLogInit) {
                try {
                    //Companion.jiraDomainName = jiraDomainName
                    //Companion.jiraUserName = jiraUserName
                    //Companion.jiraApiToken = jiraApiToken
                    //Companion.slackApiToken = slackApiToken
                    //Companion.githubUserName = githubUserName
                    //Companion.githubPassword = githubPassword
                    //Companion.trelloUserName = trelloUserName
                    //Companion.trelloPassword = trelloPassword
                    //Companion.trelloKey = trelloKey
                    //Companion.trelloToken = trelloToken
                    //Companion.gitlabApiToken = gitlabApiToken
                    //Companion.pivotalApiToken = pivotalApiToken
                    //Companion.basecampApiToken = basecampApiToken
                    //Companion.clubhouseApiToken = clubhouseApiToken
                    //Companion.asanaApiToken = asanaApiToken
                    logAttachLifeCycleObservers(context = context)
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
                        saveDefaultFileDetails(filePath = filePath)
                    }
                    workQueueLinked = LinkedBlockingQueueUtil()
                    val logcatObserver = UnhandledExceptionObserver()
                    Thread.setDefaultUncaughtExceptionHandler(logcatObserver)
                    coroutineCallMemoryService.async {
                        intentServiceMemory = Intent(context, LoggerBirdMemoryService::class.java)
                        context.startService(intentServiceMemory)
                    }
                    filePathSecessionName = filePath

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            controlLogInit = true
            return controlLogInit
        }


        /**
         * This Method Attaches A LifeCycle Observer Or Fragment Observer For The Current Activity Or Fragment.
         * @param context is for getting reference from the application context , you must deploy this parameter.
         */
        private fun logAttachLifeCycleObservers(context: Context) {
            activityLifeCycleObserver =
                LogActivityLifeCycleObserver()
            (context as Application).registerActivityLifecycleCallbacks(activityLifeCycleObserver)
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
            if (Companion::activityLifeCycleObserver.isInitialized) {
                (context as Application).unregisterActivityLifecycleCallbacks(
                    activityLifeCycleObserver
                )
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
         * This Method used for invoking queue.
         */
        internal fun callEnqueue() {
            workQueueLinked.controlRunnable = false
            if (runnableList.size > 0) {
                runnableList.removeAt(0)
                if (runnableList.size > 0) {
                    workQueueLinked.put(runnableList[0])
                }
            }
        }

        /**
         * This Method adds takeCpuDetails into queue.
         * @throws exception if controlLogInit value is false.
         */
        fun callCpuDetails() {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeCpuDetails()
                    }
                }
                runnableList.add(Runnable {
                    takeCpuDetails()
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        /**
         * This Method adds takeMemoryUsageDetails into queue.
         * @param threshold takes threshold value to determine whether memory is overused.
         * @throws exception if controlLogInit value is false.
         */
        fun callMemoryUsageDetails(threshold: Long?) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeMemoryUsageDetails(threshold = threshold)
                    }
                }
                runnableList.add(Runnable {
                    takeMemoryUsageDetails(threshold = threshold)
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        /**
         * This Method adds takeComponentDetails into queue.
         * @param view parameter used for getting id of the component.
         * @param resources parameter used for getting component name with view parameter.
         * @throws exception if controlLogInit value is false.
         */
        fun callComponentDetails(view: View?, resources: Resources?) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeComponentDetails(
                            view = view,
                            resources = resources
                        )
                    }
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

        /**
         * This Method adds takeLifeCycle into queue.
         * @throws exception if controlLogInit value is false.
         */
        fun callLifeCycleDetails() {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put { takeLifeCycleDetails() }
                }
                runnableList.add(Runnable { takeLifeCycleDetails() })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }


        /**
         * This Method adds takeAnalyticsDetails into queue.
         * @param bundle parameter used for getting details from analytic bundle.
         * @throws exception if controlLogInit value is false.
         */
        fun callAnalyticsDetails(bundle: Bundle?) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeAnalyticsDetails(bundle = bundle)
                    }
                }
                runnableList.add(Runnable { takeAnalyticsDetails(bundle = bundle) })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }


        /**
         * This Method adds takeFragmentManagerDetails into queue.
         * @param fragmentManager parameter used for getting details from FragmentManager and printing all fragments in FragmentManager.
         * @throws exception if controlLogInit value is false.
         */
        fun callFragmentManagerDetails(fragmentManager: FragmentManager?) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeFragmentManagerDetails(fragmentManager = fragmentManager)
                    }
                }
                runnableList.add(Runnable { takeFragmentManagerDetails(fragmentManager = fragmentManager) })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)

            }
        }

        /**
         * This Method adds takeHttpRequestDetails into queue.
         * @param httpUrlConnection parameter used for getting details from HttpUrlConnection which is used for printing response code and response message.
         * @throws exception if controlLogInit value is false.
         */
        fun callHttpRequestDetails(httpUrlConnection: HttpURLConnection?) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeHttpRequestDetails(httpUrlConnection = httpUrlConnection)
                    }
                }
                runnableList.add(Runnable { takeHttpRequestDetails(httpUrlConnection = httpUrlConnection) })

            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        /**
         * This Method adds takeInAPurchaseDetails into queue.
         * @param billingClient parameter used for getting status of BillingClient.
         * @param billingResult parameter used for getting the response code and message of Billing flow .
         * @param skuDetailsParams parameter used for getting the skusList and sku type of Billing flow.
         * @param billingFlowParams parameter used for getting the details of the sku's in the Billing flow.
         * @param acknowledgePurchaseParams parameter used for getting the details developer payload and purchase token.
         * @throws exception if controlLogInit value is false.
         */
        fun callInAPurchase(
            billingClient: BillingClient? = null,
            billingResult: BillingResult? = null,
            skuDetailsParams: SkuDetailsParams? = null,
            billingFlowParams: BillingFlowParams? = null,
            acknowledgePurchaseParams: AcknowledgePurchaseParams? = null
        ) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeInAPurchaseDetails(
                            billingClient = billingClient,
                            billingResult = billingResult,
                            skuDetailsParams = skuDetailsParams,
                            billingFlowParams = billingFlowParams,
                            acknowledgePurchaseParams = acknowledgePurchaseParams
                        )
                    }
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

        /**
         * This Method adds takeRetrofitRequestDetails into queue.
         * @param okHttpClient parameter used for getting details from okHttp client.
         * @param okHttpRequest parameter used for getting details from okHttp request and get it's body,header,url and method values.
         * @param okHttpURLConnection parameter used for getting details from okHttpUrlConnection and get it's response code , error message , response message .
         * @throws exception if controlLogInit value is false.
         */
        fun callOkHttpRequestDetails(
            url: String? = null,
            okHttpURLConnection: HttpURLConnection? = null
        ) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeOkHttpDetails(
                            url = url,
                            okHttpURLConnection = okHttpURLConnection
                        )
                    }
                }
                runnableList.add(Runnable {
                    takeOkHttpDetails(
                        url = url,
                        okHttpURLConnection = okHttpURLConnection
                    )
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        /**
         * This Method adds takeRetrofitRequestDetails into queue.
         * @param retrofit parameter used for getting details from Retrofit which is used for getting base url of request.
         * @param response parameter used for getting details from Response which is used for getting response code ,response message , response success and response body.
         * @param request parameter used for getting details from Request which is used for getting request query and request method.
         * @throws exception if controlLogInit value is false.
         */
        fun callRetrofitRequestDetails(
            retrofit: Retrofit? = null,
            response: Response? = null,
            request: Request? = null
        ) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeRetrofitRequestDetails(
                            retrofit = retrofit,
                            response = response,
                            request = request
                        )
                    }
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

        /**
         * This Method adds takeRealmDetails into queue.
         * @param realm parameter used for getting details from Realm which is used for getting permissions,privileges and copy realm data.
         * @param realm model parameter used for getting details from RealmModel which is used for giving realm data to the Realm method which is copyFromRealm().
         * @throws exception if controlLogInit value is false.
         */
        fun callRealmDetails(realm: Realm? = null, realmModel: RealmModel? = null) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeRealmDetails(realm = realm, realmModel = realmModel)
                    }
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
                recyclerViewItemObserver = LogDataSetObserver(context = context)
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
         * //In progress method still need to be modified!
         * This Method adds takeExceptionDetails into queue.
         * @param exception parameter used for getting details from Exception class details
         * @param tag parameter used for getting details of which method caused this exception.
         * @param throwable used for getting details from Throwable class details.
         * @throws exception if controlLogInit value is false.
         */
        fun callExceptionDetails(
            exception: Exception? = null,
            tag: String? = null,
            throwable: Throwable? = null
        ) {
            if (runnableList.isEmpty()) {
                workQueueLinked.put {
                    takeExceptionDetails(exception = exception, tag = tag, throwable = throwable)
                }
            }
            runnableList.add(Runnable {
                takeExceptionDetails(
                    exception = exception,
                    tag = tag,
                    throwable = throwable
                )
            })
        }

        /**
         * //In progress method still need to be modified!
         * This Method adds sendDetailsAsEmail into queue.
         * @param file parameter used for getting file details for sending as email.
         * @param context parameter used for getting context of the current activity or fragment.
         * @param progressBar parameter used for getting custom progressbar that provided by method caller , if progressbar is null there will be default progressbar with default layout and you need to provide rootview in order to not get deneme.example.loggerbird.exception from default progressbar.
         * @param rootView parameter used for getting the current view of activity or fragment.
         * @throws exception if controlLogInit value is false.
         */
        fun callEmailSender(
            file: File? = null,
            context: Context,
            activity: Activity? = null,
            to: String,
            message: String? = null,
            subject: String? = null,
            progressBar: ProgressBar? = null,
            rootView: ViewGroup? = null
        ) {
            if (controlLogInit) {
                if (runnableList.isEmpty()) {
                    workQueueLinked.put(Runnable {
                        if (file != null) {
                            sendDetailsAsEmail(
                                file = file,
                                context = context,
                                activity = activity,
                                progressBar = progressBar,
                                rootView = rootView,
                                to = to,
                                message = message,
                                subject = subject
                            )
                        } else {
                            sendDetailsAsEmail(
                                file = this.filePath,
                                context = context,
                                activity = activity,
                                progressBar = progressBar,
                                rootView = rootView,
                                to = to,
                                message = message,
                                subject = subject
                            )
                        }
                    })
                }
                runnableList.add(Runnable {
                    if (file != null) {
                        sendDetailsAsEmail(
                            file = file,
                            context = context,
                            activity = activity,
                            progressBar = progressBar,
                            rootView = rootView,
                            to = to,
                            message = message,
                            subject = subject
                        )
                    } else {
                        sendDetailsAsEmail(
                            file = this.filePath,
                            context = context,
                            activity = activity,
                            progressBar = progressBar,
                            rootView = rootView,
                            to = to,
                            message = message,
                            subject = subject
                        )
                    }
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        /**
         * This method is used for deleting old files.
         * @param loggerBirdService is used for getting current instance reference of LoggerBirdService.
         */
        internal fun deleteOldFiles(loggerBirdService: LoggerBirdService) {
            if (controlLogInit) {
                val controlEmailAction = true
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        loggerBirdService.deleteOldFiles(controlEmailAction = controlEmailAction)
                    }
                }
                runnableList.add(Runnable {
                    loggerBirdService.deleteOldFiles(controlEmailAction = controlEmailAction)
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        /**
         * This method is used for deleting media file.
         * @param loggerBirdService is used for getting current instance reference of LoggerBirdService.
         * @param filePathMedia is used for getting the reference of current media file.
         */
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        internal fun deleteSingleMediaFile(
            loggerBirdService: LoggerBirdService,
            filePathMedia: File
        ) {
            if (controlLogInit) {
                val controlEmailAction = true
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        loggerBirdService.deleteSingleMediaFile(
                            controlEmailAction = controlEmailAction,
                            filePathMedia = filePathMedia
                        )
                    }
                }
                runnableList.add(Runnable {
                    loggerBirdService.deleteSingleMediaFile(
                        controlEmailAction = controlEmailAction,
                        filePathMedia = filePathMedia
                    )
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        /**
         * This Method Takes Device Information Details
         */
        private fun takeDeviceInformationDetails() {
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
            takeDevicePerformanceDetails()
        }

        /**This Method Takes Device Performance Details
         * @throws exception if error occurs then deneme.example.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeDevicePerformanceDetails() {
            try {
                val stringBuilderCpuAbi: StringBuilder = StringBuilder()
                val cpuAbi: String
                val memoryInfo = ActivityManager.MemoryInfo()
                val activityManager: ActivityManager =
                    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityManager.getMemoryInfo(memoryInfo)
                val runtime: Runtime = Runtime.getRuntime()
                val availableMemory = memoryInfo.availMem / 1048576L
                val totalMemory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    memoryInfo.totalMem / 1048576L
                } else {
                    null
                }
                val lowMemory = memoryInfo.lowMemory
                val runtimeMaxMemory = runtime.maxMemory() / 1048576L
                val runtimeTotalMemory = runtime.totalMemory() / 1048576L
                val runtimeFreeMemory = runtime.freeMemory() / 1048576L
                val availableProcessors = runtime.availableProcessors()
                val usedMemorySize = (runtimeTotalMemory - runtimeFreeMemory)
                cpuAbi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    for (cpuAbiItem in Build.SUPPORTED_ABIS.iterator()) {
                        stringBuilderCpuAbi.append(cpuAbiItem + "\n")
                    }
                    stringBuilderCpuAbi.toString()
                } else {
                    Build.CPU_ABI.toString()
                }
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
                stringBuilderBuild.append(
                    "Available Memory:$availableMemory MB\nTotal Memory:$totalMemory MB\nRuntime Max Memory: $runtimeMaxMemory MB \n" +
                            "Runtime Total Memory:$runtimeTotalMemory MB\nRuntime Free Memory:$runtimeFreeMemory MB\nLow Memory: ${lowMemory.toString().trim()}\nAvailable Processors:$availableProcessors\n"
                            + "Used Memory Size:$usedMemorySize MB\nCPU ABI:${cpuAbi.trim()}\nNetwork Usage(Send):$sendNetworkUsage Bytes\nNetwork Usage(Received):$receivedNetworkUsage Bytes\n"
                            + "Battery:${battery.toString().trim()}\n "
                )
            } catch (e: Exception) {
                e.printStackTrace()
                callEnqueue()
                callExceptionDetails(
                    exception = e,
                    tag = Constants.performanceTag
                )
            }
        }

        /**
         * This Method Takes CPU and Processor Details of Android Device
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeCpuDetails() {
            workQueueLinked.controlRunnable = true
            coroutineCallCpu.async {
                if (controlLogInit) {
                    try {
                        stringBuilderCpu = StringBuilder()
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)
                        stringBuilderCpu.append(formattedTime + ":" + Constants.cpuTag + "\n")
                        val fileCpuInfo = File("/proc/cpuinfo")
                        if (fileCpuInfo.exists()) {
                            fileCpuInfo.bufferedReader()
                                .forEachLine { stringBuilderCpu.append(it + "\n") }
                        } else {
                            stringBuilderCpu.append("Cpu Information directory not found")
                        }
                        saveCpuDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callEnqueue()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.cpuTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }
        }

        /**This Method Determines Whether Memory is Overused.
         * @param threshold takes threshold value to determine whether memory is overused.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeMemoryUsageDetails(threshold: Long?) {
            workQueueLinked.controlRunnable = true
            coroutineCallMemoryUsageDetails.async {
                if (controlLogInit) {
                    try {
                        stringBuilderMemoryUsage = StringBuilder()
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateTimeInstance()
                        formattedTime = formatter.format(date)
                        val runtime: Runtime = Runtime.getRuntime()
                        val runtimeTotalMemory = runtime.totalMemory()
                        val runtimeFreeMemory = runtime.freeMemory()
                        val usedMemorySize = (runtimeTotalMemory - runtimeFreeMemory)
                        if (threshold != null) {
                            memoryThreshold = threshold
                        }
                        if (usedMemorySize > memoryThreshold) {
                            memoryOverused = true
                            stringBuilderMemoryUsage.append("Memory Overused: $memoryOverused\nMemory Usage: $usedMemorySize Bytes\n")
                        }
                        saveMemoryUsageDetails()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        callEnqueue()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.memoryUsageTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }
        }


        /**
         * This Method Takes Component Details.
         * @param view parameter used for getting id of the component.
         * @param resources parameter used for getting component name with view parameter.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeComponentDetails(view: View?, resources: Resources?) {
            workQueueLinked.controlRunnable = true
            coroutineCallComponent.async {
                if (controlLogInit) {
                    try {
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
                        callEnqueue()
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

        /**
         * This Method Takes Life-Cycle Details.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        internal fun takeLifeCycleDetails() {
            workQueueLinked.controlRunnable = true
            if (LoggerBirdService.controlServiceOnDestroyState) {
                if (controlLogInit) {
                    try {
                        stringBuilderLifeCycle.append(Constants.lifeCycleTag + ":" + "\n")
                        if (Companion::fragmentLifeCycleObserver.isInitialized) {
                            if (fragmentLifeCycleObserver.returnFragmentLifeCycleState().isNotEmpty()) {
                                for (classList in fragmentLifeCycleObserver.returnClassList()) {
                                    stringBuilderLifeCycle.append("$classList:\n")
                                    for (stateList in fragmentLifeCycleObserver.returnFragmentLifeCycleState().split(
                                        "\n"
                                    )) {
                                        if (stateList.contains(classList)) {
                                            stringBuilderLifeCycle.append(stateList + "\n")
                                        }
                                    }
                                }
                            }
                        }
                        if (Companion::activityLifeCycleObserver.isInitialized) {
                            if (activityLifeCycleObserver.returnActivityLifeCycleState().isNotEmpty()) {
                                for (classList in activityLifeCycleObserver.returnClassList()) {
                                    stringBuilderLifeCycle.append("$classList:\n")
                                    for (stateList in activityLifeCycleObserver.returnActivityLifeCycleState().split(
                                        "\n"
                                    )) {
                                        if (stateList.contains(classList)) {
                                            stringBuilderLifeCycle.append(stateList + "\n")
                                        }
                                    }
                                }
                            }
                        }
//                        stringBuilderLifeCycle.append(LoggerBirdService.onDestroyMessage)
                        saveLifeCycleDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callEnqueue()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.activityTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            } else {
                coroutineCallLifeCycle.async {
                    if (controlLogInit) {
                        try {
                            stringBuilderLifeCycle = StringBuilder()
                            stringBuilderLifeCycle.append(Constants.lifeCycleTag + ":" + "\n")
                            if (Companion::fragmentLifeCycleObserver.isInitialized) {
                                if (fragmentLifeCycleObserver.returnFragmentLifeCycleState().isNotEmpty()) {
                                    for (classList in fragmentLifeCycleObserver.returnClassList()) {
                                        stringBuilderLifeCycle.append("$classList:\n")
                                        for (stateList in fragmentLifeCycleObserver.returnFragmentLifeCycleState().split(
                                            "\n"
                                        )) {
                                            if (stateList.contains(classList)) {
                                                stringBuilderLifeCycle.append(stateList + "\n")
                                            }
                                        }
                                    }
                                }
                            }
                            if (Companion::activityLifeCycleObserver.isInitialized) {
                                if (activityLifeCycleObserver.returnActivityLifeCycleState().isNotEmpty()) {
                                    for (classList in activityLifeCycleObserver.returnClassList()) {
                                        stringBuilderLifeCycle.append("$classList:\n")
                                        for (stateList in activityLifeCycleObserver.returnActivityLifeCycleState().split(
                                            "\n"
                                        )) {
                                            if (stateList.contains(classList)) {
                                                stringBuilderLifeCycle.append(stateList + "\n")
                                            }
                                        }
                                    }
                                }
                            }
                            saveLifeCycleDetails()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            callEnqueue()
                            callExceptionDetails(
                                exception = e,
                                tag = Constants.activityTag
                            )
                        }

                    } else {
                        throw LoggerBirdException(Constants.logInitErrorMessage)
                    }
                }
            }
        }

        /**
         * This Method Takes Analytics Details.
         * @param bundle parameter used for getting details from analytic bundle.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                        callEnqueue()
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
         * @param fragmentManager parameter used for getting details from FragmentManager and printing all fragments in FragmentManager.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                        callEnqueue()
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
         * @param httpUrlConnection parameter used for getting details from HttpUrlConnection which is used for printing response code and response message.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                        callEnqueue()
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
         * @param billingClient parameter used for getting status of BillingClient.
         * @param billingResult parameter used for getting the response code and message of Billing flow .
         * @param skuDetailsParams parameter used for getting the skusList and sku type of Billing flow.
         * @param billingFlowParams parameter used for getting the details of the sku's in the Billing flow.
         * @param acknowledgePurchaseParams parameter used for getting the details developer payload and purchase token.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                        val prettyJson: String =
                            gson.toJson(billingFlowParams?.skuDetails);
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
                                1 -> responseMessage =
                                    "User pressed back or canceled a dialog"
                                2 -> responseMessage =
                                    "Network connection is down"
                                3 -> responseMessage =
                                    "The Google Play Billing AIDL version is not supported for the type requested"
                                4 -> responseMessage =
                                    "Requested product is not available for purchase"
                                5 -> responseMessage =
                                    "Invalid arguments provided to the API.This error can also indicate that the application was not correctly signed or properly \n set up for Google Play Billing , or does not have the neccessary permissions in the manifest"
                                6 -> responseMessage =
                                    "Fatal error during the API action"
                                7 -> responseMessage =
                                    "Failure to purchase since item is already owned"
                                8 -> responseMessage =
                                    "Failure to consume since item is not owned"
                            }
                        }
                        stringBuilderInAPurchase.append(
                            "\n" + formattedTime + ":" + Constants.inAPurchaseTag +
                                    "\n" + "Billing Flow Item Consumed:" + billingFlowParams?.skuDetails?.isRewarded +
                                    "\n" + "Billing Response Code:" + billingResult?.responseCode +
                                    "\n" + "Billing Response Message:" + responseMessage +
                                    "\n" + "Billing Client Is Ready:" + billingClient?.isReady +
                                    "\n" + "Sku Type:" + skuDetailsParams?.skuType +
                                    "\n" + "Sku List:" + stringBuilderSkuDetailList.toString() +
                                    "\n" + "Billing Flow Sku Details:" + prettyJson +
                                    "\n" + "Billing Flow Sku:" + billingFlowParams?.sku +
                                    "\n" + "Billing Flow Account Id:" + billingFlowParams?.accountId +
                                    "\n" + "Billing Flow Developer Id:" + billingFlowParams?.developerId +
                                    "\n" + "Billing flow Old Sku:" + billingFlowParams?.oldSku +
                                    "\n" + "Billing Flow Old Sku Purchase Token:" + billingFlowParams?.oldSkuPurchaseToken +
                                    "\n" + "Acknowledge Params:" + acknowledgePurchaseParams?.developerPayload +
                                    "\n" + "Acknowledge Purchase Token:" + acknowledgePurchaseParams?.purchaseToken
                        )
                        saveInAPurchaseDetails()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callEnqueue()
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
         * @param retrofit parameter used for getting details from Retrofit which is used for getting base url of request.
         * @param response parameter used for getting details from Response which is used for getting response code ,response message , response success and response body.
         * @param request parameter used for getting details from Request which is used for getting request query and request method.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                                    ) + "," + request.url.queryParameterValue(
                                        parameterQueryCounter
                                    ) + "\n"
                                )
                                parameterQueryCounter++
                            }
                        }
                        withContext(Dispatchers.IO) {
                            coroutineCallRetrofitTask.async {
                                withContext(Dispatchers.IO) {
                                    stringBuilderRetrofit.append("\n" + formattedTime + ":" + Constants.retrofitTag + "\n" + "Retrofit Request Code:" + response?.code + " " + "Response Message:" + response?.message + "\n" + "Retrofit Url:" + retrofit?.baseUrl() + " " + "Request Url:" + request?.url + "\n" + "Response Success:" + response?.isSuccessful + "\n" + "Request Method:" + request?.method + "\n" + stringBuilderQuery.toString() + "Response Value:" + response?.body?.string())
                                    saveRetrofitRequestDetails()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callEnqueue()
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


        /**
         * This Method Takes OkHttp Request Details.
         * @param url parameter used for getting reference of url used in okhttp request.
         * @param okHttpURLConnection parameter used for getting details from okHttpUrlConnection and get it's response code , error message , response message.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws exception if logInit method return value is false.
         */
        private fun takeOkHttpDetails(
            url: String? = null,
            okHttpURLConnection: HttpURLConnection? = null
        ) {
            workQueueLinked.controlRunnable = true
            coroutineCallOkHttpClient.async {
                if (controlLogInit) {
                    try {
                        stringBuilderOkHttp = StringBuilder()
                        val date = Calendar.getInstance().time
                        val formatter = SimpleDateFormat.getDateInstance()
                        formattedTime = formatter.format(date)
                        val okHttpClient: OkHttpClient? = loggerBirdInterceptorClient()
                        var okHttpRequest: Request? = null
                        val fromBodyBuilder = FormBody.Builder()
                        if (url != null) {
                            okHttpRequest = Request.Builder()
                                .url(url)
                                .post(fromBodyBuilder.build())
                                .build()
                        }
                        val okHttpClientInterceptors = okHttpClient?.interceptors
                        val okHttpClientTimeOut = okHttpClient?.connectTimeoutMillis
                        val okHttpRequestHeaders = okHttpRequest?.headers
                        val okHttpRequestUrl = okHttpRequest?.url
                        val okHttpRequestMethod = okHttpRequest?.method
                        val okHttpConnectionResponseCode = okHttpURLConnection?.responseCode
                        val okHttpConnectionError = okHttpURLConnection?.errorStream
                        val okHttpConnectionResponse = okHttpURLConnection?.responseMessage
                        var okHttpResponse: Response? = null
                        withContext(Dispatchers.IO) {
                            if (okHttpRequest != null) {
                                okHttpResponse = okHttpClient?.newCall(okHttpRequest)?.execute()
                            }
                            stringBuilderOkHttp.append(
                                "\n" + formattedTime + " " + Constants.okHttpTag + "\n"
                                        + "okHttp Client Interceptors: $okHttpClientInterceptors \n"
                                        + stringBuilderInterceptor.toString() + "\n"
                                        + "okHttp Client Connection Time Out: $okHttpClientTimeOut \n"
                                        + "okHttp Request Headers: $okHttpRequestHeaders \n"
                                        + "okHttp Request Url: $okHttpRequestUrl \n"
                                        + "okHttp Request Method: $okHttpRequestMethod \n"
                                        + "okHttp Connection Response Code: $okHttpConnectionResponseCode \n"
                                        + "okHttp Connection Error: $okHttpConnectionError \n"
                                        + "okHttp Connection Response: $okHttpConnectionResponse \n"
                                        + "okHttp Response Body:${okHttpResponse?.body?.string()} \n"
                            )
                            saveOkHttpRequestDetails()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callEnqueue()
                        callExceptionDetails(
                            exception = e,
                            tag = Constants.okHttpTag
                        )
                    }
                } else {
                    throw LoggerBirdException(Constants.logInitErrorMessage)
                }
            }
        }

        /**
         * This method creates a HttpClient as an OkHttp Client to Intercept Retrofit Logs
         * @throws exception if logInit method return value is false
         */
        fun loggerBirdInterceptorClient(): OkHttpClient? {
            var loggerBirdHttpClient: OkHttpClient? = null
            if (controlLogInit) {
                try {
                    val internetConnectionUtil =
                        InternetConnectionUtil()
                    if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                        val loggerBirdGeneralInterceptor = HttpLoggingInterceptor()
                        loggerBirdGeneralInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                        loggerBirdHttpClient = OkHttpClient().newBuilder()
                            .addInterceptor(LogOkHttpInterceptor())
                            .addInterceptor(LogOkHttpErrorInterceptor())
                            .addInterceptor(LogOkHttpAuthenticationInterceptor())
                            .addNetworkInterceptor(LogOkHttpCacheInterceptor())
                            .addInterceptor(loggerBirdGeneralInterceptor)
                            .build()

                    } else {
                        throw LoggerBirdException(
                            Constants.networkErrorMessage
                        )
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(exception = e, tag = Constants.okHttpTag)
                }
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
            return loggerBirdHttpClient
        }

        /**
         * This Method Takes Realm Details.
         * @param realm parameter used for getting details from Realm which is used for getting permissions,privileges and copy realm data.
         * @param realm model parameter used for getting details from RealmModel which is used for giving realm data to the Realm method which is copyFromRealm().
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                        callEnqueue()
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
         * @param exception parameter used for getting details from Exception class details
         * @param tag parameter used for getting details of which method caused this exception.
         * @param throwable used for getting details from Throwable class details.
         * @throws exception if logInit method return value is false.
         */
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        private fun takeExceptionDetails(
            exception: Exception? = null,
            tag: String? = null,
            throwable: Throwable? = null
        ) {
            workQueueLinked.controlRunnable = true
            try {
                if (!uncaughtExceptionHandlerController) {
                    coroutineCallException.async {
                        stringBuilderException = StringBuilder()
                        val date = Calendar.getInstance().time
                        val formatter =
                            SimpleDateFormat.getDateTimeInstance()
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
                } else {
                    stringBuilderException = StringBuilder()
                    val date = Calendar.getInstance().time
                    val formatter =
                        SimpleDateFormat.getDateTimeInstance()
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
                        LoggerBirdService.loggerBirdService.addUnhandledExceptionMessage(
                            context = context,
                            unhandledExceptionMessage = exception.stackTrace[0].className
                        )
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
                        LoggerBirdService.loggerBirdService.addUnhandledExceptionMessage(
                            context = context,
                            unhandledExceptionMessage = throwable.stackTrace[0].className
                        )
                    }
                    saveExceptionDetails()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * This Method Sends Desired File As Email.
         * @param file parameter used for getting file details for sending as email.
         * @param context parameter used for getting context of the current activity or fragment.
         * @param progressBar parameter used for getting custom progressbar that provided by method caller , if progressbar is null there will be default progressbar with default layout and you need to provide rootview in order to not get deneme.example.loggerbird.exception from default progressbar.
         * @param rootView parameter used for getting the current view of activity or fragment.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun sendDetailsAsEmail(
            file: File,
            context: Context,
            activity: Activity? = null,
            to: String,
            subject: String? = null,
            message: String? = null,
            progressBar: ProgressBar? = null,
            rootView: ViewGroup? = null
        ) {
            workQueueLinked.controlRunnable = true
            coroutineCallEmailTask.async {
                try {
                    if (controlLogInit) {
                        withContext(Dispatchers.Main) {
                            if (defaultProgressBarView != null) {
                                defaultProgressBar.visibility =
                                    View.VISIBLE
                            } else {
                                progressBar?.let {
                                    it.visibility = View.VISIBLE
                                }.run {
                                    defaultProgressBarView =
                                        LayoutInflater.from(context)
                                            .inflate(
                                                R.layout.default_progressbar,
                                                rootView,
                                                true
                                            )
                                    defaultProgressBar =
                                        ProgressBar(context)
                                    defaultProgressBar =
                                        defaultProgressBarView!!.findViewById(
                                            R.id.progressBar
                                        )
                                }
                            }
                        }
                        if (progressBar != null) {
                            EmailUtil.sendEmail(
                                file = file,
                                context = context,
                                activity = activity,
                                progressBar = progressBar,
                                to = to,
                                message = message,
                                subject = subject
                            )
                        } else {
                            EmailUtil.sendEmail(
                                file = file,
                                context = context,
                                activity = activity,
                                progressBar = defaultProgressBar,
                                to = to,
                                message = message,
                                subject = subject
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.emailTag
                    )
                }
            }
        }

        //dummy method probably removed.
        private fun attachRootView(rootView: ViewGroup) {
            val view: View = LayoutInflater.from(context)
                .inflate(
                    R.layout.default_file_text_reader,
                    rootView,
                    true
                )
            textViewFileReader =
                view.findViewById(R.id.textView_file_reader)
            buttonFileReader =
                view.findViewById(R.id.button_file_reader)
            textViewFileReader.movementMethod =
                ScrollingMovementMethod()
            when {
                stringBuilderComponent.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderComponent
                stringBuilderLifeCycle.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderLifeCycle
                stringBuilderFragmentManager.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderFragmentManager
                stringBuilderAnalyticsManager.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderAnalyticsManager
                stringBuilderHttp.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderHttp
                stringBuilderInAPurchase.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderInAPurchase
                stringBuilderRetrofit.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderRetrofit
                stringBuilderQuery.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderQuery
                stringBuilderRealm.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderRealm
                stringBuilderException.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderException
                stringBuilderAll.isNotEmpty() -> textViewFileReader.text =
                    stringBuilderAll
            }
            buttonFileReader.setOnClickListener {
                //                rootView.removeView(view.rootView)
            }
        }

        /**
         * This Method used for when a saving method exceed's 2mb file limit and delete's old entries at the start and add's new entries to the end of the file.
         * @param stringBuilder is used for getting the reference of stringBuilder of called saving method.
         * @param file is used for getting the reference of stringBuilder of called saving method.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                    File(
                        file.toString().substringBeforeLast("/"),
                        "logger_bird_details_temp.txt"
                    )
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
                callEnqueue()
                callExceptionDetails(
                    exception = e,
                    tag = Constants.exceedFileLimitTag
                )
            }
            stringBuilderExceedFileWriterLimit = StringBuilder()
        }

        /**
         * This method saves default file details into the file.
         *  @param filePath takes absolute path of given file.
         */
        private fun saveDefaultFileDetails(filePath: File) {
            if (!filePath.exists()) {
                filePath.createNewFile()
                takeDeviceInformationDetails()
                filePath.appendText(
                    stringBuilderBuild.toString()
                )
            }
        }


        /**
         * This Method Saves Component Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(stringBuilderComponent.toString())
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderComponent.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderComponent.toString()
                            )
                        }
                    }
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.componentTag
                    )
                }
            }
        }


        /**
         * This Method Saves Life-Cycle Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveLifeCycleDetails() {
            if (stringBuilderLifeCycle.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(
                            stringBuilderLifeCycle.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderLifeCycle.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderLifeCycle.toString()
                            )
                        }
                    }
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                    if (LoggerBirdService.controlServiceOnDestroyState) {
                        saveSessionIntoOldSessionFile()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.activityTag
                    )
                }
            }
        }

        /**
         * This Method Saves FragmentManager Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveFragmentManagerDetails() {
            if (stringBuilderFragmentManager.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
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
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.fragmentManagerTag
                    )
                }
            }
        }

        /**
         * This Method Saves Analytics Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveAnalyticsDetails() {
            if (stringBuilderAnalyticsManager.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    if (filePathName != null) {
                        filePath = File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        filePath = File(
                            fileDirectory, "logger_bird.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
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
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.analyticsTag
                    )
                }

            }
        }

        /**
         * This Method Saves HttpRequest Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveHttpRequestDetails() {
            if (stringBuilderHttp.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(
                            stringBuilderHttp.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderHttp.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderHttp.toString()
                            )
                        }
                    }
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.httpTag
                    )
                }
            }
        }

        /**
         * This Method Saves Android In A Purchase Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveInAPurchaseDetails() {
            if (stringBuilderInAPurchase.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(
                            stringBuilderInAPurchase.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderInAPurchase.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderInAPurchase.toString()
                            )
                        }
                    }
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.inAPurchaseTag
                    )
                }
            }
        }

        /**
         * This Method Saves OkHttp Request Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveOkHttpRequestDetails() {
            if (stringBuilderOkHttp.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(
                            stringBuilderOkHttp.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderOkHttp.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderOkHttp.toString()
                            )
                        }
                    }
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.okHttpTag
                    )
                }
            }
        }

        /**
         * This Method Saves Retrofit Request Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveRetrofitRequestDetails() {
            if (stringBuilderRetrofit.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(
                            stringBuilderRetrofit.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderRetrofit.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderRetrofit.toString()
                            )
                        }
                    }
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.retrofitTag
                    )
                }
            }
        }

        /**
         * This Method Saves Realm Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveRealmDetails() {
            if (stringBuilderRealm.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(
                            stringBuilderRealm.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderRealm.toString()
                            )
                        }
                    }
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(
                        exception = e,
                        tag = Constants.realmTag
                    )
                }
            }
        }

        /**
         * This Method Saves Performance Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveMemoryUsageDetails(
        ) {
            if (stringBuilderMemoryUsage.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(
                            stringBuilderMemoryUsage.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderMemoryUsage.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderMemoryUsage.toString()
                            )
                        }
                    }
                    if (!memoryOverused) {
                        callEnqueue()
                        if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                            exceededFileLimitWriter(
                                stringBuilder = stringBuilderExceedFileWriterLimit,
                                file = filePath
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(exception = e, tag = Constants.memoryUsageTag)
                }
            }
        }


        /**
         * This Method Saves Cpu Details To Txt File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveCpuDetails() {
            if (stringBuilderCpu.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    filePath = if (filePathName != null) {
                        File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(
                            stringBuilderCpu.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderCpu.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderCpu.toString()
                            )
                        }
                    }
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    callExceptionDetails(exception = e, tag = Constants.cpuTag)
                }
            }
        }

        /**
         * This Method Saves Exception Details To Txt File.
         * @throws exception if error occurs and prints error into logcat.
         */
        private fun saveExceptionDetails() {
            if (stringBuilderException.isNotEmpty()) {
                try {
                    fileDirectory = context.filesDir
                    if (filePathName != null) {
                        filePath = File(
                            fileDirectory,
                            "$filePathName.txt"
                        )
                    } else {
                        filePath = File(
                            fileDirectory,
                            "logger_bird_details.txt"
                        )
                    }
                    if (!filePath.exists()) {
                        filePath.createNewFile()
                        takeDeviceInformationDetails()
                        filePath.appendText(
                            stringBuilderBuild.toString()
                        )
                        filePath.appendText(
                            stringBuilderException.toString()
                        )
                    } else {
                        if (filePath.length() > fileLimit) {
                            stringBuilderExceedFileWriterLimit.append(
                                stringBuilderException.toString()
                            )
                        } else {
                            filePath.appendText(
                                stringBuilderException.toString()
                            )
                        }
                    }
                    callEnqueue()
                    if (runnableList.size == 0 && stringBuilderExceedFileWriterLimit.isNotEmpty()) {
                        exceededFileLimitWriter(
                            stringBuilder = stringBuilderExceedFileWriterLimit,
                            file = filePath
                        )
                    }
                    if (uncaughtExceptionHandlerController) {
                        saveSessionIntoOldSessionFile()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * This Method Saves Details File Into Old Session File.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun saveSessionIntoOldSessionFile() {
            try {
                val scannerOldSessionFile = Scanner(filePath)
                val oldSessionFile = if (filePathName != null) {
                    File(
                        filePath.path.substringBeforeLast("/"),
                        filePathName + "_old_session.txt"
                    )
                } else {
                    File(
                        filePath.path.substringBeforeLast("/"),
                        "logger_bird_details_old_session.txt"
                    )
                }
                if (oldSessionFile.exists()) {
                    oldSessionFile.delete()
                    oldSessionFile.createNewFile()
                } else {
                    oldSessionFile.createNewFile()
                }
                do {
                    oldSessionFile.appendText(
                        scannerOldSessionFile.nextLine() + "\n"
                    )
                } while (scannerOldSessionFile.hasNextLine())
                oldSessionFile.appendText(LoggerBirdMemoryService.stringBuilderMemoryUsage.toString())
                filePath.delete()
                filePath = oldSessionFile
                if (uncaughtExceptionHandlerController) {
                    val sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                            ?: return
                    with(sharedPref.edit()) {
                        putString("unhandled_file_path", filePath.absolutePath)
                        commit()
                    }
                } else {
                    val sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                    val editor: SharedPreferences.Editor = sharedPref.edit()
                    editor.remove("unhandled_file_path")
                    editor.commit()
                }

                android.os.Process.killProcess(android.os.Process.myPid());
                exitProcess(0);
            } catch (e: Exception) {
                e.printStackTrace()
                callEnqueue()
                callExceptionDetails(
                    exception = e,
                    tag = Constants.saveSessionOldFileTag
                )
            }
        }

        /**
         * This method returns activity result in order to given permission.
         * @param requestCode takes request code from relevant permission.
         * @param resultCode takes result code from relevant permission.
         * @param data takes permission intent.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails ,
         * which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        @RequiresApi(Build.VERSION_CODES.M)
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (controlLogInit) {
                try {
                    if (!LoggerBirdService.controlVideoPermission && !LoggerBirdService.controlDrawableSettingsPermission && !LoggerBirdService.controlAudioPermission) {
                        LoggerBirdService.controlPermissionRequest = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (resultCode == Activity.RESULT_OK && data != null) {
                                LoggerBirdService.loggerBirdService.callVideoRecording(
                                    requestCode = requestCode,
                                    resultCode = resultCode,
                                    data = data
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    R.string.permission_denied,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        } else {
                            throw LoggerBirdException(Constants.videoRecordingSdkTag + "current min is:" + Build.VERSION.SDK_INT)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LoggerBirdService.callEnqueueVideo()
                    callEnqueue()
                    callExceptionDetails(exception = e, tag = Constants.onActivityResultTag)
                }
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        /**
         * This method is used for taking given permission result.
         * @param requestCode is used for taking request code from relevant permission.
         * @param permissions is used for taking permissions that are wanted from user
         * @param grantResults is used for granted permission results.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails ,
         * which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        fun onRequestPermissionResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            if (controlLogInit) {
                try {
                    var permissionCounter = 0
                    LoggerBirdService.controlPermissionRequest = false
                    do {
                        if (permissions[permissionCounter] == "android.permission.WRITE_EXTERNAL_STORAGE" || permissions[permissionCounter] == "android.permission.RECORD_AUDIO") {
                            if (grantResults[0] == 0) {
                                Toast.makeText(
                                    context,
                                    R.string.permission_granted,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                Toast.makeText(
                                    context,
                                    R.string.permission_denied,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                        }
                        permissionCounter++
                    } while (permissions.iterator().hasNext())
                } catch (e: Exception) {
                    e.printStackTrace()
                    LoggerBirdService.callEnqueueVideo()
                    callEnqueue()
                    callExceptionDetails(exception = e, tag = Constants.onPermissionResultTag)
                }
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        }

        /**
         * This method is used for returning whether Clubhouse token is initialized.
         * @return true if it initialized.
         */
        internal fun clubhouseIsInitialized(): Boolean {
            if (this::clubhouseApiToken.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for returning whether Gitlab token is initialized.
         * @return true if it initialized.
         */
        internal fun gitlabIsInitialized(): Boolean {
            if (this::gitlabApiToken.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for returning whether Slack token is initialized.
         * @return true if it initialized.
         */
        internal fun slackIsInitialized(): Boolean {
            if (this::slackApiToken.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for returning whether Pivotal token is initialized.
         * @return true if it initialized.
         */
        internal fun pivotalIsInitialized(): Boolean {
            if (this::pivotalApiToken.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for returning whether Github token is initialized.
         * @return true if it initialized.
         */
        internal fun githubIsInitialized(): Boolean {
            if (this::githubUserName.isInitialized && this::githubPassword.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for returning whether Asana token is initialized.
         * @return true if it initialized.
         */
        internal fun asanaIsInitialized(): Boolean {
            if (this::asanaApiToken.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for returning whether Jira token is initialized.
         * @return true if it initialized.
         */
        internal fun jiraIsInitialized(): Boolean {
            if (this::jiraApiToken.isInitialized && this::jiraDomainName.isInitialized && this::jiraUserName.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for returning whether Basecamp token is initialized.
         * @return true if it initialized.
         */
        internal fun basecampIsInitialized(): Boolean {
            if (this::basecampApiToken.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for returning whether Trello token is initialized.
         * @return true if it initialized.
         */
        internal fun trelloIsInitialized(): Boolean {
            if (this::trelloUserName.isInitialized && this::trelloPassword.isInitialized) {
                return true
            }
            return false
        }

    }

    /**
     * LoggerbirdIntegration class is a builder class for calling third party integrations in this pattern.
     */
    class LoggerBirdIntegration(
        val clubhouseApiToken: String?,
        val slackApiToken: String?,
        val gitlabApiToken: String?,
        val githubUserName: String?,
        val githubPassword: String?,
        val asanaApiToken: String?,
        val basecampApiToken: String?,
        val pivotalApiToken: String?,
        val trelloUserName: String?,
        val trelloPassword: String?,
        val trelloKey: String?,
        val trelloToken: String?,
        val jiraDomainName: String?,
        val jiraUserName: String?,
        val jiraApiToken: String?
    ) {
        data class Builder(
            private var clubhouseApiToken: String? = null,
            private var slackApiToken: String? = null,
            private var gitlabApiToken: String? = null,
            private var githubUserName: String? = null,
            private var githubPassword: String? = null,
            private var asanaApiToken: String? = null,
            private var basecampApiToken: String? = null,
            private var pivotalApiToken: String? = null,
            private var trelloUserName: String? = null,
            private var trelloPassword: String? = null,
            private var trelloKey: String? = null,
            private var trelloToken: String? = null,
            private var jiraDomainName: String? = null,
            private var jiraUserName: String? = null,
            private var jiraApiToken: String? = null
        ) {
            fun setClubhouseIntegration(clubhouseApiToken: String) =
                apply { LoggerBird.clubhouseApiToken = clubhouseApiToken }

            fun setSlackIntegration(slackApiToken: String) =
                apply { LoggerBird.slackApiToken = slackApiToken }

            fun setGitlabIntegration(gitlabApiToken: String) =
                apply { LoggerBird.gitlabApiToken = gitlabApiToken }

            fun setGithubIntegration(githubUserName: String, githubPassword: String) = apply {
                LoggerBird.githubUserName = githubUserName;LoggerBird.githubPassword =
                githubPassword
            }

            fun setAsanaIntegration(asanaApiToken: String) =
                apply { LoggerBird.asanaApiToken = asanaApiToken }

            fun setBasecampIntegration(basecampApiToken: String) =
                apply { LoggerBird.basecampApiToken = basecampApiToken }

            fun setPivotalIntegraton(pivotalApiToken: String) =
                apply { LoggerBird.pivotalApiToken = pivotalApiToken }

            fun setTrelloIntegration(
                trelloUserName: String,
                trelloPassword: String,
                trelloKey: String,
                trelloToken: String
            ) = apply {
                LoggerBird.trelloUserName = trelloUserName;LoggerBird.trelloPassword =
                trelloPassword; LoggerBird.trelloKey = trelloKey; LoggerBird.trelloToken =
                trelloToken
            }

            fun setJiraIntegration(
                jiraDomainName: String,
                jiraUserName: String,
                jiraApiToken: String
            ) = apply {
                LoggerBird.jiraDomainName = jiraDomainName;LoggerBird.jiraUserName =
                jiraUserName; LoggerBird.jiraApiToken = jiraApiToken
            }

            fun build() = LoggerBirdIntegration(
                clubhouseApiToken,
                slackApiToken,
                gitlabApiToken,
                githubUserName,
                githubPassword,
                asanaApiToken,
                basecampApiToken,
                pivotalApiToken,
                trelloUserName,
                trelloPassword,
                trelloKey,
                trelloToken,
                jiraDomainName,
                jiraUserName,
                jiraApiToken
            )
        }
    }
}
