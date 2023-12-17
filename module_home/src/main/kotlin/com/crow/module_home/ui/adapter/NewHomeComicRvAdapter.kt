@file:Suppress("FunctionName", "NonAsciiCharacters")

package com.crow.module_home.ui.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.mangax.copymanga.appComicCardHeight
import com.crow.mangax.copymanga.formatHotValue
import com.crow.mangax.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.mangax.ui.adapter.BaseGlideLoadingViewHolder
import com.crow.base.ui.view.TooltipsView
import com.crow.mangax.copymanga.tryConvert
import com.crow.module_home.databinding.HomeFragmentComicRvBodyBinding
import com.crow.module_home.databinding.HomeFragmentComicRvHeaderBinding
import com.crow.module_home.databinding.HomeFragmentComicRvRecRefreshBinding
import com.crow.module_home.model.entity.HomeHeader
import com.crow.module_home.model.resp.homepage.FinishComic
import com.crow.module_home.model.resp.homepage.HotComic
import com.crow.module_home.model.resp.homepage.NewComic
import com.crow.module_home.model.resp.homepage.Topices
import com.crow.module_home.model.resp.homepage.results.AuthorResult
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/adapter
 * @Time: 2023/9/16 2:50
 * @Author: CrowForKotlin
 * @Description: HomeFragment Rv Adapter
 * @formatter:on
 **************************/

