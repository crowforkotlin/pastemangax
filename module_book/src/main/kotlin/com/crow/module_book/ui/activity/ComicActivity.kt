package com.crow.module_book.ui.activity

import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.view.WindowInsetsControllerCompat
import com.crow.base.copymanga.BaseStrings
import com.crow.base.tools.extensions.immersureFullScreen
import com.crow.base.tools.extensions.immersureFullView
import com.crow.base.tools.extensions.immerureCutoutCompat
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.module_book.databinding.BookActivityComicBinding
import com.crow.module_book.ui.fragment.comic.BookComicCategories
import com.crow.module_book.ui.viewmodel.ComicViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ComicActivity : BaseMviActivity<BookActivityComicBinding>() {

    private val mComicVM by viewModel<ComicViewModel>()

    private val mWindowInsetsCompat: WindowInsetsControllerCompat by lazy { WindowInsetsControllerCompat(window, mBinding.root) }

    private val mComicCategory by lazy { BookComicCategories(this, mBinding.comicFcv) }

    override fun getViewBinding() = BookActivityComicBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

        // 全屏
        immersureFullScreen(mWindowInsetsCompat)

        // 内存重启 后 savedInstanceState不为空 防止重复添加Fragment
        if (savedInstanceState == null) {

            // 条漫
            mComicCategory.apply(BookComicCategories.Type.STRIPT)
        }
    }

    /**
     * ● Called when the window focus changes. It sets the menu visibility to the last known state to apply immersive mode again if needed.
     *
     * ● 2023-06-25 01:59:38 周日 上午
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            immersureFullScreen(mWindowInsetsCompat)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersureFullView(window)
        immerureCutoutCompat(window)
    }

    override fun onResume() {
        super.onResume()
        immersureFullScreen(mWindowInsetsCompat)
    }

    override fun initData() {
        mComicVM.mPathword = intent.getStringExtra(BaseStrings.PATH_WORD)
        mComicVM.mUuid = intent.getStringExtra(BaseStrings.UUID)
    }

    override fun onStart() {
        super.onStart()
        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
