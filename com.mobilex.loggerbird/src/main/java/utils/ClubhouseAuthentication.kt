package utils

import android.app.Activity
import adapter.RecyclerViewClubhouseAdapter
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import models.api.*
import models.*
import models.api.clubhouse.*
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

class ClubhouseAuthentication {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpClubhouse = CoroutineScope(Dispatchers.IO)
    private val coroutineCallClubhouse = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private lateinit var timerTaskQueue: TimerTask
    private var spinnerPositionUser: Int = 0
    private var userPosition = 0
    private var userId: String = ""
    private var userName: String = ""
    private var arrayListUsers: ArrayList<String> = ArrayList()
    private val arrayListUsersId: ArrayList<String> = ArrayList()
    private var hashMapUsers: HashMap<String, String> = HashMap()
    private val arrayListStoryType: ArrayList<String> = arrayListOf("Bug","Chore","Feature")
    private var spinnerPositionStoryType: Int = 0
    private var storyTypePosition = 0
    private var storyType: String? = null
    private var spinnerPositionEpic: Int = 0
    private var projectPosition = 0
    private var project: String? = null
    private val arrayListProjectId: ArrayList<String> = ArrayList()
    private val arrayListProjectName: ArrayList<String> = ArrayList()
    private var hashMapProjects: HashMap<String, String> = HashMap()
    private var epicPosition = 0
    private var epic: String? = null
    private val arrayListEpicId: ArrayList<String> = ArrayList()
    private val arrayListEpicName: ArrayList<String> = ArrayList()
    private var hashMapEpic: HashMap<String, String> = HashMap()
    private var storyName: String?=""
    private var storyDescription: String?=""
    private var estimate: String? = ""
    internal var dueDate: String? = ""
    private lateinit var storyId: String
    private val arrayListAttachments: ArrayList<String> = ArrayList()
    private var descriptionString = StringBuilder()

    companion object{
        const val BASE_URL = "https://api.clubhouse.io/api/v3/"
    }
    
