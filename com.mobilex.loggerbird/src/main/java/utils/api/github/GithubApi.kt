package utils.api.github

import adapter.recyclerView.api.github.RecyclerViewGithubAttachmentAdapter
import adapter.recyclerView.api.github.RecyclerViewGithubAssigneeAdapter
import adapter.recyclerView.api.github.RecyclerViewGithubLabelAdapter
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
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird
import models.*
import models.api.github.*
import okhttp3.*
import services.LoggerBirdService
import utils.other.DefaultToast
import utils.other.InternetConnectionUtil
import utils.other.LinkedBlockingQueueUtil
import java.io.File
import java.io.IOException
import java.lang.StringBuilder
import java.nio.file.Files
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/** Loggerbird Gitlab api configration class **/
internal class GithubApi {
    //Global variables.
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpGithub = CoroutineScope(Dispatchers.IO)
    private val coroutineCallGithub = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var title: String = ""
    private var comment: String? = null
    private var assignee: String? = null
    private var labels: String? = null
    private var repos: String? = null
    private var mileStone: String? = null
    private var linkedRequests: String? = null
    private var project: String? = null
    private var arrayListRepo: ArrayList<String> = ArrayList()
    private var arrayListProject: ArrayList<String> = ArrayList()
    private var arrayListProjectUrl: ArrayList<String> = ArrayList()
    private var arrayListAssignee: ArrayList<String> = ArrayList()
    private var arrayListMileStones: ArrayList<String> = ArrayList()
    private var hashMapMileStones: HashMap<String, Int> = HashMap()
    private var arrayListLinkedRequests: ArrayList<String> = ArrayList()
    private var arrayListLinkedRequestsUrl: ArrayList<String> = ArrayList()
    private var hashMapLinkedRequests: HashMap<String, Int> = HashMap()
    private var arrayListLabels: ArrayList<String> = ArrayList()
    private var queueCounter = 0
    private lateinit var timerTaskQueue: TimerTask
    private var repoPosition = 0
    private var mileStonePosition = 0
    private var linkedRequestPosition = 0
    private var projectPosition = 0
    private val defaultToast = DefaultToast()
    private var repoId: Int? = null
    private var workQueueLinkedGithubAttachments: LinkedBlockingQueueUtil =
        LinkedBlockingQueueUtil()
    private var runnableListGithubAttachments: ArrayList<Runnable> = ArrayList()
    private val arrayListAttachmentsUrl: ArrayList<String> = ArrayList()
    private val stringBuilderGithub = StringBuilder()
    /**
     * This method is used for calling an github action with network connection check.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param task is for getting reference of which github action will be executed.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws LoggerBirdException if network connection error occurs.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    internal fun callGithub(
        activity: Activity,
        context: Context,
        task: String,
        filePathMedia: File? = null
    ) {
        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        coroutineCallOkHttpGithub.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    checkQueueTime(activity = activity)
                    okHttpGithubAuthentication(
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
                githubExceptionHandler(e = e)
            }
        }
    }

    /**
     * This method is used for calling an github action with internet connection check.
     * @param context is for getting reference from the application context.
     * @param activity is used for getting reference of current activity.
     * @param task is for getting reference of which github action will be executed.
     * @param filePathMediaName is used for getting the reference of current media file.
     * @throws LoggerBirdException if internet connection error occurs.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun okHttpGithubAuthentication(
        context: Context,
        activity: Activity,
        task: String,
        filePathMediaName: File?
    ) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://api.github.com")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                githubExceptionHandler(e = e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallGithub.async {
                            try {
                                when (task) {
                                    "create" -> githubCreateIssue(activity = activity)
                                    "get" -> gatherGithubDetails()
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                LoggerBird.callEnqueue()
                                LoggerBird.callExceptionDetails(
                                    exception = e,
                                    tag = Constants.githubTag
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
                    githubExceptionHandler(e = e)
                }
            }
        })
    }

    /**
     * This method is used for creating github issue.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun githubCreateIssue(
        activity: Activity
    ) {
        try {
            stringBuilderGithub.clear()
            arrayListAttachmentsUrl.clear()
            this.activity = activity
            val jsonObject = JsonObject()
            if (RecyclerViewGithubLabelAdapter.ViewHolder.arrayListLabelNames.isNotEmpty()) {
                val jsonArrayLabels = JsonArray()
                RecyclerViewGithubLabelAdapter.ViewHolder.arrayListLabelNames.forEach {
                    jsonArrayLabels.add(it.labelName)
                }
                jsonObject.add("labels", jsonArrayLabels)
            } else {
                if (!labels.isNullOrEmpty()) {
                    if (arrayListLabels.isNotEmpty()) {
                        val jsonArrayLabels = JsonArray()
                        jsonArrayLabels.add(labels)
                        jsonObject.add("labels", jsonArrayLabels)
                    }
                }
            }
            if (!mileStone.isNullOrEmpty()) {
                jsonObject.addProperty("milestone", hashMapMileStones[mileStone!!])
            }
            if (RecyclerViewGithubAssigneeAdapter.ViewHolder.arrayListAssigneeNames.isNotEmpty()) {
                val jsonArrayAssignee = JsonArray()
                RecyclerViewGithubAssigneeAdapter.ViewHolder.arrayListAssigneeNames.forEach {
                    jsonArrayAssignee.add(it.assigneeName)
                }
                jsonObject.add("assignees", jsonArrayAssignee)
            } else {
                if (!assignee.isNullOrEmpty()) {
                    jsonObject.addProperty("assignee", assignee)
                }
            }

            if (!comment.isNullOrEmpty()) {
                stringBuilderGithub.append("Comment:$comment\n")
            }
            if (!linkedRequests.isNullOrEmpty() && arrayListLinkedRequestsUrl.size > linkedRequestPosition) {
                stringBuilderGithub.append("Linked Pull Request:" + arrayListLinkedRequestsUrl[linkedRequestPosition] + "\n")
            }
            if (!project.isNullOrEmpty() && arrayListProjectUrl.size > projectPosition) {
                stringBuilderGithub.append("Project:" + arrayListProjectUrl[projectPosition] + "\n")
            }
            if (stringBuilderGithub.isNotEmpty()) {
                jsonObject.addProperty("body", stringBuilderGithub.toString())
            }
            jsonObject.addProperty("title", title)
            RetrofitGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repos/")
                .create(AccountIdService::class.java)
                .createGithubIssue(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        githubExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        val coroutineCallGithubIssue = CoroutineScope(Dispatchers.IO)
                        coroutineCallGithubIssue.async {
                            Log.d("github_details", response.code().toString())
                            val githubList = response.body()
                            repoId = githubList!!["url"].asString.substringAfterLast("/").toInt()
                            RecyclerViewGithubAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                                val file = it.file
                                if (file.exists()) {
                                    callGithubAttachments(
                                        repo = repos!!,
                                        filePathMedia = file
                                    )
                                }
                            }
                            if (RecyclerViewGithubAttachmentAdapter.ViewHolder.arrayListFilePaths.isEmpty()) {
                                activity.runOnUiThread {
                                    LoggerBirdService.loggerBirdService.finishShareLayout("github")
                                }
                            }
                        }
                    }
                })

        } catch (e: Exception) {
            githubExceptionHandler(e = e)
        }
    }

    /**
     * This method is used when attachment methods needs to be in queue and executed in synchronized way.
     * @param filePathMedia is used for getting the reference of current media file.
     * @param repo is used for getting reference of repository name of github.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun callGithubAttachments(filePathMedia: File, repo: String) {
        if (LoggerBird.isLogInitAttached()) {
            if (runnableListGithubAttachments.isEmpty()) {
                workQueueLinkedGithubAttachments.put {
                    createAttachments(repo = repo, file = filePathMedia)
                }
            }
            runnableListGithubAttachments.add(Runnable {
                createAttachments(repo = repo, file = filePathMedia)
            })
        } else {
            throw LoggerBirdException(Constants.logInitErrorMessage)
        }
    }

    /**
     * This method is used for removing attachment task from queue.
     * @param repo is used for getting reference of repository name of github.
     */
    private fun callEnqueueGithubAttachments(repo: String) {
        workQueueLinkedGithubAttachments.controlRunnable = false
        if (runnableListGithubAttachments.size > 0) {
            runnableListGithubAttachments.removeAt(0)
            if (runnableListGithubAttachments.size > 0) {
                workQueueLinkedGithubAttachments.put(runnableListGithubAttachments[0])
            } else {
                if (arrayListAttachmentsUrl.isNotEmpty()) {
                    val stringBuilder = StringBuilder()
                    if (stringBuilderGithub.isNotEmpty()) {
                        stringBuilder.append(stringBuilderGithub.toString())
                    }
                    var attachmentCounter = 0
                    arrayListAttachmentsUrl.forEach {
                        stringBuilder.append("\nattachment_$attachmentCounter:$it")
                        attachmentCounter++
                    }
                    val jsonObjectAttachments = JsonObject()
                    jsonObjectAttachments.addProperty("body", stringBuilder.toString())
                    addAttachmentsToIssue(jsonObjectIssue = jsonObjectAttachments, repo = repo)
                } else {
                    LoggerBirdService.loggerBirdService.finishShareLayout("github")
                }

            }
        } else {
            LoggerBirdService.loggerBirdService.finishShareLayout("github")
        }
    }

