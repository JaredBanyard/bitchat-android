package com.bitchat.android.util

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan

class AnsiEscaper {

    companion object {
        private const val ESC = "\u001B"
        private const val CSI = "$ESC["

        // ANSI color codes
        private val ansiColors = mapOf(
            "30" to Color.BLACK,
            "31" to Color.RED,
            "32" to Color.GREEN,
            "33" to Color.YELLOW,
            "34" to Color.BLUE,
            "35" to Color.MAGENTA,
            "36" to Color.CYAN,
            "37" to Color.WHITE,
            "90" to Color.GRAY, // Bright Black
            "91" to Color.parseColor("#FF6666"), // Bright Red
            "92" to Color.parseColor("#66FF66"), // Bright Green
            "93" to Color.parseColor("#FFFF66"), // Bright Yellow
            "94" to Color.parseColor("#6666FF"), // Bright Blue
            "95" to Color.parseColor("#FF66FF"), // Bright Magenta
            "96" to Color.parseColor("#66FFFF"), // Bright Cyan
            "97" to Color.parseColor("#FFFFFF")  // Bright White
        )

        private val ansiBackgroundColors = mapOf(
            "40" to Color.BLACK,
            "41" to Color.RED,
            "42" to Color.GREEN,
            "43" to Color.YELLOW,
            "44" to Color.BLUE,
            "45" to Color.MAGENTA,
            "46" to Color.CYAN,
            "47" to Color.WHITE,
            "100" to Color.GRAY, // Bright Black
            "101" to Color.parseColor("#FF6666"), // Bright Red
            "102" to Color.parseColor("#66FF66"), // Bright Green
            "103" to Color.parseColor("#FFFF66"), // Bright Yellow
            "104" to Color.parseColor("#6666FF"), // Bright Blue
            "105" to Color.parseColor("#FF66FF"), // Bright Magenta
            "106" to Color.parseColor("#66FFFF"), // Bright Cyan
            "107" to Color.parseColor("#FFFFFF")  // Bright White
        )

        fun escape(text: String): SpannableStringBuilder {
            val spannable = SpannableStringBuilder()
            var i = 0
            var currentForegroundColor = Color.WHITE // Default IRC foreground
            var currentBackgroundColor = Color.BLACK // Default IRC background
            var isBold = false
            var isItalic = false // ANSI doesn't have a standard italic, but some terminals support it
            var isUnderline = false

            while (i < text.length) {
                if (text.startsWith(CSI, i)) {
                    val endMarker = text.indexOf('m', i)
                    if (endMarker != -1) {
                        val codes = text.substring(i + CSI.length, endMarker).split(';')
                        for (code in codes) {
                            when (code) {
                                "0" -> { // Reset all attributes
                                    currentForegroundColor = Color.WHITE
                                    currentBackgroundColor = Color.BLACK
                                    isBold = false
                                    isItalic = false
                                    isUnderline = false
                                }
                                "1" -> isBold = true
                                "3" -> isItalic = true // Often mapped to italic
                                "4" -> isUnderline = true
                                "22" -> isBold = false // Normal intensity (turn off bold)
                                "23" -> isItalic = false // Not italic
                                "24" -> isUnderline = false // Not underlined
                                else -> {
                                    ansiColors[code]?.let { currentForegroundColor = it }
                                    ansiBackgroundColors[code]?.let { currentBackgroundColor = it }
                                }
                            }
                        }
                        i = endMarker + 1
                        continue
                    }
                }

                val start = spannable.length
                spannable.append(text[i])
                val end = spannable.length

                spannable.setSpan(ForegroundColorSpan(currentForegroundColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                // Only apply background if it's not the default to avoid full background coloring
                if (currentBackgroundColor != Color.BLACK) {
                    spannable.setSpan(BackgroundColorSpan(currentBackgroundColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (isBold) spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (isItalic) spannable.setSpan(StyleSpan(android.graphics.Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (isUnderline) spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                i++
            }
            return spannable
        }
    }
}