package authentication

import javax.mail.PasswordAuthentication

/**
 * SMTPAuthenticator class used for giving authentication details needed for sending email.
 */
internal class SMTPAuthenticator : javax.mail.Authenticator() {
    private val SMTP_AUTH_USER: String = "appcaesars@gmail.com"
    private val SMTP_AUTH_PWD: String = "odanobunaga1"
    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(SMTP_AUTH_USER, SMTP_AUTH_PWD)
    }
}