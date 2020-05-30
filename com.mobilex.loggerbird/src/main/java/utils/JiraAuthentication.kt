package utils

import adapter.RecyclerViewJiraAdapter
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.JiraRestClientFactory
import com.atlassian.jira.rest.client.api.domain.*
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.api.domain.input.LinkIssuesInput
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.google.gson.JsonObject
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import io.atlassian.util.concurrent.Promise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird
import models.AccountIdService
import models.JiraUserModel
import models.RecyclerViewModel
import okhttp3.*
import services.LoggerBirdService
import java.io.*
import java.net.URI


class JiraAuthentication {
    private val coroutineCallJira: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private lateinit var project: String
    private lateinit var issueType: String
    private var issueTypePosition: Int = 0
    private var priorityPosition: Int = 0
    private var projectPosition: Int = 0
    private var componentPosition: Int = 0
    private var fixVersionPosition: Int = 0
    private var linkedIssueTypePosition: Int = 0
    private var labelPosition: Int = 0
    private var assigneePosition: Int = 0
    private var reporterPosition: Int = 0
    private lateinit var reporter: String
    private lateinit var linkedIssue: String
    private lateinit var issue: String
    private lateinit var assignee: String
    private lateinit var priority: String
    private var summary: String = ""
    private lateinit var description: String
    private lateinit var label: String
    private lateinit var epicLink: String
    private lateinit var sprint: String
    private lateinit var progressBar: ProgressBar
    private val hashMapComponent: HashMap<String, Iterable<BasicComponent>> = HashMap()
    private val hashMapFixVersions: HashMap<String, Iterable<Version>> = HashMap()
    private val hashMapLinkedIssues: HashMap<String, String> = HashMap()
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
    private val arrayListFields: ArrayList<String> = ArrayList()
    private val arrayListSprint: ArrayList<String> = ArrayList()
    private val arrayListAccountId: ArrayList<String> = ArrayList()
    private val arrayListSelf: ArrayList<String> = ArrayList()
    private val arrayListEmailAdresses: ArrayList<String> = ArrayList()
    private val arrayListAvatarUrls: ArrayList<String> = ArrayList()

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
                .url("https://appcaesars.atlassian.net")
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
    private fun jiraExceptionHandler(
        e: Exception? = null,
        filePathName: File? = null,
        throwable: Throwable? = null
    ) {
        filePathName?.delete()
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
            URI("https://appcaesars.atlassian.net")
        return factory.createWithBasicHttpAuthentication(
            jiraServerUri,
            "appcaesars@gmail.com",
            "uPPXsUw0FabxeOa5CkDm0BAE"
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
                "unhandled" -> jiraUnhandledTask(restClient = restClient)
            }
        } catch (e: Exception) {
            jiraExceptionHandler(e = e, filePathName = filePathMediaName)
        }
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraNormalTask(restClient: JiraRestClient, activity: Activity, context: Context) {
        if (checkSummaryEmpty(activity = activity, context = context)) {
            val issueClient = restClient.issueClient
            val issueBuilder = IssueInputBuilder(
                arrayListProjectKeys[projectPosition],
                arrayListIssueTypesId[issueTypePosition].toLong(),
                summary
            )
            issueBuilder.setPriorityId(arrayListPrioritiesId[priorityPosition].toLong())
            issueBuilder.setDescription(description)
            issueBuilder.setFieldValue("labels", arrayListChoosenLabel)
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
            if (arrayListComponents[componentPosition].isNotEmpty()) {
                issueBuilder.setComponents(hashMapComponent[arrayListComponents[componentPosition]])
            }
            if (arrayListFixVersions[fixVersionPosition].isNotEmpty()) {
                issueBuilder.setFixVersions(hashMapFixVersions[arrayListFixVersions[fixVersionPosition]])
            }
            val basicIssue = issueClient.createIssue(issueBuilder.build()).claim()
            val issueKey = basicIssue.key
//            createdIssueKey  = issueKey
            val issueUri = basicIssue.self
            val issue: Promise<Issue> = restClient.issueClient.getIssue(issueKey)
            val jsonObjectAssignee = JsonObject()
            jsonObjectAssignee.addProperty("accountId", arrayListAccountId[assigneePosition])
            RetrofitUserJiraClient.getJiraUserClient(url = "https://appcaesars.atlassian.net/rest/api/2/issue/$issueKey/")
                .create(AccountIdService::class.java)
                .setAssignee(jsonObject = jsonObjectAssignee)
                .enqueue(object : retrofit2.Callback<List<JiraUserModel>> {
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
                        Log.d("assignee_put_success", response.code().toString())
                        Log.d("assignee_put_success", assignee)
                    }
                })

            val jsonObjectReporter = JsonObject()
            jsonObjectReporter.addProperty("self", arrayListSelf[reporterPosition])
            jsonObjectReporter.addProperty("accountId", arrayListAccountId[reporterPosition])
            jsonObjectReporter.addProperty("emailAddress", arrayListEmailAdresses[reporterPosition])
            RetrofitUserJiraClient.getJiraUserClient(url = "https://appcaesars.atlassian.net/rest/api/2/issue/$issueKey/")
                .create(AccountIdService::class.java)
                .setReporter(jsonObject = jsonObjectReporter)
                .enqueue(object : retrofit2.Callback<List<JiraUserModel>> {
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
                        Log.d("reporter_put_success", response.code().toString())
                        Log.d("reporter_put_success", reporter)
                    }
                })

