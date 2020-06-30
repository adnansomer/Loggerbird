package utils

import loggerbird.LoggerBird
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitUserClubhouseClient {
    companion object {
        internal fun getClubhouseUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(BasicAuthClubhouseInterceptor())
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
    }
}