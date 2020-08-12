package loggerbird.adapter.recyclerView.api.trello

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.mobilex.loggerbird.R
import java.util.concurrent.TimeUnit
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import loggerbird.constants.Constants
import loggerbird.LoggerBird
import loggerbird.models.recyclerView.RecyclerViewModelItem

//Custom recyclerView loggerbird.adapter class for trello item.
/**
 * @param itemList is for getting the list of labels that will be used in recyclerView.
 * @param context is for getting reference from the application context.
 * @param activity is for getting reference of current activity in the application.
 * @param rootView is for getting reference of the view that is in the root of current activity.
 */
internal class RecyclerViewTrelloItemAdapter(
    private val itemList: ArrayList<RecyclerViewModelItem>,
    private val checkList: ArrayList<Boolean>,
    private val context: Context,
    private val activity: Activity,
    private val rootView: View,
    private val checkListPosition: Int
) :
    RecyclerView.Adapter<RecyclerViewTrelloItemAdapter.ViewHolder>() {

    /**
     * Default RecyclerView.Adapter class method.
     * @param parent is for getting the view group of the recyclerView.
     * @param viewType is for getting the type of reference of the view given for recyclerView.
     * @return ViewHolder value.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_trello_item_item,
                parent,
                false
            )
        )
    }

    /**
     * Default RecyclerView.Adapter class method.
     * @return size of the list that will be used in the recyclerview.
     */
    override fun getItemCount(): Int {
        return itemList.size
    }

    /**
     * Default RecyclerView.Adapter class method.
     * @param holder is for the getting viewHolder reference for the recyclerView.
     * @param position is for the current position of the recyclerView item.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(
            item = itemList[position],
            checkList = checkList,
            itemAdapter = this,
            position = position,
            itemList = itemList,
            context = context,
            activity = activity,
            rootView = rootView,
            checkListPosition = checkListPosition
        )
    }

    //Inner ViewHolder class for RecyclerViewTrelloItemAdapter class.
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //Global variables.
        private var windowManagerRecyclerViewItemPopup: Any? = null
        private lateinit var windowManagerParamsRecyclerViewItemPopup: WindowManager.LayoutParams
        private lateinit var viewRecyclerViewItems: View
        private lateinit var textViewTitle: TextView
        private lateinit var buttonYes: Button
        private lateinit var buttonNo: Button

        //Static variables.
        companion object {
            internal var arrayListItemNames: ArrayList<RecyclerViewModelItem> = ArrayList()
            internal var arrayListItemChecked: ArrayList<Boolean> = ArrayList()
        }


        /**
         * This method is used for binding the items into recyclerView.
         * @param item is used for getting reference of the base model that are used items in the recyclerView.
         * @param itemAdapter is used for getting reference of the custom recyclerView loggerbird.adapter class.
         * @param position is used for getting reference of the current position of the item.
         * @param itemList is used for getting reference of the list of item that will be used in recyclerView.
         * @param context is for getting reference from the application context.
         * @param activity is for getting reference of current activity in the application.
         * @param rootView is for getting reference of the view that is in the root of current activity.
         * @param checkListPosition is used for getting reference of the current position of the item that exist in outer checklist recyclerView.
         */
        internal fun bindItems(
            item: RecyclerViewModelItem,
            itemAdapter: RecyclerViewTrelloItemAdapter,
            position: Int,
            itemList: ArrayList<RecyclerViewModelItem>,
            checkList: ArrayList<Boolean>,
            context: Context,
            activity: Activity,
            rootView: View,
            checkListPosition: Int
        ) {
            val textViewFileName = itemView.findViewById<TextView>(R.id.textView_file_name)
            val imageButtonCross = itemView.findViewById<ImageButton>(R.id.image_button_cross)
            val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox_item)
            arrayListItemNames = itemList
            arrayListItemChecked = checkList
//            if (arrayListItemChecked.size > position) {
//                arrayListItemChecked.add(position , arrayListItemChecked[position])
//            } else {
//                arrayListItemChecked.add(position, false)
//            }
            if (RecyclerViewTrelloCheckListAdapter.ViewHolder.arrayListCheckListNames.size > checkListPosition) {
                if (RecyclerViewTrelloCheckListAdapter.ViewHolder.hashmapCheckListCheckedList[RecyclerViewTrelloCheckListAdapter.ViewHolder.arrayListCheckListNames[checkListPosition].checkListName] != null) {
                    checkBox.isChecked =
                        RecyclerViewTrelloCheckListAdapter.ViewHolder.hashmapCheckListCheckedList[RecyclerViewTrelloCheckListAdapter.ViewHolder.arrayListCheckListNames[checkListPosition].checkListName]!![position]
                }
            }
            textViewFileName.text = item.itemName
            imageButtonCross.setSafeOnClickListener {
                    removeItemPopup(
                        activity = activity,
                        rootView = rootView,
                        itemList = itemList,
                        position = position,
                        checkListPosition = checkListPosition,
                        itemAdapter = itemAdapter
                    )
            }
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                arrayListItemChecked[position] = isChecked
                RecyclerViewTrelloCheckListAdapter.ViewHolder.hashmapCheckListCheckedList[RecyclerViewTrelloCheckListAdapter.ViewHolder.arrayListCheckListNames[checkListPosition].checkListName] =
                    arrayListItemChecked

            }
