package utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
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
import okhttp3.*
import java.io.File
import java.io.IOException

class SlackAuthentication {
    private var coroutineCallSlack: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var coroutineCallOkHttpSlack: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val internetConnectionUtil = InternetConnectionUtil()
    internal fun callSlack(activity: Activity, context: Context, filePathMedia: File? = null) {
        coroutineCallOkHttpSlack.async {
            if (internetConnectionUtil.checkNetworkConnection(context = context)) {
                okHttpSlackAuthentication(
                    activity = activity,
                    context = context,
                    filePathMediaName = filePathMedia
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

            try {

            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.slackTag)
            }
        }
    }

    private fun okHttpSlackAuthentication(
        context: Context,
        activity: Activity,
        filePathMediaName: File?
    ) {
        val client = OkHttpClient()
        val request: Request =
            Request.Builder()
                .url("https://app.slack.com/client/T011GLPDZQA/C011GLPEP4J")
                .build()
        client.newCall(request).enqueue(object : Callback {
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.slackTag)
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
                                    filePathMedia = filePathMediaName
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
                    e.printStackTrace()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(exception = e, tag = Constants.slackTag)
                }
            }
        })
    }

    private suspend fun slackAuthentication(
        activity: Activity,
        context: Context,
        filePathMedia: File?
    ) {
        val slack = Slack.getInstance()
        val token = "xoxb-1050703475826-1145080262722-ky9ACRsZfZcacuifbgXiZmEZ"
        slackTask(
            activity = activity,
            context = context,
            slack = slack,
            token = token,
            filePathMedia = filePathMedia
        )
    }

    private suspend fun slackTask(
        activity: Activity,
        context: Context,
        slack: Slack,
        token: String,
        filePathMedia: File?
    ) {
        withContext(Dispatchers.IO) {
            //            val adminUsergroupsListChannelsRequest =
//                AdminUsergroupsListChannelsRequest.builder().token(token)
//            val response = slack.methods()
//                .adminUsergroupsListChannels(adminUsergroupsListChannelsRequest.build())
//            response.channels.forEach {
//                Log.d("channel_name", it.name)
//            }

            val conversationListBuilder = ConversationsListRequest.builder().build()
            val conversationListResponse =
                slack.methods(token).conversationsList(conversationListBuilder).channels.forEach {
                    Log.d("channel_name", it.name)
                }


            val userListBuilder = UsersListRequest.builder().build()
            val userListResponse = slack.methods(token).usersList(userListBuilder).members.forEach {
                Log.d("user-name",it.name)
            }

//            val response = slack.methods(token).chatPostMessage {
//                it
//                    .channel("#ozv-team")
//                    .username("LoggerBird")
//                    .text("Deneme")
//            }

//            if (filePathMedia != null) {
//                val response2 = slack.methods(token).filesUpload {
//                    val list = ArrayList<String>()
//                    list.add("#ozv-team")
//                    it.file(filePathMedia)
//                    it.filename(filePathMedia.name)
//                    it.channels(list)
//                }
//                activity.runOnUiThread {
//                    Toast.makeText(context, response2.toString(), Toast.LENGTH_SHORT).show()
//                }
//            }
        }
    }
}