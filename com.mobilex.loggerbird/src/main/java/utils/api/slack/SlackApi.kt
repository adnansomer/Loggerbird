package utils.api.slack

import adapter.recyclerView.api.slack.RecyclerViewSlackAttachmentAdapter
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.mobilex.loggerbird.R
import com.slack.api.Slack
import com.slack.api.methods.request.conversations.ConversationsListRequest
import com.slack.api.methods.request.users.UsersListRequest
import com.slack.api.model.ConversationType
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import models.RecyclerViewModel
import okhttp3.*
import services.LoggerBirdService
import utils.other.DefaultToast
import utils.other.InternetConnectionUtil
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/** Loggerbird Slack api configration class **/
internal class SlackApi {
    private var coroutineCallSlack: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallOkHttpSlack: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private val arrayListChannels: ArrayList<String> = ArrayList()
    private val arrayListChannelsId: ArrayList<String> = ArrayList()
    private val arrayListUsers: ArrayList<String> = ArrayList()
    private val arrayListUsersName: ArrayList<String> = ArrayList()
    private var message: String = ""
    private var messageUser: String = ""
    private var spinnerPosition: Int = 0
    private var spinnerPositionChannel: Int = 0
    internal lateinit var user: String
    internal lateinit var channel: String
    private lateinit var arrayListRecyclerViewItems: ArrayList<RecyclerViewModel>
    private val hashMapUser: HashMap<String, String> = HashMap()
    private val hashMapChannel: HashMap<String, String> = HashMap()
    private var convertedToken: String = ""
    private val slack = Slack.getInstance()
    private val defaultToast: DefaultToast =
        DefaultToast()
    private var queueCounter: Int = 0
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var filePathMedia: File? = null
    private lateinit var slackTask: String
    private var messagePath: String? = null
    private var slackType: String? = null
    private var controlcallSlack: Boolean = false
    private lateinit var timerTaskQueue: TimerTask
    /** Loggerbird slack app client information **/
    companion object {
        internal const val CLIENT_ID = "1176309019584.1151103028997"
        internal const val CLIENT_SECRET = "6147f0bd55a0c777893d07c91f3b16ef"
        private const val REDIRECT_URL = "https://app.slack.com/client"
        private const val APP_ID = "A014F310UVB"
        private const val INVITATION_URL = "https://slack.com/oauth/v2/authorize?client_id=1176309019584.1151103028997&scope=app_mentions:read,channels:join,channels:read,chat:write,files:write,groups:read,groups:write,im:write,incoming-webhook,mpim:read,mpim:write,usergroups:write,users:read,users:write,usergroups:read,users.profile:read,chat:write.public,team:read"
    }

