package paint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import constants.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.math.abs


class PaintView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {
    private var mX: Float = 0.toFloat()
    private var mY: Float = 0.toFloat()
    private var mPath: Path? = null
    private val mPaint: Paint = Paint()
    private val undonePaths = ArrayList<FingerPath>()
    private val paths = ArrayList<FingerPath>()
    private var brushColor: Int = 0
    private var brushWidth: Int = 0
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mBitMapPaint = Paint(Paint.DITHER_FLAG)
    private var lastBrushColor: Int = 0
    internal var eraserEnabled = false
    private lateinit var paintView:View
    private val coroutineCallPaint: CoroutineScope = CoroutineScope(Dispatchers.IO)


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
    }

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

    internal fun clearAllPaths() {
        try {
            paths.removeAll(paths)
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mPath = Path()
            invalidate()
            mCanvas = Canvas(mBitmap!!)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

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

    private fun touchUp() {
        try {
            mPath!!.lineTo(mX, mY)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
        }
    }

    internal fun saveImage(filename: String) {
        paintView = this
        val context:Context = this.context
        coroutineCallPaint.async {
            try {
                val bitmap = Bitmap.createBitmap(paintView.width,paintView.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val fileDirectory: File = context.filesDir
                val filePath = File(
                    fileDirectory,
                    "loggerbird_screenshot_"+System.currentTimeMillis().toString()+"_"+filename+".png"
                )
                val os = FileOutputStream(filePath)
                paintView.draw(canvas)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                withContext(Dispatchers.IO) {
                    os.flush()
                    os.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintViewTag)
            }
        }
    }

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

    internal fun getBrushColor(): Int {
        return brushColor
    }

    internal fun setBrushColor(color: Int) {
        this.brushColor = color
    }

    internal fun getBrushWidth(): Int {
        return brushWidth
    }

    internal fun setBrushWidth(width: Int) {
        this.brushWidth = width
    }

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

