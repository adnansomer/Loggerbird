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
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.IOException

class GitlabAuthentication {

    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private val coroutineCallOkHttpGitlab = CoroutineScope(Dispatchers.IO)
    private val coroutineCallGitlab = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var title:String = "adnan"
    private var description:String? = "somer"
    private var assignee:String? = null
    private var labels:String? = null
    private var weight:String? = null
    private var arrayListAssignee:ArrayList<String> = ArrayList()

    internal fun callGitlab(
        activity: Activity,
        context: Context,
        task:String,
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
                                when(task){
                                    "create" ->  gitlabCreateIssue(activity = activity)
                                    //"get" ->     gatherGitlabDetails()
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
        activity: Activity
    ) {
        try {
            this.activity = activity
            val coroutineCallGitlabIssue = CoroutineScope(Dispatchers.IO)
            val jsonObject = JsonObject()
//            if(title!=null){
//                jsonObject.addProperty()
//                jsonObject.addProperty("body",title)
//            }
            jsonObject.addProperty("title",title)
            jsonObject.addProperty("description",description)
            jsonObject.addProperty("weight")
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/19430667/")
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
                        coroutineCallGitlabIssue.async {

                            Log.d("gitlab", response.code().toString())
                            val gitlab = response.body()
                        }
                    }
                })

        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
        }
    }

}