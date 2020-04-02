package deneme.example.filedeneme

import android.app.Application
import loggerbird.LoggerBird

class application:Application() {
    override fun onCreate() {
        super.onCreate()
        LoggerBird.logInit(context = this)
    }
}