package observers


import constants.Constants
import loggerbird.LoggerBird

//Custom UnHandledExceptionObserver class used for observing unhandled exceptions in the attached application and library itself.
internal class UnhandledExceptionObserver() : Thread.UncaughtExceptionHandler {
    /**
     * This Method Is Triggered When An Unhandled Exception Occurs And Execute callExceptionDetails Method For Saving Unhandled Exception Details.
     * Variables:
     * @var LoggerBird.uncaughtExceptionHandlerController is used for determining that there is an unhandled exception for callExceptionDetails method.
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
            if(e.cause != null){
                loggerBirdClass(className = e.cause!!.stackTrace[0].className,e = e)
            }else{
                loggerBirdClass(className = e.stackTrace[0].className,e = e)
            }
    }
    private fun loggerBirdClass(className:String,e:Throwable):Boolean{
        return try {
            if(Class.forName(className).`package`?.name!= "loggerbird"){
                LoggerBird.uncaughtExceptionHandlerController = true
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(throwable = e, tag = Constants.unHandledExceptionTag)
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