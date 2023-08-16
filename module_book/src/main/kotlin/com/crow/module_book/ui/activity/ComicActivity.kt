package com.crow.module_book.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.crow.base.copymanga.BaseStrings
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.hasGlobalPoint
import com.crow.base.tools.extensions.immersionPadding
import com.crow.base.tools.extensions.immersureFullScreen
import com.crow.base.tools.extensions.immersureFullView
import com.crow.base.tools.extensions.immerureCutoutCompat
import com.crow.base.tools.extensions.isDarkMode
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.module_book.databinding.BookActivityComicBinding
import com.crow.module_book.ui.fragment.comic.BookComicCategories
import com.crow.module_book.ui.view.comic.GestureHelper
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.google.android.material.shape.MaterialShapeDrawable
import org.koin.androidx.viewmodel.ext.android.viewModel


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
    @SuppressLint("PrivateResource")
    override fun initView(savedInstanceState: Bundle?) {

        // 全屏
        immersureFullScreen(mWindowInsetsCompat)

        // 沉浸式边距
        immersionPadding(mBinding.root) { view, insets, _ ->
            mBinding.comicToolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = insets.top }
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }
        }

        // 沉浸式状态栏和工具栏
        (mBinding.comicToolbar.background as MaterialShapeDrawable).apply {
            elevation = resources.getDimension(com.google.android.material.R.dimen.m3_sys_elevation_level2)
            alpha = 242
            val color = ColorUtils.setAlphaComponent(resolvedTintColor, alpha)
            window.statusBarColor = color
            window.navigationBarColor = color
        }

        // 内存重启 后 savedInstanceState不为空 防止重复添加Fragment
        if (savedInstanceState == null) {

            // 条漫
            mComicCategory.apply(BookComicCategories.Type.STRIPT)
        }
    }

    /**
     * ● 初始化事件
     *
     * ● 2023-07-08 01:06:02 周六 上午
     */
    override fun initListener() {
        mGestureHelper =  GestureHelper(this, this)
    }

    /**
     * ● 初始化数据
     *
     * ● 2023-07-07 23:55:54 周五 下午
     */
    override fun initData() {
        mComicVM.mPathword = intent.getStringExtra(BaseStrings.PATH_WORD)
        mComicVM.mUuid = intent.getStringExtra(BaseStrings.UUID)
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
        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    /**
     * ● Activity Event onTouch
     *
     * ● 2023-07-07 23:56:56 周五 下午
     */
    override fun onTouch(area: Int) { transitionBar(mBinding.comicToolbar.isVisible) }

    /**
     * ● Activity Event dispatchTouchEvent
     *
     * ● 2023-07-07 23:57:39 周五 下午
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        mGestureHelper.dispatchTouchEvent(ev, !hasGlobalPoint(mBinding.comicToolbar, ev.rawX.toInt(), ev.rawY.toInt()))
        return super.dispatchTouchEvent(ev)
    }

    private fun transitionBar(isHide: Boolean) {
        val transition = TransitionSet()
            .setDuration(BASE_ANIM_300L)
            .addTransition(Slide(Gravity.TOP))
            .addTarget(mBinding.comicToolbar)
        TransitionManager.beginDelayedTransition(mBinding.root, transition)
        mBinding.comicToolbar.isGone = isHide
        mWindowInsetsCompat.isAppearanceLightStatusBars = !isDarkMode()
        if (isHide) {
            immersureFullScreen(mWindowInsetsCompat)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            mWindowInsetsCompat.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
