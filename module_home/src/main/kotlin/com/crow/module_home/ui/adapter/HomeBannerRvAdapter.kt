package com.crow.module_home.ui.adapter

import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_home.databinding.HomeFragmentBannerRvItemBinding
import com.crow.module_home.model.resp.homepage.Banner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeBannerRvAdapter(
    private var mBannerList: MutableList<Banner> = mutableListOf(),
    inline val onTap: (String) -> Unit,
) : RecyclerView.Adapter<HomeBannerRvAdapter.ViewHolder>() {

    inner class ViewHolder(val rvBinding: HomeFragmentBannerRvItemBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathword: String = "" }

    override fun getItemCount(): Int = mBannerList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeFragmentBannerRvItemBinding.inflate(from(parent.context), parent, false)).also { vh ->
            vh.rvBinding.baneerImage.doOnClickInterval { onTap(vh.mPathword) }
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {

        // BannerData
        val banner = mBannerList[position]

        // Set Pathword
        vh.mPathword = banner.mComic?.mPathWord ?: return

        // loadImage
        Glide.with(vh.itemView)
            .load(banner.mImgUrl)
            .into(vh.rvBinding.baneerImage)

        // setImageText
         vh.rvBinding.bannerText.text = banner.mBrief
    }

    fun doBannerNotify(banners: MutableList<Banner>, delay: Long, viewLifecycleOwner: LifecycleOwner) {
        viewLifecycleOwner.lifecycleScope.launch {
            val isCountSame = itemCount == banners.size
            if (isCountSame) mBannerList = banners
            else if(itemCount != 0) {
                notifyItemRangeRemoved(0, itemCount)
                mBannerList.clear()
            }
            banners.forEachIndexed { index, data ->
                if (!isCountSame) {
                    mBannerList.add(data)
                    notifyItemInserted(index)
                } else notifyItemChanged(index)
                delay(delay)
            }
        }
    }
}