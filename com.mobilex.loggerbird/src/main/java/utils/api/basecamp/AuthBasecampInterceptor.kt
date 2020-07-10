package utils.api.basecamp

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

/**
 * This class is used for authentication interceptor of Basecamp
 */
internal class AuthBasecampInterceptor() : Interceptor {
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