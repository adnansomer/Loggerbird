package utils

import android.app.Activity
import adapter.RecyclerViewGitlabAdapter
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
    private var user: String? = null
    private val arrayListUsers: ArrayList<String> = ArrayList()
    private val arrayListUsersId: ArrayList<String> = ArrayList()
    private var hashMapUsers: HashMap<String, String> = HashMap()

    private val arrayListStoryType: ArrayList<String> = arrayListOf("Bug","Chore","Feature")
    private var spinnerPositionStoryType: Int = 0
    private var storyTypePosition = 0
    private var storyType: String? = null

    private var projectPosition = 0
    private var project: String? = null
    private val arrayListProjectId: ArrayList<String> = ArrayList()
    private val arrayListProjectName: ArrayList<String> = ArrayList()
    private var hashMapProjects: HashMap<String, String> = HashMap()

    private var storyName: String?=null
    private var storyDescription: String?=null
    internal var dueDate: String = ""


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
                gatherClubhouseProjectDetails()
                gatherClubhouseUserDetails()

            } catch (e: Exception) {
                LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error")
                clubhouseExceptionHandler(e = e, filePathName = filePathMedia)

            }
        }
    }

    private suspend fun gatherClubhouseProjectDetails(){
        try {
            RetrofitUserClubhouseClient.getClubhouseUserClient(url = "https://api.clubhouse.io/api/v3/")
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
            RetrofitUserClubhouseClient.getClubhouseUserClient(url = "https://api.clubhouse.io/api/v3/")
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
                                val id = it.asJsonObject["id"].asString
                                val name = it.asJsonObject["profile"].asJsonObject["name"].asString
                                arrayListUsersId.add(id)
                                arrayListUsers.add(name)
                                hashMapUsers[name!!] = id!!
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
            RetrofitUserClubhouseClient.getClubhouseUserClient(url = "https://api.clubhouse.io/api/v3/")
                .create(AccountIdService::class.java)
                .createClubhouseStory(
                    token = LoggerBird.clubhouseApiToken,
                    project_id = hashMapProjects[arrayListProjectName[projectPosition]].toString(),
                    name = storyName!!,
                    description = storyDescription!!,
                    storyType = storyType!!.toLowerCase(),
                    deadline = dueDate!!)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(call: retrofit2.Call<JsonObject>, t: Throwable) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.clubhouseTag)
                    }

                    override fun onResponse(call: retrofit2.Call<JsonObject>, response: retrofit2.Response<JsonObject>) {
                        coroutineCallClubhouseIssue.async {
                            activity.runOnUiThread {
                                LoggerBirdService.loggerBirdService.buttonClubhouseCancel.performClick()
                            }
                            if (response.code() in 400..499) {
                                LoggerBirdService.loggerBirdService.finishShareLayout("clubhouse_error")
                            }
                            Log.d("clubhousecreate", response.code().toString())
                            val freshdesk = response.body()
                            Log.d("clubhousecreate", freshdesk.toString())
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateFields() {
        timerTaskQueue.cancel()
        activity.runOnUiThread {
            LoggerBirdService.loggerBirdService.initializeClubhouseSpinner(
                arrayListClubhouseRequester = arrayListUsers,
                arrayListClubhouseProjects = arrayListProjectName,
                arrayListClubhouseStoryType = arrayListStoryType
            )
        }
    }

    internal fun gatherClubhouseSpinnerDetails(
        spinnerUser: Spinner,
        spinnerStoryType: Spinner
    ) {
        spinnerPositionUser = spinnerUser.selectedItemPosition
        user = spinnerUser.selectedItem.toString()
        spinnerPositionStoryType = spinnerStoryType.selectedItemPosition
        storyType = spinnerStoryType.selectedItem.toString()

    }

    internal fun gatherClubhouseEditTextDetails(
        editTextStoryName: EditText,
        editTextStoryDescription: EditText
    ) {
        storyName = editTextStoryName.text.toString()
        storyDescription = editTextStoryDescription.text.toString()
    }

    internal fun gatherClubhouseProjectAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
    }

    internal fun clubhouseProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
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
        timerQueue.schedule(timerTaskQueue, 180000)
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