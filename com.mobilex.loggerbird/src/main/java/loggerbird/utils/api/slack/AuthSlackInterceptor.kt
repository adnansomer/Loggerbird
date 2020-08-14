package loggerbird.utils.api.slack

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

/**
 * This class is used for loggerbird.authentication interceptor of Slack.
 */
internal class AuthSlackInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().header("Content-Type", "application/x-www-form-urlencoded").header("Authorization", "Bearer ${SlackApi.token}").build()
        return chain.proceed(request)
    }
}