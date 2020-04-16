package listeners

import android.content.res.Resources
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginStart
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird


class FloatingActionButtonPaintOnTouchListener(
    private val floatingActionButtonPaint: FloatingActionButton,
    private val floatingActionButtonPaintSave: FloatingActionButton,
    private val floatingActionButtonPaintBrush: FloatingActionButton,
    private val floatingActionButtonPaintDelete: FloatingActionButton,
    private val floatingActionButtonPaintPalette: FloatingActionButton,
    private val floatingActionButtonPaintErase: FloatingActionButton

) : View.OnTouchListener {
    private var viewDx: Float = 0F
    private var viewDy: Float = 0F
    private var viewMinFabLayoutDx: Float = 0F
    private var viewMinFabLayoutDy: Float = 0F
    private var lastAction: Int = 0
    private val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
    private val deviceHeight = Resources.getSystem().displayMetrics.heightPixels
    private val viewMinFabLayout: LinearLayout =
        (floatingActionButtonPaintSave.parent as LinearLayout)

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    floatingActionButtonPaintSave.visibility = View.GONE
                    floatingActionButtonPaintBrush.visibility = View.GONE
                    floatingActionButtonPaintDelete.visibility = View.GONE
                    floatingActionButtonPaintPalette.visibility = View.GONE
                    floatingActionButtonPaintErase.visibility = View.GONE
                    viewDx = view.x - event.rawX
                    viewDy = view.y - event.rawY
                    viewMinFabLayoutDx = viewMinFabLayout.x - event.rawX
                    viewMinFabLayoutDy = viewMinFabLayout.y - event.rawY
                    lastAction = MotionEvent.ACTION_DOWN
                }
                MotionEvent.ACTION_MOVE -> {
                    if (deviceHeight > (event.rawY + viewDy + view.height) && (event.rawY + viewDy) > 0) {
                        view.y = event.rawY + viewDy

                    }
                    if (deviceWidth > (event.rawX + viewDx + view.width) && (event.rawX + viewDx) > 0) {
                        view.x = event.rawX + viewDx
                    }
                    lastAction = MotionEvent.ACTION_MOVE
                }
                MotionEvent.ACTION_UP -> {
                    if (deviceWidth < (event.rawX + (floatingActionButtonPaint.width))) {
                        view.x = deviceWidth.toFloat() - view.width
                        Log.d("corner", "a")
                        viewMinFabLayout.orientation =
                            LinearLayout.HORIZONTAL
                        viewMinFabLayout.gravity =
                            Gravity.BOTTOM or Gravity.CENTER_VERTICAL
                        val params: CoordinatorLayout.LayoutParams =
                            (viewMinFabLayout.layoutParams as CoordinatorLayout.LayoutParams)
                        params.anchorGravity = Gravity.BOTTOM or Gravity.CENTER_VERTICAL

                    } else if (event.rawX - (floatingActionButtonPaint.width) < 0) {
                        Log.d("corner", "b")
                        view.x = 0F
                        reverseLayout()
                        viewMinFabLayout.orientation =
                            LinearLayout.HORIZONTAL
//                        viewMinFabLayout.gravity =
//                            Gravity.START or Gravity.CENTER_VERTICAL
                        val params: CoordinatorLayout.LayoutParams =
                            (viewMinFabLayout.layoutParams as CoordinatorLayout.LayoutParams)
                        params.anchorGravity = Gravity.END or  Gravity.CENTER_VERTICAL
                    }
                    if (deviceHeight < (event.rawY + (floatingActionButtonPaint.height))) {
                        Log.d("corner", "c")
                        view.y = deviceHeight.toFloat() - view.height
                        viewMinFabLayout.orientation =
                            LinearLayout.VERTICAL
                        viewMinFabLayout.gravity =
                            Gravity.TOP or Gravity.CENTER_HORIZONTAL
                        val params: CoordinatorLayout.LayoutParams =
                            (viewMinFabLayout.layoutParams as CoordinatorLayout.LayoutParams)
                        params.anchorGravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    } else if (event.rawY - (floatingActionButtonPaint.height) < 0) {
                        Log.d("corner", "d")
                        view.y = 0F
                        viewMinFabLayout.orientation =
                            LinearLayout.VERTICAL
                        viewMinFabLayout.gravity =
                            Gravity.TOP or Gravity.CENTER_HORIZONTAL
                        val params: CoordinatorLayout.LayoutParams =
                            (viewMinFabLayout.layoutParams as CoordinatorLayout.LayoutParams)
                        params.anchorGravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    }
                    lastAction = MotionEvent.ACTION_UP
                    floatingActionButtonPaintSave.visibility = View.VISIBLE
                    floatingActionButtonPaintBrush.visibility = View.VISIBLE
                    floatingActionButtonPaintDelete.visibility = View.VISIBLE
                    floatingActionButtonPaintPalette.visibility = View.VISIBLE
                    floatingActionButtonPaintErase.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.floatingActionButtonPaintTag
            )
        }
        return false
    }
    private fun reverseLayout(){
        val viewList :ArrayList<View> = ArrayList()
        for((viewCounter, _) in (0 .. viewMinFabLayout.childCount).withIndex()){
            if(viewMinFabLayout.getChildAt(viewCounter)!=null){
                viewList.add(viewMinFabLayout.getChildAt(viewCounter))
            }
        }
        viewMinFabLayout.removeAllViewsInLayout()
        viewList.reverse()
        for(childView in viewList.iterator()){
            viewMinFabLayout.addView(childView)
        }
    }
}