            if (this.issue.isNotEmpty()) {
                val linkIssueInput = LinkIssuesInput(
                    issueKey,
                    this.issue,
                    hashMapLinkedIssues[arrayListIssueLinkedTypes[linkedIssueTypePosition]]
                )
                issueClient.linkIssue(linkIssueInput)
            }
            if (this.epicLink.isNotEmpty()) {
                val linkIssueInput = LinkIssuesInput(
                    issueKey,
                    this.epicLink,
                    hashMapLinkedIssues[arrayListIssueLinkedTypes[linkedIssueTypePosition]]
                )
                issueClient.linkIssue(linkIssueInput)
            }
            var fileCounter = 0
            do {
                if (RecyclerViewJiraAdapter.ViewHolder.arrayListFilePaths.size > fileCounter) {
                    val file =
                        RecyclerViewJiraAdapter.ViewHolder.arrayListFilePaths[fileCounter].file
                    val inputStreamMediaFile = FileInputStream(file)
                    issueClient.addAttachment(
                        issue.get().attachmentsUri,
                        inputStreamMediaFile,
                        file.absolutePath
                    )
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
            LoggerBirdService.loggerBirdService.finishShareLayout("jira")
            Log.d("issue", issueUri.toString())
            Log.d("issue", issueKey.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraUnhandledTask(restClient: JiraRestClient) {
        val issueClient = restClient.issueClient
        val issueBuilder = IssueInputBuilder(
            "DEN",
            10004,
            "unhandled_exception:"
        )
        issueBuilder.setDescription("An unhandled exception occurred in the application check log details for more information!")
        val inputStreamSecessionFile =
            FileInputStream(LoggerBird.filePathSecessionName)
        val basicIssue = issueClient.createIssue(issueBuilder.build()).claim()
        val issueKey = basicIssue.key
        val issue: Promise<Issue> = restClient.issueClient.getIssue(issueKey)
        issueClient.addAttachment(
            issue.get().attachmentsUri,
            inputStreamSecessionFile,
            LoggerBird.filePathSecessionName.absolutePath
        )
        LoggerBirdService.loggerBirdService.finishShareLayout("jira")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraTaskGatherDetails(
        restClient: JiraRestClient,
        activity: Activity
    ) {
        try {
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
            arrayListSprint.clear()
            hashMapComponent.clear()
            hashMapFixVersions.clear()
            hashMapLinkedIssues.clear()
//            jiraTaskGatherFields(restClient = restClient)

            jiraTaskGatherProjectKeys(restClient = restClient)
            jiraTaskGatherIssueTypes(restClient = restClient)
            jiraTaskGatherAssignees(restClient = restClient)
//            jiraTaskGatherReporters(restClient = restClient)
            jiraTaskGatherLinkedIssues(restClient = restClient)
            jiraTaskGatherIssues(restClient = restClient)
            jiraTaskGatherPriorities(restClient = restClient)
            jiraTaskGatherFixComp(restClient = restClient)
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeJiraSpinner(
                    arrayListProjectNames = arrayListProjects,
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
                    arrayListSprint = arrayListSprint
                )
            }
        } catch (e: Exception) {
            jiraExceptionHandler(e = e)
        }
    }


    private fun jiraTaskGatherProjectKeys(restClient: JiraRestClient) {
        val projectClient = restClient.projectClient
        val projectList = projectClient.allProjects
        projectList.claim().forEach {
            if (it.name != null) {
                arrayListProjects.add(it.name!!)
            }
            arrayListProjectKeys.add(it.key)
        }
    }

    private fun jiraTaskGatherIssueTypes(restClient: JiraRestClient) {
        val metaDataClient = restClient.metadataClient
        val issueTypeList = metaDataClient.issueTypes
        issueTypeList.claim().forEach {
            arrayListIssueTypes.add(it.name)
            arrayListIssueTypesId.add(it.id.toInt())
        }
    }

    private fun jiraTaskGatherAssignees(restClient: JiraRestClient) {

//        https://appcaesars.atlassian.net/rest/api/2/user/search?query

        RetrofitUserJiraClient.getJiraUserClient(url = "https://appcaesars.atlassian.net/rest/api/2/user/")
            .create(AccountIdService::class.java)
            .getAccountIdList().enqueue(object : retrofit2.Callback<List<JiraUserModel>> {
                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                override fun onFailure(call: retrofit2.Call<List<JiraUserModel>>, t: Throwable) {
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
                }
            })

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
    }

    private fun jiraTaskGatherIssues(restClient: JiraRestClient) {
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
                if (issue.getField("Sprint")?.value != null) {
                    arrayListSprint.add(issue.getField("Sprint")?.value.toString())
                }
                arrayListIssues.add(issue.key)
                if (issue.issueType.name == "Epic") {
                    arrayListEpicLink.add(issue.key)
                }
                issue.labels.forEach { label ->
                    arrayListLabel.add(label)
                }
            }
        }

    }

    private fun jiraTaskGatherPriorities(restClient: JiraRestClient) {
        val metaDataClient = restClient.metadataClient
        val priorityList = metaDataClient.priorities
        priorityList.claim().forEach {
            arrayListPriorities.add(it.name)
            if (it.id != null) {
                arrayListPrioritiesId.add(it.id!!.toInt())
            }
        }
    }

    private fun jiraTaskGatherFixComp(restClient: JiraRestClient) {
        val projectClient = restClient.projectClient
//        arrayListComponents.add("")
//        arrayListFixVersions.add("")
        projectClient.allProjects.claim().forEach {
            projectClient.getProject(it.key).claim().components.forEach { component ->
                arrayListComponents.add(component.name)
                hashMapComponent[component.name] =
                    projectClient.getProject(it.key).claim().components
            }
            projectClient.getProject(it.key).claim().versions.forEach { version ->
                arrayListFixVersions.add(version.name)
                hashMapFixVersions[version.name] = projectClient.getProject(it.key).claim().versions
            }
        }
    }

    private fun jiraTaskGatherFields(restClient: JiraRestClient) {
        val metaDataClient = restClient.metadataClient
        metaDataClient.fields.claim().forEach {
            arrayListFields.add(it.id)
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

    internal fun getArrayListSprint(): ArrayList<String> {
        return arrayListSprint
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

    internal fun checkSummaryEmpty(activity: Activity, context: Context): Boolean {
        return if (summary.isNotEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                Toast.makeText(
                    context,
                    R.string.jira_summary_empty,
                    Toast.LENGTH_SHORT
                ).show()
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


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal suspend fun jiraUnhandledExceptionTask(context: Context, activity: Activity) {
        withContext(coroutineCallJira.coroutineContext) {
            try {
                jiraTaskCreateIssue(
                    restClient = jiraAuthentication(),
                    context = context,
                    createMethod = "unhandled",
                    activity = activity
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
