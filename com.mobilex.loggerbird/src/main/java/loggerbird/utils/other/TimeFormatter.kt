package loggerbird.utils.other

import java.util.*
import java.util.concurrent.TimeUnit
/**
 * This class is used for formatting time value.
 */
internal class TimeFormatter {
    /**
     * This method is used for formatting certain time value in day/hour/second/millisecond format.
     * @param remainingSeconds is for getting reference of time value.
     * @return String value.
     */
    internal fun timeString(remainingSeconds: Long): String {
        return String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toDays(remainingSeconds),
            TimeUnit.MILLISECONDS.toHours(remainingSeconds) - TimeUnit.DAYS.toHours(
                TimeUnit.MILLISECONDS.toDays(
                    remainingSeconds
                )
            ),
            TimeUnit.MILLISECONDS.toMinutes(remainingSeconds) - TimeUnit.HOURS.toMinutes(
                TimeUnit.MILLISECONDS.toHours(
                    remainingSeconds
                )
            ),
            TimeUnit.MILLISECONDS.toSeconds(remainingSeconds) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(
                    remainingSeconds
                )
            )
        )
    }
}