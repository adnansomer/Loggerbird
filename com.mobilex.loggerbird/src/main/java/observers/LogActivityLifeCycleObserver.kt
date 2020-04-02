package observers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import listeners.FloatingActionButtonAnimationListener
import listeners.FloatingActionButtonOnTouchListener
import services.LoggerBirdForegroundServiceVideo
import utils.LinkedBlockingQueueUtil
import java.io.File
import java.io.FileOutputStream
import java.lang.Runnable

class LogActivityLifeCycleObserver(contextMetrics:Context) : Activity(), Application.ActivityLifecycleCallbacks {
    //Global variables.
    private var stringBuilderBundle: StringBuilder = StringBuilder()
    private lateinit var floating_action_button: FloatingActionButton
    private lateinit var floating_action_button_screenshot: FloatingActionButton
    private lateinit var floating_action_button_video: FloatingActionButton
    private lateinit var floating_action_button_audio: FloatingActionButton
    private lateinit var context: Context
    private lateinit var view: View
    private lateinit var rootView: View
    private var controlOldCoordinates: Boolean = false
    private var coroutineCallScreenShot: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAnimation: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallVideo: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallForegroundService: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAudio: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var audioRecording = false
    private var videoRecording = false
    private var mediaRecorderAudio: MediaRecorder? = null
    private var state: Boolean = false
    private lateinit var filePath: File
    private var isOpen = false
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private var screenDensity: Int = 0
    private var projectManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var mediaProjectionCallback: MediaProjectionCallback
    private var mediaRecorderVideo: MediaRecorder?  = MediaRecorder()
    private var requestCode: Int = 0
    private var resultCode: Int = 0
    private var dataIntent: Intent? = null
    private lateinit var intentForegroundServiceVideo: Intent


