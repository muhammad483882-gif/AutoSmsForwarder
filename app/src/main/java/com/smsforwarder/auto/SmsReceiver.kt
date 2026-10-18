package com.smsforwarder.auto

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat

/**
 * گیرنده پیامک‌های ورودی.
 * با رسیدن هر پیامک، قوانین فعال بررسی می‌شوند و پیامک‌های منطبق فوروارد می‌شوند.
 * این کامپوننت در Manifest ثبت شده و نیازی به باز بودن برنامه ندارد.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // اگر کلید اصلی خاموش باشد کاری نکن
        if (!Prefs.isMasterEnabled(context)) return

        // بررسی دسترسی دریافت پیامک
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (msgs.isEmpty()) return

        val sender = msgs[0].originatingAddress ?: return

        // پیامک‌های چندبخشی را به هم می‌چسبانیم
        val body = StringBuilder().apply {
            msgs.forEach { append(it.displayMessageBody ?: "") }
        }.toString()

        if (body.isBlank()) return

        val now = System.currentTimeMillis()
        val matched = RulesStore.load(context)
            .filter { it.enabled && Utils.matches(it, sender, body) }

        matched.forEachIndexed { index, rule ->
            val record = ForwardRecord(
                id = now + index,
                from = sender,
                to = rule.destination,
                text = body,
                time = now,
                status = RecordStatus.PENDING,
                error = null
            )
            HistoryStore.add(context, record)
            Forwarder.send(context, record)
        }
    }
}
