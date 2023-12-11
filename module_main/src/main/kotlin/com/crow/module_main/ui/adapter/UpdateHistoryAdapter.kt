package com.crow.module_main.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentUpdateHistoryRvBinding
import com.crow.module_main.model.resp.update.Update
import kotlinx.coroutines.delay

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.adapter
 * @Time: 2023/6/21 0:39
 * @Author: CrowForKotlin
 * @Description: UpdateHistoryAdapter
 * @formatter:on
 **************************/
class UpdateHistoryAdapter : RecyclerView.Adapter<UpdateHistoryAdapter.UpdateViewHolder>() {

    private val mDiffCallback: DiffUtil.ItemCallback<Update> = object : DiffUtil.ItemCallback<Update>() {
        override fun areItemsTheSame(oldItem: Update, newItem: Update): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Update, newItem: Update): Boolean {
            return oldItem.mVersionCode == newItem.mVersionCode
        }
    }

    private var mItems: MutableList<Update> = mutableListOf()

    inner class UpdateViewHolder(val binding: MainFragmentUpdateHistoryRvBinding) : RecyclerView.ViewHolder(binding.root) {


        fun setMotionLayoutState(currentState: Boolean) {
            val goalProgress = if (currentState) 1f else 0f
            safeRunBlock { startTransition(currentState) }
            if (binding.updateMotion.progress != goalProgress) {
                val desiredState = if (currentState) binding.updateMotion.startState else binding.updateMotion.endState
                if (binding.updateMotion.currentState != desiredState) {
                    safeRunBlock { startTransition(currentState) }
                }
            }
        }

        fun safeRunBlock(block: () -> Unit) {
            if (ViewCompat.isLaidOut(binding.updateMotion)) {
                block()
            } else {
                binding.updateMotion.post(block)
            }
        }

        fun startTransition(currentState: Boolean) {
            if (currentState) {
                binding.updateMotion.transitionToStart()
            } else {
                binding.updateMotion.transitionToEnd()
            }
        }
    }

    override fun getItemCount(): Int = mItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdateViewHolder {
        return UpdateViewHolder(MainFragmentUpdateHistoryRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->
            vh.binding.updateUp.doOnClickInterval(false) {
                if (vh.binding.updateUp.rotation == 0.0f) {
                    vh.setMotionLayoutState(false)
                    getItem(vh.absoluteAdapterPosition).mExpand = true
                } else {
                    vh.binding.updateMotion.transitionToStart()
                    vh.setMotionLayoutState(true)
                    getItem(vh.absoluteAdapterPosition).mExpand = false
                }
            }
        }
    }

    override fun onBindViewHolder(vh: UpdateViewHolder, position: Int) {
        val item = getItem(position)
        vh.binding.updateTime.text = vh.itemView.context.getString(R.string.main_update_time, item.mTime)
        vh.binding.updateVersion.text = item.mVersionName
        vh.binding.updateInfo.text = item.mContent
    }

    private fun getItem(@IntRange(from = 0) position: Int) = mItems[position]

    suspend fun notifyInstet(updates: MutableList<Update>, duration: Long) {
        updates.forEachIndexed { index, data ->
            mItems.add(data)
            notifyItemInserted(index)
            delay(duration)
        }
    }
}