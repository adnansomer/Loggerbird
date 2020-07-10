package utils.api.pivotal

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

/**
 * This class is used for authentication interceptor of Pivotal
 */
internal class AuthPivotalTrackerInterceptor(apiToken:String) : Interceptor {
    private var credentials: String = apiToken

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().header("X-TrackerToken", credentials)
            .header("Content-Type", "application/json").build()
        return chain.proceed(request)
    }
}