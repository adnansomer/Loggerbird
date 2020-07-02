package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.mobilex.loggerbird.R

class AutoCompleteTextViewBasecampAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    private val arrayListIconName: ArrayList<String>,
    private val arrayListIconShape:ArrayList<String>
) : ArrayAdapter<String>(context, resource, arrayListIconName) {

    override fun getCount(): Int {
        return arrayListIconName.size
    }

    override fun getItem(position: Int): String? {
        return arrayListIconName[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.auto_text_view_basecamp_icon_item,
                parent,
                false
        )
        val textViewIconName =view.findViewById<TextView>(R.id.textView_icon_name)
        val textViewIconShape = view.findViewById<TextView>(R.id.textView_icon_shape)
        textViewIconName.text = arrayListIconName[position]
        textViewIconShape.text = arrayListIconShape[position]
        return view
    }
}