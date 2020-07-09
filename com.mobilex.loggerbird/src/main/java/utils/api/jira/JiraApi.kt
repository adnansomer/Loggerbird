package utils.api.jira

import adapter.recyclerView.api.jira.RecyclerViewJiraAttachmentAdapter
import adapter.recyclerView.api.jira.RecyclerViewJiraComponentAdapter
import adapter.recyclerView.api.jira.RecyclerViewJiraFixVersionsAdapter
import adapter.recyclerView.api.jira.RecyclerViewJiraIssueAdapter
import adapter.recyclerView.api.jira.RecyclerViewJiraLabelAdapter
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import loggerbird.LoggerBird.Companion.jiraDomainName
import models.*
import models.api.jira.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import services.LoggerBirdService
import utils.other.DefaultToast
import utils.other.InternetConnectionUtil
import java.io.*
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/** Loggerbird Jira api configration class **/
internal class JiraApi {
    //Global variables.
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
    private var epicNamePosition: Int = 0
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
    private var epicName: String = ""
    private var component: String? = null
    private var fixVersion: String? = null
    private val hashMapComponent: HashMap<String, String> = HashMap()
    private val hashMapComponentPosition: HashMap<String, Int> = HashMap()
    private val hashMapFixVersions: HashMap<String, String> = HashMap()
    private val hashMapFixVersionsPosition: HashMap<String, Int> = HashMap()
    private val hashMapLinkedIssues: HashMap<String, String> = HashMap()
    private val hashMapSprint: HashMap<String, String> = HashMap()
    private val hashMapBoard: HashMap<String, String> = HashMap()
    private val arrayListProjects: ArrayList<String> = ArrayList()
    private val arrayListProjectKeys: ArrayList<String> = ArrayList()
    private val arrayListIssueTypes: ArrayList<String> = ArrayList()
    private val arrayListIssueTypesId: ArrayList<Int> = ArrayList()
    private val arrayListAssignee: ArrayList<String> = ArrayList()
    private val arrayListIssueLinkedTypes: ArrayList<String> = ArrayList()
    private val arrayListOutwardLinkedTypes: ArrayList<String> = ArrayList()
    private val arrayListInwardLinkedTypes: ArrayList<String> = ArrayList()
    private val arrayListIssues: ArrayList<String> = ArrayList()
    private val arrayListReporter: ArrayList<String> = ArrayList()
    private val arrayListPriorities: ArrayList<String> = ArrayList()
    private val arrayListPrioritiesId: ArrayList<Int> = ArrayList()
    private val arrayListComponents: ArrayList<String> = ArrayList()
    private val arrayListFixVersions: ArrayList<String> = ArrayList()
    private val arrayListLabel: ArrayList<String> = ArrayList()
    private val arrayListChoosenLabel: ArrayList<String> = ArrayList()
    private val arrayListEpicLink: ArrayList<String> = ArrayList()
    //private val arrayListFields: ArrayList<String> = ArrayList()
    private val arrayListSprintName: ArrayList<String> = ArrayList()
    private val arrayListEpicName: ArrayList<String> = ArrayList()
    private val arrayListAccountId: ArrayList<String> = ArrayList()
    private val arrayListSelf: ArrayList<String> = ArrayList()
    private val arrayListEmailAdresses: ArrayList<String> = ArrayList()
    private val arrayListAvatarUrls: ArrayList<String> = ArrayList()
    private val arrayListBoardId: ArrayList<String> = ArrayList()
    private val defaultToast: DefaultToast =
        DefaultToast()
    private var sprintField: String? = null
    private var queueCounter = 0
    private lateinit var activity: Activity
    private var startDateField: String? = null
    private var startDate: String? = null
    private var epicNameField: String? = null
    private var epicLinkField: String? = null
    private var queueCreateTask = 0
    private lateinit var timerTaskQueue: TimerTask
    private var controlDuplication = false

