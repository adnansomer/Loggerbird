package utils
import android.util.Log
import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.JiraRestClientFactory
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import io.atlassian.util.concurrent.Promise
import java.net.URI

class JiraAuthentication {
    internal fun callJiraIssue(){
        val factory :JiraRestClientFactory = AsynchronousJiraRestClientFactory()
        val jiraServerUri:URI= URI("https://mobilex.atlassian.net/secure/Dashboard.jspa")
        val restClient:JiraRestClient = factory.createWithBasicHttpAuthentication(jiraServerUri,"berk.avcioglu@mobilex.com.tr","umbasta1")
        val promise : Promise<Issue> = restClient.issueClient.getIssue("LBG-30")
        val issue:Issue = promise.claim()
        Log.d("issue",issue.description!!)
    }
}