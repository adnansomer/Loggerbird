package paint

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import java.io.FileOutputStream
import java.util.ArrayList

class PaintView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var mX: Float = 0.toFloat()
    private var mY: Float = 0.toFloat()
    private var mPath: Path? = null
    private val mPaint: Paint = Paint()
    private val paths = ArrayList<FingerPath>()
    private var brushColor: Int = 0
    private var bgColor = DEFAULT_BG_COLOR
    private var brushWidth: Int = 0
    private var mBitmap: Bitmap? = null
    private var mCanvas: Canvas? = null
    private val mBitMapPaint = Paint(Paint.DITHER_FLAG)
    private var lastBrushColor: Int = 0
    var eraserEnabled = false

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
        val DEFAULT_BRUSH_COLOR = Color.BLACK
        val DEFAULT_BG_COLOR = Color.WHITE
        private val TOUCH_TOLERANCE = 4.0f
    }

    fun init(metrics: DisplayMetrics) {

        val height = metrics.heightPixels
        val width = metrics.widthPixels

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)

        brushColor = DEFAULT_BRUSH_COLOR
        brushWidth = BRUSH_SIZE

    }

    fun clear() {
        bgColor = DEFAULT_BG_COLOR
        paths.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        canvas.save()
        mCanvas!!.drawColor(bgColor)

        for (fp in paths) {
            mPaint.color = fp.color
            mPaint.strokeWidth = fp.strokeWidth.toFloat()
            mPaint.maskFilter = null

            mCanvas!!.drawPath(fp.path!!, mPaint)
        }

        canvas.drawBitmap(mBitmap!!, 0f, 0f, mBitMapPaint)
        canvas.restore()
    }

    private fun touchStart(x: Float, y: Float) {
        mPath = Path()
        val fp = FingerPath(brushColor, brushWidth, mPath)
        paths.add(fp)

        mPath!!.reset()
        mPath!!.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {

        mPath!!.lineTo(mX, mY)
    }

    fun saveImage(filename: String){

        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val dir = Environment.getExternalStorageDirectory().path + "/Pictures/" + filename + ".png"
        try{
            val os = FileOutputStream(dir)
            this.draw(canvas)

            bitmap.compress(Bitmap.CompressFormat.PNG,100, os)
            os.flush()
            os.close()
        }
        catch(e : Exception){
            e.printStackTrace()
        }
    }

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

    fun getBrushColor(): Int{

        return brushColor
    }

    fun setBrushColor(color: Int){

        this.brushColor = color
    }

    fun getBrushWidth(): Int{

        return brushWidth
    }

    fun setBrushWidth(width: Int){

        this.brushWidth = width
    }

    fun enableEraser(){

        eraserEnabled = true
        lastBrushColor = brushColor
        brushColor = DEFAULT_BG_COLOR
    }

    fun disableEraser(){
        eraserEnabled = false
        brushColor = lastBrushColor
    }
}

