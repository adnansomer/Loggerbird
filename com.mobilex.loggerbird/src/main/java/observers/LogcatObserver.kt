package observers

import loggerbird.LoggerBird

//Custom UnHandledException class for getting instance of unhandled com.mobilex.loggerbird.exception.LoggerBirdException message with logExceptionDetails method and saves it to the txt file with saveExceptionDetails method.
class LogcatObserver : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        LoggerBird.takeExceptionDetails(
            throwable = e
        )
        LoggerBird.saveExceptionDetails()
    }
}