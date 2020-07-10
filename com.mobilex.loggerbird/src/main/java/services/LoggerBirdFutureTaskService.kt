package services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobilex.loggerbird.R
import constants.Constants
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import utils.email.EmailUtil
import utils.other.LinkedBlockingQueueUtil
import java.io.File
import java.lang.Runnable
import java.util.*
import kotlin.collections.ArrayList

/**
 * This class is a service for handling future task operations.
 */
internal class LoggerBirdFutureTaskService : Service() {
    private lateinit var timerTask: TimerTask
    private val timer = Timer()

    internal companion object {
        private val NOTIFICATION_CHANNEL_ID = "LoggerBirdForegroundFutureService"
        internal var runnableListEmail: ArrayList<Runnable> = ArrayList()
        private var workQueueLinkedEmail: LinkedBlockingQueueUtil =
            LinkedBlockingQueueUtil()
        private val coroutineCallFutureTask = CoroutineScope(Dispatchers.IO)
        internal fun callEnqueueEmail() {
            workQueueLinkedEmail.controlRunnable = false
            if (runnableListEmail.size > 0) {
                runnableListEmail.removeAt(0)
                if (runnableListEmail.size > 0) {
                    workQueueLinkedEmail.put(runnableListEmail[0])
                }
            }
        }
    }


    /**
     * This method called when service in onBind state.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * This Method Called When Service In onStartCommand state.
     * @return START_STICKY to stick into device as a service
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        Log.d("timer_future", "im created!!")
        return START_STICKY
    }

    /**
     * This method called when service in onCreate state of lifecycle.
     */
    override fun onCreate() {
        super.onCreate()
        timerTask = object : TimerTask() {
            override fun run() {
                Log.d("timer_executed", "timer_executed!")
                calculateFutureTime()
            }
        }
        timer.schedule(timerTask, 0, 60000)
    }

    /**
     * This method called when service in onDestroy state of lifecycle.
     */
    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * This method called when service in onTaskRemoved state.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    /**
     * This method is used for calculating the time that user selected as a future task.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun calculateFutureTime() {
        coroutineCallFutureTask.async {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this@LoggerBirdFutureTaskService.applicationContext)
            if (System.currentTimeMillis() >= sharedPref.getLong("future_task_time", 0)) {
                try {
                    val arrayListFilePath: ArrayList<File> = ArrayList()
                    timer.cancel()
                    getFileList(context = this@LoggerBirdFutureTaskService)?.forEach {
                        arrayListFilePath.add(File(it))
                    }
                    val subject = sharedPref.getString("future_task_email_subject", null)
                    val message = sharedPref.getString("future_task_email_message", null)
                    val arrayListUserList: ArrayList<String>? =
                        getUserList(context = this@LoggerBirdFutureTaskService)
                    if (arrayListUserList != null) {
                        if (arrayListUserList.isNotEmpty()) {
                            arrayListUserList.forEach {
                                addQueue(
                                    to = it,
                                    arrayListFilePath = arrayListFilePath,
//                        file = filePath,
                                    message = message,
                                    subject = subject
                                )
//                                callEmail(
//                                    to = it,
//                                    arrayListFilePath = arrayListFilePath,
////                        file = filePath,
//                                    message = message,
//                                    subject = subject
//                                )
                            }
                            runnableListEmail.forEach {
                                it.run()
                            }

                        } else {
                            val to = sharedPref.getString("future_task_email_to", null)
                            callEmail(
                                to = to!!,
                                arrayListFilePath = arrayListFilePath,
//                        file = filePath,
                                message = message,
                                subject = subject
                            )
                        }
                    } else {
                        val to = sharedPref.getString("future_task_email_to", null)
                        callEmail(
                            to = to!!,
                            arrayListFilePath = arrayListFilePath,
//                        file = filePath,
                            message = message,
                            subject = subject
                        )
                    }
//                    val filePath = File(sharedPref.getString("future_task_email_file", null)!!)


                    Log.d("future_task", "future_task_executed !!")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("timer_exception", e.printStackTrace().toString())
                }
//                stopSelf()
            }
        }
    }

    /**
     * This method is used for building notification while service is running.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun startLoggerBirdForegroundServiceFuture() {
        try {
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                .setContentTitle(resources.getString(R.string.future_task_notification_title))
                .setSmallIcon(R.drawable.loggerbird)
                .build()
            startForeground(5, notification)
            LoggerBirdService.callEnqueueVideo()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBirdService.callEnqueueVideo()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.futureTaskTag)
        }
    }

    /**
     * This method is used for creating notification channel for foreground service.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
            startLoggerBirdForegroundServiceFuture()
        }
    }

    /**
     * This method is used for getting files list of future task.
     * @param context is for getting reference from the application context.
     * @return list of future task attachments.
     */
    private fun getFileList(context: Context): ArrayList<String>? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val gson = Gson()
        val json = sharedPref.getString("file_future_list", "")
        if (json?.isNotEmpty()!!) {
            return gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
        }
        return null
    }

    /**
     * This method is used for getting user list of future task.
     * @param context is for getting reference from the application context.
     * @return list of future task attachments.
     */
    private fun getUserList(context: Context): ArrayList<String>? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val gson = Gson()
        val json = sharedPref.getString("user_future_list", "")
        if (json?.isNotEmpty()!!) {
            return gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
        }
        return null
    }

    /**
     * This method is used for calling email sender method.
     * @param to is used for getting the reference of email address that email action will send.
     * @param arrayListFilePath is used for getting list of media files that email action will send.
     * @param messsage is used for getting the reference of message that email action will send.
     * @param subject is used for getting the reference of subject that email action will send.
     * @return list of future task attachments.
     */
    private fun callEmail(
        to: String,
//        context: Context,
        arrayListFilePath: ArrayList<File>? = null,
        message: String? = null,
        subject: String? = null
//        controlService: Boolean
    ) {
        workQueueLinkedEmail.put {
            EmailUtil.sendSingleEmail(
                to = to,
                context = this@LoggerBirdFutureTaskService,
                arrayListFilePaths = arrayListFilePath,
//                        file = filePath,
                message = message,
                subject = subject,
                controlServiceTask = true
            )
        }

    }

    /**
     * This method is used for adding future tasks into queue.
     * @param to is used for getting the reference of email address that email action will send.
     * @param arrayListFilePath is used for getting list of media files that email action will send.
     * @param messsage is used for getting the reference of message that email action will send.
     * @param subject is used for getting the reference of subject that email action will send.
     * @return list of future task attachments.
     */
    private fun addQueue(
        to: String,
//        context: Context,
        arrayListFilePath: ArrayList<File>? = null,
        message: String? = null,
        subject: String? = null
//        controlService: Boolean
    ) {
        runnableListEmail.add(Runnable {
            EmailUtil.sendSingleEmail(
                to = to,
                context = this@LoggerBirdFutureTaskService,
                arrayListFilePaths = arrayListFilePath,
//                        file = filePath,
                message = message,
                subject = subject,
                controlServiceTask = true
            )
        })
    }
}