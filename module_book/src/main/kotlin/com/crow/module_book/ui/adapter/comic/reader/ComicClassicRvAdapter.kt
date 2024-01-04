package com.crow.module_book.ui.adapter.comic.reader

import android.animation.ObjectAnimator
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntRange
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.decode.DataSource
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import coil.target.Target
import coil.transition.CrossfadeTransition
import coil.transition.Transition
import coil.transition.TransitionTarget
import com.crow.base.app.app
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.mangax.copymanga.BaseUserConfig
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.immersionPadding
import com.crow.mangax.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.base.ui.view.event.BaseEvent
import com.crow.module_book.databinding.BookActivityComicRvBinding
import com.crow.module_book.databinding.BookFragmentClassicIntentRvBinding
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo
import com.crow.module_book.model.resp.comic_page.Content

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.adapter
 * @Time: 2023/9/1 1:15
 * @Author: CrowForKotlin
 * @formatter:on
 **************************/
class ComicClassicRvAdapter(val onPrevNext: (ReaderPrevNextInfo) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val Header = 0
        private const val Body = 1
        private const val Footer = 2
    }

    var mChapterName: String? = null
    var mComicName: String? = null

    /**
     * ● Diff 回调
     *
     * ● 2023-09-02 20:06:41 周六 下午
     */
    private val mDiffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return true
        }
    }

    /**
     * ● 异步 Differ 实例
     *
     * ● 2023-09-02 20:06:53 周六 下午
     */
    private val mDiffer = AsyncListDiffer(this, mDiffCallback)


    inner class BodyViewHolder(binding: BookActivityComicRvBinding) : BaseGlideLoadingViewHolder<BookActivityComicRvBinding>(binding) {
        fun onBind(position: Int) {
            val item = getItem(position) as Content
            val imageUrl = when {
                item.mImageUrl.contains("c800x.") -> item.mImageUrl.replace("c800x.", "c${BaseUserConfig.RESOLUTION}x.")
                item.mImageUrl.contains("c1200x.") -> item.mImageUrl.replace("c1200x.", "c${BaseUserConfig.RESOLUTION}x.")
                item.mImageUrl.contains("c1500x.") -> item.mImageUrl.replace("c1500x.", "c${BaseUserConfig.RESOLUTION}x.")
                else -> item.mImageUrl
            }

            binding.loading.isVisible = true
            binding.loadingText.isVisible = true
            binding.loadingText.text = AppProgressFactory.PERCENT_0
            binding.retry.isGone = true
            mAppGlideProgressFactory?.removeProgressListener()?.remove()
            mAppGlideProgressFactory = AppProgressFactory.createProgressListener(imageUrl) { _, _, percentage, _, _ -> binding.loadingText.text = AppProgressFactory.formateProgress(percentage) }
            itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = ViewGroup.LayoutParams.MATCH_PARENT }

            val request = ImageRequest.Builder(itemView.context)
                .data(imageUrl)
                .listener(
                    onSuccess = { request, result ->
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = ViewGroup.LayoutParams.WRAP_CONTENT }
                    }
                )

                .target(binding.image)
                .build()
            app.imageLoader.enqueue(request)
//            binding.image.load(imageUrl)
            /*Glide.with(itemView.context)
                .load(imageUrl)
                .addListener(
                    mAppGlideProgressFactory?.getRequestListener(
                        failure = {
                            binding.retry.alpha = 1f
                            binding.retry.isVisible = true
                            binding.loading.isInvisible = true
                            binding.loadingText.isInvisible = true
                            binding.retry.doOnClickInterval(false) {
                                binding.retry.animateFadeOut().withEndAction {
                                    onBind(position)
                                }
                            }
                            false
                        },
                        ready = { _, _ ->
                            itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = ViewGroup.LayoutParams.WRAP_CONTENT }
                            false
                         }
                    )
                )
                .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                    val transition = if (dataSource == DataSource.REMOTE) {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        DrawableCrossFadeTransition(300, true)
                    } else {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        NoTransition()
                    }
                    transition
                })
                .into(binding.image)*/
        }
    }

    inner class IntentViewHolder(val binding: BookFragmentClassicIntentRvBinding, isNext: Boolean) : RecyclerView.ViewHolder(binding.root) {

        init {
            if (isNext) immersionPadding(binding.root, paddingStatusBar = false, paddingNaviateBar = true)
            else immersionPadding(binding.root, paddingStatusBar = true, paddingNaviateBar = false)

            binding.comicNext.setOnClickListener {
                BaseEvent.getSIngleInstance().doOnInterval {
                    onPrevNext(getItem(absoluteAdapterPosition) as ReaderPrevNextInfo)
                }
            }
        }

        fun onBind(position: Int) {
            binding.comicNext.text = (getItem(absoluteAdapterPosition) as ReaderPrevNextInfo).mInfo
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Header ->IntentViewHolder(BookFragmentClassicIntentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false), false)
            Footer ->IntentViewHolder(BookFragmentClassicIntentRvBinding.inflate(LayoutInflater.from(parent.context), parent,false), true)
            Body -> BodyViewHolder(BookActivityComicRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> error("Unknown view type!")
        }
    }

    override fun getItemCount(): Int = mDiffer.currentList.size

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            0 -> Header
            itemCount - 1 -> Footer
            else -> Body
        }
    }

    override fun onViewRecycled(vh: RecyclerView.ViewHolder) {
        super.onViewRecycled(vh)
        when(vh) {
            is BodyViewHolder -> { vh.itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = FrameLayout.LayoutParams.MATCH_PARENT } }
            is IntentViewHolder -> { vh.itemView.updateLayoutParams<ViewGroup.LayoutParams> { height = FrameLayout.LayoutParams.WRAP_CONTENT } }
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
        when(vh) {
            is BodyViewHolder -> vh.onBind(position)
            is IntentViewHolder -> vh.onBind(position)
        }
    }

    private fun getItem(@IntRange(from = 0) position: Int) = mDiffer.currentList[position]

    fun getCurrentList() = mDiffer.currentList

    fun submitList(pages: MutableList<Any>) = mDiffer.submitList(pages)
}