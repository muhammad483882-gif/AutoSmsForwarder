package com.smsforwarder.auto

import org.json.JSONObject

/** حالت تطبیق یک قانون فوروارد */
enum class MatchMode {
    /** فقط شماره فرستنده */
    SENDER,

    /** فقط کلمه کلیدی در متن */
    KEYWORD,

    /** شماره فرستنده و کلمه کلیدی همزمان */
    BOTH
}

/**
 * یک قانون فوروارد:
 * هر پیامکی که مطابق شرط قانون باشد، به [destination] فوروارد می‌شود.
 */
data class Rule(
    val id: Long,
    val mode: MatchMode,
    val sender: String,
    val keyword: String,
    val destination: String,
    var enabled: Boolean
) {

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("mode", mode.name)
        put("sender", sender)
        put("keyword", keyword)
        put("destination", destination)
        put("enabled", enabled)
    }

    companion object {
        fun fromJson(o: JSONObject): Rule = Rule(
            id = o.getLong("id"),
            mode = runCatching { MatchMode.valueOf(o.getString("mode")) }.getOrDefault(MatchMode.SENDER),
            sender = o.optString("sender", ""),
            keyword = o.optString("keyword", ""),
            destination = o.optString("destination", ""),
            enabled = o.optBoolean("enabled", true)
        )
    }
}
