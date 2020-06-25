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
import utils.EmailUtil
import utils.LinkedBlockingQueueUtil
import java.io.File
import java.lang.Runnable
import java.util.*
import kotlin.collections.ArrayList

class LoggerBirdFutureTaskService : Service() {
    private lateinit var timerTask: TimerTask
    private val timer = Timer()

    internal companion object {
        private val NOTIFICATION_CHANNEL_ID = "LoggerBirdForegroundFutureService"
        internal var runnableListEmail: ArrayList<Runnable> = ArrayList()
        private var workQueueLinkedEmail: LinkedBlockingQueueUtil = LinkedBlockingQueueUtil()
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

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

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        Log.d("timer_future", "im created!!")
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    private fun calculateFutureTime() {

        coroutineCallFutureTask.async {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this@LoggerBirdFutureTaskService.applicationContext)
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

    private fun startLoggerBirdForegroundServiceFuture() {
        try {
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                .setContentTitle(resources.getString(R.string.future_task_notification_title))
                .setSmallIcon(R.drawable.loggerbird)
                .build()
            startForeground(5, notification)
            LoggerBirdService.callEnqueue()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBirdService.callEnqueue()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.futureTaskTag)
        }
    }

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

    private fun getFileList(context: Context): ArrayList<String>? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val gson = Gson()
        val json = sharedPref.getString("file_future_list", "")
        if (json?.isNotEmpty()!!) {
            return gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
        }
        return null
    }

    private fun getUserList(context: Context): ArrayList<String>? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val gson = Gson()
        val json = sharedPref.getString("user_future_list", "")
        if (json?.isNotEmpty()!!) {
            return gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
        }
        return null
    }

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