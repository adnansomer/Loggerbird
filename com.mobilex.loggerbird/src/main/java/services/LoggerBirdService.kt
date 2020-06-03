package services

import adapter.RecyclerViewJiraAdapter
import adapter.RecyclerViewSlackAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.*
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.circularreveal.CircularRevealLinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import models.RecyclerViewModel
import observers.LogActivityLifeCycleObserver
import org.aviran.cookiebar2.CookieBar
import paint.PaintActivity
import paint.PaintView
import utils.*
import utils.EmailUtil
import utils.LinkedBlockingQueueUtil
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

internal class LoggerBirdService() : Service(), LoggerBirdShakeDetector.Listener {
    //Global variables:
    private lateinit var activity: Activity
    private var intentService: Intent? = null
    private lateinit var context: Context
    private lateinit var view: View
    private lateinit var rootView: View
    private var windowManager: Any? = null
    private var windowManagerProgressBar: Any? = null
    private var windowManagerFeedback: Any? = null
    private var windowManagerJira: Any? = null
    private var windowManagerSlack: Any? = null
    //private var windowManagerJiraAuth: Any? = null
    private lateinit var windowManagerParams: WindowManager.LayoutParams
    private lateinit var windowManagerParamsFeedback: WindowManager.LayoutParams
    private lateinit var windowManagerParamsProgressBar: WindowManager.LayoutParams
    private lateinit var windowManagerParamsJira: WindowManager.LayoutParams
    private lateinit var windowManagerParamsJiraAuth: WindowManager.LayoutParams
    private lateinit var windowManagerParamsSlack: WindowManager.LayoutParams
    private var coroutineCallScreenShot: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAnimation: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallVideo: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAudio: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallVideoStarter = CoroutineScope(Dispatchers.IO)
    private val coroutineCallSendSingleFile: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineCallDiscardFile: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var mediaRecorderAudio: MediaRecorder? = null
    private var state: Boolean = false
    private lateinit var filePathVideo: File
    private lateinit var filePathAudio: File
    private var isOpen = false
    //private lateinit var fabOpen: Animation
    //private lateinit var fabClose: Animation
    private var screenDensity: Int = 0
    private var projectManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjectionCallback: MediaProjectionCallback? = null
    private var mediaRecorderVideo: MediaRecorder? = null
    private var requestCode: Int = 0
    private var resultCode: Int = -1
    private var dataIntent: Intent? = null
    private lateinit var logActivityLifeCycleObserver: LogActivityLifeCycleObserver
    private lateinit var initializeFloatingActionButton: Runnable
    private lateinit var takeOldCoordinates: Runnable
    private var isFabEnable: Boolean = false
    private var isActivateDialogShown: Boolean = false
    private var coroutineCallVideoCounter: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAudioCounter: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallVideoFileSize: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAudioFileSize: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallFeedback: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var counterVideo: Int = 0
    private var counterAudio: Int = 0
    private var timerVideo: Timer? = null
    private var timerAudio: Timer? = null
    private var timerVideoFileSize: Timer? = null
    private var timerAudioFileSize: Timer? = null
    private var timerTaskVideo: TimerTask? = null
    private var timerTaskAudio: TimerTask? = null
    private var timerVideoTaskFileSize: TimerTask? = null
    private var timerAudioTaskFileSize: TimerTask? = null
    private var fileSizeFormatter: Formatter = Formatter()
    private lateinit var cookieBar: CookieBar
    private lateinit var viewFeedback: View
    private lateinit var viewJira: View
    private lateinit var viewSlack: View
    //  private lateinit var viewJiraAuth: View
    private lateinit var wrapper: FrameLayout
    private lateinit var floating_action_button_feedback: FloatingActionButton
    private lateinit var floating_action_button_feed_close: FloatingActionButton
    private lateinit var editText_feedback: EditText
    private val fileLimit: Long = 10485760
    private var sessionTimeStart: Long? = System.currentTimeMillis()
    private var sessionTimeEnd: Long? = null
    private var timeControllerVideo: Long? = null
    private var controlTimeControllerVideo: Boolean = false
    private lateinit var mediaCodecsFile: File
    private val arrayListFileName: ArrayList<String> = ArrayList()
    private val coroutineCallFilesAction: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var controlFileAction: Boolean = false
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarView: View

    //Jira:
    internal val jiraAuthentication = JiraAuthentication()
    private val slackAuthentication = SlackAuthentication()
    //    private lateinit var spinnerProject: Spinner
    private lateinit var autoTextViewProject: AutoCompleteTextView
    //    private lateinit var spinnerIssueType: Spinner
    private lateinit var autoTextViewIssueType: AutoCompleteTextView
    private lateinit var recyclerViewJiraAttachment: RecyclerView
    private lateinit var layout_jira_summary: TextInputLayout
    private lateinit var editTextSummary: EditText
    private lateinit var editTextDescription: EditText
    //  private lateinit var editTextJiraAuthMail: EditText
//  private lateinit var editTextJiraAuthPassword: EditText
//    private lateinit var spinnerReporter: Spinner
    private lateinit var autoTextViewReporter: AutoCompleteTextView
    //    private lateinit var spinnerLinkedIssue: Spinner
    private lateinit var autoTextViewLinkedIssue: AutoCompleteTextView
    //    private lateinit var spinnerIssue: Spinner
    private lateinit var autoTextViewIssue: AutoCompleteTextView
    //    private lateinit var spinnerAssignee: Spinner
    private lateinit var autoTextViewAssignee: AutoCompleteTextView
    //    private lateinit var spinnerPriority: Spinner
    private lateinit var autoTextViewPriority: AutoCompleteTextView
    //    private lateinit var spinnerComponent: Spinner
    private lateinit var autoTextViewComponent: AutoCompleteTextView
    //    private lateinit var spinnerFixVersions: Spinner
    private lateinit var autoTextViewFixVersions: AutoCompleteTextView
    //    private lateinit var spinnerLabel: Spinner
    private lateinit var autoTextViewLabel: AutoCompleteTextView
    //    private lateinit var spinnerEpicLink: Spinner
    private lateinit var autoTextViewEpicLink: AutoCompleteTextView
    //    private lateinit var spinnerSprint: Spinner
    private lateinit var autoTextViewSprint: AutoCompleteTextView
    private lateinit var buttonJiraCreate: Button
    internal lateinit var buttonJiraCancel: Button
    //  private lateinit var buttonJiraAuthCancel: Button
//  private lateinit var buttonJiraAuthNext: Button
    private lateinit var layoutJira: FrameLayout
    private lateinit var toolbarJira: Toolbar
    private lateinit var progressBarJira: ProgressBar
    private lateinit var progressBarJiraLayout: FrameLayout
    private lateinit var cardViewSprint:CardView
    //    private lateinit var layoutJiraAuth: LinearLayout
    private val arrayListJiraFileName: ArrayList<RecyclerViewModel> = ArrayList()
    //    private val arrayListJiraProject: ArrayList<String> = ArrayList()
//    private val arrayListJiraIssueType: ArrayList<String> = ArrayList()
//    private val arrayListJiraReporter: ArrayList<String> = ArrayList()
//    private val arrayListJiraLinkedIssue: ArrayList<String> = ArrayList()
//    private val arrayListJiraAssignee: ArrayList<String> = ArrayList()
//    private val arrayListJiraPriority: ArrayList<String> = ArrayList()
    private lateinit var jiraAdapter: RecyclerViewJiraAdapter
    //    private lateinit var spinnerProjectAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewProjectAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerIssueTypeAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewIssueTypeAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerReporterAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewReporterAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerLinkedIssueAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewLinkedIssueAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerIssueAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewIssueAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerAssigneeAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewAssigneeAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerPriorityAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewPriorityAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerFixVersionsAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewFixVersionsAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerComponentAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewComponentAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerLabelAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewLabelAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerEpicLinkAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewEpicLinkAdapter: ArrayAdapter<String>
    //    private lateinit var spinnerSprintAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewSprintAdapter: ArrayAdapter<String>
    private var projectPosition = 0

    //Slack:
    private lateinit var buttonSlackCreate: Button
    internal lateinit var buttonSlackCancel: Button
    private lateinit var spinnerChannels: Spinner
    private lateinit var spinnerUsers: Spinner
    private lateinit var editTextMessage: EditText
    private lateinit var spinnerChannelsAdapter: ArrayAdapter<String>
    private lateinit var spinnerUsersAdapter: ArrayAdapter<String>
    private lateinit var slackAdapter: RecyclerViewSlackAdapter
    private lateinit var recyclerViewSlackAttachment: RecyclerView
    private val arrayListSlackFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var progressBarSlack: ProgressBar
    private lateinit var progressBarSlackLayout: FrameLayout
    private val defaultToast :DefaultToast = DefaultToast()


