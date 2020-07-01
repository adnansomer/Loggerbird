package utils

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class BasicAuthBasecampInterceptor() : Interceptor {
//    private var credentials: String = apiToken

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}