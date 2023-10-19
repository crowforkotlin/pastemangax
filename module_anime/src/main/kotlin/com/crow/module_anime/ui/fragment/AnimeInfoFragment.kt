package com.crow.module_anime.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.NoTransition
import com.crow.base.app.app
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUserConfig
import com.crow.base.copymanga.appComicCardHeight
import com.crow.base.copymanga.appComicCardWidth
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.copymanga.formatValue
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisible
import com.crow.base.tools.extensions.popSyncWithClear
import com.crow.base.tools.extensions.px2dp
import com.crow.base.tools.extensions.px2sp
import com.crow.base.tools.extensions.removeWhiteSpace
import com.crow.base.tools.extensions.startActivity
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_anime.R
import com.crow.module_anime.databinding.AnimeFragmentInfoBinding
import com.crow.module_anime.model.intent.AnimeIntent
import com.crow.module_anime.model.resp.chapter.AnimeChapterResp
import com.crow.module_anime.model.resp.chapter.AnimeChapterResult
import com.crow.module_anime.model.resp.info.AnimeInfoResp
import com.crow.module_anime.ui.activity.AnimeActivity
import com.crow.module_anime.ui.adapter.AnimeChapterRvAdapter
import com.crow.module_anime.ui.viewmodel.AnimeViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR

/**
 * ● 动漫信息页面
 * ● @author : crowforkotlin
 * ● 2023-10-11 22:59:51 周三 下午
 */
class AnimeInfoFragment : BaseMviFragment<AnimeFragmentInfoBinding>() {

    /**
     * ● Anime ViewModel
     *
     * ● 2023-10-10 01:01:05 周二 上午
     */
    private val mVM by viewModel<AnimeViewModel>()

    /**
     * ● 路径
     *
     * ● 2023-10-12 00:47:19 周四 上午
     */
    private val mPathword: String by lazy {
        arguments?.getString(BaseStrings.PATH_WORD) ?: run {
            toast(getString(com.crow.base.R.string.BaseUnknowError))
            navigateUp()
            ""
        }
    }

    /**
     * ● 漫画点击实体
     *
     * ● 2023-10-12 00:47:19 周四 上午
     */
    private val mName: String by lazy {
        arguments?.getString(BaseStrings.NAME) ?: run {
            toast(getString(com.crow.base.R.string.BaseUnknowError))
            navigateUp()
            ""
        }
    }

    /**
     * ● Glide进度加载工厂
     *
     * ● 2023-10-12 01:11:44 周四 上午
     */
    private var mAppGlideProgressFactory: AppGlideProgressFactory? = null

    /**
     * ● 章节适配器
     *
     * ● 2023-10-12 02:49:48 周四 上午
     */
    private val mAdapter by lazy {
        AnimeChapterRvAdapter { pos, chapter ->
            if (BaseUserConfig.HOTMANGA_TOKEN.isEmpty()) {
                toast(getString(R.string.anime_token_error))
                return@AnimeChapterRvAdapter
            }
            launchAnimeActivity(pos, chapter)
        }
    }

    /**
     * ● 获取VB
     *
     * ● 2023-10-12 00:47:26 周四 上午
     */
    override fun getViewBinding(inflater: LayoutInflater) = AnimeFragmentInfoBinding.inflate(layoutInflater)

