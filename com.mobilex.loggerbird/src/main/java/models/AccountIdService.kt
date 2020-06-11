package models

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface AccountIdService {
    @POST("issue")
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
}