package com.crow.module_anime.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import com.crow.base.app.app
import com.crow.base.copymanga.BaseLoadStateAdapter
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUserConfig
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.px2sp
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.BaseErrorViewStub
import com.crow.base.ui.view.baseErrorViewStub
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_anime.R
import com.crow.module_anime.databinding.AnimeDiscoverMoreLayoutBinding
import com.crow.module_anime.databinding.AnimeFragmentBinding
import com.crow.module_anime.databinding.AnimeTipsTokenLayoutBinding
import com.crow.module_anime.model.intent.AnimeIntent
import com.crow.module_anime.ui.adapter.AnimeDiscoverPageAdapter
import com.crow.module_anime.ui.viewmodel.AnimeViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import java.util.Calendar
import kotlin.properties.Delegates
import com.crow.base.R as baseR

class AnimeFragment : BaseMviFragment<AnimeFragmentBinding>() {


    /**
     * ● Static Area
     *
     * ● 2023-10-10 01:01:13 周二 上午
     */
    companion object { const val ANIME = "ANIME" }

    /**
     * ● Anime ViewModel
     *
     * ● 2023-10-10 01:01:05 周二 上午
     */
    private val mVM by viewModel<AnimeViewModel>()

    /**
     * ● Discover Page Rv Adapter
     *
     * ● 2023-10-10 01:00:55 周二 上午
     */
    private val mAdapter by lazy {
        AnimeDiscoverPageAdapter { anime ->
            navigateAnimeInfoPage(anime.mPathWord, anime.mName)
        }
    }

    /**
     * ● subtitle textview
     *
     * ● 2023-10-01 21:59:31 周日 下午
     */
    private var mToolbarSubtitle: TextView? = null

    /**
     * ● 子标题
     *
     * ● 2023-10-10 02:20:34 周二 上午
     */
    private var  mSubtitle: String by Delegates.observable(app.applicationContext.getString(baseR.string.base_all)) { _, _, new ->
        mBinding.topbar.subtitle = new
    }

    /**
     * ● 提示窗口VB
     *
     * ● 2023-10-15 02:22:36 周日 上午
     */
    private var mTipDialog: AlertDialog? = null

    /**
     * ● 是否取消token提示窗口
     *
     * ● 2023-10-15 02:48:32 周日 上午
     */
    private var mIsCancelTokenDialog: Boolean = false

    /**
     * ● BaseViewStub
     *
     * ● 2023-10-29 20:59:42 周日 下午
     * @author crowforkotlin
     */
    private var mBaseErrorViewStub by BaseNotNullVar<BaseErrorViewStub>(true)


    /**
     * ● 获取VB
     *
     * ● 2023-10-10 01:01:31 周二 上午
     */
    override fun getViewBinding(inflater: LayoutInflater) = AnimeFragmentBinding.inflate(layoutInflater)

