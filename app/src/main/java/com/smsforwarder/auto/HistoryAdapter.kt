package com.smsforwarder.auto

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.smsforwarder.auto.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** آداپتر لیست تاریخچه ارسال */
class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val items = mutableListOf<ForwardRecord>()
    private val timeFormat = SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.getDefault())

    fun submit(records: List<ForwardRecord>) {
        items.clear()
        items.addAll(records)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        val ctx = holder.binding.root.context

        // برچسب وضعیت + رنگ
        val (label, color) = when (record.status) {
            RecordStatus.PENDING ->
                ctx.getString(R.string.status_pending) to ContextCompat.getColor(ctx, R.color.chip_wait)
            RecordStatus.SENT ->
                ctx.getString(R.string.status_sent) to ContextCompat.getColor(ctx, R.color.chip_ok)
            RecordStatus.DELIVERED ->
                ctx.getString(R.string.status_delivered) to ContextCompat.getColor(ctx, R.color.chip_delivered)
            RecordStatus.FAILED ->
                ctx.getString(R.string.status_failed) to ContextCompat.getColor(ctx, R.color.chip_fail)
        }

        holder.binding.chipStatus.text = label
        holder.binding.chipStatus.backgroundTintList =
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 42))
        holder.binding.chipStatus.setTextColor(color)

        holder.binding.textTime.text = timeFormat.format(Date(record.time))
        holder.binding.textRoute.text =
            ctx.getString(R.string.from_to_fmt, record.from, record.to)
        holder.binding.textBody.text = record.text

        if (record.error.isNullOrBlank()) {
            holder.binding.textError.visibility = View.GONE
        } else {
            holder.binding.textError.visibility = View.VISIBLE
            holder.binding.textError.text = record.error
        }
    }
}
