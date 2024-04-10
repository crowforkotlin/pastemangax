package com.crow.module_book.ui.adapter.comic

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.crow.base.app.app
import com.crow.base.tools.extensions.log
import com.crow.mangax.R.drawable.base_icon_app
import com.crow.mangax.copymanga.getImageUrl
import com.crow.mangax.ui.adapter.MangaCoilVH
import com.crow.module_book.databinding.BookComicCommentRvBinding
import com.crow.module_book.model.resp.comic_comment_total.ComicTotalCommentResult

/**
 * ⦁ ComicCommentRvAdapter
 *
 * ⦁ 2024/3/3 21:08
 * @author crowforkotlin
 * @formatter:on
 */
class ComicTotalCommentRvAdapter(
    private val mLifecycleScope: LifecycleCoroutineScope,
    private val onClick: (ComicTotalCommentResult) -> Unit
) : PagingDataAdapter<ComicTotalCommentResult, ComicTotalCommentRvAdapter.VH>(DiffCallback()) {

    /**
     * ⦁ DiffCallback
     *
     * ⦁ 2023-10-22 01:28:53 周日 上午
     * @author crowforkotlin
     */
    class DiffCallback: DiffUtil.ItemCallback<ComicTotalCommentResult>() {
        override fun areItemsTheSame(oldItem: ComicTotalCommentResult, newItem: ComicTotalCommentResult): Boolean {
            return oldItem.mId == newItem.mId
        }

        override fun areContentsTheSame(oldItem: ComicTotalCommentResult, newItem: ComicTotalCommentResult): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * ⦁ Bookshelf ViewHolder
     *
     * ⦁ 2023-10-22 01:29:27 周日 上午
     * @author crowforkotlin
     */
    inner class VH(binding: BookComicCommentRvBinding) : MangaCoilVH<BookComicCommentRvBinding>(binding) {

        init {
        }

        fun onBind(item: ComicTotalCommentResult) {

            val avatar = with(item.mUserAvatar) { if(this == null) null else getImageUrl(this) }

            app.imageLoader.enqueue(ImageRequest.Builder(itemView.context)
                .data(avatar)
                .scale(Scale.FIT)
                .placeholder(base_icon_app)
                .decoderFactory { source, option, _ -> Decoder { DecodeResult(drawable = BitmapFactory.decodeStream(source.source.source().inputStream()).toDrawable(option.context.resources), false) } }
                .target(binding.icon)
                .build())

            binding.name.text = item.mUserName
            binding.comment.text = item.mComment
            binding.commentTime.text = item.mCreateAt
        }
    }

    /**
     * ⦁ onCreateVH
     *
     * ⦁ 2023-10-22 01:29:37 周日 上午
     * @author crowforkotlin
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(BookComicCommentRvBinding.inflate(
        LayoutInflater.from(parent.context), parent,false))

    /**
     * ⦁ onBindVH
     *
     * ⦁ 2023-10-22 01:29:46 周日 上午
     * @author crowforkotlin
     */
    override fun onBindViewHolder(vh: VH, position: Int) { vh.onBind(getItem(position) ?: return) }
}