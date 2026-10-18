package com.smsforwarder.auto

import java.util.Locale

/** ابزارهای عمومی: نرمال‌سازی شماره تلفن و تطبیق قوانین */
object Utils {

    /** تبدیل ارقام فارسی/عربی به انگلیسی */
    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder(input.length)
        input.forEach { c ->
            val code = c.code
            when (code) {
                in 0x06F0..0x06F9 -> sb.append(code - 0x06F0) // ۰..۹
                in 0x0660..0x0669 -> sb.append(code - 0x0660) // ٠..٩
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * نرمال‌سازی شماره برای مقایسه:
     * ‎+98912...‎ و ‎0098912...‎ و ‎98912...‎ و ‎912...‎ همگی به ‎0912...‎ تبدیل می‌شوند.
     * اگر ورودی شماره نباشد (مثل فرستنده‌های حروفی مثل Bank) بدون تغییر و با حروف بزرگ برمی‌گردد.
     */
    fun normalizePhone(raw: String): String {
        val s = toEnglishDigits(raw).trim().replace(Regex("[\\s()\\-.]"), "")
        val hasPlus = s.startsWith("+")
        val digits = s.filter { it.isDigit() }
        if (digits.isEmpty()) return s.trim().uppercase(Locale.ROOT)

        var d = digits
        if (d.startsWith("0098")) {
            d = "0" + d.substring(4)
        } else if (d.startsWith("98") && d.length >= 12) {
            d = "0" + d.substring(2)
        }
        if (d.length == 10 && d.startsWith("9")) {
            d = "0$d"
        }
        return d
    }

    /** آیا پیامک دریافتی با قانون مطابقت دارد؟ */
    fun matches(rule: Rule, from: String, body: String): Boolean {
        val senderMatch = rule.sender.isNotBlank() &&
                normalizePhone(rule.sender) == normalizePhone(from)
        val keywordMatch = rule.keyword.isNotBlank() &&
                body.contains(rule.keyword.trim(), ignoreCase = true)

        return when (rule.mode) {
            MatchMode.SENDER -> senderMatch
            MatchMode.KEYWORD -> keywordMatch
            MatchMode.BOTH -> senderMatch && keywordMatch
        }
    }
}
