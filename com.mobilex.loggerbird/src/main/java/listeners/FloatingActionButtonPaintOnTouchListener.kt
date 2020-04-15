package listeners

import android.content.res.Resources
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import constants.Constants
import loggerbird.LoggerBird

class FloatingActionButtonPaintOnTouchListener(
    private val windowManager: WindowManager,
    private val windowManagerView: View,
    private val windowManagerParams: WindowManager.LayoutParams,
    private val floatingActionButtonPaint: FloatingActionButton,
    private val floatingActionButtonPaintSave:FloatingActionButton,
    private val floatingActionButtonPaintBrush:FloatingActionButton,
    private val floatingActionButtonPaintDelete:FloatingActionButton,
    private val floatingActionButtonPaintPalette:FloatingActionButton,
    private val floatingActionButtonPaintErase:FloatingActionButton
    
) : View.OnTouchListener {
    private var windowManagerDx: Float = 0F
    private var windowManagerDy: Float = 0F
    private var lastAction: Int = 0
    private val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
    private val deviceHeight = Resources.getSystem().displayMetrics.heightPixels
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        try {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    floatingActionButtonPaint.visibility = View.GONE
                    floatingActionButtonPaintSave.visibility = View.GONE
                    floatingActionButtonPaintBrush.visibility = View.GONE
                    floatingActionButtonPaintDelete.visibility = View.GONE
                    floatingActionButtonPaintPalette.visibility = View.GONE
                    floatingActionButtonPaintErase.visibility = View.GONE
                    windowManagerDx = windowManagerParams.x - event.rawX
                    windowManagerDy = windowManagerParams.y - event.rawY
                    lastAction = MotionEvent.ACTION_DOWN
                }
                MotionEvent.ACTION_MOVE -> {
                    windowManagerParams.x = event.rawX.toInt() + windowManagerDx.toInt()
                    windowManagerParams.y = event.rawY.toInt() + windowManagerDy.toInt()
                    lastAction = MotionEvent.ACTION_MOVE
                    windowManager.updateViewLayout(
                        windowManagerView,
                        windowManagerParams
                    )
                }
                MotionEvent.ACTION_UP -> {
                    if (deviceWidth < (event.rawX + (floatingActionButtonPaint.width))) {
                        windowManagerParams.x = deviceWidth / 2
                        Log.d("corner", "a")
                        (floatingActionButtonPaint.layoutParams as FrameLayout.LayoutParams).setMargins(750,0,0,0)
                        (floatingActionButtonPaintSave.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            0,
                            0,
                            0
                        )
                        (floatingActionButtonPaintBrush.layoutParams as FrameLayout.LayoutParams).setMargins(
                            600,
                            0,
                            0,
                            0
                        )
                        (floatingActionButtonPaintDelete.layoutParams as FrameLayout.LayoutParams).setMargins(
                            300,
                            0,
                            0,
                            0
                        )
                        (floatingActionButtonPaintPalette.layoutParams as FrameLayout.LayoutParams).setMargins(
                            450,
                            0,
                            0,
                            0
                        )
                        (floatingActionButtonPaintErase.layoutParams as FrameLayout.LayoutParams).setMargins(
                            150,
                            0,
                            0,
                            0
                        )
                    } else if (event.rawX - (floatingActionButtonPaint.width) < 0) {
                        Log.d("corner", "b")
                        windowManagerParams.x = -(deviceWidth / 2)
                        (floatingActionButtonPaint.layoutParams as FrameLayout.LayoutParams).setMargins(0,0,0,0)
                        (floatingActionButtonPaintSave.layoutParams as FrameLayout.LayoutParams).setMargins(
                            750,
                            0,
                            0,
                            0
                        )
                        (floatingActionButtonPaintBrush.layoutParams as FrameLayout.LayoutParams).setMargins(
                            150,
                            0,
                            0,
                            0
                        )
                        (floatingActionButtonPaintDelete.layoutParams as FrameLayout.LayoutParams).setMargins(
                            450,
                            0,
                            0,
                            0
                        )
                        (floatingActionButtonPaintPalette.layoutParams as FrameLayout.LayoutParams).setMargins(
                            300,
                            0,
                            0,
                            0
                        )
                        (floatingActionButtonPaintErase.layoutParams as FrameLayout.LayoutParams).setMargins(
                            600,
                            0,
                            0,
                            0
                        )
                    }
                    if (deviceHeight < (event.rawY + (floatingActionButtonPaint.height))) {
                        Log.d("corner", "c")
                        windowManagerParams.y = (deviceHeight / 2)
                        (floatingActionButtonPaint.layoutParams as FrameLayout.LayoutParams).setMargins(0,750,0,0)
                        (floatingActionButtonPaintSave.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            0,
                            0,
                            0
                        )
                        (floatingActionButtonPaintBrush.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            600,
                            0,
                            0
                        )
                        (floatingActionButtonPaintDelete.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            300,
                            0,
                            0
                        )
                        (floatingActionButtonPaintPalette.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            450,
                            0,
                            0
                        )
                        (floatingActionButtonPaintErase.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            150,
                            0,
                            0
                        )

                    } else if (event.rawY - (floatingActionButtonPaint.height) < 0) {
                        Log.d("corner", "d")
                        windowManagerParams.y = -(deviceHeight / 2)
                        (floatingActionButtonPaint.layoutParams as FrameLayout.LayoutParams).setMargins(0,0,0,0)
                        (floatingActionButtonPaintSave.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            750,
                            0,
                            0
                        )
                        (floatingActionButtonPaintBrush.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            150,
                            0,
                            0
                        )
                        (floatingActionButtonPaintDelete.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            450,
                            0,
                            0
                        )
                        (floatingActionButtonPaintPalette.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            300,
                            0,
                            0
                        )
                        (floatingActionButtonPaintErase.layoutParams as FrameLayout.LayoutParams).setMargins(
                            0,
                            600,
                            0,
                            0
                        )
                    }
                    lastAction = MotionEvent.ACTION_UP
                    windowManager.updateViewLayout(
                        windowManagerView,
                        windowManagerParams
                    )
                    floatingActionButtonPaint.visibility = View.VISIBLE
                    floatingActionButtonPaintSave.visibility =  View.VISIBLE
                    floatingActionButtonPaintBrush.visibility =  View.VISIBLE
                    floatingActionButtonPaintDelete.visibility =  View.VISIBLE
                    floatingActionButtonPaintPalette.visibility =  View.VISIBLE
                    floatingActionButtonPaintErase.visibility =  View.VISIBLE
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
}