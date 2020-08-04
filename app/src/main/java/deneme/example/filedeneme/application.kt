package deneme.example.filedeneme

import android.app.Application
import android.util.Log
import loggerbird.LoggerBird
import loggerbird.LoggerBirdIntegrationBuilder
import loggerbird.LoggerBirdLogLevel

class application : Application() {
    override fun onCreate() {
        super.onCreate()

        LoggerBird.logInit(
            context = this,
            logLevel = LoggerBirdLogLevel.ALL
        )
        LoggerBirdIntegrationBuilder.Builder()
            .setClubhouseIntegration()
            .setGitlabIntegration()
            .setSlackIntegration()
            .setPivotalIntegraton()
            .setGithubIntegration()
            .setAsanaIntegration()
            .setJiraIntegration()
            .setBasecampIntegration()
            .setBitbucketIntegration()
            .setTrelloIntegration()
            .build()

        LoggerBird.callCpuDetails()
    }
}