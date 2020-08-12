package loggerbird.utils.api.basecamp

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * This class is used for creating retrofit client for basecamp.
 */
internal class RetrofitBasecampClient {
    companion object {
        /**
         * This method is used for creating client in order to use in retrofit builder.
         */
       internal fun getBasecampUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    AuthBasecampInterceptor()
                )
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