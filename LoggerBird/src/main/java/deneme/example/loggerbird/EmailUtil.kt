package deneme.example.loggerbird

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ProgressBar
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
class EmailUtil {
    companion object {
        private lateinit var  properties: Properties
        private lateinit var mimeBodyPart: MimeBodyPart
        private lateinit var mimeMessage: MimeMessage
        private lateinit var multiPart: Multipart
        private lateinit var authenticator: SMTPAuthenticator
        private lateinit var mailSession: javax.mail.Session
        private lateinit var  transport:Transport
        private lateinit var dataSource: FileDataSource

        /**
         * This Method Takes desired log file and send as email.
         * Parameters:
         * @param file parameter used for getting file details for sending as email.
         * @param arrayListFile parameter used for holding list of temp files which is used for sending multiple emails for each temp files.
         * @param context parameter used for getting context of the current activity or fragment.
         * @param progressBar parameter used for getting the progressbar reference provided by user or getting default progressbar reference.
         * Variables:
         * @var internetConnectionUtil is used for instantiate the InternetConnectionUtil class.
         * @var coroutineCallEmail is used for get the method in coroutine scope(Dispatchers.IO) which leads method to be called random thread which is different from main thread as asynchronously.
         * Exceptions:
         * @throws exception if error occurs and saves the exception in logExceptionDetails.
         * @throws ExceptionCustom if internet or network check gives exceptions.
         */
        fun sendEmail(file: File? = null, arrayListFile: ArrayList<File>? = null,context:Context,progressBar: ProgressBar,coroutinecallEmail:CoroutineScope) {
            val internetConnectionUtil:InternetConnectionUtil=InternetConnectionUtil()
            try {
                if(!progressBar.isShown){
                    progressBar.visibility= View.VISIBLE
                }
                if(internetConnectionUtil.checkNetworkConnection(context = context)){
                    if(internetConnectionUtil.makeHttpRequest()==200){
                        Log.d("email_time", systemTime())
                        if (file != null) {
                            sendSingleEmail(file)
                        } else if (arrayListFile != null) {
                            for (arrayList in arrayListFile) {
                                sendMultipleEmail(arrayList)
                            }
                        }
                        if (file != null) {
                            file.delete()
                        } else if (arrayListFile != null) {
                            for (arrayList in arrayListFile) {
                                arrayList.delete()
                            }
                        }
                        Log.d("email_time", systemTime())
                        coroutinecallEmail.launch {
                            withContext(Dispatchers.Main) {
                                progressBar.visibility =View.GONE
                            }
                        }
                    }else{
                        coroutinecallEmail.launch {
                            withContext(Dispatchers.Main) {
                                progressBar.visibility =View.GONE
                            }
                        }
                        throw ExceptionCustom(Constants.internetErrorMessage)
                    }
                }else{
                    coroutinecallEmail.launch {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility =View.GONE
                        }
                    }
                    throw ExceptionCustom(Constants.networkErrorMessage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                LogDeneme.logExceptionDetails(e)
                coroutinecallEmail.launch {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility =View.GONE
                    }
                }
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
         * This Method intialize email system.
         * Variables:
         * @var properties is used for getting some details for email system.
         * @var authenticator is used for getting details of mail information that will send mail.
         * @var mailSession is used for creating mail instance.
         * @var transport is used for transporting the instance and information that created by multipart.
         * @var multipart is used for creating contents that used in mail.
         * @var mimeMessage is used for mime the content that given.
         * Exceptions:
         * @throws exception if error occurs and saves the exception in logExceptionDetails.
         */
        private fun initializeEmail(){
            try {
                properties = Properties()
                properties.put("mail.transport.protocol", "smtp")
                properties.put("mail.smtp.host", Constants.SMTP_HOST_NAME)
                properties.put("mail.smtp.auth", "true")
                properties.put("mail.smtp.port", "587")
                properties.put(
                    "mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory"
                )
                properties.put("mail.smtp.starttls.enable", "true")
                authenticator= SMTPAuthenticator()
                mailSession =
                    javax.mail.Session.getDefaultInstance(properties, authenticator)
                mailSession.debug = true
                transport = mailSession.transport
                mimeMessage = MimeMessage(mailSession)
                multiPart = MimeMultipart()
                mimeMessage.setFrom(InternetAddress("appcaesars@gmail.com"))
                mimeMessage.addRecipients(Message.RecipientType.TO, "appcaesars@gmail.com")
                mimeMessage.setSubject("log_details")
                mimeBodyPart= MimeBodyPart()
                transport.connect()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        /**
         * This Method used for sending single email which doesnt exceeds 2mb size.
         * Variables:
         * @var dataSource is used for getting filepath.
         * @var mimeBodyPart takes file content and name.
         * @var mailSession is used for creating mail instance.
         * @var multipart is used for getting contents that used in file.
         * @var mimeMessage is used for mime the content that given.
         * @var transport is used for sending email instance.
         * Exceptions:
         * @throws exception if error occurs and saves the exception in logExceptionDetails.
         */
        private fun sendSingleEmail(file:File? = null){
            initializeEmail()
            dataSource= FileDataSource(file?.path)
            mimeBodyPart.dataHandler = DataHandler(dataSource)
            mimeBodyPart.fileName = file?.name
            multiPart.addBodyPart(mimeBodyPart)
            mimeMessage.setContent(multiPart)
            transport.sendMessage(
                mimeMessage,
                mimeMessage.getRecipients(Message.RecipientType.TO)
            )
            transport.close()
        }
        /**
         * This Method used for sending multiple  email which  exceeds 2mb size.
         * Variables:
         * @var dataSource is used for getting filepath.
         * @var mimeBodyPart takes file content and name.
         * @var mailSession is used for creating mail instance.
         * @var multipart is used for getting contents that used in file.
         * @var mimeMessage is used for mime the content that given.
         * @var transport is used for sending email instance.
         * Exceptions:
         * @throws exception if error occurs and saves the exception in logExceptionDetails.
         */
        private fun sendMultipleEmail(file: File? = null){
            initializeEmail()
            dataSource= FileDataSource(file?.path)
            mimeBodyPart.dataHandler = DataHandler(dataSource)
            mimeBodyPart.fileName = file?.name
            multiPart.addBodyPart(mimeBodyPart)
            mimeMessage.setContent(multiPart)
            transport.sendMessage(
                mimeMessage,
                mimeMessage.getRecipients(Message.RecipientType.TO)
            )
            transport.close()
        }
    }
}