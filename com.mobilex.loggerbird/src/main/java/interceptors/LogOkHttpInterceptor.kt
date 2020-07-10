package interceptors

import loggerbird.LoggerBird
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * This interceptor class is used for getting actual time between sending request and received response.
 * @return response of request
 */
internal class LogOkHttpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val requestTime = System.nanoTime()
        LoggerBird.stringBuilderInterceptor.append(
            String.format(
                "Sending request %s on %s%n%s",
                request.url,
                chain.connection(),
                request.headers
            ) + "\n"
        )
        val responseTime = System.nanoTime()
        LoggerBird.stringBuilderInterceptor.append(
            String.format(
                "Received response for %s in %.1fms%n%s",
                response.request.url,
                (responseTime - requestTime) / 1e6,
                response.headers
            ) + "\n"
        )
        return response
    }
}

/**
 * This interceptor class is used for catching response errors and gives message of corresponding error code.
 * @return response of request
 */
internal class LogOkHttpErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response = chain.proceed(request)
        LoggerBird.stringBuilderInterceptor.append("response code:" + response.code + "\n")
        LoggerBird.stringBuilderInterceptor.append("response message:")
        when (response.code) {
            400 -> {
                LoggerBird.stringBuilderInterceptor.append(
                    "Bad Request Error Message" + "\n"
                )
            }
            401 -> {
                LoggerBird.stringBuilderInterceptor.append(
                    "UnauthorizedError" + "\n"
                )
            }

            403 -> {
                LoggerBird.stringBuilderInterceptor.append(
                    "Forbidden Message" + "\n"
                )
            }

            404 -> {
                LoggerBird.stringBuilderInterceptor.append(
                    "NotFound Message" + "\n"
                )
            }

            405 -> {
                LoggerBird.stringBuilderInterceptor.append(
                    "Bad Method" + "\n"
                )
            }

            407 -> {
                LoggerBird.stringBuilderInterceptor.append(
                    "Authorization Required" + "\n"
                )
            }

            408 -> {
                LoggerBird.stringBuilderInterceptor.append(
                    "Request Time Out" + "\n"
                )
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
 * @return response of cache interceptor
 */
internal class LogOkHttpCacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(10, TimeUnit.DAYS)
            .build()
        LoggerBird.stringBuilderInterceptor.append(
            response.newBuilder().header(
                "Cache-Control",
                cacheControl.toString()
            ).build().toString() + "\n"
        )
        return response.newBuilder().header("Cache-Control", cacheControl.toString()).build()
    }
}

/**
 * This interceptor class is used to intercept the actual request and to supply your API key in REST API calls with a custom header.
 * @return response of chain
 */
internal class LogOkHttpAuthenticationInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("Authorization", "AuthToken")
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
