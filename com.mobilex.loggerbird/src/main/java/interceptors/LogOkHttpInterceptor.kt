package interceptors

import android.util.Log
import okhttp3.*

class LogOkHttpInterceptor : Interceptor, Authenticator {

    fun myHttpClient(): OkHttpClient {

        val builder = OkHttpClient().newBuilder()
            .addInterceptor(LogOkHttpInterceptor())
            .addNetworkInterceptor(LogOkHttpInterceptor())

        return builder.build()
    }

    override fun intercept(chain: Interceptor.Chain): Response{

        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .build()

        val startTime = System.nanoTime()
        Log.i("", request.url.toString() + " start time :" + startTime)
        Log.i("", "" + request.header("Authorization"))

        val response = chain.proceed(request)
        val endTime = System.nanoTime()
        Log.i("", request.url.toString() + " time taken to process :" + (endTime - startTime))

        return response
    }


    override fun authenticate(route: Route?, response: Response): Request? {

        return null
    }

}

class ErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response = chain.proceed(request)

        when (response.code) {
            400 -> {
                //Bad Request Error Message
            }
            401 -> {
                //UnauthorizedError Message
            }

            403 -> {
                //Forbidden Message
            }

            404 -> {
                //NotFound Message
            }

            405 -> {
                //Bad Method
            }

            407 -> {
                //Authorization Required
            }

            408 -> {
                //Request Time Out
            }


        }

        return response
    }
}