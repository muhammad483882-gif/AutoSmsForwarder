package com.smsforwarder.auto

import org.json.JSONObject

/** وضعیت هر رکورد تاریخچه */
enum class RecordStatus {
    /** در حال ارسال */
    PENDING,

    /** ارسال شد (تحویل به اپراتور) */
    SENT,

    /** تحویل به گیرنده */
    DELIVERED,

    /** ارسال ناموفق */
    FAILED
}

/**
 * یک رکورد در تاریخچه ارسال.
 * id به صورت timestamp ثبت می‌شود تا برای به‌روزرسانی وضعیت استفاده شود.
 */
data class ForwardRecord(
    val id: Long,
    val from: String,
    val to: String,
    val text: String,
    val time: Long,
    var status: RecordStatus,
    var error: String?
) {

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("from", from)
        put("to", to)
        put("text", text)
        put("time", time)
        put("status", status.name)
        if (error != null) put("error", error)
    }

    companion object {
        fun fromJson(o: JSONObject): ForwardRecord = ForwardRecord(
            id = o.getLong("id"),
            from = o.optString("from", ""),
            to = o.optString("to", ""),
            text = o.optString("text", ""),
            time = o.optLong("time", 0L),
            status = runCatching { RecordStatus.valueOf(o.getString("status")) }.getOrDefault(RecordStatus.PENDING),
            error = if (o.has("error")) o.optString("error", null) else null
        )
    }
}
