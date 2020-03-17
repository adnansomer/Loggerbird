package observers

import androidx.recyclerview.widget.RecyclerView
import loggerbird.LoggerBird
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

internal class LogRecyclerViewAdapterDataObserver : RecyclerView.AdapterDataObserver() {
    //Global variables.
    private var stringBuilderRecyclerViewAdapterDataObserver: StringBuilder = StringBuilder()
    private var formattedTime: String? = null
    private lateinit var currentRecyclerViewAdapterDataObserverState: String
    private val formatter = SimpleDateFormat.getDateTimeInstance()
    private val recyclerViewList: ArrayList<Any?> = ArrayList()

    init {
        stringBuilderRecyclerViewAdapterDataObserver.append("RecyclerView Adapter Data Observer:" + "\n")
    }

    /**
     * This Method Called When RecyclerView Observer Detect's An  OnChanged  State In The Current RecyclerView.
     * Variables:
     * @var currentRecyclerViewAdapterDataObserverState states takes current state as a String in the recyclerView observer.
     * @var stringBuilderRecyclerViewAdapterDataObserverState used for printing recyclerView observer detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */

    override fun onChanged() {
        try {
            super.onChanged()
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewAdapterDataObserverState = "onChanged"
            stringBuilderRecyclerViewAdapterDataObserver.append("$formattedTime:$currentRecyclerViewAdapterDataObserverState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callExceptionDetails(e)
        }
    }

    /**
     * This Method Called When RecyclerView Observer Detect's An  OnItemRemoved  State In The Current RecyclerView.
     * Parameters:
     * @param positionStart takes removed item's start positions in recyclerView.
     * @param itemCount takes new item count after an item removed from recyclerView.
     * Variables:
     * @var currentRecyclerViewAdapterDataObserverState states takes current state as a String in the recyclerView observer.
     * @var stringBuilderRecyclerViewAdapterDataObserverState used for printing recyclerView observer detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        try {
            super.onItemRangeRemoved(positionStart, itemCount)
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewAdapterDataObserverState = "onItemRangeRemoved"
            stringBuilderRecyclerViewAdapterDataObserver.append(
                "$formattedTime:$currentRecyclerViewAdapterDataObserverState"+" "+
                "position:$positionStart,item count:$itemCount"+"\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callExceptionDetails(e)
        }
    }

    /**
     * This Method Called When RecyclerView Observer Detect's An  OnItemRangeMoved  State In The Current RecyclerView.
     * Parameters:
     * @param fromPosition takes original item's start positions in recyclerView.
     * @param toPosition takes new position of item after move transaction completed in recyclerView.
     * @param itemCount takes new item count after an item moved in recyclerView.
     * Variables:
     * @var currentRecyclerViewAdapterDataObserverState states takes current state as a String in the recyclerView observer.
     * @var stringBuilderRecyclerViewAdapterDataObserverState used for printing recyclerView observer detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        try {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewAdapterDataObserverState = "onItemRangeRemoved"
            stringBuilderRecyclerViewAdapterDataObserver.append(
                "$formattedTime:$currentRecyclerViewAdapterDataObserverState"+" "+
                "from position:$fromPosition,to position:$toPosition,item count:$itemCount"+"\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callExceptionDetails(e)
        }
    }

    /**
     * This Method Called When RecyclerView Observer Detect's An  OnItemRangeInserted  State In The Current RecyclerView.
     * Parameters:
     * @param positionStart takes inserted item's start positions in recyclerView.
     * @param itemCount takes new item count after an item inserted to recyclerView.
     * Variables:
     * @var currentRecyclerViewAdapterDataObserverState states takes current state as a String in the recyclerView observer.
     * @var stringBuilderRecyclerViewAdapterDataObserverState used for printing recyclerView observer detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        try {
            super.onItemRangeInserted(positionStart, itemCount)
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewAdapterDataObserverState = "onItemRangeInserted"
            stringBuilderRecyclerViewAdapterDataObserver.append(
                "$formattedTime:$currentRecyclerViewAdapterDataObserverState"+" "+
                "position:$positionStart,item count:$itemCount"+"\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callExceptionDetails(e)
        }
    }

    /**
     * This Method Called When RecyclerView Observer Detect's An  OnItemRangeChanged  State In The Current RecyclerView.
     * Parameters:
     * @param positionStart takes changed item's start positions in recyclerView.
     * @param itemCount takes new item count after an item range changed in recyclerView.
     * Variables:
     * @var currentRecyclerViewAdapterDataObserverState states takes current state as a String in the recyclerView observer.
     * @var stringBuilderRecyclerViewAdapterDataObserverState used for printing recyclerView observer detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        try {
            super.onItemRangeChanged(positionStart, itemCount)
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewAdapterDataObserverState = "onItemRangeChanged"
            stringBuilderRecyclerViewAdapterDataObserver.append(
                "$formattedTime:$currentRecyclerViewAdapterDataObserverState"+" "+
                "position:$positionStart,item count:$itemCount"+"\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callExceptionDetails(e)
        }
    }

    /**
     * This Method Called When RecyclerView Observer Detect's An  OnItemRangeChanged  State In The Current RecyclerView.
     * Parameters:
     * @param positionStart takes changed item's start positions in recyclerView.
     * @param itemCount takes new item count after an item range changed in recyclerView.
     * @param payload takes list of item's in the recyclerView.
     * Variables:
     * @var currentRecyclerViewAdapterDataObserverState states takes current state as a String in the recyclerView observer.
     * @var stringBuilderRecyclerViewAdapterDataObserverState used for printing recyclerView observer detail's.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        try {
            super.onItemRangeChanged(positionStart, itemCount, payload)
            recyclerViewList.add(payload)
            val date = Calendar.getInstance().time
            formattedTime = formatter.format(date)
            currentRecyclerViewAdapterDataObserverState = "onItemRangeChanged"
            stringBuilderRecyclerViewAdapterDataObserver.append(
                "$formattedTime:$currentRecyclerViewAdapterDataObserverState"+" "+
                "position:$positionStart,item count:$itemCount"+"\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callExceptionDetails(e)
        }
    }

    /**
     * This Method Is Used For Getting RecyclerViewList.
     * Variables:
     * @var recyclerViewList print's recyclerViews that called in onItemRangeChanged method.
     * @return ArrayList<String>.
     */
    fun recyclerViewList(): ArrayList<Any?> {
        return recyclerViewList
    }

    /**
     * This Method Is Used For Printing Observer Outcome.
     * Variables:
     * @var stringBuilderRecyclerViewAdapterDataObserver will print recyclerView AdapterData state detail's.
     * @return String value.
     */
    fun returnRecyclerViewState(): String {
        return stringBuilderRecyclerViewAdapterDataObserver.toString()
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderRecyclerViewAdapterDataObserver.
     */
    fun refreshRecyclerViewObserverState() {
        stringBuilderRecyclerViewAdapterDataObserver = StringBuilder()
    }
}