    /**
     * ● 初始化视图
     *
     * ● 2023-10-10 01:01:42 周二 上午
     */
    override fun initView(savedInstanceState: Bundle?) {

        // 初始化viewstub
        mBaseErrorViewStub = baseErrorViewStub(mBinding.error, lifecycle) { mBinding.refresh.autoRefresh() }

        // 初始化RV适配器
        mBinding.list.adapter = mAdapter


        // 设置加载动画独占1行，漫画卡片3行
        (mBinding.list.layoutManager as GridLayoutManager).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == mAdapter.itemCount  && mAdapter.itemCount > 0) 3
                    else 1
                }
            }
        }

        // 设置适配器
        mBinding.list.adapter = mAdapter.withLoadStateFooter(BaseLoadStateAdapter { mAdapter.retry() })
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-10-10 01:01:55 周二 上午
     */
    override fun initListener() {

        // 设置容器Fragment的回调监听
        parentFragmentManager.setFragmentResultListener(ANIME, this) { _, bundle ->
            if (bundle.getInt(BaseStrings.ID) == 3) {
                mBinding.refresh.autoRefreshAnimationOnly()
                mBinding.refresh.finishRefresh((BASE_ANIM_300L.toInt() shl 1) or 0xFF)
                if (bundle.getBoolean(BaseStrings.ENABLE_DELAY)) {
                    launchDelay(BASE_ANIM_200L) { onCollectState() }
                } else {
                    onCollectState()
                }
            }
        }

        // 刷新监听
        mBinding.refresh.setOnRefreshListener { mAdapter.refresh() }

        // 漫画加载状态监听
        mAdapter.addLoadStateListener {
            if(it.source.refresh is LoadState.NotLoading) {

                val toolbar = mBinding.topbar

                if (toolbar.menu.isEmpty()) {

                    // 先清空
                    toolbar.menu.clear()

                    // 加载布局
                    toolbar.inflateMenu(R.menu.anime_menu)

                    // 年份
                    toolbar.menu[0].doOnClickInterval { onSelectMenu(R.string.anime_year) }

                    // 类别
                    toolbar.menu[1].doOnClickInterval { onSelectMenu(R.string.anime_search) }

                    // 更新时间

                    val instance = BaseEvent.getSIngleInstance()
                    toolbar.menu[2].doOnClickInterval {
                        if (instance.getBoolean("ANIME_FRAGMENT_UPDATE_ORDER") == true) {
                            instance.setBoolean("ANIME_FRAGMENT_UPDATE_ORDER", false)
                            mVM.setOrder("-datetime_updated")
                        } else {
                            instance.setBoolean("ANIME_FRAGMENT_UPDATE_ORDER", true)
                            mVM.setOrder("datetime_updated")
                        }
                        updateAnime()
                    }

                    // 热度
                    toolbar.menu[3].doOnClickInterval {
                        if (instance.getBoolean("ANIME_FRAGMENT_POPULAR_ORDER") == true) {
                            instance.setBoolean("ANIME_FRAGMENT_POPULAR_ORDER", false)
                            mVM.setOrder("-popular")
                        } else {
                            instance.setBoolean("ANIME_FRAGMENT_POPULAR_ORDER", true)
                            mVM.setOrder("popular")
                        }
                        updateAnime()
                    }

                    if (toolbar.subtitle.isNullOrEmpty()) {
                        mSubtitle = getString(baseR.string.base_all)
                    }

                    // subtitle textview
                    mToolbarSubtitle = toolbar::class.java.superclass.getDeclaredField("mSubtitleTextView").run {
                        isAccessible = true
                        get(toolbar) as TextView
                    }
                }
            }
        }
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-10-14 23:49:23 周六 下午
     */
    override fun initObserver(saveInstanceState: Bundle?) {

        mVM.onOutput { intent ->
            when(intent) {
                is AnimeIntent.DiscoverPageIntent -> {
                    intent.mViewState
                        .doOnSuccess {
                            if (mBinding.refresh.isRefreshing) mBinding.refresh.finishRefresh()
                            if(!mVM.mIsLogin) {
                                mVM.mAccount?.apply { mVM.input(AnimeIntent.LoginIntent(mUsername, mPassword)) }
                            }
                        }
                        .doOnError { _, _ ->
                            if (mAdapter.itemCount == 0) {

                                // 错误提示淡入
                                mBaseErrorViewStub.loadLayout(visible = true, animation = true)

                                // 发现页 “漫画” 淡出
                                mBinding.list.animateFadeOutWithEndInVisibility()
                            }

                            if (mBaseErrorViewStub.isGone()) toast(getString(baseR.string.BaseLoadingErrorNeedRefresh))
                        }
                        .doOnResult {
                            // 错误提示 可见
                            if (mBaseErrorViewStub.isVisible()) {
                                mBaseErrorViewStub.loadLayout(visible = false, animation = false)
                                mBinding.list.animateFadeIn()
                            }
                        }
                }
                is AnimeIntent.RegIntent -> {
                    intent.mViewState
                        .doOnError { _, _ -> onRetryError() }
                        .doOnSuccess { if (mTipDialog?.isShowing == false) mIsCancelTokenDialog = false }
                        .doOnResult {
                            intent.failureResp?.let { onRetryError() }
                            if (intent.user != null) {
                                mVM.input(AnimeIntent.LoginIntent(intent.reg.mUsername, intent.reg.mPassword))
                            }
                        }
                }
                is AnimeIntent.LoginIntent -> {
                    intent.mViewState
                        .doOnError { _, _ -> onRetryError() }
                        .doOnSuccess { if (mTipDialog?.isShowing == false) mIsCancelTokenDialog = false }
                        .doOnResult {
                            if (BaseUserConfig.HOTMANGA_TOKEN.isNotEmpty()) {
                                mTipDialog?.let{  dialog ->
                                    dialog.cancel()
                                    toast(getString(R.string.anime_token_ok))
                                }
                                mTipDialog = null
                            }
                        }
                }
            }
        }
    }


    /**
     * ● 请求失败重试
     *
     * ● 2023-10-15 02:55:35 周日 上午
     */
    private fun onRetryError() {
        if (mTipDialog?.isShowing == true) {
            launchDelay(BaseEvent.BASE_FLAG_TIME_1000) {
                if (mTipDialog?.isShowing == true) {
                    mVM.input(AnimeIntent.RegIntent(mVM.genReg()))
                    toast(getString(R.string.anime_token_retrying))
                } else {
                    toast(getString(R.string.anime_token_retry_tips))
                }
            }
        }
    }

    /**
     * ● Flow 收集状态
     *
     * ● 2023-10-10 01:32:20 周二 上午
     */
    private fun onCollectState() {
        if (mVM.mDiscoverPageFlow == null) {
            mVM.input(AnimeIntent.DiscoverPageIntent())
        }

        repeatOnLifecycle {
            mVM.mDiscoverPageFlow?.collect {
                mAdapter.submitData(it)
            }
        }
    }

    /**
     * ● 更新漫画
     *
     * ● 2023-10-10 01:32:34 周二 上午
     */
    private fun updateAnime() {
        viewLifecycleOwner.lifecycleScope.launch {
            mAdapter.submitData(PagingData.empty())
            mVM.input(AnimeIntent.DiscoverPageIntent())
            onCollectState()
        }
    }

    /**
     * ● 选择菜单
     *
     * ● 2023-10-11 23:10:54 周三 下午
     */
    private fun onSelectMenu(type: Int) {

        mBinding.list.stopScroll()

        val binding = AnimeDiscoverMoreLayoutBinding.inflate(layoutInflater)
        val chipTextSize = mContext.px2sp(resources.getDimension(baseR.dimen.base_sp12_5))
        var job: Job? = null
        val dialog = mContext.newMaterialDialog {
            it.setTitle(getString(type))
            it.setView(binding.root)
            it.setOnDismissListener { job?.cancel() }
        }
        when(type) {
            R.string.anime_year -> {
                job = viewLifecycleOwner.lifecycleScope.launch {
                    val year = Calendar.getInstance().get(Calendar.YEAR)
                    repeat(22) {
                        val newYear = when (it) {
                            0 -> getString(baseR.string.base_all)
                            1 -> year
                            else -> year - (it - 1)
                        }
                        val chip = Chip(mContext)
                        chip.text = newYear.toString()
                        chip.textSize = chipTextSize
                        chip.doOnClickInterval { _ ->
                            dialog.cancel()
                            mSubtitle = newYear.toString()
                            mToolbarSubtitle?.animateFadeIn()
                            mVM.setYear(if (newYear.toString() == getString(baseR.string.base_all)) "" else newYear.toString())
                            updateAnime()
                        }
                        binding.moreChipGroup.addView(chip)
                        delay(16L)
                    }
                }
            }
            R.string.anime_search ->{
                job = viewLifecycleOwner.lifecycleScope.launch {
                }
            }
            else -> error("Unknow menu type!")
        }
    }

    /**
     * ● 导航至动漫信息页面
     *
     * ● 2023-10-11 23:13:38 周三 下午
     */
    private fun navigateAnimeInfoPage(pathword: String, name: String) {

        if(checkAccountState()) return

        val tag = Fragments.AnimeInfo.name
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        bundle.putSerializable(BaseStrings.NAME, name)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(
            id = baseR.id.app_main_fcv,
            hideTarget = requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
            addedTarget = get<Fragment>(named(tag)).also { it.arguments = bundle },
            tag = tag,
            backStackName = tag
        )
    }

    /**
     * ● 检查番剧账户状态
     *
     * ● 2023-10-14 23:50:46 周六 下午
     */
    private fun checkAccountState(): Boolean {
        val tokenEmpty = BaseUserConfig.HOTMANGA_TOKEN.isEmpty()
        if (tokenEmpty) {
            if (mTipDialog == null) {
                val binding= AnimeTipsTokenLayoutBinding.inflate(layoutInflater)
                mTipDialog = mContext.newMaterialDialog { dialog ->
                    dialog.setView(binding.root)
                    dialog.setCancelable(false)
                }
                binding.close.doOnClickInterval {
                    mIsCancelTokenDialog = true
                    mTipDialog?.cancel()
                }
            }
            else { mTipDialog?.show() }
            if (!mIsCancelTokenDialog) { mVM.input(AnimeIntent.RegIntent(mVM.genReg())) }
        }
        return tokenEmpty
    }
}