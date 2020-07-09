package utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * This class is used for creating Retrofit Client for Clubhouse
 **/
class RetrofitUserGitlabClient {
    companion object {
        /**
         * This method is used for creating client in order to use in Retrofit Builder.
         */
        internal fun getGitlabUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(BasicAuthGitlabInterceptor())
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}