package fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.mobilex.loggerbird.R
import constants.Constants
import kotlinx.android.synthetic.main.fragment_logger_bird.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import listeners.FloatingActionButtonAnimationListener
import listeners.FloatingActionButtonOnTouchListener
import loggerbird.LoggerBird
import java.io.File
import java.io.FileOutputStream


class FragmentLoggerBird(private val viewFragment: View, private val contextFragment: Context) :
    Fragment() {
    private var coroutineCallScreenShot: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAnimation: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var audioRecording = false
    private var videoRecording = false
    private var output: String? = null
    private var outputOld: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private lateinit var filePath: File
    private var isOpen = false
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation

    companion object {
        internal fun newInstance(viewFragment: View, context: Context): FragmentLoggerBird {
            return FragmentLoggerBird(viewFragment = viewFragment, contextFragment = context)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logger_bird, container, false)
    }

    override fun onStart() {
        super.onStart()
        try {
            fragment_floating_action_button.setOnTouchListener(
                FloatingActionButtonOnTouchListener(
                    context = contextFragment,
                    layout = fragment_layout,
                    floatingActionButtonScreenShot = fragment_floating_action_button_screenshot,
                    floatingActionButtonVideo = fragment_floating_action_button_video,
                    floatingActionButtonAudio = fragment_floating_action_button_audio
                )
            )
            buttonClicks()
            fragment_floating_action_button.setOnClickListener {
                //                buttonVisibility()
                coroutineCallAnimation.async {
                    fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
                    fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
                    withContext(Dispatchers.Main) {
                        fabOpen.setAnimationListener(
                            FloatingActionButtonAnimationListener(
                                context = contextFragment,
                                floatingActionButtonAudio = fragment_floating_action_button_audio
                            )
                        )
                        fabClose.setAnimationListener(
                            FloatingActionButtonAnimationListener(
                                context = contextFragment,
                                floatingActionButtonAudio = fragment_floating_action_button_audio
                            )
                        )
                        animationVisibility()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.floatingActionButtonTag)
        }
    }

    private fun takeScreenShot(viewFragment: View): Bitmap {
        val view: View = (viewFragment.parent as View)
        val bitmap: Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveScreenShot(viewFragment: View, context: Context) {
        val fileDirectory: File = context.filesDir
        val filePath = File(
            fileDirectory,
            "logger_bird_screenshot" + System.currentTimeMillis().toString() + ".png"
        )
        try {
            filePath.createNewFile()
            val fileOutputStream = FileOutputStream(filePath)
            takeScreenShot(viewFragment = viewFragment).compress(
                Bitmap.CompressFormat.PNG,
                100,
                fileOutputStream
            )
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.screenShotTag)
        }
    }

    private fun buttonVisibility() {
        if (fragment_floating_action_button_audio.visibility == View.GONE) {
            fragment_floating_action_button_audio.visibility = View.VISIBLE
        } else {
            fragment_floating_action_button_audio.visibility = View.GONE
            // handlerTemp.postDelayed(Runnable {},3000)
        }
        if (fragment_floating_action_button_video.visibility == View.GONE) {
            fragment_floating_action_button_video.visibility = View.VISIBLE
            // handler.postDelayed(Runnable { },500)
        } else {
            fragment_floating_action_button_video.visibility = View.GONE
            //handler.postDelayed(Runnable {   },500)
        }
        if (fragment_floating_action_button_screenshot.visibility == View.GONE) {
            fragment_floating_action_button_screenshot.visibility = View.VISIBLE
            // handlerTemp.postDelayed(Runnable { },3000)
        } else {
            fragment_floating_action_button_screenshot.visibility = View.GONE
        }
    }


    private fun buttonClicks() {
        fragment_floating_action_button_screenshot.setOnClickListener {
            coroutineCallScreenShot.async {
                saveScreenShot(viewFragment = viewFragment, context = contextFragment)
            }
        }
        fragment_floating_action_button_audio.setOnClickListener {
            takeRecording()
        }
        fragment_floating_action_button_video.setOnClickListener {
            if (!videoRecording) {
                fragment_floating_action_button_video.setImageResource(R.drawable.ic_videocam_off_black_24dp)
//                    Toast.makeText(context, "Video recording started", Toast.LENGTH_SHORT).show()
                videoRecording = true
            } else {
                fragment_floating_action_button_video.setImageResource(R.drawable.ic_videocam_black_24dp)
//                    Toast.makeText(context, "Video recording finished", Toast.LENGTH_SHORT).show()
                videoRecording = false
            }
        }
    }


    private fun takeRecording() {
        try {
            if (checkPermission()) {
                if (!audioRecording) {
                    val fileDirectory: File = context!!.filesDir
                    filePath = File(
                        fileDirectory,
                        "logger_bird_audio" + System.currentTimeMillis()
                            .toString() + "recording.mp3"
                    )
                    mediaRecorder = MediaRecorder()
                    output =
                        "logger_bird_audio" + System.currentTimeMillis().toString() + "recording.mp3"
                    outputOld =
                        Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"
                    mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                    mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mediaRecorder?.setOutputFile(filePath)
                    } else {
                        mediaRecorder?.setOutputFile(outputOld)
                    }
                    startRecording()
                    fragment_floating_action_button_audio.setImageResource(R.drawable.ic_mic_off_black_24dp)
                    audioRecording = true
                } else {
                    stopRecording()
                    fragment_floating_action_button_audio.setImageResource(R.drawable.ic_mic_black_24dp)
                    audioRecording = false
                }
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ), 111
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.audioRecordingTag)
        }
    }

    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(context, "Audio recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.audioStartRecordingTag)
        }
    }

    private fun stopRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            Toast.makeText(context, "Audio recording finished", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED == checkSelfPermission(
            contextFragment,
            Manifest.permission.RECORD_AUDIO
        )
    }

    //    private fun animationVisibility() {
