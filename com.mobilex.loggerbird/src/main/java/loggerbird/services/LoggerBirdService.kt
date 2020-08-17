package loggerbird.services

import loggerbird.adapter.autoCompleteTextViews.api.basecamp.AutoCompleteTextViewBasecampCategoryAdapter
import loggerbird.adapter.autoCompleteTextViews.api.trello.AutoCompleteTextViewTrelloLabelAdapter
import loggerbird.adapter.recyclerView.api.asana.RecyclerViewAsanaAttachmentAdapter
import loggerbird.adapter.recyclerView.api.asana.RecyclerViewAsanaSubTaskAdapter
import loggerbird.adapter.recyclerView.api.basecamp.RecyclerViewBasecampAssigneeAdapter
import loggerbird.adapter.recyclerView.api.basecamp.RecyclerViewBasecampAttachmentAdapter
import loggerbird.adapter.recyclerView.api.basecamp.RecyclerViewBasecampNotifyAdapter
import loggerbird.adapter.recyclerView.api.bitbucket.RecyclerViewBitbucketAttachmentAdapter
import loggerbird.adapter.recyclerView.api.clubhouse.RecyclerViewClubhouseAttachmentAdapter
import loggerbird.adapter.recyclerView.api.github.RecyclerViewGithubAssigneeAdapter
import loggerbird.adapter.recyclerView.api.github.RecyclerViewGithubAttachmentAdapter
import loggerbird.adapter.recyclerView.api.github.RecyclerViewGithubLabelAdapter
import loggerbird.adapter.recyclerView.api.github.RecyclerViewGithubProjectAdapter
import loggerbird.adapter.recyclerView.api.gitlab.RecyclerViewGitlabAttachmentAdapter
import loggerbird.adapter.recyclerView.api.jira.RecyclerViewJiraAttachmentAdapter
import loggerbird.adapter.recyclerView.api.jira.RecyclerViewJiraComponentAdapter
import loggerbird.adapter.recyclerView.api.jira.RecyclerViewJiraFixVersionsAdapter
import loggerbird.adapter.recyclerView.api.jira.RecyclerViewJiraIssueAdapter
import loggerbird.adapter.recyclerView.api.jira.RecyclerViewJiraLabelAdapter
import loggerbird.adapter.recyclerView.api.pivotal.*
import loggerbird.adapter.recyclerView.api.pivotal.RecyclerViewPivotalAttachmentAdapter
import loggerbird.adapter.recyclerView.api.pivotal.RecyclerViewPivotalBlockerAdapter
import loggerbird.adapter.recyclerView.api.pivotal.RecyclerViewPivotalLabelAdapter
import loggerbird.adapter.recyclerView.api.pivotal.RecyclerViewPivotalOwnerAdapter
import loggerbird.adapter.recyclerView.api.slack.RecyclerViewSlackAttachmentAdapter
import loggerbird.adapter.recyclerView.api.trello.RecyclerViewTrelloAttachmentAdapter
import loggerbird.adapter.recyclerView.api.trello.RecyclerViewTrelloCheckListAdapter
import loggerbird.adapter.recyclerView.api.trello.RecyclerViewTrelloLabelAdapter
import loggerbird.adapter.recyclerView.api.trello.RecyclerViewTrelloMemberAdapter
import loggerbird.adapter.recyclerView.email.RecyclerViewEmaiToListAttachmentAdapter
import loggerbird.adapter.recyclerView.email.RecyclerViewEmailAttachmentAdapter
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jakewharton.rxbinding2.view.RxView
import com.mobilex.loggerbird.R
import loggerbird.constants.Constants
import loggerbird.exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird
import loggerbird.models.*
import loggerbird.models.recyclerView.*
import loggerbird.observers.LogActivityLifeCycleObserver
import loggerbird.paint.PaintActivity
import loggerbird.paint.PaintView
import loggerbird.utils.email.EmailUtil
import loggerbird.utils.other.LinkedBlockingQueueUtil
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import java.text.SimpleDateFormat
import android.text.InputFilter
import androidx.core.widget.addTextChangedListener
import loggerbird.adapter.autoCompleteTextViews.api.asana.AutoCompleteTextViewAsanaAssigneeAdapter
import loggerbird.adapter.autoCompleteTextViews.api.asana.AutoCompleteTextViewAsanaPriorityAdapter
import loggerbird.adapter.autoCompleteTextViews.api.asana.AutoCompleteTextViewAsanaProjectAdapter
import loggerbird.adapter.autoCompleteTextViews.api.asana.AutoCompleteTextViewAsanaSectorAdapter
import loggerbird.adapter.autoCompleteTextViews.api.basecamp.AutoCompleteTextViewBasecampAssigneeAdapter
import loggerbird.adapter.autoCompleteTextViews.api.basecamp.AutoCompleteTextViewBasecampNotifyAdapter
import loggerbird.adapter.autoCompleteTextViews.api.basecamp.AutoCompleteTextViewBasecampProjectAdapter
import loggerbird.adapter.autoCompleteTextViews.api.bitbucket.AutoCompleteTextViewBitbucketAssigneeAdapter
import loggerbird.adapter.autoCompleteTextViews.api.bitbucket.AutoCompleteTextViewBitbucketKindAdapter
import loggerbird.adapter.autoCompleteTextViews.api.bitbucket.AutoCompleteTextViewBitbucketPriorityAdapter
import loggerbird.adapter.autoCompleteTextViews.api.bitbucket.AutoCompleteTextViewBitbucketProjectAdapter
import loggerbird.adapter.autoCompleteTextViews.api.clubhouse.AutoCompleteTextViewClubhouseEpicAdapter
import loggerbird.adapter.autoCompleteTextViews.api.clubhouse.AutoCompleteTextViewClubhouseProjectAdapter
import loggerbird.adapter.autoCompleteTextViews.api.clubhouse.AutoCompleteTextViewClubhouseRequesterAdapter
import loggerbird.adapter.autoCompleteTextViews.api.clubhouse.AutoCompleteTextViewClubhouseStoryTypeAdapter
import loggerbird.adapter.autoCompleteTextViews.api.github.*
import loggerbird.adapter.autoCompleteTextViews.api.github.AutoCompleteTextViewGithubAssigneeAdapter
import loggerbird.adapter.autoCompleteTextViews.api.github.AutoCompleteTextViewGithubLabelAdapter
import loggerbird.adapter.autoCompleteTextViews.api.github.AutoCompleteTextViewGithubMilestoneAdapter
import loggerbird.adapter.autoCompleteTextViews.api.github.AutoCompleteTextViewGithubProjectAdapter
import loggerbird.adapter.autoCompleteTextViews.api.github.AutoCompleteTextViewGithubRepoAdapter
import loggerbird.adapter.autoCompleteTextViews.api.gitlab.*
import loggerbird.adapter.autoCompleteTextViews.api.gitlab.AutoCompleteTextViewGitlabAssigneeAdapter
import loggerbird.adapter.autoCompleteTextViews.api.gitlab.AutoCompleteTextViewGitlabConfidentialityAdapter
import loggerbird.adapter.autoCompleteTextViews.api.gitlab.AutoCompleteTextViewGitlabLabelAdapter
import loggerbird.adapter.autoCompleteTextViews.api.gitlab.AutoCompleteTextViewGitlabProjectAdapter
import loggerbird.adapter.autoCompleteTextViews.api.jira.*
import loggerbird.adapter.autoCompleteTextViews.api.jira.AutoCompleteTextViewJiraIssueAdapter
import loggerbird.adapter.autoCompleteTextViews.api.jira.AutoCompleteTextViewJiraIssueTypeAdapter
import loggerbird.adapter.autoCompleteTextViews.api.jira.AutoCompleteTextViewJiraLinkedIssueAdapter
import loggerbird.adapter.autoCompleteTextViews.api.jira.AutoCompleteTextViewJiraProjectAdapter
import loggerbird.adapter.autoCompleteTextViews.api.jira.AutoCompleteTextViewJiraReporterAdapter
import loggerbird.adapter.autoCompleteTextViews.api.pivotal.*
import loggerbird.adapter.autoCompleteTextViews.api.pivotal.AutoCompleteTextViewPivotalOwnersAdapter
import loggerbird.adapter.autoCompleteTextViews.api.pivotal.AutoCompleteTextViewPivotalPointsAdapter
import loggerbird.adapter.autoCompleteTextViews.api.pivotal.AutoCompleteTextViewPivotalProjectAdapter
import loggerbird.adapter.autoCompleteTextViews.api.pivotal.AutoCompleteTextViewPivotalRequesterAdapter
import loggerbird.adapter.autoCompleteTextViews.api.pivotal.AutoCompleteTextViewPivotalStoryTypeAdapter
import loggerbird.adapter.autoCompleteTextViews.api.trello.AutoCompleteTextViewTrelloBoardAdapter
import loggerbird.adapter.autoCompleteTextViews.api.trello.AutoCompleteTextViewTrelloMemberAdapter
import loggerbird.adapter.autoCompleteTextViews.api.trello.AutoCompleteTextViewTrelloProjectAdapter
import loggerbird.adapter.recyclerView.fileAction.RecyclerViewFileActionAttachmentAdapter
import loggerbird.listeners.OnSwipeTouchListener
import loggerbird.listeners.floatingActionButtons.FloatingActionButtonOnTouchListener
import loggerbird.listeners.layouts.LayoutFeedbackOnTouchListener
import loggerbird.listeners.layouts.LayoutJiraOnTouchListener
import loggerbird.models.room.UnhandledDuplication
import loggerbird.room.UnhandledDuplicationDb
import loggerbird.utils.api.asana.AsanaApi
import loggerbird.utils.api.basecamp.BasecampApi
import loggerbird.utils.api.bitbucket.BitbucketApi
import loggerbird.utils.api.clubhouse.ClubhouseApi
import loggerbird.utils.api.github.GithubApi
import loggerbird.utils.api.gitlab.GitlabApi
import loggerbird.utils.api.jira.JiraApi
import loggerbird.utils.api.pivotal.PivotalTrackerApi
import loggerbird.utils.api.slack.SlackApi
import loggerbird.utils.api.trello.TrelloApi
import loggerbird.utils.other.DefaultToast
import loggerbird.utils.other.InputTypeFilter

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
    private var windowManagerJiraDatePicker: Any? = null
    private var windowManagerSlack: Any? = null
    private var windowManagerUnhandledDuplication: Any? = null
    private var windowManagerEmail: Any? = null
    private var windowManagerFutureTask: Any? = null
    private var windowManagerFutureDate: Any? = null
    private var windowManagerFutureTime: Any? = null
    private var windowManagerGithub: Any? = null
    private var windowManagerGitlab: Any? = null
    private var windowManagerGitlabDatePicker: Any? = null
    private var windowManagerTrello: Any? = null
    private var windowManagerTrelloTimeline: Any? = null
    private var windowManagerTrelloTime: Any? = null
    private var windowManagerTrelloDate: Any? = null
    private var windowManagerPivotal: Any? = null
    private var windowManagerBasecamp: Any? = null
    private var windowManagerBasecampDate: Any? = null
    private var windowManagerAsana: Any? = null
    private var windowManagerAsanaDate: Any? = null
    private var windowManagerClubhouse: Any? = null
    private var windowManagerClubhouseDatePicker: Any? = null
    private var windowManagerLoggerBirdActivatePopup: Any? = null
    private var windowManagerLoggerBirdStartPopup: Any? = null
    private var windowManagerLoggerBirdDismissPopup: Any? = null
    private var windowManagerLoggerBirdFileActionPopup: Any? = null
    private var windowManagerLoggerBirdUnhandledException: Any? = null
    private var windowManagerBitbucket: Any? = null
    private var windowManagerLoggerBirdFileActionListPopup: Any? = null
    private lateinit var windowManagerParams: WindowManager.LayoutParams
    private lateinit var windowManagerParamsFeedback: WindowManager.LayoutParams
    private lateinit var windowManagerParamsProgressBar: WindowManager.LayoutParams
    private lateinit var windowManagerParamsJira: WindowManager.LayoutParams
    private lateinit var windowManagerParamsSlack: WindowManager.LayoutParams
    private lateinit var windowManagerParamsJiraDatePicker: WindowManager.LayoutParams
    private lateinit var windowManagerParamsGitlabDatePicker: WindowManager.LayoutParams
    private lateinit var windowManagerParamsUnhandledDuplication: WindowManager.LayoutParams
    private lateinit var windowManagerParamsEmail: WindowManager.LayoutParams
    private lateinit var windowManagerParamsFutureTask: WindowManager.LayoutParams
    private lateinit var windowManagerParamsFutureDate: WindowManager.LayoutParams
    private lateinit var windowManagerParamsFutureTime: WindowManager.LayoutParams
    private lateinit var windowManagerParamsGithub: WindowManager.LayoutParams
    private lateinit var windowManagerParamsGitlab: WindowManager.LayoutParams
    private lateinit var windowManagerParamsTrello: WindowManager.LayoutParams
    private lateinit var windowManagerParamsTrelloTimeline: WindowManager.LayoutParams
    private lateinit var windowManagerParamsTrelloDate: WindowManager.LayoutParams
    private lateinit var windowManagerParamsTrelloTime: WindowManager.LayoutParams
    private lateinit var windowManagerParamsPivotal: WindowManager.LayoutParams
    private lateinit var windowManagerParamsBaseCamp: WindowManager.LayoutParams
    private lateinit var windowManagerParamsBaseCampDate: WindowManager.LayoutParams
    private lateinit var windowManagerParamsAsana: WindowManager.LayoutParams
    private lateinit var windowManagerParamsAsanaDate: WindowManager.LayoutParams
    private lateinit var windowManagerParamsClubhouse: WindowManager.LayoutParams
    private lateinit var windowManagerParamsClubhouseDatePicker: WindowManager.LayoutParams
    private lateinit var windowManagerParamsLoggerBirdActivatePopup: WindowManager.LayoutParams
    private lateinit var windowManagerParamsLoggerBirdStartPopup: WindowManager.LayoutParams
    private lateinit var windowManagerParamsLoggerBirdDismissPopup: WindowManager.LayoutParams
    private lateinit var windowManagerParamsLoggerBirdFileAction: WindowManager.LayoutParams
    private lateinit var windowManagerParamsLoggerBirdUnhandledException: WindowManager.LayoutParams
    private lateinit var windowManagerParamsBitbucket: WindowManager.LayoutParams
    private lateinit var windowManagerParamsLoggerBirdFileActionList: WindowManager.LayoutParams
    private var coroutineCallScreenShot: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallVideo: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAudio: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallVideoStarter = CoroutineScope(Dispatchers.IO)
    private val coroutineCallSendSingleFile: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineCallDiscardFile: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val coroutineCallFileActionList: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var mediaRecorderAudio: MediaRecorder? = null
    private var state: Boolean = false
    private var filePathVideo: File? = null
    private var filePathAudio: File? = null
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
    private var isFabEnable: Boolean = false
    private var isActivateDialogShown: Boolean = false
    private var coroutineCallVideoCounter: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAudioCounter: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallVideoFileSize: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallAudioFileSize: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallFeedback: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var counterVideo: Int = 0
    private var counterAudio: Int = 0
    private var counterMediaLimit: Int = 60000
    private var timerVideo: Timer? = null
    private var timerAudio: Timer? = null
    private var timerVideoFileSize: Timer? = null
    private var timerAudioFileSize: Timer? = null
    private var timerTaskVideo: TimerTask? = null
    private var timerTaskAudio: TimerTask? = null
    private var timerVideoTaskFileSize: TimerTask? = null
    private var timerAudioTaskFileSize: TimerTask? = null
    private lateinit var viewFeedback: View
    private lateinit var viewJira: View
    private lateinit var viewSlack: View
    private lateinit var viewUnhandledDuplication: View
    private lateinit var viewEmail: View
    private lateinit var viewFutureTask: View
    private lateinit var viewFutureDate: View
    private lateinit var viewFutureTime: View
    private lateinit var viewGithub: View
    private lateinit var viewGitlab: View
    private lateinit var viewTrello: View
    private lateinit var viewTrelloTimeline: View
    private lateinit var viewTrelloDate: View
    private lateinit var viewTrelloTime: View
    private lateinit var viewPivotal: View
    private lateinit var viewBasecamp: View
    private lateinit var viewBasecampDate: View
    private lateinit var viewAsana: View
    private lateinit var viewAsanaDate: View
    private lateinit var viewClubhouse: View
    private lateinit var viewLoggerBirdActivatePopup: View
    private lateinit var viewLoggerBirdStartPopup: View
    private lateinit var viewLoggerBirdDismissPopup: View
    private lateinit var viewLoggerBirdFileActionPopup: View
    private lateinit var viewLoggerBirdUnhandledExceptionPopup: View
    private lateinit var viewLoggerBirdFileActionListPopup: View
    private lateinit var viewBitbucket: View
    private val fileLimit: Long = 20971520
    private var sessionTimeStart: Long? = System.currentTimeMillis()
    private var sessionTimeEnd: Long? = null
    private var timeControllerVideo: Long? = null
    private var controlTimeControllerVideo: Boolean = false
    private lateinit var mediaCodecsFile: File
    private val arrayListFileName: ArrayList<String> = ArrayList()
    private val coroutineCallFilesAction: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var controlFileAction: Boolean = false
    private lateinit var progressBarView: View
    private val defaultToast: DefaultToast = DefaultToast()
    private var unhandledMediaFilePath: String? = null

    //Jira:
    internal val jiraAuthentication = JiraApi()
    private lateinit var autoTextViewJiraProject: AutoCompleteTextView
    private lateinit var autoTextViewJiraIssueType: AutoCompleteTextView
    private lateinit var recyclerViewJiraAttachment: RecyclerView
    private lateinit var editTextJiraSummary: EditText
    private lateinit var editTextJiraDescription: EditText
    private lateinit var autoTextViewJiraReporter: AutoCompleteTextView
    private lateinit var autoTextViewJiraLinkedIssue: AutoCompleteTextView
    private lateinit var autoTextViewJiraIssue: AutoCompleteTextView
    private lateinit var autoTextViewJiraAssignee: AutoCompleteTextView
    private lateinit var autoTextViewJiraPriority: AutoCompleteTextView
    private lateinit var autoTextViewJiraComponent: AutoCompleteTextView
    private lateinit var autoTextViewJiraFixVersions: AutoCompleteTextView
    private lateinit var autoTextViewJiraLabel: AutoCompleteTextView
    private lateinit var autoTextViewJiraEpicLink: AutoCompleteTextView
    private lateinit var autoTextViewJiraSprint: AutoCompleteTextView
    private lateinit var autoTextViewJiraEpicName: AutoCompleteTextView
    private lateinit var buttonJiraCreate: Button
    internal lateinit var buttonJiraCancel: Button
    private lateinit var layoutJira: FrameLayout
    private lateinit var toolbarJira: Toolbar
    private lateinit var progressBarJira: ProgressBar
    private lateinit var progressBarJiraLayout: FrameLayout
    private lateinit var cardViewJiraSprint: CardView
    private lateinit var cardViewJiraStartDate: CardView
    private lateinit var cardViewJiraEpicName: CardView
    private lateinit var cardViewJiraEpicLink: CardView
    private lateinit var imageViewStartDate: ImageView
    private lateinit var imageButtonRemoveDate: ImageButton
    private lateinit var calendarViewJiraStartDate: CalendarView
    private lateinit var calendarViewJiraView: View
    private lateinit var calendarViewJiraLayout: FrameLayout
    private var calendarViewJiraDate: Long? = null
    private lateinit var buttonCalendarViewJiraCancel: Button
    private lateinit var buttonCalendarViewJiraOk: Button
    private lateinit var scrollViewJira: ScrollView
    private val arrayListJiraFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var jiraAttachmentAdapter: RecyclerViewJiraAttachmentAdapter
    private lateinit var autoTextViewJiraProjectAdapter: AutoCompleteTextViewJiraProjectAdapter
    private lateinit var autoTextViewJiraIssueTypeAdapter: AutoCompleteTextViewJiraIssueTypeAdapter
    private lateinit var autoTextViewJiraReporterAdapter: AutoCompleteTextViewJiraReporterAdapter
    private lateinit var autoTextViewJiraLinkedIssueAdapter: AutoCompleteTextViewJiraLinkedIssueAdapter
    private lateinit var autoTextViewJiraIssueAdapter: AutoCompleteTextViewJiraIssueAdapter
    private lateinit var autoTextViewJiraAssigneeAdapter: AutoCompleteTextViewJiraAssigneeAdapter
    private lateinit var autoTextViewJiraPriorityAdapter: AutoCompleteTextViewJiraPriorityAdapter
    private lateinit var autoTextViewFixVersionsAdapter: AutoCompleteTextViewJiraFixVersionsAdapter
    private lateinit var autoTextViewJiraComponentAdapter: AutoCompleteTextViewJiraComponentAdapter
    private lateinit var autoTextViewJiraLabelAdapter: AutoCompleteTextViewJiraLabelAdapter
    private lateinit var autoTextViewJiraEpicLinkAdapter: AutoCompleteTextViewJiraEpicLinkAdapter
    private lateinit var autoTextViewJiraSprintAdapter: AutoCompleteTextViewJiraSprintAdapter
    private lateinit var autoTextViewJiraEpicNameAdapter: AutoCompleteTextViewJiraEpicNameAdapter
    private var projectJiraPosition: Int = 0
    private var controlProjectJiraPosition: Boolean = false
    internal lateinit var cardViewJiraIssueList: CardView
    private lateinit var recyclerViewJiraIssueList: RecyclerView
    private lateinit var jiraAdapterIssueList: RecyclerViewJiraIssueAdapter
    private lateinit var imageViewJiraIssue: ImageView
    private val arrayListJiraIssueName: ArrayList<RecyclerViewModelIssue> = ArrayList()
    private var arrayListJiraIssue: ArrayList<String> = ArrayList()
    internal lateinit var cardViewJiraLabelList: CardView
    private lateinit var recyclerViewJiraLabelList: RecyclerView
    private lateinit var jiraAdapterLabelList: RecyclerViewJiraLabelAdapter
    private lateinit var imageViewJiraLabel: ImageView
    private val arrayListJiraLabelName: ArrayList<RecyclerViewModelLabel> = ArrayList()
    private var arrayListJiraLabel: ArrayList<String> = ArrayList()
    internal lateinit var cardViewJiraComponentList: CardView
    private lateinit var recyclerViewJiraComponentList: RecyclerView
    private lateinit var jiraAdapterComponentList: RecyclerViewJiraComponentAdapter
    private lateinit var imageViewJiraComponent: ImageView
    private val arrayListJiraComponentName: ArrayList<RecyclerViewModelComponent> = ArrayList()
    private var arrayListJiraComponent: ArrayList<String> = ArrayList()
    internal lateinit var cardViewJiraFixVersionsList: CardView
    private lateinit var recyclerViewJiraFixVersionsList: RecyclerView
    private lateinit var jiraAdapterFixVersionsList: RecyclerViewJiraFixVersionsAdapter
    private lateinit var imageViewJiraFixVersions: ImageView
    private val arrayListJiraFixVersionsName: ArrayList<RecyclerViewModelFixVersions> = ArrayList()
    private var arrayListJiraFixVersions: ArrayList<String> = ArrayList()
    //Feedback:
    private lateinit var floating_action_button_feedback: Button
    private lateinit var floating_action_button_feed_close: Button
    private lateinit var editText_feedback: EditText
    private lateinit var toolbarFeedback: Toolbar
    private lateinit var progressBarFeedback: ProgressBar
    private lateinit var progressBarFeedbackLayout: FrameLayout

    //Slack:
    private val slackAuthentication = SlackApi()
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
    private lateinit var slackAttachmentAdapter: RecyclerViewSlackAttachmentAdapter
    private lateinit var recyclerViewSlackAttachment: RecyclerView
    private lateinit var recyclerViewSlackAttachmentUser: RecyclerView
    private val arrayListSlackFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var slackChannelLayout: ScrollView
    private lateinit var slackUserLayout: ScrollView
    private lateinit var slackBottomNavigationView: BottomNavigationView
    private lateinit var toolbarSlack: Toolbar

    //Email:
    private lateinit var buttonEmailCreate: Button
    private lateinit var buttonEmailCancel: Button
    private lateinit var imageViewEmailAdd: ImageView
    private lateinit var imageViewEmailClear: ImageView
    private lateinit var cardViewEmailToList: CardView
    private lateinit var editTextEmailTo: EditText
    private lateinit var editTextEmailContent: EditText
    private lateinit var editTextEmailSubject: EditText
    private lateinit var toolbarEmail: Toolbar
    private lateinit var recyclerViewEmailAttachment: RecyclerView
    private lateinit var recyclerViewEmailToList: RecyclerView
    private lateinit var emailAttachmentAdapter: RecyclerViewEmailAttachmentAdapter
    private lateinit var emailToListAttachmentAdapter: RecyclerViewEmaiToListAttachmentAdapter
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
    private lateinit var frameLayoutFutureDate: FrameLayout
    private lateinit var calendarViewFutureTask: CalendarView
    private lateinit var buttonFutureTaskDateCreate: Button
    private lateinit var buttonFutureTaskDateCancel: Button
    //Future-Task-Time:
    private var futureStartTime: Long? = null
    private lateinit var frameLayoutFutureTime: FrameLayout
    private lateinit var timePickerFutureTask: TimePicker
    private lateinit var buttonFutureTaskTimeCreate: Button
    private lateinit var buttonFutureTaskTimeCancel: Button

    //Gitlab:
    private val gitlabAuthentication = GitlabApi()
    private lateinit var autoTextViewGitlabProject: AutoCompleteTextView
    private lateinit var editTextGitlabTitle: EditText
    private lateinit var editTextGitlabDescription: EditText
    private lateinit var autoTextViewGitlabProjectAdapter: AutoCompleteTextViewGitlabProjectAdapter
    private lateinit var autoTextViewGitlabAssigneeAdapter: AutoCompleteTextViewGitlabAssigneeAdapter
    private lateinit var autoTextViewGitlabLabelsAdapter: AutoCompleteTextViewGitlabLabelAdapter
    private lateinit var autoTextViewGitlabMilestoneAdapter: AutoCompleteTextViewGitlabMilestoneAdapter
    private lateinit var autoTextViewGitlabConfidentialityAdapter: AutoCompleteTextViewGitlabConfidentialityAdapter
    private lateinit var autoTextViewGitlabMilestone: AutoCompleteTextView
    private lateinit var autoTextViewGitlabAssignee: AutoCompleteTextView
    private lateinit var editTextGitlabWeight: EditText
    private lateinit var autoTextViewGitlabLabels: AutoCompleteTextView
    private lateinit var autoTextViewGitlabConfidentiality: AutoCompleteTextView
    private lateinit var textViewGitlabDueDate: TextView
    private lateinit var buttonGitlabCreate: Button
    internal lateinit var buttonGitlabCancel: Button
    private lateinit var toolbarGitlab: Toolbar
    private lateinit var calendarViewGitlabView: View
    private lateinit var calendarViewGitlabLayout: FrameLayout
    private lateinit var calendarViewGitlabDueDate: CalendarView
    private var calendarViewGitlabDate: Long? = null
    private lateinit var buttonCalendarViewGitlabCancel: Button
    private lateinit var buttonCalendarViewGitlabOk: Button
    private lateinit var recyclerViewGitlabAttachment: RecyclerView
    private lateinit var gitlabAttachmentAdapter: RecyclerViewGitlabAttachmentAdapter
    private val arrayListGitlabFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var textViewGitlabConfidentiality: TextView
    private lateinit var imageViewGitlabConfidentiality: ImageView
    private lateinit var textViewGitlabMilestone: TextView
    private lateinit var imageViewGitlabMilestone: ImageView
    private lateinit var textViewGitlabAssignee: TextView
    private lateinit var imageViewGitlabAssignee: ImageView
    private lateinit var imageViewGitlabDueDate: ImageView
    //Github
    internal val githubAuthentication = GithubApi()
    private lateinit var buttonGithubCreate: Button
    private lateinit var buttonGithubCancel: Button
    private lateinit var editTextGithubTitle: EditText
    private lateinit var editTextGithubComment: EditText
    private lateinit var toolbarGithub: Toolbar
    private lateinit var recyclerViewGithubAttachment: RecyclerView
    private lateinit var githubAttachmentAdapter: RecyclerViewGithubAttachmentAdapter
    private lateinit var autoTextViewGithubAssignee: AutoCompleteTextView
    private lateinit var autoTextViewGithubLabels: AutoCompleteTextView
    private lateinit var autoTextViewGithubRepo: AutoCompleteTextView
    private lateinit var autoTextViewGithubProject: AutoCompleteTextView
    private lateinit var autoTextViewGithubMileStone: AutoCompleteTextView
    private lateinit var autoTextViewGithubLinkedRequests: AutoCompleteTextView
    private lateinit var autoTextViewGithubAssigneeAdapter: AutoCompleteTextViewGithubAssigneeAdapter
    private lateinit var autoTextViewGithubLabelsAdapter: AutoCompleteTextViewGithubLabelAdapter
    private lateinit var autoTextViewGithubRepoAdapter: AutoCompleteTextViewGithubRepoAdapter
    private lateinit var autoTextViewGithubProjectAdapter: AutoCompleteTextViewGithubProjectAdapter
    private lateinit var autoTextViewGithubMileStoneAdapter: AutoCompleteTextViewGithubMilestoneAdapter
    private lateinit var autoTextViewGithubLinkedRequestsAdapter: AutoCompleteTextViewGithubLinkedRequestsAdapter
    private val arrayListGithubFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var scrollViewGithub: ScrollView
    private lateinit var recyclerViewGithubAssignee: RecyclerView
    private lateinit var githubAssigneeAdapter: RecyclerViewGithubAssigneeAdapter
    internal lateinit var cardViewGithubAssigneeList: CardView
    private val arrayListGithubAssigneeName: ArrayList<RecyclerViewModelAssignee> = ArrayList()
    private lateinit var imageViewGithubAssignee: ImageView
    private lateinit var arrayListGithubAssignee: ArrayList<String>
    private lateinit var recyclerViewGithubLabel: RecyclerView
    private lateinit var githubLabelAdapter: RecyclerViewGithubLabelAdapter
    internal lateinit var cardViewGithubLabelList: CardView
    private val arrayListGithubLabelName: ArrayList<RecyclerViewModelLabel> = ArrayList()
    private lateinit var imageViewGithubLabel: ImageView
    private lateinit var arrayListGithubLabel: ArrayList<String>
    private lateinit var recyclerViewGithubProject: RecyclerView
    private lateinit var githubProjectAdapter: RecyclerViewGithubProjectAdapter
    internal lateinit var cardViewGithubProjectList: CardView
    private val arrayListGithubProjectName: ArrayList<RecyclerViewModelProject> = ArrayList()
    private lateinit var imageViewGithubProject: ImageView
    private lateinit var arrayListGithubProject: ArrayList<String>
    //Trello
    internal val trelloAuthentication = TrelloApi()
    private lateinit var buttonTrelloCreate: Button
    private lateinit var buttonTrelloCancel: Button
    private lateinit var editTextTrelloTitle: EditText
    private lateinit var editTextTrelloDescription: EditText
    private lateinit var editTextTrelloCheckList: EditText
    private lateinit var toolbarTrello: Toolbar
    private lateinit var recyclerViewTrelloAttachment: RecyclerView
    private lateinit var trelloAttachmentAdapter: RecyclerViewTrelloAttachmentAdapter
    private lateinit var autoTextViewTrelloProject: AutoCompleteTextView
    private lateinit var autoTextViewTrelloBoard: AutoCompleteTextView
    private lateinit var autoTextViewTrelloMember: AutoCompleteTextView
    private lateinit var autoTextViewTrelloLabel: AutoCompleteTextView
    private lateinit var autoTextViewTrelloProjectAdapter: AutoCompleteTextViewTrelloProjectAdapter
    private lateinit var autoTextViewTrelloBoardAdapter: AutoCompleteTextViewTrelloBoardAdapter
    private lateinit var autoTextViewTrelloMemberAdapter: AutoCompleteTextViewTrelloMemberAdapter
    private lateinit var autoTextViewTrelloLabelAdapter: AutoCompleteTextViewTrelloLabelAdapter
    private lateinit var recyclerViewTrelloLabel: RecyclerView
    private lateinit var trelloLabelAdapter: RecyclerViewTrelloLabelAdapter
    private val arrayListTrelloFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var scrollViewTrello: ScrollView
    internal lateinit var cardViewTrelloLabelList: CardView
    private val arrayListTrelloLabelName: ArrayList<RecyclerViewModelLabel> = ArrayList()
    private lateinit var imageViewTrelloLabel: ImageView
    private lateinit var arrayListTrelloLabel: ArrayList<String>
    internal lateinit var cardViewTrelloMemberList: CardView
    private lateinit var recyclerViewTrelloMember: RecyclerView
    private lateinit var trelloMemberAdapter: RecyclerViewTrelloMemberAdapter
    private var arrayListTrelloMemberName: ArrayList<RecyclerViewModelMember> = ArrayList()
    private lateinit var imageViewTrelloMember: ImageView
    private lateinit var arrayListTrelloMember: ArrayList<String>
    internal lateinit var cardViewTrelloCheckList: CardView
    private lateinit var recyclerViewTrelloCheckList: RecyclerView
    private lateinit var trelloCheckListAdapter: RecyclerViewTrelloCheckListAdapter
    private var arrayListTrelloCheckListName: ArrayList<RecyclerViewModelCheckList> = ArrayList()
    private lateinit var imageViewTrelloCheckList: ImageView
    private lateinit var arrayListTrelloCheckList: ArrayList<String>
    //trello_timeline:
    private lateinit var imageViewTrelloCalendar: ImageView
    private lateinit var imageButtonTrelloRemoveTimeline: ImageButton

    private lateinit var imageViewTrelloDate: ImageView
    private lateinit var imageButtonTrelloDateRemove: ImageButton
    private lateinit var imageViewTrelloTime: ImageView
    private lateinit var imageButtonTrelloTimeRemove: ImageButton
    private lateinit var buttonTrelloTimelineProceed: Button
    private lateinit var buttonTrelloTimelineCancel: Button
    private var calendarTrello: Calendar? = null
    //trello_date:
    private var trelloStartDate: Long? = null
    private lateinit var frameLayoutTrelloDate: FrameLayout
    private lateinit var calendarViewTrello: CalendarView
    private lateinit var buttonTrelloDateCreate: Button
    private lateinit var buttonTrelloDateCancel: Button

    //trello_time:
    private var trelloStartTime: Long? = null
    private lateinit var frameLayoutTrelloTime: FrameLayout
    private lateinit var timePickerTrello: TimePicker
    private lateinit var buttonTrelloTimeCreate: Button
    private lateinit var buttonTrelloTimeCancel: Button

    //Pivotal Tracker
    internal val pivotalAuthentication = PivotalTrackerApi()
    private lateinit var buttonPivotalCreate: Button
    private lateinit var buttonPivotalCancel: Button
    private lateinit var toolbarPivotal: Toolbar
    private lateinit var scrollViewPivotal: ScrollView
    private lateinit var autoTextViewPivotalProject: AutoCompleteTextView
    private lateinit var editTextPivotalTitle: EditText
    private lateinit var editTextPivotalBlockers: EditText
    private lateinit var editTextPivotalDescription: EditText
    private lateinit var editTextPivotalTasks: EditText
    private lateinit var autoTextViewPivotalStoryType: AutoCompleteTextView
    private lateinit var autoTextViewPivotalPoints: AutoCompleteTextView
    private lateinit var autoTextViewPivotalOwners: AutoCompleteTextView
    private lateinit var autoTextViewPivotalLabel: AutoCompleteTextView
    private lateinit var autoTextViewPivotalRequester: AutoCompleteTextView
    private lateinit var autoTextViewPivotalProjectAdapter: AutoCompleteTextViewPivotalProjectAdapter
    private lateinit var autoTextViewPivotalLabelAdapter: AutoCompleteTextViewPivotalLabelAdapter
    private lateinit var autoTextViewPivotalStoryTypeAdapter: AutoCompleteTextViewPivotalStoryTypeAdapter
    private lateinit var autoTextViewPivotalPointsAdapter: AutoCompleteTextViewPivotalPointsAdapter
    private lateinit var autoTextViewPivotalOwnersAdapter: AutoCompleteTextViewPivotalOwnersAdapter
    private lateinit var autoTextViewPivotalRequesterAdapter: AutoCompleteTextViewPivotalRequesterAdapter
    private lateinit var imageViewPivotalOwners: ImageView
    internal lateinit var cardViewPivotalOwnersList: CardView
    private lateinit var recyclerViewPivotalOwnerList: RecyclerView
    private lateinit var imageViewPivotalBlockers: ImageView
    internal lateinit var cardViewPivotalBlockersList: CardView
    private lateinit var recyclerViewPivotalBlockersList: RecyclerView
    private lateinit var imageViewPivotalLabel: ImageView
    internal lateinit var cardViewPivotalLabelList: CardView
    private lateinit var recyclerViewPivotalLabelList: RecyclerView
    private lateinit var imageViewPivotalTask: ImageView
    internal lateinit var cardViewPivotalTasksList: CardView
    private lateinit var recyclerViewPivotalTaskList: RecyclerView
    internal lateinit var cardViewPivotalAttachments: CardView
    private lateinit var recyclerViewPivotalAttachmentList: RecyclerView
    private val arrayListPivotalFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var pivotalAttachmentAdapter: RecyclerViewPivotalAttachmentAdapter
    private val arrayListPivotalTaskName: ArrayList<RecyclerViewModelTask> = ArrayList()
    private lateinit var pivotalTaskAdapter: RecyclerViewPivotalTaskAdapter
    private val arrayListPivotalBlockerName: ArrayList<RecyclerViewModelBlocker> = ArrayList()
    private lateinit var pivotalBlockerAdapter: RecyclerViewPivotalBlockerAdapter
    private val arrayListPivotalLabelName: ArrayList<RecyclerViewModelLabel> = ArrayList()
    private lateinit var pivotalLabelAdapter: RecyclerViewPivotalLabelAdapter
    private lateinit var arrayListPivotalLabel: ArrayList<String>
    private val arrayListPivotalOwnerName: ArrayList<RecyclerViewModelOwner> = ArrayList()
    private lateinit var pivotalOwnerAdapter: RecyclerViewPivotalOwnerAdapter
    private lateinit var arrayListPivotalOwner: ArrayList<String>

    //Basecamp
    internal val basecampAuthentication = BasecampApi()
    private lateinit var buttonBasecampCancel: Button
    private lateinit var buttonBasecampCreate: Button
    private lateinit var toolbarBasecamp: Toolbar
    private lateinit var scrollViewBasecamp: ScrollView
    private lateinit var autoTextViewBasecampProject: AutoCompleteTextView
    private lateinit var autoTextViewBasecampCategory: AutoCompleteTextView
    private lateinit var autoTextViewBasecampAssignee: AutoCompleteTextView
    private lateinit var autoTextViewBasecampNotify: AutoCompleteTextView
    private lateinit var autoTextViewBasecampProjectAdapter: AutoCompleteTextViewBasecampProjectAdapter
    private lateinit var autoTextViewBasecampCategoryAdapter: AutoCompleteTextViewBasecampCategoryAdapter
    private lateinit var autoTextViewBasecampAssigneeAdapter: AutoCompleteTextViewBasecampAssigneeAdapter
    private lateinit var autoTextViewBasecampNotifyAdapter: AutoCompleteTextViewBasecampNotifyAdapter
    private lateinit var editTextBasecampDescriptionMessage: EditText
    private lateinit var editTextBasecampDescriptionTodo: EditText
    private lateinit var editTextBasecampTitle: EditText
    private lateinit var editTextBasecampName: EditText
    private lateinit var editTextBasecampContent: EditText
    private lateinit var imageViewBasecampAssignee: ImageView
    private lateinit var imageViewBasecampNotify: ImageView
    internal lateinit var cardViewBasecampAssigneeList: CardView
    internal lateinit var cardViewBasecampNotifyList: CardView
    private lateinit var recyclerViewBasecampAttachmentList: RecyclerView
    private lateinit var recyclerViewBasecampNotifyList: RecyclerView
    private lateinit var recyclerViewBasecampAssigneeList: RecyclerView
    private lateinit var basecampAttachmentAdapter: RecyclerViewBasecampAttachmentAdapter
    private val arrayListBasecampFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var basecampAssigneeAdapter: RecyclerViewBasecampAssigneeAdapter
    private val arrayListBasecampAssigneeName: ArrayList<RecyclerViewModelAssignee> = ArrayList()
    private lateinit var arrayListBasecampAssignee: ArrayList<String>
    private lateinit var basecampNotifyAdapter: RecyclerViewBasecampNotifyAdapter
    private val arrayListBasecampNotifyName: ArrayList<RecyclerViewModelNotify> = ArrayList()
    private lateinit var arrayListBasecampNotify: ArrayList<String>
    private lateinit var imageViewBasecampDate: ImageView
    private lateinit var imageButtonBasecampRemoveDate: ImageButton

    //basecamp_date:
    private lateinit var frameLayoutBasecampDate: FrameLayout
    private lateinit var calendarViewBasecamp: CalendarView
    private lateinit var buttonBasecampDateCreate: Button
    private lateinit var buttonBasecampDateCancel: Button

    //Asana
    internal val asanaAuthentication = AsanaApi()
    private lateinit var toolbarAsana: Toolbar
    private lateinit var scrollViewAsana: ScrollView
    private lateinit var autoTextViewAsanaProject: AutoCompleteTextView
    private lateinit var autoTextViewAsanaProjectAdapter: AutoCompleteTextViewAsanaProjectAdapter
    private lateinit var autoTextViewAsanaAssignee: AutoCompleteTextView
    private lateinit var autoTextViewAsanaAssigneeAdapter: AutoCompleteTextViewAsanaAssigneeAdapter
    private lateinit var autoTextViewAsanaSector: AutoCompleteTextView
    private lateinit var autoTextViewAsanaSectorAdapter: AutoCompleteTextViewAsanaSectorAdapter
    private lateinit var autoTextViewAsanaPriority: AutoCompleteTextView
    private lateinit var autoTextViewAsanaPriorityAdapter: AutoCompleteTextViewAsanaPriorityAdapter
    private lateinit var editTextAsanaDescription: EditText
    private lateinit var editTextAsanaSubTask: EditText
    private lateinit var editTextAsanaTaskName: EditText
    private lateinit var imageViewAsanaTaskAdd: ImageView
    private lateinit var recyclerViewAsanaAttachmentList: RecyclerView
    private val arrayListAsanaFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var recyclerViewAsanaSubTasksList: RecyclerView
    private val arrayListAsanaSubtaskName: ArrayList<RecyclerViewModelSubtask> = ArrayList()
    internal lateinit var cardViewAsanaSubTasksList: CardView
    private lateinit var asanaAttachmentAdapter: RecyclerViewAsanaAttachmentAdapter
    private lateinit var asanaSubTasksAdapter: RecyclerViewAsanaSubTaskAdapter
    private lateinit var imageViewAsanaStartDate: ImageView
    private lateinit var imageButtonAsanaRemoveDate: ImageButton
    private lateinit var buttonAsanaCancel: Button
    private lateinit var buttonAsanaCreate: Button
    private var arrayListAsanaSubtaskAssignee: ArrayList<String> = ArrayList()
    private var arrayListAsanaSubtaskSection: ArrayList<String> = ArrayList()

    //asana_date:
    private lateinit var frameLayoutAsanaDate: FrameLayout
    private lateinit var calendarViewAsana: CalendarView
    private lateinit var buttonAsanaDateCreate: Button
    private lateinit var buttonAsanaDateCancel: Button

    //Clubhouse
    internal val clubhouseAuthentication = ClubhouseApi()
    internal lateinit var buttonClubhouseCancel: Button
    private lateinit var buttonClubhouseCreate: Button
    private lateinit var toolbarClubhouse: Toolbar
    private lateinit var calendarViewClubhouseView: View
    private lateinit var calendarViewClubhouseLayout: FrameLayout
    private lateinit var calendarViewClubhouseDueDate: CalendarView
    private var calendarViewClubhouseDate: Long? = null
    private lateinit var buttonCalendarViewClubhouseCancel: Button
    private lateinit var buttonCalendarViewClubhouseOk: Button
    private lateinit var textViewClubhouseDueDate: TextView
    private lateinit var autoTextViewClubhouseProject: AutoCompleteTextView
    private lateinit var autoTextViewClubhouseProjectAdapter: AutoCompleteTextViewClubhouseProjectAdapter
    private lateinit var autoTextViewClubhouseEpic: AutoCompleteTextView
    private lateinit var autoTextViewClubhouseEpicAdapter: AutoCompleteTextViewClubhouseEpicAdapter
    private lateinit var editTextClubhouseStoryName: EditText
    private lateinit var editTextClubhouseStoryDescription: EditText
    private lateinit var editTextClubhouseEstimate: EditText
    private lateinit var autoTextViewClubhouseStoryType: AutoCompleteTextView
    private lateinit var autoTextViewClubhouseStoryTypeAdapter:  AutoCompleteTextViewClubhouseStoryTypeAdapter
    private lateinit var autoTextViewClubhouseRequester: AutoCompleteTextView
    private lateinit var autoTextViewClubhouseRequesterAdapter:   AutoCompleteTextViewClubhouseRequesterAdapter
    private val arrayListClubhouseFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var clubhouseAttachmentAdapter: RecyclerViewClubhouseAttachmentAdapter
    private lateinit var recyclerViewClubhouseAttachment: RecyclerView
    private lateinit var textViewClubhouseEpic: TextView
    private lateinit var linearLayoutClubhouseEpic: LinearLayout
    private lateinit var imageViewClubhouseDueDate: ImageView

    //LoggerBird Activate Popup:
    private lateinit var textViewLoggerBirdActivatePopupActivate: TextView
    private lateinit var textViewLoggerBirdActivatePopupDismiss: TextView

    //LoggerBird Start Popup:
    private lateinit var textViewLoggerBirdStartPopupSessionTime: TextView

    //LoggerBird Dismiss Popup:
    private lateinit var textViewLoggerBirdDismissPopupFeedBack: TextView

    //LoggerBird File Action Popup:
    private lateinit var textViewLoggerBirdFileActionPopupDiscard: TextView
    private lateinit var textViewLoggerBirdFileActionPopupEmail: TextView
    //LoggerBird File Action List Popup:
    private lateinit var imageViewLoggerBirdFileActionListPopupBack: ImageView
    private lateinit var recyclerViewLoggerBirdFileActionListPopup: RecyclerView
    private lateinit var loggerBirdFileActionListAdapter: RecyclerViewFileActionAttachmentAdapter
    private var arrayListLoggerBirdFileActionList: ArrayList<RecyclerViewModel> = ArrayList()

    //LoggerBird Unhandled Exception Popup:
    private lateinit var textViewLoggerBirdUnhandledExceptionPopupDiscard: TextView
    private lateinit var textViewLoggerBirdUnhandledExceptionPopupShare: TextView
    private lateinit var checkBoxLoggerBirdUnhandledExceptionPopupDuplication: CheckBox

    //Bitbucket:
    internal val bitbucketAuthentication = BitbucketApi()
    private lateinit var buttonBitbucketCancel: Button
    private lateinit var buttonBitbucketCreate: Button
    private lateinit var toolbarBitbucket: Toolbar
    private lateinit var scrollViewBitbucket: ScrollView
    private lateinit var autoTextViewBitbucketProject: AutoCompleteTextView
    private lateinit var autoTextViewBitbucketProjectAdapter: AutoCompleteTextViewBitbucketProjectAdapter
    private lateinit var autoTextviewBitbucketKind: AutoCompleteTextView
    private lateinit var autoTextViewBitbucketKindAdapter: AutoCompleteTextViewBitbucketKindAdapter
    private lateinit var autoTextViewBitbucketAssignee: AutoCompleteTextView
    private lateinit var autoTextViewBitbucketAssigneeAdapter: AutoCompleteTextViewBitbucketAssigneeAdapter
    private lateinit var autoTextViewBitbucketPriority: AutoCompleteTextView
    private lateinit var autoTextViewBitbucketPriorityAdapter: AutoCompleteTextViewBitbucketPriorityAdapter
    private lateinit var editTextBitbucketTitle: EditText
    private lateinit var editTextBitbucketDescription: EditText
    private lateinit var recyclerViewBitbucketAttachmentList: RecyclerView
    private lateinit var bitbucketAttachmentAdapter: RecyclerViewBitbucketAttachmentAdapter
    private val arrayListBitbucketFileName: ArrayList<RecyclerViewModel> = ArrayList()

    //Static global variables:
    internal companion object {
        internal lateinit var floatingActionButtonView: View
        internal lateinit var floating_action_button: FloatingActionButton
        internal lateinit var filePathMedia: File
        private lateinit var floating_action_button_screenshot: FloatingActionButton
        private lateinit var floating_action_button_video: FloatingActionButton
        private lateinit var floating_action_button_audio: FloatingActionButton
        private lateinit var revealLinearLayoutShare: CircularRevealLinearLayout
        private lateinit var textView_send_email: TextView
        private lateinit var textView_share_jira: TextView
        private lateinit var textView_share_slack: TextView
        private lateinit var textView_share_gitlab: TextView
        private lateinit var textView_share_github: TextView
        private lateinit var textView_share_trello: TextView
        private lateinit var textView_share_pivotal: TextView
        private lateinit var textView_share_basecamp: TextView
        private lateinit var textView_share_asana: TextView
        private lateinit var textView_share_clubhouse: TextView
        private lateinit var textView_share_bitbucket: TextView
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
        private var DISPLAY_WIDTH = 720
        private var DISPLAY_HEIGHT = 1280
        private val ORIENTATIONS = SparseIntArray()
        internal var controlPermissionRequest: Boolean = false
        private var runnableList: ArrayList<Runnable> = ArrayList()
        private var runnableListEmail: ArrayList<Runnable> = ArrayList()
        private var workQueueLinkedVideo: LinkedBlockingQueueUtil =
            LinkedBlockingQueueUtil()
        private var workQueueLinkedEmail: LinkedBlockingQueueUtil =
            LinkedBlockingQueueUtil()
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
        internal lateinit var imageViewProgressBarClose: ImageView
        /**
         * This method is used for removing video task from queue.
         */
        internal fun callEnqueueVideo() {
            workQueueLinkedVideo.controlRunnable = false
            if (runnableList.size > 0) {
                runnableList.removeAt(0)
                if (runnableList.size > 0) {
                    workQueueLinkedVideo.put(runnableList[0])
                }
            }
        }

        /**
         * This method is used for removing email task from queue.
         */
        internal fun callEnqueueEmail() {
            workQueueLinkedEmail.controlRunnable = false
            if (runnableListEmail.size > 0) {
                runnableListEmail.removeAt(0)
                if (runnableListEmail.size > 0) {
                    workQueueLinkedEmail.put(runnableListEmail[0])
                } else {
                    loggerBirdService.finishShareLayout("single_email")
                }
            } else {
                loggerBirdService.finishShareLayout("single_email")
            }
        }

        /**
         * This method is used for reseting email queue.
         */
        internal fun resetEnqueueEmail() {
            runnableListEmail.clear()
            workQueueLinkedEmail.controlRunnable = false
        }

        /**
         * This method is used for reseting video queue.
         */
        internal fun resetEnqueueVideo() {
            runnableList.clear()
            workQueueLinkedVideo.controlRunnable = false
        }

        /**
         * This method is used for checking intentForeGroundServiceVideo variable is initialized.
         * @return Boolean value.
         */
        internal fun controlIntentForegroundServiceVideo(): Boolean {
            if (this::intentForegroundServiceVideo.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for checking loggerBirdService variable is initialized.
         * @return Boolean value.
         */
        internal fun controlLoggerBirdServiceInit(): Boolean {
            if (Companion::loggerBirdService.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for checking floatingActionButtonView variable is initialized.
         * @return Boolean value.
         */
        internal fun controlFloatingActionButtonView(): Boolean {
            if (this::floatingActionButtonView.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for checking revealLinearLayoutShare variable is initialized.
         * @return Boolean value.
         */
        internal fun controlRevealShareLayout(): Boolean {
            if (this::revealLinearLayoutShare.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for checking workingAnimation variable is initialized.
         * @return Boolean value.
         */
        internal fun controlWorkingAnimation(): Boolean {
            if (this::workingAnimation.isInitialized) {
                return true
            }
            return false
        }

        /**
         * This method is used for calling share view.
         * @param filePathMedia is used for getting the reference of current media file.
         */
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        internal fun callShareView(filePathMedia: File? = null) {
            if (filePathMedia != null) {
                loggerBirdService.activity.runOnUiThread {
                    loggerBirdService.shareView(filePathMedia = filePathMedia)
                }
            } else {
                loggerBirdService.activity.runOnUiThread {
                    revealLinearLayoutShare.visibility = View.GONE
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

    //Constructor
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
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
     * @param rootIntent used for getting context reference from the Activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of logExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     * @return IBinder value.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
//            if (!audioRecording && !videoRecording && !screenshotDrawing) {
//                arrayListFile.forEach {
//                    if (it.exists()) {
//                        it.delete()
//                    }
//                }
//            }
//            else {
//                addFileList()
//            }
            arrayListFile.forEach {
                if (it.exists()) {
                    it.delete()
                }
            }
            if(this::activity.isInitialized){
                dailySessionTimeRecorder(activity = activity)
            }
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

    /**
     * This method is used for getting reference of current activity and context.
     * @param activity is used for getting reference of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun initializeActivity(activity: Activity) {
        this.activity = activity
        this.context = activity
    }


    /**
     * This method is used for creating Main Loggerbird layout which is attached to application overlay.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun initializeFloatingActionButton(activity: Activity) {
        try {
            if (windowManager != null && this::view.isInitialized) {
                if (revealLinearLayoutShare.visibility == View.VISIBLE) {
                    controlMediaFile()
                }
                (windowManager as WindowManager).removeViewImmediate(view)
                if (!checkUnhandledFilePath() && !this.controlFileAction) {
                    initializeLoggerBirdClosePopup(activity = activity)
                }
                windowManager = null
                isFabEnable = false

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(activity)) {
                        initializeFloatingActionButtonView()
                    } else {
                        checkDrawOtherAppPermission(activity = (context as Activity))
                    }
                } else {
                    initializeFloatingActionButtonView()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.floatingActionButtonTag)
        }
    }

    /**
     * This method is used for initializing view of the main loggerbird layout.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun initializeFloatingActionButtonView() {
        try {
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
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
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
            revealLinearLayoutShare = view.findViewById(R.id.reveal_linear_layout_share)
            textView_send_email = view.findViewById(R.id.textView_send_email)
            textView_discard = view.findViewById(R.id.textView_discard)
            if (this.controlFileAction) {
                textView_discard.visibility = View.GONE
            } else {
                textView_discard.visibility = View.VISIBLE
            }
            textView_share_jira = view.findViewById(R.id.textView_share_jira)
            textView_share_slack = view.findViewById(R.id.textView_share_slack)
            textView_share_github = view.findViewById(R.id.textView_share_github)
            textView_share_trello = view.findViewById(R.id.textView_share_trello)
            textView_share_pivotal = view.findViewById(R.id.textView_share_pivotal)
            textView_share_basecamp = view.findViewById(R.id.textView_share_basecamp)
            textView_share_asana = view.findViewById(R.id.textView_share_asana)
            textView_share_clubhouse = view.findViewById(R.id.textView_share_clubhouse)
            textView_share_bitbucket = view.findViewById(R.id.textView_share_bitbucket)
            textView_counter_video = view.findViewById(R.id.fragment_textView_counter_video)
            textView_counter_audio = view.findViewById(R.id.fragment_textView_counter_audio)
            textView_video_size = view.findViewById(R.id.fragment_textView_size_video)
            textView_audio_size = view.findViewById(R.id.fragment_textView_size_audio)
            checkBoxFutureTask = view.findViewById(R.id.checkBox_future_task)
            textView_share_gitlab = view.findViewById(R.id.textView_share_gitlab)
            floating_action_button.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
            floating_action_button.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
            if (audioRecording || videoRecording || screenshotDrawing) {
                workingAnimation =
                    AnimationUtils.loadAnimation(context, R.anim.pulse_in_out)
                if (controlWorkingAnimation()) {
                    floating_action_button.startAnimation(workingAnimation)
                    floating_action_button.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                this,
                                R.color.mediaRecordColor
                            )
                        )
                    floating_action_button.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
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
            if (videoRecording) {
                floating_action_button_video.visibility = View.GONE
            }
            if (audioRecording) {
                floating_action_button_audio.visibility = View.GONE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                buttonClicks()
            }
            if (!checkUnhandledFilePath() && !this.controlFileAction) {
                initializeLoggerBirdStartPopup(activity = activity)
            }
            isFabEnable = true
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.floatingActionButtonTag)
        }
    }

    /**
     * This method formats a long time value into day format.
     */
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

    /**
     * This method formats a long time value into hour format.
     */
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

    /**
     * This method is used for getting reference of current activity and context.
     * @param activity is used for getting reference of current activity.
     */
    internal fun initializeNewActivity(activity: Activity) {
        this.activity = activity
        this.context = activity
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the fragment_logger_bird.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("ClickableViewAccessibility")
    private fun buttonClicks() {
        floating_action_button.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    checkDrawOtherAppPermission(activity = (context as Activity))
                } else {
                    animationVisibility()
                }
            } else {
                animationVisibility()
            }
        }
        floating_action_button_screenshot.setSafeOnClickListener {
            if (floating_action_button_screenshot.visibility == View.VISIBLE) {
                if (!PaintActivity.controlPaintInPictureState && !screenshotDrawing) {
                    if (!audioRecording && !videoRecording) {
                        resetFileReferences()
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
                    resetFileReferences()
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
                if (filePathAudio != null) {
                    shareView(filePathMedia = filePathAudio!!)
                }
            }
        }

        floating_action_button_video.setSafeOnClickListener {
            if (floating_action_button_video.visibility == View.VISIBLE) {
                if (!audioRecording && !screenshotDrawing) {
                    resetFileReferences()
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
                if (filePathVideo != null) {
                    shareView(filePathMedia = filePathVideo!!)
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
                textViewCounterVideo = textView_counter_video,
                textViewCounterAudio = textView_counter_audio,
                textViewVideoSize = textView_video_size,
                textViewAudioSize = textView_audio_size,
                revealLinearLayoutShare = revealLinearLayoutShare
            )
        )
    }

    private fun resetFileReferences() {
        filePathAudio = null
        filePathVideo = null
        PaintView.filePathScreenShot = null
    }

    /**
     * This method is used for controlling state of buttons that are inside in the fragment_logger_bird.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun shareView(filePathMedia: File) {
        floating_action_button.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))
        floating_action_button.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
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
        revealLinearLayoutShare.visibility = View.VISIBLE
        textView_share_jira.visibility = View.GONE
        textView_share_clubhouse.visibility = View.GONE
        textView_share_asana.visibility = View.GONE
        textView_share_basecamp.visibility = View.GONE
        textView_share_github.visibility = View.GONE
        textView_share_gitlab.visibility = View.GONE
        textView_share_pivotal.visibility = View.GONE
        textView_share_slack.visibility = View.GONE
        textView_share_trello.visibility = View.GONE
        textView_share_bitbucket.visibility = View.GONE
        if (LoggerBird.clubhouseIsInitialized()) {
            textView_share_clubhouse.visibility = View.VISIBLE
        }

        if (LoggerBird.asanaIsInitialized()) {
            textView_share_asana.visibility = View.VISIBLE
        }

        if (LoggerBird.basecampIsInitialized()) {
            textView_share_basecamp.visibility = View.VISIBLE
        }

        if (LoggerBird.githubIsInitialized()) {
            textView_share_github.visibility = View.VISIBLE
        }

        if (LoggerBird.gitlabIsInitialized()) {
            textView_share_gitlab.visibility = View.VISIBLE
        }

        if (LoggerBird.pivotalIsInitialized()) {
            textView_share_pivotal.visibility = View.VISIBLE
        }

        if (LoggerBird.slackIsInitialized()) {
            textView_share_slack.visibility = View.VISIBLE
        }

        if (LoggerBird.jiraIsInitialized()) {
            textView_share_jira.visibility = View.VISIBLE
        }

        if (LoggerBird.trelloIsInitialized()) {
            textView_share_trello.visibility = View.VISIBLE
        }
        if (LoggerBird.bitbucketIsInitialized()) {
            textView_share_bitbucket.visibility = View.VISIBLE
        }
        shareViewClicks(filePathMedia = filePathMedia)
    }

    /**
     * This method is used for initializing shareview buttons clicks which are inside in the fragment_logger_bird.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun shareViewClicks(filePathMedia: File) {
        if (revealLinearLayoutShare.isVisible) {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            val coroutineScopeShareView = CoroutineScope(Dispatchers.IO)
            textView_send_email.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "email"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializeEmailLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }

            textView_share_jira.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "jira"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializeJiraLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }

            textView_share_slack.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                    coroutineScopeShareView.async {
                        if (!checkDuplicationField(
                                sharedPref = sharedPref,
                                filePath = filePathMedia,
                                field = "slack"
                            )
                        ) {
                            activity.runOnUiThread {
                                initializeSlackLayout(filePathMedia = filePathMedia)
                            }
                        }
                    }
                }
            }

            textView_share_github.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "github"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializeGithubLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }

            textView_share_gitlab.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "gitlab"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializeGitlabLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }

            textView_share_trello.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "trello"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializeTrelloLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }

            textView_share_pivotal.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "pivotal"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializePivotalLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }
            textView_share_basecamp.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "basecamp"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializeBasecampLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }
            textView_share_asana.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "asana"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializeAsanaLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }
            textView_share_clubhouse.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "clubhouse"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializeClubhouseLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }
            textView_share_bitbucket.setSafeOnClickListener {
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.GONE
                }
                coroutineScopeShareView.async {
                    if (!checkDuplicationField(
                            sharedPref = sharedPref,
                            filePath = filePathMedia,
                            field = "bitbucket"
                        )
                    ) {
                        activity.runOnUiThread {
                            initializeBitbucketLayout(filePathMedia = filePathMedia)
                        }
                    }
                }
            }

            textView_discard.setSafeOnClickListener {
                discardMediaFile()
            }

//            if (sharedPref.getBoolean("future_task_check", false)) {
//                checkBoxFutureTask.isChecked = true
//            }
//            checkBoxFutureTask.setOnCheckedChangeListener { buttonView, isChecked ->
//                if (isChecked) {
//                    with(sharedPref.edit()) {
//                        putBoolean("future_task_check", true)
//                        commit()
//                    }
//                } else {
//                    stopService(Intent(context, LoggerBirdFutureTaskService::class.java))
//                }
//            }
//            checkBoxFutureTask.setOnClickListener {
//                if (checkBoxFutureTask.isChecked) {
//                    defaultToast.attachToast(
//                        activity = activity,
//                        toastMessage = activity.resources.getString(R.string.future_task_enabled)
//                    )
//                    initializeFutureTaskLayout(filePathMedia = filePathMedia)
//                } else {
//                    with(sharedPref.edit()) {
//                        remove("future_task_time")
//                        remove("future_file_path")
//                        commit()
//                    }
//                    defaultToast.attachToast(
//                        activity = activity,
//                        toastMessage = activity.resources.getString(R.string.future_task_disabled)
//                    )
//                }
//            }
        }
    }

    private fun checkDuplicationField(
        sharedPref: SharedPreferences,
        filePath: File,
        field: String
    ): Boolean {
        if (sharedPref.getBoolean("duplication_enabled", false)) {
            val unhandledDuplicationDb =
                UnhandledDuplicationDb.getUnhandledDuplicationDb(LoggerBird.context.applicationContext)
            val unhandledDuplicationDao = unhandledDuplicationDb?.unhandledDuplicationDao()
            if (unhandledDuplicationDao != null) {
                if (unhandledDuplicationDao.getUnhandledDuplicationCount() >= 1000) {
                    unhandledDuplicationDao.deleteUnhandledDuplication()
                }
            }
            unhandledDuplicationDao?.getUnhandledDuplication()?.forEach {
                if (sharedPref.getString(
                        "unhandled_stack_class", null
                    ) == it.className
                    && sharedPref.getString(
                        "unhandled_stack_method",
                        null
                    ) == it.methodName
                    && sharedPref.getString(
                        "unhandled_stack_line",
                        null
                    ) == it.lineName
                    && field == it.fieldName
                    && sharedPref.getString(
                        "unhandled_stack_exception", null
                    ) == it.exceptionName
                ) {
                    activity.runOnUiThread {
                        attachUnhandledDuplicationLayout(filePath = filePath, field = field)
                    }
                    return true
                }
            }
        }
        return false
    }

    /**
     * This method is used for controlling animation of fragment_logger_bird layout.
     */
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

    /**
     * This method is used for checking taking write external storage recording permission(if permission doesn't given than it will request the permission).
     * @return Boolean value.
     */
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

    /**
     * This method is used for checking taking audio recording permission(if permission doesn't given than it will request the permission).
     * @return Boolean value.
     */
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

    /**
     * This method is used for requesting drawing overlay permission.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkDrawOtherAppPermission(activity: Activity) {
        controlPermissionRequest = true
        controlDrawableSettingsPermission = true
        sd.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.packageName)
            )
            activity.startActivityForResult(intent, REQUEST_CODE_DRAW_OTHER_APP_SETTINGS)
        }
    }

    /**
     * This method is used for taking screenshot of current view in the window.
     */
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

    /**
     * This method is used for starting or stopping screenshot action.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun takeScreenShot(view: View, context: Context) {
        if (checkWriteExternalStoragePermission()) {
            PaintActivity.closeActivitySession()
            coroutineCallScreenShot.async {
                try {
                    arrayListFileName.clear()
                    if(getFileList() != null){
                        arrayListFileName.addAll(getFileList()!!)
                    }
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
                            floating_action_button.setImageResource(R.drawable.ic_photo_camera_black_24dp)
                            floating_action_button.backgroundTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        this@LoggerBirdService,
                                        R.color.mediaRecordColor
                                    )
                                )
                            floating_action_button.imageTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        this@LoggerBirdService,
                                        R.color.white
                                    )
                                )
                            context.startActivity(screenshotIntent)

                        }

                    } else {
                        activity.runOnUiThread {
                            Toast.makeText(context, R.string.session_file_limit, Toast.LENGTH_SHORT).show()
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

    /**
     * This method is used for starting or stopping audio recording.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun takeAudioRecording() {
        if (checkAudioPermission() && checkWriteExternalStoragePermission()) {
            coroutineCallAudio.async {
                try {
                    if (!audioRecording) {
                        arrayListFileName.clear()
                        if(getFileList() != null){
                            arrayListFileName.addAll(getFileList()!!)
                        }
                        if (arrayListFileName.size <= 10) {
                            val fileDirectory: File = context.filesDir
                            filePathAudio = File(
                                fileDirectory,
                                "logger_bird_audio" + System.currentTimeMillis().toString() + "recording.3gpp"
                            )
                            addFileNameList(fileName = filePathAudio!!.absolutePath)
                            arrayListFile.add(filePathAudio!!)
                            addFileListAsync()
                            mediaRecorderAudio = MediaRecorder()
                            mediaRecorderAudio?.setAudioSource(MediaRecorder.AudioSource.MIC)
                            mediaRecorderAudio?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                            mediaRecorderAudio?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mediaRecorderAudio?.setOutputFile(filePathAudio!!)
                            } else {
                                mediaRecorderAudio?.setOutputFile(filePathAudio!!.path)
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

                                    ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                            this@LoggerBirdService,
                                            R.color.mediaRecordColor
                                        )
                                    )
                                floating_action_button.imageTintList =
                                    ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                            this@LoggerBirdService,
                                            R.color.white
                                        )
                                    )
                            }
                        } else {
                            activity.runOnUiThread {
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

    /**
     * This method is used for starting audio recording.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

    /**
     * This method is used for stopping audio recording.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

    /**
     * This method is used for starting the foreground service of video recording.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun takeForegroundService() {
        workQueueLinkedVideo.controlRunnable = true
        intentForegroundServiceVideo =
            Intent((context as Activity), LoggerBirdForegroundServiceVideo::class.java)
        startForegroundServiceVideo()
    }

    /**
     * This method is used for starting or stopping video recording.
     * @param requestCode is used for getting reference of request code which is comes from onActivityResult method on activity.
     * @param resultCode is used for getting reference of result code which is comes from onActivityResult method on activity.
     * @param data is used for getting reference of intent data which is comes from onActivityResult method on activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun takeVideoRecording(requestCode: Int, resultCode: Int, data: Intent?) {
        workQueueLinkedVideo.controlRunnable = true
        if (checkWriteExternalStoragePermission() && checkAudioPermission()) {
            coroutineCallVideo.async {
                try {
                    if (!videoRecording) {
                        arrayListFileName.clear()
                        if(getFileList() != null){
                            arrayListFileName.addAll(getFileList()!!)
                        }
                        if (arrayListFileName.size <= 10) {
                            this@LoggerBirdService.requestCode = requestCode
                            this@LoggerBirdService.resultCode = resultCode
                            this@LoggerBirdService.dataIntent = data
                            startScreenRecording()
                        } else {
                            activity.runOnUiThread {
                                Toast.makeText(
                                    context,
                                    R.string.session_file_limit,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        if (this@LoggerBirdService.filePathVideo != null) {
                            if (this@LoggerBirdService.filePathVideo!!.length() > 0 || controlTimeControllerVideo) {
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
                                        if (filePathVideo != null) {
                                            if (filePathVideo!!.exists()) {
                                                filePathVideo!!.delete()
                                            }
                                        }
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
                    resetEnqueueVideo()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(
                        exception = e,
                        tag = Constants.videoRecordingTag
                    )
                }
            }
        }
    }

    /**
     * This method is used for starting video recording.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun startScreenRecording() {
        if (!videoRecording) {
            shareScreen()
        } else {
            stopScreenRecord()
        }
    }

    /**
     * This method is used for starting video recording animations.
     */
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
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@LoggerBirdService,
                            R.color.mediaRecordColor
                        )
                    )
                floating_action_button.imageTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@LoggerBirdService,
                            R.color.white
                        )
                    )
                callEnqueueVideo()
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

    /**
     * This method is used for initializing video recorder.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                    addFileNameList(fileName = filePathVideo!!.absolutePath)
                    arrayListFile.add(filePathVideo!!)
                    addFileListAsync()
                    mediaCodecsFile = File("/data/misc/media/media_codecs_profiling_results.xml")
                    mediaRecorderVideo?.setOutputFile(filePathVideo!!.path)
                    mediaRecorderVideo?.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                    mediaRecorderVideo?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    mediaRecorderVideo?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    mediaRecorderVideo?.setVideoEncodingBitRate(1024 * 1024)
                    //mediaRecorderVideo?.setVideoFrameRate(CameraProfile.QUALITY_HIGH)
                    mediaRecorderVideo?.setVideoFrameRate(30)
                    //Device can automatically fix its video frame rate in some devices
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
            callEnqueueVideo()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.videoRecordingTag)
        }
    }

    /**
     * This method is used for creating virtual device for video recording.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createVirtualDisplay(): VirtualDisplay? {
        val metrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(metrics)
        screenDensity = metrics.densityDpi
        DISPLAY_HEIGHT = metrics.heightPixels + getNavigationBarHeight()
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

    /**
     * This method is used for getting height of navigation bar of device.
     */
    private fun getNavigationBarHeight(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            val usableHeight = metrics.heightPixels
            activity.windowManager.defaultDisplay.getRealMetrics(metrics)
            val realHeight = metrics.heightPixels
            return if (realHeight > usableHeight) {
                realHeight - usableHeight
            } else {
                0
            }
        }
        return 0
    }

    /**
     * This method is used when screen recording is finished.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stopForegroundServiceVideo()
                }
                videoRecording = false
                stopVideoFileSize()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.videoRecordingTag)
        }
    }

    /**
     * This method is used for clearing media project.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.videoRecordingTag)
        }
    }

    /**
     * This method used when video foreground needs to be started.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundServiceVideo() {
        Log.d("start_foreground", "Foreground Service started!!!!!")
        (context as Activity).startForegroundService(intentForegroundServiceVideo)
    }

    /**
     * This method used when video foreground service needs to be stoped.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun stopForegroundServiceVideo() {
        try {
            (context as Activity).stopService(intentForegroundServiceVideo)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.videoRecordingTag)
        }
    }

    /**
     * This method is used when video recording methods needs to be in queue and executed in synchronized way.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun callVideoRecording(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (LoggerBird.isLogInitAttached()) {
                workQueueLinkedVideo.controlRunnable = false
                runnableList.clear()
                workQueueLinkedVideo.clear()
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        callForegroundService()
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        takeVideoRecording(
                            requestCode = requestCode,
                            resultCode = resultCode,
                            data = data
                        )
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            R.string.screen_recording_unsupported,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                if (runnableList.isEmpty()) {
                    workQueueLinkedVideo.put {
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

    /**
     * This method is used when video foreground service methods needs to be in queue and executed in synchronized way.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun callForegroundService() {
        if (LoggerBird.isLogInitAttached()) {
            if (!videoRecording) {
                if (runnableList.isEmpty()) {
                    workQueueLinkedVideo.put {
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

    /**
     * This method is used when email methods needs to be in queue and executed in synchronized way.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun callEmail(filePathMedia: File? = null, to: String) {
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

    /**
     * This method is used when device was shaked.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun hearShake() {
        try {
            Log.d("shake", "shake fired!!")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this.activity)) {
                    initializeShakeAction()
                } else {
                    if (!isFabEnable) {
                        if (!isActivateDialogShown) {
                            initializeLoggerBirdActivatePopup(activity = this.activity)
                            isActivateDialogShown = true
                        }
                    }
                }
            } else {
                initializeShakeAction()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.shakerTag)
        }
    }

    /**
     * This method is used for initializing actions when device was shaked.
     */
    private fun initializeShakeAction() {
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
    }

    /**
     * This method is used for blocking consecutive clicks in order to prevent spam.
     */
    @SuppressLint("CheckResult")
    private fun View.setSafeOnClickListener(onClick: (View) -> Unit) {
        RxView.clicks(this).throttleFirst(2000, TimeUnit.MILLISECONDS).subscribe {
            onClick(this)
        }
    }

    /**
     * This method is used for checking video file size is less than default file size limit.
     */
    private fun takeVideoFileSize() {
        coroutineCallVideoFileSize.async {
            try {
                timerVideoFileSize = Timer()
                timerVideoTaskFileSize = object : TimerTask() {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun run() {
                        val fileSize = filePathVideo!!.length()
                        val sizePrintVideo =
                            android.text.format.Formatter.formatShortFileSize(
                                activity,
                                fileSize
                            )
//                        if (fileSize > fileLimit) {
//                            timerVideoTaskFileSize?.cancel()
//                            activity.runOnUiThread {
//                                textView_counter_video.performClick()
//                            }
//                        }
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

    /**
     * This method is used for stopping video recording when file size have been exceeded.
     */
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

    /**
     * This method is used for checking audio file size is less than default file size limit.
     */
    private fun takeAudioFileSize() {
        coroutineCallAudioFileSize.async {
            try {
                timerAudioFileSize = Timer()
                timerAudioTaskFileSize = object : TimerTask() {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    override fun run() {
                        val fileSize = filePathAudio!!.length()
                        val sizePrintAudio =
                            android.text.format.Formatter.formatShortFileSize(
                                activity,
                                fileSize
                            )
//                        if (fileSize > fileLimit) {
//                            timerAudioTaskFileSize?.cancel()
//                            activity.runOnUiThread {
//                                textView_counter_audio.performClick()
//                            }
//                        }
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

    /**
     * This method is used for stopping audio recording when file size have been exceeded.
     */
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


    /**
     * This method starts video counter.
     */
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
                            if(counterTime > counterMediaLimit){
                                activity.runOnUiThread {
                                    timerTaskVideo?.cancel()
                                    textView_counter_video.performClick()
                                }
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


        /**
         * This method starts audio counter.
         */
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
                            if(counterTimer > counterMediaLimit){
                                activity.runOnUiThread {
                                    timerTaskAudio?.cancel()
                                    textView_counter_audio.performClick()
                                }
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

    /**
     * This method stops video counter.
     */
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

    /**
     * This method stops audio counter.
     */
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
                textView_counter_audio.performClick()
            }
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.audioRecordingCounterTag
            )
        }
    }

    /**
     * This method is used for saving total and last session time.
     */
    private fun dailySessionTimeRecorder(activity: Activity) {
        sessionTimeEnd = System.currentTimeMillis()
        if (sessionTimeEnd != null && sessionTimeStart != null) {
            val sessionDuration = sessionTimeEnd!! - sessionTimeStart!!
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
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

    /**
     * This method is used for getting total time.
     * @return Long value.
     */
    private fun totalSessionTime(): Long {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        return sharedPref.getLong("session_time", 0)
    }

    /**
     * This method is used for getting last session time.
     * @return Long value.
     */
    private fun lastSessionTime(): Long {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        return sharedPref.getLong("last_session_time", 0)
    }

    /**
     * This method is used for adding Loggerbird file list.
     */
    internal fun addFileList() {
        arrayListFileName.clear()
        if (getFileList() != null) {
            arrayListFileName.addAll(getFileList()!!)
        }
        arrayListFile.forEach {
            arrayListFileName.add(it.absolutePath)
        }
        val gson = Gson()
        val json = gson.toJson(arrayListFileName)
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext) ?: return
        with(sharedPref.edit()) {
            putString("file_quantity", json)
            commit()
        }
    }

    /**
     * This method is used for adding Loggerbird file list in async way.
     */
    internal fun addFileListAsync() {
        coroutineCallFileActionList.async {
           getFileList()?.forEach {
               if(!arrayListFileName.contains(it)){
                   arrayListFileName.add(it)
               }
           }
            val gson = Gson()
            val json = gson.toJson(arrayListFileName)
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            with(sharedPref.edit()) {
                putString("file_quantity", json)
                apply()
            }
        }
    }

    /**
     * This method is used for getting Loggerbird file list.
     */
    private fun getFileList(): ArrayList<String>? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        val gson = Gson()
        val json = sharedPref.getString("file_quantity", "")
        if (json?.isNotEmpty()!!) {
            val arrayListFile:ArrayList<String> = gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
                for(file in 0..arrayListFile.size){
                    if(arrayListFile.size <= file ){
                        break
                    }else{
                        if(!File(arrayListFile[file]).exists()){
                            arrayListFile.removeAt(file)
                        }
                    }
                }
            return arrayListFile
        }
        return null
    }

    /**
     * This method is used for adding file name to file name list.
     * @param fileName is used for getting reference of current file.
     */
    internal fun addFileNameList(fileName: String) {
        arrayListFileName.add(fileName)
    }

    /**
     * This method is used for deleting Loggerbird file list.
     */
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

    /**
     * This method is used for checking that number of Loggerbird files greater than ten.
     */
    private fun controlActionFiles() {
            val arrayListFileList: ArrayList<String>? = getFileList()
            if(arrayListFileList != null){
                if (arrayListFileList.size > 10) {
                    chooseActionFiles()
                }
            }
    }

    /**
     * This method is used for choosing action for a Loggerbird file(delete or send as email).
     */
    private fun chooseActionFiles() {
        this.controlFileAction = true
        initializeLoggerBirdFileActionPopup(activity = this.activity)
    }

    /**
     * This method deletes old Loggerbird files.
     * @param controlEmailAction is used for controlling the email action.
     */
    internal fun deleteOldFiles(controlEmailAction: Boolean? = null) {
        val arrayListOldFiles: ArrayList<String> = ArrayList()
        if (getFileList() != null) {
            arrayListOldFiles.addAll(getFileList()!!)
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

    /**
     * This method send old Loggerbird files as email.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

    /**
     * This method deletes current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun discardMediaFile() {
        coroutineCallDiscardFile.async {
            try {
                if (checkUnhandledFilePath()) {
                    finishShareLayout("unhandled")
                } else {
                    if (controlMediaFile()) {
                        finishShareLayout(message = "media")
                    } else {
                        finishShareLayout(message = "media_error")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finishShareLayout(message = "media_error")
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.discardFileTag)
            }
        }
    }

    /**
     * This method is called when a media file is sent with email action.
     * @param filePathMedia is used for getting the reference of current media file.
     * @param to is used for getting the reference of email address that email action will send.
     * @param messsage is used for getting the reference of message that email action will send.
     * @param subject is used for getting the reference of subject that email action will send.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun sendSingleMediaFile(
        filePathMedia: File? = null,
        to: String,
        message: String? = null,
        subject: String? = null
    ) {
        coroutineCallSendSingleFile.async {
            try {
                LoggerBird.callEmailSender(
                    context = context,
                    activity = activity,
                    file = filePathMedia,
                    to = to,
                    message = message,
                    subject = subject
                )
//                if (filePathMedia.exists()) {
//                    LoggerBird.callEmailSender(
//                        context = context,
//                        activity = activity,
//                        file = filePathMedia,
//                        to = to,
//                        message = message,
//                        subject = subject
//                    )
//                } else {
//                    finishShareLayout("single_email_error")
//                    resetEnqueueEmail()
//                }
            } catch (e: Exception) {
                finishShareLayout("single_email_error")
                e.printStackTrace()
                resetEnqueueEmail()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.singleFileDeleteTag)
            }
        }
    }

    /**
     * This method is used when an error , timeout or success happens with a loggerbird action with main floating action button.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun finishShareLayout(message: String) {
        activity.runOnUiThread {
            when (message) {
                "media" -> {
                    Toast.makeText(context, R.string.share_media_delete, Toast.LENGTH_SHORT).show()
                    finishSuccessFab()
                }
                "media_error" -> {
                    Toast.makeText(context, R.string.share_media_delete_error, Toast.LENGTH_SHORT)
                        .show()
                    finishErrorFab()
                }
                "single_email" -> {
                    detachProgressBar()
                    removeEmailLayout()
                    Toast.makeText(context, R.string.share_file_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab(duplicationField = "email")
                }
                "single_email_error" -> {
                    Toast.makeText(context, R.string.share_file_sent_error, Toast.LENGTH_SHORT)
                        .show()
                    removeEmailLayout()
                    detachProgressBar()
                }
                "jira" -> {
                    removeJiraLayout()
                    Toast.makeText(context, R.string.jira_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab(duplicationField = "jira")
                    detachProgressBar()
                }
                "jira_error" -> {
                    removeJiraLayout()
                    Toast.makeText(context, R.string.jira_sent_error, Toast.LENGTH_SHORT).show()
                    detachProgressBar()
                }
                "jira_error_time_out" -> {
                    removeJiraLayout()
                    Toast.makeText(context, R.string.jira_sent_error_time_out, Toast.LENGTH_SHORT)
                        .show()
                    detachProgressBar()
                }

                "slack" -> {
                    detachProgressBar()
                    removeSlackLayout()
                    Toast.makeText(context, R.string.slack_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab(duplicationField = "slack")
                }

                "slack_error" -> {
                    removeSlackLayout()
                    Toast.makeText(context, R.string.slack_sent_error, Toast.LENGTH_SHORT).show()
                    detachProgressBar()
                }

                "slack_error_time_out" -> {
                    removeSlackLayout()
                    Toast.makeText(context, R.string.slack_sent_error_time_out, Toast.LENGTH_SHORT)
                        .show()
                    detachProgressBar()
                }

                "gitlab" -> {
                    Toast.makeText(context, R.string.gitlab_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab(duplicationField = "gitlab")
                    removeGitlabLayout()
                    detachProgressBar()
                }

                "gitlab_error" -> {
                    removeGitlabLayout()
                    Toast.makeText(context, R.string.gitlab_sent_error, Toast.LENGTH_SHORT).show()
                    detachProgressBar()
                }

                "gitlab_error_time_out" -> {
                    removeGitlabLayout()
                    Toast.makeText(context, R.string.gitlab_sent_error_time_out, Toast.LENGTH_SHORT)
                        .show()
                    detachProgressBar()
                }

                "github" -> {
                    detachProgressBar()
                    removeGithubLayout()
                    Toast.makeText(context, R.string.github_issue_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab(duplicationField = "github")
                }
                "github_error" -> {
                    detachProgressBar()
                    removeGithubLayout()
                    Toast.makeText(context, R.string.github_issue_failure, Toast.LENGTH_SHORT)
                        .show()
                }
                "github_error_time_out" -> {
                    detachProgressBar()
                    removeGithubLayout()
                    Toast.makeText(context, R.string.github_issue_time_out, Toast.LENGTH_SHORT)
                        .show()
                }
                "trello" -> {
                    detachProgressBar()
                    removeTrelloLayout()
                    Toast.makeText(context, R.string.trello_issue_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab(duplicationField = "trello")
                }
                "trello_error" -> {
                    detachProgressBar()
                    removeTrelloLayout()
                    Toast.makeText(context, R.string.trello_issue_failure, Toast.LENGTH_SHORT)
                        .show()
                }
                "trello_error_time_out" -> {
                    detachProgressBar()
                    removeTrelloLayout()
                    Toast.makeText(context, R.string.trello_issue_time_out, Toast.LENGTH_SHORT)
                        .show()
                }
                "pivotal" -> {
                    detachProgressBar()
                    removePivotalLayout()
                    Toast.makeText(context, R.string.pivotal_issue_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab(duplicationField = "pivotal")
                }
                "pivotal_error" -> {
                    detachProgressBar()
                    removePivotalLayout()
                    Toast.makeText(context, R.string.pivotal_issue_failure, Toast.LENGTH_SHORT)
                        .show()
                }
                "pivotal_error_time_out" -> {
                    detachProgressBar()
                    removePivotalLayout()
                    Toast.makeText(context, R.string.pivotal_issue_time_out, Toast.LENGTH_SHORT)
                        .show()
                }
                "basecamp" -> {
                    detachProgressBar()
                    removeBasecampLayout()
                    Toast.makeText(context, R.string.basecamp_issue_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab(duplicationField = "basecamp")
                }
                "basecamp_error" -> {
                    detachProgressBar()
                    removeBasecampLayout()
                    Toast.makeText(context, R.string.basecamp_issue_failure, Toast.LENGTH_SHORT)
                        .show()
                }
                "basecamp_error_time_out" -> {
                    detachProgressBar()
                    removeBasecampLayout()
                    Toast.makeText(context, R.string.basecamp_issue_time_out, Toast.LENGTH_SHORT)
                        .show()
                }
                "asana" -> {
                    detachProgressBar()
                    removeAsanaLayout()
                    Toast.makeText(context, R.string.asana_issue_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab(duplicationField = "asana")
                }
                "asana_error" -> {
                    detachProgressBar()
                    removeAsanaLayout()
                    Toast.makeText(context, R.string.asana_issue_failure, Toast.LENGTH_SHORT)
                        .show()
                }
                "asana_error_time_out" -> {
                    detachProgressBar()
                    removeAsanaLayout()
                    Toast.makeText(context, R.string.asana_issue_time_out, Toast.LENGTH_SHORT)
                        .show()
                }
                "clubhouse" -> {
                    detachProgressBar()
                    removeClubhouseLayout()
                    Toast.makeText(context, R.string.clubhouse_issue_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab(duplicationField = "clubhouse")
                }
                "clubhouse_error" -> {
                    detachProgressBar()
                    removeClubhouseLayout()
                    Toast.makeText(context, R.string.clubhouse_issue_failure, Toast.LENGTH_SHORT)
                        .show()
                }
                "clubhouse_error_time_out" -> {
                    detachProgressBar()
                    removeClubhouseLayout()
                    Toast.makeText(context, R.string.clubhouse_issue_time_out, Toast.LENGTH_SHORT)
                        .show()
                }
                "unhandled" -> {
                    Toast.makeText(
                        context,
                        R.string.unhandled_file_discard_success,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    finishSuccessFab()
                }
                "bitbucket" -> {
                    detachProgressBar()
                    removeBitbucketLayout()
                    Toast.makeText(context, R.string.bitbucket_issue_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab(duplicationField = "bitbucket")
                }
                "bitbucket_error" -> {
                    detachProgressBar()
                    removeBitbucketLayout()
                    Toast.makeText(context, R.string.bitbucket_issue_failure, Toast.LENGTH_SHORT)
                        .show()
                }
                "bitbucket_error_time_out" -> {
                    detachProgressBar()
                    removeBitbucketLayout()
                    Toast.makeText(context, R.string.bitbucket_issue_time_out, Toast.LENGTH_SHORT)
                        .show()
                }

            }
            if (controlFloatingActionButtonView() && !this.controlFileAction) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * This method is used for showing an success happened when using a loggerbird action with main floating action button.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun finishSuccessFab(duplicationField: String? = null) {
        if (controlRevealShareLayout() && controlFloatingActionButtonView()) {
            if (checkUnhandledFilePath()) {
                val sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                if (sharedPref.getString("unhandled_file_path", null) != null) {
                    val filepath = File(sharedPref.getString("unhandled_file_path", null)!!)
                    if (filepath.exists()) {
                        filepath.delete()
                    }
                }
                if (sharedPref.getString("unhandled_media_file_path", null) != null) {
                    val mediaFilePath =
                        File(sharedPref.getString("unhandled_media_file_path", null)!!)
                    if (mediaFilePath.exists()) {
                        mediaFilePath.exists()
                    }
                }
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.remove("unhandled_file_path")
                editor.remove("unhandled_media_file_path")
                editor.apply()
                if (sharedPref.getBoolean(
                        "duplication_enabled",
                        false
                    ) && duplicationField != null
                ) {
                    val coroutineScopeUnhandledDuplication = CoroutineScope(Dispatchers.IO)
                    coroutineScopeUnhandledDuplication.async {
                        val unhandledDuplicationDb =
                            UnhandledDuplicationDb.getUnhandledDuplicationDb(LoggerBird.context.applicationContext)
                        val unhandledDuplicationDao =
                            unhandledDuplicationDb?.unhandledDuplicationDao()
                        val unhandledDuplication = UnhandledDuplication(
                            className = sharedPref.getString("unhandled_stack_class", null),
                            methodName = sharedPref.getString("unhandled_stack_method", null),
                            lineName = sharedPref.getString("unhandled_stack_line", null),
                            fieldName = duplicationField,
                            exceptionName = sharedPref.getString("unhandled_stack_exception", null)
                        )
                        with(unhandledDuplicationDao) {
                            this?.insertUnhandledDuplication(unhandledDuplication = unhandledDuplication)
                        }
                    }
                }
            }
            if (this.controlFileAction) {
                if (RecyclerViewFileActionAttachmentAdapter.ViewHolder.position != null) {
                    val arrayListTempFileAction: ArrayList<String> = ArrayList()
                    if (arrayListLoggerBirdFileActionList.size > RecyclerViewFileActionAttachmentAdapter.ViewHolder.position!!) {
                        arrayListLoggerBirdFileActionList.removeAt(
                            RecyclerViewFileActionAttachmentAdapter.ViewHolder.position!!
                        )
                        recyclerViewLoggerBirdFileActionListPopup.adapter?.notifyDataSetChanged()
                        arrayListLoggerBirdFileActionList.forEach {
                            arrayListTempFileAction.add(it.file.absolutePath)
                        }
                        val gson = Gson()
                        val json = gson.toJson(arrayListTempFileAction)
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                                ?: return
                        with(sharedPref.edit()) {
                            putString("file_quantity", json)
                            commit()
                        }
                    }
                }
            }
            revealLinearLayoutShare.visibility = View.GONE
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

    /**
     * This method is used for showing an error happened when using a loggerbird action with main floating action button.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun finishErrorFab() {
        if (controlRevealShareLayout() && controlFloatingActionButtonView()) {
            revealLinearLayoutShare.visibility = View.GONE
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

    /**
     * This method deletes media file after email sent.
     * @param controlEmailAction is used for controlling the email action.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun deleteSingleMediaFile(controlEmailAction: Boolean? = null, filePathMedia: File) {
        if (filePathMedia.exists()) {
            filePathMedia.delete()
        }

        finishShareLayout("single_email")
        LoggerBird.callEnqueue()
    }

    /**
     * This method attaches default progressbar into the window.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun attachProgressBar(task: String) {
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
        closeProgressBar(task)

    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun closeProgressBar(task: String) {
        imageViewProgressBarClose = progressBarView.findViewById(R.id.imageView_progressbar_close)
        imageViewProgressBarClose.setSafeOnClickListener {
            detachProgressBar()
            when (task) {
                "email" -> {
                    removeEmailLayout()
                }
                "jira" -> {
                    removeJiraLayout()
                }
                "slack" -> {
                    removeSlackLayout()
                }
                "gitlab" -> {
                    removeGitlabLayout()
                }
                "github" -> {
                    removeGithubLayout()
                }
                "trello" -> {
                    removeTrelloLayout()
                }
                "pivotal" -> {
                    removePivotalLayout()
                }
                "basecamp" -> {
                    removeBasecampLayout()
                }
                "asana" -> {
                    removeAsanaLayout()
                }
                "clubhouse" -> {
                    removeClubhouseLayout()
                }
                "bitbucket" -> {
                    removeBitbucketLayout()
                }
            }
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * This method detaches default progressbar.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun detachProgressBar() {
        if (this::progressBarView.isInitialized && windowManagerProgressBar != null) {
            (windowManagerProgressBar as WindowManager).removeViewImmediate(progressBarView)
            windowManagerProgressBar = null
        }
    }

    /**
     * This method returns current activity reference.
     */
    internal fun returnActivity(): Activity {
        return activity
    }
    //Feedback
    /**
     * This method is used for creating feedback layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
                progressBarFeedbackLayout =
                    viewFeedback.findViewById(R.id.feedback_progressbar_background)
                buttonClicksFeedback()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.feedbackTag)
        }
    }

    /**
     * This method is used for removing loggerbird_jira_popup from window.
     */
    private fun removeFeedBackLayout() {
        if (windowManagerFeedback != null && this::viewFeedback.isInitialized) {
            (windowManagerFeedback as WindowManager).removeViewImmediate(viewFeedback)
            windowManagerFeedback = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_feedback.
     */
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

    /**
     * This method is used for sending feedback.
     */
    private fun sendFeedback(to: String) {
        if (editText_feedback.text.trim().isNotEmpty()) {
            removeFeedBackLayout()
            coroutineCallFeedback.async {
                EmailUtil.sendFeedbackEmail(
                    context = context,
                    message = editText_feedback.text.toString(),
                    to = to
                )
                withContext(Dispatchers.Main) {
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

    //Jira
    /**
     * This method is used for creating jira layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeJiraLayout(filePathMedia: File) {
        try {
            if (windowManagerJira != null && this::viewJira.isInitialized) {
                (windowManagerJira as WindowManager).removeViewImmediate(viewJira)
                arrayListJiraFileName.clear()
            }
            this.rootView = activity.window.decorView.findViewById(android.R.id.content)
            viewJira = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_jira_popup, (this.rootView as ViewGroup), false)
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
                activity.window.navigationBarColor =
                    ContextCompat.getColor(this, R.color.black)
                activity.window.statusBarColor = ContextCompat.getColor(this, R.color.black)

                autoTextViewJiraProject = viewJira.findViewById(R.id.auto_textView_jira_project)
                autoTextViewJiraIssueType =
                    viewJira.findViewById(R.id.auto_textView_jira_issue_type)
                recyclerViewJiraAttachment =
                    viewJira.findViewById(R.id.recycler_view_jira_attachment)
                editTextJiraSummary = viewJira.findViewById(R.id.editText_jira_summary)
                editTextJiraDescription = viewJira.findViewById(R.id.editText_jira_description)
                autoTextViewJiraReporter =
                    viewJira.findViewById(R.id.auto_textView_jira_reporter)
                autoTextViewJiraLinkedIssue =
                    viewJira.findViewById(R.id.auto_textView_jira_linked_issues)
                autoTextViewJiraIssue = viewJira.findViewById(R.id.auto_textView_jira_issues)
                autoTextViewJiraAssignee =
                    viewJira.findViewById(R.id.auto_textView_jira_assignee)
                autoTextViewJiraPriority =
                    viewJira.findViewById(R.id.auto_textView_jira_priority)
                autoTextViewJiraComponent =
                    viewJira.findViewById(R.id.auto_textView_jira_component)
                autoTextViewJiraFixVersions =
                    viewJira.findViewById(R.id.auto_textView_jira_fix_versions)
                autoTextViewJiraLabel = viewJira.findViewById(R.id.auto_textView_jira_labels)
                autoTextViewJiraEpicLink =
                    viewJira.findViewById(R.id.auto_textView_jira_epic_link)
                autoTextViewJiraSprint = viewJira.findViewById(R.id.auto_textView_jira_sprint)
                autoTextViewJiraEpicName =
                    viewJira.findViewById(R.id.auto_textView_jira_epic_name)
                buttonJiraCreate = viewJira.findViewById(R.id.button_jira_create)
                buttonJiraCancel = viewJira.findViewById(R.id.button_jira_cancel)
                toolbarJira = viewJira.findViewById(R.id.textView_jira_title)
                layoutJira = viewJira.findViewById(R.id.layout_jira)
                progressBarJira = viewJira.findViewById(R.id.jira_progressbar)
                progressBarJiraLayout = viewJira.findViewById(R.id.jira_progressbar_background)
                cardViewJiraSprint = viewJira.findViewById(R.id.cardView_sprint)
                cardViewJiraStartDate = viewJira.findViewById(R.id.cardView_start_date)
                cardViewJiraEpicName = viewJira.findViewById(R.id.cardView_epic_name)
                cardViewJiraEpicLink = viewJira.findViewById(R.id.cardView_epic_link)
                imageViewStartDate = viewJira.findViewById(R.id.imageView_start_date)
                imageButtonRemoveDate =
                    viewJira.findViewById(R.id.image_button_jira_remove_date)
                scrollViewJira = viewJira.findViewById(R.id.scrollView_jira)

                scrollViewJira.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        hideKeyboard(activity = activity, view = viewJira)
                    }
                    return@setOnTouchListener false
                }
                cardViewJiraIssueList = viewJira.findViewById(R.id.cardView_issues_list)
                imageViewJiraIssue = viewJira.findViewById(R.id.imageView_issue_add)
                recyclerViewJiraIssueList =
                    viewJira.findViewById(R.id.recycler_view_issues_list)

                cardViewJiraLabelList = viewJira.findViewById(R.id.cardView_label_list)
                imageViewJiraLabel = viewJira.findViewById(R.id.imageView_label_add)
                recyclerViewJiraLabelList = viewJira.findViewById(R.id.recycler_view_label_list)

                cardViewJiraComponentList = viewJira.findViewById(R.id.cardView_component_list)
                imageViewJiraComponent = viewJira.findViewById(R.id.imageView_component_add)
                recyclerViewJiraComponentList =
                    viewJira.findViewById(R.id.recycler_view_component_list)

                cardViewJiraFixVersionsList =
                    viewJira.findViewById(R.id.cardView_fix_versions_list)
                imageViewJiraFixVersions =
                    viewJira.findViewById(R.id.imageView_fix_versions_add)
                recyclerViewJiraFixVersionsList =
                    viewJira.findViewById(R.id.recycler_view_fix_versions_list)

                jiraAuthentication.callJira(
                    context = context,
                    activity = activity,
                    task = "get",
                    createMethod = "normal"
                )

                initializeJiraAttachmentRecyclerView(filePathMedia = filePathMedia)
                initializeJiraIssueRecyclerView()
                initializeJiraLabelRecyclerView()
                initializeJiraComponentRecyclerView()
                initializeJiraFixVersionsRecyclerView()
                buttonClicksJira(filePathMedia = filePathMedia)
                attachProgressBar(task = "jira")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
    }

    /**
     * This method is used for removing loggerbird_jira_popup from window.
     */
    private fun removeJiraLayout() {
        if (windowManagerJira != null && this::viewJira.isInitialized) {
            (windowManagerJira as WindowManager).removeViewImmediate(viewJira)
            windowManagerJira = null
            arrayListJiraFileName.clear()
            projectJiraPosition = 0
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_jira_popup.
     */
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
                autoTextViewProject = autoTextViewJiraProject,
                autoTextViewIssueType = autoTextViewJiraIssueType,
                autoTextViewLinkedIssues = autoTextViewJiraLinkedIssue,
                autoTextViewIssues = autoTextViewJiraIssue,
                autoTextViewAssignee = autoTextViewJiraAssignee,
                autoTextViewReporter = autoTextViewJiraReporter,
                autoTextViewPriority = autoTextViewJiraPriority,
                autoTextViewComponent = autoTextViewJiraComponent,
                autoTextViewFixVersions = autoTextViewJiraFixVersions,
                autoTextViewLabel = autoTextViewJiraLabel,
                autoTextViewEpicLink = autoTextViewJiraEpicLink,
                autoTextViewSprint = autoTextViewJiraSprint,
                autoTextViewEpicName = autoTextViewJiraEpicName
            )
            jiraAuthentication.gatherJiraEditTextDetails(
                editTextSummary = editTextJiraSummary,
                editTextDescription = editTextJiraDescription
            )
            if (autoTextViewJiraIssueType.editableText.toString() != "Epic") {
                callJiraTask(filePathMedia = filePathMedia)
            } else {
                if (jiraAuthentication.checkJiraEpicName(activity = activity, context = context)) {
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
                        putString("jira_project", autoTextViewJiraProject.editableText.toString())
                        putInt("jira_project_position", projectJiraPosition)
                        putString(
                            "jira_issue_type",
                            autoTextViewJiraIssueType.editableText.toString()
                        )
                        putString("jira_summary", editTextJiraSummary.text.toString())
                        putString("jira_description", editTextJiraDescription.text.toString())
                        putString(
                            "jira_component",
                            autoTextViewJiraComponent.editableText.toString()
                        )
                        putString("jira_reporter", autoTextViewJiraReporter.editableText.toString())
                        putString(
                            "jira_linked_issue",
                            autoTextViewJiraLinkedIssue.editableText.toString()
                        )
                        putString("jira_issue", autoTextViewJiraIssue.editableText.toString())
                        putString("jira_assignee", autoTextViewJiraAssignee.editableText.toString())
                        putString("jira_priority", autoTextViewJiraPriority.editableText.toString())
                        putString(
                            "jira_fix_versions",
                            autoTextViewJiraFixVersions.editableText.toString()
                        )
                        putString("jira_labels", autoTextViewJiraLabel.editableText.toString())
                        putString(
                            "jira_epic_link",
                            autoTextViewJiraEpicLink.editableText.toString()
                        )
                        putString("jira_sprint", autoTextViewJiraSprint.editableText.toString())
                        putString(
                            "jira_epic_name",
                            autoTextViewJiraEpicName.editableText.toString()
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
                    projectJiraPosition = 0
                    editTextJiraDescription.text = null
                    editTextJiraSummary.text = null
                    autoTextViewJiraComponent.setText("", false)
                    autoTextViewJiraReporter.setText("", false)
                    autoTextViewJiraIssue.setText("", false)
                    autoTextViewJiraAssignee.setText("", false)
                    autoTextViewJiraFixVersions.setText("", false)
                    autoTextViewJiraLabel.setText("", false)
                    autoTextViewJiraEpicLink.setText("", false)
                    autoTextViewJiraSprint.setText("", false)
                    autoTextViewJiraEpicName.setText("", false)
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

        imageButtonRemoveDate.setOnClickListener {
            jiraAuthentication.setStartDate(startDate = null)
            imageButtonRemoveDate.visibility = View.GONE
        }
        imageViewJiraIssue.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewJira)
            if (!arrayListJiraIssueName.contains(
                    RecyclerViewModelIssue(
                        autoTextViewJiraIssue.editableText.toString()
                    )
                ) && arrayListJiraIssue.contains(
                    autoTextViewJiraIssue.editableText.toString()
                )
            ) {
                arrayListJiraIssueName.add(RecyclerViewModelIssue(autoTextViewJiraIssue.editableText.toString()))
                jiraAdapterIssueList.notifyDataSetChanged()
                cardViewJiraIssueList.visibility = View.VISIBLE
            } else if (arrayListJiraIssueName.contains(
                    RecyclerViewModelIssue(autoTextViewJiraIssue.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.jira_issue_exist)
                )
            } else if (!arrayListJiraIssue.contains(autoTextViewJiraIssue.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.jira_issue_doesnt_exist)
                )
            }

        }
        imageViewJiraLabel.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewJira)
            if (!arrayListJiraLabelName.contains(
                    RecyclerViewModelLabel(
                        autoTextViewJiraLabel.editableText.toString()
                    )
                ) && arrayListJiraLabel.contains(
                    autoTextViewJiraLabel.editableText.toString()
                )
            ) {
                arrayListJiraLabelName.add(RecyclerViewModelLabel(autoTextViewJiraLabel.editableText.toString()))
                jiraAdapterLabelList.notifyDataSetChanged()
                cardViewJiraLabelList.visibility = View.VISIBLE
            } else if (arrayListJiraLabelName.contains(
                    RecyclerViewModelLabel(autoTextViewJiraLabel.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.jira_label_exist)
                )
            } else if (!arrayListJiraLabel.contains(autoTextViewJiraLabel.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.jira_label_doesnt_exist)
                )
            }

        }
        imageViewJiraComponent.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewJira)
            if (!arrayListJiraComponentName.contains(
                    RecyclerViewModelComponent(
                        autoTextViewJiraComponent.editableText.toString()
                    )
                ) && arrayListJiraComponent.contains(
                    autoTextViewJiraComponent.editableText.toString()
                )
            ) {
                arrayListJiraComponentName.add(RecyclerViewModelComponent(autoTextViewJiraComponent.editableText.toString()))
                jiraAdapterComponentList.notifyDataSetChanged()
                cardViewJiraComponentList.visibility = View.VISIBLE
            } else if (arrayListJiraComponentName.contains(
                    RecyclerViewModelComponent(autoTextViewJiraComponent.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.jira_component_exist)
                )
            } else if (!arrayListJiraComponent.contains(autoTextViewJiraComponent.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.jira_component_doesnt_exist)
                )
            }

        }

        imageViewJiraFixVersions.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewJira)
            if (!arrayListJiraFixVersionsName.contains(
                    RecyclerViewModelFixVersions(
                        autoTextViewJiraFixVersions.editableText.toString()
                    )
                ) && arrayListJiraFixVersions.contains(
                    autoTextViewJiraFixVersions.editableText.toString()
                )
            ) {
                arrayListJiraFixVersionsName.add(
                    RecyclerViewModelFixVersions(
                        autoTextViewJiraFixVersions.editableText.toString()
                    )
                )
                jiraAdapterFixVersionsList.notifyDataSetChanged()
                cardViewJiraFixVersionsList.visibility = View.VISIBLE
            } else if (arrayListJiraFixVersionsName.contains(
                    RecyclerViewModelFixVersions(autoTextViewJiraFixVersions.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.jira_fix_versions_exist)
                )
            } else if (!arrayListJiraFixVersions.contains(autoTextViewJiraFixVersions.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.jira_fix_versions_doesnt_exist)
                )
            }

        }

    }

    /**
     * This method is used for initializing jira issue recyclerView.
     */
    private fun initializeJiraIssueRecyclerView() {
        arrayListJiraIssueName.clear()
        recyclerViewJiraIssueList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        jiraAdapterIssueList =
            RecyclerViewJiraIssueAdapter(
                arrayListJiraIssueName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewJiraIssueList.adapter = jiraAdapterIssueList
    }

    /**
     * This method is used for initializing jira label recyclerView.
     */
    private fun initializeJiraLabelRecyclerView() {
        arrayListJiraLabelName.clear()
        recyclerViewJiraLabelList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        jiraAdapterLabelList =
            RecyclerViewJiraLabelAdapter(
                arrayListJiraLabelName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewJiraLabelList.adapter = jiraAdapterLabelList
    }

    /**
     * This method is used for initializing jira component recyclerView.
     */
    private fun initializeJiraComponentRecyclerView() {
        arrayListJiraComponentName.clear()
        recyclerViewJiraComponentList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        jiraAdapterComponentList =
            RecyclerViewJiraComponentAdapter(
                arrayListJiraComponentName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewJiraComponentList.adapter = jiraAdapterComponentList
    }

    /**
     * This method is used for initializing jira fix versions recyclerView.
     */
    private fun initializeJiraFixVersionsRecyclerView() {
        arrayListJiraFixVersionsName.clear()
        recyclerViewJiraFixVersionsList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        jiraAdapterFixVersionsList =
            RecyclerViewJiraFixVersionsAdapter(
                arrayListJiraFixVersionsName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewJiraFixVersionsList.adapter = jiraAdapterFixVersionsList
    }

    /**
     * This method is used for opening jira issue.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun callJiraTask(filePathMedia: File) {
        if (jiraAuthentication.checkJiraProjectEmpty(
                activity = activity,
                autoTextViewProject = autoTextViewJiraProject
            ) && jiraAuthentication.checkJiraIssueTypeEmpty(
                activity = activity,
                autoTextViewIssueType = autoTextViewJiraIssueType
            ) && jiraAuthentication.checkJiraSummaryEmpty(
                activity = activity,
                context = context
            ) && jiraAuthentication.checkJiraReporterEmpty(
                activity = activity,
                context = context
            ) && jiraAuthentication.checkJiraFixVersionsEmpty(
                activity = activity,
                context = context
            ) && jiraAuthentication.checkJiraEpicLinkEmpty(
                activity = activity,
                context = context
            ) && jiraAuthentication.checkJiraComponentEmpty(
                activity = activity,
                context = context
            ) && jiraAuthentication.checkJiraLabel(
                activity = activity,
                autoTextViewLabel = autoTextViewJiraLabel
            ) && jiraAuthentication.checkJiraIssue(
                activity = activity,
                autoTextViewIssue = autoTextViewJiraIssue
            ) && jiraAuthentication.checkJiraPriorityEmpty(
                activity = activity,
                autoTextViewPriority = autoTextViewJiraPriority
            ) && jiraAuthentication.checkJiraLinkedIssuesEmpty(
                activity = activity,
                autoTextViewLinkedIssues = autoTextViewJiraLinkedIssue
            )
        ) {
            attachProgressBar(task = "jira")
//                hideKeyboard(activity = activity)
            jiraAuthentication.callJira(
                filePathMedia = filePathMedia,
                context = context,
                activity = activity,
                task = "create",
                createMethod = "normal"
            )
        }
    }

    /**
     * This method is used for adding files to jira file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addJiraFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListJiraFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListJiraFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListJiraFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListJiraFileName
    }

    /**
     * This method is used for initializing jira attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeJiraAttachmentRecyclerView(filePathMedia: File) {
        recyclerViewJiraAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        jiraAttachmentAdapter =
            RecyclerViewJiraAttachmentAdapter(
                addJiraFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewJiraAttachment.adapter = jiraAttachmentAdapter
    }

    /**
     * This method is used for initializing  autoCompleteTextViews in the loggerbird_jira_popup.
     * @param arrayListJiraProjectNames is used for getting the project list for project autoCompleteTextView.
     * @param arrayListJiraProjectKeys is used for getting the project key list.
     * @param arrayListJiraIssueTypes is used for getting the issue type list for issue type autoCompleteTextView.
     * @param arrayListJiraReporterNames is used for getting the reporter list for reporter autoCompleteTextView.
     * @param arrayListJiraLinkedIssues is used for getting the linked issues list for linked issues autoCompleteTextView.
     * @param arrayListJiraIssues is used for getting the issues list for issues autoCompleteTextView.
     * @param arrayListJiraAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param arrayListJiraPriority is used for getting the priority list for priority autoCompleteTextView.
     * @param arrayListJiraComponent is used for getting the component list for component autoCompleteTextView.
     * @param arrayListJiraFixVersions is used for getting the fix versions list for fix versions autoCompleteTextView.
     * @param arrayListJiraEpicLink is used for getting the epic link list for epic link autoCompleteTextView.
     * @param arrayListJiraSprint is used for getting the sprint list for sprint autoCompleteTextView.
     * @param arrayListJiraEpicName is used for getting the epic list for epic autoCompleteTextView.
     * @param hashMapJiraBoardList is used for getting the board list.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeJiraAutoTextViews(
        arrayListJiraProjectNames: ArrayList<String>,
        arrayListJiraProjectKeys: ArrayList<String>,
        arrayListJiraIssueTypes: ArrayList<String>,
        arrayListJiraReporterNames: ArrayList<String>,
        arrayListJiraLinkedIssues: ArrayList<String>,
        arrayListJiraIssues: ArrayList<String>,
        arrayListJiraAssignee: ArrayList<String>,
        arrayListJiraPriority: ArrayList<String>,
        arrayListJiraComponent: ArrayList<String>,
        arrayListJiraFixVersions: ArrayList<String>,
        arrayListJiraLabel: ArrayList<String>,
        arrayListJiraEpicLink: ArrayList<String>,
        arrayListJiraSprint: ArrayList<String>,
        arrayListJiraEpicName: ArrayList<String>,
        hashMapJiraBoardList: HashMap<String, String>
    ) {
        try {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            editTextJiraSummary.setText(sharedPref.getString("jira_summary", null))
            editTextJiraDescription.setText(sharedPref.getString("jira_description", null))
            initializeJiraProjectName(
                arrayListJiraProjectNames = arrayListJiraProjectNames,
                sharedPref = sharedPref
            )
            initializeJiraIssueType(
                arrayListJiraIssueTypes = arrayListJiraIssueTypes,
                arrayListJiraEpicName = arrayListJiraEpicName,
                sharedPref = sharedPref
            )
            initializeJiraReporter(
                arrayListJiraReporterNames = arrayListJiraReporterNames,
                sharedPref = sharedPref
            )
            initializeJiraLinkedIssues(
                arrayListJiraLinkedIssues = arrayListJiraLinkedIssues,
                sharedPref = sharedPref
            )
            initializeJiraIssues(arrayListJiraIssues = arrayListJiraIssues, sharedPref = sharedPref)
            initializeJiraAssignee(
                arrayListJiraAssignee = arrayListJiraAssignee,
                sharedPref = sharedPref
            )
            initializeJiraPriority(
                arrayListJiraPriority = arrayListJiraPriority,
                sharedPref = sharedPref
            )
            initializeJiraComponent(
                arrayListJiraComponent = arrayListJiraComponent,
                sharedPref = sharedPref
            )
            initializeJiraFixVersions(
                arrayListJiraFixVersions = arrayListJiraFixVersions,
                sharedPref = sharedPref
            )
            initializeJiraLabels(arrayListJiraLabel = arrayListJiraLabel, sharedPref = sharedPref)
            initializeJiraEpicLink(
                arrayListJiraEpicLink = arrayListJiraEpicLink,
                sharedPref = sharedPref
            )
            initializeJiraSprint(
                arrayListJiraSprint = arrayListJiraSprint,
                hashMapJiraBoardList = hashMapJiraBoardList,
                arrayListJiraProjectKeys = arrayListJiraProjectKeys,
                sharedPref = sharedPref
            )
            initializeJiraEpicName(
                arrayListJiraEpicName = arrayListJiraEpicName,
                sharedPref = sharedPref
            )

            if (checkUnhandledFilePath()) {
                editTextJiraSummary.setText(activity.resources.getString(R.string.jira_summary_unhandled_exception))
                if (checkBoxUnhandledChecked()) {
                    editTextJiraDescription.setText(
                        sharedPref.getString(
                            "unhandled_exception_message",
                            null
                        )
                    )
                    editTextJiraDescription.isFocusable = false
                }
            }
            detachProgressBar()
        } catch (e: Exception) {
            detachProgressBar()
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
    }

    /**
     * This method is used for initializing sprint autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraSprint is used for getting the sprint list for sprint autoCompleteTextView.
     * @param hashMapJiraBoardList is used for getting the board list.
     * @param arrayListJiraProjectKeys is used for getting project key list.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeJiraSprint(
        arrayListJiraSprint: ArrayList<String>,
        hashMapJiraBoardList: HashMap<String, String>,
        arrayListJiraProjectKeys: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        if (!controlProjectJiraPosition) {
            projectJiraPosition = sharedPref.getInt("jira_project_position", 0)
        }
        controlProjectJiraPosition = false
        if (hashMapJiraBoardList[arrayListJiraProjectKeys[projectJiraPosition]] == "scrum") {
            cardViewJiraSprint.visibility = View.VISIBLE
            cardViewJiraStartDate.visibility = View.VISIBLE
            autoTextViewJiraSprintAdapter = AutoCompleteTextViewJiraSprintAdapter(
                this,
                R.layout.auto_text_view_jira_sprint_item,
                arrayListJiraSprint
            )
            autoTextViewJiraSprint.setAdapter(autoTextViewJiraSprintAdapter)
            if (arrayListJiraSprint.isNotEmpty()) {
                if (sharedPref.getString("jira_sprint", null) != null) {
                    autoTextViewJiraSprint.setText(
                        sharedPref.getString("jira_sprint", null),
                        false
                    )
                }
//                autoTextViewSprint.setText(arrayListSprint[0], false)
            }
            autoTextViewJiraSprint.setOnTouchListener { v, event ->
                autoTextViewJiraSprint.showDropDown()
                false
            }
            autoTextViewJiraSprint.setOnItemClickListener { parent, view, position, id ->
                jiraAuthentication.setSprintPosition(sprintPosition = position)
                hideKeyboard(activity = activity, view = viewJira)
            }
        } else {
            cardViewJiraSprint.visibility = View.GONE
            cardViewJiraStartDate.visibility = View.GONE
        }
    }

    /**
     * This method is used for initializing epic link autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraEpicLink is used for getting the epic link list for epic link autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeJiraEpicLink(
        arrayListJiraEpicLink: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraEpicLinkAdapter = AutoCompleteTextViewJiraEpicLinkAdapter(
            this,
            R.layout.auto_text_view_jira_epic_link_item,
            arrayListJiraEpicLink
        )
        autoTextViewJiraEpicLink.setAdapter(autoTextViewJiraEpicLinkAdapter)
        if (arrayListJiraEpicLink.isNotEmpty()) {
            if (sharedPref.getString("jira_epic_link", null) != null) {
                autoTextViewJiraEpicLink.setText(
                    sharedPref.getString("jira_epic_link", null),
                    false
                )
            }
//                autoTextViewEpicLink.setText(arrayListEpicLink[0], false)
        }
        autoTextViewJiraEpicLink.setOnTouchListener { v, event ->
            autoTextViewJiraEpicLink.showDropDown()
            false
        }
        autoTextViewJiraEpicLink.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewJira)
        }
    }

    /**
     * This method is used for initializing label autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraLabel is used for getting the label list for label autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeJiraLabels(
        arrayListJiraLabel: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraLabelAdapter = AutoCompleteTextViewJiraLabelAdapter(
            this,
            R.layout.auto_text_view_jira_label_item,
            arrayListJiraLabel
        )
        autoTextViewJiraLabel.setAdapter(autoTextViewJiraLabelAdapter)
        if (arrayListJiraLabel.isNotEmpty()) {
            if (sharedPref.getString("jira_labels", null) != null) {
                autoTextViewJiraLabel.setText(sharedPref.getString("jira_labels", null), false)
            }
//                autoTextViewLabel.setText(arrayListLabel[0], false)
        }
        autoTextViewJiraLabel.setOnTouchListener { v, event ->
            autoTextViewJiraLabel.showDropDown()
            false
        }
        autoTextViewJiraLabel.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewJira)
        }
        this.arrayListJiraLabel = arrayListJiraLabel
    }

    /**
     * This method is used for initializing fix versions autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraFixVersions is used for getting the fix versions list for fix versions autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeJiraFixVersions(
        arrayListJiraFixVersions: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewFixVersionsAdapter = AutoCompleteTextViewJiraFixVersionsAdapter(
            this,
            R.layout.auto_text_view_jira_fix_versions_item,
            arrayListJiraFixVersions
        )
        autoTextViewJiraFixVersions.setAdapter(autoTextViewFixVersionsAdapter)
        if (arrayListJiraFixVersions.isNotEmpty()) {
            if (sharedPref.getString("jira_fix_versions", null) != null) {
                autoTextViewJiraFixVersions.setText(
                    sharedPref.getString("jira_fix_versions", null),
                    false
                )
            }
//                autoTextViewFixVersions.setText(arrayListFixVersions[0], false)
        }
        autoTextViewJiraFixVersions.setOnTouchListener { v, event ->
            autoTextViewJiraFixVersions.showDropDown()
            false
        }
        autoTextViewJiraFixVersions.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setFixVersionsPosition(fixVersionsPosition = position)
            hideKeyboard(activity = activity, view = viewJira)
        }
        this.arrayListJiraFixVersions = arrayListJiraFixVersions
    }

    /**
     * This method is used for initializing component autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraComponent is used for getting the component list for component autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeJiraComponent(
        arrayListJiraComponent: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraComponentAdapter = AutoCompleteTextViewJiraComponentAdapter(
            this,
            R.layout.auto_text_view_jira_component_item,
            arrayListJiraComponent
        )
        autoTextViewJiraComponent.setAdapter(autoTextViewJiraComponentAdapter)
        if (arrayListJiraComponent.isNotEmpty()) {
            if (sharedPref.getString("jira_component", null) != null) {
                autoTextViewJiraComponent.setText(
                    sharedPref.getString("jira_component", null),
                    false
                )
            }
//                autoTextViewComponent.setText(arrayListComponent[0], false)
        }
        autoTextViewJiraComponent.setOnTouchListener { v, event ->
            autoTextViewJiraComponent.showDropDown()
            false
        }
        autoTextViewJiraComponent.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setComponentPosition(componentPosition = position)
        }
        this.arrayListJiraComponent = arrayListJiraComponent
    }

    /**
     * This method is used for initializing priority autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraPriority is used for getting the priority list for priority autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeJiraPriority(
        arrayListJiraPriority: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraPriorityAdapter = AutoCompleteTextViewJiraPriorityAdapter(
            this,
            R.layout.auto_text_view_jira_priority_item,
            arrayListJiraPriority
        )
        autoTextViewJiraPriority.setAdapter(autoTextViewJiraPriorityAdapter)
        if (arrayListJiraPriority.isNotEmpty()) {
            if (sharedPref.getString("jira_priority", null) != null) {
                autoTextViewJiraPriority.setText(
                    sharedPref.getString("jira_priority", null),
                    false
                )
            } else {
                autoTextViewJiraPriority.setText(arrayListJiraPriority[0], false)
            }
        }
        autoTextViewJiraPriority.setOnTouchListener { v, event ->
            autoTextViewJiraPriority.showDropDown()
            false
        }
        autoTextViewJiraPriority.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setPriorityPosition(priorityPosition = position)
            hideKeyboard(activity = activity, view = viewJira)
        }
        autoTextViewJiraPriority.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListJiraPriority.contains(autoTextViewJiraPriority.editableText.toString())) {
                    if (arrayListJiraPriority.isNotEmpty()) {
                        if (sharedPref.getString("jira_priority", null) != null) {
                            autoTextViewJiraPriority.setText(
                                sharedPref.getString(
                                    "jira_priority",
                                    null
                                ), false
                            )
                        } else {
                            autoTextViewJiraPriority.setText(arrayListJiraPriority[0], false)
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing assignee autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeJiraAssignee(
        arrayListJiraAssignee: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraAssigneeAdapter = AutoCompleteTextViewJiraAssigneeAdapter(
            this,
            R.layout.auto_text_view_jira_assignee_item,
            arrayListJiraAssignee
        )
        autoTextViewJiraAssignee.setAdapter(autoTextViewJiraAssigneeAdapter)
        if (arrayListJiraAssignee.isNotEmpty()) {
            if (sharedPref.getString("jira_assignee", null) != null) {
                autoTextViewJiraAssignee.setText(
                    sharedPref.getString("jira_assignee", null),
                    false
                )
            }
//                autoTextViewAssignee.setText(arrayListAssignee[0], false)
        }
        autoTextViewJiraAssignee.setOnTouchListener { v, event ->
            autoTextViewJiraAssignee.showDropDown()
            false
        }
        autoTextViewJiraAssignee.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setAssigneePosition(assigneePosition = position)
            hideKeyboard(activity = activity, view = viewJira)
        }
    }

    /**
     * This method is used for initializing issues autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraIssues is used for getting the issues list for issues autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeJiraIssues(
        arrayListJiraIssues: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraIssueAdapter = AutoCompleteTextViewJiraIssueAdapter(
            this,
            R.layout.auto_text_view_jira_issue_item,
            arrayListJiraIssues
        )
        autoTextViewJiraIssue.setAdapter(autoTextViewJiraIssueAdapter)
        if (arrayListJiraIssues.isNotEmpty()) {
            if (sharedPref.getString("jira_issue", null) != null) {
                autoTextViewJiraIssue.setText(sharedPref.getString("jira_issue", null), false)
            }
//                autoTextViewIssue.setText(arrayListIssues[0], false)
        }
        autoTextViewJiraIssue.setOnTouchListener { v, event ->
            autoTextViewJiraIssue.showDropDown()
            false
        }
        autoTextViewJiraIssue.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewJira)
        }
        this.arrayListJiraIssue = arrayListJiraIssues
    }

    /**
     * This method is used for initializing linked issues autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraLinkedIssues is used for getting the linked issues list for linked issues autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeJiraLinkedIssues(
        arrayListJiraLinkedIssues: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraLinkedIssueAdapter = AutoCompleteTextViewJiraLinkedIssueAdapter(
            this,
            R.layout.auto_text_view_jira_linked_issue_item,
            arrayListJiraLinkedIssues
        )
        autoTextViewJiraLinkedIssue.setAdapter(autoTextViewJiraLinkedIssueAdapter)
        if (arrayListJiraLinkedIssues.isNotEmpty()) {
            if (sharedPref.getString("jira_linked_issue", null) != null) {
                autoTextViewJiraLinkedIssue.setText(
                    sharedPref.getString("jira_linked_issue", null),
                    false
                )
            } else {
                autoTextViewJiraLinkedIssue.setText(arrayListJiraLinkedIssues[0], false)
            }
        }
        autoTextViewJiraLinkedIssue.setOnTouchListener { v, event ->
            autoTextViewJiraLinkedIssue.showDropDown()
            false
        }
        autoTextViewJiraLinkedIssue.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setLinkedIssueTypePosition(linkedIssueTypePosition = position)
            hideKeyboard(activity = activity, view = viewJira)
        }
        autoTextViewJiraLinkedIssue.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListJiraLinkedIssues.contains(autoTextViewJiraLinkedIssue.editableText.toString())) {
                    if (arrayListJiraLinkedIssues.isNotEmpty()) {
                        if (sharedPref.getString("jira_linked_issue", null) != null) {
                            autoTextViewJiraLinkedIssue.setText(
                                sharedPref.getString(
                                    "jira_linked_issue",
                                    null
                                ), false
                            )
                        } else {
                            autoTextViewJiraLinkedIssue.setText(arrayListJiraLinkedIssues[0], false)
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing reporter autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraReporterNames is used for getting the reporter list for reporter autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeJiraReporter(
        arrayListJiraReporterNames: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraReporterAdapter = AutoCompleteTextViewJiraReporterAdapter(
            this,
            R.layout.auto_text_view_jira_reporter_item,
            arrayListJiraReporterNames
        )
        autoTextViewJiraReporter.setAdapter(autoTextViewJiraReporterAdapter)
        if (arrayListJiraReporterNames.isNotEmpty()) {
            if (sharedPref.getString("jira_reporter", null) != null) {
                autoTextViewJiraReporter.setText(
                    sharedPref.getString("jira_reporter", null),
                    false
                )
            }
//                autoTextViewReporter.setText(arrayListReporterNames[0], false)
        }
        autoTextViewJiraReporter.setOnTouchListener { v, event ->
            autoTextViewJiraReporter.showDropDown()
            false
        }
        autoTextViewJiraReporter.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setReporterPosition(reporterPosition = position)
            hideKeyboard(activity = activity, view = viewJira)
        }
    }


    /**
     * This method is used for initializing issue type autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraIssueTypes is used for getting the issue type list for issue type autoCompleteTextView.
     * @param arrayListJiraEpicName is used for getting the epic list.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeJiraIssueType(
        arrayListJiraIssueTypes: ArrayList<String>,
        arrayListJiraEpicName: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraIssueTypeAdapter = AutoCompleteTextViewJiraIssueTypeAdapter(
            this,
            R.layout.auto_text_view_jira_issue_type_item,
            arrayListJiraIssueTypes
        )
        autoTextViewJiraIssueType.setAdapter(autoTextViewJiraIssueTypeAdapter)
        if (checkUnhandledFilePath()) {
            autoTextViewJiraIssueType.setText(arrayListJiraIssueTypes[2], false)
            jiraAuthentication.setIssueTypePosition(issueTypePosition = 2)
        } else {
            if (arrayListJiraIssueTypes.isNotEmpty()) {
                if (sharedPref.getString("jira_issue_type", null) != null) {
                    autoTextViewJiraIssueType.setText(
                        sharedPref.getString("jira_issue_type", null),
                        false
                    )
                } else {
                    autoTextViewJiraIssueType.setText(arrayListJiraIssueTypes[0], false)
                }
            }
        }
        autoTextViewJiraIssueType.setOnTouchListener { v, event ->
            autoTextViewJiraIssueType.showDropDown()
            false
        }
        autoTextViewJiraIssueType.setOnItemClickListener { parent, view, position, id ->
            jiraAuthentication.setIssueTypePosition(issueTypePosition = position)
            hideKeyboard(activity = activity, view = viewJira)
            initializeJiraEpicName(
                arrayListJiraEpicName = arrayListJiraEpicName,
                sharedPref = sharedPref
            )
        }
        autoTextViewJiraIssueType.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListJiraIssueTypes.contains(autoTextViewJiraIssueType.editableText.toString())) {
                    if (arrayListJiraIssueTypes.isNotEmpty()) {
                        if (sharedPref.getString("jira_issue_type", null) != null) {
                            autoTextViewJiraIssueType.setText(
                                sharedPref.getString(
                                    "jira_issue_type",
                                    null
                                ), false
                            )
                        } else {
                            autoTextViewJiraIssueType.setText(arrayListJiraIssueTypes[0], false)
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing project autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraProjectNames is used for getting the project list for project autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeJiraProjectName(
        arrayListJiraProjectNames: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraProjectAdapter = AutoCompleteTextViewJiraProjectAdapter(
            this,
            R.layout.auto_text_view_jira_project_item,
            arrayListJiraProjectNames
        )
        autoTextViewJiraProject.setAdapter(autoTextViewJiraProjectAdapter)
        if (arrayListJiraProjectNames.isNotEmpty() && autoTextViewJiraProject.text.isEmpty()) {
            if (sharedPref.getString("jira_project", null) != null) {
                autoTextViewJiraProject.setText(
                    sharedPref.getString("jira_project", null),
                    false
                )
            } else {
                autoTextViewJiraProject.setText(arrayListJiraProjectNames[0], false)
            }
        }
        autoTextViewJiraProject.setOnTouchListener { v, event ->
            autoTextViewJiraProject.showDropDown()
            false
        }
        autoTextViewJiraProject.setOnItemClickListener { parent, view, position, id ->
            projectJiraPosition = position
            controlProjectJiraPosition = true
            jiraAuthentication.setProjectPosition(projectPosition = position)
            attachProgressBar(task = "jira")
            hideKeyboard(activity = activity, view = viewJira)
            jiraAuthentication.callJira(
                context = context,
                activity = activity,
                task = "get",
                createMethod = "normal"
            )
        }
        autoTextViewJiraProject.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListJiraProjectNames.contains(autoTextViewJiraProject.editableText.toString())) {
                    if (arrayListJiraProjectNames.isNotEmpty()) {
                        if (sharedPref.getString("jira_project", null) != null) {
                            autoTextViewJiraProject.setText(
                                sharedPref.getString("jira_project", null),
                                false
                            )
                        } else {
                            autoTextViewJiraProject.setText(arrayListJiraProjectNames[0], false)
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing epic autoCompleteTextView in the loggerbird_jira_popup.
     * @param arrayListJiraEpicName is used for getting the epic list for epic autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    private fun initializeJiraEpicName(
        arrayListJiraEpicName: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        if (autoTextViewJiraIssueType.editableText.toString() == "Epic") {
            cardViewJiraEpicName.visibility = View.VISIBLE
            cardViewJiraEpicLink.visibility = View.GONE
            autoTextViewJiraEpicNameAdapter = AutoCompleteTextViewJiraEpicNameAdapter(
                this,
                R.layout.auto_text_view_jira_epic_name_item,
                arrayListJiraEpicName
            )
            autoTextViewJiraEpicName.setAdapter(autoTextViewJiraEpicNameAdapter)
            if (arrayListJiraEpicName.isNotEmpty()) {
                if (sharedPref.getString("jira_epic_name", null) != null) {
                    autoTextViewJiraEpicName.setText(
                        sharedPref.getString("jira_epic_name", null),
                        false
                    )
                } else {
                    autoTextViewJiraEpicName.setText(arrayListJiraEpicName[0], false)
                }
            }
            autoTextViewJiraEpicName.setOnTouchListener { v, event ->
                autoTextViewJiraEpicName.showDropDown()
                false
            }
            autoTextViewJiraEpicName.setOnItemClickListener { parentEpic, viewEpic, positionEpic, idEpic ->
                jiraAuthentication.setEpicNamePosition(epicNamePosition = positionEpic)
                hideKeyboard(activity = activity, view = viewJira)
            }
            autoTextViewJiraEpicName.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus && autoTextViewJiraEpicName.text.toString().isEmpty()) {
                    if (!arrayListJiraEpicName.contains(autoTextViewJiraEpicName.editableText.toString())) {
                        if (arrayListJiraEpicName.isNotEmpty()) {
                            if (sharedPref.getString("jira_epic_name", null) != null) {
                                autoTextViewJiraEpicName.setText(
                                    sharedPref.getString(
                                        "jira_epic_name",
                                        null
                                    ), false
                                )
                            } else {
                                autoTextViewJiraEpicName.setText(arrayListJiraEpicName[0], false)
                            }
                        }
                    }
                }
            }
        } else {
            cardViewJiraEpicName.visibility = View.GONE
            cardViewJiraEpicLink.visibility = View.VISIBLE
        }
    }

    /**
     * This method is used for initializing components of jira_calendar_view.
     */
    private fun initializeJiraStartDatePicker() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        var startDate = "$mYear-$mMonth-$mDayOfMonth"
        calendarViewJiraLayout =
            calendarViewJiraView.findViewById(R.id.jira_calendar_view_layout)
        calendarViewJiraStartDate = calendarViewJiraView.findViewById(R.id.calendarView_start_date)
        buttonCalendarViewJiraCancel =
            calendarViewJiraView.findViewById(R.id.button_jira_calendar_cancel)
        buttonCalendarViewJiraOk =
            calendarViewJiraView.findViewById(R.id.button_jira_calendar_ok)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            calendarViewJiraStartDate.minDate = System.currentTimeMillis()
        }
        if (calendarViewJiraDate != null) {
            calendarViewJiraStartDate.setDate(calendarViewJiraDate!!, true, true)
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
            imageButtonRemoveDate.visibility = View.VISIBLE
        }
        calendarViewJiraStartDate.setOnDateChangeListener { viewStartDate, year, month, dayOfMonth ->
            calendarViewJiraDate = viewStartDate.date
            val tempMonth = month + 1
            startDate = "$year-$tempMonth-$dayOfMonth"
        }

    }

    /**
     * This method is used for creating custom jira date-picker layout which is attached to application overlay.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun attachJiraDatePicker() {
        try {
            val rootView: ViewGroup =
                activity.window.decorView.findViewById(android.R.id.content)
            calendarViewJiraView =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    LayoutInflater.from(activity)
                        .inflate(R.layout.jira_calendar_view, rootView, false)
                } else {
                    LayoutInflater.from(activity)
                        .inflate(R.layout.jira_calendar_view_lower, rootView, false)
                }
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
            initializeJiraStartDatePicker()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraDatePopupTag)
        }
    }

    /**
     * This method is used for removing jira_calendar_view from window.
     */
    private fun detachJiraDatePicker() {
        if (this::calendarViewJiraView.isInitialized) {
            (windowManagerJiraDatePicker as WindowManager).removeViewImmediate(
                calendarViewJiraView
            )
        }
    }

    //Slack
    /**
     * This method is used for creating slack layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeSlackLayout(filePathMedia: File) {
        try {
            if (windowManagerSlack != null && this::viewSlack.isInitialized) {
                (windowManagerSlack as WindowManager).removeViewImmediate(viewSlack)
                arrayListSlackFileName.clear()
            }
            viewSlack = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_slack_popup, (this.rootView as ViewGroup), false)
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

                activity.window.navigationBarColor =
                    ContextCompat.getColor(this, R.color.black)
                activity.window.statusBarColor = ContextCompat.getColor(this, R.color.black)

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
                toolbarSlack = viewSlack.findViewById(R.id.toolbar_slack)
                slackBottomNavigationView =
                    viewSlack.findViewById(R.id.slack_bottom_nav_view)

                slackAuthentication.callSlack(
                    context = context,
                    activity = activity,
                    filePathMedia = filePathMedia,
                    slackTask = "get"
                )
                initializeSlackRecyclerView(filePathMedia = filePathMedia)
                buttonClicksSlack(filePathMedia)
                attachProgressBar(task = "slack")

            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.slackTag)
        }
    }

    /**
     * This method is used for removing loggerbird_slack_popup from window.
     */
    private fun removeSlackLayout() {
        if (windowManagerSlack != null && this::viewSlack.isInitialized) {
            (windowManagerSlack as WindowManager).removeViewImmediate(viewSlack)
            windowManagerSlack = null
            arrayListSlackFileName.clear()
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_slack_popup.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun buttonClicksSlack(filePathMedia: File) {
        buttonSlackCreate.setSafeOnClickListener {
            slackAuthentication.gatherSlackChannelSpinnerDetails(
                spinnerChannel = spinnerChannels
            )
            slackAuthentication.gatherSlackEditTextDetails(editTextMessage = editTextMessage)
            slackAuthentication.gatherSlackRecyclerViewDetails(arrayListRecyclerViewItems = arrayListSlackFileName)
            if (slackAuthentication.checkMessageEmpty(activity = activity, context = context)) {
                attachProgressBar(task = "slack")
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
                attachProgressBar(task = "slack")
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
                    attachProgressBar(task = "slack")
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    /**
     * This method is used for initializing slack attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    private fun initializeSlackRecyclerView(filePathMedia: File) {
        recyclerViewSlackAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewSlackAttachmentUser.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        if (filePathMedia.exists()) {
            slackAttachmentAdapter =
                RecyclerViewSlackAttachmentAdapter(
                    addSlackFileNames(filePathMedia = filePathMedia),
                    context = context,
                    activity = activity,
                    rootView = rootView
                )
        }

        recyclerViewSlackAttachment.adapter = slackAttachmentAdapter
        recyclerViewSlackAttachmentUser.adapter = slackAttachmentAdapter

    }

    /**
     * This method is used for adding files to slack file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    private fun addSlackFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        arrayListSlackFileName.add(RecyclerViewModel(file = filePathMedia))
        if (unhandledMediaFilePath != null) {
            arrayListSlackFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        arrayListSlackFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        return arrayListSlackFileName
    }

    /**
     * This method is used for initializing spinners in the loggerbird_slack_popup.
     * @param arrayListSlackChannels is used for getting the channels list for channels autoCompleteTextView.
     * @param arrayListSlackUsers is used for getting the users list for users autoCompleteTextView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun initializeSlackSpinner(
        arrayListSlackChannels: ArrayList<String>,
        arrayListSlackUsers: ArrayList<String>
    ) {

        spinnerChannelsAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListSlackChannels)
        spinnerChannelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerChannels.adapter = spinnerChannelsAdapter

        spinnerUsersAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListSlackUsers)
        spinnerUsersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUsers.adapter = spinnerUsersAdapter

        detachProgressBar()
    }

    /**
     * This method is used for hiding the keyboard from the window.
     */
    private fun hideKeyboard(activity: Activity, view: View) {
        val inputMethodManager =
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * This method is used for gathering unhandled loggerbird.exception details.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun gatherUnhandledExceptionDetails() {
        try {
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            val filePath = File(sharedPref.getString("unhandled_file_path", null)!!)
            initializeLoggerBirdUnhandledExceptionPopup(
                activity = this.activity,
                sharedPref = sharedPref,
                filePath = filePath
            )
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

    /**
     * This method is used for checking the file exist when sending an unhandled action.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun checkUnhandledFilePath(): Boolean {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        if (sharedPref.getString("unhandled_file_path", null) != null) {
//            val filepath = File(sharedPref.getString("unhandled_file_path", null)!!)
//            if (filepath.exists()) {
//                return true
//            }
//            else {
//                activity.runOnUiThread {
//                    defaultToast.attachToast(
//                        activity = activity,
//                        toastMessage = activity.resources.getString(R.string.unhandled_file_doesnt_exist)
//                    )
//                }
//            }
            if (sharedPref.getString("unhandled_media_file_path", null) != null) {
                unhandledMediaFilePath = sharedPref.getString("unhandled_media_file_path", null)
            }
            return true
        }
        return false
    }

    /**
     * This method is used for if duplication checkbox is checked or not for enabling the duplication check.
     * @return Boolean value.
     */
    private fun checkBoxUnhandledChecked(): Boolean {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        if (sharedPref.getBoolean("duplication_enabled", false)) {
            return true
        }
        return false
    }

    /**
     * This method is used after an successful unhandled loggerbird.exception jira issue opened.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun unhandledExceptionCustomizeJiraIssueSent() {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.remove("unhandled_file_path")
        editor.apply()
//        CookieBar.dismiss(activity)
        activity.runOnUiThread {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.jira_sent)
            )
        }
    }

    /**
     * This method is used for adding unhandled loggerbird.exception message.
     * @param context is for getting reference from the application context.
     * @param unhandledExceptionMessage is for getting reference of the unhandled loggerbird.exception message.
     */
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
    //Unhandled duplication
    /**
     * This method is used for creating unhandled duplcation layout which is attached to application overlay.
     * @param filePath is used for getting the reference of current  file.
     */
    internal fun attachUnhandledDuplicationLayout(
        filePath: File,
        field: String
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
            filePath = filePath,
            field = field
        )
    }


    /**
     * This method is used for removing unhandled_duplication_popup from window.
     */
    private fun detachUnhandledDuplicationLayout() {
        if (this::viewUnhandledDuplication.isInitialized) {
            (windowManagerUnhandledDuplication as WindowManager).removeViewImmediate(
                viewUnhandledDuplication
            )
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the unhandled_duplication_popup.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeUnhandledDuplicationButtons(
        filePath: File,
        field: String
    ) {
        val buttonProceed =
            viewUnhandledDuplication.findViewById<Button>(R.id.button_unhandled_duplication_proceed)
        val buttonCancel =
            viewUnhandledDuplication.findViewById<Button>(R.id.button_unhandled_duplication_cancel)
        buttonProceed.setSafeOnClickListener {
            detachUnhandledDuplicationLayout()
            when (field) {
                "email" -> initializeEmailLayout(filePathMedia = filePath)
                "jira" -> initializeJiraLayout(filePathMedia = filePath)
                "slack" -> initializeSlackLayout(filePathMedia = filePath)
                "github" -> initializeGithubLayout(filePathMedia = filePath)
                "gitlab" -> initializeGitlabLayout(filePathMedia = filePath)
                "trello" -> initializeTrelloLayout(filePathMedia = filePath)
                "pivotal" -> initializePivotalLayout(filePathMedia = filePath)
                "basecamp" -> initializeBasecampLayout(filePathMedia = filePath)
                "asana" -> initializeAsanaLayout(filePathMedia = filePath)
                "clubhouse" -> initializeClubhouseLayout(filePathMedia = filePath)
                "bitbucket" -> initializeBitbucketLayout(filePathMedia = filePath)
            }
        }
        buttonCancel.setSafeOnClickListener {
            detachUnhandledDuplicationLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * This method is used for controlling the media file actions.
     * @return Boolean values.
     */
    private fun controlMediaFile(): Boolean {
        if (filePathVideo != null) {
            return if (filePathVideo!!.exists()) {
                filePathVideo!!.delete()
                true
            } else {
                false
            }
        }
        if (filePathAudio != null) {
            return if (filePathAudio!!.exists()) {
                filePathAudio!!.delete()
                true
            } else {
                false
            }
        }
        if (PaintView.controlScreenShotFile()) {
            return if (PaintView.filePathScreenShot!!.exists()) {
                PaintView.filePathScreenShot!!.delete()
                true
            } else {
                false
            }
        }
        return false
    }

    //Email
    /**
     * This method is used for creating email layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
        imageViewEmailClear = viewEmail.findViewById(R.id.imageView_email_clear)
        editTextEmailTo = viewEmail.findViewById(R.id.editText_email_to)
        editTextEmailSubject = viewEmail.findViewById(R.id.editText_email_subject)
        editTextEmailContent = viewEmail.findViewById(R.id.editText_email_message)
        toolbarEmail = viewEmail.findViewById(R.id.toolbar_email)
        recyclerViewEmailAttachment =
            viewEmail.findViewById(R.id.recycler_view_email_attachment)
        recyclerViewEmailToList = viewEmail.findViewById(R.id.recycler_view_email_to_list)
        cardViewEmailToList = viewEmail.findViewById(R.id.cardView_to_list)
        initializeEmailAttachmentRecyclerView(filePathMedia = filePathMedia)
        initializeEmailToRecyclerView()
        initializeEmailButtons()
    }


    /**
     * This method is used for removing loggerbird_email_popup from window.
     */
    internal fun removeEmailLayout() {
        if (this::viewEmail.isInitialized && windowManagerEmail != null) {
            (windowManagerEmail as WindowManager).removeViewImmediate(
                viewEmail
            )
            windowManagerEmail = null
            arrayListEmailFileName.clear()
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_email_popup.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeEmailButtons() {
        try {
            buttonEmailCreate.setSafeOnClickListener {
                if (checkBoxFutureTask.isChecked && checkEmailFormat(editTextEmailTo.text.toString())) {
                    attachProgressBar(task = "email")
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
                            callEmail(to = it.email)
                        }
                    } else {
                        if (checkEmailFormat(editTextEmailTo.text.toString())) {
                            callEmail(
                                to = editTextEmailTo.text.toString()
                            )
                        }
                    }
                }
            }
            buttonEmailCancel.setSafeOnClickListener {
                removeEmailLayout()
            }
            imageViewEmailAdd.setSafeOnClickListener {
                hideKeyboard(activity = activity, view = viewEmail)
                if (checkEmailFormat(editTextEmailTo.text.toString())) {
                    cardViewEmailToList.visibility = View.VISIBLE
                    addEmailToUser(email = editTextEmailTo.text.toString())
                }
            }
            imageViewEmailClear.setSafeOnClickListener {
                hideKeyboard(activity = activity, view = viewEmail)
                editTextEmailTo.text = null
                imageViewEmailClear.visibility = View.GONE
            }
            toolbarEmail.setNavigationOnClickListener {
                removeEmailLayout()
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.VISIBLE
                }
            }
            editTextEmailTo.addTextChangedListener {
                if (editTextEmailTo.text.toString().isEmpty()) {
                    imageViewEmailClear.visibility = View.GONE
                } else {
                    imageViewEmailClear.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callEnqueueEmail()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)

        }
    }

    /**
     * This method is used for sending email in a future time.
     */
    private fun createFutureTaskEmail() {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        with(sharedPref.edit()) {
            if (arraylistEmailToUsername.isNotEmpty()) {
                addFutureUserList()
            } else {
                putString("future_task_email_to", editTextEmailTo.text.toString())
            }
            putString("future_task_email_subject", editTextEmailSubject.text.toString())
            putString("future_task_email_message", editTextEmailContent.text.toString())
//            putString("future_task_email_file", filePathMedia.absolutePath)
            commit()
        }
        addFutureFileList()
        val intentServiceFuture =
            Intent(context, LoggerBirdFutureTaskService::class.java)
        context.startForegroundService(intentServiceFuture)
        controlFutureTask = true
    }

    /**
     * This method is used for sending email with attachment.
     * @param filePathMedia is used for getting the reference of current media file.
     * @param to is used for getting reference of email address.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun createEmailTask(filePathMedia: File? = null, to: String) {
        try {
            activity.runOnUiThread {
                attachProgressBar(task = "email")
            }
            sendSingleMediaFile(
                filePathMedia = filePathMedia,
                to = to,
                subject = editTextEmailSubject.text.toString(),
                message = editTextEmailContent.text.toString()
            )

        } catch (e: Exception) {
            e.printStackTrace()
            resetEnqueueEmail()
            detachProgressBar()
            removeEmailLayout()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
        }
    }


    /**
     * This method is used for initializing email attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeEmailAttachmentRecyclerView(filePathMedia: File) {
        recyclerViewEmailAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        emailAttachmentAdapter =
            RecyclerViewEmailAttachmentAdapter(
                addEmailFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewEmailAttachment.adapter = emailAttachmentAdapter
    }


    /**
     * This method is used for initializing email to recyclerView.
     */
    private fun initializeEmailToRecyclerView() {
        recyclerViewEmailToList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        emailToListAttachmentAdapter =
            RecyclerViewEmaiToListAttachmentAdapter(
                arraylistEmailToUsername,
                cardView = cardViewEmailToList,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewEmailToList.adapter = emailToListAttachmentAdapter
    }

    /**
     * This method is used for adding email to email user list.
     * @param email is used for getting the reference of current user email address.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addEmailToUser(email: String) {
        if (!arraylistEmailToUsername.contains(RecyclerViewModelTo(email = email))) {
            arraylistEmailToUsername.add(RecyclerViewModelTo(email = email))
            emailToListAttachmentAdapter.notifyDataSetChanged()
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.email_to_duplication)
            )
        }

    }

    /**
     * This method is used for adding files to email file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addEmailFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListEmailFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListEmailFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListEmailFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListEmailFileName
    }

    /**
     * This method is used for that are in the correct format.
     * @return Boolean value.
     */
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

    //not used!
    private fun checkBoxFutureTaskIsChecked(filePathMedia: File): Boolean {
        if (checkBoxFutureTask.isChecked) {
            initializeFutureTaskLayout(filePathMedia = filePathMedia)
            return true
        }
        return false
    }
    //Future task
    /**
     * This method is used for creating future task layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     */
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

    /**
     * This method is used for removing loggerbird_future_task_popup from window.
     */
    private fun removeFutureLayout() {
        if (this::viewFutureTask.isInitialized && windowManagerFutureTask != null) {
            (windowManagerFutureTask as WindowManager).removeViewImmediate(
                viewFutureTask
            )
            windowManagerFutureTask = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_future_task_popup.
     */
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
            checkFutureDateAndTimeEmpty(filePathMedia = filePathMedia)
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

    /**
     * This method is used for creating future date layout which is attached to application overlay.
     */
    private fun initializeFutureDateLayout() {
        removeFutureDateLayout()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewFutureDate =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LayoutInflater.from(activity)
                    .inflate(R.layout.future_calendar_view, rootView, false)
            } else {
                LayoutInflater.from(activity)
                    .inflate(R.layout.future_calendar_view_lower, rootView, false)
            }
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
        frameLayoutFutureDate = viewFutureDate.findViewById(R.id.future_calendar_view_layout)
        calendarViewFutureTask = viewFutureDate.findViewById(R.id.calendarView_start_date)
        buttonFutureTaskDateCreate = viewFutureDate.findViewById(R.id.button_future_calendar_ok)
        buttonFutureTaskDateCancel = viewFutureDate.findViewById(R.id.button_future_calendar_cancel)

        buttonClicksFutureDate()

    }

    /**
     * This method is used for removing future_calendar_view from window.
     */
    private fun removeFutureDateLayout() {
        if (this::viewFutureDate.isInitialized && windowManagerFutureDate != null) {
            (windowManagerFutureDate as WindowManager).removeViewImmediate(
                viewFutureDate
            )
            windowManagerFutureDate = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the future_calendar_view.
     */
    private fun buttonClicksFutureDate() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        calendarFuture.set(mYear, mMonth, mDayOfMonth)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            calendarViewFutureTask.minDate = System.currentTimeMillis()
        }
        frameLayoutFutureDate.setOnClickListener {
            removeFutureDateLayout()
        }
        calendarViewFutureTask.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendarFuture.set(year, month + 1, dayOfMonth)
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

    /**
     * This method is used for creating future time layout which is attached to application overlay.
     */
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
        frameLayoutFutureTime = viewFutureTime.findViewById(R.id.future_time_view_layout)
        timePickerFutureTask = viewFutureTime.findViewById(R.id.timePicker_start_time)
        buttonFutureTaskTimeCreate = viewFutureTime.findViewById(R.id.button_future_time_ok)
        buttonFutureTaskTimeCancel = viewFutureTime.findViewById(R.id.button_future_time_cancel)

        buttonClicksFutureTime()

    }

    /**
     * This method is used for removing future_time_picker from window.
     */
    private fun removeFutureTimeLayout() {
        if (this::viewFutureTime.isInitialized && windowManagerFutureTime != null) {
            (windowManagerFutureTime as WindowManager).removeViewImmediate(
                viewFutureTime
            )
            windowManagerFutureTime = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the future_time_picker.
     */
    private fun buttonClicksFutureTime() {
        frameLayoutFutureTime.setOnClickListener {
            removeFutureTimeLayout()
        }
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


    /**
     * This method is used for start date and start time is not null in future task layout.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    private fun checkFutureDateAndTimeEmpty(filePathMedia: File) {
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

    //This method is used for adding selected files in email layout for future task.
    private fun addFutureFileList() {
        val arrayListFileNames: ArrayList<String> = ArrayList()
        RecyclerViewEmailAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
            if (it.file.name == "logger_bird_details.txt") {
                val futureLoggerBirdFile = File(context.filesDir, "logger_bird_details_future.txt")
                if (!futureLoggerBirdFile.exists()) {
                    futureLoggerBirdFile.createNewFile()
                } else {
                    futureLoggerBirdFile.delete()
                    futureLoggerBirdFile.createNewFile()
                }
                val scanner = Scanner(it.file)
                do {
                    futureLoggerBirdFile.appendText(scanner.nextLine() + "\n")
                } while (scanner.hasNextLine())
                arrayListFileNames.add(futureLoggerBirdFile.absolutePath)
            } else {
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

    //This method is used for adding selected users in email layout for future task.
    private fun addFutureUserList() {
        val arrayListUsers: ArrayList<String> = ArrayList()
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

    //Github
    /**
     * This method is used for creating github layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGithubLayout(filePathMedia: File) {
        try {
            removeGithubLayout()
            viewGithub = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_github_popup, (this.rootView as ViewGroup), false)
            windowManagerParamsGithub = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            windowManagerGithub = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerGithub as WindowManager).addView(
                viewGithub,
                windowManagerParamsGithub
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.black)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.black)

            buttonGithubCreate = viewGithub.findViewById(R.id.button_github_create)
            buttonGithubCancel = viewGithub.findViewById(R.id.button_github_cancel)
            autoTextViewGithubAssignee =
                viewGithub.findViewById(R.id.auto_textView_github_assignee)
            autoTextViewGithubLabels = viewGithub.findViewById(R.id.auto_textView_github_labels)
            autoTextViewGithubLinkedRequests =
                viewGithub.findViewById(R.id.auto_textView_github_linked_requests)
            autoTextViewGithubMileStone =
                viewGithub.findViewById(R.id.auto_textView_github_milestone)
            autoTextViewGithubRepo =
                viewGithub.findViewById(R.id.auto_textView_github_repo)
            autoTextViewGithubProject =
                viewGithub.findViewById(R.id.auto_textView_github_project)
            editTextGithubTitle = viewGithub.findViewById(R.id.editText_github_title)
            editTextGithubComment = viewGithub.findViewById(R.id.editText_github_comment)
            recyclerViewGithubAttachment =
                viewGithub.findViewById(R.id.recycler_view_github_attachment)
            toolbarGithub = viewGithub.findViewById(R.id.toolbar_github)
            scrollViewGithub = viewGithub.findViewById(R.id.scrollView_github)
            scrollViewGithub.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard(activity = activity, view = viewGithub)
                }
                return@setOnTouchListener false
            }
            recyclerViewGithubAssignee =
                viewGithub.findViewById(R.id.recycler_view_assignee_list)
            cardViewGithubAssigneeList = viewGithub.findViewById(R.id.cardView_assignee_list)
            imageViewGithubAssignee = viewGithub.findViewById(R.id.imageView_assignee_add)

            recyclerViewGithubLabel = viewGithub.findViewById(R.id.recycler_view_label_list)
            cardViewGithubLabelList = viewGithub.findViewById(R.id.cardView_label_list)
            imageViewGithubLabel = viewGithub.findViewById(R.id.imageView_label_add)

            recyclerViewGithubProject = viewGithub.findViewById(R.id.recycler_view_project_list)
            cardViewGithubProjectList = viewGithub.findViewById(R.id.cardView_project_list)
            imageViewGithubProject = viewGithub.findViewById(R.id.imageView_project_add)

            toolbarGithub.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.github_menu_save -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        with(sharedPref.edit()) {
                            putString(
                                "github_repo",
                                autoTextViewGithubRepo.editableText.toString()
                            )
                            putString(
                                "github_project",
                                autoTextViewGithubProject.editableText.toString()
                            )
                            putString("github_title", editTextGithubTitle.text.toString())
                            putString("github_comment", editTextGithubComment.text.toString())
                            putString(
                                "github_assignee",
                                autoTextViewGithubAssignee.editableText.toString()
                            )
                            putString(
                                "github_labels",
                                autoTextViewGithubLabels.editableText.toString()
                            )
                            putString(
                                "github_milestone",
                                autoTextViewGithubMileStone.editableText.toString()
                            )
                            putString(
                                "github_pull_requests",
                                autoTextViewGithubLinkedRequests.editableText.toString()
                            )
                            commit()
                        }
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.github_issue_preferences_save)
                        )
                    }
                    R.id.github_menu_clear -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        val editor: SharedPreferences.Editor = sharedPref.edit()
                        editor.remove("github_comment")
                        editor.remove("github_title")
                        editor.remove("github_repo")
                        editor.remove("github_project")
                        editor.remove("github_milestone")
                        editor.remove("github_assignee")
                        editor.remove("github_labels")
                        editor.remove("github_pull_requests")
                        editor.apply()
                        clearGithubComponents()
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.github_issue_preferences_delete)
                        )
                    }
                }
                return@setOnMenuItemClickListener true
            }

            toolbarGithub.setNavigationOnClickListener {
                removeGithubLayout()
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.VISIBLE
                }
            }

            initializeGithubAttachmentRecyclerView(filePathMedia = filePathMedia)
            initializeGithubAssigneeRecyclerView()
            initializeGithubLabelRecyclerView()
            initializeGithubProjectRecyclerView()
            buttonClicksGithub(filePathMedia = filePathMedia)
            githubAuthentication.callGithub(
                activity = activity,
                context = context,
                task = "get",
                filePathMedia = filePathMedia
            )
            attachProgressBar(task = "github")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.githubTag)
        }
    }

    /**
     * This method is used for clearing github components.
     */
    private fun clearGithubComponents() {
        cardViewGithubAssigneeList.visibility = View.GONE
        cardViewGithubLabelList.visibility = View.GONE
        arrayListGithubAssigneeName.clear()
        arrayListGithubLabelName.clear()
        githubAssigneeAdapter.notifyDataSetChanged()
        githubLabelAdapter.notifyDataSetChanged()
        editTextGithubTitle.text = null
        editTextGithubComment.text = null
//        autoTextViewGithubRepo.setText("", false)
        autoTextViewGithubAssignee.setText("", false)
        autoTextViewGithubLabels.setText("", false)
        autoTextViewGithubMileStone.setText("", false)
        autoTextViewGithubLinkedRequests.setText("", false)
    }

    /**
     * This method is used for removing loggerbird_github_popup from window.
     */
    internal fun removeGithubLayout() {
        if (this::viewGithub.isInitialized && windowManagerGithub != null) {
            (windowManagerGithub as WindowManager).removeViewImmediate(
                viewGithub
            )
            windowManagerGithub = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_github_popup.
     */
    private fun buttonClicksGithub(filePathMedia: File) {
        buttonGithubCreate.setSafeOnClickListener {
            if (checkGithubTitleEmpty() && githubAuthentication.checkGithubRepoEmpty(
                    activity = activity,
                    autoTextViewRepo = autoTextViewGithubRepo
                ) && githubAuthentication.checkGithubAssignee(
                    activity = activity,
                    autoTextViewAssignee = autoTextViewGithubAssignee
                ) && githubAuthentication.checkGithubLabel(
                    activity = activity,
                    autoTextViewLabels = autoTextViewGithubLabels
                ) && githubAuthentication.checkGithubMileStone(
                    activity = activity,
                    autoTextViewMileStone = autoTextViewGithubMileStone
                ) && githubAuthentication.checkGithubLinkedPullRequest(
                    activity = activity,
                    autoTextViewLinkedPullRequest = autoTextViewGithubLinkedRequests
                )
            ) {
                attachProgressBar(task = "github")
                githubAuthentication.gatherAutoTextDetails(
                    autoTextViewAssignee = autoTextViewGithubAssignee,
                    autoTextViewRepos = autoTextViewGithubRepo,
                    autoTextViewProject = autoTextViewGithubProject,
                    autoTextViewLabels = autoTextViewGithubLabels,
                    autoTextViewLinkedRequests = autoTextViewGithubLinkedRequests,
                    autoTextViewMileStone = autoTextViewGithubMileStone
                )
                githubAuthentication.gatherEditTextDetails(
                    editTextComment = editTextGithubComment,
                    editTextTitle = editTextGithubTitle
                )
                githubAuthentication.callGithub(
                    activity = activity,
                    context = context,
                    task = "create",
                    filePathMedia = filePathMedia
                )
            }
        }
        buttonGithubCancel.setSafeOnClickListener {
            removeGithubLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
        imageViewGithubAssignee.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewGithub)
            if (!arrayListGithubAssigneeName.contains(
                    RecyclerViewModelAssignee(
                        autoTextViewGithubAssignee.editableText.toString()
                    )
                ) && arrayListGithubAssignee.contains(autoTextViewGithubAssignee.editableText.toString())
            ) {
                arrayListGithubAssigneeName.add(RecyclerViewModelAssignee(autoTextViewGithubAssignee.editableText.toString()))
                githubAssigneeAdapter.notifyDataSetChanged()
                cardViewGithubAssigneeList.visibility = View.VISIBLE
            } else if (arrayListGithubAssigneeName.contains(
                    RecyclerViewModelAssignee(
                        autoTextViewGithubAssignee.editableText.toString()
                    )
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.github_assignee_exist)
                )
            } else if (!arrayListGithubAssignee.contains(autoTextViewGithubAssignee.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.github_assignee_doesnt_exist)
                )
            }

        }
        imageViewGithubLabel.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewGithub)
            if (!arrayListGithubLabelName.contains(
                    RecyclerViewModelLabel(
                        autoTextViewGithubLabels.editableText.toString()
                    )
                ) && arrayListGithubLabel.contains(
                    autoTextViewGithubLabels.editableText.toString()
                )
            ) {
                arrayListGithubLabelName.add(RecyclerViewModelLabel(autoTextViewGithubLabels.editableText.toString()))
                githubLabelAdapter.notifyDataSetChanged()
                cardViewGithubLabelList.visibility = View.VISIBLE
            } else if (arrayListGithubLabelName.contains(
                    RecyclerViewModelLabel(autoTextViewGithubLabels.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.github_label_exist)
                )
            } else if (!arrayListGithubLabel.contains(autoTextViewGithubLabels.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.github_label_doesnt_exist)
                )
            }

        }
        imageViewGithubProject.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewGithub)
            if (!arrayListGithubProjectName.contains(
                    RecyclerViewModelProject(
                        autoTextViewGithubProject.editableText.toString()
                    )
                ) && arrayListGithubProject.contains(
                    autoTextViewGithubProject.editableText.toString()
                )
            ) {
                arrayListGithubProjectName.add(RecyclerViewModelProject(autoTextViewGithubProject.editableText.toString()))
                githubProjectAdapter.notifyDataSetChanged()
                cardViewGithubProjectList.visibility = View.VISIBLE
            } else if (arrayListGithubProjectName.contains(
                    RecyclerViewModelProject(autoTextViewGithubProject.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.github_project_exist)
                )
            } else if (!arrayListGithubProject.contains(autoTextViewGithubProject.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.github_project_doesnt_exist)
                )
            }

        }
    }

    /**
     * This method is used for initializing github attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGithubAttachmentRecyclerView(filePathMedia: File) {
        arrayListGithubFileName.clear()
        recyclerViewGithubAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        githubAttachmentAdapter =
            RecyclerViewGithubAttachmentAdapter(
                addGithubFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewGithubAttachment.adapter = githubAttachmentAdapter
    }

    /**
     * This method is used for adding files to github file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addGithubFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListGithubFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListGithubFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListGithubFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListGithubFileName
    }

    /**
     * This method is used for initializing github assignee recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGithubAssigneeRecyclerView() {
        arrayListGithubAssigneeName.clear()
        recyclerViewGithubAssignee.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        githubAssigneeAdapter =
            RecyclerViewGithubAssigneeAdapter(
                arrayListGithubAssigneeName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewGithubAssignee.adapter = githubAssigneeAdapter
    }

    /**
     * This method is used for initializing github label recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGithubLabelRecyclerView() {
        arrayListGithubLabelName.clear()
        recyclerViewGithubLabel.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        githubLabelAdapter =
            RecyclerViewGithubLabelAdapter(
                arrayListGithubLabelName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewGithubLabel.adapter = githubLabelAdapter
    }

    /**
     * This method is used for initializing github project recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGithubProjectRecyclerView() {
        arrayListGithubProjectName.clear()
        recyclerViewGithubProject.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        githubProjectAdapter =
            RecyclerViewGithubProjectAdapter(
                arrayListGithubProjectName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewGithubProject.adapter = githubProjectAdapter
    }

    /**
     * This method is used for title field is not empty in github layout.
     * @return Boolean value.
     */
    private fun checkGithubTitleEmpty(): Boolean {
        if (editTextGithubTitle.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.github_title_empty)
            )
        }
        return false
    }

    /**
     * This method is used for initializing  autoCompleteTextViews in the loggerbird_github_popup.
     * @param arrayListGithubRepos is used for getting the repository list for repository autoCompleteTextView.
     * @param arrayListGithubProject is used for getting the project list for project autoCompleteTextView.
     * @param arrayListGithubAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param arrayListGithubMileStones is used for getting the milestone list for milestone autoCompleteTextView.
     * @param arrayListGithubLinkedRequests is used for getting the linked request list for linked request autoCompleteTextView.
     * @param arrayListGithubLabels is used for getting the label list for label autoCompleteTextView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    internal fun initializeGithubAutoTextViews(
        arrayListGithubRepos: ArrayList<String>,
        arrayListGithubProject: ArrayList<String>,
        arrayListGithubAssignee: ArrayList<String>,
        arrayListGithubMileStones: ArrayList<String>,
        arrayListGithubLinkedRequests: ArrayList<String>,
        arrayListGithubLabels: ArrayList<String>
    ) {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        editTextGithubTitle.setText(sharedPref.getString("github_title", null))
        editTextGithubComment.setText(sharedPref.getString("github_comment", null))
        initializeGithubRepos(arrayListGithubRepos = arrayListGithubRepos, sharedPref = sharedPref)
        initializeGithubProject(
            arrayListGithubProject = arrayListGithubProject,
            sharedPref = sharedPref
        )
        initializeGithubAssignee(
            arrayListGithubAssignee = arrayListGithubAssignee,
            sharedPref = sharedPref
        )
        initializeGithubMileStones(
            arrayListGithubMileStones = arrayListGithubMileStones,
            sharedPref = sharedPref
        )
        initializeGithubLinkedRequests(
            arrayListGithubLinkedRequests = arrayListGithubLinkedRequests,
            sharedPref = sharedPref
        )
        initializeGithubLabels(
            arrayListGithubLabels = arrayListGithubLabels,
            sharedPref = sharedPref
        )
        this.arrayListGithubAssignee = arrayListGithubAssignee
        this.arrayListGithubLabel = arrayListGithubLabels
        this.arrayListGithubProject = arrayListGithubProject
        detachProgressBar()
    }

    /**
     * This method is used for initializing repository autoCompleteTextView in the loggerbird_github_popup.
     * @param arrayListGithubRepos is used for getting the repository list for repository autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeGithubRepos(
        arrayListGithubRepos: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewGithubRepoAdapter = AutoCompleteTextViewGithubRepoAdapter(
            this,
            R.layout.auto_text_view_github_repo_item,
            arrayListGithubRepos
        )
        autoTextViewGithubRepo.setAdapter(autoTextViewGithubRepoAdapter)
        if (arrayListGithubRepos.isNotEmpty() && autoTextViewGithubRepo.editableText.isEmpty()) {
            if (sharedPref.getString("github_repo", null) != null) {
                autoTextViewGithubRepo.setText(
                    sharedPref.getString("github_repo", null),
                    false
                )
            } else {
                autoTextViewGithubRepo.setText(arrayListGithubRepos[0], false)
            }
        }
        autoTextViewGithubRepo.setOnTouchListener { v, event ->
            autoTextViewGithubRepo.showDropDown()
            false
        }
        autoTextViewGithubRepo.setOnItemClickListener { parent, view, position, id ->
            githubAuthentication.setRepoPosition(repoPosition = position)
            attachProgressBar(task = "github")
            hideKeyboard(activity = activity, view = viewGithub)
            clearGithubComponents()
            githubAuthentication.callGithub(
                context = context,
                activity = activity,
                task = "get"
            )
        }
        autoTextViewGithubRepo.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListGithubRepos.contains(autoTextViewGithubRepo.editableText.toString())) {
                    if (arrayListGithubRepos.isNotEmpty()) {
                        if (sharedPref.getString("github_repo", null) != null) {
                            if (arrayListGithubRepos.contains(
                                    sharedPref.getString(
                                        "github_repo",
                                        null
                                    )!!
                                )
                            ) {
                                autoTextViewGithubRepo.setText(
                                    sharedPref.getString("github_repo", null),
                                    false
                                )
                            } else {
                                autoTextViewGithubRepo.setText(arrayListGithubRepos[0], false)
                            }
                        } else {
                            autoTextViewGithubRepo.setText(arrayListGithubRepos[0], false)
                        }
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing assignee autoCompleteTextView in the loggerbird_github_popup.
     * @param arrayListGithubAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeGithubAssignee(
        arrayListGithubAssignee: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewGithubAssigneeAdapter = AutoCompleteTextViewGithubAssigneeAdapter(
            this,
            R.layout.auto_text_view_github_assignee_item,
            arrayListGithubAssignee
        )
        autoTextViewGithubAssignee.setAdapter(autoTextViewGithubAssigneeAdapter)
        if (arrayListGithubAssignee.isNotEmpty() && autoTextViewGithubAssignee.editableText.isEmpty()) {
            if (sharedPref.getString("github_assignee", null) != null) {
                if (arrayListGithubAssignee.contains(
                        sharedPref.getString(
                            "github_assignee",
                            null
                        )!!
                    )
                ) {
                    autoTextViewGithubAssignee.setText(
                        sharedPref.getString("github_assignee", null),
                        false
                    )
                } else {
                    autoTextViewGithubAssignee.setText(arrayListGithubAssignee[0], false)
                }
            }
        }
        autoTextViewGithubAssignee.setOnTouchListener { v, event ->
            autoTextViewGithubAssignee.showDropDown()
            false
        }
        autoTextViewGithubAssignee.setOnDismissListener {
            hideKeyboard(activity = activity, view = viewGithub)
        }
    }

    /**
     * This method is used for initializing milestone autoCompleteTextView in the loggerbird_github_popup.
     * @param arrayListGithubMileStones is used for getting the milestone list for milestone autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeGithubMileStones(
        arrayListGithubMileStones: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewGithubMileStoneAdapter = AutoCompleteTextViewGithubMilestoneAdapter(
            this,
            R.layout.auto_text_view_github_milestone_item,
            arrayListGithubMileStones
        )
        autoTextViewGithubMileStone.setAdapter(autoTextViewGithubMileStoneAdapter)
        if (arrayListGithubMileStones.isNotEmpty() && autoTextViewGithubMileStone.editableText.isEmpty()) {
            if (sharedPref.getString("github_milestone", null) != null) {
                if (arrayListGithubMileStones.contains(
                        sharedPref.getString(
                            "github_milestone",
                            null
                        )!!
                    )
                ) {
                    autoTextViewGithubMileStone.setText(
                        sharedPref.getString("github_milestone", null),
                        false
                    )
                } else {
                    autoTextViewGithubMileStone.setText(arrayListGithubMileStones[0], false)
                }
            }
        }
        autoTextViewGithubMileStone.setOnTouchListener { v, event ->
            autoTextViewGithubMileStone.showDropDown()
            false
        }
        autoTextViewGithubMileStone.setOnItemClickListener { parent, view, position, id ->
            githubAuthentication.setMileStonePosition(mileStonePosition = position)
            hideKeyboard(activity = activity, view = viewGithub)
        }
    }

    /**
     * This method is used for initializing project autoCompleteTextView in the loggerbird_github_popup.
     * @param arrayListGithubProject is used for getting the project list for project autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeGithubProject(
        arrayListGithubProject: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewGithubProjectAdapter = AutoCompleteTextViewGithubProjectAdapter(
            this,
            R.layout.auto_text_view_github_project_item,
            arrayListGithubProject
        )
        autoTextViewGithubProject.setAdapter(autoTextViewGithubProjectAdapter)
        if (arrayListGithubProject.isNotEmpty() && autoTextViewGithubProject.editableText.isEmpty()) {
            if (sharedPref.getString("github_project", null) != null) {
                if (arrayListGithubProject.contains(
                        sharedPref.getString(
                            "github_project",
                            null
                        )!!
                    )
                ) {
                    autoTextViewGithubProject.setText(
                        sharedPref.getString("github_project", null),
                        false
                    )
                } else {
                    autoTextViewGithubProject.setText(arrayListGithubProject[0], false)
                }
            }
        }
        autoTextViewGithubProject.setOnTouchListener { v, event ->
            autoTextViewGithubProject.showDropDown()
            false
        }
        autoTextViewGithubProject.setOnItemClickListener { parent, view, position, id ->
            githubAuthentication.setProjectPosition(projectPosition = position)
            hideKeyboard(activity = activity, view = viewGithub)
        }
    }

    /**
     * This method is used for initializing label autoCompleteTextView in the loggerbird_github_popup.
     * @param arrayListGithubLabels is used for getting the label list for label autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeGithubLabels(
        arrayListGithubLabels: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewGithubLabelsAdapter = AutoCompleteTextViewGithubLabelAdapter(
            this,
            R.layout.auto_text_view_github_labels_item,
            arrayListGithubLabels
        )
        autoTextViewGithubLabels.setAdapter(autoTextViewGithubLabelsAdapter)
        if (arrayListGithubLabels.isNotEmpty() && autoTextViewGithubLabels.editableText.isEmpty()) {
            if (sharedPref.getString("github_labels", null) != null) {
                if (arrayListGithubLabels.contains(sharedPref.getString("github_labels", null)!!)) {
                    autoTextViewGithubLabels.setText(
                        sharedPref.getString("github_labels", null),
                        false
                    )
                } else {
                    autoTextViewGithubLabels.setText(arrayListGithubLabels[0], false)
                }
            }
        }
        autoTextViewGithubLabels.setOnTouchListener { v, event ->
            autoTextViewGithubLabels.showDropDown()
            false
        }
    }

    /**
     * This method is used for initializing linked requests autoCompleteTextView in the loggerbird_github_popup.
     * @param arrayListGithubLinkedRequests is used for getting the linked request list for linked request autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeGithubLinkedRequests(
        arrayListGithubLinkedRequests: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewGithubLinkedRequestsAdapter = AutoCompleteTextViewGithubLinkedRequestsAdapter(
            this,
            R.layout.auto_text_view_github_linked_request_item,
            arrayListGithubLinkedRequests
        )
        autoTextViewGithubLinkedRequests.setAdapter(autoTextViewGithubLinkedRequestsAdapter)
        if (arrayListGithubLinkedRequests.isNotEmpty() && autoTextViewGithubLinkedRequests.editableText.isEmpty()) {
            if (sharedPref.getString("github_pull_requests", null) != null) {
                if (arrayListGithubLinkedRequests.contains(
                        sharedPref.getString(
                            "github_pull_requests",
                            null
                        )!!
                    )
                ) {
                    autoTextViewGithubLinkedRequests.setText(
                        sharedPref.getString("github_pull_requests", null),
                        false
                    )
                } else {
                    autoTextViewGithubLinkedRequests.setText(
                        arrayListGithubLinkedRequests[0],
                        false
                    )
                }
            }
        }
        autoTextViewGithubLinkedRequests.setOnTouchListener { v, event ->
            autoTextViewGithubLinkedRequests.showDropDown()
            false
        }
        autoTextViewGithubLinkedRequests.setOnItemClickListener { parent, view, position, id ->
            githubAuthentication.setLinkedRequestPosition(linkedRequestPosition = position)
            hideKeyboard(activity = activity, view = viewGithub)
        }
    }

    //Trello
    /**
     * This method is used for creating trello layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeTrelloLayout(filePathMedia: File) {
        try {
            removeTrelloLayout()
            viewTrello = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_trello_popup, (this.rootView as ViewGroup), false)
            windowManagerParamsTrello = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            windowManagerTrello = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerTrello as WindowManager).addView(
                viewTrello,
                windowManagerParamsTrello
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.black)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.black)

            buttonTrelloCancel = viewTrello.findViewById(R.id.button_trello_cancel)
            buttonTrelloCreate = viewTrello.findViewById(R.id.button_trello_create)
            editTextTrelloTitle = viewTrello.findViewById(R.id.editText_trello_title)
            editTextTrelloDescription =
                viewTrello.findViewById(R.id.editText_trello_description)
            editTextTrelloCheckList = viewTrello.findViewById(R.id.editText_trello_check_list)
            toolbarTrello = viewTrello.findViewById(R.id.toolbar_trello)
            recyclerViewTrelloAttachment =
                viewTrello.findViewById(R.id.recycler_view_trello_attachment)
            autoTextViewTrelloProject =
                viewTrello.findViewById(R.id.auto_textView_trello_project)
            autoTextViewTrelloBoard = viewTrello.findViewById(R.id.auto_textView_trello_board)
            autoTextViewTrelloMember = viewTrello.findViewById(R.id.auto_textView_trello_member)
            autoTextViewTrelloLabel = viewTrello.findViewById(R.id.auto_textView_trello_label)
            recyclerViewTrelloLabel = viewTrello.findViewById(R.id.recycler_view_label_list)
            imageViewTrelloLabel = viewTrello.findViewById(R.id.imageView_label_add)
            cardViewTrelloLabelList = viewTrello.findViewById(R.id.cardView_label_list)
            recyclerViewTrelloMember = viewTrello.findViewById(R.id.recycler_view_member_list)
            imageViewTrelloMember = viewTrello.findViewById(R.id.imageView_member_add)
            cardViewTrelloMemberList = viewTrello.findViewById(R.id.cardView_member_list)
            recyclerViewTrelloCheckList =
                viewTrello.findViewById(R.id.recycler_view_check_list_list)
            imageViewTrelloCheckList = viewTrello.findViewById(R.id.imageView_check_list_add)
            cardViewTrelloCheckList = viewTrello.findViewById(R.id.cardView_check_list_list)
            imageViewTrelloCalendar = viewTrello.findViewById(R.id.imageView_start_date)
            imageButtonTrelloRemoveTimeline =
                viewTrello.findViewById(R.id.image_button_trello_remove_date)
            scrollViewTrello = viewTrello.findViewById(R.id.scrollView_trello)
            scrollViewTrello.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard(activity = activity, view = viewTrello)
                }
                return@setOnTouchListener false
            }

            toolbarTrello.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.trello_menu_save -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        with(sharedPref.edit()) {
                            putString(
                                "trello_project",
                                autoTextViewTrelloProject.editableText.toString()
                            )
                            putString(
                                "trello_board",
                                autoTextViewTrelloBoard.editableText.toString()
                            )
                            putString("trello_title", editTextTrelloTitle.text.toString())
                            putString(
                                "trello_description",
                                editTextTrelloDescription.text.toString()
                            )
                            putString(
                                "trello_checklist",
                                editTextTrelloCheckList.text.toString()
                            )
                            putString(
                                "trello_member",
                                autoTextViewTrelloMember.editableText.toString()
                            )
                            putString(
                                "trello_label",
                                autoTextViewTrelloLabel.editableText.toString()
                            )
                            commit()
                        }
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.trello_issue_preferences_save)
                        )
                    }
                    R.id.trello_menu_clear -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        val editor: SharedPreferences.Editor = sharedPref.edit()
                        editor.remove("trello_title")
                        editor.remove("trello_description")
                        editor.remove("trello_project")
                        editor.remove("trello_board")
                        editor.remove("trello_member")
                        editor.remove("trello_label")
                        editor.remove("trello_checklist")
                        editor.apply()
                        clearTrelloComponents()
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.trello_issue_preferences_delete)
                        )
                    }
                }
                return@setOnMenuItemClickListener true
            }

            toolbarTrello.setNavigationOnClickListener {
                removeTrelloLayout()
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.VISIBLE
                }
            }
            initializeTrelloRecyclerView(filePathMedia = filePathMedia)
            initializeTrelloLabelRecyclerView()
            initializeTrelloMemberRecyclerView()
            initializeTrelloCheckListRecyclerView()
            buttonClicksTrello()
            trelloAuthentication.callTrello(
                activity = activity,
                context = context,
                task = "get",
                filePathMedia = filePathMedia
            )
            attachProgressBar(task = "trello")
        } catch (e: Exception) {
            finishShareLayout("trello_error")
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.trelloTag)
        }
    }

    /**
     * This method is used for removing loggerbird_trello_popup from window.
     */
    internal fun removeTrelloLayout() {
        if (this::viewTrello.isInitialized && windowManagerTrello != null) {
            (windowManagerTrello as WindowManager).removeViewImmediate(
                viewTrello
            )
            windowManagerTrello = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_trello_popup.
     */
    private fun buttonClicksTrello() {
        buttonTrelloCreate.setSafeOnClickListener {
            trelloAuthentication.gatherAutoTextDetails(
                autoTextViewProject = autoTextViewTrelloProject,
                autoTextViewBoard = autoTextViewTrelloBoard,
                autoTextViewMember = autoTextViewTrelloMember,
                autoTextViewLabel = autoTextViewTrelloLabel
            )
            trelloAuthentication.gatherEditTextDetails(
                editTextTitle = editTextTrelloTitle,
                editTextDescription = editTextTrelloDescription,
                editTextCheckList = editTextTrelloCheckList
            )
            trelloAuthentication.gatherCalendarDetails(calendar = calendarTrello)
            if (trelloAuthentication.checkTitle(
                    activity = activity,
                    editTextTitle = editTextTrelloTitle
                ) && trelloAuthentication.checkTrelloBoardEmpty(
                    activity = activity,
                    autoTextViewBoard = autoTextViewTrelloBoard
                ) && trelloAuthentication.checkTrelloLabel(
                    activity = activity,
                    autoTextViewLabel = autoTextViewTrelloLabel
                ) && trelloAuthentication.checkTrelloMember(
                    activity = activity,
                    autoTextViewMember = autoTextViewTrelloMember
                ) && trelloAuthentication.checkTrelloProjectEmpty(
                    activity = activity,
                    autoTextViewProject = autoTextViewTrelloProject
                )
            ) {
                attachProgressBar(task = "trello")
                trelloAuthentication.callTrello(
                    activity = activity,
                    context = context,
                    task = "create"
                )
            }

        }
        buttonTrelloCancel.setSafeOnClickListener {
            removeTrelloLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
        imageViewTrelloLabel.setSafeOnClickListener {
            if (!arrayListTrelloLabelName.contains(
                    RecyclerViewModelLabel(
                        autoTextViewTrelloLabel.editableText.toString()
                    )
                ) && arrayListTrelloLabel.contains(
                    autoTextViewTrelloLabel.editableText.toString()
                )
            ) {
                arrayListTrelloLabelName.add(RecyclerViewModelLabel(autoTextViewTrelloLabel.editableText.toString()))
                trelloLabelAdapter.notifyDataSetChanged()
                cardViewTrelloLabelList.visibility = View.VISIBLE
            } else if (arrayListTrelloLabelName.contains(
                    RecyclerViewModelLabel(autoTextViewTrelloLabel.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.trello_label_exist)
                )
            } else if (!arrayListTrelloLabel.contains(autoTextViewTrelloLabel.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.trello_label_doesnt_exist)
                )
            }

        }
        imageViewTrelloMember.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewTrello)
            if (!arrayListTrelloMemberName.contains(
                    RecyclerViewModelMember(
                        autoTextViewTrelloMember.editableText.toString()
                    )
                ) && arrayListTrelloMember.contains(
                    autoTextViewTrelloMember.editableText.toString()
                )
            ) {
                arrayListTrelloMemberName.add(RecyclerViewModelMember(autoTextViewTrelloMember.editableText.toString()))
                trelloMemberAdapter.notifyDataSetChanged()
                cardViewTrelloMemberList.visibility = View.VISIBLE
            } else if (arrayListTrelloMemberName.contains(
                    RecyclerViewModelMember(autoTextViewTrelloMember.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.trello_member_exist)
                )
            } else if (!arrayListTrelloMember.contains(autoTextViewTrelloMember.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.trello_member_doesnt_exist)
                )
            }
        }
        imageViewTrelloCheckList.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewTrello)
            if (!arrayListTrelloCheckListName.contains(
                    RecyclerViewModelCheckList(
                        editTextTrelloCheckList.text.toString()
                    )
                ) && editTextTrelloCheckList.text.isNotEmpty()
            ) {
                arrayListTrelloCheckListName.add(RecyclerViewModelCheckList(editTextTrelloCheckList.text.toString()))
                trelloCheckListAdapter.notifyDataSetChanged()
                cardViewTrelloCheckList.visibility = View.VISIBLE
                RecyclerViewTrelloCheckListAdapter.ViewHolder.hashmapCheckListNames[editTextTrelloCheckList.text.toString()] =
                    null
                RecyclerViewTrelloCheckListAdapter.ViewHolder.hashmapCheckListCheckedList[editTextTrelloCheckList.text.toString()] =
                    null
//                RecyclerViewTrelloCheckListAdapter.ViewHolder.hashmapCheckListNames[editTextTrelloCheckList.text.toString()] = RecyclerViewTrelloItemAdapter.ViewHolder.arrayListItemNames
            } else if (arrayListTrelloCheckListName.contains(
                    RecyclerViewModelCheckList(editTextTrelloCheckList.text.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.trello_check_list_exist)
                )
            } else if (editTextTrelloCheckList.text.isEmpty()) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.trello_check_list_empty)
                )
            }
        }
        imageViewTrelloCalendar.setSafeOnClickListener {
            initializeTrelloTimelineLayout()
        }

        imageButtonTrelloRemoveTimeline.setSafeOnClickListener {
            trelloStartDate = null
            trelloStartTime = null
            imageButtonTrelloRemoveTimeline.visibility = View.GONE

        }
    }

    /**
     * This method is used for initializing  autoCompleteTextViews in the loggerbird_trello_popup.
     * @param arrayListTrelloProject is used for getting the project list for project autoCompleteTextView.
     * @param arrayListTrelloBoards is used for getting the board list for board autoCompleteTextView.
     * @param arrayListTrelloMember is used for getting the member list for member autoCompleteTextView.
     * @param arrayListTrelloLabel is used for getting the label list for label autoCompleteTextView.
     * @param arrayListTrelloLabelColor is used for getting the label color list.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    internal fun initializeTrelloAutoTextViews(
        arrayListTrelloProject: ArrayList<String>,
        arrayListTrelloBoards: ArrayList<String>,
        arrayListTrelloMember: ArrayList<String>,
        arrayListTrelloLabel: ArrayList<String>,
        arrayListTrelloLabelColor: ArrayList<String>
    ) {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        editTextTrelloTitle.setText(sharedPref.getString("trello_title", null))
        editTextTrelloDescription.setText(sharedPref.getString("trello_description", null))
        editTextTrelloCheckList.setText(sharedPref.getString("trello_checklist", null))
        initializeTrelloProject(
            arrayListTrelloProject = arrayListTrelloProject,
            sharedPref = sharedPref
        )
        initializeTrelloBoard(
            arrayListTrelloBoards = arrayListTrelloBoards,
            sharedPref = sharedPref
        )
        initializeTrelloMember(
            arrayListTrelloMember = arrayListTrelloMember,
            sharedPref = sharedPref
        )
        initializeTrelloLabel(
            arrayListTrelloLabel = arrayListTrelloLabel,
            arrayListTrelloLabelColor = arrayListTrelloLabelColor,
            sharedPref = sharedPref
        )
        this.arrayListTrelloLabel = arrayListTrelloLabel
        this.arrayListTrelloMember = arrayListTrelloMember
        detachProgressBar()
    }

    /**
     * This method is used for initializing project autoCompleteTextView in the loggerbird_trello_popup.
     * @param arrayListTrelloProject is used for getting the project list for project autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeTrelloProject(
        arrayListTrelloProject: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewTrelloProjectAdapter = AutoCompleteTextViewTrelloProjectAdapter(
            this,
            R.layout.auto_text_view_trello_project_item,
            arrayListTrelloProject
        )
        autoTextViewTrelloProject.setAdapter(autoTextViewTrelloProjectAdapter)
        if (arrayListTrelloProject.isNotEmpty() && autoTextViewTrelloProject.editableText.isEmpty()) {
            if (sharedPref.getString("trello_project", null) != null) {
                if (arrayListTrelloProject.contains(
                        sharedPref.getString(
                            "trello_project",
                            null
                        )!!
                    )
                ) {
                    autoTextViewTrelloProject.setText(
                        sharedPref.getString("trello_project", null),
                        false
                    )
                } else {
                    autoTextViewTrelloProject.setText(arrayListTrelloProject[0], false)
                }
            } else {
                autoTextViewTrelloProject.setText(arrayListTrelloProject[0], false)
            }
        }
        autoTextViewTrelloProject.setOnTouchListener { v, event ->
            autoTextViewTrelloProject.showDropDown()
            false
        }
        autoTextViewTrelloProject.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewTrello)
            clearTrelloComponents()
            trelloAuthentication.setProjectPosition(projectPosition = position)
            trelloAuthentication.callTrello(
                activity = activity,
                context = context,
                task = "get"
            )
            attachProgressBar("trello")
        }
    }

    /**
     * This method is used for initializing board autoCompleteTextView in the loggerbird_trello_popup.
     * @param arrayListTrelloBoards is used for getting the board list for board autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeTrelloBoard(
        arrayListTrelloBoards: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewTrelloBoardAdapter = AutoCompleteTextViewTrelloBoardAdapter(
            this,
            R.layout.auto_text_view_trello_board_item,
            arrayListTrelloBoards
        )
        autoTextViewTrelloBoard.setAdapter(autoTextViewTrelloBoardAdapter)
        if (arrayListTrelloBoards.isNotEmpty() && autoTextViewTrelloBoard.editableText.isEmpty()) {
            if (sharedPref.getString("trello_board", null) != null) {
                if (arrayListTrelloBoards.contains(
                        sharedPref.getString(
                            "trello_board",
                            null
                        )!!
                    )
                ) {
                    autoTextViewTrelloBoard.setText(
                        sharedPref.getString("trello_board", null),
                        false
                    )
                } else {
                    autoTextViewTrelloBoard.setText(arrayListTrelloBoards[0], false)
                }
            } else {
                autoTextViewTrelloBoard.setText(arrayListTrelloBoards[0], false)
            }
        }
        autoTextViewTrelloBoard.setOnTouchListener { v, event ->
            autoTextViewTrelloBoard.showDropDown()
            false
        }
        autoTextViewTrelloBoard.setOnItemClickListener { parent, view, position, id ->
            trelloAuthentication.setBoardPosition(boardPosition = position)
            hideKeyboard(activity = activity, view = viewTrello)
        }
    }


    /**
     * This method is used for initializing member autoCompleteTextView in the loggerbird_trello_popup.
     * @param arrayListTrelloMember is used for getting the member list for member autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeTrelloMember(
        arrayListTrelloMember: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewTrelloMemberAdapter = AutoCompleteTextViewTrelloMemberAdapter(
            this,
            R.layout.auto_text_view_trello_member_item,
            arrayListTrelloMember
        )
        autoTextViewTrelloMember.setAdapter(autoTextViewTrelloMemberAdapter)
        if (arrayListTrelloMember.isNotEmpty() && autoTextViewTrelloMember.editableText.isEmpty()) {
            if (sharedPref.getString("trello_member", null) != null) {
                if (arrayListTrelloMember.contains(
                        sharedPref.getString(
                            "trello_member",
                            null
                        )!!
                    )
                ) {
                    autoTextViewTrelloMember.setText(
                        sharedPref.getString("trello_member", null),
                        false
                    )
                } else {
                    autoTextViewTrelloMember.setText(arrayListTrelloMember[0], false)
                }
            }
        }
        autoTextViewTrelloMember.setOnTouchListener { v, event ->
            autoTextViewTrelloMember.showDropDown()
            false
        }
        autoTextViewTrelloMember.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewTrello)
        }
    }


    /**
     * This method is used for initializing label autoCompleteTextView in the loggerbird_label_popup.
     * @param arrayListTrelloLabel is used for getting the label list for label autoCompleteTextView.
     * @param arrayListTrelloLabelColor is used for getting the label color.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeTrelloLabel(
        arrayListTrelloLabel: ArrayList<String>,
        arrayListTrelloLabelColor: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewTrelloLabelAdapter =
            AutoCompleteTextViewTrelloLabelAdapter(
                this,
                R.layout.auto_text_view_trello_label_item,
                arrayListTrelloLabel,
                arrayListTrelloLabelColor
            )
        autoTextViewTrelloLabel.setAdapter(autoTextViewTrelloLabelAdapter)
        if (arrayListTrelloLabel.isNotEmpty() && autoTextViewTrelloLabel.editableText.isEmpty()) {
            if (sharedPref.getString("trello_label", null) != null) {
                if (arrayListTrelloLabel.contains(
                        sharedPref.getString(
                            "trello_label",
                            null
                        )!!
                    )
                ) {
                    autoTextViewTrelloLabel.setText(
                        sharedPref.getString("trello_label", null),
                        false
                    )
                } else {
                    autoTextViewTrelloLabel.setText(arrayListTrelloLabel[0], false)
                }
            }
        }
        autoTextViewTrelloLabel.setOnTouchListener { v, event ->
            autoTextViewTrelloLabel.showDropDown()
            false
        }
        autoTextViewTrelloLabel.setOnItemClickListener { parent, view, position, id ->
            trelloAuthentication.setLabelPosition(labelPosition = position)
            hideKeyboard(activity = activity, view = viewTrello)
        }
    }

    /**
     * This method is used for initializing trello attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeTrelloRecyclerView(filePathMedia: File) {
        arrayListTrelloFileName.clear()
        recyclerViewTrelloAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        trelloAttachmentAdapter =
            RecyclerViewTrelloAttachmentAdapter(
                addTrelloFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewTrelloAttachment.adapter = trelloAttachmentAdapter
    }

    /**
     * This method is used for adding files to trello file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addTrelloFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListTrelloFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListTrelloFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListTrelloFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListTrelloFileName
    }

    /**
     * This method is used for initializing trello label recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeTrelloLabelRecyclerView() {
        arrayListTrelloLabelName.clear()
        recyclerViewTrelloLabel.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        trelloLabelAdapter =
            RecyclerViewTrelloLabelAdapter(
                arrayListTrelloLabelName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewTrelloLabel.adapter = trelloLabelAdapter
    }

    /**
     * This method is used for initializing trello member recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeTrelloMemberRecyclerView() {
        arrayListTrelloMemberName.clear()
        recyclerViewTrelloMember.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        trelloMemberAdapter =
            RecyclerViewTrelloMemberAdapter(
                arrayListTrelloMemberName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewTrelloMember.adapter = trelloMemberAdapter
    }

    /**
     * This method is used for initializing trello checklist recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeTrelloCheckListRecyclerView() {
        arrayListTrelloCheckListName.clear()
        recyclerViewTrelloCheckList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        trelloCheckListAdapter =
            RecyclerViewTrelloCheckListAdapter(
                arrayListTrelloCheckListName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewTrelloCheckList.adapter = trelloCheckListAdapter
    }


    /**
     * This method is used for clearing trello components.
     */
    private fun clearTrelloComponents() {
        cardViewTrelloMemberList.visibility = View.GONE
        cardViewTrelloLabelList.visibility = View.GONE
        arrayListTrelloMemberName.clear()
        arrayListTrelloLabelName.clear()
        trelloMemberAdapter.notifyDataSetChanged()
        trelloLabelAdapter.notifyDataSetChanged()
        editTextTrelloTitle.text = null
        editTextTrelloDescription.text = null
        editTextTrelloCheckList.text = null
        autoTextViewTrelloLabel.setText("", false)
        autoTextViewTrelloMember.setText("", false)
        autoTextViewTrelloBoard.setText("", false)
//        autoTextViewTrelloProject.setText("",false)
    }

    /**
     * This method is used for creating trello-time layout which is attached to application overlay.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun initializeTrelloTimelineLayout() {
        removeTrelloTimelineLayout()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewTrelloTimeline =
            LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_trello_start_date_popup, rootView, false)
        windowManagerParamsTrelloTimeline =
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
        windowManagerTrelloTimeline = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerTrelloTimeline as WindowManager).addView(
            viewTrelloTimeline,
            windowManagerParamsTrelloTimeline
        )
        imageViewTrelloDate = viewTrelloTimeline.findViewById(R.id.imageView_trello_date)
        imageButtonTrelloDateRemove =
            viewTrelloTimeline.findViewById(R.id.image_button_trello_date_remove)
        imageViewTrelloTime = viewTrelloTimeline.findViewById(R.id.imageView_trello_time)
        imageButtonTrelloTimeRemove =
            viewTrelloTimeline.findViewById(R.id.image_button_trello_time_remove)
        buttonTrelloTimelineCancel = viewTrelloTimeline.findViewById(R.id.button_trello_date_cancel)
        buttonTrelloTimelineProceed =
            viewTrelloTimeline.findViewById(R.id.button_trello_date_proceed)
        buttonClicksTrelloTimeline()
    }

    /**
     * This method is used for removing loggerbird_trello_start_date_popup from window.
     */
    private fun removeTrelloTimelineLayout() {
        if (this::viewTrelloTimeline.isInitialized && windowManagerTrelloTimeline != null) {
            (windowManagerTrelloTimeline as WindowManager).removeViewImmediate(
                viewTrelloTimeline
            )
            windowManagerTrelloTimeline = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_trello_start_date_popup.
     */
    private fun buttonClicksTrelloTimeline() {
        imageViewTrelloDate.setSafeOnClickListener {
            initializeTrelloDateLayout()
        }
        imageButtonTrelloDateRemove.setSafeOnClickListener {
            trelloStartDate = null
            imageButtonTrelloDateRemove.visibility = View.GONE
        }
        imageViewTrelloTime.setSafeOnClickListener {
            initializeTrelloTimeLayout()
        }
        imageButtonTrelloTimeRemove.setSafeOnClickListener {
            trelloStartTime = null
            imageButtonTrelloTimeRemove.visibility = View.GONE
        }

        buttonTrelloTimelineProceed.setSafeOnClickListener {
            if (checkTrelloDateAndTimeEmpty()) {
                imageButtonTrelloRemoveTimeline.visibility = View.VISIBLE
            }
        }
        buttonTrelloTimelineCancel.setSafeOnClickListener {
            removeTrelloTimelineLayout()
        }
    }

    /**
     * This method is used for creating trello-date layout which is attached to application overlay.
     */
    private fun initializeTrelloDateLayout() {
        removeTrelloDateLayout()
        calendarTrello = Calendar.getInstance()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewTrelloDate =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LayoutInflater.from(activity)
                    .inflate(R.layout.trello_calendar_view, rootView, false)
            } else {
                LayoutInflater.from(activity)
                    .inflate(R.layout.trello_calendar_view_lower, rootView, false)
            }
        windowManagerParamsTrelloDate =
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
        windowManagerTrelloDate = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerTrelloDate as WindowManager).addView(
            viewTrelloDate,
            windowManagerParamsTrelloDate
        )
        frameLayoutTrelloDate = viewTrelloDate.findViewById(R.id.trello_calendar_view_layout)
        calendarViewTrello = viewTrelloDate.findViewById(R.id.calendarView_start_date)
        buttonTrelloDateCancel = viewTrelloDate.findViewById(R.id.button_trello_calendar_cancel)
        buttonTrelloDateCreate = viewTrelloDate.findViewById(R.id.button_trello_calendar_ok)
        buttonClicksTrelloDateLayout()
    }

    /**
     * This method is used for removing trello_calendar_view from window.
     */
    private fun removeTrelloDateLayout() {
        if (this::viewTrelloDate.isInitialized && windowManagerTrelloDate != null) {
            (windowManagerTrelloDate as WindowManager).removeViewImmediate(
                viewTrelloDate
            )
            windowManagerTrelloDate = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the trello_calendar_view.
     */
    private fun buttonClicksTrelloDateLayout() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        calendarTrello?.set(mYear, mMonth, mDayOfMonth)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            calendarViewTrello.minDate = System.currentTimeMillis()
        }
        frameLayoutTrelloDate.setOnClickListener {
            removeTrelloDateLayout()
        }
        calendarViewTrello.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendarTrello?.set(year, month + 1, dayOfMonth)
//            startDate = "$year-$month-$dayOfMonth"
        }
        buttonTrelloDateCreate.setSafeOnClickListener {
            trelloStartDate = calendarViewTrello.date
            Log.d("time", trelloStartDate.toString())
            Log.d("time", System.currentTimeMillis().toString())
            imageButtonTrelloDateRemove.visibility = View.VISIBLE
            removeTrelloDateLayout()
        }
        buttonTrelloDateCancel.setSafeOnClickListener {
            removeTrelloDateLayout()
        }
    }

    private fun initializeTrelloTimeLayout() {
        removeTrelloTimeLayout()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewTrelloTime =
            LayoutInflater.from(activity)
                .inflate(R.layout.trello_time_picker, rootView, false)
        windowManagerParamsTrelloTime =
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
        windowManagerTrelloTime = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerTrelloTime as WindowManager).addView(
            viewTrelloTime,
            windowManagerParamsTrelloTime
        )
        frameLayoutTrelloTime = viewTrelloTime.findViewById(R.id.trello_time_view_layout)
        timePickerTrello = viewTrelloTime.findViewById(R.id.timePicker_start_time)
        buttonTrelloTimeCancel = viewTrelloTime.findViewById(R.id.button_trello_time_cancel)
        buttonTrelloTimeCreate = viewTrelloTime.findViewById(R.id.button_trello_time_ok)
        buttonClicksTrelloTimeLayout()
    }

    private fun removeTrelloTimeLayout() {
        if (this::viewTrelloTime.isInitialized && windowManagerTrelloTime != null) {
            (windowManagerTrelloTime as WindowManager).removeViewImmediate(
                viewTrelloTime
            )
            windowManagerTrelloTime = null
        }
    }

    private fun buttonClicksTrelloTimeLayout() {
        frameLayoutTrelloTime.setOnClickListener {
            removeTrelloTimeLayout()
        }
        buttonTrelloTimeCreate.setSafeOnClickListener {
            calendarTrello?.set(Calendar.HOUR_OF_DAY, timePickerTrello.hour)
            calendarTrello?.set(Calendar.MINUTE, timePickerTrello.minute)
            trelloStartTime =
                timePickerTrello.hour.toLong() + timePickerTrello.minute.toLong()
            imageButtonTrelloTimeRemove.visibility = View.VISIBLE
            removeTrelloTimeLayout()
        }
        buttonTrelloTimeCancel.setSafeOnClickListener {
            removeTrelloTimeLayout()
        }
    }

    /**
     * This method is used for start date and start time is not null in trello layout.
     * @return Boolean value.
     */
    private fun checkTrelloDateAndTimeEmpty(): Boolean {
        if (trelloStartDate != null && trelloStartTime != null) {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_time_line_success)
            )
            removeTrelloTimelineLayout()
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.trello_time_line_empty)
            )
        }
        return false
    }

    //Gitlab
    /**
     * This method is used for creating gitlab layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGitlabLayout(filePathMedia: File) {
        try {
            removeGitlabLayout()
            viewGitlab = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_gitlab_popup, (this.rootView as ViewGroup), false)
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
            autoTextViewGitlabProject =
                viewGitlab.findViewById(R.id.auto_textview_gitlab_project)
            editTextGitlabTitle = viewGitlab.findViewById(R.id.editText_gitlab_title)
            editTextGitlabDescription =
                viewGitlab.findViewById(R.id.editText_gitlab_description)
            editTextGitlabWeight = viewGitlab.findViewById(R.id.editText_gitlab_weight)
            autoTextViewGitlabMilestone =
                viewGitlab.findViewById(R.id.auto_textView_gitlab_milestone)
            autoTextViewGitlabAssignee =
                viewGitlab.findViewById(R.id.auto_textView_gitlab_assignee)
            autoTextViewGitlabLabels = viewGitlab.findViewById(R.id.auto_textView_gitlab_labels)
            autoTextViewGitlabConfidentiality =
                viewGitlab.findViewById(R.id.auto_textView_gitlab_confidentiality)
            textViewGitlabDueDate = viewGitlab.findViewById(R.id.textView_gitlab_due_date)
            buttonGitlabCreate = viewGitlab.findViewById(R.id.button_gitlab_create)
            buttonGitlabCancel = viewGitlab.findViewById(R.id.button_gitlab_cancel)
            imageViewGitlabDueDate =
                viewGitlab.findViewById(R.id.imageView_gitlab_delete_due_date)
            recyclerViewGitlabAttachment =
                viewGitlab.findViewById(R.id.recycler_view_gitlab_attachment)
            editTextGitlabWeight.filters = arrayOf<InputFilter>(
                InputTypeFilter(
                    "0",
                    "100"
                )
            )
            gitlabAuthentication.callGitlab(
                activity = activity,
                context = context,
                task = "get",
                filePathMedia = filePathMedia
            )

            attachProgressBar("gitlab")
            initializeGitlabAttachmentRecyclerView(filePathMedia = filePathMedia)
            buttonClicksGitlab(filePathMedia = filePathMedia)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.jiraTag)
        }
    }


    /**
     * This method is used for initializing gitlab date-picker.
     */
    private fun initializeGitlabDatePicker() {
        val calendar = Calendar.getInstance()
        var mYear = calendar.get(Calendar.YEAR)
        var mMonth = calendar.get(Calendar.MONTH)
        var mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        var dueDate: String = ""
        val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy")
        calendarViewGitlabLayout =
            calendarViewGitlabView.findViewById(R.id.gitlab_calendar_view_layout)
        calendarViewGitlabDueDate =
            calendarViewGitlabView.findViewById(R.id.calendarView_gitlab_due_date)
        buttonCalendarViewGitlabCancel =
            calendarViewGitlabView.findViewById(R.id.button_gitlab_calendar_cancel)
        buttonCalendarViewGitlabOk =
            calendarViewGitlabView.findViewById(R.id.button_gitlab_calendar_ok)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            calendarViewGitlabDueDate.minDate = System.currentTimeMillis()
        }
        if (calendarViewGitlabDate != null) {
            calendarViewGitlabDueDate.date = calendarViewGitlabDate!!
        }

        buttonCalendarViewGitlabCancel.setOnClickListener {
            detachGitlabDatePicker()
        }
        imageViewGitlabDueDate.setOnClickListener {

            imageViewGitlabDueDate.visibility = View.GONE
            textViewGitlabDueDate.text = null
            gitlabAuthentication.dueDate = null

        }
        buttonCalendarViewGitlabOk.setOnClickListener {
            if (dueDate != null) {
                gitlabAuthentication.dueDate = dueDate
            }
            detachGitlabDatePicker()
        }

        calendarViewGitlabDueDate.setOnDateChangeListener { viewStartDate, year, month, dayOfMonth ->
            mYear = year
            mMonth = month + 1
            mDayOfMonth = dayOfMonth
            calendarViewGitlabDate = viewStartDate.date
            dueDate = "$mYear-$mMonth-$mDayOfMonth"
            val dueDateFormat = "$mMonth/$mDayOfMonth/$mYear"
            activity.runOnUiThread {
                textViewGitlabDueDate.text = dueDateFormat
                textViewGitlabDueDate.setTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this,
                            R.color.black
                        )
                    )
                )
                imageViewGitlabDueDate.visibility = View.VISIBLE
            }
        }
    }

    /**
     * This method is used for creating custom gitlab date-picker layout which is attached to application overlay.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun attachGitlabDatePicker() {
        try {
            val rootView: ViewGroup =
                activity.window.decorView.findViewById(android.R.id.content)
            calendarViewGitlabView = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LayoutInflater.from(activity)
                    .inflate(R.layout.gitlab_calendar_view, rootView, false)
            } else {
                LayoutInflater.from(activity)
                    .inflate(R.layout.gitlab_calendar_view_lower, rootView, false)
            }
            windowManagerParamsGitlabDatePicker =
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
            windowManagerGitlabDatePicker = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerGitlabDatePicker as WindowManager).addView(
                calendarViewGitlabView,
                windowManagerParamsGitlabDatePicker
            )
            initializeGitlabDatePicker()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.gitlabDatePopupTag)
        }
    }

    /**
     * This method is used for removing gitlab_calendar_view from window.
     */
    private fun detachGitlabDatePicker() {
        if (this::calendarViewGitlabView.isInitialized) {
            (windowManagerGitlabDatePicker as WindowManager).removeViewImmediate(
                calendarViewGitlabView
            )
        }
    }

    /**
     * This method is used for removing loggerbird_gitlab_popup from window.
     */
    private fun removeGitlabLayout() {
        if (windowManagerGitlab != null && this::viewGitlab.isInitialized) {
            (windowManagerGitlab as WindowManager).removeViewImmediate(viewGitlab)
            windowManagerGitlab = null
            arrayListGitlabFileName.clear()
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun buttonClicksGitlab(filePathMedia: File) {
        buttonGitlabCreate.setSafeOnClickListener {
            if (checkGitlabTitleEmpty() && checkGitlabAssigneeEmpty()
                && gitlabAuthentication.checkGitlabAssignee(
                    activity = activity,
                    autoTextViewAssignee = autoTextViewGitlabAssignee
                )
                && gitlabAuthentication.checkGitlabLabel(
                    activity = activity,
                    autoTextViewLabel = autoTextViewGitlabLabels
                )
                && gitlabAuthentication.checkGitlabMilestone(
                    activity = activity,
                    autoTextViewMilestone = autoTextViewGitlabMilestone
                )
                && gitlabAuthentication.checkGitlabProject(
                    activity = activity,
                    autoTextViewProject = autoTextViewGitlabProject
                )
            ) {
                attachProgressBar(task = "gitlab")
                gitlabAuthentication.gatherGitlabEditTextDetails(
                    editTextTitle = editTextGitlabTitle,
                    editTextDescription = editTextGitlabDescription,
                    editTextWeight = editTextGitlabWeight
                )
                gitlabAuthentication.gatherGitlabProjectAutoTextDetails(
                    autoTextViewProject = autoTextViewGitlabProject,
                    autoTextViewLabels = autoTextViewGitlabLabels,
                    autoTextViewConfidentiality = autoTextViewGitlabConfidentiality,
                    autoTextViewMilestone = autoTextViewGitlabMilestone,
                    autoTextViewAssignee = autoTextViewGitlabAssignee
                )
                gitlabAuthentication.callGitlab(
                    activity = activity,
                    context = context,
                    task = "create",
                    filePathMedia = filePathMedia
                )
            }
        }
        textViewGitlabDueDate.setSafeOnClickListener {
            attachGitlabDatePicker()
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

    /**
     * This method is used for initializing gitlab attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGitlabAttachmentRecyclerView(filePathMedia: File) {
        arrayListGitlabFileName.clear()
        recyclerViewGitlabAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        gitlabAttachmentAdapter =
            RecyclerViewGitlabAttachmentAdapter(
                addGitlabFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewGitlabAttachment.adapter = gitlabAttachmentAdapter
    }

    /**
     * This method is used for initializing  spinners in the loggerbird_gitlab_popup.
     * @param arrayListGitlabProjects is used for getting the project list for project spinner.
     * @param arrayListGitlabAssignee is used for getting the assignee list for assignee spinner.
     * @param arrayListGitlabMilestones is used for getting the milestone list for milestone spinner.
     * @param arrayListGitlabLabels is used for getting the label list for label spinner.
     * @param arrayListGitlabConfidentiality is used for getting the confidentiality list for confidentiality spinner.
     */

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun initializeGitlabSpinner(
        arrayListGitlabProjects: ArrayList<String>,
        arrayListGitlabAssignee: ArrayList<String>,
        arrayListGitlabMilestones: ArrayList<String>,
        arrayListGitlabLabels: ArrayList<String>,
        arrayListGitlabConfidentiality: ArrayList<String>
    ) {
        initializeGitlabProject(arrayListGitlabProjects = arrayListGitlabProjects)
        initializeGitlabAssignee(arrayListGitlabAssignee = arrayListGitlabAssignee)
        initializeGitlabMilestones(arrayListGitlabMilestones = arrayListGitlabMilestones)
        initializeGitlabLabels(arrayListGitlabLabels = arrayListGitlabLabels)
        initializeGitlabConfidentiality(arrayListGitlabConfidentiality = arrayListGitlabConfidentiality)
        detachProgressBar()
    }

    /**
     * This method is used for initializing project spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabProjects is used for getting the project list for project spinner.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeGitlabProject(
        arrayListGitlabProjects: ArrayList<String>
    ) {
        autoTextViewGitlabProjectAdapter =
            AutoCompleteTextViewGitlabProjectAdapter(
                this,
                R.layout.auto_text_view_gitlab_project_item,
                arrayListGitlabProjects
            )
        autoTextViewGitlabProject.setAdapter(autoTextViewGitlabProjectAdapter)
        if (arrayListGitlabProjects.isNotEmpty() && autoTextViewGitlabProject.text.isEmpty()) {
            autoTextViewGitlabProject.setText(arrayListGitlabProjects[0], false)
        }
        autoTextViewGitlabProject.setOnTouchListener { v, event ->
            autoTextViewGitlabProject.showDropDown()
            false
        }
        autoTextViewGitlabConfidentiality.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewGitlab)
        }
        autoTextViewGitlabProject.setOnItemClickListener { parent, view, position, id ->
            gitlabAuthentication.gitlabProjectPosition(projectPosition = position)
            attachProgressBar(task = "gitlab")
            gitlabAuthentication.callGitlab(
                activity = activity,
                context = context,
                task = "get"
            )
            autoTextViewGitlabProject.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    if (!arrayListGitlabProjects.contains(autoTextViewGitlabProject.editableText.toString())) {
                        if (arrayListGitlabProjects.isNotEmpty()) {
                            defaultToast.attachToast(
                                activity = activity,
                                toastMessage = activity.resources.getString(R.string.textView_gitlab_project_doesnt_exist)
                            )
                            autoTextViewGitlabProject.setText(arrayListGitlabProjects[0], false)

                        }
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing assignee spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabAssignee is used for getting the assignee list for assignee spinner.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeGitlabAssignee(
        arrayListGitlabAssignee: ArrayList<String>
    ) {
        autoTextViewGitlabAssigneeAdapter =
            AutoCompleteTextViewGitlabAssigneeAdapter(
                this,
                R.layout.auto_text_view_gitlab_assignee_item,
                arrayListGitlabAssignee
            )
        autoTextViewGitlabAssignee.setAdapter(autoTextViewGitlabAssigneeAdapter)
        if (arrayListGitlabAssignee.isNotEmpty() && autoTextViewGitlabAssignee.text.isEmpty()) {
            autoTextViewGitlabAssignee.setText(arrayListGitlabAssignee[0], false)
        }
        autoTextViewGitlabAssignee.setOnTouchListener { v, event ->
            autoTextViewGitlabAssignee.showDropDown()
            false
        }
        autoTextViewGitlabAssignee.setOnItemClickListener { parent, view, position, id ->
            gitlabAuthentication.gitlabAssigneePosition(assigneePosition = position)
        }
        autoTextViewGitlabAssignee.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewGitlab)
        }
        autoTextViewGitlabAssignee.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListGitlabAssignee.contains(autoTextViewGitlabAssignee.editableText.toString())) {
                    if (arrayListGitlabAssignee.isNotEmpty()) {
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = activity.resources.getString(R.string.textView_gitlab_assignee_doesnt_exist)
                        )
                        autoTextViewGitlabAssignee.setText(arrayListGitlabAssignee[0], false)
                        checkGitlabAssigneeEmpty()
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing label spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabLabels is used for getting the label list for label spinner.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeGitlabLabels(
        arrayListGitlabLabels: ArrayList<String>
    ) {
        autoTextViewGitlabLabelsAdapter = AutoCompleteTextViewGitlabLabelAdapter(
            this,
            R.layout.auto_text_view_gitlab_label_item,
            arrayListGitlabLabels
        )
        autoTextViewGitlabLabels.setAdapter(autoTextViewGitlabLabelsAdapter)
        if (arrayListGitlabLabels.isNotEmpty() && autoTextViewGitlabLabels.text.isEmpty()) {
            autoTextViewGitlabLabels.setText(arrayListGitlabLabels[0], false)
        }
        autoTextViewGitlabLabels.setOnTouchListener { v, event ->
            autoTextViewGitlabLabels.showDropDown()
            false
        }
        autoTextViewGitlabLabels.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewGitlab)
        }
        autoTextViewGitlabLabels.setOnItemClickListener { parent, view, position, id ->
            gitlabAuthentication.gitlabLabelPosition(labelPosition = position)
        }
        autoTextViewGitlabLabels.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListGitlabLabels.contains(autoTextViewGitlabLabels.editableText.toString())) {
                    if (arrayListGitlabLabels.isNotEmpty()) {
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = activity.resources.getString(R.string.textView_gitlab_label_doesnt_exist)
                        )
                        autoTextViewGitlabLabels.setText(arrayListGitlabLabels[0], false)
                    }
                }
            }
        }
    }


    /**
     * This method is used for initializing confidentiality spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabConfidentiality is used for getting the confidentiality list for confidentiality spinner.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeGitlabConfidentiality(
        arrayListGitlabConfidentiality: ArrayList<String>
    ) {
        autoTextViewGitlabConfidentialityAdapter = AutoCompleteTextViewGitlabConfidentialityAdapter(
            this,
            R.layout.auto_text_view_gitlab_confidentiality_item,
            arrayListGitlabConfidentiality
        )
        autoTextViewGitlabConfidentiality.setAdapter(autoTextViewGitlabConfidentialityAdapter)
        if (arrayListGitlabConfidentiality.isNotEmpty() && autoTextViewGitlabConfidentiality.text.isEmpty()) {
            autoTextViewGitlabConfidentiality.setText(arrayListGitlabConfidentiality[0], false)
        }
        autoTextViewGitlabConfidentiality.setOnTouchListener { v, event ->
            autoTextViewGitlabConfidentiality.showDropDown()
            false
        }
        autoTextViewGitlabConfidentiality.setOnItemClickListener { parent, view, position, id ->
            gitlabAuthentication.gitlabConfidentialityPosition(confidentialityPosition = position)
        }
        autoTextViewGitlabConfidentiality.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewGitlab)
        }
        autoTextViewGitlabConfidentiality.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListGitlabConfidentiality.contains(autoTextViewGitlabConfidentiality.editableText.toString())) {
                    if (arrayListGitlabConfidentiality.isNotEmpty()) {
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = activity.resources.getString(R.string.textView_gitlab_confidentiality_doesnt_exist)
                        )
                        autoTextViewGitlabConfidentiality.setText(
                            arrayListGitlabConfidentiality[0],
                            false
                        )
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing milestone spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabMilestones is used for getting the milestone list for milestone spinner.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeGitlabMilestones(
        arrayListGitlabMilestones: ArrayList<String>
    ) {
        autoTextViewGitlabMilestoneAdapter = AutoCompleteTextViewGitlabMilestoneAdapter(
            this,
            R.layout.auto_text_view_gitlab_milestone_item,
            arrayListGitlabMilestones
        )
        autoTextViewGitlabMilestone.setAdapter(autoTextViewGitlabMilestoneAdapter)
        if (arrayListGitlabMilestones.isNotEmpty() && autoTextViewGitlabMilestone.text.isEmpty()) {
            autoTextViewGitlabMilestone.setText(arrayListGitlabMilestones[0], false)
        }
        autoTextViewGitlabMilestone.setOnTouchListener { v, event ->
            autoTextViewGitlabMilestone.showDropDown()
            false
        }
        autoTextViewGitlabMilestone.setOnItemClickListener { parent, view, position, id ->
            gitlabAuthentication.gitlabMilestonesPosition(milestonePosition = position)
        }
        autoTextViewGitlabMilestone.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewGitlab)
        }

        autoTextViewGitlabMilestone.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListGitlabMilestones.contains(autoTextViewGitlabMilestone.editableText.toString())) {
                    if (arrayListGitlabMilestones.isNotEmpty()) {
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = activity.resources.getString(R.string.textView_gitlab_milestone_doesnt_exist)
                        )
                        autoTextViewGitlabMilestone.setText(arrayListGitlabMilestones[0], false)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkGitlabAssigneeEmpty(): Boolean {
        if (autoTextViewGitlabAssignee.text.toString().isNotEmpty()) {
            return true

        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.editText_gitlab_title_empty)
            )
        }
        return false
    }


    /**
     * This method is used for adding files to gitlab file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addGitlabFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListGitlabFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListGitlabFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListGitlabFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListGitlabFileName
    }

    /**
     * This method is used for title is not empty in gitlab layout.
     * @return Boolean value.
     */
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


    //Pivotal
    /**
     * This method is used for creating pivotal layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializePivotalLayout(filePathMedia: File) {
        try {
            removePivotalLayout()
            viewPivotal = LayoutInflater.from(activity)
                .inflate(
                    R.layout.loggerbird_pivotal_tracker_popup,
                    (this.rootView as ViewGroup),
                    false
                )
            windowManagerParamsPivotal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            windowManagerPivotal = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerPivotal as WindowManager).addView(
                viewPivotal,
                windowManagerParamsPivotal
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.black)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.black)

            buttonPivotalCancel = viewPivotal.findViewById(R.id.button_pivotal_cancel)
            buttonPivotalCreate = viewPivotal.findViewById(R.id.button_pivotal_create)
            toolbarPivotal = viewPivotal.findViewById(R.id.toolbar_pivotal)
            scrollViewPivotal = viewPivotal.findViewById(R.id.scrollView_pivotal)
            editTextPivotalTitle = viewPivotal.findViewById(R.id.editText_pivotal_title)
            autoTextViewPivotalStoryType =
                viewPivotal.findViewById(R.id.auto_textView_pivotal_story_type)
            autoTextViewPivotalPoints =
                viewPivotal.findViewById(R.id.auto_textView_pivotal_points)
            autoTextViewPivotalRequester =
                viewPivotal.findViewById(R.id.auto_textView_pivotal_requester)
            autoTextViewPivotalProject =
                viewPivotal.findViewById(R.id.auto_textView_pivotal_project)
            autoTextViewPivotalOwners =
                viewPivotal.findViewById(R.id.auto_textView_pivotal_owner)
            autoTextViewPivotalLabel =
                viewPivotal.findViewById(R.id.auto_textView_pivotal_label)
            imageViewPivotalOwners = viewPivotal.findViewById(R.id.imageView_owner_add)
            cardViewPivotalOwnersList = viewPivotal.findViewById(R.id.cardView_owner_list)
            recyclerViewPivotalOwnerList =
                viewPivotal.findViewById(R.id.recycler_view_pivotal_owner_list)
            editTextPivotalBlockers = viewPivotal.findViewById(R.id.editText_pivotal_blockers)
            imageViewPivotalBlockers = viewPivotal.findViewById(R.id.imageView_block_add)
            cardViewPivotalBlockersList = viewPivotal.findViewById(R.id.cardView_blockers_list)
            recyclerViewPivotalBlockersList =
                viewPivotal.findViewById(R.id.recycler_view_pivotal_blockers_list)
            editTextPivotalDescription =
                viewPivotal.findViewById(R.id.editText_pivotal_description)
            imageViewPivotalLabel = viewPivotal.findViewById(R.id.imageView_label_add)
            cardViewPivotalLabelList = viewPivotal.findViewById(R.id.cardView_label_list)
            recyclerViewPivotalLabelList =
                viewPivotal.findViewById(R.id.recycler_view_pivotal_label_list)
            editTextPivotalTasks = viewPivotal.findViewById(R.id.editText_pivotal_tasks)
            imageViewPivotalTask = viewPivotal.findViewById(R.id.imageView_task_add)
            cardViewPivotalTasksList = viewPivotal.findViewById(R.id.cardView_task_list)
            recyclerViewPivotalTaskList =
                viewPivotal.findViewById(R.id.recycler_view_pivotal_task_list)
            cardViewPivotalAttachments = viewPivotal.findViewById(R.id.cardView_attachment)
            recyclerViewPivotalAttachmentList =
                viewPivotal.findViewById(R.id.recycler_view_pivotal_attachment)

            scrollViewPivotal.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard(activity = activity, view = viewPivotal)
                }
                return@setOnTouchListener false
            }

            toolbarPivotal.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.pivotal_menu_save -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        with(sharedPref.edit()) {
                            putString(
                                "pivotal_project",
                                autoTextViewPivotalProject.editableText.toString()
                            )
                            putString(
                                "pivotal_label",
                                autoTextViewPivotalLabel.editableText.toString()
                            )
                            putString(
                                "pivotal_owner",
                                autoTextViewPivotalOwners.editableText.toString()
                            )
                            putString(
                                "pivotal_points",
                                autoTextViewPivotalPoints.editableText.toString()
                            )
                            putString(
                                "pivotal_requester",
                                autoTextViewPivotalRequester.editableText.toString()
                            )
                            putString(
                                "pivotal_story_type",
                                autoTextViewPivotalStoryType.editableText.toString()
                            )
                            putString("pivotal_title", editTextPivotalTitle.text.toString())
                            putString(
                                "pivotal_blockers",
                                editTextPivotalBlockers.text.toString()
                            )
                            putString(
                                "pivotal_description",
                                editTextPivotalDescription.text.toString()
                            )
                            putString("pivotal_tasks", editTextPivotalTasks.text.toString())
                            commit()
                        }
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.pivotal_issue_preferences_save)
                        )
                    }
                    R.id.pivotal_menu_clear -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        val editor: SharedPreferences.Editor = sharedPref.edit()
                        editor.remove("pivotal_project")
                        editor.remove("pivotal_label")
                        editor.remove("pivotal_owner")
                        editor.remove("pivotal_points")
                        editor.remove("pivotal_requester")
                        editor.remove("pivotal_story_type")
                        editor.remove("pivotal_title")
                        editor.remove("pivotal_blockers")
                        editor.remove("pivotal_description")
                        editor.remove("pivotal_tasks")
                        editor.apply()
                        clearPivotalComponents()
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.pivotal_issue_preferences_delete)
                        )
                    }
                }
                return@setOnMenuItemClickListener true
            }

            toolbarPivotal.setNavigationOnClickListener {
                removePivotalLayout()
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.VISIBLE
                }
            }
            initializePivotalAttachmentRecyclerView(filePathMedia = filePathMedia)
            initializePivotalTaskRecyclerView()
            initializePivotalBlockerRecyclerView()
            initializePivotalLabelRecyclerView()
            initializePivotalOwnerRecyclerView()
            buttonClicksPivotal()
            pivotalAuthentication.callPivotal(
                activity = activity,
                context = context,
                task = "get",
                filePathMedia = filePathMedia
            )
            attachProgressBar(task = "pivotal")
        } catch (e: Exception) {
            finishShareLayout("pivotal_error")
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.pivotalTag)
        }
    }

    /**
     * This method is used for removing loggerbird_pivotal_popup from window.
     */
    internal fun removePivotalLayout() {
        if (this::viewPivotal.isInitialized && windowManagerPivotal != null) {
            (windowManagerPivotal as WindowManager).removeViewImmediate(
                viewPivotal
            )
            windowManagerPivotal = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_pivotal_popup.
     */
    private fun buttonClicksPivotal() {
        buttonPivotalCreate.setSafeOnClickListener {
            pivotalAuthentication.gatherAutoTextDetails(
                autoTextViewProject = autoTextViewPivotalProject,
                autoTextViewStoryType = autoTextViewPivotalStoryType,
                autoTextViewLabel = autoTextViewPivotalLabel,
                autoTextViewPoints = autoTextViewPivotalPoints,
                autoTextViewOwners = autoTextViewPivotalOwners,
                autoTextViewRequester = autoTextViewPivotalRequester
            )
            pivotalAuthentication.gatherEditTextDetails(
                editTextTitle = editTextPivotalTitle,
                editTextTasks = editTextPivotalTasks,
                editTextDescription = editTextPivotalDescription,
                editTextBlockers = editTextPivotalBlockers
            )
            if (pivotalAuthentication.checkPivotalProject(
                    activity = activity,
                    autoTextViewProject = autoTextViewPivotalProject
                )
                && pivotalAuthentication.checkPivotalStoryType(
                    activity = activity,
                    autoTextViewStoryType = autoTextViewPivotalStoryType
                )
                && pivotalAuthentication.checkPivotalTitle(
                    activity = activity,
                    editTextTitle = editTextPivotalTitle
                )
                && pivotalAuthentication.checkPivotalLabel(
                    activity = activity,
                    autoTextViewLabel = autoTextViewPivotalLabel
                )
                && pivotalAuthentication.checkPivotalPoint(
                    activity = activity,
                    autoTextViewPoint = autoTextViewPivotalPoints
                )
                && pivotalAuthentication.checkPivotalOwner(
                    activity = activity,
                    autoTextViewOwner = autoTextViewPivotalOwners
                )
                && pivotalAuthentication.checkPivotalRequester(
                    activity = activity,
                    autoTextViewRequester = autoTextViewPivotalRequester
                )
            ) {
                attachProgressBar(task = "pivotal")
                pivotalAuthentication.callPivotal(
                    activity = activity,
                    context = context,
                    task = "create"
                )
            }

        }
        buttonPivotalCancel.setSafeOnClickListener {
            removePivotalLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
        imageViewPivotalTask.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewPivotal)
            if (!arrayListPivotalTaskName.contains(
                    RecyclerViewModelTask(
                        editTextPivotalTasks.text.toString()
                    )
                ) && editTextPivotalTasks.text.isNotEmpty()
            ) {
                arrayListPivotalTaskName.add(RecyclerViewModelTask(editTextPivotalTasks.text.toString()))
                pivotalTaskAdapter.notifyDataSetChanged()
                cardViewPivotalTasksList.visibility = View.VISIBLE
            } else if (arrayListPivotalTaskName.contains(
                    RecyclerViewModelTask(editTextPivotalTasks.text.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.pivotal_task_exist)
                )
            } else if (editTextPivotalTasks.text.isEmpty()) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.pivotal_task_empty)
                )
            }
        }
        imageViewPivotalBlockers.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewPivotal)
            if (!arrayListPivotalBlockerName.contains(
                    RecyclerViewModelBlocker(
                        editTextPivotalBlockers.text.toString()
                    )
                ) && editTextPivotalBlockers.text.isNotEmpty()
            ) {
                arrayListPivotalBlockerName.add(RecyclerViewModelBlocker(editTextPivotalBlockers.text.toString()))
                pivotalBlockerAdapter.notifyDataSetChanged()
                cardViewPivotalBlockersList.visibility = View.VISIBLE
            } else if (arrayListPivotalBlockerName.contains(
                    RecyclerViewModelBlocker(editTextPivotalBlockers.text.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.pivotal_blocker_exist)
                )
            } else if (editTextPivotalBlockers.text.isEmpty()) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.pivotal_blocker_empty)
                )
            }
        }
        imageViewPivotalLabel.setSafeOnClickListener {
            if (!arrayListPivotalLabelName.contains(
                    RecyclerViewModelLabel(
                        autoTextViewPivotalLabel.editableText.toString()
                    )
                ) && arrayListPivotalLabel.contains(
                    autoTextViewPivotalLabel.editableText.toString()
                )
            ) {
                arrayListPivotalLabelName.add(RecyclerViewModelLabel(autoTextViewPivotalLabel.editableText.toString()))
                pivotalLabelAdapter.notifyDataSetChanged()
                cardViewPivotalLabelList.visibility = View.VISIBLE
            } else if (arrayListPivotalLabelName.contains(
                    RecyclerViewModelLabel(autoTextViewPivotalLabel.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.pivotal_label_exist)
                )
            } else if (!arrayListPivotalLabel.contains(autoTextViewPivotalLabel.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.pivotal_label_doesnt_exist)
                )
            }

        }
        imageViewPivotalOwners.setSafeOnClickListener {
            if (!arrayListPivotalOwnerName.contains(
                    RecyclerViewModelOwner(
                        autoTextViewPivotalOwners.editableText.toString()
                    )
                ) && arrayListPivotalOwner.contains(
                    autoTextViewPivotalOwners.editableText.toString()
                )
            ) {
                arrayListPivotalOwnerName.add(RecyclerViewModelOwner(autoTextViewPivotalOwners.editableText.toString()))
                pivotalOwnerAdapter.notifyDataSetChanged()
                cardViewPivotalOwnersList.visibility = View.VISIBLE
            } else if (arrayListPivotalOwnerName.contains(
                    RecyclerViewModelOwner(autoTextViewPivotalOwners.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.pivotal_owner_exist)
                )
            } else if (!arrayListPivotalOwner.contains(autoTextViewPivotalOwners.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.pivotal_owner_doesnt_exist)
                )
            }

        }
    }

    /**
     * This method is used for initializing  autoCompleteTextViews in the loggerbird_pivotal_popup.
     * @param arrayListPivotalProject is used for getting the project list for project autoCompleteTextView.
     * @param arrayListPivotalStoryType is used for getting the story type list for story type autoCompleteTextView.
     * @param arrayListPivotalPoints is used for getting the points list for points autoCompleteTextView.
     * @param arrayListPivotalRequester is used for getting the request list for request autoCompleteTextView.
     * @param arrayListPivotalOwners is used for getting the owner list for owner autoCompleteTextView.
     * @param arrayListPivotalLabels is used for getting the label list for label autoCompleteTextView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    internal fun initializePivotalAutoTextViews(
        arrayListPivotalProject: ArrayList<String>,
        arrayListPivotalStoryType: ArrayList<String>,
        arrayListPivotalPoints: ArrayList<String>,
        arrayListPivotalRequester: ArrayList<String>,
        arrayListPivotalOwners: ArrayList<String>,
        arrayListPivotalLabels: ArrayList<String>
    ) {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        editTextPivotalTitle.setText(sharedPref.getString("pivotal_title", null))
        editTextPivotalTasks.setText(sharedPref.getString("pivotal_tasks", null))
        editTextPivotalDescription.setText(sharedPref.getString("pivotal_description", null))
        editTextPivotalBlockers.setText(sharedPref.getString("pivotal_blockers", null))
        initializePivotalProject(
            arrayListPivotalProject = arrayListPivotalProject,
            sharedPref = sharedPref
        )
        initializePivotalStoryType(
            arrayListPivotalStoryType = arrayListPivotalStoryType,
            sharedPref = sharedPref
        )
        initializePivotalPoints(
            arrayListPivotalPoints = arrayListPivotalPoints,
            sharedPref = sharedPref
        )
        initializePivotalRequester(
            arrayListPivotalRequester = arrayListPivotalRequester,
            sharedPref = sharedPref
        )
        initializePivotalOwners(
            arrayListPivotalOwners = arrayListPivotalOwners,
            sharedPref = sharedPref
        )
        initializePivotalLabel(
            arrayListPivotalLabel = arrayListPivotalLabels,
            sharedPref = sharedPref
        )
        detachProgressBar()
    }

    /**
     * This method is used for initializing project autoCompleteTextView in the loggerbird_pivotal_popup.
     * @param arrayListPivotalProject is used for getting the project list for project autoCompleteTextView.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializePivotalProject(
        arrayListPivotalProject: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewPivotalProjectAdapter = AutoCompleteTextViewPivotalProjectAdapter(
            this,
            R.layout.auto_text_view_pivotal_project_item,
            arrayListPivotalProject
        )
        autoTextViewPivotalProject.setAdapter(autoTextViewPivotalProjectAdapter)
        if (arrayListPivotalProject.isNotEmpty() && autoTextViewPivotalProject.editableText.isEmpty()) {
            if (sharedPref.getString("pivotal_project", null) != null) {
                if (arrayListPivotalProject.contains(
                        sharedPref.getString(
                            "pivotal_project",
                            null
                        )!!
                    )
                ) {
                    autoTextViewPivotalProject.setText(
                        sharedPref.getString("pivotal_project", null),
                        false
                    )
                } else {
                    autoTextViewPivotalProject.setText(arrayListPivotalProject[0], false)
                }
            } else {
                autoTextViewPivotalProject.setText(arrayListPivotalProject[0], false)
            }
        }
        autoTextViewPivotalProject.setOnTouchListener { v, event ->
            autoTextViewPivotalProject.showDropDown()
            false
        }
        autoTextViewPivotalProject.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewPivotal)
            clearPivotalComponents()
            pivotalAuthentication.setProjectPosition(projectPosition = position)
            pivotalAuthentication.callPivotal(
                activity = activity,
                context = context,
                task = "get"
            )
            attachProgressBar(task = "pivotal")
        }
    }

    /**
     * This method is used for initializing story type autoCompleteTextView in the loggerbird_pivotal_popup.
     * @param arrayListPivotalStoryType is used for getting the story type list for story type autoCompleteTextView.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializePivotalStoryType(
        arrayListPivotalStoryType: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewPivotalStoryTypeAdapter = AutoCompleteTextViewPivotalStoryTypeAdapter(
            this,
            R.layout.auto_text_view_pivotal_story_item,
            arrayListPivotalStoryType
        )
        autoTextViewPivotalStoryType.setAdapter(autoTextViewPivotalStoryTypeAdapter)
        if (arrayListPivotalStoryType.isNotEmpty() && autoTextViewPivotalStoryType.editableText.isEmpty()) {
            if (sharedPref.getString("pivotal_story_type", null) != null) {
                if (arrayListPivotalStoryType.contains(
                        sharedPref.getString(
                            "pivotal_story_type",
                            null
                        )!!
                    )
                ) {
                    autoTextViewPivotalStoryType.setText(
                        sharedPref.getString("pivotal_story_type", null),
                        false
                    )
                } else {
                    autoTextViewPivotalStoryType.setText(arrayListPivotalStoryType[0], false)
                }
            }
        }
        autoTextViewPivotalStoryType.setOnTouchListener { v, event ->
            autoTextViewPivotalStoryType.showDropDown()
            false
        }
        autoTextViewPivotalStoryType.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewPivotal)
        }
    }

    /**
     * This method is used for initializing points autoCompleteTextView in the loggerbird_pivotal_popup.
     * @param arrayListPivotalPoints is used for getting the points list for points autoCompleteTextView.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializePivotalPoints(
        arrayListPivotalPoints: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewPivotalPointsAdapter = AutoCompleteTextViewPivotalPointsAdapter(
            this,
            R.layout.auto_text_view_pivotal_points_item,
            arrayListPivotalPoints
        )
        autoTextViewPivotalPoints.setAdapter(autoTextViewPivotalPointsAdapter)
        if (arrayListPivotalPoints.isNotEmpty() && autoTextViewPivotalPoints.editableText.isEmpty()) {
            if (sharedPref.getString("pivotal_points", null) != null) {
                if (arrayListPivotalPoints.contains(
                        sharedPref.getString(
                            "pivotal_points",
                            null
                        )!!
                    )
                ) {
                    autoTextViewPivotalPoints.setText(
                        sharedPref.getString("pivotal_points", null),
                        false
                    )
                } else {
                    autoTextViewPivotalPoints.setText(arrayListPivotalPoints[0], false)
                }
            }
        }
        autoTextViewPivotalPoints.setOnTouchListener { v, event ->
            autoTextViewPivotalPoints.showDropDown()
            false
        }
        autoTextViewPivotalPoints.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewPivotal)
        }
    }

    /**
     * This method is used for initializing requester autoCompleteTextView in the loggerbird_pivotal_popup.
     * @param arrayListPivotalRequester is used for getting the requester list for requester autoCompleteTextView.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializePivotalRequester(
        arrayListPivotalRequester: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewPivotalRequesterAdapter = AutoCompleteTextViewPivotalRequesterAdapter(
            this,
            R.layout.auto_text_view_pivotal_requester_item,
            arrayListPivotalRequester
        )
        autoTextViewPivotalRequester.setAdapter(autoTextViewPivotalRequesterAdapter)
        if (arrayListPivotalRequester.isNotEmpty() && autoTextViewPivotalRequester.editableText.isEmpty()) {
            if (sharedPref.getString("pivotal_requester", null) != null) {
                if (arrayListPivotalRequester.contains(
                        sharedPref.getString(
                            "pivotal_requester",
                            null
                        )!!
                    )
                ) {
                    autoTextViewPivotalRequester.setText(
                        sharedPref.getString("pivotal_requester", null),
                        false
                    )
                } else {
                    autoTextViewPivotalRequester.setText(arrayListPivotalRequester[0], false)
                }
            }
        }
        autoTextViewPivotalRequester.setOnTouchListener { v, event ->
            autoTextViewPivotalRequester.showDropDown()
            false
        }
        autoTextViewPivotalRequester.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewPivotal)
        }
    }

    /**
     * This method is used for initializing owners autoCompleteTextView in the loggerbird_pivotal_popup.
     * @param arrayListPivotalOwners is used for getting the owner list for owner autoCompleteTextView.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializePivotalOwners(
        arrayListPivotalOwners: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewPivotalOwnersAdapter = AutoCompleteTextViewPivotalOwnersAdapter(
            this,
            R.layout.auto_text_view_pivotal_owners_item,
            arrayListPivotalOwners
        )
        autoTextViewPivotalOwners.setAdapter(autoTextViewPivotalOwnersAdapter)
        if (arrayListPivotalOwners.isNotEmpty() && autoTextViewPivotalOwners.editableText.isEmpty()) {
            if (sharedPref.getString("pivotal_owner", null) != null) {
                if (arrayListPivotalOwners.contains(
                        sharedPref.getString(
                            "pivotal_owner",
                            null
                        )!!
                    )
                ) {
                    autoTextViewPivotalOwners.setText(
                        sharedPref.getString("pivotal_owner", null),
                        false
                    )
                } else {
                    autoTextViewPivotalOwners.setText(arrayListPivotalOwners[0], false)
                }
            }
        }
        autoTextViewPivotalOwners.setOnTouchListener { v, event ->
            autoTextViewPivotalOwners.showDropDown()
            false
        }
        autoTextViewPivotalOwners.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewPivotal)
        }
        this.arrayListPivotalOwner = arrayListPivotalOwners
    }


    /**
     * This method is used for initializing label autoCompleteTextView in the loggerbird_pivotal_popup.
     * @param arrayListPivotalLabel is used for getting the label list for label autoCompleteTextView.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializePivotalLabel(
        arrayListPivotalLabel: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewPivotalLabelAdapter = AutoCompleteTextViewPivotalLabelAdapter(
            this,
            R.layout.auto_text_view_pivotal_label_item,
            arrayListPivotalLabel
        )
        autoTextViewPivotalLabel.setAdapter(autoTextViewPivotalLabelAdapter)
        if (arrayListPivotalLabel.isNotEmpty() && autoTextViewPivotalLabel.editableText.isEmpty()) {
            if (sharedPref.getString("pivotal_label", null) != null) {
                if (arrayListPivotalLabel.contains(
                        sharedPref.getString(
                            "pivotal_label",
                            null
                        )!!
                    )
                ) {
                    autoTextViewPivotalLabel.setText(
                        sharedPref.getString("pivotal_label", null),
                        false
                    )
                } else {
                    autoTextViewPivotalLabel.setText(arrayListPivotalLabel[0], false)
                }
            }
        }
        autoTextViewPivotalLabel.setOnTouchListener { v, event ->
            autoTextViewPivotalLabel.showDropDown()
            false
        }
        autoTextViewPivotalLabel.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewPivotal)
        }
        this.arrayListPivotalLabel = arrayListPivotalLabel
    }

    /**
     * This method is used for initializing pivotal attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializePivotalAttachmentRecyclerView(filePathMedia: File) {
        arrayListPivotalFileName.clear()
        recyclerViewPivotalAttachmentList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pivotalAttachmentAdapter =
            RecyclerViewPivotalAttachmentAdapter(
                addPivotalFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewPivotalAttachmentList.adapter = pivotalAttachmentAdapter
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addPivotalFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListPivotalFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListPivotalFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListPivotalFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListPivotalFileName
    }

    /**
     * This method is used for initializing pivotal task recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializePivotalTaskRecyclerView() {
        arrayListPivotalTaskName.clear()
        recyclerViewPivotalTaskList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pivotalTaskAdapter =
            RecyclerViewPivotalTaskAdapter(
                arrayListPivotalTaskName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewPivotalTaskList.adapter = pivotalTaskAdapter
    }

    /**
     * This method is used for initializing pivotal blocker recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializePivotalBlockerRecyclerView() {
        arrayListPivotalBlockerName.clear()
        recyclerViewPivotalBlockersList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pivotalBlockerAdapter =
            RecyclerViewPivotalBlockerAdapter(
                arrayListPivotalBlockerName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewPivotalBlockersList.adapter = pivotalBlockerAdapter
    }

    /**
     * This method is used for initializing pivotal label recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializePivotalLabelRecyclerView() {
        arrayListPivotalLabelName.clear()
        recyclerViewPivotalLabelList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pivotalLabelAdapter =
            RecyclerViewPivotalLabelAdapter(
                arrayListPivotalLabelName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewPivotalLabelList.adapter = pivotalLabelAdapter
    }

    /**
     * This method is used for initializing pivotal owner recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializePivotalOwnerRecyclerView() {
        arrayListPivotalOwnerName.clear()
        recyclerViewPivotalOwnerList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pivotalOwnerAdapter =
            RecyclerViewPivotalOwnerAdapter(
                arrayListPivotalOwnerName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewPivotalOwnerList.adapter = pivotalOwnerAdapter
    }

    /**
     * This method is used for clearing pivotal components.
     */
    private fun clearPivotalComponents() {
        cardViewPivotalBlockersList.visibility = View.GONE
        cardViewPivotalTasksList.visibility = View.GONE
        cardViewPivotalLabelList.visibility = View.GONE
        cardViewPivotalOwnersList.visibility = View.GONE
        arrayListPivotalOwnerName.clear()
        arrayListPivotalLabelName.clear()
        arrayListPivotalTaskName.clear()
        arrayListPivotalBlockerName.clear()
        pivotalOwnerAdapter.notifyDataSetChanged()
        pivotalLabelAdapter.notifyDataSetChanged()
        pivotalBlockerAdapter.notifyDataSetChanged()
        pivotalTaskAdapter.notifyDataSetChanged()
        editTextPivotalBlockers.text = null
        editTextPivotalDescription.text = null
        editTextPivotalTasks.text = null
        editTextPivotalTitle.text = null
        autoTextViewPivotalOwners.setText("", false)
        autoTextViewPivotalLabel.setText("", false)
        autoTextViewPivotalRequester.setText("", false)
        autoTextViewPivotalPoints.setText("", false)
        autoTextViewPivotalStoryType.setText("", false)
//        autoTextViewPivotalProject.setText("",false)
    }

    //Basecamp
    /**
     * This method is used for creating basecamp layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeBasecampLayout(filePathMedia: File) {
        try {
            removeBasecampLayout()
            viewBasecamp = LayoutInflater.from(activity)
                .inflate(
                    R.layout.loggerbird_basecamp_popup,
                    (this.rootView as ViewGroup),
                    false
                )
            windowManagerParamsBaseCamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            windowManagerBasecamp = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerBasecamp as WindowManager).addView(
                viewBasecamp,
                windowManagerParamsBaseCamp
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.black)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.black)

            buttonBasecampCancel = viewBasecamp.findViewById(R.id.button_basecamp_cancel)
            buttonBasecampCreate = viewBasecamp.findViewById(R.id.button_basecamp_create)
            toolbarBasecamp = viewBasecamp.findViewById(R.id.toolbar_basecamp)
            autoTextViewBasecampProject =
                viewBasecamp.findViewById(R.id.auto_textView_basecamp_project)
            autoTextViewBasecampAssignee =
                viewBasecamp.findViewById(R.id.auto_textView_basecamp_assignee)
            autoTextViewBasecampCategory =
                viewBasecamp.findViewById(R.id.auto_textView_basecamp_category)
            autoTextViewBasecampNotify =
                viewBasecamp.findViewById(R.id.auto_textView_basecamp_notify)
            editTextBasecampDescriptionMessage =
                viewBasecamp.findViewById(R.id.editText_basecamp_description_messsage)
            editTextBasecampDescriptionTodo =
                viewBasecamp.findViewById(R.id.editText_basecamp_description_todo)
            editTextBasecampTitle =
                viewBasecamp.findViewById(R.id.editText_basecamp_title)
            editTextBasecampContent = viewBasecamp.findViewById(R.id.editText_basecamp_content)
            editTextBasecampName = viewBasecamp.findViewById(R.id.editText_basecamp_name)
            imageViewBasecampAssignee = viewBasecamp.findViewById(R.id.imageView_assignee_add)
            imageViewBasecampNotify = viewBasecamp.findViewById(R.id.imageView_notify_add)
            cardViewBasecampAssigneeList =
                viewBasecamp.findViewById(R.id.cardView_assignee_list)
            cardViewBasecampNotifyList = viewBasecamp.findViewById(R.id.cardView_notify_list)
            recyclerViewBasecampAssigneeList =
                viewBasecamp.findViewById(R.id.recycler_view_basecamp_assignee_list)
            recyclerViewBasecampNotifyList =
                viewBasecamp.findViewById(R.id.recycler_view_basecamp_notify_list)
            recyclerViewBasecampAttachmentList =
                viewBasecamp.findViewById(R.id.recycler_view_basecamp_attachment)
            imageButtonBasecampRemoveDate =
                viewBasecamp.findViewById(R.id.image_button_basecamp_remove_date)
            imageViewBasecampDate = viewBasecamp.findViewById(R.id.imageView_start_date)
            scrollViewBasecamp = viewBasecamp.findViewById(R.id.scrollView_basecamp)
            scrollViewBasecamp.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard(activity = activity, view = viewBasecamp)
                }
                return@setOnTouchListener false
            }

            toolbarBasecamp.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.basecamp_menu_save -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        with(sharedPref.edit()) {
                            putString(
                                "basecamp_project",
                                autoTextViewBasecampProject.editableText.toString()
                            )
                            putString(
                                "basecamp_assignee",
                                autoTextViewBasecampAssignee.editableText.toString()
                            )
                            putString(
                                "basecamp_category",
                                autoTextViewBasecampCategory.editableText.toString()
                            )
                            putString(
                                "basecamp_notify",
                                autoTextViewBasecampNotify.editableText.toString()
                            )
                            putString(
                                "basecamp_description_message",
                                editTextBasecampDescriptionMessage.text.toString()
                            )
                            putString(
                                "basecamp_description_todo",
                                editTextBasecampDescriptionTodo.text.toString()
                            )
                            putString(
                                "basecamp_title",
                                editTextBasecampTitle.text.toString()
                            )
                            putString(
                                "basecamp_content",
                                editTextBasecampContent.text.toString()
                            )
                            putString("basecamp_name", editTextBasecampName.text.toString())
                            commit()
                        }
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.basecamp_issue_preferences_save)
                        )
                    }
                    R.id.basecamp_menu_clear -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        val editor: SharedPreferences.Editor = sharedPref.edit()
                        editor.remove("basecamp_project")
                        editor.remove("basecamp_assignee")
                        editor.remove("basecamp_category")
                        editor.remove("basecamp_notify")
                        editor.remove("basecamp_description_message")
                        editor.remove("basecamp_description_todo")
                        editor.remove("basecamp_title")
                        editor.remove("basecamp_content")
                        editor.remove("basecamp_name")
                        editor.apply()
                        clearBasecampComponents()
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.basecamp_issue_preferences_delete)
                        )
                    }
                }
                return@setOnMenuItemClickListener true
            }

            toolbarBasecamp.setNavigationOnClickListener {
                removeBasecampLayout()
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.VISIBLE
                }
            }
            initializeBasecampAttachmentRecyclerView(filePathMedia = filePathMedia)
            initializeBasecampAssigneeRecyclerView()
            initializeBasecampNotifyRecyclerView()
            buttonClicksBasecamp()
            basecampAuthentication.callBasecamp(
                activity = activity,
                context = context,
                task = "get",
                filePathMedia = filePathMedia
            )
            attachProgressBar(task = "basecamp")
        } catch (e: Exception) {
            finishShareLayout("basecamp_error")
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.basecampTag)
        }
    }

    /**
     * This method is used for removing loggerbird_basecamp_popup from window.
     */
    internal fun removeBasecampLayout() {
        if (this::viewBasecamp.isInitialized && windowManagerBasecamp != null) {
            (windowManagerBasecamp as WindowManager).removeViewImmediate(
                viewBasecamp
            )
            windowManagerBasecamp = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_basecamp_popup.
     */
    private fun buttonClicksBasecamp() {
        buttonBasecampCreate.setSafeOnClickListener {
            basecampAuthentication.gatherAutoTextDetails(
                autoTextViewProject = autoTextViewBasecampProject,
                autoTextViewAssignee = autoTextViewBasecampAssignee,
                autoTextViewCategory = autoTextViewBasecampCategory,
                autoTextViewNotify = autoTextViewBasecampNotify
            )
            basecampAuthentication.gatherEditTextDetails(
                editTextTitle = editTextBasecampTitle,
                editTextDescriptionMessage = editTextBasecampDescriptionMessage,
                editTextDescriptionTodo = editTextBasecampDescriptionTodo,
                editTextContent = editTextBasecampContent,
                editTextName = editTextBasecampName
            )
            if (basecampAuthentication.checkBasecampProject(
                    activity = activity,
                    autoTextViewProject = autoTextViewBasecampProject
                )
                && basecampAuthentication.checkBasecampTitle(
                    activity = activity,
                    editTextTitle = editTextBasecampTitle,
                    autoTextViewCategory = autoTextViewBasecampCategory,
                    editTextDescriptionMessage = editTextBasecampDescriptionMessage
                )
                && basecampAuthentication.checkBasecampTodo(
                    activity = activity,
                    editTextName = editTextBasecampName,
                    editTextDescriptionTodo = editTextBasecampDescriptionTodo,
                    editTextContent = editTextBasecampContent,
                    autoTextViewNotify = autoTextViewBasecampNotify,
                    autoTextViewAssignee = autoTextViewBasecampAssignee
                )
            ) {
                basecampAuthentication.callBasecamp(
                    activity = activity,
                    context = context,
                    task = "create"
                )
                attachProgressBar(task = "basecamp")
            }
        }
        buttonBasecampCancel.setSafeOnClickListener {
            removeBasecampLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
        imageViewBasecampAssignee.setSafeOnClickListener {
            if (!arrayListBasecampAssigneeName.contains(
                    RecyclerViewModelAssignee(
                        autoTextViewBasecampAssignee.editableText.toString()
                    )
                ) && arrayListBasecampAssignee.contains(
                    autoTextViewBasecampAssignee.editableText.toString()
                )
            ) {
                arrayListBasecampAssigneeName.add(
                    RecyclerViewModelAssignee(
                        autoTextViewBasecampAssignee.editableText.toString()
                    )
                )
                basecampAssigneeAdapter.notifyDataSetChanged()
                cardViewBasecampAssigneeList.visibility = View.VISIBLE
            } else if (arrayListBasecampAssigneeName.contains(
                    RecyclerViewModelAssignee(autoTextViewBasecampAssignee.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.basecamp_assignee_exist)
                )
            } else if (!arrayListBasecampAssignee.contains(autoTextViewBasecampAssignee.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.basecamp_assignee_doesnt_exist)
                )
            }

        }
        imageViewBasecampNotify.setSafeOnClickListener {
            if (!arrayListBasecampNotifyName.contains(
                    RecyclerViewModelNotify(
                        autoTextViewBasecampNotify.editableText.toString()
                    )
                ) && arrayListBasecampNotify.contains(
                    autoTextViewBasecampNotify.editableText.toString()
                )
            ) {
                arrayListBasecampNotifyName.add(RecyclerViewModelNotify(autoTextViewBasecampNotify.editableText.toString()))
                basecampNotifyAdapter.notifyDataSetChanged()
                cardViewBasecampNotifyList.visibility = View.VISIBLE
            } else if (arrayListBasecampNotifyName.contains(
                    RecyclerViewModelNotify(autoTextViewBasecampNotify.editableText.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.basecamp_notify_exist)
                )
            } else if (!arrayListBasecampNotify.contains(autoTextViewBasecampNotify.editableText.toString())) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.basecamp_notify_doesnt_exist)
                )
            }

        }
        imageButtonBasecampRemoveDate.setSafeOnClickListener {
            imageButtonBasecampRemoveDate.visibility = View.GONE
            basecampAuthentication.setStartDate(startDate = null)
        }
        imageViewBasecampDate.setSafeOnClickListener {
            initializeBasecampDateLayout()
        }
    }

    /**
     * This method is used for initializing  autoCompleteTextViews in the loggerbird_basecamp_popup.
     * @param arrayListBasecampProject is used for getting the project list for project autoCompleteTextView.
     * @param arrayListBasecampCategory is used for getting the category list for category autoCompleteTextView.
     * @param arrayListBasecampCategoryIcon is used for getting the category icon list.
     * @param arrayListBasecampAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param arrayListBasecampNotify is used for getting the notify list for notify autoCompleteTextView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    internal fun initializeBasecampAutoTextViews(
        arrayListBasecampProject: ArrayList<String>,
        arrayListBasecampCategory: ArrayList<String>,
        arrayListBasecampCategoryIcon: ArrayList<String>,
        arrayListBasecampAssignee: ArrayList<String>,
        arrayListBasecampNotify: ArrayList<String>
    ) {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        editTextBasecampDescriptionMessage.setText(
            sharedPref.getString(
                "basecamp_description_message",
                null
            )
        )
        editTextBasecampDescriptionTodo.setText(
            sharedPref.getString(
                "basecamp_description_todo",
                null
            )
        )
        editTextBasecampTitle.setText(
            sharedPref.getString(
                "basecamp_title",
                null
            )
        )
        editTextBasecampContent.setText(
            sharedPref.getString(
                "basecamp_content",
                null
            )
        )
        editTextBasecampName.setText(
            sharedPref.getString(
                "basecamp_name",
                null
            )
        )
        initializeBasecampProject(
            arrayListBasecampProject = arrayListBasecampProject,
            sharedPref = sharedPref
        )
        initializeBasecampCategory(
            arrayListBasecampCategory = arrayListBasecampCategory,
            arrayListBasecampCategoryIcon = arrayListBasecampCategoryIcon,
            sharedPref = sharedPref
        )
        initializeBasecampAssignee(
            arrayListBasecampAssignee = arrayListBasecampAssignee,
            sharedPref = sharedPref
        )
        initializeBasecampNotify(
            arrayListBasecampNotify = arrayListBasecampNotify,
            sharedPref = sharedPref
        )
        detachProgressBar()
    }

    /**
     * This method is used for initializing project autoCompleteTextView in the loggerbird_basecamp_popup.
     * @param arrayListBasecampProject is used for getting the project list for project autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeBasecampProject(
        arrayListBasecampProject: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewBasecampProjectAdapter = AutoCompleteTextViewBasecampProjectAdapter(
            this,
            R.layout.auto_text_view_basecamp_project_item,
            arrayListBasecampProject
        )
        autoTextViewBasecampProject.setAdapter(autoTextViewBasecampProjectAdapter)
        if (arrayListBasecampProject.isNotEmpty() && autoTextViewBasecampProject.editableText.isEmpty()) {
            if (sharedPref.getString("basecamp_project", null) != null) {
                if (arrayListBasecampProject.contains(
                        sharedPref.getString(
                            "basecamp_project",
                            null
                        )!!
                    )
                ) {
                    autoTextViewBasecampProject.setText(
                        sharedPref.getString("basecamp_project", null),
                        false
                    )
                } else {
                    autoTextViewBasecampProject.setText(arrayListBasecampProject[0], false)
                }
            } else {
                autoTextViewBasecampProject.setText(arrayListBasecampProject[0], false)
            }
        }
        autoTextViewBasecampProject.setOnTouchListener { v, event ->
            autoTextViewBasecampProject.showDropDown()
            false
        }
        autoTextViewBasecampProject.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewBasecamp)
            clearBasecampComponents()
            basecampAuthentication.setProjectPosition(projectPosition = position)
            basecampAuthentication.callBasecamp(
                activity = activity,
                context = context,
                task = "get"
            )
            attachProgressBar(task = "basecamp")
        }
    }

    /**
     * This method is used for initializing category autoCompleteTextView in the loggerbird_basecamp_popup.
     * @param arrayListBasecampCategory is used for getting the category list for category autoCompleteTextView.
     * @param arrayListBasecampCategoryIcon is used for getting the category icon list.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeBasecampCategory(
        arrayListBasecampCategory: ArrayList<String>,
        arrayListBasecampCategoryIcon: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewBasecampCategoryAdapter =
            AutoCompleteTextViewBasecampCategoryAdapter(
                this,
                R.layout.auto_text_view_basecamp_category_item,
                arrayListBasecampCategory,
                arrayListBasecampCategoryIcon
            )
        autoTextViewBasecampCategory.setAdapter(autoTextViewBasecampCategoryAdapter)
        if (arrayListBasecampCategory.isNotEmpty() && autoTextViewBasecampCategory.editableText.isEmpty()) {
            if (sharedPref.getString("basecamp_category", null) != null) {
                if (arrayListBasecampCategory.contains(
                        sharedPref.getString(
                            "basecamp_category",
                            null
                        )!!
                    )
                ) {
                    autoTextViewBasecampCategory.setText(
                        sharedPref.getString("basecamp_category", null),
                        false
                    )
                } else {
                    autoTextViewBasecampCategory.setText(arrayListBasecampCategory[0], false)
                }
            }
        }
        autoTextViewBasecampCategory.setOnTouchListener { v, event ->
            autoTextViewBasecampCategory.showDropDown()
            false
        }
        autoTextViewBasecampCategory.setOnItemClickListener { parent, view, position, id ->
            basecampAuthentication.setCategoryPosition(categoryPosition = position)
            hideKeyboard(activity = activity, view = viewBasecamp)
        }
    }

    /**
     * This method is used for initializing notify autoCompleteTextView in the loggerbird_basecamp_popup.
     * @param arrayListBasecampAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeBasecampAssignee(
        arrayListBasecampAssignee: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewBasecampAssigneeAdapter = AutoCompleteTextViewBasecampAssigneeAdapter(
            this,
            R.layout.auto_text_view_basecamp_assignee_item,
            arrayListBasecampAssignee
        )
        autoTextViewBasecampAssignee.setAdapter(autoTextViewBasecampAssigneeAdapter)
        if (arrayListBasecampAssignee.isNotEmpty() && autoTextViewBasecampAssignee.editableText.isEmpty()) {
            if (sharedPref.getString("basecamp_assignee", null) != null) {
                if (arrayListBasecampAssignee.contains(
                        sharedPref.getString(
                            "basecamp_assignee",
                            null
                        )!!
                    )
                ) {
                    autoTextViewBasecampAssignee.setText(
                        sharedPref.getString("basecamp_assignee", null),
                        false
                    )
                } else {
                    autoTextViewBasecampAssignee.setText(arrayListBasecampAssignee[0], false)
                }
            }
        }
        autoTextViewBasecampAssignee.setOnTouchListener { v, event ->
            autoTextViewBasecampAssignee.showDropDown()
            false
        }
        autoTextViewBasecampAssignee.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewBasecamp)
        }
        this.arrayListBasecampAssignee = arrayListBasecampAssignee
    }

    /**
     * This method is used for initializing notify autoCompleteTextView in the loggerbird_basecamp_popup.
     * @param arrayListBasecampNotify is used for getting the notify list for notify autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeBasecampNotify(
        arrayListBasecampNotify: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewBasecampNotifyAdapter = AutoCompleteTextViewBasecampNotifyAdapter(
            this,
            R.layout.auto_text_view_basecamp_notify_item,
            arrayListBasecampNotify
        )
        autoTextViewBasecampNotify.setAdapter(autoTextViewBasecampNotifyAdapter)
        if (arrayListBasecampNotify.isNotEmpty() && autoTextViewBasecampNotify.editableText.isEmpty()) {
            if (sharedPref.getString("basecamp_notify", null) != null) {
                if (arrayListBasecampNotify.contains(
                        sharedPref.getString(
                            "basecamp_notify",
                            null
                        )!!
                    )
                ) {
                    autoTextViewBasecampNotify.setText(
                        sharedPref.getString("basecamp_notify", null),
                        false
                    )
                } else {
                    autoTextViewBasecampNotify.setText(arrayListBasecampNotify[0], false)
                }
            }
        }
        autoTextViewBasecampNotify.setOnTouchListener { v, event ->
            autoTextViewBasecampNotify.showDropDown()
            false
        }
        autoTextViewBasecampNotify.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewBasecamp)
        }
        this.arrayListBasecampNotify = arrayListBasecampNotify
    }

    /**
     * This method is used for initializing basecamp attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeBasecampAttachmentRecyclerView(filePathMedia: File) {
        arrayListBasecampFileName.clear()
        recyclerViewBasecampAttachmentList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        basecampAttachmentAdapter =
            RecyclerViewBasecampAttachmentAdapter(
                addBasecampFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewBasecampAttachmentList.adapter = basecampAttachmentAdapter
    }

    /**
     * This method is used for adding files to basecamp file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addBasecampFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListBasecampFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListBasecampFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListBasecampFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListBasecampFileName
    }

    /**
     * This method is used for initializing basecamp assignee recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeBasecampAssigneeRecyclerView() {
        arrayListBasecampAssigneeName.clear()
        recyclerViewBasecampAssigneeList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        basecampAssigneeAdapter =
            RecyclerViewBasecampAssigneeAdapter(
                arrayListBasecampAssigneeName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewBasecampAssigneeList.adapter = basecampAssigneeAdapter
    }

    /**
     * This method is used for initializing basecamp notify recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeBasecampNotifyRecyclerView() {
        arrayListBasecampNotifyName.clear()
        recyclerViewBasecampNotifyList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        basecampNotifyAdapter =
            RecyclerViewBasecampNotifyAdapter(
                arrayListBasecampNotifyName,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewBasecampNotifyList.adapter = basecampNotifyAdapter
    }

    /**
     * This method is used for clearing basecamp components.
     */
    private fun clearBasecampComponents() {
        cardViewBasecampAssigneeList.visibility = View.GONE
        cardViewBasecampNotifyList.visibility = View.GONE
        arrayListBasecampNotifyName.clear()
        arrayListBasecampAssigneeName.clear()
        arrayListBasecampAssignee.clear()
        arrayListBasecampNotify.clear()
        basecampAssigneeAdapter.notifyDataSetChanged()
        basecampNotifyAdapter.notifyDataSetChanged()
        editTextBasecampDescriptionTodo.text = null
        editTextBasecampDescriptionMessage.text = null
        editTextBasecampTitle.text = null
        editTextBasecampContent.text = null
        editTextBasecampName.text = null
        autoTextViewBasecampCategory.setText("", false)
        autoTextViewBasecampNotify.setText("", false)
        autoTextViewBasecampAssignee.setText("", false)
//        autoTextViewPivotalProject.setText("",false)
    }

    /**
     * This method is used for creating basecamp-date layout which is attached to application overlay.
     */
    private fun initializeBasecampDateLayout() {
        removeBasecampDateLayout()
//        calendarTrello = Calendar.getInstance()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewBasecampDate =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LayoutInflater.from(activity)
                    .inflate(R.layout.basecamp_calendar_view, rootView, false)
            } else {
                LayoutInflater.from(activity)
                    .inflate(R.layout.basecamp_calendar_view_lower, rootView, false)
            }
        windowManagerParamsBaseCampDate =
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
        windowManagerBasecampDate = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerBasecampDate as WindowManager).addView(
            viewBasecampDate,
            windowManagerParamsBaseCampDate
        )
        frameLayoutBasecampDate = viewBasecampDate.findViewById(R.id.basecamp_calendar_view_layout)
        calendarViewBasecamp = viewBasecampDate.findViewById(R.id.calendarView_start_date)
        buttonBasecampDateCancel =
            viewBasecampDate.findViewById(R.id.button_basecamp_calendar_cancel)
        buttonBasecampDateCreate = viewBasecampDate.findViewById(R.id.button_basecamp_calendar_ok)
        buttonClicksBasecampDateLayout()
    }

    private fun removeBasecampDateLayout() {
        if (this::viewBasecampDate.isInitialized && windowManagerBasecampDate != null) {
            (windowManagerBasecampDate as WindowManager).removeViewImmediate(
                viewBasecampDate
            )
            windowManagerBasecampDate = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the basecamp_calendar_view.
     */
    private fun buttonClicksBasecampDateLayout() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        var startDate = "$mYear-$mMonth-$mDayOfMonth"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            calendarViewBasecamp.minDate = System.currentTimeMillis()
        }
        frameLayoutBasecampDate.setOnClickListener {
            removeBasecampDateLayout()
        }
        calendarViewBasecamp.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val tempMonth = month + 1
            startDate = "$year-$tempMonth-$dayOfMonth"
        }
        buttonBasecampDateCreate.setSafeOnClickListener {
            imageButtonBasecampRemoveDate.visibility = View.VISIBLE
            basecampAuthentication.setStartDate(startDate = startDate)
            removeBasecampDateLayout()
        }
        buttonBasecampDateCancel.setSafeOnClickListener {
            removeBasecampDateLayout()
        }
    }


    //Asana
    /**
     * This method is used for creating asana layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeAsanaLayout(filePathMedia: File) {
        try {
            removeAsanaLayout()
            viewAsana = LayoutInflater.from(activity)
                .inflate(
                    R.layout.loggerbird_asana_popup,
                    (this.rootView as ViewGroup),
                    false
                )
            windowManagerParamsAsana = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            windowManagerAsana = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerAsana as WindowManager).addView(
                viewAsana,
                windowManagerParamsAsana
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.black)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.black)

            buttonAsanaCancel = viewAsana.findViewById(R.id.button_asana_cancel)
            buttonAsanaCreate = viewAsana.findViewById(R.id.button_asana_create)
            toolbarAsana = viewAsana.findViewById(R.id.toolbar_asana)
            autoTextViewAsanaProject = viewAsana.findViewById(R.id.auto_textView_asana_project)
            autoTextViewAsanaAssignee =
                viewAsana.findViewById(R.id.auto_textView_asana_assignee)
            autoTextViewAsanaSector =
                viewAsana.findViewById(R.id.auto_textView_asana_section)
            autoTextViewAsanaPriority =
                viewAsana.findViewById(R.id.auto_textView_asana_priority)
            editTextAsanaDescription = viewAsana.findViewById(R.id.editText_asana_description)
            editTextAsanaSubTask = viewAsana.findViewById(R.id.editText_asana_sub_tasks)
            editTextAsanaTaskName = viewAsana.findViewById(R.id.editText_asana_task_name)
            imageViewAsanaStartDate = viewAsana.findViewById(R.id.imageView_start_date)
            imageViewAsanaTaskAdd = viewAsana.findViewById(R.id.imageView_task_add)
            imageButtonAsanaRemoveDate =
                viewAsana.findViewById(R.id.image_button_asana_remove_date)
            recyclerViewAsanaAttachmentList =
                viewAsana.findViewById(R.id.recycler_view_asana_attachment)
            recyclerViewAsanaSubTasksList =
                viewAsana.findViewById(R.id.recycler_view_asana_sub_tasks_list)
            cardViewAsanaSubTasksList = viewAsana.findViewById(R.id.cardView_sub_tasks_list)
            scrollViewAsana = viewAsana.findViewById(R.id.scrollView_asana)
            scrollViewAsana.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard(activity = activity, view = viewAsana)
                }
                return@setOnTouchListener false
            }

            toolbarAsana.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.asana_menu_save -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        with(sharedPref.edit()) {
                            putString(
                                "asana_project",
                                autoTextViewAsanaProject.editableText.toString()
                            )
                            putString(
                                "asana_assignee",
                                autoTextViewAsanaAssignee.editableText.toString()
                            )
                            putString(
                                "asana_section",
                                autoTextViewAsanaSector.editableText.toString()
                            )
                            putString(
                                "asana_priority",
                                autoTextViewAsanaPriority.editableText.toString()
                            )
                            putString(
                                "asana_description",
                                editTextAsanaDescription.text.toString()
                            )
                            putString(
                                "asana_subtask",
                                editTextAsanaSubTask.text.toString()
                            )
                            putString(
                                "asana_task_name",
                                editTextAsanaTaskName.text.toString()
                            )
                            commit()
                        }
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.asana_issue_preferences_save)
                        )
                    }
                    R.id.asana_menu_clear -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        val editor: SharedPreferences.Editor = sharedPref.edit()
                        editor.remove("asana_project")
                        editor.remove("asana_assignee")
                        editor.remove("asana_section")
                        editor.remove("asana_priority")
                        editor.remove("asana_description")
                        editor.remove("asana_subtask")
                        editor.remove("asana_task_name")
                        editor.apply()
                        clearAsanaComponents()
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.asana_issue_preferences_delete)
                        )
                    }
                }
                return@setOnMenuItemClickListener true
            }

            toolbarAsana.setNavigationOnClickListener {
                removeAsanaLayout()
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.VISIBLE
                }
            }
            initializeAsanaAttachmentRecyclerView(filePathMedia = filePathMedia)
            buttonClicksAsana()
            asanaAuthentication.callAsana(
                activity = activity,
                context = context,
                task = "get",
                filePathMedia = filePathMedia
            )
            attachProgressBar(task = "asana")
        } catch (e: Exception) {
            finishShareLayout("asana_error")
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.asanaTag)
        }
    }

    /**
     * This method is used for removing loggerbird_asana_popup from window.
     */
    internal fun removeAsanaLayout() {
        if (this::viewAsana.isInitialized && windowManagerAsana != null) {
            (windowManagerAsana as WindowManager).removeViewImmediate(
                viewAsana
            )
            windowManagerAsana = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_asana_popup.
     */
    private fun buttonClicksAsana() {
        buttonAsanaCreate.setSafeOnClickListener {
            asanaAuthentication.gatherAutoTextDetails(
                autoTextViewProject = autoTextViewAsanaProject,
                autoTextViewSection = autoTextViewAsanaSector,
                autoTextViewAssignee = autoTextViewAsanaAssignee,
                autoTextViewPriority = autoTextViewAsanaPriority
            )
            asanaAuthentication.gatherEditTextDetails(
                editTextDescription = editTextAsanaDescription,
                editTextSubtasks = editTextAsanaSubTask,
                editTextTaskName = editTextAsanaTaskName
            )
            if (asanaAuthentication.checkAsanaProject(
                    activity = activity,
                    autoTextViewProject = autoTextViewAsanaProject
                )
                && asanaAuthentication.checkAsanaTask(
                    activity = activity,
                    editTextTask = editTextAsanaTaskName
                )
                && asanaAuthentication.checkAsanaSection(
                    activity = activity,
                    autoTextViewSection = autoTextViewAsanaSector
                )
                && asanaAuthentication.checkAsanaAssignee(
                    activity = activity,
                    autoTextViewAssignee = autoTextViewAsanaAssignee
                )
            ) {
                asanaAuthentication.callAsana(
                    activity = activity,
                    context = context,
                    task = "create"
                )
                attachProgressBar(task = "asana")
            }
        }
        buttonAsanaCancel.setSafeOnClickListener {
            removeAsanaLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
        imageViewAsanaTaskAdd.setSafeOnClickListener {
            hideKeyboard(activity = activity, view = viewAsana)
            if (!arrayListAsanaSubtaskName.contains(
                    RecyclerViewModelSubtask(
                        editTextAsanaSubTask.text.toString()
                    )
                ) && editTextAsanaSubTask.text.isNotEmpty()
            ) {
                arrayListAsanaSubtaskName.add(RecyclerViewModelSubtask(editTextAsanaSubTask.text.toString()))
                asanaSubTasksAdapter.notifyDataSetChanged()
                cardViewAsanaSubTasksList.visibility = View.VISIBLE
            } else if (arrayListAsanaSubtaskName.contains(
                    RecyclerViewModelSubtask(editTextAsanaSubTask.text.toString())
                )
            ) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.asana_sub_task_exist)
                )
            } else if (editTextAsanaSubTask.text.isEmpty()) {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.asana_sub_task_empty)
                )
            }
        }
        imageButtonAsanaRemoveDate.setSafeOnClickListener {
            imageButtonAsanaRemoveDate.visibility = View.GONE
            asanaAuthentication.setStartDate(startDate = null)
        }
        imageViewAsanaStartDate.setSafeOnClickListener {
            initializeAsanaDateLayout()
        }

    }

    /**
     * This method is used for initializing asana attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeAsanaAttachmentRecyclerView(filePathMedia: File) {
        arrayListAsanaFileName.clear()
        recyclerViewAsanaAttachmentList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        asanaAttachmentAdapter =
            RecyclerViewAsanaAttachmentAdapter(
                addAsanaFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewAsanaAttachmentList.adapter = asanaAttachmentAdapter
    }

    /**
     * This method is used for adding files to asana file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addAsanaFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListAsanaFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListAsanaFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListAsanaFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListAsanaFileName
    }

    /**
     * This method is used for initializing asana sub-task recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeAsanaSubtaskRecyclerView(filePathMedia: File) {
        arrayListAsanaSubtaskName.clear()
        recyclerViewAsanaSubTasksList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        asanaSubTasksAdapter =
            RecyclerViewAsanaSubTaskAdapter(
                arrayListAsanaSubtaskName,
                context = context,
                activity = activity,
                rootView = rootView,
                filePathMedia = filePathMedia,
                arrayListAssignee = arrayListAsanaSubtaskAssignee,
                arrayListSection = arrayListAsanaSubtaskSection
            )
        recyclerViewAsanaSubTasksList.adapter = asanaSubTasksAdapter
    }


    /**
     * This method is used for initializing  autoCompleteTextViews in the loggerbird_asana_popup.
     * @param arrayListAsanaProject is used for getting the project list for project autoCompleteTextView.
     * @param arrayListAsanaAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param arrayListAsanaSection is used for getting the section list for section autoCompleteTextView.
     * @param arrayListAsanaPriority is used for getting the priority list for priority autoCompleteTextView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    internal fun initializeAsanaAutoTextViews(
        arrayListAsanaProject: ArrayList<String>,
        arrayListAsanaAssignee: ArrayList<String>,
        arrayListAsanaSection: ArrayList<String>,
        arrayListAsanaPriority: ArrayList<String>,
        filePathMedia: File
    ) {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        editTextAsanaDescription.setText(
            sharedPref.getString(
                "asana_description",
                null
            )
        )
        editTextAsanaSubTask.setText(
            sharedPref.getString(
                "asana_subtask",
                null
            )
        )
        editTextAsanaTaskName.setText(
            sharedPref.getString(
                "asana_task_name",
                null
            )
        )
        initializeAsanaProject(
            arrayListAsanaProject = arrayListAsanaProject,
            sharedPref = sharedPref
        )
        initializeAsanaSection(
            arrayListAsanaSection = arrayListAsanaSection,
            sharedPref = sharedPref
        )
        initializeAsanaAssignee(
            arrayListAsanaAssignee = arrayListAsanaAssignee,
            sharedPref = sharedPref
        )
        initializeAsanaPriority(
            arrayListAsanaPriority = arrayListAsanaPriority,
            sharedPref = sharedPref
        )
        initializeAsanaSubtaskRecyclerView(filePathMedia = filePathMedia)
        detachProgressBar()
    }

    /**
     * This method is used for initializing project autoCompleteTextView in the loggerbird_asana_popup.
     * @param arrayListAsanaProject is used for getting the project list for project autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeAsanaProject(
        arrayListAsanaProject: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewAsanaProjectAdapter = AutoCompleteTextViewAsanaProjectAdapter(
            this,
            R.layout.auto_text_view_asana_project_item,
            arrayListAsanaProject
        )
        autoTextViewAsanaProject.setAdapter(autoTextViewAsanaProjectAdapter)
        if (arrayListAsanaProject.isNotEmpty() && autoTextViewAsanaProject.editableText.isEmpty()) {
            if (sharedPref.getString("asana_project", null) != null) {
                if (arrayListAsanaProject.contains(
                        sharedPref.getString(
                            "asana_project",
                            null
                        )!!
                    )
                ) {
                    autoTextViewAsanaProject.setText(
                        sharedPref.getString("asana_project", null),
                        false
                    )
                } else {
                    autoTextViewAsanaProject.setText(arrayListAsanaProject[0], false)
                }
            } else {
                autoTextViewAsanaProject.setText(arrayListAsanaProject[0], false)
            }
        }
        autoTextViewAsanaProject.setOnTouchListener { v, event ->
            autoTextViewAsanaProject.showDropDown()
            false
        }
        autoTextViewAsanaProject.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewAsana)
            clearAsanaComponents()
            asanaAuthentication.setProjectPosition(projectPosition = position)
            asanaAuthentication.callAsana(
                activity = activity,
                context = context,
                task = "get"
            )
            attachProgressBar(task = "asana")
        }
    }

    /**
     * This method is used for initializing section autoCompleteTextView in the loggerbird_asana_popup.
     * @param arrayListAsanaSection is used for getting the section list for section autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeAsanaSection(
        arrayListAsanaSection: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewAsanaSectorAdapter = AutoCompleteTextViewAsanaSectorAdapter(
            this,
            R.layout.auto_text_view_asana_sector_item,
            arrayListAsanaSection
        )
        autoTextViewAsanaSector.setAdapter(autoTextViewAsanaSectorAdapter)
        if (arrayListAsanaSection.isNotEmpty() && autoTextViewAsanaSector.editableText.isEmpty()) {
            if (sharedPref.getString("asana_section", null) != null) {
                if (arrayListAsanaSection.contains(
                        sharedPref.getString(
                            "asana_section",
                            null
                        )!!
                    )
                ) {
                    autoTextViewAsanaSector.setText(
                        sharedPref.getString("asana_section", null),
                        false
                    )
                } else {
                    autoTextViewAsanaSector.setText(arrayListAsanaSection[0], false)
                }
            }
        }
        autoTextViewAsanaSector.setOnTouchListener { v, event ->
            autoTextViewAsanaSector.showDropDown()
            false
        }
        autoTextViewAsanaSector.setOnItemClickListener { parent, view, position, id ->
            asanaAuthentication.setSectionPosition(sectionPosition = position)
            hideKeyboard(activity = activity, view = viewAsana)
        }
        this.arrayListAsanaSubtaskSection = arrayListAsanaSection
    }

    /**
     * This method is used for initializing priority autoCompleteTextView in the loggerbird_asana_popup.
     * @param arrayListAsanaPriority is used for getting the priority list for priority autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeAsanaPriority(
        arrayListAsanaPriority: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewAsanaPriorityAdapter = AutoCompleteTextViewAsanaPriorityAdapter(
            this,
            R.layout.auto_text_view_asana_priority_item,
            arrayListAsanaPriority
        )
        autoTextViewAsanaPriority.setAdapter(autoTextViewAsanaPriorityAdapter)
        if (arrayListAsanaPriority.isNotEmpty() && autoTextViewAsanaPriority.editableText.isEmpty()) {
            if (sharedPref.getString("asana_priority", null) != null) {
                if (arrayListAsanaPriority.contains(
                        sharedPref.getString(
                            "asana_priority",
                            null
                        )!!
                    )
                ) {
                    autoTextViewAsanaPriority.setText(
                        sharedPref.getString("asana_priority", null),
                        false
                    )
                } else {
                    autoTextViewAsanaPriority.setText(arrayListAsanaPriority[0], false)
                }
            }
        }
        autoTextViewAsanaPriority.setOnTouchListener { v, event ->
            autoTextViewAsanaPriority.showDropDown()
            false
        }
        autoTextViewAsanaPriority.setOnItemClickListener { parent, view, position, id ->
            //            basecampAuthentication.setCategoryPosition(categoryPosition = position)
            hideKeyboard(activity = activity, view = viewAsana)
        }
    }

    /**
     * This method is used for initializing assignee autoCompleteTextView in the loggerbird_asana_popup.
     * @param arrayListAsanaAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeAsanaAssignee(
        arrayListAsanaAssignee: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewAsanaAssigneeAdapter = AutoCompleteTextViewAsanaAssigneeAdapter(
            this,
            R.layout.auto_text_view_asana_assignee_item,
            arrayListAsanaAssignee
        )
        autoTextViewAsanaAssignee.setAdapter(autoTextViewAsanaAssigneeAdapter)
        if (arrayListAsanaAssignee.isNotEmpty() && autoTextViewAsanaAssignee.editableText.isEmpty()) {
            if (sharedPref.getString("asana_assignee", null) != null) {
                if (arrayListAsanaAssignee.contains(
                        sharedPref.getString(
                            "asana_assignee",
                            null
                        )!!
                    )
                ) {
                    autoTextViewAsanaAssignee.setText(
                        sharedPref.getString("asana_assignee", null),
                        false
                    )
                } else {
                    autoTextViewAsanaAssignee.setText(arrayListAsanaAssignee[0], false)
                }
            }
        }
        autoTextViewAsanaAssignee.setOnTouchListener { v, event ->
            autoTextViewAsanaAssignee.showDropDown()
            false
        }
        autoTextViewAsanaAssignee.setOnItemClickListener { parent, view, position, id ->
            asanaAuthentication.setAssignee(assigneePosition = position)
            hideKeyboard(activity = activity, view = viewAsana)
        }
        this.arrayListAsanaSubtaskAssignee = arrayListAsanaAssignee
    }

    /**
     * This method is used for creating asana-date layout which is attached to application overlay.
     */
    private fun initializeAsanaDateLayout() {
        removeAsanaDateLayout()
        val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
        viewAsanaDate =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LayoutInflater.from(activity)
                    .inflate(R.layout.asana_calendar_view, rootView, false)
            } else {
                LayoutInflater.from(activity)
                    .inflate(R.layout.asana_calendar_view_lower, rootView, false)
            }
        windowManagerParamsAsanaDate =
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
        windowManagerAsanaDate = activity.getSystemService(Context.WINDOW_SERVICE)!!
        (windowManagerAsanaDate as WindowManager).addView(
            viewAsanaDate,
            windowManagerParamsAsanaDate
        )
        frameLayoutAsanaDate = viewAsanaDate.findViewById(R.id.asana_calendar_view_layout)
        calendarViewAsana = viewAsanaDate.findViewById(R.id.calendarView_start_date)
        buttonAsanaDateCancel =
            viewAsanaDate.findViewById(R.id.button_asana_calendar_cancel)
        buttonAsanaDateCreate = viewAsanaDate.findViewById(R.id.button_asana_calendar_ok)
        buttonClicksAsanaDateLayout()
    }

    /**
     * This method is used for removing asana_calendar_view from window.
     */
    private fun removeAsanaDateLayout() {
        if (this::viewAsanaDate.isInitialized && windowManagerAsanaDate != null) {
            (windowManagerAsanaDate as WindowManager).removeViewImmediate(
                viewAsanaDate
            )
            windowManagerAsanaDate = null
        }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the asana_calendar_view.
     */
    private fun buttonClicksAsanaDateLayout() {
        val calendar = Calendar.getInstance()
        val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mTempMonth: String
        mTempMonth = if (mMonth in 1..9) {
            "0$mMonth"
        } else {
            mMonth.toString()
        }
        val mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val mTempDay: String
        mTempDay = if (mDayOfMonth in 1..9) {
            "0$mDayOfMonth"
        } else {
            mDayOfMonth.toString()
        }
        var startDate = "$mYear-$mTempMonth-$mTempDay"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            calendarViewAsana.minDate = System.currentTimeMillis() + 86400000
        }
        frameLayoutAsanaDate.setOnClickListener {
            removeAsanaDateLayout()
        }
        calendarViewAsana.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val tempMonthMonth = month + 1
            val tempMonth: String = if (tempMonthMonth in 1..9) {
                "0$tempMonthMonth"
            } else {
                tempMonthMonth.toString()
            }
            val tempDay: String = if (dayOfMonth in 1..9) {
                "0$dayOfMonth"
            } else {
                dayOfMonth.toString()
            }
            startDate = "$year-$tempMonth-$tempDay"
        }
        buttonAsanaDateCreate.setSafeOnClickListener {
            imageButtonAsanaRemoveDate.visibility = View.VISIBLE
            asanaAuthentication.setStartDate(startDate = startDate)
            removeAsanaDateLayout()
        }
        buttonAsanaDateCancel.setSafeOnClickListener {
            removeAsanaDateLayout()
        }
    }

    /**
     * This method is used for clearing asana components.
     */
    private fun clearAsanaComponents() {
        cardViewAsanaSubTasksList.visibility = View.GONE
        arrayListAsanaSubtaskName.clear()
        asanaSubTasksAdapter.notifyDataSetChanged()
        editTextAsanaSubTask.text = null
        editTextAsanaDescription.text = null
        editTextAsanaTaskName.text = null
        autoTextViewAsanaPriority.setText("", false)
        autoTextViewAsanaSector.setText("", false)
        autoTextViewAsanaAssignee.setText("", false)
    }


    //Clubhouse
    /**
     * This method is used for creating clubhouse layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeClubhouseLayout(filePathMedia: File) {
        try {
            removeClubhouseLayout()
            viewClubhouse = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_clubhouse_popup, (this.rootView as ViewGroup), false)
            windowManagerParamsClubhouse = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            windowManagerClubhouse = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerClubhouse as WindowManager).addView(
                viewClubhouse,
                windowManagerParamsClubhouse
            )
            activity.window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.black)
            toolbarClubhouse = viewClubhouse.findViewById(R.id.toolbar_clubhouse)
            buttonClubhouseCancel = viewClubhouse.findViewById(R.id.button_clubhouse_cancel)
            buttonClubhouseCreate = viewClubhouse.findViewById(R.id.button_clubhouse_create)
            editTextClubhouseStoryName =
                viewClubhouse.findViewById(R.id.editText_clubhouse_story_name)
            editTextClubhouseStoryDescription =
                viewClubhouse.findViewById(R.id.editText_clubhouse_description)
            autoTextViewClubhouseRequester =
                viewClubhouse.findViewById(R.id.auto_textView_clubhouse_requester)
            autoTextViewClubhouseStoryType =
                viewClubhouse.findViewById(R.id.auto_textView_clubhouse_story_type)
            recyclerViewClubhouseAttachment =
                viewClubhouse.findViewById(R.id.recycler_view_clubhouse_attachment)
            autoTextViewClubhouseProject =
                viewClubhouse.findViewById(R.id.auto_textview_clubhouse_project)
            autoTextViewClubhouseEpic =
                viewClubhouse.findViewById(R.id.auto_textView_clubhouse_epic)
            textViewClubhouseDueDate =
                viewClubhouse.findViewById(R.id.textView_clubhouse_due_date)
            editTextClubhouseEstimate =
                viewClubhouse.findViewById(R.id.editText_clubhouse_estimate_point)
            textViewClubhouseEpic = viewClubhouse.findViewById(R.id.textView_clubhouse_epic)
            linearLayoutClubhouseEpic =
                viewClubhouse.findViewById(R.id.linearLayout_clubhouse_epic)
            imageViewClubhouseDueDate =
                viewClubhouse.findViewById(R.id.imageView_delete_clubhouse_dueDate)
            clubhouseAuthentication.callClubhouse(
                activity = activity,
                context = context,
                task = "get",
                filePathMedia = filePathMedia
            )
            attachProgressBar(task = "clubhouse")
            initializeClubhouseAttachmentRecyclerView(filePathMedia = filePathMedia)
            buttonClicksClubhouse(filePathMedia = filePathMedia)
        } catch (e: Exception) {
            finishShareLayout("clubhouse_error")
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.clubhouseTag)
        }
    }

    /**
     * This method is used for removing loggerbird_clubhouse_popup from window.
     */
    internal fun removeClubhouseLayout() {
        if (windowManagerClubhouse != null && this::viewClubhouse.isInitialized) {
            (windowManagerClubhouse as WindowManager).removeViewImmediate(viewClubhouse)
            windowManagerClubhouse = null
            arrayListClubhouseFileName.clear()
        }
    }

    /**
     * This method is used for initializing clubhouse attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeClubhouseAttachmentRecyclerView(filePathMedia: File) {
        arrayListClubhouseFileName.clear()
        recyclerViewClubhouseAttachment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        clubhouseAttachmentAdapter =
            RecyclerViewClubhouseAttachmentAdapter(
                addClubhouseFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewClubhouseAttachment.adapter = clubhouseAttachmentAdapter
    }

    /**
     * This method is used for adding files to clubhouse file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addClubhouseFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListClubhouseFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListClubhouseFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListClubhouseFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListClubhouseFileName
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_clubhouse_popup.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun buttonClicksClubhouse(filePathMedia: File) {
        buttonClubhouseCreate.setSafeOnClickListener {
            if (checkClubhouseStoryNameEmpty() && checkClubhouseStoryDescriptionEmpty()
                && checkClubhouseStoryTypeEmpty() && checkClubhouseRequesterEmpty()
                && clubhouseAuthentication.checkClubhouseProject(
                    activity = activity,
                    autoTextViewProjects = autoTextViewClubhouseProject
                )
                && clubhouseAuthentication.checkClubhouseRequester(
                    activity = activity,
                    autoTextViewRequester = autoTextViewClubhouseRequester
                )
                && clubhouseAuthentication.checkClubhouseStoryType(
                    activity = activity,
                    autoTextViewStoryType = autoTextViewClubhouseStoryType
                )
            ) {
                clubhouseAuthentication.gatherClubhouseProjectAutoTextDetails(
                    autoTextViewProject = autoTextViewClubhouseProject,
                    autoTextViewEpic = autoTextViewClubhouseEpic,
                    autoTextViewStoryType = autoTextViewClubhouseStoryType,
                    autoTextViewRequester = autoTextViewClubhouseRequester
                )
                clubhouseAuthentication.gatherClubhouseEditTextDetails(
                    editTextStoryName = editTextClubhouseStoryName,
                    editTextStoryDescription = editTextClubhouseStoryDescription,
                    editTextEstimate = editTextClubhouseEstimate
                )
                attachProgressBar(task = "clubhouse")
                clubhouseAuthentication.callClubhouse(
                    activity = activity,
                    context = context,
                    task = "create",
                    filePathMedia = filePathMedia
                )
            }
        }
        textViewClubhouseDueDate.setSafeOnClickListener {
            attachClubhouseDatePicker()
        }
        toolbarClubhouse.setNavigationOnClickListener {
            removeClubhouseLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
        buttonClubhouseCancel.setSafeOnClickListener {
            removeClubhouseLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
        textViewClubhouseEpic.setSafeOnClickListener {
            linearLayoutClubhouseEpic.visibility = View.VISIBLE
            textViewClubhouseEpic.visibility = View.GONE
        }
    }

    /**
     * This method is used for initializing  autoCompleteTextViews in the loggerbird_clubhouse_popup.
     * @param arrayListClubhouseRequester is used for getting the requester list for requester autoCompleteTextView.
     * @param arrayListClubhouseProjects is used for getting the project list for project autoCompleteTextView.
     * @param arrayListClubhouseStoryType is used for getting the story type list for story type autoCompleteTextView.
     * @param arrayListClubhouseEpic is used for getting the epic list for epic autoCompleteTextView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun initializeClubhouseAutoTextViews(
        arrayListClubhouseRequester: ArrayList<String>,
        arrayListClubhouseProjects: ArrayList<String>,
        arrayListClubhouseStoryType: ArrayList<String>,
        arrayListClubhouseEpic: ArrayList<String>
    ) {
        initializeClubhouseProject(arrayListClubhouseProjects)
        initializeClubhouseRequester(arrayListClubhouseRequester)
        initializeClubhouseStoryType(arrayListClubhouseStoryType)
        initializeClubhouseEpic(arrayListClubhouseEpic)
        detachProgressBar()
    }

    /**
     * This method is used for initializing project autoCompleteTextView in the loggerbird_clubhouse_popup.
     * @param arrayListClubhouseProjects is used for getting the project list for project autoCompleteTextView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeClubhouseProject(
        arrayListClubhouseProjects: ArrayList<String>
    ) {
        autoTextViewClubhouseProjectAdapter = AutoCompleteTextViewClubhouseProjectAdapter(
            this,
            R.layout.auto_text_view_clubhouse_project_item,
            arrayListClubhouseProjects
        )
        autoTextViewClubhouseProject.setAdapter(autoTextViewClubhouseProjectAdapter)
        if (arrayListClubhouseProjects.isNotEmpty() && autoTextViewClubhouseProject.text.isEmpty()) {
            autoTextViewClubhouseProject.setText(arrayListClubhouseProjects[0], false)
        }
        autoTextViewClubhouseProject.setOnTouchListener { v, event ->
            autoTextViewClubhouseProject.showDropDown()
            false
        }
        autoTextViewClubhouseProject.setOnItemClickListener { parent, view, position, id ->
            clubhouseAuthentication.clubhouseProjectPosition(projectPosition = position)
            hideKeyboard(activity = activity, view = viewClubhouse)
            attachProgressBar(task = "clubhouse")
            clubhouseAuthentication.callClubhouse(
                activity = activity,
                context = context,
                task = "get"
            )
        }
    }

    /**
     * This method is used for initializing epic autoCompleteTextView in the loggerbird_clubhouse_popup.
     * @param arrayListClubhouseEpic is used for getting the epic list for epic autoCompleteTextView.
     */
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeClubhouseEpic(
        arrayListClubhouseEpic: ArrayList<String>
    ) {
        autoTextViewClubhouseEpicAdapter = AutoCompleteTextViewClubhouseEpicAdapter(
            this,
            R.layout.auto_text_view_clubhouse_epic_item,
            arrayListClubhouseEpic
        )
        autoTextViewClubhouseEpic.setAdapter(autoTextViewClubhouseEpicAdapter)
        if (arrayListClubhouseEpic.isNotEmpty() && autoTextViewClubhouseEpic.text.isEmpty()) {
            autoTextViewClubhouseEpic.setText(arrayListClubhouseEpic[0], false)
        }
        autoTextViewClubhouseEpic.setOnTouchListener { v, event ->
            autoTextViewClubhouseEpic.showDropDown()
            false
        }
        autoTextViewClubhouseEpic.setOnItemClickListener { parent, view, position, id ->
            clubhouseAuthentication.clubhouseEpicPosition(epicPosition = position)
            hideKeyboard(activity = activity, view = viewClubhouse)
        }
        autoTextViewClubhouseEpic.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListClubhouseEpic.contains(autoTextViewClubhouseEpic.editableText.toString())) {
                    if (arrayListClubhouseEpic.isNotEmpty()) {
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = activity.resources.getString(R.string.textView_clubhouse_epic_doesnt_exist)
                        )
                        autoTextViewClubhouseEpic.setText(arrayListClubhouseEpic[0], false)
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing story type autoCompleteTextView in the loggerbird_clubhouse_popup.
     * @param arrayListClubhouseStoryType is used for getting the story type list for story type autoCompleteTextView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeClubhouseStoryType(
        arrayListClubhouseStoryType: ArrayList<String>
    ) {
        autoTextViewClubhouseStoryTypeAdapter = AutoCompleteTextViewClubhouseStoryTypeAdapter(
            this,
            R.layout.auto_text_view_clubhouse_story_item,
            arrayListClubhouseStoryType
        )
        autoTextViewClubhouseStoryType.setAdapter(autoTextViewClubhouseStoryTypeAdapter)
        if (arrayListClubhouseStoryType.isNotEmpty() && autoTextViewClubhouseStoryType.text.isEmpty()) {
            autoTextViewClubhouseStoryType.setText(arrayListClubhouseStoryType[0], false)
        }
        autoTextViewClubhouseStoryType.setOnTouchListener { v, event ->
            autoTextViewClubhouseStoryType.showDropDown()
            false
        }
        autoTextViewClubhouseStoryType.setOnItemClickListener { parent, view, position, id ->
            clubhouseAuthentication.clubhouseEpicPosition(epicPosition = position)
            hideKeyboard(activity = activity, view = viewClubhouse)
        }
        autoTextViewClubhouseStoryType.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListClubhouseStoryType.contains(autoTextViewClubhouseStoryType.editableText.toString())) {
                    if (arrayListClubhouseStoryType.isNotEmpty()) {
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = activity.resources.getString(R.string.textView_clubhouse_story_type_doesnt_exist)
                        )
                        autoTextViewClubhouseStoryType.setText(
                            arrayListClubhouseStoryType[0],
                            false
                        )
                    }
                }
            }
        }
    }

    /**
     * This method is used for initializing requester autoCompleteTextView in the loggerbird_clubhouse_popup.
     * @param arrayListClubhouseRequester is used for getting the requester list for requester autoCompleteTextView.
     */
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeClubhouseRequester(
        arrayListClubhouseRequester: ArrayList<String>
    ) {
        autoTextViewClubhouseRequesterAdapter = AutoCompleteTextViewClubhouseRequesterAdapter(
            this,
            R.layout.auto_text_view_clubhouse_requester_item,
            arrayListClubhouseRequester
        )
        autoTextViewClubhouseRequester.setAdapter(autoTextViewClubhouseRequesterAdapter)
        if (arrayListClubhouseRequester.isNotEmpty() && autoTextViewClubhouseRequester.text.isEmpty()) {
            autoTextViewClubhouseRequester.setText(arrayListClubhouseRequester[0], false)
        }
        autoTextViewClubhouseRequester.setOnTouchListener { v, event ->
            autoTextViewClubhouseRequester.showDropDown()
            false
        }
        autoTextViewClubhouseRequester.setOnItemClickListener { parent, view, position, id ->
            clubhouseAuthentication.clubhouseUserPosition(userPosition = position)
            hideKeyboard(activity = activity, view = viewClubhouse)
        }
        autoTextViewClubhouseRequester.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (!arrayListClubhouseRequester.contains(autoTextViewClubhouseRequester.editableText.toString())) {
                    if (arrayListClubhouseRequester.isNotEmpty()) {
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = activity.resources.getString(R.string.textView_clubhouse_user_doesnt_exist)
                        )
                        autoTextViewClubhouseRequester.setText(
                            arrayListClubhouseRequester[0],
                            false
                        )
                    }
                }
            }
        }
    }

    /**
     * This method is used for creating custom clubhouse date-picker layout which is attached to application overlay.
     */
    private fun attachClubhouseDatePicker() {
        try {
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            calendarViewClubhouseView =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    LayoutInflater.from(activity)
                        .inflate(R.layout.clubhouse_calendar_view, rootView, false)
                } else {
                    LayoutInflater.from(activity)
                        .inflate(R.layout.clubhouse_calendar_view_lower, rootView, false)
                }
            windowManagerParamsClubhouseDatePicker =
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
            windowManagerClubhouseDatePicker = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerClubhouseDatePicker as WindowManager).addView(
                calendarViewClubhouseView,
                windowManagerParamsClubhouseDatePicker
            )
            initializeClubhouseDatePicker()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.clubhouseDatePopupTag)
        }
    }

    /**
     * This method is used for initializing clubhouse date-picker.
     */
    private fun initializeClubhouseDatePicker() {
        val calendar = Calendar.getInstance()
        var mYear = calendar.get(Calendar.YEAR)
        var mMonth = calendar.get(Calendar.MONTH)
        var mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        var dueDate: String? = null
        var dueDateFormat: String? = null
        calendarViewClubhouseLayout =
            calendarViewClubhouseView.findViewById(R.id.clubhouse_calendar_view_layout)
        calendarViewClubhouseDueDate =
            calendarViewClubhouseView.findViewById(R.id.calendarView_clubhouse_due_date)
        buttonCalendarViewClubhouseCancel =
            calendarViewClubhouseView.findViewById(R.id.button_clubhouse_calendar_cancel)
        buttonCalendarViewClubhouseOk =
            calendarViewClubhouseView.findViewById(R.id.button_clubhouse_calendar_ok)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            calendarViewClubhouseDueDate.minDate = System.currentTimeMillis()
        }
        if (calendarViewClubhouseDate != null) {
            calendarViewClubhouseDueDate.date = calendarViewClubhouseDate!!
        }
        calendarViewClubhouseDueDate.setOnDateChangeListener { viewStartDate, year, month, dayOfMonth ->
            mYear = year
            mMonth = month + 1
            mDayOfMonth = dayOfMonth
            calendarViewClubhouseDate = viewStartDate.date
            dueDate = "$mMonth/$mDayOfMonth/$mYear"
            dueDateFormat = "$mYear-$mMonth-$mDayOfMonth"
            activity.runOnUiThread {
                textViewClubhouseDueDate.text = dueDate
                textViewClubhouseDueDate.setTextColor(resources.getColor(R.color.black))
                imageViewClubhouseDueDate.visibility = View.VISIBLE
            }
            clubhouseAuthentication.dueDate = dueDateFormat
        }
        imageViewClubhouseDueDate.setOnClickListener {
            activity.runOnUiThread {
                imageViewClubhouseDueDate.visibility = View.GONE
                textViewClubhouseDueDate.text = null
            }
            clubhouseAuthentication.dueDate = null
        }
        buttonCalendarViewClubhouseCancel.setOnClickListener {
            detachClubhouseDatePicker()
        }
        buttonCalendarViewClubhouseOk.setOnClickListener {
            clubhouseAuthentication.dueDate = dueDateFormat
            detachClubhouseDatePicker()
        }
    }

    /**
     * This method is used for removing clubhouse_calendar_view from window.
     */
    private fun detachClubhouseDatePicker() {
        if (this::calendarViewClubhouseView.isInitialized) {
            (windowManagerClubhouseDatePicker as WindowManager).removeViewImmediate(
                calendarViewClubhouseView
            )
        }
    }

    /**
     * This method is used for story field is not empty in clubhouse layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkClubhouseStoryNameEmpty(): Boolean {
        if (editTextClubhouseStoryName.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.editText_clubhouse_story_empty)
            )
        }
        return false
    }

    /**
     * This method is used for description field is not empty in clubhouse layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkClubhouseStoryDescriptionEmpty(): Boolean {
        if (editTextClubhouseStoryDescription.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.editText_clubhouse_story_description_empty)
            )
        }
        return false
    }

    /**
     * This method is used for story type field is not empty in clubhouse layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkClubhouseStoryTypeEmpty(): Boolean {
        if (autoTextViewClubhouseStoryType.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.editText_clubhouse_story_type_empty)
            )
        }
        return false
    }


    /**
     * This method is used for assignee field is not empty in clubhouse layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkClubhouseRequesterEmpty(): Boolean {
        if (autoTextViewClubhouseRequester.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.editText_clubhouse_story_requester_empty)
            )
        }
        return false
    }

    /**
     * This method is used for initializing Loggerbird activation popup.
     * @param activity is used for getting reference of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initializeLoggerBirdActivatePopup(activity: Activity) {
        try {
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            viewLoggerBirdActivatePopup =
                LayoutInflater.from(activity)
                    .inflate(R.layout.loggerbird_activate_popup, rootView, false)
            windowManagerParamsLoggerBirdActivatePopup =
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
            windowManagerParamsLoggerBirdActivatePopup.gravity = Gravity.TOP
            windowManagerLoggerBirdActivatePopup =
                activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerLoggerBirdActivatePopup as WindowManager).addView(
                viewLoggerBirdActivatePopup,
                windowManagerParamsLoggerBirdActivatePopup
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.cookieBarColor)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.cookieBarColor)

            viewLoggerBirdActivatePopup.scaleX = 0F
            viewLoggerBirdActivatePopup.scaleY = 0F
            viewLoggerBirdActivatePopup.animate()
                .scaleX(1F)
                .scaleY(1F)
                .setDuration(500)
                .setInterpolator(BounceInterpolator())
                .setStartDelay(0)
                .start()
            textViewLoggerBirdActivatePopupActivate =
                viewLoggerBirdActivatePopup.findViewById(R.id.btn_action_activate)
            textViewLoggerBirdActivatePopupDismiss =
                viewLoggerBirdActivatePopup.findViewById(R.id.btn_action_dismiss)
            initializeButtonClicksLoggerBirdActivatePopup(activity = activity)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.loggerBirdActivatePopupTag
            )
        }
    }

    /**
     * This method is used for removing Loggerbird activation popup.
     */
    internal fun removeLoggerBirdActivateLayout() {
        if (this::viewLoggerBirdActivatePopup.isInitialized && windowManagerLoggerBirdActivatePopup != null) {
            (windowManagerLoggerBirdActivatePopup as WindowManager).removeViewImmediate(
                viewLoggerBirdActivatePopup
            )
            windowManagerLoggerBirdActivatePopup = null
        }
    }

    /**
     * This method is used for initializing button clicks of Loggerbird activation popup.
     * @param activity is used for getting reference of current activity.
     */
    private fun initializeButtonClicksLoggerBirdActivatePopup(activity: Activity) {
        textViewLoggerBirdActivatePopupActivate.setSafeOnClickListener {
            initializeFloatingActionButton(activity = activity)
            removeLoggerBirdActivateLayout()
        }
        textViewLoggerBirdActivatePopupDismiss.setSafeOnClickListener {
            sd.stop()
            removeLoggerBirdActivateLayout()
        }
    }

    /**
     * This method is used for initializing Loggerbird start popup.
     * @param activity is used for getting reference of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initializeLoggerBirdStartPopup(activity: Activity) {
        try {
            removeLoggerBirdDismissLayout()
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)

            viewLoggerBirdStartPopup =
                LayoutInflater.from(activity)
                    .inflate(R.layout.loggerbird_start_popup, rootView, false)

            windowManagerParamsLoggerBirdStartPopup =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                } else {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                }
            windowManagerParamsLoggerBirdStartPopup.gravity = Gravity.TOP
            windowManagerLoggerBirdStartPopup = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerLoggerBirdStartPopup as WindowManager).addView(
                viewLoggerBirdStartPopup,
                windowManagerParamsLoggerBirdStartPopup
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.cookieBarColor)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.cookieBarColor)

            val animation: Animation =
                AnimationUtils.loadAnimation(applicationContext, R.anim.slide_in_from_top)
            viewLoggerBirdStartPopup.animation = animation

            viewLoggerBirdStartPopup.setOnTouchListener(object :
                OnSwipeTouchListener(this@LoggerBirdService) {
                override fun onSwipeLeft() {
                    Log.e("ViewSwipe", "Left")
                    removeLoggerBirdStartLayout()
                }

                override fun onSwipeRight() {
                    Log.e("ViewSwipe", "Right")
                    removeLoggerBirdStartLayout()
                }
            })

            textViewLoggerBirdStartPopupSessionTime =
                viewLoggerBirdStartPopup.findViewById(R.id.textView_session_time_pop_up)
            textViewLoggerBirdStartPopupSessionTime.text =
                resources.getString(R.string.total_session_time) + timeStringDay(
                    totalSessionTime()
                ) + "\n" + resources.getString(R.string.last_session_time) + timeStringDay(
                    lastSessionTime()
                )
            val timerStartPopup = Timer()
            val timerTaskStartPopup = object : TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        val animation: Animation = AnimationUtils.loadAnimation(
                            applicationContext,
                            R.anim.slide_out_to_top
                        )
                        viewLoggerBirdStartPopup.animation = animation
                        removeLoggerBirdStartLayout()
                    }
                }
            }
            timerStartPopup.schedule(timerTaskStartPopup, 3000)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.loggerBirdStartPopupTag)
        }
    }

    /**
     * This method is used for removing Loggerbird start popup.
     */
    internal fun removeLoggerBirdStartLayout() {
        if (this::viewLoggerBirdStartPopup.isInitialized && windowManagerLoggerBirdStartPopup != null) {
            (windowManagerLoggerBirdStartPopup as WindowManager).removeViewImmediate(
                viewLoggerBirdStartPopup
            )
            windowManagerLoggerBirdStartPopup = null
        }
    }

    /**
     * This method is used for initializing Loggerbird close popup.
     * @param activity is used for getting reference of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initializeLoggerBirdClosePopup(activity: Activity) {
        try {
            removeLoggerBirdStartLayout()
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            viewLoggerBirdDismissPopup =
                LayoutInflater.from(activity)
                    .inflate(R.layout.loggerbird_close_popup, rootView, false)
            windowManagerParamsLoggerBirdDismissPopup =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                } else {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                }
            windowManagerParamsLoggerBirdDismissPopup.gravity = Gravity.TOP
            windowManagerLoggerBirdDismissPopup =
                activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerLoggerBirdDismissPopup as WindowManager).addView(
                viewLoggerBirdDismissPopup,
                windowManagerParamsLoggerBirdDismissPopup
            )
            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.cookieBarColor)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.cookieBarColor)
            val animation: Animation =
                AnimationUtils.loadAnimation(applicationContext, R.anim.slide_in_from_top)
            viewLoggerBirdDismissPopup.animation = animation

            viewLoggerBirdDismissPopup.setOnTouchListener(object :
                OnSwipeTouchListener(this@LoggerBirdService) {
                override fun onSwipeLeft() {
                    Log.e("ViewSwipe", "Left")
                    viewLoggerBirdDismissPopup.clearAnimation()
                    removeLoggerBirdDismissLayout()

                }

                override fun onSwipeRight() {
                    Log.e("ViewSwipe", "Right")
                    viewLoggerBirdDismissPopup.clearAnimation()
                    removeLoggerBirdDismissLayout()
                }
            })

            textViewLoggerBirdDismissPopupFeedBack =
                viewLoggerBirdDismissPopup.findViewById(R.id.textView_feed_back_pop_up)
            initializeButtonClicksLoggerBirdDismissPopup()
            val timerDismissPopup = Timer()
            val timerTaskDismissPopup = object : TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        val animation: Animation = AnimationUtils.loadAnimation(
                            applicationContext,
                            R.anim.slide_out_to_top
                        )
                        viewLoggerBirdDismissPopup.animation = animation
                        removeLoggerBirdDismissLayout()
                    }
                }
            }
            timerDismissPopup.schedule(timerTaskDismissPopup, 3000)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.loggerBirdClosePopupTag)
        }
    }

    /**
     * This method is used for removing Loggerbird close popup.
     */
    internal fun removeLoggerBirdDismissLayout() {
        if (this::viewLoggerBirdDismissPopup.isInitialized && windowManagerLoggerBirdDismissPopup != null) {
            (windowManagerLoggerBirdDismissPopup as WindowManager).removeViewImmediate(
                viewLoggerBirdDismissPopup
            )
            windowManagerLoggerBirdDismissPopup = null
        }
    }

    /**
     * This method is used for initializing button clicks of Loggerbird close popup.
     */
    private fun initializeButtonClicksLoggerBirdDismissPopup() {
        textViewLoggerBirdDismissPopupFeedBack.setSafeOnClickListener {
            initializeFeedBackLayout()
            removeLoggerBirdDismissLayout()
        }
    }

    /**
     * This method is used for initializing button clicks of Loggerbird file action popup to determine whether keep or clean files.
     * @param activity is used for getting reference of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initializeLoggerBirdFileActionPopup(activity: Activity) {
        try {
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            viewLoggerBirdFileActionPopup =
                LayoutInflater.from(activity)
                    .inflate(R.layout.loggerbird_file_action_popup, rootView, false)
            windowManagerParamsLoggerBirdFileAction =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT

                    )
                } else {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                }
            windowManagerParamsLoggerBirdFileAction.gravity = Gravity.TOP
            windowManagerLoggerBirdFileActionPopup =
                activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerLoggerBirdFileActionPopup as WindowManager).addView(
                viewLoggerBirdFileActionPopup,
                windowManagerParamsLoggerBirdFileAction
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.cookieBarColor)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.cookieBarColor)
            viewLoggerBirdFileActionPopup.scaleX = 0F
            viewLoggerBirdFileActionPopup.scaleY = 0F
            viewLoggerBirdFileActionPopup.animate()
                .scaleX(1F)
                .scaleY(1F)
                .setDuration(500)
                .setInterpolator(BounceInterpolator())
                .setStartDelay(0)
                .start()
            textViewLoggerBirdFileActionPopupDiscard =
                viewLoggerBirdFileActionPopup.findViewById(R.id.textView_files_action_discard)
            textViewLoggerBirdFileActionPopupEmail =
                viewLoggerBirdFileActionPopup.findViewById(R.id.textView_files_action_mail)
            initializeButtonClicksLoggerBirdFileActionPopup(activity = activity)
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.loggerBirdFileActionPopupTag
            )
        }
    }

    /**
     * This method is used for removing Loggerbird file action popup.
     */
    internal fun removeLoggerBirdFileActionLayout() {
        if (this::viewLoggerBirdFileActionPopup.isInitialized && windowManagerLoggerBirdFileActionPopup != null) {
            (windowManagerLoggerBirdFileActionPopup as WindowManager).removeViewImmediate(
                viewLoggerBirdFileActionPopup
            )
            windowManagerLoggerBirdFileActionPopup = null
        }
    }

    /**
     * This method is used for initializing button clicks of Loggerbird file action popup.
     */
    private fun initializeButtonClicksLoggerBirdFileActionPopup(activity: Activity) {
        textViewLoggerBirdFileActionPopupDiscard.setSafeOnClickListener {
            if (this.controlFileAction) {
                this.controlFileAction = false
                removeLoggerBirdFileActionLayout()
                deleteOldFiles()
            }
        }
        textViewLoggerBirdFileActionPopupEmail.setSafeOnClickListener {
            if (this.controlFileAction) {
//                this.controlFileAction = false
//                removeLoggerBirdFileActionLayout()
                initializeLoggerBirdFileActionListPopup(activity = activity)
//                sendOldFilesEmail()
            }
        }
    }

    /**
     * This method is used for initializing button clicks of Loggerbird file action list popup to determine to where to share files.
     * @param activity is used for getting reference of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initializeLoggerBirdFileActionListPopup(activity: Activity) {
        try {
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            viewLoggerBirdFileActionListPopup =
                LayoutInflater.from(activity)
                    .inflate(R.layout.loggerbird_file_action_list, rootView, false)
            windowManagerParamsLoggerBirdFileActionList =
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
            windowManagerLoggerBirdFileActionListPopup =
                activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerLoggerBirdFileActionListPopup as WindowManager).addView(
                viewLoggerBirdFileActionListPopup,
                windowManagerParamsLoggerBirdFileActionList
            )
            imageViewLoggerBirdFileActionListPopupBack =
                viewLoggerBirdFileActionListPopup.findViewById(R.id.imageView_file_action_list_back)
            recyclerViewLoggerBirdFileActionListPopup =
                viewLoggerBirdFileActionListPopup.findViewById(R.id.recycler_view_file_action_list_attachment)
            initializeLoggerBirdFileActionListAttachmentRecyclerView(rootView = rootView)
            initializeButtonClicksLoggerBirdFileActionListPopup()
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.loggerBirdFileActionListPopupTag
            )
        }
    }

    /**
     * This method is used for removing Loggerbird file action list popup.
     */
    internal fun removeLoggerBirdFileActionListLayout() {
        if (this::viewLoggerBirdFileActionListPopup.isInitialized && windowManagerLoggerBirdFileActionListPopup != null) {
            (windowManagerLoggerBirdFileActionListPopup as WindowManager).removeViewImmediate(
                viewLoggerBirdFileActionListPopup
            )
            windowManagerLoggerBirdFileActionListPopup = null
        }
    }

    /**
     * This method is used for initializing button clicks of Loggerbird file action list popup.
     */
    private fun initializeButtonClicksLoggerBirdFileActionListPopup() {
        imageViewLoggerBirdFileActionListPopupBack.setSafeOnClickListener {
            removeLoggerBirdFileActionListLayout()
        }
    }

    /**
     * This method is used for initializing file action list attachment recyclerView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeLoggerBirdFileActionListAttachmentRecyclerView(rootView: ViewGroup) {
        arrayListLoggerBirdFileActionList.clear()
        getFileList()?.forEach {
            arrayListLoggerBirdFileActionList.add(RecyclerViewModel(File(it)))
        }
        recyclerViewLoggerBirdFileActionListPopup.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        loggerBirdFileActionListAdapter =
            RecyclerViewFileActionAttachmentAdapter(
                arrayListLoggerBirdFileActionList,
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewLoggerBirdFileActionListPopup.adapter = loggerBirdFileActionListAdapter
    }


    /**
     * This method is used for initializing Loggerbird unhandled loggerbird.exception popup.
     * @param activity is used for getting reference of current activity.
     * @param sharedPref is used for getting reference of shared preferences to keep file names in local database.
     * @param filePath is used for getting reference of filepath.
     */
    private fun initializeLoggerBirdUnhandledExceptionPopup(
        activity: Activity,
        sharedPref: SharedPreferences,
        filePath: File
    ) {
        try {
            if (controlRevealShareLayout() && controlFloatingActionButtonView()) {
                revealLinearLayoutShare.visibility = View.GONE
                floating_action_button.visibility = View.GONE
            }
            removeLoggerBirdUnhandledExceptionLayout()
            val rootView: ViewGroup = activity.window.decorView.findViewById(android.R.id.content)
            viewLoggerBirdUnhandledExceptionPopup =
                LayoutInflater.from(activity)
                    .inflate(R.layout.loggerbird_unhandled_popup, rootView, false)
            windowManagerParamsLoggerBirdUnhandledException =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                } else {
                    WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                }
            windowManagerParamsLoggerBirdUnhandledException.gravity = Gravity.TOP
            windowManagerLoggerBirdUnhandledException =
                activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerLoggerBirdUnhandledException as WindowManager).addView(
                viewLoggerBirdUnhandledExceptionPopup,
                windowManagerParamsLoggerBirdUnhandledException
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.cookieBarColor)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.cookieBarColor)

            viewLoggerBirdUnhandledExceptionPopup.scaleX = 0F
            viewLoggerBirdUnhandledExceptionPopup.scaleY = 0F
            viewLoggerBirdUnhandledExceptionPopup.animate()
                .scaleX(1F)
                .scaleY(1F)
                .setDuration(500)
                .setInterpolator(BounceInterpolator())
                .setStartDelay(0)
                .start()
            textViewLoggerBirdUnhandledExceptionPopupDiscard =
                viewLoggerBirdUnhandledExceptionPopup.findViewById(R.id.textView_unhandled_discard)
            textViewLoggerBirdUnhandledExceptionPopupShare =
                viewLoggerBirdUnhandledExceptionPopup.findViewById(R.id.textView_unhandled_share_title)
            checkBoxLoggerBirdUnhandledExceptionPopupDuplication =
                viewLoggerBirdUnhandledExceptionPopup.findViewById(R.id.checkBox_unhandled)

            initializeButtonClicksLoggerBirdUnhandledExceptionPopup(
                activity = activity,
                sharedPref = sharedPref,
                filePath = filePath
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(
                exception = e,
                tag = Constants.loggerBirdUnhandledExceptionPopupTag
            )
        }
    }

    /**
     * This method is used for removing Loggerbird unhandled loggerbird.exception popup.
     */
    internal fun removeLoggerBirdUnhandledExceptionLayout() {
        if (this::viewLoggerBirdUnhandledExceptionPopup.isInitialized && windowManagerLoggerBirdUnhandledException != null) {
            (windowManagerLoggerBirdUnhandledException as WindowManager).removeViewImmediate(
                viewLoggerBirdUnhandledExceptionPopup
            )
            windowManagerLoggerBirdUnhandledException = null
        }
    }

    /**
     * This method is used for initializing button clicks of Loggerbird unhandled loggerbird.exception popup.
     * @param activity is used for getting reference of current activity.
     * @param sharedPref is used for getting reference of shared preferences to keep file names in local database.
     * @param filePath is used for getting reference of filepath.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeButtonClicksLoggerBirdUnhandledExceptionPopup(
        sharedPref: SharedPreferences,
        filePath: File,
        activity: Activity
    ) {
        if (sharedPref.getBoolean("duplication_enabled", false)) {
            checkBoxLoggerBirdUnhandledExceptionPopupDuplication.isChecked = true
        }
        checkBoxLoggerBirdUnhandledExceptionPopupDuplication.setOnClickListener {
            if (checkBoxLoggerBirdUnhandledExceptionPopupDuplication.isChecked) {
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
        textViewLoggerBirdUnhandledExceptionPopupDiscard.setSafeOnClickListener {
            if (filePath.exists()) {
                filePath.delete()
            }
            if (sharedPref.getString("unhandled_media_file_path", null) != null) {
                val mediaFile = File(sharedPref.getString("unhandled_media_file_path", null)!!)
                mediaFile.delete()
            }
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.remove("unhandled_file_path")
            if (sharedPref.getString("unhandled_media_file_path", null) != null) {
                editor.remove("unhandled_media_file_path")
            }
            editor.apply()
            defaultToast.attachToast(
                activity = activity,
                toastMessage = context.resources.getString(R.string.unhandled_file_discard_success)
            )
            removeLoggerBirdUnhandledExceptionLayout()
        }
        textViewLoggerBirdUnhandledExceptionPopupShare.setSafeOnClickListener {
            removeLoggerBirdUnhandledExceptionLayout()
            initializeFloatingActionButton(activity = activity)
            shareView(filePathMedia = filePath)
        }
    }


    //Bitbucket
    /**
     * This method is used for creating bitbucket layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeBitbucketLayout(filePathMedia: File) {
        try {
            removeBitbucketLayout()
            viewBitbucket = LayoutInflater.from(activity)
                .inflate(
                    R.layout.loggerbird_bitbucket_popup,
                    (this.rootView as ViewGroup),
                    false
                )
            windowManagerParamsBitbucket = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            windowManagerBitbucket = activity.getSystemService(Context.WINDOW_SERVICE)!!
            (windowManagerBitbucket as WindowManager).addView(
                viewBitbucket,
                windowManagerParamsBitbucket
            )

            activity.window.navigationBarColor =
                ContextCompat.getColor(this, R.color.black)
            activity.window.statusBarColor = ContextCompat.getColor(this, R.color.black)

            buttonBitbucketCancel = viewBitbucket.findViewById(R.id.button_bitbucket_cancel)
            buttonBitbucketCreate = viewBitbucket.findViewById(R.id.button_bitbucket_create)
            toolbarBitbucket = viewBitbucket.findViewById(R.id.toolbar_bitbucket)
            autoTextViewBitbucketProject =
                viewBitbucket.findViewById(R.id.auto_textView_bitbucket_project)
            autoTextviewBitbucketKind =
                viewBitbucket.findViewById(R.id.auto_textView_bitbucket_kind)
            autoTextViewBitbucketAssignee =
                viewBitbucket.findViewById(R.id.auto_textView_bitbucket_assignee)
            autoTextViewBitbucketPriority =
                viewBitbucket.findViewById(R.id.auto_textView_bitbucket_priority)
            editTextBitbucketTitle = viewBitbucket.findViewById(R.id.editText_bitbucket_title)
            editTextBitbucketDescription =
                viewBitbucket.findViewById(R.id.editText_bitbucket_description)
            editTextBitbucketTitle = viewBitbucket.findViewById(R.id.editText_bitbucket_title)
            editTextBitbucketDescription =
                viewBitbucket.findViewById(R.id.editText_bitbucket_description)
            recyclerViewBitbucketAttachmentList =
                viewBitbucket.findViewById(R.id.recycler_view_bitbucket_attachment)
            scrollViewBitbucket = viewBitbucket.findViewById(R.id.scrollView_bitbucket)
            scrollViewBitbucket.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    hideKeyboard(activity = activity, view = viewBitbucket)
                }
                return@setOnTouchListener false
            }

            toolbarBitbucket.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.bitbucket_menu_save -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        with(sharedPref.edit()) {
                            putString(
                                "bitbucket_project",
                                autoTextViewBitbucketProject.editableText.toString()
                            )
                            putString(
                                "bitbucket_assignee",
                                autoTextViewBitbucketAssignee.editableText.toString()
                            )
                            putString(
                                "bitbucket_kind",
                                autoTextviewBitbucketKind.editableText.toString()
                            )
                            putString(
                                "bitbucket_priority",
                                autoTextViewBitbucketPriority.editableText.toString()
                            )
                            putString(
                                "bitbucket_description",
                                editTextBitbucketDescription.text.toString()
                            )
                            putString(
                                "bitbucket_title",
                                editTextBitbucketTitle.text.toString()
                            )
                            commit()
                        }
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.bitbucket_issue_preferences_save)
                        )
                    }
                    R.id.bitbucket_menu_clear -> {
                        val sharedPref =
                            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                        val editor: SharedPreferences.Editor = sharedPref.edit()
                        editor.remove("bitbucket_project")
                        editor.remove("bitbucket_assignee")
                        editor.remove("bitbucket_kind")
                        editor.remove("bitbucket_priority")
                        editor.remove("bitbucket_description")
                        editor.remove("bitbucket_title")
                        editor.apply()
                        clearBitbucketComponents()
                        defaultToast.attachToast(
                            activity = activity,
                            toastMessage = context.resources.getString(R.string.bitbucket_issue_preferences_delete)
                        )
                    }
                }
                return@setOnMenuItemClickListener true
            }

            toolbarBitbucket.setNavigationOnClickListener {
                removeBitbucketLayout()
                if (controlFloatingActionButtonView()) {
                    floatingActionButtonView.visibility = View.VISIBLE
                }
            }
            initializeBitbucketAttachmentRecyclerView(filePathMedia = filePathMedia)
            buttonClicksBitbucket()
            bitbucketAuthentication.callBitbucket(
                activity = activity,
                context = context,
                task = "get",
                filePathMedia = filePathMedia
            )
            attachProgressBar(task = "bitbucket")
        } catch (e: Exception) {
            finishShareLayout("bitbucket_error")
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.bitbucketTag)
        }
    }

    /**
     * This method is used for initializing bitbucket attachment recyclerView.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeBitbucketAttachmentRecyclerView(filePathMedia: File) {
        arrayListBitbucketFileName.clear()
        recyclerViewBitbucketAttachmentList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        bitbucketAttachmentAdapter =
            RecyclerViewBitbucketAttachmentAdapter(
                addBitbucketFileNames(filePathMedia = filePathMedia),
                context = context,
                activity = activity,
                rootView = rootView
            )
        recyclerViewBitbucketAttachmentList.adapter = bitbucketAttachmentAdapter
    }

    /**
     * This method is used for adding files to bitbucket file list.
     * @param filePathMedia is used for getting the reference of current media file.
     * @return ArrayList<RecyclerViewModel> value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun addBitbucketFileNames(filePathMedia: File): ArrayList<RecyclerViewModel> {
        if (filePathMedia.exists()) {
            arrayListBitbucketFileName.add(RecyclerViewModel(file = filePathMedia))
        }
        if (unhandledMediaFilePath != null) {
            arrayListBitbucketFileName.add(RecyclerViewModel(file = File(unhandledMediaFilePath!!)))
        }
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListBitbucketFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListBitbucketFileName
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_bitbucket_popup.
     */
    private fun buttonClicksBitbucket() {
        buttonBitbucketCreate.setSafeOnClickListener {
            bitbucketAuthentication.gatherAutoTextDetails(
                autoTextViewProject = autoTextViewBitbucketProject,
                autoTextViewPriority = autoTextViewBitbucketPriority,
                autoTextViewAssignee = autoTextViewBitbucketAssignee,
                autoTextViewKind = autoTextviewBitbucketKind
            )
            bitbucketAuthentication.gatherEditTextDetails(
                editTextDescription = editTextBitbucketDescription,
                editTextTitle = editTextBitbucketTitle
            )
            if (bitbucketAuthentication.checkBitbucketProject(
                    activity = activity,
                    autoTextViewProject = autoTextViewBitbucketProject
                )
                && bitbucketAuthentication.checkBitbucketTitle(
                    activity = activity,
                    editTextTitle = editTextBitbucketTitle
                )
                && bitbucketAuthentication.checkBitbucketAssignee(
                    activity = activity,
                    autoTextViewAssignee = autoTextViewBitbucketAssignee
                )
                && bitbucketAuthentication.checkBitbucketKind(
                    activity = activity,
                    autoTextViewKind = autoTextviewBitbucketKind
                )
                && bitbucketAuthentication.checkBitbucketPriority(
                    activity = activity,
                    autoTextViewPriority = autoTextViewBitbucketPriority
                )
            ) {
                bitbucketAuthentication.callBitbucket(
                    activity = activity,
                    context = context,
                    task = "create"
                )
                attachProgressBar(task = "bitbucket")
            }
        }
        buttonBitbucketCancel.setSafeOnClickListener {
            removeBitbucketLayout()
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * This method is used for removing loggerbird_bitbucket_popup from window.
     */
    internal fun removeBitbucketLayout() {
        if (this::viewBitbucket.isInitialized && windowManagerBitbucket != null) {
            (windowManagerBitbucket as WindowManager).removeViewImmediate(
                viewBitbucket
            )
            windowManagerBitbucket = null
        }
    }

    /**
     * This method is used for initializing  autoCompleteTextViews in the loggerbird_bitbucket_popup.
     * @param arrayListBitbucketProject is used for getting the project list for project autoCompleteTextView.
     * @param arrayListBitbucketAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param arrayListBitbucketKind is used for getting the kind list for kind autoCompleteTextView.
     * @param arrayListBitbucketPriority is used for getting the priority list for priority autoCompleteTextView.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    internal fun initializeBitbucketAutoTextViews(
        arrayListBitbucketProject: ArrayList<String>,
        arrayListBitbucketAssignee: ArrayList<String>,
        arrayListBitbucketKind: ArrayList<String>,
        arrayListBitbucketPriority: ArrayList<String>
    ) {
        val sharedPref =
            PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        editTextBitbucketTitle.setText(
            sharedPref.getString(
                "bitbucket_title",
                null
            )
        )
        editTextBitbucketDescription.setText(
            sharedPref.getString(
                "bitbucket_description",
                null
            )
        )
        initializeBitbucketProject(
            arrayListBitbucketProject = arrayListBitbucketProject,
            sharedPref = sharedPref
        )
        initializeBitbucketAssignee(
            arrayListBitbucketAssignee = arrayListBitbucketAssignee,
            sharedPref = sharedPref
        )
        initializeBitbucketPriority(
            arrayListBitbucketPriority = arrayListBitbucketPriority,
            sharedPref = sharedPref
        )
        initializeBitbucketKind(
            arrayListBitbucketKind = arrayListBitbucketKind,
            sharedPref = sharedPref
        )
        detachProgressBar()
    }

    /**
     * This method is used for initializing project autoCompleteTextView in the loggerbird_bitbucket_popup.
     * @param arrayListBitbucketProject is used for getting the project list for project autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeBitbucketProject(
        arrayListBitbucketProject: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewBitbucketProjectAdapter = AutoCompleteTextViewBitbucketProjectAdapter(
            this,
            R.layout.auto_text_view_bitbucket_project_item,
            arrayListBitbucketProject
        )
        autoTextViewBitbucketProject.setAdapter(autoTextViewBitbucketProjectAdapter)
        if (arrayListBitbucketProject.isNotEmpty() && autoTextViewBitbucketProject.editableText.isEmpty()) {
            if (sharedPref.getString("bitbucket_project", null) != null) {
                if (arrayListBitbucketProject.contains(
                        sharedPref.getString(
                            "bitbucket_project",
                            null
                        )!!
                    )
                ) {
                    autoTextViewBitbucketProject.setText(
                        sharedPref.getString("bitbucket_project", null),
                        false
                    )
                } else {
                    autoTextViewBitbucketProject.setText(arrayListBitbucketProject[0], false)
                }
            } else {
                autoTextViewBitbucketProject.setText(arrayListBitbucketProject[0], false)
            }
        }
        autoTextViewBitbucketProject.setOnTouchListener { v, event ->
            autoTextViewBitbucketProject.showDropDown()
            false
        }
        autoTextViewBitbucketProject.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewBitbucket)
            clearBitbucketComponents()
            bitbucketAuthentication.setProjectPosition(projectPosition = position)
            bitbucketAuthentication.callBitbucket(
                activity = activity,
                context = context,
                task = "get"
            )
            attachProgressBar(task = "bitbucket")
        }
    }

    /**
     * This method is used for initializing assignee autoCompleteTextView in the loggerbird_bitbucket_popup.
     * @param arrayListBitbucketAssignee is used for getting the assignee list for assignee autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeBitbucketAssignee(
        arrayListBitbucketAssignee: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewBitbucketAssigneeAdapter = AutoCompleteTextViewBitbucketAssigneeAdapter(
            this,
            R.layout.auto_text_view_bitbucket_assignee_item,
            arrayListBitbucketAssignee
        )
        autoTextViewBitbucketAssignee.setAdapter(autoTextViewBitbucketAssigneeAdapter)
        if (arrayListBitbucketAssignee.isNotEmpty() && autoTextViewBitbucketAssignee.editableText.isEmpty()) {
            if (sharedPref.getString("bitbucket_assignee", null) != null) {
                if (arrayListBitbucketAssignee.contains(
                        sharedPref.getString(
                            "bitbucket_assignee",
                            null
                        )!!
                    )
                ) {
                    autoTextViewBitbucketAssignee.setText(
                        sharedPref.getString("bitbucket_assignee", null),
                        false
                    )
                } else {
                    autoTextViewBitbucketAssignee.setText(arrayListBitbucketAssignee[0], false)
                }
            }
        }
        autoTextViewBitbucketAssignee.setOnTouchListener { v, event ->
            autoTextViewBitbucketAssignee.showDropDown()
            false
        }
        autoTextViewBitbucketAssignee.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewBitbucket)
            bitbucketAuthentication.setAssignee(assigneePosition = position)
        }
    }

    /**
     * This method is used for initializing priority autoCompleteTextView in the loggerbird_bitbucket_popup.
     * @param arrayListBitbucketPriority is used for getting the priority list for priority autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeBitbucketPriority(
        arrayListBitbucketPriority: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewBitbucketPriorityAdapter = AutoCompleteTextViewBitbucketPriorityAdapter(
            this,
            R.layout.auto_text_view_bitbucket_priority_item,
            arrayListBitbucketPriority
        )
        autoTextViewBitbucketPriority.setAdapter(autoTextViewBitbucketPriorityAdapter)
        if (arrayListBitbucketPriority.isNotEmpty() && autoTextViewBitbucketPriority.editableText.isEmpty()) {
            if (sharedPref.getString("bitbucket_priority", null) != null) {
                if (arrayListBitbucketPriority.contains(
                        sharedPref.getString(
                            "bitbucket_priority",
                            null
                        )!!
                    )
                ) {
                    autoTextViewBitbucketPriority.setText(
                        sharedPref.getString("bitbucket_priority", null),
                        false
                    )
                } else {
                    autoTextViewBitbucketPriority.setText(arrayListBitbucketPriority[0], false)
                }
            } else {
                autoTextViewBitbucketPriority.setText(arrayListBitbucketPriority[0], false)
            }
        }
        autoTextViewBitbucketPriority.setOnTouchListener { v, event ->
            autoTextViewBitbucketPriority.showDropDown()
            false
        }
        autoTextViewBitbucketPriority.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewBitbucket)
        }
    }

    /**
     * This method is used for initializing kind autoCompleteTextView in the loggerbird_bitbucket_popup.
     * @param arrayListBitbucketKind is used for getting the kind list for kind autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeBitbucketKind(
        arrayListBitbucketKind: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewBitbucketKindAdapter = AutoCompleteTextViewBitbucketKindAdapter(
            this,
            R.layout.auto_text_view_bitbucket_kind_item,
            arrayListBitbucketKind
        )
        autoTextviewBitbucketKind.setAdapter(autoTextViewBitbucketKindAdapter)
        if (arrayListBitbucketKind.isNotEmpty() && autoTextviewBitbucketKind.editableText.isEmpty()) {
            if (sharedPref.getString("bitbucket_kind", null) != null) {
                if (arrayListBitbucketKind.contains(
                        sharedPref.getString(
                            "bitbucket_kind",
                            null
                        )!!
                    )
                ) {
                    autoTextviewBitbucketKind.setText(
                        sharedPref.getString("bitbucket_kind", null),
                        false
                    )
                } else {
                    autoTextviewBitbucketKind.setText(arrayListBitbucketKind[0], false)
                }
            } else {
                autoTextviewBitbucketKind.setText(arrayListBitbucketKind[0], false)
            }
        }
        autoTextviewBitbucketKind.setOnTouchListener { v, event ->
            autoTextviewBitbucketKind.showDropDown()
            false
        }
        autoTextviewBitbucketKind.setOnItemClickListener { parent, view, position, id ->
            hideKeyboard(activity = activity, view = viewBitbucket)
        }
    }

    /**
     * This method is used for clearing bitbucket components.
     */
    private fun clearBitbucketComponents() {
        editTextBitbucketDescription.text = null
        editTextBitbucketTitle.text = null
        autoTextViewBitbucketPriority.setText("", false)
        autoTextviewBitbucketKind.setText("", false)
        autoTextViewBitbucketPriority.setText("", false)
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