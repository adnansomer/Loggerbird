package utils

import loggerbird.LoggerBird
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitUserPivotalClient {
    companion object {
       internal fun getPivotalUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    BasicAuthPivotalTrackerInterceptor(
                        LoggerBird.pivotalUserName,
                        LoggerBird.pivotalApiToken
                    )
                )
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
    }
}