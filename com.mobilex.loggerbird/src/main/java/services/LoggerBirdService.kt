package services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding2.view.RxView
import com.mobilex.loggerbird.R
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import listeners.*
import loggerbird.LoggerBird
import observers.LogActivityLifeCycleObserver
import org.aviran.cookiebar2.CookieBar
import org.aviran.cookiebar2.CookieBarDismissListener
import paint.PaintActivity
import utils.LinkedBlockingQueueUtil
import java.io.File
import java.util.concurrent.TimeUnit


internal class  LoggerBirdService() : Service(), LoggerBirdShakeDetector.Listener {
    //Global variables:
    private lateinit var activity: Activity
    private var intentService: Intent? = null
    private lateinit var context: Context
    private lateinit var view: View
    private lateinit var rootView: View
    private var windowManager: Any? = null
    private lateinit var windowManagerParams: WindowManager.LayoutParams
    private var coroutineCallScreenShot: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAnimation: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallVideo: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAudio: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var audioRecording = false
    private var videoRecording = false
    private var mediaRecorderAudio: MediaRecorder? = null
    private var state: Boolean = false
    private lateinit var filePath: File
    private var isOpen = false
    private var screenDensity: Int = 0
    private var projectManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var mediaProjectionCallback: MediaProjectionCallback
    private var mediaRecorderVideo: MediaRecorder? = MediaRecorder()
    private var requestCode: Int = 0
    private var resultCode: Int = -1
    private var dataIntent: Intent? = null
    private lateinit var logActivityLifeCycleObserver: LogActivityLifeCycleObserver
    private lateinit var initializeFloatingActionButton: Runnable
    private lateinit var takeOldCoordinates: Runnable
    private var isFabEnable: Boolean = false
    private var isActivateDialogShown: Boolean = false


