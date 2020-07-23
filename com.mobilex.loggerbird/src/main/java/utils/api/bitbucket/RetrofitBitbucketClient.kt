package utils.api.bitbucket

import loggerbird.LoggerBird
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * This class is used for creating retrofit client for Bitbucket
 */
internal class RetrofitBitbucketClient {
    companion object {
        /**
         * This method is used for creating client in order to use in retrofit builder.
         */
       internal fun getBitbucketUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    AuthBitbucketInterceptor(
                        LoggerBird.bitbucketUserName,
                        LoggerBird.bitbucketPassword
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