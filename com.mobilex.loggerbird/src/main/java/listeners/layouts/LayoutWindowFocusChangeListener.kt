package listeners.layouts

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi


//Dummy class might be useful in future.
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
internal class LayoutWindowFocusChangeListener(private val view: View, private val initializeFloatingActionButton: Runnable) :
    ViewTreeObserver.OnWindowFocusChangeListener {
    companion object{
        var controlFocusChangeListener:Boolean = true
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if(!hasFocus){
            initializeFloatingActionButton.run()
//            view.viewTreeObserver.removeOnWindowFocusChangeListener(this)
//            view.viewTreeObserver.addOnGlobalLayoutListener(LayoutGlobalLayoutListener(rootView = (view as ViewGroup) , runnable = initializeFloatingActionButton))
        }
    }
}