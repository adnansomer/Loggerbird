package interceptors

import android.util.Log
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

    class LogOkHttpInterceptor : Interceptor{

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {

            val request = chain.request()

            val t1 = System.nanoTime()
            Log.d("OkHttp", String.format("Sending request %s on %s%n%s", request.url, chain.connection(), request.headers))

            val response = chain.proceed(request)
            val t2 = System.nanoTime()
            Log.d("OkHttp", String.format("Received response for %s in %.1fms%n%s", response.request.url, (t2 - t1) / 1e6, response.headers))

        return response
        }

    }

    class LogOkHttpErrorInterceptor : Interceptor {

            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request = chain.request()
                val response = chain.proceed(request)

                when (response.code) {
                    400 -> {
                        Log.d("HTTP Interceptor Error", "Bad Request Error Message")
                    }
                    401 -> {
                        Log.d("HTTP Interceptor Error", "UnauthorizedError")
                    }

                    403 -> {
                        Log.d("HTTP Interceptor Error", "Forbidden Message")
                    }

                    404 -> {
                        Log.d("HTTP Interceptor Error", "NotFound Message")
                    }

                    405 -> {
                        Log.d("HTTP Interceptor Error", "Bad Method")
                    }

                    407 -> {
                        Log.d("HTTP Interceptor Error", "Authorization Required")
                    }

                    408 -> {
                        Log.d("HTTP Interceptor Error", "Request Time Out")
                    }

                }

                return response
            }

    }

    class LogOkHttpCacheInterceptor : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {

            val response: Response = chain.proceed(chain.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(10, TimeUnit.DAYS)
                .build()

            return response.newBuilder().header("Cache-Control", cacheControl.toString()).build()

            }
    }

    class LogOkHttpAuthTokenInterceptor : Interceptor {

            override fun intercept(chain: Interceptor.Chain): Response {

                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("Authorization", "AuthToken")
                val request = requestBuilder.build()

                return chain.proceed(request)
            }
    }

    /**
    * Interceptor used to intercept the actual request and
    * to supply your API Key in REST API calls via a custom header.
    */
    class AuthenticationInterceptor : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {

            val newRequest = chain.request().newBuilder()
            .addHeader("X-CMC_PRO_API_KEY", "CMC_PRO_API_KEY")
            .build()

        return chain.proceed(newRequest)
    }
}