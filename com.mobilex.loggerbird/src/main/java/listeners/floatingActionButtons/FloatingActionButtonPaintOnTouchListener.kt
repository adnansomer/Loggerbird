package listeners.floatingActionButtons

import android.content.res.Resources
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobilex.loggerbird.R
import constants.Constants
import loggerbird.LoggerBird

//This class is used for making paint floating action buttons movable.
internal class FloatingActionButtonPaintOnTouchListener(
    private val floatingActionButtonPaint: FloatingActionButton,
    private val floatingActionButtonPaintSave: FloatingActionButton,
    private val floatingActionButtonPaintBack: FloatingActionButton,
    private val floatingActionButtonPaintBrush: FloatingActionButton,
    private val floatingActionButtonPaintDelete: FloatingActionButton,
    private val floatingActionButtonPaintPalette: FloatingActionButton,
    private val floatingActionButtonPaintErase: FloatingActionButton

) : View.OnTouchListener {
    private var viewDx: Float = 0F
    private var viewDy: Float = 0F
    private var floatingActionButtonPaintBrushDx = 0F
    private var floatingActionButtonPaintBrushDy = 0F
    private var floatingActionButtonPaintPaletteDx = 0F
    private var floatingActionButtonPaintPaletteDy = 0F
    private var floatingActionButtonPaintDeleteDx = 0F
    private var floatingActionButtonPaintDeleteDy = 0F
    private var floatingActionButtonPaintEraseDx = 0F
    private var floatingActionButtonPaintEraseDy = 0F
    private var floatingActionButtonPaintBackDx = 0F
    private var floatingActionButtonPaintBackDy = 0F
    private var floatingActionButtonPaintSaveDx = 0F
    private var floatingActionButtonPaintSaveDy = 0F
    private var lastAction: Int = 0
    private val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
    private val deviceHeight = Resources.getSystem().displayMetrics.heightPixels
    private var controlLayoutGravity = false

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
//                    adjustGravity()
                    floatingActionButtonPaint.setImageResource(R.drawable.ic_add_white_24dp)
                    floatingActionButtonPaintSave.visibility = View.GONE
                    floatingActionButtonPaintBack.visibility = View.GONE
                    floatingActionButtonPaintBrush.visibility = View.GONE
                    floatingActionButtonPaintDelete.visibility = View.GONE
                    floatingActionButtonPaintPalette.visibility = View.GONE
                    floatingActionButtonPaintErase.visibility = View.GONE
                    viewDx = view.x - event.rawX
                    viewDy = view.y - event.rawY
                    floatingActionButtonPaintBrushDx = floatingActionButtonPaintBrush.x - event.rawX
                    floatingActionButtonPaintBrushDy = floatingActionButtonPaintBrush.y - event.rawY
                    floatingActionButtonPaintPaletteDx =
                        floatingActionButtonPaintPalette.x - event.rawX
                    floatingActionButtonPaintPaletteDy =
                        floatingActionButtonPaintPalette.y - event.rawY
                    floatingActionButtonPaintDeleteDx =
                        floatingActionButtonPaintDelete.x - event.rawX
                    floatingActionButtonPaintDeleteDy =
                        floatingActionButtonPaintDelete.y - event.rawY
                    floatingActionButtonPaintEraseDx = floatingActionButtonPaintErase.x - event.rawX
                    floatingActionButtonPaintEraseDy = floatingActionButtonPaintErase.y - event.rawY
                    floatingActionButtonPaintBackDx = floatingActionButtonPaintBack.x - event.rawX
                    floatingActionButtonPaintBackDy = floatingActionButtonPaintBack.y - event.rawY
                    floatingActionButtonPaintSaveDx = floatingActionButtonPaintSave.x - event.rawX
                    floatingActionButtonPaintSaveDy = floatingActionButtonPaintSave.y - event.rawY
                    lastAction = MotionEvent.ACTION_DOWN
                }
                MotionEvent.ACTION_MOVE -> {
                    if (deviceHeight > (event.rawY + viewDy + view.height) && (event.rawY + viewDy) > 0) {
                        view.y = event.rawY + viewDy
                        floatingActionButtonPaintBrush.y =
                            event.rawY + floatingActionButtonPaintBrushDy
                        floatingActionButtonPaintPalette.y =
                            event.rawY + floatingActionButtonPaintPaletteDy
                        floatingActionButtonPaintDelete.y =
                            event.rawY + floatingActionButtonPaintDeleteDy
                        floatingActionButtonPaintErase.y =
                            event.rawY + floatingActionButtonPaintEraseDy
                        floatingActionButtonPaintBack.y =
                            event.rawY + floatingActionButtonPaintBackDy
                        floatingActionButtonPaintSave.y =
                            event.rawY + floatingActionButtonPaintSaveDy

                    }
                    if (deviceWidth > (event.rawX + viewDx + view.width) && (event.rawX + viewDx) > 0) {
                        view.x = event.rawX + viewDx
                        floatingActionButtonPaintBrush.x =
                            event.rawX + floatingActionButtonPaintBrushDx
                        floatingActionButtonPaintPalette.x =
                            event.rawX + floatingActionButtonPaintPaletteDx
                        floatingActionButtonPaintDelete.x =
                            event.rawX + floatingActionButtonPaintDeleteDx
                        floatingActionButtonPaintErase.x =
                            event.rawX + floatingActionButtonPaintEraseDx
                        floatingActionButtonPaintBack.x =
                            event.rawX + floatingActionButtonPaintBackDx
                        floatingActionButtonPaintSave.x =
                            event.rawX + floatingActionButtonPaintSaveDx
                    }
                    lastAction = MotionEvent.ACTION_MOVE
                }
                MotionEvent.ACTION_UP -> {
                    if (deviceWidth < (event.rawX + (floatingActionButtonPaint.width))) {
                        Log.d("corner", "a")
                        controlLayoutGravity= true
                        floatingActionButtonPaintBrush.y = view.y
                        floatingActionButtonPaintPalette.y = view.y
                        floatingActionButtonPaintDelete.y = view.y
                        floatingActionButtonPaintErase.y = view.y
                        floatingActionButtonPaintBack.y = view.y
                        floatingActionButtonPaintSave.y = view.y
                        view.x = deviceWidth.toFloat() - view.width
                        floatingActionButtonPaintBrush.x =
                            (view.x - view.width)
                        floatingActionButtonPaintPalette.x =
                            (view.x - view.width) - 150
                        floatingActionButtonPaintDelete.x =
                            (view.x - view.width) - 300
                        floatingActionButtonPaintErase.x =
                            (view.x - view.width) - 450
                        floatingActionButtonPaintBack.x =
                            (view.x - view.width) - 600
                        floatingActionButtonPaintSave.x =
                            (view.x - view.width) - 750

                    } else if (event.rawX - (floatingActionButtonPaint.width) < 0) {
                        Log.d("corner", "b")
                        controlLayoutGravity= true
                        floatingActionButtonPaintBrush.y = view.y
                        floatingActionButtonPaintPalette.y = view.y
                        floatingActionButtonPaintDelete.y = view.y
                        floatingActionButtonPaintErase.y = view.y
                        floatingActionButtonPaintBack.y = view.y
                        floatingActionButtonPaintSave.y = view.y
                        view.x = 0F
                        floatingActionButtonPaintBrush.x =
                            view.width.toFloat()
                        floatingActionButtonPaintPalette.x =
                            view.width.toFloat() + 150
                        floatingActionButtonPaintDelete.x =
                            view.width.toFloat() + 300
                        floatingActionButtonPaintErase.x =
                            view.width.toFloat() + 450
                        floatingActionButtonPaintBack.x =
                            view.width.toFloat() + 600
                        floatingActionButtonPaintSave.x =
                            view.width.toFloat() + 750
                    }
                    if (deviceHeight < (event.rawY + (floatingActionButtonPaint.height))) {
                        Log.d("corner", "c")
                        controlLayoutGravity= true
                        floatingActionButtonPaintBrush.x = view.x
                        floatingActionButtonPaintPalette.x = view.x
                        floatingActionButtonPaintDelete.x = view.x
                        floatingActionButtonPaintErase.x = view.x
                        floatingActionButtonPaintBack.x = view.x
                        floatingActionButtonPaintSave.x = view.x
                        view.y = deviceHeight.toFloat() - view.height
                        floatingActionButtonPaintBrush.y =
                            (view.y - view.height)
                        floatingActionButtonPaintPalette.y =
                            (view.y - view.height) - 150
                        floatingActionButtonPaintDelete.y =
                            (view.y - view.height) - 300
                        floatingActionButtonPaintErase.y =
                            (view.y - view.height) - 450
                        floatingActionButtonPaintBack.y =
                            (view.y - view.height) - 600
                        floatingActionButtonPaintSave.y =
                            (view.y - view.height) - 750

                    } else if (event.rawY - (floatingActionButtonPaint.height) < 0) {
                        Log.d("corner", "d")
                        controlLayoutGravity= true
                        floatingActionButtonPaintBrush.x = view.x
                        floatingActionButtonPaintPalette.x = view.x
                        floatingActionButtonPaintDelete.x = view.x
                        floatingActionButtonPaintErase.x = view.x
                        floatingActionButtonPaintBack.x = view.x
                        floatingActionButtonPaintSave.x = view.x
                        view.y = 0F
                        floatingActionButtonPaintBrush.y =
                            view.height.toFloat()
                        floatingActionButtonPaintPalette.y =
                            view.height.toFloat() + 150
                        floatingActionButtonPaintDelete.y =
                            view.height.toFloat() + 300
                        floatingActionButtonPaintErase.y =
                            view.height.toFloat() + 450
                        floatingActionButtonPaintBack.y =
                            view.height.toFloat() + 600
                        floatingActionButtonPaintSave.y =
                            view.height.toFloat() + 750

                    }
//                    floatingActionButtonPaint.setImageResource(R.drawable.ic_close_black_24dp)
//                    floatingActionButtonPaintSave.visibility = View.VISIBLE
//                    floatingActionButtonPaintBrush.visibility = View.VISIBLE
//                    floatingActionButtonPaintDelete.visibility = View.VISIBLE
//                    floatingActionButtonPaintPalette.visibility = View.VISIBLE
//                    floatingActionButtonPaintErase.visibility = View.VISIBLE
//                    floatingActionButtonPaintBack.visibility = View.VISIBLE
//                    adjustGravity()
                    floatingActionButtonPaint.performClick()
                    lastAction = MotionEvent.ACTION_UP
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
        return true
    }
    //    private fun reverseLayout(){
//        val viewList :ArrayList<View> = ArrayList()
//        for((viewCounter, _) in (0 .. viewMinFabLayout.childCount).withIndex()){
//            if(viewMinFabLayout.getChildAt(viewCounter)!=null){
//                viewList.add(viewMinFabLayout.getChildAt(viewCounter))
//            }
//        }
//        viewMinFabLayout.removeAllViewsInLayout()
//        viewList.reverse()
//        for(childView in viewList.iterator()){
//            viewMinFabLayout.addView(childView)
//        }
//    }
    private fun adjustGravity(){
        if(controlLayoutGravity){
            (floatingActionButtonPaintBrush.layoutParams as  CoordinatorLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
            (floatingActionButtonPaintPalette.layoutParams as  CoordinatorLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
            (floatingActionButtonPaintDelete.layoutParams as  CoordinatorLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
            (floatingActionButtonPaintErase.layoutParams as  CoordinatorLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
            (floatingActionButtonPaintBack.layoutParams as  CoordinatorLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
            (floatingActionButtonPaintSave.layoutParams as  CoordinatorLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
            controlLayoutGravity = false
        }
    }
}