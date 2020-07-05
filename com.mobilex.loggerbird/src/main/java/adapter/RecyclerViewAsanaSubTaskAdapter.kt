package adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.mobilex.loggerbird.R
import java.util.concurrent.TimeUnit
import android.provider.Settings
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import constants.Constants
import loggerbird.LoggerBird
import models.RecyclerViewModel
import models.RecyclerViewModelSubtask
import services.LoggerBirdService
import utils.DefaultToast
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RecyclerViewAsanaSubTaskAdapter(
    private val subtaskList: ArrayList<RecyclerViewModelSubtask>,
    private val context: Context,
    private val activity: Activity,
    private val rootView: View,
    private val filePathMedia: File,
    private val arrayListAssignee: ArrayList<String>,
    private val arrayListSection: ArrayList<String>
) :
    RecyclerView.Adapter<RecyclerViewAsanaSubTaskAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_asana_subtask_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return subtaskList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(
            item = subtaskList[position],
            adapter = this,
            position = position,
            subtaskList = subtaskList,
            context = context,
            activity = activity,
            rootView = rootView,
            filePathMedia = filePathMedia,
            arrayListAssignee = arrayListAssignee,
            arrayListSection = arrayListSection
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
        private var windowManagerAsanaSub: Any? = null
        private var windowManagerAsanaSubDate: Any? = null
        private lateinit var windowManagerParamsAsanaSub: WindowManager.LayoutParams
        private lateinit var windowManagerParamsAsanaSubDate: WindowManager.LayoutParams
        private lateinit var viewAsanaSub: View
        private lateinit var viewAsanaSubDate: View
        private lateinit var layoutAsanaSub: FrameLayout
        private lateinit var scrollViewAsanaSub: ScrollView
        //        private lateinit var editTextAsanaSubTaskName: EditText
        private lateinit var imageviewAsanaSubStartDate: ImageView
        private lateinit var imageButtonAsanaSubRemoveDate: ImageButton
        private lateinit var autoTextViewAsanaSubSection: AutoCompleteTextView
        private lateinit var autoTextViewAsanaSubSectionAdapter: ArrayAdapter<String>
        private lateinit var autoTextViewAsanaSubAssignee: AutoCompleteTextView
        private lateinit var autoTextViewAsanaSubAssigneeAdapter: ArrayAdapter<String>
        private lateinit var editTextAsanaSubDescription: EditText
        private lateinit var recyclerViewAsanaSubAttachmentList: RecyclerView
        private lateinit var asanaSubAdapter: RecyclerViewAsanaSubAdapter
        private lateinit var cardViewAsanaSubAttachment: CardView
        private lateinit var buttonAsanaSubCancel: Button
        private lateinit var buttonAsanaSubCreate: Button
        private val arrayListAsanaSubFileName: ArrayList<RecyclerViewModel> = ArrayList()
        private lateinit var subtaskName: String
        private var subStartDate: String? = null
        private var subAssigneePosition: Int? = null
        private var subSectionPosition: Int? = null
        private var subDescription: String? = null
        private var subFile: String? = null
        private var arrayListSubAssigneeNames: ArrayList<String> = ArrayList()
        private var arrayListSubSectorNames: ArrayList<String> = ArrayList()
        //asana-sub date
        private lateinit var frameLayoutAsanaSubDate: FrameLayout
        private lateinit var calendarViewAsanaSub: CalendarView
        private lateinit var buttonAsanaDateSubCancel: Button
        private lateinit var buttonAsanaDateSubCreate: Button

        private val defaultToast = DefaultToast()

        companion object {
            internal var arrayListSubtask: ArrayList<RecyclerViewModelSubtask> = ArrayList()
            internal var hashMapSubAssignee: HashMap<String, Int?> = HashMap()
            internal var hashMapSubDate: HashMap<String, String?> = HashMap()
            internal var hashmapSubSection: HashMap<String, Int?> = HashMap()
            internal var hashMapSubDescription: HashMap<String, String?> = HashMap()
            internal var hashMapSubFile: HashMap<String, ArrayList<RecyclerViewModel>?> = HashMap()
        }


        fun bindItems(
            item: RecyclerViewModelSubtask,
            adapter: RecyclerViewAsanaSubTaskAdapter,
            position: Int,
            subtaskList: ArrayList<RecyclerViewModelSubtask>,
            context: Context,
            activity: Activity,
            rootView: View,
            filePathMedia: File,
            arrayListAssignee: ArrayList<String>,
            arrayListSection: ArrayList<String>
        ) {
            arrayListSubtask = subtaskList
            val textViewFileName = itemView.findViewById<TextView>(R.id.textView_file_name)
            val imageButtonCross = itemView.findViewById<ImageButton>(R.id.image_button_cross)
            val subTaskButton = itemView.findViewById<ImageButton>(R.id.image_button_sub_details)
            textViewFileName.text = item.subtaskName
            subtaskName = item.subtaskName
            imageButtonCross.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    removeItemPopup(
                        activity = activity,
                        rootView = rootView,
                        subtaskList = subtaskList,
                        position = position,
                        adapter = adapter
                    )
                }
            }
            subTaskButton.setSafeOnClickListener {
                initializeSubLayout(
                    activity = activity,
                    context = context,
                    rootView = rootView,
                    filePathMedia = filePathMedia,
                    arrayListAssignee = arrayListAssignee,
                    arrayListSection = arrayListSection
                )
            }

        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun removeItemPopup(
            activity: Activity,
            rootView: View,
            subtaskList: ArrayList<RecyclerViewModelSubtask>,
            position: Int,
            adapter: RecyclerViewAsanaSubTaskAdapter
        ) {
            try {
                viewRecyclerViewItems = LayoutInflater.from(activity)
                    .inflate(
                        R.layout.recycler_view_asana_subtask_popup,
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
                        textViewTitle =
                            viewRecyclerViewItems.findViewById(R.id.textView_recycler_view_asana_title)
                        buttonYes =
                            viewRecyclerViewItems.findViewById(R.id.button_recycler_view_asana_yes)
                        buttonNo =
                            viewRecyclerViewItems.findViewById(R.id.button_recycler_view_asana_no)
                        buttonClicksAsanaSubtaskPopup(
                            adapter = adapter,
                            subtaskList = subtaskList,
                            position = position
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.recyclerViewAsanaAdapterTag
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

        private fun buttonClicksAsanaSubtaskPopup(
            subtaskList: ArrayList<RecyclerViewModelSubtask>,
            position: Int,
            adapter: RecyclerViewAsanaSubTaskAdapter
        ) {
            buttonYes.setSafeOnClickListener {
                subtaskList.removeAt(position)
                arrayListSubtask = subtaskList
                adapter.notifyDataSetChanged()
                removePopupLayout()
                hashMapSubAssignee.remove(subtaskList[position].subtaskName)
                hashMapSubDate.remove(subtaskList[position].subtaskName)
                hashMapSubDescription.remove(subtaskList[position].subtaskName)
                hashmapSubSection.remove(subtaskList[position].subtaskName)
                hashMapSubFile.remove(subtaskList[position].subtaskName)
                if (subtaskList.size <= 0) {
                    LoggerBirdService.loggerBirdService.cardViewAsanaSubTasksList.visibility =
                        View.GONE
                }
            }
            buttonNo.setSafeOnClickListener {
                removePopupLayout()
            }

        }


        private fun removePopupLayout() {
            if (windowManagerRecyclerViewItemPopup != null && this::viewRecyclerViewItems.isInitialized) {
                (windowManagerRecyclerViewItemPopup as WindowManager).removeViewImmediate(
                    viewRecyclerViewItems
                )
                windowManagerRecyclerViewItemPopup = null
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @RequiresApi(Build.VERSION_CODES.M)
        private fun initializeSubLayout(
            activity: Activity,
            context: Context,
            rootView: View,
            filePathMedia: File,
            arrayListAssignee: ArrayList<String>,
            arrayListSection: ArrayList<String>
        ) {
            try {
                removeSubLayout()
                viewAsanaSub = LayoutInflater.from(activity)
                    .inflate(
                        R.layout.loggerbird_asana_sub_popup,
                        (rootView as ViewGroup),
                        false
                    )
                if (Settings.canDrawOverlays(activity)) {
                    windowManagerParamsAsanaSub =
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

                    windowManagerAsanaSub =
                        activity.getSystemService(Context.WINDOW_SERVICE)!!
                    if (windowManagerAsanaSub != null) {
                        (windowManagerAsanaSub as WindowManager).addView(
                            viewAsanaSub,
                            windowManagerParamsAsanaSub
                        )
                        layoutAsanaSub = viewAsanaSub.findViewById(R.id.layout_asana_sub)
                        scrollViewAsanaSub = viewAsanaSub.findViewById(R.id.scrollView_asana_sub)
//                        editTextAsanaSubTaskName =
//                            viewAsanaSub.findViewById(R.id.editText_asana_task_name)
                        autoTextViewAsanaSubSection =
                            viewAsanaSub.findViewById(R.id.auto_textView_asana_sub_section)
                        autoTextViewAsanaSubAssignee =
                            viewAsanaSub.findViewById(R.id.auto_textView_asana_sub_assignee)
                        imageviewAsanaSubStartDate =
                            viewAsanaSub.findViewById(R.id.imageView_start_date)
                        imageButtonAsanaSubRemoveDate =
                            viewAsanaSub.findViewById(R.id.image_button_asana_sub_remove_date)
                        autoTextViewAsanaSubSection =
                            viewAsanaSub.findViewById(R.id.auto_textView_asana_sub_section)
                        editTextAsanaSubDescription =
                            viewAsanaSub.findViewById(R.id.editText_asana_sub_description)
                        recyclerViewAsanaSubAttachmentList =
                            viewAsanaSub.findViewById(R.id.recycler_view_asana_sub_attachment)
                        cardViewAsanaSubAttachment =
                            viewAsanaSub.findViewById(R.id.cardView_attachment)
                        buttonAsanaSubCancel =
                            viewAsanaSub.findViewById(R.id.button_asana_sub_cancel)
                        buttonAsanaSubCreate =
                            viewAsanaSub.findViewById(R.id.button_asana_sub_create)
                        scrollViewAsanaSub.setOnTouchListener { v, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                hideKeyboard(activity = activity, view = viewAsanaSub)
                            }
                            return@setOnTouchListener false
                        }
                        initializeAsanaSubRecyclerView(
                            activity = activity,
                            context = context,
                            rootView = rootView,
                            filePathMedia = filePathMedia
                        )
                        initializeAutoTextViews(
                            activity = activity,
                            context = context,
                            arrayListAssignee = arrayListAssignee,
                            arrayListSection = arrayListSection
                        )
                        buttonClicksAsanaSub(activity = activity)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.recyclerViewAsanaSubAdapterTag
                )
            }
        }

        private fun hideKeyboard(activity: Activity, view: View) {
            val inputMethodManager =
                (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        private fun removeSubLayout() {
            if (windowManagerAsanaSub != null && this::viewAsanaSub.isInitialized) {
                (windowManagerAsanaSub as WindowManager).removeViewImmediate(
                    viewAsanaSub
                )
                windowManagerAsanaSub = null
            }
        }

        private fun buttonClicksAsanaSub(activity: Activity) {
            layoutAsanaSub.setOnClickListener {
                removeSubLayout()
            }
            buttonAsanaSubCreate.setSafeOnClickListener {
                if (checkSubAssignee(
                        activity = activity,
                        autoCompleteTextViewSubAssignee = autoTextViewAsanaSubAssignee
                    ) && checkSubSection(
                        activity = activity,
                        autoCompleteTextViewSubSector = autoTextViewAsanaSubSection
                    )
                ) {
                    hashMapSubAssignee[subtaskName] =
                        subAssigneePosition
                    hashMapSubDate[subtaskName] = this.subStartDate
                    hashMapSubDescription[subtaskName] = editTextAsanaSubDescription.text.toString()
                    hashmapSubSection[subtaskName] = subSectionPosition
                    hashMapSubFile[subtaskName] =
                        RecyclerViewAsanaSubAdapter.ViewHolder.arrayListFilePaths
                    removeSubLayout()
                }
            }
            buttonAsanaSubCancel.setSafeOnClickListener {
                removeSubLayout()
            }
            imageButtonAsanaSubRemoveDate.setSafeOnClickListener {
                imageButtonAsanaSubRemoveDate.visibility = View.GONE
//                asanaAuthentication.setStartDate(startDate = null)
            }
            imageviewAsanaSubStartDate.setSafeOnClickListener {
                initializeAsanaSubDateLayout(activity = activity)
            }
        }

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        private fun initializeAsanaSubRecyclerView(
            activity: Activity,
            context: Context,
            rootView: View,
            filePathMedia: File
        ) {
            arrayListAsanaSubFileName.clear()
            recyclerViewAsanaSubAttachmentList.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            asanaSubAdapter = RecyclerViewAsanaSubAdapter(
                addAsanaSubFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
            recyclerViewAsanaSubAttachmentList.adapter = asanaSubAdapter
        }

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        private fun addAsanaSubFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
            if (filePathMedia.exists()) {
                arrayListAsanaSubFileName.add(RecyclerViewModel(file = filePathMedia))
            }
            if (LoggerBird.filePathSecessionName.exists()) {
                arrayListAsanaSubFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
            }
            return arrayListAsanaSubFileName
        }

        private fun initializeAutoTextViews(
            activity: Activity,
            context: Context,
            arrayListAssignee: ArrayList<String>,
            arrayListSection: ArrayList<String>
        ) {
            initializeAssignee(
                activity = activity,
                context = context,
                arrayListAssignee = arrayListAssignee
            )
            initializeSection(
                activity = activity,
                context = context,
                arrayListSector = arrayListSection
            )
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun initializeAssignee(
            activity: Activity,
            context: Context,
            arrayListAssignee: ArrayList<String>
        ) {
            autoTextViewAsanaSubAssigneeAdapter = AutoCompleteTextViewAsanaSubAdapter(
                context,
                android.R.layout.simple_dropdown_item_1line,
                arrayListAssignee
            )
            autoTextViewAsanaSubAssignee.setAdapter(autoTextViewAsanaSubAssigneeAdapter)
            autoTextViewAsanaSubAssignee.setOnTouchListener { v, event ->
                autoTextViewAsanaSubAssignee.showDropDown()
                false
            }
            autoTextViewAsanaSubAssignee.setOnItemClickListener { parent, view, position, id ->
                subAssigneePosition = position
                hideKeyboard(activity = activity, view = viewAsanaSub)
            }
            this.arrayListSubAssigneeNames = arrayListAssignee
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun initializeSection(
            activity: Activity,
            context: Context,
            arrayListSector: ArrayList<String>
        ) {
            autoTextViewAsanaSubSectionAdapter = AutoCompleteTextViewAsanaSubAdapter(
                context,
                android.R.layout.simple_dropdown_item_1line,
                arrayListSector
            )
            autoTextViewAsanaSubSection.setAdapter(autoTextViewAsanaSubSectionAdapter)
            autoTextViewAsanaSubSection.setOnTouchListener { v, event ->
                autoTextViewAsanaSubSection.showDropDown()
                false
            }
            autoTextViewAsanaSubSection.setOnItemClickListener { parent, view, position, id ->
                subSectionPosition = position
                hideKeyboard(activity = activity, view = viewAsanaSub)
            }
            this.arrayListSubSectorNames = arrayListSector
        }


        private fun initializeAsanaSubDateLayout(activity: Activity) {
            removeAsanaSubDateLayout()
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            viewAsanaSubDate =
                LayoutInflater.from(activity)
                    .inflate(R.layout.asana_calendar_view, rootView, false)
            windowManagerParamsAsanaSubDate =
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
            windowManagerAsanaSubDate = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerAsanaSubDate as WindowManager).addView(
                viewAsanaSubDate,
                windowManagerParamsAsanaSubDate
            )
            frameLayoutAsanaSubDate = viewAsanaSubDate.findViewById(R.id.asana_calendar_view_layout)
            calendarViewAsanaSub = viewAsanaSubDate.findViewById(R.id.calendarView_start_date)
            buttonAsanaDateSubCancel =
                viewAsanaSubDate.findViewById(R.id.button_asana_calendar_cancel)
            buttonAsanaDateSubCreate = viewAsanaSubDate.findViewById(R.id.button_asana_calendar_ok)
            buttonClicksAsanaSubDateLayout()
        }

        private fun removeAsanaSubDateLayout() {
            if (this::viewAsanaSubDate.isInitialized && windowManagerAsanaSubDate != null) {
                (windowManagerAsanaSubDate as WindowManager).removeViewImmediate(
                    viewAsanaSubDate
                )
                windowManagerAsanaSubDate = null
            }
        }

        private fun buttonClicksAsanaSubDateLayout() {
            val calendar = Calendar.getInstance()
            val mYear = calendar.get(Calendar.YEAR)
            val mMonth = calendar.get(Calendar.MONTH)
            val mTempMonth: String
            mTempMonth = if (mMonth in 1..9) {
                "0$mMonth"
            } else {
                mMonth.toString()
            }
            val mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val mTempDay: String
            mTempDay = if (mDayOfMonth in 1..9) {
                "0$mDayOfMonth"
            } else {
                mDayOfMonth.toString()
            }
            var startDate = "$mYear-$mTempMonth-$mTempDay"
            calendarViewAsanaSub.minDate = System.currentTimeMillis() + 86400000
            frameLayoutAsanaSubDate.setOnClickListener {
                removeAsanaSubDateLayout()
            }
            calendarViewAsanaSub.setOnDateChangeListener { view, year, month, dayOfMonth ->
                val tempMonth: String = if (month in 1..9) {
                    "0$month"
                } else {
                    month.toString()
                }
                val tempDay: String = if (dayOfMonth in 1..9) {
                    "0$dayOfMonth"
                } else {
                    dayOfMonth.toString()
                }
                startDate = "$year-$tempMonth-$tempDay"
            }
            buttonAsanaDateSubCreate.setSafeOnClickListener {
                imageButtonAsanaSubRemoveDate.visibility = View.VISIBLE
//                asanaAuthentication.setStartDate(startDate = startDate)
                this.subStartDate = startDate
                removeAsanaSubDateLayout()
            }
            buttonAsanaDateSubCancel.setSafeOnClickListener {
                removeAsanaSubDateLayout()
            }
        }

        private fun checkSubAssignee(
            activity: Activity,
            autoCompleteTextViewSubAssignee: AutoCompleteTextView
        ): Boolean {
            if (!arrayListSubAssigneeNames.contains(autoCompleteTextViewSubAssignee.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.asana_assignee_doesnt_exist)
                )
                return false
            }
            return true
        }

        private fun checkSubSection(
            activity: Activity,
            autoCompleteTextViewSubSector: AutoCompleteTextView
        ): Boolean {
            if (!arrayListSubSectorNames.contains(autoCompleteTextViewSubSector.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.asana_section_doesnt_exist)
                )
                return false
            }
            return true
        }


        @SuppressLint("CheckResult")
        fun View.setSafeOnClickListener(onClick: (View) -> Unit) {
            RxView.clicks(this).throttleFirst(2000, TimeUnit.MILLISECONDS).subscribe {
                onClick(this)
            }
        }
    }
}