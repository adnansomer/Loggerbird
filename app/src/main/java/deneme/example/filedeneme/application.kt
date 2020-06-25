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
            slackApiToken = "523949707746.1211142797476.aac41d7db8c537ba3875eda6d7d9df058051ac4e90eef746d851c62ade6c21ac",
            githubUserName = "berkavc",
            githubPassword = "umbasta1",
            trelloUserName = "appcaesars@gmail.com",
            trelloPassword = "umbasta1",
            trelloKey = "4b4185b55f32b9e76fef2effcc9147c8",
            trelloToken = "23c4a97f599e7db20e36b32d38853210cf2120bbb955a7cc4fe9550a24f8b32c",
            pivotalUserName = "appcaesars@gmail.com",
            pivotalApiToken = "fb4f04edbdeaeed3758839f75c1939b1"
        )
    }
}