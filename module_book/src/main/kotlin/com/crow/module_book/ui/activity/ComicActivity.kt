package com.crow.module_book.ui.activity

import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutGone
import com.crow.base.tools.extensions.hasGlobalPoint
import com.crow.base.tools.extensions.immersionFullScreen
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.immersureFullView
import com.crow.base.tools.extensions.immerureCutoutCompat
import com.crow.base.tools.extensions.info
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.base.ui.view.BaseErrorViewStub
import com.crow.base.ui.view.baseErrorViewStub
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.mangax.copymanga.entity.AppConfig.Companion.mDarkMode
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.mangax.copymanga.tryConvert
import com.crow.module_book.databinding.BookActivityComicBinding
import com.crow.module_book.model.entity.comic.ComicActivityInfo
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.ui.fragment.comic.reader.ComicCategories
import com.crow.module_book.ui.fragment.comic.reader.ComicClassicFragment
import com.crow.module_book.ui.fragment.comic.reader.ComicStriptFragment
import com.crow.module_book.ui.helper.GestureHelper
import com.crow.module_book.ui.view.comic.rv.ComicFrameLayout
import com.crow.module_book.ui.view.comic.rv.ComicRecyclerView
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR


class ComicActivity : BaseMviActivity<BookActivityComicBinding>(), GestureHelper.GestureListener {

    companion object {
        const val INFO = "INFO"
        const val TITLE = "TITLE"
        const val SUB_TITLE = "SUB_TITLE"
    }

    /**
     * ● 漫画VM
     *
     * ● 2023-07-07 23:53:41 周五 下午
     */
    private val mVM by viewModel<ComicViewModel>()

    /**
     * ● WindowInset For immersure or systembar
     *
     * ● 2023-07-07 23:53:58 周五 下午
     */
    private val mWindowInsetsCompat: WindowInsetsControllerCompat by lazy { WindowInsetsControllerCompat(window, mBinding.root) }

    /**
     * ● 漫画选项（条漫、等...）
     *
     * ● 2023-07-07 23:54:42 周五 下午
     */
    private val mComicCategory by lazy { ComicCategories(this, mBinding.comicFcv) }

    /**
     * ● Activitiy GestureHelper （手势处理）
     *
     * ● 2023-07-08 00:00:48 周六 上午
     */
    private lateinit var mGestureHelper: GestureHelper

    /**
     * ● ErrorViewStub
     *
     * ● 2024-01-27 23:47:49 周六 下午
     * @author crowforkotlin
     */
    private var mBaseErrorViewStub by BaseNotNullVar<BaseErrorViewStub>(true)

    private var mInit = false

    /**
     * ● 获取ViewBinding
     *
     * ● 2023-07-07 23:55:09 周五 下午
     */
    override fun getViewBinding() = BookActivityComicBinding.inflate(layoutInflater)

