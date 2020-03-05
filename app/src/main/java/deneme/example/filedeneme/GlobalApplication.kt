package deneme.example.filedeneme

import android.app.Application
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig

class GlobalApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val appToken:String="{3ouw7p}"
        val environment:String=AdjustConfig.ENVIRONMENT_SANDBOX
        val adjustConfig:AdjustConfig= AdjustConfig(this,appToken,environment)
        val applicationLifecycleCallbacks=AdjustLifecycleCallbacks
        Adjust.onCreate(adjustConfig)
        registerActivityLifecycleCallbacks(applicationLifecycleCallbacks)

    }
}