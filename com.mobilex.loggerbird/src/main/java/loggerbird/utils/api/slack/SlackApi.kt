package loggerbird.utils.api.slack

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.google.gson.JsonObject
import com.mobilex.loggerbird.R
import loggerbird.constants.Constants
import loggerbird.exception.LoggerBirdException
import kotlinx.coroutines.*
import loggerbird.LoggerBird
import loggerbird.adapter.recyclerView.api.slack.RecyclerViewSlackAttachmentAdapter
import loggerbird.models.AccountIdService
import loggerbird.models.RecyclerViewModel
import okhttp3.*
import loggerbird.services.LoggerBirdService
import loggerbird.utils.other.DefaultToast
import loggerbird.utils.other.InternetConnectionUtil
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.text.StringBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

/** Loggerbird Slack api configuration class **/
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
//    private val slack = Slack.getInstance()
    private val defaultToast: DefaultToast = DefaultToast()
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
        private const val INVITATION_URL =
            "https://slack.com/oauth/v2/authorize?client_id=1176309019584.1151103028997&scope=app_mentions:read,channels:join,channels:read,chat:write,files:write,groups:read,groups:write,im:write,incoming-webhook,mpim:read,mpim:write,usergroups:write,users:read,users:write,usergroups:read,users.profile:read,chat:write.public,team:read"
        internal var token:String? = null
    }

    /**
     * This method is used for calling Slack Api in order to determine operation.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param slackTask is used for determining the task.
     * @param messagePath is used for determining where to send the current message.
     * @param slackType is used for getting the type of slack request.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
        queueCounter = 0
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
                slackExceptionHandler(e = e)
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
//                        checkQueueTime(activity = activity)
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            token = sharedPref.getString("slackAccessToken", "")
            if (token == "") {
                withContext(Dispatchers.IO) {
//                    val convertToken = slack.methods().oauthV2Access {
//                        it.code(LoggerBird.slackApiToken)
//                        it.clientId(CLIENT_ID)
//                        it.clientSecret(CLIENT_SECRET)
//                        it.redirectUri(REDIRECT_URL)
//                    }
//                    val convertedToken = convertToken.accessToken
//                    val sharedPref =
//                        PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
//                    with(sharedPref.edit()) {
//                        putString("slackAccessToken", convertedToken)
//                        commit()
//                    }
                    gatherTaskAccessToken()
                }
            }else{
                when (slackTask) {
                    "get" -> token?.let {
                        arrayListUsers.clear()
                        arrayListUsersName.clear()
                        arrayListChannels.clear()
                        arrayListChannelsId.clear()
                        hashMapChannel.clear()
                        hashMapUser.clear()
//                        gatherSlackDetails(
//                            activity = activity,
//                            context = context,
//                            slack = slack,
//                            token = it,
//                            filePathMedia = filePathMedia
//                        )
                        gatherTaskChannels()
                        gatherTaskUsers()
                    }
                    "create" -> token?.let {
                        slackTask(
                            activity = activity,
                            context = context,
//                            slack = slack,
                            token = token!!,
                            filePathMedia = filePathMedia,
                            messagePath = messagePath,
                            slackType = slackType
                        )
                    }
                }
            }

        } catch (e: Exception) {
            LoggerBird.callEnqueue()
            Log.d(Constants.slackTag, "No Authorizated Token")
        }
    }
//
//    /**
//     * This method is used for gathering all details to be send to Slack.
//     * @param activity is used for getting reference of current activity.
//     * @param context is for getting reference from the application context.
//     * @param slack is used for using Slack reference.
//     * @param token is used for getting Slack Api token.
//     * @param filePathMedia is used getting filepath of the recorded media.
//     * @param slackType is used for getting the type of slack request.
//     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
//     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
//     */
//    private suspend fun gatherSlackDetails(
//        activity: Activity,
//        context: Context,
//        slack: Slack,
//        token: String,
//        filePathMedia: File?,
//        slackType: String? = null
//    ) {
//
//        val coroutineCallGatherDetails = CoroutineScope(Dispatchers.IO)
//        coroutineCallGatherDetails.launch(Dispatchers.IO) {
//            try {
//                queueCounter = 0
//                val coroutineCallGatherDetails = CoroutineScope(Dispatchers.IO)
//                coroutineCallGatherDetails.launch {
//                    arrayListChannels.clear()
//                    arrayListUsers.clear()
//                    hashMapUser.clear()
//                    hashMapChannel.clear()
//                    arrayListUsersName.clear()
//                }
//                withContext(Dispatchers.IO) {
//                    slackTaskGatherChannels(slack = slack, token = token)
//                    slackTaskGatherUsers(slack = slack, token = token)
//                }
//            } catch (e: SocketTimeoutException) {
//                LoggerBirdService.loggerBirdService.finishShareLayout("slack_error")
//                Log.d(Constants.slackTag, "No Authorizated Token")
//                slackExceptionHandler(
//                    e = e,
//                    filePathName = filePathMedia,
//                    socketTimeOut = SocketTimeoutException()
//                )
//
//            }
//        }
//    }

    /**
     * This method is used for updating data fields of Slack.
     */
    private fun updateFields() {
        queueCounter--
//        timerTaskQueue.cancel()
        if (queueCounter == 0) {
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeSlackSpinner(
                    arrayListSlackChannels = arrayListChannels,
                    arrayListSlackUsers = arrayListUsersName
                )
            }
        }
    }

