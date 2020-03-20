package constants

//Constant class.
internal class Constants {
    companion object {
        //Static global constant variables.
        const val fileCreationSuccessMessage = "File Created Successfully!"
        const val componentDetailsSuccessMessage = "Component Details Gathered!"
        const val componentTag = "component"
        const val recyclerViewTag = "recyclerView"
        const val lifeCycleTag = "activity"
        const val fragmentTag = "fragment"
        const val analyticsTag = "analytics"
        const val fragmentManagerTag = "fragmentManager"
        const val fragmentLifeCycleObserverTag = "fragmentLifeCycleObserver"
        const val activityLifeCycleObserverTag = "activityLifeCycleObserver"
        const val httpRequestDetailSuccessMessage = "Http Request Details Gathered!"
        const val httpTag = "http"
        const val inAPurchaseTag = "in_a_purchase"
        const val retrofitRequestDetailSuccessMessage = "Http Request Details Gathered!"
        const val retrofitTag = "retrofit"
        const val realmDetailSuccessMessage = "Realm Details Gathered!"
        const val realmTag = "realm"
        const val builderTag = "builder"
        const val allSuccessMessage = "All Details Gathered!"
        const val allTag = "all"
        const val exceptionMessage = "Exception Details Gathered!"
        const val exceptionTag = ""
        const val emailTag = "email"
        const val unHandledExceptionTag = "Unhandled Exception"
        const val transaction = "Transaction Name:"
        const val logInitErrorMessage = "Call logInit method before calling any other method!"
        const val logInitAttachedErrorMessage =
            "logInit method is already attached to your application!"
        const val internetErrorMessage = "Invalid internet connection response code!"
        const val networkErrorMessage = "Invalid network response"
        const val saveErrorMessage =
            "Empty Log Instance , Please Call Appropriate Instance Method Before Calling Save Method Hint="
        const val componentMethodTag = "LoggerBird.takeComponentDetails()"
        const val lifeCycleMethodTag = "LoggerBird.LifeCycleDetails()"
        const val retrofitMethodTag = "LoggerBird.takeRetrofitRequestDetails()"
        const val analyticsMethodTag = "LoggerBird.takeAnalyticsDetails()"
        const val fragmentManagerMethodTag = "LoggerBird.takeFragmentManagerDetails()"
        const val htppRequestMethodTag = "LoggerBird.takeHttpRequestDetails()"
        const val inAPurchaseMethodTag = "LoggerBird.takeInAPurchaseDetails()"
        const val realmMethodTag = "LoggerBird.takeRealmDetails()"
        const val exceptionMethodTag = "LoggerBird.takeExceptionDetails()"
        const val SMTP_HOST_NAME: String = "smtp.gmail.com";
        const val exceedFileLimitTag: String = "Exceed File Limit"
        const val saveSessionOldFileTag: String = "Save Session Old File"
        const val workQueueUtilTag: String = "Work Queue Util"
        const val recyclerViewAdapterDataObserverTag: String = "RecyclerView Adapter Data Observer"
        const val recyclerViewChildAttachStateChangeListenerTag: String =
            "RecyclerView Child Attach State Change Listener"
        const val recyclerViewItemTouchListener: String = "RecyclerView Item Touch Listener"
        const val recyclerViewScrollListener: String = "RecyclerView Scroll Listener"
        const val serviceTag: String = "Service"
    }
}