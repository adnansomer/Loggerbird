package utils

import adapter.RecyclerViewJiraAdapter
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.FileUtils
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.JiraRestClientFactory
import com.atlassian.jira.rest.client.api.ProjectRestClient
import com.atlassian.jira.rest.client.api.domain.*
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import io.atlassian.util.concurrent.Promise
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import models.*
import okhttp3.*
import services.LoggerBirdService
import java.io.*
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.system.exitProcess


class JiraAuthentication {
    private val coroutineCallJira: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private var project: String? = null
    private var issueType: String? = null
    private var issueTypePosition: Int = 0
    private var priorityPosition: Int = 0
    private var projectPosition: Int = 0
    private var componentPosition: Int = 0
    private var fixVersionPosition: Int = 0
    private var linkedIssueTypePosition: Int = 0
    private var labelPosition: Int = 0
    private var assigneePosition: Int = 0
    private var reporterPosition: Int = 0
    private var sprintPosition: Int = 0
    private var reporter: String? = null
    private var linkedIssue: String? = null
    private var issue: String? = null
    private var assignee: String? = null
    private var priority: String? = null
    private var summary: String = ""
    private var description: String? = null
    private var label: String? = null
    private var epicLink: String? = null
    private var sprint: String? = null
    private var component: String? = null
    private var fixVersion: String? = null
    private val hashMapComponent: HashMap<String, Iterable<BasicComponent>> = HashMap()
    private val hashMapFixVersions: HashMap<String, Iterable<Version>> = HashMap()
    private val hashMapLinkedIssues: HashMap<String, String> = HashMap()
    private val hashMapSprint: HashMap<String, String> = HashMap()
    private val hashMapBoard: HashMap<String, String> = HashMap()
    private lateinit var arrayListRecyclerViewItems: ArrayList<RecyclerViewModel>
    private val arrayListProjects: ArrayList<String> = ArrayList()
    private val arrayListProjectKeys: ArrayList<String> = ArrayList()
    private val arrayListIssueTypes: ArrayList<String> = ArrayList()
    private val arrayListIssueTypesId: ArrayList<Int> = ArrayList()
    private val arrayListAssignee: ArrayList<String> = ArrayList()
    private val arrayListIssueLinkedTypes: ArrayList<String> = ArrayList()
    private val arrayListIssues: ArrayList<String> = ArrayList()
    private val arrayListReporter: ArrayList<String> = ArrayList()
    private val arrayListPriorities: ArrayList<String> = ArrayList()
    private val arrayListPrioritiesId: ArrayList<Int> = ArrayList()
    private val arrayListComponents: ArrayList<String> = ArrayList()
    private val arrayListFixVersions: ArrayList<String> = ArrayList()
    private val arrayListLabel: ArrayList<String> = ArrayList()
    private val arrayListChoosenLabel: ArrayList<String> = ArrayList()
    private val arrayListEpicLink: ArrayList<String> = ArrayList()
    //    private val arrayListFields: ArrayList<String> = ArrayList()
    private val arrayListSprintName: ArrayList<String> = ArrayList()
    private val arrayListAccountId: ArrayList<String> = ArrayList()
    private val arrayListSelf: ArrayList<String> = ArrayList()
    private val arrayListEmailAdresses: ArrayList<String> = ArrayList()
    private val arrayListAvatarUrls: ArrayList<String> = ArrayList()
    private val arrayListBoardId: ArrayList<String> = ArrayList()
    private val jiraDomainName = LoggerBird.jiraDomainName
    private val jiraUserName = LoggerBird.jiraUserName
    private val jiraApiToken = LoggerBird.jiraApiToken
    private val defaultToast: DefaultToast = DefaultToast()
    private var sprintField: String? = null
    private var queueCounter = 0
    private lateinit var activity: Activity
    private var startDateField: String? = null
    private var startDate: String? = null
    private var queueCreateTask = 0
    private lateinit var timerTaskQueue: TimerTask

    companion object {
        internal lateinit var createdIssueKey: String
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun callJiraIssue(
        filePathName: File? = null,
        context: Context,
        activity: Activity,
        jiraTask: String,
        createMethod: String
    ) {
        coroutineCallJira.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    okHttpJiraAuthentication(
                        filePathMediaName = filePathName,
                        context = context,
                        activity = activity,
                        jiraTask = jiraTask,
                        createMethod = createMethod
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
                jiraExceptionHandler(e = e, filePathName = filePathName)
            }
        }
    }

