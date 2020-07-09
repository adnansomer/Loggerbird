package utils.email

import adapter.recyclerView.email.RecyclerViewEmailAttachmentAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import constants.Constants
import loggerbird.LoggerBird
import authentication.SMTPAuthenticator
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.mobilex.loggerbird.R
import exception.LoggerBirdException
import kotlinx.coroutines.*
import services.LoggerBirdFutureTaskService
import services.LoggerBirdService
import utils.other.DefaultConnectionQueueUtil
import utils.other.DefaultToast
import utils.other.InternetConnectionUtil
import java.io.File
import java.lang.Runnable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Transport
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import kotlin.collections.ArrayList

/**
 * EmailUtil class is used for sending desired logfile as email.
 */
internal class EmailUtil {
    private var conversation = ArrayList<FirebaseTextMessage>()
    private var coroutineCallSmartReply: CoroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        //Static global variables.
        private lateinit var properties: Properties
        private lateinit var mimeBodyPart: MimeBodyPart
        private lateinit var mimeMessage: MimeMessage
        private lateinit var multiPart: Multipart
        private lateinit var authenticator: SMTPAuthenticator
        private lateinit var mailSession: javax.mail.Session
        private lateinit var transport: Transport
        private lateinit var dataSource: FileDataSource
        private val defaultConnectionQueueUtil =
            DefaultConnectionQueueUtil()
        private val defaultToast = DefaultToast()
        private lateinit var runnableConnectionTimeOut: Runnable


