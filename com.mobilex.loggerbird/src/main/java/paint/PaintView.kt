package paint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird
import services.LoggerBirdService
import java.io.File
import java.io.FileOutputStream
import java.util.*
import models.paint.*
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * This class is used for defining parameter and functions of canvas and drawing activity.
 * @param context is for getting reference from the application context.
 * @param attrs is for getting reference of current activity in the application.
 */
internal class PaintView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {
    private var mX: Float = 0.toFloat()
    private var mY: Float = 0.toFloat()
    private var mPath: Path? = null
    private val mPaint: Paint = Paint()
    private val undonePaths = ArrayList<FingerPath>()
    private val paths = ArrayList<FingerPath>()
    private var brushWidth: Int = 0
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mBitMapPaint = Paint(Paint.DITHER_FLAG)
    private var lastBrushColor: Int = 0
    private val coroutineCallPaint: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var paintView:View
    internal var brushColor: Int = 0
    internal var eraserEnabled = false

    init {
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = DEFAULT_BRUSH_COLOR
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.xfermode = null
        mPaint.alpha = 0xff
    }

    companion object {
        var BRUSH_SIZE = 10
        const val DEFAULT_BRUSH_COLOR = Color.BLACK
        private const val TOUCH_TOLERANCE = 4.0f
        internal val arrayListFileNameScreenshot:ArrayList<String> = ArrayList()
        internal lateinit var filePathScreenShot : File
        internal fun controlScreenShotFile(): Boolean {
            if (this::filePathScreenShot.isInitialized) {
                return true
            }
            return false
        }
    }

    /**
     * This method is used for handling x and y axis for touch event while moving drawing.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun init(metrics: DisplayMetrics) {
        try {
            undonePaths.clear()
            val height = metrics.heightPixels
            val width = metrics.widthPixels
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap!!)
            brushColor = DEFAULT_BRUSH_COLOR
            brushWidth = BRUSH_SIZE
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

    /**
     * This method is used for clearing all draw paths and invalidate canvas.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun clearAllPaths() {
        try {
            paths.removeAll(paths)
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mPath = Path()
            mCanvas = Canvas(mBitmap!!)
            invalidate()

        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

    /**
     * This method is used for clearing current draw paths and invalidate canvas.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun clear() {
        try {
            paths.clear()
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

    /**
     * This method an override method to draw a canvas.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    override fun onDraw(canvas: Canvas) {
        try {
            canvas.save()
            for (fp in paths) {
                mPaint.color = fp.color
                mPaint.strokeWidth = fp.strokeWidth.toFloat()
                mPaint.maskFilter = null
                mCanvas!!.drawPath(fp.path!!, mPaint)
            }
            canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitMapPaint)
            canvas.restore()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

    /**
     * This method is used for handling x and y axis for touch event while starting drawing.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun touchStart(x: Float, y: Float) {
        try {
            mPath = Path()
            val fp = FingerPath(brushColor, brushWidth, mPath)
            paths.add(fp)
            mPath!!.reset()
            mPath!!.moveTo(x, y)
            mX = x
            mY = y
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

    /**
     * This method is used for handling x and y axis for touch event while moving drawing.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun touchMove(x: Float, y: Float) {
        try {
            val dx = abs(x - mX)
            val dy = abs(y - mY)
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
                mX = x
                mY = y
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

    /**
     * This method is used for handling x and y axis for touch up event while drawing.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun touchUp() {
        try {
            mPath!!.lineTo(mX, mY)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

    /**
     * This method is used to save image into local storage of device.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun saveImage(filename: String) {
        paintView = this
        val context:Context = this.context
        coroutineCallPaint.async {
            try {
                val bitmap = Bitmap.createBitmap(paintView.width,paintView.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val fileDirectory: File = context.filesDir
                filePathScreenShot = File(fileDirectory, "loggerbird_screenshot_"+System.currentTimeMillis().toString()+"_"+filename+".png")
                LoggerBirdService.arrayListFile.add(filePathScreenShot)
                val os = FileOutputStream(filePathScreenShot)
                paintView.draw(canvas)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                withContext(Dispatchers.IO) {
                    os.flush()
                    os.close()
                }
                arrayListFileNameScreenshot.add(filePathScreenShot.absolutePath)
                LoggerBirdService.callShareView(filePathMedia = filePathScreenShot)
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
            }
        }
    }

    /**
     * This method is used for defining parameters of motion event.
     * @return boolean value of motion event
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        invalidate()
        return true
    }

    /**
     * This method is a getter method that returns current brush color.
     * @return value of current brush color
     */
    fun getBrushColor(): Int {
        return brushColor
    }

    /**
     * This method is a setter method to set brush color.
     */
    fun setBrushColor(color: Int) {
        this.brushColor = color
    }

    /**
     * This method is a setter method to set brush color.
     * @return value of current brush width
     */
    internal fun getBrushWidth(): Int {
        return brushWidth
    }

    /**
     * This method is used for enabling eraser mode in order to erase drawings in Paint Activity.
     */
    internal fun setBrushWidth(width: Int) {
        this.brushWidth = width
    }

    /**
     * This method is used for enabling eraser mode in order to erase drawings in Paint Activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun enableEraser() {
        try {
            eraserEnabled = true
            lastBrushColor = brushColor
            mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            brushColor = Color.TRANSPARENT
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

    /**
     * This method is used for stopping eraser in order to draw in Paint Activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun disableEraser() {
        try {
            eraserEnabled = false
            mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            brushColor = lastBrushColor
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }
}