    /**
     * This method is used for creating github attachment when github issue created.
     * @param repo is used for getting reference of repository name of github.
     * @param file is used for getting reference of the current file.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAttachments(repo: String, file: File) {
        try {
            val coroutineCallAttachments = CoroutineScope(Dispatchers.IO)
            coroutineCallAttachments.async {

                val jsonObject = JsonObject()
                jsonObject.addProperty(
                    "message",
                    "loggerbirdfile"
                )
                withContext(Dispatchers.IO) {
                    jsonObject.addProperty(
                        "content",
                        Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()))
                    )
                }
                RetrofitGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
                    .create(AccountIdService::class.java)
                    .setGithubAttachments(
                        jsonObject = jsonObject,
                        fileName = file.name.replace("_", "") + System.currentTimeMillis()
                    )
                    .enqueue(object : retrofit2.Callback<JsonObject> {
                        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                        override fun onFailure(
                            call: retrofit2.Call<JsonObject>,
                            t: Throwable
                        ) {
                            githubExceptionHandler(throwable = t)
                        }

                        override fun onResponse(
                            call: retrofit2.Call<JsonObject>,
                            response: retrofit2.Response<JsonObject>
                        ) {
                            if (file.name != "logger_bird_details.txt") {
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                            Log.d("attachment_put_success", response.code().toString())
                            Log.d("attachment_put_succes", response.message())
                            if (response.body() != null) {
                                arrayListAttachmentsUrl.add(response.body()!!["content"].asJsonObject["download_url"].asString)
                                callEnqueueGithubAttachments(repo = repo)
                            }
                        }
                    })
            }
        } catch (e: Exception) {
            githubExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for adding github attachment when github attachment created.
     * @param jsonObjectIssue is used for getting reference json object created in create attachment method.
     * @see createAttachments method.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun addAttachmentsToIssue(jsonObjectIssue: JsonObject, repo: String) {
        val coroutineCallAttachments = CoroutineScope(Dispatchers.IO)
        coroutineCallAttachments.async {
            RetrofitGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
                .create(AccountIdService::class.java)
                .setGithubIssue(jsonObject = jsonObjectIssue, id = repoId!!)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        githubExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        Log.d(
                            "github_issue_attachment",
                            response.code().toString()
                        )
                        val githubList = response.body()
                        activity.runOnUiThread {
                            LoggerBirdService.loggerBirdService.finishShareLayout("github")
                        }
                    }
                })
        }
    }

    /**
     * This method is used for initializing the gathering action of github.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun gatherGithubDetails() {
        try {
            queueCounter = 0
            arrayListRepo.clear()
            arrayListProject.clear()
            arrayListProjectUrl.clear()
            arrayListAssignee.clear()
            arrayListMileStones.clear()
            hashMapMileStones.clear()
            arrayListLabels.clear()
            arrayListLinkedRequests.clear()
            arrayListLinkedRequestsUrl.clear()
            hashMapLinkedRequests.clear()
            gatherTaskRepositories()
        } catch (e: Exception) {
            githubExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for getting repository details for github.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun gatherTaskRepositories() {
        queueCounter++
        RetrofitGithubClient.getGithubUserClient(url = "https://api.github.com/user/")
            .create(AccountIdService::class.java)
            .getGithubRepo()
            .enqueue(object : retrofit2.Callback<List<GithubRepoModel>> {
                override fun onFailure(
                    call: retrofit2.Call<List<GithubRepoModel>>,
                    t: Throwable
                ) {
                    githubExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<List<GithubRepoModel>>,
                    response: retrofit2.Response<List<GithubRepoModel>>
                ) {
                    val coroutineCallGithubRepo = CoroutineScope(Dispatchers.IO)
                    coroutineCallGithubRepo.async {
                        Log.d("github_repo_success", response.code().toString())
                        val githubList = response.body()
                        githubList?.forEach {
                            if (it.name != null) {
                                arrayListRepo.add(it.name!!)
                            }
                        }
                        if (arrayListRepo.size > repoPosition) {
                            gatherTaskProject(repo = arrayListRepo[repoPosition])
                            gatherTaskAssignee(repo = arrayListRepo[repoPosition])
                            gatherTaskLabels(repo = arrayListRepo[repoPosition])
                            gatherTaskMileStone(repo = arrayListRepo[repoPosition])
                            gatherTaskPullRequests(repo = arrayListRepo[repoPosition])
                        }

                        updateFields()

                    }
                }
            })
    }

    /**
     * This method is used for getting assignee details for github.
     * @param repo is used for getting reference of repository name of github.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun gatherTaskAssignee(repo: String) {
        queueCounter++
        RetrofitGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
            .create(AccountIdService::class.java)
            .getGithubAssignees()
            .enqueue(object : retrofit2.Callback<List<GithubAssigneeModel>> {
                override fun onFailure(
                    call: retrofit2.Call<List<GithubAssigneeModel>>,
                    t: Throwable
                ) {
                    githubExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<List<GithubAssigneeModel>>,
                    response: retrofit2.Response<List<GithubAssigneeModel>>
                ) {
                    val coroutineCallGithubAssignee = CoroutineScope(Dispatchers.IO)
                    coroutineCallGithubAssignee.async {
                        Log.d("github_assignee_success", response.code().toString())
                        val githubList = response.body()
                        githubList?.forEach {
                            if (it.login != null) {
                                if (!arrayListAssignee.contains(it.login!!)) {
                                    arrayListAssignee.add(it.login!!)
                                }
                            }
                        }
                        updateFields()
                    }
                }
            })
    }

    /**
     * This method is used for getting project details for github.
     * @param repo is used for getting reference of repository name of github.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun gatherTaskProject(repo: String) {
        queueCounter++
        RetrofitGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
            .create(AccountIdService::class.java)
            .getGithubProjects()
            .enqueue(object : retrofit2.Callback<List<GithubProjectModel>> {
                override fun onFailure(
                    call: retrofit2.Call<List<GithubProjectModel>>,
                    t: Throwable
                ) {
                    githubExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<List<GithubProjectModel>>,
                    response: retrofit2.Response<List<GithubProjectModel>>
                ) {
                    val coroutineCallGithubProject = CoroutineScope(Dispatchers.IO)
                    coroutineCallGithubProject.async {
                        Log.d("github_project_success", response.code().toString())
                        val githubList = response.body()
                        githubList?.forEach {
                            if (it.html_url != null && it.name != null) {
                                arrayListProject.add(it.name!!)
                                arrayListProjectUrl.add(it.html_url!!)
                            }
                        }
                        updateFields()
                    }
                }
            })
    }

    /**
     * This method is used for getting label details for github.
     * @param repo is used for getting reference of repository name of github.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun gatherTaskLabels(repo: String) {
        queueCounter++
        RetrofitGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
            .create(AccountIdService::class.java)
            .getGithubLabels()
            .enqueue(object : retrofit2.Callback<List<GithubLabelsModel>> {
                override fun onFailure(
                    call: retrofit2.Call<List<GithubLabelsModel>>,
                    t: Throwable
                ) {
                    githubExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<List<GithubLabelsModel>>,
                    response: retrofit2.Response<List<GithubLabelsModel>>
                ) {
                    val coroutineCallGithubLabels = CoroutineScope(Dispatchers.IO)
                    coroutineCallGithubLabels.async {
                        Log.d("github_labels_success", response.code().toString())
                        val githubList = response.body()
                        githubList?.forEach {
                            if (it.name != null) {
                                arrayListLabels.add(it.name!!)
                            }
                        }
                        updateFields()
                    }
                }
            })
    }

    /**
     * This method is used for getting milestone details for github.
     * @param repo is used for getting reference of repository name of github.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun gatherTaskMileStone(repo: String) {
        queueCounter++
        RetrofitGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
            .create(AccountIdService::class.java)
            .getGithubMileStones()
            .enqueue(object : retrofit2.Callback<List<GithubMileStoneModel>> {
                override fun onFailure(
                    call: retrofit2.Call<List<GithubMileStoneModel>>,
                    t: Throwable
                ) {
                    githubExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<List<GithubMileStoneModel>>,
                    response: retrofit2.Response<List<GithubMileStoneModel>>
                ) {
                    val coroutineCallGithubMileStone = CoroutineScope(Dispatchers.IO)
                    coroutineCallGithubMileStone.async {
                        Log.d("github_milestone_list", response.code().toString())
                        val githubList = response.body()
                        githubList?.forEach {
                            if (it.title != null && it.number != null) {
                                arrayListMileStones.add(it.title!!)
                                hashMapMileStones[it.title!!] = it.number!!
                            }
                        }
                        updateFields()
                    }
                }
            })
    }

    /**
     * This method is used for getting pull requests details for github.
     * @param repo is used for getting reference of repository name of github.
     * @throws exception if error occurs.
     * @see githubExceptionHandler method.
     */
    private fun gatherTaskPullRequests(repo: String) {
        queueCounter++
        RetrofitGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
            .create(AccountIdService::class.java)
            .getGithubPullRequest()
            .enqueue(object : retrofit2.Callback<List<GithubPullRequestsModel>> {
                override fun onFailure(
                    call: retrofit2.Call<List<GithubPullRequestsModel>>,
                    t: Throwable
                ) {
                    githubExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<List<GithubPullRequestsModel>>,
                    response: retrofit2.Response<List<GithubPullRequestsModel>>
                ) {
                    val coroutineCallGithubPullRequests = CoroutineScope(Dispatchers.IO)
                    coroutineCallGithubPullRequests.async {
                        Log.d("github_pull_requests", response.code().toString())
                        val githubList = response.body()
                        githubList?.forEach {
                            if (it.title != null && it.html_url != null) {
                                arrayListLinkedRequests.add(it.title!!)
                                arrayListLinkedRequestsUrl.add(it.html_url!!)
//                                hashMapLinkedRequests[it.title!!] = it.number!!
                            }
                        }
                        updateFields()
                    }
                }
            })
    }

