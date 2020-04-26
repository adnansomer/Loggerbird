package paint

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import com.divyanshu.colorseekbar.ColorSeekBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mobilex.loggerbird.R
import constants.Constants
import kotlinx.android.synthetic.main.activity_paint.*
import kotlinx.android.synthetic.main.activity_paint_save_dialog.view.*
import kotlinx.android.synthetic.main.activity_paint_seek_view.view.*
import kotlinx.android.synthetic.main.activity_paint_seek_view.view.brushWidthSeekText
import kotlinx.android.synthetic.main.activity_paint_seek_view_color.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import listeners.FloatingActionButtonPaintOnTouchListener
import loggerbird.LoggerBird
import services.LoggerBirdService


class PaintActivity : Activity() {
    private val REQUEST_WRITE_EXTERNAL = 1
    private lateinit var screenShot: Drawable
    private val coroutineCallPaintActivity: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var controlButtonVisibility: Boolean = true
    private var onStopCalled = false
    companion object{
        private lateinit var activity:Activity
        internal fun closeActivitySession(){
            if(Companion::activity.isInitialized){
                activity.finish()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)
        activity = this
        coroutineCallPaintActivity.async {
            try {
                val metrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(metrics)
                paintView.init(metrics)
                screenShot = convertBitmapToDrawable()
                paintView.background = screenShot
                //paintView.setBackgroundResource(R.drawable.screenshot_aura)
                if (Build.VERSION.SDK_INT >= 23) {
                    window.navigationBarColor = resources.getColor(R.color.black, theme)
                    window.statusBarColor = resources.getColor(R.color.black, theme)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.navigationBarColor = resources.getColor(R.color.black)
                        window.statusBarColor = resources.getColor(R.color.black)
                    }
                }
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                //or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                        //or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
            }
        }
    }

