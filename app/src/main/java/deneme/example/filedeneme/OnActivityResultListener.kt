package deneme.example.filedeneme

import android.util.Log
import androidx.activity.result.ActivityResultCallback

class OnActivityResultListener : ActivityResultCallback<Any> {
    override fun onActivityResult(result: Any?) {
       Log.d("activity_result","onActivityResult invoked")
    }
}