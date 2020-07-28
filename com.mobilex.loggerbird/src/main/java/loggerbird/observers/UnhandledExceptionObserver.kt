package loggerbird.observers

import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.constants.Constants
import loggerbird.LoggerBird
import loggerbird.models.room.UnhandledDuplication
import loggerbird.models.room.UnhandledDuplicationDb

//Custom UnHandledExceptionObserver class used for observing unhandled exceptions in the attached application and library itself.
internal class UnhandledExceptionObserver : Thread.UncaughtExceptionHandler {
    /**
     * This Method Is Triggered When An Unhandled Exception Occurs And Execute callExceptionDetails Method For Saving Unhandled Exception Details.
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(LoggerBird.context.applicationContext)
//            val coroutineScopeUnhandledDuplication = CoroutineScope(Dispatchers.IO)
            if (e.cause != null) {
                with(sharedPref.edit()) {
                    putString(
                        "unhandled_stack_class",
                        e.cause!!.stackTrace[0].className
                    )
                    putString(
                        "unhandled_stack_method",
                        e.cause!!.stackTrace[0].methodName
                    )
                    putString(
                        "unhandled_stack_line",
                        e.cause!!.stackTrace[0].lineNumber.toString()
                    )
                    commit()
                }
//                coroutineScopeUnhandledDuplication.async {
//                    val unhandledDuplicationDb =
//                        UnhandledDuplicationDb.getUnhandledDuplicationDb(LoggerBird.context.applicationContext)
//                    val unhandledDuplicationDao = unhandledDuplicationDb?.unhandledDuplicationDao()
//                    val unhandledDuplication = UnhandledDuplication(
//                        className = e.cause!!.stackTrace[0].className,
//                        methodName = e.cause!!.stackTrace[0].methodName,
//                        lineName = e.cause!!.stackTrace[0].lineNumber.toString()
//                    )
//                    with(unhandledDuplicationDao) {
//                        this?.insertUnhandledDuplication(unhandledDuplication = unhandledDuplication)
//                    }
//                }
                loggerBirdClass(className = e.cause!!.stackTrace[0].className, e = e)
            } else {
                with(sharedPref.edit()) {
                    putString(
                        "unhandled_stack_class",
                        e.stackTrace[0].className
                    )
                    putString(
                        "unhandled_stack_method",
                        e.stackTrace[0].methodName
                    )
                    putString(
                        "unhandled_stack_line",
                        e.stackTrace[0].lineNumber.toString()
                    )
                    commit()
                }
//                coroutineScopeUnhandledDuplication.async {
//                    val unhandledDuplicationDb =
//                        UnhandledDuplicationDb.getUnhandledDuplicationDb(LoggerBird.context.applicationContext)
//                    val unhandledDuplicationDao = unhandledDuplicationDb?.unhandledDuplicationDao()
//                    val unhandledDuplication = UnhandledDuplication(
//                        className = e.stackTrace[0].className,
//                        methodName = e.stackTrace[0].methodName,
//                        lineName = e.stackTrace[0].lineNumber.toString()
//                    )
//                    with(unhandledDuplicationDao) {
//                        this?.insertUnhandledDuplication(unhandledDuplication = unhandledDuplication)
//                    }
//                }
                loggerBirdClass(className = e.stackTrace[0].className, e = e)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.uncaughtExceptionHandlerController = true
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(throwable = e, tag = Constants.unHandledExceptionTag)
        }
    }

    private fun loggerBirdClass(className: String, e: Throwable): Boolean {
        return try {
//            if(Class.forName(className).`package`?.name?.substringBefore(".") == "loggerbird"){
//                val sharedPref =
//                    PreferenceManager.getDefaultSharedPreferences(LoggerBird.context.applicationContext)
//                val editor: SharedPreferences.Editor = sharedPref.edit()
//                editor.remove("unhandled_file_path")
//                editor.commit()
//            }else{
//                LoggerBird.uncaughtExceptionHandlerController = true
//                LoggerBird.callEnqueue()
//                LoggerBird.callExceptionDetails(
//                    throwable = e,
//                    tag = Constants.unHandledExceptionTag)
//            }
            if (Class.forName(className).`package`?.name?.substringBefore(".") != "loggerbird") {
                LoggerBird.uncaughtExceptionHandlerController = true
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    throwable = e,
                    tag = Constants.unHandledExceptionTag
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.uncaughtExceptionHandlerController = true
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(throwable = e, tag = Constants.unHandledExceptionTag)
            false
        }
    }
}