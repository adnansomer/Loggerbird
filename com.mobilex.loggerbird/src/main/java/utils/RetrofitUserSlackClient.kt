package utils

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import utils.OauthInterceptor
import utils.SlackAuthentication

class RetrofitUserSlackClient {
    companion object {
       internal fun getSlackUserClient(url:String): Retrofit {
           val gson = GsonBuilder().setLenient().create()
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    OauthInterceptor(
                        SlackAuthentication.CLIENT_ID,
                        SlackAuthentication.CLIENT_SECRET
                    )
                )
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
        }
    }
}