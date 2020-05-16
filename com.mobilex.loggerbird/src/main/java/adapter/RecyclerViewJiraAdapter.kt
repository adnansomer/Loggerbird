package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobilex.loggerbird.R
import models.RecyclerViewJiraModel

class RecyclerViewJiraAdapter(private val fileList:ArrayList<RecyclerViewJiraModel>): RecyclerView.Adapter<RecyclerViewJiraAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_jira_item, parent, false))
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(fileList[position])

    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        fun bindItems(item : RecyclerViewJiraModel){
            val textViewFileName = itemView.findViewById<TextView>(R.id.textView_file_name)
            val floatingActionButtonCross = itemView.findViewById<FloatingActionButton>(R.id.floating_action_button_cross)
            textViewFileName.text = item.file.name
            floatingActionButtonCross.setOnClickListener {
            }

        }
    }
}