@file:SuppressWarnings("RestrictedApi")
package com.crow.module_home.ui.fragment

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.GridLayoutManager
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.appIsDarkMode
import com.crow.base.copymanga.entity.Fragments
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.baseCoroutineException
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toast
import com.crow.base.tools.extensions.withLifecycle
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.BaseErrorViewStub
import com.crow.base.ui.view.baseErrorViewStub
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.view.event.BaseEventEntity
import com.crow.base.ui.view.event.click.BaseIEventIntervalExt
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_home.R
import com.crow.module_home.databinding.HomeFragmentNewBinding
import com.crow.module_home.databinding.HomeFragmentSearchViewBinding
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.homepage.Topices
import com.crow.module_home.ui.adapter.NewHomeComicRvAdapter
import com.crow.module_home.ui.adapter.NewHomeVpAdapter
import com.crow.module_home.ui.compose.Banner
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.qualifier.named
import kotlin.system.exitProcess
import com.crow.base.R as baseR

class NewHomeFragment : BaseMviFragment<HomeFragmentNewBinding>() {

    /**
     * ● 静态区
     *
     * ● 2023-09-17 01:28:03 周日 上午
     */
    companion object {
        const val HOME = "Home"
        const val SEARCH_TAG = "INPUT"
    }

    /**
     * ● 主页 VM
     *
     * ● 2023-09-17 01:27:48 周日 上午
     */
    private val mVM by activityViewModel<HomeViewModel>()

    /**
     * ● 推荐 “换一批” 刷新按钮
     *
     * ● 2023-09-17 01:27:01 周日 上午
     */
    private var mRecRefresh: MaterialButton? = null

    /**
     * ● 错误的View
     *
     * ● 2023-10-29 20:59:42 周日 下午
     * @author crowforkotlin
     */
    private var mBaseErrorViewStub by BaseNotNullVar<BaseErrorViewStub>(true)

    /**
     * ● 主页数据量较多， 采用Rv方式
     *
     * ● 2023-09-17 01:26:14 周日 上午
     */
    private val mDataRvAdapter by lazy {
        NewHomeComicRvAdapter(
            mOnRefresh = { button ->
                button.isEnabled = false
                mRecRefresh = button
                mVM.input(HomeIntent.GetRecPageByRefresh())
            },
            mOnClick = { name, pathword -> navigateBookComicInfo(name, pathword) },
            mOnTopic = { topices -> navigateTopic(topices) }
        )
    }

    /**
     * ● 全局 Event 事件
     *
     * ● 2023-09-17 01:28:34 周日 上午
     */
    private val mBaseEvent by lazy { BaseEvent.getSIngleInstance() }

    /**
     * ● 漫画搜索碎片
     *
     * ● 2023-09-17 01:47:09 周日 上午
     */
    private var mSearchBinding: HomeFragmentSearchViewBinding? = null

    /**
     * ● 注册FlowBus 设置主页头像
     *
     * ● 2023-09-17 01:28:24 周日 上午
     */
    init {
        FlowBus.with<Drawable>(BaseEventEnum.SetIcon.name).register(this) { drawable ->
            if (!isHidden) {
                lifecycleScope.launch(CoroutineName(this::class.java.simpleName) + baseCoroutineException) {
                    withStarted {
                        mBinding.homeToolbar.navigationIcon = drawable
                    }
                }
            }
        }
    }

    /**
     * ● 导航至BookComicInfo
     *
     * ● 2023-06-16 22:18:11 周五 下午
     */
    private fun navigateBookComicInfo(name: String, pathword: String) {
        val tag = Fragments.BookComicInfo.name
        val bundle = Bundle()
        bundle.putString(BaseStrings.PATH_WORD, pathword)
        bundle.putString(BaseStrings.NAME, name)
        requireParentFragment()
            .parentFragmentManager
            .navigateToWithBackStack(
                id = baseR.id.app_main_fcv,
                hideTarget = requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
                addedTarget = get<Fragment>(named(tag)).also { it.arguments = bundle },
                tag = tag,
                backStackName = tag
            )
    }

