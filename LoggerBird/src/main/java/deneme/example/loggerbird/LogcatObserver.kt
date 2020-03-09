package deneme.example.loggerbird
//Custom UnHandledException class for getting instance of unhandled exception message with logExceptionDetails method and saves it to the txt file with saveExceptionDetails method.
class LogcatObserver:Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
     LogDeneme.logExceptionDetails(throwable = e)
        LogDeneme.saveExceptionDetails()
    }
}