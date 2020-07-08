//package fragments
//
//import android.Manifest
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.hardware.display.DisplayManager
//import android.hardware.display.VirtualDisplay
//import android.media.MediaRecorder
//import android.media.projection.MediaProjection
//import android.media.projection.MediaProjectionManager
//import android.os.Build
//import android.os.Bundle
//import android.os.Environment
//import android.util.SparseIntArray
//import android.view.LayoutInflater
//import android.view.Surface
//import android.view.View
//import android.view.View.VISIBLE
//import android.view.ViewGroup
//import android.view.animation.Animation
//import android.view.animation.AnimationUtils
//import android.widget.Toast
//import androidx.annotation.RequiresApi
//import androidx.core.content.ContextCompat.checkSelfPermission
//import androidx.fragment.app.Fragment
//import com.mobilex.loggerbird.R
//import constants.Constants
//import kotlinx.android.synthetic.main.fragment_logger_bird.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.withContext
//import listeners.floatingActionButton.FloatingActionButtonAnimationListener
//import listeners.floatingActionButton.FloatingActionButtonOnTouchListener
//import loggerbird.LoggerBird
//import java.io.File
//import java.io.FileOutputStream
//import android.util.DisplayMetrics
//
//
//class FragmentLoggerBird(private val viewFragment: View, private val mContext: Context) :
//    Fragment() {
//    private var coroutineCallScreenShot: CoroutineScope = CoroutineScope(Dispatchers.IO)
//    private var coroutineCallVideo: CoroutineScope = CoroutineScope(Dispatchers.IO)
//    private var coroutineCallAudio: CoroutineScope = CoroutineScope(Dispatchers.IO)
//    private var audioRecording = false
//    private var videoRecording = false
//    private var mediaRecorderAudio: MediaRecorder? = null
//    private var state: Boolean = false
//    private lateinit var filePath: File
//    private var isOpen = false
//    private var screenDensity: Int = 0
//    private var projectManager: MediaProjectionManager? = null
//    private var mediaProjection: MediaProjection? = null
//    private var virtualDisplay: VirtualDisplay? = null
//    private lateinit var mediaProjectionCallback: MediaProjectionCallback
//    private var mediaRecorderVideo: MediaRecorder? = null
//
//
//    companion object {
//        private const val REQUEST_CODE_VIDEO = 1000
//        private const val REQUEST_CODE_AUDIO_PERMISSION = 2001
//        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 2002
//        private var DISPLAY_WIDTH = 1080
//        private var DISPLAY_HEIGHT = 1920
//        private val ORIENTATIONS = SparseIntArray()
//
//        init {
//            ORIENTATIONS.append(Surface.ROTATION_0, 90)
//            ORIENTATIONS.append(Surface.ROTATION_90, 0)
//            ORIENTATIONS.append(Surface.ROTATION_180, 270)
//            ORIENTATIONS.append(Surface.ROTATION_270, 180)
//        }
//
//        internal fun newInstance(viewFragment: View, context: Context): FragmentLoggerBird {
//            return FragmentLoggerBird(viewFragment = viewFragment, mContext = context)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_logger_bird, container, false)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    override fun onStart() {
//        super.onStart()
//        try {
//            buttonClicks()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            LoggerBird.callEnqueue()
//            LoggerBird.callExceptionDetails(exception = e, tag = Constants.floatingActionButtonTag)
//        }
//    }
//
//
////    private fun buttonVisibility() {
////        if (fragment_floating_action_button_audio.visibility == View.GONE) {
////            fragment_floating_action_button_audio.visibility = View.VISIBLE
////        } else {
////            fragment_floating_action_button_audio.visibility = View.GONE
////            // handlerTemp.postDelayed(Runnable {},3000)
////        }
////        if (fragment_floating_action_button_video.visibility == View.GONE) {
////            fragment_floating_action_button_video.visibility = View.VISIBLE
////            // handler.postDelayed(Runnable { },500)
////        } else {
////            fragment_floating_action_button_video.visibility = View.GONE
////            //handler.postDelayed(Runnable {   },500)
////        }
////        if (fragment_floating_action_button_screenshot.visibility == View.GONE) {
////            fragment_floating_action_button_screenshot.visibility = View.VISIBLE
////            // handlerTemp.postDelayed(Runnable { },3000)
////        } else {
////            fragment_floating_action_button_screenshot.visibility = View.GONE
////        }
////    }
//
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun buttonClicks() {
////        fragment_floating_action_button.setOnTouchListener(
////            FloatingActionButtonOnTouchListener(
////                floatingActionButtonScreenShot = fragment_floating_action_button_screenshot,
////                floatingActionButtonVideo = fragment_floating_action_button_video,
////                floatingActionButtonAudio = fragment_floating_action_button_audio
////            )
////        )
////        fragment_floating_action_button.setOnClickListener {
////            coroutineCallAnimation.async {
////                fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
////                fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
////                withContext(Dispatchers.Main) {
////                    fabOpen.setAnimationListener(
////                        FloatingActionButtonAnimationListener(
////                            context = mContext,
////                            floatingActionButtonAudio = fragment_floating_action_button_audio
////                        )
////                    )
////                    fabClose.setAnimationListener(
////                        FloatingActionButtonAnimationListener(
////                            context = mContext,
////                            floatingActionButtonAudio = fragment_floating_action_button_audio
////                        )
////                    )
////                    animationVisibility()
////                }
////            }
////        }
//        fragment_floating_action_button_screenshot.setOnClickListener {
//            takeScreenShot(viewFragment = viewFragment, context = mContext)
//        }
//        fragment_floating_action_button_audio.setOnClickListener {
//            takeAudioRecording()
//        }
//
//        fragment_floating_action_button_video.setOnClickListener {
//            takeVideoRecording()
//        }
//    }
//
//
//    private fun animationVisibility() {
//        if (isOpen) {
//            isOpen = false
//            fragment_floating_action_button_video.animate().alphaBy(1.0F)
//            fragment_floating_action_button_video.animate().alpha(0.0F)
//            fragment_floating_action_button_video.animate().scaleXBy(1.0F)
//            fragment_floating_action_button_video.animate().scaleX(0.0F)
//            fragment_floating_action_button_video.animate().scaleYBy(1.0F)
//            fragment_floating_action_button_video.animate().scaleY(0.0F)
//            fragment_floating_action_button_video.animate().rotation(360F)
//            fragment_floating_action_button_video.animate().setDuration(200L)
//            fragment_floating_action_button_video.animate().start()
//            fragment_floating_action_button_screenshot.animate().alphaBy(1.0F)
//            fragment_floating_action_button_screenshot.animate().alpha(0.0F)
//            fragment_floating_action_button_screenshot.animate().scaleXBy(1.0F)
//            fragment_floating_action_button_screenshot.animate().scaleX(0.0F)
//            fragment_floating_action_button_screenshot.animate().scaleYBy(1.0F)
//            fragment_floating_action_button_screenshot.animate().scaleY(0.0F)
//            fragment_floating_action_button_screenshot.animate().rotation(360F)
//            fragment_floating_action_button_screenshot.animate().setDuration(200L)
//            fragment_floating_action_button_screenshot.animate().start()
//            fragment_floating_action_button_audio.animate().alphaBy(1.0F)
//            fragment_floating_action_button_audio.animate().alpha(0.0F)
//            fragment_floating_action_button_audio.animate().scaleXBy(1.0F)
//            fragment_floating_action_button_audio.animate().scaleX(0.0F)
//            fragment_floating_action_button_audio.animate().scaleYBy(1.0F)
//            fragment_floating_action_button_audio.animate().scaleY(0.0F)
//            fragment_floating_action_button_audio.animate().rotation(360F)
//            fragment_floating_action_button_audio.animate().setDuration(200L)
//            fragment_floating_action_button_audio.animate().start()
////            fragment_floating_action_button.setImageResource(R.drawable.ic_add_black_24dp)
////            fragment_floating_action_button.animate().rotationBy(180F)
//        } else {
//            isOpen = true
//            fragment_floating_action_button_screenshot.visibility = VISIBLE
//            fragment_floating_action_button_screenshot.animate().alphaBy(0.0F)
//            fragment_floating_action_button_screenshot.animate().alpha(1.0F)
//            fragment_floating_action_button_screenshot.animate().scaleXBy(0.0F)
//            fragment_floating_action_button_screenshot.animate().scaleX(1.0F)
//            fragment_floating_action_button_screenshot.animate().scaleYBy(0.0F)
//            fragment_floating_action_button_screenshot.animate().scaleY(1.0F)
//            fragment_floating_action_button_screenshot.animate().rotation(360F)
//            fragment_floating_action_button_screenshot.animate().setDuration(200L)
//            fragment_floating_action_button_screenshot.animate().start()
//            fragment_floating_action_button_audio.visibility = VISIBLE
//            fragment_floating_action_button_audio.animate().alphaBy(0.0F)
//            fragment_floating_action_button_audio.animate().alpha(1.0F)
//            fragment_floating_action_button_audio.animate().scaleXBy(0.0F)
//            fragment_floating_action_button_audio.animate().scaleX(1.0F)
//            fragment_floating_action_button_audio.animate().scaleYBy(0.0F)
//            fragment_floating_action_button_audio.animate().scaleY(1.0F)
//            fragment_floating_action_button_audio.animate().rotation(360F)
//            fragment_floating_action_button_audio.animate().setDuration(200L)
//            fragment_floating_action_button_audio.animate().start()
//            fragment_floating_action_button_video.visibility = VISIBLE
//            fragment_floating_action_button_video.animate().alphaBy(0.0F)
//            fragment_floating_action_button_video.animate().alpha(1.0F)
//            fragment_floating_action_button_video.animate().scaleXBy(0.0F)
//            fragment_floating_action_button_video.animate().scaleX(1.0F)
//            fragment_floating_action_button_video.animate().scaleYBy(0.0F)
//            fragment_floating_action_button_video.animate().scaleY(1.0F)
//            fragment_floating_action_button_video.animate().rotation(360F)
//            fragment_floating_action_button_video.animate().setDuration(200L)
//            fragment_floating_action_button_video.animate().start()
////            fragment_floating_action_button.animate().rotationBy(180F)
////            fragment_floating_action_button.setImageResource(R.drawable.ic_close_black_24dp)
//        }
//    }
//
//    private fun checkWriteExternalStoragePermission(): Boolean {
//        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(
//                mContext,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//        ) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ), REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION
//            )
//            return false
//        }
//        return true
//    }
//
//    private fun checkAudioPermission(): Boolean {
//        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(
//                mContext,
//                Manifest.permission.RECORD_AUDIO
//            )
//        ) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.RECORD_AUDIO
//                ), REQUEST_CODE_AUDIO_PERMISSION
//            )
//            return false
//        }
//        return true
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode == REQUEST_CODE_AUDIO_PERMISSION) {
//            Toast.makeText(mContext, "Permission Granted!", Toast.LENGTH_SHORT).show()
//        } else if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
//            Toast.makeText(mContext, "Permission Granted!", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun createScreenShot(viewFragment: View): Bitmap {
//        val view: View = (viewFragment.parent as View)
//        val bitmap: Bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        view.draw(canvas)
//        return bitmap
//    }
//
//    private fun takeScreenShot(viewFragment: View, context: Context) {
//        if (checkWriteExternalStoragePermission()) {
//            coroutineCallScreenShot.async {
//                val fileDirectory: File = context.filesDir
//                val filePath = File(
//                    fileDirectory,
//                    "logger_bird_screenshot" + System.currentTimeMillis().toString() + ".png"
//                )
//                try {
//                    withContext(Dispatchers.IO) {
//                        filePath.createNewFile()
//                        val fileOutputStream = FileOutputStream(filePath)
//                        createScreenShot(viewFragment = viewFragment).compress(
//                            Bitmap.CompressFormat.PNG,
//                            100,
//                            fileOutputStream
//                        )
//                        fileOutputStream.close()
//                    }
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(mContext, "ScreenShot Taken!", Toast.LENGTH_SHORT).show()
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    LoggerBird.callEnqueue()
//                    LoggerBird.callExceptionDetails(exception = e, tag = Constants.screenShotTag)
//                }
//            }
//        } else {
//            Toast.makeText(mContext, "Permission Denied!", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun takeAudioRecording() {
//        if (checkAudioPermission() && checkWriteExternalStoragePermission()) {
//            coroutineCallAudio.async {
//                try {
//                    if (!audioRecording) {
//                        val fileDirectory: File = context!!.filesDir
//                        filePath = File(
//                            fileDirectory,
//                            "logger_bird_audio" + System.currentTimeMillis()
//                                .toString() + "recording.3gpp"
//                        )
//                        mediaRecorderAudio = MediaRecorder()
//                        mediaRecorderAudio?.setAudioSource(MediaRecorder.AudioSource.MIC)
//                        mediaRecorderAudio?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                        mediaRecorderAudio?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            mediaRecorderAudio?.setOutputFile(filePath)
//                        } else {
//                            mediaRecorderAudio?.setOutputFile(filePath.path)
//                        }
//                        startAudioRecording()
//                        fragment_floating_action_button_audio.setImageResource(R.drawable.ic_mic_off_black_24dp)
//                        audioRecording = true
//                    } else {
//                        stopAudioRecording()
//                        fragment_floating_action_button_audio.setImageResource(R.drawable.ic_mic_black_24dp)
//                        audioRecording = false
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    LoggerBird.callEnqueue()
//                    LoggerBird.callExceptionDetails(
//                        exception = e,
//                        tag = Constants.audioRecordingTag
//                    )
//                }
//            }
//        } else {
//            Toast.makeText(mContext, "Permission Denied!", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private suspend fun startAudioRecording() {
//        try {
//            withContext(Dispatchers.IO) {
//                mediaRecorderAudio?.prepare()
//            }
//            mediaRecorderAudio?.start()
//            state = true
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Audio recording started", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            LoggerBird.callEnqueue()
//            LoggerBird.callExceptionDetails(exception = e, tag = Constants.audioStartRecordingTag)
//        }
//    }
//
//    private suspend fun stopAudioRecording() {
//        try {
//            if (state) {
//                mediaRecorderAudio?.stop()
//                mediaRecorderAudio?.release()
//                state = false
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Audio recording finished", Toast.LENGTH_SHORT).show()
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            LoggerBird.callEnqueue()
//            LoggerBird.callExceptionDetails(exception = e, tag = Constants.audioStopRecordingTag)
//        }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun takeVideoRecording() {
//        if (checkWriteExternalStoragePermission() && checkAudioPermission()) {
//            coroutineCallVideo.async {
//                try {
//                    if (!videoRecording) {
//                        startScreenRecording()
//                    } else {
//                        stopScreenRecord()
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(
//                                mContext,
//                                "Screen recording finished",
//                                Toast.LENGTH_SHORT
//                            ).show()
////                            fragment_floating_action_button_video.setImageResource(R.drawable.ic_videocam_black_24dp)
//                            fragment_floating_action_button_video.setBackgroundResource(R.drawable.ic_videocam_black_24dp)
//                        }
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    LoggerBird.callEnqueue()
//                    LoggerBird.callExceptionDetails(
//                        exception = e,
//                        tag = Constants.videoRecordingTag
//                    )
//                }
//            }
//        } else {
//            Toast.makeText(mContext, "Permission Denied!", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun startScreenRecording() {
//        if (!videoRecording) {
//            shareScreen()
//        } else {
//            stopScreenRecord()
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun initRecorder() {
//        try {
//            val metrics = DisplayMetrics()
//            (context as Activity).windowManager.defaultDisplay.getMetrics(metrics)
//            screenDensity = metrics.densityDpi
//            mediaRecorderVideo = MediaRecorder()
//            DISPLAY_HEIGHT = metrics.heightPixels
//            DISPLAY_WIDTH = metrics.widthPixels
//            mediaRecorderVideo!!.setAudioSource(MediaRecorder.AudioSource.MIC)
//            mediaRecorderVideo!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
//            mediaRecorderVideo!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//            val fileDirectory: File = context!!.filesDir
//            filePath = File(
//                fileDirectory,
//                "logger_bird_video" + System.currentTimeMillis()
//                    .toString() + ".mp4"
//            )
//            mediaRecorderVideo!!.setOutputFile(filePath.path)
//            mediaRecorderVideo!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
//            mediaRecorderVideo!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//            mediaRecorderVideo!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//            mediaRecorderVideo!!.setVideoEncodingBitRate(512 * 1000)
//            mediaRecorderVideo!!.setVideoFrameRate(30)
//            val rotation: Int = (context as Activity).windowManager.defaultDisplay.rotation
//            val orientation: Int = ORIENTATIONS.get(rotation + 90)
//            mediaRecorderVideo!!.setOrientationHint(orientation)
//            mediaRecorderVideo!!.prepare()
//            mediaRecorderVideo!!.start()
//            videoRecording = true
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun shareScreen() {
//        projectManager =
//            mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
//        if (mediaProjection == null) {
//            startActivityForResult(projectManager!!.createScreenCaptureIntent(), REQUEST_CODE_VIDEO)
//        }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (resultCode ==Activity.RESULT_OK) {
//            initRecorder()
//            Toast.makeText(mContext, "Screen recording started", Toast.LENGTH_SHORT)
//                .show()
//            mediaProjectionCallback = MediaProjectionCallback()
//            mediaProjection = projectManager!!.getMediaProjection(resultCode, data!!)
//            mediaProjection!!.registerCallback(mediaProjectionCallback, null)
//            virtualDisplay = createVirtualDisplay()
////            fragment_floating_action_button_video.setImageResource(R.drawable.ic_videocam_off_black_24dp)
////            fragment_floating_action_button_video.setBackgroundResource(R.drawable.ic_videocam_off_black_24dp)
//        }else{
//            Toast.makeText(mContext, "Screen cast permission denied", Toast.LENGTH_SHORT)
//                .show()
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun createVirtualDisplay(): VirtualDisplay? {
//        return mediaProjection!!.createVirtualDisplay(
//            "LoggerBirdFragment",
//            DISPLAY_WIDTH,
//            DISPLAY_HEIGHT,
//            screenDensity,
//            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//            mediaRecorderVideo!!.surface,
//            null,
//            null
//        )
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun stopScreenRecord() {
//        videoRecording = false
//        mediaRecorderVideo!!.stop()
//        mediaRecorderVideo!!.reset()
//        if (virtualDisplay != null) {
//            virtualDisplay!!.release()
//        }
//        destroyMediaProjection()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun destroyMediaProjection() {
//        if (mediaProjection != null) {
//            mediaProjection!!.unregisterCallback(mediaProjectionCallback)
//            mediaProjection!!.stop()
//            mediaProjection = null
//            videoRecording = false
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    override fun onDestroy() {
//        super.onDestroy()
//        destroyMediaProjection()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    inner class MediaProjectionCallback : MediaProjection.Callback() {
//        override fun onStop() {
//            stopScreenRecord()
//        }
//    }
//}
//