    /**
     * ● 导航至BookNovelInfo
     *
     * ● 2023-06-16 22:17:57 周五 下午
     */
    private fun navigateBookNovelInfo(name: String, pathword: String) {
        val tag = Fragments.BookNovelInfo.name
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
     * ● 导航至Topic
     *
     * ● 2023-06-16 22:18:11 周五 下午
     */
    private fun navigateTopic(topic: Topices) {
        val tag = Fragments.Topic.name
        val bundle = Bundle()
        bundle.putString(TopicFragment.TOPIC, toJson(topic))
        requireParentFragment()
            .parentFragmentManager
            .navigateToWithBackStack(
                id = baseR.id.app_main_fcv,
                hideTarget = requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
                addedTarget = get<Fragment>(named(tag)).also { it.arguments = bundle },
                tag = tag,
                backStackName = tag
            )
    }

    /**
     * ● 加载主页数据
     *
     * ● 2023-09-17 19:40:26 周日 下午
     */
    private fun doLoadHomePage() {
        if (mBaseEvent.getBoolean("HOME_FRAGMENT_LOAD_HOME_PAGE") == true) return
        mBaseEvent.setBoolean("HOME_FRAGMENT_LOAD_HOME_PAGE", true)

        // 错误提示 可见

        if (mBaseErrorViewStub.isVisible()) {
            mBaseErrorViewStub.loadLayout(false)
            mBinding.homeRv.animateFadeIn()
        }

        // Banner 不可见 谈出
        if (mBinding.homeComposeBanner.isGone) mBinding.homeComposeBanner.animateFadeIn()

        // 启动协程 加载Rv界面
        viewLifecycleOwner.lifecycleScope.launch {

            // 等待Compose 界面 Complete
            async {
                mBinding.homeComposeBanner.setContent {
                    Banner(banners = mVM.getSnapshotBanner()) { banner ->
                        mBaseEvent.doOnInterval {
                            navigateBookComicInfo(banner.mBrief, banner.mComic?.mPathWord ?: return@doOnInterval)
                        }
                    }
                }
                yield()
            }.await()

            // 结束刷新
            mBinding.homeRefresh.finishRefresh(300)

            // HomeData 界面处理
            mDataRvAdapter.submitList(mVM.getSnapshotHomeData(), 50L)
        }
    }

    /**
     * ● 导航至设置Fragment
     *
     * ● 2023-09-17 19:42:43 周日 下午
     */
    private fun navigateSettings() {
        val name = Fragments.Settings.name
        requireParentFragment().parentFragmentManager.navigateToWithBackStack(baseR.id.app_main_fcv,
            requireActivity().supportFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
            get(named(name)), name, name
        )
    }

    /**
     * ● 初始化SearchView
     *
     * ● 2023-09-17 19:43:02 周日 下午
     */
    @SuppressLint("PrivateResource")
    private fun initSearchView() {
        if (mBaseEvent.getBoolean("HOME_FRAGMENT_INIT_SEARCH_VIEW") == true) return
        mBaseEvent.setBoolean("HOME_FRAGMENT_INIT_SEARCH_VIEW", true)
        mBinding.homeSearchView.apply {
            val binding = HomeFragmentSearchViewBinding.inflate(layoutInflater).also { mSearchBinding = it }                                                                 // 获取SearchViewBinding
            val searchComicFragment = SearchComicFragment.newInstance(mBinding.homeSearchView) { name, pathword ->  navigateBookComicInfo(name, pathword) }   // 实例化SearchComicFragment
            val searchNovelFragment = SearchNovelFragment.newInstance(mBinding.homeSearchView) { name, pathword -> navigateBookNovelInfo(name, pathword) }     // 实例化SearchNovelFragment
            val bgColor: Int; val tintColor: Int
            if (appIsDarkMode) {
                bgColor = ContextCompat.getColor(mContext, com.google.android.material.R.color.m3_sys_color_dark_surface)
                tintColor = ContextCompat.getColor(mContext, android.R.color.white)
            } else {
                bgColor = ContextCompat.getColor(mContext, android.R.color.white)
                tintColor = ContextCompat.getColor(mContext, android.R.color.black)
            }
            toolbar.setNavigationIcon(baseR.drawable.base_ic_back_24dp)  // 设置SearchView toolbar导航图标
            toolbar.navigationIcon?.setTint(tintColor)
            toolbar.setBackgroundColor(bgColor)                                               // 设置SearchView toolbar背景色白，沉浸式
            binding.homeSearchVp.setBackgroundColor(bgColor)
            setStatusBarSpacerEnabled(false)                                                       // 关闭状态栏空格间距
            addView(binding.root)                                                                          // 添加SearcViewBinding 视图内容
            binding.homeSearchVp.adapter = NewHomeVpAdapter(mutableListOf(searchComicFragment, searchNovelFragment), childFragmentManager, viewLifecycleOwner.lifecycle)  // 创建适配器
            binding.homeSearchVp.offscreenPageLimit = 2                              // 设置预加载2页
            TabLayoutMediator(binding.homeSearchTablayout, binding.homeSearchVp) { tab, pos ->
                when(pos) {
                    0 -> { tab.text = getString(R.string.home_comic) }
                    1 -> { tab.text = getString(R.string.home_novel) }
                }
            }.attach()      // 关联VP和TabLayout
            editText.setOnEditorActionListener { _, _, event->                          // 监听EditText 通知对应VP对应页发送意图
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    when(binding.homeSearchVp.currentItem) {
                        0 -> searchComicFragment.doInputSearchComicIntent()
                        1 -> searchNovelFragment.doInputSearchNovelIntent()
                    }
                }
                false
            }
        }
    }

