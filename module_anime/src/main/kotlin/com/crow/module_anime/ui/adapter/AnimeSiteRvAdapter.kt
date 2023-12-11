package com.crow.module_anime.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_anime.R
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


/**
 * ● SiteRvAdapter
 *
 * ● 2023-11-11 15:45:26 周六 下午
 * @author crowforkotlin
 */
class AnimeSiteRvAdapter(
    private var mClick: (position: Int, site: String) -> Unit
) : RecyclerView.Adapter<AnimeSiteRvAdapter.SiteVH>() {

    inner class SiteVH(val button: MaterialRadioButton) : RecyclerView.ViewHolder(button) {
        
        init {
            button.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            button.doOnClickInterval { mClick(absoluteAdapterPosition, getItem(absoluteAdapterPosition)) }
        }
        
        @SuppressLint("SetTextI18n")
        fun onBind(item: String, position: Int) {
            button.text = itemView.context.getString(R.string.anime_site_position, position)
        }
    }

    private var mSites: MutableList<String> = mutableListOf()

    private val mMutex = Mutex()

    fun getItem(position: Int) = mSites[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteVH { return SiteVH(MaterialRadioButton(parent.context)) }

    override fun getItemCount(): Int = mSites.size

    override fun onBindViewHolder(vh: SiteVH, position: Int) {
        vh.onBind(getItem(position), position)
    }

    suspend fun doNotify(newDataResult: MutableList<String>, delayMs: Long = 1L) {
        mMutex.withLock {
            val isCountSame = itemCount == newDataResult.size
            if (isCountSame) {
                mSites = newDataResult
            }
            else if(itemCount != 0) {
                notifyItemRangeRemoved(0, itemCount)
                mSites.clear()
                delay(BASE_ANIM_200L)
            }
            newDataResult.forEachIndexed { index, data ->
                if (!isCountSame) {
                    mSites.add(data)
                    notifyItemInserted(index)
                } else notifyItemChanged(index)
                delay(delayMs)
            }
        }
    }
}