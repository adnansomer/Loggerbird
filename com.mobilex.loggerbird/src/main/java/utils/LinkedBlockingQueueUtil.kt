package utils

import android.content.Context
import loggerbird.LoggerBird
import java.util.concurrent.LinkedBlockingQueue
//LinkedBlockingQueue class that allows managing the threads for file operations.
internal class LinkedBlockingQueueUtil(context: Context) : LinkedBlockingQueue<Runnable>() {
    var controlRunnable: Boolean = false
    override fun put(e: Runnable) {
        try {
            if (!controlRunnable) {
                controlRunnable = true
                e.run()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callExceptionDetails(e)
        }
    }
}