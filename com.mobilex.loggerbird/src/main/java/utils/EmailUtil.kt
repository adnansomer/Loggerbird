package utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import constants.Constants
import loggerbird.LoggerBird
import authentication.SMTPAuthenticator
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult
import com.mobilex.loggerbird.R
import exception.LoggerBirdException
import kotlinx.coroutines.*
import java.io.File
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


//EmailUtil class is used for sending desired logfile as email.
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
        internal suspend fun sendEmail(
            file: File? = null,
            context: Context,
            progressBar: ProgressBar
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
                            subject = "log_details",
                            file = file,
                            context = context
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
                    progressBar.visibility = View.GONE
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
            context: Context
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
                            context = context
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


        internal suspend fun sendFeedbackEmail(context: Context, message: String) {
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
                            context = context
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

        //dummy method probably deleted in the future
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
        private suspend fun initializeEmail(context: Context, subject: String? = null) {
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
                mimeMessage.setFrom(InternetAddress("appcaesars@gmail.com"))
                mimeMessage.addRecipients(Message.RecipientType.TO, "appcaesars@gmail.com")
                mimeMessage.subject = subject
                mimeBodyPart = MimeBodyPart()
                transport.connect()
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
        private suspend fun sendSingleEmail(
            context: Context,
            subject: String,
            message: String? = null,
            file: File? = null
        ) {
            try {
                initializeEmail(subject = subject, context = context)
                if (file != null) {
                    dataSource = FileDataSource(file.path)
                    mimeBodyPart.dataHandler = DataHandler(
                        dataSource
                    )
                    mimeBodyPart.fileName = file.name
                    multiPart.addBodyPart(
                        mimeBodyPart
                    )
                    mimeMessage.setContent(
                        multiPart
                    )
                } else {
                    mimeMessage.setContent(message, "text/plain")
                }
                if (transport.isConnected) {
                    transport.sendMessage(
                        mimeMessage,
                        mimeMessage.getRecipients(Message.RecipientType.TO)
                    )
                    var toastMessage: String? = null
                    when (subject) {
                        "feed_back_details" -> toastMessage =
                            context.resources.getString(R.string.feed_back_email_success)
                        "unhandled_log_details" -> toastMessage =
                            context.resources.getString(R.string.unhandled_exception_success)
                        "log_details" -> toastMessage =
                            context.resources.getString(R.string.log_details_success)
                    }
                    if (toastMessage != null) {
                        if (message != null) {
                            val emailUtil = EmailUtil()
                            emailUtil.smartReplyFeedback(
                                context = context,
                                message = message,
                                toastMessage = toastMessage
                            )
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    R.string.email_send_failure,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            R.string.email_send_failure,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                transport.close()
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
    }

    private fun smartReplyFeedback(context: Context, message: String, toastMessage: String) {
        try {
            coroutineCallSmartReply.async {
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
                        val maxConfidenceMessage: String = it.suggestions[0].text
                        (context as Activity).runOnUiThread {
                            Toast.makeText(
                                context,
                                maxConfidenceMessage,
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
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.smartReplyFeedbackTag)
        }
    }
}