    /**
     * This method is used for getting details of autoCompleteTextViews in the github layout.
     * @param autoTextViewAssignee is used for getting assignee details from assignee autoCompleteTextView in the github layout.
     * @param autoTextViewLabels is used for getting label details from label autoCompleteTextView in the github layout.
     * @param autoTextViewRepos is used for getting repository details from repository autoCompleteTextView in the github layout.
     * @param autoTextViewProject is used for getting project details from project autoCompleteTextView in the github layout.
     * @param autoTextViewMileStone is used for getting milestone details from milestone autoCompleteTextView in the github layout.
     * @param autoTextViewLinkedRequests is used for getting linked request details from linked request autoCompleteTextView in the github layout.
     */
    internal fun gatherAutoTextDetails(
        autoTextViewAssignee: AutoCompleteTextView,
        autoTextViewLabels: AutoCompleteTextView,
        autoTextViewRepos: AutoCompleteTextView,
        autoTextViewProject: AutoCompleteTextView,
        autoTextViewMileStone: AutoCompleteTextView,
        autoTextViewLinkedRequests: AutoCompleteTextView
    ) {
        assignee = autoTextViewAssignee.editableText.toString()
        labels = autoTextViewLabels.editableText.toString()
        repos = autoTextViewRepos.editableText.toString()
        mileStone = autoTextViewMileStone.editableText.toString()
        linkedRequests = autoTextViewLinkedRequests.editableText.toString()
        project = autoTextViewProject.editableText.toString()
    }

