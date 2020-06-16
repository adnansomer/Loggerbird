package models

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*
import java.io.InputStream

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
}