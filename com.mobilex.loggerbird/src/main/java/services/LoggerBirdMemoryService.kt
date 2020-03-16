package services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import constants.Constants.Companion.memoryUsageTag
import loggerbird.LoggerBird.Companion.takeMemoryUsageDetails
import java.lang.Exception
import java.util.*

internal class LoggerBirdMemoryService : Service(){

    //Static variables
    companion object {
        private var timer: Timer? = null
        private var timerTask: TimerTask? = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * This Method Called When Service Detect's An OnCreate State In The Current Activity.
     */
    override fun onCreate() {

        try{
            startMemoryUsage()
        } catch (e : Exception){
            e.printStackTrace()
        }
        super.onCreate()
    }

    /**
     * This Method Called When Service In onStartCommand State
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return Service.START_NOT_STICKY
    }

    /**
     * This Method Called When Service In onTaskRemoved State and stops observing memory consumption
     */
    override fun onTaskRemoved(rootIntent: Intent?) {

        stopMemoryUsage()
        super.onTaskRemoved(rootIntent)
    }

    /**
     * This Method Called When Service In onDestroy State
     */
    override fun onDestroy() {

        super.onDestroy()
    }

    /**
     * This function starts time to get Memory Usage data every 5 seconds.
     * @var timer starts timer to count.
     */

    fun startMemoryUsage() {

        timer = Timer()
        initializeMemoryUsage()
        timer!!.schedule(
            timerTask, 0, 5000)
    }

    /**
     * This Method Runs the Timer to See Memory Usage
     */
    fun initializeMemoryUsage() {

        timerTask = object : TimerTask() {
            override fun run() {
                Log.d(memoryUsageTag, "" + takeMemoryUsageDetails(null))
            }
        }
    }

    /**
     * This Method Stops the Timer
     */
    fun stopMemoryUsage() {

        try{
            if (timer != null) {
                timer!!.cancel()
                timer = null
             }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}