    private fun convertBitmapToDrawable(): Drawable {
        return BitmapDrawable(resources, LoggerBirdService.screenshotBitmap)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onStart() {
        super.onStart()
        try {
            LoggerBirdService.floatingActionButtonView.visibility = View.GONE
            setButtonDefaultMargins()
            buttonClicks()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    private fun setButtonDefaultMargins() {
        (paint_floating_action_button.layoutParams as CoordinatorLayout.LayoutParams).setMargins(
            0,
            0,
            0,
            150
        )
        (paint_floating_action_button_brush.layoutParams as CoordinatorLayout.LayoutParams).setMargins(
            0,
            0,
            0,
            300
        )
        (paint_floating_action_button_palette.layoutParams as CoordinatorLayout.LayoutParams).setMargins(
            0,
            0,
            0,
            450
        )
        (paint_floating_action_button_delete.layoutParams as CoordinatorLayout.LayoutParams).setMargins(
            0,
            0,
            0,
            600
        )
        (paint_floating_action_button_erase.layoutParams as CoordinatorLayout.LayoutParams).setMargins(
            0,
            0,
            0,
            750
        )
        (paint_floating_action_button_back.layoutParams as CoordinatorLayout.LayoutParams).setMargins(
            0,
            0,
            0,
            900
        )
        (paint_floating_action_button_save.layoutParams as CoordinatorLayout.LayoutParams).setMargins(
            0,
            0,
            0,
            1050
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun buttonClicks() {
        paint_floating_action_button.setOnTouchListener(
            FloatingActionButtonPaintOnTouchListener(
                floatingActionButtonPaint = paint_floating_action_button,
                floatingActionButtonPaintSave = paint_floating_action_button_save,
                floatingActionButtonPaintBack = paint_floating_action_button_back,
                floatingActionButtonPaintPalette = paint_floating_action_button_palette,
                floatingActionButtonPaintErase = paint_floating_action_button_erase,
                floatingActionButtonPaintDelete = paint_floating_action_button_delete,
                floatingActionButtonPaintBrush = paint_floating_action_button_brush
            )
        )
        paint_floating_action_button.setOnClickListener {
            animationVisibility()
        }
        paint_floating_action_button_save.setOnClickListener {
            if (requestPermission()) {
                showFileSavingDialog()
            }
        }
        paint_floating_action_button_back.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pictureInPictureMode()
            } else {
                finish()
            }
        }
        paint_floating_action_button_brush.setOnClickListener {
            showBrushWidthSetterDialog()
        }

        paint_floating_action_button_delete.setOnClickListener {
            showDeleteSnackBar()
        }

        paint_floating_action_button_palette.setOnClickListener {
            showColorChooseDialog()
        }
        paint_floating_action_button_erase.setOnClickListener {
            if (paintView.eraserEnabled) {
                paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                paintView.disableEraser()
            } else {
                paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_red_24dp)
                paintView.enableEraser()
                paintView.clear()

            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pictureInPictureMode() {
        // Calculate the aspect ratio of the PiP screen.
        val aspectRatio = Rational(9,16)
        val mPictureInPictureParamsBuilder = PictureInPictureParams.Builder()
        mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio)
        enterPictureInPictureMode(mPictureInPictureParamsBuilder.build())
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {

        if (isInPictureInPictureMode) {

            paint_floating_action_button.visibility = View.GONE
            paint_floating_action_button_save.visibility = View.GONE
            paint_floating_action_button_back.visibility = View.GONE
            paint_floating_action_button_brush.visibility = View.GONE
            paint_floating_action_button_delete.visibility = View.GONE
            paint_floating_action_button_palette.visibility = View.GONE
            paint_floating_action_button_erase.visibility = View.GONE
        } else {
            //Restore ui
            if (onStopCalled) {
                finish()
            }
            paint_floating_action_button.visibility = View.VISIBLE
            paint_floating_action_button_save.visibility = View.VISIBLE
            paint_floating_action_button_back.visibility = View.VISIBLE
            paint_floating_action_button_brush.visibility = View.VISIBLE
            paint_floating_action_button_delete.visibility = View.VISIBLE
            paint_floating_action_button_palette.visibility = View.VISIBLE
            paint_floating_action_button_erase.visibility = View.VISIBLE
        }
    }

    private fun showDeleteSnackBar() {
        try {
            val objLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val snackBarDelete = Snackbar.make(
                this.findViewById(android.R.id.content),
                "delete",
                Snackbar.LENGTH_INDEFINITE
            )
            val layout: Snackbar.SnackbarLayout = snackBarDelete.view as Snackbar.SnackbarLayout
            val parentParams: FrameLayout.LayoutParams =
                layout.layoutParams as FrameLayout.LayoutParams
            parentParams.setMargins(0, 0, 0, -50)
            layout.layoutParams = parentParams
            layout.setPadding(0, 0, 0, -50)
            layout.layoutParams = parentParams
            val rootView: ViewGroup = window.decorView.findViewById(android.R.id.content)
            val snackView: View = layoutInflater.inflate(R.layout.activity_paint_save_snackbar, rootView, false)

            val textViewYes: TextView = snackView.findViewById(R.id.snackbar_yes)
            textViewYes.setOnClickListener {
                paintView.clearAllPaths()
                if (paintView.eraserEnabled) {
                    paintView.disableEraser()
                    paintView.eraserEnabled = false
                    paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                }

                val snackbarYes : Snackbar = Snackbar.make(it, "Deleted!", Snackbar.LENGTH_SHORT)
                snackbarYes.setAction("Dismiss") {
                    snackbarYes.dismiss()
                }.show()
            }

            val textViewNo: TextView = snackView.findViewById(R.id.snackbar_no)
            textViewNo.setOnClickListener {
                val snackBarNo: Snackbar = Snackbar.make(it, "Cancelled!", Snackbar.LENGTH_SHORT)
                snackBarNo.setAction("Dismiss") {
                    snackBarNo.dismiss()
                }.show()
            }

            layout.addView(snackView, objLayoutParams)
            snackBarDelete.show()

        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    private fun requestPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL
            )
        }
        return true
    }

    private fun showColorChooseDialog() {
        try {
            val colorPickerDialog =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                } else {
                    AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                }
            val inflater = LayoutInflater.from(this@PaintActivity)
            val rootView: ViewGroup = window.decorView.findViewById(android.R.id.content)
            val paintSeekView =
                inflater.inflate(R.layout.activity_paint_seek_view_color, rootView, false)
            val colorSeekBar = paintSeekView.color_seek_bar
            var selectedColor = 0
            colorSeekBar.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
                override fun onColorChangeListener(color: Int) {
                    if (paintView.eraserEnabled) {
                        paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                        paintView.disableEraser()
                    }
                    selectedColor = color
                    paintSeekView.setBackgroundColor(color)

                }
            })
            colorPickerDialog.setPositiveButton("Apply") { dialog, _ ->
                run {
                    paintView.setBrushColor(selectedColor)
                    dialog.dismiss()
                }
            }
            colorPickerDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            colorPickerDialog.setView(paintSeekView)
            colorPickerDialog.create()
            colorPickerDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    private fun showBrushWidthSetterDialog() {
        try {
            val lineWidthDialog =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                } else {
                    AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                }
            val inflater = LayoutInflater.from(this@PaintActivity)
            val rootView: ViewGroup = window.decorView.findViewById(android.R.id.content)
            val seekView = inflater.inflate(R.layout.activity_paint_seek_view, rootView, false)
            seekView.brushWidthSeek.max = 100
            seekView.brushWidthSeek.progress = paintView.getBrushWidth()
            seekView.brush_increase.setOnClickListener {
                seekView.brushWidthSeek.progress = seekView.brushWidthSeek.progress + 1
                paintView.setBrushWidth(seekView.brushWidthSeek.progress)
            }

            seekView.brush_decrease.setOnClickListener {
                seekView.brushWidthSeek.progress = seekView.brushWidthSeek.progress - 1
                paintView.setBrushWidth(seekView.brushWidthSeek.progress)
            }

            seekView.brushWidthSeek.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    seekView.brushWidthSeekText.text = "Current width : $i%"

                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    seekView.brushWidthSeekText.text = "Brush width is adjusting"
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    seekView.brushWidthSeekText.text = "Adjust your brush width"
                }
            })
            lineWidthDialog.setView(seekView)
            lineWidthDialog.setPositiveButton("Apply") { dialog, _ ->
                run {
                    dialog.dismiss()
                    paintView.setBrushWidth(seekView.brushWidthSeek.progress)
                }
            }
            lineWidthDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            lineWidthDialog.create()
            lineWidthDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    private fun showFileSavingDialog() {
        try {
            val saveDialog = AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            val inflater = LayoutInflater.from(this@PaintActivity)
            val saveView = inflater.inflate(R.layout.activity_paint_save_dialog, null)
            var fileName: String
            saveDialog.setView(saveView)
            saveDialog.setPositiveButton("OK") { _, _ ->
                fileName = saveView.paint_save_issue.text.toString()
                paintView.saveImage(fileName)
                val snackBarFileSaving: Snackbar =
                    Snackbar.make(paintView, "Successfully saved!", Snackbar.LENGTH_SHORT)
                snackBarFileSaving.setAction("Dismiss") {
                    snackBarFileSaving.dismiss()
                }.show()
                finish()
            }
            saveDialog.setNegativeButton(
                "Cancel"
            ) { dialog, _ -> dialog.cancel() }
            saveDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    override fun onPause() {
        super.onPause()
        LoggerBirdService.floatingActionButtonView.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        onStopCalled = true
        LoggerBirdService.floatingActionButtonView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        LoggerBirdService.floatingActionButtonView.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        LoggerBirdService.floatingActionButtonView.visibility = View.VISIBLE
    }

    private fun animationVisibility() {
        if (!controlButtonVisibility) {
            controlButtonVisibility = true
            paint_floating_action_button_brush.animate().rotation(-360F)
            paint_floating_action_button_brush.animate().duration = 400L
            paint_floating_action_button_brush.animate().start()
            paint_floating_action_button_palette.animate().rotation(-360F)
            paint_floating_action_button_palette.animate().duration = 400L
            paint_floating_action_button_palette.animate().start()
            paint_floating_action_button_delete.animate().rotation(-360F)
            paint_floating_action_button_delete.animate().duration = 400L
            paint_floating_action_button_delete.animate().start()
            paint_floating_action_button_erase.animate().rotation(-360F)
            paint_floating_action_button_erase.animate().duration = 400L
            paint_floating_action_button_erase.animate().start()
            paint_floating_action_button_back.animate().rotation(-360F)
            paint_floating_action_button_back.animate().duration = 400L
            paint_floating_action_button_back.animate().start()
            paint_floating_action_button_save.animate().rotation(-360F)
            paint_floating_action_button_save.animate().duration = 400L
            paint_floating_action_button_save.animate().start()
            paint_floating_action_button_brush.visibility = View.GONE
            paint_floating_action_button_palette.visibility = View.GONE
            paint_floating_action_button_delete.visibility = View.GONE
            paint_floating_action_button_erase.visibility = View.GONE
            paint_floating_action_button_back.visibility = View.GONE
            paint_floating_action_button_save.visibility = View.GONE
            paint_floating_action_button.setImageResource(R.drawable.ic_add_white_24dp)
        } else {
            controlButtonVisibility = false
            paint_floating_action_button_brush.visibility = View.VISIBLE
            paint_floating_action_button_brush.animate().rotation(360F)
            paint_floating_action_button_brush.animate().duration = 400L
            paint_floating_action_button_brush.animate().start()
            paint_floating_action_button_palette.visibility = View.VISIBLE
            paint_floating_action_button_palette.animate().rotation(360F)
            paint_floating_action_button_palette.animate().duration = 400L
            paint_floating_action_button_palette.animate().start()
            paint_floating_action_button_delete.visibility = View.VISIBLE
            paint_floating_action_button_delete.animate().rotation(360F)
            paint_floating_action_button_delete.animate().duration = 400L
            paint_floating_action_button_delete.animate().start()
            paint_floating_action_button_erase.visibility = View.VISIBLE
            paint_floating_action_button_erase.animate().rotation(360F)
            paint_floating_action_button_erase.animate().duration = 400L
            paint_floating_action_button_erase.animate().start()
            paint_floating_action_button_back.visibility = View.VISIBLE
            paint_floating_action_button_back.animate().rotation(360F)
            paint_floating_action_button_back.animate().duration = 400L
            paint_floating_action_button_back.animate().start()
            paint_floating_action_button_save.visibility = View.VISIBLE
            paint_floating_action_button_save.animate().rotation(360F)
            paint_floating_action_button_save.animate().duration = 400L
            paint_floating_action_button_save.animate().start()
            paint_floating_action_button.setImageResource(R.drawable.ic_close_black_24dp)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}