package paint

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
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
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.mobilex.loggerbird.R
import kotlinx.android.synthetic.main.activity_paint.*
import kotlinx.android.synthetic.main.activity_paint.view.*
import kotlinx.android.synthetic.main.brush_width_layout.view.*
import yuku.ambilwarna.AmbilWarnaDialog


class PaintActivity : Activity() {

    val REQUEST_WRITE_EXTERNAL = 1
    private var isOpen = false
    private var bitmap : Bitmap? = null


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        paintView.init(metrics)

        val byteArray : ByteArray? = intent.getByteArrayExtra("BitmapScreenshot")
        bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray!!.size)
        val drawable: Drawable = BitmapDrawable(bitmap)

        denemeview.background = drawable


    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onStart() {
        super.onStart()

        buttonClicks()

        paint_floating_action_button.setOnClickListener {
            animationVisibility()
        }

    }

    private fun buttonClicks() : Boolean {
        paint_floating_action_button_save.setOnClickListener {
            requestPermission()
            showFileSavingDialog()
        }

        paint_floating_action_button_brush.setOnClickListener {
            showLineWidthSetterDialog()
        }

        paint_floating_action_button_delete.setOnClickListener {
            showClearConfirmationDialog()
        }

        paint_floating_action_button_palette.setOnClickListener {
            showColorPickerDialog()

        }

        paint_floating_action_button_erase.setOnClickListener {
            if(paintView.eraserEnabled) {
                paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                paintView.disableEraser()
            }
            else{
                paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_red_24dp)
                paintView.enableEraser()
            }

        }

        return true
    }

    private fun animationVisibility() {
        if (isOpen) {
            isOpen = false

            paint_floating_action_button_brush.animate().alphaBy(1.0F)
            paint_floating_action_button_brush.animate().alpha(0.0F)
            paint_floating_action_button_brush.animate().scaleXBy(1.0F)
            paint_floating_action_button_brush.animate().scaleX(0.0F)
            paint_floating_action_button_brush.animate().scaleYBy(1.0F)
            paint_floating_action_button_brush.animate().scaleY(0.0F)
            paint_floating_action_button_brush.animate().rotation(360F)
            paint_floating_action_button_brush.animate().setDuration(400L)
            paint_floating_action_button_brush.animate().start()

            paint_floating_action_button_delete.animate().alphaBy(1.0F)
            paint_floating_action_button_delete.animate().alpha(0.0F)
            paint_floating_action_button_delete.animate().scaleXBy(1.0F)
            paint_floating_action_button_delete.animate().scaleX(0.0F)
            paint_floating_action_button_delete.animate().scaleYBy(1.0F)
            paint_floating_action_button_delete.animate().scaleY(0.0F)
            paint_floating_action_button_delete.animate().rotation(360F)
            paint_floating_action_button_delete.animate().setDuration(400L)
            paint_floating_action_button_delete.animate().start()

            paint_floating_action_button_erase.animate().alphaBy(1.0F)
            paint_floating_action_button_erase.animate().alpha(0.0F)
            paint_floating_action_button_erase.animate().scaleXBy(1.0F)
            paint_floating_action_button_erase.animate().scaleX(0.0F)
            paint_floating_action_button_erase.animate().scaleYBy(1.0F)
            paint_floating_action_button_erase.animate().scaleY(0.0F)
            paint_floating_action_button_erase.animate().rotation(360F)
            paint_floating_action_button_erase.animate().setDuration(400L)
            paint_floating_action_button_erase.animate().start()

            paint_floating_action_button_palette.animate().alphaBy(1.0F)
            paint_floating_action_button_palette.animate().alpha(0.0F)
            paint_floating_action_button_palette.animate().scaleXBy(1.0F)
            paint_floating_action_button_palette.animate().scaleX(0.0F)
            paint_floating_action_button_palette.animate().scaleYBy(1.0F)
            paint_floating_action_button_palette.animate().scaleY(0.0F)
            paint_floating_action_button_palette.animate().rotation(360F)
            paint_floating_action_button_palette.animate().setDuration(400L)
            paint_floating_action_button_palette.animate().start()

            paint_floating_action_button_save.animate().alphaBy(1.0F)
            paint_floating_action_button_save.animate().alpha(0.0F)
            paint_floating_action_button_save.animate().scaleXBy(1.0F)
            paint_floating_action_button_save.animate().scaleX(0.0F)
            paint_floating_action_button_save.animate().scaleYBy(1.0F)
            paint_floating_action_button_save.animate().scaleY(0.0F)
            paint_floating_action_button_save.animate().rotation(360F)
            paint_floating_action_button_save.animate().setDuration(400L)
            paint_floating_action_button_save.animate().start()

            paint_floating_action_button.animate().rotationBy(180F)

        } else {
            isOpen = true

            paint_floating_action_button_brush.visibility = View.VISIBLE
            paint_floating_action_button_brush.animate().alphaBy(0.0F)
            paint_floating_action_button_brush.animate().alpha(1.0F)
            paint_floating_action_button_brush.animate().scaleXBy(0.0F)
            paint_floating_action_button_brush.animate().scaleX(1.0F)
            paint_floating_action_button_brush.animate().scaleYBy(0.0F)
            paint_floating_action_button_brush.animate().scaleY(1.0F)
            paint_floating_action_button_brush.animate().rotation(360F)
            paint_floating_action_button_brush.animate().setDuration(500L)
            paint_floating_action_button_brush.animate().start()

            paint_floating_action_button_delete.visibility = View.VISIBLE
            paint_floating_action_button_delete.animate().alphaBy(0.0F)
            paint_floating_action_button_delete.animate().alpha(1.0F)
            paint_floating_action_button_delete.animate().scaleXBy(0.0F)
            paint_floating_action_button_delete.animate().scaleX(1.0F)
            paint_floating_action_button_delete.animate().scaleYBy(0.0F)
            paint_floating_action_button_delete.animate().scaleY(1.0F)
            paint_floating_action_button_delete.animate().rotation(360F)
            paint_floating_action_button_delete.animate().setDuration(500L)
            paint_floating_action_button_delete.animate().start()

            paint_floating_action_button_erase.visibility = View.VISIBLE
            paint_floating_action_button_erase.animate().alphaBy(0.0F)
            paint_floating_action_button_erase.animate().alpha(1.0F)
            paint_floating_action_button_erase.animate().scaleXBy(0.0F)
            paint_floating_action_button_erase.animate().scaleX(1.0F)
            paint_floating_action_button_erase.animate().scaleYBy(0.0F)
            paint_floating_action_button_erase.animate().scaleY(1.0F)
            paint_floating_action_button_erase.animate().rotation(360F)
            paint_floating_action_button_erase.animate().setDuration(500L)
            paint_floating_action_button_erase.animate().start()

            paint_floating_action_button_palette.visibility = View.VISIBLE
            paint_floating_action_button_palette.animate().alphaBy(0.0F)
            paint_floating_action_button_palette.animate().alpha(1.0F)
            paint_floating_action_button_palette.animate().scaleXBy(0.0F)
            paint_floating_action_button_palette.animate().scaleX(1.0F)
            paint_floating_action_button_palette.animate().scaleYBy(0.0F)
            paint_floating_action_button_palette.animate().scaleY(1.0F)
            paint_floating_action_button_palette.animate().rotation(360F)
            paint_floating_action_button_palette.animate().setDuration(500L)
            paint_floating_action_button_palette.animate().start()

            paint_floating_action_button_save.visibility = View.VISIBLE
            paint_floating_action_button_save.animate().alphaBy(0.0F)
            paint_floating_action_button_save.animate().alpha(1.0F)
            paint_floating_action_button_save.animate().scaleXBy(0.0F)
            paint_floating_action_button_save.animate().scaleX(1.0F)
            paint_floating_action_button_save.animate().scaleYBy(0.0F)
            paint_floating_action_button_save.animate().scaleY(1.0F)
            paint_floating_action_button_save.animate().rotation(360F)
            paint_floating_action_button_save.animate().setDuration(500L)
            paint_floating_action_button_save.animate().start()

            paint_floating_action_button.
            paint_floating_action_button.animate().rotationBy(180F)

        }
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

    private var clearDialogClickListener: DialogInterface.OnClickListener =
        DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    paintView.clear()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }

    private fun showClearConfirmationDialog(){
        val builder = AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
        builder.setMessage("Are you sure you want to delete?")
            .setTitle("Delete Screenshot")
            .setPositiveButton("Yes", clearDialogClickListener)
            .setNegativeButton("No", clearDialogClickListener)
            .show()
    }

    private fun showLineWidthSetterDialog(){
        val lineWidthDialog = AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)

        val inflater = LayoutInflater.from(this@PaintActivity)
        val seekView = inflater.inflate(R.layout.brush_width_layout, null)

        seekView.brushWidthSeekText.text = "Current width: " + paintView.getBrushWidth()

        seekView.brushWidthSeek.max = 100
        seekView.brushWidthSeek.progress = paintView.getBrushWidth()
        seekView.brushWidthSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                seekView.brushWidthSeekText.text = "Current width : $i"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        lineWidthDialog.setTitle("Please select brush width")
        lineWidthDialog.setView(seekView)
        lineWidthDialog.setPositiveButton("OK") { dialog, _ ->
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
        var fileName: String
        val builder = AlertDialog.Builder(this)
        builder.setTitle("File name")

        val input = EditText(this)

        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) {
                _, _ -> fileName = input.text.toString()
            paintView.saveImage(fileName)
            Toast.makeText(this,"Save completed", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun showColorPickerDialog(){

        val colorPicker = AmbilWarnaDialog(this, paintView.getBrushColor(), object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                if(paintView.eraserEnabled){
                    paint_floating_action_button_erase.setImageResource(R.drawable.ic_backspace_black_24dp)
                    paintView.disableEraser()
                }
                paintView.setBrushColor(color)
            }
        })


        colorPicker.dialog.setTitle("Select color")
        colorPicker.show()
    }
}

