package utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.JiraRestClientFactory
import com.atlassian.jira.rest.client.api.domain.BasicUser
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import io.atlassian.util.concurrent.Promise
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URI


class JiraAuthentication() {
    private val coroutineCallJira: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun callJiraIssue(filePathName: File? = null, context: Context, activity: Activity) {
        coroutineCallJira.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    okHttpJiraAuthentication(
                        filePathMediaName = filePathName,
                        context = context,
                        activity = activity
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
        activity: Activity
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
                        jiraTask(filePathMediaName = filePathMediaName)
                        LoggerBirdService.loggerBirdService.finishShareLayout("jira")
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraTask(filePathMediaName: File? = null) {
        val factory: JiraRestClientFactory = AsynchronousJiraRestClientFactory()
        val jiraServerUri =
            URI("https://appcaesars.atlassian.net")
        val restClient: JiraRestClient = factory.createWithBasicHttpAuthentication(
            jiraServerUri,
            "appcaesars@gmail.com",
            "uPPXsUw0FabxeOa5CkDm0BAE"
        )
        val issueClient = restClient.issueClient
//                    val issueType = IssueType(null,10004,"bug",false,"Assignment LoggerBird",null)
//                    val basicProject = BasicProject(null,"LGB",10004,"LoggerBird")
        val issueBuilder = IssueInputBuilder("LGB", 10004, "unhandled!")
        issueBuilder.setDescription("LoggerBird_2")
        val basicUser = BasicUser(
            URI("https://appcaesars.atlassian.net/rest/api/latest/issue/10045"),
            "Adnan",
            "Adnan"
        )
        issueBuilder.setAssignee(basicUser)
//                    issueBuilder.setReporter(basicUser)
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
//                        val issueInput:IssueInput = IssueInput.createWithFields(FieldInput(IssueFieldId.ASSIGNEE_FIELD,ComplexIssueInputFieldValue.with("Adnan","Adnan")))
//                        issueClient.updateIssue(issueUri,issueInput).claim()
        }
        val inputStreamSecessionFile =
            FileInputStream(LoggerBird.filePathSecessionName)
        issueClient.addAttachment(
            issue.get().attachmentsUri,
            inputStreamSecessionFile,
            LoggerBird.filePathSecessionName.absolutePath
        )
        Log.d("issue", issueUri.toString())
        Log.d("issue", issueKey.toString())
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal suspend fun jiraUnhandledExceptionTask() {
        withContext(coroutineCallJira.coroutineContext) {
            jiraTask()
        }
    }
}