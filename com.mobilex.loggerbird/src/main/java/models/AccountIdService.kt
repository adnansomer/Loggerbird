package models

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import models.github.*
import models.gitlab.GitlabLabelsModel
import models.gitlab.GitlabMilestonesModel
import models.gitlab.GitlabProjectModel
import models.gitlab.GitlabUsersModel
import models.jira.*
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface AccountIdService {
    // jira
    @POST("issue")
    fun createIssue(@Body jsonObject:JsonObject):Call<JsonObject>
    @GET("search?query")
    fun getAccountIdList(): Call<List<JiraUserModel>>
//    @Headers("Content-Type: application/json")
    @PUT("assignee")
    fun setAssignee(@Body jsonObject:JsonObject): Call<List<JiraUserModel>>
    @PUT("?")
    fun setReporter(@Body jsonObject:JsonObject): Call<List<JiraUserModel>>
    @PUT("?")
    fun setSprint(@Body jsonObject:JsonObject): Call<List<JiraSprintModel>>
    @PUT("?")
    fun setStartDate(@Body jsonObject:JsonObject): Call<List<JiraSprintModel>>
    @Multipart
    @POST("attachments")
    fun setAttachments(@Part file:MultipartBody.Part): Call<List<JiraSprintModel>>
    @GET("sprint")
    fun getSprintList(): Call<JsonObject>
    @GET("board")
    fun getBoardList(): Call<JsonObject>
    @GET("search?")
    fun getAttachmentList(@Query("project")  projectKey:String,@Query("fields") attachmentTitle:String): Call<JsonObject>
    @GET("field")
    fun getFieldList(): Call<List<JiraFieldModel>>
    @GET("project")
    fun getProjectList(): Call<List<JiraProjectModel>>
    @GET("issuetype")
    fun getIssueTypes(): Call<List<JiraIssueTypeModel>>
    @GET("priority")
    fun getPriorities(): Call<List<JiraPriorityModel>>
    @GET("search?query")
    fun getIssueList(): Call<JsonObject>
    @GET("?")
    fun getFixCompList(): Call<JsonObject>

    //    @GET("component")
//    fun getComponentList(): Call<List<JiraComponentModel>>
//    @GET("version")
//    fun getVersionList(): Call<List<JiraVersionModel>>
    @GET("label")
    fun getLabelList(): Call<JsonObject>
    @GET("search?jql=issuetype=10000")
    fun getEpicList(): Call<JsonObject>
    @GET("issueLinkType")
    fun getLinkedIssueList(): Call<JsonObject>
//    @GET("component")
//    fun getCompList(): Call<List<JiraProjectmodel>>
//    @GET("versions")
//    fun getVersionsList(): Call<List<JiraProjectmodel>>

    //slack
    @POST("oauth.v2.access")
    fun getSlackAuth():Call<JsonObject>
    @POST("v2/authorize?client_id=1176309019584.1151103028997&client_secret=6147f0bd55a0c777893d07c91f3b16ef&scope=app_mentions:read,channels:join,channels:read,chat:write,files:write,groups:read,groups:write,im:write,incoming-webhook,mpim:read,mpim:write,usergroups:write,users:read,users:write,usergroups:read,users.profile:read,chat:write.public,team:read")
    fun getSlackVerification():Call<JsonElement>
    @GET("?")
    fun getSlackVerificationToken():Call<JsonObject>

    //github
    @POST("issues")
    fun createGithubIssue(@Body jsonObject:JsonObject):Call<JsonObject>
    @GET("repos")
    fun getGithubRepo():Call<List<GithubRepoModel>>
    @GET("projects")
    fun getGithubProjects():Call<List<GithubProjectModel>>
    @GET("assignees")
    fun getGithubAssignees():Call<List<GithubAssigneeModel>>
    @GET("labels")
    fun getGithubLabels():Call<List<GithubLabelsModel>>
    @GET("milestones")
    fun getGithubMileStones():Call<List<GithubMileStoneModel>>
    @GET("pulls")
    fun getGithubPullRequest():Call<List<GithubPullRequestsModel>>
//    @Multipart
    @PUT("contents/LoggerBirdFiles/{file_name}")
    fun setGithubAttachments(@Body jsonObject: JsonObject,@Path("file_name")fileName:String):Call<JsonObject>
    @PATCH("issues/{id}")
    fun setGithubIssue(@Body jsonObject: JsonObject,@Path("id")id:Int):Call<JsonObject>

    //Gitlab
    @POST("issues")
    fun createGitlabIssue(@Body jsonObject: JsonObject): Call<JsonObject>
    @GET("projects/?membership=true")
    fun getGitlabProjects(): Call<List<GitlabProjectModel>>
    @GET("milestones")
    fun getGitlabMilestones(): Call<List<GitlabMilestonesModel>>
    @GET("labels")
    fun getGitlabLabels(): Call<List<GitlabLabelsModel>>
    @GET("users")
    fun getGitlabUsers(): Call<List<GitlabUsersModel>>
    @Multipart
    @POST("uploads")
    fun sendGitlabAttachments(@Part file:MultipartBody.Part):Call<JsonObject>
    @PUT("{iid}")
    fun setGitlabIssue(@Path("iid")iid:String,@Query("description") description:String):Call<JsonObject>
    //query soru isareinden sonra


    //trello
    @GET("boards?")
    fun getTrelloProjects(@Query("key") key:String,@Query("token") token:String):Call<List<TrelloProjectModel>>
    @GET("actions?")
    fun getTrelloBoards(@Query("key") key:String,@Query("token") token:String):Call<JsonArray>
    @POST("cards?")
    fun createTrelloIssue(@Body jsonObject: JsonObject,@Query("key") key:String,@Query("token") token:String,@Query("idList") idList:String):Call<JsonObject>
    @Multipart
    @POST("attachments")
    fun setTrelloAttachments(@Part file:MultipartBody.Part,@Query("key") key:String,@Query("token") token:String): Call<JsonObject>
    @GET("memberships?")
    fun getTrelloMembers(@Query("key") key:String,@Query("token") token:String):Call<JsonArray>
    @GET("{idName}?")
    fun getTrelloMembersName(@Path("idName") idName:String,@Query("key") key:String,@Query("token") token:String ):Call<JsonObject>
    @GET("labels?")
    fun getTrelloLabels(@Query("key") key:String,@Query("token") token:String):Call<JsonArray>
    @POST("idLabels?")
    fun setTrelloLabels(@Body jsonArray: JsonArray,@Query("key") key:String,@Query("token") token:String): Call<JsonObject>
}