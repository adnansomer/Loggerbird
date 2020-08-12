package loggerbird.observers

import android.util.Log
import androidx.preference.PreferenceManager
import loggerbird.constants.Constants
import loggerbird.LoggerBird

//Custom UnHandledExceptionObserver class used for observing unhandled exceptions in the attached application and library itself.
internal class UnhandledExceptionObserver : Thread.UncaughtExceptionHandler {
    /**
     * This Method Is Triggered When An Unhandled Exception Occurs And Execute callExceptionDetails Method For Saving Unhandled Exception Details.
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
//            if(e.cause != null){
//                e.cause!!.stackTrace.forEach {
//                    Log.d("loggerbird_error",it.className)
//                    Log.d("loggerbird_error",it.methodName)
//                    Log.d("loggerbird_error",it.lineNumber.toString())
//                }
//            }else{
//                e.stackTrace.forEach {
//                    Log.d("loggerbird_error",it.className)
//                    Log.d("loggerbird_error",it.methodName)
//                    Log.d("loggerbird_error",it.lineNumber.toString())
//                }
//            }
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(LoggerBird.context.applicationContext)
            if (e.cause != null) {
                with(sharedPref.edit()) {
                    putString("unhandled_stack_exception",e.toString())
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
                loggerBirdClass(className = e.cause!!.stackTrace[0].className, e = e)
            } else {
                with(sharedPref.edit()) {
                    putString("unhandled_stack_exception",e.toString())
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