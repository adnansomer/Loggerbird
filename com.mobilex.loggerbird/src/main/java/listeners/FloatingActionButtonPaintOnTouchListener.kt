package listeners

import android.content.res.Resources
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobilex.loggerbird.R
import constants.Constants
import loggerbird.LoggerBird


class FloatingActionButtonPaintOnTouchListener(
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

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
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
                        view.x = deviceWidth.toFloat() - view.width
                        floatingActionButtonPaintBrush.x =
                            (view.x - floatingActionButtonPaintBrush.width)
                        floatingActionButtonPaintPalette.x =
                            (view.x - floatingActionButtonPaintPalette.width) - 150
                        floatingActionButtonPaintDelete.x =
                            (view.x - floatingActionButtonPaintDelete.width) - 300
                        floatingActionButtonPaintErase.x =
                            (view.x - floatingActionButtonPaintErase.width) - 450
                        floatingActionButtonPaintBack.x =
                            (view.x - floatingActionButtonPaintBack.width) - 600
                        floatingActionButtonPaintSave.x =
                            (view.x - floatingActionButtonPaintSave.width) - 750
                        floatingActionButtonPaintBrush.y = view.y
                        floatingActionButtonPaintPalette.y = view.y
                        floatingActionButtonPaintDelete.y = view.y
                        floatingActionButtonPaintErase.y = view.y
                        floatingActionButtonPaintBack.y = view.y
                        floatingActionButtonPaintSave.y = view.y

                    } else if (event.rawX - (floatingActionButtonPaint.width) < 0) {
                        Log.d("corner", "b")
                        view.x = 0F
                        floatingActionButtonPaintBrush.x =
                            floatingActionButtonPaintBrush.width.toFloat()
                        floatingActionButtonPaintPalette.x =
                            floatingActionButtonPaintPalette.width.toFloat() + 150
                        floatingActionButtonPaintDelete.x =
                            floatingActionButtonPaintDelete.width.toFloat() + 300
                        floatingActionButtonPaintErase.x =
                            floatingActionButtonPaintErase.width.toFloat() + 450
                        floatingActionButtonPaintBack.x =
                            floatingActionButtonPaintBack.width.toFloat() + 600
                        floatingActionButtonPaintSave.x =
                            floatingActionButtonPaintSave.width.toFloat() + 750
                        floatingActionButtonPaintBrush.y = view.y
                        floatingActionButtonPaintPalette.y = view.y
                        floatingActionButtonPaintDelete.y = view.y
                        floatingActionButtonPaintErase.y = view.y
                        floatingActionButtonPaintBack.y = view.y
                        floatingActionButtonPaintSave.y = view.y
                    }
                    if (deviceHeight < (event.rawY + (floatingActionButtonPaint.height))) {
                        Log.d("corner", "c")
                        view.y = deviceHeight.toFloat() - view.height
                        floatingActionButtonPaintBrush.x = view.x
                        floatingActionButtonPaintPalette.x = view.x
                        floatingActionButtonPaintDelete.x = view.x
                        floatingActionButtonPaintErase.x = view.x
                        floatingActionButtonPaintBack.x = view.x
                        floatingActionButtonPaintSave.x = view.x
                        floatingActionButtonPaintBrush.y =
                            (view.y - floatingActionButtonPaintBrush.height)
                        floatingActionButtonPaintPalette.y =
                            (view.y - floatingActionButtonPaintPalette.height) - 150
                        floatingActionButtonPaintDelete.y =
                            (view.y - floatingActionButtonPaintDelete.height) - 300
                        floatingActionButtonPaintErase.y =
                            (view.y - floatingActionButtonPaintErase.height) - 450
                        floatingActionButtonPaintBack.y =
                            (view.y - floatingActionButtonPaintBack.height) - 600
                        floatingActionButtonPaintSave.y =
                            (view.y - floatingActionButtonPaintSave.height) - 750

                    } else if (event.rawY - (floatingActionButtonPaint.height) < 0) {
                        Log.d("corner", "d")
                        view.y = 0F
                        floatingActionButtonPaintBrush.x = view.x
                        floatingActionButtonPaintPalette.x = view.x
                        floatingActionButtonPaintDelete.x = view.x
                        floatingActionButtonPaintErase.x = view.x
                        floatingActionButtonPaintBack.x = view.x
                        floatingActionButtonPaintSave.x = view.x
                        floatingActionButtonPaintBrush.y =
                            floatingActionButtonPaintBrush.height.toFloat()
                        floatingActionButtonPaintPalette.y =
                            floatingActionButtonPaintPalette.height.toFloat() + 150
                        floatingActionButtonPaintDelete.y =
                            floatingActionButtonPaintDelete.height.toFloat() + 300
                        floatingActionButtonPaintErase.y =
                            floatingActionButtonPaintErase.height.toFloat() + 450
                        floatingActionButtonPaintBack.y =
                            floatingActionButtonPaintBack.height.toFloat() + 600
                        floatingActionButtonPaintSave.y =
                            floatingActionButtonPaintSave.height.toFloat() + 750

                    }
                    floatingActionButtonPaint.setImageResource(R.drawable.ic_close_black_24dp)
                    floatingActionButtonPaintSave.visibility = View.VISIBLE
                    floatingActionButtonPaintBrush.visibility = View.VISIBLE
                    floatingActionButtonPaintDelete.visibility = View.VISIBLE
                    floatingActionButtonPaintPalette.visibility = View.VISIBLE
                    floatingActionButtonPaintErase.visibility = View.VISIBLE
                    floatingActionButtonPaintBack.visibility = View.VISIBLE
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
        return false
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
}