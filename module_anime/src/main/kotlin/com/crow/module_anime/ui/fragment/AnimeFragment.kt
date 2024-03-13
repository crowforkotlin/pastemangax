package com.crow.module_anime.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isEmpty
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import com.crow.base.app.app
import com.crow.mangax.copymanga.BaseLoadStateAdapter
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.BaseStrings.ID
import com.crow.mangax.copymanga.BaseStrings.URL.HotManga
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.appEvent
import com.crow.mangax.copymanga.entity.CatlogConfig.mDarkMode
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_100L
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutInVisibility
import com.crow.base.tools.extensions.animateFadeOutGone
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.info
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.px2sp
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.toast
import com.crow.base.tools.extensions.withLifecycle
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.BaseErrorViewStub
import com.crow.base.ui.view.baseErrorViewStub
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.view.event.BaseEventEntity
import com.crow.base.ui.view.event.click.BaseIEventIntervalExt
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_anime.R
import com.crow.module_anime.databinding.AnimeDiscoverMoreLayoutBinding
import com.crow.module_anime.databinding.AnimeFragmentBinding
import com.crow.module_anime.databinding.AnimeFragmentSearchViewBinding
import com.crow.module_anime.databinding.AnimeLayoutSiteBinding
import com.crow.module_anime.databinding.AnimeTipsTokenLayoutBinding
import com.crow.module_anime.model.intent.AnimeIntent
import com.crow.module_anime.ui.adapter.AnimeDiscoverPageAdapter
import com.crow.module_anime.ui.adapter.AnimeSearchPageAdapter
import com.crow.module_anime.ui.adapter.AnimeSiteRvAdapter
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
import kotlin.system.exitProcess
import com.crow.mangax.R as mangaR
import com.crow.base.R as baseR

class AnimeFragment : BaseMviFragment<AnimeFragmentBinding>() {

    /**
     * ⦁ Static Area
     *
     * ⦁ 2023-10-10 01:01:13 周二 上午
     */
    companion object { const val ANIME = "ANIME" }

    /**
     * ⦁ Anime ViewModel
     *
     * ⦁ 2023-10-10 01:01:05 周二 上午
     */
    private val mVM by viewModel<AnimeViewModel>()

    /**
     * ⦁ Discover Page Rv Adapter
     *
     * ⦁ 2023-10-10 01:00:55 周二 上午
     */
    private val mAdapter by lazy {
        AnimeDiscoverPageAdapter(lifecycleScope) { anime ->
            navigateAnimeInfoPage(anime.mPathWord, anime.mName)
        }
    }

    /**
     * ⦁ Search Page Rv Adapter
     *
     * ⦁ 2023-12-06 21:15:18 周三 下午
     * @author crowforkotlin
     */
    private val mSearchAdapter by lazy {
        AnimeSearchPageAdapter { anime ->
           navigateAnimeInfoPage(anime.mPathWord, anime.mName)
        }
    }

    /**
     * ⦁ SearchBinding
     *
     * ⦁ 2023-12-06 21:21:52 周三 下午
     * @author crowforkotlin
     */
    private var mSearchBinding: AnimeFragmentSearchViewBinding? = null

    /**
     * ⦁ subtitle textview
     *
     * ⦁ 2023-10-01 21:59:31 周日 下午
     */
    private var mToolbarSubtitle: TextView? = null

    /**
     * ⦁ 子标题
     *
     * ⦁ 2023-10-10 02:20:34 周二 上午
     */
    private var  mSubtitle: String by Delegates.observable(app.applicationContext.getString(mangaR.string.mangax_all)) { _, _, new -> mBinding.topbar.subtitle = new }

    /**
     * ⦁ 提示窗口VB
     *
     * ⦁ 2023-10-15 02:22:36 周日 上午
     */
    private var mTipDialog: AlertDialog? = null

    /**
     * ⦁ 是否取消token提示窗口
     *
     * ⦁ 2023-10-15 02:48:32 周日 上午
     */
    private var mIsCancelTokenDialog: Boolean = false

    /**
     * ⦁ BaseViewStub
     *
     * ⦁ 2023-10-29 20:59:42 周日 下午
     * @author crowforkotlin
     */
    private var mBaseErrorViewStub by BaseNotNullVar<BaseErrorViewStub>(true)

