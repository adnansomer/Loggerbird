package services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mobilex.loggerbird.R
import constants.Constants
import loggerbird.LoggerBird

/**
 * This class is used for supporting a foreground service for screen recording.
 */
internal class LoggerBirdForegroundServiceVideo : Service() {
    internal companion object {
        private val NOTIFICATION_CHANNEL_ID = "LoggerBirdForegroundService"
    }

    /**
     * This Method Called When Service In onStartCommand State
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     * @return START_STICKY to stick into device as a service
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            createNotificationChannel()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBirdService.callEnqueueVideo()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.foregroundServiceVideo)
        }
        return START_STICKY
    }

    /**
     * This method is used for building notification while service is running.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun startLoggerBirdForegroundServiceVideo() {
        try {
            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            val notification = notificationBuilder.setOngoing(true)
                .setContentTitle(resources.getString(R.string.foreground_service_video_notification_title))
                .setSmallIcon(R.drawable.loggerbird)
                .build()
            startForeground(5, notification)
            LoggerBirdService.callEnqueueVideo()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBirdService.callEnqueueVideo()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e , tag = Constants.foregroundServiceVideo)
        }
    }

    /**
     * This method is used for creating notification channel for foreground service.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
            startLoggerBirdForegroundServiceVideo()
        }
    }

    /**
     * This method called when service in onTaskRemoved state and stops observing memory consumption
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    /**
     * This method called when service in onTaskDestroy state.
     */
    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * This method called when service in onBind state.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * This method called when service in onUnbind state.
     */
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
}