    //Static global variables.
    companion object {
        private var currentLifeCycleState: String? = null
        private var formattedTime: String? = null
        internal var returnActivityLifeCycleClassName: String? = null
        internal var floatingActionButtonLastDx: Float = 0F
        internal var floatingActionButtonScreenShotLastDx: Float = 0F
        internal var floatingActionButtonVideoLastDx: Float = 0F
        internal var floatingActionButtonAudioLastDx: Float = 0F
        internal var floatingActionButtonLastDy: Float = 0F
        internal var floatingActionButtonScreenShotLastDy: Float = 0F
        internal var floatingActionButtonVideoLastDy: Float = 0F
        internal var floatingActionButtonAudioLastDy: Float = 0f
        private const val REQUEST_CODE_VIDEO = 1000
        private const val REQUEST_CODE_AUDIO_PERMISSION = 2001
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 2002
        private var DISPLAY_WIDTH = 1080
        private var DISPLAY_HEIGHT = 1920
        private val ORIENTATIONS = SparseIntArray()
        internal var controlPermissionRequest: Boolean = false
        private var runnableList: ArrayList<Runnable> = ArrayList()
        private var workQueueLinked: LinkedBlockingQueueUtil = LinkedBlockingQueueUtil()
        internal fun callEnqueue() {
            workQueueLinked.controlRunnable = false
            if (runnableList.size > 0) {
                runnableList.removeAt(0)
                if (runnableList.size > 0) {
                    workQueueLinked.put(runnableList[0])
                }
            }
        }
    }

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
        LoggerBird.stringBuilderActivityLifeCycleObserver.append("\n" + "Life Cycle Details:" + "\n")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        try {
            stringBuilderBundle = StringBuilder()
            for (outStateItem in outState.keySet()) {
                stringBuilderBundle.append(
                    "$outStateItem:" + outState.get(
                        outStateItem
                    ) + "\n"
                )
            }
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onSaveInstanceState"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n" + stringBuilderBundle.toString() + "\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        try {
            this.context = activity
            val metrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(metrics)
            screenDensity = metrics.densityDpi
            DISPLAY_HEIGHT = metrics.heightPixels
            DISPLAY_WIDTH = metrics.widthPixels
            LoggerBird.fragmentLifeCycleObserver =
                LogFragmentLifeCycleObserver()
            (activity as AppCompatActivity).supportFragmentManager.registerFragmentLifecycleCallbacks(
                LoggerBird.fragmentLifeCycleObserver,
                true
            )
            returnActivityLifeCycleClassName = activity.javaClass.simpleName
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onCreate"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
            if (LoggerBird.classList.isEmpty()) {
                LoggerBird.classList.add(activity.javaClass.simpleName)
            }
            while (LoggerBird.classList.iterator().hasNext()) {
                if (LoggerBird.classList.contains(activity.javaClass.simpleName)) {
                    break
                } else {
                    LoggerBird.classList.add(activity.javaClass.simpleName)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    override fun onActivityStarted(activity: Activity) {
        try {
            if (this::rootView.isInitialized && this::view.isInitialized) {
                (rootView as ViewGroup).removeView(view)
            }
            val rootView: ViewGroup =
                activity.window.decorView.findViewById(android.R.id.content)
            val view: View = LayoutInflater.from(activity)
                .inflate(
                    R.layout.fragment_logger_bird,
                    rootView,
                    false
                )
            val layoutParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            activity.addContentView(view, layoutParams)
            this.rootView = rootView
            this.view = view
            floating_action_button = view.findViewById(R.id.fragment_floating_action_button)
            floating_action_button_screenshot =
                view.findViewById(R.id.fragment_floating_action_button_screenshot)
            floating_action_button_video =
                view.findViewById(R.id.fragment_floating_action_button_video)
            floating_action_button_audio =
                view.findViewById(R.id.fragment_floating_action_button_audio)

            if(videoRecording){
                floating_action_button_video.setImageResource(R.drawable.ic_videocam_off_black_24dp)
            }
//            checkOldCoordinates()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                buttonClicks()
            }
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStart"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    override fun onActivityResumed(activity: Activity) {
        try {
            controlPermissionRequest = false
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onResume"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    override fun onActivityPaused(activity: Activity) {
        try {
            controlOldCoordinates = true
            if (controlPermissionRequest) {
                stopForegroundServiceVideo()
            }
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onPause"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    override fun onActivityStopped(activity: Activity) {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onStop"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        try {
            val date = Calendar.getInstance().time
            val formatter = SimpleDateFormat.getDateTimeInstance()
            formattedTime = formatter.format(date)
            currentLifeCycleState = "onDestroy"
            LoggerBird.stringBuilderActivityLifeCycleObserver.append(Constants.activityTag + ":" + activity.javaClass.simpleName + " " + "$formattedTime:$currentLifeCycleState\n")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.activityTag
            )
        }
    }

    /**
     * This Method Re-Creates Instantiation For stringBuilderLifeCycleObserver.
     */
    internal fun refreshLifeCycleObserverState() {
        LoggerBird.stringBuilderActivityLifeCycleObserver = StringBuilder()
    }

    /**
     * This Method Is Used For Getting Activity List.
     * Variables:
     * @var classList takes list of activities that are called with this observer.
     * @return ArrayList<String>.
     */
    internal fun returnClassList(): ArrayList<String> {
        return LoggerBird.classList
    }

    /**
     * This Method Is Used For Printing Observer Outcome.
     * Variables:
     * @var stringBuilderLifeCycleObserver will print activity details.
     * @return String value.
     */
    internal fun returnActivityLifeCycleState(): String {
        return LoggerBird.stringBuilderActivityLifeCycleObserver.toString()
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buttonClicks() {
        floating_action_button.setOnTouchListener(
            FloatingActionButtonOnTouchListener(
                floatingActionButtonScreenShot = floating_action_button_screenshot,
                floatingActionButtonVideo = floating_action_button_video,
                floatingActionButtonAudio = floating_action_button_audio
            )
        )
        floating_action_button.setOnClickListener {
            coroutineCallAnimation.async {
                fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
                fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
                withContext(Dispatchers.Main) {
                    fabOpen.setAnimationListener(
                        FloatingActionButtonAnimationListener(
                            context = context,
                            floatingActionButtonAudio = floating_action_button_audio
                        )
                    )
                    fabClose.setAnimationListener(
                        FloatingActionButtonAnimationListener(
                            context = context,
                            floatingActionButtonAudio = floating_action_button_audio
                        )
                    )
                    animationVisibility()
                }
            }
        }
        floating_action_button_screenshot.setOnClickListener {
            takeScreenShot(view = view, context = context)
        }
        floating_action_button_audio.setOnClickListener {
            takeAudioRecording()
        }

        floating_action_button_video.setOnClickListener {
            callVideoRecording(
                requestCode = requestCode,
                resultCode = resultCode,
                data = dataIntent
            )
        }
    }

    private fun animationVisibility() {
        if (isOpen) {
            isOpen = false
            floating_action_button_video.animate().alphaBy(1.0F)
            floating_action_button_video.animate().alpha(0.0F)
            floating_action_button_video.animate().scaleXBy(1.0F)
            floating_action_button_video.animate().scaleX(0.0F)
            floating_action_button_video.animate().scaleYBy(1.0F)
            floating_action_button_video.animate().scaleY(0.0F)
            floating_action_button_video.animate().rotation(360F)
            floating_action_button_video.animate().setDuration(200L)
            floating_action_button_video.animate().start()
            floating_action_button_screenshot.animate().alphaBy(1.0F)
            floating_action_button_screenshot.animate().alpha(0.0F)
            floating_action_button_screenshot.animate().scaleXBy(1.0F)
            floating_action_button_screenshot.animate().scaleX(0.0F)
            floating_action_button_screenshot.animate().scaleYBy(1.0F)
            floating_action_button_screenshot.animate().scaleY(0.0F)
            floating_action_button_screenshot.animate().rotation(360F)
            floating_action_button_screenshot.animate().setDuration(200L)
            floating_action_button_screenshot.animate().start()
            floating_action_button_audio.animate().alphaBy(1.0F)
            floating_action_button_audio.animate().alpha(0.0F)
            floating_action_button_audio.animate().scaleXBy(1.0F)
            floating_action_button_audio.animate().scaleX(0.0F)
            floating_action_button_audio.animate().scaleYBy(1.0F)
            floating_action_button_audio.animate().scaleY(0.0F)
            floating_action_button_audio.animate().rotation(360F)
            floating_action_button_audio.animate().setDuration(200L)
            floating_action_button_audio.animate().start()
            floating_action_button.setImageResource(R.drawable.ic_add_black_24dp)
            floating_action_button.animate().rotationBy(180F)
        } else {
            isOpen = true
            floating_action_button_screenshot.visibility = View.VISIBLE
            floating_action_button_screenshot.animate().alphaBy(0.0F)
            floating_action_button_screenshot.animate().alpha(1.0F)
            floating_action_button_screenshot.animate().scaleXBy(0.0F)
            floating_action_button_screenshot.animate().scaleX(1.0F)
            floating_action_button_screenshot.animate().scaleYBy(0.0F)
            floating_action_button_screenshot.animate().scaleY(1.0F)
            floating_action_button_screenshot.animate().rotation(360F)
            floating_action_button_screenshot.animate().setDuration(200L)
            floating_action_button_screenshot.animate().start()
            floating_action_button_audio.visibility = View.VISIBLE
            floating_action_button_audio.animate().alphaBy(0.0F)
            floating_action_button_audio.animate().alpha(1.0F)
            floating_action_button_audio.animate().scaleXBy(0.0F)
            floating_action_button_audio.animate().scaleX(1.0F)
            floating_action_button_audio.animate().scaleYBy(0.0F)
            floating_action_button_audio.animate().scaleY(1.0F)
            floating_action_button_audio.animate().rotation(360F)
            floating_action_button_audio.animate().setDuration(200L)
            floating_action_button_audio.animate().start()
            floating_action_button_video.visibility = View.VISIBLE
            floating_action_button_video.animate().alphaBy(0.0F)
            floating_action_button_video.animate().alpha(1.0F)
            floating_action_button_video.animate().scaleXBy(0.0F)
            floating_action_button_video.animate().scaleX(1.0F)
            floating_action_button_video.animate().scaleYBy(0.0F)
            floating_action_button_video.animate().scaleY(1.0F)
            floating_action_button_video.animate().rotation(360F)
            floating_action_button_video.animate().setDuration(200L)
            floating_action_button_video.animate().start()
            floating_action_button.animate().rotationBy(180F)
            floating_action_button.setImageResource(R.drawable.ic_add_red_24dp)
        }
    }

    private fun checkWriteExternalStoragePermission(): Boolean {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            controlPermissionRequest = true
            requestPermissions(
                (context as Activity),
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION
            )
            return false
        }
        return true
    }

    private fun checkAudioPermission(): Boolean {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            controlPermissionRequest = true
            requestPermissions(
                (context as Activity),
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ), REQUEST_CODE_AUDIO_PERMISSION
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_AUDIO_PERMISSION) {
            Toast.makeText(context, "Permission Granted!", Toast.LENGTH_SHORT).show()
        } else if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            Toast.makeText(context, "Permission Granted!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createScreenShot(view: View): Bitmap {
        val viewScreenShot: View = (view.parent as View)
        val bitmap: Bitmap = Bitmap.createBitmap(
            viewScreenShot.width,
            viewScreenShot.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        viewScreenShot.draw(canvas)
        return bitmap
    }

    private fun takeScreenShot(view: View, context: Context) {
        if (checkWriteExternalStoragePermission()) {
            coroutineCallScreenShot.async {
                val fileDirectory: File = context.filesDir
                val filePath = File(
                    fileDirectory,
                    "logger_bird_screenshot" + System.currentTimeMillis().toString() + ".png"
                )
                try {
                    withContext(Dispatchers.IO) {
                        filePath.createNewFile()
                        val fileOutputStream = FileOutputStream(filePath)
                        createScreenShot(view = view).compress(
                            Bitmap.CompressFormat.PNG,
                            100,
                            fileOutputStream
                        )
                        fileOutputStream.close()
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "ScreenShot Taken!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(exception = e, tag = Constants.screenShotTag)
                }
            }
        } else {
            Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takeAudioRecording() {
        if (checkAudioPermission() && checkWriteExternalStoragePermission()) {
            coroutineCallAudio.async {
                try {
                    if (!audioRecording) {
                        val fileDirectory: File = context.filesDir
                        filePath = File(
                            fileDirectory,
                            "logger_bird_audio" + System.currentTimeMillis()
                                .toString() + "recording.3gpp"
                        )
                        mediaRecorderAudio = MediaRecorder()
                        mediaRecorderAudio?.setAudioSource(MediaRecorder.AudioSource.MIC)
                        mediaRecorderAudio?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        mediaRecorderAudio?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mediaRecorderAudio?.setOutputFile(filePath)
                        } else {
                            mediaRecorderAudio?.setOutputFile(filePath.path)
                        }
                        startAudioRecording()
                        floating_action_button_audio.setImageResource(R.drawable.ic_mic_off_black_24dp)
                        audioRecording = true
                    } else {
                        stopAudioRecording()
                        floating_action_button_audio.setImageResource(R.drawable.ic_mic_black_24dp)
                        audioRecording = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(
                        exception = e,
                        tag = Constants.audioRecordingTag
                    )
                }
            }
        } else {
            Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun startAudioRecording() {
        try {
            withContext(Dispatchers.IO) {
                mediaRecorderAudio?.prepare()
            }
            mediaRecorderAudio?.start()
            state = true
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Audio recording started", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.audioStartRecordingTag)
        }
    }

    private suspend fun stopAudioRecording() {
        try {
            if (state) {
                mediaRecorderAudio?.stop()
                mediaRecorderAudio?.release()
                state = false
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Audio recording finished", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.audioStopRecordingTag)
        }
    }

    private fun takeForegroundService() {
        workQueueLinked.controlRunnable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intentForegroundServiceVideo =
                Intent((context as Activity), LoggerBirdForegroundServiceVideo::class.java)
            startForegroundServiceVideo()
        } else {
            callEnqueue()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun takeVideoRecording(requestCode: Int, resultCode: Int, data: Intent?) {
        workQueueLinked.controlRunnable = true
        if (checkWriteExternalStoragePermission() && checkAudioPermission()) {
            coroutineCallVideo.async {
                try {
                    if (!videoRecording) {
                        this@LogActivityLifeCycleObserver.requestCode = requestCode
                        this@LogActivityLifeCycleObserver.resultCode = resultCode
                        this@LogActivityLifeCycleObserver.dataIntent = data
                        startScreenRecording()
                    } else {
                        stopScreenRecord()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Screen recording finished",
                                Toast.LENGTH_SHORT
                            ).show()
                            floating_action_button_video.setImageResource(R.drawable.ic_videocam_black_24dp)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callEnqueue()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(
                        exception = e,
                        tag = Constants.videoRecordingTag
                    )
                }
            }
        } else {
            Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun startScreenRecording() {
        if (!videoRecording) {
            shareScreen()
        } else {
            stopScreenRecord()
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun shareScreen() {
        projectManager =
            (context as Activity).getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (dataIntent != null && resultCode == Activity.RESULT_OK) {
            mediaProjection = projectManager!!.getMediaProjection(
                resultCode,
                dataIntent!!
            )
            initRecorder()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Screen recording started", Toast.LENGTH_SHORT)
                    .show()
                mediaProjectionCallback = MediaProjectionCallback()
                mediaProjection!!.registerCallback(mediaProjectionCallback, null)
                virtualDisplay = createVirtualDisplay()
                floating_action_button_video.setImageResource(R.drawable.ic_videocam_off_black_24dp)
                callEnqueue()
            }
        } else {
            controlPermissionRequest = true
            (context as Activity).startActivityForResult(
                projectManager?.createScreenCaptureIntent(),
                REQUEST_CODE_VIDEO
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initRecorder() {
        try {
            mediaRecorderVideo!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorderVideo!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mediaRecorderVideo!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            val fileDirectory: File = context.filesDir
            filePath = File(
                fileDirectory,
                "logger_bird_video" + System.currentTimeMillis()
                    .toString() + ".mp4"
            )
            mediaRecorderVideo!!.setOutputFile(filePath.path)
            mediaRecorderVideo!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mediaRecorderVideo!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mediaRecorderVideo!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorderVideo!!.setVideoEncodingBitRate(512 * 1000)
            mediaRecorderVideo!!.setVideoFrameRate(30)
            val rotation: Int = (context as Activity).windowManager.defaultDisplay.rotation
            val orientation: Int = ORIENTATIONS.get(rotation + 90)
            mediaRecorderVideo!!.setOrientationHint(orientation)
            mediaRecorderVideo!!.prepare()
            mediaRecorderVideo!!.start()
            videoRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
            callEnqueue()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.videoRecordingTag)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection!!.createVirtualDisplay(
            "LoggerBirdFragment",
            DISPLAY_WIDTH,
            DISPLAY_HEIGHT,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorderVideo!!.surface,
            null,
            null
        )
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun stopScreenRecord() {
        videoRecording = false
        mediaRecorderVideo!!.stop()
        mediaRecorderVideo!!.reset()
        if (virtualDisplay != null) {
            virtualDisplay!!.release()
        }
        destroyMediaProjection()
        stopForegroundServiceVideo()
        callEnqueue()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun destroyMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection!!.unregisterCallback(mediaProjectionCallback)
            mediaProjection!!.stop()
            mediaProjection = null
            videoRecording = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        super.onDestroy()
        destroyMediaProjection()
    }

    private fun checkOldCoordinates() {
        if (controlOldCoordinates) {
            Log.d("old_coordinates", floating_action_button.x.toString())
            floating_action_button.x =
                floatingActionButtonLastDx
            floating_action_button.y =
                floatingActionButtonLastDy
            floating_action_button_screenshot.x =
                floatingActionButtonScreenShotLastDx
            floating_action_button_screenshot.y =
                floatingActionButtonScreenShotLastDy
            floating_action_button_video.x =
                floatingActionButtonVideoLastDx
            floating_action_button_video.y =
                floatingActionButtonVideoLastDy
            floating_action_button_audio.x =
                floatingActionButtonAudioLastDx
            floating_action_button_audio.y =
                floatingActionButtonAudioLastDy
            controlOldCoordinates = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundServiceVideo() {
        Log.d("start_foreground", "Foreground Service started!!!!!")
        (context as Activity).startForegroundService(intentForegroundServiceVideo)
    }

    private fun stopForegroundServiceVideo() {
        (context as Activity).stopService(intentForegroundServiceVideo)
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun callVideoRecording(requestCode: Int, resultCode: Int, data: Intent?) {
        if (LoggerBird.isLogInitAttached()) {
            callForegroundService()
            if (runnableList.isEmpty()) {
                workQueueLinked.put {
                    takeVideoRecording(
                        requestCode = requestCode,
                        resultCode = resultCode,
                        data = data
                    )
                }
            }
            runnableList.add(Runnable {
                takeVideoRecording(requestCode = requestCode, resultCode = resultCode, data = data)
            })
        } else {
            throw LoggerBirdException(Constants.logInitErrorMessage)
        }
    }

    private fun callForegroundService() {
        if (LoggerBird.isLogInitAttached()) {
            if(!videoRecording){
                if (runnableList.isEmpty()) {
                    workQueueLinked.put {
                        takeForegroundService()
                    }
                }
                runnableList.add(Runnable {
                    takeForegroundService()
                })
            }
        } else {
            throw LoggerBirdException(Constants.logInitErrorMessage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
//            stopScreenRecord()
        }
    }
}