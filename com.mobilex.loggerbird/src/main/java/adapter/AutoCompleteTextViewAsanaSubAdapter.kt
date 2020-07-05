package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import com.mobilex.loggerbird.R

class AutoCompleteTextViewAsanaSubAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    private val arrayListName: ArrayList<String>
) : ArrayAdapter<String>(context, resource, arrayListName) {

    override fun getCount(): Int {
        return arrayListName.size
    }

    override fun getItem(position: Int): String? {
        return arrayListName[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.auto_text_view_asana_sub_item,
                parent,
                false
        )
        val textViewName =view.findViewById<TextView>(R.id.textView_name)
        textViewName.text = arrayListName[position]
        return view
    }
}