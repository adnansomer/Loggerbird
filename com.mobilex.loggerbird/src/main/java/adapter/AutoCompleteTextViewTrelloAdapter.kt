package adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.mobilex.loggerbird.R

class AutoCompleteTextViewTrelloAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    private val arrayListLabel: ArrayList<String>,
    private val arrayListLabelColor:ArrayList<String>
) : ArrayAdapter<String>(context, resource, arrayListLabel) {

    override fun getCount(): Int {
        return arrayListLabel.size
    }

    override fun getItem(position: Int): String? {
        return arrayListLabel[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.auto_text_view_trello_label_item,
                parent,
                false
        )
        val textViewLabel =view.findViewById<TextView>(R.id.textView_label_name)
        var backgroundColor:Int = ContextCompat.getColor(context,R.color.white)
        textViewLabel.text = arrayListLabel[position]

        when(arrayListLabelColor[position]){
            "green" -> backgroundColor = ContextCompat.getColor(context , R.color.green)
            "red" -> backgroundColor = ContextCompat.getColor(context,R.color.red)
            "yellow" -> backgroundColor = ContextCompat.getColor(context,R.color.yellow)
            "orange" -> backgroundColor = ContextCompat.getColor(context,R.color.orange)
            "purple" -> backgroundColor = ContextCompat.getColor(context,R.color.purple)
        }
        textViewLabel.setBackgroundColor(backgroundColor)
        return view
    }
}