package utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.mobilex.loggerbird.R
import com.slack.api.RequestConfigurator
import com.slack.api.Slack
import com.slack.api.methods.request.admin.usergroups.AdminUsergroupsListChannelsRequest
import com.slack.api.methods.request.conversations.ConversationsListRequest
import com.slack.api.methods.request.usergroups.UsergroupsListRequest
import com.slack.api.methods.request.users.UsersInfoRequest
import com.slack.api.methods.request.users.UsersListRequest
import com.slack.api.model.Conversation
import com.slack.api.model.Usergroup
import constants.Constants
import exception.LoggerBirdException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import loggerbird.LoggerBird
import models.RecyclerViewModel
import okhttp3.*
import services.LoggerBirdService
import java.io.File
import java.io.IOException

class SlackAuthentication {
    private var coroutineCallSlack: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallOkHttpSlack: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    private val arrayListChannels: ArrayList<String> = ArrayList()
    private val arrayListUsers: ArrayList<String> = ArrayList()
    private val arrayListUsersName: ArrayList<String> = ArrayList()
    private var message: String = ""
    private var messageUser: String = ""
    private var spinnerPosition : Int = 0
    internal lateinit var user: String
    internal lateinit var channel: String
    private lateinit var arrayListRecyclerViewItems: ArrayList<RecyclerViewModel>
    private val hashMapUser: HashMap<String, String> = HashMap()

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun callSlack(
        activity: Activity,
        context: Context,
        filePathMedia: File? = null,
        slackTask: String,
        messagePath: String? = null
    ) {
        coroutineCallOkHttpSlack.async {
            try {
                if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                    okHttpSlackAuthentication(
                        activity = activity,
                        context = context,
                        filePathMediaName = filePathMedia,
                        slackTask = slackTask,
                        messagePath = messagePath
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
                jiraExceptionHandler(e = e, filePathName = filePathMedia)
            }
        }
    }

    private fun okHttpSlackAuthentication(
        context: Context,
        activity: Activity,
        filePathMediaName: File?,
        slackTask: String,
        messagePath: String? = null

    ) {
        val client = OkHttpClient()
        val request: Request =
            Request.Builder()
                .url("https://app.slack.com/client/T011GLPDZQA/C011GLPEP4J")
                .build()
        client.newCall(request).enqueue(object : Callback {
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onFailure(call: Call, e: IOException) {
                jiraExceptionHandler(e = e)
            }

            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onResponse(call: Call, response: Response) {
                Log.d("response_message", response.message)
                Log.d("response_code", response.code.toString())
                try {
                    if (response.code in 200..299) {
                        coroutineCallSlack.async {
                            try {
                                slackAuthentication(
                                    activity = activity,
                                    context = context,
                                    filePathMedia = filePathMediaName,
                                    slackTask = slackTask,
                                    messagePath = messagePath
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
                    jiraExceptionHandler(e = e)
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private suspend fun slackAuthentication(
        activity: Activity,
        context: Context,
        filePathMedia: File?,
        slackTask: String,
        messagePath: String? = null
    ) {
        val slack = Slack.getInstance()
    //    val token = "xoxb-1050703475826-1145080262722-ky9ACRsZfZcacuifbgXiZmEZ"
        val token = "xoxb-1176309019584-1152486968594-k4brnZhlrUXAAy80Be0GmaVv"

        when (slackTask) {
            "get" -> gatherSlackDetails(
                activity = activity,
                context = context,
                slack = slack,
                token = token,
                filePathMedia = filePathMedia
            )
            "create" -> slackTask(
                activity = activity,
                context = context,
                slack = slack,
                token = token,
                filePathMedia = filePathMedia,
                messagePath = messagePath

            )
        }
    }

    private suspend fun gatherSlackDetails(
        activity: Activity,
        context: Context,
        slack: Slack,
        token: String,
        filePathMedia: File?
    ) {
        arrayListChannels.clear()
        arrayListUsers.clear()
        hashMapUser.clear()
        arrayListUsersName.clear()
        withContext(Dispatchers.IO) {
            slackTaskGatherChannels(slack = slack, token = token)
            slackTaskGatherUsers(slack = slack, token = token)
            activity.runOnUiThread {
                LoggerBirdService.loggerBirdService.initializeSlackSpinner(
                    arrayListChannels = arrayListChannels,
                    arrayListUsers = arrayListUsersName
                )
            }
        }
    }

    private suspend fun slackTaskGatherUsers(slack: Slack, token: String) {
        withContext(Dispatchers.IO) {
            val userListBuilder = UsersListRequest.builder().build()
            slack.methods(token).usersList(userListBuilder).members.forEach {
                arrayListUsers.add(it.id)
                arrayListUsersName.add(it.name)
                hashMapUser.put(it.name,it.id)
//              Log.d("user-name", it.name)
            }

        }
    }

    private suspend fun slackTaskGatherChannels(slack: Slack, token: String) {
        withContext(Dispatchers.IO) {
            val conversationListBuilder = ConversationsListRequest.builder().build()
            slack.methods(token).conversationsList(conversationListBuilder).channels.forEach {
                arrayListChannels.add(it.name)
                // Log.d("channel_name", it.name)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private suspend fun slackTask(
        activity: Activity,
        context: Context,
        slack: Slack,
        token: String,
        filePathMedia: File?,
        messagePath: String?
    ) {
        try {
            withContext(Dispatchers.IO) {
                if(messagePath != null){
                    slack.methods(token).chatPostMessage {
                        it.channel(hashMapUser.get(arrayListUsersName[spinnerPosition]).toString())
                        it.text(messageUser)
                        it.asUser(true)
                    }
                }

                if (filePathMedia != null && messagePath != null) {
                    slack.methods(token).filesUpload {
                        val list = ArrayList<String>()
                        list.add(hashMapUser.get(arrayListUsersName[spinnerPosition]).toString())
                        it.file(filePathMedia)
                        it.filename(filePathMedia.name)
                        it.channels(list)
                    }
                    activity.runOnUiThread {
                        LoggerBirdService.loggerBirdService.buttonSlackCancel.performClick()
                    }
                    LoggerBirdService.loggerBirdService.finishShareLayout("slack")
                }
            }
        } catch (e: Exception) {
            jiraExceptionHandler(e = e, filePathName = filePathMedia)
        }
    }

    internal fun checkMessageEmpty(activity: Activity, context: Context): Boolean {
        return if (message.isNotEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                Toast.makeText(
                    context,
                    R.string.slack_message_empty,
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
    }

    internal fun checkMessageEmptyUser(activity: Activity, context: Context): Boolean{
        return if (messageUser.isNotEmpty()) {
            true
        } else {
            activity.runOnUiThread {
                Toast.makeText(
                    context,
                    R.string.slack_message_empty,
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
    }

    internal fun gatherSlackChannelSpinnerDetails(
        spinnerChannel: Spinner
        //spinnerUser: Spinner
    ) {
        channel = spinnerChannel.selectedItem.toString()
        //user = spinnerUser.selectedItem.toString()
    }

    internal fun gatherSlackUserSpinnerDetails(
        spinnerUser : Spinner
    ){
        spinnerPosition = spinnerUser.selectedItemPosition
        user = spinnerUser.selectedItem.toString()
    }

    internal fun gatherSlackEditTextDetails(
        editTextMessage: EditText
    ) {
        message = editTextMessage.text.toString()
    }

    internal fun gatherSlackUserEditTextDetails(
        editTextMessage: EditText
    ){
        messageUser = editTextMessage.text.toString()
    }

    internal fun gatherSlackRecyclerViewDetails(arrayListRecyclerViewItems: ArrayList<RecyclerViewModel>) {
        this.arrayListRecyclerViewItems = arrayListRecyclerViewItems
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun jiraExceptionHandler(
        e: Exception? = null,
        filePathName: File? = null,
        throwable: Throwable? = null
    ) {
        filePathName?.delete()
        LoggerBirdService.loggerBirdService.finishShareLayout("slack_error")
        e?.printStackTrace()
        LoggerBird.callEnqueue()
        LoggerBird.callExceptionDetails(
            exception = e,
            tag = Constants.slackTag,
            throwable = throwable
        )
    }
}