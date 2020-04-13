package paint

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.divyanshu.colorseekbar.ColorSeekBar
import com.google.android.material.snackbar.Snackbar
import com.mobilex.loggerbird.R
import kotlinx.android.synthetic.main.activity_paint.*
import kotlinx.android.synthetic.main.activity_paint_save_dialog.view.*
import kotlinx.android.synthetic.main.activity_paint_seek_view.view.*
import kotlinx.android.synthetic.main.activity_paint_seek_view.view.brushWidthSeekText
import kotlinx.android.synthetic.main.activity_paint_seek_view_color.view.*


class PaintActivity : Activity() {

    val REQUEST_WRITE_EXTERNAL = 1
    private var bitmap : Bitmap? = null
    private lateinit var screenShot : Drawable

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        paintView.init(metrics)

        screenShot = convertBitmapToDrawable()
        paintView.background = screenShot
        //paintView.setBackgroundResource(R.drawable.screenshot_1586760803)


        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black))
            getWindow().setStatusBarColor(getResources().getColor(R.color.black))
        }

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                //or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun convertBitmapToDrawable() : Drawable{

        val byteArray : ByteArray? = intent.getByteArrayExtra("BitmapScreenshot")
        bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray!!.size)
        val drawable: Drawable = BitmapDrawable(bitmap)

        return drawable
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onStart() {
        super.onStart()

        paint_floating_action_button.setOnClickListener {
            paint_floating_action_button.isExpanded = !paint_floating_action_button.isExpanded
            paint_floating_action_button.isActivated = paint_floating_action_button.isExpanded
        }

        buttonClicks()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun buttonClicks() : Boolean {

        paint_floating_action_button_save.setOnClickListener {
            if(requestPermission()) {
                showFileSavingDialog()
            }
        }

        paint_floating_action_button_brush.setOnClickListener {
            showLineWidthSetterDialog()
        }

        paint_floating_action_button_delete.setOnClickListener {
            showDeleteSnackbar()
        }

        paint_floating_action_button_palette.setOnClickListener {
            showColorChooseDialog()
        }


        paint_floating_action_button_erase.setOnClickListener {
            if(paintView.eraserEnabled) {
                paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                paintView.disableEraser()
            }
            else{
                paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_red_24dp)
                paintView.enableEraser()
                paintView.clear()
            }

        }

        return true
    }

    private fun showDeleteSnackbar() {

        val objLayoutParams : LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val snackbar = Snackbar.make(this.findViewById(android.R.id.content), "delete", Snackbar.LENGTH_INDEFINITE)

        val layout : Snackbar.SnackbarLayout = snackbar.view as Snackbar.SnackbarLayout

        val parentParams : FrameLayout.LayoutParams = layout.layoutParams as FrameLayout.LayoutParams
        parentParams.setMargins(0,0,0,-50)
        layout.setLayoutParams(parentParams)
        layout.setPadding(0, 0, 0, -50)
        layout.setLayoutParams(parentParams)

        val snackView : View = layoutInflater.inflate(R.layout.activity_paint_save_snackbar,null)

        val messageTextView : TextView =  snackView.findViewById(R.id.message_text_view) as TextView
        messageTextView.setText("Are you sure you want to delete?")

        val textViewOne : TextView = snackView.findViewById(R.id.snackbar_yes)
        textViewOne.setText("YES")

        textViewOne.setOnClickListener {

            Snackbar.make(it, "Deleted!", Snackbar.LENGTH_SHORT).show()
            paintView.clearAllPaths()

            if(paintView.eraserEnabled){
                paintView.disableEraser()
                paintView.eraserEnabled = false
                paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
            }
        }

        val textViewTwo : TextView = snackView.findViewById(R.id.snackbar_no)
        textViewTwo.setText("NO")

        textViewTwo.setOnClickListener {
            Snackbar.make(it, "Cancelled!", Snackbar.LENGTH_SHORT)
                .show()
        }

        layout.addView(snackView, objLayoutParams)
        snackbar.show()
    }

    private fun requestPermission() : Boolean {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL
            )
        }
        return true
    }

    private fun showColorChooseDialog() {

        val colorPickerDialog = AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        val inflater = LayoutInflater.from(this@PaintActivity)
        val paintSeekView = inflater.inflate(R.layout.activity_paint_seek_view_color, null)

        val colorSeekBar = paintSeekView.color_seek_bar
        var selectedColor : Int = 0

        colorSeekBar.setOnColorChangeListener(object: ColorSeekBar.OnColorChangeListener{
            override fun onColorChangeListener(color: Int) {
                if(paintView.eraserEnabled){
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

    }

    private fun showLineWidthSetterDialog(){

        val lineWidthDialog = AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK)

        val inflater = LayoutInflater.from(this@PaintActivity)
        val seekView = inflater.inflate(R.layout.activity_paint_seek_view, null)

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

        seekView.brushWidthSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
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
    }


    private fun showFileSavingDialog(){

        val saveDialog = AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        val inflater = LayoutInflater.from(this@PaintActivity)
        val saveView = inflater.inflate(R.layout.activity_paint_save_dialog, null)

        var fileName: String

        saveDialog.setView(saveView)

        saveDialog.setPositiveButton("OK") { _, _ -> fileName = saveView.paint_save_issue.text.toString()
            paintView.saveImage(fileName)
            Snackbar.make(paintView, "Successfully saved!", Snackbar.LENGTH_SHORT).show()
        }

        saveDialog.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        saveDialog.show()

    }
}