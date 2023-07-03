package com.crow.module_main.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.BASE_ANIM_100L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentUpdateHistoryRvBinding
import com.crow.module_main.model.resp.MainAppUpdateResp
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
class UpdateHistoryAdapter(var mUpdateResult: MutableList<Update>) : RecyclerView.Adapter<UpdateHistoryAdapter.UpdateViewHolder>() {

    inner class UpdateViewHolder(val rvBinding: MainFragmentUpdateHistoryRvBinding) : RecyclerView.ViewHolder(rvBinding.root) {

    }

    override fun getItemCount(): Int = mUpdateResult.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdateViewHolder {
        return UpdateViewHolder(MainFragmentUpdateHistoryRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->

            vh.rvBinding.updateUp.doOnClickInterval(false) {
                vh.rvBinding.updateMotion.setTransitionListener(object : MotionLayout.TransitionListener {
                    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) { }
                    override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
                    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) { }
                    override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}
                })

                if (vh.rvBinding.updateMotion.currentState == R.id.start && vh.rvBinding.updateMotion.progress.toInt() == 0) {
                    vh.rvBinding.updateMotion.transitionToEnd()
                } else if(vh.rvBinding.updateMotion.progress.toInt() == 1){
                    vh.rvBinding.updateMotion.transitionToStart()
                }
            }
        }
    }

    override fun onBindViewHolder(vh: UpdateViewHolder, position: Int) {
        val item = mUpdateResult[position]
        vh.rvBinding.updateTime.text = vh.itemView.context.getString(R.string.main_update_time, item.mTime)
        vh.rvBinding.updateVersion.text = item.mVersionName
        vh.rvBinding.updateInfo.text = item.mContent
    }


    suspend fun doNotify(updateResp: MainAppUpdateResp, delay: Long) {
        if(itemCount != 0) {
            notifyItemRangeRemoved(0, itemCount)
            mUpdateResult.clear()
            delay(BASE_ANIM_100L)
        }
        updateResp.mUpdates.forEachIndexed { index, data ->
            mUpdateResult.add(data)
            notifyItemInserted(index)
            delay(delay)
        }
    }
}