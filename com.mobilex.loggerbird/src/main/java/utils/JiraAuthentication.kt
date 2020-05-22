package utils

import adapter.RecyclerViewJiraAdapter
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
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
import models.RecyclerViewJiraModel
import okhttp3.*
import org.joda.time.DateTime
import retrofit2.Retrofit
import services.LoggerBirdService
import java.io.*
import java.net.URI
import java.time.LocalDate


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
    private var linkedIssueTypePosition:Int = 0
    private lateinit var reporter: String
    private lateinit var linkedIssue: String
    private lateinit var issue: String
    private lateinit var assignee: String
    private lateinit var priority: String
    private var summary: String = ""
    private lateinit var description: String
    private val hashMapComponent: HashMap<String, Iterable<BasicComponent>> = HashMap()
    private val hashMapFixVersions: HashMap<String, Iterable<Version>> = HashMap()
    private val hashMapLinkedIssues:HashMap<String,String> = HashMap()
    private lateinit var arrayListRecyclerViewItems: ArrayList<RecyclerViewJiraModel>
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
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun callJiraIssue(
        filePathName: File? = null,
        context: Context,
        activity: Activity,
        jiraTask: String
    ) {
        coroutineCallJira.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    okHttpJiraAuthentication(
                        filePathMediaName = filePathName,
                        context = context,
                        activity = activity,
                        jiraTask = jiraTask
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
        jiraTask: String
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
                                context = context
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
        activity: Activity? = null,
        context: Context
    ) {
        try {
            if (activity != null) {
                jiraNormalTask(restClient = restClient, context = context, activity = activity)
            } else {
                jiraUnhandledTask(restClient = restClient, context = context)
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
//            issueBuilder.setReporterName(reporter)
//            issueBuilder.setAssigneeName("5eb3efa5ad226b0ba423144a")
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
            issueBuilder.setComponents(hashMapComponent[arrayListComponents[componentPosition]])
            issueBuilder.setFixVersions(hashMapFixVersions[arrayListFixVersions[fixVersionPosition]])
            val basicIssue = issueClient.createIssue(issueBuilder.build()).claim()
            val issueKey = basicIssue.key
            val issueUri = basicIssue.self
            val issue: Promise<Issue> = restClient.issueClient.getIssue(issueKey)
            val linkIssueInput = LinkIssuesInput(issueKey, this.issue, hashMapLinkedIssues[arrayListIssueLinkedTypes[linkedIssueTypePosition]])
            issueClient.linkIssue(linkIssueInput)
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
                    if (file.exists()) {
                        file.delete()
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
                LoggerBirdService.loggerBirdService.buttonCancel.performClick()
            }
            LoggerBirdService.loggerBirdService.finishShareLayout("jira")
            Log.d("issue", issueUri.toString())
            Log.d("issue", issueKey.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraUnhandledTask(restClient: JiraRestClient, context: Context) {
        val issueClient = restClient.issueClient
        val issueBuilder = IssueInputBuilder(
            "DEN",
            10004,
            context.resources.getString(R.string.jira_summary_unhandled_exception)
        )
        issueBuilder.setDescription(context.resources.getString(R.string.jira_description_unhandled_exception))
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
            arrayListIssueLinkedTypes.clear()
            arrayListIssues.clear()
            arrayListPriorities.clear()
            arrayListPrioritiesId.clear()
            arrayListReporter.clear()
            arrayListComponents.clear()
            arrayListFixVersions.clear()
            jiraTaskGatherProjectKeys(restClient = restClient)
            jiraTaskGatherIssueTypes(restClient = restClient)
            jiraTaskGatherAssignees(restClient = restClient)
            jiraTaskGatherReporters(restClient = restClient)
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
                    arrayListFixVersions = arrayListFixVersions
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
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
//        RetrofitUserJiraClient.getJiraUserClient().create(AccountIdService::class.java)
//            .getAccountIdList().enqueue(object : retrofit2.Callback<List<JiraUserModel>> {
//                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
//                override fun onFailure(call: retrofit2.Call<List<JiraUserModel>>, t: Throwable) {
//                    jiraExceptionHandler(throwable = t)
//                }
//
//                override fun onResponse(
//                    call: retrofit2.Call<List<JiraUserModel>>,
//                    response: retrofit2.Response<List<JiraUserModel>>
//                ) {
//                    var counter = 0
//                    do{
//                        Log.d("response_retrofit", response.raw().body?.toString())
//                        counter++
//                    }while (response.body()!!.iterator().hasNext())
//
//                    val accountIdList = response.body()?.get(0)
//                    var accountListCounter = 0
//                    val userClient = restClient.userClient
//                    if (accountIdList != null) {
//                        do {
//                            if (accountIdList.size > accountListCounter) {
//                                val user =
//                                    userClient.getUser(URI("https://appcaesars.atlassian.net/rest/api/2/user?accountId=" + accountIdList[accountListCounter]))
//                                        .claim()
//                                arrayListAssignee.add(user.displayName)
//                            } else {
//                                break
//                            }
//                            accountListCounter++
//                        } while (accountIdList.iterator().hasNext())
//                    }
//                }
//            })

        val searchClient = restClient.searchClient
        searchClient.searchJql("project = LGB").claim().issues.forEach {
            if (it.assignee != null) {
                if (!arrayListAssignee.contains(it.assignee!!.displayName)) {
                    arrayListAssignee.add(it.assignee!!.displayName)
                }
            }

            if (it.reporter != null) {
                if (!arrayListAssignee.contains(it.reporter!!.displayName)) {
                    arrayListAssignee.add(it.reporter!!.displayName)
                }
            }

        }
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
        projectClient.allProjects.claim().forEach {
            searchClient.searchJql("project=" + it.key).claim().issues.forEach {
                arrayListIssues.add(it.key)
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

    private fun jiraTaskGatherReporters(restClient: JiraRestClient) {
        arrayListReporter.addAll(arrayListAssignee)
    }

    internal fun getArrayListProjects(): ArrayList<String> {
        return arrayListProjects
    }

    internal fun getArrayListIssueTypes(): ArrayList<String> {
        return arrayListIssueTypes
    }

    internal fun gatherJiraSpinnerDetails(
        spinnerProject: Spinner,
        spinnerIssueType: Spinner,
        spinnerReporter: Spinner,
        spinnerLinkedIssues: Spinner,
        spinnerIssues: Spinner,
        spinnerAssignee: Spinner,
        spinnerPriority: Spinner,
        spinnerComponent: Spinner,
        spinnerFixVersions: Spinner
    ) {
        project = spinnerProject.selectedItem.toString()
        projectPosition = spinnerProject.selectedItemPosition
        issueType = spinnerIssueType.selectedItem.toString()
        issueTypePosition = spinnerIssueType.selectedItemPosition
        reporter = spinnerReporter.selectedItem.toString()
        linkedIssue = spinnerLinkedIssues.selectedItem.toString()
        issue = spinnerIssues.selectedItem.toString()
        assignee = spinnerAssignee.selectedItem.toString()
        priority = spinnerPriority.selectedItem.toString()
        priorityPosition = spinnerPriority.selectedItemPosition
        componentPosition = spinnerComponent.selectedItemPosition
        fixVersionPosition = spinnerFixVersions.selectedItemPosition
        linkedIssueTypePosition = spinnerLinkedIssues.selectedItemPosition
    }

    internal fun gatherJiraEditTextDetails(
        editTextSummary: EditText,
        editTextDescription: EditText
    ) {
        summary = editTextSummary.text.toString()
        description = editTextDescription.text.toString()
    }

    internal fun gatherJiraRecyclerViewDetails(arrayListRecyclerViewItems: ArrayList<RecyclerViewJiraModel>) {
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


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal suspend fun jiraUnhandledExceptionTask(context: Context) {
        withContext(coroutineCallJira.coroutineContext) {
            try {
                jiraTaskCreateIssue(restClient = jiraAuthentication(), context = context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}