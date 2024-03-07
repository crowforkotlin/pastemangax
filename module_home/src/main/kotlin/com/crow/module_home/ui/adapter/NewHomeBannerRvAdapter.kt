package com.crow.module_home.ui.adapter

import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_home.databinding.HomeFragmentBannerRvItemBinding
import com.crow.module_home.model.resp.homepage.Banner
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NewHomeBannerRvAdapter(val onTap: (String) -> Unit) : RecyclerView.Adapter<NewHomeBannerRvAdapter.BannerVH>() {

    /**
     * ⦁ ViewHolder
     *
     * ⦁ 2023-09-17 19:23:47 周日 下午
     */
    inner class BannerVH(val binding: HomeFragmentBannerRvItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.baneerImage.doOnClickInterval { onTap(getItem(absoluteAdapterPosition).mComic?.mPathWord ?: return@doOnClickInterval) }
        }
    }

    /**
     * ⦁ Banner Data
     *
     * ⦁ 2023-09-17 19:23:53 周日 下午
     */
    private var mBanners: MutableList<Banner> = mutableListOf()

    /**
     * ⦁ Coroutine lock
     *
     * ⦁ 2023-09-17 19:25:13 周日 下午
     */
    private val mMutex = Mutex()

    /**
     * ⦁ Banner Data Size
     *
     * ⦁ 2023-09-17 19:26:22 周日 下午
     */
    override fun getItemCount(): Int = mBanners.size

    /**
     * ⦁ Create ViewHolder
     *
     * ⦁ 2023-09-17 19:26:13 周日 下午
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerVH {
        return BannerVH(HomeFragmentBannerRvItemBinding.inflate(from(parent.context), parent, false))
    }

    /**
     * ⦁ Reuse ViewHolder
     *
     * ⦁ 2023-09-17 19:25:42 周日 下午
     */
    override fun onBindViewHolder(vh: BannerVH, position: Int) {

        // BannerData
        val banner = getItem(position)

        // setImageText
         vh.binding.bannerText.text = banner.mBrief
    }

    /**
     * ⦁ Get Banner
     *
     * ⦁ 2023-09-17 19:24:03 周日 下午
     */
    private fun getItem(@IntRange(from = 0) position: Int) = mBanners[position]

    /**
     * ⦁ Submit BannerData
     *
     * ⦁ 2023-09-17 19:24:10 周日 下午
     */
    suspend fun submitList(banners: MutableList<Banner>, duration: Long) {

        // 上锁
        mMutex.withLock {
            val isCountSame = itemCount == banners.size

            // 延时小于阈值则 直接显示
            val isDurationThreshold = duration > 10
            if (isCountSame) mBanners = banners
            else if(itemCount != 0) {
                notifyItemRangeRemoved(0, itemCount)
                mBanners.clear()
            }
            banners.forEachIndexed { index, data ->
                if (!isCountSame) {
                    mBanners.add(data)
                    notifyItemInserted(index)
                } else {
                    notifyItemChanged(index)
                }
                if (isDurationThreshold) delay(duration)
            }
        }
    }
}