package utils.api.clubhouse

import android.app.Activity
import adapter.recyclerView.api.clubhouse.RecyclerViewClubhouseAttachmentAdapter
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
import utils.other.InternetConnectionUtil
import utils.other.LinkedBlockingQueueUtil

/** Loggerbird Clubhouse api configration class **/
internal class ClubhouseApi {
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
    private var storyDescription: String?=null
    private var estimate: String? = null
    internal var dueDate: String? = null
    private lateinit var storyId: String
    private val arrayListAttachments: ArrayList<String> = ArrayList()
    private var descriptionString = StringBuilder()
    private var workQueueLinkedClubhouseAttachments: LinkedBlockingQueueUtil =
        LinkedBlockingQueueUtil()
    private var runnableListClubhouseAttachments: ArrayList<Runnable> = ArrayList()

    companion object{
        const val BASE_URL = "https://api.clubhouse.io/api/v3/"
    }
    /**
     * This method is used for calling Clubhouse Api in order to determine operation.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param task is used for determining the task.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                        Toast.makeText(context, R.string.network_check_failure, Toast.LENGTH_SHORT).show()
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

    /**
     * This method is used for checking OkHttp connection.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param task is used for determining the task.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

    /**
     * This method is used for gathering all details to be send to Clubhouse.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                arrayListAttachments.clear()
                gatherClubhouseProjectDetails()
                gatherClubhouseUserDetails()
                gatherClubhouseEpicDetails()

            } catch (e: Exception) {
                LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error")
                clubhouseExceptionHandler(e = e, filePathName = filePathMedia)

            }
        }
    }

    /**
     * This method is used for gathering all details to be send to Clubhouse.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private suspend fun gatherClubhouseProjectDetails(){
        try {
            RetrofitClubhouseClient.getClubhouseUserClient(
                url = BASE_URL
            )
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

    /**
     * This method is used for gathering Clubhouse users.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private suspend fun gatherClubhouseUserDetails(){
        try {
            RetrofitClubhouseClient.getClubhouseUserClient(
                url = BASE_URL
            )
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

    /**
     * This method is used for gathering Clubhouse epic date.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private suspend fun gatherClubhouseEpicDetails(){
        try {
            RetrofitClubhouseClient.getClubhouseUserClient(
                url = BASE_URL
            )
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

    /**
     * This method is used for creating issue for Clubhouse with using Api.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun createClubhouseStory(
        activity: Activity,
        context: Context,
        filePathMedia: File?
    ) {
        try {
            this.activity = activity
            val coroutineCallClubhouseIssue = CoroutineScope(Dispatchers.IO)
            RetrofitClubhouseClient.getClubhouseUserClient(
                url = BASE_URL
            )
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
                            RecyclerViewClubhouseAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                                val file = it.file
                                if (file.exists()) {
                                    callClubhouseAttachments(
                                        storyId = storyId,
                                        filePathMedia = file
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

    /**
     * This method is used for creating queue to send more than one file consecutively.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param storyId is for getting storyId to update already opened issue.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun callClubhouseAttachments(filePathMedia: File, storyId: String) {
        if (LoggerBird.isLogInitAttached()) {
            if (runnableListClubhouseAttachments.isEmpty()) {
                workQueueLinkedClubhouseAttachments.put {
                    createAttachments(storyId = storyId,filePathMedia  = filePathMedia)
                }
            }
            runnableListClubhouseAttachments.add(Runnable {
                createAttachments(storyId = storyId, filePathMedia = filePathMedia)
            })
        } else {
            throw LoggerBirdException(Constants.logInitErrorMessage)
        }
    }

    /**
     * This method is used for creating attachments and their URL to be send as an attachment.
     * @param storyId is for getting storyId to update project with attachment URL.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAttachments(filePathMedia: File?, storyId: String) {
        try {
            val requestFile = filePathMedia!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val filePathMediaName = filePathMedia!!.name
            val body = MultipartBody.Part.createFormData("file", filePathMediaName, requestFile)

            RetrofitClubhouseClient.getClubhouseUserClient(
                url = BASE_URL
            )
                .create(AccountIdService::class.java)
                .sendClubhouseAttachments(token = LoggerBird.clubhouseApiToken, file = body)
                .enqueue(object : retrofit2.Callback<JsonArray> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(call: retrofit2.Call<JsonArray>, t: Throwable) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.clubhouseTag)
                    }
                    override fun onResponse(call: retrofit2.Call<JsonArray>, response: retrofit2.Response<JsonArray>) {
                        if (response.code() in 400..499) {
                            LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error")
                        }

                        val coroutineCallClubhouseAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallClubhouseAttachments.async {
                            Log.d("clubhouse_attachment", response.code().toString())
                            Log.d("clubhouse_attachment", response.body().toString())

                            if (filePathMedia.name != "logger_bird_details.txt") {
                                if (filePathMedia.exists()) {
                                    filePathMedia.delete()
                                }
                            }
                            if (response.body() != null) {
                                response.body()?.getAsJsonArray()?.forEach {
                                    arrayListAttachments.add(it.asJsonObject["url"].asString)
                                }
                                callEnqueueClubhouseAttachments(storyId = storyId)
                            }
                        }
                    }
                })
        }catch (e: Exception) {
            clubhouseExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for calling attachments consecutively from queue.
     * @param storyId is for getting issueId to update issue with attachment URL.
     */
    private fun callEnqueueClubhouseAttachments(storyId: String) {
        workQueueLinkedClubhouseAttachments.controlRunnable = false
        if (runnableListClubhouseAttachments.size > 0) {
            runnableListClubhouseAttachments.removeAt(0)
            if (runnableListClubhouseAttachments.size > 0) {
                workQueueLinkedClubhouseAttachments.put(runnableListClubhouseAttachments[0])
            } else {
                if (arrayListAttachments.isNotEmpty()) {
                    var attachmentCounter = 1
                    val stringBuilder = StringBuilder()
                    arrayListAttachments.forEach {
                        stringBuilder.append("\nattachment_$attachmentCounter:$it")
                        attachmentCounter++
                    }
                    val updatedDescription = "$storyDescription\n" + stringBuilder.toString()
                    uploadAttachments(storyId =  storyId, description = updatedDescription.toString())

                } else {
                    LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse")
                }

            }
        } else {
            LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse")
        }
    }

