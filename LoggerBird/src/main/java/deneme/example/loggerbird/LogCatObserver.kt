package deneme.example.loggerbird
import android.util.Log
import java.util.logging.Logger

class LogCatObserver:Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
     LogDeneme.logExceptionDetails(throwable = e)
        LogDeneme.saveExceptionDetails()
    }
}