package adapter

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
import constants.Constants
import loggerbird.LoggerBird
import models.RecyclerViewModelFixVersions
import services.LoggerBirdService

class RecyclerViewJiraFixVersionsAdapter(
    private val fixVersionsList: ArrayList<RecyclerViewModelFixVersions>,
    private val context: Context,
    private val activity: Activity,
    private val rootView: View
) :
    RecyclerView.Adapter<RecyclerViewJiraFixVersionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_jira_fix_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return fixVersionsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(
            item = fixVersionsList[position],
            adapter = this,
            position = position,
            fixVersionsList = fixVersionsList,
            context = context,
            activity = activity,
            rootView = rootView
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //        private lateinit var alertDialogItemDelete: AlertDialog
        private var windowManagerRecyclerViewItemPopup: Any? = null
        private lateinit var windowManagerParamsRecyclerViewItemPopup: WindowManager.LayoutParams
        private lateinit var viewRecyclerViewItems: View
        private lateinit var textViewTitle: TextView
        private lateinit var buttonYes: Button
        private lateinit var buttonNo: Button

        companion object{
             internal  var arrayListFixVersionsNames:ArrayList<RecyclerViewModelFixVersions> = ArrayList()
        }


        fun bindItems(
            item: RecyclerViewModelFixVersions,
            adapter: RecyclerViewJiraFixVersionsAdapter,
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    removeItemPopup(
                        activity = activity,
                        rootView = rootView,
                        fixVersionsList = fixVersionsList,
                        position = position,
                        adapter = adapter
                    )
                }
            }

        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun removeItemPopup(
            activity: Activity,
            rootView: View,
            fixVersionsList: ArrayList<RecyclerViewModelFixVersions>,
            position: Int,
            adapter: RecyclerViewJiraFixVersionsAdapter
        ) {
            try {
                viewRecyclerViewItems = LayoutInflater.from(activity)
                    .inflate(
                        R.layout.recycler_view_jira_fix_popup,
                        (rootView as ViewGroup),
                        false
                    )
                if (Settings.canDrawOverlays(activity)) {
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
//                    windowManagerParamsFeedback.gravity = Gravity.BOTTOM
                        (windowManagerRecyclerViewItemPopup as WindowManager).addView(
                            viewRecyclerViewItems,
                            windowManagerParamsRecyclerViewItemPopup
                        )
                        textViewTitle = viewRecyclerViewItems.findViewById(R.id.textView_recycler_view_jira_title)
                        buttonYes = viewRecyclerViewItems.findViewById(R.id.button_recycler_view_jira_yes)
                        buttonNo = viewRecyclerViewItems.findViewById(R.id.button_recycler_view_jira_no)
                        buttonClicksFixVersionPopup(adapter = adapter , fixVersionList = fixVersionsList , position = position)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.recyclerViewJiraAdapterTag
                )
            }


//            val alertDialogBuilder = AlertDialog.Builder(context)
//            alertDialogBuilder.setTitle("Delete File:")
//            alertDialogBuilder.setMessage("Are you sure to remove file from attachments?")
//            alertDialogBuilder.setPositiveButton("Yes") { dialog, which ->
//                fileList.removeAt(position)
//                adapter.notifyDataSetChanged()
//            }
//            alertDialogBuilder.setNegativeButton("No") { dialog, which ->
//                alertDialogItemDelete.dismiss()
//            }
//            alertDialogItemDelete = alertDialogBuilder.create()
//            alertDialogItemDelete.show()
        }

        private fun buttonClicksFixVersionPopup(fixVersionList: ArrayList<RecyclerViewModelFixVersions>, position: Int, adapter: RecyclerViewJiraFixVersionsAdapter) {
            buttonYes.setSafeOnClickListener {
                fixVersionList.removeAt(position)
                arrayListFixVersionsNames = fixVersionList
                adapter.notifyDataSetChanged()
                if(fixVersionList.size <=0){
                    LoggerBirdService.loggerBirdService.cardViewJiraFixVersionsList.visibility = View.GONE
                }
                removePopupLayout()
            }
            buttonNo.setSafeOnClickListener {
                removePopupLayout()
            }

        }


        private fun removePopupLayout(){
            if (windowManagerRecyclerViewItemPopup != null && this::viewRecyclerViewItems.isInitialized) {
                (windowManagerRecyclerViewItemPopup as WindowManager).removeViewImmediate(
                    viewRecyclerViewItems
                )
                windowManagerRecyclerViewItemPopup = null
            }
        }

        @SuppressLint("CheckResult")
        fun View.setSafeOnClickListener(onClick: (View) -> Unit) {
            RxView.clicks(this).throttleFirst(2000, TimeUnit.MILLISECONDS).subscribe {
                onClick(this)
            }
        }
    }

}