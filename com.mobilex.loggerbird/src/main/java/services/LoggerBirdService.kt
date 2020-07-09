package services

import adapter.autoCompleteTextViews.api.basecamp.AutoCompleteTextViewBasecampCategoryAdapter
import adapter.autoCompleteTextViews.api.trello.AutoCompleteTextViewTrelloLabelAdapter
import adapter.recyclerView.api.asana.RecyclerViewAsanaAttachmentAdapter
import adapter.recyclerView.api.asana.RecyclerViewAsanaSubTaskAdapter
import adapter.recyclerView.api.basecamp.RecyclerViewBasecampAssigneeAdapter
import adapter.recyclerView.api.basecamp.RecyclerViewBasecampAttachmentAdapter
import adapter.recyclerView.api.basecamp.RecyclerViewBasecampNotifyAdapter
import adapter.recyclerView.api.clubhouse.RecyclerViewClubhouseAttachmentAdapter
import adapter.recyclerView.api.github.RecyclerViewGithubAssigneeAdapter
import adapter.recyclerView.api.github.RecyclerViewGithubAttachmentAdapter
import adapter.recyclerView.api.github.RecyclerViewGithubLabelAdapter
import adapter.recyclerView.api.gitlab.RecyclerViewGitlabAttachmentAdapter
import adapter.recyclerView.api.jira.RecyclerViewJiraAttachmentAdapter
import adapter.recyclerView.api.jira.RecyclerViewJiraComponentAdapter
import adapter.recyclerView.api.jira.RecyclerViewJiraFixVersionsAdapter
import adapter.recyclerView.api.jira.RecyclerViewJiraIssueAdapter
import adapter.recyclerView.api.jira.RecyclerViewJiraLabelAdapter
import adapter.recyclerView.api.pivotal.*
import adapter.recyclerView.api.pivotal.RecyclerViewPivotalAttachmentAdapter
import adapter.recyclerView.api.pivotal.RecyclerViewPivotalBlockerAdapter
import adapter.recyclerView.api.pivotal.RecyclerViewPivotalLabelAdapter
import adapter.recyclerView.api.pivotal.RecyclerViewPivotalOwnerAdapter
import adapter.recyclerView.api.slack.RecyclerViewSlackAttachmentAdapter
import adapter.recyclerView.api.trello.RecyclerViewTrelloAttachmentAdapter
import adapter.recyclerView.api.trello.RecyclerViewTrelloLabelAdapter
import adapter.recyclerView.api.trello.RecyclerViewTrelloMemberAdapter
import adapter.recyclerView.email.RecyclerViewEmaiToListAttachmentAdapter
import adapter.recyclerView.email.RecyclerViewEmailAttachmentAdapter
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
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird
import models.*
import models.recyclerView.*
import observers.LogActivityLifeCycleObserver
import org.aviran.cookiebar2.CookieBar
import paint.PaintActivity
import paint.PaintView
import utils.email.EmailUtil
import utils.other.LinkedBlockingQueueUtil
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import java.text.SimpleDateFormat
import android.text.InputFilter
import listeners.floatingActionButtons.FloatingActionButtonOnTouchListener
import listeners.layouts.LayoutFeedbackOnTouchListener
import listeners.layouts.LayoutJiraOnTouchListener
import utils.api.asana.AsanaApi
import utils.api.basecamp.BasecampApi
import utils.api.clubhouse.ClubhouseApi
import utils.api.github.GithubApi
import utils.api.gitlab.GitlabApi
import utils.api.jira.JiraApi
import utils.api.pivotal.PivotalTrackerApi
import utils.api.slack.SlackApi
import utils.api.trello.TrelloApi
import utils.other.DefaultToast
import utils.other.InputTypeFilter

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
    private var coroutineCallScreenShot: CoroutineScope = CoroutineScope(Dispatchers.IO)
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
    private lateinit var cookieBar: CookieBar
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
    private val fileLimit: Long = 10485760
    private var sessionTimeStart: Long? = System.currentTimeMillis()
    private var sessionTimeEnd: Long? = null
    private var timeControllerVideo: Long? = null
    private var controlTimeControllerVideo: Boolean = false
    private lateinit var mediaCodecsFile: File
    private val arrayListFileName: ArrayList<String> = ArrayList()
    private val coroutineCallFilesAction: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var controlFileAction: Boolean = false
    private lateinit var progressBarView: View
    private val defaultToast: DefaultToast =
        DefaultToast()

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
    private lateinit var autoTextViewJiraProjectAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraIssueTypeAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraReporterAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraLinkedIssueAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraIssueAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraAssigneeAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraPriorityAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewFixVersionsAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraComponentAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraLabelAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraEpicLinkAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraSprintAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewJiraEpicNameAdapter: ArrayAdapter<String>
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
    private lateinit var progressBarSlack: ProgressBar
    private lateinit var progressBarSlackLayout: FrameLayout
    private lateinit var slackChannelLayout: ScrollView
    private lateinit var slackUserLayout: ScrollView
    private lateinit var slackBottomNavigationView: BottomNavigationView
    private lateinit var toolbarSlack: Toolbar

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
    private lateinit var spinnerGitlabMilestone: Spinner
    private lateinit var spinnerGitlabAssignee: Spinner
    private lateinit var editTextGitlabWeight: EditText
    private lateinit var spinnerGitlabLabels: Spinner
    private lateinit var spinnerGitlabConfidentiality: Spinner
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
    private lateinit var progressBarGitlab: ProgressBar
    private lateinit var progressBarGitlabLayout: FrameLayout
    private lateinit var autoTextViewGitlabProjectAdapter: ArrayAdapter<String>
    private lateinit var spinnerGitlabAssigneeAdapter: ArrayAdapter<String>
    private lateinit var spinnerGitlabLabelsAdapter: ArrayAdapter<String>
    private lateinit var spinnerGitlabMilestoneAdapter: ArrayAdapter<String>
    private lateinit var spinnerGitlabConfidentialityAdapter: ArrayAdapter<String>
    private val arrayListGitlabFileName: ArrayList<RecyclerViewModel> = ArrayList()

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
    private lateinit var autoTextViewGithubAssigneeAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewGithubLabelsAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewGithubRepoAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewGithubProjectAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewGithubMileStoneAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewGithubLinkedRequestsAdapter: ArrayAdapter<String>
    private val arrayListGithubFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var scrollViewGithub: ScrollView
    private lateinit var recyclerViewGithubAssignee: RecyclerView
    private lateinit var githubAssigneeAdapter: RecyclerViewGithubAssigneeAdapter
    internal lateinit var cardViewGithubAssigneeList: CardView
    private val arrayListGithubAssigneeName: ArrayList<RecyclerViewModelAssignee> = ArrayList()
    private lateinit var imageViewAssignee: ImageView
    private lateinit var arrayListGithubAssignee: ArrayList<String>
    private lateinit var recyclerViewGithubLabel: RecyclerView
    private lateinit var githubLabelAdapter: RecyclerViewGithubLabelAdapter
    internal lateinit var cardViewGithubLabelList: CardView
    private val arrayListGithubLabelName: ArrayList<RecyclerViewModelLabel> = ArrayList()
    private lateinit var imageViewGithubLabel: ImageView
    private lateinit var arrayListGithubLabel: ArrayList<String>

    //Trello
    internal val trelloAuthentication = TrelloApi()
    private lateinit var buttonTrelloCreate: Button
    private lateinit var buttonTrelloCancel: Button
    private lateinit var editTextTrelloTitle: EditText
    private lateinit var toolbarTrello: Toolbar
    private lateinit var recyclerViewTrelloAttachment: RecyclerView
    private lateinit var trelloAttachmentAdapter: RecyclerViewTrelloAttachmentAdapter
    private lateinit var autoTextViewTrelloProject: AutoCompleteTextView
    private lateinit var autoTextViewTrelloBoard: AutoCompleteTextView
    private lateinit var autoTextViewTrelloMember: AutoCompleteTextView
    private lateinit var autoTextViewTrelloLabel: AutoCompleteTextView
    private lateinit var autoTextViewTrelloProjectAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewTrelloBoardAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewTrelloMemberAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewTrelloLabelLabelAdapter: AutoCompleteTextViewTrelloLabelAdapter
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
    private lateinit var autoTextViewPivotalProjectAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewPivotalLabelAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewPivotalStoryTypeAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewPivotalPointsAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewPivotalOwnersAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewPivotalRequesterAdapter: ArrayAdapter<String>
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
    private lateinit var autoTextViewBasecampProjectAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewBasecampCategoryCategoryAdapter: AutoCompleteTextViewBasecampCategoryAdapter
    private lateinit var autoTextViewBasecampAssigneeAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewBasecampNotifyAdapter: ArrayAdapter<String>
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
    private lateinit var autoTextViewAsanaProjectAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewAsanaAssignee: AutoCompleteTextView
    private lateinit var autoTextViewAsanaAssigneeAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewAsanaSector: AutoCompleteTextView
    private lateinit var autoTextViewAsanaCategoryAdapter: ArrayAdapter<String>
    private lateinit var autoTextViewAsanaPriority: AutoCompleteTextView
    private lateinit var autoTextViewAsanaPriorityAdapter: ArrayAdapter<String>
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
    private lateinit var autoTextViewClubhouseProjectAdapter: ArrayAdapter<String>
    private lateinit var spinnerClubhouseEpic: Spinner
    private lateinit var spinnerClubhouseEpicAdapter: ArrayAdapter<String>
    private lateinit var editTextClubhouseStoryName: EditText
    private lateinit var editTextClubhouseStoryDescription: EditText
    private lateinit var editTextClubhouseEstimate: EditText
    private lateinit var spinnerClubhouseStoryType: Spinner
    private lateinit var spinnerClubhouseStoryTypeAdapter: ArrayAdapter<String>
    private lateinit var spinnerClubhouseRequester: Spinner
    private lateinit var spinnerClubhouseRequesterAdapter: ArrayAdapter<String>
    private val arrayListClubhouseFileName: ArrayList<RecyclerViewModel> = ArrayList()
    private lateinit var clubhouseAttachmentAdapter: RecyclerViewClubhouseAttachmentAdapter
    private lateinit var recyclerViewClubhouseAttachment: RecyclerView
    private lateinit var progressBarClubhouse: ProgressBar
    private lateinit var progressBarClubhouseLayout: FrameLayout


    //Static global variables:
    internal companion object {
        internal lateinit var floatingActionButtonView: View
        lateinit var floating_action_button: FloatingActionButton
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
     * @param rootIntent used for getting context reference from the Activity.
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

    /**
     * This method is used for getting reference of current activity and context.
     * @param activity is used for getting reference of current activity.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    internal fun initializeActivity(activity: Activity) {
        this.activity = activity
        this.context = activity
    }


    /**
     * This method is used for creating Main Loggerbird layout which is attached to application overlay.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    internal fun initializeFloatingActionButton(activity: Activity) {
        try {
            if (windowManager != null && this::view.isInitialized) {
                if (revealLinearLayoutShare.visibility == View.VISIBLE) {
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
                    revealLinearLayoutShare = view.findViewById(R.id.reveal_linear_layout_share)
                    textView_send_email = view.findViewById(R.id.textView_send_email)
                    textView_discard = view.findViewById(R.id.textView_discard)
                    textView_share_jira = view.findViewById(R.id.textView_share_jira)
                    textView_share_slack = view.findViewById(R.id.textView_share_slack)
                    textView_share_github = view.findViewById(R.id.textView_share_github)
                    textView_share_trello = view.findViewById(R.id.textView_share_trello)
                    textView_share_pivotal = view.findViewById(R.id.textView_share_pivotal)
                    textView_share_basecamp = view.findViewById(R.id.textView_share_basecamp)
                    textView_share_asana = view.findViewById(R.id.textView_share_asana)
                    textView_share_clubhouse = view.findViewById(R.id.textView_share_clubhouse)
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
                revealLinearLayoutShare = revealLinearLayoutShare
            )
        )
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

        if(LoggerBird.clubhouseIsInitialized()){
            textView_share_clubhouse.visibility = View.VISIBLE
        }

        if(LoggerBird.asanaIsInitialized()){
            textView_share_asana.visibility = View.VISIBLE
        }

        if(LoggerBird.basecampIsInitialized()){
            textView_share_basecamp.visibility = View.VISIBLE
        }

        if(LoggerBird.githubIsInitialized()){
            textView_share_github.visibility = View.VISIBLE
        }

        if(LoggerBird.gitlabIsInitialized()){
            textView_share_gitlab.visibility = View.VISIBLE
        }

        if(LoggerBird.pivotalIsInitialized()){
            textView_share_pivotal.visibility = View.VISIBLE
        }

        if(LoggerBird.slackIsInitialized()){
            textView_share_slack.visibility = View.VISIBLE
        }

        if(LoggerBird.jiraIsInitialized()){
            textView_share_jira.visibility = View.VISIBLE
        }

        if(LoggerBird.trelloIsInitialized()){
            textView_share_trello.visibility = View.VISIBLE
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
            textView_send_email.setSafeOnClickListener {
                //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    attachProgressBar()
//                }
//                sendSingleMediaFile(filePathMedia = filePathMedia)
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

            textView_share_github.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }
                    initializeGithubLayout(filePathMedia = filePathMedia)
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

            textView_share_trello.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }
                    initializeTrelloLayout(filePathMedia = filePathMedia)
                }
            }

            textView_share_pivotal.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }
                    initializePivotalLayout(filePathMedia = filePathMedia)
                }
            }
            textView_share_basecamp.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }
                    initializeBasecampLayout(filePathMedia = filePathMedia)
                }
            }
            textView_share_asana.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }
                    initializeAsanaLayout(filePathMedia = filePathMedia)
                }
            }
            textView_share_clubhouse.setSafeOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (controlFloatingActionButtonView()) {
                        floatingActionButtonView.visibility = View.GONE
                    }
                    initializeClubhouseLayout(filePathMedia = filePathMedia)
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

    /**
     * This method is used for starting or stopping audio recording.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

    /**
     * This method is used for starting audio recording.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
    private fun takeForegroundService() {
        workQueueLinkedVideo.controlRunnable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intentForegroundServiceVideo =
                Intent((context as Activity), LoggerBirdForegroundServiceVideo::class.java)
            startForegroundServiceVideo()
        } else {
            resetEnqueueVideo()
        }
    }

    /**
     * This method is used for starting or stopping video recording.
     * @param requestCode is used for getting reference of request code which is comes from onActivityResult method on activity.
     * @param resultCode is used for getting reference of result code which is comes from onActivityResult method on activity.
     * @param data is used for getting reference of intent data which is comes from onActivityResult method on activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun takeVideoRecording(requestCode: Int, resultCode: Int, data: Intent?) {
        workQueueLinkedVideo.controlRunnable = true
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
                    ColorStateList.valueOf(resources.getColor(R.color.mediaRecordColor))
                floating_action_button.imageTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.white))
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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

    /**
     * This method is used when screen recording is finished.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                stopForegroundServiceVideo()
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    internal fun callVideoRecording(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (LoggerBird.isLogInitAttached()) {
                workQueueLinkedVideo.controlRunnable = false
                runnableList.clear()
                workQueueLinkedVideo.clear()
                callForegroundService()
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

    /**
     * This method is used when device was shaked.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun hearShake() {
        try {
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

    /**
     * This method is used for getting Loggerbird file list.
     */
    private fun getFileList(): ArrayList<String>? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        val gson = Gson()
        val json = sharedPref.getString("file_quantity", "")
        if (json?.isNotEmpty()!!) {
            return gson.fromJson(json, object : TypeToken<ArrayList<String>>() {}.type)
        }
        return null
    }

    /**
     * This method is used for adding file name to file name list.
     * @param fileName is used for getting reference of current file.
     */
    private fun addFileNameList(fileName: String) {
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
        if (getFileList() != null) {
            if (getFileList()!!.size > 10) {
                chooseActionFiles()
            }
        }
    }

    /**
     * This method is used for choosing action for a Loggerbird file(delete or send as email).
     */
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

    /**
     * This method deletes old Loggerbird files.
     * @param controlEmailAction is used for controlling the email action.
     */
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

    /**
     * This method send old Loggerbird files as email.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
                if(checkUnhandledFilePath()){
                    finishShareLayout("unhandled")
                }else{
                    if (controlMedialFile()) {
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
                    resetEnqueueEmail()
                }
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
                    //detachProgressBar()
                }
                "media_error" -> {
                    Toast.makeText(context, R.string.share_media_delete_error, Toast.LENGTH_SHORT)
                        .show()
                    finishErrorFab()
                    //detachProgressBar()
                }
                "single_email" -> {
                    detachProgressBar()
                    removeEmailLayout()
                    Toast.makeText(context, R.string.share_file_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab()
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
                    Toast.makeText(context, R.string.slack_sent_error_time_out, Toast.LENGTH_SHORT)
                        .show()
                    progressBarSlackLayout.visibility = View.GONE
                    progressBarSlack.visibility = View.GONE
                }

                "gitlab" -> {
                    Toast.makeText(context, R.string.gitlab_sent, Toast.LENGTH_SHORT).show()
                    finishSuccessFab()
                    removeGitlabLayout()
                    progressBarGitlabLayout.visibility = View.GONE
                    progressBarGitlab.visibility = View.GONE
                }

                "gitlab_error" -> {
                    removeGitlabLayout()
                    Toast.makeText(context, R.string.gitlab_sent_error, Toast.LENGTH_SHORT).show()
                    progressBarGitlabLayout.visibility = View.GONE
                    progressBarGitlab.visibility = View.GONE

                }

                "gitlab_error_time_out" -> {
                    removeGitlabLayout()
                    Toast.makeText(context, R.string.gitlab_sent_error_time_out, Toast.LENGTH_SHORT)
                        .show()
                    progressBarGitlabLayout.visibility = View.GONE
                    progressBarGitlab.visibility = View.GONE
                }

                "github" -> {
                    detachProgressBar()
                    removeGithubLayout()
                    Toast.makeText(context, R.string.github_issue_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab()
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
                    finishSuccessFab()
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
                    finishSuccessFab()
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
                    finishSuccessFab()
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
                    finishSuccessFab()
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
                    progressBarClubhouseLayout.visibility = View.GONE
                    progressBarClubhouse.visibility = View.GONE
                    removeClubhouseLayout()
                    Toast.makeText(context, R.string.clubhouse_issue_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab()
                }
                "clubhouse_error" -> {
                    progressBarClubhouseLayout.visibility = View.GONE
                    progressBarClubhouse.visibility = View.GONE
                    removeClubhouseLayout()
                    Toast.makeText(context, R.string.clubhouse_issue_failure, Toast.LENGTH_SHORT)
                        .show()
                }
                "clubhouse_error_time_out" -> {
                    progressBarClubhouseLayout.visibility = View.GONE
                    progressBarClubhouse.visibility = View.GONE
                    removeClubhouseLayout()
                    Toast.makeText(context, R.string.clubhouse_issue_time_out, Toast.LENGTH_SHORT)
                        .show()
                }
                "unhandled" ->{
                    Toast.makeText(context, R.string.unhandled_file_discard_success, Toast.LENGTH_SHORT)
                        .show()
                    finishSuccessFab()
                }

            }
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * This method is used for showing an success happened when using a loggerbird action with main floating action button.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun finishSuccessFab() {
        if (controlRevealShareLayout() && controlFloatingActionButtonView()) {
            if(checkUnhandledFilePath()){
                val sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                if (sharedPref.getString("unhandled_file_path", null) != null) {
                    val filepath = File(sharedPref.getString("unhandled_file_path", null)!!)
                    if (filepath.exists()) {
                        filepath.delete()
                    } else {
                        activity.runOnUiThread {
                            defaultToast.attachToast(
                                activity = activity,
                                toastMessage = activity.resources.getString(R.string.unhandled_file_doesnt_exist)
                            )
                        }
                    }
                }
                val editor: SharedPreferences.Editor = sharedPref.edit()
                editor.remove("unhandled_file_path")
                editor.apply()
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
     * This method is used for creating jira layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                    progressBarFeedbackLayout =
                        viewFeedback.findViewById(R.id.feedback_progressbar_background)
                    buttonClicksFeedback()
                }
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
            this.arrayListJiraIssue.clear()
            this.arrayListJiraLabel
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
            initializeJiraLabels(arrayListLabel = arrayListJiraLabel, sharedPref = sharedPref)
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
            autoTextViewJiraSprintAdapter =
                ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewJiraEpicLinkAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
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
        arrayListLabel: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraLabelAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListLabel)
        autoTextViewJiraLabel.setAdapter(autoTextViewJiraLabelAdapter)
        if (arrayListLabel.isNotEmpty()) {
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
        this.arrayListJiraLabel = arrayListLabel
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
        autoTextViewFixVersionsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewJiraComponentAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewJiraPriorityAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewJiraAssigneeAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewJiraIssueAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListJiraIssues)
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
     * @param arrayListJiraIssueTypes is used for getting the linked issues list for linked issues autoCompleteTextView.
     * @param sharedPref is used for getting the reference of SharedPreferences of current activity.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun initializeJiraLinkedIssues(
        arrayListJiraLinkedIssues: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewJiraLinkedIssueAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewJiraReporterAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewJiraIssueTypeAdapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewJiraProjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarJira.visibility = View.VISIBLE
                progressBarJiraLayout.visibility = View.VISIBLE
//                    attachProgressBar()
            }
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
            autoTextViewJiraEpicNameAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
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

    //Slack
    /**
     * This method is used for creating slack layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

        progressBarSlack.visibility = View.GONE
        progressBarSlackLayout.visibility = View.GONE

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

        calendarViewJiraStartDate.minDate = System.currentTimeMillis()
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
            startDate = "$year-$month-$dayOfMonth"
        }

    }

    /**
     * This method is used for creating custom jira date-picker layout which is attached to application overlay.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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

    /**
     * This method is used for gathering unhandled exception details.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                    val textViewShare =
                        it.findViewById<TextView>(R.id.textView_unhandled_share_title)
//                    val textViewCustomizeJira =
//                        it.findViewById<TextView>(R.id.textView_unhandled_jira_customize)
                    val checkBoxDuplication =
                        it.findViewById<CheckBox>(R.id.checkBox_unhandled)
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
                    textViewShare.setSafeOnClickListener {
                        initializeFloatingActionButton(activity = this.activity)
                        shareView(filePathMedia = filePath)
//                        if (checkBoxDuplication.isChecked) {
//                            attachProgressBar()
//                            jiraAuthentication.callJira(
//                                filePathMedia = filePath,
//                                context = context,
//                                activity = activity,
//                                task = "unhandled_duplication",
//                                createMethod = "default"
//                            )
//                        } else {
//                            createDefaultUnhandledJiraIssue(filePath = filePath)
//                        }
                    }
//                    textViewCustomizeJira.setSafeOnClickListener {
//                        if (checkBoxDuplication.isChecked) {
//                            attachProgressBar()
//                            jiraAuthentication.callJira(
//                                filePathMedia = filePath,
//                                context = context,
//                                activity = activity,
//                                task = "unhandled_duplication",
//                                createMethod = "customize"
//                            )
//                        } else {
//                            createCustomizedUnhandledJiraIssue(filePath = filePath)
//                        }
//                    }
                }.setSwipeToDismiss(false)
                .setEnableAutoDismiss(false)
                .show()

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
     * This method is used for sending default unhandled jira issue.
     * @param filePathMedia is used for getting the reference of current media file.
     */
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

    /**
     * This method is used for creating unhandled jira layout.
     */
    internal fun createCustomizedUnhandledJiraIssue(filePath: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (controlFloatingActionButtonView()) {
                floatingActionButtonView.visibility = View.GONE
            }
            initializeJiraLayout(filePathMedia = filePath)
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
     * This method is used after an successful unhandled exception jira issue opened.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun unhandledExceptionCustomizeJiraIssueSent() {
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
    /**
     * This method is used for adding unhandled exception message.
     * @param context is for getting reference from the application context.
     * @param unhandledExceptionMessage is for getting reference of the unhandled exception message.
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
    //Unhandled duplication
    /**
     * This method is used for creating unhandled duplcation layout which is attached to application overlay.
     * @param unhandledExceptionIssueMethod is used for getting the reference of method type for unhandled duplication exception.
     * @param filePath is used for getting the reference of current  file.
     */
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

    /**
     * This method is used for controlling the media file actions.
     * @return Boolean values.
     */
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

    //Email
    /**
     * This method is used for creating email layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     */
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
        initializeEmailAttachmentRecyclerView(filePathMedia = filePathMedia)
        initializeEmailToRecyclerView()
        initializeEmailButtons(filePathMedia = filePathMedia)
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                hideKeyboard(activity = activity, view = viewEmail)
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

    /**
     * This method is used for sending email with attachment.
     * @param filePathMedia is used for getting the reference of current media file.
     * @param to is used for getting reference of email address.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
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
                cardView = cardViewToList,
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
        calendarViewFutureTask.minDate = System.currentTimeMillis()
        frameLayoutFutureDate.setOnClickListener {
            removeFutureDateLayout()
        }
        calendarViewFutureTask.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendarFuture.set(year, month, dayOfMonth)
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeGithubLayout(filePathMedia: File) {
        try {
            removeGithubLayout()
            viewGithub = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_github_popup, (this.rootView as ViewGroup), false)

            if (Settings.canDrawOverlays(activity)) {
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
                imageViewAssignee = viewGithub.findViewById(R.id.imageView_assignee_add)

                recyclerViewGithubLabel = viewGithub.findViewById(R.id.recycler_view_label_list)
                cardViewGithubLabelList = viewGithub.findViewById(R.id.cardView_label_list)
                imageViewGithubLabel = viewGithub.findViewById(R.id.imageView_label_add)


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
                buttonClicksGithub(filePathMedia = filePathMedia)
                githubAuthentication.callGithub(
                    activity = activity,
                    context = context,
                    task = "get",
                    filePathMedia = filePathMedia
                )
                attachProgressBar()

            }
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
                )
            ) {
                attachProgressBar()
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
        imageViewAssignee.setSafeOnClickListener {
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
        autoTextViewGithubRepoAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                attachProgressBar()
            }
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
        autoTextViewGithubAssigneeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewGithubMileStoneAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewGithubProjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewGithubLabelsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewGithubLinkedRequestsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeTrelloLayout(filePathMedia: File) {
        try {
            removeTrelloLayout()
            viewTrello = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_trello_popup, (this.rootView as ViewGroup), false)

            if (Settings.canDrawOverlays(activity)) {
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
                            editor.remove("trello_project")
                            editor.remove("trello_board")
                            editor.remove("trello_member")
                            editor.remove("trello_label")
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
                buttonClicksTrello()
                trelloAuthentication.callTrello(
                    activity = activity,
                    context = context,
                    task = "get",
                    filePathMedia = filePathMedia
                )
                attachProgressBar()
            }
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
            trelloAuthentication.gatherEditTextDetails(editTextTitle = editTextTrelloTitle)
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
                attachProgressBar()
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
        autoTextViewTrelloProjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
            attachProgressBar()
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
        autoTextViewTrelloBoardAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewTrelloMemberAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewTrelloLabelLabelAdapter =
            AutoCompleteTextViewTrelloLabelAdapter(
                this,
                R.layout.auto_text_view_trello_label_item,
                arrayListTrelloLabel,
                arrayListTrelloLabelColor
            )
        autoTextViewTrelloLabel.setAdapter(autoTextViewTrelloLabelLabelAdapter)
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
        autoTextViewTrelloLabel.setText("", false)
        autoTextViewTrelloMember.setText("", false)
        autoTextViewTrelloBoard.setText("", false)
//        autoTextViewTrelloProject.setText("",false)
    }

    /**
     * This method is used for creating trello-time layout which is attached to application overlay.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
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
            LayoutInflater.from(activity)
                .inflate(R.layout.trello_calendar_view, rootView, false)
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
        calendarViewTrello.minDate = System.currentTimeMillis()
        frameLayoutTrelloDate.setOnClickListener {
            removeTrelloDateLayout()
        }
        calendarViewTrello.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendarTrello?.set(year, month, dayOfMonth)
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun initializeGitlabLayout(filePathMedia: File) {
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
                autoTextViewGitlabProject =
                    viewGitlab.findViewById(R.id.auto_textview_gitlab_project)
                editTextGitlabTitle = viewGitlab.findViewById(R.id.editText_gitlab_title)
                editTextGitlabDescription =
                    viewGitlab.findViewById(R.id.editText_gitlab_description)
                editTextGitlabWeight = viewGitlab.findViewById(R.id.editText_gitlab_weight)
                spinnerGitlabMilestone = viewGitlab.findViewById(R.id.spinner_gitlab_milestone)
                spinnerGitlabAssignee = viewGitlab.findViewById(R.id.spinner_gitlab_assignee)
                spinnerGitlabLabels = viewGitlab.findViewById(R.id.spinner_gitlab_labels)
                spinnerGitlabConfidentiality =
                    viewGitlab.findViewById(R.id.spinner_gitlab_confidentiality)
                textViewGitlabDueDate = viewGitlab.findViewById(R.id.textView_gitlab_due_date)
                buttonGitlabCreate = viewGitlab.findViewById(R.id.button_gitlab_create)
                buttonGitlabCancel = viewGitlab.findViewById(R.id.button_gitlab_cancel)
                progressBarGitlab = viewGitlab.findViewById(R.id.gitlab_progressbar)
                progressBarGitlabLayout =
                    viewGitlab.findViewById(R.id.gitlab_progressbar_background)
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
                progressBarGitlab.visibility = View.VISIBLE
                progressBarGitlabLayout.visibility = View.VISIBLE
                initializeGitlabAttachmentRecyclerView(filePathMedia = filePathMedia)
                buttonClicksGitlab(filePathMedia = filePathMedia)
            }
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

        calendarViewGitlabDueDate.minDate = System.currentTimeMillis()
        if (calendarViewGitlabDate != null) {
            calendarViewGitlabDueDate.date = calendarViewGitlabDate!!
        }

        buttonCalendarViewGitlabCancel.setOnClickListener {
            detachGitlabDatePicker()
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
                textViewGitlabDueDate.setTextColor(resources.getColor(R.color.black))
            }
        }
    }

    /**
     * This method is used for creating custom gitlab date-picker layout which is attached to application overlay.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun attachGitlabDatePicker() {
        try {
            val rootView: ViewGroup =
                activity.window.decorView.findViewById(android.R.id.content)
            calendarViewGitlabView =
                LayoutInflater.from(activity)
                    .inflate(R.layout.gitlab_calendar_view, rootView, false)
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

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_gitlab_popup.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun buttonClicksGitlab(filePathMedia: File) {
        buttonGitlabCreate.setSafeOnClickListener {
            if (checkGitlabTitleEmpty()) {
                progressBarGitlabLayout.visibility = View.VISIBLE
                progressBarGitlab.visibility = View.VISIBLE
//                attachProgressBar()
                gitlabAuthentication.gatherGitlabEditTextDetails(
                    editTextTitle = editTextGitlabTitle,
                    editTextDescription = editTextGitlabDescription,
                    editTextWeight = editTextGitlabWeight
                )
                gitlabAuthentication.gatherGitlabProjectSpinnerDetails(
                    spinnerAssignee = spinnerGitlabAssignee,
                    spinnerLabels = spinnerGitlabLabels,
                    spinnerMilestone = spinnerGitlabMilestone,
                    spinnerConfidentiality = spinnerGitlabConfidentiality
                )
                gitlabAuthentication.gatherGitlabProjectAutoTextDetails(
                    autoTextViewProject = autoTextViewGitlabProject
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

        initializeGitLabAssignee(arrayListGitlabAssignee = arrayListGitlabAssignee)

        initializeGitLabMilestones(arrayListGitlabMilestones = arrayListGitlabMilestones)

        initializeGitLabLabels(arrayListGitlabLabels = arrayListGitlabLabels)

        initializeGitLabConfidentiality(arrayListGitlabConfidentiality = arrayListGitlabConfidentiality)

        progressBarGitlab.visibility = View.GONE
        progressBarGitlabLayout.visibility = View.GONE
    }

    /**
     * This method is used for initializing project spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabProjects is used for getting the project list for project spinner.
     */
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeGitlabProject(
        arrayListGitlabProjects: ArrayList<String>
    ) {

        autoTextViewGitlabProjectAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayListGitlabProjects)
        autoTextViewGitlabProject.setAdapter(autoTextViewGitlabProjectAdapter)

        if (arrayListGitlabProjects.isNotEmpty() && autoTextViewGitlabProject.text.isEmpty()) {
            autoTextViewGitlabProject.setText(arrayListGitlabProjects[0], false)
        }

        autoTextViewGitlabProject.setOnTouchListener { v, event ->
            autoTextViewGitlabProject.showDropDown()
            false
        }

        autoTextViewGitlabProject.setOnItemClickListener { parent, view, position, id ->
            gitlabAuthentication.gitlabProjectPosition(projectPosition = position)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarGitlab.visibility = View.VISIBLE
                progressBarGitlabLayout.visibility = View.VISIBLE

            }
            gitlabAuthentication.callGitlab(
                activity = activity,
                context = context,
                task = "get"
            )
        }
    }

    /**
     * This method is used for initializing assignee spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabAssignee is used for getting the assignee list for assignee spinner.
     */
    internal fun initializeGitLabAssignee(
        arrayListGitlabAssignee: ArrayList<String>
    ) {
        spinnerGitlabAssigneeAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListGitlabAssignee)
        spinnerGitlabAssigneeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGitlabAssignee.adapter = spinnerGitlabAssigneeAdapter

        spinnerGitlabAssignee.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                gitlabAuthentication.gitlabAssigneePosition(assigneePosition = position)
            }
        }
    }

    /**
     * This method is used for initializing label spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabLabels is used for getting the label list for label spinner.
     */
    internal fun initializeGitLabLabels(
        arrayListGitlabLabels: ArrayList<String>
    ) {
        spinnerGitlabLabelsAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListGitlabLabels)
        spinnerGitlabLabelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGitlabLabels.adapter = spinnerGitlabLabelsAdapter

        spinnerGitlabLabels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                gitlabAuthentication.gitlabLabelPosition(labelPosition = position)
            }
        }
    }

    /**
     * This method is used for initializing confidentiality spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabConfidentiality is used for getting the confidentiality list for confidentiality spinner.
     */
    internal fun initializeGitLabConfidentiality(
        arrayListGitlabConfidentiality: ArrayList<String>
    ) {
        spinnerGitlabConfidentialityAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListGitlabConfidentiality)
        spinnerGitlabConfidentialityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGitlabConfidentiality.adapter = spinnerGitlabConfidentialityAdapter

        spinnerGitlabConfidentiality.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    gitlabAuthentication.gitlabConfidentialityPosition(confidentialityPosition = position)
                }
            }
    }

    /**
     * This method is used for initializing milestone spinner in the loggerbird_gitlab_popup.
     * @param arrayListGitlabMilestones is used for getting the milestone list for milestone spinner.
     */
    internal fun initializeGitLabMilestones(
        arrayListGitlabMilestones: ArrayList<String>
    ) {
        spinnerGitlabMilestoneAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListGitlabMilestones)
        spinnerGitlabMilestoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGitlabMilestone.adapter = spinnerGitlabMilestoneAdapter

        spinnerGitlabMilestoneAdapter.notifyDataSetChanged()

        spinnerGitlabMilestone.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    gitlabAuthentication.gitlabMilestonesPosition(milestonePosition = position)
                }
            }
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializePivotalLayout(filePathMedia: File) {
        try {
            removePivotalLayout()
            viewPivotal = LayoutInflater.from(activity)
                .inflate(
                    R.layout.loggerbird_pivotal_tracker_popup,
                    (this.rootView as ViewGroup),
                    false
                )

            if (Settings.canDrawOverlays(activity)) {
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
                attachProgressBar()
            }
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
                attachProgressBar()
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
        autoTextViewPivotalProjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
            attachProgressBar()
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
        autoTextViewPivotalStoryTypeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewPivotalPointsAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewPivotalRequesterAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewPivotalOwnersAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewPivotalLabelAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeBasecampLayout(filePathMedia: File) {
        try {
            removeBasecampLayout()
            viewBasecamp = LayoutInflater.from(activity)
                .inflate(
                    R.layout.loggerbird_basecamp_popup,
                    (this.rootView as ViewGroup),
                    false
                )

            if (Settings.canDrawOverlays(activity)) {
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
                attachProgressBar()
            }
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
                attachProgressBar()
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
            arrayListProject = arrayListBasecampProject,
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
        arrayListProject: ArrayList<String>,
        sharedPref: SharedPreferences
    ) {
        autoTextViewBasecampProjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            arrayListProject
        )
        autoTextViewBasecampProject.setAdapter(autoTextViewBasecampProjectAdapter)
        if (arrayListProject.isNotEmpty() && autoTextViewBasecampProject.editableText.isEmpty()) {
            if (sharedPref.getString("basecamp_project", null) != null) {
                if (arrayListProject.contains(
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
                    autoTextViewBasecampProject.setText(arrayListProject[0], false)
                }
            } else {
                autoTextViewBasecampProject.setText(arrayListProject[0], false)
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
            attachProgressBar()
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
//        autoTextViewBasecampCategoryAdapter = ArrayAdapter(
//            this,
//            android.R.layout.simple_dropdown_item_1line,
//            arrayListCategory
//        )
        autoTextViewBasecampCategoryCategoryAdapter =
            AutoCompleteTextViewBasecampCategoryAdapter(
                this,
                R.layout.auto_text_view_basecamp_icon_item,
                arrayListBasecampCategory,
                arrayListBasecampCategoryIcon
            )
        autoTextViewBasecampCategory.setAdapter(autoTextViewBasecampCategoryCategoryAdapter)
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
        autoTextViewBasecampAssigneeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewBasecampNotifyAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
            LayoutInflater.from(activity)
                .inflate(R.layout.basecamp_calendar_view, rootView, false)
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
        calendarViewBasecamp.minDate = System.currentTimeMillis()
        frameLayoutBasecampDate.setOnClickListener {
            removeBasecampDateLayout()
        }
        calendarViewBasecamp.setOnDateChangeListener { view, year, month, dayOfMonth ->
            startDate = "$year-$month-$dayOfMonth"
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
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeAsanaLayout(filePathMedia: File) {
        try {
            removeAsanaLayout()
            viewAsana = LayoutInflater.from(activity)
                .inflate(
                    R.layout.loggerbird_asana_popup,
                    (this.rootView as ViewGroup),
                    false
                )

            if (Settings.canDrawOverlays(activity)) {
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
                attachProgressBar()
            }
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
                attachProgressBar()
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
        autoTextViewAsanaProjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
            attachProgressBar()
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
        autoTextViewAsanaCategoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            arrayListAsanaSection
        )
        autoTextViewAsanaSector.setAdapter(autoTextViewAsanaCategoryAdapter)
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
        autoTextViewAsanaPriorityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
        autoTextViewAsanaAssigneeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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
            LayoutInflater.from(activity)
                .inflate(R.layout.asana_calendar_view, rootView, false)
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
        calendarViewAsana.minDate = System.currentTimeMillis() + 86400000
        frameLayoutAsanaDate.setOnClickListener {
            removeAsanaDateLayout()
        }
        calendarViewAsana.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val tempMonth: String = if (month in 1..9) {
                "0$month"
            } else {
                month.toString()
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
        arrayListAsanaFileName
        asanaSubTasksAdapter.notifyDataSetChanged()
        asanaAttachmentAdapter.notifyDataSetChanged()
        editTextAsanaSubTask.text = null
        editTextAsanaDescription.text = null
        editTextAsanaTaskName.text = null
        autoTextViewAsanaPriority.setText("", false)
        autoTextViewAsanaSector.setText("", false)
        autoTextViewAsanaAssignee.setText("", false)
//        autoTextViewPivotalProject.setText("",false)
    }


    //Clubhouse
    /**
     * This method is used for creating clubhouse layout which is attached to application overlay.
     * @param filePathMedia is used for getting the reference of current media file.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun initializeClubhouseLayout(filePathMedia: File) {
        try {
            removeClubhouseLayout()
            viewClubhouse = LayoutInflater.from(activity)
                .inflate(R.layout.loggerbird_clubhouse_popup, (this.rootView as ViewGroup), false)

            if (Settings.canDrawOverlays(activity)) {
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
                spinnerClubhouseRequester =
                    viewClubhouse.findViewById(R.id.spinner_clubhouse_requester)
                spinnerClubhouseStoryType =
                    viewClubhouse.findViewById(R.id.spinner_clubhouse_story_type)
                recyclerViewClubhouseAttachment =
                    viewClubhouse.findViewById(R.id.recycler_view_clubhouse_attachment)
                autoTextViewClubhouseProject =
                    viewClubhouse.findViewById(R.id.auto_textview_clubhouse_project)
                spinnerClubhouseEpic = viewClubhouse.findViewById(R.id.spinner_clubhouse_epic)
                textViewClubhouseDueDate =
                    viewClubhouse.findViewById(R.id.textView_clubhouse_due_date)
                editTextClubhouseEstimate =
                    viewClubhouse.findViewById(R.id.editText_clubhouse_estimate_point)
                progressBarClubhouse = viewClubhouse.findViewById(R.id.clubhouse_progressbar)
                progressBarClubhouseLayout =
                    viewClubhouse.findViewById(R.id.clubhouse_progressbar_background)

                clubhouseAuthentication.callClubhouse(
                    activity = activity,
                    context = context,
                    task = "get",
                    filePathMedia = filePathMedia
                )
                progressBarClubhouse.visibility = View.VISIBLE
                progressBarClubhouseLayout.visibility = View.VISIBLE
                initializeClubhouseAttachmentRecyclerView(filePathMedia = filePathMedia)
                buttonClicksClubhouse(filePathMedia = filePathMedia)

            }
        } catch (e: Exception) {
            finishShareLayout("clubhouse_error")
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.clubhouseTag)
        }
    }

    /**
     * This method is used for initializing project autoCompleteTextView in the loggerbird_clubhouse_popup.
     * @param arrayListClubhouseProjects is used for getting the project list for project autoCompleteTextView.
     */
    @SuppressLint("ClickableViewAccessibility")
    internal fun initializeClubhouseProject(
        arrayListClubhouseProjects: ArrayList<String>
    ) {

        autoTextViewClubhouseProjectAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarClubhouse.visibility = View.VISIBLE
                progressBarClubhouseLayout.visibility = View.VISIBLE

            }
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

        spinnerClubhouseEpicAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListClubhouseEpic)
        spinnerClubhouseEpicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClubhouseEpic.adapter = spinnerClubhouseEpicAdapter
        spinnerClubhouseEpicAdapter.notifyDataSetChanged()

        spinnerClubhouseEpic.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                clubhouseAuthentication.clubhouseEpicPosition(epicPosition = position)
            }
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
    internal fun initializeClubhouseSpinner(
        arrayListClubhouseRequester: ArrayList<String>,
        arrayListClubhouseProjects: ArrayList<String>,
        arrayListClubhouseStoryType: ArrayList<String>,
        arrayListClubhouseEpic: ArrayList<String>

    ) {
        initializeClubhouseProject(arrayListClubhouseProjects)
        initializeClubhouseRequester(arrayListClubhouseRequester)
        initializeClubhouseStoryType(arrayListClubhouseStoryType)
        initializeClubhouseEpic(arrayListClubhouseEpic)
        progressBarClubhouse.visibility = View.GONE
        progressBarClubhouseLayout.visibility = View.GONE
    }

    /**
     * This method is used for initializing story type autoCompleteTextView in the loggerbird_clubhouse_popup.
     * @param arrayListClubhouseStoryType is used for getting the story type list for story type autoCompleteTextView.
     */
    internal fun initializeClubhouseStoryType(
        arrayListClubhouseStoryType: ArrayList<String>
    ) {
        spinnerClubhouseStoryTypeAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListClubhouseStoryType)
        spinnerClubhouseStoryTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClubhouseStoryType.adapter = spinnerClubhouseStoryTypeAdapter
        spinnerClubhouseStoryTypeAdapter.notifyDataSetChanged()

        spinnerClubhouseStoryType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clubhouseAuthentication.clubhouseStoryTypePosition(storyTypePosition = position)
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
                LayoutInflater.from(activity)
                    .inflate(R.layout.clubhouse_calendar_view, rootView, false)
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
        var dueDate: String = ""
        var dueDateFormat: String = ""
        calendarViewClubhouseLayout =
            calendarViewClubhouseView.findViewById(R.id.clubhouse_calendar_view_layout)
        calendarViewClubhouseDueDate =
            calendarViewClubhouseView.findViewById(R.id.calendarView_clubhouse_due_date)
        buttonCalendarViewClubhouseCancel =
            calendarViewClubhouseView.findViewById(R.id.button_clubhouse_calendar_cancel)
        buttonCalendarViewClubhouseOk =
            calendarViewClubhouseView.findViewById(R.id.button_clubhouse_calendar_ok)

        calendarViewClubhouseDueDate.minDate = System.currentTimeMillis()
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
            }
        }

        buttonCalendarViewClubhouseCancel.setOnClickListener {
            detachClubhouseDatePicker()
        }

        buttonCalendarViewClubhouseOk.setOnClickListener {
            if (dueDate != null) {
                clubhouseAuthentication.dueDate = dueDateFormat
            }
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
     * This method is used for initializing requester autoCompleteTextView in the loggerbird_clubhouse_popup.
     * @param arrayListClubhouseRequester is used for getting the requester list for requester autoCompleteTextView.
     */
    internal fun initializeClubhouseRequester(
        arrayListClubhouseRequester: ArrayList<String>
    ) {
        spinnerClubhouseRequesterAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListClubhouseRequester)
        spinnerClubhouseRequesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClubhouseRequester.adapter = spinnerClubhouseRequesterAdapter
        spinnerClubhouseRequesterAdapter.notifyDataSetChanged()

        spinnerClubhouseRequester.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    clubhouseAuthentication.clubhouseUserPosition(userPosition = position)
                }
            }
    }

    /**
     * This method is used for initializing button clicks of buttons that are inside in the loggerbird_clubhouse_popup.
     * @param filePathMedia is used for getting the reference of current media file.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun buttonClicksClubhouse(filePathMedia: File) {
        buttonClubhouseCreate.setSafeOnClickListener {
            if (checkClubhouseStoryNameEmpty() && checkClubhouseStoryDescriptionEmpty() &&
                checkClubhouseStoryDueDateEmpty() && checkClubhouseStoryEstimatePoint()
            ) {

                clubhouseAuthentication.gatherClubhouseSpinnerDetails(
                    spinnerUser = spinnerClubhouseRequester,
                    spinnerStoryType = spinnerClubhouseStoryType,
                    spinnerEpic = spinnerClubhouseEpic
                )

                clubhouseAuthentication.gatherClubhouseProjectAutoTextDetails(
                    autoTextViewProject = autoTextViewClubhouseProject
                )

                clubhouseAuthentication.gatherClubhouseEditTextDetails(
                    editTextStoryName = editTextClubhouseStoryName,
                    editTextStoryDescription = editTextClubhouseStoryDescription,
                    editTextEstimate = editTextClubhouseEstimate
                )

                progressBarClubhouse.visibility = View.VISIBLE
                progressBarClubhouseLayout.visibility = View.VISIBLE
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
     * This method is used for date field is not empty in clubhouse layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkClubhouseStoryDueDateEmpty(): Boolean {
        if (textViewClubhouseDueDate.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.textView_clubhouse_story_duedate_empty)
            )
        }
        return false
    }

    /**
     * This method used for estimate point field is not empty in clubhouse layout.
     * @return Boolean value.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun checkClubhouseStoryEstimatePoint(): Boolean {
        if (editTextClubhouseEstimate.text.toString().isNotEmpty()) {
            return true
        } else {
            defaultToast.attachToast(
                activity = activity,
                toastMessage = activity.resources.getString(R.string.textView_clubhouse_story_estimate_empty)
            )
        }
        return false
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
        if (LoggerBird.filePathSecessionName.exists()) {
            arrayListClubhouseFileName.add(RecyclerViewModel(file = LoggerBird.filePathSecessionName))
        }
        return arrayListClubhouseFileName
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