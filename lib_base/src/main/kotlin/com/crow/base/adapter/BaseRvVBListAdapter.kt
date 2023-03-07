package com.crow.base.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/adapter
 * @Time: 2022/5/6 18:08
 * @Author: BarryAllen
 * @Description: BaseRvListAdapter
 * @formatter:off
 **************************/

abstract class BaseRvVBListAdapter<T: Any, VB : ViewBinding>(getContentIsSame : (oldItem: T, newItem: T) -> Boolean)
    : ListAdapter<T, BaseRvVBListAdapter<T, VB>.ViewHolder>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem === newItem
    override fun areContentsTheSame(oldItem: T, newItem: T) = getContentIsSame(oldItem, newItem)
}) {

    inner class ViewHolder(val mBinding: VB) : RecyclerView.ViewHolder(mBinding.root)

    abstract fun getViewBinding(parent: ViewGroup): VB

    open fun ViewHolder.initCreateViewHolder(parent: ViewGroup,viewType: Int) { }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(getViewBinding(parent))
        holder.initCreateViewHolder(parent,viewType)
        return holder
    }
}