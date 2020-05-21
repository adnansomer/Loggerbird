package models

import retrofit2.Call
import retrofit2.http.GET

interface AccountIdService {
    @GET("search?query")
    fun getAccountIdList(): Call<List<JiraUserModel>>
}