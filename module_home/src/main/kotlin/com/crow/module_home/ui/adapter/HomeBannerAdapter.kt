package com.crow.module_home.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.app.appContext
import com.crow.base.extensions.clickGap
import com.crow.module_home.databinding.HomeRvBannerBinding
import com.crow.module_home.model.ComicType
import com.crow.module_home.model.resp.homepage.Banner
import com.crow.module_home.ui.fragment.HomeFragment

class HomeBannerAdapter(
    val bannerList: MutableList<Banner>,
    val mClickComicListener: HomeFragment.ClickComicListener,
) : RecyclerView.Adapter<HomeBannerAdapter.ViewHolder>() {

    private var mComicListener: HomeFragment.ClickComicListener? = null

    inner class ViewHolder(val rvBinding: HomeRvBannerBinding) : RecyclerView.ViewHolder(rvBinding.root) {
        var mPathword: String = ""
    }

    override fun getItemCount(): Int = bannerList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeRvBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also { vh ->
            vh.rvBinding.baneerImage.clickGap { _, _ -> mClickComicListener.onClick(ComicType.Banner, vh.mPathword) }
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val banner = bannerList[position]
        Glide.with(appContext)
            .load(banner.mImgUrl)
            .into(vh.rvBinding.baneerImage)
        vh.rvBinding.bannerText.text = banner.mBrief
        vh.mPathword = banner.mComic!!.mPathWord
    }

    fun setListener(clickListener: HomeFragment.ClickComicListener) { mComicListener = clickListener }

}