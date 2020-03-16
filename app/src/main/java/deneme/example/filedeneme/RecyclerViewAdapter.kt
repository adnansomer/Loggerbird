package deneme.example.filedeneme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(val nameList : ArrayList<RecyclerModel>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>()  {

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {

        val personName : TextView = view.findViewById(R.id.recycler_view_name)

        fun bindItems(item: RecyclerModel) {
            personName.setText(item.name)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return nameList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(nameList.get(position))
    }

}