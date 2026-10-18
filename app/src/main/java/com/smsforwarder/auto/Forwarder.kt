package com.smsforwarder.auto

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

/** ارسال پیامک فورواردشده به شماره مقصد (ارسال مستقیم از سیم‌کارت) */
object Forwarder {

    fun send(context: Context, record: ForwardRecord) {
        // بررسی دسترسی ارسال پیامک
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            HistoryStore.update(
                context, record.id, RecordStatus.FAILED,
                context.getString(R.string.err_no_send_permission)
            )
            return
        }

        try {
            val sm = getSmsManager(context)
            val base = (record.id % Int.MAX_VALUE).toInt()

            fun pendingIntent(action: String, code: Int): PendingIntent {
                val intent = Intent(context, SentStatusReceiver::class.java)
                    .setAction(action)
                    .putExtra("id", record.id)
                return PendingIntent.getBroadcast(
                    context, code, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val parts = sm.divideMessage(record.text)
            if (parts.size <= 1) {
                sm.sendTextMessage(
                    record.to, null, record.text,
                    pendingIntent(SentStatusReceiver.ACTION_SENT, base),
                    pendingIntent(SentStatusReceiver.ACTION_DELIVERED, base + 1)
                )
            } else {
                // پیامک طولانی: برای هر بخش یک PendingIntent جداگانه
                val sentPIs = ArrayList<PendingIntent>(parts.size)
                val deliveredPIs = ArrayList<PendingIntent>(parts.size)
                parts.indices.forEach { i ->
                    sentPIs.add(pendingIntent(SentStatusReceiver.ACTION_SENT, base + i * 2))
                    deliveredPIs.add(
                        pendingIntent(SentStatusReceiver.ACTION_DELIVERED, base + i * 2 + 1)
                    )
                }
                sm.sendMultipartTextMessage(record.to, null, parts, sentPIs, deliveredPIs)
            }
        } catch (e: Exception) {
            HistoryStore.update(
                context, record.id, RecordStatus.FAILED,
                e.message ?: context.getString(R.string.err_unknown_send)
            )
        }
    }

    private fun getSmsManager(context: Context): SmsManager =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
}
