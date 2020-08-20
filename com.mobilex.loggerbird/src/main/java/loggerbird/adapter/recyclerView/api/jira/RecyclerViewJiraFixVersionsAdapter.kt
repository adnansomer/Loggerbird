package loggerbird.adapter.recyclerView.api.jira

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
import androidx.annotation.RequiresApi
import loggerbird.constants.Constants
import loggerbird.LoggerBird
import loggerbird.models.recyclerView.RecyclerViewModelFixVersions
import loggerbird.services.LoggerBirdService

//Custom recyclerView loggerbird.adapter class for jira fix-versions.
/**
 * @param fixVersionsList is for getting the list of fix-versions that will be used in recyclerView.
 * @param context is for getting reference from the application context.
 * @param activity is for getting reference of current activity in the application.
 * @param rootView is for getting reference of the view that is in the root of current activity.
 */
internal class RecyclerViewJiraFixVersionsAdapter(
    private val fixVersionsList: ArrayList<RecyclerViewModelFixVersions>,
    private val context: Context,
    private val activity: Activity,
    private val rootView: View
) :
    RecyclerView.Adapter<RecyclerViewJiraFixVersionsAdapter.ViewHolder>() {

    /**
     * Default RecyclerView.Adapter class method.
     * @param parent is for getting the view group of the recyclerView.
     * @param viewType is for getting the type of reference of the view given for recyclerView.
     * @return ViewHolder value.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_jira_fix_item,
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
        return fixVersionsList.size
    }

    /**
     * Default RecyclerView.Adapter class method.
     * @param holder is for the getting viewHolder reference for the recyclerView.
     * @param position is for the current position of the recyclerView item.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(
            item = fixVersionsList[position],
            fixVersionsAdapter = this,
            position = position,
            fixVersionsList = fixVersionsList,
            context = context,
            activity = activity,
            rootView = rootView
        )
    }

    //Inner ViewHolder class for RecyclerViewJiraFixVersionsAdapter class.
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
            internal var arrayListFixVersionsNames: ArrayList<RecyclerViewModelFixVersions> =
                ArrayList()
        }

        /**
         * This method is used for binding the items into recyclerView.
         * @param item is used for getting reference of the base model that are used items in the recyclerView.
         * @param fixVersionsAdapter is used for getting reference of the custom recyclerView loggerbird.adapter class.
         * @param position is used for getting reference of the current position of the item.
         * @param fixVersionsList is used for getting reference of the list of fix-versions that will be used in recyclerView.
         * @param context is for getting reference from the application context.
         * @param activity is for getting reference of current activity in the application.
         * @param rootView is for getting reference of the view that is in the root of current activity.
         */
        internal fun bindItems(
            item: RecyclerViewModelFixVersions,
            fixVersionsAdapter: RecyclerViewJiraFixVersionsAdapter,
            position: Int,
            fixVersionsList: ArrayList<RecyclerViewModelFixVersions>,
            context: Context,
            activity: Activity,
            rootView: View
        ) {
            arrayListFixVersionsNames = fixVersionsList
            val textViewFileName = itemView.findViewById<TextView>(R.id.textView_file_name)
            val imageButtonCross = itemView.findViewById<ImageButton>(R.id.image_button_cross)
            textViewFileName.text = item.fixVersionsName
            imageButtonCross.setSafeOnClickListener {
                    removeItemPopup(
                        activity = activity,
                        rootView = rootView,
                        fixVersionsList = fixVersionsList,
                        position = position,
                        fixVersionsAdapter = fixVersionsAdapter
                    )
            }

        }

        /**
         * This method is used for creating custom remove item popup for the recyclerView which is attached to application overlay.
         * @param activity is for getting reference of current activity in the application.
         * @param rootView is for getting reference of the view that is in the root of current activity.
         * @param fixVersionsList is used for getting reference of the list of fix-versions that will be used in recyclerView.
         * @param position is used for getting reference of the current position of the item.
         * @param fixVersionsAdapter is used for getting reference of the custom recyclerView loggerbird.adapter class.
         * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private fun removeItemPopup(
            activity: Activity,
            rootView: View,
            fixVersionsList: ArrayList<RecyclerViewModelFixVersions>,
            position: Int,
            fixVersionsAdapter: RecyclerViewJiraFixVersionsAdapter
        ) {
            try {
                viewRecyclerViewItems = LayoutInflater.from(activity)
                    .inflate(
                        R.layout.recycler_view_jira_fix_popup,
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
                            viewRecyclerViewItems.findViewById(R.id.textView_recycler_view_jira_title)
                        buttonYes =
                            viewRecyclerViewItems.findViewById(R.id.button_recycler_view_jira_yes)
                        buttonNo =
                            viewRecyclerViewItems.findViewById(R.id.button_recycler_view_jira_no)
                        buttonClicksFixVersionPopup(
                            fixVersionsAdapter = fixVersionsAdapter,
                            fixVersionList = fixVersionsList,
                            position = position
                        )
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.recyclerViewJiraAdapterTag
                )
            }
        }

        /**
         * This method is used for initializing button clicks of buttons that are inside in the recycler_view_jira_fix_popup.
         * @param fixVersionList is used for getting reference of the list of fix-versions that will be used in recyclerView.
         * @param position is used for getting reference of the current position of the item.
         * @param fixVersionsAdapter is used for getting reference of the custom recyclerView loggerbird.adapter class.
         */
        private fun buttonClicksFixVersionPopup(
            fixVersionList: ArrayList<RecyclerViewModelFixVersions>,
            position: Int,
            fixVersionsAdapter: RecyclerViewJiraFixVersionsAdapter
        ) {
            buttonYes.setSafeOnClickListener {
                fixVersionList.removeAt(position)
                arrayListFixVersionsNames = fixVersionList
                fixVersionsAdapter.notifyDataSetChanged()
                if (fixVersionList.size <= 0) {
                    LoggerBirdService.loggerBirdService.cardViewJiraFixVersionsList.visibility =
                        View.GONE
                }
                removePopupLayout()
            }
            buttonNo.setSafeOnClickListener {
                removePopupLayout()
            }

        }


        /**
         * This method is used for removing recycler_view_jira_fix_popup from window.
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