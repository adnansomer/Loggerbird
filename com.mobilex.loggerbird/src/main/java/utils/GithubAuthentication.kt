package utils

import adapter.RecyclerViewGithubAdapter
import adapter.RecyclerViewGithubAssigneeAdapter
import adapter.RecyclerViewGithubLabelAdapter
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.gson.Gson
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
import utils.*
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.IOException
import java.lang.StringBuilder
import java.nio.file.Files
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

internal class GithubAuthentication {
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
    private var project:String? = null
    private var arrayListRepo: ArrayList<String> = ArrayList()
    private var arrayListProject:ArrayList<String> = ArrayList()
    private var arrayListProjectUrl:ArrayList<String> = ArrayList()
    private var arrayListAssignee: ArrayList<String> = ArrayList()
    private var arrayListMileStones: ArrayList<String> = ArrayList()
    private var hashMapMileStones: HashMap<String, Int> = HashMap()
    private var arrayListLinkedRequests: ArrayList<String> = ArrayList()
    private var arrayListLinkedRequestsUrl:ArrayList<String> = ArrayList()
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
    private var workQueueLinkedGithubAttachments: LinkedBlockingQueueUtil = LinkedBlockingQueueUtil()
    private var runnableListGithubAttachments: ArrayList<Runnable> = ArrayList()
    private val arrayListAttachmentsUrl: ArrayList<String> = ArrayList()
     private val stringBuilderGithub = StringBuilder()
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

