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
import models.RecyclerViewModelComponent
import services.LoggerBirdService

class RecyclerViewJiraComponentAdapter(
    private val componentList: ArrayList<RecyclerViewModelComponent>,
    private val context: Context,
    private val activity: Activity,
    private val rootView: View
) :
    RecyclerView.Adapter<RecyclerViewJiraComponentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_jira_component_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return componentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(
            item = componentList[position],
            adapter = this,
            position = position,
            componentList = componentList,
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
             internal  var arrayListComponentNames:ArrayList<RecyclerViewModelComponent> = ArrayList()
        }


        fun bindItems(
            item: RecyclerViewModelComponent,
            adapter: RecyclerViewJiraComponentAdapter,
            position: Int,
            componentList: ArrayList<RecyclerViewModelComponent>,
            context: Context,
            activity: Activity,
            rootView: View
        ) {
            arrayListComponentNames = componentList
            val textViewFileName = itemView.findViewById<TextView>(R.id.textView_file_name)
            val imageButtonCross = itemView.findViewById<ImageButton>(R.id.image_button_cross)
            textViewFileName.text = item.componentName
            imageButtonCross.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    removeItemPopup(
                        activity = activity,
                        rootView = rootView,
                        componentList = componentList,
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
            componentList: ArrayList<RecyclerViewModelComponent>,
            position: Int,
            adapter: RecyclerViewJiraComponentAdapter
        ) {
            try {
                viewRecyclerViewItems = LayoutInflater.from(activity)
                    .inflate(
                        R.layout.recycler_view_jira_component_popup,
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
                        buttonClicksComponentPopup(adapter = adapter , componentList = componentList , position = position)
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

        private fun buttonClicksComponentPopup(componentList: ArrayList<RecyclerViewModelComponent>, position: Int, adapter: RecyclerViewJiraComponentAdapter) {
            buttonYes.setSafeOnClickListener {
                componentList.removeAt(position)
                arrayListComponentNames = componentList
                adapter.notifyDataSetChanged()
                if(componentList.size <=0){
                    LoggerBirdService.loggerBirdService.cardViewJiraComponentList.visibility = View.GONE
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