//            if(checkListPosition == position){
//                RecyclerViewTrelloCheckListAdapter.ViewHolder.hashmapCheckListNames[RecyclerViewTrelloCheckListAdapter.ViewHolder.arrayListCheckListNames[position].checkListName] = arrayListItemNames
//            }
        }

        /**
         * This method is used for creating custom remove item popup for the recyclerView which is attached to application overlay.
         * @param activity is for getting reference of current activity in the application.
         * @param rootView is for getting reference of the view that is in the root of current activity.
         * @param itemList is used for getting reference of the list of items that will be used in recyclerView.
         * @param position is used for getting reference of the current position of the item.
         * @param itemAdapter is used for getting reference of the custom recyclerView loggerbird.adapter class.
         * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun removeItemPopup(
            activity: Activity,
            rootView: View,
            itemList: ArrayList<RecyclerViewModelItem>,
            position: Int,
            checkListPosition: Int,
            itemAdapter: RecyclerViewTrelloItemAdapter
        ) {
            try {
                viewRecyclerViewItems = LayoutInflater.from(activity)
                    .inflate(
                        R.layout.recycler_view_trello_item_item_popup,
                        (rootView as ViewGroup),
                        false
                    )
                    windowManagerParamsRecyclerViewItemPopup =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            WindowManager.LayoutParams(
                                WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                                PixelFormat.TRANSLUCENT
                            )
                        } else {
                            WindowManager.LayoutParams(
                                WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.TYPE_APPLICATION,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                                PixelFormat.TRANSLUCENT
                            )
                        }

                    windowManagerRecyclerViewItemPopup =
                        activity.getSystemService(Context.WINDOW_SERVICE)!!
                    if (windowManagerRecyclerViewItemPopup != null) {
                        (windowManagerRecyclerViewItemPopup as WindowManager).addView(
                            viewRecyclerViewItems,
                            windowManagerParamsRecyclerViewItemPopup
                        )
                        textViewTitle =
                            viewRecyclerViewItems.findViewById(R.id.textView_recycler_view_trello_title)
                        buttonYes =
                            viewRecyclerViewItems.findViewById(R.id.button_recycler_view_trello_yes)
                        buttonNo =
                            viewRecyclerViewItems.findViewById(R.id.button_recycler_view_trello_no)
                        buttonClicksTrelloPopup(
                            itemAdapter = itemAdapter,
                            itemList = itemList,
                            checkListPosition = checkListPosition,
                            position = position
                        )
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.recyclerViewTrelloAdapterTag
                )
            }
        }

        /**
         * This method is used for initializing button clicks of buttons that are inside in the recycler_view_trello_item_item_popup.
         * @param itemList is used for getting reference of the list of item that will be used in recyclerView.
         * @param position is used for getting reference of the current position of the item.
         * @param itemAdapter is used for getting reference of the custom recyclerView loggerbird.adapter class.
         */
        private fun buttonClicksTrelloPopup(
            itemList: ArrayList<RecyclerViewModelItem>,
            position: Int,
            checkListPosition: Int,
            itemAdapter: RecyclerViewTrelloItemAdapter
        ) {
            buttonYes.setSafeOnClickListener {
                RecyclerViewTrelloCheckListAdapter.ViewHolder.hashmapCheckListCheckedList[RecyclerViewTrelloCheckListAdapter.ViewHolder.arrayListCheckListNames[checkListPosition].checkListName] =
                    arrayListItemChecked
                RecyclerViewTrelloCheckListAdapter.ViewHolder.hashmapCheckListNames[RecyclerViewTrelloCheckListAdapter.ViewHolder.arrayListCheckListNames[checkListPosition].checkListName] =
                    arrayListItemNames
                itemList.removeAt(position)
                arrayListItemChecked.removeAt(position)
                arrayListItemNames = itemList
                itemAdapter.notifyDataSetChanged()
                removePopupLayout()
            }
            buttonNo.setSafeOnClickListener {
                removePopupLayout()
            }

        }


        /**
         * This method is used for removing recycler_view_trello_item_item_popup from window.
         */
        private fun removePopupLayout() {
            if (windowManagerRecyclerViewItemPopup != null && this::viewRecyclerViewItems.isInitialized) {
                (windowManagerRecyclerViewItemPopup as WindowManager).removeViewImmediate(
                    viewRecyclerViewItems
                )
                windowManagerRecyclerViewItemPopup = null
            }
        }

        /**
         * This method is used for preventing spamming of a button and allows to be button click methods executed in every 2 second.
         */
        @SuppressLint("CheckResult")
        private fun View.setSafeOnClickListener(onClick: (View) -> Unit) {
            RxView.clicks(this).throttleFirst(2000, TimeUnit.MILLISECONDS).subscribe {
                onClick(this)
            }
        }
    }

}