    private fun githubCreateIssue(
        activity: Activity
    ) {
        try {
            stringBuilderGithub.clear()
            arrayListAttachmentsUrl.clear()
            this.activity = activity
            val jsonObject = JsonObject()
            val gson = Gson()
//            if(linkedRequests != null){
//                jsonObject.addProperty("",hashMapLinkedRequests[linkedRequests!!])
//            }
            if(RecyclerViewGithubLabelAdapter.ViewHolder.arrayListLabelNames.isNotEmpty()){
                val jsonArrayLabels = JsonArray()
                RecyclerViewGithubLabelAdapter.ViewHolder.arrayListLabelNames.forEach {
                    jsonArrayLabels.add(it.labelName)
                }
                jsonObject.add("labels", jsonArrayLabels)
            }else{
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
            if(!linkedRequests.isNullOrEmpty() && arrayListLinkedRequestsUrl.size > linkedRequestPosition){
                stringBuilderGithub.append("Linked Pull Request:" + arrayListLinkedRequestsUrl[linkedRequestPosition] + "\n")
            }
            if(!project.isNullOrEmpty() && arrayListProjectUrl.size > projectPosition){
                stringBuilderGithub.append("Project:" + arrayListProjectUrl[projectPosition] + "\n")
            }
            if(stringBuilderGithub.isNotEmpty()){
                jsonObject.addProperty("body", stringBuilderGithub.toString())
            }
            jsonObject.addProperty("title", title)
            RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repos/")
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
                            RecyclerViewGithubAdapter.ViewHolder.arrayListFilePaths.forEach {
                                val file = it.file
                                if (file.exists()) {
                                    callGithubAttachments(
                                        repo = repos!!,
                                        filePathMedia = file
                                    )
                                }
                            }
                            if (RecyclerViewGithubAdapter.ViewHolder.arrayListFilePaths.isEmpty()) {
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

    internal fun gatherAutoTextDetails(
        autoTextViewAssignee: AutoCompleteTextView,
        autoTextViewLabels: AutoCompleteTextView,
        autoTextViewRepos: AutoCompleteTextView,
        autoTextViewProject:AutoCompleteTextView,
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

    internal fun gatherEditTextDetails(editTextTitle: EditText, editTextComment: EditText) {
        title = editTextTitle.text.toString()
        comment = editTextComment.text.toString()
    }

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

    private fun gatherTaskRepositories() {
        queueCounter++
        RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/user/")
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

    private fun gatherTaskAssignee(repo: String) {
        queueCounter++
        RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
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

    private fun gatherTaskProject(repo: String) {
        queueCounter++
        RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
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

    private fun gatherTaskLabels(repo: String) {
        queueCounter++
        RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
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

    private fun gatherTaskMileStone(repo: String) {
        queueCounter++
        RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
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

    private fun gatherTaskPullRequests(repo: String) {
        queueCounter++
        RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
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
                            if (it.title != null && it.html_url!= null) {
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

    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeGithubAutoTextViews(
                    arrayListRepos = arrayListRepo,
                    arrayListProject = arrayListProject,
                    arrayListAssignee = arrayListAssignee,
                    arrayListMileStones = arrayListMileStones,
                    arrayListLinkedRequests = arrayListLinkedRequests,
                    arrayListLabels = arrayListLabels
                )
            }
        }

    }


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

    internal fun setRepoPosition(repoPosition: Int) {
        this.repoPosition = repoPosition
    }

    internal fun setMileStonePosition(mileStonePosition: Int) {
        this.mileStonePosition = mileStonePosition
    }

    internal fun setLinkedRequestPosition(linkedRequestPosition: Int) {
        this.linkedRequestPosition = linkedRequestPosition
    }
    internal fun setProjectPosition(projectPosition:Int){
        this.projectPosition = projectPosition
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkGithubRepoEmpty(
        activity: Activity,
        autoTextViewGithubRepo: AutoCompleteTextView
    ): Boolean {
        if (autoTextViewGithubRepo.editableText.toString().isNotEmpty() && arrayListRepo.contains(
                autoTextViewGithubRepo.editableText.toString()
            )
        ) {
            return true
        } else if (autoTextViewGithubRepo.editableText.toString().isEmpty()) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.github_repo_empty)
            )
        } else if (!arrayListRepo.contains(autoTextViewGithubRepo.editableText.toString())) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.github_repo_doesnt_exist)
            )
        }
        return false
    }

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

    internal fun checkGithubLabel(
        activity: Activity,
        autoTextViewGithubLabels: AutoCompleteTextView
    ): Boolean {
        if (arrayListLabels.contains(autoTextViewGithubLabels.editableText.toString()) || autoTextViewGithubLabels.editableText.toString().isEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.github_label_doesnt_exist)
            )
        }
        return false
    }

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAttachments(repo: String, file: File) {
        try {
//            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//            val body = MultipartBody.Part.createFormData("file",file.name,requestFile)
            val coroutineCallAttachments = CoroutineScope(Dispatchers.IO)
            coroutineCallAttachments.async {

                val jsonObject = JsonObject()
                jsonObject.addProperty(
                    "message",
                    "loggerbirdfile"
                )
                withContext(Dispatchers.IO){
                    jsonObject.addProperty(
                        "content",
                        Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()))
                    )
                }
                RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
                    .create(AccountIdService::class.java)
                    .setGithubAttachments(
//                    file = body,
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
//                            val jsonObjectIssue = JsonObject()
//                            jsonObjectIssue.addProperty(
//                                "body",
//                                "[" + response.body()!!["content"].asJsonObject["name"].asString + "]" + "(" +
//                                        response.body()!!["content"].asJsonObject["download_url"].asString + ")"
//                            )
//                            jsonObjectIssue.addProperty(
//                                "body",
//                                        response.body()!!["content"].asJsonObject["download_url"].asString
//                            )
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

    private fun callEnqueueGithubAttachments(repo: String) {
        workQueueLinkedGithubAttachments.controlRunnable = false
        if (runnableListGithubAttachments.size > 0) {
            runnableListGithubAttachments.removeAt(0)
            if (runnableListGithubAttachments.size > 0) {
                workQueueLinkedGithubAttachments.put(runnableListGithubAttachments[0])
            } else {
                if (arrayListAttachmentsUrl.isNotEmpty()) {
//                    val gson = Gson()
//                    val jsonArrayAttachments = gson.toJsonTree(arrayListAttachmentsUrl).asJsonArray
                    val stringBuilder = StringBuilder()
                    if(stringBuilderGithub.isNotEmpty()){
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

    private fun addAttachmentsToIssue(jsonObjectIssue: JsonObject, repo: String) {
        val coroutineCallAttachments = CoroutineScope(Dispatchers.IO)
        coroutineCallAttachments.async {
            RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo/")
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

}