package utils.other

import android.os.Build
import androidx.annotation.RequiresApi
import constants.Constants
import loggerbird.LoggerBird
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import java.util.Base64.getDecoder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


internal class LoggerBirdEncryption {
    private var secretKey: SecretKeySpec? = null
    private lateinit var key: ByteArray
    @RequiresApi(Build.VERSION_CODES.KITKAT)
   internal fun setKey(myKey: String) {
        val sha: MessageDigest
        try {
            key = myKey.toByteArray(StandardCharsets.UTF_8)
            sha = MessageDigest.getInstance("SHA-1")
            key = sha.digest(key)
            key = key.copyOf(16)
            secretKey = SecretKeySpec(key, "AES")
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.encryptionTag)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    internal fun encrypt(stringToEncrypt: String, secret: String): String? {
        try {
            setKey(secret)
            val cipher =
                Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder()
                    .encodeToString(cipher.doFinal(stringToEncrypt.toByteArray(StandardCharsets.UTF_8)))
            } else {
                android.util.Base64.encodeToString(
                    cipher.doFinal(
                        stringToEncrypt.toByteArray(
                            StandardCharsets.UTF_8
                        )
                    ), android.util.Base64.NO_WRAP
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.encryptionTag)
        }
        return null
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    internal fun decrypt(stringToDecrypt: String, secret: String): String? {
        var strToDecrypt = stringToDecrypt
        try {
            if (strToDecrypt.endsWith("\n")) {
                strToDecrypt = strToDecrypt.substring(0, strToDecrypt.length - 1)
            }
            setKey(secret)
            val cipher =
                Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String(cipher.doFinal(getDecoder().decode(strToDecrypt)))
            } else {
                String(
                    cipher.doFinal(
                        android.util.Base64.decode(
                            strToDecrypt,
                            android.util.Base64.NO_WRAP
                        )
                    )
                )
            }
        }catch (e: Exception) {
            e.printStackTrace()
            LoggerBird.callEnqueue()
            LoggerBird.callExceptionDetails(exception = e, tag = Constants.encryptionTag)
        }
        return null
    }

}