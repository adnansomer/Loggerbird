package deneme.example.filedeneme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_main3.*


class Main3Activity : AppCompatActivity() {
    private lateinit var  bottomSheet:BottomSheetDialog
    private lateinit var  bottomSheetView:View
    private lateinit var textView:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val rootView: ViewGroup =
            this.window.decorView.findViewById(android.R.id.content)
        button_dummy_4.setOnClickListener {
            bottomSheet = BottomSheetDialog(this)
            bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_view , rootView , false)
            textView=bottomSheetView.findViewById(R.id.textView)
            bottomSheet.setContentView(bottomSheetView)
            bottomSheet.setOnShowListener {
                Log.d("bottom_sheet","clicked")
            }
            textView.setOnClickListener {
                Log.d("current_focus",window.decorView.rootView.toString())
            }
            bottomSheet.show()
        }

    }

}
