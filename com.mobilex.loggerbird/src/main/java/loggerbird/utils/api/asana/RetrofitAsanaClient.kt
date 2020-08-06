package loggerbird.utils.api.asana

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * This class is used for creating retrofit client for asana.
 */
internal class RetrofitAsanaClient {
    companion object {
        /**
         * This method is used for creating client in order to use in retrofit builder.
         */
       internal fun getAsanaUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    AuthAsanaInterceptor()
                )
                .connectTimeout(20, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
    }
}