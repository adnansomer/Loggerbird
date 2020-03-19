package interceptors

import android.util.Log
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

    /**
     * This interceptor class is used for getting actual time between sending request and recieved response.
     * Variables:
     * @var requestTime obtains the system time after doing request.
     * @var responseTime obtains the system time after getting response to calculate actual time between request.
     */
     class LogOkHttpInterceptor : Interceptor{

        override fun intercept(chain: Interceptor.Chain): Response {

            val request = chain.request()
            val requestTime = System.nanoTime()
            Log.d("OkHttp", String.format("Sending request %s on %s%n%s", request.url, chain.connection(), request.headers))

            val response = chain.proceed(request)
            val responseTime = System.nanoTime()
            Log.d("OkHttp", String.format("Received response for %s in %.1fms%n%s", response.request.url, (responseTime - requestTime) / 1e6, response.headers))

        return response

        }

     }

    /**
     * This interceptor class is used for catching response errors and gives message of corresponding error code.
     * Variables:
     * @var request gets request.
     * @var response gets response returned from server.
     */
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

    /**
     * This interceptor class caches HTTP and HTTPS responses to the filesystem so they may be reused, saving time and bandwidth.
     * This interceptor is required when if the Cache-Control is not enabled from the server case.With using this interceptor,
     * we still can cache the response from OkHttp Client using Interceptor.In this case we are using addNetworkInterceptor function,
     * This is because in this case, the operation is happening at the network layer.
     * Variables:
     * @var response
     * @var cacheControl
     */
     class LogOkHttpCacheInterceptor : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {

            val response: Response = chain.proceed(chain.request())
            val cacheControl = CacheControl.Builder()
                .maxAge(10, TimeUnit.DAYS)
                .build()

            return response.newBuilder().header("Cache-Control", cacheControl.toString()).build()

            }
    }

    /**
     * This interceptor class is used to intercept the actual request and to supply your API key in REST API calls with a custom header.
     * Variables:
     * @var orginalRequest is a variable that is created request with using interceptor chain.
     * @var request returns a builded request with having header name and value to intercept request and token.
     */
     class LogOkHttpAuthenticationInterceptor : Interceptor {

            override fun intercept(chain: Interceptor.Chain): Response {

                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("Authorization", "AuthToken")
                val request = requestBuilder.build()

                return chain.proceed(request)
            }
    }