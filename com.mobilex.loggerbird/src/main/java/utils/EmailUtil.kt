package utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import constants.Constants
import loggerbird.LoggerBird
import authentication.SMTPAuthenticator
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
                            file = file
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
                        throw LoggerBirdException(
                            Constants.internetErrorMessage
                        )
                    }
                } else {
                    throw LoggerBirdException(
                        Constants.networkErrorMessage
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
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
        internal fun sendUnhandledException(
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
                            file = file
                        )
                        Log.d(
                            "email_time",
                            systemTime()
                        )


                    } else {
                        throw LoggerBirdException(
                            Constants.internetErrorMessage
                        )
                    }
                } else {
                    throw LoggerBirdException(
                        Constants.networkErrorMessage
                    )
                }
            } catch (e: Exception) {
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
        private fun initializeEmail() {
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
                mimeMessage.subject = "log_details"
                mimeBodyPart = MimeBodyPart()
                transport.connect()
            } catch (e: Exception) {
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
        private fun sendSingleEmail(file: File? = null) {
            try {
                initializeEmail()
                dataSource = FileDataSource(file?.path)
                mimeBodyPart.dataHandler = DataHandler(
                    dataSource
                )
                mimeBodyPart.fileName = file?.name
                multiPart.addBodyPart(
                    mimeBodyPart
                )
                mimeMessage.setContent(
                    multiPart
                )
                transport.sendMessage(
                    mimeMessage,
                    mimeMessage.getRecipients(Message.RecipientType.TO)
                )
                transport.close()
            } catch (e: Exception) {
                e.printStackTrace()
                LoggerBird.callEnqueue()
                LoggerBird.callExceptionDetails(exception = e, tag = Constants.emailTag)
            }
        }
    }
}