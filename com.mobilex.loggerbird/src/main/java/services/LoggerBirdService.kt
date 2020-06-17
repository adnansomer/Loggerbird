package services

import adapter.*
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
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
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import models.RecyclerViewModelTo
import observers.LogActivityLifeCycleObserver
import org.aviran.cookiebar2.CookieBar
import paint.PaintActivity
import paint.PaintView
import utils.*
import utils.EmailUtil
import utils.LinkedBlockingQueueUtil
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

internal class LoggerBirdService : Service(), LoggerBirdShakeDetector.Listener {
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
    private var windowManagerJiraDatePicker: Any? = null
    private var windowManagerUnhandledDuplication: Any? = null
    private var windowManagerEmail: Any? = null
    private var windowManagerFutureTask: Any? = null
    private var windowManagerFutureDate: Any? = null
    private var windowManagerFutureTime: Any? = null
    private var windowManagerGitlab: Any? = null
    private lateinit var windowManagerParams: WindowManager.LayoutParams
    private lateinit var windowManagerParamsFeedback: WindowManager.LayoutParams
    private lateinit var windowManagerParamsProgressBar: WindowManager.LayoutParams
    private lateinit var windowManagerParamsJira: WindowManager.LayoutParams
    private lateinit var windowManagerParamsJiraAuth: WindowManager.LayoutParams
    private lateinit var windowManagerParamsSlack: WindowManager.LayoutParams
    private lateinit var windowManagerParamsJiraDatePicker: WindowManager.LayoutParams
    private lateinit var windowManagerParamsUnhandledDuplication: WindowManager.LayoutParams
    private lateinit var windowManagerParamsEmail: WindowManager.LayoutParams
    private lateinit var windowManagerParamsFutureTask: WindowManager.LayoutParams
    private lateinit var windowManagerParamsFutureDate: WindowManager.LayoutParams
    private lateinit var windowManagerParamsFutureTime: WindowManager.LayoutParams
    private lateinit var windowManagerParamsGitlab: WindowManager.LayoutParams
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
    private lateinit var viewUnhandledDuplication: View
    private lateinit var viewEmail: View
    private lateinit var viewFutureTask: View
    private lateinit var viewFutureDate: View
    private lateinit var viewFutureTime: View
    private lateinit var viewGitlab: View
    private lateinit var wrapper: FrameLayout
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
    private val defaultToast: DefaultToast = DefaultToast()
    private val arrayListUnhandledExceptionMessage: ArrayList<String> = ArrayList()

    //Jira:
    internal val jiraAuthentication = JiraAuthentication()
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
    private lateinit var autoTextViewEpicName: AutoCompleteTextView
    private lateinit var buttonJiraCreate: Button
    internal lateinit var buttonJiraCancel: Button
    internal fun controlButtonJiraCancel(): Boolean {
        if (this::buttonJiraCancel.isInitialized) {
            return true
        }
        return false
    }

    //  private lateinit var buttonJiraAuthCancel: Button
    //  private lateinit var buttonJiraAuthNext: Button
    private lateinit var layoutJira: FrameLayout
    private lateinit var toolbarJira: Toolbar
    private lateinit var toolbarSlack: Toolbar
    private lateinit var progressBarJira: ProgressBar
    private lateinit var progressBarJiraLayout: FrameLayout
    private lateinit var cardViewSprint: CardView
    private lateinit var cardViewStartDate: CardView
    private lateinit var cardViewEpicName: CardView
    private lateinit var imageViewStartDate: ImageView
    //    private lateinit var textViewRemoveDate: TextView
    private lateinit var imageButtonRemoveDate: ImageButton
    private lateinit var calendarViewStartDate: CalendarView
    private lateinit var calendarViewJiraView: View
    private lateinit var calendarViewJiraLayout: FrameLayout
    private var calendarViewJiraDate: Long? = null
    private lateinit var buttonCalendarViewJiraCancel: Button
    private lateinit var buttonCalendarViewJiraOk: Button
    private lateinit var scrollViewJira: ScrollView
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
    private lateinit var autoTextViewEpicNameAdapter: ArrayAdapter<String>
    private var projectPosition: Int = 0
    private var controlProjectPosition: Boolean = false


    //Feedback:
    private lateinit var floating_action_button_feedback: Button
    private lateinit var floating_action_button_feed_close: Button
    private lateinit var editText_feedback: EditText
    private lateinit var toolbarFeedback: Toolbar
    private lateinit var progressBarFeedback: ProgressBar
    private lateinit var progressBarFeedbackLayout: FrameLayout

    //Slack:
    private val slackAuthentication = SlackAuthentication()
    private lateinit var buttonSlackCreate: Button
    internal lateinit var buttonSlackCancel: Button
    private lateinit var buttonSlackCreateUser: Button
    internal lateinit var buttonSlackCancelUser: Button
    private lateinit var spinnerChannels: Spinner
    private lateinit var spinnerUsers: Spinner
    private lateinit var editTextMessage: EditText
    private lateinit var editTextMessageUser: EditText
    private lateinit var spinnerChannelsAdapter: ArrayAdapter<String>
    private lateinit var spinnerUsersAdapter: ArrayAdapter<String>
    private lateinit var slackAdapter: RecyclerViewSlackAdapter
    private lateinit var recyclerViewSlackAttachment: RecyclerView
    private lateinit var recyclerViewSlackAttachmentUser: RecyclerView
    private val arrayListSlackFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var progressBarSlack: ProgressBar
    private lateinit var progressBarSlackLayout: FrameLayout
    private lateinit var slackChannelLayout: ScrollView
    private lateinit var slackUserLayout: ScrollView
    private lateinit var slackBottomNavigationView: BottomNavigationView

    //Email:
    private lateinit var buttonEmailCreate: Button
    private lateinit var buttonEmailCancel: Button
    private lateinit var imageViewEmailAdd: ImageView
    private lateinit var cardViewToList: CardView
    private lateinit var editTextTo: EditText
    private lateinit var editTextContent: EditText
    private lateinit var editTextSubject: EditText
    private lateinit var toolbarEmail: Toolbar
    private lateinit var recyclerViewEmailAttachment: RecyclerView
    private lateinit var recyclerViewEmailToList: RecyclerView
    private lateinit var emailAdapter: RecyclerViewEmailAdapter
    private lateinit var emailToListAdapter: RecyclerViewEmaiToListAdapter
    private val arrayListEmailFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private val arraylistEmailToUsername: ArrayList<RecyclerViewModelTo> = ArrayList()

    //Future-Task:
    private lateinit var checkBoxFutureTask: CheckBox
    private lateinit var imageViewFutureCalendar: ImageView
    private lateinit var imageButtonFutureTaskRemoveDate: ImageButton
    private lateinit var imageViewFutureTime: ImageView
    private lateinit var imageButtonFutureTaskRemoveTime: ImageButton
    private lateinit var buttonFutureTaskProceed: Button
    private lateinit var buttonFutureTaskCancel: Button
    private val calendarFuture = Calendar.getInstance()

    //Future-Task-Date:
    private var futureStartDate: Long? = null
    private lateinit var frameLayoutDate: FrameLayout
    private lateinit var calendarViewFutureTask: CalendarView
    private lateinit var buttonFutureTaskDateCreate: Button
    private lateinit var buttonFutureTaskDateCancel: Button
    //Future-Task-Time:
    private var futureStartTime: Long? = null
    private lateinit var frameLayoutTime: FrameLayout
    private lateinit var timePickerFutureTask: TimePicker
    private lateinit var buttonFutureTaskTimeCreate: Button
    private lateinit var buttonFutureTaskTimeCancel: Button
    private lateinit var emailTo: String
    private lateinit var emailMessage: String
    private lateinit var emailFile: File
    private lateinit var emailSubject: String
    private lateinit var emailArrayListFilePath: ArrayList<File>

    //Gitlab:
    private val gitlabAuthentication = GitlabAuthentication()
    private lateinit var editTextGitlabTitle: EditText
    private lateinit var editTextGitlabDescription: EditText
    private lateinit var editTextGitlabMilestone: EditText
    private lateinit var editTextGitlabAssignee: EditText
    private lateinit var buttonGitlabCreate: Button
    private lateinit var buttonGitlabCancel: Button
    private lateinit var toolbarGitlab: Toolbar
    private lateinit var recyclerViewGitlabAttachment: RecyclerView
    private lateinit var gitlabAdapter:RecyclerViewGitlabAdapter
    private val arrayListGitlabFileName: ArrayList<RecyclerViewModel> = ArrayList()


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
        private lateinit var textView_share_gitlab: TextView
        private lateinit var textView_discard: TextView
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
        private var runnableListEmail: ArrayList<Runnable> = ArrayList()
        private var workQueueLinked: LinkedBlockingQueueUtil = LinkedBlockingQueueUtil()
        private var workQueueLinkedEmail: LinkedBlockingQueueUtil = LinkedBlockingQueueUtil()
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
        private lateinit var workingAnimation: Animation
        internal val arrayListFile: ArrayList<File> = ArrayList()
        internal var controlFutureTask: Boolean = false
        internal lateinit var recyclerViewSlackAttachment: RecyclerView
        internal lateinit var recyclerViewSlackAttachmentUser: RecyclerView
        internal lateinit var recyclerViewSlackNoAttachment: TextView
        internal lateinit var recyclerViewSlackUserNoAttachment: TextView


        internal fun callEnqueue() {
            workQueueLinked.controlRunnable = false
            if (runnableList.size > 0) {
                runnableList.removeAt(0)
                if (runnableList.size > 0) {
                    workQueueLinked.put(runnableList[0])
                }
            }
        }