//    /**
//     * This method is used for gathering slack user from Api
//     * @param slack is used for using Slack reference.
//     * @param token is used for getting Slack Api token.
//     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
//     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
//     */
//    private suspend fun slackTaskGatherUsers(
//        slack: Slack,
//        token: String
//    ) {
//        queueCounter++
//        withContext(Dispatchers.IO) {
//            try {
//                val userListBuilder = UsersListRequest.builder().build()
//                val userResponse = slack.methods(token).usersList(userListBuilder).members.forEach {
//                    arrayListUsers.add(it.id)
//                    arrayListUsersName.add(it.name)
//                    hashMapUser[it.name] = it.id
//                }
//                updateFields()
//            } catch (e: Exception) {
//                if (!controlcallSlack) {
//                    controlcallSlack = true
//                    callSlack(
//                        activity,
//                        context,
//                        filePathMedia,
//                        slackTask,
//                        messagePath,
//                        slackType
//                    )
//                    timerTaskQueue.cancel()
//                }
//                Log.d(Constants.slackTag, "Not Authorizated Token")
//                e.printStackTrace()
//            }
//        }
//    }

//    /**
//     * This method is used for gathering Slack channels from Api.
//     * @param slack is used for using Slack reference.
//     * @param token is used for getting Slack Api token.
//     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
//     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
//     */
//    private suspend fun slackTaskGatherChannels(slack: Slack, token: String) {
//        queueCounter++
//        withContext(Dispatchers.IO) {
//            try {
//                val list: List<ConversationType> =
//                    listOf(ConversationType.PRIVATE_CHANNEL, ConversationType.PUBLIC_CHANNEL)
//                val conversationListBuilder = ConversationsListRequest.builder()
//                    .types(list)
//                    .build()
//                val channelResponse = slack.methods(token)
//                    .conversationsList(conversationListBuilder).channels.forEach {
//                    arrayListChannels.add(it.name)
//                    arrayListChannelsId.add(it.id)
//                    hashMapChannel[it.name] = it.id
//                }
//                updateFields()
//            } catch (e: Exception) {
//
//                if (!controlcallSlack) {
//                    controlcallSlack = true
//                    callSlack(
//                        activity,
//                        context,
//                        filePathMedia,
//                        slackTask,
//                        messagePath,
//                        slackType
//                    )
//                }
//                Log.d(Constants.slackTag, "Not Authorizated Token")
//                e.printStackTrace()
//            }
//        }
//    }

    /**
     * This method is used for determining slack task.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
//     * @param slack is used for using Slack reference.
     * @param token is used for getting Slack Api token.
     * @param filePathMedia is used getting filepath of the recorded media.
     * @param slackType is used for getting the type of slack request.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private suspend fun slackTask(
        activity: Activity,
        context: Context,
//        slack: Slack,
        token: String,
        filePathMedia: File?,
        messagePath: String?,
        slackType: String?
    ) {
        val stringBuilder = StringBuilder()
        when (slackType) {
            "user" ->
                try {
                    withContext(Dispatchers.IO) {
                        if (messagePath != null) {
//                            slack.methods(token).chatPostMessage {
//                                it.channel(hashMapUser[arrayListUsersName[spinnerPosition]].toString())
//                                stringBuilder.append("Life Cycle Details:" + "\n")
//                                var classCounter = 0
//                                LoggerBird.classPathList.forEach {
//                                    stringBuilder.append("$it (${LoggerBird.classPathListCounter[classCounter]})\n")
//                                    classCounter++
//                                }
//                                it.text(messageUser + "\n" + stringBuilder.toString())
//                                it.asUser(true)
//                            }
                            stringBuilder.append("Life Cycle Details:" + "\n")
                            var classCounter = 0
                            LoggerBird.classPathList.forEach {
                                stringBuilder.append("$it (${LoggerBird.classPathListCounter[classCounter]})\n")
                                classCounter++
                            }
                            createTaskMessage(
                                channel = hashMapUser[arrayListUsersName[spinnerPosition]].toString(),
                                text = messageUser + "\n" + stringBuilder.toString()
                            )
                        }
//                        if (filePathMedia != null && messagePath != null) {
//                            var fileCounter = 0
//                            do {
//                                val list = ArrayList<String>()
//                                list.add(hashMapUser[arrayListUsersName[spinnerPosition]].toString())
//                                if (RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.size > fileCounter) {
//                                    val file =
//                                        RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths[fileCounter].file
//                                    slack.methods(token).filesUpload {
//                                        it.file(file)
//                                        it.filename(file.name)
//                                        it.channels(list)
//                                    }
//                                    if (file.name != "logger_bird_details.txt") {
//                                        if (file.exists()) {
//                                            file.delete()
//                                        }
//                                    }
//                                } else {
//                                    break
//                                }
//                                fileCounter++
//
//                            } while (RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.iterator()
//                                    .hasNext()
//                            )
//
//                            activity.runOnUiThread {
//                                LoggerBirdService.loggerBirdService.buttonSlackCancel.performClick()
//                            }
//                            LoggerBirdService.loggerBirdService.finishShareLayout("slack")
//                        }
                    }

                } catch (e: Exception) {
                    slackExceptionHandler(e = e)
                }

            "channel" ->
                try {
                    withContext(Dispatchers.IO) {
                        if (messagePath != null) {
//                            slack.methods(token).conversationsJoin {
//                                it.channel(hashMapChannel[arrayListChannels[spinnerPositionChannel]].toString())
//                            }
//
//                            slack.methods(token).chatPostMessage {
//                                stringBuilder.append("Life Cycle Details:" + "\n")
//                                var classCounter = 0
//                                LoggerBird.classPathList.forEach {
//                                    stringBuilder.append("$it (${LoggerBird.classPathListCounter[classCounter]})\n")
//                                    classCounter++
//                                }
//                                it.channel(channel + "\n" + stringBuilder.toString())
//                                it.text(message)
//                                it.asUser(true)
//                            }
                            stringBuilder.append("Life Cycle Details:" + "\n")
                            var classCounter = 0
                            LoggerBird.classPathList.forEach {
                                stringBuilder.append("$it (${LoggerBird.classPathListCounter[classCounter]})\n")
                                classCounter++
                            }
                            createTaskMessage(
                                channel = hashMapChannel[arrayListChannels[spinnerPositionChannel]].toString(),
                                text = message + "\n" + stringBuilder.toString()
                            )
                        }
//
//                        if (filePathMedia != null && messagePath != null) {
//                            var fileCounter = 0
//                            do {
//                                val list = ArrayList<String>()
//                                list.add(channel)
//                                if (RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.size > fileCounter) {
//                                    val file =
//                                        RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths[fileCounter].file
//                                    slack.methods(token).filesUpload {
//                                        it.file(file)
//                                        it.filename(file.name)
//                                        it.channels(list)
//                                    }
//                                    if (file.name != "logger_bird_details.txt") {
//                                        if (file.exists()) {
//                                            file.delete()
//                                        }
//                                    }
//                                } else {
//                                    break
//                                }
//                                fileCounter++
//                            } while (RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.iterator()
//                                    .hasNext()
//                            )
//
//                            activity.runOnUiThread {
//                                LoggerBirdService.loggerBirdService.buttonSlackCancel.performClick()
//                            }
//                            LoggerBirdService.loggerBirdService.finishShareLayout("slack")
//                        }
                    }

                } catch (e: Exception) {
                    slackExceptionHandler(
                        e = e,
                        socketTimeOut = SocketTimeoutException()
                    )
                }
        }
    }

    /**
     * This method is used for checking whether Slack message to be send to channel is empty.
     * @param activity is used for getting reference of current activity.
     * @param context is for getting reference from the application context.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
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
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    internal fun gatherSlackRecyclerViewDetails(arrayListRecyclerViewItems: ArrayList<RecyclerViewModel>) {
        this.arrayListRecyclerViewItems = arrayListRecyclerViewItems
    }

    /**
     * This method is used for checking time for time out situation.
     * @param activity is used for getting reference of current activity.
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    private fun checkQueueTime(activity: Activity) {
        val timerQueue = Timer()
        timerTaskQueue = object : TimerTask() {
            override fun run() {
                activity.runOnUiThread {
                    LoggerBirdService.loggerBirdService.finishShareLayout("slack_error_time_out")
                }
            }
        }
        timerQueue.schedule(timerTaskQueue, 10000)
    }

    /**
     * This method is used for handling exceptions of Slack Api.
     * @param e is used for defining loggerbird.exception.
     * @param filePathName is used getting filepath of the recorded media.
     * @param throwable is used for defining throwable
     * @param socketTimeOut is used for defining socket time out loggerbird.exception
     * @throws exception if error occurs then com.mobilex.loggerbird.loggerbird.exception message will be hold in the instance of takeExceptionDetails
     * method and saves exceptions instance to the txt file with saveExceptionDetails method.
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun slackExceptionHandler(
        e: Exception? = null,
        throwable: Throwable? = null,
        socketTimeOut: SocketTimeoutException? = null
    ) {
        resetSlackValues(shareLayoutMessage ="slack_error" )
        e?.printStackTrace()
        socketTimeOut?.message
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.slackTag,
            throwable = throwable
        )
    }
    //---- Retrofit -----\\
    /**
     * This method is used for getting access-token for slack.
     * @throws exception if error occurs.
     * @see slackExceptionHandler method.
     */
    private fun gatherTaskAccessToken() {
        queueCounter++
        RetrofitSlackClient.getSlackUserClient(url = "https://slack.com/api/")
            .create(AccountIdService::class.java)
            .getSlackAccessToken(code = "523949707746.1300931714102.63177181b056ffc0c614cc99fab399fd162d8a1a60eb9a33aa91a2effe2037a1" , clientId = CLIENT_ID ,clientSecret = CLIENT_SECRET)
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    slackExceptionHandler(throwable = t)
                }

                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    if (response.code() !in 200..299) {
                        slackExceptionHandler()
                    } else {
                            Log.d("slack_access_token_suc", response.code().toString())
                            val accessTokenList = response.body()
                            if(accessTokenList != null){
                                try {
                                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
                                    with(sharedPref.edit()) {
                                        putString("slackAccessToken",accessTokenList["access_token"].asString)
                                        commit()
                                    }
                                    gatherTaskChannels()
                                    gatherTaskUsers()
                                } catch (e: Exception) {
                                   slackExceptionHandler(e = e)
                                }
                            }else{
                                slackExceptionHandler()
                            }
                            updateFields()

                    }
                }
            })
    }
    /**
     * This method is used for getting channels for slack.
     * @throws exception if error occurs.
     * @see slackExceptionHandler method.
     */
    private fun gatherTaskChannels() {
        queueCounter++
        RetrofitSlackClient.getSlackUserClient(url = "https://slack.com/api/")
            .create(AccountIdService::class.java)
            .getSlackChannels()
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    slackExceptionHandler(throwable = t)
                }
                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    if (response.code() !in 200..299) {
                        slackExceptionHandler()
                    } else {
                        val coroutineCallSlackChannels = CoroutineScope(Dispatchers.IO)
                        coroutineCallSlackChannels.async {
                            Log.d("slack_channels_suc", response.code().toString())
                            val channelsList = response.body()
                                 channelsList?.get("channels")?.asJsonArray?.forEach {
                                    arrayListChannelsId.add(it.asJsonObject["id"].asString)
                                     arrayListChannels.add(it.asJsonObject["name"].asString)
                                     hashMapChannel[it.asJsonObject["name"].asString] = it.asJsonObject["id"].asString
                                }
                            updateFields()
                        }
                    }
                }
            })
    }
    /**
     * This method is used for getting users for slack.
     * @throws exception if error occurs.
     * @see slackExceptionHandler method.
     */
    private fun gatherTaskUsers() {
        queueCounter++
        RetrofitSlackClient.getSlackUserClient(url = "https://slack.com/api/")
            .create(AccountIdService::class.java)
            .getSlackUsers()
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    slackExceptionHandler(throwable = t)
                }
                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    if (response.code() !in 200..299) {
                        slackExceptionHandler()
                    } else {
                        val coroutineCallSlackUsers = CoroutineScope(Dispatchers.IO)
                        coroutineCallSlackUsers.async {
                            Log.d("slack_users_suc", response.code().toString())
                            val usersList = response.body()
                            usersList?.get("members")?.asJsonArray?.forEach {
                                arrayListUsers.add(it.asJsonObject["id"].asString)
                                arrayListUsersName.add(it.asJsonObject["name"].asString)
                                hashMapUser[it.asJsonObject["name"].asString] = it.asJsonObject["id"].asString
                            }
                            updateFields()
                        }
                    }
                }
            })
    }
    /**
     * This method is used for sending messages for slack.
     * @throws exception if error occurs.
     * @see slackExceptionHandler method.
     */
    private fun createTaskMessage(channel: String, text: String) {
        queueCounter++
        val jsonObject = JsonObject()
        jsonObject.addProperty("channel",channel)
        jsonObject.addProperty("text",text)
        jsonObject.addProperty("as_user",true)
        RetrofitSlackClient.getSlackUserClient(url = "https://slack.com/api/")
            .create(AccountIdService::class.java)
            .createSlackMessage(jsonObject = jsonObject)
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    slackExceptionHandler(throwable = t)
                }
                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    if (response.code() !in 200..299) {
                        slackExceptionHandler()
                    } else {
                        val coroutineCallSlackUsers = CoroutineScope(Dispatchers.IO)
                        coroutineCallSlackUsers.async {
                            Log.d("slack_msg_suc", response.code().toString())
                            if(RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.isNotEmpty()){
                                RecyclerViewSlackAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                                    queueCounter++
                                    createTaskAttachment(file = it.file,channel = channel)
                                }
                            }
                            resetSlackValues(shareLayoutMessage = "slack")
                        }
                    }
                }
            })
    }
    /**
     * This method is used for adding attachments for messages for slack.
     * @param file is used for getting reference of the current file.
     * @param channel is ued for getting reference of the current channel.
     * @throws exception if error occurs.
     * @see slackExceptionHandler method.
     */
    private fun createTaskAttachment(file:File,channel: String) {
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        RetrofitSlackClient.getSlackUserClient(url = "https://slack.com/api/")
            .create(AccountIdService::class.java)
            .setSlackAttachments(file = body,channels = channel)
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onFailure(
                    call: retrofit2.Call<JsonObject>,
                    t: Throwable
                ) {
                    slackExceptionHandler(throwable = t)
                }
                override fun onResponse(
                    call: retrofit2.Call<JsonObject>,
                    response: retrofit2.Response<JsonObject>
                ) {
                    if (response.code() !in 200..299) {
                        slackExceptionHandler()
                    } else {
                        val coroutineCallSlackAttachments = CoroutineScope(Dispatchers.IO)
                        coroutineCallSlackAttachments.async {
                            Log.d("slack_attachment_suc", response.code().toString())
                            resetSlackValues(shareLayoutMessage = "slack")
                        }
                    }
                }
            })
    }
    /**
     * This method is used for resetting the values in slack action.
     */
    private fun resetSlackValues(shareLayoutMessage:String) {
        queueCounter--
        if (queueCounter == 0 || shareLayoutMessage == "slack_error" || shareLayoutMessage == "slack_error_time_out") {
//            timerTaskQueue.cancel()
            arrayListUsersName.clear()
            arrayListUsers.clear()
            arrayListChannelsId.clear()
            arrayListChannels.clear()
            hashMapUser.clear()
            hashMapChannel.clear()
            message = ""
            messageUser = ""
            spinnerPosition = 0
            spinnerPositionChannel = 0
            filePathMedia = null
            slackType = null
            messagePath = null
            controlcallSlack = false
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.finishShareLayout(shareLayoutMessage)
            }
        }
    }
}
