package deneme.example.filedeneme

import android.content.Context
import android.os.AsyncTask
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import okhttp3.Response


interface ApiServiceInterface {

    @POST("api.php")
    fun hitCountCheck(
        @Query("action") action: String,
        @Query("format") format: String,
        @Query("list") list: String,
        @Query("srsearch") srsearch: String
    ):
            Call<RetroFitModel.Result>


    companion object {


        fun create(context: Context): ApiServiceInterface {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create()
                )
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .baseUrl("https://api.plos.org")
                .build()

//            LogDeneme.saveAllDetails(fileName ="berk_deneme",retrofit =retrofit,context = context)

            return retrofit.create(ApiServiceInterface::class.java)
        }

        fun createObject(): Retrofit {
            val retrofit = Retrofit.Builder()
//                .addCallAdapterFactory(
//                    RxJava2CallAdapterFactory.create()
//                )
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .baseUrl("https://api.plos.org")
                .client(deneme.example.filedeneme.ApiServiceInterface.Companion.client)
                .build()

            return retrofit
        }

        fun httpClient(request: Request): Response? {

            val response = networkRequestTask(
                client,
                request
            ).execute().get()
            //  val response= client.newCall(request).execute()

            return response
        }

        var client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

    }
}

class networkRequestTask(val client: OkHttpClient, val request: Request) :
    AsyncTask<Void, Void, Response?>() {

    override fun doInBackground(vararg params: Void?): Response? {
        // ...

//        ApiServiceInterface.httpClient(networkRequest)
        val response = client.newCall(request).execute()

//        val jsonObject: JSONObject?= JSONObject(response.body!!.string())

//        jsonObject.len
//        jsonArray.add(jsonObject?.names().getString())

//        Log.d("response_message_async",response?.body?.string())

        return response
    }

    override fun onPreExecute() {
        super.onPreExecute()
        // ...
    }

    override fun onPostExecute(result: Response?) {
        super.onPostExecute(result)

        // ...
    }
}