package com.crow.module_home.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crow.base.app.appContext
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeRvBannerBinding
import com.crow.module_home.model.resp.homepage.Banner

class HomeBannerAdapter(
    val bannerList: MutableList<Banner>,
    inline val clickCallback: HomeBannerAdapter.(Banner, String) -> Unit,
) : RecyclerView.Adapter<HomeBannerAdapter.ViewHolder>() {

    inner class ViewHolder(val rvBinding: HomeRvBannerBinding) :
        RecyclerView.ViewHolder(rvBinding.root) {

    }

    override fun getItemCount(): Int = bannerList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(HomeRvBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = bannerList[position]
        Glide.with(appContext)
            .load(item.mImgUrl)
            .placeholder(R.drawable.home_ic_search_24dp)
            .into(holder.rvBinding.baneerImage)
        holder.rvBinding.bannerText.text = item.mBrief
    }

}