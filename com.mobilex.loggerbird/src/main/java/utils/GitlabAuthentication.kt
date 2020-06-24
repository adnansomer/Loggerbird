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
import models.*
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

class GitlabAuthentication {
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
    private var workQueueLinkedGitlabAttachments: LinkedBlockingQueueUtil = LinkedBlockingQueueUtil()
    private var runnableListGitlabAttachments: ArrayList<Runnable> = ArrayList()
    private lateinit var timerTaskQueue: TimerTask
    private lateinit var issueId: String
    internal var dueDate: String? = null

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

            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/" + hashMapProjects[arrayListProjects[projectPosition]] + "/")
                .create(AccountIdService::class.java)
                .createGitlabIssue(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        LoggerBirdService.loggerBirdService.finishShareLayout("gitlab")
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        coroutineCallGitlabIssue.async {

                            issueId = response.body()!!["iid"].asString
                            RecyclerViewGitlabAdapter.ViewHolder.arrayListFilePaths.forEach {
                                val file = it.file
                                if (file.exists()) {
                                    createAttachments(
                                        projectId = arrayListProjectsId[projectPosition],
                                        filePathMedia = filePathMedia,
                                        issueId = issueId
                                    )
                                }
                            }

                            activity.runOnUiThread {
                                LoggerBirdService.loggerBirdService.buttonGitlabCancel.performClick()
                            }
                            if (response.code() in 400..499) {
                                LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error")
                            }
                            Log.d("gitlab", response.code().toString())
                            val gitlab = response.body()
                            Log.d("GITLAB", gitlab.toString())
                            Log.d("GITLAB", issueId.toString())
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun gatherGitlabProjectDetails() {
        try {
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/")
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

    private fun gatherGitlabMilestonesDetails(projectId: String) {
        try {
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/$projectId/")
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

    private fun gatherGitlabLabelsDetails(projectId: String) {
        try {
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/$projectId/")
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

    private fun gatherGitlabUsersDetails(projectId: String) {
        try {
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/$projectId/")
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAttachments(projectId: String, filePathMedia: File?, issueId: String) {
        try {
            val jsonObject = JsonObject()
            val requestFile = filePathMedia!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val filePathMediaName = "@" + filePathMedia!!.name.replace("_", "")
            val body = MultipartBody.Part.createFormData("file", filePathMediaName, requestFile)

            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/" + hashMapProjects[arrayListProjects[projectPosition]] + "/")
                .create(AccountIdService::class.java)
                .sendGitlabAttachments(file = body)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error")
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }
                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        val gitlabAttachments = response.body()
                        arrayListAttachments.add("https://gitlab.com" + response.body()!!.asJsonObject["full_path"].asString)
                        Log.d("gitlab_attachment", response.code().toString())
                        Log.d("gitlab_attachment", response.body().toString())

                        val stringBuilder = StringBuilder()
                        var attachmentCounter = 0
                        arrayListAttachments.forEach {
                            stringBuilder.append("\nattachment_$attachmentCounter:$it")
                            attachmentCounter++
                        }

                        if (filePathMedia.name != "logger_bird_details.txt") {
                            if (filePathMedia.exists()) {
                                filePathMedia.delete()
                            }
                        }

                        addAttachmentsToIssue(projectId = projectId, issueId = issueId, descriptionStringBuilder= stringBuilder.toString())
                    }
                })
            
        } catch (e: Exception) {
            gitlabExceptionHandler(e = e)
        }
    }

//    private fun callEnqueueGitlabAttachments(projectId: String, issueId: String) {
//        workQueueLinkedGitlabAttachments.controlRunnable = false
//        if (runnableListGitlabAttachments.size > 0) {
//            runnableListGitlabAttachments.removeAt(0)
//            if (runnableListGitlabAttachments.size > 0) {
//                workQueueLinkedGitlabAttachments.put(runnableListGitlabAttachments[0])
//            } else {
//                if (arrayListAttachments.isNotEmpty()) {
//                    val stringBuilder = StringBuilder()
//                    if(stringBuilderGitlab.isNotEmpty()){
//                        stringBuilder.append(stringBuilderGitlab.toString())
//                    }
//                    var attachmentCounter = 0
//                    arrayListAttachments.forEach {
//                        stringBuilder.append("\nattachment_$attachmentCounter:$it")
//                        attachmentCounter++
//                    }
//                    val jsonObjectAttachments = JsonObject()
//                    jsonObjectAttachments.addProperty("body", stringBuilder.toString())
//
//                    addAttachmentsToIssue(jsonObjectIssue = jsonObjectAttachments, projectId = projectId, issueId = issueId, descriptionStringBuilder= stringBuilder.toString())
//                } else {
//                    LoggerBirdService.loggerBirdService.finishShareLayout("gitlab")
//                }
//            }
//        } else {
//            LoggerBirdService.loggerBirdService.finishShareLayout("gitlab")
//        }
//    }

    private fun addAttachmentsToIssue(projectId: String, issueId: String, descriptionStringBuilder: String) {
        val coroutineCallAttachments = CoroutineScope(Dispatchers.IO)
        coroutineCallAttachments.async {
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/" + hashMapProjects[arrayListProjects[projectPosition]] + "/issues/")
                .create(AccountIdService::class.java)
                .setGitlabIssue(description = descriptionStringBuilder, iid = issueId)
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
                        Log.d("github_issue_attachment", response.code().toString())
                        arrayListAttachments.clear()
                        val githubList = response.body()
                        activity.runOnUiThread {
                            LoggerBirdService.loggerBirdService.finishShareLayout("gitlab")
                        }
                    }
                })
            }
        }

    private fun updateFields() {
        timerTaskQueue.cancel()
        activity.runOnUiThread {
            LoggerBirdService.loggerBirdService.initializeGitlabSpinner(
                arrayListGitlabProjects = arrayListProjects,
                arrayListGitlabUsers = arrayListUsers,
                arrayListGitlabMilestones = arrayListMilestones,
                arrayListGitlabLabels = arrayListLabels,
                arrayListGitlabConfidentiality = arrayListConfidentiality
            )
        }
    }

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

    internal fun gatherGitlabEditTextDetails(
        editTextTitle: EditText,
        editTextDescription: EditText,
        editTextWeight: EditText
    ) {
        title = editTextTitle.text.toString()
        description = editTextDescription.text.toString()
        weight = editTextWeight.text.toString()
    }

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

    internal fun gatherGitlabProjectAutoTextDetails(
        autoTextViewProject: AutoCompleteTextView
    ) {

        project = autoTextViewProject.editableText.toString()
    }

    internal fun gitlabProjectPosition(projectPosition: Int) {

        this.projectPosition = projectPosition
    }

    internal fun gitlabAssigneePosition(assigneePosition: Int) {

        this.spinnerPositionAssignee = assigneePosition
    }

    internal fun gitlabLabelPosition(labelPosition: Int) {

        this.spinnerPositionLabels = labelPosition
    }

    internal fun gitlabMilestonesPosition(milestonePosition: Int) {

        this.spinnerPositionMilestones = milestonePosition
    }

    internal fun gitlabConfidentialityPosition(confidentialityPosition: Int) {

        this.spinnerPositionConfidentiality = confidentialityPosition
    }

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