    //Static global variables:
    internal companion object {
        internal lateinit var floatingActionButtonView: View
        lateinit var floating_action_button: FloatingActionButton
        internal lateinit var filePathMedia: File
        private lateinit var floating_action_button_screenshot: FloatingActionButton
        private lateinit var floating_action_button_video: FloatingActionButton
        private lateinit var floating_action_button_audio: FloatingActionButton
        private lateinit var reveal_linear_layout_share: CircularRevealLinearLayout
        private lateinit var textView_send_email: TextView
        private lateinit var textView_share_jira: TextView
        private lateinit var textView_share_slack: TextView
        private lateinit var textView_discard: TextView
        //private lateinit var textView_dismiss : TextView
        private lateinit var textView_counter_video: TextView
        private lateinit var textView_counter_audio: TextView
        private lateinit var textView_video_size: TextView
        private lateinit var textView_audio_size: TextView
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
        internal lateinit var sensorManager: SensorManager
        internal var audioRecording = false
        internal var videoRecording = false
        internal var screenshotDrawing = false

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

        internal fun controlFloatingActionButtonView(): Boolean {
            if (this::floatingActionButtonView.isInitialized) {
                return true
            }
            return false
        }

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        internal fun callShareView(filePathMedia: File? = null) {
            if (filePathMedia != null) {
                loggerBirdService.activity.runOnUiThread {
                    loggerBirdService.shareView(filePathMedia = filePathMedia)
                }
            } else {
                loggerBirdService.activity.runOnUiThread {
                    reveal_linear_layout_share.visibility = View.GONE
                    floating_action_button.animate()
                        .rotationBy(360F)
                        .setDuration(200)
                        .scaleX(1F)
                        .scaleY(1F)
                        .withEndAction {
                            floating_action_button.setImageResource(R.drawable.loggerbird)
                            floating_action_button.animate()
                                .rotationBy(0F)
                                .setDuration(200)
                                .scaleX(1F)
                                .scaleY(1F)
                                .start()
                        }
                        .start()
                }
            }
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
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            sd = LoggerBirdShakeDetector(this)
            sd.start(sensorManager)
            logActivityLifeCycleObserver =
                LogActivityLifeCycleObserver.logActivityLifeCycleObserverInstance
            initializeActivity(activity = logActivityLifeCycleObserver.activityInstance())
            controlActionFiles()
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
            dailySessionTimeRecorder()
            addFileList()
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
//        if (activity is AppCompatActivity) {
//            initializeFloatingActionButton(activity = activity)
//        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    internal fun initializeFloatingActionButton(activity: Activity) {

        if (windowManager != null && this::view.isInitialized) {
            (windowManager as WindowManager).removeViewImmediate(view)

            CookieBar.build(activity)
                .setCustomView(R.layout.loggerbird_close_popup)
                .setCustomViewInitializer {
                    val textViewFeedBack = it.findViewById<TextView>(R.id.textView_feed_back_pop_up)
                    textViewFeedBack.setSafeOnClickListener {
                        initializeFeedBackLayout()
                        CookieBar.dismiss(activity)
                    }
                }
                .setSwipeToDismiss(true)
                .setDuration(2000)
                .show()
            windowManager = null
            isFabEnable = false
        } else {
            removeFeedBackLayout()
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

            view.scaleX = 0F
            view.scaleY = 0F
            view.animate()
                .scaleX(1F)
                .scaleY(1F)
                .setDuration(500)
                .setInterpolator(BounceInterpolator())
                .setStartDelay(0)
                .start()

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
                floating_action_button = view.findViewById(R.id.fragment_floating_action_button)
                floating_action_button_screenshot =
                    view.findViewById(R.id.fragment_floating_action_button_screenshot)
                floating_action_button_video =
                    view.findViewById(R.id.fragment_floating_action_button_video)
                floating_action_button_audio =
                    view.findViewById(R.id.fragment_floating_action_button_audio)
                reveal_linear_layout_share = view.findViewById(R.id.reveal_linear_layout_share)
                textView_send_email = view.findViewById(R.id.textView_send_email)
                textView_discard = view.findViewById(R.id.textView_discard)
                textView_share_jira = view.findViewById(R.id.textView_share_jira)
                textView_share_slack = view.findViewById(R.id.textView_share_slack)
                //textView_dismiss = view.findViewById(R.id.textView_dismiss)
                textView_counter_video = view.findViewById(R.id.fragment_textView_counter_video)
                textView_counter_audio = view.findViewById(R.id.fragment_textView_counter_audio)
                textView_video_size = view.findViewById(R.id.fragment_textView_size_video)
                textView_audio_size = view.findViewById(R.id.fragment_textView_size_audio)

                (floating_action_button_screenshot.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    450,
                    0,
                    0
                )
                (floating_action_button_video.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    150,
                    0,
                    0
                )
                (textView_counter_video.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    150,
                    0,
                    0
                )
                (textView_video_size.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    150,
                    0,
                    0
                )
                (floating_action_button_audio.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    300,
                    0,
                    0
                )
                (textView_counter_audio.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    300,
                    0,
                    0
                )
                (textView_audio_size.layoutParams as FrameLayout.LayoutParams).setMargins(
                    0,
                    300,
                    0,
                    0
                )

                if (videoRecording) {
                    floating_action_button_video.visibility = View.GONE
                }
                if (audioRecording) {
                    floating_action_button_audio.visibility = View.GONE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    buttonClicks()
                }
                CookieBar.build(activity)
                    .setCustomView(R.layout.loggerbird_start_popup)
                    .setCustomViewInitializer {
                        val textViewSessionTime =
                            it.findViewById<TextView>(R.id.textView_session_time_pop_up)
                        textViewSessionTime.text =
                            resources.getString(R.string.total_session_time) + timeStringDay(
                                totalSessionTime()
                            ) + "\n" + resources.getString(R.string.last_session_time) + timeStringDay(
                                lastSessionTime()
                            )
                    }
                    .setSwipeToDismiss(true)
                    .setEnableAutoDismiss(true)
                    .setDuration(3000)
                    .show()
                isFabEnable = true

            } else {
                checkDrawOtherAppPermission(activity = (context as Activity))
            }
        }
    }

