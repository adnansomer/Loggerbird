package observers

import loggerbird.LoggerBird
import kotlin.system.exitProcess

//Custom UnHandledException class for getting instance of unhandled com.mobilex.loggerbird.exception.LoggerBirdException message with logExceptionDetails method and saves it to the txt file with saveExceptionDetails method.
internal class LogcatObserver : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        LoggerBird.uncaughtExceptionHandlerController=true
        LoggerBird.callExceptionDetails(throwable = e)}
}