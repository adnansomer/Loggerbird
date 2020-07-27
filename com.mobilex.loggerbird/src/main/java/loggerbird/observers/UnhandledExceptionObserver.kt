package loggerbird.observers

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import loggerbird.constants.Constants
import loggerbird.LoggerBird
import kotlin.collections.ArrayList

//Custom UnHandledExceptionObserver class used for observing unhandled exceptions in the attached application and library itself.
internal class UnhandledExceptionObserver : Thread.UncaughtExceptionHandler {
    private val arrayListPackages :ArrayList<String> = ArrayList()
    /**
     * This Method Is Triggered When An Unhandled Exception Occurs And Execute callExceptionDetails Method For Saving Unhandled Exception Details.
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        if(e.cause != null){
            loggerBirdClass(className = e.cause!!.stackTrace[0].className, e = e)
        }else{
            loggerBirdClass(className = e.stackTrace[0].className, e = e)
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
            if(Class.forName(className).`package`?.name?.substringBefore(".") != "loggerbird"){
                LoggerBird.uncaughtExceptionHandlerController = true
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    throwable = e,
                    tag = Constants.unHandledExceptionTag)
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