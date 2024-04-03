package com.crow.module_book.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutGone
import com.crow.base.tools.extensions.doOnClickInterval
import com.crow.base.tools.extensions.hasGlobalPoint
import com.crow.base.tools.extensions.immersionFullScreen
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.immersionFullView
import com.crow.base.tools.extensions.immerureCutoutCompat
import com.crow.base.tools.extensions.isAllWhiteSpace
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.repeatOnLifecycle
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.tools.extensions.updatePadding
import com.crow.base.ui.view.BaseErrorViewStub
import com.crow.base.ui.view.baseErrorViewStub
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.mangax.copymanga.BaseLoadStateAdapter
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.mangax.copymanga.entity.CatlogConfig.mDarkMode
import com.crow.mangax.copymanga.entity.Fragments
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.mangax.copymanga.tryConvert
import com.crow.module_book.R
import com.crow.mangax.R as mangaR
import com.crow.module_book.model.entity.comic.ComicActivityInfo
import com.crow.module_book.model.entity.comic.reader.ReaderEvent
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.ui.adapter.comic.ComicCommentRvAdapter
import com.crow.module_book.ui.fragment.comic.reader.ComicCategories
import com.crow.module_book.ui.fragment.comic.reader.ComicPageHorizontalFragment
import com.crow.module_book.ui.fragment.comic.reader.ComicStandardFragment
import com.crow.module_book.ui.fragment.comic.reader.ComicStriptFragment
import com.crow.module_book.ui.helper.GestureHelper
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import kotlin.math.min
import com.crow.base.R as baseR


class ComicActivity : BaseComicActivity(), GestureHelper.GestureListener {

    companion object {
        const val EVENT = "FLAG"
        const val ACTIVITY_OPTION  = "PARENT_OPTION"
        const val VALUE = "VALUE"
        const val FRAGMENT_OPTION = "CHILD_OPTION"
        const val ROTATE = "ROTATE"
        const val READER_MODE = "READER_MODE"
        const val OPTION = "OPTION"
        const val INFO = "INFO"
        const val TITLE = "TITLE"
        const val SUB_TITLE = "SUB_TITLE"
        const val SLIDE = "SLIDE"
        const val CHAPTER_POSITION = "CHAPTER_POSITION"
        const val CHAPTER_POSITION_OFFSET = "CHAPTER_POSITION_OFFSET"
        const val POS = "POSITION"
        const val POS_OFFSET = "OFFSET"
    }

    /**
     * ⦁ 漫画VM
     *
     * ⦁ 2023-07-07 23:53:41 周五 下午
     */
    private val mVM by viewModel<ComicViewModel>()

    /**
     * ⦁ WindowInset For immersure or systembar
     *
     * ⦁ 2023-07-07 23:53:58 周五 下午
     */
    private val mWindowInsetsCompat: WindowInsetsControllerCompat by lazy { WindowInsetsControllerCompat(window, mBinding.root) }

    /**
     * ⦁ 漫画选项（条漫、等...）
     *
     * ⦁ 2023-07-07 23:54:42 周五 下午
     */
    private val mComicCategory by lazy { ComicCategories(this, mBinding.comicFcv) }

    /**
     * ⦁ Activitiy GestureHelper （手势处理）
     *
     * ⦁ 2023-07-08 00:00:48 周六 上午
     */
    private lateinit var mGestureHelper: GestureHelper

    /**
     * ⦁ ErrorViewStub
     *
     * ⦁ 2024-01-27 23:47:49 周六 下午
     * @author crowforkotlin
     */
    private var mBaseErrorViewStub by BaseNotNullVar<BaseErrorViewStub>(true)

    private var mIsSliding = false
    private var mIsLock = false

    /**
     * ⦁ 评论适配器
     *
     * ⦁ 2023-07-07 21:49:53 周五 下午
     */
    private var mCommentAdapter: ComicCommentRvAdapter? = null

