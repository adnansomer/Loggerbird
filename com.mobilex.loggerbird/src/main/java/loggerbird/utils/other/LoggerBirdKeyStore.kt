package loggerbird.utils.other

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

internal class LoggerBirdKeyStore {

    companion object{
        @RequiresApi(Build.VERSION_CODES.M)
        private val keyGenerator : KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        @RequiresApi(Build.VERSION_CODES.M)
        private val keyGenParameterSpec = KeyGenParameterSpec.Builder("MyKeyAlias",KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        @RequiresApi(Build.VERSION_CODES.M)
        private var keyGen = keyGenerator.init(keyGenParameterSpec)
        @RequiresApi(Build.VERSION_CODES.M)
        private var keyGenerate = keyGenerator.generateKey()
    }

    private fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKeyEntry = keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
        return  secretKeyEntry.secretKey
    }

    internal fun encryptData(data: String) : Pair<ByteArray,ByteArray> {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        var temp = data
        while (temp.toByteArray().size % 16 != 0)
            temp +="\u0020"
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val ivBytes = cipher.iv
        val encryptedBytes = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))

        return  Pair(ivBytes, encryptedBytes)

    }

    internal fun decryptData(ivBytes : ByteArray, data: ByteArray): String{
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val spec = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE,getKey(),spec)
        return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
    }
}