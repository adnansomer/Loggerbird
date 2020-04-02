package listeners

import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class LogMediaProjectionListener(private val mediaRecorder: MediaRecorder) : MediaProjection.Callback() {
    override fun onStop() {
        super.onStop()
        mediaRecorder.stop()
        mediaRecorder.reset()
    }
}