    /**
     * This method is used for getting details of editTexts in the github layout.
     * @param editTextTitle is used for getting title details from title editText in the github layout.
     * @param editTextComment is used for getting comment details from comment editText in the github layout.
     */
    internal fun gatherEditTextDetails(editTextTitle: EditText, editTextComment: EditText) {
        title = editTextTitle.text.toString()
        comment = editTextComment.text.toString()
    }

    /**
     * This method is used for updating and controlling the queue of background tasks in the github actions.
     */
    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeGithubAutoTextViews(
                    arrayListGithubRepos = arrayListRepo,
                    arrayListGithubProject = arrayListProject,
                    arrayListGithubAssignee = arrayListAssignee,
                    arrayListGithubMileStones = arrayListMileStones,
                    arrayListGithubLinkedRequests = arrayListLinkedRequests,
                    arrayListGithubLabels = arrayListLabels
                )
            }
        }

    }

    /**
     * This method is used for controlling the time of background tasks in the github actions.
    If tasks will last longer than three minutes then github layout will be removed.
     */
    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("github_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 180000)
    }

    /**
     * This method is used for default exception handling of github class.
     * @param e is used for getting reference of exception.
     * @param throwable is used for getting reference of throwable.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun githubExceptionHandler(
        e: Exception? = null,
        throwable: Throwable? = null
    ) {
        resetGithubValues()
        if (this::timerTaskQueue.isInitialized) {
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("github_error")
        throwable?.printStackTrace()
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.githubTag,
            throwable = throwable
        )
    }

    /**
     * This method is used for getting reference of current repository position in the repository autoCompleteTextView in the github layout.
     * @param repoPosition is used for getting reference of repository position.
     */
    internal fun setRepoPosition(repoPosition: Int) {
        this.repoPosition = repoPosition
    }

    /**
     * This method is used for getting reference of current milestone position in the milestone autoCompleteTextView in the github layout.
     * @param mileStonePosition is used for getting reference of milestone position.
     */
    internal fun setMileStonePosition(mileStonePosition: Int) {
        this.mileStonePosition = mileStonePosition
    }

    /**
     * This method is used for getting reference of current linked request position in the linked request autoCompleteTextView in the github layout.
     * @param linkedRequestPosition is used for getting reference of linked request position.
     */
    internal fun setLinkedRequestPosition(linkedRequestPosition: Int) {
        this.linkedRequestPosition = linkedRequestPosition
    }

    /**
     * This method is used for getting reference of current project position in the project autoCompleteTextView in the github layout.
     * @param projectPosition is used for getting reference of project position.
     */
    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }


    /**
     * This method is used for resetting the values in github action.
     * @param shareLayoutMessage is used for getting reference of the github action message.
     */
    private fun resetGithubValues() {
        arrayListAttachmentsUrl.clear()
        stringBuilderGithub.clear()
        arrayListLinkedRequests.clear()
        arrayListLinkedRequestsUrl.clear()
        arrayListLabels.clear()
        arrayListProjectUrl.clear()
        arrayListProject.clear()
        arrayListMileStones.clear()
        arrayListAssignee.clear()
        arrayListRepo.clear()
        title = ""
        comment = null
        assignee = null
        labels = null
        repos = null
        mileStone = null
        linkedRequests = null
        runnableListGithubAttachments.clear()
        workQueueLinkedGithubAttachments.controlRunnable = false
    }

    /**
     * This method is used for checking repository reference exist in the repository list or not empty in the repository AutoCompleteTextView field in the repository layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewRepo is used for getting reference of repository autoCompleteTextView in the github layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkGithubRepoEmpty(
        activity: Activity,
        autoTextViewRepo: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewRepo.editableText.toString().isNotEmpty() && arrayListRepo.contains(
                autoTextViewRepo.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewRepo.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.github_repo_empty)
            )
        } else if (!arrayListRepo.contains(autoTextViewRepo.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.github_repo_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking repository reference exist in the assignee list or not empty in the assignee AutoCompleteTextView field in the assignee layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewAssignee is used for getting reference of assignee autoCompleteTextView in the github layout.
     * @return Boolean value.
     */
    internal fun checkGithubAssignee(
        activity: Activity,
        autoTextViewAssignee: AutoCompleteTextView
    ): Boolean {
        if (arrayListAssignee.contains(autoTextViewAssignee.editableText.toString()) || autoTextViewAssignee.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.github_assignee_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking label reference exist in the label list or not empty in the label AutoCompleteTextView field in the label layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewLabels is used for getting reference of label autoCompleteTextView in the github layout.
     * @return Boolean value.
     */
    internal fun checkGithubLabel(
        activity: Activity,
        autoTextViewLabels: AutoCompleteTextView
    ): Boolean {
        if (arrayListLabels.contains(autoTextViewLabels.editableText.toString()) || autoTextViewLabels.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.github_label_doesnt_exist)
            )
        }
        return false
    }

    /**
     * This method is used for checking milestone reference exist in the milestone list or not empty in the milestone AutoCompleteTextView field in the milestone layout.
     * @param activity is used for getting reference of current activity.
     * @param autoTextViewMileStone is used for getting reference of milestone autoCompleteTextView in the github layout.
     * @return Boolean value.
     */
    internal fun checkGithubMileStone(
        activity: Activity,
        autoTextViewMileStone: AutoCompleteTextView
    ): Boolean {
        if (arrayListMileStones.contains(autoTextViewMileStone.editableText.toString()) || autoTextViewMileStone.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.github_mile_stone_doesnt_exist)
            )
        }
        return false
    }

}