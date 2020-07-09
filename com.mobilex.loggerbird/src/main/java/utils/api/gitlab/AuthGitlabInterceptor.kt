package utils.api.gitlab

import loggerbird.LoggerBird
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

/**
 * This class is used for authentication interceptor of Gitlab
 */
internal class AuthGitlabInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder()
            .header("Content-Type", "application/json")
            .header("PRIVATE-TOKEN",LoggerBird.gitlabApiToken)
            .build()
        return chain.proceed(request)
    }
}