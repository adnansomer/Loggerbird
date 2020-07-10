package paint

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import com.divyanshu.colorseekbar.ColorSeekBar
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
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
import listeners.floatingActionButtons.FloatingActionButtonPaintOnTouchListener
import loggerbird.LoggerBird
import services.LoggerBirdService
import java.util.concurrent.TimeUnit

/**
 * This class is used for defining Paint Activity life-cycle and its methods.
 */
internal class PaintActivity : Activity() {
    private val REQUEST_WRITE_EXTERNAL = 1
    private lateinit var screenShot: Drawable
    private val coroutineCallPaintActivity: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var controlButtonVisibility: Boolean = true
    private var onStopCalled = false
    private var pipModeChange = false
    private var lastTime: Long = 0
    private lateinit var arrayListFileName:ArrayList<String>

    companion object {
        private lateinit var activity: Activity
        internal var controlPaintInPictureState:Boolean = false
        internal fun closeActivitySession() {
            if (Companion::activity.isInitialized) {
                activity.finish()
                activity.overridePendingTransition(R.anim.no_animation,R.anim.slide_in_bottom)
            }
        }
    }

    /**
     * This method converts bitmap to drawable.
     * @return BitmapDrawable is for obtaining Bitmap value of drawable
     */
    private fun convertBitmapToDrawable(): Drawable {
        return BitmapDrawable(resources, LoggerBirdService.screenshotBitmap)
    }

    /**
     * This method is used for defining margins of floating action button in Paint Activity..
     */
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

    /**
     * This method is used for defining button click methods.
     */
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    /**
     * This method creates custom snackbar for showing a dialog to user whether delete drawing.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun showDeleteSnackBar() {
        try {
            val objLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val snackBarDelete = Snackbar.make(this.findViewById(android.R.id.content), R.string.snackbar_delete, Snackbar.LENGTH_INDEFINITE)
            val layout: Snackbar.SnackbarLayout = snackBarDelete.view as Snackbar.SnackbarLayout
            val parentParams: FrameLayout.LayoutParams = layout.layoutParams as FrameLayout.LayoutParams
            parentParams.setMargins(0, 0, 0, -50)
            layout.layoutParams = parentParams
            layout.setPadding(0, 0, 0, -50)
            layout.layoutParams = parentParams
            val rootView: ViewGroup = window.decorView.findViewById(android.R.id.content)
            val snackView: View = layoutInflater.inflate(R.layout.activity_paint_save_snackbar, rootView, false)
            val messageTextView: TextView = snackView.findViewById(R.id.message_text_view) as TextView
            messageTextView.text = resources.getString(R.string.snackbar_delete_verification)

            val textViewYes: TextView = snackView.findViewById(R.id.snackbar_yes)
            textViewYes.text = resources.getString(R.string.snackbar_yes)
            textViewYes.setSafeOnClickListener {
                val snackbarYes: Snackbar = Snackbar.make(it, resources.getString(R.string.snackbar_delete_success), Snackbar.LENGTH_SHORT)
                snackbarYes.setAction(resources.getString(R.string.snackbar_dismiss)) { snackbarYes.dismiss() }.show()
                paintView.clearAllPaths()
                if (paintView.eraserEnabled) {
                    paintView.disableEraser()
                    paintView.eraserEnabled = false
                    paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                }
            }

            val textViewNo: TextView = snackView.findViewById(R.id.snackbar_no)
            textViewNo.text =  resources.getString(R.string.snackbar_no)
            textViewNo.setSafeOnClickListener {
                val snackBarNo: Snackbar = Snackbar.make(it,  resources.getString(R.string.snackbar_cancelled), Snackbar.LENGTH_SHORT)
                snackBarNo.setAction(resources.getString(R.string.snackbar_dismiss)) {
                    snackBarNo.dismiss()
                }.show()
            }
            layout.addView(snackView, objLayoutParams)
            snackBarDelete.setAction(resources.getString(R.string.snackbar_dismiss)) {
                snackBarDelete.dismiss()
            }.show()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    /**
     * This method is used for blocking consecutive clicks in order to prevent spam.
     */
    @SuppressLint("CheckResult")
    fun View.setSafeOnClickListener(onClick: (View) -> Unit) {
        RxView.clicks(this).throttleFirst(2000, TimeUnit.MILLISECONDS).subscribe {
            onClick(this)
        }
    }

