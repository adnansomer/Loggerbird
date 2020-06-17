package utils

import loggerbird.LoggerBird
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitUserGitlabClient {
    companion object {
        internal fun getGitlabUserClient(url:String): Retrofit {
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    BasicAuthGitlabInterceptor(
                        LoggerBird.gitlabDomainName,
                        LoggerBird.gitlabAccessToken
                    )
                )
                .build()
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}