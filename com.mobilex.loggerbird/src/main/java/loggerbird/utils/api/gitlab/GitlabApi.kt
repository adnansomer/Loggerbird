package loggerbird.utils.api.gitlab

import android.app.Activity
import loggerbird.adapter.recyclerView.api.gitlab.RecyclerViewGitlabAttachmentAdapter
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.mobilex.loggerbird.R
import loggerbird.constants.Constants
import loggerbird.exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import loggerbird.models.*
import okhttp3.*
import loggerbird.services.LoggerBirdService
import java.io.File
import java.io.IOException
import java.util.*
import loggerbird.utils.other.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import com.google.gson.JsonObject
import loggerbird.models.api.gitlab.GitlabLabelsModel
import loggerbird.models.api.gitlab.GitlabMilestonesModel
import loggerbird.models.api.gitlab.GitlabProjectModel
import loggerbird.models.api.gitlab.GitlabUsersModel
import loggerbird.observers.LogFragmentLifeCycleObserver
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import loggerbird.utils.other.InternetConnectionUtil
import loggerbird.utils.other.LinkedBlockingQueueUtil
import java.net.*

/** Loggerbird Gitlab api configuration class **/
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
    internal var confidentiality: String? = null
    private var projectPosition = 0
    private var assigneePosition = 0
    private var labelPosition = 0
    private var milestonePosition = 0
    private var confidentialityPosition = 0
    private val arrayListProjects: ArrayList<String> = ArrayList()
    private val arrayListProjectsId: ArrayList<String> = ArrayList()
    private val arrayListMilestones: ArrayList<String> = ArrayList()
    private val arrayListMilestonesId: ArrayList<String> = ArrayList()
    internal val arrayListLabels: ArrayList<String> = ArrayList()
    private val arrayListLabelsId: ArrayList<String> = ArrayList()
    private val arrayListUsers: ArrayList<String> = ArrayList()
    private val arrayListUsersId: ArrayList<String> = ArrayList()
    private val arrayListConfidentiality: ArrayList<String> = arrayListOf("False", "True")
    private val arrayListAttachments: ArrayList<String> = ArrayList()
    private var hashMapProjects: HashMap<String, String> = HashMap()
    private var hashMapMilestones: HashMap<String, String> = HashMap()
    private var hashMapLabels: HashMap<String, String> = HashMap()
    private var hashMapUsers: HashMap<String, String> = HashMap()
    private lateinit var timerTaskQueue: TimerTask
    private lateinit var issueId: String
    internal var dueDate: String? = null
    private var workQueueLinkedGitlabAttachments: LinkedBlockingQueueUtil = LinkedBlockingQueueUtil()
    private var runnableListGitlabAttachments: ArrayList<Runnable> = ArrayList()
    private val defaultToast = DefaultToast()
    /**
     * This method is used for calling Slack Api in order to determine operation.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param task is used for determining the task.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
            val stringBuilder = StringBuilder()
            if (description != null) {
                stringBuilder.append(description + "\n")
            }
            if (title != null) {
                jsonObject.addProperty("title", title)
            }
            stringBuilder.append("Life Cycle Details:" + "\n")
            var classCounter = 0
            LoggerBird.classPathList.forEach {
                stringBuilder.append("$it (${LoggerBird.classPathListCounter[classCounter]})\n")
                classCounter++
            }
            jsonObject.addProperty("description", stringBuilder.toString())
            jsonObject.addProperty("milestone_id", hashMapMilestones[arrayListMilestones[milestonePosition]])
            jsonObject.addProperty("labels", labels)
            jsonObject.addProperty("assignee_ids", hashMapUsers[arrayListUsers[assigneePosition]])
            if (weight != null) { jsonObject.addProperty("weight", weight) }
            if (dueDate != null) { jsonObject.addProperty("due_date", dueDate) }
            jsonObject.addProperty("confidential", confidentiality!!.toLowerCase())

            RetrofitGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/" + hashMapProjects[arrayListProjects[projectPosition]] + "/")
                .create(AccountIdService::class.java)
                .createGitlabIssue(jsonObject = jsonObject)
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
                        gitlabExceptionHandler(throwable = t)
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
                        gitlabExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabMilestonesModel>>,
                        response: retrofit2.Response<List<GitlabMilestonesModel>>
                    ) {

                        if (response.code() !in 200..299) {
                            gitlabExceptionHandler()
                        }else{
                            val coroutineCallGitlabDetails = CoroutineScope(Dispatchers.IO)
                            coroutineCallGitlabDetails.async {
                                Log.d("gitlabmilestones", response.code().toString())
                                val gitlab = response.body()
                                Log.d("gitlabmilestones", gitlab.toString())

                                val gitlabMilestonesList = response.body()

                                arrayListMilestones.add("")
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
                        gitlabExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabLabelsModel>>,
                        response: retrofit2.Response<List<GitlabLabelsModel>>
                    ) {

                        if (response.code() !in 200..299) {
                            gitlabExceptionHandler()
                        }else{
                            val coroutineCallGitlabDetails = CoroutineScope(Dispatchers.IO)
                            coroutineCallGitlabDetails.async {
                                Log.d("gitlablabels", response.code().toString())
                                val gitlab = response.body()
                                Log.d("gitlablabels", gitlab.toString())

                                val gitlabLabelsList = response.body()
                                arrayListLabels.add("")
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
                        gitlabExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabUsersModel>>,
                        response: retrofit2.Response<List<GitlabUsersModel>>
                    ) {
                        if (response.code() !in 200..299) {
                            gitlabExceptionHandler()
                        }else{
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
                        gitlabExceptionHandler(throwable = t)
                    }
                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        if (response.code() !in 200..299) {
                            gitlabExceptionHandler()
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
                    stringBuilder.append("\n" + "Life Cycle Details" + LoggerBird.stringBuilderActivityLifeCycleObserver.toString() + LogFragmentLifeCycleObserver.stringBuilderFragmentLifeCycleObserver.toString())
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
                        if (response.code() !in 200..299) {
                            gitlabExceptionHandler()
                        }else{
                            val coroutineCallGitlabAttachments = CoroutineScope(Dispatchers.IO)
                            coroutineCallGitlabAttachments.async {
                                val gitlabAttachments = response.body()
                                Log.d("gitlab_attachment_result", response.code().toString())
                                Log.d("gitlab_attachment_result", response.body().toString())
                                LoggerBirdService.loggerBirdService.finishShareLayout("gitlab")
                            }
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
     * This method is used for gathering project details to be send to Gitlab.
     * @param autoTextViewProject for getting reference of project.
     */
    internal fun gatherGitlabProjectAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView,
        autoTextViewLabels: AutoCompleteTextView,
        autoTextViewConfidentiality: AutoCompleteTextView,
        autoTextViewMilestone : AutoCompleteTextView,
        autoTextViewAssignee : AutoCompleteTextView
    ) {

        project = autoTextViewProject.editableText.toString()
        labels = autoTextViewLabels.editableText.toString()
        confidentiality = autoTextViewConfidentiality.editableText.toString()
        milestones = autoTextViewMilestone.editableText.toString()
        assignee = autoTextViewAssignee.editableText.toString()
    }

    /**
     * This method is used for gathering project details to be send to Gitlab.
     * @param projectPosition for getting position of project from spinner.
     */
    internal fun gitlabProjectPosition(projectPosition: Int) {

        this.projectPosition = projectPosition
    }

    /**
     * This method is used for gathering project details to be send to Gitlab.
     * @param labelPosition for getting position of assignee from spinner.
     */
    internal fun gitlabAssigneePosition(assigneePosition: Int) {

        this.assigneePosition = assigneePosition
    }

    /**
     * This method is used for gathering project details to be send to Gitlab.
     * @param labelPosition for getting position of label.
     */
    internal fun gitlabLabelPosition(labelPosition: Int) {

        this.labelPosition = labelPosition
    }

    /**
     * This method is used for issue milestones details to be send to Gitlab.
     * @param milestonePosition for getting position of milestone from spinner.
     */
    internal fun gitlabMilestonesPosition(milestonePosition: Int) {

        this.milestonePosition = milestonePosition
    }

    /**
     * This method is used for gathering issue confidentiality details to be send to Gitlab.
     * @param confidentialityPosition for getting position of confidentiality from spinner.
     */
    internal fun gitlabConfidentialityPosition(confidentialityPosition: Int) {

        this.confidentialityPosition = confidentialityPosition
    }

    /**
     * This method is used for checking assignee reference exist in the assginee list or not empty in the AutoCompleteTextView field in the Gitlab layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewAssignee is used for getting reference of assignee autoCompleteTextView in the Gitlab layout.
     * @return Boolean value.
     */
    internal fun checkGitlabProject(
        activity: Activity,
        autoTextViewProject: AutoCompleteTextView
    ): Boolean {
        if (arrayListProjects.contains(autoTextViewProject.editableText.toString()) || autoTextViewProject.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(activity = activity, toastMessage = activity.resources.getString(R.string.textView_gitlab_project_doesnt_exist))
        }
        return false
    }

    /**
     * This method is used for checking assignee reference exist in the assginee list or not empty in the AutoCompleteTextView field in the Gitlab layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewAssignee is used for getting reference of assignee autoCompleteTextView in the Gitlab layout.
     * @return Boolean value.
     */
    internal fun checkGitlabAssignee(
        activity: Activity,
        autoTextViewAssignee: AutoCompleteTextView
    ): Boolean {
        if (arrayListUsers.contains(autoTextViewAssignee.editableText.toString()) || autoTextViewAssignee.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(activity = activity, toastMessage = activity.resources.getString(R.string.textView_gitlab_assignee_doesnt_exist))
        }
        return false
    }

    /**
     * This method is used for checking milestone reference exist in the milestone list or not empty in the AutoCompleteTextView field in the Gitlab layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewAssignee is used for getting reference of milestone autoCompleteTextView in the Gitlab layout.
     * @return Boolean value.
     */
    internal fun checkGitlabMilestone(
        activity: Activity,
        autoTextViewMilestone: AutoCompleteTextView
    ): Boolean {
        if (arrayListMilestones.contains(autoTextViewMilestone.editableText.toString()) || autoTextViewMilestone.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(activity = activity, toastMessage = activity.resources.getString(R.string.textView_gitlab_milestone_doesnt_exist))
        }
        return false
    }

    /**
     * This method is used for checking label reference exist in the label list or not empty in the label AutoCompleteTextView field in the Gitlab layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewAssignee is used for getting reference of member autoCompleteTextView in the Gitlab layout.
     * @return Boolean value.
     */
    internal fun checkGitlabLabel(
        activity: Activity,
        autoTextViewLabel: AutoCompleteTextView
    ): Boolean {
        if (arrayListLabels.contains(autoTextViewLabel.editableText.toString()) || autoTextViewLabel.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(activity = activity, toastMessage = activity.resources.getString(R.string.textView_gitlab_label_doesnt_exist))
        }
        return false
    }

    /**
     * This method is used for handling exceptions of Slack Api.
     * @param e is used for defining loggerbird.exception.
     * @param filePathName is used getting filepath of the recorded media.
     * @param throwable is used for defining throwable
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun gitlabExceptionHandler(
        e: Exception? = null,
        filePathName: File? = null,
        throwable: Throwable? = null
    ) {
        when (throwable) {
            is SocketTimeoutException -> {
                LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error_time_out")
            }
            is IOException -> {
                LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error_time_out")
            }
            else -> {
                LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error")
            }
        }
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.gitlabTag,
            throwable = throwable
        )
    }
}
