package com.smsforwarder.auto

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.smsforwarder.auto.databinding.ActivityRuleEditorBinding

/** ساخت یا ویرایش یک قانون فوروارد */
class RuleEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRuleEditorBinding
    private var editId = -1L
    private var existingRule: Rule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRuleEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editId = intent.getLongExtra("id", -1L)
        if (editId != -1L) {
            existingRule = RulesStore.load(this).firstOrNull { it.id == editId }
        }
        if (existingRule == null) {
            editId = -1L
        }

        // تنظیم عنوان و دکمه بازگشت
        binding.toolbar.title = if (editId != -1L) {
            getString(R.string.editor_title_edit)
        } else {
            getString(R.string.editor_title_new)
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (existingRule != null) {
            prefill(existingRule!!)
        } else {
            binding.btnDelete.visibility = View.GONE
        }

        binding.radioMode.setOnCheckedChangeListener { _, _ -> updateFieldVisibility() }
        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        updateFieldVisibility()
    }

    private fun prefill(rule: Rule) {
        when (rule.mode) {
            MatchMode.SENDER -> binding.radioSender.isChecked = true
            MatchMode.KEYWORD -> binding.radioKeyword.isChecked = true
            MatchMode.BOTH -> binding.radioBoth.isChecked = true
        }
        binding.etSender.setText(rule.sender)
        binding.etKeyword.setText(rule.keyword)
        binding.etDestination.setText(rule.destination)
    }

    private fun currentMode(): MatchMode = when (binding.radioMode.checkedRadioButtonId) {
        R.id.radioSender -> MatchMode.SENDER
        R.id.radioKeyword -> MatchMode.KEYWORD
        else -> MatchMode.BOTH
    }

    private fun updateFieldVisibility() {
        val mode = currentMode()
        binding.layoutSender.visibility =
            if (mode == MatchMode.KEYWORD) View.GONE else View.VISIBLE
        binding.layoutKeyword.visibility =
            if (mode == MatchMode.SENDER) View.GONE else View.VISIBLE

        binding.textModeDesc.setText(
            when (mode) {
                MatchMode.SENDER -> R.string.mode_sender_desc
                MatchMode.KEYWORD -> R.string.mode_keyword_desc
                MatchMode.BOTH -> R.string.mode_both_desc
            }
        )
    }

    private fun save() {
        val mode = currentMode()
        val sender = binding.etSender.text.toString().trim()
        val keyword = binding.etKeyword.text.toString().trim()
        val destination = binding.etDestination.text.toString().trim()

        var valid = true
        if (mode != MatchMode.KEYWORD && sender.isEmpty()) {
            binding.layoutSender.error = getString(R.string.err_need_sender)
            valid = false
        } else {
            binding.layoutSender.error = null
        }
        if (mode != MatchMode.SENDER && keyword.isEmpty()) {
            binding.layoutKeyword.error = getString(R.string.err_need_keyword)
            valid = false
        } else {
            binding.layoutKeyword.error = null
        }
        if (destination.isEmpty()) {
            binding.layoutDestination.error = getString(R.string.err_need_destination)
            valid = false
        } else {
            binding.layoutDestination.error = null
        }
        if (!valid) return

        val rule = Rule(
            id = if (editId != -1L) editId else RulesStore.newId(),
            mode = mode,
            sender = sender,
            keyword = keyword,
            destination = destination,
            enabled = existingRule?.enabled ?: true
        )
        RulesStore.upsert(this, rule)
        Toast.makeText(this, R.string.rule_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setMessage(R.string.delete_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                RulesStore.delete(this, editId)
                Toast.makeText(this, R.string.rule_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
