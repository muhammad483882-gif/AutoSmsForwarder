package com.smsforwarder.auto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsforwarder.auto.databinding.ActivityMainBinding

/**
 * صفحه اصلی: کلید روشن/خاموش اصلی، نوار درخواست دسترسی‌ها و لیست قوانین.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: RuleAdapter

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            refreshUi()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RuleAdapter(
            onToggle = { rule, checked ->
                RulesStore.upsert(this, rule.copy(enabled = checked))
                refreshCount()
            },
            onClick = { rule ->
                startActivity(
                    Intent(this, RuleEditorActivity::class.java)
                        .putExtra("id", rule.id)
                )
            }
        )
        binding.recyclerRules.layoutManager = LinearLayoutManager(this)
        binding.recyclerRules.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, RuleEditorActivity::class.java))
        }
        binding.btnGrant.setOnClickListener { requestPermissions() }
        binding.bannerPermissions.setOnClickListener { requestPermissions() }

        binding.switchMaster.setOnCheckedChangeListener { _, checked ->
            Prefs.setMasterEnabled(this, checked)
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS
            )
        )
    }

    private fun hasPermissions(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED

    private fun refreshUi() {
        val rules = RulesStore.load(this)
        adapter.submit(rules)
        binding.emptyRules.visibility = if (rules.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerRules.visibility = if (rules.isEmpty()) View.GONE else View.VISIBLE
        binding.bannerPermissions.visibility =
            if (hasPermissions()) View.GONE else View.VISIBLE
        binding.switchMaster.isChecked = Prefs.isMasterEnabled(this)
        refreshCount()
    }

    private fun refreshCount() {
        val rules = RulesStore.load(this)
        binding.textRulesCount.text =
            getString(R.string.rules_count_fmt, rules.count { it.enabled }, rules.size)
    }
}
