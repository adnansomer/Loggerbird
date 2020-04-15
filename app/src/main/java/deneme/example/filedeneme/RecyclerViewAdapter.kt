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

class RecyclerViewAdapter(val context: Context, val nameList : ArrayList<RecyclerModel>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>()  {
    class ViewHolder(private val context: Context,view : View) : RecyclerView.ViewHolder(view) {
        private var handler: Handler = Handler()
        private lateinit var imageViewTempBlur: ImageView
        val personName : TextView = view.findViewById(R.id.recycler_view_name)
        val cardView : CardView = view.findViewById(R.id.card_view)
        fun bindItems(position: Int,item: RecyclerModel) {
            personName.setText(item.name)
//            blurImage(cardView.background)
//            cardView.background.alpha = 254
//            cardView.background.setTint(context.resources.getColor(R.color.blur_1,context.theme))
//            if(position%2 ==0){
//
//
//            }else{
//
//            }

//            cardView.setBackgroundResource(R.drawable.cici_kus)
//            imageViewTempBlur = ImageView(context)
//            imageViewTempBlur.setImageDrawable(cardView.background)
//            blurImage(imageViewTempBlur)
//            cardView.background = imageViewTempBlur.drawable


        }
        fun blurImage(drawable: Drawable) {
            handler.post {
                imageViewTempBlur = ImageView(context)
                imageViewTempBlur.setImageDrawable(drawable)
                imageViewTempBlur.setImageBitmap(takeScreenShotWithBlur(view = imageViewTempBlur))
            }


        }

        fun takeScreenShotWithBlur(view: View): Bitmap {
//        val viewTemp: View = (view.parent as View)
            val bitmap: Bitmap = Bitmap.createBitmap(
                (100),
                (100),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            Glide.with(context)
                .load(imageViewTempBlur.drawToBitmap())
                .apply(RequestOptions.bitmapTransform(BlurTransformation()))
                .apply(RequestOptions.bitmapTransform(BlurTransformation()))
                .into(imageViewTempBlur)
            cardView.background = imageViewTempBlur.background
            return bitmap
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(context,view)

    }

    override fun getItemCount(): Int {
        return nameList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(position,nameList.get(position))
    }




}
