package com.crow.module_main.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.crow.base.R as baseR
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.imageLoader
import coil.request.ImageRequest
import com.crow.base.R.string.base_exit_app
import com.crow.base.app.app
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutGone
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.dp2px
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.isLatestVersion
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.navigateToWithBackStack
import com.crow.base.tools.extensions.newMaterialDialog
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.px2dp
import com.crow.base.tools.extensions.toast
import com.crow.base.tools.extensions.updatePadding
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.view.event.BaseEventEntity
import com.crow.base.ui.view.event.click.BaseIEventIntervalExt
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnErrorInCoroutine
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnResultInCoroutine
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.BaseStrings.ID
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.appEvent
import com.crow.mangax.copymanga.entity.AppConfig
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.mangax.copymanga.getImageUrl
import com.crow.module_anime.ui.fragment.AnimeFragment
import com.crow.module_bookshelf.ui.fragment.BookshelfFragment
import com.crow.module_discover.ui.fragment.DiscoverComicFragment
import com.crow.module_home.ui.fragment.NewHomeFragment
import com.crow.module_main.R
import com.crow.module_main.databinding.MainFragmentContainerBinding
import com.crow.module_main.databinding.MainNoticeLayoutBinding
import com.crow.module_main.databinding.MainUpdateLayoutBinding
import com.crow.module_main.databinding.MainUpdateUrlLayoutBinding
import com.crow.module_main.model.intent.AppIntent
import com.crow.module_main.model.resp.MainAppUpdateInfoResp
import com.crow.module_main.model.resp.MainNoticeResp
import com.crow.module_main.ui.adapter.ContainerAdapter
import com.crow.module_main.ui.adapter.MainAppUpdateRv
import com.crow.module_main.ui.view.DepthPageTransformer
import com.crow.module_main.ui.viewmodel.MainViewModel
import com.crow.module_mine.ui.adapter.MineRvAdapter
import com.crow.module_mine.ui.viewmodel.MineViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.qualifier.named
import kotlin.system.exitProcess


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/ui/fragment
 * @Time: 2023/3/7 14:00
 * @Author: CrowForKotlin
 * @Description: HomeContainerFragment
 * @formatter:on
 **************************/

class ContainerFragment : BaseMviFragment<MainFragmentContainerBinding>() {

    init {
        FlowBus.with<Unit>(BaseEventEnum.UpdateApp.name).register(this) {
            mVM.input(AppIntent.GetUpdateInfo())
        }
    }

    /** ⦁ 碎片容器适配器 */
    private var mContainerAdapter: ContainerAdapter? = null

    /** ⦁ （Activity级别）容器VM */
    private val mVM by activityViewModel<MainViewModel>()

    /** ⦁ （Activity级别）用户VM */
    private val mUserVM by activityViewModel<MineViewModel>()

    /** ⦁ 碎片集 */
    private val mFragmentList by lazy { mutableListOf<Fragment>(NewHomeFragment(), DiscoverComicFragment(), BookshelfFragment(), AnimeFragment()) }

    // 用户适配器数据
    private val mAdapterData = mutableListOf (
        com.crow.module_mine.R.drawable.mine_ic_login_24dp to app.getString(com.crow.module_mine.R.string.mine_login),
        com.crow.module_mine.R.drawable.mine_ic_reg_24dp to app.getString(com.crow.module_mine.R.string.mine_reg),
        com.crow.module_mine.R.drawable.mine_ic_history_24dp to app.getString(com.crow.module_mine.R.string.mine_browsing_history),
        com.crow.mangax.R.drawable.base_ic_download_24dp to app.getString(com.crow.module_mine.R.string.mine_download),
        com.crow.module_mine.R.drawable.mine_ic_about_24dp to app.getString(com.crow.module_mine.R.string.mine_about),
        com.crow.module_mine.R.drawable.mine_ic_update_24dp to app.getString(com.crow.module_mine.R.string.mine_check_update),
        com.crow.module_mine.R.drawable.mine_ic_update_history_24dp to app.getString(com.crow.module_mine.R.string.mine_update_history_title)
    )

    private var mClickClose: (() -> Unit)? = null


