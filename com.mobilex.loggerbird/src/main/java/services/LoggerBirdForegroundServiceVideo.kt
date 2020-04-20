package services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mobilex.loggerbird.R
import constants.Constants
import loggerbird.LoggerBird
import observers.LogActivityLifeCycleObserver

class LoggerBirdForegroundServiceVideo : Service() {
    companion object {
        val NOTIFICATION_CHANNEL_ID = "LoggerBirdForegroundService"
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            createNotificationChannel()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBirdService.callEnqueue()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.foregroundServiceVideo)
        }
        return START_NOT_STICKY
    }

    private fun startLoggerBirdForegroundServiceVideo() {
        try {
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                .setContentTitle("LoggerBird screen recording is running.")
                .setSmallIcon(R.drawable.loggerbird_icon)
                .build()
            startForeground(5, notification)
            LoggerBirdService.callEnqueue()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBirdService.callEnqueue()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e , tag = Constants.foregroundServiceVideo)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
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
            startLoggerBirdForegroundServiceVideo()
        }
    }
}