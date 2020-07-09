package utils.api.trello

import adapter.recyclerView.api.trello.RecyclerViewTrelloAttachmentAdapter
import adapter.recyclerView.api.trello.RecyclerViewTrelloLabelAdapter
import adapter.recyclerView.api.trello.RecyclerViewTrelloMemberAdapter
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import models.AccountIdService
import models.TrelloProjectModel
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import utils.other.DefaultToast
import utils.other.InternetConnectionUtil
import kotlin.collections.HashMap

/** Loggerbird Trello api configration class **/
internal class TrelloApi {
    //Global variables.
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpTrello = CoroutineScope(Dispatchers.IO)
    private val coroutineCallTrello = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var queueCounter = 0
    private lateinit var timerTaskQueue: TimerTask
    private val arrayListProjectNames: ArrayList<String> = ArrayList()
    private val arrayListProjectId: ArrayList<String> = ArrayList()
    private val arrayListBoardNames: ArrayList<String> = ArrayList()
    private val arrayListBoardId: ArrayList<String> = ArrayList()
    private val arrayListMemberNames: ArrayList<String> = ArrayList()
    private val arrayListMemberId: ArrayList<String> = ArrayList()
    private val arrayListLabelNames: ArrayList<String> = ArrayList()
    private val arrayListLabelId: ArrayList<String> = ArrayList()
    private val arrayListLabelColor: ArrayList<String> = ArrayList()
    private val hashMapLabel: HashMap<String, String> = HashMap()
    private val hashMapMember: HashMap<String, String> = HashMap()
    private var projectPosition = 0
    private var boardPosition = 0
    private var labelPosition: Int = 0
    private var board: String? = null
    private var project: String? = null
    private var title: String = ""
    private var member: String? = null
    private var label: String? = null
    private val defaultToast = DefaultToast()
    private var calendar: Calendar? = null

