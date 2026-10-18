package com.smsforwarder.auto

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsforwarder.auto.databinding.ActivityHistoryBinding

/** تاریخچه پیامک‌های فورواردشده */
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(R.string.history_title)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = HistoryAdapter()
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_clear -> {
                    confirmClear()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        val records = HistoryStore.all(this)
        adapter.submit(records)
        binding.emptyHistory.visibility =
            if (records.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerHistory.visibility =
            if (records.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun confirmClear() {
        AlertDialog.Builder(this)
            .setMessage(R.string.clear_history_confirm)
            .setPositiveButton(R.string.clear_history) { _, _ ->
                HistoryStore.clear(this)
                reload()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