    /**
     * ⦁ 手势检测
     *
     * ⦁ 2023-09-08 01:04:40 周五 上午
     */
    private val mGestureDetector by lazy {
        GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                when(mBinding.viewPager.currentItem) {
                    1 -> childFragmentManager.setFragmentResult("onDoubleTap_Discover_Comic", arguments ?: bundleOf())
                    2 -> childFragmentManager.setFragmentResult("onDoubleTap_Bookshelf", arguments ?: bundleOf())
                }
                return super.onDoubleTap(e)
            }
        })
    }

    /**
     * ⦁ Global BaseEvent
     *
     * ⦁ 2023-09-16 18:37:52 周六 下午
     */
    private val mEvent: BaseEvent by lazy { BaseEvent.getSIngleInstance() }

    private var mNoticeBinding: MainNoticeLayoutBinding? = null

    /** ⦁ 获取ViewBinding */
    override fun getViewBinding(inflater: LayoutInflater) = MainFragmentContainerBinding.inflate(inflater)

    /** ⦁ 初始化视图 */
    override fun initView(savedInstanceState: Bundle?) {


        // 沉浸式 VP BottomNavigation
        immersionPadding(mBinding.root) { view, insets, _ ->
            val top = insets.top
            mBinding.viewPager.updatePadding(top = top)
            mBinding.bottomNavigation.updatePadding(bottom = insets.bottom)
            mBinding.drawerConstraint.updatePadding(top = top)
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin= insets.right
            }
        }

        // 适配器 初始化 （设置Adapter、预加载页数）
        mBinding.viewPager.offscreenPageLimit = 4
        mBinding.viewPager.isUserInputEnabled = false
        mBinding.viewPager.setPageTransformer(DepthPageTransformer())
        mBinding.viewPager.adapter = ContainerAdapter(mFragmentList, childFragmentManager, viewLifecycleOwner.lifecycle)

        mBinding.userRv.adapter = MineRvAdapter(mAdapterData) { pos, content ->
            toast("$pos")
        }
    }

    /**
     * ⦁ Lifecycle onStart
     *
     * ⦁ 2023-12-12 00:47:21 周二 上午
     * @author crowforkotlin
     */
    override fun onStart() {
        super.onStart()
        mBackDispatcher = requireActivity().onBackPressedDispatcher.addCallback(this) {
            val item = mBinding.viewPager.currentItem
            if (item in 1..2) {
                appEvent.doOnInterval(object : BaseIEventIntervalExt<BaseEvent> {
                    override fun onIntervalOk(baseEventEntity: BaseEventEntity<BaseEvent>) { toast(getString( base_exit_app )) }
                    override fun onIntervalFailure(gapTime: Long) {
                        requireActivity().finish()
                        exitProcess(0)
                    }
                })
                return@addCallback
            }
            childFragmentManager.setFragmentResult(BaseStrings.BACKPRESS + mBinding.viewPager.currentItem, Bundle.EMPTY)
        }
    }

    /**
     * ⦁ Lifecycle OnViewCreated
     *
     * ⦁ 2023-09-10 20:01:04 周日 下午
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            mVM.mIsRestarted = true
            if (!isHidden) onNotifyPage()
        } else {
            saveItemPageID(0)
            mEvent.setBoolean(mFragmentList[0].hashCode().toString(), true)
        }
    }

    /**
     * ⦁ Lifecycle onCreate 内存重启逻辑
     *
     * ⦁ 2023-07-02 20:22:37 周日 下午
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVM.input(AppIntent.GetUpdateInfo())
        mVM.input(AppIntent.GetNotice(force = true))
    }

    /**
     * ⦁ Lifecycle onDestroyView
     *
     * ⦁ 2023-07-02 20:22:59 周日 下午
     */
    override fun onDestroyView() {
        mNoticeBinding = null
        super.onDestroyView()
        mFragmentList.forEach { mEvent.remove(it.hashCode().toString()) }
    }

    /** ⦁ 当视图隐藏状态发生改变 并触发 */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) return

        // 可见： 当返回ContainerFragment时回调此方法 则通知设置Icon
        sendOptionResult(BaseEventEnum.SetIcon.name, mUserVM.mIconUrl ?: "")

        onNotifyPage()
    }

    /** ⦁ 初始化监听器 */
    @SuppressLint("ClickableViewAccessibility")
    override fun initListener() {

        // 通用事件
        parentFragmentManager.setFragmentResultListener(BaseStrings.OPTION, viewLifecycleOwner) { key, bundle ->
            val event = bundle.getString(BaseStrings.EVENT)
             when(event) {
                 BaseEventEnum.GetNotice.name -> {
                     mVM.input(AppIntent.GetNotice(force = false))
                 }
                 BaseEventEnum.GetIconUrl.name -> { sendOptionResult(BaseEventEnum.SetIcon.name, mUserVM.mIconUrl ?: "") }
                 NewHomeFragment.ICON -> {
                     mBinding.root.open()
                 }
             }
        }

        // 登录类别
        parentFragmentManager.setFragmentResultListener(BaseEventEnum.LoginCategories.name, viewLifecycleOwner) { _, bundle ->
            if (bundle.getBoolean("isLogout", false)) {
                mUserVM.doClearUserInfo()
            }
            if (bundle.getBoolean(BaseStrings.ENABLE_DELAY, false)) {
                launchDelay(BASE_ANIM_200L) {
                     childFragmentManager.setFragmentResult(BaseEventEnum.LoginCategories.name,  bundleOf(
                         ID to mBinding.viewPager.currentItem))
                }
            } else {
                 childFragmentManager.setFragmentResult(BaseEventEnum.LoginCategories.name,  bundleOf(
                     ID to mBinding.viewPager.currentItem))
            }
        }

        // 子Fragment 清除用户信息
        childFragmentManager.setFragmentResultListener(BaseEventEnum.ClearUserInfo.name, viewLifecycleOwner) { _, _ ->
            mUserVM.doClearUserInfo()
        }

        // 设置底部导航视图点击Item可见
        mBinding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.main_menu_homepage -> doSwitchFragment(0)
                R.id.main_menu_discovery_comic -> doSwitchFragment(1)
                R.id.main_menu_bookshelf -> doSwitchFragment(2)
                R.id.main_menu_anime -> doSwitchFragment(3)
                // R.id.main_menu_discovery_novel -> doSwitchFragment(2)
            }
            true
        }

        // VP 页面回调
        mBinding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (mEvent.getBoolean(mFragmentList[mBinding.viewPager.currentItem].hashCode().toString()) == true) return
                    else mEvent.setBoolean(mFragmentList[mBinding.viewPager.currentItem].hashCode().toString(), true)
                    val bundle = bundleOf(ID to mBinding.viewPager.currentItem, BaseStrings.ENABLE_DELAY to false)
                    when(mBinding.viewPager.currentItem) {
                        0 -> childFragmentManager.setFragmentResult(NewHomeFragment.HOME, bundle)
                        1 -> childFragmentManager.setFragmentResult(DiscoverComicFragment.COMIC, bundle)
                        2 -> childFragmentManager.setFragmentResult(BookshelfFragment.BOOKSHELF, bundle)
                        3 -> childFragmentManager.setFragmentResult(AnimeFragment.ANIME, bundle)
                    }
                }
            }
        })

        // Item onTouchEvent 漫画
        mBinding.bottomNavigation.setItemOnTouchListener(R.id.main_menu_discovery_comic) { _, event ->
            mGestureDetector.onTouchEvent(event)
        }

        // Item onTouchEvent 书架
        mBinding.bottomNavigation.setItemOnTouchListener(R.id.main_menu_bookshelf) { _, event ->
            mGestureDetector.onTouchEvent(event)
        }
    }

    /** ⦁ 初始化观察者 */
    override fun initObserver(saveInstanceState: Bundle?) {

        // 用户信息 收集
        mUserVM.userInfo.onCollect(this) {

            // 初始化 用户Tokne
            it?.apply {
                MangaXAccountConfig.mAccountToken = mToken
                MangaXAccountConfig.mAccount = mUsername
            }
        }

        // 观察ContainerVM
        mVM.onOutput { intent ->
            when(intent) {
                is AppIntent.GetDynamicSite -> {
                    intent.mViewState
                        .doOnErrorInCoroutine { _, _ -> mVM.saveAppConfig() }
                        .doOnResultInCoroutine {
                            BaseStrings.URL.COPYMANGA = Base64.decode(intent.siteResp!!.mSiteList!!.first()!!.mEncodeSite, Base64.DEFAULT).decodeToString()
                            mVM.saveAppConfig((AppConfig.getInstance() ?: mVM.getReadedAppConfig()) ?: return@doOnResultInCoroutine)
                        }
                }
                is AppIntent.GetUpdateInfo -> {
                    intent.mViewState
                        .doOnError { _, _ -> toast(getString(R.string.main_update_error)) }
                        .doOnResult { doUpdateChecker(saveInstanceState, intent.appUpdateResp!!) }
                }
                is AppIntent.GetNotice -> {
                    val isForce = intent.force
                    intent.mViewState
                        .doOnLoading { if(!isForce && mNoticeBinding == null) showDialog(false, null) }
                        .doOnResult {
                            if (!intent.force && mNoticeBinding == null) return@doOnResult
                            intent.notice?.apply {
                                showDialog(isForce, this) {
                                    lifecycleScope.launch {
                                        if (isForce) {
                                            val config = mVM.getReadedAppConfig() ?: return@launch
                                            config.log()
                                            if (config.mNoticeVersion < mVersion) {
                                                mVM.saveAppConfig(config.copy(mNoticeVersion = mVersion))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        /*.doOnError { _, _ ->
                            mNoticeBinding?.let { binding ->
                                if (binding.loading.isGone) return@doOnError
                                binding.loading.animateFadeOutGone()
                                    .withEndAction { binding.retry.animateFadeIn() }
                                *//*if (binding.content.isVisible) {
                                    binding.author.animateFadeOutInVisibility()
                                    binding.time.animateFadeOutInVisibility()
                                    binding.content.animateFadeOutInVisibility()
                                }
                                if (binding.loading.isVisible) {
                                    binding.loading.animateFadeOut()
                                        .withEndAction {
                                            binding.loading.isInvisible = true
                                            if (binding.retry.isGone) binding.retry.animateFadeIn()
                                        }
                                } else {
                                    if (binding.retry.isGone) binding.retry.animateFadeIn()
                                }*//*
                            }
                        }
                        .doOnResult {
                            intent.notice?.apply {
                                if (isForce) {
                                    lifecycleScope.launch {
                                        val config = mVM.getReadedAppConfig() ?: return@launch
                                        if (config.mNoticeVersion < mVersion) return@launch
                                        if (mNoticeBinding != null) {
                                            mClickClose = {
                                                lifecycleScope.launch {
                                                    mVM.saveAppConfig(config.copy(mNoticeVersion = mVersion))
                                                }
                                            }
                                            mNoticeBinding!!.apply {
                                                if (close.isVisible) {
                                                    close.isClickable = false
                                                    close.animateFadeOut()
                                                }
                                                loadForceNotice(this)
                                            }
                                        } else {
                                            loadForceNotice(loadNoticeDialog(true) {
                                                lifecycleScope.launch {
                                                    mVM.saveAppConfig(config.copy(mNoticeVersion = mVersion))
                                                }
                                            })
                                        }
                                    }
                                } else {
                                    loadNoticeContent(mNoticeBinding ?: return@doOnResult)
                                }
                            }
                        }*/
                }
            }
        }
    }
    private var mIsForce: Boolean = false
    private fun MainNoticeLayoutBinding.loadForceJob(noticeResp: MainNoticeResp) {
        title.text = noticeResp.mForceContent
        val forceTime = noticeResp.mForceTime
        if (forceTime == 0) {
            readed.isClickable = true
            return
        }
        lifecycleScope.launch {
            val config = mVM.getReadedAppConfig() ?: return@launch run { readed.isClickable = true }
            if (config.mNoticeVersion < noticeResp.mVersion) return@launch run { readed.isClickable = true }
            readed.isClickable = false
            repeat(forceTime) {
                readed.text = "${forceTime - it}"
                delay(BaseEvent.BASE_FLAG_TIME_1000)
            }
            readed.text = noticeResp.mReadedButtonText
            readed.alpha = 0.5f
            readed.animate()
                .setDuration(BASE_ANIM_200L)
                .alpha(1f)
                .withEndAction { readed.isClickable = true }
                .start()
        }
    }
    private fun MainNoticeLayoutBinding.loadContent(noticeResp: MainNoticeResp) {
        mBinding.apply {
            author.text = noticeResp.mNewAuthor
            time.text = noticeResp.mNewTime
            content.text = noticeResp.mContent
            readed.text = noticeResp.mReadedButtonText
            if (icon.isInvisible) {
                icon.doOnClickInterval {
                    val tag = Fragments.Image.name
                    val fragment = get<Fragment>(named(Fragments.Image.name)).also { it.arguments =
                        bundleOf(
                            BaseStrings.IMAGE_URL to noticeResp.mAuthorLink,
                            BaseStrings.NAME to "编辑"
                        )
                    }
                    parentFragmentManager.navigateToWithBackStack(com.crow.mangax.R.id.app_main_fcv, parentFragmentManager.findFragmentByTag(Fragments.Container.name)!!, fragment, tag, tag)
                }
                icon.setImageResource(com.crow.mangax.R.drawable.base_icon_app)
                icon.animateFadeIn()
            }
            app.imageLoader.enqueue(
                ImageRequest.Builder(mContext)
                    .data(noticeResp.mAuthorLink) // 加载的图片地址或占位符
                    .decoderFactory { result, option, _-> Decoder { DecodeResult(drawable = android.graphics.BitmapFactory.decodeStream(result.source.source().inputStream()).toDrawable(option.context.resources), false) } }
                    .target { icon.setImageBitmap((it as BitmapDrawable).bitmap) }
                    .build()
            )
            if (readed.isInvisible) { readed.animateFadeIn() }
            if (mIsForce) {
                loadForceJob(noticeResp)
            }
        }
    }
    private fun MainNoticeLayoutBinding.updateNotice(isForce: Boolean, noticeResp: MainNoticeResp?) {
        if (loading.tag == null) {
            loading.tag = Unit
            loading.animateFadeOut()
                .withEndAction {
                    loading.isGone = true
                    loadContent(noticeResp ?: return@withEndAction)
                }
        } else {
            "log $isForce \t $mIsForce".log()
            if (isForce != mIsForce) {
                if (close.isVisible) {
                    close.isClickable = false
                    close.animateFadeOut()
                    loadForceJob(noticeResp ?: return)
                }
            }
        }
    }
    private fun showDialog(isForce: Boolean, noticeResp: MainNoticeResp?, onClickButton: (() -> Unit)? = null) {
        if (mNoticeBinding != null)  {
            mNoticeBinding!!.updateNotice(isForce, noticeResp ?: return)
            return
        } else {
            mIsForce = isForce
        }
        mNoticeBinding = MainNoticeLayoutBinding.inflate(layoutInflater)
        mNoticeBinding!!.apply {
            val dialog = mContext.newMaterialDialog { it.setView(root) }
            val screenHeight = mContext.resources.displayMetrics.heightPixels / 2
            dialog.setCancelable(false)
            (scrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight.toInt()
            retry.doOnClickInterval {
                retry.animateFadeOut()
                    .withEndAction {
                        retry.isGone = true
                        loading.animateFadeIn()
                            .withEndAction { mVM.input(AppIntent.GetNotice(force = false)) }
                    }
            }
            readed.doOnClickInterval {
                mNoticeBinding = null
                onClickButton?.invoke()
                dialog.dismiss()
            }
            close.doOnClickInterval {
                mNoticeBinding = null
                dialog.dismiss()
            }
            if (isForce) {
                readed.isClickable = false
                close.isClickable = false
                if (readed.isVisible) readed.animateFadeOut()
                if(close.isVisible) close.animateFadeOut()
            } else {
                close.isClickable = true
                if(close.isInvisible) close.animateFadeIn()
            }
        }
        if (mIsForce) {
            mNoticeBinding?.updateNotice(isForce, noticeResp ?: return)
        }
    }

    /**
     * ⦁ 通知页面更新
     *
     * ⦁ 2023-06-29 01:28:48 周四 上午
     */
    private fun onNotifyPage() {
        if (mVM.mIsRestarted) {
            mVM.mIsRestarted = false
            val bundle = bundleOf(ID to (arguments?.getInt(ID) ?: 0).also {
                saveItemPageID(it)
                mEvent.setBoolean(mFragmentList[it].hashCode().toString(), true)
            }, BaseStrings.ENABLE_DELAY to true)
            childFragmentManager.setFragmentResult(NewHomeFragment.HOME, bundle)
            childFragmentManager.setFragmentResult(DiscoverComicFragment.COMIC, bundle)
            childFragmentManager.setFragmentResult(BookshelfFragment.BOOKSHELF, bundle)
            childFragmentManager.setFragmentResult(AnimeFragment.ANIME, bundle)
        }
    }


    /** ⦁ 执行选择Fragment */
    private fun doSwitchFragment(position: Int) {
        if (mBinding.viewPager.currentItem != position) {
            mBinding.viewPager.currentItem = position
        }
        saveItemPageID(position)
    }

    /**
     * ⦁ 保存当前页面ID
     *
     * ⦁ 2023-07-02 20:21:35 周日 下午
     */
    private fun saveItemPageID(position: Int) {
        if (arguments == null) {
            val bundle = Bundle()
            bundle.putInt(ID, position)
            arguments = bundle
        } else {
            requireArguments().putInt(ID, position)
        }
    }

    /**
     * ⦁ 检查更新
     *
     * ⦁ 2023-09-16 18:32:59 周六 下午
     * @param savedInstanceState 检查内存重启状态
     */
    private fun doUpdateChecker(savedInstanceState: Bundle?, appUpdateResp: MainAppUpdateInfoResp) {
        val update = appUpdateResp.mUpdate
        if (savedInstanceState != null) {
            mEvent.setBoolean("INIT_UPDATE", true)
            if (!appUpdateResp.mForceUpdate) return
        }
        if (isLatestVersion(latest = update.mVersionCode.toLong())) return run {
            if (mEvent.getBoolean("INIT_UPDATE") == true) toast(getString(R.string.main_update_tips))
            mEvent.setBoolean("INIT_UPDATE", true)
        }
        mEvent.setBoolean("INIT_UPDATE", true)
        if (isHidden) return
        val updateBinding = MainUpdateLayoutBinding.inflate(layoutInflater)
        val updateDialog = mContext.newMaterialDialog { dialog ->
            dialog.setCancelable(false)
            dialog.setView(updateBinding.root)
        }
        val screenHeight = resources.displayMetrics.heightPixels / 3
        (updateBinding.mainUpdateScrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight
        updateBinding.mainUpdateCancel.isInvisible = appUpdateResp.mForceUpdate
        updateBinding.mainUpdateTitle.text = update.mTitle
        updateBinding.mainUpdateText.text = update.mContent
        updateBinding.mainUpdateTime.text = getString(R.string.main_update_time, update.mTime)
        if (!appUpdateResp.mForceUpdate) { updateBinding.mainUpdateCancel.doOnClickInterval { updateDialog.dismiss() } }
        updateBinding.mainUpdateGo.doOnClickInterval(flagTime = BASE_ANIM_300L) {
            updateDialog.dismiss()
            val updateUrlBinding = MainUpdateUrlLayoutBinding.inflate(layoutInflater)
            val updateUrlDialog = mContext.newMaterialDialog {
                it.setCancelable(false)
                it.setView(updateUrlBinding.root)
            }
            (updateUrlBinding.mainUpdateUrlRv.layoutParams as ConstraintLayout.LayoutParams).height = screenHeight
            updateUrlBinding.close.isInvisible = appUpdateResp.mForceUpdate
            updateUrlBinding.mainUpdateUrlRv.post {
                updateUrlBinding.mainUpdateUrlRv.adapter = MainAppUpdateRv(update.mUrl)
            }
            if (!appUpdateResp.mForceUpdate) { updateUrlBinding.close.doOnClickInterval { updateUrlDialog.dismiss() } }
        }
        updateBinding.mainUpdateHistory.doOnClickInterval(flagTime = BASE_ANIM_300L) {
            parentFragmentManager.navigateToWithBackStack(
                R.id.app_main_fcv,
                parentFragmentManager.findFragmentByTag(Fragments.Container.name)!!,
                get<Fragment>(named(Fragments.UpdateHistory.name)).also { it.arguments = bundleOf("force_update" to appUpdateResp.mForceUpdate) },
                Fragments.UpdateHistory.name,
                Fragments.UpdateHistory.name
            )
            updateDialog.dismiss()
        }
    }

    private fun sendOptionResult(event: String, type: Any) {
        childFragmentManager.setFragmentResult(BaseStrings.OPTION,
            bundleOf(
                BaseStrings.EVENT to event,
                BaseStrings.VALUE to type
            )
        )
    }

    private fun loadNoticeDialog(force: Boolean, clickClose: (() -> Unit)? = null): MainNoticeLayoutBinding {
        mClickClose = clickClose
        val binding = MainNoticeLayoutBinding.inflate(layoutInflater).also { mNoticeBinding = it }
        if (!force) {
            binding.close.isVisible = true
            binding.close.isClickable = true
        }
        val dialog = mContext.newMaterialDialog {
            it.setView(binding.root)
        }
        val screenHeight = resources.displayMetrics.heightPixels / 2
        dialog.setCancelable(false)
        (binding.scrollview.layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxHeight = screenHeight.toInt()
        binding.retry.doOnClickInterval {
            binding.retry.animateFadeOut()
                .withEndAction {
                    binding.retry.isGone = true
                    binding.loading.animateFadeIn()
                        .withEndAction { mVM.input(AppIntent.GetNotice(force = false)) }
                }
        }
        binding.readed.doOnClickInterval {
            mNoticeBinding = null
            mClickClose?.invoke()
            dialog.dismiss()
        }
        binding.close.doOnClickInterval {
            mNoticeBinding = null
            mClickClose?.invoke()
            dialog.dismiss()
        }
        return binding
    }

    private suspend fun MainNoticeResp.loadForceNotice(binding: MainNoticeLayoutBinding) {
        binding.readed.let { readed ->
            loadNoticeContent(binding, true)
            coroutineScope {
                if (mForceTime == 0) {
                    binding.readed.isClickable = true
                    return@coroutineScope
                }
                readed.isClickable = false
                val max = mForceTime
                launch {
                    repeat(max) {
                        binding.readed.text = "${max - it}"
                        delay(BaseEvent.BASE_FLAG_TIME_1000)
                    }
                    readed.text = mReadedButtonText
                    readed.alpha = 0.5f
                    readed.animate()
                        .setDuration(BASE_ANIM_200L)
                        .alpha(1f)
                        .withEndAction { readed.isClickable = true }
                        .start()
                }
            }
        }
    }

    private fun MainNoticeResp.loadNoticeContent(binding: MainNoticeLayoutBinding, isForce: Boolean = false) {
        fun loadContent() {
            binding.author.text = mNewAuthor
            binding.time.text = mNewTime
            binding.content.text = mContent
            binding.loading.isGone = true
            if (binding.content.isInvisible) {
                binding.author.animateFadeIn()
                binding.time.animateFadeIn()
            }
            if (binding.readed.isInvisible) { binding.readed.animateFadeIn() }
            if (!isForce) {
                binding.readed.isClickable = true
                binding.readed.text = mReadedButtonText
            }
        }
        if (binding.retry.isVisible) binding.retry.animateFadeOutGone()
        if (!isForce && binding.close.isInvisible) {
            binding.close.isClickable = true
            binding.close.animateFadeIn()
        }
        binding.loading.animateFadeOut()
            .withEndAction {
                binding.loading.isGone = true
                if (isForce) { binding.title.text = this.mForceContent }
                loadContent()
            }
    }
}