    /**
     * This method is used for calling an jira action with network connection check.
     * @param filePathMedia is used for getting the reference of current media file.
     * @param context is for getting reference from the application context.
     * @param activity is used for getting reference of current activity.
     * @param task is for getting reference of which jira action will be executed.
     * @param createMethod is for getting reference of which create jira method will be executed.
     * @throws LoggerBirdException if network connection error occurs.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun callJira(
        filePathMedia: File? = null,
        context: Context,
        activity: Activity,
        task: String,
        createMethod: String
    ) {
        coroutineCallJira.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    okHttpJiraAuthentication(
                        filePathMediaName = filePathMedia,
                        context = context,
                        activity = activity,
                        task = task,
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
                jiraExceptionHandler(e = e, filePathName = filePathMedia)
            }
        }
    }

    /**
     * This method is used for calling an jira action with internet connection check.
     * @param filePathMediaName is used for getting the reference of current media file.
     * @param context is for getting reference from the application context.
     * @param activity is used for getting reference of current activity.
     * @param task is for getting reference of which jira action will be executed.
     * @param createMethod is for getting reference of which create jira method will be executed.
     * @throws LoggerBirdException if internet connection error occurs.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun okHttpJiraAuthentication(
        filePathMediaName: File?,
        context: Context,
        activity: Activity,
        task: String,
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
                        when (task) {
                            "create" -> jiraTaskCreateIssue(
                                filePathMediaName = filePathMediaName,
                                activity = activity,
                                context = context,
                                createMethod = createMethod
                            )
                            "get" -> jiraTaskGatherDetails(
                                activity = activity
                            )
                            "unhandled_duplication" ->
                                if (duplicateErrorMessageCheck(
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

    /**
     * This method is used for calling an jira create action.
     * @param filePathMediaName is used for getting the reference of current media file.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param createMethod is for getting reference of which create jira method will be executed.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraTaskCreateIssue(
        filePathMediaName: File? = null,
        activity: Activity,
        context: Context,
        createMethod: String
    ) {
        try {
            when (createMethod) {
                "normal" -> jiraNormalTask(
                    context = context,
                    activity = activity
                )
                "unhandled" -> if (filePathMediaName != null) {
                    if (filePathMediaName.exists()) {
                        jiraUnhandledTask(
                            activity = activity,
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

    /**
     * This method is used for creating jira normal task.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraNormalTask(activity: Activity, context: Context) {
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
                val coroutineCallJiraCreateIssue = CoroutineScope(Dispatchers.IO)
                coroutineCallJiraCreateIssue.async {
                    try {
                        val jsonObjectIssue = JsonObject()
                        val jsonObjectContent = JsonObject()
                        val jsonObjectProjectId = JsonObject()
                        val jsonObjectIssueType = JsonObject()
                        val jsonObjectPriorityId = JsonObject()
                        val gson = GsonBuilder().create()
                        jsonObjectPriorityId.addProperty(
                            "id",
                            arrayListPrioritiesId[priorityPosition].toString()
                        )
                        jsonObjectIssueType.addProperty(
                            "id",
                            arrayListIssueTypesId[issueTypePosition].toString()
                        )
                        jsonObjectProjectId.addProperty(
                            "key",
                            arrayListProjectKeys[projectPosition]
                        )
                        jsonObjectContent.add("priority", jsonObjectPriorityId)
                        jsonObjectContent.add("project", jsonObjectProjectId)
                        jsonObjectContent.add("issuetype", jsonObjectIssueType)
                        jsonObjectContent.addProperty("summary", summary)
                        if (description != null || RecyclerViewJiraIssueAdapter.ViewHolder.arrayListIssueNames.isNotEmpty()) {
                            if (description!!.isNotEmpty() || RecyclerViewJiraIssueAdapter.ViewHolder.arrayListIssueNames.isNotEmpty()) {
                                val stringBuilderDescription = StringBuilder()
                                stringBuilderDescription.append("Description:$description")
                                var counter = 0
                                RecyclerViewJiraIssueAdapter.ViewHolder.arrayListIssueNames.forEach {
                                    stringBuilderDescription.append("\n" + "Linked Issue_" + counter + ":" + LoggerBird.jiraDomainName + "/browse/" + it.issueName)
                                    counter++
                                }
                                jsonObjectContent.addProperty(
                                    "description",
                                    stringBuilderDescription.toString()
                                )
                            }
                        }
                        if (RecyclerViewJiraLabelAdapter.ViewHolder.arrayListLabelNames.isNotEmpty()) {
                            val jsonArrayLabels = JsonArray()
                            RecyclerViewJiraLabelAdapter.ViewHolder.arrayListLabelNames.forEach {
                                jsonArrayLabels.add(it.labelName)
                            }
                            jsonObjectContent.add("labels", jsonArrayLabels)
                        } else {
                            if (arrayListChoosenLabel.isNotEmpty()) {
                                val jsonArrayLabels =
                                    gson.toJsonTree(arrayListChoosenLabel).asJsonArray
                                jsonObjectContent.add("labels", jsonArrayLabels)
                            }
                        }
                        if (issueType == "Epic") {
                            if (epicName.isNotEmpty() && epicNameField != null) {
                                jsonObjectContent.addProperty(epicNameField, epicName)
                            }
                        }

                        if (RecyclerViewJiraComponentAdapter.ViewHolder.arrayListComponentNames.isNotEmpty()) {
                            val jsonArrayComponent = JsonArray()
                            var jsonObjectComponentId: JsonObject
                            RecyclerViewJiraComponentAdapter.ViewHolder.arrayListComponentNames.forEach {
                                jsonObjectComponentId = JsonObject()
                                jsonObjectComponentId.addProperty(
                                    "id",
                                    hashMapComponent[arrayListComponents[hashMapComponentPosition[it.componentName]!!]]
                                )
                                jsonArrayComponent.add(jsonObjectComponentId)
                            }
                            jsonObjectContent.add("components", jsonArrayComponent)
                        } else {
                            if (arrayListComponents.size > componentPosition) {
                                if (arrayListComponents[componentPosition].isNotEmpty() && component != null) {
                                    if (component!!.isNotEmpty()) {
                                        val jsonArrayComponent = JsonArray()
                                        val jsonObjectComponentId = JsonObject()
                                        jsonObjectComponentId.addProperty(
                                            "id",
                                            hashMapComponent[arrayListComponents[componentPosition]]
                                        )
                                        jsonArrayComponent.add(jsonObjectComponentId)
                                        jsonObjectContent.add("components", jsonArrayComponent)
                                    }

                                }
                            }
                        }
                        if (RecyclerViewJiraFixVersionsAdapter.ViewHolder.arrayListFixVersionsNames.isNotEmpty()) {
                            val jsonArrayFixVersions = JsonArray()
                            var jsonObjectFixVersionsId: JsonObject
                            RecyclerViewJiraFixVersionsAdapter.ViewHolder.arrayListFixVersionsNames.forEach {
                                jsonObjectFixVersionsId = JsonObject()
                                jsonObjectFixVersionsId.addProperty(
                                    "id",
                                    hashMapFixVersions[arrayListFixVersions[hashMapFixVersionsPosition[it.fixVersionsName]!!]]
                                )
                                jsonArrayFixVersions.add(jsonObjectFixVersionsId)
                            }
                            jsonObjectContent.add("fixVersions", jsonArrayFixVersions)
                        } else {
                            if (arrayListFixVersions.size > fixVersionPosition) {
                                if (arrayListFixVersions[fixVersionPosition].isNotEmpty() && fixVersion != null) {
                                    if (fixVersion!!.isNotEmpty()) {
                                        val jsonArrayFixVersions = JsonArray()
                                        val jsonObjectFixVersionsId = JsonObject()
                                        jsonObjectFixVersionsId.addProperty(
                                            "id",
                                            hashMapFixVersions[arrayListFixVersions[fixVersionPosition]]
                                        )
                                        jsonArrayFixVersions.add(jsonObjectFixVersionsId)
                                        jsonObjectContent.add("fixVersions", jsonArrayFixVersions)
                                    }
                                }
                            }
                        }
                        if (epicLink != null) {
                            if (epicLink!!.isNotEmpty()) {
                                jsonObjectContent.addProperty(
                                    epicLinkField,
                                    epicLink!!.substringAfter("(").substringBefore(")")
                                )
                            }
                        }

                        jsonObjectIssue.add("fields", jsonObjectContent)
                        if (issue != null || RecyclerViewJiraIssueAdapter.ViewHolder.arrayListIssueNames.isNotEmpty()) {
                            val jsonArrayIssue = JsonArray()
                            var jsonObjectIssueAdd = JsonObject()
                            var jsonObjectIssueLink = JsonObject()
                            var jsonObjectIssueLinkType = JsonObject()
                            var jsonObjectOutwardIssueKey = JsonObject()
                            val jsonObjectUpdate = JsonObject()
                            if (RecyclerViewJiraIssueAdapter.ViewHolder.arrayListIssueNames.isNotEmpty()) {
//                                RecyclerViewJiraIssueAdapter.ViewHolder.arrayListIssueNames.forEach {
                                jsonObjectOutwardIssueKey = JsonObject()
                                jsonObjectIssueAdd = JsonObject()
                                jsonObjectIssueLink = JsonObject()
                                jsonObjectIssueLinkType = JsonObject()
                                jsonObjectIssueLinkType.addProperty(
                                    "name",
                                    hashMapLinkedIssues[arrayListIssueLinkedTypes[linkedIssueTypePosition]]
                                )
                                jsonObjectOutwardIssueKey.addProperty(
                                    "key",
                                    RecyclerViewJiraIssueAdapter.ViewHolder.arrayListIssueNames[0].issueName
                                )
                                jsonObjectIssueLink.add("type", jsonObjectIssueLinkType)

                                if (arrayListInwardLinkedTypes.contains(arrayListIssueLinkedTypes[linkedIssueTypePosition])) {
                                    jsonObjectIssueLink.add(
                                        "inwardIssue",
                                        jsonObjectOutwardIssueKey
                                    )

                                } else if (arrayListOutwardLinkedTypes.contains(
                                        arrayListIssueLinkedTypes[linkedIssueTypePosition]
                                    )
                                ) {
                                    jsonObjectIssueLink.add(
                                        "outwardIssue",
                                        jsonObjectOutwardIssueKey
                                    )
                                }
                                jsonObjectIssueAdd.add("add", jsonObjectIssueLink)
                                jsonArrayIssue.add(jsonObjectIssueAdd)
//                                }
                            } else {
                                if (issue!!.isNotEmpty()) {
                                    jsonObjectIssueLinkType.addProperty(
                                        "name",
                                        hashMapLinkedIssues[arrayListIssueLinkedTypes[linkedIssueTypePosition]]
                                    )
                                    jsonObjectOutwardIssueKey.addProperty("key", issue)
                                    jsonObjectIssueLink.add("type", jsonObjectIssueLinkType)

                                    if (arrayListInwardLinkedTypes.contains(
                                            arrayListIssueLinkedTypes[linkedIssueTypePosition]
                                        )
                                    ) {
                                        jsonObjectIssueLink.add(
                                            "inwardIssue",
                                            jsonObjectOutwardIssueKey
                                        )

                                    } else if (arrayListOutwardLinkedTypes.contains(
                                            arrayListIssueLinkedTypes[linkedIssueTypePosition]
                                        )
                                    ) {
                                        jsonObjectIssueLink.add(
                                            "outwardIssue",
                                            jsonObjectOutwardIssueKey
                                        )
                                    }
                                    jsonObjectIssueAdd.add("add", jsonObjectIssueLink)
                                    jsonArrayIssue.add(jsonObjectIssueAdd)
//                                val linkIssueInput = LinkIssuesInput(
//                            issueKey,
//                            this.issue,
//                            hashMapLinkedIssues[arrayListIssueLinkedTypes[linkedIssueTypePosition]]
//                        )
//                        issueClient.linkIssue(linkIssueInput)
                                }
                            }
                            jsonObjectUpdate.add("issuelinks", jsonArrayIssue)
                            jsonObjectIssue.add("update", jsonObjectUpdate)

                        }
                        RetrofitJiraClient.getJiraUserClient(
                            url = "$jiraDomainName/rest/api/2/"
                        )
                            .create(AccountIdService::class.java)
                            .createIssue(jsonObjectIssue)
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
                                    val coroutineCallCreateIssue = CoroutineScope(Dispatchers.IO)
                                    coroutineCallCreateIssue.async {
                                        try {
                                            Log.d(
                                                "create_issue_details",
                                                response.code().toString()
                                            )
                                            val createdIssue = response.body()
                                            val issueKey = createdIssue!!["key"].asString
                                            createAssignee(issueKey = issueKey)
                                            createReporter(issueKey = issueKey)
                                            createSprint(issueKey = issueKey)
                                            createDate(issueKey = issueKey)
                                            RecyclerViewJiraAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                                                val file = it.file
                                                if (file.exists()) {
                                                    createAttachments(
                                                        activity = activity,
                                                        issueKey = issueKey,
                                                        file = file,
                                                        task = "normal"
                                                    )
                                                }
                                            }
                                        } catch (e: Exception) {
                                            jiraExceptionHandler(e = e)
                                        }
                                    }
                                    //updateFields()
                                }
                            })
                    } catch (e: Exception) {
                        jiraExceptionHandler(e = e)
                    }
                }
            }
        } catch (e: Exception) {
            jiraExceptionHandler(e = e)
        }
    }


    /**
     * This method is used for creating issue assignee when jira issue created.
     * @param issueKey is used for getting reference of current created issue id.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun createAssignee(issueKey: String) {
        if (this.assignee != null) {
            if (this.assignee!!.isNotEmpty()) {
                queueCreateTask++
                val jsonObjectAssignee = JsonObject()
                jsonObjectAssignee.addProperty(
                    "accountId",
                    arrayListAccountId[assigneePosition]
                )
                RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/issue/$issueKey/")
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
    }

    /**
     * This method is used for creating issue reporter when jira issue created.
     * @param issueKey is used for getting reference of current created issue id.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun createReporter(issueKey: String) {
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
                RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/issue/$issueKey/")
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
    }

    /**
     * This method is used for creating issue sprint when jira issue created.
     * @param issueKey is used for getting reference of current created issue id.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun createSprint(issueKey: String) {
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
                RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/issue/$issueKey/")
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
    }

    /**
     * This method is used for creating issue date when jira issue created.
     * @param issueKey is used for getting reference of current created issue id.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun createDate(issueKey: String) {
        if (this.startDate != null && this.startDateField != null) {
            queueCreateTask++
            val jsonObjectStartDate = JsonObject()
            val jsonObjectFieldStartDate = JsonObject()
            jsonObjectStartDate.addProperty(
                startDateField,
                startDate
            )
            jsonObjectFieldStartDate.add("fields", jsonObjectStartDate)
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/issue/$issueKey/")
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
    }

    /**
     * This method is used for creating issue attachment when jira issue created.
     * @param issueKey is used for getting reference of current created issue id.
     * @param file is used for getting reference of the current file.
     * @param activity is used for getting reference of current activity.
     * @param task is for getting reference of which jira action will be executed.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun createAttachments(issueKey: String, file: File, activity: Activity, task: String) {
        if (task != "unhandled") {
            queueCreateTask++
        }

        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/issue/$issueKey/")
            .create(AccountIdService::class.java)
            .setAttachments(file = body)
            .enqueue(object : retrofit2.Callback<List<JiraSprintModel>> {
                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                override fun onFailure(
                    call: retrofit2.Call<List<JiraSprintModel>>,
                    t: Throwable
                ) {
                    if (task != "unhandled") {
                        resetJiraValues()
                    }
                    jiraExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<List<JiraSprintModel>>,
                    response: retrofit2.Response<List<JiraSprintModel>>
                ) {
                    if (task != "unhandled") {
                        resetJiraValues()
                    }
                    if (file.name != "logger_bird_details.txt") {
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                    Log.d("attachment_put_success", response.code().toString())
                }
            })
    }

    /**
     * This method is used for creating jira unhandled task.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraUnhandledTask(
        activity: Activity,
        context: Context,
        filePathName: File
    ) {
        checkQueueTime(activity = activity)
        val coroutineCallCreateUnhandledIssue = CoroutineScope(Dispatchers.IO)
        coroutineCallCreateUnhandledIssue.async {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(LoggerBirdService.loggerBirdService.returnActivity().applicationContext)
            val jsonObjectIssue = JsonObject()
            val jsonObjectContent = JsonObject()
            val jsonObjectProjectId = JsonObject()
            val jsonObjectIssueType = JsonObject()
            val jsonObjectPriorityId = JsonObject()
            jsonObjectPriorityId.addProperty(
                "id",
                0
            )
            jsonObjectIssueType.addProperty(
                "id",
                10004
            )
            jsonObjectProjectId.addProperty(
                "key",
                "UN"
            )
            jsonObjectContent.add("project", jsonObjectProjectId)
            jsonObjectContent.add("issuetype", jsonObjectIssueType)
            jsonObjectContent.addProperty(
                "summary",
                context.resources.getString(R.string.jira_summary_unhandled_exception)
            )
            jsonObjectContent.addProperty(
                "description",
                sharedPref.getString("unhandled_exception_message", null)
            )
            jsonObjectIssue.add("fields", jsonObjectContent)
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
                .create(AccountIdService::class.java)
                .createIssue(jsonObjectIssue)
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
                        val coroutineCallCreateIssue = CoroutineScope(Dispatchers.IO)
                        coroutineCallCreateIssue.async {
                            try {
                                Log.d(
                                    "create_issue_details",
                                    response.code().toString()
                                )
                                val createdIssue = response.body()
                                val issueKey = createdIssue!!["key"].asString
                                if (filePathName.exists()) {
                                    createAttachments(
                                        issueKey = issueKey,
                                        file = filePathName,
                                        activity = activity,
                                        task = "unhandled"
                                    )
                                }
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
                            } catch (e: Exception) {
                                jiraExceptionHandler(e = e)
                            }
                        }
                    }
                })
        }

    }


    /**
     * This method is used for creating jira unhandled exception task.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePath is used for getting the reference of current file.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun jiraUnhandledExceptionTask(
        context: Context,
        activity: Activity,
        filePath: File
    ) {
        try {
            jiraTaskCreateIssue(
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
        activity: Activity
    ): Boolean {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        if (sharedPref.getString("unhandled_exception_message", null) != null) {
            val exceptionMessage = sharedPref.getString("unhandled_exception_message", null)
            jiraTaskGatherIssues(task = "duplication", exceptionMessage = exceptionMessage)
            Thread.sleep(10000)
        }
        return controlDuplication
    }

    /**
     * This method is used for initializing the gathering action of jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraTaskGatherDetails(
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
                arrayListOutwardLinkedTypes.clear()
                arrayListInwardLinkedTypes.clear()
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
                arrayListEpicName.clear()
                arrayListBoardId.clear()
                hashMapComponent.clear()
                hashMapComponentPosition.clear()
                hashMapFixVersions.clear()
                hashMapFixVersionsPosition.clear()
                hashMapLinkedIssues.clear()
                hashMapSprint.clear()
                hashMapBoard.clear()
                jiraTaskGatherSprintFields()
                jiraTaskGatherProject()
                jiraTaskGatherIssueTypes()
                jiraTaskGatherAssignees()
                jiraTaskGatherLinkedIssues()
                jiraTaskGatherIssues(task = "normal")
                jiraTaskGatherLabels()
                jiraTaskGatherPriorities()
                jiraTaskGatherSprint()
                jiraTaskGatherBoards()
                Log.d("que_counter", queueCounter.toString())
            }

        } catch (e: Exception) {
            jiraExceptionHandler(e = e)
        }
    }

    /**
     * This method is used for getting project details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherProject() {
        queueCounter++
        val coroutineCallProjectKeys = CoroutineScope(Dispatchers.IO)
        coroutineCallProjectKeys.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
                .create(AccountIdService::class.java)
                .getProjectList()
                .enqueue(object : retrofit2.Callback<List<JiraProjectModel>> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<List<JiraProjectModel>>,
                        t: Throwable
                    ) {
                        jiraExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<JiraProjectModel>>,
                        response: retrofit2.Response<List<JiraProjectModel>>
                    ) {
                        coroutineCallProjectKeys.async {
                            Log.d("project_details", response.code().toString())
                            val projectList = response.body()
                            projectList?.forEach {
                                if (it.name != null && it.key != null) {
                                    arrayListProjects.add(it.name!!)
                                    arrayListProjectKeys.add(it.key!!)
                                    jiraTaskGatherFixComp(
                                        projectKey = it.key!!
                                    )
                                }

                            }
                            updateFields()
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting issue type details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherIssueTypes() {
        queueCounter++
        val coroutineCallIssueTypes = CoroutineScope(Dispatchers.IO)
        coroutineCallIssueTypes.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
                .create(AccountIdService::class.java)
                .getIssueTypes()
                .enqueue(object : retrofit2.Callback<List<JiraIssueTypeModel>> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<List<JiraIssueTypeModel>>,
                        t: Throwable
                    ) {
                        jiraExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<JiraIssueTypeModel>>,
                        response: retrofit2.Response<List<JiraIssueTypeModel>>
                    ) {
                        coroutineCallIssueTypes.async {
                            Log.d("issue_type_details", response.code().toString())
                            val issueTypeList = response.body()
                            issueTypeList?.forEach {
                                if (it.name != null && it.id != null) {
                                    arrayListIssueTypes.add(it.name!!)
                                    arrayListIssueTypesId.add(it.id!!.toInt())
                                }

                            }
                            updateFields()
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting assignee details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherAssignees() {
        queueCounter++
        val coroutineCallGatherAssignee = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherAssignee.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/user/")
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
                        coroutineCallGatherAssignee.async {
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
                            updateFields()
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting linked issue details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherLinkedIssues() {
        queueCounter++
        val coroutineCallLinkedIssues = CoroutineScope(Dispatchers.IO)
        coroutineCallLinkedIssues.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
                .create(AccountIdService::class.java)
                .getLinkedIssueList()
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
                        coroutineCallLinkedIssues.async {
                            Log.d("linked_issue_details", response.code().toString())
                            val linkedIssueDetails = response.body()
                            linkedIssueDetails?.getAsJsonArray("issueLinkTypes")?.forEach {
                                if (!arrayListIssueLinkedTypes.contains(it.asJsonObject["inward"].asString)) {
                                    arrayListIssueLinkedTypes.add(it.asJsonObject["inward"].asString)
                                    arrayListInwardLinkedTypes.add(it.asJsonObject["inward"].asString)
                                    hashMapLinkedIssues[it.asJsonObject["inward"].asString] =
                                        it.asJsonObject["name"].asString
                                }
                                if (!arrayListIssueLinkedTypes.contains(it.asJsonObject["outward"].asString)) {
                                    arrayListIssueLinkedTypes.add(it.asJsonObject["outward"].asString)
                                    arrayListOutwardLinkedTypes.add(it.asJsonObject["outward"].asString)
                                    hashMapLinkedIssues[it.asJsonObject["outward"].asString] =
                                        it.asJsonObject["name"].asString
                                }
                            }
                            updateFields()
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting issue details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherIssues(task: String, exceptionMessage: String? = null) {
        if (task != "duplication") {
            queueCounter++
        }
        val coroutineCallGatherIssues = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherIssues.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
                .create(AccountIdService::class.java)
                .getIssueList()
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
                        coroutineCallGatherIssues.async {
                            Log.d("issue_details", response.code().toString())
                            val issueList = response.body()
                            issueList?.getAsJsonArray("issues")?.forEach {
                                if (task != "duplication" && exceptionMessage == null) {
                                    arrayListIssues.add(it.asJsonObject["key"].asString)
                                } else {
                                    if (exceptionMessage!! == it.asJsonObject["fields"].asJsonObject["description"].asString) {
                                        controlDuplication = true
                                    }
                                }
                            }
                            if (task != "duplication") {
                                updateFields()
                            }
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting label details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherLabels() {
        queueCounter++
        val coroutineCallGatherLabels = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherLabels.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
                .create(AccountIdService::class.java)
                .getLabelList()
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
                        coroutineCallGatherLabels.async {
                            Log.d("label_details", response.code().toString())
                            val labelList = response.body()
                            labelList?.getAsJsonArray("values")?.forEach {
                                arrayListLabel.add(it.asString)
                            }
                            updateFields()
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting epic details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherEpics() {
        queueCounter++
        val coroutineCallEpic = CoroutineScope(Dispatchers.IO)
        coroutineCallEpic.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
                .create(AccountIdService::class.java)
                .getEpicList()
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
                        coroutineCallEpic.async {
                            Log.d("epic_details", response.code().toString())
                            val epicList = response.body()
                            epicList?.getAsJsonArray("issues")?.forEach {

                                if (!arrayListEpicLink.contains(it.asJsonObject["fields"].asJsonObject[epicNameField].asString + " " + "-" + " " + "(" + it.asJsonObject["key"].asString + ")")) {
                                    arrayListEpicLink.add(it.asJsonObject["fields"].asJsonObject[epicNameField].asString + " " + "-" + " " + "(" + it.asJsonObject["key"].asString + ")")
                                }
                                if (!arrayListEpicName.contains(it.asJsonObject["fields"].asJsonObject[epicNameField].asString)) {
                                    arrayListEpicName.add(it.asJsonObject["fields"].asJsonObject[epicNameField].asString)
                                }
//                                arrayListEpicLink.add(it.asJsonObject[epicNameField].asString)
                            }
                            updateFields()
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting priority details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherPriorities() {
        queueCounter++
        val coroutineCallPriorities = CoroutineScope(Dispatchers.IO)
        coroutineCallPriorities.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
                .create(AccountIdService::class.java)
                .getPriorities()
                .enqueue(object : retrofit2.Callback<List<JiraPriorityModel>> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<List<JiraPriorityModel>>,
                        t: Throwable
                    ) {
                        jiraExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<JiraPriorityModel>>,
                        response: retrofit2.Response<List<JiraPriorityModel>>
                    ) {
                        coroutineCallPriorities.async {
                            Log.d("priority_details", response.code().toString())
                            val priorityList = response.body()
                            priorityList?.forEach {
                                if (it.name != null && it.id != null) {
                                    arrayListPriorities.add(it.name!!)
                                    arrayListPrioritiesId.add(it.id!!.toInt())
                                }
                            }
                            updateFields()
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting fixed versions and component details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherFixComp(projectKey: String) {
        queueCounter++
        val coroutineCallGatherfixComp = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherfixComp.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/project/$projectKey/")
                .create(AccountIdService::class.java)
                .getFixCompList()
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
                        coroutineCallGatherfixComp.async {
                            Log.d("fix_comp_details", response.code().toString())
                            val fixCompList = response.body()
                            var compPositionCounter = 0
                            fixCompList?.getAsJsonArray("components")?.forEach {
                                if (!arrayListComponents.contains(it.asJsonObject["name"].asString)) {
                                    arrayListComponents.add(it.asJsonObject["name"].asString)
                                    hashMapComponent[it.asJsonObject["name"].asString] =
                                        it.asJsonObject["id"].asString
                                    hashMapComponentPosition[it.asJsonObject["name"].asString] =
                                        compPositionCounter
                                }
                                compPositionCounter++
                            }
                            var fixPositionCounter = 0
                            fixCompList?.getAsJsonArray("versions")?.forEach {
                                if (!arrayListFixVersions.contains(it.asJsonObject["name"].asString)) {
                                    arrayListFixVersions.add(it.asJsonObject["name"].asString)
                                    hashMapFixVersions[it.asJsonObject["name"].asString] =
                                        it.asJsonObject["id"].asString
                                    hashMapFixVersionsPosition[it.asJsonObject["name"].asString] =
                                        fixPositionCounter
                                }
                                fixPositionCounter++
                            }
                            updateFields()
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting sprint details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherSprint() {
        queueCounter++
        val coroutineCallSprint = CoroutineScope(Dispatchers.IO)
        coroutineCallSprint.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/agile/1.0/")
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
                        coroutineCallSprint.async {
                            Log.d("sprint_details", response.code().toString())
                            val boardList = response.body()
                            boardList?.getAsJsonArray("values")?.forEach {
                                if (it.asJsonObject["type"].asString == "scrum") {
                                    arrayListBoardId.add(it.asJsonObject["id"].asString)
                                }
                            }
                            arrayListBoardId.forEach {
                                RetrofitJiraClient.getJiraUserClient(
                                    url = "$jiraDomainName/rest/agile/1.0/board/$it/"
                                )
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
                    }
                })
        }
    }

    /**
     * This method is used for getting board details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherBoards() {
        queueCounter++
        val coroutineCallGatherBoards = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherBoards.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/agile/1.0/")
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
                        coroutineCallGatherBoards.async {
                            Log.d("board_details", response.code().toString())
                            val boardList = response.body()
                            boardList?.getAsJsonArray("values")?.forEach {
                                hashMapBoard[it.asJsonObject["location"].asJsonObject["projectKey"].asString] =
                                    it.asJsonObject["type"].asString
                            }
                            updateFields()
                        }
                    }
                })
        }
    }

    /**
     * This method is used for getting sprint field details for jira.
     * @throws exception if error occurs.
     * @see jiraExceptionHandler method.
     */
    private fun jiraTaskGatherSprintFields() {
        queueCounter++
        val coroutineCallGatherFields = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherFields.async {
            RetrofitJiraClient.getJiraUserClient(url = "$jiraDomainName/rest/api/2/")
                .create(AccountIdService::class.java)
                .getFieldList()
                .enqueue(object : retrofit2.Callback<List<JiraFieldModel>> {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun onFailure(
                        call: retrofit2.Call<List<JiraFieldModel>>,
                        t: Throwable
                    ) {
                        jiraExceptionHandler(throwable = t)
                    }

                    override fun onResponse(
                        call: retrofit2.Call<List<JiraFieldModel>>,
                        response: retrofit2.Response<List<JiraFieldModel>>
                    ) {
                        coroutineCallGatherFields.async {
                            Log.d("field_details", response.code().toString())
                            val fieldList = response.body()
                            fieldList?.forEach {
                                when (it.name) {
                                    "Sprint" -> sprintField = it.id
                                    "Start date" -> startDateField = it.id
                                    "Epic Name" -> epicNameField = it.id
                                    "Epic Link" -> epicLinkField = it.id
                                }
                            }
                            updateFields()
                            jiraTaskGatherEpics()
                        }
                    }
                })
        }

    }

