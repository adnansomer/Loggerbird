package models

import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*
import utils.JiraAuthentication

interface AccountIdService {
    @GET("search?query")
    fun getAccountIdList(): Call<List<JiraUserModel>>

//    @Headers("Content-Type: application/json")
    @PUT("assignee")
    fun setAssignee(@Body jsonObject:JsonObject): Call<List<JiraUserModel>>
}