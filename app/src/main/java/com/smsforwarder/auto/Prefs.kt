package com.smsforwarder.auto

import android.content.Context

/** تنظیمات کلی برنامه (کلید روشن/خاموش اصلی و ...) */
object Prefs {

    private const val FILE = "sms_forwarder_prefs"
    private const val KEY_MASTER = "master_enabled"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isMasterEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MASTER, true)

    fun setMasterEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_MASTER, value).apply()
    }
}
