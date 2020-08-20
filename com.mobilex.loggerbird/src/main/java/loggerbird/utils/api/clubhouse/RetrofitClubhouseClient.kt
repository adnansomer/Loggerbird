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
                .connectTimeout(120, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
    }
}