    override fun onDestroy() {
        super.onDestroy()
        AppProgressFactory.clear()
        mCommentAdapter = null
    }

    /**
     * ⦁ 初始化视图
     *
     * ⦁ 2023-07-07 23:55:31 周五 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        val dp5 = resources.getDimensionPixelSize(baseR.dimen.base_dp5)

        // 初始化viewstub
        mBaseErrorViewStub = baseErrorViewStub(mBinding.error, lifecycle) {
            mBaseErrorViewStub.loadLayout(visible = false, animation = true)
            mBinding.loading.animateFadeIn()
            launchDelay(BASE_ANIM_300L) {
                mVM.input(BookIntent.GetComicPage(mVM.mPathword, mVM.mCurrentChapterUuid))
            }
        }

        // 全屏
        immersionFullScreen(mWindowInsetsCompat)

        // 沉浸式边距
        immersionPadding(mBinding.root) { view, insets, _ ->
            val top = insets.top
            mBinding.topAppbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = top }
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }
            mBinding.comment.updatePadding(insets.left, dp5 + top, insets.right, dp5 + insets.bottom )
            mBinding.bottomAppbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin =  dp5 + insets.bottom }
        }

        // 初始化适配器
        mBinding.commentList.adapter = ComicCommentRvAdapter(lifecycleScope) { }.run {
            mCommentAdapter = this
            withLoadStateFooter(BaseLoadStateAdapter { retry() })
        }

        // 禁止滑动
        mBinding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        // 沉浸式状态栏和工具栏
        immersionBarStyle()

        // 内存重启 后 savedInstanceState不为空 防止重复添加Fragment
        if (savedInstanceState == null) {

            mBinding.loading.isVisible = true
        }
    }

    /**
     * ⦁ 初始化事件
     *
     * ⦁ 2023-07-08 01:06:02 周六 上午
     */
    override fun initListener() {

        val slideListener = Slider.OnChangeListener  { _, value, _ ->
            supportFragmentManager.setFragmentResult(SLIDE, bundleOf(SLIDE to value.toInt()))
        }

        mGestureHelper =  GestureHelper(this, this)

        mBinding.topAppbar.navigateIconClickGap { finishActivity() }

        mBinding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                if (!mIsSliding) { supportFragmentManager.setFragmentResult(SLIDE, bundleOf(SLIDE to slider.value.toInt())) }
                mIsSliding = true
                mBinding.slider.addOnChangeListener(slideListener)
            }
            override fun onStopTrackingTouch(p0: Slider) {
                mIsSliding = false
                mBinding.slider.clearOnChangeListeners()
            }
        })

        mBinding.bottomToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_options -> {
                    get<BottomSheetDialogFragment>(named(Fragments.ComicBottom.name)).show(supportFragmentManager, null)
                }
            }
            true
        }

        mBinding.sliderLight.addOnChangeListener { slider, value, b ->
            val light = value.toInt()
            mVM.updateLight(light)
            mBinding.fullView.setBackgroundColor(ColorUtils.setAlphaComponent(Color.BLACK, light))
        }

        mBinding.light.doOnClickInterval {
            val transition = TransitionSet()
                .setDuration(BASE_ANIM_300L)
                .addTransition(Fade().addTarget(mBinding.cardSlide))
            TransitionManager.beginDelayedTransition(mBinding.bottomAppbar, transition)
            if (mBinding.cardSlide.isVisible) {
                mBinding.cardSlide.isGone = true
            } else {
                mBinding.cardSlide.isVisible = true
            }
        }

        mBinding.commentButton.doOnClickInterval { mBinding.root.open() }

        mBinding.rotate.doOnClickInterval {
            supportFragmentManager.setFragmentResult("ROTATE", bundleOf())
        }

        supportFragmentManager.setFragmentResultListener(ACTIVITY_OPTION, this) { key, bundle ->
            when(bundle.getInt(EVENT, -1)) {
                ReaderEvent.READER_MODE -> {
                    lifecycleScope.launch {
                        supportFragmentManager.clearFragmentResultListener(CHAPTER_POSITION)
                        supportFragmentManager.clearFragmentResultListener(SLIDE)
                        val value = bundle.getInt(VALUE)
                        val comicType = ComicCategories.Type.entries.find { it.id == value } ?: ComicCategories.Type.STANDARD
                        mVM.updateReaderMode(comicType)
                        mComicCategory.apply(comicType)
                        val pos = mVM.getChapterPagePos()
                        comicType.whenCategories(
                            onStandard = { setChapterResult(pos, mVM.getPosOffset()) },
                            onStript = { setChapterResult(pos, mVM.getPosOffset()) },
                            onPageLtr = { setChapterResult(pos, 0) },
                            onPageRtl = { setChapterResult(pos, 0) },
                            onPageTtb = { setChapterResult(pos, 0) },
                            onPageBtt =  { setChapterResult(pos, 0) }
                        )
                    }
                }
            }
        }

        mBinding.refresh.setOnRefreshListener {
            launchDelay(BASE_ANIM_300L shl 1) { mBinding.refresh.isRefreshing = false }
            onClollectState(true)
        }

        mBinding.commentSubmit.doOnClickInterval {
            val text = (mBinding.commentInputEdit.text ?: "").toString()
            when {
                MangaXAccountConfig.mAccountToken.isEmpty() -> {
                    toast(getString(mangaR.string.mangax_token_error_relogin))
                }
                text.isEmpty() -> {
                    toast(getString(R.string.book_comment_edit_not_empty))
                }
                text.isAllWhiteSpace() -> {
                    toast(getString(R.string.book_comment_edit_not_empty))
                }
                text.length > 200 -> {
                    toast(getString(R.string.book_comment_edit_length_invalid))
                }
                else -> {
                    mVM.input(BookIntent.SubmitComment(mVM.mCurrentChapterUuid, text))
                }
            }
        }

        mBinding.root.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) { }
            override fun onDrawerClosed(drawerView: View) {
                // 禁止滑动
                mBinding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            override fun onDrawerStateChanged(newState: Int) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(mBinding.root.windowToken, 0)
            }
            override fun onDrawerOpened(drawerView: View) {
                sendFragmentResult(ReaderEvent.OPEN_DRAWER)
                // 允许滑动
                mBinding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                lifecycleScope.tryConvert(mVM.mComicInfo.mSubTitle) {
                    if (mBinding.commentTopbar.subtitle != it) {
                        mBinding.commentTopbar.subtitle = it
                        onClollectState(true)
                    }
                }
            }
        })
    }

    /**
     * ⦁ 初始化数据
     *
     * ⦁ 2023-07-07 23:55:54 周五 下午
     */
    override fun initData(savedInstanceState: Bundle?) {
        if (!intent.getBooleanExtra(ROTATE, false)) {
            val info = toTypeEntity<ComicActivityInfo>(intent.getStringExtra(INFO)) ?: return finishActivity(getString(baseR.string.base_unknow_error))
            mVM.mComicInfo = info
            mVM.mChapterNextUuid = info.mChapterNextUuid
            mVM.mChapterPrevUuid = info.mChapterPrevUuid
            mBinding.topAppbar.title = info.mTitle
            mBinding.topAppbar.subtitle = info.mSubTitle
            mVM.input(BookIntent.GetComicPage(info.mPathword, info.mChapterCurrentUuid))
        } else {
            intent.removeExtra(ROTATE)
        }
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                mComicCategory.apply(mVM.getSetting()?.mReadMode ?: ComicCategories.Type.STANDARD)
            }
        }
        mVM.initComicReader {
            mVM.mReaderComic?.let { comic ->
                setChapterResult(comic.mChapterPagePosition, comic.mChapterPagePositionOffset)
            }
        }
    }

    /**
     * ⦁ 初始化观察者
     *
     * ⦁ 2023-09-01 23:08:52 周五 下午
     */
    override fun initObserver(savedInstanceState: Bundle?) {

        onClollectState()

        lifecycleScope.launch {
            mVM.uiState.collect { state ->
                state?.let { uiState ->
                    if (mBinding.infobar.isGone) mBinding.infobar.animateFadeIn()
                    val readerContent = uiState.mReaderContent
                    val currentPage = uiState.mCurrentPagePos
                    val _currentPage = if (currentPage == -1) 1 else currentPage
                    val totalPage = uiState.mTotalPages
                    mBinding.infobar.update(
                        currentPage = _currentPage,
                        totalPage = totalPage,
                        info = readerContent.mChapterInfo ?: return@let,
                        percent = mVM.computePercent(
                            pageIndex = _currentPage,
                            totalPage = totalPage,
                            info = readerContent.mChapterInfo
                        )
                    )
                    val chapterName = readerContent.mChapterInfo.mChapterName
                    mBinding.topAppbar.title = readerContent.mComicName
                    mBinding.topAppbar.subtitle =  chapterName
                    if (mBinding.root.isOpen) { mBinding.commentTopbar.subtitle = chapterName }
                    if (mBinding.bottomAppbar.isGone || !mIsSliding) {
                        var pageFloat = uiState.mCurrentPagePos.toFloat()
                        val pageTotal = uiState.mTotalPages.toFloat()
                        pageFloat = pageFloat.coerceIn(1f, if(pageFloat >= 1f) pageFloat else 1f)
                        updateSliderValue(pageFloat, pageTotal)
                    }
                    if (currentPage == -1) return@collect
                    mVM.tryUpdateReaderComicrInfo(currentPage, state.mCurrentPagePosOffset, state.mChapterID, readerContent.mChapterInfo) {
                        intent.putExtra(INFO, toJson(it))
                    }
                }
            }
        }

        mVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicPage -> {
                    if (!this.intent.getBooleanExtra("INIT", false)) {
                        intent.mViewState
                            .doOnError { _, _ -> showErrorPage() }
                            .doOnResult {
                                val page = intent.comicpage
                                if (page != null) {
                                    this.intent.putExtra("INIT", true)
                                    setChapterResult(-1, mVM.getPosOffset())
                                    if(mBinding.loading.isVisible) mBinding.loading.animateFadeOutGone()
                                    lifecycleScope.tryConvert(page.mComic.mName, mBinding.topAppbar::setTitle)
                                    lifecycleScope.tryConvert(page.mChapter.mName) {
                                        mBinding.topAppbar.subtitle = it
                                        mBinding.commentTopbar.subtitle = it
                                    }
                                    updateSliderValue(1f, page.mChapter.mContents.size.toFloat())
                                } else {
                                    showErrorPage()
                                }
                            }
                    }
                }
                is BookIntent.GetComicComment -> {

                }
                is BookIntent.SubmitComment -> {
                    intent.mViewState
                        .doOnLoading { mBinding.commentSubmit.isEnabled = false }
                        .doOnSuccess {
                            launchDelay(BASE_ANIM_300L) { mBinding.refresh.isRefreshing = false }
                            mBinding.commentSubmit.isEnabled = true
                        }
                        .doOnError { _, _ -> toast(getString(baseR.string.base_unknow_error)) }
                        .doOnResult {
                            mBinding.commentInputEdit.text = null
                            if(intent.resp?.mCode == 200) toast(getString(R.string.book_comment_success))
                            else  {
                                lifecycleScope.tryConvert(intent.resp?.mMessage ?: return@doOnResult run { toast(getString(baseR.string.base_unknow_error)) }) { toast(it) }
                            }
                        }
                }
            }
        }
    }

    private fun onClollectState(reset: Boolean = false) {
        lifecycleScope.launch {
            if (reset) {
                mVM.mComicCommentFlowPage = null
                mCommentAdapter?.submitData(PagingData.empty())
            }
            withCreated {
                if (mVM.mComicCommentFlowPage == null) {

                    // 发送获取书架 “漫画” 的意图 需要动态收集书架状态才可
                    mVM.input(BookIntent.GetComicComment(mVM.mCurrentChapterUuid))

                }

                // 每个观察者需要一个单独的生命周期块，在同一个会导致第二个观察者失效 收集书架 漫画Pager状态
                repeatOnLifecycle {
                    mVM.mComicCommentFlowPage?.collect {
                        mCommentAdapter?.submitData(it)
                    }
                }
            }
        }
    }

    private fun showErrorPage() {
        launchDelay(BASE_ANIM_300L) {
            mBinding.loading.animateFadeOut().withEndAction {
                mBinding.loading.isGone = true
                mBaseErrorViewStub.loadLayout(visible = true, animation = true)
            }
        }
    }

    /**
     * ⦁ Lifecycle onCreate
     *
     * ⦁ 2023-07-07 23:56:16 周五 下午
     */
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        immersionFullView(window)
        immerureCutoutCompat(window)
    }

    /**
     * ⦁ Lifecycle onStart
     *
     * ⦁ 2023-07-07 23:56:48 周五 下午
     */
    override fun onStart() {
        super.onStart()
        onBackPressedDispatcher.addCallback(this) { finishActivity() }
    }

    /**
     * ⦁ Activity Event onTouch
     *
     * ⦁ 2023-07-07 23:56:56 周五 下午
     */
    override fun onTouch(area: Int, ev: MotionEvent) {
        if (mIsLock) return
        transitionBar(mBinding.topAppbar.isVisible)
    }

    /**
     * ⦁ Activity Event dispatchTouchEvent
     *
     * ⦁ 2023-07-07 23:57:39 周五 下午
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mGestureHelper.dispatchTouchEvent(ev, hasGlobalPoint(ev))
        return super.dispatchTouchEvent(ev)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val action = event?.action
        val code = event?.keyCode
        if (action == KeyEvent.ACTION_DOWN) {
            if (code == KeyEvent.KEYCODE_VOLUME_UP) {
                if (mBinding.root.isOpen) { mBinding.root.closeDrawers() } else { mBinding.root.open() }
                return true
            }
            if (code == KeyEvent.KEYCODE_VOLUME_DOWN) {
                mIsLock = !mIsLock
                toast(
                    if (mIsLock) {
                        getString(R.string.book_lock)
                    } else {
                        getString(R.string.book_unlock)
                    }
                )
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    /**
     * ⦁ 检查点击范围内是否存在指定控件
     *
     * ⦁ 2023-09-04 01:30:21 周一 上午
     */
    private fun hasGlobalPoint(ev: MotionEvent): Boolean {
        if (mBinding.root.isOpen) return true
        val rawX = ev.rawX.toInt()
        val rawY = ev.rawY.toInt()
        val hasToolbar = hasGlobalPoint(mBinding.topAppbar, rawX, rawY)
        val hasBottomBar = hasGlobalPoint(mBinding.bottomAppbar, rawX, rawY)
        if (hasToolbar || hasBottomBar) return true
        var hasRetry =false
        var hasButton = false
        val fragment = supportFragmentManager.fragments.firstOrNull()
        mBaseErrorViewStub.mVsBinding?.let { binding ->
            if (mBaseErrorViewStub.isVisible()) {
                hasRetry = hasGlobalPoint(binding.retry, rawX, rawY)
            }
        }
        if (fragment is ComicStandardFragment || fragment is ComicStriptFragment || fragment is ComicPageHorizontalFragment) {
            val rv = ((fragment.view as FrameLayout)[0] as RecyclerView)
            val childView = rv.findChildViewUnder(ev.x, ev.y)
            if(childView is FrameLayout || childView is ConstraintLayout) {
                childView.forEach {
                    if (fragment.isRemoving) return false
                    if(it is MaterialButton) {
                        hasButton = hasGlobalPoint(it, rawX, rawY)
                    }
                }
            }
        }
        return hasToolbar || hasButton || hasRetry
    }

    /**
     * ⦁ Exit Activity With Animation and can add information
     *
     * ⦁ 2023-09-01 22:41:49 周五 下午
     */
    @Suppress("DEPRECATION")
    private fun finishActivity(message: String? = null) {
        message?.let { toast(it) }
        finish()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    /**
     * ⦁ TransitionBar With Animation
     *
     * ⦁ 2023-09-01 22:43:35 周五 下午
     */
    private fun transitionBar(isHide: Boolean) {
        val transition = TransitionSet()
            .setDuration(BASE_ANIM_300L)
            .addTransition(Slide(Gravity.TOP).addTarget(mBinding.topAppbar))
            .addTransition(Slide(Gravity.BOTTOM).addTarget(mBinding.bottomAppbar))
            .addTransition(Fade().addTarget(mBinding.infobar))
        TransitionManager.beginDelayedTransition(mBinding.root, transition)
        mBinding.topAppbar.isGone = isHide
        mBinding.bottomAppbar.isGone = isHide
        mWindowInsetsCompat.isAppearanceLightStatusBars = !mDarkMode
        mWindowInsetsCompat.isAppearanceLightNavigationBars = !mDarkMode
        if (isHide) {
            immersionFullScreen(mWindowInsetsCompat)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            mWindowInsetsCompat.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    /**
     * ⦁ 沉浸式工具栏、导航栏、状态栏样式
     *
     * ⦁ 2023-09-02 19:12:24 周六 下午
     */
    private fun immersionBarStyle(alpha: Int = 255) {
        (mBinding.topAppbar.background as MaterialShapeDrawable).apply {
            elevation = resources.getDimension(baseR.dimen.base_dp3)
            val color = ColorUtils.setAlphaComponent(resolvedTintColor, alpha)
            window.statusBarColor = color
            window.navigationBarColor = color
            mBinding.comment.setBackgroundColor(color)
        }
    }

    private fun setChapterResult(position: Int, offset: Int) {
        supportFragmentManager.setFragmentResult(CHAPTER_POSITION, Bundle().also {
            it.putInt(CHAPTER_POSITION_OFFSET, offset)
            it.putInt(CHAPTER_POSITION, position)
        })
    }

    private fun updateSliderValue(value: Float, to: Float) {
        var pageTotal = to
        var pageValue = value
        val pageFrom: Float
        if (pageTotal <= 1f) {
            pageFrom = 0f
            pageValue = 1f
            pageTotal =  1f
        } else {
            pageFrom = 1f
        }
        pageValue = min(pageTotal, pageValue)
        mBinding.slider.valueFrom =pageFrom
        mBinding.slider.value = pageValue
        mBinding.slider.valueTo = pageTotal
    }

    private inline fun ComicCategories.Type.whenCategories(
        crossinline onStandard: () -> Unit,
        crossinline onStript: () -> Unit,
        crossinline onPageLtr: () -> Unit,
        crossinline onPageRtl: () -> Unit,
        crossinline onPageTtb: () -> Unit,
        crossinline onPageBtt: () -> Unit,
        crossinline orElse: () -> Unit = {}
    ) {
        when(this) {
            ComicCategories.Type.STANDARD -> onStandard()
            ComicCategories.Type.STRIPT -> onStript()
            ComicCategories.Type.PAGE_HORIZONTAL_LTR -> onPageLtr()
            ComicCategories.Type.PAGE_HORIZONTAL_RTL -> onPageRtl()
            ComicCategories.Type.PAGE_VERTICAL_TTB -> onPageTtb()
            ComicCategories.Type.PAGE_VERTICAL_BTT -> onPageBtt()
            else -> { orElse() }
        }
    }
}
