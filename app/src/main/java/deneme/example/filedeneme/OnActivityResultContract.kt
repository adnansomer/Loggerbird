package deneme.example.filedeneme

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class OnActivityResultContract : ActivityResultContract<Any, Any>() {
    override fun createIntent(input: Any?): Intent {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}