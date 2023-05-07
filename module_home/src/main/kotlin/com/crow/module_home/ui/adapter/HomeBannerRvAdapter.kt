package com.crow.module_home.ui.adapter

import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.app.appContext
import com.crow.base.copymanga.entity.BookType
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.module_home.databinding.HomeFragmentBannerRvItemBinding
import com.crow.module_home.model.resp.homepage.Banner
import kotlinx.coroutines.delay

class HomeBannerRvAdapter(
    private var mBannerList: MutableList<Banner> = mutableListOf(),
    inline val onTap: (BookType, String) -> Unit,
) : RecyclerView.Adapter<HomeBannerRvAdapter.ViewHolder>() {

    inner class ViewHolder(val rvBinding: HomeFragmentBannerRvItemBinding) : RecyclerView.ViewHolder(rvBinding.root) { var mPathword: String = "" }

    override fun getItemCount(): Int = mBannerList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeFragmentBannerRvItemBinding.inflate(from(parent.context), parent, false)).also { vh ->
            vh.rvBinding.baneerImage.doOnClickInterval { onTap(BookType.Banner, vh.mPathword) }
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val banner = mBannerList[position]
        Glide.with(appContext).load(banner.mImgUrl).into(vh.rvBinding.baneerImage)
        vh.rvBinding.bannerText.text = banner.mBrief
        vh.mPathword = banner.mComic?.mPathWord ?: return
    }

    suspend fun doBannerNotify(banners: MutableList<Banner>, delay: Long) {
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