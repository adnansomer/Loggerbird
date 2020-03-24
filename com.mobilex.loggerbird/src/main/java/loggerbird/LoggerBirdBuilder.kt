package loggerbird

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import java.io.File
import java.net.HttpURLConnection

class LoggerBirdBuilder private constructor(
    val context: Context, val file: File, val fragmentManager: FragmentManager,
    val view: View, val resources: Resources, val httpUrlConnection: HttpURLConnection,
    val retrofit: Retrofit, val response: Response, val request: Request, val bundle: Bundle
) {

    /**This class makes LoggerBird library ready for Builder Pattern
     * @var context initializes context for using relevant method.
     * @var file    initializes file for using relevant method
     * @var fragmentManager initializes fragmentManager for using relevant method
     * @var view initializes view for using relevant method
     * @var resources initializes resources for using relevant method
     * @var httpUrlConnection initializes httpUrlConnection for using relevant method
     * @var retrofit initializes retrofit for using relevant method
     * @var response initializes response for using relevant method
     * @var requesst initializes requesst for using relevant method
     * @var bundle initializes bundle for using relevant method
     */

//    data class Builder(
//        private var context:Context? = null,
//        private var file: File? = null,
//        private var fragmentManager: FragmentManager? = null,
//        private var view: View? = null,
//        private var resources: Resources? = null,
//        private var okHttpUrlConnection: HttpURLConnection? = null,
//        private var okHttpClient: OkHttpClient? = null,
//        private var okHttpRequest : Request? = null,
//        private var retrofit: Retrofit? = null,
//        private var response: Response? = null,
//        private var request: Request? = null,
//        private var bundle: Bundle? = null
//
//    ) {
//
//        fun logInit() = apply { LoggerBird.logInit(context!!, file, fragmentManager)}
//
//        fun isLogInitAttached() = apply { LoggerBird.isLogInitAttached() }
//
//        fun refreshLogInitInstance() = apply { LoggerBird.refreshLogInitInstance() }
//
//        fun logDetachObserver() = apply { LoggerBird.logDetachObserver() }
//
//        fun logDetachFragmentObserver() = apply { LoggerBird.logDetachFragmentObserver(fragmentManager!!) }
//
//        fun logDetach() = apply { LoggerBird.logDetach() }
//
//        fun logRefreshInstance() = apply { LoggerBird.logRefreshInstance() }
//
//        fun logAttach() = apply { LoggerBird.logAttach(context!!, fragmentManager) }
//
//        fun takeComponentDetails() = apply { LoggerBird.takeComponentDetails(view, resources) }
//
//        fun takeLifeCycleDetails() = apply { LoggerBird.takeLifeCycleDetails() }
//
//        fun takeDeviceCpuDetails() = apply { LoggerBird.takeDeviceCpuDetails() }
//
//        fun takeDeviceInformationDetails() = apply { LoggerBird.takeDeviceInformationDetails() }
//
//        fun takeMemoryUsageDetails() = apply { LoggerBird.takeMemoryUsageDetails(threshold = null) }
//
//        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
//        fun takeDevicePerformanceDetails() = apply { LoggerBird.takeDevicePerformanceDetails() }
//
//        fun takeAnalyticsDetails() = apply { LoggerBird.takeAnalyticsDetails(bundle) }
//
//        fun takeFragmentManagerDetails() = apply { LoggerBird.takeFragmentManagerDetails(fragmentManager) }
//
//        fun takeHttpRequestDetails() = apply { LoggerBird.takeOkHttpDetails(okHttpClient,okHttpRequest,okHttpUrlConnection) }
//
//        fun takeInAPurchaseDetails() = apply { LoggerBird.takeInAPurchaseDetails() }
//
//        fun takeRetrofitRequestDetails() = apply { LoggerBird.takeRetrofitRequestDetails(retrofit, response, request) }
//
//        fun takeExceptionDetails() = apply { LoggerBird.takeExceptionDetails() }
//
//    }
}