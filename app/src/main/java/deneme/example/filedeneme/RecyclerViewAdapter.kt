package deneme.example.filedeneme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation

class RecyclerViewAdapter(val countryList: ArrayList<RecyclerModel>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false))
    }

    override fun getItemCount(): Int {
        return countryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bindItems(countryList[position])

    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {

        fun bindItems(item : RecyclerModel){

            val countryName = itemView.findViewById<TextView>(R.id.recycler_view_name)
            //val countryImage = itemView.findViewById<ImageView>(R.id.recycler_view_img)
            countryName.setText(item.name)

//            Glide.with(itemView.context)
//                .load(item.imageUrl)
//                .placeholder(R.drawable.ic_android_black_100dp)
//                .into(countryImage)

        }

    }

}