        /**
         * This Method Takes Log File And Send As Email.
         * Parameters:
         * @param file parameter used for getting file details for sending as email.
         * @param context parameter used for getting context of the current activity or fragment.
         * @param progressBar parameter used for getting the progressbar reference provided by user or getting default progressbar reference.
         * Variables:
         * @var internetConnectionUtil is used for instantiate the InternetConnectionUtil class and used it's network and internet check operations.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws LoggerBirdException if internet or network check gives exceptions.
         */
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        internal suspend fun sendEmail(
            file: File? = null,
            context: Context,
            activity: Activity? = null,
            progressBar: ProgressBar,
            to: String,
            subject: String? = null,
            message: String? = null
        ) {
            try {
                val internetConnectionUtil = InternetConnectionUtil()
                if (internetConnectionUtil.checkNetworkConnection(
                        context = context
                    )
                ) {
                    if (internetConnectionUtil.makeHttpRequest() == 200) {
                        Log.d(
                            "email_time",
                            systemTime()
                        )
                        runnableConnectionTimeOut = Runnable {
                            connectionTimeoutAction(
                                activity = activity
                            )
                        }
                        defaultConnectionQueueUtil.checkQueueTimeMainThreadAction(
                            activity = activity,
                            runnable = runnableConnectionTimeOut,
                            delay = 20000
                        )
                        sendSingleEmail(
                            file = file,
                            context = context,
                            activity = activity,
                            to = to,
                            subject = subject,
                            message = message,
                            controlServiceTask = false,
                            progressBar = progressBar
                        )
                        Log.d(
                            "email_time",
                            systemTime()
                        )
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                        }
                        LoggerBird.callEnqueue()
                    } else {
                        withContext(Dispatchers.Main) {
                            if (activity != null) {
                                defaultToast.attachToast(
                                    activity = activity,
                                    toastMessage = activity.resources.getString(R.string.internet_connection_check_failure)
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    R.string.internet_connection_check_failure,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        throw LoggerBirdException(
                            Constants.internetErrorMessage
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        if (activity != null) {
                            defaultToast.attachToast(
                                activity = activity,
                                toastMessage = activity.resources.getString(R.string.network_check_failure)
                            )
                        } else {
                            Toast.makeText(
                                context,
                                R.string.network_check_failure,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                    throw LoggerBirdException(
                        Constants.networkErrorMessage
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    defaultConnectionQueueUtil.cancelTimer()
                    progressBar.visibility = View.GONE
                    if (activity != null) {
                        activity.runOnUiThread {
                            defaultToast.attachToast(
                                activity = activity,
                                toastMessage = activity.resources.getString(R.string.email_send_failure)
                            )
                        }

                    } else {
                        Toast.makeText(
                            context,
                            R.string.email_send_failure,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
//                LoggerBirdService.loggerBirdService.finishShareLayout("single_email_error")
                LoggerBirdService.loggerBirdService.returnActivity().runOnUiThread {
                    LoggerBirdService.loggerBirdService.removeEmailLayout()
                    LoggerBirdService.loggerBirdService.detachProgressBar()
                    LoggerBirdService.resetEnqueueEmail()
                }
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
            }
        }

        /**
         * This Method Takes Log File With Unhandled Exception And Send As Email.
         * Parameters:
         * @param file parameter used for getting file details for sending as email.
         * @param context parameter used for getting context of the current activity or fragment.
         * Variables:
         * @var internetConnectionUtil is used for instantiate the InternetConnectionUtil class and used it's network and internet check operations.
         * @var LoggerBird.uncaughtExceptionHandlerController is used for determining that there is an unhandled exception for callExceptionDetails method.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         * @throws LoggerBirdException if internet or network check gives exceptions.
         */
        internal suspend fun sendUnhandledException(
            file: File,
            context: Context,
            to: String
        ) {
            try {
                val internetConnectionUtil = InternetConnectionUtil()
                if (internetConnectionUtil.checkNetworkConnection(
                        context = context
                    )
                ) {
                    if (internetConnectionUtil.makeHttpRequest() == 200) {
                        Log.d(
                            "email_time",
                            systemTime()
                        )
                        sendSingleEmail(
                            subject = "unhandled_log_details",
                            file = file,
                            context = context,
                            to = to,
                            controlServiceTask = false
                        )
                        Log.d(
                            "email_time",
                            systemTime()
                        )


                    } else {
                        withContext(Dispatchers.Main) {
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
                } else {
                    withContext(Dispatchers.Main) {
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        R.string.email_send_failure,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
            }
        }


        /**
         * This Method Takes Log File With Unhandled Exception And Send As Email.
         * Parameters:
         * @param message parameter used for getting text message for sending as email.
         * @param context parameter used for getting context of the current activity or fragment.
         * @param to parameter used for getting e-mail address to be send as an email.
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        internal suspend fun sendFeedbackEmail(context: Context, message: String, to: String) {
            try {
                val internetConnectionUtil = InternetConnectionUtil()
                if (internetConnectionUtil.checkNetworkConnection(
                        context = context
                    )
                ) {
                    if (internetConnectionUtil.makeHttpRequest() == 200) {
                        Log.d(
                            "email_time",
                            systemTime()
                        )
                        sendSingleEmail(
                            message = message,
                            subject = "feed_back_details",
                            context = context,
                            to = to,
                            controlServiceTask = false
                        )
                        Log.d(
                            "email_time",
                            systemTime()
                        )


                    } else {
                        withContext(Dispatchers.Main) {
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
                } else {
                    withContext(Dispatchers.Main) {
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        R.string.email_send_failure,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
            }

        }

        /**
         * This method is used for returning system time.
         */
        private fun systemTime(): String {
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
            val currentTime: LocalDateTime = LocalDateTime.now()
            val formattedTime: String = currentTime.format(formatter)
            return formattedTime
        }

        /**
         * This Method initialize email system.
         * Parameters:
         * Variables:
         * @var properties is used for getting some details for email system.
         * @var authenticator is used for getting details of mail information that will send mail.
         * @var mailSession is used for creating mail instance.
         * @var transport is used for transporting the instance and information that created by multipart.
         * @var multipart is used for creating contents that used in mail.
         * @var mimeMessage is used for mime the content that given.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        private  fun initializeEmail(context: Context, to: String, subject: String? = null) {
            try {
                properties = Properties()
                properties["mail.transport.protocol"] = "smtp"
                properties["mail.smtp.host"] = Constants.SMTP_HOST_NAME
                properties["mail.smtp.auth"] = "true"
                properties["mail.smtp.port"] = "587"
                properties["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                properties["mail.smtp.starttls.enable"] = "true"
                authenticator =
                    SMTPAuthenticator()
                mailSession =
                    javax.mail.Session.getDefaultInstance(
                        properties,
                        authenticator
                    )
                mailSession.debug = true
                transport = mailSession.transport
                mimeMessage = MimeMessage(
                    mailSession
                )
                multiPart = MimeMultipart()
                mimeMessage.setFrom(InternetAddress("appcaesar@gmail.com"))
                mimeMessage.addRecipients(Message.RecipientType.TO, to)
                mimeMessage.subject = subject
                transport.connect()
            } catch (e: Exception) {
                val coroutineCallMain = CoroutineScope(Dispatchers.Main)
                coroutineCallMain.launch {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            R.string.email_send_failure,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
            }
        }

        /**
         * This Method used for sending single email.
         * Parameters:
         * @param file parameter used for getting file details for sending as email.
         * Variables:
         * @var dataSource is used for getting filepath.
         * @var mimeBodyPart takes file content and name.
         * @var mailSession is used for creating mail instance.
         * @var multipart is used for getting contents that used in file.
         * @var mimeMessage is used for mime the content that given.
         * @var transport is used for sending email instance.
         * Exceptions:
         * @throws exception if error occurs then com.mobilex.loggerbird.exception message will be put in the queue with callExceptionDetails , which it's details gathered by takeExceptionDetails method and saves exceptions instance to the txt file with saveExceptionDetails method.
         */
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        internal  fun sendSingleEmail(
            context: Context,
            activity: Activity? = null,
            to: String,
            subject: String? = null,
            message: String? = null,
            file: File? = null,
            arrayListFilePaths: ArrayList<File>? = null,
            controlServiceTask: Boolean,
            progressBar: ProgressBar? = null
        ) {
            try {
                val coroutineCallMain = CoroutineScope(Dispatchers.Main)
                if (progressBar != null) {
                    coroutineCallMain.launch {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.VISIBLE
                        }
                    }
                }
                initializeEmail(
                    context = context,
                    to = to,
                    subject = subject
                )
                mimeBodyPart = MimeBodyPart()
                if (message != null) {
                    mimeBodyPart.setContent(message, "text/plain")
                }
                multiPart.addBodyPart(
                    mimeBodyPart
                )
                if (RecyclerViewEmailAttachmentAdapter.ViewHolder.controlArrayListFilePaths()) {
                    if (RecyclerViewEmailAttachmentAdapter.ViewHolder.arrayListFilePaths.isNotEmpty()) {
                        RecyclerViewEmailAttachmentAdapter.ViewHolder.arrayListFilePaths.forEach {
                            createFileMultiPart(
                                file = it.file
                            )
                        }
                    }
                }
                if (arrayListFilePaths != null) {
                    if (arrayListFilePaths.isNotEmpty()) {
                        arrayListFilePaths.forEach {
                            createFileMultiPart(
                                file = it
                            )
                        }
                    }
                }
                if (file != null) {
                    createFileMultiPart(file = file)
                }
                mimeMessage.setContent(
                    multiPart
                )
                if (transport.isConnected) {
                    transport.sendMessage(
                        mimeMessage,
                        mimeMessage.getRecipients(Message.RecipientType.TO)
                    )
                    if (!controlServiceTask) {
                        var toastMessage: String? = null
                        when (subject) {
                            "feed_back_details" -> toastMessage =
                                context.resources.getString(R.string.feed_back_email_success)
                            "unhandled_log_details" -> toastMessage =
                                context.resources.getString(R.string.unhandled_exception_success)
                            else ->
                                coroutineCallMain.launch {
                                    withContext(Dispatchers.Main) {
                                        defaultConnectionQueueUtil.cancelTimer()
                                        LoggerBirdService.callEnqueueEmail()
                                    }
                                }

                        }
                        if (toastMessage != null) {
                            if (message != null) {
                                val emailUtil = EmailUtil()
                                emailUtil.smartReplyFeedback(
                                    context = context,
                                    message = message,
                                    toastMessage = toastMessage
                                )
                            }
//                        else {
//                            withContext(Dispatchers.Main) {
//                                Toast.makeText(
//                                    context,
//                                    R.string.email_send_failure,
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }

                        }
                        if (progressBar != null) {
                            coroutineCallMain.launch {
                                withContext(Dispatchers.Main) {
                                    progressBar.visibility = View.GONE
                                }
                            }

                        }
                    }else{
                        LoggerBirdFutureTaskService.callEnqueueEmail()
                        if(LoggerBirdFutureTaskService.runnableListEmail.size == 0){
                            arrayListFilePaths?.forEach {
                                if(it.exists()){
                                    it.delete()
                                }
                            }
                            val sharedPref =
                                PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                            val editor: SharedPreferences.Editor = sharedPref.edit()
                            editor.remove("future_task_email_message")
                            editor.remove("file_future_list")
                            editor.remove("user_future_list")
                            editor.remove("future_task_email_to")
                            editor.remove("future_task_time")
                            editor.remove("future_task_email_subject")
                            editor.remove("future_task_check")
                            editor.commit()
                            context.stopService(Intent(context, LoggerBirdFutureTaskService::class.java))
                        }
                    }
                    LoggerBirdService.loggerBirdService.finishShareLayout("single_email")
                    transport.close()
                }
            } catch (e: Exception) {
                if (activity != null) {
                    activity.runOnUiThread {
                        defaultConnectionQueueUtil.cancelTimer()
                        LoggerBirdService.loggerBirdService.finishShareLayout("single_email_error")
                    }
                } else {
                    val coroutineCallMain = CoroutineScope(Dispatchers.Main)
                    coroutineCallMain.launch {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                R.string.email_send_failure,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                e.printStackTrace()
                LoggerBirdService.resetEnqueueEmail()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
            }
        }

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        private fun connectionTimeoutAction(activity: Activity?) {
            activity?.runOnUiThread {
                defaultToast.attachToast(
                    activity = activity,
                    toastMessage = activity.resources.getString(R.string.email_connection_time_out)
                )
                LoggerBirdService.loggerBirdService.removeEmailLayout()
                LoggerBirdService.loggerBirdService.detachProgressBar()
                LoggerBirdService.resetEnqueueEmail()
            }
        }

        private fun createFileMultiPart(file: File) {
            mimeBodyPart = MimeBodyPart()
            dataSource = FileDataSource(file.path)
            mimeBodyPart.dataHandler = DataHandler(
                dataSource
            )
            mimeBodyPart.fileName = file.name
            multiPart.addBodyPart(
                mimeBodyPart
            )
        }

    }

    private fun smartReplyFeedback(context: Context, message: String, toastMessage: String) {
        try {
            coroutineCallSmartReply.async {
                val languageIdentifier =
                    FirebaseNaturalLanguage.getInstance().languageIdentification
                languageIdentifier.identifyLanguage(message).addOnSuccessListener {
                    when (it) {
                        "und" -> {
                            (context as Activity).runOnUiThread {
                                Toast.makeText(
                                    context,
                                    toastMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        "en" -> {
                            smartReplyMessage(
                                message = message,
                                context = context,
                                toastMessage = toastMessage
                            )
                        }
                        else -> {
                            translateMessage(
                                languageCode = it,
                                message = message,
                                context = context,
                                toastMessage = toastMessage
                            )
                        }
                    }

                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.smartReplyFeedbackTag)
        }
    }

    private fun smartReplyMessage(message: String, context: Context, toastMessage: String) {
        try {
            conversation.add(
                FirebaseTextMessage.createForRemoteUser(
                    message,
                    System.currentTimeMillis(),
                    Build.ID
                )
            )
            val smartReply = FirebaseNaturalLanguage.getInstance().smartReply
            smartReply.suggestReplies(conversation).addOnSuccessListener {
                if (it.status == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                    (context as Activity).runOnUiThread {
                        Toast.makeText(
                            context,
                            toastMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (it.status == SmartReplySuggestionResult.STATUS_SUCCESS) {
                    if (it.suggestions.isEmpty()) {
                        (context as Activity).runOnUiThread {
                            Toast.makeText(
                                context,
                                toastMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val maxConfidenceMessage: String =
                            it.suggestions[(0 until it.suggestions.size).random()].text
                        (context as Activity).runOnUiThread {
                            Toast.makeText(
                                context,
                                maxConfidenceMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else if (it.status == SmartReplySuggestionResult.STATUS_NO_REPLY) {
                    (context as Activity).runOnUiThread {
                        Toast.makeText(
                            context,
                            toastMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                smartReply.close()
            }.addOnFailureListener {
                (context as Activity).runOnUiThread {
                    Toast.makeText(
                        context,
                        toastMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                smartReply.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.smartReplyFeedbackTag)
        }
    }

    private fun translateMessage(
        languageCode: String,
        message: String,
        context: Context,
        toastMessage: String
    ) {
        try {
            val options: FirebaseTranslatorOptions =
                FirebaseTranslatorOptions.Builder().setSourceLanguage(
                    FirebaseTranslateLanguage.languageForLanguageCode(languageCode)!!
                ).setTargetLanguage(FirebaseTranslateLanguage.EN)
                    .build()
            val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
            translator.downloadModelIfNeeded().addOnSuccessListener {
                translator.translate(message).addOnSuccessListener {
                    smartReplyMessage(
                        message = it,
                        context = context,
                        toastMessage = toastMessage
                    )
                    translator.close()
                }.addOnFailureListener {
                    translator.close()
                    LoggerBird.callEnqueue()
                    LoggerBird.callExceptionDetails(
                        exception = it,
                        tag = Constants.translateDownloaderTag
                    )

                }
            }.addOnFailureListener {
                translator.close()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(
                    exception = it,
                    tag = Constants.translateDownloaderTag
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.translateTag)
        }
    }
}