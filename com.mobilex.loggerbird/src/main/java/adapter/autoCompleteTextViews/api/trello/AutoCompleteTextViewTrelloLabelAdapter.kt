package adapter.autoCompleteTextViews.api.trello

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.mobilex.loggerbird.R

//Custom autoCompleteTextView adapter class for trello label.
/**
 * @param context is for getting reference from the application context.
 * @param resource is for getting the custom layout resources of the adapter.
 * @param arrayListLabelName is for getting the list of label names that will be used in the autoCompleteTextView.
 * @param arrayListLabelColor is for getting the list of label icons that will be used in the autoCompleteTextView.
 */
internal class AutoCompleteTextViewTrelloLabelAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    private val arrayListLabelName: ArrayList<String>,
    private val arrayListLabelColor: ArrayList<String>
) : ArrayAdapter<String>(context, resource, arrayListLabelName) {

    /**
     * Default ArrayAdapter class method.
     * @return size of the list that will be used in the autoCompleteTextView.
     */
    override fun getCount(): Int {
        return arrayListLabelName.size
    }

    /**
     * Default ArrayAdapter class method.
     * @return the specific item in the given position , which is in the list of autoCompleteTextView.
     */
    override fun getItem(position: Int): String? {
        return arrayListLabelName[position]
    }

    /**
     * Default ArrayAdapter class method.
     * @param position is for getting the current item's position in the list of autoCompleteTextView.
     * @param convertView is for getting the view of autoCompleteTextView.
     * @param parent is for getting the view group of the autoCompleteTextView.
     * @return View value.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.auto_text_view_trello_label_item,
            parent,
            false
        )
        val textViewLabel = view.findViewById<TextView>(R.id.textView_label_name)
        var backgroundColor: Int = ContextCompat.getColor(context, R.color.white)
        textViewLabel.text = arrayListLabelName[position]

        when (arrayListLabelColor[position]) {
            "green" -> backgroundColor = ContextCompat.getColor(context, R.color.green)
            "red" -> backgroundColor = ContextCompat.getColor(context, R.color.red)
            "yellow" -> backgroundColor = ContextCompat.getColor(context, R.color.yellow)
            "orange" -> backgroundColor = ContextCompat.getColor(context, R.color.orange)
            "purple" -> backgroundColor = ContextCompat.getColor(context, R.color.purple)
        }
        textViewLabel.setBackgroundColor(backgroundColor)
        return view
    }
}