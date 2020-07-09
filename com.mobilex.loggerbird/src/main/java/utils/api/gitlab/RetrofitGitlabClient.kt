package utils.api.gitlab

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * This class is used for creating Retrofit Client for Clubhouse
 **/
internal class RetrofitGitlabClient {
    companion object {
        /**
         * This method is used for creating client in order to use in Retrofit Builder.
         */
        internal fun getGitlabUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthGitlabInterceptor())
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}