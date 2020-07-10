package utils.api.gitlab

import android.app.Activity
import adapter.recyclerView.api.gitlab.RecyclerViewGitlabAttachmentAdapter
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
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.google.gson.JsonObject
import models.api.gitlab.GitlabLabelsModel
import models.api.gitlab.GitlabMilestonesModel
import models.api.gitlab.GitlabProjectModel
import models.api.gitlab.GitlabUsersModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import utils.other.InternetConnectionUtil
import utils.other.LinkedBlockingQueueUtil

/** Loggerbird Gitlab api configration class **/
internal class GitlabApi {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpGitlab = CoroutineScope(Dispatchers.IO)
    private val coroutineCallGitlab = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var title: String? = ""
    private var description: String? = ""
    private var weight: String? = ""
    private var assignee: String? = null
    private var labels: String? = null
    private var milestones: String? = null
    private var project: String? = null
    private var confidentiality: String? = null
    private var spinnerPositionProject: Int = 0
    private var spinnerPositionLabels: Int = 0
    private var spinnerPositionAssignee: Int = 0
    private var spinnerPositionMilestones: Int = 0
    private var spinnerPositionConfidentiality: Int = 0
    private var projectPosition = 0
    private var assigneePosition = 0
    private var labelPosition = 0
    private var milestonePosition = 0
    private var confidentialityPosition = 0
    private val arrayListProjects: ArrayList<String> = ArrayList()
    private val arrayListProjectsId: ArrayList<String> = ArrayList()
    private val arrayListMilestones: ArrayList<String> = ArrayList()
    private val arrayListMilestonesId: ArrayList<String> = ArrayList()
    private val arrayListLabels: ArrayList<String> = ArrayList()
    private val arrayListLabelsId: ArrayList<String> = ArrayList()
    private val arrayListUsers: ArrayList<String> = ArrayList()
    private val arrayListUsersId: ArrayList<String> = ArrayList()
    private val arrayListConfidentiality: ArrayList<String> = ArrayList()
    private val arrayListAttachments: ArrayList<String> = ArrayList()
    private var hashMapProjects: HashMap<String, String> = HashMap()
    private var hashMapMilestones: HashMap<String, String> = HashMap()
    private var hashMapLabels: HashMap<String, String> = HashMap()
    private var hashMapUsers: HashMap<String, String> = HashMap()
    private lateinit var timerTaskQueue: TimerTask
    private lateinit var issueId: String
    internal var dueDate: String? = null
    private var workQueueLinkedGitlabAttachments: LinkedBlockingQueueUtil =
        LinkedBlockingQueueUtil()
    private var runnableListGitlabAttachments: ArrayList<Runnable> = ArrayList()
    /**
     * This method is used for calling Slack Api in order to determine operation.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param task is used for determining the task.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun callGitlab(
        activity: Activity,
        context: Context,
        task: String,
        filePathMedia: File? = null
    ) {
        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        coroutineCallOkHttpGitlab.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    checkQueueTime(activity = activity)
                    okHttpGitlabAuthentication(
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
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
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
    private fun okHttpGitlabAuthentication(
        context: Context,
        activity: Activity,
        task: String,
        filePathMediaName: File?
    ) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://gitlab.com")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.d("gitlab_response_message", response.message)
                Log.d("gitlab_response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallGitlab.async {
                            try {
                                when (task) {
                                    "create" -> gitlabCreateIssue(
                                        activity = activity,
                                        context = context,
                                        filePathMedia = filePathMedia
                                    )

                                    "get" -> gatherGitlabDetails(
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
                                    tag = Constants.slackTag
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
                    LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
            }

        })
    }

    /**
     * This method is used for creating issue for Gitlab with using Api.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun gitlabCreateIssue(
        activity: Activity,
        context: Context,
        filePathMedia: File?
    ) {
        try {
            this.activity = activity
            val coroutineCallGitlabIssue = CoroutineScope(Dispatchers.IO)
            val jsonObject = JsonObject()
            if (title != null) {
                jsonObject.addProperty("title", title)
            }
            if (description != null) {
                jsonObject.addProperty("description", description)
            }
            jsonObject.addProperty(
                "milestone_id",
                hashMapMilestones[arrayListMilestones[spinnerPositionMilestones]]
            )
            jsonObject.addProperty("labels", labels)
            jsonObject.addProperty("assignee_ids", hashMapUsers[arrayListUsers[spinnerPositionAssignee]])
            if (weight != null) {
                jsonObject.addProperty("weight", weight)
            }
            if (dueDate != null) {
                jsonObject.addProperty("due_date", dueDate)
            }
            jsonObject.addProperty("confidential", confidentiality)

            RetrofitGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/" + hashMapProjects[arrayListProjects[projectPosition]] + "/")
                .create(AccountIdService::class.java)
                .createGitlabIssue(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        if (response.code() in 400..499) {
                            LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error")
                        }
                        Log.d("gitlab", response.code().toString())
                        val gitlab = response.body()
                        Log.d("gitlab", gitlab.toString())

                        coroutineCallGitlabIssue.async {
                            issueId = response.body()!!["iid"].asString
                            RecyclerViewGitlabAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                                val file = it.file
                                if (file.exists()) {
                                    callGitlabAttachments(
                                        issueId = issueId,
                                        file = file,
                                        projectId = arrayListProjectsId[projectPosition]
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
     * @param file is used getting filepath of the recorded media.
     * @param issueId is for getting issueId to update already opened issue.
     * @param projectId is for getting projectId to update already opened issue.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun callGitlabAttachments(file: File, issueId: String, projectId: String) {
        if (LoggerBird.isLogInitAttached()) {
            if (runnableListGitlabAttachments.isEmpty()) {
                workQueueLinkedGitlabAttachments.put {
                    createAttachments(projectId = projectId,filePathMedia  = file, issueId = issueId)
                }
            }
            runnableListGitlabAttachments.add(Runnable {
                createAttachments(projectId = projectId, filePathMedia = file, issueId = issueId)
            })
        } else {
            throw LoggerBirdException(Constants.logInitErrorMessage)
        }
    }

    /**
     * This method is used for gathering all details to be send to Gitlab.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun gatherGitlabProjectDetails() {
        try {
            RetrofitGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/")
                .create(AccountIdService::class.java)
                .getGitlabProjects()
                .enqueue(object : retrofit2.Callback<List<GitlabProjectModel>> {
                    override fun onFailure(
                        call: retrofit2.Call<List<GitlabProjectModel>>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabProjectModel>>,
                        response: retrofit2.Response<List<GitlabProjectModel>>
                    ) {
                        val coroutineCallGitlabDetails = CoroutineScope(Dispatchers.IO)
                        coroutineCallGitlabDetails.async {
                            Log.d("gitlabprojects", response.code().toString())
                            val gitlab = response.body()
                            Log.d("gitlabprojects", gitlab.toString())

                            val gitlabList = response.body()
                            gitlabList?.forEach {
                                if (it.id != null) {
                                    arrayListProjects.add(it.name!!)
                                    arrayListProjectsId.add(it.id!!)
                                    hashMapProjects[it.name!!] = it.id!!
                                }
                            }
                            gatherGitlabMilestonesDetails(projectId = arrayListProjectsId[projectPosition])
                            gatherGitlabLabelsDetails(projectId = arrayListProjectsId[projectPosition])
                            gatherGitlabUsersDetails(projectId = arrayListProjectsId[projectPosition])
                            updateFields()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
        }
    }

    /**
     * This method is used for gathering Gitlab milestone data.
     * @param projectId is for getting projectId to gather relevant milestones of the project.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun gatherGitlabMilestonesDetails(projectId: String) {
        try {
            RetrofitGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/$projectId/")
                .create(AccountIdService::class.java)
                .getGitlabMilestones()
                .enqueue(object : retrofit2.Callback<List<GitlabMilestonesModel>> {

                    override fun onFailure(
                        call: retrofit2.Call<List<GitlabMilestonesModel>>, t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabMilestonesModel>>,
                        response: retrofit2.Response<List<GitlabMilestonesModel>>
                    ) {

                        val coroutineCallGitlabDetails = CoroutineScope(Dispatchers.IO)
                        coroutineCallGitlabDetails.async {
                            Log.d("gitlabmilestones", response.code().toString())
                            val gitlab = response.body()
                            Log.d("gitlabmilestones", gitlab.toString())

                            val gitlabMilestonesList = response.body()
                            gitlabMilestonesList?.forEach {
                                if (it.id != null) {
                                    arrayListMilestones.add(it.title!!)
                                    arrayListMilestonesId.add(it.id!!)
                                    hashMapMilestones[it.title!!] = it.id!!
                                }
                            }
                            updateFields()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
        }
    }

    /**
     * This method is used for gathering Gitlab labels data.
     * @param projectId is for getting projectId to gather relevant labels of the project.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun gatherGitlabLabelsDetails(projectId: String) {
        try {
            RetrofitGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/$projectId/")
                .create(AccountIdService::class.java)
                .getGitlabLabels()
                .enqueue(object : retrofit2.Callback<List<GitlabLabelsModel>> {

                    override fun onFailure(
                        call: retrofit2.Call<List<GitlabLabelsModel>>, t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabLabelsModel>>,
                        response: retrofit2.Response<List<GitlabLabelsModel>>
                    ) {

                        val coroutineCallGitlabDetails = CoroutineScope(Dispatchers.IO)
                        coroutineCallGitlabDetails.async {
                            Log.d("gitlablabels", response.code().toString())
                            val gitlab = response.body()
                            Log.d("gitlablabels", gitlab.toString())

                            val gitlabLabelsList = response.body()
                            gitlabLabelsList?.forEach {
                                if (it.id != null) {
                                    arrayListLabels.add(it.name!!)
                                    arrayListLabelsId.add(it.id!!)
                                    hashMapLabels[it.name!!] = it.id!!
                                }
                            }
                            updateFields()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
        }
    }

    /**
     * This method is used for gathering Gitlab users.
     * @param projectId is for getting projectId to gather relevant users of project.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun gatherGitlabUsersDetails(projectId: String) {
        try {
            RetrofitGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/$projectId/")
                .create(AccountIdService::class.java)
                .getGitlabUsers()
                .enqueue(object : retrofit2.Callback<List<GitlabUsersModel>> {

                    override fun onFailure(
                        call: retrofit2.Call<List<GitlabUsersModel>>, t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabUsersModel>>,
                        response: retrofit2.Response<List<GitlabUsersModel>>
                    ) {

                        val coroutineCallGitlabDetails = CoroutineScope(Dispatchers.IO)
                        coroutineCallGitlabDetails.async {
                            val gitlab = response.body()
                            Log.d("gitlabusers", gitlab.toString())

                            val gitlabMilestonesList = response.body()
                            gitlabMilestonesList?.forEach {
                                if (it.id != null) {
                                    arrayListUsers.add(it.name!!)
                                    arrayListUsersId.add(it.id!!)
                                    hashMapUsers[it.name!!] = it.id!!
                                }
                            }
                            updateFields()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
        }
    }

    /**
     * This method is used for gathering all details to be send to Gitlab.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private suspend fun gatherGitlabDetails(
        activity: Activity,
        context: Context,
        filePathMedia: File?
    ) {
        val coroutineCallGatherDetails = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherDetails.async(Dispatchers.IO) {
            try {
                arrayListProjects.clear()
                arrayListProjectsId.clear()
                arrayListMilestones.clear()
                arrayListMilestonesId.clear()
                arrayListLabels.clear()
                arrayListLabelsId.clear()
                arrayListUsers.clear()
                arrayListUsersId.clear()
                arrayListAttachments.clear()
                hashMapProjects.clear()
                hashMapMilestones.clear()
                hashMapLabels.clear()
                hashMapUsers.clear()
                arrayListConfidentiality.clear()
                arrayListConfidentiality.add("false")
                arrayListConfidentiality.add("true")
                gatherGitlabProjectDetails()

            } catch (e: Exception) {
                LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error")
                gitlabExceptionHandler(e = e, filePathName = filePathMedia)

            }
        }
    }

    /**
     * This method is used for creating attachments and their URL to be send as an attachment.
     * @param projectId is for getting projectId to update project with attachment URL.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param issueId is for getting projectId to update issue with attachment URL.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAttachments(projectId: String, filePathMedia: File?, issueId: String) {
        try {
            val jsonObject = JsonObject()
            val requestFile = filePathMedia!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val filePathMediaName = "@" +filePathMedia!!.name
            val body = MultipartBody.Part.createFormData("file", filePathMediaName, requestFile)

            RetrofitGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/" + hashMapProjects[arrayListProjects[projectPosition]] + "/")
                .create(AccountIdService::class.java)
                .sendGitlabAttachments(file = body)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }
                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        if (response.code() in 400..499) {
                            LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error")
                        }

                        if (filePathMedia!!.name != "logger_bird_details.txt") {
                            if (filePathMedia!!.exists()) {
                                filePathMedia!!.delete()
                            }
                        }
                        val coroutineCallGitlanAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallGitlanAttachments.async {
                            Log.d("gitlab_attachment", response.code().toString())
                            Log.d("gitlab_attachment", response.body().toString())

                            if (response.body() != null) {
                                arrayListAttachments.add("https://gitlab.com" + response.body()!!.asJsonObject["full_path"].asString)

                                callEnqueueGitlabAttachments(issueId = issueId, projectId = projectId)
                            }
                        }
                    }
                })
        }catch (e: Exception) {
            gitlabExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for calling attachments consecutively from queue.
     * @param issueId is for getting issueId to update issue with attachment URL.
     * @param projectId is for getting projectId to update issue with attachment URL.
     */
    private fun callEnqueueGitlabAttachments(issueId: String, projectId: String) {
        workQueueLinkedGitlabAttachments.controlRunnable = false
        if (runnableListGitlabAttachments.size > 0) {
            runnableListGitlabAttachments.removeAt(0)
            if (runnableListGitlabAttachments.size > 0) {
                workQueueLinkedGitlabAttachments.put(runnableListGitlabAttachments[0])
            } else {
                if (arrayListAttachments.isNotEmpty()) {
                    var attachmentCounter = 1
                    val stringBuilder = StringBuilder()
                    arrayListAttachments.forEach {
                        stringBuilder.append("\nattachment_$attachmentCounter:$it")
                        attachmentCounter++
                    }
                    val updatedDescription = "$description\n" + stringBuilder.toString()
                    addAttachmentsToIssue(issueId =  issueId, projectId = projectId, description = updatedDescription.toString())

                } else {
                    LoggerBirdService.loggerBirdService.finishShareLayout("gitlab")
                }

            }
        } else {
            LoggerBirdService.loggerBirdService.finishShareLayout("gitlab")
        }
    }

