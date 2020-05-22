package utils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitUserJiraClient {
    companion object {
        fun getJiraUserClient(): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://appcaesars.atlassian.net/rest/api/2/user/")
                .addConverterFactory(GsonConverterFactory.create()).build()
        }
    }
}