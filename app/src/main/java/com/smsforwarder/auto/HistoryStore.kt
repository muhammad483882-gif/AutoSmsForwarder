package com.smsforwarder.auto

import android.content.Context
import org.json.JSONArray

/** ذخیره و بازیابی تاریخچه ارسال (حداکثر ۳۰۰ رکورد آخر) */
object HistoryStore {

    private const val FILE = "sms_forwarder_prefs"
    private const val KEY_HISTORY = "history"
    private const val MAX_ENTRIES = 300

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    private fun load(context: Context): MutableList<ForwardRecord> {
        val json = prefs(context).getString(KEY_HISTORY, null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<ForwardRecord>()
            for (i in 0 until arr.length()) {
                list.add(ForwardRecord.fromJson(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun save(context: Context, records: List<ForwardRecord>) {
        val arr = JSONArray()
        records.forEach { arr.put(it.toJson()) }
        prefs(context).edit().putString(KEY_HISTORY, arr.toString()).apply()
    }

    /** رکورد جدید را در ابتدای لیست اضافه می‌کند */
    fun add(context: Context, record: ForwardRecord) {
        val list = load(context)
        list.add(0, record)
        while (list.size > MAX_ENTRIES) {
            list.removeAt(list.size - 1)
        }
        save(context, list)
    }

    /** وضعیت یک رکورد را بر اساس id به‌روز می‌کند */
    fun update(context: Context, id: Long, status: RecordStatus, error: String?) {
        val list = load(context)
        val index = list.indexOfFirst { it.id == id }
        if (index >= 0) {
            val old = list[index]
            list[index] = old.copy(status = status, error = error ?: old.error)
            save(context, list)
        }
    }

    fun all(context: Context): List<ForwardRecord> = load(context)

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_HISTORY).apply()
    }
}
