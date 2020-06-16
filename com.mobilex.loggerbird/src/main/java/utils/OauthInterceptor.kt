package utils

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class OauthInterceptor(clientId:String, clientSecret:String):Interceptor {
    private var credentials: String = Credentials.basic(clientId, clientSecret)

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", credentials).header("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}