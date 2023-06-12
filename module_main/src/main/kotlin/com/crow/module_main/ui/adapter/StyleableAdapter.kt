package com.crow.module_main.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_main.databinding.MainStyleableRvBinding
import com.crow.module_main.model.entity.StyleableEntity

class StyleableAdapter(val mStyables: MutableList<StyleableEntity>, val onSwitch: (Int, Boolean) -> Unit) : RecyclerView.Adapter<StyleableAdapter.ViewHolder>() {

    inner class ViewHolder(val rvBinding: MainStyleableRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    override fun getItemCount(): Int = mStyables.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(MainStyleableRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->
            vh.rvBinding.mainStyleableRvSwitch.doOnClickInterval {
                onSwitch(vh.absoluteAdapterPosition, vh.rvBinding.mainStyleableRvSwitch.isChecked)
            }
        }
    }


    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val styable = mStyables[position]
        vh.rvBinding.mainStyleableRvText.text = styable.mContent
        vh.rvBinding.mainStyleableRvText.isEnabled = styable.mIsContentEnable
        vh.rvBinding.mainStyleableRvSwitch.isChecked = styable.mIsChecked
        vh.rvBinding.mainStyleableRvSwitch.isEnabled = styable.mIsSwitchEnable
    }
}