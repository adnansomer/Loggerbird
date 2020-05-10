package utils

import android.util.Log
import com.atlassian.httpclient.api.HttpClient
import com.atlassian.httpclient.api.Response
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.JiraRestClientFactory
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.http.HttpRequest
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class JiraAuthentication {
    private val coroutineCallJira: CoroutineScope = CoroutineScope(Dispatchers.IO)
    internal fun callJiraIssue() {
        coroutineCallJira.async {
            try {
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
        val auth = String(Base64.encode("appcaesars@gmail.com" + ":" + "N9id4dk3xViS0bcp7olpD37C"))
        val headerAuthorization = "Authorization"
        val headerAuthorizationValue = "Basic$auth"
        val client: OkHttpClient = OkHttpClient()
        val request: Request =
            Request.Builder().header(headerAuthorization, headerAuthorizationValue)
                .url("https://appcaesars.atlassian.net/secure/RapidBoard.jspa?projectKey=LGB&useStoredSettings=true&rapidView=1")
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("error")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                val factory: JiraRestClientFactory = AsynchronousJiraRestClientFactory()
                val jiraServerUri: URI =
                    URI("https://appcaesars.atlassian.net/secure/RapidBoard.jspa?projectKey=LGB&useStoredSettings=true&rapidView=1")
                val restClient: JiraRestClient = factory.createWithBasicHttpAuthentication(
                    jiraServerUri,
                    "appcaesars@gmail.com",
                    "umbasta1"
                )
                Log.d("response", restClient.searchClient.getFilter(1).claim().description)
                val promise: Promise<Issue> = restClient.issueClient.getIssue("LGB-1")
                val issue: Issue = promise.claim()
                Log.d("issue", issue.description!!)
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
}