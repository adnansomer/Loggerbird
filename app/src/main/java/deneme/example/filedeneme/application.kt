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
            slackApiToken = "1176309019584.1186220134178.cc8053a267f150b8c7193f6b6488de5b872a692b31cd750b85f3e86f021ad1c6",
            gitlabDomainName = "https://gitlab.com/adnansomer",
            gitlabAccessToken = "wLD4tf4jyKRmCM27S27d"
        )
    }
}