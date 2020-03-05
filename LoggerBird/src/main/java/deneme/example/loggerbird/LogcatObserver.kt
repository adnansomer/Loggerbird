package deneme.example.loggerbird
class LogcatObserver:Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
     LogDeneme.logExceptionDetails(throwable = e)
        LogDeneme.saveExceptionDetails()
    }
}