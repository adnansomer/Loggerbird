package services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.mobilex.loggerbird.R
import constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import utils.EmailUtil
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class LoggerBirdFutureTaskService : Service() {
    private lateinit var timerTask: TimerTask
    private val timer = Timer()
    private lateinit var arrayListFilePath:ArrayList<File>


    internal companion object {
        private val NOTIFICATION_CHANNEL_ID = "LoggerBirdForegroundFutureService"
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
        LoggerBirdService.controlFutureTask = true
        createNotificationChannel()
        Log.d("timer_future","im created!!")
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    private fun calculateFutureTime() {
        val coroutineCallFutureTask = CoroutineScope(Dispatchers.IO)
        coroutineCallFutureTask.async {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this@LoggerBirdFutureTaskService.applicationContext)
            if (System.currentTimeMillis() >= sharedPref.getLong("future_task_time", 0)) {
                try {
                    timer.cancel()
                    val filePath = File(sharedPref.getString("future_task_email_file", null)!!)
                    val to = sharedPref.getString("future_task_email_to",null)
                    val subject = sharedPref.getString("future_task_email_subject",null)
                    val message = sharedPref.getString("future_task_email_message",null)
                    EmailUtil.sendSingleEmail(
                        to = to!!,
                        context = this@LoggerBirdFutureTaskService,
//                            arrayListFilePaths = arrayListFilePath,
                        file = filePath,
                        message = message,
                        subject = subject
                    )
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
                .setSmallIcon(R.drawable.loggerbird_icon)
                .build()
            startForeground(5, notification)
            LoggerBirdService.callEnqueue()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBirdService.callEnqueue()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e , tag = Constants.futureTaskTag)
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


}