    private fun okHttpJiraAuthentication(
        filePathMediaName: File?,
        context: Context,
        activity: Activity,
        jiraTask: String,
        createMethod: String
    ) {
        val client = OkHttpClient()
        val request: Request =
            Request.Builder()
                .url(jiraDomainName)
                .build()
        client.newCall(request).enqueue(object : Callback {
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onFailure(call: Call, e: IOException) {
                jiraExceptionHandler(e = e, filePathName = filePathMediaName)
            }

            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        checkQueueTime(activity = activity)
                        when (jiraTask) {
                            "create" -> jiraTaskCreateIssue(
                                filePathMediaName = filePathMediaName,
                                restClient = jiraAuthentication(),
                                activity = activity,
                                context = context,
                                createMethod = createMethod
                            )
                            "get" -> jiraTaskGatherDetails(
                                restClient = jiraAuthentication(),
                                activity = activity
                            )
                            "unhandled_duplication" ->
                                if (duplicateErrorMessageCheck(
                                        restClient = jiraAuthentication(),
                                        activity = activity
                                    )
                                ) {
                                    activity.runOnUiThread {
                                        LoggerBirdService.loggerBirdService.detachProgressBar()
                                        when (createMethod) {
                                            "default" -> LoggerBirdService.loggerBirdService.attachUnhandledDuplicationLayout(
                                                unhandledExceptionIssueMethod = "default",
                                                filePath = filePathMediaName!!
                                            )
                                            "customize" -> LoggerBirdService.loggerBirdService.attachUnhandledDuplicationLayout(
                                                unhandledExceptionIssueMethod = "customize",
                                                filePath = filePathMediaName!!
                                            )
                                        }
                                    }
                                } else {
                                    activity.runOnUiThread {
                                        LoggerBirdService.loggerBirdService.detachProgressBar()
                                        when (createMethod) {
                                            "default" -> LoggerBirdService.loggerBirdService.createDefaultUnhandledJiraIssue(
                                                filePath = filePathMediaName!!
                                            )
                                            "customize" -> LoggerBirdService.loggerBirdService.createCustomizedUnhandledJiraIssue(
                                                filePath = filePathMediaName!!
                                            )
                                        }
                                    }
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
                    jiraExceptionHandler(e = e, filePathName = filePathMediaName)
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun jiraExceptionHandler(
        e: Exception? = null,
        filePathName: File? = null,
        throwable: Throwable? = null
    ) {
//        if(filePathName?.name != "logger_bird_details_old_session.txt"){
//            filePathName?.delete()
//        }
        if(this::timerTaskQueue.isInitialized){
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("jira_error")
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.jiraAuthenticationtag,
            throwable = throwable
        )
    }

    private fun jiraAuthentication(): JiraRestClient {
        val factory: JiraRestClientFactory = AsynchronousJiraRestClientFactory()
        val jiraServerUri =
            URI(jiraDomainName)
        return factory.createWithBasicHttpAuthentication(
            jiraServerUri,
            jiraUserName,
            jiraApiToken
        )
    }

// project: String +
// issueType: String +
// reporter: String +-
// dueTime:Date -
// sprint:String -
// linkedIssue: String +
// label:String +
// epiclink:String +
// issue:String +
// assignee: String +-
// priority: String +
// summary: String +
// description: String +
// fixVersions:String +
// component:String +
// val issueType = IssueType(null,10004,"bug",false,"Assignment LoggerBird",null)
// val basicProject = BasicProject(null,"LGB",10004,"LoggerBird")

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraTaskCreateIssue(
        filePathMediaName: File? = null,
        restClient: JiraRestClient,
        activity: Activity,
        context: Context,
        createMethod: String
    ) {
        try {
            when (createMethod) {
                "normal" -> jiraNormalTask(
                    restClient = restClient,
                    context = context,
                    activity = activity
                )
                "unhandled" -> if (filePathMediaName != null) {
                    if (filePathMediaName.exists()) {
                        jiraUnhandledTask(
                            restClient = restClient,
                            context = context,
                            filePathName = filePathMediaName
                        )
                    } else {
                        activity.runOnUiThread {
                            LoggerBirdService.loggerBirdService.detachProgressBar()
                            defaultToast.attachToast(
                                activity = activity,
                                toastMessage = context.resources.getString(R.string.unhandled_file_doesnt_exist)
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            jiraExceptionHandler(e = e, filePathName = filePathMediaName)
        }
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraNormalTask(restClient: JiraRestClient, activity: Activity, context: Context) {
        try {
            if (checkSummaryEmpty(
                    activity = activity,
                    context = context
                ) && checkReporterEmpty(
                    activity = activity,
                    context = context
                ) && checkFixVersionsEmpty(
                    activity = activity,
                    context = context
                ) && checkEpicLinkEmpty(activity = activity, context = context)
            ) {
                val issueClient = restClient.issueClient
                val issueBuilder = IssueInputBuilder(
                    arrayListProjectKeys[projectPosition],
                    arrayListIssueTypesId[issueTypePosition].toLong(),
                    summary
                )
                issueBuilder.setPriorityId(arrayListPrioritiesId[priorityPosition].toLong())
                if (this.description != null) {
                    if (this.description!!.isNotEmpty()) {
                        issueBuilder.setDescription(description)
                    }
                }
                if (this.arrayListChoosenLabel.isNotEmpty()) {
                    issueBuilder.setFieldValue("labels", arrayListChoosenLabel)
                }
                //            issueBuilder.setFieldValue("reporter",reporter)
                //            val basicUser = BasicUser(URI(arrayListSelf[assigneePosition]),arrayListName[assigneePosition],assignee,arrayListAccountId[assigneePosition])
                //            issueBuilder.setAssigneeName(basicUser.displayName)
                ////            issueBuilder.setReporter(basicUser)
                //            issueBuilder.setFieldValue("assignee",basicUser)
                //            issueBuilder.setReporter(basicUser.)
                //        issueBuilder.setAssigneeName("0")
                //            val basicUser = BasicUser(
                //                URI("https://appcaesars.atlassian.net/rest/api/2/user?accountId=5eb3efa5ad226b0ba423144a"),
                //                "caesars App",
                //                "caesars App"
                //
                //            )
                //            issueBuilder.setAssignee(basicUser)
                //        issueBuilder.setReporter()
                //                    issueBuilder.addProperty()
                //                    val issueInput = IssueInputBuilder(basicProject,issueType,"LoggerBird_Assignment").build()
                //                    val issueCreated = issueClient.createIssue(issueBuilder.build()).claim().key
                //            issueBuilder.setDueDate(DateTime.parse("2020-06-25"))
                if (arrayListComponents.size > componentPosition) {
                    if (arrayListComponents[componentPosition].isNotEmpty() && this.component != null) {
                        if (this.component!!.isNotEmpty()) {
                            issueBuilder.setComponents(hashMapComponent[arrayListComponents[componentPosition]])
                        }

                    }
                }

                if (arrayListFixVersions.size > fixVersionPosition) {
                    if (arrayListFixVersions[fixVersionPosition].isNotEmpty() && this.fixVersion != null) {
                        if (this.fixVersion!!.isNotEmpty()) {
                            issueBuilder.setFixVersions(hashMapFixVersions[arrayListFixVersions[fixVersionPosition]])
                        }
                    }
                }

                val basicIssue = issueClient.createIssue(issueBuilder.build()).claim()
                val issueKey = basicIssue.key
                //            createdIssueKey  = issueKey
                val issueUri = basicIssue.self
                val issue: Promise<Issue> = restClient.issueClient.getIssue(issueKey)
                if (this.assignee != null) {
                    if (this.assignee!!.isNotEmpty()) {
                        queueCreateTask++
                        val jsonObjectAssignee = JsonObject()
                        jsonObjectAssignee.addProperty(
                            "accountId",
                            arrayListAccountId[assigneePosition]
                        )
                        RetrofitUserJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/issue/$issueKey/")
                            .create(AccountIdService::class.java)
                            .setAssignee(jsonObject = jsonObjectAssignee)
                            .enqueue(object : retrofit2.Callback<List<JiraUserModel>> {
                                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                                override fun onFailure(
                                    call: retrofit2.Call<List<JiraUserModel>>,
                                    t: Throwable
                                ) {
                                    resetJiraValues()
                                    jiraExceptionHandler(throwable = t)
                                }

                                override fun onResponse(
                                    call: retrofit2.Call<List<JiraUserModel>>,
                                    response: retrofit2.Response<List<JiraUserModel>>
                                ) {
                                    resetJiraValues()
                                    Log.d("assignee_put_success", response.code().toString())
                                }
                            })
                    }
                }

                if (this.reporter != null) {
                    if (this.reporter!!.isNotEmpty()) {
                        queueCreateTask++
                        val jsonObjectReporter = JsonObject()
                        val jsonObjectField = JsonObject()
                        val jsonObjectReporterField = JsonObject()
                        jsonObjectReporter.addProperty("self", arrayListSelf[reporterPosition])
                        jsonObjectReporter.addProperty(
                            "accountId",
                            arrayListAccountId[reporterPosition]
                        )
                        jsonObjectReporter.addProperty(
                            "emailAddress",
                            arrayListEmailAdresses[reporterPosition]
                        )
                        jsonObjectReporterField.add("reporter", jsonObjectReporter)
                        jsonObjectField.add("fields", jsonObjectReporterField)
                        RetrofitUserJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/issue/$issueKey/")
                            .create(AccountIdService::class.java)
                            .setReporter(jsonObject = jsonObjectField)
                            .enqueue(object : retrofit2.Callback<List<JiraUserModel>> {
                                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                                override fun onFailure(
                                    call: retrofit2.Call<List<JiraUserModel>>,
                                    t: Throwable
                                ) {
                                    resetJiraValues()
                                    jiraExceptionHandler(throwable = t)
                                }

                                override fun onResponse(
                                    call: retrofit2.Call<List<JiraUserModel>>,
                                    response: retrofit2.Response<List<JiraUserModel>>
                                ) {
                                    resetJiraValues()
                                    Log.d("reporter_put_success", response.code().toString())
                                }
                            })
                    }

                }

                if (this.sprint != null && this.sprintField != null) {
                    if (this.sprint!!.isNotEmpty()) {
                        queueCreateTask++
                        val jsonObjectSprint = JsonObject()
                        val jsonObjectFieldSprint = JsonObject()
                        jsonObjectSprint.addProperty(
                            sprintField,
                            hashMapSprint[arrayListSprintName[sprintPosition]]?.toInt()
                        )
                        jsonObjectFieldSprint.add("fields", jsonObjectSprint)
                        RetrofitUserJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/issue/$issueKey/")
                            .create(AccountIdService::class.java)
                            .setSprint(jsonObject = jsonObjectFieldSprint)
                            .enqueue(object : retrofit2.Callback<List<JiraSprintModel>> {
                                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                                override fun onFailure(
                                    call: retrofit2.Call<List<JiraSprintModel>>,
                                    t: Throwable
                                ) {
                                    resetJiraValues()
                                    jiraExceptionHandler(throwable = t)
                                }

                                override fun onResponse(
                                    call: retrofit2.Call<List<JiraSprintModel>>,
                                    response: retrofit2.Response<List<JiraSprintModel>>
                                ) {
                                    resetJiraValues()
                                    Log.d("sprint_put_success", response.code().toString())
                                }
                            })
                    }
                }

                if (this.startDate != null && this.startDateField != null) {
                    queueCreateTask++
                    val jsonObjectStartDate = JsonObject()
                    val jsonObjectFieldStartDate = JsonObject()
                    jsonObjectStartDate.addProperty(
                        startDateField,
                        startDate
                    )
                    jsonObjectFieldStartDate.add("fields", jsonObjectStartDate)
                    RetrofitUserJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/issue/$issueKey/")
                        .create(AccountIdService::class.java)
                        .setStartDate(jsonObject = jsonObjectFieldStartDate)
                        .enqueue(object : retrofit2.Callback<List<JiraSprintModel>> {
                            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                            override fun onFailure(
                                call: retrofit2.Call<List<JiraSprintModel>>,
                                t: Throwable
                            ) {
                                resetJiraValues()
                                jiraExceptionHandler(throwable = t)
                            }

                            override fun onResponse(
                                call: retrofit2.Call<List<JiraSprintModel>>,
                                response: retrofit2.Response<List<JiraSprintModel>>
                            ) {
                                resetJiraValues()
                                Log.d("start_put_success", response.code().toString())
                            }
                        })
                }

                if (this.issue != null) {
                    if (this.issue!!.isNotEmpty()) {
                        val linkIssueInput = LinkIssuesInput(
                            issueKey,
                            this.issue,
                            hashMapLinkedIssues[arrayListIssueLinkedTypes[linkedIssueTypePosition]]
                        )
                        issueClient.linkIssue(linkIssueInput)
                    }
                }
                if (this.epicLink != null) {
                    if (this.epicLink!!.isNotEmpty()) {
                        val linkIssueInput = LinkIssuesInput(
                            issueKey,
                            this.epicLink,
                            hashMapLinkedIssues[arrayListIssueLinkedTypes[linkedIssueTypePosition]]
                        )
                        issueClient.linkIssue(linkIssueInput)
                    }
                }
                var fileCounter = 0
                do {
                    if (RecyclerViewJiraAdapter.ViewHolder.arrayListFilePaths.size > fileCounter) {
                        val file =
                            RecyclerViewJiraAdapter.ViewHolder.arrayListFilePaths[fileCounter].file
                        if (file.exists()) {
                            val inputStreamMediaFile = FileInputStream(file)
                            issueClient.addAttachment(
                                issue.get().attachmentsUri,
                                inputStreamMediaFile,
                                file.absolutePath
                            )
                        }
                        if (file.name != "logger_bird_details.txt") {
                            if (file.exists()) {
                                file.delete()
                            }
                        }
                    } else {
                        break
                    }
                    fileCounter++

                } while (RecyclerViewJiraAdapter.ViewHolder.arrayListFilePaths.iterator().hasNext())
                //            val inputStreamSecessionFile =
                //                FileInputStream(LoggerBird.filePathSecessionName)
                //            issueClient.addAttachment(
                //                issue.get().attachmentsUri,
                //                inputStreamSecessionFile,
                //                LoggerBird.filePathSecessionName.absolutePath
                //            )
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.buttonJiraCancel.performClick()
                }
                if (!LoggerBirdService.loggerBirdService.checkUnhandledFilePath()) {
                    LoggerBirdService.loggerBirdService.finishShareLayout("jira")
                } else {
                    LoggerBirdService.loggerBirdService.unhandledExceptionCustomizeIssueSent()
                }
                timerTaskQueue.cancel()
                Log.d("issue", issueUri.toString())
                Log.d("issue", issueKey.toString())
            }
        } catch (e: Exception) {
            jiraExceptionHandler(e = e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraUnhandledTask(
        restClient: JiraRestClient,
        context: Context,
        filePathName: File
    ) {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(LoggerBirdService.loggerBirdService.returnActivity().applicationContext)
        val issueClient = restClient.issueClient
        val issueBuilder = IssueInputBuilder(
            "DEN",
            10004,
            context.resources.getString(R.string.jira_summary_unhandled_exception)
        )
        issueBuilder.setDescription(sharedPref.getString("unhandled_exception_message", null))
        val inputStreamSecessionFile =
            FileInputStream(filePathName)
        val basicIssue = issueClient.createIssue(issueBuilder.build()).claim()
        val issueKey = basicIssue.key
        val issue: Promise<Issue> = restClient.issueClient.getIssue(issueKey)
        issueClient.addAttachment(
            issue.get().attachmentsUri,
            inputStreamSecessionFile,
            filePathName.absolutePath
        )
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove("unhandled_file_path")
        editor.apply()
        timerTaskQueue.cancel()
        LoggerBirdService.loggerBirdService.returnActivity().runOnUiThread {
            LoggerBirdService.loggerBirdService.detachProgressBar()
            defaultToast.attachToast(
                activity = LoggerBirdService.loggerBirdService.returnActivity(),
                toastMessage = context.resources.getString(R.string.jira_sent)
            )
        }
//        defaultToast.attachToast(activity = LoggerBirdService.loggerBirdService.returnActivity() , toastMessage = "Unhandled Exception occurred , automatically opening jira issue!")
//        LoggerBirdService.loggerBirdService.finishShareLayout("jira")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraTaskGatherDetails(
        restClient: JiraRestClient,
        activity: Activity
    ) {
        try {
            queueCounter = 0
            this.activity = activity
            val coroutineCallGatherDetails = CoroutineScope(Dispatchers.IO)
            coroutineCallGatherDetails.launch {
                arrayListProjects.clear()
                arrayListProjectKeys.clear()
                arrayListIssueTypes.clear()
                arrayListIssueTypesId.clear()
                arrayListAssignee.clear()
                arrayListAccountId.clear()
                arrayListSelf.clear()
                arrayListEmailAdresses.clear()
                arrayListAvatarUrls.clear()
                arrayListIssueTypesId.clear()
                arrayListIssueLinkedTypes.clear()
                arrayListIssues.clear()
                arrayListPriorities.clear()
                arrayListPrioritiesId.clear()
                arrayListReporter.clear()
                arrayListComponents.clear()
                arrayListFixVersions.clear()
                arrayListLabel.clear()
                arrayListChoosenLabel.clear()
                arrayListEpicLink.clear()
                arrayListSprintName.clear()
                arrayListBoardId.clear()
                hashMapComponent.clear()
                hashMapFixVersions.clear()
                hashMapLinkedIssues.clear()
                hashMapSprint.clear()
                hashMapBoard.clear()
                jiraTaskGatherSprintFields(restClient = restClient)
                jiraTaskGatherProjectKeys(restClient = restClient)
                jiraTaskGatherIssueTypes(restClient = restClient)
                jiraTaskGatherAssignees(restClient = restClient)
                jiraTaskGatherLinkedIssues(restClient = restClient)
                jiraTaskGatherIssues(restClient = restClient)
                jiraTaskGatherPriorities(restClient = restClient)
                jiraTaskGatherSprint(restClient = restClient)
                jiraTaskGatherBoards()
                Log.d("que_counter", queueCounter.toString())
            }

        } catch (e: Exception) {
            jiraExceptionHandler(e = e)
//            updateFields()
//            e.printStackTrace()
//            LoggerBird.callEnqueue()
//            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
    }

    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeJiraSpinner(
                    arrayListProjectNames = arrayListProjects,
                    arrayListProjectKeys = arrayListProjectKeys,
                    arrayListIssueTypes = arrayListIssueTypes,
                    arrayListAssignee = arrayListAssignee,
                    arrayListReporterNames = arrayListReporter,
                    arrayListLinkedIssues = arrayListIssueLinkedTypes,
                    arrayListIssues = arrayListIssues,
                    arrayListPriority = arrayListPriorities,
                    arrayListComponent = arrayListComponents,
                    arrayListFixVersions = arrayListFixVersions,
                    arrayListLabel = arrayListLabel,
                    arrayListEpicLink = arrayListEpicLink,
                    arrayListSprint = arrayListSprintName,
                    hashMapBoardList = hashMapBoard
                )
            }
        }

    }

    private fun jiraTaskGatherProjectKeys(restClient: JiraRestClient) {
        queueCounter++
        val coroutineCallProjectKeys = CoroutineScope(Dispatchers.IO)
        coroutineCallProjectKeys.async {
            val projectClient = restClient.projectClient
            val projectList = projectClient.allProjects
            projectList.claim().forEach {
                if (it.name != null) {
                    arrayListProjects.add(it.name!!)
                }
                arrayListProjectKeys.add(it.key)
            }
            jiraTaskGatherFixComp(
                projectClient = projectClient,
                projectKey = arrayListProjectKeys[projectPosition]
            )
            updateFields()
        }
    }

    private fun jiraTaskGatherIssueTypes(restClient: JiraRestClient) {
        queueCounter++
        val coroutineCallIssueTypes = CoroutineScope(Dispatchers.IO)
        coroutineCallIssueTypes.async {
            val metaDataClient = restClient.metadataClient
            val issueTypeList = metaDataClient.issueTypes
            issueTypeList.claim().forEach {
                arrayListIssueTypes.add(it.name)
                arrayListIssueTypesId.add(it.id.toInt())
            }
            updateFields()
        }
    }

    private fun jiraTaskGatherAssignees(restClient: JiraRestClient) {

//        https://appcaesars.atlassian.net/rest/api/2/user/search?query
        queueCounter++
        val coroutineCallGatherAssignee = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherAssignee.async {
            RetrofitUserJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/user/")
                .create(AccountIdService::class.java)
                .getAccountIdList().enqueue(object : retrofit2.Callback<List<JiraUserModel>> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<List<JiraUserModel>>,
                        t: Throwable
                    ) {
                        jiraExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<JiraUserModel>>,
                        response: retrofit2.Response<List<JiraUserModel>>
                    ) {
                        val displayNameList = response.body()
                        displayNameList?.forEach {
                            if (it.displayName != null) {
                                if (!arrayListAssignee.contains(it.displayName!!)) {
                                    arrayListAssignee.add(it.displayName!!)
                                    arrayListReporter.add(it.displayName!!)
                                    arrayListAccountId.add(it.accountId!!)
                                    arrayListSelf.add(it.self!!)
                                    arrayListEmailAdresses.add(it.emailAddress!!)
                                }
                            }
                        }
//                        jiraTaskGatherReporters(restClient = restClient)
                        updateFields()
                    }
                })
        }


//        val searchClient = restClient.searchClient
//        searchClient.searchJql("project = LGB").claim().issues.forEach {
//            if (it.assignee != null) {
//                if (!arrayListAssignee.contains(it.assignee!!.displayName)) {
//                    arrayListAssignee.add(it.assignee!!.displayName)
//                }
//            }
//
//            if (it.reporter != null) {
//                if (!arrayListAssignee.contains(it.reporter!!.displayName)) {
//                    arrayListAssignee.add(it.reporter!!.displayName)
//                }
//            }
//
//        }
//        val userClient = restClient.userClient
//        userClient.getUser("").claim()
    }
//            val projectRolesClient = restClient.projectRolesRestClient
//            val issueClient = restClient.issueClient
//


    private fun jiraTaskGatherLinkedIssues(restClient: JiraRestClient) {
        queueCounter++
        val coroutineCallLinkedIssues = CoroutineScope(Dispatchers.IO)
        coroutineCallLinkedIssues.async {
            val metaDataClient = restClient.metadataClient
            val linkedIssuesList = metaDataClient.issueLinkTypes
            linkedIssuesList.claim().forEach {
                if (!arrayListIssueLinkedTypes.contains(it.outward)) {
                    arrayListIssueLinkedTypes.add(it.outward)
                    hashMapLinkedIssues[it.outward] = it.name

                }
                if (!arrayListIssueLinkedTypes.contains(it.inward)) {
                    arrayListIssueLinkedTypes.add(it.inward)
                    hashMapLinkedIssues[it.inward] = it.name
                }
            }
            updateFields()
        }
    }

    private fun jiraTaskGatherIssues(restClient: JiraRestClient) {
        queueCounter++
        val coroutineCallGatherIssues = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherIssues.async {
            val searchClient = restClient.searchClient
            val projectClient = restClient.projectClient
//        arrayListIssues.add("")
//        arrayListLabel.add("")
//        arrayListEpicLink.add("")
            projectClient.allProjects.claim().forEach {
                searchClient.searchJql("project=" + it.key).claim().issues.forEach { issue ->
                    //                issue.fields.forEach {
//                    Log.d("sprint",it.name)
//                }

//                if (issue.getField("Sprint")?.value != null) {
//                    arrayListSprint.add(issue.getField("Sprint")?.value.toString())
//                }
                    arrayListIssues.add(issue.key)
                    if (issue.issueType.name == "Epic") {
                        arrayListEpicLink.add(issue.key)
                    }
                    issue.labels.forEach { label ->
                        arrayListLabel.add(label)
                    }
                }
            }
            updateFields()
        }

    }

    private fun jiraTaskGatherPriorities(restClient: JiraRestClient) {
        queueCounter++
        val coroutineCallPriorities = CoroutineScope(Dispatchers.IO)
        coroutineCallPriorities.async {
            val metaDataClient = restClient.metadataClient
            val priorityList = metaDataClient.priorities
            priorityList.claim().forEach {
                arrayListPriorities.add(it.name)
                if (it.id != null) {
                    arrayListPrioritiesId.add(it.id!!.toInt())
                }
            }
            updateFields()
        }
    }

    private fun jiraTaskGatherFixComp(projectClient: ProjectRestClient, projectKey: String) {
        projectClient.getProject(projectKey).claim().components.forEach { component ->
            arrayListComponents.add(component.name)
            hashMapComponent[component.name] =
                projectClient.getProject(projectKey).claim().components
        }
        projectClient.getProject(projectKey).claim().versions.forEach { version ->
            arrayListFixVersions.add(version.name)
            hashMapFixVersions[version.name] =
                projectClient.getProject(projectKey).claim().versions
        }
    }

    private fun jiraTaskGatherSprint(restClient: JiraRestClient) {
        queueCounter++
        val coroutineCallSprint = CoroutineScope(Dispatchers.IO)
        coroutineCallSprint.async {
            RetrofitUserJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/agile/1.0/")
                .create(AccountIdService::class.java)
                .getBoardList().enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(call: retrofit2.Call<JsonObject>, t: Throwable) {
                        jiraExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        Log.d("sprint_details", response.code().toString())
                        val boardList = response.body()
                        boardList?.getAsJsonArray("values")?.forEach {
                            if (it.asJsonObject["type"].asString == "scrum") {
                                arrayListBoardId.add(it.asJsonObject["id"].asString)
                            }
                        }
                        arrayListBoardId.forEach {
                            RetrofitUserJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/agile/1.0/board/$it/")
                                .create(AccountIdService::class.java)
                                .getSprintList()
                                .enqueue(object : retrofit2.Callback<JsonObject> {
                                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                                    override fun onFailure(
                                        call: retrofit2.Call<JsonObject>,
                                        t: Throwable
                                    ) {
                                        jiraExceptionHandler(throwable = t)
                                    }

                                    override fun onResponse(
                                        call: retrofit2.Call<JsonObject>,
                                        response: retrofit2.Response<JsonObject>
                                    ) {
                                        Log.d("sprint_details", response.code().toString())
                                        val displaySprintList = response.body()
                                        displaySprintList?.getAsJsonArray("values")
                                            ?.forEach { sprint ->
                                                if (sprint.asJsonObject["state"].asString == "active") {
                                                    arrayListSprintName.add(sprint.asJsonObject["name"].asString)
                                                    hashMapSprint[sprint.asJsonObject["name"].asString] =
                                                        sprint.asJsonObject["id"].asString
                                                }
                                            }

                                    }
                                })
                        }
                        updateFields()
                    }
                })
        }
    }

    private fun jiraTaskGatherBoards() {
        queueCounter++
        val coroutineCallGatherBoards = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherBoards.async {
            RetrofitUserJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/agile/1.0/")
                .create(AccountIdService::class.java)
                .getBoardList()
                .enqueue(object : retrofit2.Callback<JsonObject> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<JsonObject>,
                        t: Throwable
                    ) {
                        jiraExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<JsonObject>,
                        response: retrofit2.Response<JsonObject>
                    ) {
                        Log.d("board_details", response.code().toString())
                        val boardList = response.body()
                        boardList?.getAsJsonArray("values")?.forEach {
                            hashMapBoard[it.asJsonObject["location"].asJsonObject["projectKey"].asString] =
                                it.asJsonObject["type"].asString
                        }
                        updateFields()
                    }
                })
        }
    }

    private fun jiraTaskGatherSprintFields(restClient: JiraRestClient) {
        queueCounter++
        val coroutineCallGatherFields = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherFields.async {
            val metaDataClient = restClient.metadataClient
            metaDataClient.fields.claim().forEach {
                when (it.name) {
                    "Sprint" -> sprintField = it.id
                    "Start date" -> startDateField = it.id
                }
            }
            //           arrayListFields.add(it.id)
            //            arrayListFields.add(it.name)
            updateFields()
        }

    }


    private fun jiraTaskGatherReporters(restClient: JiraRestClient) {
        arrayListReporter.addAll(arrayListAssignee)
    }

    internal fun getArrayListProjects(): ArrayList<String> {
        return arrayListProjects
    }

    internal fun getArrayListIssueTypes(): ArrayList<String> {
        return arrayListIssueTypes
    }

    internal fun getArrayListAsignee(): ArrayList<String> {
        return arrayListAssignee
    }

    internal fun getArrayListReporter(): ArrayList<String> {
        return arrayListReporter
    }

    internal fun getArrayListIssueLinkedTypes(): ArrayList<String> {
        return arrayListIssueLinkedTypes
    }

    internal fun getArrayListIssues(): ArrayList<String> {
        return arrayListIssues
    }

    internal fun getArrayListPriorities(): ArrayList<String> {
        return arrayListPriorities
    }

    internal fun getArrayListComponent(): ArrayList<String> {
        return arrayListComponents
    }

    internal fun getArrayListFixVersions(): ArrayList<String> {
        return arrayListFixVersions
    }

    internal fun getArrayListLabel(): ArrayList<String> {
        return arrayListLabel
    }

    internal fun getArrayListEpicLink(): ArrayList<String> {
        return arrayListEpicLink
    }


    internal fun gatherJiraSpinnerDetails(
//        spinnerProject: Spinner,
        autoTextViewProject: AutoCompleteTextView,
//        spinnerIssueType: Spinner,
        autoTextViewIssueType: AutoCompleteTextView,
//        spinnerReporter: Spinner,
        autoTextViewReporter: AutoCompleteTextView,
//        spinnerLinkedIssues: Spinner,
        autoTextViewLinkedIssues: AutoCompleteTextView,
//        spinnerIssues: Spinner,
        autoTextViewIssues: AutoCompleteTextView,
//        spinnerAssignee: Spinner,
        autoTextViewAssignee: AutoCompleteTextView,
//        spinnerPriority: Spinner,
        autoTextViewPriority: AutoCompleteTextView,
//        spinnerComponent: Spinner,
        autoTextViewComponent: AutoCompleteTextView,
//        spinnerFixVersions: Spinner,
        autoTextViewFixVersions: AutoCompleteTextView,
//        spinnerLabel: Spinner,
        autoTextViewLabel: AutoCompleteTextView,
//        spinnerEpicLink: Spinner,
        autoTextViewEpicLink: AutoCompleteTextView,
//        spinnerSprint: Spinner
        autoTextViewSprint: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
//        project = spinnerProject.selectedItem.toString()
//        projectPosition = spinnerProject.selectedItemPosition
        issueType = autoTextViewIssueType.editableText.toString()
//        issueType = spinnerIssueType.selectedItem.toString()
//        issueTypePosition = spinnerIssueType.selectedItemPosition
        reporter = autoTextViewReporter.editableText.toString()
//        reporter = spinnerReporter.selectedItem.toString()
//        reporterPosition = spinnerReporter.selectedItemPosition
        linkedIssue = autoTextViewLinkedIssues.editableText.toString()
//        linkedIssue = spinnerLinkedIssues.selectedItem.toString()
        issue = autoTextViewIssues.editableText.toString()
//        issue = spinnerIssues.selectedItem.toString()
        assignee = autoTextViewAssignee.editableText.toString()
//        assignee = spinnerAssignee.selectedItem.toString()
//        assigneePosition = spinnerAssignee.selectedItemPosition
        priority = autoTextViewPriority.editableText.toString()
//        priority = spinnerPriority.selectedItem.toString()
//        priorityPosition = spinnerPriority.selectedItemPosition
//        componentPosition = spinnerComponent.selectedItemPosition
//        fixVersionPosition = spinnerFixVersions.selectedItemPosition
//        linkedIssueTypePosition = spinnerLinkedIssues.selectedItemPosition
        arrayListChoosenLabel.add(autoTextViewLabel.editableText.toString())
//        arrayListChoosenLabel.add(spinnerLabel.selectedItem.toString())
        epicLink = autoTextViewEpicLink.editableText.toString()
//        epicLink = spinnerEpicLink.selectedItem.toString()
        //  sprint = spinnerSprint.selectedItem.toString()
        component = autoTextViewComponent.editableText.toString()
        fixVersion = autoTextViewFixVersions.editableText.toString()
        sprint = autoTextViewSprint.editableText.toString()
    }

    internal fun gatherJiraEditTextDetails(
        editTextSummary: EditText,
        editTextDescription: EditText
    ) {
        summary = editTextSummary.text.toString()
        description = editTextDescription.text.toString()
    }

    internal fun gatherJiraRecyclerViewDetails(arrayListRecyclerViewItems: ArrayList<RecyclerViewModel>) {
        this.arrayListRecyclerViewItems = arrayListRecyclerViewItems
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkSummaryEmpty(activity: Activity, context: Context): Boolean {
        return if (summary.isNotEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = context.resources.getString(R.string.jira_summary_empty)
                )
            }
//            activity.runOnUiThread {
//                Toast.makeText(
//                    context,
//                    R.string.jira_summary_empty,
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkReporterEmpty(activity: Activity, context: Context): Boolean {
        return if (arrayListReporter.contains(reporter)) {
            true
        } else {
            activity.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = context.resources.getString(R.string.jira_reporter_empty)
                )
            }
            false
        }
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkFixVersionsEmpty(activity: Activity, context: Context): Boolean {
        return if (arrayListFixVersions.contains(fixVersion) || fixVersion!!.isEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = context.resources.getString(R.string.jira_fix_version_empty)
                )
            }
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkEpicLinkEmpty(activity: Activity, context: Context): Boolean {
        return if (arrayListEpicLink.contains(epicLink) || epicLink!!.isEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = context.resources.getString(R.string.jira_epic_link_empty)
                )
            }
            false
        }
    }


    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    internal fun setIssueTypePosition(issueTypePosition: Int) {
        this.issueTypePosition = issueTypePosition
    }

    internal fun setReporterPosition(reporterPosition: Int) {
        this.reporterPosition = reporterPosition
    }

    internal fun setAssigneePosition(assigneePosition: Int) {
        this.assigneePosition = assigneePosition
    }

    internal fun setPriorityPosition(priorityPosition: Int) {
        this.priorityPosition = priorityPosition
    }

    internal fun setComponentPosition(componentPosition: Int) {
        this.componentPosition = componentPosition
    }

    internal fun setFixVersionsPosition(fixVersionsPosition: Int) {
        this.fixVersionPosition = fixVersionsPosition
    }

    internal fun setLinkedIssueTypePosition(linkedIssueTypePosition: Int) {
        this.linkedIssueTypePosition = linkedIssueTypePosition
    }

    internal fun setSprintPosition(sprintPosition: Int) {
        this.sprintPosition = sprintPosition
    }

    internal fun setStartDate(startDate: String?) {
        this.startDate = startDate
    }

    private fun resetJiraValues() {
        queueCreateTask--
        if (queueCreateTask == 0) {
            project = null
            issueType = null
            reporter = null
            linkedIssue = null
            issue = null
            assignee = null
            priority = null
            description = null
            label = null
            epicLink = null
            sprint = null
            component = null
            fixVersion = null
            sprintField = null
            startDateField = null
            startDate = null
            issueTypePosition = 0
            priorityPosition = 0
            projectPosition = 0
            componentPosition = 0
            fixVersionPosition = 0
            linkedIssueTypePosition = 0
            labelPosition = 0
            assigneePosition = 0
            reporterPosition = 0
            sprintPosition = 0
        }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun jiraUnhandledExceptionTask(
        context: Context,
        activity: Activity,
        filePath: File
    ) {
//        withContext(coroutineCallJira.coroutineContext) {
        try {
            jiraTaskCreateIssue(
                restClient = jiraAuthentication(),
                context = context,
                createMethod = "unhandled",
                activity = activity,
                filePathMediaName = filePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBirdService.loggerBirdService.detachProgressBar()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
//        }
    }

    //    internal fun duplicateIssueCheck(restClient: JiraRestClient): Boolean {
//        val sharedPref =
//            PreferenceManager.getDefaultSharedPreferences(LoggerBird.context.applicationContext)
//        if (sharedPref.getString("unhandled_file_path", null) != null) {
//            val fileUnhandled = File(sharedPref.getString("unhandled_file_path", null)!!)
//            var fileIssue: File
//                try {
//                    val arrayListFile:ArrayList<File> = ArrayList()
//                    val projectClient = restClient.projectClient
//                    val searchClient = restClient.searchClient
//                    projectClient.allProjects.claim().forEach {
//                        //                        val jsonObjectAssignee = JsonObject()
////                        jsonObjectAssignee.addProperty(
////                            "accountId",
////                            arrayListAccountId[assigneePosition]
////                        )
//                        RetrofitUserJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
//                            .create(AccountIdService::class.java)
//                            .getAttachmentList(projectKey = it.key , attachmentTitle = "attachment")
//                            .enqueue(object : retrofit2.Callback<JsonObject> {
//                                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
//                                override fun onFailure(
//                                    call: retrofit2.Call<JsonObject>,
//                                    t: Throwable
//                                ) {
////                                    resetJiraValues()
//                                    jiraExceptionHandler(throwable = t)
//                                }
//
//                                override fun onResponse(
//                                    call: retrofit2.Call<JsonObject>,
//                                    response: retrofit2.Response<JsonObject>
//                                ) {
////                                    resetJiraValues()
//                                    Log.d("attachment_get_success", response.code().toString())
//                                    response.body()?.getAsJsonArray("issues")?.forEach { issue ->
//                                        val jsonObjectIssues:JsonObject = issue.asJsonObject
//                                            val jsonObjectFields:JsonObject = jsonObjectIssues.asJsonObject["fields"].asJsonObject
//                                            jsonObjectFields.getAsJsonArray("attachment").forEach { self ->
//                                                fileIssue = File(URI(self.asJsonObject["content"].asString))
//                                                if (fileIssue.readBytes().contentEquals(
//                                                        fileUnhandled.readBytes()
//                                                    )) {
//                                                    Log.d("found_duplicate", "duplication!")
//                                                    return
//                                                }
//                                        }
//                                    }
//                                }
//                            })
////                        searchClient.searchJql("project=" + it.key+"&fields=attachment").claim()
////                            .issues.forEach { issue ->
////                            issue.attachments.find { file ->
////                                fileIssue = File(file.contentUri)
////                                Log.d("file",file.filename)
////                                if (fileIssue == fileUnhandled) {
////                                    Log.d("found_duplicate", "duplication!")
////                                    return@async true
////                                }
////                                return@forEach
////                            }
//
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    LoggerBird.callEnqueue()
//                    LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
//                }
//        }
//        return false
//    }
    internal fun duplicateErrorMessageCheck(
        restClient: JiraRestClient,
        activity: Activity
    ): Boolean {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        var controlDuplication = false
        if (sharedPref.getString("unhandled_exception_message", null) != null) {
            val exceptionMessage = sharedPref.getString("unhandled_exception_message", null)
            val searchClient = restClient.searchClient
            val projectClient = restClient.projectClient
            projectClient.allProjects.claim().forEach {
                searchClient.searchJql("project=" + it.key).claim().issues.forEach { issue ->
                    if (issue.description == exceptionMessage) {
                        Log.d("found_duplication", "true!!!!")
                        controlDuplication = true
                        return controlDuplication
                    }
                }
            }
        }
        return controlDuplication
    }

    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("jira_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 20000)
    }
}
