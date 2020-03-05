package deneme.example.filedeneme

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.adjust.sdk.Adjust

object   AdjustLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityPaused(activity: Activity) {
       Adjust.onPause()
    }

    override fun onActivityResumed(activity: Activity) {
       Adjust.onResume()
    }
}