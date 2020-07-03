package utils

import loggerbird.LoggerBird
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthClubhouseInterceptor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder()
            .header("Content-Type", "application/json")
            .header("token",LoggerBird.clubhouseApiToken)
            .build()
        return chain.proceed(request)
    }
}