    /**
     * ● Lifecycle onStart
     *
     * ● 2023-10-11 23:26:52 周三 下午
     */
    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) { navigateUp() }
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-10-11 23:27:06 周三 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        // 设置 内边距属性 实现沉浸式效果
        immersionRoot()

        // 设置 漫画图的卡片 宽高
        mBinding.cardview.layoutParams.height = appComicCardHeight
        mBinding.cardview.layoutParams.width = appComicCardWidth

        // 设置刷新时不允许列表滚动
        mBinding.refresh.setDisableContentWhenRefresh(true)

        // 初始化Adapter
        mBinding.list.adapter = mAdapter

        val more = ". . ."
        mBinding.title.text = mName
        mBinding.desc.text =  more
        mBinding.company.text = getString(R.string.anime_company, more)
        mBinding.hot.text = getString(R.string.anime_hot, more)
        mBinding.showTime.text = getString(R.string.anime_show_time, more)
        mBinding.update.text = getString(R.string.anime_update_time, more)
        mBinding.newChapter.text = getString(R.string.anime_new_chapter, more)
    }

    /**
     * ● 初始化数据
     *
     * ● 2023-10-12 01:07:10 周四 上午
     */
    override fun initData(savedInstanceState: Bundle?) {

        mVM.input(AnimeIntent.PageInfoIntent(mPathword))
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-10-12 01:22:41 周四 上午
     */
    override fun initListener() {
        mBinding.refresh.setOnRefreshListener {
            mVM.input(AnimeIntent.PageInfoIntent(mPathword))
            mVM.input(AnimeIntent.ChapterListIntent(mPathword))
        }
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-10-12 01:07:37 周四 上午
     */
    override fun initObserver(saveInstanceState: Bundle?) {
        mVM.onOutput { intent ->
            when(intent) {
                is AnimeIntent.PageInfoIntent -> {
                    intent.mViewState

                        .doOnSuccess { if (mBinding.refresh.isRefreshing) mBinding.refresh.finishRefresh() }

                        // 发生错误 取消动画 退出界面 提示
                        .doOnError { _, _ -> toast(getString(baseR.string.BaseLoadingError)) }

                        // 显示书页内容 根据意图类型 再次发送获取章节意图的请求
                        .doOnResult {
                            loadAnimInfoPage(intent.info ?: return@doOnResult)
                            mVM.input(AnimeIntent.ChapterListIntent(intent.pathword))
                        }
                }
                is AnimeIntent.ChapterListIntent -> {
                    intent.mViewState
                        .doOnError { _, _ ->
                            if (mBinding.linear.isVisible) mBinding.linear.animateFadeOutWithEndInVisibility()
                            if (mBinding.tips.isGone) mBinding.tips.animateFadeIn()
                        }
                        .doOnResult {
                            loadAnimeChapterList(intent.chapters ?: return@doOnResult)
                        }

                }
            }
        }
    }

    /**
     * ● 加载动漫章节列表
     *
     * ● 2023-10-12 02:46:44 周四 上午
     */
    private fun loadAnimeChapterList(chapters: AnimeChapterResp) {

        if (mBinding.linear.isInvisible) mBinding.linear.animateFadeIn()
        if (mBinding.tips.isVisible) mBinding.tips.animateFadeOutWithEndInVisible()

        viewLifecycleOwner.lifecycleScope.launch {
            mAdapter.doNotify(chapters.mList.toMutableList())
        }
    }

    /**
     * ● 显示漫画信息页面
     *
     * ● 2023-06-15 23:00:25 周四 下午
     */
    private fun loadAnimInfoPage(info: AnimeInfoResp) {

        val anim = info.mCartoon

        mAppGlideProgressFactory = AppGlideProgressFactory.createGlideProgressListener(anim.mCover) { _, _, percentage, _, _ -> mBinding.loadingText.text = AppGlideProgressFactory.getProgressString(percentage) }

        Glide.with(this)
            .load(anim.mCover)
            .addListener(mAppGlideProgressFactory?.getRequestListener())
            .transition(GenericTransitionOptions<Drawable>().transition { dataSource, _ ->
                if (dataSource == DataSource.REMOTE) {
                    mBinding.loading.isInvisible = true
                    mBinding.loadingText.isInvisible = true
                    DrawableCrossFadeTransition(BASE_ANIM_200L.toInt(), true)
                } else {
                    mBinding.loading.isInvisible = true
                    mBinding.loadingText.isInvisible = true
                    NoTransition()
                }
            })
            .into(mBinding.image)

        mBinding.company.text = getString(R.string.anime_company, anim.mCompany.mName)
        mBinding.hot.text = getString(R.string.anime_hot, formatValue(anim.mPopular))
        mBinding.showTime.text = getString(R.string.anime_show_time, anim.mYears)
        mBinding.update.text = getString(R.string.anime_update_time, anim.mDatetimeUpdated)
        mBinding.newChapter.text = getString(R.string.anime_new_chapter, anim.mLastChapter.mName)
        mBinding.title.text = anim.mName
        mBinding.desc.text = anim.mBrief.removeWhiteSpace()
        mBinding.chipGroup.removeAllViews()

        anim.mTheme.forEach { theme ->
            mBinding.chipGroup.addView(Chip(mContext).also {
                it.text = theme.mName
                it.textSize = app.px2sp(resources.getDimension(baseR.dimen.base_sp12_5))
                it.chipStrokeWidth = app.px2dp(resources.getDimension(baseR.dimen.base_dp1))
                it.isClickable = false
            })
        }
    }

    /**
     * ● 返回上一个界面
     *
     * ● 2023-10-11 23:27:17 周三 下午
     */
    private fun navigateUp() { parentFragmentManager.popSyncWithClear(Fragments.AnimeInfo.name) }

    private fun launchAnimeActivity(pos: Int, chapter: AnimeChapterResult) {

        when(chapter.mLines.size) {
            0 ->toast(getString(R.string.anime_play_no_line))
            else -> {
                mContext.startActivity<AnimeActivity> {
                    putExtra(BaseStrings.NAME, chapter.mName)
                    putExtra(BaseStrings.PATH_WORD, mPathword)
                    putExtra(AnimeActivity.ANIME_CHAPTER_UUIDS, toJson(mAdapter.getUUIDS()))
                    putExtra(AnimeActivity.ANIME_CHAPTER_UUID_POSITION, pos)
                }
            }
        }

    }
}