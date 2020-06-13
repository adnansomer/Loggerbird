package services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import java.io.File
import java.util.*

class LoggerBirdFutureTaskService : Service() {
    private val context = LoggerBird.context
    private lateinit var timerTask: TimerTask
    private val timer = Timer()
    private lateinit var emailSubject: String
    private lateinit var emailMessage: String
    private lateinit var emailFile: File
    private lateinit var emailTo: String


   internal companion object{
        internal lateinit var  loggerBirdFutureTaskService:LoggerBirdFutureTaskService
    }
    override fun onBind(intent: Intent?): IBinder? {
        loggerBirdFutureTaskService = this
        return null
    }

    override fun onCreate() {
        super.onCreate()
        emailTo = LoggerBirdService.loggerBirdService.getEmailTo()
        emailMessage = LoggerBirdService.loggerBirdService.getEmailMessage()
        emailSubject = LoggerBirdService.loggerBirdService.getEmailSubject()
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
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    private fun calculateFutureTime() {
        val coroutineCallFutureTask = CoroutineScope(Dispatchers.IO)
        coroutineCallFutureTask.async {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
            if (System.currentTimeMillis() >= sharedPref.getLong("future_task_time", 0)) {
                try {
                    timer.cancel()
                    val filePath = File(sharedPref.getString("future_file_path", null)!!)
                    LoggerBird.callEmailSender(
                        to = emailTo,
                        context = context,
                        file = filePath,
                        message = emailMessage,
                        subject = emailSubject
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


}