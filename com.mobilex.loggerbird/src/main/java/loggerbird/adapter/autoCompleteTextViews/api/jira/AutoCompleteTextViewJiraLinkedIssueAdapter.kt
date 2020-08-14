package loggerbird.adapter.autoCompleteTextViews.api.jira

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import com.mobilex.loggerbird.R

//Custom autoCompleteTextView loggerbird.adapter class for jira linked issue.
/**
 * @param context is for getting reference from the application context.
 * @param resource is for getting the custom layout resources of the loggerbird.adapter.
 * @param arrayListLinkedIssueName is for getting the list that will be used in the autoCompleteTextView.
 */
internal class AutoCompleteTextViewJiraLinkedIssueAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    private val arrayListLinkedIssueName: ArrayList<String>
) : ArrayAdapter<String>(context, resource, arrayListLinkedIssueName) {

    /**
     * Default ArrayAdapter class method.
     * @return size of the list that will be used in the autoCompleteTextView.
     */
    override fun getCount(): Int {
        return arrayListLinkedIssueName.size
    }

    /**
     * Default ArrayAdapter class method.
     * @return the specific item in the given position , which is in the list of autoCompleteTextView.
     */
    override fun getItem(position: Int): String? {
        return arrayListLinkedIssueName[position]
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
            R.layout.auto_text_view_jira_linked_issue_item,
            parent,
            false
        )
        val textViewName = view.findViewById<TextView>(R.id.textView_jira_linked_issues_name)
        textViewName.text = arrayListLinkedIssueName[position]
        return view
    }
}