    /**
     * This method is used for requesting permission to save drawing into local storage of device.
     * @return permission granted or deny
     */
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

    /**
     * This method is used for changing brush color in order to draw with different colors.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
            val paintSeekView = inflater.inflate(R.layout.activity_paint_seek_view_color, rootView, false)
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
            colorPickerDialog.setPositiveButton(resources.getString(R.string.snackbar_apply)) { dialog, _ ->
                run {
                    if(selectedColor == 0){
                        selectedColor = paintView.brushColor
                    }
                    paintView.setBrushColor(selectedColor)
                    dialog.dismiss()
                }
            }
            colorPickerDialog.setNegativeButton(resources.getString(R.string.snackbar_cancel)) { dialog, _ -> dialog.dismiss() }
            colorPickerDialog.setView(paintSeekView)
            colorPickerDialog.create()
            colorPickerDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    /**
     * This method is used for changing width of brush in order to draw thicker or thinner.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                    val currentWidth:String = resources.getText(R.string.snackbar_current_width).toString() + i + "%"
                    seekView.brushWidthSeekText.text = currentWidth
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    seekView.brushWidthSeekText.text = resources.getText(R.string.snackbar_brush_width_adjusting)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    seekView.brushWidthSeekText.text = resources.getText(R.string.snackbar_brush_width_adjust)
                }
            })
            lineWidthDialog.setView(seekView)
            lineWidthDialog.setPositiveButton(resources.getText(R.string.snackbar_apply)) { dialog, _ ->
                run {
                    dialog.dismiss()
                    paintView.setBrushWidth(seekView.brushWidthSeek.progress)
                }
            }
            lineWidthDialog.setNegativeButton(resources.getText(R.string.snackbar_cancel)) { dialog, _ -> dialog.dismiss() }
            lineWidthDialog.create()
            lineWidthDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    /**
     * This method is used for saving drawing of user to share third party tools.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun showFileSavingDialog() {
        try {
            val saveDialog = AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            val inflater = LayoutInflater.from(this@PaintActivity)
            val saveView = inflater.inflate(R.layout.activity_paint_save_dialog, null)
            var fileName: String
            saveDialog.setView(saveView)
            saveDialog.setPositiveButton(resources.getText(R.string.snackbar_ok)) { _, _ ->
                fileName = saveView.paint_save_issue.text.toString()
                paintView.saveImage(fileName)
                val snackBarFileSaving: Snackbar = Snackbar.make(paintView, "Successfully saved!", Snackbar.LENGTH_SHORT)
                snackBarFileSaving.setAction("Dismiss") {
                    snackBarFileSaving.dismiss()
                }.show()
                Toast.makeText(activity,  resources.getText(R.string.snackbar_successfully_saved), Toast.LENGTH_SHORT).show()
                finish()
                overridePendingTransition(R.anim.no_animation,R.anim.slide_in_bottom)
                if(pipModeChange){
                    Toast.makeText(activity,  resources.getText(R.string.snackbar_successfully_saved), Toast.LENGTH_SHORT).show()
                    finish()
                    overridePendingTransition(R.anim.slide_in_bottom,R.anim.no_animation)
                }
            }
            saveDialog.setNegativeButton(
                R.string.snackbar_cancel
            ) { dialog, _ -> dialog.cancel() }
            saveDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    /**
     * This method controls all animation and image changes on floating action button and its child buttons of Paint Activity.
     */
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