class NewHomeComicRvAdapter(
    private val mLifecycleScope: LifecycleCoroutineScope,
    private val mOnRefresh: (MaterialButton) -> Unit,
    private val mOnClick: (name: String, pathword: String) -> Unit,
    private val mOnTopic: (Topices) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * ● 静态区
     *
     * ● 2023-09-17 19:33:07 周日 下午
     */
    companion object {
        const val HEADER = 0
        const val REFRESH = 1
        const val BODY = 2
        const val TOPIC = 3

        const val REC: Byte = 4
        const val HOT: Byte = 5
        const val NEW: Byte = 6
        const val FINISH: Byte = 7
        const val RANK: Byte = 8
    }

    /**
     * ● HomeData
     *
     * ● 2023-09-17 19:35:13 周日 下午
     */
    private var mData: MutableList<Any> = mutableListOf()

    /**
     * ● Coroutine lock
     *
     * ● 2023-09-17 19:35:50 周日 下午
     */
    private val mMutex = Mutex()

    /**
     * ● Home Header
     *
     * ● 2023-09-17 19:36:05 周日 下午
     */
    inner class HomeComicHeaderVH(val binding: HomeFragmentComicRvHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: HomeHeader) {
            binding.homeComicButtonTitle.text = item.mText
            binding.homeComicButtonTitle.icon = ContextCompat.getDrawable(binding.root.context, item.mResource)
        }
    }

    /**
     * ● Home Comic Card
     *
     * ● 2023-09-17 19:36:15 周日 下午
     */
    inner class HomeComicBodyVH(binding: HomeFragmentComicRvBodyBinding) : BaseGlideLoadingViewHolder<HomeFragmentComicRvBodyBinding>(binding) {
        init {
            // 漫画卡片高度
            binding.image.layoutParams.height = appComicCardHeight

            // 点击 父布局卡片 以及漫画卡片 事件 回调给上级 NewHomeFragment --> ContainerFragment
            binding.root.doOnClickInterval { onClick(getItem(absoluteAdapterPosition)) }
            binding.homeBookCardView.doOnClickInterval { onClick(getItem(absoluteAdapterPosition)) }

            // Tooltips漫画名称设置
            TooltipsView.showTipsWhenLongClick(binding.name)
        }

        fun onBind(item: Any) {
            // 作者 ：Crow
            binding.author.isGone = !binding.author.isVisible
            when(item) {
                is RecComicsResult -> { initView(item.mComic.mName, item.mComic.mImageUrl, item.mComic.mAuthorResult, item.mComic.mPopular, null) }
                is HotComic -> { initView(item.mComic.mName, item.mComic.mImageUrl, item.mComic.mAuthorResult, item.mComic.mPopular, item.mComic.mLastChapterName) }
                is NewComic -> { initView(item.mComic.mName, item.mComic.mImageUrl, item.mComic.mAuthorResult, item.mComic.mPopular, item.mComic.mLastChapterName) }
                is FinishComic -> { initView(item.mName, item.mImageUrl, item.mAuthorResult, item.mPopular, null) }
                is Topices -> {
                    Glide.with(itemView).load(item.mImageUrl).into(binding.image)
                    binding.author.isVisible = true
                    binding.author.text = item.mDatetimeCreated
                    mLifecycleScope.tryConvert(item.mTitle, binding.name::setText)
                }
                else -> error("parse unknow item type!")
            }
        }

        private fun onClick(item: Any) {
            when (item) {
                is RecComicsResult -> {
                    mOnClick(item.mComic.mName, item.mComic.mPathWord)
                }
                is HotComic -> {
                    mOnClick(item.mComic.mName, item.mComic.mPathWord)
                }
                is NewComic -> {
                    mOnClick(item.mComic.mName, item.mComic.mPathWord)
                }
                is FinishComic -> {
                    mOnClick(item.mName, item.mPathWord)
                }
                is Topices -> {
                    mOnTopic(item)
                }
                else -> error("parse unknow item type!")
            }
        }

        // 初始化卡片内部视图
        private fun initView(name: String, imageUrl: String, author: List<AuthorResult>, hot: Int, lastestChapter: String?) {

            binding.loading.isVisible = true
            binding.loadingText.isVisible = true
            binding.loadingText.text = AppGlideProgressFactory.PERCENT_0
            mAppGlideProgressFactory?.onRemoveListener()?.onCleanCache()
            mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(imageUrl) { _, _, percentage, _, _ -> binding.loadingText.text = AppGlideProgressFactory.getProgressString(percentage) }

            // 加载封面
            Glide.with(itemView)
                .load(imageUrl)
                .addListener(mAppGlideProgressFactory?.getRequestListener())
                .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                    if (dataSource == com.bumptech.glide.load.DataSource.REMOTE) {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                    } else {
                        binding.loading.isInvisible = true
                        binding.loadingText.isInvisible = true
                        NoTransition()
                    }
                })
                .into(binding.image)

            // 漫画名
            mLifecycleScope.tryConvert(name, binding.name::setText)

            // 热度 ： 12.3456 W
            binding.hot.text = formatHotValue(hot)

            // 作者 ：Crow
            binding.author.text = if (binding.author.isVisible) author.joinToString { it.name } else null

            // 最新章节
            if (lastestChapter == null) binding.lastestChapter.isVisible = false
            else {
                binding.lastestChapter.isVisible = true
                binding.lastestChapter.text = lastestChapter
            }
        }
    }

    /**
     * ● Home Refresh Button For Recommand Comic
     *
     * ● 2023-09-17 19:36:24 周日 下午
     */
    inner class HomeComicRecRefreshVH(val binding: HomeFragmentComicRvRecRefreshBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind() {
            binding.homeComicRvRecRefresh.doOnClickInterval { mOnRefresh(binding.homeComicRvRecRefresh) }
        }
    }

    /**
     * ● HomeData Size
     *
     * ● 2023-09-17 19:36:46 周日 下午
     */
    override fun getItemCount(): Int = mData.size

    /**
     * ● Reuse ViewHolder
     *
     * ● 2023-09-17 19:36:57 周日 下午
     */
    override fun onBindViewHolder(vh: RecyclerView.ViewHolder, pos: Int) {
        val item = getItem(pos)
        when(vh) {
            is HomeComicHeaderVH -> vh.onBind(item as HomeHeader)
            is HomeComicBodyVH -> vh.onBind(item)
            is HomeComicRecRefreshVH -> vh.onBind()
        }
    }

    /**
     * ● Set different content depending on the ViewType
     *
     * ● 2023-09-17 19:37:21 周日 下午
     */
    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is HomeHeader -> HEADER
            is Unit -> REFRESH
            is Topices -> TOPIC
            else -> BODY
        }
    }

    /**
     * ● Create ViewHolder
     *
     * ● 2023-09-17 19:38:15 周日 下午
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> HomeComicHeaderVH(HomeFragmentComicRvHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            BODY -> HomeComicBodyVH(HomeFragmentComicRvBodyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            TOPIC -> HomeComicBodyVH(HomeFragmentComicRvBodyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            REFRESH -> HomeComicRecRefreshVH(HomeFragmentComicRvRecRefreshBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> error("unknow view type!")
        }
    }

    /**
     * ● Get home data
     *
     * ● 2023-09-17 19:38:26 周日 下午
     */
    private fun getItem(@IntRange(from = 0) position: Int) = mData[position]

    /**
     * ● Submit homeData to rv
     *
     * ● 2023-09-17 19:38:37 周日 下午
     */
    suspend fun submitList(homeData: MutableList<Any>, duration: Long) {
        mMutex.withLock {
            val isCountSame = itemCount == homeData.size
            if (isCountSame) mData = homeData
            else if (itemCount != 0) {
                notifyItemRangeRemoved(0, itemCount)
                mData.clear()
                delay(BASE_ANIM_200L)
            }
            homeData.forEachIndexed { index, data ->
                if (!isCountSame) {
                    mData.add(data)
                    notifyItemInserted(index)
                } else {
                    notifyItemChanged(index)
                }
                delay(duration)
            }
        }
    }

    /**
     * ● Refresh
     *
     * ● 2023-09-17 18:57:22 周日 下午
     */
    suspend fun onRefreshSubmitList(homeData: MutableList<Any>, duration: Long) {
        mMutex.withLock {
            if (itemCount == 0) return
            repeat(3) {
                val index = it + 1
                mData[index] = homeData[index]
                notifyItemChanged(index)
                delay(duration)
            }
        }
    }
}