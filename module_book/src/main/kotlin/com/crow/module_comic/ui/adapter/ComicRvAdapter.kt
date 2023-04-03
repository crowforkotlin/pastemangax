package com.crow.module_comic.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crow.base.app.appContext
import com.crow.base.tools.extensions.logMsg
import com.crow.module_book.databinding.BookComicRvBinding
import com.crow.module_comic.model.resp.ComicPageResp
import kotlinx.serialization.json.JsonNull.content

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/adapter
 * @Time: 2023/3/15 16:42
 * @Author: CrowForKotlin
 * @Description: ComicInfoChapterRvAdapter
 * @formatter:off
 **************************/
class ComicRvAdapter(private var mComicPageResp: ComicPageResp? = null) : RecyclerView.Adapter<ComicRvAdapter.ViewHolder>() {

    inner class ViewHolder(val rvBinding: BookComicRvBinding) : RecyclerView.ViewHolder(rvBinding.root)

    private val mHeight by lazy { (appContext.resources.displayMetrics.heightPixels / 3 ) * 2}


    private fun doListener(vh: ViewHolder): RequestListener<Drawable>{
        return object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                "Failure".logMsg()
                vh.rvBinding.comicRvRetry.isVisible = true
                vh.rvBinding.comicRvImageView.layoutParams.height = mHeight
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                vh.rvBinding.comicRvImageView.layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
                return false
            }
        }
    }

    fun setData(data: ComicPageResp) { mComicPageResp = data }

    fun getDataSize() = (mComicPageResp?.mChapter?.mSize) ?: 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BookComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)).also {  vh ->
//            vh.mPhotoView.layoutParams.height = mHeight
        }
    }

    override fun getItemCount(): Int = (mComicPageResp?.mChapter?.mSize) ?: 0

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        Glide.with(vh.itemView)
            .load((mComicPageResp ?: return).mChapter.mContents[position].url)
            .listener(doListener(vh))
            .into(vh.rvBinding.comicRvImageView)
    }

}