    /**
     * This method minimize Paint Activity to picture in picture mode.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun pictureInPictureMode() {
        controlPaintInPictureState = true
        coroutineCallPaintActivity.async {
            try {
                val aspectRatio = Rational(9, 16)
                val mPictureInPictureParamsBuilder = PictureInPictureParams.Builder()
                mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio)
                enterPictureInPictureMode(mPictureInPictureParamsBuilder.build())
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
            }
        }
    }

    /**
     * This method controls activity actions and floating action button behavior when user pushes the back button
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onBackPressed() {
        super.onBackPressed()
        LoggerBirdService.floating_action_button.clearAnimation()
        LoggerBirdService.floating_action_button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
        Toast.makeText(activity, R.string.drawing_cancelled_message, Toast.LENGTH_SHORT).show()
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
        finish()
        fabScreenshotAnimation()
        if(pipModeChange){
            Toast.makeText(activity,  R.string.drawing_cancelled_message, Toast.LENGTH_SHORT).show()
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
            finish()
            fabScreenshotAnimation()
        }
    }

    /**
     * This method gives animation and changes floating action button image in order to action of user in Paint Activity.
     * @param isInPictureInPictureMode is for obtaining the determine whether activity is picture in picture mode.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean
    ) {

        try{
            if (isInPictureInPictureMode) {
                LoggerBirdService.floating_action_button.setImageResource(R.drawable.ic_photo_camera_black_24dp)
                pipModeChange = true
                paint_floating_action_button.visibility = View.GONE
                paint_floating_action_button_save.visibility = View.GONE
                paint_floating_action_button_back.visibility = View.GONE
                paint_floating_action_button_brush.visibility = View.GONE
                paint_floating_action_button_delete.visibility = View.GONE
                paint_floating_action_button_palette.visibility = View.GONE
                paint_floating_action_button_erase.visibility = View.GONE
            } else {
                if (onStopCalled) {
                    val current = System.currentTimeMillis()
                    if ((current - lastTime) > 2500L) {
                        finish()
                        lastTime = current

                    }
                    fabScreenshotAnimation()
                }
                pipModeChange = true
                paint_floating_action_button.visibility = View.VISIBLE
                paint_floating_action_button_save.visibility = View.VISIBLE
                paint_floating_action_button_back.visibility = View.VISIBLE
                paint_floating_action_button_brush.visibility = View.VISIBLE
                paint_floating_action_button_delete.visibility = View.VISIBLE
                paint_floating_action_button_palette.visibility = View.VISIBLE
                paint_floating_action_button_erase.visibility = View.VISIBLE
            }
        }catch(e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }


    /**
     * This method gives animation and changes floating action button image in order to action of user in Paint Activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun fabScreenshotAnimation(){
        LoggerBirdService.floating_action_button.clearAnimation()
        LoggerBirdService.floating_action_button.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
        LoggerBirdService.floating_action_button.animate()
            .rotationBy(360F)
            .setDuration(200)
            .scaleX(1F)
            .scaleY(1F)
            .withEndAction {
                LoggerBirdService.floating_action_button.setImageResource(R.drawable.loggerbird)
                LoggerBirdService.floating_action_button.animate()
                    .rotationBy(0F)
                    .setDuration(200)
                    .scaleX(1F)
                    .scaleY(1F)
                    .start()
            }
            .start()
    }

    /**
     * This method creates custom snackbar for showing a dialog to user whether delete drawing.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                if (Build.VERSION.SDK_INT >= 23) {
                    window.navigationBarColor = resources.getColor(R.color.black, theme)
                    window.statusBarColor = resources.getColor(R.color.black, theme)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.navigationBarColor = resources.getColor(R.color.black)
                        window.statusBarColor = resources.getColor(R.color.black)
                    }
                }
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
            }
        }
    }

    /**
     * This method is lifecycle onStart method of Paint Activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

    /**
     * This method returns onResume state of activity lifecycle of Paint Activity.
     */
    override fun onResume() {
        super.onResume()
        if(LoggerBirdService.controlFloatingActionButtonView()){
            LoggerBirdService.floatingActionButtonView.visibility = View.GONE
        }
    }

    /**
     * This method returns onPause state of activity lifecycle of Paint Activity.
     */
    override fun onPause() {
        super.onPause()
        LoggerBirdService.floatingActionButtonView.visibility = View.VISIBLE
    }

    /**
     * This method returns onStop state of activity lifecycle of Paint Activity.
     */
    override fun onStop() {
        super.onStop()
        onStopCalled = true
        if(LoggerBirdService.controlFloatingActionButtonView()){
            LoggerBirdService.floatingActionButtonView.visibility = View.VISIBLE
        }
    }

    /**
     * This method returns onDestroy state of activity lifecycle of Paint Activity.
     */
    override fun onDestroy() {
        super.onDestroy()
        LoggerBirdService.screenshotDrawing = false
        controlPaintInPictureState = false
    }
}