    internal fun callClubhouse(
        activity: Activity,
        context: Context,
        task: String,
        filePathMedia: File? = null
    ) {
        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        coroutineCallOkHttpClubhouse.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    checkQueueTime(activity = activity)
                    okHttpClubhouseAuthentication(
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
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.clubhouseTag)
            }
        }
    }

    private fun okHttpClubhouseAuthentication(
        context: Context,
        activity: Activity,
        task: String,
        filePathMediaName: File?
    ) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://clubhouse.io/")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d("clubhouse_response_message", response.message)
                Log.d("clubhouse_response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallClubhouse.async {
                            try {
                                when (task) {
                                    "create" -> createClubhouseStory(
                                        activity = activity,
                                        context = context,
                                        filePathMedia = filePathMedia
                                    )

                                    "get" -> gatherClubhouseDetails(
                                        activity = activity,
                                        context = context,
                                        filePathMedia = filePathMedia
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                LoggerBird.callEnqueue()
                                LoggerBird.callExceptionDetails(
                                    exception = e,
                                    tag = Constants.clubhouseTag
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
                    e.printStackTrace()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(exception = e, tag = Constants.clubhouseTag)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.clubhouseTag)
            }

        })
    }

    private suspend fun gatherClubhouseDetails(
        activity: Activity,
        context: Context,
        filePathMedia: File?
    ) {
        val coroutineCallGatherDetails = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherDetails.async(Dispatchers.IO) {
            try {
                arrayListProjectId.clear()
                arrayListProjectName.clear()
                hashMapProjects.clear()
                arrayListUsers.clear()
                arrayListUsersId.clear()
                hashMapUsers.clear()
                arrayListEpicId.clear()
                arrayListEpicName.clear()
                hashMapEpic.clear()
                gatherClubhouseProjectDetails()
                gatherClubhouseUserDetails()
                gatherClubhouseEpicDetails()

            } catch (e: Exception) {
                LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error")
                clubhouseExceptionHandler(e = e, filePathName = filePathMedia)

            }
        }
    }

    private suspend fun gatherClubhouseProjectDetails(){
        try {
            RetrofitUserClubhouseClient.getClubhouseUserClient(url = BASE_URL)
                .create(AccountIdService::class.java)
                .getClubhouseProjects(token = LoggerBird.clubhouseApiToken)
                .enqueue(object : retrofit2.Callback<List<ClubhouseProjectModel>> {
                    override fun onFailure(
                        call: retrofit2.Call<List<ClubhouseProjectModel>>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.clubhouseTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<ClubhouseProjectModel>>,
                        response: retrofit2.Response<List<ClubhouseProjectModel>>
                    ) {
                        val coroutineCallClubhouseDetails = CoroutineScope(Dispatchers.IO)
                        coroutineCallClubhouseDetails.async {
                            Log.d("clubhouseProjects", response.code().toString())
                            val clubhouse = response.body()
                            Log.d("clubhouseProjects", clubhouse.toString())
                            clubhouse?.forEach {
                                if (it.name != null) {
                                    arrayListProjectId.add(it.id!!)
                                    arrayListProjectName.add(it.name!!)
                                    hashMapProjects[it.name!!] = it.id!!
                                }
                            }
                            updateFields()
                        }
                    }
                })
        } catch (e: Exception) {
            LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error")
            clubhouseExceptionHandler(e = e, filePathName = filePathMedia)
        }
    }

    private suspend fun gatherClubhouseUserDetails(){
        try {
            RetrofitUserClubhouseClient.getClubhouseUserClient(url = BASE_URL)
                .create(AccountIdService::class.java)
                .getClubhouseMembers(token = LoggerBird.clubhouseApiToken)
                .enqueue(object : retrofit2.Callback<JsonArray>{
                    override fun onFailure(
                        call: retrofit2.Call<JsonArray>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.clubhouseTag)
                    }
                    override fun onResponse(
                        call: retrofit2.Call<JsonArray>,
                        response: retrofit2.Response<JsonArray>
                    ) {
                        val coroutineCallClubhouseDetails = CoroutineScope(Dispatchers.IO)
                        coroutineCallClubhouseDetails.async {
                            Log.d("clubhouseUsers", response.code().toString())
                            val response = response.body()
                            Log.d("clubhouseUsers", response.toString())
                            response?.getAsJsonArray()?.forEach {
                                userId = it.asJsonObject["id"].asString
                                userName = it.asJsonObject["profile"].asJsonObject["name"].asString
                                arrayListUsersId.add(userId)
                                arrayListUsers.add(userName)
                                hashMapUsers[userName!!] = userId!!

                            }
                            updateFields()
                        }
                    }
                })

        } catch (e: Exception) {
            LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error")
            clubhouseExceptionHandler(e = e, filePathName = filePathMedia)
        }
    }

    private suspend fun gatherClubhouseEpicDetails(){
        try {
            RetrofitUserClubhouseClient.getClubhouseUserClient(url = BASE_URL)
                .create(AccountIdService::class.java)
                .getClubhouseEpics(token = LoggerBird.clubhouseApiToken)
                .enqueue(object : retrofit2.Callback<List<ClubHouseEpicModel>> {
                    override fun onFailure(
                        call: retrofit2.Call<List<ClubHouseEpicModel>>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.clubhouseTag)
                    }
                    override fun onResponse(
                        call: retrofit2.Call<List<ClubHouseEpicModel>> ,
                        response: retrofit2.Response<List<ClubHouseEpicModel>>
                    ) {
                        val coroutineCallClubhouseDetails = CoroutineScope(Dispatchers.IO)
                        coroutineCallClubhouseDetails.async {
                            Log.d("clubhouseEpics", response.code().toString())
                            val clubhouse = response.body()
                            Log.d("clubhouseEpics", response.toString())
                            clubhouse?.forEach {
                                if (it.name != null) {
                                    arrayListEpicId.add(it.id!!)
                                    arrayListEpicName.add(it.name!!)
                                    hashMapEpic[it.name!!] = it.id!!
                                }
                            }
                            updateFields()
                        }
                    }
                })

        } catch (e: Exception) {
            LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error")
            clubhouseExceptionHandler(e = e, filePathName = filePathMedia)
        }
    }

    private fun createClubhouseStory(
        activity: Activity,
        context: Context,
        filePathMedia: File?
    ) {
        try {
            this.activity = activity
            val coroutineCallClubhouseIssue = CoroutineScope(Dispatchers.IO)
            RetrofitUserClubhouseClient.getClubhouseUserClient(url = BASE_URL)
                .create(AccountIdService::class.java)
                .createClubhouseStory(
                    token = LoggerBird.clubhouseApiToken,
                    project_id = hashMapProjects[arrayListProjectName[projectPosition]].toString(),
                    name = storyName!!,
                    description = storyDescription!!,
                    storyType = storyType!!.toLowerCase(),
                    deadline = dueDate!!,
                    requestedBy = hashMapUsers[userName]!!,
                    epicId = hashMapEpic[epic]!!,
                    estimate = estimate!!)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(call: retrofit2.Call<JsonObject>, t: Throwable) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.clubhouseTag)
                    }
                    override fun onResponse(call: retrofit2.Call<JsonObject>, response: retrofit2.Response<JsonObject>) {
                        if (response.code() in 400..499) {
                            LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error") }
                            Log.d("clubhousecreate", response.code().toString())
                            val clubhouse = response.body()
                            Log.d("clubhousecreate", clubhouse.toString())

                        coroutineCallClubhouseIssue.async {
                            storyId = response.body()!!["id"].asString
                            RecyclerViewClubhouseAdapter.ViewHolder.arrayListFilePaths.forEach {
                                val file = it.file
                                if (file.exists()) {
                                    createAttachments(
                                        storyId = storyId,
                                        filePathMedia = filePathMedia
                                    )
                                }
                            }
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAttachments(filePathMedia: File?, storyId: String) {
        try {
            val requestFile = filePathMedia!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val filePathMediaName = filePathMedia!!.name
            val body = MultipartBody.Part.createFormData("file", filePathMediaName, requestFile)

            RetrofitUserClubhouseClient.getClubhouseUserClient(url = BASE_URL)
                .create(AccountIdService::class.java)
                .sendClubhouseAttachments(token = LoggerBird.clubhouseApiToken, file = body)
                .enqueue(object : retrofit2.Callback<JsonArray> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<JsonArray>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.clubhouseTag)
                    }
                    override fun onResponse(
                        call: retrofit2.Call<JsonArray>,
                        response: retrofit2.Response<JsonArray>
                    ) {
                        if (response.code() in 400..499) {
                            LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error") }

                        val coroutineCallClubhouseAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallClubhouseAttachments.async {
                            Log.d("clubhouse_attachment", response.code().toString())
                            Log.d("clubhouse_attachment", response.body().toString())
                            response.body()?.getAsJsonArray()?.forEach {
                                arrayListAttachments.add(it.asJsonObject["url"].asString)
                            }

                            val stringBuilder = StringBuilder()
                            var attachmentCounter = 1
                            arrayListAttachments.forEach {
                                stringBuilder.append("\nattachment_$attachmentCounter:$it\n")
                                attachmentCounter++
                            }
                            val stringDescription = "$storyDescription\n" + stringBuilder.toString()

                            uploadAttachments(storyId = storyId, description = stringDescription)
                        }
                    }
                })

        } catch (e: Exception) {
            clubhouseExceptionHandler(e = e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadAttachments(storyId: String,description: String) {
        try {
            RetrofitUserClubhouseClient.getClubhouseUserClient(url = BASE_URL)
                .create(AccountIdService::class.java)
                .setClubhouseStory(id = storyId,token = LoggerBird.clubhouseApiToken, description = description)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.clubhouseTag)
                    }
                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        if (response.code() in 400..499) {
                            LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error") }
                        else{
                            LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse")
                        }
                        val coroutineCallClubhouseAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallClubhouseAttachments.async {
                            val clubhouseAttachments = response.body()
                            Log.d("clubhouse_attachment_result", response.code().toString())
                            Log.d("clubhouse_attachment_result", response.body().toString())
                            if (filePathMedia!!.name != "logger_bird_details.txt") {
                                if (filePathMedia!!.exists()) {
                                    filePathMedia!!.delete()
                                }
                            }
                        }
                    }
                })

        } catch (e: Exception) {
            clubhouseExceptionHandler(e = e)
        }
    }

    private fun updateFields() {
        timerTaskQueue.cancel()
        activity.runOnUiThread {
            LoggerBirdService.loggerBirdService.initializeClubhouseSpinner(
                arrayListClubhouseRequester = arrayListUsers,
                arrayListClubhouseProjects = arrayListProjectName,
                arrayListClubhouseStoryType = arrayListStoryType,
                arrayListClubhouseEpic = arrayListEpicName
            )
        }
    }

    internal fun gatherClubhouseSpinnerDetails(
        spinnerUser: Spinner,
        spinnerStoryType: Spinner,
        spinnerEpic: Spinner
    ) {
        spinnerPositionUser = spinnerUser.selectedItemPosition
        userName = spinnerUser.selectedItem.toString()
        spinnerPositionStoryType = spinnerStoryType.selectedItemPosition
        storyType = spinnerStoryType.selectedItem.toString()
        spinnerPositionEpic = spinnerEpic.selectedItemPosition
        epic = spinnerEpic.selectedItem.toString()
    }

    internal fun gatherClubhouseEditTextDetails(
        editTextStoryName: EditText,
        editTextStoryDescription: EditText,
        editTextEstimate: EditText
    ) {
        storyName = editTextStoryName.text.toString()
        storyDescription = editTextStoryDescription.text.toString()
        estimate = editTextEstimate.text.toString()
    }

    internal fun gatherClubhouseProjectAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
    }

    internal fun clubhouseProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    internal fun clubhouseEpicPosition(epicPosition: Int) {
        this.epicPosition = epicPosition
    }

    internal fun clubhouseUserPosition(userPosition: Int) {
        this.spinnerPositionUser = userPosition
    }

    internal fun clubhouseStoryTypePosition(storyTypePosition: Int){
        this.spinnerPositionStoryType = storyTypePosition
    }

    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 100000)
    }

    private fun clubhouseExceptionHandler(
        e: Exception? = null,
        filePathName: File? = null,
        throwable: Throwable? = null
    ) {
        if (this::timerTaskQueue.isInitialized) {
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error")
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.clubhouseTag,
            throwable = throwable
        )
    }
}