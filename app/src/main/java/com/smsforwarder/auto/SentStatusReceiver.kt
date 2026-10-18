package com.smsforwarder.auto

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager

/**
 * دریافت نتیجه ارسال و تحویل پیامک‌های فورواردشده
 * و به‌روزرسانی وضعیت رکورد در تاریخچه.
 */
class SentStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("id", -1L)
        if (id <= 0) return

        val isSentAction = intent.action == ACTION_SENT

        when (resultCode) {
            Activity.RESULT_OK -> {
                if (isSentAction) {
                    HistoryStore.update(context, id, RecordStatus.SENT, null)
                } else {
                    HistoryStore.update(context, id, RecordStatus.DELIVERED, null)
                }
            }
            else -> {
                val reason = when (resultCode) {
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE ->
                        context.getString(R.string.err_generic_failure)
                    SmsManager.RESULT_ERROR_RADIO_OFF ->
                        context.getString(R.string.err_radio_off)
                    SmsManager.RESULT_ERROR_NO_SERVICE ->
                        context.getString(R.string.err_no_service)
                    SmsManager.RESULT_ERROR_NULL_PDU ->
                        context.getString(R.string.err_null_pdu)
                    else ->
                        context.getString(R.string.err_send_code, resultCode)
                }
                // فقط خطای مرحله «ارسال» را ناموفق ثبت می‌کنیم
                if (isSentAction) {
                    HistoryStore.update(context, id, RecordStatus.FAILED, reason)
                }
            }
        }
    }

    companion object {
        const val ACTION_SENT = "com.smsforwarder.auto.ACTION_SMS_SENT"
        const val ACTION_DELIVERED = "com.smsforwarder.auto.ACTION_SMS_DELIVERED"
        const val EXTRA_ID = "id"
    }
}
