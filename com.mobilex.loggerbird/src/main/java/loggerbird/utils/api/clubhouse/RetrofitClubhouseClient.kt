package loggerbird.utils.api.clubhouse

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * This class is used for creating Retrofit Client for Clubhouse
 **/
internal class RetrofitClubhouseClient {
    companion object {
        /**
         * This method is used for creating client in order to use in Retrofit Builder.
         */
        internal fun getClubhouseUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthClubhouseInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
    }
}