    /**
     * This method is used for sending attachments to the issue.
     * @param projectId is for getting projectId to update project with attachment URL.
     * @param description is used for mergeing description of user and attachments URL.
     * @param issueId is for getting projectId to update issue with attachment URL.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun addAttachmentsToIssue(projectId: String, issueId: String, description: String) {
        try {
            RetrofitGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/" + hashMapProjects[arrayListProjects[projectPosition]] + "/issues/")
                .create(AccountIdService::class.java)
                .setGitlabIssue(description = description, iid = issueId)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        gitlabExceptionHandler(throwable = t)
                    }
                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {

                        if (response.code() in 400..499) {
                            LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error") }
                        else{
                            LoggerBirdService.loggerBirdService.finishShareLayout("gitlab")
                        }
                        val coroutineCallGitlabAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallGitlabAttachments.async {
                            val gitlabAttachments = response.body()
                            Log.d("gitlab_attachment_result", response.code().toString())
                            Log.d("gitlab_attachment_result", response.body().toString())
                        }
                    }
                })
        } catch (e: Exception) {
            gitlabExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for updating data fields of Gitlab.
     */
    private fun updateFields() {
        timerTaskQueue.cancel()
        activity.runOnUiThread {
            LoggerBirdService.loggerBirdService.initializeGitlabSpinner(
                arrayListGitlabProjects = arrayListProjects,
                arrayListGitlabAssignee = arrayListUsers,
                arrayListGitlabMilestones = arrayListMilestones,
                arrayListGitlabLabels = arrayListLabels,
                arrayListGitlabConfidentiality = arrayListConfidentiality
            )
        }
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
                    LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 180000)
    }


    /**
     * This method is used for gathering issue details to be send to Gitlab.
     * @param editTextTitle for getting reference of issue title.
     * @param editTextDescription for getting reference of issue description.
     * @param editTextWeight for getting reference of issue weight.
     */
    internal fun gatherGitlabEditTextDetails(
        editTextTitle: EditText,
        editTextDescription: EditText,
        editTextWeight: EditText
    ) {
        title = editTextTitle.text.toString()
        description = editTextDescription.text.toString()
        weight = editTextWeight.text.toString()
    }

    /**
     * This method is used for gathering issue details to be send to Gitlab.
     * @param spinnerAssignee for getting reference of assignee.
     * @param spinnerMilestone for getting reference of milestone.
     * @param spinnerLabels for getting reference of labels.
     * @param spinnerConfidentiality for getting reference of confidentiality
     */
    internal fun gatherGitlabProjectSpinnerDetails(
        spinnerAssignee: Spinner,
        spinnerMilestone: Spinner,
        spinnerLabels: Spinner,
        spinnerConfidentiality: Spinner

    ) {

        spinnerPositionConfidentiality = spinnerAssignee.selectedItemPosition
        confidentiality = spinnerConfidentiality.selectedItem.toString()

        spinnerPositionAssignee = spinnerAssignee.selectedItemPosition
        assignee = spinnerAssignee.selectedItem.toString()

        spinnerPositionLabels = spinnerLabels.selectedItemPosition
        labels = spinnerMilestone.selectedItem.toString()

        spinnerPositionMilestones = spinnerMilestone.selectedItemPosition
        milestones = spinnerLabels.selectedItem.toString()
    }

    /**
     * This method is used for gathering project details to be send to Gitlab.
     * @param autoTextViewProject for getting reference of project.
     */
    internal fun gatherGitlabProjectAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView
    ) {

        project = autoTextViewProject.editableText.toString()
    }

    /**
     * This method is used for gathering project details to be send to Gitlab.
     * @param autoTextViewProject for getting position of project from spinner.
     */
    internal fun gitlabProjectPosition(projectPosition: Int) {

        this.projectPosition = projectPosition
    }

    /**
     * This method is used for gathering project details to be send to Gitlab.
     * @param autoTextViewProject for getting position of assignee from spinner.
     */
    internal fun gitlabAssigneePosition(assigneePosition: Int) {

        this.spinnerPositionAssignee = assigneePosition
    }

    /**
     * This method is used for gathering project details to be send to Gitlab.
     * @param autoTextViewProject for getting position of label.
     */
    internal fun gitlabLabelPosition(labelPosition: Int) {

        this.spinnerPositionLabels = labelPosition
    }

    /**
     * This method is used for issue milestones details to be send to Gitlab.
     * @param milestonePosition for getting position of milestone from spinner.
     */
    internal fun gitlabMilestonesPosition(milestonePosition: Int) {

        this.spinnerPositionMilestones = milestonePosition
    }

    /**
     * This method is used for gathering issue confidentiality details to be send to Gitlab.
     * @param confidentialityPosition for getting position of confidentiality from spinner.
     */
    internal fun gitlabConfidentialityPosition(confidentialityPosition: Int) {

        this.spinnerPositionConfidentiality = confidentialityPosition
    }

    /**
     * This method is used for handling exceptions of Slack Api.
     * @param e is used for defining exception.
     * @param filePathName is used getting filepath of the recorded media.
     * @param throwable is used for defining throwable
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun gitlabExceptionHandler(
        e: Exception? = null,
        filePathName: File? = null,
        throwable: Throwable? = null
    ) {
        if (this::timerTaskQueue.isInitialized) {
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error")
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.gitlabTag,
            throwable = throwable
        )
    }
}