    /**
     * This method is used for getting details of autoCompleteTextViews in the jira layout.
     * @param autoTextViewProject is used for getting project details from project autoCompleteTextView in the jira layout.
     * @param autoTextViewIssueType is used for getting project details from issue type autoCompleteTextView in the jira layout.
     * @param autoTextViewReporter is used for getting project details from reporter autoCompleteTextView in the jira layout.
     * @param autoTextViewLinkedIssues is used for getting linked issue details from linked issues autoCompleteTextView in the jira layout.
     * @param autoTextViewIssues is used for getting issue details from issue autoCompleteTextView in the jira layout.
     * @param autoTextViewAssignee is used for getting assignee details from assignee autoCompleteTextView in the jira layout.
     * @param autoTextViewPriority is used for getting priority details from priority autoCompleteTextView in the jira layout.
     * @param autoTextViewComponent is used for getting component details from component autoCompleteTextView in the jira layout.
     * @param autoTextViewFixVersions is used for getting fix versions details from fix versions autoCompleteTextView in the jira layout.
     * @param autoTextViewLabel is used for getting label details from label autoCompleteTextView in the jira layout.
     * @param autoTextViewEpicLink is used for getting epic link details from epic link autoCompleteTextView in the jira layout.
     * @param autoTextViewSprint is used for getting sprint details from epic link autoCompleteTextView in the jira layout.
     * @param autoTextViewEpicName is used for getting epic name details from epic name autoCompleteTextView in the jira layout.
     */
    internal fun gatherJiraSpinnerDetails(
        autoTextViewProject: AutoCompleteTextView,
        autoTextViewIssueType: AutoCompleteTextView,
        autoTextViewReporter: AutoCompleteTextView,
        autoTextViewLinkedIssues: AutoCompleteTextView,
        autoTextViewIssues: AutoCompleteTextView,
        autoTextViewAssignee: AutoCompleteTextView,
        autoTextViewPriority: AutoCompleteTextView,
        autoTextViewComponent: AutoCompleteTextView,
        autoTextViewFixVersions: AutoCompleteTextView,
        autoTextViewLabel: AutoCompleteTextView,
        autoTextViewEpicLink: AutoCompleteTextView,
        autoTextViewSprint: AutoCompleteTextView,
        autoTextViewEpicName: AutoCompleteTextView
    ) {
        project = autoTextViewProject.editableText.toString()
        issueType = autoTextViewIssueType.editableText.toString()
        reporter = autoTextViewReporter.editableText.toString()
        linkedIssue = autoTextViewLinkedIssues.editableText.toString()
        issue = autoTextViewIssues.editableText.toString()
        assignee = autoTextViewAssignee.editableText.toString()
        priority = autoTextViewPriority.editableText.toString()
        if (autoTextViewLabel.editableText.toString().isNotEmpty()) {
            arrayListChoosenLabel.add(autoTextViewLabel.editableText.toString())
        }
        epicLink = autoTextViewEpicLink.editableText.toString()
        component = autoTextViewComponent.editableText.toString()
        fixVersion = autoTextViewFixVersions.editableText.toString()
        sprint = autoTextViewSprint.editableText.toString()
        epicName = autoTextViewEpicName.editableText.toString()
    }