    /**
     * This method is used for calling an trello action with network connection check.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param task is for getting reference of which trello action will be executed.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws LoggerBirdException if network connection error occurs.
     * @throws exception if error occurs.
     * @see trelloExceptionHandler method.
     */
    internal fun callTrello(
        activity: Activity,
        context: Context,
        task: String,
        filePathMedia: File? = null
    ) {
        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        coroutineCallOkHttpTrello.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    checkQueueTime(activity = activity)
                    okHttpTrelloAuthentication(
                        activity = activity,
                        context = context,
                        task = task,
                        filePathMediaName = filePathMedia
                    )
                } else {
                    activity.runOnUiThread {
                        Toast.makeText(
                            context,
                            R.string.network_check_failure,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    throw LoggerBirdException(
                        Constants.networkErrorMessage
                    )
                }

            } catch (e: Exception) {
                trelloExceptionHandler(e = e)
            }
        }
    }

    /**
     * This method is used for calling an trello action with internet connection check.
     * @param context is for getting reference from the application context.
     * @param activity is used for getting reference of current activity.
     * @param task is for getting reference of which trello action will be executed.
     * @param filePathMediaName is used for getting the reference of current media file.
     * @throws LoggerBirdException if internet connection error occurs.
     * @throws exception if error occurs.
     * @see trelloExceptionHandler method.
     */
    private fun okHttpTrelloAuthentication(
        context: Context,
        activity: Activity,
        task: String,
        filePathMediaName: File?
    ) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://api.trello.com")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                trelloExceptionHandler(e = e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallTrello.async {
                            try {
                                when (task) {
                                    "create" -> trelloCreateCard(activity = activity)
                                    "get" -> gatherTrelloDetails()
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                LoggerBird.callEnqueue()
                                LoggerBird.callExceptionDetails(
                                    exception = e,
                                    tag = Constants.trelloTag
                                )
                            }
                        }
                    } else {
                        activity.runOnUiThread {
                            Toast.makeText(
                                context,
                                R.string.internet_connection_check_failure,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        throw LoggerBirdException(
                            Constants.internetErrorMessage
                        )
                    }
                } catch (e: Exception) {
                    trelloExceptionHandler(e = e)
                }
            }
        })
    }

    /**
     * This method is used for creating trello card.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see trelloExceptionHandler method.
     */
    private fun trelloCreateCard(activity: Activity) {
        try {
            queueCounter = 0
            queueCounter++
            this.activity = activity
            val jsonObject = JsonObject()
            val jsonArrayLabels = JsonArray()
            if (RecyclerViewTrelloLabelAdapter.ViewHolder.arrayListLabelNames.isNotEmpty()) {
                RecyclerViewTrelloLabelAdapter.ViewHolder.arrayListLabelNames.forEach {
                    jsonArrayLabels.add(hashMapLabel[it.labelName])
                }
            } else {
                if (!label.isNullOrEmpty()) {
                    jsonArrayLabels.add(hashMapLabel[label!!])
                }
            }
            val jsonArrayMembers = JsonArray()
            if (RecyclerViewTrelloMemberAdapter.ViewHolder.arrayListMemberNames.isNotEmpty()) {
                RecyclerViewTrelloMemberAdapter.ViewHolder.arrayListMemberNames.forEach {
                    jsonArrayMembers.add(hashMapMember[it.memberName])
                }
            } else {
                if (!member.isNullOrEmpty()) {
                    jsonArrayMembers.add(hashMapMember[member!!])
                }
            }
            if (calendar != null) {
//                val dateFormatter =SimpleDateFormat.getDateTimeInstance()
                jsonObject.addProperty("due", Date(calendar!!.timeInMillis).toString())
            }
            jsonObject.add("idMembers", jsonArrayMembers)
            jsonObject.add("idLabels", jsonArrayLabels)
            jsonObject.addProperty("name", title)
            RetrofitTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/")
                .create(AccountIdService::class.java)
                .createTrelloIssue(
                    jsonObject = jsonObject,
                    key = LoggerBird.trelloKey,
                    token = LoggerBird.trelloToken,
                    idList = arrayListBoardId[boardPosition]
                )
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        trelloExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        val coroutineCallTrelloAttachments = CoroutineScope(Dispatchers.IO)
                        Log.d("trello_details", response.code().toString())
                        val trelloList = response.body()
                        RecyclerViewTrelloAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                            queueCounter++
                            coroutineCallTrelloAttachments.async {
                                createAttachments(
                                    activity = activity,
                                    file = it.file,
                                    cardId = trelloList!!["id"].asString
                                )
                            }
                        }
                        resetTrelloValues()
                    }
                })

        } catch (e: Exception) {
            trelloExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for creating trello attachment when trello card created.
     * @param cardId is used for getting reference of current created card id.
     * @param file is used for getting reference of the current file.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see trelloExceptionHandler method.
     */
    private fun createAttachments(cardId: String, file: File, activity: Activity) {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        RetrofitTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/cards/$cardId/")
            .create(AccountIdService::class.java)
            .setTrelloAttachments(
                file = body,
                key = LoggerBird.trelloKey,
                token = LoggerBird.trelloToken
            )
            .enqueue(object : retrofit2.Callback<JsonObject> {
                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    resetTrelloValues()
                    trelloExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    val coroutineCallTrelloAttachments = CoroutineScope(Dispatchers.IO)
                    coroutineCallTrelloAttachments.async {
                        if (file.name != "logger_bird_details.txt") {
                            if (file.exists()) {
                                file.delete()
                            }
                        }
                        Log.d("attachment_put_success", response.code().toString())
                        Log.d("attachment_put_success", response.message())
                        resetTrelloValues()
                    }
                }
            })
    }

    /**
     * This method is used for initializing the gathering action of trello.
     * @throws exception if error occurs.
     * @see trelloExceptionHandler method.
     */
    private fun gatherTrelloDetails() {
        try {
            queueCounter = 0
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            arrayListBoardNames.clear()
            arrayListBoardId.clear()
            arrayListMemberNames.clear()
            arrayListMemberId.clear()
            hashMapLabel.clear()
            hashMapMember.clear()
            arrayListLabelNames.clear()
            arrayListLabelId.clear()
            arrayListLabelColor.clear()
            gatherTaskProject()
        } catch (e: Exception) {
            trelloExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for getting project details for trello.
     * @throws exception if error occurs.
     * @see pivotalExceptionHandler method.
     */
    private fun gatherTaskProject() {
        queueCounter++
        RetrofitTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/members/me/")
            .create(AccountIdService::class.java)
            .getTrelloProjects(key = LoggerBird.trelloKey, token = LoggerBird.trelloToken)
            .enqueue(object : retrofit2.Callback<List<TrelloProjectModel>> {
                override fun onFailure(
                    call: retrofit2.Call<List<TrelloProjectModel>>,
                    t: Throwable
                ) {
                    trelloExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<List<TrelloProjectModel>>,
                    response: retrofit2.Response<List<TrelloProjectModel>>
                ) {
                    val coroutineCallTrelloProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallTrelloProject.async {
                        Log.d("trello_project_success", response.code().toString())
                        val trelloList = response.body()
                        trelloList?.forEach {
                            if (it.name != null) {
                                arrayListProjectNames.add(it.name!!)
                                arrayListProjectId.add(it.id!!)
                            }
                        }
                        if (arrayListProjectId.size > projectPosition) {
                            gatherTaskBoard(projectId = arrayListProjectId[projectPosition])
                            gatherTaskMember(projectId = arrayListProjectId[projectPosition])
                            gatherTaskLabel(projectId = arrayListProjectId[projectPosition])
                        }
                        updateFields()

                    }
                }
            })
    }

    /**
     * This method is used for getting board details for trello.
     * @param projectId is used for getting reference of current chosen project id.
     * @throws exception if error occurs.
     * @see trelloExceptionHandler method.
     */
    private fun gatherTaskBoard(projectId: String) {
        queueCounter++
        RetrofitTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/boards/$projectId/")
            .create(AccountIdService::class.java)
            .getTrelloBoards(key = LoggerBird.trelloKey, token = LoggerBird.trelloToken)
            .enqueue(object : retrofit2.Callback<JsonArray> {
                override fun onFailure(
                    call: retrofit2.Call<JsonArray>,
                    t: Throwable
                ) {
                    trelloExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonArray>,
                    response: retrofit2.Response<JsonArray>
                ) {
                    val coroutineCallTrelloBoard = CoroutineScope(Dispatchers.IO)
                    coroutineCallTrelloBoard.async {
                        try {
                            Log.d("trello_board_success", response.code().toString())
                            val trelloList = response.body()?.asJsonArray
                            trelloList?.forEach {
                                if (it.asJsonObject["data"].asJsonObject["board"].asJsonObject["id"].asString == projectId) {
                                    if (it.asJsonObject["data"].asJsonObject["list"] != null) {
                                        if (!arrayListBoardNames.contains(it.asJsonObject["data"].asJsonObject["list"].asJsonObject["name"].asString)) {
                                            arrayListBoardNames.add(it.asJsonObject["data"].asJsonObject["list"].asJsonObject["name"].asString)
                                        }
                                        if (!arrayListBoardId.contains(it.asJsonObject["data"].asJsonObject["list"].asJsonObject["id"].asString)) {
                                            arrayListBoardId.add(it.asJsonObject["data"].asJsonObject["list"].asJsonObject["id"].asString)
                                        }
                                    }
                                }
                            }
                            updateFields()
                        } catch (e: Exception) {
                            trelloExceptionHandler(e = e)
                        }
                    }
                }
            })
    }

    /**
     * This method is used for getting member details for trello.
     * @param projectId is used for getting reference of current chosen project id.
     * @throws exception if error occurs.
     * @see trelloExceptionHandler method.
     */
    private fun gatherTaskMember(projectId: String) {
        queueCounter++
        RetrofitTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/boards/$projectId/")
            .create(AccountIdService::class.java)
            .getTrelloMembers(key = LoggerBird.trelloKey, token = LoggerBird.trelloToken)
            .enqueue(object : retrofit2.Callback<JsonArray> {
                override fun onFailure(
                    call: retrofit2.Call<JsonArray>,
                    t: Throwable
                ) {
                    trelloExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonArray>,
                    response: retrofit2.Response<JsonArray>
                ) {
                    val coroutineCallTrelloMember = CoroutineScope(Dispatchers.IO)
                    coroutineCallTrelloMember.async {
                        try {
                            Log.d("trello_member_success", response.code().toString())
                            val trelloList = response.body()
                            trelloList?.forEach {
                                gatherTaskMemberNames(idName = it.asJsonObject["idMember"].asString)
                            }
                            updateFields()
                        } catch (e: Exception) {
                            trelloExceptionHandler(e = e)
                        }
                    }
                }
            })
    }

    /**
     * This method is used for getting member names details for trello.
     * @param idName is used for getting reference of member id.
     * @throws exception if error occurs.
     * @see trelloExceptionHandler method.
     */
    private fun gatherTaskMemberNames(idName: String) {
        queueCounter++
        RetrofitTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/members/")
            .create(AccountIdService::class.java)
            .getTrelloMembersName(
                key = LoggerBird.trelloKey,
                token = LoggerBird.trelloToken,
                idName = idName
            )
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    trelloExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    val coroutineCallTrelloMemberName = CoroutineScope(Dispatchers.IO)
                    coroutineCallTrelloMemberName.async {
                        try {
                            Log.d("trello_name_success", response.code().toString())
                            val trelloList = response.body()
                            if (trelloList != null) {
                                arrayListMemberNames.add(trelloList.asJsonObject["username"].asString)
                                arrayListMemberId.add(trelloList.asJsonObject["id"].asString)
                                hashMapMember[trelloList.asJsonObject["username"].asString] =
                                    trelloList.asJsonObject["id"].asString
                            }

                            updateFields()
                        } catch (e: Exception) {
                            trelloExceptionHandler(e = e)
                        }
                    }
                }
            })
    }

    /**
     * This method is used for getting label details for trello.
     * @param projectId is used for getting reference of current chosen project id.
     * @throws exception if error occurs.
     * @see trelloExceptionHandler method.
     */
    private fun gatherTaskLabel(projectId: String) {
        queueCounter++
        RetrofitTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/boards/$projectId/")
            .create(AccountIdService::class.java)
            .getTrelloLabels(key = LoggerBird.trelloKey, token = LoggerBird.trelloToken)
            .enqueue(object : retrofit2.Callback<JsonArray> {
                override fun onFailure(
                    call: retrofit2.Call<JsonArray>,
                    t: Throwable
                ) {
                    trelloExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonArray>,
                    response: retrofit2.Response<JsonArray>
                ) {
                    val coroutineCallGithubProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallGithubProject.async {
                        try {
                            Log.d("trello_member_success", response.code().toString())
                            val trelloList = response.body()
                            trelloList?.forEach {
                                if (it.asJsonObject["name"].asString.isNotEmpty() && it.asJsonObject["name"].asString != null) {
                                    arrayListLabelNames.add(it.asJsonObject["name"].asString)
                                    hashMapLabel[it.asJsonObject["name"].asString] =
                                        it.asJsonObject["id"].asString
                                } else {
                                    arrayListLabelNames.add(it.asJsonObject["id"].asString)
                                    hashMapLabel[it.asJsonObject["id"].asString] =
                                        it.asJsonObject["id"].asString
                                }
                                arrayListLabelId.add(it.asJsonObject["id"].asString)
                                arrayListLabelColor.add(it.asJsonObject["color"].asString)
                            }
                            updateFields()
                        } catch (e: Exception) {
                            trelloExceptionHandler(e = e)
                        }
                    }
                }
            })
    }

    /**
     * This method is used for getting details of autoCompleteTextViews in the trello layout.
     * @param autoTextViewProject is used for getting project details from project autoCompleteTextView in the trello layout.
     * @param autoTextViewBoard is used for getting board details from board autoCompleteTextView in the trello layout.
     * @param autoTextViewMember is used for getting member details from member autoCompleteTextView in the trello layout.
     * @param autoTextViewLabel is used for getting label details from label autoCompleteTextView in the trello layout.
     */
    internal fun gatherAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView,
        autoTextViewBoard: AutoCompleteTextView,
        autoTextViewMember: AutoCompleteTextView,
        autoTextViewLabel: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
        board = autoTextViewBoard.editableText.toString()
        member = autoTextViewMember.editableText.toString()
        label = autoTextViewLabel.editableText.toString()
    }

    /**
     * This method is used for getting details of editText in the trello layout.
     * @param editTextTitle is used for getting title details from title editText in the trello layout.
     */
    internal fun gatherEditTextDetails(editTextTitle: EditText) {
        title = editTextTitle.text.toString()
    }

    /**
     * This method is used for getting details of editText in the trello layout.
     * @param calendar is used for getting calendar details from Calendar in the trello layout.
     */
    internal fun gatherCalendarDetails(calendar: Calendar?) {
        this.calendar = calendar
    }

    /**
     * This method is used for updating and controlling the queue of background tasks in the trello actions.
     */
    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeTrelloAutoTextViews(
                    arrayListTrelloProject = arrayListProjectNames,
                    arrayListTrelloBoards = arrayListBoardNames,
                    arrayListTrelloMember = arrayListMemberNames,
                    arrayListTrelloLabel = arrayListLabelNames,
                    arrayListTrelloLabelColor = arrayListLabelColor
                )
            }
        }

    }

    /**
     * This method is used for controlling the time of background tasks in the trello actions.
    If tasks will last longer than three minutes then trello layout will be removed.
     */
    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("trello_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 180000)
    }

    /**
     * This method is used for default exception handling of trello class.
     * @param e is used for getting reference of exception.
     * @param throwable is used for getting reference of throwable.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun trelloExceptionHandler(
        e: Exception? = null,
        throwable: Throwable? = null
    ) {
        resetTrelloValues()
        if (this::timerTaskQueue.isInitialized) {
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("trello_error")
        throwable?.printStackTrace()
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.trelloTag,
            throwable = throwable
        )
    }

    /**
     * This method is used for getting reference of current project position in the project autoCompleteTextView in the trello layout.
     * @param projectPosition is used for getting reference of project position.
     */
    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    /**
     * This method is used for getting reference of current board position in the board autoCompleteTextView in the trello layout.
     * @param boardPosition is used for getting reference of board position.
     */
    internal fun setBoardPosition(boardPosition: Int) {
        this.boardPosition = boardPosition
    }

    /**
     * This method is used for getting reference of current label position in the label autoCompleteTextView in the trello layout.
     * @param labelPosition is used for getting reference of label position.
     */
    internal fun setLabelPosition(labelPosition: Int) {
        this.labelPosition = labelPosition
    }

    /**
     * This method is used for resetting the values in trello action.
     */
    private fun resetTrelloValues() {
        queueCounter--
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            arrayListBoardNames.clear()
            arrayListBoardId.clear()
            arrayListLabelNames.clear()
            hashMapLabel.clear()
            hashMapMember.clear()
            arrayListLabelId.clear()
            arrayListLabelColor.clear()
            projectPosition = 0
            boardPosition = 0
            board = null
            project = null
            calendar = null
            title = ""
            member = null
            label = null
            labelPosition = 0
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.finishShareLayout("trello")
            }
        }
    }

    /**
     * This method is used for checking project reference exist in the project list or not empty in the project AutoCompleteTextView field in the trello layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewProject is used for getting reference of project autoCompleteTextView in the trello layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkTrelloProjectEmpty(
        activity: Activity,
        autoTextViewProject: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewProject.editableText.toString().isNotEmpty() && arrayListProjectNames.contains(
                autoTextViewProject.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewProject.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_project_empty)
            )
        } else if (!arrayListProjectNames.contains(autoTextViewProject.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_project_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking label reference exist in the label list or not empty in the label AutoCompleteTextView field in the trello layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewLabel is used for getting reference of label autoCompleteTextView in the trello layout.
     * @return Boolean value.
     */
    internal fun checkTrelloLabel(
        activity: Activity,
        autoTextViewLabel: AutoCompleteTextView
    ): Boolean {
        if (arrayListLabelNames.contains(autoTextViewLabel.editableText.toString()) || autoTextViewLabel.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_label_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking label reference exist in the label list or not empty in the label AutoCompleteTextView field in the trello layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewMember is used for getting reference of member autoCompleteTextView in the trello layout.
     * @return Boolean value.
     */
    internal fun checkTrelloMember(
        activity: Activity,
        autoTextViewMember: AutoCompleteTextView
    ): Boolean {
        if (arrayListMemberNames.contains(autoTextViewMember.editableText.toString()) || autoTextViewMember.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_member_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking board reference exist in the board list or not empty in the board AutoCompleteTextView field in the board layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewBoard is used for getting reference of board autoCompleteTextView in the board layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkTrelloBoardEmpty(
        activity: Activity,
        autoTextViewBoard: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewBoard.editableText.toString().isNotEmpty() && arrayListBoardNames.contains(
                autoTextViewBoard.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewBoard.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_board_empty)
            )
        } else if (!arrayListBoardNames.contains(autoTextViewBoard.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_board_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking title reference is not empty in the task editText field in the trello layout.
     * @param activity is used for getting reference of current activity.
     * @param editTextTitle is used for getting reference of title editText in the trello layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkTitle(activity: Activity, editTextTitle: EditText): Boolean {
        return if (editTextTitle.text.toString().isNotEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.trello_title_empty)
                )
            }
            false
        }
    }

}