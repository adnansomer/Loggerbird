package utils.api.clubhouse

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
    }
}