    /**
     * ⦁ 站点VB
     *
     * ⦁ 2023-11-11 13:20:16 周六 下午
     * @author crowforkotlin
     */
    private var mSiteBinding: AnimeLayoutSiteBinding? = null

    /**
     * ⦁ 站点窗口
     *
     * ⦁ 2023-11-11 13:20:23 周六 下午
     * @author crowforkotlin
     */
    private var mSiteDialog: AlertDialog? =null

    /**
     * ⦁ 番剧站点列表加载任务
     *
     * ⦁ 2023-12-03 18:28:15 周日 下午
     * @author crowforkotlin
     */
    private var mAnimeSiteJob: Job? = null

    /**
     * ⦁ 获取VB
     *
     * ⦁ 2023-10-10 01:01:31 周二 上午
     */
    override fun getViewBinding(inflater: LayoutInflater) = AnimeFragmentBinding.inflate(layoutInflater)

    /**
     * ⦁ Lifecycle onDestroyView
     *
     * ⦁ 2023-12-09 17:41:07 周六 下午
     * @author crowforkotlin
     */
    override fun onDestroyView() {
        super.onDestroyView()
        mSiteBinding = null
        mSiteBinding = null
        mSiteDialog = null
        mTipDialog = null
    }

    /**
     * ⦁ 初始化视图
     *
     * ⦁ 2023-10-10 01:01:42 周二 上午
     */
    override fun initView(savedInstanceState: Bundle?) {

        if (savedInstanceState != null) {
            withLifecycle(state = Lifecycle.State.RESUMED) {
                if (mBinding.searchView.isShowing) {
                    loadSearchView()
                }
            }
        }

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
     * ⦁ 初始化监听器
     *
     * ⦁ 2023-10-10 01:01:55 周二 上午
     */
    override fun initListener() {

        mBinding.list.setOnScrollChangeListener { _, _, _, _, _ ->
            if (mBinding.topbar.isOverflowMenuShowing) { mBinding.list.stopScroll() }
        }

        // 设置容器Fragment的回调监听
        parentFragmentManager.setFragmentResultListener(ANIME, this) { _, bundle ->
            if (bundle.getInt(ID) == 3) {
                mBinding.refresh.autoRefreshAnimationOnly()
                mBinding.refresh.finishRefresh((BASE_ANIM_300L.toInt() shl 1) or 0xFF)
                if (bundle.getBoolean(BaseStrings.ENABLE_DELAY)) {
                    launchDelay(BASE_ANIM_200L) { onCollectState() }
                } else {
                    onCollectState()
                }
            }
        }

        // 返回事件回调
        parentFragmentManager.setFragmentResultListener(BaseStrings.BACKPRESS + 3, this) { _, _ ->
            if (mBinding.searchView.isShowing) mBinding.searchView.hide()
            else {
                appEvent.doOnInterval(object : BaseIEventIntervalExt<BaseEvent>{
                   override fun onIntervalOk(baseEventEntity: BaseEventEntity<BaseEvent>) { toast(getString(baseR.string.base_exit_app)) }
                   override fun onIntervalFailure(gapTime: Long) {
                       requireActivity().finish()
                       exitProcess(0)
                   }
               })
            }
        }

        // 刷新监听
        mBinding.refresh.setOnRefreshListener { mAdapter.refresh() }

        // Appbar
        mBinding.topbar.apply {

            if (menu.isEmpty()) {

                // 先清空
                menu.clear()

                // 加载布局
                inflateMenu(R.menu.anime_menu)

                // 年份
                menu[0].doOnClickInterval { onSelectMenu(R.string.anime_year) }

                // 搜索
                menu[1].doOnClickInterval { loadSearchView() }

                BaseEvent.getSIngleInstance().apply {

                    // 更新时间
                    menu[2].doOnClickInterval {
                        if (getBoolean("ANIME_FRAGMENT_UPDATE_ORDER") == true) {
                            setBoolean("ANIME_FRAGMENT_UPDATE_ORDER", false)
                            mVM.setOrder("-datetime_updated")
                        } else {
                            setBoolean("ANIME_FRAGMENT_UPDATE_ORDER", true)
                            mVM.setOrder("datetime_updated")
                        }
                        updateAnime()
                    }

                    // 热度
                    menu[3].doOnClickInterval {
                        if (getBoolean("ANIME_FRAGMENT_POPULAR_ORDER") == true) {
                            setBoolean("ANIME_FRAGMENT_POPULAR_ORDER", false)
                            mVM.setOrder("-popular")
                        } else {
                            setBoolean("ANIME_FRAGMENT_POPULAR_ORDER", true)
                            mVM.setOrder("popular")
                        }
                        updateAnime()
                    }

                    // 设置站点
                    menu[4].doOnClickInterval { onSiteClick() }
                }

                // 设置副标题
                if (subtitle.isNullOrEmpty()) { mSubtitle = getString(mangaR.string.mangax_all) }

                // subtitle textview
                mToolbarSubtitle = this::class.java.superclass.getDeclaredField("mSubtitleTextView").run {
                    isAccessible = true
                    get(this@apply) as TextView
                }
                mToolbarSubtitle?.typeface = Typeface.DEFAULT_BOLD
            }
        }
    }

    /**
     * ⦁ 初始化观察者
     *
     * ⦁ 2023-10-14 23:49:23 周六 下
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
                                mBinding.list.animateFadeOutInVisibility()
                            }

                            if (mBaseErrorViewStub.isGone()) toast(getString(baseR.string.base_loading_error_need_refresh))
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
                            if (MangaXAccountConfig.mHotMangaToken.isNotEmpty()) {
                                mTipDialog?.let{  dialog ->
                                    dialog.cancel()
                                    toast(getString(R.string.anime_token_ok))
                                }
                                mTipDialog = null
                            }
                        }
                }
                is AnimeIntent.AnimeSiteIntent -> {
                    intent.mViewState
                        .doOnLoading {
                            mSiteBinding?.let { binding ->
                                if (binding.loading?.isGone == true) {
                                    binding.loading.animateFadeIn()
                                }
                            }
                        }
                        .doOnError { _, _ ->
                           mSiteBinding?.let { binding ->
                               if (binding.loading?.isVisible == true) {
                                   binding.loading.animateFadeOutGone()
                                   binding.list.animateFadeOut()
                                   binding.reload.animateFadeIn()
                               }
                           }
                        }
                        .doOnResult {
                            mSiteDialog?.let {  dialog ->
                                mSiteBinding?.let { binding ->
                                    val sites = (intent.siteResp?.mApi?.flatMap { it.map {  site -> site } } ?: return@doOnResult).toMutableList()
                                    if (binding.list.isInvisible) {
                                        binding.list.animateFadeIn()
                                    }
                                    binding.list.adapter = AnimeSiteRvAdapter { _, site ->
                                        BaseStrings.URL.setHotMangaUrl(site)
                                        dialog.cancel()
                                    }
                                        .also { adapter ->
                                            mAnimeSiteJob = lifecycleScope.launch {
                                                binding.loading?.isGone = true
                                                adapter.doNotify(sites, BASE_ANIM_100L shl 1)
                                            }
                                        }
                                }
                            }
                        }
                }
                is AnimeIntent.AnimeSearchIntent -> {
                    mSearchBinding?.apply {
                        intent.mViewState
                            .doOnLoading { root.autoRefresh() }
                            .doOnSuccess {
                                lifecycleScope.launch {
                                    delay(BASE_ANIM_300L shl 1)
                                    root.finishRefresh()
                                    BaseEvent.getSIngleInstance().setBoolean("ANIME_FRAGMENT_SEARCH_FLAG", false)
                                }
                                if (mSearchAdapter.itemCount == 0) tips.animateFadeIn() else if (tips.isVisible) tips.animateFadeOutGone()
                            }
                            .doOnResult {
                                if (tips.isVisible) tips.animateFadeOutGone()
                                if (list.isInvisible) list.animateFadeIn()
                            }
                    }
                }
            }
        }
    }

    /**
     * ⦁ 点击站点
     *
     * ⦁ 2023-11-11 13:06:59 周六 下午
     * @author crowforkotlin
     */
    private fun onSiteClick() {
        lifecycleScope.launch {
            mSiteBinding = AnimeLayoutSiteBinding.inflate(layoutInflater)
            mSiteBinding?.apply {
                val siteList: List<String> = mVM.getSiteList()
                val currentSite: String = Base64.encodeToString(HotManga.substring(8, HotManga.length).toByteArray(), Base64.NO_WRAP)
                siteList.onEachIndexed { index, site ->
                    if (currentSite.contentEquals(site)) {
                        when(index) {
                            0 -> siteMain.isChecked = true
                            1 -> siteOne.isChecked = true
                            2 -> siteTwo.isChecked = true
                            3 -> siteThree.isChecked = true
                            4 -> siteFour.isChecked = true
                            5 -> siteFive.isChecked = true
                            6 -> siteSix.isChecked = true
                        }
                    }
                }

                mVM.input(AnimeIntent.AnimeSiteIntent())

                close.doOnClickInterval { mSiteDialog?.cancel() }
                reload.doOnClickInterval {
                    reload.animateFadeOut()
                    mVM.input(AnimeIntent.AnimeSiteIntent())
                }
                mSiteDialog = mContext.newMaterialDialog {
                    it.setView(root)
                    it.setOnCancelListener {
                        mAnimeSiteJob?.cancel()
                        mAnimeSiteJob = null
                        mSiteDialog = null
                        mSiteBinding = null
                    }
                }

                val config = mVM.getReadedAppConfig() ?: return@launch run {
                    toast(getString(mangaR.string.mangax_unknow_error))
                    mSiteDialog?.cancel()
                }
                staticGroup.setOnCheckedChangeListener { _, checkedId ->
                        when(checkedId) {
                            R.id.site_main -> { BaseStrings.URL.setHotMangaUrl(mVM.getSite(0) ?: return@setOnCheckedChangeListener) }
                            R.id.site_one -> { BaseStrings.URL.setHotMangaUrl(mVM.getSite(1) ?: return@setOnCheckedChangeListener) }
                            R.id.site_two -> { BaseStrings.URL.setHotMangaUrl(mVM.getSite(2) ?: return@setOnCheckedChangeListener) }
                            R.id.site_three -> { BaseStrings.URL.setHotMangaUrl(mVM.getSite(3) ?: return@setOnCheckedChangeListener) }
                            R.id.site_four -> { BaseStrings.URL.setHotMangaUrl(mVM.getSite(4) ?: return@setOnCheckedChangeListener) }
                            R.id.site_five -> { BaseStrings.URL.setHotMangaUrl(mVM.getSite(5) ?: return@setOnCheckedChangeListener) }
                            R.id.site_six -> { BaseStrings.URL.setHotMangaUrl(mVM.getSite(6) ?: return@setOnCheckedChangeListener) }
                        }
                        mVM.saveAppConfig(config.copy(mHotMangaSite = HotManga))
                        mSiteDialog?.cancel()
                    }
            }
        }
    }

    /**
     * ⦁ 请求失败重试
     *
     * ⦁ 2023-10-15 02:55:35 周日 上午
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
     * ⦁ Flow 收集状态
     *
     * ⦁ 2023-10-10 01:32:20 周二 上午
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
     * ⦁ 更新漫画
     *
     * ⦁ 2023-10-10 01:32:34 周二 上午
     */
    private fun updateAnime() {
        viewLifecycleOwner.lifecycleScope.launch {
            mAdapter.submitData(PagingData.empty())
            mVM.input(AnimeIntent.DiscoverPageIntent())
            onCollectState()
        }
    }

    /**
     * ⦁ 选择菜单
     *
     * ⦁ 2023-10-11 23:10:54 周三 下午
     */
    private fun onSelectMenu(type: Int) {

        mBinding.list.stopScroll()

        AnimeDiscoverMoreLayoutBinding.inflate(layoutInflater).apply {

            var job: Job? = null
            val chipTextSize = mContext.px2sp(resources.getDimension(baseR.dimen.base_sp12_5))
            val dialog = mContext.newMaterialDialog {
                it.setTitle(getString(type))
                it.setView(root)
                it.setOnDismissListener { job?.cancel() }
            }
            when(type) {
                R.string.anime_year -> {
                    job = viewLifecycleOwner.lifecycleScope.launch {
                        val year = Calendar.getInstance().get(Calendar.YEAR)
                        repeat(22) {
                            val newYear = when (it) {
                                0 -> getString(mangaR.string.mangax_all)
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
                                mVM.setYear(if (newYear.toString() == getString(mangaR.string.mangax_all)) "" else newYear.toString())
                                updateAnime()
                            }
                            moreChipGroup.addView(chip)
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
    }

    /**
     * ⦁ 导航至动漫信息页面
     *
     * ⦁ 2023-10-11 23:13:38 周三 下午
     */
    private fun navigateAnimeInfoPage(pathword: String, name: String) {

        if(checkAccountState()) return

        val tag = Fragments.AnimeInfo.name
        val bundle = Bundle()
        bundle.putSerializable(BaseStrings.PATH_WORD, pathword)
        bundle.putSerializable(BaseStrings.NAME, name)
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(
            id = mangaR.id.app_main_fcv,
            hideTarget = requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
            addedTarget = get<Fragment>(named(tag)).also { it.arguments = bundle },
            tag = tag,
            backStackName = tag
        )
    }

    /**
     * ⦁ 检查番剧账户状态
     *
     * ⦁ 2023-10-14 23:50:46 周六 下午
     */
    private fun checkAccountState(): Boolean {
        val tokenEmpty = MangaXAccountConfig.mHotMangaToken.isEmpty()
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

    /**
     * ⦁ 处理搜索页面收集的结果
     *
     * ⦁ 2023-12-12 00:36:35 周二 上午
     * @author crowforkotlin
     */
    private fun onCollectSearchPage(content: String) {
        BaseEvent.getSIngleInstance().apply {
            if(getBoolean("ANIME_FRAGMENT_SEARCH_FLAG") == true) return
            setBoolean("ANIME_FRAGMENT_SEARCH_FLAG", true)
            mVM.input(AnimeIntent.AnimeSearchIntent(content))
            repeatOnLifecycle {
                mVM.mSearchPageFlow?.collect {
                    mSearchAdapter.submitData(it)
                }
            }
        }
    }

    /**
     * ⦁ 加载搜索视图
     *
     * ⦁ 2023-12-11 22:11:28 周一 下午
     * @author crowforkotlin
     */
    @SuppressLint("RestrictedApi", "PrivateResource")
    private fun loadSearchView() {
        if (mSearchBinding == null) {
            mSearchBinding = AnimeFragmentSearchViewBinding.inflate(layoutInflater)
            mSearchBinding!!.let { binding ->
                binding.list.adapter = mSearchAdapter
                mBinding.searchView.apply {

                    //  搜索刷新
                    binding.root.setOnRefreshListener { onCollectSearchPage(editText.text.toString()) }

                    // 监听EditText 通知对应VP对应页发送意图
                    editText.setOnEditorActionListener { textview, _, event->
                        if (event?.action == MotionEvent.ACTION_DOWN) if (!mBinding.refresh.isRefreshing) onCollectSearchPage(textview.text.toString())
                        false
                    }

                    val bgColor: Int; val tintColor: Int

                    when {
                        mDarkMode -> {
                            tintColor = ContextCompat.getColor(mContext, android.R.color.white)
                            bgColor = ContextCompat.getColor(mContext, com.google.android.material.R.color.m3_sys_color_dark_surface)
                        }
                        else -> {
                            tintColor = ContextCompat.getColor(mContext, android.R.color.black)
                            bgColor = ContextCompat.getColor(mContext, android.R.color.white)
                        }
                    }

                    // 设置SearchView toolbar导航图标
                    toolbar.setNavigationIcon(mangaR.drawable.base_ic_back_24dp)

                    // 设置Navigation 颜色
                    toolbar.navigationIcon?.setTint(tintColor)

                    // 设置SearchView toolbar背景色白，沉浸式
                    toolbar.setBackgroundColor(bgColor)

                    // listFrame设置背景色
                    binding.listFrame.setBackgroundColor(bgColor)

                    // 关闭状态栏空格间距
                    setStatusBarSpacerEnabled(false)
                    addView(binding.root)
                    show()
                }
            }
        } else mBinding.searchView.show()
    }
}