package deneme.example.filedeneme

import android.app.Application
import android.util.Log
import loggerbird.LoggerBird

class application : Application() {
    override fun onCreate() {
        super.onCreate()

        LoggerBird.LoggerBirdIntegration.Builder()
            .setClubhouseIntegration(clubhouseApiToken = "5ef8dbb1-aad1-4d9d-8ea0-1bfd13826aff")
            .setGitlabIntegration(gitlabApiToken = "wLD4tf4jyKRmCM27S27d")
            .setSlackIntegration(slackApiToken = "523949707746.1257384981893.00e598163567a90678d9730951c72fbdd33d4b7fbd326c64281163c2e3e31c67")
            .setPivotalIntegraton(pivotalApiToken = "fb4f04edbdeaeed3758839f75c1939b1")
            .setGithubIntegration(githubUserName = "berkavc", githubPassword = "umbasta1")
            .setAsanaIntegration(asanaApiToken = "1/1182746606250186:defab27f657ce740cf05ba7f5180cc6e")
            .setJiraIntegration(jiraDomainName = "https://appcaesars.atlassian.net",jiraUserName = "appcaesars@gmail.com",jiraApiToken = "uPPXsUw0FabxeOa5CkDm0BAE")
            .setBasecampIntegration(basecampApiToken = "BAhbB0kiAbl7ImNsaWVudF9pZCI6IjAyMDM2OGMwNDM4YzQ1ZjIyNjQzYmY5MTM2MDE5MjEzZTQzNGFiY2UiLCJleHBpcmVzX2F0IjoiMjAyMC0wNy0yN1QxMTo0NTo1MloiLCJ1c2VyX2lkcyI6WzQyMzI4NzkyLDQyNDI2NDU3XSwidmVyc2lvbiI6MSwiYXBpX2RlYWRib2x0IjoiODFkYTljZjlkYzljNDA0OThkZjhkOTRhODBmNzg4ZjYifQY6BkVUSXU6CVRpbWUNaxsewMIiQbcJOg1uYW5vX251bWkC3QI6DW5hbm9fZGVuaQY6DXN1Ym1pY3JvIgdzMDoJem9uZUkiCFVUQwY7AEY=--ed8edece558992d6423ab6e85838ce04800eabe5")
            .setBitbucketIntegration(bitbucketUserName = "appcaesars",bitbucketPassword = "umbasta1")
            .setTrelloIntegration(trelloUserName = "appcaesars@gmail.com", trelloPassword = "umbasta1", trelloToken = "23c4a97f599e7db20e36b32d38853210cf2120bbb955a7cc4fe9550a24f8b32c", trelloKey = "4b4185b55f32b9e76fef2effcc9147c8" )
            .build()

        LoggerBird.logInit(
            context = this,
            logLevel = LoggerBird.LogLevel.ALL
        )

        LoggerBird.callCpuDetails()
    }
}