package utils

import com.atlassian.httpclient.api.HttpClient
import com.atlassian.jira.rest.client.api.*
import java.net.URI

class JiraFactory :JiraRestClientFactory,JiraRestClient{
    override fun create(
        serverUri: URI?,
        authenticationHandler: AuthenticationHandler?
    ): JiraRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun create(serverUri: URI?, httpClient: HttpClient?): JiraRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createWithAuthenticationHandler(
        serverUri: URI?,
        authenticationHandler: AuthenticationHandler?
    ): JiraRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createWithBasicHttpAuthentication(
        serverUri: URI?,
        username: String?,
        password: String?
    ): JiraRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMyPermissionsRestClient(): MyPermissionsRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProjectRolesRestClient(): ProjectRolesRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGroupClient(): GroupRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMetadataClient(): MetadataRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSessionClient(): SessionRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getComponentClient(): ComponentRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSearchClient(): SearchRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAuditRestClient(): AuditRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUserClient(): UserRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIssueClient(): IssueRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVersionRestClient(): VersionRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProjectClient(): ProjectRestClient {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}