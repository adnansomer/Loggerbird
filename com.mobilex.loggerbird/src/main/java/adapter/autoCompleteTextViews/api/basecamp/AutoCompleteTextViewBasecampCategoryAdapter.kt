package adapter.autoCompleteTextViews.api.basecamp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.mobilex.loggerbird.R

//Custom autoCompleteTextView adapter class for basecamp category.
/**
 * @param context is for getting reference from the application context.
 * @param resource is for getting the custom layout resources of the adapter.
 * @param arrayListIconName is for getting the list of icon names that will be used in the autoCompleteTextView.
 * @param arrayListIconShape is for getting the list of icon shapes that will be used in the autoCompleteTextView.
 */
internal class AutoCompleteTextViewBasecampCategoryAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    private val arrayListIconName: ArrayList<String>,
    private val arrayListIconShape: ArrayList<String>
) : ArrayAdapter<String>(context, resource, arrayListIconName) {

    /**
     * Default ArrayAdapter class method.
     * @return size of the list that will be used in the autoCompleteTextView.
     */
    override fun getCount(): Int {
        return arrayListIconName.size
    }

    /**
     * Default ArrayAdapter class method.
     * @return the specific item in the given position , which is in the list of autoCompleteTextView.
     */
    override fun getItem(position: Int): String? {
        return arrayListIconName[position]
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
            R.layout.auto_text_view_basecamp_icon_item,
            parent,
            false
        )
        val textViewIconName = view.findViewById<TextView>(R.id.textView_icon_name)
        val textViewIconShape = view.findViewById<TextView>(R.id.textView_icon_shape)
        textViewIconName.text = arrayListIconName[position]
        textViewIconShape.text = arrayListIconShape[position]
        return view
    }
}