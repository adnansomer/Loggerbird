package deneme.example.filedeneme

import android.app.Application
import loggerbird.LoggerBird

class application : Application() {
    override fun onCreate() {
        super.onCreate()
        LoggerBird.logInit(
            context = this,
            jiraDomainName = "https://appcaesars.atlassian.net",
            jiraUserName = "appcaesars@gmail.com",
            jiraApiToken = "uPPXsUw0FabxeOa5CkDm0BAE",
            slackApiToken = "523949707746.1185267677605.a71d59aa1ecfd1821e98f62901ad2e2696ddf3e6949e4f9dbf415270edcc5ebc",
            githubUserName = "berkavc",
            githubPassword = "umbasta1"
        )
    }
}