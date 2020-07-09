package listeners.recyclerViews

import androidx.recyclerview.widget.RecyclerView
import constants.Constants
import loggerbird.LoggerBird
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//This class is used for listening scroll in recyclerView.
internal class LogRecyclerViewScrollListener : RecyclerView.OnScrollListener() {
    //Global variables.
    private var stringBuilderRecyclerViewScrollListener: StringBuilder = StringBuilder()
    private var formattedTime: String? = null
    private lateinit var currentRecyclerViewScrollListenerState: String
    private val formatter = SimpleDateFormat.getDateTimeInstance()
    private val recyclerViewList: ArrayList<Any?> = ArrayList()

    init {
        stringBuilderRecyclerViewScrollListener.append("RecyclerView Scroll Listener:" + "\n")
    }

    /**
     * This Method Called When RecyclerView OnScrollListener Detect's An  OnItemRangeChanged  State In The Current RecyclerView.
     * Parameters:
     * @param recyclerView takes reference of scrolled recyclerView from listener.
     * @param dx takes position of x coordinate.
     * @param dy takes position of x coordinate.
     * Variables:
     * @var currentRecyclerViewScrollListenerState states takes current state as a String in the recyclerView listener.
     * @var stringBuilderRecyclerViewScrollListener used for printing recyclerView listener detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        try {
            super.onScrolled(recyclerView, dx, dy)
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewScrollListenerState = "onScrolled"
            stringBuilderRecyclerViewScrollListener.append(
                "$formattedTime:$currentRecyclerViewScrollListenerState" + " " +
                        "recyclerView:$recyclerView,dx:$dx,dy:$dy" + "\n"
            )
            if (!recyclerViewList.contains(recyclerView)) {
                recyclerViewList.add(recyclerView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.recyclerViewScrollListener
            )
        }

    }

    /**
     * This Method Called When RecyclerView onScrollListener Detect's An  OnItemRangeChanged  State In The Current RecyclerView.
     * Parameters:
     * @param recyclerView takes reference of scrolled recyclerView from listener.
     * @param newState takes position of scrolled recyclerView.
     * Variables:
     * @var currentRecyclerViewScrollListenerState states takes current state as a String in the recyclerView listener.
     * @var stringBuilderRecyclerViewScrollListener used for printing recyclerView listener detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        try {
            super.onScrollStateChanged(recyclerView, newState)
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewScrollListenerState = "onScrollStateChanged"
            stringBuilderRecyclerViewScrollListener.append(
                "$formattedTime:$currentRecyclerViewScrollListenerState" + " " +
                        "recyclerView:$recyclerView,newState:$newState" + "\n"
            )
            if (!recyclerViewList.contains(recyclerView)) {
                recyclerViewList.add(recyclerView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.recyclerViewScrollListener
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
     * Variables:
     * @var stringBuilderRecyclerViewScrollListener will print recyclerView scroll listener state detail's.
     * @return String value.
     */
    internal fun returnRecyclerViewState(): String {
        return stringBuilderRecyclerViewScrollListener.toString()
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderRecyclerViewScrollListener.
     */
    internal fun refreshRecyclerViewObserverState() {
        stringBuilderRecyclerViewScrollListener = StringBuilder()
    }
}