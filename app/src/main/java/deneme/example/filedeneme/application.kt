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
            slackApiToken = "523949707746.1169086416502.683b3fca438d557cf9b3ac16930db18b100a5daba822ae45663954326093b358"
        )
    }
}