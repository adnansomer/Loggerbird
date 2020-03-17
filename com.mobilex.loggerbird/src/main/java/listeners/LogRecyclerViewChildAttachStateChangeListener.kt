package listeners

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import loggerbird.LoggerBird
import java.text.SimpleDateFormat
import java.util.*

internal class LogRecyclerViewChildAttachStateChangeListener:RecyclerView.OnChildAttachStateChangeListener {
    //Global variables.
    private var stringBuilderRecyclerViewChildAttachListener: StringBuilder = StringBuilder()
    private var formattedTime: String? = null
    private lateinit var currentRecyclerViewChildAttachStateChangeListenerState: String
    private val formatter = SimpleDateFormat.getDateTimeInstance()
    init {
        stringBuilderRecyclerViewChildAttachListener.append("RecyclerView Child Attach State Change Listener:" + "\n")
    }

    /**
     * This Method Called When RecyclerView OnChildAttachStateChangeListener Detect's An  OnChildViewDetachedToWindow  State In The Current RecyclerView.
     * Parameters:
     * @param view takes reference of scrolled recyclerView from listener.
     * Variables:
     * @var currentRecyclerViewChildAttachStateChangeListenerObserverState states takes current state as a String in the recyclerView listener.
     * @var stringBuilderRecyclerViewScrollListenerObserver used for printing recyclerView listener detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onChildViewDetachedFromWindow(view: View) {
        try {
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewChildAttachStateChangeListenerState = "onChildViewDetachedFromWindow"
            stringBuilderRecyclerViewChildAttachListener.append(
                "$formattedTime:$currentRecyclerViewChildAttachStateChangeListenerState" + " " +
                        "view:${view}"+"\n+root view:${view.rootView}+"+"\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callExceptionDetails(e)
        }
    }

    /**
     * This Method Called When RecyclerView OnChildAttachStateChangeListener Detect's An  OnChildViewAttachedToWindow  State In The Current RecyclerView.
     * Parameters:
     * @param view takes reference of scrolled recyclerView from listener.
     * Variables:
     * @var currentRecyclerViewChildAttachStateChangeListenerObserverState states takes current state as a String in the recyclerView listener.
     * @var stringBuilderRecyclerViewScrollListenerObserver used for printing recyclerView listener detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onChildViewAttachedToWindow(view: View) {
        try {
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewChildAttachStateChangeListenerState = "onChildViewAttachedFromWindow"
            stringBuilderRecyclerViewChildAttachListener.append(
                "$formattedTime:$currentRecyclerViewChildAttachStateChangeListenerState" + " " +
                        "view:${view}"+"\n+root view:${view.rootView}+"+"\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callExceptionDetails(e)
        }

    }

    /**
     * This Method Is Used For Printing Listener Outcome.
     * Variables:
     * @var stringBuilderRecyclerViewChildAttachStateChangeListener will print recyclerView scroll listener state detail's.
     * @return String value.
     */
    fun returnRecyclerViewState(): String {
        return stringBuilderRecyclerViewChildAttachListener.toString()
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderRecyclerViewChildAttachStateChangeListener
     */
    fun refreshRecyclerViewObserverState() {
        stringBuilderRecyclerViewChildAttachListener = StringBuilder()
    }
}