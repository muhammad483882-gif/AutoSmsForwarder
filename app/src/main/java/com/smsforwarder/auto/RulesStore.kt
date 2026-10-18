package com.smsforwarder.auto

import android.content.Context
import org.json.JSONArray

/** ذخیره و بازیابی قوانین فوروارد در SharedPreferences (به صورت JSON) */
object RulesStore {

    private const val FILE = "sms_forwarder_prefs"
    private const val KEY_RULES = "rules"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun load(context: Context): MutableList<Rule> {
        val json = prefs(context).getString(KEY_RULES, null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<Rule>()
            for (i in 0 until arr.length()) {
                list.add(Rule.fromJson(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun save(context: Context, rules: List<Rule>) {
        val arr = JSONArray()
        rules.forEach { arr.put(it.toJson()) }
        prefs(context).edit().putString(KEY_RULES, arr.toString()).apply()
    }

    fun upsert(context: Context, rule: Rule) {
        val list = load(context)
        val index = list.indexOfFirst { it.id == rule.id }
        if (index >= 0) list[index] = rule else list.add(rule)
        save(context, list)
    }

    fun delete(context: Context, id: Long) {
        val list = load(context).filter { it.id != id }
        save(context, list)
    }

    fun newId(): Long = System.currentTimeMillis()
}