    /**
     * This method is used for calling Slack Api in order to determine operation.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param slackTask is used for determining the task.
     * @param messagePath is used for determining where to send the current message.
     * @param slackType is used for getting the type of slack request.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun callSlack(
        activity: Activity,
        context: Context,
        filePathMedia: File? = null,
        slackTask: String,
        messagePath: String? = null,
        slackType: String? = null
    ) {

        this.activity = activity
        this.context = context
        this.filePathMedia = filePathMedia
        this.slackTask = slackTask
        this.messagePath = messagePath
        this.slackType = slackType

        coroutineCallOkHttpSlack.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    okHttpSlackAuthentication(
                        activity = activity,
                        context = context,
                        filePathMediaName = filePathMedia,
                        slackTask = slackTask,
                        messagePath = messagePath,
                        slackType = slackType
                    )
                } else {
                    activity.runOnUiThread {
                        Toast.makeText(
                            context,
                            R.string.network_check_failure,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    throw LoggerBirdException(
                        Constants.networkErrorMessage
                    )
                }

            } catch (e: Exception) {
                slackExceptionHandler(e = e, filePathName = filePathMedia)
            }
        }
    }

    /**
     * This method is used for checking OkHttp connection.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param slackTask is used for determining the task.
     * @param messagePath is used for determining where to send the current message.
     * @param slackType is used for getting the type of slack request.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun okHttpSlackAuthentication(
        activity: Activity,
        context: Context,
        filePathMediaName: File?,
        slackTask: String,
        messagePath: String? = null,
        slackType: String? = null
    ) {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://app.slack.com/client")
            .build()
        client.newCall(request).enqueue(object : Callback {
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onFailure(call: Call, e: IOException) {
                slackExceptionHandler(e = e)
            }

            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        checkQueueTime(activity = activity)
                        coroutineCallSlack.async {
                            try {
                                slackAuthentication(
                                    activity = activity,
                                    context = context,
                                    filePathMedia = filePathMediaName,
                                    slackTask = slackTask,
                                    messagePath = messagePath,
                                    slackType = slackType
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                LoggerBird.callEnqueue()
                                LoggerBird.callExceptionDetails(
                                    exception = e,
                                    tag = Constants.slackTag
                                )
                            }
                        }
                    } else {
                        activity.runOnUiThread {
                            Toast.makeText(
                                context,
                                R.string.internet_connection_check_failure,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        throw LoggerBirdException(
                            Constants.internetErrorMessage
                        )
                    }
                } catch (e: Exception) {
                    slackExceptionHandler(e = e)
                }
            }
        })
    }

    /**
     * This method is used for making authorization with token.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param slackTask is used for determining the task.
     * @param messagePath is used for determining where to send the current message.
     * @param slackType is used for getting the type of slack request.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private suspend fun slackAuthentication(
        activity: Activity,
        context: Context,
        filePathMedia: File?,
        slackTask: String,
        messagePath: String? = null,
        slackType: String? = null
    ) {
        try {
            this.activity = activity
            val sharedPref =
                PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            val token = sharedPref.getString("slackAccessToken", "")
            //val token = "xoxb-523949707746-1185252116928-e77ayP6N5Mv0VfJbYhQ4JyaB" //mobilex
            //val token = "xoxb-1176309019584-1152486968594-k4brnZhlrUXAAy80Be0GmaVv" //loggerbird
            if (token == "") {
                withContext(Dispatchers.IO) {
                    val convertToken = slack.methods().oauthV2Access {
                        it.code(LoggerBird.slackApiToken)
                        it.clientId(CLIENT_ID)
                        it.clientSecret(CLIENT_SECRET)
                        it.redirectUri(REDIRECT_URL)
                    }
                    val convertedToken = convertToken.accessToken
                    val sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                    with(sharedPref.edit()) {
                        putString("slackAccessToken", convertedToken)
                        commit()
                    }
                }
            }

            when (slackTask) {
                "get" -> token?.let {
                    gatherSlackDetails(
                        activity = activity,
                        context = context,
                        slack = slack,
                        token = it,
                        filePathMedia = filePathMedia
                    )
                }
                "create" -> token?.let {
                    slackTask(
                        activity = activity,
                        context = context,
                        slack = slack,
                        token = it,
                        filePathMedia = filePathMedia,
                        messagePath = messagePath,
                        slackType = slackType
                    )
                }
            }

        } catch (e: Exception) {
            slackExceptionHandler(e = e)
            LoggerBird.callEnqueue()
            Log.d(Constants.slackTag, "No Authorizated Token")
            slackExceptionHandler(
                e = e,
                filePathName = filePathMedia,
                socketTimeOut = SocketTimeoutException()
            )
        }
    }

    /**
     * This method is used for gathering all details to be send to Slack.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param slack is used for using Slack reference.
     * @param token is used for getting Slack Api token.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param slackType is used for getting the type of slack request.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private suspend fun gatherSlackDetails(
        activity: Activity,
        context: Context,
        slack: Slack,
        token: String,
        filePathMedia: File?,
        slackType: String? = null
    ) {

        val coroutineCallGatherDetails = CoroutineScope(Dispatchers.IO)
        coroutineCallGatherDetails.launch(Dispatchers.IO) {
            try {
                queueCounter = 0
                val coroutineCallGatherDetails = CoroutineScope(Dispatchers.IO)
                coroutineCallGatherDetails.launch {
                    arrayListChannels.clear()
                    arrayListUsers.clear()
                    hashMapUser.clear()
                    hashMapChannel.clear()
                    arrayListUsersName.clear()
                }
                withContext(Dispatchers.IO) {
                    slackTaskGatherChannels(slack = slack, token = token)
                    slackTaskGatherUsers(slack = slack, token = token)
                }
            } catch (e: SocketTimeoutException) {
                LoggerBirdService.loggerBirdService.finishShareLayout("slack_error")
                Log.d(Constants.slackTag, "No Authorizated Token")
                slackExceptionHandler(
                    e = e,
                    filePathName = filePathMedia,
                    socketTimeOut = SocketTimeoutException()
                )

            }
        }
    }

    /**
     * This method is used for updating data fields of Slack.
     */
    private fun updateFields() {
        queueCounter--
        timerTaskQueue.cancel()
        if (queueCounter == 0) {
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeSlackSpinner(
                    arrayListSlackChannels = arrayListChannels,
                    arrayListSlackUsers = arrayListUsersName
                )
            }
        }
    }

    /**
     * This method is used for gathering slack user from Api
     * @param slack is used for using Slack reference.
     * @param token is used for getting Slack Api token.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private suspend fun slackTaskGatherUsers(
        slack: Slack,
        token: String) {
        queueCounter++
        withContext(Dispatchers.IO) {
            try {
                val userListBuilder = UsersListRequest.builder().build()
                val userResponse = slack.methods(token).usersList(userListBuilder).members.forEach {
                    arrayListUsers.add(it.id)
                    arrayListUsersName.add(it.name)
                    hashMapUser[it.name] = it.id
                }
                updateFields()
            } catch (e: Exception) {
                if (!controlcallSlack) {
                    controlcallSlack = true
                    callSlack(
                        activity,
                        context,
                        filePathMedia,
                        slackTask,
                        messagePath,
                        slackType
                    )
                    timerTaskQueue.cancel()
                }
                Log.d(Constants.slackTag, "Not Authorizated Token")
                e.printStackTrace()
            }
        }
    }

    /**
     * This method is used for gathering Slack channels from Api.
     * @param slack is used for using Slack reference.
     * @param token is used for getting Slack Api token.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private suspend fun slackTaskGatherChannels(slack: Slack, token: String) {
        queueCounter++
        withContext(Dispatchers.IO) {
            try {
                val list: List<ConversationType> =
                    listOf(ConversationType.PRIVATE_CHANNEL, ConversationType.PUBLIC_CHANNEL)
                val conversationListBuilder = ConversationsListRequest.builder()
                    .types(list)
                    .build()
                val channelResponse = slack.methods(token)
                    .conversationsList(conversationListBuilder).channels.forEach {
                    arrayListChannels.add(it.name)
                    arrayListChannelsId.add(it.id)
                    hashMapChannel[it.name] = it.id
                }
                updateFields()
            } catch (e: Exception) {

                if (!controlcallSlack) {
                    controlcallSlack = true
                    callSlack(
                        activity,
                        context,
                        filePathMedia,
                        slackTask,
                        messagePath,
                        slackType
                    )
                }
                Log.d(Constants.slackTag, "Not Authorizated Token")
                e.printStackTrace()
            }
        }
    }

    /**
     * This method is used for determining slack task.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param slack is used for using Slack reference.
     * @param token is used for getting Slack Api token.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param slackType is used for getting the type of slack request.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private suspend fun slackTask(
        activity: Activity,
        context: Context,
        slack: Slack,
        token: String,
        filePathMedia: File?,
        messagePath: String?,
        slackType: String?
    ) {

        when (slackType) {
            "user" ->
                try {
                    withContext(Dispatchers.IO) {
                        if (messagePath != null) {
                            slack.methods(token).chatPostMessage {
                                it.channel(hashMapUser[arrayListUsersName[spinnerPosition]].toString())
                                it.text(messageUser)
                                it.asUser(true)
                            }
                        }
                        if (filePathMedia != null && messagePath != null) {
                            var fileCounter = 0
                            do {
                                val list = ArrayList<String>()
                                list.add(hashMapUser[arrayListUsersName[spinnerPosition]].toString())
                                if (RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.size > fileCounter) {
                                    val file =
                                        RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths[fileCounter].file
                                    slack.methods(token).filesUpload {
                                        it.file(file)
                                        it.filename(file.name)
                                        it.channels(list)
                                    }
                                    if (file.name != "logger_bird_details.txt") {
                                        if (file.exists()) {
                                            file.delete()
                                        }
                                    }
                                } else {
                                    break
                                }
                                fileCounter++

                            } while (RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.iterator()
                                    .hasNext()
                            )

                            activity.runOnUiThread {
                                LoggerBirdService.loggerBirdService.buttonSlackCancel.performClick()
                            }
                            LoggerBirdService.loggerBirdService.finishShareLayout("slack")
                        }
                    }

                } catch (e: Exception) {
                    slackExceptionHandler(e = e, filePathName = filePathMedia)
                }

            "channel" ->
                try {
                    withContext(Dispatchers.IO) {
                        if (messagePath != null) {

                            slack.methods(token).conversationsJoin {
                                it.channel(hashMapChannel[arrayListChannels[spinnerPositionChannel]].toString())
                            }

                            slack.methods(token).chatPostMessage {
                                it.channel(channel)
                                it.text(message)
                                it.asUser(true)
                            }
                        }

                        if (filePathMedia != null && messagePath != null) {
                            var fileCounter = 0
                            do {
                                val list = ArrayList<String>()
                                list.add(channel)
                                if (RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.size > fileCounter) {
                                    val file =
                                        RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths[fileCounter].file
                                    slack.methods(token).filesUpload {
                                        it.file(file)
                                        it.filename(file.name)
                                        it.channels(list)
                                    }
                                    if (file.name != "logger_bird_details.txt") {
                                        if (file.exists()) {
                                            file.delete()
                                        }
                                    }
                                } else {
                                    break
                                }
                                fileCounter++
                            } while (RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.iterator()
                                    .hasNext()
                            )

                            activity.runOnUiThread {
                                LoggerBirdService.loggerBirdService.buttonSlackCancel.performClick()
                            }
                            LoggerBirdService.loggerBirdService.finishShareLayout("slack")
                        }
                    }

                } catch (e: Exception) {
                    slackExceptionHandler(
                        e = e,
                        filePathName = filePathMedia,
                        socketTimeOut = SocketTimeoutException()
                    )
                }
        }
    }

    /**
     * This method is used for checking whether Slack message to be send to channel is empty.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun checkMessageEmpty(activity: Activity, context: Context): Boolean {
        return if (message.isNotEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                activity.runOnUiThread {
                    defaultToast.attachToast(
                        activity = activity,
                        toastMessage = context.resources.getString(R.string.slack_message_empty)
                    )
                }
            }
            false
        }
    }

    /**
     * This method is used for checking whether Slack message to be send to user is empty.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun checkMessageEmptyUser(activity: Activity, context: Context): Boolean {
        return if (messageUser.isNotEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = context.resources.getString(R.string.slack_message_empty)
                )
            }
            false
        }
    }

    /**
     * This method is used for gathering slack channels from spinner.
     * @param spinnerChannel for getting reference of channel spinner.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun gatherSlackChannelSpinnerDetails(
        spinnerChannel: Spinner
    ) {
        spinnerPositionChannel = spinnerChannel.selectedItemPosition
        channel = spinnerChannel.selectedItem.toString()
    }

    /**
     * This method is used for gathering slack users from spinner.
     * @param spinnerUser for getting reference of user spinner.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun gatherSlackUserSpinnerDetails(
        spinnerUser: Spinner
    ) {
        spinnerPosition = spinnerUser.selectedItemPosition
        user = spinnerUser.selectedItem.toString()
    }

    /**
     * This method is used for calling Slack message details.
     * @param editTextMessage for getting reference of slack channel message.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun gatherSlackEditTextDetails(
        editTextMessage: EditText
    ) {
        message = editTextMessage.text.toString()
    }

    /**
     * This method is used for gathering message details to be send to Slack user.
     * @param editTextMessage for getting reference of slack user message.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun gatherSlackUserEditTextDetails(
        editTextMessage: EditText
    ) {
        messageUser = editTextMessage.text.toString()
    }

    /**
     * This method is used for gathering files to be send to Slack.
     * @param arrayListRecyclerViewItems for getting recycler view items
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun gatherSlackRecyclerViewDetails(arrayListRecyclerViewItems: ArrayList<RecyclerViewModel>) {
        this.arrayListRecyclerViewItems = arrayListRecyclerViewItems
    }

    /**
     * This method is used for checking time for time out situation.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("gitlab_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 180000)
    }

    /**
     * This method is used for handling exceptions of Slack Api.
     * @param e is used for defining exception.
     * @param filePathName is used getting filepath of the recorded media.
     * @param throwable is used for defining throwable
     * @param socketTimeOut is used for defining socket time out exception
     * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun slackExceptionHandler(
        e: Exception? = null,
        filePathName: File? = null,
        throwable: Throwable? = null,
        socketTimeOut: SocketTimeoutException? = null
    ) {
        LoggerBirdService.loggerBirdService.finishShareLayout("slack_error")
        e?.printStackTrace()
        socketTimeOut?.message
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.slackTag,
            throwable = throwable
        )
    }
}