    /**
     * This method is used for sending attachments to the issue.
     * @param storyId is for getting projectId to update story with attachment URL.
     * @param description is used for mergeing description of user and attachments URL.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadAttachments(storyId: String,description: String) {
        try {
            RetrofitClubhouseClient.getClubhouseUserClient(
                url = BASE_URL
            )
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

                        }
                    }
                })

        } catch (e: Exception) {
            clubhouseExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for updating data fields of Clubhouse.
     */
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

    /**
     * This method is used for gathering issue details to be send to Clubhouse.
     * @param spinnerUser for getting reference of user.
     * @param spinnerStoryType for getting reference of story type.
     * @param spinnerEpic for getting reference of epics.
     * @param spinnerConfidentiality for getting reference of confidentiality
     */
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

    /**
     * This method is used for gathering issue details to be send to Clubhouse.
     * @param editTextStoryName for getting reference of story name.
     * @param editTextStoryDescription for getting reference of story description.
     * @param editTextEstimate for getting reference of story estimate.
     */
    internal fun gatherClubhouseEditTextDetails(
        editTextStoryName: EditText,
        editTextStoryDescription: EditText,
        editTextEstimate: EditText
    ) {
        storyName = editTextStoryName.text.toString()
        storyDescription = editTextStoryDescription.text.toString()
        estimate = editTextEstimate.text.toString()
    }

    /**
     * This method is used for gathering project details to be send to Clubhouse.
     * @param autoTextViewProject for getting reference of project.
     */
    internal fun gatherClubhouseProjectAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
    }

    /**
     * This method is used for gathering project details to be send to Clubhouse.
     * @param projectPosition for getting position of project from spinner.
     */
    internal fun clubhouseProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    /**
     * This method is used for gathering project details to be send to Clubhouse.
     * @param epicPosition for getting position of epic position from spinner.
     */
    internal fun clubhouseEpicPosition(epicPosition: Int) {
        this.epicPosition = epicPosition
    }

    /**
     * This method is used for gathering project details to be send to Clubhouse.
     * @param userPosition for getting position of user position from spinner.
     */
    internal fun clubhouseUserPosition(userPosition: Int) {
        this.spinnerPositionUser = userPosition
    }

    /**
     * This method is used for gathering story type details to be send to Clubhouse.
     * @param storyTypePosition for getting position of story type from spinner.
     */
    internal fun clubhouseStoryTypePosition(storyTypePosition: Int){
        this.spinnerPositionStoryType = storyTypePosition
    }

    /**
     * This method is used for checking time for time out situation.
     * @param activity is used for getting reference of current activity.
     */
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

    /**
     * This method is used for handling exceptions of Slack Api.
     * @param e is used for defining exception.
     * @param filePathName is used getting filepath of the recorded media.
     * @param throwable is used for defining throwable
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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