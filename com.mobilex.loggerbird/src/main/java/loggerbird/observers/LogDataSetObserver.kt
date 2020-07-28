package loggerbird.observers

import android.content.Context
import androidx.recyclerview.widget.RecyclerView

//dummy class will be deleted although can be still improved and be used in my opinion!
internal class LogDataSetObserver(context: Context) : RecyclerView(context) {
    private var mObserversArrayList: ArrayList<Any> = ArrayList()
    fun takeObserverList() {
//       synchronized(mChild){
//           for(mObserversItem in mObservers){
//               mObserversArrayList.add(mObserversItem)
//           }
//       }
    }

    fun returnObserverItemList(): ArrayList<Any> {
        return mObserversArrayList
    }
}