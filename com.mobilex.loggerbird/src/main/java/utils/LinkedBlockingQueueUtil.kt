package utils

import constants.Constants
import loggerbird.LoggerBird
import java.util.concurrent.LinkedBlockingQueue

//LinkedBlockingQueue class that allows managing the threads for file operations.
internal class LinkedBlockingQueueUtil : LinkedBlockingQueue<Runnable>() {
    //Global variables.
    var controlRunnable: Boolean = false
    override fun put(e: Runnable) {
        try {
            if (!controlRunnable) {
                controlRunnable = true
                e.run()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.workQueueUtilTag)
        }
    }
}