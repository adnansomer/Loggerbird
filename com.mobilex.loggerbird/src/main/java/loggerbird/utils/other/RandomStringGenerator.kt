package loggerbird.utils.other

import java.util.*

internal class RandomStringGenerator {
   internal fun randomStringGenerator(): String {
        val leftLimit = 65 // letter 'a' from ascii table
        val rightLimit = 90 // letter 'z' from ascii table
        val targetStringLength = 10
        val random = Random()
        val buffer = StringBuilder(targetStringLength)
        for (i in 0 until targetStringLength) {
            val randomLimitedInt =
                leftLimit + (random.nextFloat() * (rightLimit - leftLimit + 1))
            buffer.append(randomLimitedInt.toChar())
        }
        return buffer.toString()
    }
}