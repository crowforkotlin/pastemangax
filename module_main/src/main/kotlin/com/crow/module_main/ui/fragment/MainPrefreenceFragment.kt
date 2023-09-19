package com.crow.module_main.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.crow.module_main.R

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.fragment
 * @Time: 2023/9/18 0:07
 * @Author: CrowForKotlin
 * @Description:
 * @formatter:on
 **************************/

interface RecyclerViewOwner {

    val recyclerView: RecyclerView
}

class MainPrefreenceFragment :  PreferenceFragmentCompat(), RecyclerViewOwner {

    override val recyclerView: RecyclerView get() = listView

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.main_pref_settings)
        bindPreferenceSummary("style")
        bindPreferenceSummary("site")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun bindPreferenceSummary(key: String, @StringRes vararg items: Int) {
        findPreference<Preference>(key)?.summary = items.joinToString { getString(it) }
    }
}