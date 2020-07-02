package utils

import loggerbird.LoggerBird
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthAsanaInterceptor() : Interceptor {
//    private var credentials: String = apiToken

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder()
            .header("Accept", "application/json")
            .header("Authorization", "Bearer ${LoggerBird.asanaApiToken}")
            .build()
        return chain.proceed(request)
    }
}