    /**
     * This method is used for getting details of editText in the jira layout.
     * @param editTextSummary is used for getting title details from summary editText in the jira layout.
     * @param editTextDescription is used for getting title details from description editText in the jira layout.
     */
    internal fun gatherJiraEditTextDetails(
        editTextSummary: EditText,
        editTextDescription: EditText
    ) {
        summary = editTextSummary.text.toString()
        description = editTextDescription.text.toString()
    }

    /**
     * This method is used for updating and controlling the queue of background tasks in the jira actions.
     */
    private fun updateFields() {
        queueCounter--
        Log.d("que_counter", queueCounter.toString())
        if (queueCounter == 0) {
            timerTaskQueue.cancel()
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeJiraAutoTextViews(
                    arrayListJiraProjectNames = arrayListProjects,
                    arrayListJiraProjectKeys = arrayListProjectKeys,
                    arrayListJiraIssueTypes = arrayListIssueTypes,
                    arrayListJiraAssignee = arrayListAssignee,
                    arrayListJiraReporterNames = arrayListReporter,
                    arrayListJiraLinkedIssues = arrayListIssueLinkedTypes,
                    arrayListJiraIssues = arrayListIssues,
                    arrayListJiraPriority = arrayListPriorities,
                    arrayListJiraComponent = arrayListComponents,
                    arrayListJiraFixVersions = arrayListFixVersions,
                    arrayListJiraLabel = arrayListLabel,
                    arrayListJiraEpicLink = arrayListEpicLink,
                    arrayListJiraSprint = arrayListSprintName,
                    arrayListJiraEpicName = arrayListEpicName,
                    hashMapJiraBoardList = hashMapBoard
                )
            }
        }
    }

    /**
     * This method is used for default exception handling of jira class.
     * @param e is used for getting reference of exception.
     * @param filePathMedia is used for getting the reference of current media file.
     * @param throwable is used for getting reference of throwable.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun jiraExceptionHandler(
        e: Exception? = null,
        filePathName: File? = null,
        throwable: Throwable? = null
    ) {
//        if(filePathName?.name != "logger_bird_details_old_session.txt"){
//            filePathName?.delete()
//        }
        if (this::timerTaskQueue.isInitialized) {
            timerTaskQueue.cancel()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("jira_error")
        throwable?.printStackTrace()
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.jiraAuthenticationtag,
            throwable = throwable
        )
    }

    /**
     * This method is used for getting reference of current project position in the project autoCompleteTextView in the jira layout.
     * @param projectPosition is used for getting reference of project position.
     */
    internal fun setProjectPosition(projectPosition: Int) {
        this.projectPosition = projectPosition
    }

    /**
     * This method is used for getting reference of current issue type position in the project autoCompleteTextView in the jira layout.
     * @param issueTypePosition is used for getting reference of issue type position.
     */
    internal fun setIssueTypePosition(issueTypePosition: Int) {
        this.issueTypePosition = issueTypePosition
    }

    /**
     * This method is used for getting reference of current issue type position in the project autoCompleteTextView in the jira layout.
     * @param reporterPosition is used for getting reference of reporter position.
     */
    internal fun setReporterPosition(reporterPosition: Int) {
        this.reporterPosition = reporterPosition
    }

    /**
     * This method is used for getting reference of current assignee position in the assignee autoCompleteTextView in the jira layout.
     * @param assigneePosition is used for getting reference of assignee position.
     */
    internal fun setAssigneePosition(assigneePosition: Int) {
        this.assigneePosition = assigneePosition
    }

    /**
     * This method is used for getting reference of current priority position in the priority autoCompleteTextView in the jira layout.
     * @param priorityPosition is used for getting reference of priority position.
     */
    internal fun setPriorityPosition(priorityPosition: Int) {
        this.priorityPosition = priorityPosition
    }

    /**
     * This method is used for getting reference of current component position in the component autoCompleteTextView in the jira layout.
     * @param componentPosition is used for getting reference of component position.
     */
    internal fun setComponentPosition(componentPosition: Int) {
        this.componentPosition = componentPosition
    }

    /**
     * This method is used for getting reference of current fix version position in the fix version autoCompleteTextView in the jira layout.
     * @param fixVersionsPosition is used for getting reference of fix version position.
     */
    internal fun setFixVersionsPosition(fixVersionsPosition: Int) {
        this.fixVersionPosition = fixVersionsPosition
    }

    /**
     * This method is used for getting reference of current linked issue type position in the linked issue type autoCompleteTextView in the jira layout.
     * @param linkedIssueTypePosition is used for getting reference of linked issue type position.
     */
    internal fun setLinkedIssueTypePosition(linkedIssueTypePosition: Int) {
        this.linkedIssueTypePosition = linkedIssueTypePosition
    }

    /**
     * This method is used for getting reference of current sprint position in the sprint autoCompleteTextView in the jira layout.
     * @param sprintPosition is used for getting reference of sprint position.
     */
    internal fun setSprintPosition(sprintPosition: Int) {
        this.sprintPosition = sprintPosition
    }

    /**
     * This method is used for getting reference of current date time in the jira date layout.
     * @param startDate is used for getting reference of start date.
     */
    internal fun setStartDate(startDate: String?) {
        this.startDate = startDate
    }

    /**
     * This method is used for getting reference of current epic name position in the epic name autoCompleteTextView in the jira layout.
     * @param epicNamePosition is used for getting reference of epic name position.
     */
    internal fun setEpicNamePosition(epicNamePosition: Int) {
        this.epicNamePosition = epicNamePosition
    }

    /**
     * This method is used for resetting the values in jira action.
     */
    private fun resetJiraValues() {
        queueCreateTask--
        if (queueCreateTask == 0) {
            LoggerBirdService.loggerBirdService.finishShareLayout("jira")
            timerTaskQueue.cancel()
            summary = ""
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
            epicName = ""
            component = null
            fixVersion = null
            sprintField = null
            startDateField = null
            startDate = null
            epicNameField = null
            epicLinkField = null
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
            epicNamePosition = 0
        }

    }

    /**
     * This method is used for controlling the time of background tasks in the jira actions.
    If tasks will last longer than three minutes then jira layout will be removed.
     */
    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("jira_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 180000)
    }

    /**
     * This method is used for checking summary is not empty.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @return Boolean value.
     */
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
            false
        }
    }

    /**
     * This method is used for checking reporter list contains reporter.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @return Boolean value.
     */
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

    /**
     * This method is used for checking fix version list contains fix version.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @return Boolean value.
     */
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

    /**
     * This method is used for checking component list contains component.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkComponentEmpty(activity: Activity, context: Context): Boolean {
        return if (arrayListComponents.contains(component) || component!!.isEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = context.resources.getString(R.string.jira_component_version_empty)
                )
            }
            false
        }
    }

    /**
     * This method is used for checking epic link list epic link component.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @return Boolean value.
     */
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

    /**
     * This method is used for checking epic name list contains epic name.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkEpicName(activity: Activity, context: Context): Boolean {
        return if (epicName.isNotEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = context.resources.getString(R.string.jira_epic_name_empty)
                )
            }
            false
        }
    }
}
