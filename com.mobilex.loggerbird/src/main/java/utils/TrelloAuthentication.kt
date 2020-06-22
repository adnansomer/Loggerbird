package utils

import adapter.RecyclerViewTrelloAdapter
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
import java.util.logging.Logger
import kotlin.collections.ArrayList
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

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
    private var projectPosition = 0
    private var boardPosition = 0
    private var board: String? = null
    private var project: String? = null
    private var title: String? = null
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
                        val coroutineCallTrelloIssue = CoroutineScope(Dispatchers.IO)

                        Log.d("trello_details", response.code().toString())
                        val trelloList = response.body()
                        RecyclerViewTrelloAdapter.ViewHolder.arrayListFilePaths.forEach {
                            queueCounter++
                            coroutineCallTrelloIssue.async {
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
                    val coroutineCallGithubRepo = CoroutineScope(Dispatchers.IO)
                    coroutineCallGithubRepo.async {
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
                    val coroutineCallGithubProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallGithubProject.async {
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


    internal fun gatherAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView,
        autoTextViewBoard: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
        board = autoTextViewBoard.editableText.toString()
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
                    arrayListBoards = arrayListBoardNames
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


    private fun resetTrelloValues() {
        queueCounter--
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            arrayListProjectNames.clear()
            arrayListProjectId.clear()
            arrayListBoardNames.clear()
            arrayListBoardId.clear()
            projectPosition = 0
            boardPosition = 0
            board = null
            project = null
            title = null
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
            .setTrelloAttachments(file = body,key = LoggerBird.trelloKey,token = LoggerBird.trelloToken)
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
                        Log.d("attachment_put_success",response.message())
                        resetTrelloValues()
                    }
                }
            })
    }

}