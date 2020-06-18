package utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
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
    private var title: String? = ""
    private var description: String? = ""
    private var weight: String? = ""
    private var owned: String? = "true"
    private var assignee: String? = null
    private var labels: String? = null
    private var project: String? = null
    private var spinnerPositionProject: Int = 0
    private val arrayListProjects: ArrayList<String> = ArrayList()
    private val arrayListProjectsId: ArrayList<String> = ArrayList()
    private val arrayListMilestones: ArrayList<String> = ArrayList()
    private val arrayListMilestonesId: ArrayList<String> = ArrayList()
    private val arrayListLabels: ArrayList<String> = ArrayList()
    private val arrayListLabelsId: ArrayList<String> = ArrayList()
    private val arrayListUsers: ArrayList<String> = ArrayList()
    private val arrayListUsersId: ArrayList<String> = ArrayList()
    private var hashMapProjects: HashMap<String, String> = HashMap()
    private var hashMapMilestones: HashMap<String, String> = HashMap()
    private var hashMapLabels: HashMap<String, String> = HashMap()
    private var hashMapUsers: HashMap<String, String> = HashMap()

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
            jsonObject.addProperty("weight", weight)
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/" + hashMapProjects[arrayListProjects[spinnerPositionProject]] + "/")
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
                            activity.runOnUiThread {
                                LoggerBirdService.loggerBirdService.buttonGitlabCancel.performClick()
                            }
                            LoggerBirdService.loggerBirdService.finishShareLayout("gitlab")
                            Log.d("gitlab", response.code().toString())
                            val gitlab = response.body()
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
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
        }
    }

    private fun gatherGitlabMilestonesDetails() {
        try {
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/19430667/")
                .create(AccountIdService::class.java)
                .getGitlabMilestones()
                .enqueue(object : retrofit2.Callback<List<GitlabMilestonesModel>> {

                    override fun onFailure(
                        call: retrofit2.Call<List<GitlabMilestonesModel>>, t: Throwable) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabMilestonesModel>>, response: retrofit2.Response<List<GitlabMilestonesModel>>) {

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
                                    hashMapProjects[it.title!!] = it.id!!
                                }
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

    private fun gatherGitlabLabelsDetails() {
        try {
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/19430667/")
                .create(AccountIdService::class.java)
                .getGitlabLabels()
                .enqueue(object : retrofit2.Callback<List<GitlabLabelsModel>> {

                    override fun onFailure(
                        call: retrofit2.Call<List<GitlabLabelsModel>>, t: Throwable) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabLabelsModel>>, response: retrofit2.Response<List<GitlabLabelsModel>>) {

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
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabTag)
        }
    }

    private fun gatherGitlabUsersDetails() {
        try {
            RetrofitUserGitlabClient.getGitlabUserClient(url = "https://gitlab.com/api/v4/projects/19430667/")
                .create(AccountIdService::class.java)
                .getGitlabUsers()
                .enqueue(object : retrofit2.Callback<List<GitlabUsersModel>> {

                    override fun onFailure(
                        call: retrofit2.Call<List<GitlabUsersModel>>, t: Throwable) {
                        t.printStackTrace()
                        LoggerBird.callEnqueue()
                        LoggerBird.callExceptionDetails(throwable = t, tag = Constants.gitlabTag)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<GitlabUsersModel>>, response: retrofit2.Response<List<GitlabUsersModel>>) {

                        val coroutineCallGitlabDetails = CoroutineScope(Dispatchers.IO)
                        coroutineCallGitlabDetails.async {
                            Log.d("gitlabusers", response.code().toString())
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
                hashMapProjects.clear()
                arrayListProjects.clear()
                arrayListProjectsId.clear()

                withContext(Dispatchers.IO) {
                    gatherGitlabProjectDetails()
                    gatherGitlabMilestonesDetails()
                    gatherGitlabLabelsDetails()
                    gatherGitlabUsersDetails()
                }
            } catch (e: Exception) {
                LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error")
                gitlabExceptionHandler(e = e, filePathName = filePathMedia)

            }
        }
    }

    private fun updateFields() {
        activity.runOnUiThread {
            LoggerBirdService.loggerBirdService.initializeGitlabSpinner(
                arrayListProjects = arrayListProjects
            )
        }
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
        spinnerProject: Spinner
    ) {
        spinnerPositionProject = spinnerProject.selectedItemPosition
        project = spinnerProject.selectedItem.toString()
    }

    private fun gitlabExceptionHandler(
        e: Exception? = null,
        filePathName: File? = null,
        throwable: Throwable? = null
    ) {
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