    override fun onDestroy() {
        super.onDestroy()
        AppProgressFactory.clear()
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-07-07 23:55:31 周五 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        val dp5 = resources.getDimensionPixelSize(baseR.dimen.base_dp5)

        // 初始化viewstub
        mBaseErrorViewStub = baseErrorViewStub(mBinding.error, lifecycle) {
            mBaseErrorViewStub.loadLayout(visible = false, animation = true)
            mBinding.loading.animateFadeIn()
            launchDelay(BASE_ANIM_300L) {
                mVM.input(BookIntent.GetComicPage(mVM.mPathword, mVM.mUuid))
            }
        }

        // 全屏
        immersionFullScreen(mWindowInsetsCompat)

        // 沉浸式边距
        immersionPadding(mBinding.root) { view, insets, _ ->
            mBinding.topAppbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }
            mBinding.bottomAppbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin =  dp5 + insets.bottom }
        }

        // 沉浸式状态栏和工具栏
        immersionBarStyle()

        // 内存重启 后 savedInstanceState不为空 防止重复添加Fragment
        if (savedInstanceState == null) {

            mBinding.loading.isVisible = true

            lifecycleScope.launch {

                mComicCategory.apply(ComicCategories.Type.STRIPT)
                delay(5000)
            }
        }
    }

    /**
     * ● 初始化事件
     *
     * ● 2023-07-08 01:06:02 周六 上午
     */
    override fun initListener() {

        mGestureHelper =  GestureHelper(this, this)

        mBinding.topAppbar.navigateIconClickGap {
            finishActivity()
        }
    }

    /**
     * ● 初始化数据
     *
     * ● 2023-07-07 23:55:54 周五 下午
     */
    override fun initData(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val info = toTypeEntity<ComicActivityInfo>(intent.getStringExtra(INFO)) ?: return finishActivity(getString(baseR.string.base_unknow_error))
            mVM.mComicInfo = info
            mVM.mNextUuid = info.mNext
            mVM.mPrevUuid = info.mPrev
            mBinding.topAppbar.title = info.mTitle
            mBinding.topAppbar.subtitle = info.mSubTitle
            mVM.input(BookIntent.GetComicPage(info.mPathword, info.mUuid))
        }
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-09-01 23:08:52 周五 下午
     */
    override fun initObserver(savedInstanceState: Bundle?) {

        mVM.uiState.onCollect(this) { state ->
            if (state != null && state.mReaderContent.mChapterInfo != null) {
                if (mBinding.infobar.isGone) mBinding.infobar.animateFadeIn()
                mBinding.infobar.update(
                    currentPage = state.mCurrentPage,
                    totalPage = state.mTotalPages,
                    percent = mVM.computePercent(
                        pageIndex = state.mCurrentPage,
                        totalPage = state.mTotalPages,
                        info = state.mReaderContent.mChapterInfo
                    ),
                    info = state.mReaderContent.mChapterInfo
                )
            }
        }

        mVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicPage -> {
                    if (!mInit) {
                        intent.mViewState
                            .doOnError { _, _ -> showErrorPage() }
                            .doOnResult {
                                val page = intent.comicpage
                                if (page != null) {
                                    mInit = true
                                    mBinding.loading.animateFadeOutGone()
                                    lifecycleScope.tryConvert(page.mComic.mName, mBinding.topAppbar::setTitle)
                                    lifecycleScope.tryConvert(page.mChapter.mName, mBinding.topAppbar::setSubtitle)
                                } else {
                                    showErrorPage()
                                }
                            }

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
     * ● Lifecycle onCreate
     *
     * ● 2023-07-07 23:56:16 周五 下午
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersureFullView(window)
        immerureCutoutCompat(window)
    }

    /**
     * ● Lifecycle onStart
     *
     * ● 2023-07-07 23:56:48 周五 下午
     */
    override fun onStart() {
        super.onStart()
        onBackPressedDispatcher.addCallback(this) { finishActivity() }
    }

    /**
     * ● Activity Event onTouch
     *
     * ● 2023-07-07 23:56:56 周五 下午
     */
    override fun onTouch(area: Int, ev: MotionEvent) {
        transitionBar(mBinding.topAppbar.isVisible)
    }

    /**
     * ● Activity Event dispatchTouchEvent
     *
     * ● 2023-07-07 23:57:39 周五 下午
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        mGestureHelper.dispatchTouchEvent(ev, hasGlobalPoint(ev))
        return super.dispatchTouchEvent(ev)
    }

    /**
     * ● 检查点击范围内是否存在指定控件
     *
     * ● 2023-09-04 01:30:21 周一 上午
     */
    private fun hasGlobalPoint(ev: MotionEvent): Boolean {
        val rawX = ev.rawX.toInt()
        val rawY = ev.rawY.toInt()
        var hasRetry =false
        val hasToolbar = hasGlobalPoint(mBinding.topAppbar, rawX, rawY)
        var hasButton = false
        val fragment = supportFragmentManager.fragments.firstOrNull()
        mBaseErrorViewStub.mVsBinding?.let { binding ->
            if (mBaseErrorViewStub.isVisible()) {
                hasRetry = hasGlobalPoint(binding.retry, rawX, rawY)
            }
        }
        if (fragment is ComicClassicFragment || fragment is ComicStriptFragment) {
            val rv = ((fragment.view as ComicFrameLayout)[0] as ComicRecyclerView)
            val childView = rv.findChildViewUnder(ev.x, ev.y)
            if(childView is FrameLayout) {
                childView.forEach {
                    if (fragment.isRemoving) return hasToolbar
                    if(it is MaterialButton) {
                        hasButton = hasGlobalPoint(it, rawX, rawY)
                    }
                }
            }
        }
        return hasToolbar || hasButton || hasRetry
    }

    /**
     * ● Exit Activity With Animation and can add information
     *
     * ● 2023-09-01 22:41:49 周五 下午
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
     * ● TransitionBar With Animation
     *
     * ● 2023-09-01 22:43:35 周五 下午
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
     * ● 沉浸式工具栏、导航栏、状态栏样式
     *
     * ● 2023-09-02 19:12:24 周六 下午
     */
    private fun immersionBarStyle() {
        (mBinding.topAppbar.background as MaterialShapeDrawable).apply {
            elevation = resources.getDimension(baseR.dimen.base_dp3)
            alpha = 242
            val color = ColorUtils.setAlphaComponent(resolvedTintColor, alpha)
            window.statusBarColor = color
            window.navigationBarColor = color
        }
    }
}
