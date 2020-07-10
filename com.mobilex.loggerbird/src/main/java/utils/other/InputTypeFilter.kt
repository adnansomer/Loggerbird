package utils.other

import android.text.InputFilter
import android.text.Spanned

/**
 * This class is used for filtering to ensure that users obey a certain pattern.
 **/
internal class InputTypeFilter : InputFilter {
    private var mIntMin: Int
    private var mIntMax: Int

    constructor(minValue: Int, maxValue: Int) {
        mIntMin = minValue
        mIntMax = maxValue
    }

    constructor(minValue: String, maxValue: String) {
        mIntMin = minValue.toInt()
        mIntMax = maxValue.toInt()
    }

    /**
     * This method is used for filtering the number text.
     * @param source gets char sequence of text that entered
     * @param start gets start index of text
     * @param end gets end index of text
     * @param dest gets range of text
     * @param dstart gets start range of text
     * @param dend gets end range of text
     * @throws NumberFormatException in order to catch exception.
     */
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toInt()
            if (isInRange(mIntMin, mIntMax, input)) return null
        } catch (nfe: NumberFormatException) {
            nfe.printStackTrace()
        }
        return ""
    }

    /**
     * This method is used for filtering the number text.
     * @param a example variable to compare char seqeunce length
     * @param b example variable to compare char seqeunce length
     * @param c example variable to compare char seqeunce length
     */
    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}