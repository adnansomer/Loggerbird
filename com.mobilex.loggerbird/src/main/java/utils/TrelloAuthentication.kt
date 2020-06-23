package utils

import adapter.RecyclerViewTrelloAdapter
import adapter.RecyclerViewTrelloLabelAdapter
import adapter.RecyclerViewTrelloMemberAdapter
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
import kotlin.collections.HashMap

internal class TrelloAuthentication {
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
    private val hashMapLabel:HashMap<String,String> = HashMap()
    private val hashMapMember:HashMap<String,String> = HashMap()
    private var projectPosition = 0
    private var boardPosition = 0
    private var labelPosition: Int = 0
    private var board: String? = null
    private var project: String? = null
    private var title: String = ""
    private var member: String? = null
    private var label: String? = null
    private val defaultToast = DefaultToast()
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
                                    "create" -> trelloCreateIssue(activity = activity)
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

    private fun trelloCreateIssue(activity: Activity) {
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
            if(RecyclerViewTrelloMemberAdapter.ViewHolder.arrayListMemberNames.isNotEmpty()){
                RecyclerViewTrelloMemberAdapter.ViewHolder.arrayListMemberNames.forEach {
                    jsonArrayMembers.add(hashMapMember[it.memberName])
                }
            }else{
                if(!member.isNullOrEmpty())
                {
                jsonArrayMembers.add(hashMapMember[member!!])
                }
            }
            jsonObject.add("idMembers",jsonArrayMembers)
            jsonObject.add("idLabels",jsonArrayLabels)
            jsonObject.addProperty("name", title)
            RetrofitUserTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/")
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
                        RecyclerViewTrelloAdapter.ViewHolder.arrayListFilePaths.forEach {
                            queueCounter++
                            coroutineCallTrelloAttachments.async {
                                createAttachments(
                                    activity = activity,
                                    file = it.file,
                                    cardId = trelloList!!["id"].asString
                                )
                            }
//                            repoId = githubList!!["url"].asString.substringAfterLast("/").toInt()
//                            RecyclerViewGithubAdapter.ViewHolder.arrayListFilePaths.forEach {
//                                val file = it.file
//                                if (file.exists()) {
//                                    callGithubAttachments(
//                                        repo = repos!!,
//                                        filePathMedia = file
//                                    )
//                                }
//                            }
//                            if (RecyclerViewGithubAdapter.ViewHolder.arrayListFilePaths.isEmpty()) {
//                                activity.runOnUiThread {
//                                    LoggerBirdService.loggerBirdService.finishShareLayout("github")
//                                }
//                            }
                        }
                        resetTrelloValues()
                    }
                })

        } catch (e: Exception) {
            trelloExceptionHandler(e = e)
        }
    }

    private fun gatherTaskProject() {
        queueCounter++
        RetrofitUserTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/members/me/")
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

    private fun gatherTaskBoard(projectId: String) {
        queueCounter++
        RetrofitUserTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/boards/$projectId/")
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

//                        githubList?.forEach {
//                            if (it.html_url != null && it.name != null) {
//                                arrayListProject.add(it.name!!)
//                                arrayListProjectUrl.add(it.html_url!!)
//                            }
//                        }
                            updateFields()
                        } catch (e: Exception) {
                            trelloExceptionHandler(e = e)
                        }
                    }
                }
            })
    }

    private fun gatherTaskMember(projectId: String) {
        queueCounter++
        RetrofitUserTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/boards/$projectId/")
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

    private fun gatherTaskMemberNames(idName: String) {
        queueCounter++
        RetrofitUserTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/members/")
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
                                hashMapMember[trelloList.asJsonObject["username"].asString] = trelloList.asJsonObject["id"].asString
                            }

                            updateFields()
                        } catch (e: Exception) {
                            trelloExceptionHandler(e = e)
                        }
                    }
                }
            })
    }

    private fun gatherTaskLabel(projectId: String) {
        queueCounter++
        RetrofitUserTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/boards/$projectId/")
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
                                if (it.asJsonObject["name"].asString.isNotEmpty() && it.asJsonObject["name"].asString != null ) {
                                    arrayListLabelNames.add(it.asJsonObject["name"].asString)
                                    hashMapLabel[it.asJsonObject["name"].asString] = it.asJsonObject["id"].asString
                                } else {
                                    arrayListLabelNames.add(it.asJsonObject["id"].asString)
                                    hashMapLabel[it.asJsonObject["id"].asString] = it.asJsonObject["id"].asString
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

    internal fun gatherEditTextDetails(editTextTitle: EditText) {
        title = editTextTitle.text.toString()
    }

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


    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeTrelloAutoTextViews(
                    arrayListProject = arrayListProjectNames,
                    arrayListBoards = arrayListBoardNames,
                    arrayListMember = arrayListMemberNames,
                    arrayListLabel = arrayListLabelNames,
                    arrayListLabelColor = arrayListLabelColor
                )
            }
        }

    }


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

    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    internal fun setBoardPosition(boardPosition: Int) {
        this.boardPosition = boardPosition
    }

    internal fun setLabelPosition(labelPosition: Int) {
        this.labelPosition = labelPosition
    }


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
            title = ""
            member = null
            label = null
            labelPosition = 0
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.finishShareLayout("trello")
            }
        }
    }

    private fun createAttachments(cardId: String, file: File, activity: Activity) {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        RetrofitUserTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/cards/$cardId/")
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

    private fun createLabels(cardId: String, activity: Activity) {
        val jsonObjectLabels = JsonObject()
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
        jsonObjectLabels.add("idLabels",jsonArrayLabels)
        RetrofitUserTrelloClient.getTrelloUserClient(url = "https://api.trello.com/1/cards/$cardId/")
            .create(AccountIdService::class.java)
            .setTrelloLabels(jsonArray = jsonArrayLabels,key = LoggerBird.trelloKey, token = LoggerBird.trelloToken)
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
                    val coroutineCallTrelloLabels = CoroutineScope(Dispatchers.IO)
                    coroutineCallTrelloLabels.async {
                        Log.d("label_put_success", response.code().toString())
                        Log.d("label_put_success", response.message())
                        resetTrelloValues()
                    }
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkTrelloProjectEmpty(
        activity: Activity,
        autoTextViewTrelloProject: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewTrelloProject.editableText.toString().isNotEmpty() && arrayListProjectNames.contains(
                autoTextViewTrelloProject.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewTrelloProject.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_project_empty)
            )
        } else if (!arrayListProjectNames.contains(autoTextViewTrelloProject.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_project_doesnt_exist)
            )
        }
        return false
    }

    internal fun checkTrelloLabel(
        activity: Activity,
        autoTextViewTrelloLabel: AutoCompleteTextView
    ): Boolean {
        if (arrayListLabelNames.contains(autoTextViewTrelloLabel.editableText.toString()) || autoTextViewTrelloLabel.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_label_doesnt_exist)
            )
        }
        return false
    }
    internal fun checkTrelloMember(
        activity: Activity,
        autoTextViewTrelloMember: AutoCompleteTextView
    ): Boolean {
        if (arrayListMemberNames.contains(autoTextViewTrelloMember.editableText.toString()) || autoTextViewTrelloMember.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_member_doesnt_exist)
            )
        }
        return false
    }
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkTrelloBoardEmpty(
        activity: Activity,
        autoTextViewTrelloBoard: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewTrelloBoard.editableText.toString().isNotEmpty() && arrayListBoardNames.contains(
                autoTextViewTrelloBoard.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewTrelloBoard.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_board_empty)
            )
        } else if (!arrayListBoardNames.contains(autoTextViewTrelloBoard.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_board_doesnt_exist)
            )
        }
        return false
    }
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkTitle(activity: Activity, context: Context): Boolean {
        return if (title.isNotEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = context.resources.getString(R.string.trello_title_empty)
                )
            }
            false
        }
    }

}