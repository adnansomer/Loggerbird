package listeners.recyclerViews

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import constants.Constants
import loggerbird.LoggerBird
import java.text.SimpleDateFormat
import java.util.*
//This class is used for listening child attach state in recyclerView.
internal class LogRecyclerViewChildAttachStateChangeListener :
    RecyclerView.OnChildAttachStateChangeListener {
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
     * @param view takes reference of scrolled recyclerView from listener.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onChildViewDetachedFromWindow(view: View) {
        try {
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewChildAttachStateChangeListenerState = "onChildViewDetachedFromWindow"
            stringBuilderRecyclerViewChildAttachListener.append(
                "$formattedTime:$currentRecyclerViewChildAttachStateChangeListenerState" + " " +
                        "view:${view}" + "\n+root view:${view.rootView}+" + "\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.recyclerViewChildAttachStateChangeListenerTag
            )
        }
    }

    /**
     * This Method Called When RecyclerView OnChildAttachStateChangeListener Detect's An  OnChildViewAttachedToWindow  State In The Current RecyclerView.
     * @param view takes reference of scrolled recyclerView from listener.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onChildViewAttachedToWindow(view: View) {
        try {
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewChildAttachStateChangeListenerState = "onChildViewAttachedFromWindow"
            stringBuilderRecyclerViewChildAttachListener.append(
                "$formattedTime:$currentRecyclerViewChildAttachStateChangeListenerState" + " " +
                        "view:${view}" + "\n+root view:${view.rootView}+" + "\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.recyclerViewChildAttachStateChangeListenerTag
            )
        }

    }

    /**
     * This Method Is Used For Printing Listener Outcome.
     * @return String value.
     */
    internal fun returnRecyclerViewState(): String {
        return stringBuilderRecyclerViewChildAttachListener.toString()
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderRecyclerViewChildAttachStateChangeListener
     */
    internal fun refreshRecyclerViewObserverState() {
        stringBuilderRecyclerViewChildAttachListener = StringBuilder()
    }
}