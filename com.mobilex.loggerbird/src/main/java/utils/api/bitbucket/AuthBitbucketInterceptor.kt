package utils.api.bitbucket

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

/**
 * This class is used for authentication interceptor of Bitbucket
 */
internal class AuthBitbucketInterceptor(username:String, password:String):Interceptor {
    private var credentials: String = Credentials.basic(username, password)

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", credentials).header("Content-Type", "application/json").build()
        return chain.proceed(request)
    }
}