    private fun timeStringDay(remainingSeconds: Long): String {
        return String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toDays(remainingSeconds),
            TimeUnit.MILLISECONDS.toHours(remainingSeconds) - TimeUnit.DAYS.toHours(
                TimeUnit.MILLISECONDS.toDays(remainingSeconds)
            ),
            TimeUnit.MILLISECONDS.toMinutes(remainingSeconds) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    remainingSeconds
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(remainingSeconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    remainingSeconds
                )
            )
        )
    }

    private fun timeStringHour(remainingSeconds: Long): String {
        return String.format(
            Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(remainingSeconds),
            TimeUnit.MILLISECONDS.toMinutes(remainingSeconds) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    remainingSeconds
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(remainingSeconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    remainingSeconds
                )
            )
        )
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
        }
        floating_action_button_screenshot.setSafeOnClickListener {
            if (floating_action_button_screenshot.visibility == View.VISIBLE) {
                if (!PaintActivity.controlPaintInPictureState) {
                    if (!audioRecording && !videoRecording) {

                        takeScreenShot(view = activity.window.decorView.rootView, context = context)
                    } else {
                        Toast.makeText(context, R.string.media_recording_error, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        R.string.screen_shot_picture_in_picture_warning_message,
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }
        }
        floating_action_button_audio.setSafeOnClickListener {
            if (floating_action_button_audio.visibility == View.VISIBLE) {
                if (!videoRecording && !screenshotDrawing) {
                    takeAudioRecording()
                } else {
                    Toast.makeText(context, R.string.media_recording_error, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        textView_counter_audio.setSafeOnClickListener {
            if (textView_counter_audio.visibility == View.VISIBLE) {
                takeAudioRecording()
                shareView(filePathMedia = filePathAudio)
            }
        }

        floating_action_button_video.setSafeOnClickListener {
            if (floating_action_button_video.visibility == View.VISIBLE) {
                if (!audioRecording && !screenshotDrawing) {
                    callVideoRecording(
                        requestCode = requestCode,
                        resultCode = resultCode,
                        data = dataIntent
                    )
                } else {
                    Toast.makeText(context, R.string.media_recording_error, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        textView_counter_video.setSafeOnClickListener {
            if (textView_counter_video.visibility == View.VISIBLE) {
                callVideoRecording(
                    requestCode = requestCode,
                    resultCode = resultCode,
                    data = dataIntent
                )
                shareView(filePathMedia = filePathVideo)

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
                textViewCounterVideo = textView_counter_video,
                textViewCounterAudio = textView_counter_audio,
                textViewVideoSize = textView_video_size,
                textViewAudiosize = textView_audio_size,
                revealLinearLayoutShare = reveal_linear_layout_share
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun shareView(filePathMedia: File) {
        floating_action_button.animate()
            .rotationBy(360F)
            .setDuration(200)
            .scaleX(1F)
            .scaleY(1F)
            .withEndAction {
                floating_action_button.setImageResource(R.drawable.ic_share_black_24dp)
                floating_action_button.animate()
                    .rotationBy(0F)
                    .setDuration(200)
                    .scaleX(1F)
                    .scaleY(1F)
                    .start()
            }
            .start()
        floating_action_button_video.visibility = View.GONE
        floating_action_button_screenshot.visibility = View.GONE
        floating_action_button_audio.visibility = View.GONE
        textView_counter_audio.visibility = View.GONE
        textView_counter_video.visibility = View.GONE
        reveal_linear_layout_share.visibility = View.VISIBLE
        shareViewClicks(filePathMedia = filePathMedia)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun shareViewClicks(filePathMedia: File) {
        if (reveal_linear_layout_share.isVisible) {
            textView_send_email.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    attachProgressBar()
                }
                sendSingleMediaFile(filePathMedia = filePathMedia)
            }

            textView_share_jira.setOnClickListener {
                //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    attachProgressBar()
//                }
//                jiraAuthentication.callJiraIssue(
//                    filePathName = filePathMedia,
//                    context = context,
//                    activity = activity
//                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }

                    initializeJiraLayout(filePathMedia = filePathMedia)
//                    initializeJiraAuthLayout(filePathMedia = filePathMedia)
                }
            }

            textView_share_slack.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }

                    initializeSlackLayout(filePathMedia = filePathMedia)
                }
            }

            textView_discard.setOnClickListener {
                //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    attachProgressBar()
//                }
                discardMediaFile()
            }
        }
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
            floating_action_button_video.animate()
                .rotation(-360F)
                .setDuration(500L)
                .start()
            floating_action_button_screenshot.animate()
                .rotation(-360F)
                .setDuration(500L)
                .start()
            floating_action_button_audio.animate()
                .rotation(-360F)
                .setDuration(500L)
                .start()
            floating_action_button_screenshot.visibility = View.GONE
            floating_action_button_video.visibility = View.GONE
            textView_counter_video.visibility = View.GONE
            textView_video_size.visibility = View.GONE
            floating_action_button_audio.visibility = View.GONE
            textView_counter_audio.visibility = View.GONE
            textView_audio_size.visibility = View.GONE
        } else {
            isOpen = true
            floating_action_button_screenshot.visibility = View.VISIBLE
            floating_action_button_screenshot.animate()
                .rotation(360F)
                .setDuration(500L)
                .start()
            if (audioRecording) {
                textView_counter_audio.visibility = View.VISIBLE
                textView_audio_size.visibility = View.VISIBLE
            } else {
                floating_action_button_audio.visibility = View.VISIBLE
            }
            floating_action_button_audio.animate()
                .rotation(360F)
                .setDuration(500L)
                .start()
            if (videoRecording) {
                textView_counter_video.visibility = View.VISIBLE
                textView_video_size.visibility = View.VISIBLE
            } else {
                floating_action_button_video.visibility = View.VISIBLE
            }
            floating_action_button_video.animate()
                .rotation(360F)
                .setDuration(500L)
                .start()
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun takeScreenShot(view: View, context: Context) {
        if (checkWriteExternalStoragePermission()) {
            PaintActivity.closeActivitySession()
            coroutineCallScreenShot.async {
                try {
                    if (arrayListFileName.size <= 10) {
                        withContext(Dispatchers.IO) {
                            screenshotBitmap = createScreenShot(view = view)
                        }
                        withContext(Dispatchers.Main) {
                            screenshotDrawing = true
                            Toast.makeText(context, R.string.screen_shot_taken, Toast.LENGTH_SHORT)
                                .show()
                            val paintActivity = PaintActivity()
                            val screenshotIntent = Intent(
                                context as Activity,
                                paintActivity.javaClass
                            )
                            floating_action_button.animate()
                                .rotationBy(360F)
                                .setDuration(200)
                                .scaleX(1F)
                                .scaleY(1F)
                                .withEndAction {
                                    floating_action_button.setImageResource(R.drawable.ic_photo_camera_black_24dp)
                                    floating_action_button.animate()
                                        .rotationBy(0F)
                                        .setDuration(200)
                                        .scaleX(1F)
                                        .scaleY(1F)
                                        .start()
                                }
                                .start()
                            context.startActivity(screenshotIntent)
                            context.overridePendingTransition(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                        }

                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, R.string.session_file_limit, Toast.LENGTH_SHORT)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(exception = e, tag = Constants.screenShotTag)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun takeAudioRecording() {
        if (checkAudioPermission() && checkWriteExternalStoragePermission()) {
            coroutineCallAudio.async {
                try {
                    if (!audioRecording) {
                        if (arrayListFileName.size <= 10) {
                            val fileDirectory: File = context.filesDir
                            filePathAudio = File(
                                fileDirectory,
                                "logger_bird_audio" + System.currentTimeMillis().toString() + "recording.3gpp"
                            )
                            addFileNameList(fileName = filePathAudio.absolutePath)
                            mediaRecorderAudio = MediaRecorder()
                            mediaRecorderAudio?.setAudioSource(MediaRecorder.AudioSource.MIC)
                            mediaRecorderAudio?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                            mediaRecorderAudio?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mediaRecorderAudio?.setOutputFile(filePathAudio)
                            } else {
                                mediaRecorderAudio?.setOutputFile(filePathAudio.path)
                            }
                            startAudioRecording()
                            audioRecording = true
                            withContext(Dispatchers.Main) {
                                floating_action_button_audio.visibility = View.GONE
                                textView_counter_audio.visibility = View.VISIBLE
                                textView_audio_size.visibility = View.VISIBLE

                                floating_action_button.animate()
                                    .rotationBy(360F)
                                    .setDuration(300)
                                    .scaleX(1F)
                                    .scaleY(1F)
                                    .withEndAction {
                                        floating_action_button.setImageResource(R.drawable.ic_mic_black_24dp)
                                        floating_action_button.animate()
                                            .rotationBy(0F)
                                            .setDuration(200)
                                            .scaleX(1F)
                                            .scaleY(1F)
                                            .start()
                                    }
                                    .start()

                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    R.string.session_file_limit,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            textView_counter_audio.visibility = View.GONE
                            textView_audio_size.visibility = View.GONE
                        }
                        stopAudioRecording()
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private suspend fun startAudioRecording() {
        try {
            withContext(Dispatchers.IO) {
                mediaRecorderAudio?.prepare()
            }
            mediaRecorderAudio?.start()
            state = true
            withContext(Dispatchers.Main) {

                Toast.makeText(context, R.string.audio_recording_start, Toast.LENGTH_SHORT).show()
            }
            audioCounterStart()
            takeAudioFileSize()
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
                audioCounterStop()
                stopAudioFileSize()
                state = false
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.audio_recording_finish, Toast.LENGTH_SHORT)
                        .show()
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
                        if (arrayListFileName.size <= 10) {
                            this@LoggerBirdService.requestCode = requestCode
                            this@LoggerBirdService.resultCode = resultCode
                            this@LoggerBirdService.dataIntent = data
                            startScreenRecording()
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    R.string.session_file_limit,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        if (this@LoggerBirdService::filePathVideo.isInitialized) {
                            if (this@LoggerBirdService.filePathVideo.length() > 0 || controlTimeControllerVideo) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        R.string.screen_recording_finish,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    videoCounterStop()
                                    textView_counter_video.visibility = View.GONE
                                    textView_video_size.visibility = View.GONE
                                    floating_action_button_video.setImageResource(R.drawable.ic_videocam_black_24dp)
                                    if (controlTimeControllerVideo) {
                                        filePathVideo.delete()
                                        controlTimeControllerVideo = false
                                    }
                                    withContext(Dispatchers.IO) {
                                        stopScreenRecord()
                                    }
                                }
                            }
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
            withContext(Dispatchers.Main) {
                mediaProjectionCallback = MediaProjectionCallback()
                mediaProjection!!.registerCallback(mediaProjectionCallback, null)
                Toast.makeText(context, R.string.screen_recording_start, Toast.LENGTH_SHORT).show()
                floating_action_button_video.visibility = View.GONE
                textView_counter_video.visibility = View.VISIBLE
                textView_video_size.visibility = View.VISIBLE
                floating_action_button.animate()
                    .rotationBy(360F)
                    .setDuration(200)
                    .scaleX(1F)
                    .scaleY(1F)
                    .withEndAction {
                        floating_action_button.setImageResource(R.drawable.ic_videocam_black_24dp)
                        floating_action_button.animate()
                            .rotationBy(0F)
                            .setDuration(200)
                            .scaleX(1F)
                            .scaleY(1F)
                            .start()
                    }
                    .start()

                callEnqueue()
            }
            initRecorder()
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
            coroutineCallVideoStarter.async {
                mediaRecorderVideo = MediaRecorder()
                if (mediaRecorderVideo != null) {
                    mediaRecorderVideo?.setAudioSource(MediaRecorder.AudioSource.MIC)
                    mediaRecorderVideo?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
                    mediaRecorderVideo?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    val fileDirectory: File = context.filesDir
                    filePathVideo = File(
                        fileDirectory,
                        "logger_bird_video" + System.currentTimeMillis().toString() + ".mp4"
                    )
                    addFileNameList(fileName = filePathVideo.absolutePath)
                    mediaCodecsFile = File("/data/misc/media/media_codecs_profiling_results.xml")
                    mediaRecorderVideo?.setOutputFile(filePathVideo.path)
                    mediaRecorderVideo?.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                    mediaRecorderVideo?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    mediaRecorderVideo?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    mediaRecorderVideo?.setVideoEncodingBitRate(1000000000)
                    mediaRecorderVideo?.setVideoFrameRate(120)
                    val rotation: Int = (context as Activity).windowManager.defaultDisplay.rotation
                    val orientation: Int = ORIENTATIONS.get(rotation + 90)
                    mediaRecorderVideo?.setOrientationHint(orientation)
                    withContext(Dispatchers.IO) {
                        mediaRecorderVideo?.prepare()
                        mediaRecorderVideo?.start()
                        videoRecording = true
                        videoCounterStart()
                        if (mediaRecorderVideo != null) {
                            virtualDisplay = createVirtualDisplay()
                        } else {
                            callVideoRecording(
                                requestCode = requestCode,
                                resultCode = resultCode,
                                data = dataIntent
                            )
                        }
                        takeVideoFileSize()
                    }
                }
            }
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
    private suspend fun stopScreenRecord() {
        try {
            withContext(coroutineCallVideoStarter.coroutineContext) {
                if (videoRecording) {
                    if (mediaRecorderVideo != null) {
                        mediaRecorderVideo?.stop()
                        mediaRecorderVideo?.reset()
                    }
                }
                if (virtualDisplay != null) {
                    virtualDisplay?.release()
                }
                destroyMediaProjection()
                stopForegroundServiceVideo()
                videoRecording = false
                stopVideoFileSize()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun destroyMediaProjection() {
        try {
            if (mediaProjection != null) {
                mediaProjection!!.unregisterCallback(mediaProjectionCallback)
                mediaProjectionCallback = null
                mediaProjection!!.stop()
                mediaProjection = null
                videoRecording = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundServiceVideo() {
        Log.d("start_foreground", "Foreground Service started!!!!!")
        (context as Activity).startForegroundService(intentForegroundServiceVideo)
    }

    private fun stopForegroundServiceVideo() {
        try {
            (context as Activity).stopService(intentForegroundServiceVideo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun callVideoRecording(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
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
                    takeVideoRecording(
                        requestCode = requestCode,
                        resultCode = resultCode,
                        data = data
                    )
                })
            } else {
                throw LoggerBirdException(Constants.logInitErrorMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.videoRecordingTag)
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun hearShake() {
        try {
            Log.d("shake", "shake fired!!")
            if (Settings.canDrawOverlays(this.activity)) {
                if (!controlFileAction) {

                    initializeFloatingActionButton(activity = this.activity)
                } else {
                    Toast.makeText(
                        context,
                        R.string.files_action_limit,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if (!isFabEnable) {
                    if (!isActivateDialogShown) {
                        cookieBar = CookieBar.build(this.activity)
                            .setTitle(resources.getString(R.string.library_name))
                            .setMessage(resources.getString(R.string.logger_bird_floating_action_button_permission_message))
                            .setCustomView(R.layout.loggerbird_activate_popup)
                            .setIcon(R.drawable.loggerbird)
                            .setSwipeToDismiss(true)
                            .setCookieListener { isActivateDialogShown = false }
                            .setEnableAutoDismiss(false)
                            .setCustomViewInitializer(CookieBar.CustomViewInitializer() {
                                val txtActivate =
                                    it.findViewById<TextView>(R.id.btn_action_activate)
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
                            .show()
                        isActivateDialogShown = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.shakerTag)
        }
    }

    @SuppressLint("CheckResult")
    fun View.setSafeOnClickListener(onClick: (View) -> Unit) {
        RxView.clicks(this).throttleFirst(2000, TimeUnit.MILLISECONDS).subscribe {
            onClick(this)
        }
    }

    private fun takeVideoFileSize() {
        coroutineCallVideoFileSize.async {
            try {
                timerVideoFileSize = Timer()
                timerVideoTaskFileSize = object : TimerTask() {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun run() {
                        val fileSize = filePathVideo.length()
                        val sizePrintVideo =
                            android.text.format.Formatter.formatShortFileSize(
                                activity,
                                fileSize
                            )
                        if (fileSize > fileLimit) {
                            timerVideoTaskFileSize?.cancel()
                            activity.runOnUiThread {
                                textView_counter_video.performClick()
                            }
                        }
                        activity.runOnUiThread {
                            textView_video_size.text = sizePrintVideo
                        }
                        Log.d("file_size_video", sizePrintVideo)
                    }
                }
                timerVideoFileSize!!.schedule(timerVideoTaskFileSize, 0, 1000)
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.videoRecordingFileSizeTag
                )
            }
        }
    }

    private fun stopVideoFileSize() {
        try {
            timerVideoTaskFileSize?.cancel()
            timerVideoFileSize?.cancel()
            timerVideoTaskFileSize = null
            timerVideoFileSize = null
            activity.runOnUiThread {
                textView_video_size.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.videoRecordingFileSizeTag
            )
        }
    }

    private fun takeAudioFileSize() {
        coroutineCallAudioFileSize.async {
            try {
                timerAudioFileSize = Timer()
                timerAudioTaskFileSize = object : TimerTask() {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun run() {
                        val fileSize = filePathAudio.length()
                        val sizePrintAudio =
                            android.text.format.Formatter.formatShortFileSize(
                                activity,
                                fileSize
                            )
                        if (fileSize > fileLimit) {
                            timerAudioTaskFileSize?.cancel()
                            activity.runOnUiThread {
                                textView_counter_audio.performClick()
                            }
                        }
                        activity.runOnUiThread {
                            textView_audio_size.text = sizePrintAudio
                        }
                        Log.d("file_size_audio", sizePrintAudio)
                    }
                }
                timerAudioFileSize!!.schedule(timerAudioTaskFileSize, 0, 1000)
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.audioRecordingFileSizeTag
                )
            }
        }
    }

    private fun stopAudioFileSize() {
        try {
            timerAudioTaskFileSize?.cancel()
            timerAudioFileSize?.cancel()
            timerAudioTaskFileSize = null
            timerAudioFileSize = null
            activity.runOnUiThread {
                textView_audio_size.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.audioRecordingFileSizeTag
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeFeedBackLayout() {
        try {
            if (windowManagerFeedback != null && this::viewFeedback.isInitialized) {
                (windowManagerFeedback as WindowManager).removeViewImmediate(viewFeedback)
            }
            viewFeedback = LayoutInflater.from(activity)
                .inflate(
                    R.layout.loggerbird_feedback,
                    (this.rootView as ViewGroup),
                    false
                )
            if (Settings.canDrawOverlays(activity)) {
                windowManagerParamsFeedback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                    )
                } else {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                    )
                }

                windowManagerFeedback = activity.getSystemService(Context.WINDOW_SERVICE)!!
                if (windowManagerFeedback != null) {
                    (windowManagerFeedback as WindowManager).addView(
                        viewFeedback,
                        windowManagerParamsFeedback
                    )
                    floating_action_button_feedback =
                        viewFeedback.findViewById(R.id.floating_action_button_feed)
                    floating_action_button_feed_close =
                        viewFeedback.findViewById(R.id.floating_action_button_feed_dismiss)
                    editText_feedback = viewFeedback.findViewById(R.id.editText_feed_back)
                    buttonClicksFeedback()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.feedbackTag)
        }
    }

    private fun removeFeedBackLayout() {
        if (windowManagerFeedback != null && this::viewFeedback.isInitialized) {
            (windowManagerFeedback as WindowManager).removeViewImmediate(viewFeedback)
            windowManagerFeedback = null
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buttonClicksFeedback() {
        val layoutFeedbackOnTouchListener: LayoutFeedbackOnTouchListener =
            LayoutFeedbackOnTouchListener(
                windowManager = (windowManagerFeedback as WindowManager),
                windowManagerView = viewFeedback,
                windowManagerParams = windowManagerParamsFeedback
            )
        (editText_feedback).setOnTouchListener(
            layoutFeedbackOnTouchListener
        )
        floating_action_button_feedback.setOnTouchListener(layoutFeedbackOnTouchListener)
        floating_action_button_feedback.setSafeOnClickListener {
            sendFeedback()
        }
        floating_action_button_feed_close.setSafeOnClickListener {
            removeFeedBackLayout()
        }

    }

    private fun sendFeedback() {
        if (editText_feedback.text.trim().isNotEmpty()) {
            removeFeedBackLayout()
            coroutineCallFeedback.async {
                EmailUtil.sendFeedbackEmail(
                    context = context,
                    message = editText_feedback.text.toString()
                )
//                withContext(Dispatchers.Main){
//                    Toast.makeText(
//                        context,
//                        R.string.feed_back_email_success,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
            }
        } else {
            Toast.makeText(
                context,
                R.string.feed_back_email_empty_text,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun videoCounterStart() {
        coroutineCallVideoCounter.async {
            try {
                withContext(Dispatchers.Main) {
                    textView_counter_video.visibility = View.VISIBLE
                }
                counterVideo = 0
                timerVideo = Timer()
                timerTaskVideo = object : TimerTask() {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun run() {
                        val currentDate = Calendar.getInstance().time
                        if (timeControllerVideo != null && !controlTimeControllerVideo) {
                            if ((currentDate.time - timeControllerVideo!!) > 5000) {
                                controlTimeControllerVideo = true
                                Log.d("current_time", controlTimeControllerVideo.toString())
//                                timeControllerVideo = null
//                                timerTaskVideo?.cancel()
                                activity.runOnUiThread {
                                    textView_counter_video.performClick()
                                }
                            }
                        }
                        if (!controlTimeControllerVideo) {
                            timeControllerVideo = currentDate.time
                            Log.d("current_time", timeControllerVideo.toString())
                            Log.d("current_time", controlTimeControllerVideo.toString())
                            val counterTime = (counterVideo * 1000).toLong()
                            counterVideo++
                            activity.runOnUiThread {
                                textView_counter_video.text = timeStringHour(counterTime)
                            }
                        }
                    }
                }
                timerVideo!!.schedule(
                    timerTaskVideo, 0, 1000
                )
            } catch (e: Exception) {
                e.printStackTrace()
                activity.runOnUiThread {
                    textView_counter_video.performClick()
                }
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.videoRecordingCounterTag
                )
            }

        }
    }

    private fun audioCounterStart() {
        coroutineCallAudioCounter.async {
            try {
                withContext(Dispatchers.Main) {
                    textView_counter_audio.visibility = View.VISIBLE
                }
                counterAudio = 0
                timerAudio = Timer()
                timerTaskAudio = object : TimerTask() {
                    override fun run() {
                        val counterTimer = (counterAudio * 1000).toLong()
                        counterAudio++
                        activity.runOnUiThread {
                            textView_counter_audio.text = timeStringHour(counterTimer)
                        }
                    }
                }
                timerAudio!!.schedule(
                    timerTaskAudio, 0, 1000
                )
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = e,
                    tag = Constants.audioRecordingCounterTag
                )
            }

        }
    }

    private fun videoCounterStop() {
        try {
            activity.runOnUiThread {
                if (timerTaskVideo != null) {
                    timerTaskVideo?.cancel()
                }
                if (timerVideo != null) {
                    timerVideo?.cancel()
                }
                timerTaskVideo = null
                timerVideo = null
                timeControllerVideo = null
                textView_counter_video.visibility = View.GONE
                val counterZero = 0
                textView_counter_video.text = counterZero.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.videoRecordingCounterTag
            )
        }
    }

    private fun audioCounterStop() {
        try {
            activity.runOnUiThread {
                timerTaskAudio?.cancel()
                timerAudio?.cancel()
                timerTaskAudio = null
                timerAudio = null
                textView_counter_audio.visibility = View.GONE
                val counterZero = 0
                textView_counter_audio.text = counterZero.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            activity.runOnUiThread {
                textView_counter_video.performClick()
            }
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.audioRecordingCounterTag
            )
        }
    }

    private fun dailySessionTimeRecorder() {
        sessionTimeEnd = System.currentTimeMillis()
        if (sessionTimeEnd != null && sessionTimeStart != null) {
            val sessionDuration = sessionTimeEnd!! - sessionTimeStart!!
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext) ?: return
            with(sharedPref.edit()) {
                putLong("session_time", sharedPref.getLong("session_time", 0) + sessionDuration)
                commit()
            }
            with(sharedPref.edit()) {
                putLong("last_session_time", sessionDuration)
                commit()
            }
        }
    }

    private fun totalSessionTime(): Long {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        return sharedPref.getLong("session_time", 0)
    }

    private fun lastSessionTime(): Long {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        return sharedPref.getLong("last_session_time", 0)
    }

    private fun addFileList() {
        if (getFileList() != null) {
            arrayListFileName.addAll(getFileList()!!)
        }
        arrayListFileName.addAll(PaintView.arrayListFileNameScreenshot)
        val gson = Gson()
        val json = gson.toJson(arrayListFileName)
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext) ?: return
        with(sharedPref.edit()) {
            putString("file_quantity", json)
            commit()
        }
    }

    private fun getFileList(): ArrayList<String>? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        val gson = Gson()
        val json = sharedPref.getString("file_quantity", "")
        if (json?.isNotEmpty()!!) {
            return gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
        }
        return null
    }

    private fun addFileNameList(fileName: String) {
        arrayListFileName.add(fileName)
    }

    private fun deleteOldFilesList() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove("file_quantity")
        editor.apply()
        activity.runOnUiThread {
            Toast.makeText(
                context,
                R.string.files_action_delete_success,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun controlActionFiles() {
        if (getFileList() != null) {
            if (getFileList()!!.size > 10) {
                chooseActionFiles()
            }
        }
    }

    private fun chooseActionFiles() {
        this.controlFileAction = true
        CookieBar.build(activity)
            .setCustomView(R.layout.loggerbird_file_action_popup)
            .setCustomViewInitializer {
                val textViewDiscard = it.findViewById<TextView>(R.id.textView_files_action_discard)
                textViewDiscard.setSafeOnClickListener {
                    if (this.controlFileAction) {
                        this.controlFileAction = false
                        CookieBar.dismiss(activity)
                        deleteOldFiles()
                    }
                }
                val textViewEmail = it.findViewById<TextView>(R.id.textView_files_action_mail)
                textViewEmail.setSafeOnClickListener {
                    if (this.controlFileAction) {
                        this.controlFileAction = false
                        CookieBar.dismiss(activity)
                        sendOldFilesEmail()
                    }
                }
            }
            .setSwipeToDismiss(false)
            .setEnableAutoDismiss(false)
            .show()
    }

    internal fun deleteOldFiles(controlEmailAction: Boolean? = null) {
        val arrayListOldFiles: ArrayList<String> = ArrayList()
        if (getFileList() != null) {
            arrayListOldFiles.addAll(getFileList()!!)
            if (arrayListOldFiles.size > 10) {
                var fileName: File
                var fileCounter = 0
                do {
                    fileName = File(arrayListOldFiles[fileCounter])
                    if (fileName.exists()) {
                        fileName.delete()
                    }
                    fileCounter++
                    if (fileCounter == arrayListOldFiles.size) {
                        if (controlEmailAction != null) {
                            activity.runOnUiThread {
                                Toast.makeText(
                                    context,
                                    R.string.files_action_mail_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        break
                    }
                } while (arrayListOldFiles.iterator().hasNext())
                deleteOldFilesList()
            }

        }
    }

    private fun sendOldFilesEmail() {
        coroutineCallFilesAction.async {
            try {
                val arrayListOldFiles: ArrayList<String> = ArrayList()
                if (getFileList() != null) {
                    arrayListOldFiles.addAll(getFileList()!!)
                    var fileName: File
                    var fileCounter = 0
                    do {
                        fileName = File(arrayListOldFiles[fileCounter])
                        if (fileName.exists()) {
                            LoggerBird.callEmailSender(context = context, file = fileName)
                        }
                        fileCounter++
                        if (fileCounter == arrayListOldFiles.size) {
                            LoggerBird.deleteOldFiles(this@LoggerBirdService)
                            break
                        }
                    } while (arrayListOldFiles.iterator().hasNext())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.actionFileTag)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun discardMediaFile() {
        coroutineCallDiscardFile.async {
            try {
                if (this@LoggerBirdService::filePathVideo.isInitialized) {
                    try {
                        if (filePathVideo.exists()) {
                            filePathVideo.delete()
                            finishShareLayout(message = "media")
                        } else {
                            finishShareLayout(message = "media_error")
                        }
                    } catch (e: FileNotFoundException) {
                        finishShareLayout(message = "media_error")
                        e.printStackTrace()
                    }
                }

                try {
                    if (this@LoggerBirdService::filePathAudio.isInitialized) {
                        if (filePathAudio.exists()) {
                            filePathAudio.delete()
                            finishShareLayout(message = "media")
                        } else {
                            finishShareLayout(message = "media_error")
                        }
                    }
                } catch (e: FileNotFoundException) {
                    finishShareLayout(message = "media_error")
                    e.printStackTrace()
                }


                try {
                    if (PaintView.filePathScreenShot.exists()) {
                        PaintView.filePathScreenShot.delete()
                        finishShareLayout(message = "media")
                    } else {
                        finishShareLayout(message = "media_error")
                    }
                } catch (e: FileNotFoundException) {
                    finishShareLayout(message = "media_error")
                    e.printStackTrace()
                }


            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.discardFileTag)
                LoggerBird.callEnqueue()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun sendSingleMediaFile(filePathMedia: File) {
        coroutineCallSendSingleFile.async {
            try {
                if (filePathMedia.exists()) {
                    LoggerBird.callEmailSender(context = context, file = filePathMedia)
                    LoggerBird.deleteSingleMediaFile(
                        this@LoggerBirdService,
                        filePathMedia = filePathMedia
                    )
                } else {
                    finishShareLayout("single_email_error")
                }
            } catch (e: Exception) {
                finishShareLayout("single_email_error")
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.singleFileDeleteTag)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun finishShareLayout(message: String) {
        activity.runOnUiThread {
            when (message) {
                "media" -> {
                    Toast.makeText(context, R.string.share_media_delete, Toast.LENGTH_SHORT).show()
                    finishSuccessFab()
                    //detachProgressBar()
                }
                "media_error" -> {
                    Toast.makeText(context, R.string.share_media_delete_error, Toast.LENGTH_SHORT)
                        .show()
                    finishErrorFab()
                    //detachProgressBar()
                }
                "single_email" -> {
                    Toast.makeText(context, R.string.share_file_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab()
                    detachProgressBar()
                }
                "single_email_error" -> {
                    Toast.makeText(context, R.string.share_file_sent_error, Toast.LENGTH_SHORT)
                        .show()
                    finishErrorFab()
                    detachProgressBar()
                }
                "jira" -> {
                    Toast.makeText(context, R.string.jira_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab()
                }
                "jira_error" -> {
                    removeJiraLayout()
                    Toast.makeText(context, R.string.jira_sent_error, Toast.LENGTH_SHORT).show()
                    progressBarJiraLayout.visibility = View.GONE
                    progressBarJira.visibility = View.GONE
//                    detachProgressBar()
                    finishErrorFab()
                }
                "slack" -> {
                    Toast.makeText(context, R.string.slack_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab()
                }
                "slack_error" -> {
                    removeSlackLayout()
                    Toast.makeText(context, R.string.slack_sent_error, Toast.LENGTH_SHORT).show()
                    progressBarSlackLayout.visibility = View.GONE
                    progressBarSlack.visibility = View.GONE
                    finishErrorFab()
                }
            }
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun finishSuccessFab() {

        reveal_linear_layout_share.visibility = View.GONE
        Handler().postDelayed({
            floating_action_button.animate()
                .rotationBy(360F)
                .setDuration(200)
                .scaleX(1F)
                .scaleY(1F)
                .withEndAction {
                    floating_action_button.setImageResource(R.drawable.ic_done_black_24dp)
                    floating_action_button.animate()
                        .rotationBy(0F)
                        .setDuration(200)
                        .scaleX(1F)
                        .scaleY(1F)
                        .start()
                }
                .start()
        }, 0)

        Handler().postDelayed({
            floating_action_button.animate()
                .rotationBy(360F)
                .setDuration(200)
                .scaleX(1F)
                .scaleY(1F)
                .withEndAction {
                    floating_action_button.setImageResource(R.drawable.loggerbird)
                    floating_action_button.animate()
                        .rotationBy(0F)
                        .setDuration(200)
                        .scaleX(1F)
                        .scaleY(1F)
                        .start()
                }
                .start()

        }, 2500)

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun finishErrorFab() {
        reveal_linear_layout_share.visibility = View.GONE
        Handler().postDelayed({
            floating_action_button.animate()
                .rotationBy(360F)
                .setDuration(200)
                .scaleX(1F)
                .scaleY(1F)
                .withEndAction {
                    floating_action_button.setImageResource(R.drawable.ic_close_black_24dp)
                    floating_action_button.animate()
                        .rotationBy(0F)
                        .setDuration(200)
                        .scaleX(1F)
                        .scaleY(1F)
                        .start()
                }
                .start()
        }, 0)

        Handler().postDelayed({
            floating_action_button.animate()
                .rotationBy(360F)
                .setDuration(200)
                .scaleX(1F)
                .scaleY(1F)
                .withEndAction {
                    floating_action_button.setImageResource(R.drawable.loggerbird)
                    floating_action_button.animate()
                        .rotationBy(0F)
                        .setDuration(200)
                        .scaleX(1F)
                        .scaleY(1F)
                        .start()
                }
                .start()

        }, 2500)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun deleteSingleMediaFile(controlEmailAction: Boolean? = null, filePathMedia: File) {
        if (filePathMedia.exists()) {
            filePathMedia.delete()
        }

        finishShareLayout("single_email")
        LoggerBird.callEnqueue()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun attachProgressBar() {
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        progressBarView =
            LayoutInflater.from(activity).inflate(R.layout.default_progressbar, rootView, false)
        windowManagerParamsProgressBar = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
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
        windowManagerProgressBar = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerProgressBar as WindowManager).addView(
            progressBarView,
            windowManagerParamsProgressBar
        )
        progressBarView.isClickable = false
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun detachProgressBar() {
        if (this::progressBarView.isInitialized) {
            (windowManagerProgressBar as WindowManager).removeViewImmediate(progressBarView)
        }
    }

    internal fun returnActivity(): Activity {
        return activity
    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun initializeJiraAuthLayout(filePathMedia: File) {
//        try {
//            if (windowManagerJiraAuth != null && this::viewJiraAuth.isInitialized) {
//                (windowManagerJiraAuth as WindowManager).removeViewImmediate(viewJiraAuth)
////                arrayListJiraFileName.clear()
//            }
//            viewJiraAuth = LayoutInflater.from(activity)
//                .inflate(
//                    R.layout.loggerbird_jira_init_popup,
//                    (this.rootView as ViewGroup),
//                    false
//                )
//            if (Settings.canDrawOverlays(activity)) {
//                windowManagerParamsJiraAuth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    WindowManager.LayoutParams(
//                        WindowManager.LayoutParams.MATCH_PARENT,
//                        WindowManager.LayoutParams.MATCH_PARENT,
//                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//                        PixelFormat.TRANSLUCENT
//                    )
//                } else {
//                    WindowManager.LayoutParams(
//                        WindowManager.LayoutParams.MATCH_PARENT,
//                        WindowManager.LayoutParams.MATCH_PARENT,
//                        WindowManager.LayoutParams.TYPE_APPLICATION,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//                        PixelFormat.TRANSLUCENT
//                    )
//                }
//
//                windowManagerJiraAuth = activity.getSystemService(Context.WINDOW_SERVICE)!!
//                if (windowManagerJiraAuth != null) {
//                    (windowManagerJiraAuth as WindowManager).addView(
//                        viewJiraAuth,
//                        windowManagerParamsJiraAuth
//                    )
//                    if (Build.VERSION.SDK_INT >= 21) {
//                        activity.window.navigationBarColor = ContextCompat.getColor(
//                            this,
//                            R.color.black
//                        )
//                        activity.window.statusBarColor = ContextCompat.getColor(
//                            this,
//                            R.color.black
//                        )
//                    }
//                    progressBarJira = viewJiraAuth.findViewById(R.id.progressbar_jira)
//                    buttonJiraAuthNext = viewJiraAuth.findViewById(R.id.button_jira_auth_next)
//                    buttonJiraAuthCancel = viewJiraAuth.findViewById(R.id.button_jira_auth_cancel)
//                    editTextJiraAuthMail = viewJiraAuth.findViewById(R.id.editText_jira_init_email)
//                    editTextJiraAuthPassword =
//                        viewJiraAuth.findViewById(R.id.editText_jira_init_key)
//                    layoutJiraAuth = viewJiraAuth.findViewById(R.id.layout_jira_auth)
//                    buttonClicksJiraAuth(filePathMedia = filePathMedia)
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            LoggerBird.callEnqueue()
//            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
//        }
//    }

//    private fun removeJiraAuthLayout() {
//        if (windowManagerJiraAuth != null && this::viewJiraAuth.isInitialized) {
//            (windowManagerJiraAuth as WindowManager).removeViewImmediate(viewJiraAuth)
//            windowManagerJiraAuth = null
//        }
//    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeJiraLayout(filePathMedia: File) {
        try {
            if (windowManagerJira != null && this::viewJira.isInitialized) {
                (windowManagerJira as WindowManager).removeViewImmediate(viewJira)
                arrayListJiraFileName.clear()
            }
            viewJira = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_jira_popup, (this.rootView as ViewGroup), false)

            if (Settings.canDrawOverlays(activity)) {
                windowManagerParamsJira = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                    )
                } else {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                    )
                }

                windowManagerJira = activity.getSystemService(Context.WINDOW_SERVICE)!!

                if (windowManagerJira != null) {
                    (windowManagerJira as WindowManager).addView(
                        viewJira,
                        windowManagerParamsJira
                    )

                    if (Build.VERSION.SDK_INT >= 23) {
                        activity.window.navigationBarColor =
                            resources.getColor(R.color.black, theme)
                        activity.window.statusBarColor = resources.getColor(R.color.black, theme)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            activity.window.navigationBarColor = resources.getColor(R.color.black)
                            activity.window.statusBarColor = resources.getColor(R.color.black)
                        }
                    }

//                    spinnerProject = viewJira.findViewById(R.id.spinner_jira_project)
                    autoTextViewProject = viewJira.findViewById(R.id.auto_textView_jira_project)
//                    spinnerIssueType = viewJira.findViewById(R.id.spinner_jira_issue_type)
                    autoTextViewIssueType =
                        viewJira.findViewById(R.id.auto_textView_jira_issue_type)
                    recyclerViewJiraAttachment =
                        viewJira.findViewById(R.id.recycler_view_jira_attachment)
                    editTextSummary = viewJira.findViewById(R.id.editText_jira_summary)
                    editTextDescription = viewJira.findViewById(R.id.editText_jira_description)
//                    spinnerReporter = viewJira.findViewById(R.id.spinner_jira_issue_reporter)
                    autoTextViewReporter = viewJira.findViewById(R.id.auto_textView_jira_reporter)
//                    spinnerLinkedIssue =
//                        viewJira.findViewById(R.id.spinner_jira_issue_linked_issues)
                    autoTextViewLinkedIssue =
                        viewJira.findViewById(R.id.auto_textView_jira_linked_issues)
//                    spinnerIssue = viewJira.findViewById(R.id.spinner_jira_issue_issues)
                    autoTextViewIssue = viewJira.findViewById(R.id.auto_textView_jira_issues)
//                    spinnerAssignee = viewJira.findViewById(R.id.spinner_jira_issue_assignee)
                    autoTextViewAssignee = viewJira.findViewById(R.id.auto_textView_jira_assignee)
//                    spinnerPriority = viewJira.findViewById(R.id.spinner_jira_issue_priority)
                    autoTextViewPriority = viewJira.findViewById(R.id.auto_textView_jira_priority)
//                    spinnerComponent = viewJira.findViewById(R.id.spinner_jira_issue_component)
                    autoTextViewComponent = viewJira.findViewById(R.id.auto_textView_jira_component)
//                    spinnerFixVersions = viewJira.findViewById(R.id.spinner_jira_issue_fix_versions)
                    autoTextViewFixVersions =
                        viewJira.findViewById(R.id.auto_textView_jira_fix_versions)
//                    spinnerLabel = viewJira.findViewById(R.id.spinner_jira_labels)
                    autoTextViewLabel = viewJira.findViewById(R.id.auto_textView_jira_labels)
//                    spinnerEpicLink = viewJira.findViewById(R.id.spinner_jira_epic_link)
                    autoTextViewEpicLink = viewJira.findViewById(R.id.auto_textView_jira_epic_link)
//                    spinnerSprint = viewJira.findViewById(R.id.spinner_jira_sprint)
                    autoTextViewSprint = viewJira.findViewById(R.id.auto_textView_jira_sprint)
                    buttonJiraCreate = viewJira.findViewById(R.id.button_jira_create)
                    buttonJiraCancel = viewJira.findViewById(R.id.button_jira_cancel)
                    toolbarJira = viewJira.findViewById(R.id.textView_jira_title)
                    layoutJira = viewJira.findViewById(R.id.layout_jira)
                    progressBarJira = viewJira.findViewById(R.id.jira_progressbar)
                    progressBarJiraLayout = viewJira.findViewById(R.id.jira_progressbar_background)
                    cardViewSprint = viewJira.findViewById(R.id.cardView_sprint)

                    val sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                    editTextSummary.setText(sharedPref.getString("jira_summary", null))
                    editTextDescription.setText(sharedPref.getString("jira_description", null))

                    jiraAuthentication.callJiraIssue(
                        context = context,
                        activity = activity,
                        jiraTask = "get",
                        createMethod = "normal"
                    )
//                    initializeJiraSpinner(
//                        jiraAuthentication.getArrayListProjects(),
//                        jiraAuthentication.getArrayListIssueTypes(),
//                        jiraAuthentication.getArrayListReporter(),
//                        jiraAuthentication.getArrayListIssueLinkedTypes(),
//                        jiraAuthentication.getArrayListIssues(),
//                        jiraAuthentication.getArrayListAsignee(),
//                        jiraAuthentication.getArrayListPriorities(),
//                        jiraAuthentication.getArrayListComponent(),
//                        jiraAuthentication.getArrayListFixVersions(),
//                        jiraAuthentication.getArrayListLabel(),
//                        jiraAuthentication.getArrayListEpicLink(),
//                        jiraAuthentication.getArrayListSprint()
//                    )

                    initializeJiraRecyclerView(filePathMedia = filePathMedia)
                    buttonClicksJira(filePathMedia = filePathMedia)
//                    attachProgressBar()
                    progressBarJiraLayout.visibility = View.VISIBLE
                    progressBarJira.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
    }

    private fun removeJiraLayout() {
        if (windowManagerJira != null && this::viewJira.isInitialized) {
            (windowManagerJira as WindowManager).removeViewImmediate(viewJira)
            windowManagerJira = null
            arrayListJiraFileName.clear()
//
//            removeJiraSpinner(
//                jiraAuthentication.getArrayListProjects(),
//                jiraAuthentication.getArrayListIssueTypes(),
//                jiraAuthentication.getArrayListReporter(),
//                jiraAuthentication.getArrayListIssueLinkedTypes(),
//                jiraAuthentication.getArrayListIssues(),
//                jiraAuthentication.getArrayListAsignee(),
//                jiraAuthentication.getArrayListPriorities(),
//                jiraAuthentication.getArrayListComponent(),
//                jiraAuthentication.getArrayListFixVersions(),
//                jiraAuthentication.getArrayListLabel(),
//                jiraAuthentication.getArrayListEpicLink(),
//                jiraAuthentication.getArrayListSprint())
        }
    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun buttonClicksJiraAuth(filePathMedia: File) {
//        buttonJiraAuthNext.setSafeOnClickListener {
//            removeJiraAuthLayout()
//            initializeJiraLayout(filePathMedia = filePathMedia)
//        }
//
//        buttonJiraAuthCancel.setSafeOnClickListener {
//            removeJiraAuthLayout()
//        }
//    }
//
//    private fun dispatchKeyEvent(keyEvent: KeyEvent){
//        val action = keyEvent.action
//        val keyCode = keyEvent.keyCode
//
//
//            switch (keyCode) {
//            case KeyEvent.KEYCODE_BACK:
//                if (action == KeyEvent.ACTION_DOWN ){
//            //Do something in the back button
//            Log.d("BackButton","back");
//                }
//                return true;
//            default:
//                return super.dispatchKeyEvent(event);
//              }
//
//    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun buttonClicksJira(filePathMedia: File) {
        layoutJira.setOnTouchListener(
            LayoutJiraOnTouchListener(
                windowManager = (windowManagerJira as WindowManager),
                windowManagerView = viewJira,
                windowManagerParams = windowManagerParamsJira
            )
        )

        buttonJiraCreate.setSafeOnClickListener {
            jiraAuthentication.gatherJiraSpinnerDetails(
                autoTextViewProject = autoTextViewProject,
//                spinnerProject = spinnerProject,
                autoTextViewIssueType = autoTextViewIssueType,
//                spinnerIssueType = spinnerIssueType,
                autoTextViewLinkedIssues = autoTextViewLinkedIssue,
//                spinnerLinkedIssues = spinnerLinkedIssue,
                autoTextViewIssues = autoTextViewIssue,
//                spinnerIssues = spinnerIssue,
                autoTextViewAssignee = autoTextViewAssignee,
//                spinnerAssignee = spinnerAssignee,
                autoTextViewReporter = autoTextViewReporter,
//                spinnerReporter = spinnerReporter,
                autoTextViewPriority = autoTextViewPriority,
//                spinnerPriority = spinnerPriority,
                autoTextViewComponent = autoTextViewComponent,
//                spinnerComponent = spinnerComponent,
                autoTextViewFixVersions = autoTextViewFixVersions,
//                spinnerFixVersions = spinnerFixVersions,
                autoTextViewLabel = autoTextViewLabel,
//                spinnerLabel = spinnerLabel,
                autoTextViewEpicLink = autoTextViewEpicLink,
//                spinnerEpicLink = spinnerEpicLink,
                autoTextViewSprint = autoTextViewSprint
//                spinnerSprint = spinnerSprint
            )
            jiraAuthentication.gatherJiraEditTextDetails(
                editTextSummary = editTextSummary,
                editTextDescription = editTextDescription
            )
            jiraAuthentication.gatherJiraRecyclerViewDetails(arrayListRecyclerViewItems = arrayListJiraFileName)
            if (jiraAuthentication.checkSummaryEmpty(activity = activity, context = context)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    progressBarJira.visibility = View.VISIBLE
                    progressBarJiraLayout.visibility = View.VISIBLE
//                    attachProgressBar()
                }
                hideKeyboard(activity = activity)
                jiraAuthentication.callJiraIssue(
                    filePathName = filePathMedia,
                    context = context,
                    activity = activity,
                    jiraTask = "create",
                    createMethod = "normal"
                )
            }
        }

        toolbarJira.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.jira_menu_save -> {
                    val sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                    with(sharedPref.edit()) {
                        putString("jira_summary", editTextSummary.text.toString())
                        putString("jira_description", editTextDescription.text.toString())
                        commit()
                    }
                    Toast.makeText(
                        viewJira.context,
                        "Issue preferences are saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                R.id.jira_menu_clear -> {
                    val sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                    val editor: SharedPreferences.Editor = sharedPref.edit()
                    editor.remove("jira_summary")
                    editor.remove("jira_description")
                    editor.apply()
                    editTextDescription.text = null
                    editTextSummary.text = null
                    Toast.makeText(
                        viewJira.context,
                        "Issue preferences are deleted!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return@setOnMenuItemClickListener true
        }

        toolbarJira.setNavigationOnClickListener {
            removeJiraLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }

        buttonJiraCancel.setSafeOnClickListener {
            removeJiraLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }

    private fun addJiraFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        arrayListJiraFileName.add(RecyclerViewModel(file = filePathMedia))
        arrayListJiraFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        return arrayListJiraFileName
    }

    private fun initializeJiraRecyclerView(filePathMedia: File) {
        recyclerViewJiraAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        jiraAdapter = RecyclerViewJiraAdapter(
            addJiraFileNames(filePathMedia = filePathMedia),
            context = context,
            activity = activity,
            rootView = rootView
        )
        recyclerViewJiraAttachment.adapter = jiraAdapter
    }

    private fun removeJiraFileNames(): ArrayList<RecyclerViewModel> {
        arrayListJiraFileName.clear()
        jiraAdapter.notifyDataSetChanged()
        arrayListJiraFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        return arrayListJiraFileName
    }

    private fun removeJiraSpinner(
        arrayListProjectNames: ArrayList<String>,
        arrayListIssueTypes: ArrayList<String>,
        arrayListReporterNames: ArrayList<String>,
        arrayListLinkedIssues: ArrayList<String>,
        arrayListIssues: ArrayList<String>,
        arrayListAssignee: ArrayList<String>,
        arrayListPriority: ArrayList<String>,
        arrayListComponent: ArrayList<String>,
        arrayListFixVersions: ArrayList<String>,
        arrayListLabel: ArrayList<String>,
        arrayListEpicLink: ArrayList<String>,
        arrayListSprint: ArrayList<String>
    ) {

        arrayListProjectNames.clear()
        arrayListIssueTypes.clear()
        arrayListReporterNames.clear()
        arrayListLinkedIssues.clear()
        arrayListIssues.clear()
        arrayListAssignee.clear()
        arrayListPriority.clear()
        arrayListComponent.clear()
        arrayListFixVersions.clear()
        arrayListLabel.clear()
        arrayListEpicLink.clear()
        arrayListSprint.clear()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeJiraSpinner(
        arrayListProjectNames: ArrayList<String>,
        arrayListProjectKeys:ArrayList<String>,
        arrayListIssueTypes: ArrayList<String>,
        arrayListReporterNames: ArrayList<String>,
        arrayListLinkedIssues: ArrayList<String>,
        arrayListIssues: ArrayList<String>,
        arrayListAssignee: ArrayList<String>,
        arrayListPriority: ArrayList<String>,
        arrayListComponent: ArrayList<String>,
        arrayListFixVersions: ArrayList<String>,
        arrayListLabel: ArrayList<String>,
        arrayListEpicLink: ArrayList<String>,
        arrayListSprint: ArrayList<String>,
        hashMapBoardList:HashMap<String,String>
    ) {

        try {
            autoTextViewProjectAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListProjectNames
            )
            autoTextViewProject.setAdapter(autoTextViewProjectAdapter)
            if (arrayListProjectNames.isNotEmpty() && autoTextViewProject.text.isEmpty()) {
                autoTextViewProject.setText(arrayListProjectNames[0], false)
            }
            autoTextViewProject.setOnTouchListener { v, event ->
                autoTextViewProject.showDropDown()
                false
            }
            autoTextViewProject.setOnItemClickListener { parent, view, position, id ->
                projectPosition = position
                jiraAuthentication.setProjectPosition(projectPosition = position)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    progressBarJira.visibility = View.VISIBLE
                    progressBarJiraLayout.visibility = View.VISIBLE
//                    attachProgressBar()
                }
                hideKeyboard(activity = activity)
                jiraAuthentication.callJiraIssue(
                    context = context,
                    activity = activity,
                    jiraTask = "get",
                    createMethod = "normal"
                )

            }
//        spinnerProjectAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListProjectNames)
//        spinnerProjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerProject.adapter = spinnerProjectAdapter
            autoTextViewIssueTypeAdapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListIssueTypes)
            autoTextViewIssueType.setAdapter(autoTextViewIssueTypeAdapter)
            if (arrayListIssueTypes.isNotEmpty()) {
                autoTextViewIssueType.setText(arrayListIssueTypes[0], false)
            }
            autoTextViewIssueType.setOnTouchListener { v, event ->
                autoTextViewIssueType.showDropDown()
                false
            }
            autoTextViewIssueType.setOnItemClickListener { parent, view, position, id ->
                jiraAuthentication.setIssueTypePosition(issueTypePosition = position)
            }
//        spinnerIssueTypeAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListIssueTypes)
//        spinnerIssueTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerIssueType.adapter = spinnerIssueTypeAdapter
            autoTextViewReporterAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListReporterNames
            )
            autoTextViewReporter.setAdapter(autoTextViewReporterAdapter)
//            if (arrayListReporterNames.isNotEmpty()) {
//                autoTextViewReporter.setText(arrayListReporterNames[0], false)
//            }
            autoTextViewReporter.setOnTouchListener { v, event ->
                autoTextViewReporter.showDropDown()
                false
            }
            autoTextViewReporter.setOnItemClickListener { parent, view, position, id ->
                jiraAuthentication.setReporterPosition(reporterPosition = position)
            }

//        spinnerReporterAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListReporterNames)
//        spinnerReporterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerReporter.adapter = spinnerReporterAdapter

            autoTextViewLinkedIssueAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListLinkedIssues
            )
            autoTextViewLinkedIssue.setAdapter(autoTextViewLinkedIssueAdapter)
            if (arrayListLinkedIssues.isNotEmpty()) {
                autoTextViewLinkedIssue.setText(arrayListLinkedIssues[0], false)
            }
            autoTextViewLinkedIssue.setOnTouchListener { v, event ->
                autoTextViewLinkedIssue.showDropDown()
                false
            }
            autoTextViewLinkedIssue.setOnItemClickListener { parent, view, position, id ->
                jiraAuthentication.setLinkedIssueTypePosition(linkedIssueTypePosition = position)
            }
//        spinnerLinkedIssueAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListLinkedIssues)
//        spinnerLinkedIssueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerLinkedIssue.adapter = spinnerLinkedIssueAdapter

            autoTextViewIssueAdapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListIssues)
            autoTextViewIssue.setAdapter(autoTextViewIssueAdapter)
//            if (arrayListIssues.isNotEmpty()) {
//                autoTextViewIssue.setText(arrayListIssues[0], false)
//            }
            autoTextViewIssue.setOnTouchListener { v, event ->
                autoTextViewIssue.showDropDown()
                false
            }

//        spinnerIssueAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListIssues)
//        spinnerIssueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerIssue.adapter = spinnerIssueAdapter

            autoTextViewAssigneeAdapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListAssignee)
            autoTextViewAssignee.setAdapter(autoTextViewAssigneeAdapter)
//            if (arrayListAssignee.isNotEmpty()) {
//                autoTextViewAssignee.setText(arrayListAssignee[0], false)
//            }
            autoTextViewAssignee.setOnTouchListener { v, event ->
                autoTextViewAssignee.showDropDown()
                false
            }
            autoTextViewAssignee.setOnItemClickListener { parent, view, position, id ->
                jiraAuthentication.setAssigneePosition(assigneePosition = position)
            }

//        spinnerAssigneeAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListAssignee)
//        spinnerAssigneeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerAssignee.adapter = spinnerAssigneeAdapter

            autoTextViewPriorityAdapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListPriority)
            autoTextViewPriority.setAdapter(autoTextViewPriorityAdapter)
            if (arrayListPriority.isNotEmpty()) {
                autoTextViewPriority.setText(arrayListPriority[0], false)
            }
            autoTextViewPriority.setOnTouchListener { v, event ->
                autoTextViewPriority.showDropDown()
                false
            }
            autoTextViewPriority.setOnItemClickListener { parent, view, position, id ->
                jiraAuthentication.setPriorityPosition(priorityPosition = position)
            }
//        spinnerPriorityAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListPriority)
//        spinnerPriorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerPriority.adapter = spinnerPriorityAdapter

            autoTextViewComponentAdapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListComponent)
            autoTextViewComponent.setAdapter(autoTextViewComponentAdapter)
//            if (arrayListComponent.isNotEmpty()) {
//                autoTextViewComponent.setText(arrayListComponent[0], false)
//            }
            autoTextViewComponent.setOnTouchListener { v, event ->
                autoTextViewComponent.showDropDown()
                false
            }
            autoTextViewComponent.setOnItemClickListener { parent, view, position, id ->
                jiraAuthentication.setComponentPosition(componentPosition = position)
            }
//        spinnerComponentAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListComponent)
//        spinnerComponentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerComponent.adapter = spinnerComponentAdapter

            autoTextViewFixVersionsAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListFixVersions
            )
            autoTextViewFixVersions.setAdapter(autoTextViewFixVersionsAdapter)
//            if (arrayListFixVersions.isNotEmpty()) {
//                autoTextViewFixVersions.setText(arrayListFixVersions[0], false)
//            }
            autoTextViewFixVersions.setOnTouchListener { v, event ->
                autoTextViewFixVersions.showDropDown()
                false
            }
            autoTextViewFixVersions.setOnItemClickListener { parent, view, position, id ->
                jiraAuthentication.setFixVersionsPosition(fixVersionsPosition = position)
            }
//        spinnerFixVersionsAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListFixVersions)
//        spinnerFixVersionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerFixVersions.adapter = spinnerFixVersionsAdapter


            autoTextViewLabelAdapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListLabel)
            autoTextViewLabel.setAdapter(autoTextViewLabelAdapter)
//            if (arrayListLabel.isNotEmpty()) {
//                autoTextViewLabel.setText(arrayListLabel[0], false)
//            }
            autoTextViewLabel.setOnTouchListener { v, event ->
                autoTextViewLabel.showDropDown()
                false
            }
//        spinnerLabelAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListLabel)
//        spinnerLabelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerLabel.adapter = spinnerLabelAdapter

            autoTextViewEpicLinkAdapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListEpicLink)
            autoTextViewEpicLink.setAdapter(autoTextViewEpicLinkAdapter)
//            if (arrayListEpicLink.isNotEmpty()) {
//                autoTextViewEpicLink.setText(arrayListEpicLink[0], false)
//            }
            autoTextViewEpicLink.setOnTouchListener { v, event ->
                autoTextViewEpicLink.showDropDown()
                false
            }
//        spinnerEpicLinkAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListEpicLink)
//        spinnerEpicLinkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerEpicLink.adapter = spinnerEpicLinkAdapter

            if(hashMapBoardList[arrayListProjectKeys[projectPosition]] == "scrum"){
                cardViewSprint.visibility = View.VISIBLE
                autoTextViewSprintAdapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListSprint)
                autoTextViewSprint.setAdapter(autoTextViewSprintAdapter)
//            if (arrayListSprint.isNotEmpty()) {
//                autoTextViewSprint.setText(arrayListSprint[0], false)
//            }
                autoTextViewSprint.setOnTouchListener { v, event ->
                    autoTextViewSprint.showDropDown()
                    false
                }
                autoTextViewSprint.setOnItemClickListener { parent, view, position, id ->
                    jiraAuthentication.setSprintPosition(sprintPosition = position)
                }
            }else{
                cardViewSprint.visibility = View.GONE
            }


//        spinnerSprintAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListSprint)
//        spinnerSprintAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerSprint.adapter = spinnerSprintAdapter
            progressBarJiraLayout.visibility = View.GONE
            progressBarJira.visibility = View.GONE
//            detachProgressBar()
        } catch (e: Exception) {
            progressBarJiraLayout.visibility = View.GONE
            progressBarJira.visibility = View.GONE
//            detachProgressBar()
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
    }


//    private fun addJiraProjectNames(): ArrayList<String> {
//        arrayListJiraProject.add("project_a")
//        arrayListJiraProject.add("project_b")
//        return arrayListJiraProject
//    }
//
//    private fun addJiraIssueTypes(): ArrayList<String> {
//        arrayListJiraIssueType.add("issue_type_a")
//        arrayListJiraIssueType.add("issue_type_b")
//        return arrayListJiraIssueType
//    }
//
//    private fun addJiraReporterNames(): ArrayList<String> {
//        arrayListJiraReporter.add("reporter_a")
//        arrayListJiraReporter.add("reporter_b")
//        return arrayListJiraReporter
//    }
//
//    private fun addJiraLinkedIssues(): ArrayList<String> {
//        arrayListJiraLinkedIssue.add("linked_issues_a")
//        arrayListJiraLinkedIssue.add("linked_issues_b")
//        return arrayListJiraLinkedIssue
//    }
//
//    private fun addJiraAssignee(): ArrayList<String> {
//        arrayListJiraAssignee.add("assignee_a")
//        arrayListJiraAssignee.add("assignee_b")
//        return arrayListJiraAssignee
//    }
//
//    private fun addJiraPriority(): ArrayList<String> {
//        arrayListJiraPriority.add("priority_a")
//        arrayListJiraPriority.add("priority_b")
//        return arrayListJiraPriority
//    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeSlackLayout(filePathMedia: File) {
        try {
            if (windowManagerSlack != null && this::viewSlack.isInitialized) {
                (windowManagerSlack as WindowManager).removeViewImmediate(viewSlack)
                arrayListSlackFileName.clear()
            }
            viewSlack = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_slack_popup, (this.rootView as ViewGroup), false)

            if (Settings.canDrawOverlays(activity)) {
                windowManagerParamsSlack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                    )
                } else {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        PixelFormat.TRANSLUCENT
                    )
                }

                windowManagerSlack = activity.getSystemService(Context.WINDOW_SERVICE)!!

                if (windowManagerSlack != null) {
                    (windowManagerSlack as WindowManager).addView(
                        viewSlack,
                        windowManagerParamsSlack
                    )

                    if (Build.VERSION.SDK_INT >= 23) {
                        activity.window.navigationBarColor =
                            resources.getColor(R.color.black, theme)
                        activity.window.statusBarColor = resources.getColor(R.color.black, theme)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            activity.window.navigationBarColor = resources.getColor(R.color.black)
                            activity.window.statusBarColor = resources.getColor(R.color.black)
                        }
                    }

                    spinnerChannels = viewSlack.findViewById(R.id.spinner_slack_channel)
                    spinnerUsers = viewSlack.findViewById(R.id.spinner_slack_user)
                    recyclerViewSlackAttachment =
                        viewSlack.findViewById(R.id.recycler_view_slack_attachment)
                    editTextMessage = viewSlack.findViewById(R.id.editText_slack_message)
                    buttonSlackCancel = viewSlack.findViewById(R.id.button_slack_cancel)
                    buttonSlackCreate = viewSlack.findViewById(R.id.button_slack_create)
//                    toolbarJira = viewJira.findViewById(R.id.textView_jira_title)
//                    layoutJira = viewJira.findViewById(R.id.layout_jira)
                    progressBarSlack = viewSlack.findViewById(R.id.slack_progressbar)
                    progressBarSlackLayout =
                        viewSlack.findViewById(R.id.slack_progressbar_background)

                    slackAuthentication.callSlack(
                        context = context,
                        activity = activity,
                        filePathMedia = filePathMedia,
                        slackTask = "get"
                    )
                    initializeSlackRecyclerView(filePathMedia = filePathMedia)
                    buttonClicksSlack(filePathMedia)
                    progressBarSlackLayout.visibility = View.VISIBLE
                    progressBarSlack.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
    }

    private fun removeSlackLayout() {
        if (windowManagerSlack != null && this::viewSlack.isInitialized) {
            (windowManagerSlack as WindowManager).removeViewImmediate(viewSlack)
            windowManagerSlack = null
            arrayListSlackFileName.clear()
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun buttonClicksSlack(filePathMedia: File) {
        buttonSlackCreate.setSafeOnClickListener {
            slackAuthentication.gatherJiraSpinnerDetails(
                spinnerChannel = spinnerChannels,
                spinnerUser = spinnerUsers
            )
            slackAuthentication.gatherSlackEditTextDetails(editTextMessage = editTextMessage)
            slackAuthentication.gatherJiraRecyclerViewDetails(arrayListRecyclerViewItems = arrayListSlackFileName)
            if (slackAuthentication.checkMessageEmpty(activity = activity, context = context)) {
                progressBarSlack.visibility = View.VISIBLE
                progressBarSlackLayout.visibility = View.VISIBLE
                slackAuthentication.callSlack(
                    activity = activity,
                    context = context,
                    filePathMedia = filePathMedia,
                    slackTask = "create"
                )
            }
        }
        buttonSlackCancel.setSafeOnClickListener {
            removeSlackLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }


    private fun initializeSlackRecyclerView(filePathMedia: File) {
        recyclerViewSlackAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        slackAdapter = RecyclerViewSlackAdapter(
            addSlackFileNames(filePathMedia = filePathMedia),
            context = context,
            activity = activity,
            rootView = rootView
        )
        recyclerViewSlackAttachment.adapter = slackAdapter
    }

    private fun addSlackFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        arrayListSlackFileName.add(RecyclerViewModel(file = filePathMedia))
        arrayListSlackFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        return arrayListSlackFileName
    }

    internal fun initializeSlackSpinner(
        arrayListChannels: ArrayList<String>,
        arrayListUsers: ArrayList<String>
    ) {
        spinnerChannelsAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListChannels)
        spinnerChannelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChannels.adapter = spinnerChannelsAdapter

        spinnerUsersAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListUsers)
        spinnerUsersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUsers.adapter = spinnerUsersAdapter

        progressBarSlack.visibility = View.GONE
        progressBarSlackLayout.visibility = View.GONE
    }
    private fun hideKeyboard(activity: Activity){
        val inputMethodManager = (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        inputMethodManager.toggleSoftInput(0,0)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            activity.runOnUiThread {
                textView_counter_video.performClick()
            }
        }
    }
}