package utils.other

import constants.Constants
import loggerbird.LoggerBird
import java.util.concurrent.LinkedBlockingQueue

/**
 * This class is used to allow managing the threads for file operations.
 */
internal class LinkedBlockingQueueUtil : LinkedBlockingQueue<Runnable>() {
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