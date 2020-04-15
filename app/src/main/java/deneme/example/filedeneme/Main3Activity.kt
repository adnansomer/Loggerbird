package deneme.example.filedeneme

import RecyclerViewAdapter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.activity_main3.*


class Main3Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        val radius = 14f

        val decorView: View = window.decorView

        val windowBackground: Drawable = window.decorView.getBackground()

        blurView.setupWith(decorView.findViewById(android.R.id.content))
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(radius)
            .setHasFixedTransformationMatrix(true)

        getList()
    }

    private fun getList(){

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val versions = ArrayList<RecyclerModel>()
        versions.addAll(RecyclerModel.getCountryList())
        val myAdapter = RecyclerViewAdapter(versions)
        recyclerView.adapter = myAdapter

    }

}

