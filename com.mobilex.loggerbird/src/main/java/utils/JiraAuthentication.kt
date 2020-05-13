package utils

import android.util.Log
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.JiraRestClientFactory
import com.atlassian.jira.rest.client.api.domain.BasicProject
import com.atlassian.jira.rest.client.api.domain.BasicUser
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.IssueType
import com.atlassian.jira.rest.client.api.domain.input.IssueInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sun.jersey.api.client.Client
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.WebResource
import com.sun.jersey.core.util.Base64
import constants.Constants
import io.atlassian.util.concurrent.Promise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import loggerbird.LoggerBird
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.simple.*;
import org.json.simple.parser.*;
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class JiraAuthentication {
    private val coroutineCallJira: CoroutineScope = CoroutineScope(Dispatchers.IO)
    internal fun callJiraIssue() {
        coroutineCallJira.async {
            try {
//                loginToJira()
                okHttpJiraAuthentication()
//                okHttpJiraAuthentication()
//                jiraAuthentication()
//                val factory: JiraRestClientFactory = AsynchronousJiraRestClientFactory()
//                val jiraServerUri: URI =
//                    URI("https://appcaesars.atlassian.net/secure/RapidBoard.jspa?projectKey=LGB&useStoredSettings=true&rapidView=1")
////                val restClient: JiraRestClient = factory.createWithBasicHttpAuthentication(
////                    jiraServerUri,
////                    "appcaesars@gmail.com",
////                    "N9id4dk3xViS0bcp7olpD37C"
////                )
//                val restClient: JiraRestClient = factory.create(
//                    jiraServerUri,
//                    "appcaesars@gmail.com",
//                    "N9id4dk3xViS0bcp7olpD37C"
//                )
//                Log.d("response", restClient.searchClient.getFilter(1).claim().description)
//                val promise: Promise<Issue> = restClient.issueClient.getIssue("LGB-1")
//                val issue: Issue = promise.claim()
//                Log.d("issue", issue.description!!)
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.jiraAuthenticationtag
                )
            }
        }
    }

    private fun jiraAuthentication() {
        val responseClient: ClientResponse
        val auth = String(Base64.encode("appcaesars@gmail.com" + ":" + "N9id4dk3xViS0bcp7olpD37C"))
        val headerAuthorization = "Authorization"
        val headerAuthorizationValue = "Basic$auth"
        val headerType = "application/json"
        val client: Client = Client.create()
        val webResource: WebResource =
            client.resource("https://appcaesars.atlassian.net/secure/RapidBoard.jspa?projectKey=LGB&useStoredSettings=true&rapidView=1")
        responseClient =
            webResource.header(headerAuthorization, headerAuthorizationValue).type(headerType)
                .accept(headerType).get(
                    ClientResponse::class.java
                )

        Log.d("response", responseClient.status.toString())
    }

    private fun okHttpJiraAuthentication() {
        val MEDIA_TYPE: MediaType = "application/json".toMediaTypeOrNull()!!
        val postData: JSONObject = JSONObject()
        postData.put("username", "appcaesars@gmail.com")
        postData.put("password", "N9id4dk3xViS0bcp7olpD37C")
        val auth = String(Base64.encode("appcaesars@gmail.com" + ":" + "N9id4dk3xViS0bcp7olpD37C"))
        val headerAuthorization = "Authorization"
        val headerAuthorizationValue = "Basic$auth"
        val headerType = "application/json"
        val client: OkHttpClient = OkHttpClient()
        val body = postData.toString().toRequestBody(MEDIA_TYPE)
        val request: Request =
            Request.Builder()
                .post(body)
                .get()
                .url("https://appcaesars.atlassian.net/browse/LGB-1")
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("error")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                Log.d("response", response.body?.string())
                try {
                    val factory: JiraRestClientFactory = AsynchronousJiraRestClientFactory()
                    val jiraServerUri: URI =
                        URI("https://appcaesars.atlassian.net")
                    val restClient: JiraRestClient = factory.createWithBasicHttpAuthentication(
                        jiraServerUri,
                        "appcaesars@gmail.com",
                        "uPPXsUw0FabxeOa5CkDm0BAE"
                    )
                    val issueClient = restClient.issueClient
//                    val issueType = IssueType(null,10004,"bug",false,"Assignment LoggerBird",null)
//                    val basicProject = BasicProject(null,"LGB",10004,"LoggerBird")
                    val issueBuilder = IssueInputBuilder("LGB", 10004,"LOGGERBIRD_3!")
                    issueBuilder.setDescription("LoggerBird_2")
                    val basicUser = BasicUser(null,"Adnan Somer","Adnan Somer")
                    issueBuilder.setAssignee(basicUser)
//                    val issueInput = IssueInputBuilder(basicProject,issueType,"LoggerBird_Assignment").build()
                    val issueCreated = issueClient.createIssue(issueBuilder.build()).claim().key
                    Log.d("issue",issueCreated)
                } catch (e: Exception) {
                    e.printStackTrace()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(exception = e , tag = Constants.jiraAuthenticationtag)
                }

//
//                URI jiraServerUri = new URI("http://localhost:8085/rest/api/2/issue");
//                JiraRestClient restClient = null;
//                final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
//                restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "Test", "12345");
//                final IssueRestClient client = restClient.getIssueClient();
//                IssueInputBuilder issueBuilder = new IssueInputBuilder("PU",1L,"Testing the ISSUES");
//                issueBuilder.setDescription("issue ssss description");
//                IssueInput issueInput = issueBuilder.build();
//                BasicIssue issue = client.createIssue(issueInput).claim();
//                System.out.println("isss "+issue.toString());



//                val promise: Promise<Issue> = restClient.issueClient.getIssue("LGB")
//                val issue: Issue = promise.claim()
//
////                val issue = basicIssue.claim()
//                Log.d("issue",issue.key + "\n" + issue.summary)
            }
        })
    }

//    private fun httpRequestJiraAuthentication() {
//        try {
//            val client = HttpClient.newBuilder().build();
//            val request = HttpRequest.newBuilder()
//                .uri(URI.create("https://something.com"))
//                .build();
//            val response = client.send(request, BodyHandlers.ofString());
//            println(response.body())
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            LoggerBird.callEnqueue()
//            LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
//        }
//    }

    private fun loginToJira() {
        var loginResponse: String = ""
        val url = URL("https://appcaesars.atlassian.net/rest/api/2/issue/createmeta")
        val conn = (url.openConnection() as HttpURLConnection)
        conn.doOutput = true
        conn.requestMethod = "GET"
        conn.setRequestProperty("Content-Type", "application/json")
        val input =
            "{\"username\":\"" + "appcaesars@gmail.com" + "\",\"password\":\"" + "uPPXsUw0FabxeOa5CkDm0BAE" + "\"}"
        val os = conn.outputStream
        os.write(input.toByteArray())
        os.flush()
        if (conn.responseCode == 200) {
            val br = BufferedReader(InputStreamReader((conn.inputStream)))
            while ((br.readLine()) != null) {
                loginResponse += br.readLine()
            }
            Log.d("response_json", parseJiraSession(loginResponse))
        } else {
            Log.d("response", conn.responseCode.toString())
        }

    }

    private fun parseJiraSession(input: String): String {
        val parser = JSONParser()
        val obj: Any = parser.parse(input)
        val jsonObject: JSONObject = (obj as JSONObject)
        val sessionJsonObject: JSONObject = (jsonObject["session"] as JSONObject)
        return (sessionJsonObject["value"] as String)
    }
}