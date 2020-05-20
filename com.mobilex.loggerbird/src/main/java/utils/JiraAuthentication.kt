package utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.JiraRestClientFactory
import com.atlassian.jira.rest.client.api.domain.*
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import io.atlassian.util.concurrent.Promise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird
import models.RecyclerViewJiraModel
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URI


class JiraAuthentication {
    private val coroutineCallJira: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private lateinit var project: String
    private lateinit var issueType: String
    private lateinit var reporter: String
    private lateinit var linkedIssue: String
    private lateinit var assignee: String
    private lateinit var priority: String
    private lateinit var summary: String
    private lateinit var description: String
    private lateinit var arrayListRecyclerViewItems: ArrayList<RecyclerViewJiraModel>
    private val arrayListProjects: ArrayList<String> = ArrayList()
    private val arrayListProjectKeys: ArrayList<String> = ArrayList()
    private val arrayListIssueTypes: ArrayList<String> = ArrayList()
    private val arrayListAssignee: ArrayList<String> = ArrayList()
    private val arrayListIssueLinkedTypes: ArrayList<String> = ArrayList()
    private val arrayListReporter:ArrayList<String> = ArrayList()
    private val arrayListPriorities:ArrayList<String> = ArrayList()
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
                                activity = activity
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
    private fun jiraExceptionHandler(e: Exception, filePathName: File?) {
        filePathName?.delete()
        LoggerBirdService.loggerBirdService.finishShareLayout("jira_error")
        e.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.jiraAuthenticationtag
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


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraTaskCreateIssue(
        filePathMediaName: File? = null,
        restClient: JiraRestClient,
        activity: Activity? = null
    ) {
        val userClient = restClient.userClient
        val searchClient = restClient.searchClient
        val searchPromise: Promise<SearchResult> = searchClient.searchJql("project = DEN")
        val search: SearchResult = searchPromise.claim()
        Log.d("issue", search.issues.count().toString())
        val issueClient = restClient.issueClient
//        val issuePromise = issueClient.getIssue("DEN-5")
//        val issue = issuePromise.claim()
//        Log.d("issue",issue.summary)
//        Log.d("issue",issue.description!!)
//        Log.d("issue",issue.assignee?.displayName)
//                    val issueType = IssueType(null,10004,"bug",false,"Assignment LoggerBird",null)
//                    val basicProject = BasicProject(null,"LGB",10004,"LoggerBird")
//        val userPromise: Promise<User> = userClient.getUser("?accountId=5e3bc6ed3f647d0c99d7fcf2")
//        val user: User = userPromise.claim()
        val issueBuilder = IssueInputBuilder("DEN", 10004, "unhandled_berk!")
        issueBuilder.setDescription("LoggerBird_2")
//        issueBuilder.setAssigneeName("0")
//        val basicUser = BasicUser(
//            null,
//            user.name,
//            user.displayName,
//            user.accountId
//        )
//        issueBuilder.setAssignee(basicUser)
//        issueBuilder.setReporter(basicUser)
//                    issueBuilder.addProperty()
//                    val issueInput = IssueInputBuilder(basicProject,issueType,"LoggerBird_Assignment").build()
//                    val issueCreated = issueClient.createIssue(issueBuilder.build()).claim().key

        val basicIssue = issueClient.createIssue(issueBuilder.build()).claim()
        val issueKey = basicIssue.key
        val issueUri = basicIssue.self
        val issue: Promise<Issue> = restClient.issueClient.getIssue(issueKey)
        if (filePathMediaName != null) {
            val inputStreamMediaFile = FileInputStream(filePathMediaName)
            issueClient.addAttachment(
                issue.get().attachmentsUri,
                inputStreamMediaFile,
                filePathMediaName.absolutePath
            )
            if (filePathMediaName.exists()) {
                filePathMediaName.delete()
            }
        }
        val inputStreamSecessionFile =
            FileInputStream(LoggerBird.filePathSecessionName)
        issueClient.addAttachment(
            issue.get().attachmentsUri,
            inputStreamSecessionFile,
            LoggerBird.filePathSecessionName.absolutePath
        )
        activity?.runOnUiThread {
            LoggerBirdService.loggerBirdService.buttonCancel.performClick()
        }
        LoggerBirdService.loggerBirdService.finishShareLayout("jira")
        Log.d("issue", issueUri.toString())
        Log.d("issue", issueKey.toString())
    }

    private fun jiraTaskGatherDetails(
        restClient: JiraRestClient,
        activity: Activity
    ) {
        try {
            arrayListProjects.clear()
            arrayListIssueTypes.clear()
            arrayListAssignee.clear()
            arrayListIssueLinkedTypes.clear()
            arrayListPriorities.clear()
            arrayListReporter.clear()
            jiraTaskGatherProjectKeys(restClient = restClient)
            jiraTaskGatherIssueTypes(restClient = restClient)
            jiraTaskGatherAssignees(restClient = restClient)
            jiraTaskGatherLinkedIssues(restClient = restClient)
            jiraTaskGatherPriorities(restClient = restClient)
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeJiraSpinner(
                    arrayListProjectNames = arrayListProjects,
                    arrayListIssueTypes = arrayListIssueTypes,
                    arrayListAssignee = arrayListAssignee,
                    arrayListLinkedIssues = arrayListIssueLinkedTypes,
                    arrayListPriority = arrayListPriorities
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
    }

    private fun jiraTaskGatherProjectKeys(restClient: JiraRestClient) {
        arrayListProjectKeys.clear()
        val projectClient = restClient.projectClient
        val projectList = projectClient.allProjects
        projectList.claim().forEach {
            arrayListProjectKeys.add(it.key)
            if (it.name != null) {
                arrayListProjects.add(it.name!!)
            }
        }
    }

    private fun jiraTaskGatherIssueTypes(restClient: JiraRestClient) {
        val metaDataClient = restClient.metadataClient
        val issueTypeList = metaDataClient.issueTypes
        issueTypeList.claim().forEach {
            arrayListIssueTypes.add(it.name)
        }
    }

    private fun jiraTaskGatherAssignees(restClient: JiraRestClient) {
        val projectClient = restClient.projectClient
        projectClient.allProjects.claim().forEach {
            val projectRestClient = restClient.projectClient
            projectRestClient.getProject(it.key).claim().projectRoles.forEach { projectRole ->
                val projectRestRoleClient = restClient.projectRolesRestClient
                projectRestRoleClient.getRole(it.self,it.id).claim().actors.forEach { actor ->
                    arrayListAssignee.add(actor.name)
                }
            }
        }
    }


    private fun jiraTaskGatherLinkedIssues(restClient: JiraRestClient) {
        val metaDataClient = restClient.metadataClient
        val linkedIssuesList = metaDataClient.issueLinkTypes
        linkedIssuesList.claim().forEach {
            arrayListIssueLinkedTypes.add(it.name)
        }
    }
    private fun jiraTaskGatherPriorities(restClient: JiraRestClient){
        val metaDataClient = restClient.metadataClient
        val priorityList = metaDataClient.priorities
        priorityList.claim().forEach {
            arrayListPriorities.add(it.name)
        }
    }
    private fun jiraTaskGatherReporters(restClient: JiraRestClient){
        val metaDataClient = restClient.projectClient
        val reporterList = metaDataClient
//        reporterList.claim().forEach {
//            arrayListIssueLinkedTypes.add(it.name)
//        }
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
        spinnerAssignee: Spinner,
        spinnerPriority: Spinner
    ) {
        project = spinnerProject.selectedItem.toString()
        issueType = spinnerIssueType.selectedItem.toString()
        reporter = spinnerReporter.selectedItem.toString()
        linkedIssue = spinnerLinkedIssues.selectedItem.toString()
        assignee = spinnerAssignee.selectedItem.toString()
        priority = spinnerPriority.selectedItem.toString()
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal suspend fun jiraUnhandledExceptionTask() {
        withContext(coroutineCallJira.coroutineContext) {
            try {
                jiraTaskCreateIssue(restClient = jiraAuthentication())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}