        internal fun callEnqueueEmail() {
            workQueueLinkedEmail.controlRunnable = false
            if (runnableListEmail.size > 0) {
                runnableListEmail.removeAt(0)
                if (runnableListEmail.size > 0) {
                    workQueueLinkedEmail.put(runnableListEmail[0])
                } else {
                    loggerBirdService.detachProgressBar()
                    loggerBirdService.removeEmailLayout()
                    loggerBirdService.defaultToast.attachToast(
                        activity = loggerBirdService.returnActivity(),
                        toastMessage = loggerBirdService.context.resources.getString(R.string.email_send_success)
                    )
                }
            } else {
                loggerBirdService.detachProgressBar()
                loggerBirdService.removeEmailLayout()
                loggerBirdService.defaultToast.attachToast(
                    activity = loggerBirdService.returnActivity(),
                    toastMessage = loggerBirdService.context.resources.getString(R.string.email_send_success)
                )
            }
        }

        internal fun resetEnqueueMail() {
            runnableListEmail.clear()
            workQueueLinkedEmail.controlRunnable = false
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

        internal fun controlRevealShareLayout(): Boolean {
            if (this::reveal_linear_layout_share.isInitialized) {
                return true
            }
            return false
        }

        internal fun controlWorkingAnimation(): Boolean {
            if (this::workingAnimation.isInitialized) {
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
            if (!controlFutureTask) {
                arrayListFile.forEach {
                    if (it.exists()) {
                        it.delete()
                    }
                }
            }
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

        try {
            if (windowManager != null && this::view.isInitialized) {
                if (reveal_linear_layout_share.visibility == View.VISIBLE) {
                    controlMedialFile()
                }
                (windowManager as WindowManager).removeViewImmediate(view)

                CookieBar.build(activity)
                    .setCustomView(R.layout.loggerbird_close_popup)
                    .setCustomViewInitializer {
                        val textViewFeedBack =
                            it.findViewById<TextView>(R.id.textView_feed_back_pop_up)
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
                val rootView: ViewGroup =
                    activity.window.decorView.findViewById(android.R.id.content)
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
                    textView_counter_video = view.findViewById(R.id.fragment_textView_counter_video)
                    textView_counter_audio = view.findViewById(R.id.fragment_textView_counter_audio)
                    textView_video_size = view.findViewById(R.id.fragment_textView_size_video)
                    textView_audio_size = view.findViewById(R.id.fragment_textView_size_audio)
                    checkBoxFutureTask = view.findViewById(R.id.checkBox_future_task)
                    textView_share_gitlab = view.findViewById(R.id.textView_share_gitlab)

                    floating_action_button.imageTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.white))
                    floating_action_button.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.black))

                    floating_action_button.imageTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.white))
                    floating_action_button.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.black))

                    if (audioRecording || videoRecording || screenshotDrawing) {
                        workingAnimation =
                            AnimationUtils.loadAnimation(context, R.anim.pulse_in_out)
                        if (controlWorkingAnimation()) {
                            floating_action_button.startAnimation(workingAnimation)
                            floating_action_button.backgroundTintList =
                                ColorStateList.valueOf(resources.getColor(R.color.mediaRecordColor))
                            floating_action_button.imageTintList =
                                ColorStateList.valueOf(resources.getColor(R.color.white))
                            if (audioRecording) {
                                floating_action_button.setImageResource(R.drawable.ic_mic_black_24dp)
                            }
                            if (videoRecording) {
                                floating_action_button.setImageResource(R.drawable.ic_videocam_black_24dp)
                            }
                            if (screenshotDrawing) {
                                floating_action_button.setImageResource(R.drawable.ic_photo_camera_black_24dp)
                            }
                        }
                    }

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
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.floatingActionButtonTag)
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun shareView(filePathMedia: File) {
        floating_action_button.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(R.color.black))
        floating_action_button.imageTintList =
            ColorStateList.valueOf(resources.getColor(R.color.white))
        floating_action_button.clearAnimation()
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
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            textView_send_email.setSafeOnClickListener {
                initializeEmailLayout(filePathMedia = filePathMedia)
            }

            textView_share_jira.setSafeOnClickListener {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }
                    initializeJiraLayout(filePathMedia = filePathMedia)
                }
            }

            textView_share_slack.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }
                    initializeSlackLayout(filePathMedia = filePathMedia)
                }
            }

            textView_share_gitlab.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }
                    initializeGitlabLayout(filePathMedia = filePathMedia)
                }
            }

            textView_discard.setSafeOnClickListener {
                discardMediaFile()
            }

            if (sharedPref.getBoolean("future_task_check", false)) {
                checkBoxFutureTask.isChecked = true
            }
            checkBoxFutureTask.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    with(sharedPref.edit()) {
                        putBoolean("future_task_check", true)
                        commit()
                    }
                } else {
                    stopService(Intent(context, LoggerBirdFutureTaskService::class.java))
                }
            }
            checkBoxFutureTask.setOnClickListener {
                if (checkBoxFutureTask.isChecked) {
                    defaultToast.attachToast(
                        activity = activity,
                        toastMessage = activity.resources.getString(R.string.future_task_enabled)
                    )
                    initializeFutureTaskLayout(filePathMedia = filePathMedia)
                } else {
                    with(sharedPref.edit()) {
                        remove("future_task_time")
                        remove("future_file_path")
                        commit()
                    }
                    defaultToast.attachToast(
                        activity = activity,
                        toastMessage = activity.resources.getString(R.string.future_task_disabled)
                    )
                }
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
                            workingAnimation =
                                AnimationUtils.loadAnimation(context, R.anim.pulse_in_out)
                            floating_action_button.startAnimation(workingAnimation)
                            floating_action_button.backgroundTintList =
                                ColorStateList.valueOf(resources.getColor(R.color.mediaRecordColor))
                            floating_action_button.imageTintList =
                                ColorStateList.valueOf(resources.getColor(R.color.white))
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
                            arrayListFile.add(filePathAudio)
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
                                workingAnimation =
                                    AnimationUtils.loadAnimation(context, R.anim.pulse_in_out)
                                floating_action_button.startAnimation(workingAnimation)
                                floating_action_button.backgroundTintList =
                                    ColorStateList.valueOf(resources.getColor(R.color.mediaRecordColor))
                                floating_action_button.imageTintList =
                                    ColorStateList.valueOf(resources.getColor(R.color.white))
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
                workingAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse_in_out)
                floating_action_button.startAnimation(workingAnimation)
                floating_action_button.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.mediaRecordColor))
                floating_action_button.imageTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.white))
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
                    arrayListFile.add(filePathVideo)
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

    private fun callEmail(filePathMedia: File, to: String) {
        if (LoggerBird.isLogInitAttached()) {
            if (runnableListEmail.isEmpty()) {
                workQueueLinkedEmail.put {
                    createEmailTask(filePathMedia = filePathMedia, to = to)
                }
            }
            runnableListEmail.add(Runnable {
                createEmailTask(filePathMedia = filePathMedia, to = to)
            })
        } else {
            throw LoggerBirdException(Constants.logInitErrorMessage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun hearShake() {
        try {
//            val file:File ?  = null
//            file!!.createNewFile()
            Log.d("shake", "shake fired!!")
            if (Settings.canDrawOverlays(this.activity)) {
                if (checkUnhandledFilePath()) {
                    gatherUnhandledExceptionDetails()
                } else {
                    if (!controlFileAction) {
                        initializeFloatingActionButton(activity = this.activity)
                    } else {
                        Toast.makeText(
                            context,
                            R.string.files_action_limit,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
                            .setCustomViewInitializer(CookieBar.CustomViewInitializer {
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
//            val file: File? = null
//            file!!.createNewFile()
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
                    toolbarFeedback = viewFeedback.findViewById(R.id.toolbar_feedback)
                    progressBarFeedback = viewFeedback.findViewById(R.id.feedback_progressbar)
                    progressBarFeedbackLayout = viewFeedback.findViewById(R.id.feedback_progressbar_background)
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
            sendFeedback("adnansomer@gmail.com")
        }
        floating_action_button_feed_close.setSafeOnClickListener {
            removeFeedBackLayout()
        }

    }

    private fun sendFeedback(to: String) {
        if (editText_feedback.text.trim().isNotEmpty()) {
            removeFeedBackLayout()
            coroutineCallFeedback.async {
                EmailUtil.sendFeedbackEmail(
                    context = context,
                    message = editText_feedback.text.toString(),
                    to = to
                )
                withContext(Dispatchers.Main){
                    Toast.makeText(
                        context,
                        R.string.feed_back_email_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                    ?: return
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
                            LoggerBird.callEmailSender(
                                context = context,
                                file = fileName,
                                to = "appcaesars@gmail.com"
                            )
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
                if (controlMedialFile()) {
                    finishShareLayout(message = "media")
                } else {
                    finishShareLayout(message = "media_error")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                finishShareLayout(message = "media_error")
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.discardFileTag)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun sendSingleMediaFile(
        filePathMedia: File,
        to: String,
        message: String? = null,
        subject: String? = null
    ) {
        coroutineCallSendSingleFile.async {
            try {
                if (filePathMedia.exists()) {
                    LoggerBird.callEmailSender(
                        context = context,
                        activity = activity,
                        file = filePathMedia,
                        to = to,
                        message = message,
                        subject = subject
                    )
                } else {
                    finishShareLayout("single_email_error")
                    resetEnqueueMail()
                }
            } catch (e: Exception) {
                finishShareLayout("single_email_error")
                e.printStackTrace()
                resetEnqueueMail()
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
//                    finishErrorFab()
                    removeEmailLayout()
                    detachProgressBar()
                }
                "jira" -> {
                    Toast.makeText(context, R.string.jira_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab()
                }
                "jira_error" -> {
                    removeJiraLayout()
                    Toast.makeText(context, R.string.jira_sent_error, Toast.LENGTH_SHORT).show()
                    if (this::progressBarJiraLayout.isInitialized && this::progressBarJira.isInitialized) {
                        progressBarJiraLayout.visibility = View.GONE
                        progressBarJira.visibility = View.GONE
                    }

                    detachProgressBar()
                }
                "jira_error_time_out" -> {
                    removeJiraLayout()
                    Toast.makeText(context, R.string.jira_sent_error_time_out, Toast.LENGTH_SHORT)
                        .show()
                    if (this::progressBarJiraLayout.isInitialized && this::progressBarJira.isInitialized) {
                        progressBarJiraLayout.visibility = View.GONE
                        progressBarJira.visibility = View.GONE
                    }

                    detachProgressBar()
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
                }

                "slack_error_time_out" -> {
                    removeSlackLayout()
                    Toast.makeText(context, R.string.slack_sent_error_time_out, Toast.LENGTH_SHORT).show()
                    progressBarSlackLayout.visibility = View.GONE
                    progressBarSlack.visibility = View.GONE
                }

            }
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun finishSuccessFab() {
        if (controlRevealShareLayout() && controlFloatingActionButtonView()) {
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
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun finishErrorFab() {
        if (controlRevealShareLayout() && controlFloatingActionButtonView()) {
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
    internal fun attachProgressBar() {
        detachProgressBar()
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
    internal fun detachProgressBar() {
        if (this::progressBarView.isInitialized && windowManagerProgressBar != null) {
            (windowManagerProgressBar as WindowManager).removeViewImmediate(progressBarView)
            windowManagerProgressBar = null
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


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeJiraLayout(filePathMedia: File) {
        try {
            if (windowManagerJira != null && this::viewJira.isInitialized) {
                (windowManagerJira as WindowManager).removeViewImmediate(viewJira)
                arrayListJiraFileName.clear()
            }
            this.rootView = activity.window.decorView.findViewById(android.R.id.content)
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
                    autoTextViewEpicName = viewJira.findViewById(R.id.auto_textView_jira_epic_name)
                    buttonJiraCreate = viewJira.findViewById(R.id.button_jira_create)
                    buttonJiraCancel = viewJira.findViewById(R.id.button_jira_cancel)
                    toolbarJira = viewJira.findViewById(R.id.textView_jira_title)
                    layoutJira = viewJira.findViewById(R.id.layout_jira)
                    progressBarJira = viewJira.findViewById(R.id.jira_progressbar)
                    progressBarJiraLayout = viewJira.findViewById(R.id.jira_progressbar_background)
                    cardViewSprint = viewJira.findViewById(R.id.cardView_sprint)
                    cardViewStartDate = viewJira.findViewById(R.id.cardView_start_date)
                    cardViewEpicName = viewJira.findViewById(R.id.cardView_epic_name)
                    imageViewStartDate = viewJira.findViewById(R.id.imageView_start_date)
//                    textViewRemoveDate = viewJira.findViewById(R.id.textView_jira_remove_date)
                    imageButtonRemoveDate =
                        viewJira.findViewById(R.id.image_button_jira_remove_date)
                    scrollViewJira = viewJira.findViewById(R.id.scrollView_jira)

                    scrollViewJira.setOnTouchListener { v, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            hideKeyboard(activity = activity)
                        }
                        return@setOnTouchListener false
                    }
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
            projectPosition = 0
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
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
                autoTextViewSprint = autoTextViewSprint,
                autoTextViewEpicName = autoTextViewEpicName
//                spinnerSprint = spinnerSprint
            )
            jiraAuthentication.gatherJiraEditTextDetails(
                editTextSummary = editTextSummary,
                editTextDescription = editTextDescription
            )
            jiraAuthentication.gatherJiraRecyclerViewDetails(arrayListRecyclerViewItems = arrayListJiraFileName)
            if (autoTextViewIssueType.editableText.toString() != "Epic") {
                callJiraTask(filePathMedia = filePathMedia)
            } else {
                if (jiraAuthentication.checkEpicName(activity = activity, context = context)) {
                    callJiraTask(filePathMedia = filePathMedia)
                }
            }
        }

        toolbarJira.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.jira_menu_save -> {
                    val sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                    with(sharedPref.edit()) {
                        putString("jira_project", autoTextViewProject.editableText.toString())
                        putInt("jira_project_position", projectPosition)
                        putString(
                            "jira_issue_type",
                            autoTextViewIssueType.editableText.toString()
                        )
                        putString("jira_summary", editTextSummary.text.toString())
                        putString("jira_description", editTextDescription.text.toString())
                        putString(
                            "jira_component",
                            autoTextViewComponent.editableText.toString()
                        )
                        putString("jira_reporter", autoTextViewReporter.editableText.toString())
                        putString(
                            "jira_linked_issue",
                            autoTextViewLinkedIssue.editableText.toString()
                        )
                        putString("jira_issue", autoTextViewIssue.editableText.toString())
                        putString("jira_assignee", autoTextViewAssignee.editableText.toString())
                        putString("jira_priority", autoTextViewPriority.editableText.toString())
                        putString(
                            "jira_fix_versions",
                            autoTextViewFixVersions.editableText.toString()
                        )
                        putString("jira_labels", autoTextViewLabel.editableText.toString())
                        putString(
                            "jira_epic_link",
                            autoTextViewEpicLink.editableText.toString()
                        )
                        putString("jira_sprint", autoTextViewSprint.editableText.toString())
                        putString(
                            "jira_epic_name",
                            autoTextViewEpicName.editableText.toString()
                        )
                        commit()
                    }
                    defaultToast.attachToast(
                        activity = activity,
                        toastMessage = context.resources.getString(R.string.jira_issue_preferences_save)
                    )
                }
                R.id.jira_menu_clear -> {
                    val sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                    val editor: SharedPreferences.Editor = sharedPref.edit()
                    editor.remove("jira_project")
                    editor.remove("jira_project_position")
                    editor.remove("jira_issue_type")
                    editor.remove("jira_summary")
                    editor.remove("jira_description")
                    editor.remove("jira_component")
                    editor.remove("jira_reporter")
                    editor.remove("jira_linked_issue")
                    editor.remove("jira_issue")
                    editor.remove("jira_assignee")
                    editor.remove("jira_priority")
                    editor.remove("jira_fix_versions")
                    editor.remove("jira_labels")
                    editor.remove("jira_epic_link")
                    editor.remove("jira_sprint")
                    editor.apply()
                    projectPosition = 0
                    editTextDescription.text = null
                    editTextSummary.text = null
                    autoTextViewComponent.setText("", false)
                    autoTextViewReporter.setText("", false)
                    autoTextViewIssue.setText("", false)
                    autoTextViewAssignee.setText("", false)
                    autoTextViewFixVersions.setText("", false)
                    autoTextViewLabel.setText("", false)
                    autoTextViewEpicLink.setText("", false)
                    autoTextViewSprint.setText("", false)
                    autoTextViewEpicName.setText("", false)
                    defaultToast.attachToast(
                        activity = activity,
                        toastMessage = context.resources.getString(R.string.jira_issue_preferences_delete)
                    )
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
        imageViewStartDate.setSafeOnClickListener {
            attachJiraDatePicker()
        }

//        textViewRemoveDate.setOnClickListener {
//            jiraAuthentication.setStartDate(startDate = null)
//            textViewRemoveDate.visibility = View.GONE
//        }
        imageButtonRemoveDate.setOnClickListener {
            jiraAuthentication.setStartDate(startDate = null)
            imageButtonRemoveDate.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun callJiraTask(filePathMedia: File) {
        if (jiraAuthentication.checkSummaryEmpty(
                activity = activity,
                context = context
            ) && jiraAuthentication.checkReporterEmpty(
                activity = activity,
                context = context
            ) && jiraAuthentication.checkFixVersionsEmpty(
                activity = activity,
                context = context
            ) && jiraAuthentication.checkEpicLinkEmpty(
                activity = activity,
                context = context
            ) && jiraAuthentication.checkComponentEmpty(
                activity = activity,
                context = context
            )
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarJira.visibility = View.VISIBLE
                progressBarJiraLayout.visibility = View.VISIBLE
//                    attachProgressBar()
            }
//                hideKeyboard(activity = activity)
            jiraAuthentication.callJiraIssue(
                filePathName = filePathMedia,
                context = context,
                activity = activity,
                jiraTask = "create",
                createMethod = "normal"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addJiraFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListJiraFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (!checkUnhandledFilePath() && LoggerBird.filePathSecessionName.exists()) {
            arrayListJiraFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListJiraFileName
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
        arrayListSprint: ArrayList<String>,
        arrayListEpicName: ArrayList<String>
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
        arrayListEpicName.clear()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeJiraSpinner(
        arrayListProjectNames: ArrayList<String>,
        arrayListProjectKeys: ArrayList<String>,
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
        arrayListEpicName: ArrayList<String>,
        hashMapBoardList: HashMap<String, String>
    ) {

        try {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            editTextSummary.setText(sharedPref.getString("jira_summary", null))
            editTextDescription.setText(sharedPref.getString("jira_description", null))
            initializeProjectName(
                arrayListProjectNames = arrayListProjectNames,
                sharedPref = sharedPref
            )
            initializeIssueType(
                arrayListIssueTypes = arrayListIssueTypes,
                arrayListEpicName = arrayListEpicName,
                sharedPref = sharedPref
            )
            initializeReporter(
                arrayListReporterNames = arrayListReporterNames,
                sharedPref = sharedPref
            )
            initializeLinkedIssues(
                arrayListLinkedIssues = arrayListLinkedIssues,
                sharedPref = sharedPref
            )
            initializeIssues(arrayListIssues = arrayListIssues, sharedPref = sharedPref)
            initializeAssignee(arrayListAssignee = arrayListAssignee, sharedPref = sharedPref)
            initializePriority(arrayListPriority = arrayListPriority, sharedPref = sharedPref)
            initializeComponent(arrayListComponent = arrayListComponent, sharedPref = sharedPref)
            initializeFixVersions(
                arrayListFixVersions = arrayListFixVersions,
                sharedPref = sharedPref
            )
            initializeLabels(arrayListLabel = arrayListLabel, sharedPref = sharedPref)
            initializeEpicLink(arrayListEpicLink = arrayListEpicLink, sharedPref = sharedPref)
            initializeSprint(
                arrayListSprint = arrayListSprint,
                hashMapBoardList = hashMapBoardList,
                arrayListProjectKeys = arrayListProjectKeys,
                sharedPref = sharedPref
            )
            initializeEpicName(arrayListEpicName = arrayListEpicName, sharedPref = sharedPref)

            if (checkUnhandledFilePath()) {
                editTextSummary.setText(activity.resources.getString(R.string.jira_summary_unhandled_exception))
                if (checkBoxUnhandledChecked()) {
                    editTextDescription.setText(
                        sharedPref.getString(
                            "unhandled_exception_message",
                            null
                        )
                    )
                    editTextDescription.isFocusable = false
                }
            }

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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeSprint(
        arrayListSprint: ArrayList<String>,
        hashMapBoardList: HashMap<String, String>,
        arrayListProjectKeys: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        if (!controlProjectPosition) {
            projectPosition = sharedPref.getInt("jira_project_position", 0)
        }
        controlProjectPosition = false
        if (hashMapBoardList[arrayListProjectKeys[projectPosition]] == "scrum") {
            cardViewSprint.visibility = View.VISIBLE
            cardViewStartDate.visibility = View.VISIBLE
            autoTextViewSprintAdapter =
                ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    arrayListSprint
                )
            autoTextViewSprint.setAdapter(autoTextViewSprintAdapter)
            if (arrayListSprint.isNotEmpty()) {
                if (sharedPref.getString("jira_sprint", null) != null) {
                    autoTextViewSprint.setText(
                        sharedPref.getString("jira_sprint", null),
                        false
                    )
                }
//                autoTextViewSprint.setText(arrayListSprint[0], false)
            }
            autoTextViewSprint.setOnTouchListener { v, event ->
                autoTextViewSprint.showDropDown()
                false
            }
            autoTextViewSprint.setOnItemClickListener { parent, view, position, id ->
                jiraAuthentication.setSprintPosition(sprintPosition = position)
                hideKeyboard(activity = activity)
            }
        } else {
            cardViewSprint.visibility = View.GONE
            cardViewStartDate.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeEpicLink(
        arrayListEpicLink: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewEpicLinkAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListEpicLink
            )
        autoTextViewEpicLink.setAdapter(autoTextViewEpicLinkAdapter)
        if (arrayListEpicLink.isNotEmpty()) {
            if (sharedPref.getString("jira_epic_link", null) != null) {
                autoTextViewEpicLink.setText(
                    sharedPref.getString("jira_epic_link", null),
                    false
                )
            }
//                autoTextViewEpicLink.setText(arrayListEpicLink[0], false)
        }
        autoTextViewEpicLink.setOnTouchListener { v, event ->
            autoTextViewEpicLink.showDropDown()
            false
        }
        autoTextViewEpicLink.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeLabels(arrayListLabel: ArrayList<String>, sharedPref: SharedPreferences) {
        autoTextViewLabelAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListLabel)
        autoTextViewLabel.setAdapter(autoTextViewLabelAdapter)
        if (arrayListLabel.isNotEmpty()) {
            if (sharedPref.getString("jira_labels", null) != null) {
                autoTextViewLabel.setText(sharedPref.getString("jira_labels", null), false)
            }
//                autoTextViewLabel.setText(arrayListLabel[0], false)
        }
        autoTextViewLabel.setOnTouchListener { v, event ->
            autoTextViewLabel.showDropDown()
            false
        }
        autoTextViewLabel.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeFixVersions(
        arrayListFixVersions: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewFixVersionsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            arrayListFixVersions
        )
        autoTextViewFixVersions.setAdapter(autoTextViewFixVersionsAdapter)
        if (arrayListFixVersions.isNotEmpty()) {
            if (sharedPref.getString("jira_fix_versions", null) != null) {
                autoTextViewFixVersions.setText(
                    sharedPref.getString("jira_fix_versions", null),
                    false
                )
            }
//                autoTextViewFixVersions.setText(arrayListFixVersions[0], false)
        }
        autoTextViewFixVersions.setOnTouchListener { v, event ->
            autoTextViewFixVersions.showDropDown()
            false
        }
        autoTextViewFixVersions.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setFixVersionsPosition(fixVersionsPosition = position)
            hideKeyboard(activity = activity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeComponent(
        arrayListComponent: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewComponentAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListComponent
            )
        autoTextViewComponent.setAdapter(autoTextViewComponentAdapter)
        if (arrayListComponent.isNotEmpty()) {
            if (sharedPref.getString("jira_component", null) != null) {
                autoTextViewComponent.setText(
                    sharedPref.getString("jira_component", null),
                    false
                )
            }
//                autoTextViewComponent.setText(arrayListComponent[0], false)
        }
        autoTextViewComponent.setOnTouchListener { v, event ->
            autoTextViewComponent.showDropDown()
            false
        }
        autoTextViewComponent.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setComponentPosition(componentPosition = position)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializePriority(
        arrayListPriority: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewPriorityAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListPriority
            )
        autoTextViewPriority.setAdapter(autoTextViewPriorityAdapter)
        if (arrayListPriority.isNotEmpty()) {
            if (sharedPref.getString("jira_priority", null) != null) {
                autoTextViewPriority.setText(
                    sharedPref.getString("jira_priority", null),
                    false
                )
            } else {
                autoTextViewPriority.setText(arrayListPriority[0], false)
            }
        }
        autoTextViewPriority.setOnTouchListener { v, event ->
            autoTextViewPriority.showDropDown()
            false
        }
        autoTextViewPriority.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setPriorityPosition(priorityPosition = position)
            hideKeyboard(activity = activity)
        }
        autoTextViewPriority.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListPriority.contains(autoTextViewPriority.editableText.toString())) {
                    if (arrayListPriority.isNotEmpty()) {
                        if (sharedPref.getString("jira_priority", null) != null) {
                            autoTextViewPriority.setText(
                                sharedPref.getString(
                                    "jira_priority",
                                    null
                                ), false
                            )
                        } else {
                            autoTextViewPriority.setText(arrayListPriority[0], false)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeAssignee(
        arrayListAssignee: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewAssigneeAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListAssignee
            )
        autoTextViewAssignee.setAdapter(autoTextViewAssigneeAdapter)
        if (arrayListAssignee.isNotEmpty()) {
            if (sharedPref.getString("jira_assignee", null) != null) {
                autoTextViewAssignee.setText(
                    sharedPref.getString("jira_assignee", null),
                    false
                )
            }
//                autoTextViewAssignee.setText(arrayListAssignee[0], false)
        }
        autoTextViewAssignee.setOnTouchListener { v, event ->
            autoTextViewAssignee.showDropDown()
            false
        }
        autoTextViewAssignee.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setAssigneePosition(assigneePosition = position)
            hideKeyboard(activity = activity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeIssues(
        arrayListIssues: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewIssueAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListIssues)
        autoTextViewIssue.setAdapter(autoTextViewIssueAdapter)
        if (arrayListIssues.isNotEmpty()) {
            if (sharedPref.getString("jira_issue", null) != null) {
                autoTextViewIssue.setText(sharedPref.getString("jira_issue", null), false)
            }
//                autoTextViewIssue.setText(arrayListIssues[0], false)
        }
        autoTextViewIssue.setOnTouchListener { v, event ->
            autoTextViewIssue.showDropDown()
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeLinkedIssues(
        arrayListLinkedIssues: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewLinkedIssueAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            arrayListLinkedIssues
        )
        autoTextViewLinkedIssue.setAdapter(autoTextViewLinkedIssueAdapter)
        if (arrayListLinkedIssues.isNotEmpty()) {
            if (sharedPref.getString("jira_linked_issue", null) != null) {
                autoTextViewLinkedIssue.setText(
                    sharedPref.getString("jira_linked_issue", null),
                    false
                )
            } else {
                autoTextViewLinkedIssue.setText(arrayListLinkedIssues[0], false)
            }
        }
        autoTextViewLinkedIssue.setOnTouchListener { v, event ->
            autoTextViewLinkedIssue.showDropDown()
            false
        }
        autoTextViewLinkedIssue.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setLinkedIssueTypePosition(linkedIssueTypePosition = position)
            hideKeyboard(activity = activity)
        }
        autoTextViewLinkedIssue.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListLinkedIssues.contains(autoTextViewLinkedIssue.editableText.toString())) {
                    if (arrayListLinkedIssues.isNotEmpty()) {
                        if (sharedPref.getString("jira_linked_issue", null) != null) {
                            autoTextViewLinkedIssue.setText(
                                sharedPref.getString(
                                    "jira_linked_issue",
                                    null
                                ), false
                            )
                        } else {
                            autoTextViewLinkedIssue.setText(arrayListLinkedIssues[0], false)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeReporter(
        arrayListReporterNames: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewReporterAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            arrayListReporterNames
        )
        autoTextViewReporter.setAdapter(autoTextViewReporterAdapter)
        if (arrayListReporterNames.isNotEmpty()) {
            if (sharedPref.getString("jira_reporter", null) != null) {
                autoTextViewReporter.setText(
                    sharedPref.getString("jira_reporter", null),
                    false
                )
            }
//                autoTextViewReporter.setText(arrayListReporterNames[0], false)
        }
        autoTextViewReporter.setOnTouchListener { v, event ->
            autoTextViewReporter.showDropDown()
            false
        }
        autoTextViewReporter.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setReporterPosition(reporterPosition = position)
            hideKeyboard(activity = activity)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeIssueType(
        arrayListIssueTypes: ArrayList<String>,
        arrayListEpicName: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewIssueTypeAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListIssueTypes
            )
        autoTextViewIssueType.setAdapter(autoTextViewIssueTypeAdapter)
        if (checkUnhandledFilePath()) {
            autoTextViewIssueType.setText(arrayListIssueTypes[2], false)
            jiraAuthentication.setIssueTypePosition(issueTypePosition = 2)
        } else {
            if (arrayListIssueTypes.isNotEmpty()) {
                if (sharedPref.getString("jira_issue_type", null) != null) {
                    autoTextViewIssueType.setText(
                        sharedPref.getString("jira_issue_type", null),
                        false
                    )
                } else {
                    autoTextViewIssueType.setText(arrayListIssueTypes[0], false)
                }
            }
        }
        autoTextViewIssueType.setOnTouchListener { v, event ->
            autoTextViewIssueType.showDropDown()
            false
        }
        autoTextViewIssueType.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setIssueTypePosition(issueTypePosition = position)
            hideKeyboard(activity = activity)
            initializeEpicName(arrayListEpicName = arrayListEpicName, sharedPref = sharedPref)
        }
        autoTextViewIssueType.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListIssueTypes.contains(autoTextViewIssueType.editableText.toString())) {
                    if (arrayListIssueTypes.isNotEmpty()) {
                        if (sharedPref.getString("jira_issue_type", null) != null) {
                            autoTextViewIssueType.setText(
                                sharedPref.getString(
                                    "jira_issue_type",
                                    null
                                ), false
                            )
                        } else {
                            autoTextViewIssueType.setText(arrayListIssueTypes[0], false)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeProjectName(
        arrayListProjectNames: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewProjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            arrayListProjectNames
        )
        autoTextViewProject.setAdapter(autoTextViewProjectAdapter)
        if (arrayListProjectNames.isNotEmpty() && autoTextViewProject.text.isEmpty()) {
            if (sharedPref.getString("jira_project", null) != null) {
                autoTextViewProject.setText(
                    sharedPref.getString("jira_project", null),
                    false
                )
            } else {
                autoTextViewProject.setText(arrayListProjectNames[0], false)
            }
        }
        autoTextViewProject.setOnTouchListener { v, event ->
            autoTextViewProject.showDropDown()
            false
        }
        autoTextViewProject.setOnItemClickListener { parent, view, position, id ->
            projectPosition = position
            controlProjectPosition = true
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
        autoTextViewProject.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListProjectNames.contains(autoTextViewProject.editableText.toString())) {
                    if (arrayListProjectNames.isNotEmpty()) {
                        if (sharedPref.getString("jira_project", null) != null) {
                            autoTextViewProject.setText(
                                sharedPref.getString("jira_project", null),
                                false
                            )
                        } else {
                            autoTextViewProject.setText(arrayListProjectNames[0], false)
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeEpicName(
        arrayListEpicName: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        if (autoTextViewIssueType.editableText.toString() == "Epic") {
            cardViewEpicName.visibility = View.VISIBLE
            autoTextViewEpicNameAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                arrayListEpicName
            )
            autoTextViewEpicName.setAdapter(autoTextViewEpicNameAdapter)
            if (arrayListEpicName.isNotEmpty()) {
                if (sharedPref.getString("jira_epic_name", null) != null) {
                    autoTextViewEpicName.setText(
                        sharedPref.getString("jira_epic_name", null),
                        false
                    )
                } else {
                    autoTextViewEpicName.setText(arrayListEpicName[0], false)
                }
            }
            autoTextViewEpicName.setOnTouchListener { v, event ->
                autoTextViewEpicName.showDropDown()
                false
            }
            autoTextViewEpicName.setOnItemClickListener { parentEpic, viewEpic, positionEpic, idEpic ->
                jiraAuthentication.setEpicNamePosition(epicNamePosition = positionEpic)
                hideKeyboard(activity = activity)
            }
            autoTextViewEpicName.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus && autoTextViewEpicName.text.toString().isEmpty()) {
                    if (!arrayListEpicName.contains(autoTextViewEpicName.editableText.toString())) {
                        if (arrayListEpicName.isNotEmpty()) {
                            if (sharedPref.getString("jira_epic_name", null) != null) {
                                autoTextViewEpicName.setText(
                                    sharedPref.getString(
                                        "jira_epic_name",
                                        null
                                    ), false
                                )
                            } else {
                                autoTextViewEpicName.setText(arrayListEpicName[0], false)
                            }
                        }
                    }
                }
            }
        } else {
            cardViewEpicName.visibility = View.GONE
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGitlabLayout(filePathMedia: File){
        try {
            removeGitlabLayout()
            viewGitlab = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_gitlab_popup, (this.rootView as ViewGroup), false)

            if (Settings.canDrawOverlays(activity)) {
                windowManagerParamsGitlab = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

                windowManagerGitlab = activity.getSystemService(Context.WINDOW_SERVICE)!!
                if (windowManagerGitlab != null) {
                    (windowManagerGitlab as WindowManager).addView(
                        viewGitlab,
                        windowManagerParamsGitlab
                    )
                }

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

                toolbarGitlab = viewGitlab.findViewById(R.id.toolbar_gitlab)
                editTextGitlabTitle = viewGitlab.findViewById(R.id.editText_gitlab_title)
                editTextGitlabDescription = viewGitlab.findViewById(R.id.editText_gitlab_description)
               // editTextGitlabMilestone = viewGitlab.findViewById(R.id.editText_gitlab_milestone)
               // editTextGitlabAssignee = viewGitlab.findViewById(R.id.editText_gitlab_assignee)
                buttonGitlabCreate = viewGitlab.findViewById(R.id.button_gitlab_create)
                buttonGitlabCancel = viewGitlab.findViewById(R.id.button_gitlab_cancel)
                recyclerViewGitlabAttachment = viewGitlab.findViewById(R.id.recycler_view_gitlab_attachment)
                initializeGitlabRecyclerView(filePathMedia = filePathMedia)
                buttonClicksGitlab(filePathMedia = filePathMedia)
            }
        }catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
    }

    private fun removeGitlabLayout(){
        if (windowManagerGitlab != null && this::viewGitlab.isInitialized) {
            (windowManagerGitlab as WindowManager).removeViewImmediate(viewGitlab)
            windowManagerGitlab = null
            arrayListGitlabFileName.clear()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun buttonClicksGitlab(filePathMedia: File){

        buttonGitlabCreate.setSafeOnClickListener {
            if(checkGitlabTitleEmpty()){
                attachProgressBar()
//                gitlabAuthentication.gatherAutoTextDetails(
//                    autoTextViewAssignee = autoTextViewGithubAssignee,
//                    autoTextViewProject = autoTextViewGithubProject,
//                    autoTextViewLabels = autoTextViewGithubLabels,
//                    autoTextViewLinkedRequests = autoTextViewGithubLinkedRequests,
//                    autoTextViewMileStone = autoTextViewGithubMileStone
//                )

                gitlabAuthentication.callGitlab(
                    activity = activity,
                    context = context,
                    task = "create",
                    filePathMedia = filePathMedia
                )
            }
        }

        toolbarGitlab.setNavigationOnClickListener {
            removeGitlabLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }

        buttonGitlabCancel.setSafeOnClickListener {
            removeGitlabLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGitlabRecyclerView(filePathMedia: File) {
        arrayListGitlabFileName.clear()
        recyclerViewGitlabAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        gitlabAdapter = RecyclerViewGitlabAdapter(
            addGitlabFileNames(filePathMedia = filePathMedia),
            context = context,
            activity = activity,
            rootView = rootView
        )
        recyclerViewGitlabAttachment.adapter = gitlabAdapter
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addGitlabFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListGitlabFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (!checkUnhandledFilePath() && LoggerBird.filePathSecessionName.exists()) {
            arrayListGitlabFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListGitlabFileName
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkGitlabTitleEmpty(): Boolean {
        if (editTextGitlabTitle.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.editText_gitlab_title_empty)
            )
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeSlackLayout(filePathMedia: File) {
        try {
            removeSlackLayout()
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
                        activity.window.statusBarColor =
                            resources.getColor(R.color.black, theme)
                    } else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            activity.window.navigationBarColor =
                                resources.getColor(R.color.black)
                            activity.window.statusBarColor = resources.getColor(R.color.black)
                        }
                    }

                    spinnerChannels = viewSlack.findViewById(R.id.spinner_slack_channel)
                    spinnerUsers = viewSlack.findViewById(R.id.spinner_slack_user)
                    slackChannelLayout = viewSlack.findViewById(R.id.slack_send_channel_layout)
                    slackUserLayout = viewSlack.findViewById(R.id.slack_send_user_layout)
                    recyclerViewSlackAttachment =
                        viewSlack.findViewById(R.id.recycler_view_slack_attachment)
                    recyclerViewSlackAttachmentUser =
                        viewSlack.findViewById(R.id.recycler_view_slack_attachment_user)
                    editTextMessage = viewSlack.findViewById(R.id.editText_slack_message)
                    editTextMessageUser =
                        viewSlack.findViewById(R.id.editText_slack_message_user)
                    buttonSlackCancel = viewSlack.findViewById(R.id.button_slack_cancel)
                    buttonSlackCreate = viewSlack.findViewById(R.id.button_slack_create)
                    buttonSlackCancelUser =
                        viewSlack.findViewById(R.id.button_slack_cancel_user)
                    buttonSlackCreateUser =
                        viewSlack.findViewById(R.id.button_slack_create_user)
                    progressBarSlack = viewSlack.findViewById(R.id.slack_progressbar)
                    toolbarSlack = viewSlack.findViewById(R.id.toolbar_slack)
                    slackBottomNavigationView =
                        viewSlack.findViewById(R.id.slack_bottom_nav_view)
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
            slackAuthentication.gatherSlackChannelSpinnerDetails(
                spinnerChannel = spinnerChannels
            )
            slackAuthentication.gatherSlackEditTextDetails(editTextMessage = editTextMessage)
            slackAuthentication.gatherSlackRecyclerViewDetails(arrayListRecyclerViewItems = arrayListSlackFileName)
            if (slackAuthentication.checkMessageEmpty(activity = activity, context = context)) {
                progressBarSlack.visibility = View.VISIBLE
                progressBarSlackLayout.visibility = View.VISIBLE
                slackAuthentication.callSlack(
                    activity = activity,
                    context = context,
                    filePathMedia = filePathMedia,
                    slackTask = "create",
                    messagePath = slackAuthentication.channel,
                    slackType = "channel"
                )
            }
        }

        buttonSlackCreateUser.setSafeOnClickListener {
            slackAuthentication.gatherSlackUserSpinnerDetails(
                spinnerUser = spinnerUsers
            )
            slackAuthentication.gatherSlackUserEditTextDetails(editTextMessage = editTextMessageUser)
            slackAuthentication.gatherSlackRecyclerViewDetails(arrayListRecyclerViewItems = arrayListSlackFileName)
            if (slackAuthentication.checkMessageEmptyUser(
                    activity = activity,
                    context = context
                )
            ) {
                progressBarSlack.visibility = View.VISIBLE
                progressBarSlackLayout.visibility = View.VISIBLE
                slackAuthentication.callSlack(
                    activity = activity,
                    context = context,
                    filePathMedia = filePathMedia,
                    slackTask = "create",
                    messagePath = slackAuthentication.user,
                    slackType = "user"
                )
            }
        }

        buttonSlackCancel.setSafeOnClickListener {
            removeSlackLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }

        buttonSlackCancelUser.setSafeOnClickListener {
            removeSlackLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }

        toolbarSlack.setNavigationOnClickListener {
            removeSlackLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }

        slackBottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.slack_menu_channel -> {
                    slackChannelLayout.visibility = View.VISIBLE
                    slackUserLayout.visibility = View.GONE
                }

                R.id.slack_menu_user -> {
                    slackChannelLayout.visibility = View.GONE
                    slackUserLayout.visibility = View.VISIBLE
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
        toolbarSlack.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.slack_menu_refresh -> {
                    slackAuthentication.callSlack(
                        context = context,
                        activity = activity,
                        filePathMedia = filePathMedia,
                        slackTask = "get"
                    )
                    progressBarSlackLayout.visibility = View.VISIBLE
                    progressBarSlack.visibility = View.VISIBLE
                }
            }
            return@setOnMenuItemClickListener true
        }

    }

    private fun initializeSlackRecyclerView(filePathMedia: File) {

        recyclerViewSlackAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewSlackAttachmentUser.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        if (filePathMedia.exists()) {
            slackAdapter = RecyclerViewSlackAdapter(
                addSlackFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        }

        recyclerViewSlackAttachment.adapter = slackAdapter
        recyclerViewSlackAttachmentUser.adapter = slackAdapter

    }

    private fun addSlackFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        arrayListSlackFileName.add(RecyclerViewModel(file = filePathMedia))
        arrayListSlackFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        return arrayListSlackFileName
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        inputMethodManager.hideSoftInputFromWindow(viewJira.windowToken, 0)
    }

    private fun initializeStartDatePicker() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        var startDate = "$mYear-$mMonth-$mDayOfMonth"
        calendarViewJiraLayout =
            calendarViewJiraView.findViewById(R.id.jira_calendar_view_layout)
        calendarViewStartDate = calendarViewJiraView.findViewById(R.id.calendarView_start_date)
        buttonCalendarViewJiraCancel =
            calendarViewJiraView.findViewById(R.id.button_jira_calendar_cancel)
        buttonCalendarViewJiraOk =
            calendarViewJiraView.findViewById(R.id.button_jira_calendar_ok)

        calendarViewStartDate.minDate = System.currentTimeMillis()
        if (calendarViewJiraDate != null) {
            calendarViewStartDate.setDate(calendarViewJiraDate!!, true, true)
        }
        calendarViewJiraLayout.setOnClickListener {
            detachJiraDatePicker()
        }
        buttonCalendarViewJiraCancel.setOnClickListener {
            detachJiraDatePicker()
        }

        buttonCalendarViewJiraOk.setOnClickListener {
            jiraAuthentication.setStartDate(startDate = startDate)
            detachJiraDatePicker()
//            textViewRemoveDate.visibility = View.VISIBLE
            imageButtonRemoveDate.visibility = View.VISIBLE
        }
        calendarViewStartDate.setOnDateChangeListener { viewStartDate, year, month, dayOfMonth ->
            calendarViewJiraDate = viewStartDate.date
            startDate = "$year-$month-$dayOfMonth"
        }

    }

    private fun attachJiraDatePicker() {
        try {
            val rootView: ViewGroup =
                activity.window.decorView.findViewById(android.R.id.content)
            calendarViewJiraView =
                LayoutInflater.from(activity)
                    .inflate(R.layout.jira_calendar_view, rootView, false)
            windowManagerParamsJiraDatePicker =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            windowManagerJiraDatePicker = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerJiraDatePicker as WindowManager).addView(
                calendarViewJiraView,
                windowManagerParamsJiraDatePicker
            )
            initializeStartDatePicker()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraDatePopupTag)
        }
    }

    private fun detachJiraDatePicker() {
        if (this::calendarViewJiraView.isInitialized) {
            (windowManagerJiraDatePicker as WindowManager).removeViewImmediate(
                calendarViewJiraView
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun gatherUnhandledExceptionDetails() {
        try {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            val filePath = File(sharedPref.getString("unhandled_file_path", null)!!)
            CookieBar.build(activity)
                .setCustomView(R.layout.loggerbird_unhandled_popup)
                .setCustomViewInitializer {
                    val textViewDiscard =
                        it.findViewById<TextView>(R.id.textView_unhandled_discard)
                    val textViewShareWithJira =
                        it.findViewById<TextView>(R.id.textView_unhandled_share_jira)
                    val textViewCustomizeJira =
                        it.findViewById<TextView>(R.id.textView_unhandled_jira_customize)
                    val checkBoxDuplication =
                        it.findViewById<CheckBox>(R.id.checkBox_unhandled_jira)
                    if (sharedPref.getBoolean("duplication_enabled", false)) {
                        checkBoxDuplication.isChecked = true
                    }
                    checkBoxDuplication.setOnClickListener {
                        if (checkBoxDuplication.isChecked) {
                            with(sharedPref.edit()) {
                                putBoolean("duplication_enabled", true)
                                commit()
                            }
                            defaultToast.attachToast(
                                activity = activity,
                                toastMessage = activity.resources.getString(R.string.duplication_check_enabled)
                            )
                        } else {
                            with(sharedPref.edit()) {
                                putBoolean("duplication_enabled", false)
                                commit()
                            }
                            defaultToast.attachToast(
                                activity = activity,
                                toastMessage = activity.resources.getString(R.string.duplication_check_disabled)
                            )
                        }
                    }
                    textViewDiscard.setSafeOnClickListener {
                        if (filePath.exists()) {
                            filePath.delete()
                        }
                        val editor: SharedPreferences.Editor = sharedPref.edit()
                        editor.remove("unhandled_file_path")
                        editor.apply()
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.unhandled_file_discard_success)
                        )
                        CookieBar.dismiss(activity)
                    }
                    textViewShareWithJira.setSafeOnClickListener {
                        if (checkBoxDuplication.isChecked) {
                            attachProgressBar()
                            jiraAuthentication.callJiraIssue(
                                filePathName = filePath,
                                context = context,
                                activity = activity,
                                jiraTask = "unhandled_duplication",
                                createMethod = "default"
                            )
                        } else {
                            createDefaultUnhandledJiraIssue(filePath = filePath)
                        }
                    }
                    textViewCustomizeJira.setSafeOnClickListener {
                        if (checkBoxDuplication.isChecked) {
                            attachProgressBar()
                            jiraAuthentication.callJiraIssue(
                                filePathName = filePath,
                                context = context,
                                activity = activity,
                                jiraTask = "unhandled_duplication",
                                createMethod = "customize"
                            )
                        } else {
                            createCustomizedUnhandledJiraIssue(filePath = filePath)
                        }
                    }
                }.setSwipeToDismiss(false)
                .setEnableAutoDismiss(false)
                .show()


//            jiraAuthentication.callJiraIssue(
//                context = context,
//                activity = activity,
//                jiraTask = "duplicate_file",
//                createMethod = "duplicate_file"
//            )
        } catch (e: Exception) {
            detachProgressBar()
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.unhandledExceptionPopupTag
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    internal fun createDefaultUnhandledJiraIssue(filePath: File) {
        attachProgressBar()
        val coroutineCallUnhandledTask = CoroutineScope(Dispatchers.IO)
        coroutineCallUnhandledTask.async {
            jiraAuthentication.jiraUnhandledExceptionTask(
                context = context,
                activity = activity,
                filePath = filePath
            )
        }
        CookieBar.dismiss(activity)
    }

    internal fun createCustomizedUnhandledJiraIssue(filePath: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.GONE
            }
            initializeJiraLayout(filePathMedia = filePath)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkUnhandledFilePath(): Boolean {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        if (sharedPref.getString("unhandled_file_path", null) != null) {
            val filepath = File(sharedPref.getString("unhandled_file_path", null)!!)
            if (filepath.exists()) {
                return true
            } else {
                activity.runOnUiThread {
                    defaultToast.attachToast(
                        activity = activity,
                        toastMessage = activity.resources.getString(R.string.unhandled_file_doesnt_exist)
                    )
                }
            }
            return false
        }
        return false
    }

    private fun checkBoxUnhandledChecked(): Boolean {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        if (sharedPref.getBoolean("duplication_enabled", false)) {
            return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun unhandledExceptionCustomizeIssueSent() {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove("unhandled_file_path")
        editor.apply()
        CookieBar.dismiss(activity)
        activity.runOnUiThread {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.jira_sent)
            )
        }
    }

    //    internal fun addUnhandledExceptionMessage(context: Context, unhandledExceptionMessage: String) {
//        val sharedPref =
//            PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
//        if (checkContainedExceptionMessage(context = context)) {
//            sharedPref.edit().remove("unhandled_exception_message").commit()
//            arrayListUnhandledExceptionMessage.clear()
//        }
//        if (checkDuplicateExceptionMessage(
//                context = context,
//                unhandledExceptionMessage = unhandledExceptionMessage
//            )
//        ) {
//            with(sharedPref.edit()) {
//                putBoolean("unhandled_exception_message_duplication", true)
//                    .commit()
//            }
//        } else {
//            with(sharedPref.edit()) {
//                putBoolean("unhandled_exception_message_duplication", false)
//                    .commit()
//            }
//            with(sharedPref.edit()) {
//                arrayListUnhandledExceptionMessage.add(unhandledExceptionMessage)
//                val gson = Gson()
//                val json = gson.toJson(arrayListUnhandledExceptionMessage)
//                putString("unhandled_exception_message", json)
//                commit()
//            }
//        }
//    }
    internal fun addUnhandledExceptionMessage(
        context: Context,
        unhandledExceptionMessage: String
    ) {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        with(sharedPref.edit()) {
            putString("unhandled_exception_message", "class name:$unhandledExceptionMessage")
            commit()
        }
    }


//    private fun checkDuplicateExceptionMessage(
//        context: Context,
//        unhandledExceptionMessage: String
//    ): Boolean {
//        val sharedPref =
//            PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
//        if (sharedPref.getString("unhandled_exception_message", null) != null) {
//            val gson = Gson()
//            val json = sharedPref.getString("unhandled_exception_message", null)
//            if (json?.isNotEmpty()!!) {
//                val arrayListExceptionMessage: ArrayList<String> =
//                    gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
//                return arrayListExceptionMessage.contains(unhandledExceptionMessage)
//            }
//        }
//        return false
//    }
//
//    private fun checkContainedExceptionMessage(context: Context): Boolean {
//        val sharedPref =
//            PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
//        if (sharedPref.getString("unhandled_exception_message", null) != null) {
//            val gson = Gson()
//            val json = sharedPref.getString("unhandled_exception_message", null)
//            if (json?.isNotEmpty()!!) {
//                val arrayListExceptionMessage: ArrayList<String> =
//                    gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
//                arrayListUnhandledExceptionMessage.addAll(arrayListExceptionMessage)
//                return arrayListExceptionMessage.size > 20
//            }
//        }
//        return false
//    }
//
//    private fun checkUnhandledExceptionDuplicated(activity: Activity): Boolean {
//        val sharedPref =
//            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
//        if (sharedPref.getBoolean("unhandled_exception_message_duplication", false)) {
//            return true
//        }
//        return false
//    }

    internal fun attachUnhandledDuplicationLayout(
        unhandledExceptionIssueMethod: String,
        filePath: File
    ) {
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewUnhandledDuplication =
            LayoutInflater.from(activity)
                .inflate(R.layout.unhandled_duplication_popup, rootView, false)
        windowManagerParamsUnhandledDuplication =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        windowManagerUnhandledDuplication = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerUnhandledDuplication as WindowManager).addView(
            viewUnhandledDuplication,
            windowManagerParamsUnhandledDuplication
        )
        initializeUnhandledDuplicationButtons(
            unhandledExceptionIssueMethod = unhandledExceptionIssueMethod,
            filePath = filePath
        )
    }

    private fun detachUnhandledDuplicationLayout() {
        if (this::viewUnhandledDuplication.isInitialized) {
            (windowManagerUnhandledDuplication as WindowManager).removeViewImmediate(
                viewUnhandledDuplication
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeUnhandledDuplicationButtons(
        unhandledExceptionIssueMethod: String,
        filePath: File
    ) {
        val buttonProceed =
            viewUnhandledDuplication.findViewById<Button>(R.id.button_unhandled_duplication_proceed)
        val buttonCancel =
            viewUnhandledDuplication.findViewById<Button>(R.id.button_unhandled_duplication_cancel)
        buttonProceed.setSafeOnClickListener {
            when (unhandledExceptionIssueMethod) {
                "default" -> createDefaultUnhandledJiraIssue(filePath = filePath)
                "customize" -> createCustomizedUnhandledJiraIssue(filePath = filePath)
            }
            detachUnhandledDuplicationLayout()
        }
        buttonCancel.setSafeOnClickListener {
            detachUnhandledDuplicationLayout()
        }
    }

    private fun controlMedialFile(): Boolean {
        if (this@LoggerBirdService::filePathVideo.isInitialized) {
            return if (filePathVideo.exists()) {
                filePathVideo.delete()
                true
            } else {
                false
            }
        }
        if (this@LoggerBirdService::filePathAudio.isInitialized) {
            return if (filePathAudio.exists()) {
                filePathAudio.delete()
                true
            } else {
                false
            }
        }
        if (PaintView.controlScreenShotFile()) {
            return if (PaintView.filePathScreenShot.exists()) {
                PaintView.filePathScreenShot.delete()
                true
            } else {
                false
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeEmailLayout(filePathMedia: File) {
        arrayListEmailFileName.clear()
        arraylistEmailToUsername.clear()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewEmail =
            LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_email_popup, rootView, false)
        windowManagerParamsEmail =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        windowManagerEmail = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerEmail as WindowManager).addView(
            viewEmail,
            windowManagerParamsEmail
        )
        buttonEmailCreate =
            viewEmail.findViewById(R.id.button_email_create)
        buttonEmailCancel =
            viewEmail.findViewById(R.id.button_email_cancel)
        imageViewEmailAdd = viewEmail.findViewById(R.id.imageView_email_add)
        editTextTo = viewEmail.findViewById(R.id.editText_email_to)
        editTextSubject = viewEmail.findViewById(R.id.editText_email_subject)
        editTextContent = viewEmail.findViewById(R.id.editText_email_message)
        toolbarEmail = viewEmail.findViewById(R.id.toolbar_email)
        recyclerViewEmailAttachment =
            viewEmail.findViewById(R.id.recycler_view_email_attachment)
        recyclerViewEmailToList = viewEmail.findViewById(R.id.recycler_view_email_to_list)
        cardViewToList = viewEmail.findViewById(R.id.cardView_to_list)
        initializeEmailRecyclerView(filePathMedia = filePathMedia)
        initializeEmailToRecyclerView()
        initializeEmailButtons(filePathMedia = filePathMedia)
//        detachProgressBar()
    }

    internal fun removeEmailLayout() {
        if (this::viewEmail.isInitialized && windowManagerEmail != null) {
            (windowManagerEmail as WindowManager).removeViewImmediate(
                viewEmail
            )
            windowManagerEmail = null
            arrayListEmailFileName.clear()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeEmailButtons(filePathMedia: File) {
        try {
            buttonEmailCreate.setSafeOnClickListener {
                if (checkBoxFutureTask.isChecked) {
                    attachProgressBar()
                    val coroutineCallFutureTask = CoroutineScope(Dispatchers.IO)
                    coroutineCallFutureTask.async {
//                        if (arraylistEmailToUsername.isNotEmpty()) {
//                            arraylistEmailToUsername.forEach {
//                                createFutureTaskEmail()
//                            }
//                        } else {
//                            if (checkEmailFormat(editTextTo.text.toString())) {
//                                createFutureTaskEmail()
//                            }
//                        }
                        createFutureTaskEmail()
                        activity.runOnUiThread {
                            removeEmailLayout()
                            defaultToast.attachToast(
                                activity = activity,
                                toastMessage = activity.resources.getString(R.string.future_task_enabled)
                            )
                            finishShareLayout(message = "single_email")
                        }
                    }

                } else {
                    if (arraylistEmailToUsername.isNotEmpty()) {
                        arraylistEmailToUsername.forEach {
                            callEmail(filePathMedia = filePathMedia, to = it.email)
                        }
                    } else {
                        if (checkEmailFormat(editTextTo.text.toString())) {
                            callEmail(
                                filePathMedia = filePathMedia,
                                to = editTextTo.text.toString()
                            )
                        }
                    }
                }
            }
            buttonEmailCancel.setSafeOnClickListener {
                removeEmailLayout()
            }
            imageViewEmailAdd.setSafeOnClickListener {
                if (checkEmailFormat(editTextTo.text.toString())) {
                    cardViewToList.visibility = View.VISIBLE
                    addEmailToUser(email = editTextTo.text.toString())
                }
            }
            toolbarEmail.setNavigationOnClickListener {
                removeEmailLayout()
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callEnqueueEmail()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)

        }
    }

    private fun createFutureTaskEmail() {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        with(sharedPref.edit()) {
            if(arraylistEmailToUsername.isNotEmpty()){
                addFutureUserList()
            }else{
                putString("future_task_email_to", editTextTo.text.toString())
            }
            putString("future_task_email_subject", editTextSubject.text.toString())
            putString("future_task_email_message", editTextContent.text.toString())
//            putString("future_task_email_file", filePathMedia.absolutePath)
            commit()
        }
        addFutureFileList()
        val intentServiceFuture =
            Intent(context, LoggerBirdFutureTaskService::class.java)
        context.startForegroundService(intentServiceFuture)
        controlFutureTask = true
    }

    private fun createEmailTask(filePathMedia: File, to: String) {
        try {
            attachProgressBar()
            sendSingleMediaFile(
                filePathMedia = filePathMedia,
                to = to,
                subject = editTextSubject.text.toString(),
                message = editTextContent.text.toString()
            )

        } catch (e: Exception) {
            e.printStackTrace()
            resetEnqueueMail()
            detachProgressBar()
            removeEmailLayout()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeEmailRecyclerView(filePathMedia: File) {
        recyclerViewEmailAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        emailAdapter = RecyclerViewEmailAdapter(
            addEmailFileNames(filePathMedia = filePathMedia),
            context = context,
            activity = activity,
            rootView = rootView
        )
        recyclerViewEmailAttachment.adapter = emailAdapter
    }

    private fun initializeEmailToRecyclerView() {
        recyclerViewEmailToList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        emailToListAdapter = RecyclerViewEmaiToListAdapter(
            arraylistEmailToUsername,
            cardView = cardViewToList,
            context = context,
            activity = activity,
            rootView = rootView
        )
        recyclerViewEmailToList.adapter = emailToListAdapter
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addEmailToUser(email: String) {
        if (!arraylistEmailToUsername.contains(RecyclerViewModelTo(email = email))) {
            arraylistEmailToUsername.add(RecyclerViewModelTo(email = email))
            emailToListAdapter.notifyDataSetChanged()
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.email_to_duplication)
            )
        }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addEmailFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListEmailFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (!checkUnhandledFilePath() && LoggerBird.filePathSecessionName.exists()) {
            arrayListEmailFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListEmailFileName
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkEmailFormat(to: String): Boolean {
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(to).matches()) {
            true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.email_invalid_format)
            )
            false
        }
    }

    private fun checkBoxFutureTaskIsChecked(filePathMedia: File): Boolean {
        if (checkBoxFutureTask.isChecked) {
            initializeFutureTaskLayout(filePathMedia = filePathMedia)
            return true
        }
        return false
    }

    private fun initializeFutureTaskLayout(filePathMedia: File) {
        removeFutureLayout()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewFutureTask =
            LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_future_task_popup, rootView, false)
        windowManagerParamsFutureTask =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        windowManagerFutureTask = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerFutureTask as WindowManager).addView(
            viewFutureTask,
            windowManagerParamsFutureTask
        )
        imageViewFutureCalendar = viewFutureTask.findViewById(R.id.imageView_future_task_date)
        imageButtonFutureTaskRemoveDate =
            viewFutureTask.findViewById(R.id.image_button_future_date_remove)
        imageViewFutureTime = viewFutureTask.findViewById(R.id.imageView_future_task_time)
        imageButtonFutureTaskRemoveTime =
            viewFutureTask.findViewById(R.id.image_button_future_time_remove)
        buttonFutureTaskProceed = viewFutureTask.findViewById(R.id.button_future_task_proceed)
        buttonFutureTaskCancel = viewFutureTask.findViewById(R.id.button_future_task_cancel)

        buttonClicksFuture(filePathMedia = filePathMedia)
    }

    private fun removeFutureLayout() {
        if (this::viewFutureTask.isInitialized && windowManagerFutureTask != null) {
            (windowManagerFutureTask as WindowManager).removeViewImmediate(
                viewFutureTask
            )
            windowManagerFutureTask = null
        }
    }

    private fun buttonClicksFuture(filePathMedia: File) {
        imageViewFutureCalendar.setSafeOnClickListener {
            initializeFutureDateLayout()
        }
        imageButtonFutureTaskRemoveDate.setSafeOnClickListener {
            futureStartDate = null
            imageButtonFutureTaskRemoveDate.visibility = View.GONE
        }
        imageViewFutureTime.setSafeOnClickListener {
            initializeFutureTimeLayout()
        }
        imageButtonFutureTaskRemoveTime.setSafeOnClickListener {
            futureStartTime = null
            imageButtonFutureTaskRemoveTime.visibility = View.GONE
        }
        buttonFutureTaskProceed.setSafeOnClickListener {
            checkDateAndTimeEmpty(filePathMedia = filePathMedia)
        }
        buttonFutureTaskCancel.setSafeOnClickListener {
            checkBoxFutureTask.isChecked = false
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            with(sharedPref.edit()) {
                remove("future_task_time")
                remove("future_file_path")
                commit()
            }
            futureStartDate = null
            futureStartTime = null
            removeFutureLayout()
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.future_task_disabled)
            )
        }
    }

    private fun initializeFutureDateLayout() {
        removeFutureDateLayout()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewFutureDate =
            LayoutInflater.from(activity)
                .inflate(R.layout.future_calendar_view, rootView, false)
        windowManagerParamsFutureDate =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        windowManagerFutureDate = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerFutureDate as WindowManager).addView(
            viewFutureDate,
            windowManagerParamsFutureDate
        )
        frameLayoutDate = viewFutureDate.findViewById(R.id.future_calendar_view_layout)
        calendarViewFutureTask = viewFutureDate.findViewById(R.id.calendarView_start_date)
        buttonFutureTaskDateCreate = viewFutureDate.findViewById(R.id.button_future_calendar_ok)
        buttonFutureTaskDateCancel = viewFutureDate.findViewById(R.id.button_future_calendar_cancel)

        buttonClicksFutureDate()

    }

    private fun removeFutureDateLayout() {
        if (this::viewFutureDate.isInitialized && windowManagerFutureDate != null) {
            (windowManagerFutureDate as WindowManager).removeViewImmediate(
                viewFutureDate
            )
            windowManagerFutureDate = null
        }
    }

    private fun buttonClicksFutureDate() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        calendarFuture.set(mYear, mMonth, mDayOfMonth)
        calendarViewFutureTask.minDate = System.currentTimeMillis()
        frameLayoutDate.setOnClickListener {
            removeFutureDateLayout()
        }
        calendarViewFutureTask.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendarFuture.set(year, month, dayOfMonth)
//            startDate = "$year-$month-$dayOfMonth"
        }
        buttonFutureTaskDateCreate.setSafeOnClickListener {
            futureStartDate = calendarViewFutureTask.date
            Log.d("time", futureStartDate.toString())
            Log.d("time", System.currentTimeMillis().toString())
            imageButtonFutureTaskRemoveDate.visibility = View.VISIBLE
            removeFutureDateLayout()
        }
        buttonFutureTaskDateCancel.setSafeOnClickListener {
            removeFutureDateLayout()
        }
    }

    private fun initializeFutureTimeLayout() {
        removeFutureTimeLayout()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewFutureTime =
            LayoutInflater.from(activity)
                .inflate(R.layout.future_time_picker, rootView, false)
        windowManagerParamsFutureTime =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        windowManagerFutureTime = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerFutureTime as WindowManager).addView(
            viewFutureTime,
            windowManagerParamsFutureTime
        )
        frameLayoutTime = viewFutureTime.findViewById(R.id.future_time_view_layout)
        timePickerFutureTask = viewFutureTime.findViewById(R.id.timePicker_start_time)
        buttonFutureTaskTimeCreate = viewFutureTime.findViewById(R.id.button_future_time_ok)
        buttonFutureTaskTimeCancel = viewFutureTime.findViewById(R.id.button_future_time_cancel)

        buttonClicksFutureTime()

    }

    private fun removeFutureTimeLayout() {
        if (this::viewFutureTime.isInitialized && windowManagerFutureTime != null) {
            (windowManagerFutureTime as WindowManager).removeViewImmediate(
                viewFutureTime
            )
            windowManagerFutureTime = null
        }
    }

    private fun buttonClicksFutureTime() {
        frameLayoutTime.setOnClickListener {
            removeFutureTimeLayout()
        }
//        timePickerFutureTask.setOnTimeChangedListener { view, hourOfDay, minute ->
//
//        }
        buttonFutureTaskTimeCreate.setSafeOnClickListener {
            calendarFuture.set(Calendar.HOUR_OF_DAY, timePickerFutureTask.hour)
            calendarFuture.set(Calendar.MINUTE, timePickerFutureTask.minute)
            futureStartTime =
                timePickerFutureTask.hour.toLong() + timePickerFutureTask.minute.toLong()
            imageButtonFutureTaskRemoveTime.visibility = View.VISIBLE
            removeFutureTimeLayout()
        }
        buttonFutureTaskTimeCancel.setSafeOnClickListener {
            removeFutureTimeLayout()
        }
    }

    private fun checkDateAndTimeEmpty(filePathMedia: File) {
        if (futureStartDate != null && futureStartTime != null) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.future_task_gathered)
            )
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            with(sharedPref.edit()) {
                putLong("future_task_time", calendarFuture.timeInMillis)
                putString("future_file_path", filePathMedia.absolutePath)
                commit()
            }
            removeFutureLayout()
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.future_task_empty)
            )
        }
    }

    private fun addFutureFileList() {
        val arrayListFileNames: ArrayList<String> = ArrayList()
        RecyclerViewEmailAdapter.ViewHolder.arrayListFilePaths.forEach {
            if (it.file.name == "logger_bird_details.txt") {
                val futureLoggerBirdFile = File(context.filesDir,"logger_bird_details_future.txt")
                if(!futureLoggerBirdFile.exists()){
                    futureLoggerBirdFile.createNewFile()
                }else{
                    futureLoggerBirdFile.delete()
                    futureLoggerBirdFile.createNewFile()
                }
                val scanner = Scanner(it.file)
                do {
                    futureLoggerBirdFile.appendText(scanner.nextLine() + "\n")
                } while (scanner.hasNextLine())
                arrayListFileNames.add(futureLoggerBirdFile.absolutePath)
            }else{
                arrayListFileNames.add(it.file.absolutePath)
            }

        }
        val gson = Gson()
        val json = gson.toJson(arrayListFileNames)
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext) ?: return
        with(sharedPref.edit()) {
            putString("file_future_list", json)
            commit()
        }
    }

    private fun addFutureUserList() {
        val arrayListUsers:ArrayList<String> = ArrayList()
        arraylistEmailToUsername.forEach {
            arrayListUsers.add(it.email)
        }
        val gson = Gson()
        val json = gson.toJson(arrayListUsers)
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext) ?: return
        with(sharedPref.edit()) {
            putString("user_future_list", json)
            commit()
        }
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