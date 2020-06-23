package utils

import android.text.InputFilter
import android.text.Spanned

/** Input type filter**/

class InputTypeFilter : InputFilter {
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

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}