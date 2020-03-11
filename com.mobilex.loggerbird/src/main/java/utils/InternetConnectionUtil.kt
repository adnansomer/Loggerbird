package utils

import android.content.Context
import android.net.*
import android.os.Build
import java.net.HttpURLConnection
import java.net.URL

//InternetConnectionUtil class used for making network and internet connection check.
 internal class InternetConnectionUtil {
    /**
     * This Method makes http request and pings "https://accounts.google.com" url for checking current internet connection state.
     * Variables:
     * @var internetConnectionResult is used for getting response code after making http request.
     * @var getUrl is used for getting reference which url will get pinged.
     * @var internetConnection is used for getting response code after pinging url.
     */
    fun makeHttpRequest(): Int {
        var internetConnectionResult: Int = 0
        val getUrl: URL = URL("https://accounts.google.com")
        val internetConnection: HttpURLConnection = getUrl.openConnection() as HttpURLConnection
        internetConnection.connect()
        internetConnectionResult = internetConnection.responseCode
        return internetConnectionResult
    }

    /**
     * This Method makes network request and checks if current device have a wi-fi or celular connection.
     * Variables:
     * @var connectivityManager is used for ConnectivityManager reference
     * @var networkInfo is used for getting details if device has a network connection which used in devices which sdk is less than 23.
     * @var internetConnection is used for getting response code after pinging url.
     */
    fun checkNetworkConnection(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT < 23) {
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
                return true
            }
        } else {
            var checkNetwork: Boolean = false
            val networkRequest = NetworkRequest.Builder()
                .build()
            val networkInfo = connectivityManager.activeNetwork
            if (networkInfo != null) {
                val networkCapabilities: NetworkCapabilities =
                    connectivityManager.getNetworkCapabilities(networkInfo)
                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
//            var networkInfo=object:ConnectivityManager.NetworkCallback() {
//                override fun onAvailable(network: Network) {
//                    super.onAvailable(network)
//                    checkNetwork=true
//                }
//
//                override fun onLost(network: Network) {
//                    super.onLost(network)
//                    checkNetwork=false
//                }
//            }
        }
        return false
    }
}