//        if (isOpen) {
//            fragment_floating_action_button_video.startAnimation(fabClose)
//            fragment_floating_action_button_audio.startAnimation(fabClose)
//            fragment_floating_action_button_screenshot.startAnimation(fabClose)
////            fragment_floating_action_button.startAnimation(fabRClockwise)
//            isOpen = false
//        } else {
//            fragment_floating_action_button_video.startAnimation(fabOpen)
//            fragment_floating_action_button_audio.startAnimation(fabOpen)
//            fragment_floating_action_button_screenshot.startAnimation(fabOpen)
//
////            fragment_floating_action_button.startAnimation(fabRAntiClockwise)
//            isOpen = true
//        }
//    }
//    private fun animationVisibility() {
//        if (isOpen) {
//            isOpen = false
//            fragment_floating_action_button_video.visibility = View.INVISIBLE
//            fragment_floating_action_button_audio.visibility = View.INVISIBLE
//            fragment_floating_action_button_screenshot.visibility = View.INVISIBLE
//            fragment_floating_action_button.setImageResource(R.drawable.ic_add_black_24dp)
//            fragment_floating_action_button.setBackgroundTintList(
//                ColorStateList.valueOf(
//                    getResources().getColor(R.color.colorBlack)
//                )
//            )
////            fragment_floating_action_button.animate().rotationBy(45F)
//        } else {
//            isOpen = true
//            fragment_floating_action_button_video.visibility = View.VISIBLE
//            fragment_floating_action_button_audio.visibility = View.VISIBLE
//            fragment_floating_action_button_screenshot.visibility = View.VISIBLE
//            fragment_floating_action_button_video.animate().rotationBy(360F)
//            fragment_floating_action_button_audio.animate().rotationBy(360F)
//            fragment_floating_action_button_screenshot.animate().rotationBy(360F)
//            fragment_floating_action_button.setImageResource(R.drawable.ic_add_red_24dp)
//            fragment_floating_action_button.setBackgroundTintList(
//                ContextCompat.getColorStateList(
//                    contextFragment,
//                    R.color.colorBlack
//                )
//            )
////            fragment_floating_action_button.animate().rotationBy(-45F)
//        }
//    }
    private fun animationVisibility() {
        if (isOpen) {
            isOpen = false
            fragment_floating_action_button_video.animate().alphaBy(1.0F)
            fragment_floating_action_button_video.animate().alpha(0.0F)
            fragment_floating_action_button_video.animate().scaleXBy(1.0F)
            fragment_floating_action_button_video.animate().scaleX(0.0F)
            fragment_floating_action_button_video.animate().scaleYBy(1.0F)
            fragment_floating_action_button_video.animate().scaleY(0.0F)
            fragment_floating_action_button_video.animate().rotation(360F)
            fragment_floating_action_button_video.animate().setDuration(200L)
            fragment_floating_action_button_video.animate().start()
            fragment_floating_action_button_screenshot.animate().alphaBy(1.0F)
            fragment_floating_action_button_screenshot.animate().alpha(0.0F)
            fragment_floating_action_button_screenshot.animate().scaleXBy(1.0F)
            fragment_floating_action_button_screenshot.animate().scaleX(0.0F)
            fragment_floating_action_button_screenshot.animate().scaleYBy(1.0F)
            fragment_floating_action_button_screenshot.animate().scaleY(0.0F)
            fragment_floating_action_button_screenshot.animate().rotation(360F)
            fragment_floating_action_button_screenshot.animate().setDuration(200L)
            fragment_floating_action_button_screenshot.animate().start()
            fragment_floating_action_button_audio.animate().alphaBy(1.0F)
            fragment_floating_action_button_audio.animate().alpha(0.0F)
            fragment_floating_action_button_audio.animate().scaleXBy(1.0F)
            fragment_floating_action_button_audio.animate().scaleX(0.0F)
            fragment_floating_action_button_audio.animate().scaleYBy(1.0F)
            fragment_floating_action_button_audio.animate().scaleY(0.0F)
            fragment_floating_action_button_audio.animate().rotation(360F)
            fragment_floating_action_button_audio.animate().setDuration(200L)
            fragment_floating_action_button_audio.animate().start()
            fragment_floating_action_button.setImageResource(R.drawable.ic_add_black_24dp)
            fragment_floating_action_button.animate().rotationBy(180F)
        } else {
            isOpen = true
            fragment_floating_action_button_screenshot.visibility = VISIBLE
            fragment_floating_action_button_screenshot.animate().alphaBy(0.0F)
            fragment_floating_action_button_screenshot.animate().alpha(1.0F)
            fragment_floating_action_button_screenshot.animate().scaleXBy(0.0F)
            fragment_floating_action_button_screenshot.animate().scaleX(1.0F)
            fragment_floating_action_button_screenshot.animate().scaleYBy(0.0F)
            fragment_floating_action_button_screenshot.animate().scaleY(1.0F)
            fragment_floating_action_button_screenshot.animate().rotation(360F)
            fragment_floating_action_button_screenshot.animate().setDuration(200L)
            fragment_floating_action_button_screenshot.animate().start()
            fragment_floating_action_button_audio.visibility = VISIBLE
            fragment_floating_action_button_audio.animate().alphaBy(0.0F)
            fragment_floating_action_button_audio.animate().alpha(1.0F)
            fragment_floating_action_button_audio.animate().scaleXBy(0.0F)
            fragment_floating_action_button_audio.animate().scaleX(1.0F)
            fragment_floating_action_button_audio.animate().scaleYBy(0.0F)
            fragment_floating_action_button_audio.animate().scaleY(1.0F)
            fragment_floating_action_button_audio.animate().rotation(360F)
            fragment_floating_action_button_audio.animate().setDuration(200L)
            fragment_floating_action_button_audio.animate().start()
            fragment_floating_action_button_video.visibility = VISIBLE
            fragment_floating_action_button_video.animate().alphaBy(0.0F)
            fragment_floating_action_button_video.animate().alpha(1.0F)
            fragment_floating_action_button_video.animate().scaleXBy(0.0F)
            fragment_floating_action_button_video.animate().scaleX(1.0F)
            fragment_floating_action_button_video.animate().scaleYBy(0.0F)
            fragment_floating_action_button_video.animate().scaleY(1.0F)
            fragment_floating_action_button_video.animate().rotation(360F)
            fragment_floating_action_button_video.animate().setDuration(200L)
            fragment_floating_action_button_video.animate().start()
            fragment_floating_action_button.animate().rotationBy(180F)
            fragment_floating_action_button.setImageResource(R.drawable.ic_add_red_24dp)
        }
    }
}
