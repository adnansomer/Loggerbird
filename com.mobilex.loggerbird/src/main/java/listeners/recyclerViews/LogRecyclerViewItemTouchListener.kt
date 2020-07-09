package listeners.recyclerViews

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import constants.Constants
import loggerbird.LoggerBird
import java.text.SimpleDateFormat
import java.util.*
//This class is used for listening item touch in recyclerView.
internal class LogRecyclerViewItemTouchListener : RecyclerView.OnItemTouchListener {
    //Global variables.
    private var stringBuilderRecyclerViewItemTouchListener: StringBuilder = StringBuilder()
    private var formattedTime: String? = null
    private lateinit var currentRecyclerViewItemTouchListenerState: String
    private val formatter = SimpleDateFormat.getDateTimeInstance()
    private val recyclerViewList: ArrayList<Any?> = ArrayList()

    init {
        stringBuilderRecyclerViewItemTouchListener.append("RecyclerView Item Touch Listener:" + "\n")
    }

    /**
     * This Method Called When RecyclerView onTouchListener Detect's An  OnTouchEvent State In The Current RecyclerView.
     * @param rv takes reference of item touched recyclerView from listener.
     * @param e takes position of scrolled recyclerView.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        try {
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewItemTouchListenerState = "onTouchEvent"
            stringBuilderRecyclerViewItemTouchListener.append(
                "$formattedTime:$currentRecyclerViewItemTouchListenerState" + " " +
                        "recyclerView:$rv,motion event:$e" + "\n"
            )
            if (!recyclerViewList.contains(rv)) {
                recyclerViewList.add(rv)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.recyclerViewItemTouchListener
            )
        }
    }

    /**
     * This Method Called When RecyclerView onTouchListener Detect's An  OnInterceptTouchEvent State In The Current RecyclerView.
     * @param rv takes reference of item touched recyclerView from listener.
     * @param e takes position of scrolled recyclerView.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     * @return Boolean value(if return value is true then recyclerview can't scroll).
     */
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        try {
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewItemTouchListenerState = "onInterceptTouchEvent"
            stringBuilderRecyclerViewItemTouchListener.append(
                "$formattedTime:$currentRecyclerViewItemTouchListenerState" + " " +
                        "recyclerView:$rv,motion event:$e" + "\n"
            )
            if (!recyclerViewList.contains(rv)) {
                recyclerViewList.add(rv)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.recyclerViewItemTouchListener
            )
        }
        return false
    }

    /**
     * This Method Called When RecyclerView onRequestDisallowInterceptTouchEvent Detect's An  OnTouchEvent State In The Current RecyclerView.
     * @param disallowIntercept becomes true when a child of RecyclerView does not want RecyclerView and its ancestors to.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        try {
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewItemTouchListenerState = "onTouchEvent"
            stringBuilderRecyclerViewItemTouchListener.append(
                "$formattedTime:$currentRecyclerViewItemTouchListenerState" + " " +
                        "disallowIntercept:$disallowIntercept" + "\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.recyclerViewItemTouchListener
            )
        }
    }

    /**
     * This Method Is Used For Getting RecyclerViewList.
     * @return ArrayList<String>.
     */
    internal fun recyclerViewList(): ArrayList<Any?> {
        return recyclerViewList
    }

    /**
     * This Method Is Used For Printing Listener Outcome.
     * @return String value.
     */
    internal fun returnRecyclerViewState(): String {
        return stringBuilderRecyclerViewItemTouchListener.toString()
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderRecyclerViewItemTouchListener.
     */
    internal fun refreshRecyclerViewObserverState() {
        stringBuilderRecyclerViewItemTouchListener = StringBuilder()
    }
}