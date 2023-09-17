package com.crow.module_home.ui.adapter

import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_home.databinding.HomeFragmentBannerRvItemBinding
import com.crow.module_home.model.resp.homepage.Banner
import kotlinx.coroutines.delay

class NewHomeBannerRvAdapter(inline val onTap: (String) -> Unit) :
    RecyclerView.Adapter<NewHomeBannerRvAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: HomeFragmentBannerRvItemBinding) : RecyclerView.ViewHolder(binding.root) { var mPathword: String = "" }

    private var mBanners: MutableList<Banner> = mutableListOf()


    private fun getItem(@IntRange(from = 0) position: Int) = mBanners[position]

    suspend fun submitList(banners: MutableList<Banner>, duration: Long) {
        val isCountSame = itemCount == banners.size
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

    override fun getItemCount(): Int = mBanners.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeFragmentBannerRvItemBinding.inflate(from(parent.context), parent, false)).also { vh ->
            vh.binding.baneerImage.doOnClickInterval { onTap(vh.mPathword) }
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {

        // BannerData
        val banner = getItem(position)

        // Set Pathword
        vh.mPathword = banner.mComic?.mPathWord ?: return

        // loadImage
        Glide.with(vh.itemView)
            .load(banner.mImgUrl)
            .into(vh.binding.baneerImage)

        // setImageText
         vh.binding.bannerText.text = banner.mBrief
    }
}