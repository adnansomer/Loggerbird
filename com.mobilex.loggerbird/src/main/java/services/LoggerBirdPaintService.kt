package services

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.divyanshu.colorseekbar.ColorSeekBar
import com.google.android.material.snackbar.Snackbar
import com.mobilex.loggerbird.R
import constants.Constants
import kotlinx.android.synthetic.main.activity_paint.*
import kotlinx.android.synthetic.main.activity_paint_save_dialog.view.*
import kotlinx.android.synthetic.main.activity_paint_save_dialog.view.brushWidthSeekText
import kotlinx.android.synthetic.main.activity_paint_seek_view.view.*
import kotlinx.android.synthetic.main.activity_paint_seek_view_color.view.*
import kotlinx.coroutines.*
import listeners.FloatingActionButtonPaintGlobalLayoutListener
import loggerbird.LoggerBird
import java.lang.Exception

internal class LoggerBirdPaintService : Service() {
    private var intentService: Intent? = null
    private lateinit var context: Context
    private lateinit var activity: Activity
    private val REQUEST_WRITE_EXTERNAL = 1
    private lateinit var screenShot: Drawable
    private val coroutineCallPaintActivity: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var windowManager: Any
    private lateinit var windowManagerParams: WindowManager.LayoutParams
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            intentService = intent
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.serviceTag)
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    internal fun initializeActivity(activity: Activity) {
        this.context = activity
        this.activity = activity
            try {
                val rootView: ViewGroup =
                    activity.window.decorView.findViewById(android.R.id.content)
                val view: View
//                val layoutParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
                view = LayoutInflater.from(activity)
                    .inflate(
                        R.layout.activity_paint,
                        rootView,
                        false
                    )
                if (Settings.canDrawOverlays(activity)) {
                    windowManagerParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams(
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT
                        )
                    } else {
                        WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT
                        )
                    }
                    val paintRunnable = Runnable {initializePaintView()}
                    view.viewTreeObserver.addOnGlobalLayoutListener(FloatingActionButtonPaintGlobalLayoutListener(paintRunnable = paintRunnable , view = view))
                    windowManager = activity.getSystemService(Context.WINDOW_SERVICE)!!
                        (windowManager as WindowManager).addView(view, windowManagerParams)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
            }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializePaintView(){
        try {
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            activity.paintView.init(metrics)
            screenShot = convertBitmapToDrawable()
            activity.paintView.background = screenShot
//        paintView.setBackgroundResource(R.drawable.screenshot_1586760803)
            if (Build.VERSION.SDK_INT >= 23) {
                activity.window.navigationBarColor =
                    resources.getColor(R.color.black, theme)
                activity.window.statusBarColor =
                    resources.getColor(R.color.black, theme)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.window.navigationBarColor =
                        resources.getColor(R.color.black)
                    activity.window.statusBarColor = resources.getColor(R.color.black)
                }
            }
            activity.window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        //or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            buttonClicks()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.paintActivityTag)
        }
    }

    private fun convertBitmapToDrawable(): Drawable {
        return BitmapDrawable(activity.resources, LoggerBirdService.screenshotBitmap)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun buttonClicks(): Boolean {
//        val windowManager:View = window.decorView.findViewById(android.R.id.content)
//        paint_floating_action_button.setOnTouchListener(FloatingActionButtonPaintOnTouchListener(
//            windowManager = (windowManager as WindowManager),
//            windowManagerView = paintView,
//            windowManagerParams = paintView.layoutParams,
//            floatingActionButtonPaint = paint_floating_action_button,
//            floatingActionButtonPaintBrush = paint_floating_action_button_brush,
//            floatingActionButtonPaintDelete = paint_floating_action_button_delete,
//            floatingActionButtonPaintErase = paint_floating_action_button_erase,
//            floatingActionButtonPaintPalette = paint_floating_action_button_palette,
//            floatingActionButtonPaintSave = paint_floating_action_button_save
//        ))
        activity.paint_floating_action_button.setOnClickListener {
            activity.paint_floating_action_button.isExpanded = !activity.paint_floating_action_button.isExpanded
            activity.paint_floating_action_button.isActivated = activity.paint_floating_action_button.isExpanded
        }
        activity.paint_floating_action_button_save.setOnClickListener {
            if (requestPermission()) {
                showFileSavingDialog()
            }
        }

        activity.paint_floating_action_button_brush.setOnClickListener {
            showBrushWidthSetterDialog()
        }

        activity.paint_floating_action_button_delete.setOnClickListener {
            showDeleteSnackBar()
        }

        activity.paint_floating_action_button_palette.setOnClickListener {
            showColorChooseDialog()
        }
        activity.paint_floating_action_button_erase.setOnClickListener {
            if (activity.paintView.eraserEnabled) {
                activity.paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                activity.paintView.disableEraser()
            } else {
                activity.paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_red_24dp)
                activity.paintView.enableEraser()
                activity.paintView.clear()
            }

        }

        return true
    }

    private fun showDeleteSnackBar() {
        try {
            val objLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val snackBarDelete = Snackbar.make(
                activity.findViewById(android.R.id.content),
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
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            val snackView: View =
                activity.layoutInflater.inflate(
                    R.layout.activity_paint_save_snackbar,
                    rootView,
                    false
                )
            val messageTextView: TextView =
                snackView.findViewById(R.id.message_text_view) as TextView
            messageTextView.text = "Are you sure you want to delete?"
            val textViewYes: TextView = snackView.findViewById(R.id.snackbar_yes)
            textViewYes.text = "YES"
            textViewYes.setOnClickListener {
                Snackbar.make(it, "Deleted!", Snackbar.LENGTH_SHORT).show()
                activity.paintView.clearAllPaths()
                if (activity.paintView.eraserEnabled) {
                    activity.paintView.disableEraser()
                    activity.paintView.eraserEnabled = false
                    activity.paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                }
            }
            val textViewNo: TextView = snackView.findViewById(R.id.snackbar_no)
            textViewNo.text = "NO"
            textViewNo.setOnClickListener {
                val snackBarNo: Snackbar = Snackbar.make(it, "Cancelled!", Snackbar.LENGTH_SHORT)
                snackBarNo.setAction("Dismiss") {
                    snackBarNo.dismiss()
                }.show()
            }
            layout.addView(snackView, objLayoutParams)
            snackBarDelete.setAction("Dismiss") {
                snackBarDelete.dismiss()
            }.show()
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
                activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
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
            val inflater = LayoutInflater.from(activity)
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            val paintSeekView =
                inflater.inflate(R.layout.activity_paint_seek_view_color, rootView, false)
            val colorSeekBar = paintSeekView.color_seek_bar
            var selectedColor = 0
            colorSeekBar.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
                override fun onColorChangeListener(color: Int) {
                    if (activity.paintView.eraserEnabled) {
                        activity.paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                        activity.paintView.disableEraser()
                    }
                    selectedColor = color
                    paintSeekView.setBackgroundColor(color)

                }
            })
            colorPickerDialog.setPositiveButton("Apply") { dialog, _ ->
                run {
                    activity.paintView.setBrushColor(selectedColor)
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
            val inflater = LayoutInflater.from(activity)
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            val seekView = inflater.inflate(R.layout.activity_paint_seek_view, rootView, false)
            seekView.brushWidthSeek.max = 100
            seekView.brushWidthSeek.progress = activity.paintView.getBrushWidth()
            seekView.brush_increase.setOnClickListener {
                seekView.brushWidthSeek.progress = seekView.brushWidthSeek.progress + 1
                activity.paintView.setBrushWidth(seekView.brushWidthSeek.progress)
            }

            seekView.brush_decrease.setOnClickListener {
                seekView.brushWidthSeek.progress = seekView.brushWidthSeek.progress - 1
                activity.paintView.setBrushWidth(seekView.brushWidthSeek.progress)
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
                    activity.paintView.setBrushWidth(seekView.brushWidthSeek.progress)
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
            val inflater = LayoutInflater.from(activity)
            val saveView = inflater.inflate(R.layout.activity_paint_save_dialog, null)
            var fileName: String
            saveDialog.setView(saveView)
            saveDialog.setPositiveButton("OK") { _, _ ->
                fileName = saveView.paint_save_issue.text.toString()
                activity.paintView.saveImage(fileName)
                val snackBarFileSaving: Snackbar =
                    Snackbar.make(activity.paintView, "Successfully saved!", Snackbar.LENGTH_SHORT)
                snackBarFileSaving.setAction("Dismiss") {
                    snackBarFileSaving.dismiss()
                }.show()
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
}