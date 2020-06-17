package utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import com.google.gson.JsonObject
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import models.AccountIdService
import models.GithubAssigneeModel
import models.GithubRepoModel
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.IOException

internal class GithubAuthentication {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpGithub = CoroutineScope(Dispatchers.IO)
    private val coroutineCallGithub = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var title:String = ""
    private var comment:String? = null
    private var assignee:String? = null
    private var labels:String? = null
    private var projects:String? = null
    private var mileStone:String? = null
    private var linkedRequests:String? = null
    private var arrayListRepo:ArrayList<String> = ArrayList()
    private var arrayListAssignee:ArrayList<String> = ArrayList()
    internal fun callGithub(
        activity: Activity,
        context: Context,
        task:String,
        filePathMedia: File? = null
    ) {
        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        coroutineCallOkHttpGithub.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    okHttpSlackAuthentication(
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
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.githubTag)
            }
        }
    }

    private fun okHttpSlackAuthentication(
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
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.githubTag)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallGithub.async {
                            try {
                                when(task){
                                    "create" ->  githubCreateIssue(activity = activity)
                                    "get" -> gatherGithubDetails()
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
                    LoggerBird.callExceptionDetails(exception = e, tag = Constants.githubTag)
                }
            }
        })
    }
    private fun githubCreateIssue(
        activity: Activity
    ) {
        try {
            this.activity = activity
            val coroutineCallGithubIssue = CoroutineScope(Dispatchers.IO)
            val jsonObject = JsonObject()
            if(assignee != null){
                jsonObject.addProperty("assignee",assignee)
            }
            if(comment!=null){
                jsonObject.addProperty("body",comment)
            }
            jsonObject.addProperty("title",title)
            RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$projects/")
                .create(AccountIdService::class.java)
                .createGithubIssue(jsonObject = jsonObject)
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.githubTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        coroutineCallGithubIssue.async {
                            Log.d("github_details", response.code().toString())
                            val githubList = response.body()
                            LoggerBirdService.loggerBirdService.finishShareLayout("github")
                        }
                    }
                })

        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.githubTag)
        }
    }
    internal fun gatherAutoTextDetails(
        autoTextViewAssignee: AutoCompleteTextView,
        autoTextViewLabels:AutoCompleteTextView,
        autoTextViewProject:AutoCompleteTextView,
        autoTextViewMileStone:AutoCompleteTextView,
        autoTextViewLinkedRequests:AutoCompleteTextView
    ){
        assignee = autoTextViewAssignee.editableText.toString()
        labels = autoTextViewLabels.editableText.toString()
        projects = autoTextViewProject.editableText.toString()
        mileStone = autoTextViewMileStone.editableText.toString()
        linkedRequests = autoTextViewLinkedRequests.editableText.toString()
    }
    internal fun gatherEditTextDetails(editTextTitle: EditText,editTextComment:EditText){
        title = editTextTitle.text.toString()
        comment = editTextComment.text.toString()
    }
    private fun gatherGithubDetails(){
        arrayListRepo.clear()
        arrayListAssignee.clear()
        gatherTaskRepositories()
    }

    private fun gatherTaskRepositories(){
        RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/user/")
            .create(AccountIdService::class.java)
            .getGithubRepo()
            .enqueue(object : retrofit2.Callback<List<GithubRepoModel>> {
                override fun onFailure(
                    call: retrofit2.Call<List<GithubRepoModel>>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(throwable = t, tag = Constants.githubTag)
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
                            if(it.name != null){
                                arrayListRepo.add(it.name!!)
                                gatherTaskAssignee(repo = it.name!!)
                            }
                        }
                    }
                }
            })
    }
    private fun gatherTaskAssignee(repo:String){
        RetrofitUserGithubClient.getGithubUserClient(url = "https://api.github.com/repos/${LoggerBird.githubUserName}/$repo")
            .create(AccountIdService::class.java)
            .getGithubAssignees()
            .enqueue(object : retrofit2.Callback<List<GithubAssigneeModel>> {
                override fun onFailure(
                    call: retrofit2.Call<List<GithubAssigneeModel>>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(throwable = t, tag = Constants.githubTag)
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
                            if(it.login != null){
                                arrayListAssignee.add(it.login!!)
                            }
                        }
                    }
                }
            })
    }

}