package loggerbird.adapter.autoCompleteTextViews.api.jira

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import com.mobilex.loggerbird.R

//Custom autoCompleteTextView loggerbird.adapter class for jira issue type.
/**
 * @param context is for getting reference from the application context.
 * @param resource is for getting the custom layout resources of the loggerbird.adapter.
 * @param arrayListIssueTypeName is for getting the list that will be used in the autoCompleteTextView.
 */
internal class AutoCompleteTextViewJiraIssueTypeAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    private val arrayListIssueTypeName: ArrayList<String>
) : ArrayAdapter<String>(context, resource, arrayListIssueTypeName) {

    /**
     * Default ArrayAdapter class method.
     * @return size of the list that will be used in the autoCompleteTextView.
     */
    override fun getCount(): Int {
        return arrayListIssueTypeName.size
    }

    /**
     * Default ArrayAdapter class method.
     * @return the specific item in the given position , which is in the list of autoCompleteTextView.
     */
    override fun getItem(position: Int): String? {
        return arrayListIssueTypeName[position]
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
            R.layout.auto_text_view_jira_issue_type_item,
            parent,
            false
        )
        val textViewName = view.findViewById<TextView>(R.id.textView_jira_issue_type_name)
        textViewName.text = arrayListIssueTypeName[position]
        return view
    }
}