    //Static global variables:
    internal companion object {
        internal lateinit var floatingActionButtonView: View
        private lateinit var floating_action_button: FloatingActionButton
        private lateinit var floating_action_button_screenshot: FloatingActionButton
        private lateinit var floating_action_button_video: FloatingActionButton
        private lateinit var floating_action_button_audio: FloatingActionButton
        private lateinit var video_counter : Chronometer
        private lateinit var audio_counter: Chronometer
        internal var controlServiceOnDestroyState: Boolean = false
        internal var floatingActionButtonLastDx: Float? = null
        internal var floatingActionButtonScreenShotLastDx: Float? = null
        internal var floatingActionButtonVideoLastDx: Float? = null
        internal var floatingActionButtonAudioLastDx: Float? = null
        internal var floatingActionButtonLastDy: Float? = null
        internal var floatingActionButtonScreenShotLastDy: Float? = null
        internal var floatingActionButtonVideoLastDy: Float? = null
        internal var floatingActionButtonAudioLastDy: Float? = null
        internal const val REQUEST_CODE_VIDEO = 1000
        internal const val REQUEST_CODE_AUDIO_PERMISSION = 2001
        internal const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 2002
        internal const val REQUEST_CODE_DRAW_OTHER_APP_SETTINGS = 2003
        private var DISPLAY_WIDTH = 1080
        private var DISPLAY_HEIGHT = 1920
        private val ORIENTATIONS = SparseIntArray()
        internal var controlPermissionRequest: Boolean = false
        private var runnableList: ArrayList<Runnable> = ArrayList()
        private var workQueueLinked: LinkedBlockingQueueUtil = LinkedBlockingQueueUtil()
        internal var controlVideoPermission: Boolean = false
        internal var controlAudioPermission: Boolean = false
        internal var controlDrawableSettingsPermission: Boolean = false
        internal var controlWriteExternalPermission: Boolean = false
        internal lateinit var intentForegroundServiceVideo: Intent
        internal lateinit var screenshotBitmap: Bitmap
        internal lateinit var loggerBirdService: LoggerBirdService
        internal lateinit var sd: LoggerBirdShakeDetector
        internal lateinit var sensorManager:SensorManager
        internal var pauseOffset: Long = 0
        internal var pauseOffsetAudio : Long = 0
        internal var isVideoRunning : Boolean = false
        internal var isAudioRunning : Boolean = false

        internal fun callEnqueue() {
            workQueueLinked.controlRunnable = false
            if (runnableList.size > 0) {
                runnableList.removeAt(0)
                if (runnableList.size > 0) {
                    workQueueLinked.put(runnableList[0])
                }
            }

        }

        internal fun resetEnqueue() {
            runnableList.clear()
            workQueueLinked.controlRunnable = false
        }

        internal fun controlIntentForegroundServiceVideo(): Boolean {
            if (this::intentForegroundServiceVideo.isInitialized) {
                return true
            }
            return false
        }

        internal fun controlLoggerBirdServiceInit(): Boolean {
            if (Companion::loggerBirdService.isInitialized) {
                return true
            }
            return false
        }

        internal fun controlFloatingActionButtonView():Boolean{
            if(this::floatingActionButtonView.isInitialized){
                return true
            }
            return false
        }
    }

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
        loggerBirdService = this
        Log.d("service", "service_init")
    }

    /**
     * This Method Called When Service Detect's An  OnBind State In The Current Activity.
     * Parameters:
     * @param intent used for getting context reference from the Activity.
     * @return IBinder value.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * This Method Called When Service Detect's An  OnBind State In The Current Activity.
     * Parameters:
     * @param intent used for getting context reference from the Activity.
     * @param flags (no idea).
     * @param startId (no idea).
     * @return START_NOT_STICKY value.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            intentService = intent
            sensorManager =  getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sd = LoggerBirdShakeDetector(this)
            sd.start(sensorManager)
            logActivityLifeCycleObserver = LogActivityLifeCycleObserver.logActivityLifeCycleObserverInstance
            initializeActivity(activity = logActivityLifeCycleObserver.activityInstance())
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.serviceTag)
        }
        return START_NOT_STICKY
    }

    /**
     * This Method Called When Service Detect's An  OnBind State In The Current Activity.
     * Parameters:
     * @param rootIntent used for getting context reference from the Activity.
     * Variables:
     * @var currentLifeCycleState states takes current state as a String in the Activity life cycle.
     * @var onDestroyMessage used for providing detail's for stringBuilder in LoggerBird.takelifeCycleDetails.
     * Exceptions:
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     * @return IBinder value.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            controlServiceOnDestroyState = true
            LoggerBird.takeLifeCycleDetails()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.serviceTag)
        }
    }

    /**
     * This Method Called When Service Created.
     */
    override fun onCreate() {
        super.onCreate()
    }

    /**
     * This Method Called When Service Destroyed.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        super.onDestroy()
        try {
            destroyMediaProjection()
//            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    internal fun initializeActivity(activity: Activity) {
        this.activity = activity
        this.context = activity
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun hearShake() {

        if (Settings.canDrawOverlays(this.activity)) {
            initializeFloatingActionButton(activity = this.activity)
        } else {

            if (!isFabEnable) {
                if (!isActivateDialogShown) {

                    CookieBar.build(activity)
                        .setCustomView(R.layout.loggerbird_activate_popup)
                        .setTitle("Loggerbird")
                        .setMessage("Do you want to activate Loggerbird?")
                        .setIcon(R.drawable.loggerbird)
                        .setBackgroundColor(R.color.secondaryColor)
                        .setCustomViewInitializer(CookieBar.CustomViewInitializer() {

                            val txtActivate = it.findViewById<TextView>(R.id.btn_action_activate)
                            val txtDismiss = it.findViewById<TextView>(R.id.btn_action_dismiss)

                            txtActivate.setSafeOnClickListener {
                                initializeFloatingActionButton(activity = activity)
                                CookieBar.dismiss(activity)
                            }

                            txtDismiss.setSafeOnClickListener {
                                sd.stop()
                                CookieBar.dismiss(activity)
                            }

                        })
                        .setEnableAutoDismiss(false)
                        .setSwipeToDismiss(true)
                        .setCookieListener(CookieBarDismissListener {
                            isActivateDialogShown = false
                        })
                        .show()
                    isActivateDialogShown = true
                }

            }
        }
    }

    @SuppressLint("CheckResult")
    fun View.setSafeOnClickListener(onClick : (View) -> Unit) {
        RxView.clicks(this).throttleFirst(2000, TimeUnit.MILLISECONDS).subscribe {
            onClick(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    internal fun initializeFloatingActionButton(activity: Activity) {

        if (windowManager != null && this::view.isInitialized) {
            (windowManager as WindowManager).removeViewImmediate(view)
            windowManager = null

            CookieBar.build(activity)
                .setMessage("Loggerbird is closed!")
                .setBackgroundColor(R.color.secondaryColor)
                .setEnableAutoDismiss(true)
                .setSwipeToDismiss(true)
                .setDuration(1000)
                .show()
            isFabEnable = false

        }else {

            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            val view: View
            val layoutParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            view = LayoutInflater.from(activity)
                .inflate(
                    R.layout.fragment_logger_bird,
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
                windowManager = activity.getSystemService(Context.WINDOW_SERVICE)!!
                (windowManager as WindowManager).addView(view, windowManagerParams)
                this.rootView = rootView
                this.view = view
                floatingActionButtonView = view
                video_counter = view.findViewById(R.id.video_counter)
                audio_counter = view.findViewById(R.id.audio_counter)
                floating_action_button = view.findViewById(R.id.fragment_floating_action_button)
                floating_action_button_screenshot = view.findViewById(R.id.fragment_floating_action_button_screenshot)
                floating_action_button_video = view.findViewById(R.id.fragment_floating_action_button_video)
                floating_action_button_audio = view.findViewById(R.id.fragment_floating_action_button_audio)
                (floating_action_button_screenshot.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    150,
                    0,
                    0
                )
                (floating_action_button_video.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    300,
                    0,
                    0
                )

                (video_counter.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    300,
                    0,
                    0
                )

                (floating_action_button_audio.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    450,
                    0,
                    0
                )

                (audio_counter.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    450,
                    0,
                    0
                )

                if (videoRecording) {
                    floating_action_button_video.setImageResource(R.drawable.ic_videocam_off_black_24dp)
                }
                if (audioRecording) {
                    floating_action_button_audio.setImageResource(R.drawable.ic_mic_off_black_24dp)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    buttonClicks()
                }

                CookieBar.build(activity)
                    .setMessage("Loggerbird is activated!")
                    .setBackgroundColor(R.color.secondaryColor)
                    .setSwipeToDismiss(true)
                    .setEnableAutoDismiss(true)
                    .setDuration(1000)
                    .show()
                isFabEnable = true
            } else {
                checkDrawOtherAppPermission(activity = (context as Activity))
            }
        }
    }

    internal fun initializeNewActivity(activity: Activity) {
        this.activity = activity
        this.context = activity
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    private fun buttonClicks() {
        floating_action_button.setOnClickListener {
            if (!Settings.canDrawOverlays(context)) {
                checkDrawOtherAppPermission(activity = (context as Activity))
            } else {
                animationVisibility()
            }
            floating_action_button_screenshot.setOnClickListener {
                if (floating_action_button_screenshot.visibility == View.VISIBLE) {
                    if(!PaintActivity.controlPaintInPictureState){
                        takeScreenShot(view = activity.window.decorView.rootView, context = context)
                    }else{
                        Toast.makeText(context, "Please save or remove your drawing before take a screenshot", Toast.LENGTH_LONG).show()
                    }
                }
            }
            floating_action_button_audio.setOnClickListener {
                if (floating_action_button_audio.visibility == View.VISIBLE) {
                    takeAudioRecording()
                }
            }

            floating_action_button_video.setOnClickListener {
                if (floating_action_button_video.visibility == View.VISIBLE) {
                    callVideoRecording(
                        requestCode = requestCode,
                        resultCode = resultCode,
                        data = dataIntent
                    )
                }
            }
        }
        floating_action_button.setOnTouchListener(
            FloatingActionButtonOnTouchListener(
                windowManager = (windowManager as WindowManager),
                windowManagerView = view,
                windowManagerParams = windowManagerParams,
                floatingActionButton = floating_action_button,
                floatingActionButtonScreenShot = floating_action_button_screenshot,
                floatingActionButtonVideo = floating_action_button_video,
                floatingActionButtonAudio = floating_action_button_audio,
                textViewVideo = video_counter
            )
        )
    }

    private fun attachFloatingActionButtonLayoutListener() {
        floating_action_button.viewTreeObserver.addOnGlobalLayoutListener(
            FloatingActionButtonGlobalLayoutListener(
                floatingActionButton = floating_action_button
            )
        )
        floating_action_button_screenshot.viewTreeObserver.addOnGlobalLayoutListener(
            FloatingActionButtonScreenshotGlobalLayoutListener(floatingActionButtonScreenshot = floating_action_button_screenshot)
        )
        floating_action_button_video.viewTreeObserver.addOnGlobalLayoutListener(
            FloatingActionButtonVideoGlobalLayoutListener(floatingActionButtonVideo = floating_action_button_video)
        )

        floating_action_button_audio.viewTreeObserver.addOnGlobalLayoutListener(
            FloatingActionButtonAudioGlobalLayoutListener(floatingActionButtonAudio = floating_action_button_audio)
        )
    }

    private fun animationVisibility() {
        if (isOpen) {
            isOpen = false
            floating_action_button_video.animate().rotation(-360F)
            floating_action_button_video.animate().duration = 400L
            floating_action_button_video.animate().start()
            floating_action_button_screenshot.animate().rotation(-360F)
            floating_action_button_screenshot.animate().duration = 400L
            floating_action_button_screenshot.animate().start()
            floating_action_button_audio.animate().rotation(-360F)
            floating_action_button_audio.animate().duration = 400L
            floating_action_button_audio.animate().start()
            floating_action_button_screenshot.visibility = View.GONE
            floating_action_button_video.visibility = View.GONE
            floating_action_button_audio.visibility = View.GONE
            video_counter.visibility = View.GONE
            audio_counter.visibility = View.GONE
//            floating_action_button.setImageResource(R.drawable.)
        } else {
            isOpen = true
            floating_action_button_screenshot.visibility = View.VISIBLE
            floating_action_button_screenshot.animate().rotation(360F)
            floating_action_button_screenshot.animate().duration = 400L
            floating_action_button_screenshot.animate().start()
            floating_action_button_audio.visibility = View.VISIBLE
            floating_action_button_audio.animate().rotation(360F)
            floating_action_button_audio.animate().duration = 400L
            floating_action_button_audio.animate().start()
            floating_action_button_video.visibility = View.VISIBLE
            floating_action_button_video.animate().rotation(360F)
            floating_action_button_video.animate().duration = 400L
            floating_action_button_video.animate().start()
            if(isVideoRunning){
            video_counter.visibility = View.VISIBLE}
            if(isAudioRunning){
                audio_counter.visibility = View.VISIBLE}
//            floating_action_button.setImageResource(R.drawable.ic_close_black_24dp)
        }
    }

    private fun checkWriteExternalStoragePermission(): Boolean {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            controlPermissionRequest = true
            controlWriteExternalPermission = true
            ActivityCompat.requestPermissions(
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
            controlAudioPermission = true
            ActivityCompat.requestPermissions(
                (context as Activity),
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ), REQUEST_CODE_AUDIO_PERMISSION
            )
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkDrawOtherAppPermission(activity: Activity) {
        controlPermissionRequest = true
        controlDrawableSettingsPermission = true
        sd.stop()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        activity.startActivityForResult(intent, REQUEST_CODE_DRAW_OTHER_APP_SETTINGS)
    }

    private fun createScreenShot(view: View): Bitmap {
        val viewScreenShot: View = view
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
            PaintActivity.closeActivitySession()
            coroutineCallScreenShot.async {
                val fileDirectory: File = context.filesDir
                var byteArray: ByteArray? = null
//                val filePath = File(
//                    fileDirectory,
//                    "logger_bird_screenshot" + System.currentTimeMillis().toString() + ".png"
//                )
                try {
                    withContext(Dispatchers.IO) {
                        //                        filePath.createNewFile()
//                        val fileOutputStream = FileOutputStream(filePath)
//                        createScreenShot(view = view).compress(
//                            Bitmap.CompressFormat.PNG,
//                            100,
//                            fileOutputStream
//                        )
//                        fileOutputStream.close()
//                        val bStream = ByteArrayOutputStream()
//                        createScreenShot(view = view).compress(Bitmap.CompressFormat.PNG, 100, bStream)
//                        byteArray = bStream.toByteArray()

                        screenshotBitmap = createScreenShot(view = view)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Screenshot taken!", Toast.LENGTH_SHORT).show()
                        val paintActivity = PaintActivity()
                        val screenshotIntent = Intent(
                            context as Activity,
                            paintActivity.javaClass
                        )

                        context.startActivity(screenshotIntent)
                        context.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)

//                        val loggerBirdPaintService = LoggerBirdPaintService()
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            loggerBirdPaintService.initializeActivity(activity = activity)
//                        }
//                        val screenshotServiceIntent=Intent(context,loggerBirdPaintService.javaClass)
//                        context.startService(screenshotServiceIntent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(exception = e, tag = Constants.screenShotTag)
                }
            }
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
                audio_counter.visibility = View.VISIBLE
                audioChoronometerReset()
                audioChoronometerStart()
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
                    audioChoronometerStop()
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
            resetEnqueue()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun takeVideoRecording(requestCode: Int, resultCode: Int, data: Intent?) {
        workQueueLinked.controlRunnable = true
        if (checkWriteExternalStoragePermission() && checkAudioPermission()) {
            coroutineCallVideo.async {
                try {
                    if (!videoRecording) {
                        this@LoggerBirdService.requestCode = requestCode
                        this@LoggerBirdService.resultCode = resultCode
                        this@LoggerBirdService.dataIntent = data
                        startScreenRecording()
                    } else {
                        stopScreenRecord()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Screen recording finished", Toast.LENGTH_SHORT).show()
                            videoChoronometerStop()

                            floating_action_button_video.setImageResource(R.drawable.ic_videocam_black_24dp)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    resetEnqueue()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(
                        exception = e,
                        tag = Constants.videoRecordingTag
                    )
                }
            }
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
            (context as Activity).getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (dataIntent != null && resultCode == Activity.RESULT_OK) {
            mediaProjection = projectManager!!.getMediaProjection(
                resultCode,
                dataIntent!!
            )
            initRecorder()
            withContext(Dispatchers.Main) {
                mediaProjectionCallback = MediaProjectionCallback()
                mediaProjection!!.registerCallback(mediaProjectionCallback, null)
                virtualDisplay = createVirtualDisplay()
                Toast.makeText(context, "Screen recording started", Toast.LENGTH_SHORT).show()
                video_counter.visibility = View.VISIBLE
                videoChoronometerReset()
                videoChoronometerStart()
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

    private fun videoChoronometerStart(){
        if (!isVideoRunning) {
            video_counter.setBase(SystemClock.elapsedRealtime() - pauseOffset)
            video_counter.start()
            isVideoRunning = true
        }
    }

    private fun videoChoronometerStop() {
        if (isVideoRunning) {
            video_counter.stop()
            pauseOffset = SystemClock.elapsedRealtime() - video_counter.getBase()
            isVideoRunning = false
        }
    }

    private fun videoChoronometerReset() {
        video_counter.setBase(SystemClock.elapsedRealtime())
        pauseOffset = 0
    }

    private fun audioChoronometerStart(){
        if (!isAudioRunning) {
            audio_counter.setBase(SystemClock.elapsedRealtime() - pauseOffsetAudio)
            audio_counter.start()
            isAudioRunning = true
        }
    }

    private fun audioChoronometerStop() {
        if (isAudioRunning) {
            audio_counter.stop()
            pauseOffsetAudio = SystemClock.elapsedRealtime() - audio_counter.getBase()
            isAudioRunning = false
        }
    }

    private fun audioChoronometerReset() {
        audio_counter.setBase(SystemClock.elapsedRealtime())
        pauseOffsetAudio = 0
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
                "logger_bird_video" + System.currentTimeMillis().toString() + ".mp4"
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
        val metrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(metrics)
        screenDensity = metrics.densityDpi
        DISPLAY_HEIGHT = metrics.heightPixels
        DISPLAY_WIDTH = metrics.widthPixels
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
        if (videoRecording) {
            mediaRecorderVideo!!.stop()
            mediaRecorderVideo!!.reset()
        }
        if (virtualDisplay != null) {
            virtualDisplay!!.release()
        }
        destroyMediaProjection()
        stopForegroundServiceVideo()
        videoRecording = false
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
//            if(this::intentForegroundServiceVideo.isInitialized){
//                stopForegroundServiceVideo()
//            }
            workQueueLinked.controlRunnable = false
            runnableList.clear()
            workQueueLinked.clear()
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
            if (!videoRecording) {
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