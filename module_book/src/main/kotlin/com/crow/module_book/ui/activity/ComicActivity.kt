package com.crow.module_book.ui.activity

import android.os.Build
import android.os.Bundle
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
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.appIsDarkMode
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.hasGlobalPoint
import com.crow.base.tools.extensions.immersionFullScreen
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.immersureFullView
import com.crow.base.tools.extensions.immerureCutoutCompat
import com.crow.base.tools.extensions.navigateIconClickGap
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.databinding.BookActivityComicBinding
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.ui.fragment.comic.reader.BookClassicComicFragment
import com.crow.module_book.ui.fragment.comic.reader.BookComicCategories
import com.crow.module_book.ui.helper.GestureHelper
import com.crow.module_book.ui.view.comic.rv.ComicFrameLayout
import com.crow.module_book.ui.view.comic.rv.ComicRecyclerView
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.MaterialShapeDrawable
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR


class ComicActivity : BaseMviActivity<BookActivityComicBinding>(), GestureHelper.GestureListener {

    /**
     * ● 漫画VM
     *
     * ● 2023-07-07 23:53:41 周五 下午
     */
    private val mComicVM by viewModel<ComicViewModel>()

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
    private val mComicCategory by lazy { BookComicCategories(this, mBinding.comicFcv) }

    /**
     * ● Activitiy GestureHelper （手势处理）
     *
     * ● 2023-07-08 00:00:48 周六 上午
     */
    private lateinit var mGestureHelper: GestureHelper

    /**
     * ● 是否需要加载（默认为true）
     *
     * ● 2023-09-04 01:35:45 周一 上午
     */
    private var mIsNeedLoading = true

    /**
     * ● 获取ViewBinding
     *
     * ● 2023-07-07 23:55:09 周五 下午
     */
    override fun getViewBinding() = BookActivityComicBinding.inflate(layoutInflater)

    /**
     * ● 初始化视图
     *
     * ● 2023-07-07 23:55:31 周五 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        // 全屏
        immersionFullScreen(mWindowInsetsCompat)

        // 沉浸式边距
        immersionPadding(mBinding.root) { view, insets, _ ->
            mBinding.comicToolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }
        }

        // 沉浸式状态栏和工具栏
        immersionBarStyle()

        // 内存重启 后 savedInstanceState不为空 防止重复添加Fragment
        if (savedInstanceState == null) {

            // CLASSIC 经典 （按钮点击下一章）
            mComicCategory.apply(BookComicCategories.Type.CLASSIC)
        }
    }

    /**
     * ● 初始化事件
     *
     * ● 2023-07-08 01:06:02 周六 上午
     */
    override fun initListener() {

        mGestureHelper =  GestureHelper(this, this)

        mBinding.comicToolbar.navigateIconClickGap {
            finishActivity()
        }
    }

    /**
     * ● 初始化数据
     *
     * ● 2023-07-07 23:55:54 周五 下午
     */
    override fun initData() {
        mComicVM.mPathword = (intent.getStringExtra(BaseStrings.PATH_WORD) ?: "").also {
            if (it.isEmpty()) finishActivity(getString(baseR.string.BaseError, "pathword is null or empty"))
        }
        mComicVM.mUuid = (intent.getStringExtra(ComicViewModel.UUID) ?: "").also {
            if (it.isEmpty()) finishActivity(getString(baseR.string.BaseError, "uuid is null or empty"))
        }
        mComicVM.mPrevUuid = intent.getStringExtra(ComicViewModel.PREV_UUID)
        mComicVM.mNextUuid = intent.getStringExtra(ComicViewModel.NEXT_UUID)
        mComicVM.input(BookIntent.GetComicPage(mComicVM.mPathword, mComicVM.mUuid, enableLoading = true))
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-09-01 23:08:52 周五 下午
     */
    override fun initObserver(savedInstanceState: Bundle?) {

        mComicVM.uiState.onCollect(this) { state ->
            if (state != null && state.mReaderContent.mInfo != null) {
                if (mBinding.comicInfoBar.isGone) mBinding.comicInfoBar.animateFadeIn()
                mBinding.comicInfoBar.update(
                    currentPage = state.mCurrentPage,
                    totalPage = state.mTotalPages,
                    percent = mComicVM.computePercent(
                        pageIndex = state.mCurrentPage,
                        totalPage = state.mTotalPages,
                        info = state.mReaderContent.mInfo
                    ),
                    info = state.mReaderContent.mInfo
                )
            }
        }

        mComicVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicPage -> {
                    if (intent.enableLoading) {
                        intent.mViewState
                            .doOnLoading{
                                if (!mIsNeedLoading) {
                                    mIsNeedLoading = true
                                    return@doOnLoading
                                }
                                showLoadingAnim() { dialog -> dialog.applyWindow(dimAmount = 0.3f, isFullScreen = true) }
                            }
                            .doOnError { _, _ ->
                                toast(getString(baseR.string.BaseLoadingError))
                                dismissLoadingAnim { finishActivity() }
                            }
                            .doOnResult {
                                dismissLoadingAnim()
                                val page = intent.comicpage
                                if (page != null) {
                                    mBinding.comicToolbar.title = page.mComic.mName
                                    mBinding.comicToolbar.subtitle = page.mChapter.mName
                                }
                            }
                    }
                }
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
        if (mComicVM.mOrientation != resources.configuration.orientation) {
            mComicVM.mOrientation = resources.configuration.orientation
            mIsNeedLoading = false
        }
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
        transitionBar(mBinding.comicToolbar.isVisible)
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
        val hasToolbar = hasGlobalPoint(mBinding.comicToolbar, ev.rawX.toInt(), ev.rawY.toInt())
        var hasButton = false
        val fragment = supportFragmentManager.fragments.firstOrNull()
        if (fragment is BookClassicComicFragment) {
            val rv = ((fragment.view as ComicFrameLayout)[0] as ComicRecyclerView)
            val childView = rv.findChildViewUnder(ev.x, ev.y)
            if(childView is FrameLayout) {
                childView.forEach {
                    if (fragment.isRemoving) return hasToolbar
                    if(it is MaterialButton) {
                        hasButton = hasGlobalPoint(it, ev.rawX.toInt(), ev.rawY.toInt())
                    }
                }
            }
        }
        return hasToolbar || hasButton
    }
    /**
     * ● 判断是否是 Classic中的Button
     *
     * ● 2023-09-03 23:25:45 周日 下午
     */
    private fun judgeIsClassicButton(ev: MotionEvent): Boolean {
        val fragment = supportFragmentManager.fragments.firstOrNull()
        if (fragment is BookClassicComicFragment) {
            super.dispatchTouchEvent(ev)
        }
        return true
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
            .addTransition(Slide(Gravity.TOP))
            .addTarget(mBinding.comicToolbar)
        TransitionManager.beginDelayedTransition(mBinding.root, transition)
        mBinding.comicToolbar.isGone = isHide
        mWindowInsetsCompat.isAppearanceLightStatusBars = !appIsDarkMode
        mWindowInsetsCompat.isAppearanceLightNavigationBars = !appIsDarkMode
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
        (mBinding.comicToolbar.background as MaterialShapeDrawable).apply {
            elevation = resources.getDimension(baseR.dimen.base_dp3)
            alpha = 242
            val color = ColorUtils.setAlphaComponent(resolvedTintColor, alpha)
            window.statusBarColor = color
            window.navigationBarColor = color
        }
    }
}
