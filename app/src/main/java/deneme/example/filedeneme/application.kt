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
            .setSlackIntegration(slackApiToken = "523949707746.1211376222932.90281f60d1b678e38ab143ca6c2f45dc02b2cf4a6cea5ce28495a9ba9b3c5724")
            .setPivotalIntegraton(pivotalApiToken = "fb4f04edbdeaeed3758839f75c1939b1")
            .setGithubIntegration(githubUserName = "berkavc", githubPassword = "umbasta1")
            .setAsanaIntegration(asanaApiToken = "1/1182746606250186:defab27f657ce740cf05ba7f5180cc6e")
            .setJiraIntegration(jiraDomainName = "https://appcaesars.atlassian.net",jiraUserName = "appcaesars@gmail.com",jiraApiToken = "uPPXsUw0FabxeOa5CkDm0BAE")
            .setBasecampIntegration(basecampApiToken = "BAhbB0kiAbB7ImNsaWVudF9pZCI6ImMzYzMxZmVhMTFmZDRkNjUxMjRkZmRkNjFkYmE4Y2FhM2UxOGZlYTIiLCJleHBpcmVzX2F0IjoiMjAyMC0wNy0xM1QwNjo1ODoyNVoiLCJ1c2VyX2lkcyI6WzQyMzI4NzkwXSwidmVyc2lvbiI6MSwiYXBpX2RlYWRib2x0IjoiZGQ4NDExNDYyOGQ5OTIyOTEzNGRmNmIzMDY5MmY1YTkifQY6BkVUSXU6CVRpbWUNphkewEDolekJOg1uYW5vX251bWkB/ToNbmFub19kZW5pBjoNc3VibWljcm8iByUwOgl6b25lSSIIVVRDBjsARg==--de534e30ac4000d1d2421b991ec38dea834fa522")
            .setTrelloIntegration(trelloUserName = "appcaesars@gmail.com", trelloPassword = "umbasta1", trelloToken = "23c4a97f599e7db20e36b32d38853210cf2120bbb955a7cc4fe9550a24f8b32c", trelloKey = "4b4185b55f32b9e76fef2effcc9147c8" )
            .build()

        LoggerBird.logInit(
            context = this
        )
    }
}