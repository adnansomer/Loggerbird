package utils.other

import android.content.Context
import android.net.*
import android.os.Build
import constants.Constants
import loggerbird.LoggerBird
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
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun makeHttpRequest(): Int {
        var internetConnectionResult = 0
        try {
            val getUrl: URL = URL("https://accounts.google.com")
            val internetConnection: HttpURLConnection = getUrl.openConnection() as HttpURLConnection
            internetConnection.connect()
            internetConnectionResult = internetConnection.responseCode

        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
        }
        return internetConnectionResult
    }

    /**
     * This Method makes network request and checks if current device have a wi-fi or cellular connection.
     * Parameters:
     * @param context parameter used for getting context of the current activity or fragment.
     * Variables:
     * @var connectivityManager is used for ConnectivityManager reference.
     * @var networkInfo is used for getting details if device has a network connection which used in devices which sdk is less than 23.
     * @var internetConnection is used for getting response code after pinging url.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun checkNetworkConnection(context: Context): Boolean {
        try {
            val connectivityManager: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT < 23) {
                val networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
                    return true
                }
            } else {
                val networkInfo = connectivityManager.activeNetwork
                if (networkInfo != null) {
                    val networkCapabilities: NetworkCapabilities =
                        connectivityManager.getNetworkCapabilities(networkInfo)
                    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
        }
        return false
    }
}