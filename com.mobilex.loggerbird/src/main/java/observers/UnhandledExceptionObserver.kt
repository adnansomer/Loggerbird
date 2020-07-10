package observers

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.preference.PreferenceManager
import com.mobilex.loggerbird.BuildConfig
import constants.Constants
import loggerbird.LoggerBird
import services.LoggerBirdService
import java.net.URL
import java.util.*
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.pm.PackageInfoCompat
import java.util.jar.JarFile
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
            addLoggerBirdPackages()
            if(arrayListPackages.contains(Class.forName(className).`package`?.name)){
                val sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(LoggerBird.context.applicationContext)
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.remove("unhandled_file_path")
                editor.commit()
            }else{
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

    private fun addLoggerBirdPackages(){
        arrayListPackages.clear()
        arrayListPackages.add("adapter")
        arrayListPackages.add("authentication")
        arrayListPackages.add("constants")
        arrayListPackages.add("exception")
        arrayListPackages.add("fragments")
        arrayListPackages.add("interceptors")
        arrayListPackages.add("listeners")
        arrayListPackages.add("loggerbird")
        arrayListPackages.add("models")
        arrayListPackages.add("observers")
        arrayListPackages.add("paint")
        arrayListPackages.add("services")
        arrayListPackages.add("utils")
    }
}