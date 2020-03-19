package observers
import constants.Constants
import loggerbird.LoggerBird
//Custom UnHandledException class for getting instance of unhandled com.mobilex.loggerbird.exception.LoggerBirdException message with logExceptionDetails method and saves it to the txt file with saveExceptionDetails method.
internal class LogcatObserver() : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        LoggerBird.uncaughtExceptionHandlerController=true
        LoggerBird.callExceptionDetails(throwable = e,tag = Constants.unHandledExceptionTag)
    }
}