package utils

import loggerbird.LoggerBird
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitUserTrelloClient {
    companion object {
       internal fun getTrelloUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    BasicAuthTrelloInterceptor(
                        LoggerBird.trelloUserName,
                        LoggerBird.trelloPassword
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