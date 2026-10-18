package com.smsforwarder.auto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smsforwarder.auto.databinding.ItemRuleBinding

/** آداپتر لیست قوانین در صفحه اصلی */
class RuleAdapter(
    private val onToggle: (Rule, Boolean) -> Unit,
    private val onClick: (Rule) -> Unit
) : RecyclerView.Adapter<RuleAdapter.ViewHolder>() {

    private val items = mutableListOf<Rule>()

    fun submit(rules: List<Rule>) {
        items.clear()
        items.addAll(rules)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemRuleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rule = items[position]
        val ctx = holder.binding.root.context

        holder.binding.textCondition.text = when (rule.mode) {
            MatchMode.SENDER -> ctx.getString(R.string.rule_row_condition_sender, rule.sender)
            MatchMode.KEYWORD -> ctx.getString(R.string.rule_row_condition_keyword, rule.keyword)
            MatchMode.BOTH -> ctx.getString(R.string.rule_row_condition_both, rule.sender, rule.keyword)
        }
        holder.binding.textDestination.text =
            ctx.getString(R.string.rule_row_destination, rule.destination)

        holder.binding.switchRule.setOnCheckedChangeListener(null)
        holder.binding.switchRule.isChecked = rule.enabled
        holder.binding.switchRule.setOnCheckedChangeListener { _, checked ->
            onToggle(rule, checked)
        }

        holder.binding.root.alpha = if (rule.enabled) 1f else 0.55f
        holder.binding.root.setOnClickListener { onClick(rule) }
    }
}