    /**
     * ● 获取ViewBinding
     *
     * ● 2023-09-17 19:42:53 周日 下午
     */
    override fun getViewBinding(inflater: LayoutInflater) = HomeFragmentNewBinding.inflate(inflater)

    /**
     * ● Lifecycle Start
     *
     * ● 2023-09-17 19:43:10 周日 下午
     */
    override fun onStart() {
        super.onStart()
        val baseEvent : BaseEvent = BaseEvent.newInstance(BaseEvent.BASE_FLAG_TIME_1000 shl 1)
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (mBinding.homeSearchView.isShowing) mBinding.homeSearchView.hide()
            else {
                baseEvent.doOnInterval(object : BaseIEventIntervalExt<BaseEvent>{
                    override fun onIntervalOk(baseEventEntity: BaseEventEntity<BaseEvent>) { toast(getString(R.string.home_exit_app)) }
                    override fun onIntervalFailure(gapTime: Long) {
                        requireActivity().finish()
                        exitProcess(0)
                    }
                })
            }
        }
    }

    /**
     * ● Lifecycle Stop
     *
     * ● 2023-09-17 19:43:17 周日 下午
     */
    override fun onStop() {
        super.onStop()
        mBaseEvent.remove(SEARCH_TAG)
    }

    /** ● Lifecycle Destroy */
    override fun onDestroyView() {
        super.onDestroyView()
        mRecRefresh = null  // 置空“换一批”控件 防止内存泄漏
        mSearchBinding = null
        parentFragmentManager.clearFragmentResultListener(HOME)
        mBaseEvent.remove("HOME_FRAGMENT_INIT_SEARCH_VIEW")
        mBaseEvent.remove("HOME_FRAGMENT_LOAD_HOME_PAGE")
    }

    /**
     * ● 初始化数据
     *
     * ● 2023-09-17 19:43:24 周日 下午
     */
    override fun initData(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mBaseEvent.remove("HOME_FRAGMENT_LOAD_HOME_PAGE")
            return
        }

        // 获取主页数据
        mVM.input(HomeIntent.GetHomePage())

        // Refresh
        mBinding.homeRefresh.autoRefreshAnimationOnly()
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-09-17 19:43:32 周日 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        // 内存重启后隐藏SearchView
        if (savedInstanceState != null) {
            withLifecycle(state = Lifecycle.State.RESUMED) {
                mBinding.homeSearchView.hide()
            }
        }

        // 初始化viewstub
        mBaseErrorViewStub = baseErrorViewStub(mBinding.error, lifecycle) { mBinding.homeRefresh.autoRefresh() }

        // 设置刷新时不允许列表滚动
        mBinding.homeRefresh.setDisableContentWhenRefresh(true)

        // 设置适配器
        mBinding.homeRv.adapter = mDataRvAdapter

        // 设置加载动画独占1行，漫画卡片3行
        (mBinding.homeRv.layoutManager as GridLayoutManager).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when(mDataRvAdapter.getItemViewType(position)) {
                        NewHomeComicRvAdapter.HEADER -> spanCount
                        NewHomeComicRvAdapter.REFRESH -> spanCount
                        NewHomeComicRvAdapter.TOPIC -> spanCount
                        else -> 1
                    }
                }
            }
        }

        // 初始化Banner 高度
        mBinding.homeComposeBanner.layoutParams.height = (resources.displayMetrics.widthPixels / 1.875 + 0.5).toInt()
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-09-17 19:43:48 周日 下午
     */
    override fun initListener() {

        // 设置容器Fragment的回调监听
        parentFragmentManager.setFragmentResultListener(HOME, this) { _, bundle ->
            if (bundle.getInt(BaseStrings.ID) == 0) {
                if (bundle.getBoolean(BaseStrings.ENABLE_DELAY)) {
                    launchDelay(BASE_ANIM_200L) { mVM.input(HomeIntent.GetHomePage()) }
                }
                else mVM.input(HomeIntent.GetHomePage())
            }
        }

        // 登录成功 监听
        parentFragmentManager.setFragmentResultListener(BaseEventEnum.LoginCategories.name, this) { _, bundle ->
            if (bundle.getInt(BaseStrings.ID) == 0) {
                mVM.input(HomeIntent.GetHomePage())
            }
        }

        // 搜索
        mBinding.homeToolbar.menu[0].doOnClickInterval {
            if (mBinding.homeSearchView.isShowing) {
                mSearchBinding?.homeSearchVp?.let {  vp ->
                    when(vp.currentItem) {
                        0 -> { (childFragmentManager.fragments[0] as SearchComicFragment).doInputSearchComicIntent() }
                        1 -> { (childFragmentManager.fragments[1] as SearchNovelFragment).doInputSearchNovelIntent() }
                    }
                }
            } else {
                initSearchView()
                mBinding.homeSearchView.show()
            }
        }

        // 设置
        mBinding.homeToolbar.menu[1].doOnClickInterval { navigateSettings() }

        // MaterialToolBar NavigateIcon 点击事件
        mBinding.homeToolbar.navigateIconClickGap(flagTime = BaseEvent.BASE_FLAG_TIME_500 shl 1) {
            mBinding.homeRv.stopScroll()
            get<BottomSheetDialogFragment>(named(Fragments.User.name)).show(requireParentFragment().parentFragmentManager, null)
        }

        // 刷新
        mBinding.homeRefresh.setOnRefreshListener {
            mBaseEvent.doOnInterval(object : BaseIEventIntervalExt<BaseEvent> {
                override fun onIntervalOk(baseEventEntity: BaseEventEntity<BaseEvent>) {
                    mBaseEvent.remove("HOME_FRAGMENT_LOAD_HOME_PAGE")
                    mVM.input(HomeIntent.GetHomePage())
                }
                override fun onIntervalFailure(gapTime: Long) {
                    it.finishRefresh()
                }
            })
        }
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-09-17 19:43:53 周日 下午
     */
    override fun initObserver(saveInstanceState: Bundle?) {

        mVM.onOutput { intent ->
            when (intent) {
                // （获取主页）（根据 刷新事件 来决定是否启用加载动画） 正常加载数据、反馈View
                is HomeIntent.GetHomePage -> {
                    intent.mViewState
                        .doOnSuccess {
                            if (mBinding.homeRefresh.isRefreshing) {
                                mBinding.homeRefresh.finishRefresh(BASE_ANIM_300L.toInt())
                                if (mBinding.error.isGone) {
                                    toast(getString(baseR.string.BaseLoadingErrorNeedRefresh))
                                }
                            }
                        }
                        .doOnResult { doLoadHomePage() }
                        .doOnError { _, _ ->

                            if (mDataRvAdapter.itemCount == 0) {

                                // Banner 不可见
                                if (mBinding.homeComposeBanner.isVisible) {

                                    // Banner GONE
                                    mBinding.homeComposeBanner.animateFadeOut().withEndAction {

                                        // Banner 消失
                                        mBinding.homeComposeBanner.isGone = true

                                        // 错误提示淡入
                                        mBaseErrorViewStub.loadLayout(visible = true, animation = true)
                                    }
                                } else {

                                    // 错误提示淡入
                                    mBaseErrorViewStub.loadLayout(visible = true, animation = true)

                                }

                                // 发现页 “漫画” 淡出
                                mBinding.homeRv.animateFadeOutWithEndInVisibility()

                                // 取消刷新
                                mBinding.homeRefresh.finishRefresh()
                            }
                        }
                }

                // （刷新获取）不启用 加载动画 正常加载数据 -> 反馈View
                is HomeIntent.GetRecPageByRefresh -> {
                    intent.mViewState
                        .doOnError { _, _ ->
                            toast(getString(baseR.string.BaseLoadingError))
                            mRecRefresh?.isEnabled = true
                        }
                        .doOnResult {
                            viewLifecycleOwner.lifecycleScope.launch {
                                mDataRvAdapter.onRefreshSubmitList(mVM.getSnapshotHomeData(), 50L)
                                mRecRefresh?.isEnabled = true
                            }
                        }
                }
            }
        }
    }
}