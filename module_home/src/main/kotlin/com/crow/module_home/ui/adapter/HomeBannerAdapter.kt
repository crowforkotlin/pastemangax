package com.crow.module_home.ui.adapter

import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.app.appContext
import com.crow.base.tools.extensions.clickGap
import com.crow.module_home.databinding.HomeBannerRvBinding
import com.crow.module_home.databinding.HomeBannerRvBinding.inflate
import com.crow.module_home.model.ComicType
import com.crow.module_home.model.resp.homepage.Banner
import com.crow.module_home.ui.fragment.HomeFragment

class HomeBannerAdapter(
    val bannerList: MutableList<Banner>,
    val mITapComicListener: HomeFragment.ITapComicListener,
) : RecyclerView.Adapter<HomeBannerAdapter.ViewHolder>() {

    private var mComicListener: HomeFragment.ITapComicListener? = null

    inner class ViewHolder(val rvBinding: HomeBannerRvBinding) : RecyclerView.ViewHolder(rvBinding.root) {
        var mPathword: String = ""
    }

    override fun getItemCount(): Int = bannerList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflate(from(parent.context), parent, false)).also { vh ->
            vh.rvBinding.baneerImage.clickGap { _, _ -> mITapComicListener.onTap(ComicType.Banner, vh.mPathword) }
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

    fun setListener(clickListener: HomeFragment.ITapComicListener) { mComicListener = clickListener }

    /*suspend fun doOnNotify(datas: List<Banner>, delay: Long = 20L, waitTime: Long = 100L) {
        bannerList.clear()
        datas.forEachIndexed { index, banner ->
            if (banner.mType <= 2) bannerList.add(banner)
            notifyItemChanged(index)
        }
    }*/
}