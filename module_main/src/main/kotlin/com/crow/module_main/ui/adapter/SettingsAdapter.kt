package com.crow.module_main.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_main.databinding.MainFragmentSettingsRvBinding

class SettingsAdapter(val contents: MutableList<Pair<Int?, String>>, val doOnClick: (pos: Int) -> Unit) : RecyclerView.Adapter<SettingsAdapter.SettingsMainViewHolder>() {

    inner class SettingsMainViewHolder(val rvBinding: MainFragmentSettingsRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    override fun getItemCount(): Int = contents.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsMainViewHolder {
        return SettingsMainViewHolder(MainFragmentSettingsRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->
            vh.itemView.doOnClickInterval {
                doOnClick(vh.absoluteAdapterPosition)
            }
        }
    }
    override fun onBindViewHolder(vh: SettingsMainViewHolder, position: Int) {
        val content = contents[position]
        vh.rvBinding.settingsRvText.text = content.second
        vh.rvBinding.settingsRvIcon.setImageDrawable(ContextCompat.getDrawable(vh.itemView.context, content.first ?: return))
    }
}