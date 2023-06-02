package com.crow.module_book.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.copymanga.BaseStrings
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.setMaskAmount
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.base.ui.dialog.LoadingAnimDialog
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.databinding.BookActivityComicBinding
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.ui.adapter.ComicRvAdapter
import com.crow.module_book.ui.view.PageBadgeView
import com.crow.module_book.ui.viewmodel.BookInfoViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.crow.base.R as baseR

class ComicActivity : BaseMviActivity<BookActivityComicBinding>() {

    var mPathword: String? = null

    private lateinit var mComicRvAdapter: ComicRvAdapter

    private val mComicVM by viewModel<BookInfoViewModel>()

    private var mBadgeView: PageBadgeView? = null

    private fun onShowComicPage(comicPageResp: ComicPageResp) {
        FlowBus.with<String>(BaseEventEnum.UpdateChapter.name).post(lifecycleScope, comicPageResp.mChapter.mName)
        val comicContents = comicPageResp.mChapter.mWords.zip(comicPageResp.mChapter.mContents).sortedBy { it.first }.map { it.second }.toMutableList()
        comicContents.add(null)
        mComicRvAdapter = ComicRvAdapter(comicContents, comicPageResp.mChapter.mNext != null, comicPageResp. mChapter.mPrev != null) {
            mComicVM.input(BookIntent.GetComicPage(comicPageResp.mChapter.mComicPathWord, comicPageResp.mChapter.mNext ?: return@ComicRvAdapter))
        }
        mBinding.comicRv.layoutManager = LinearLayoutManager(this)
        mBinding.comicRv.adapter = mComicRvAdapter
        mBadgeView?.apply {
            updateTotalCount(mComicRvAdapter.itemCount)
            mBadgeBinding.root.animateFadeIn()
        }
    }

    override fun getViewBinding() = BookActivityComicBinding.inflate(layoutInflater)

    override fun initData() {
        if (mComicVM.mComicPage != null) return
        mComicVM.input(BookIntent.GetComicPage((intent.getStringExtra(BaseStrings.PATH_WORD) ?: return).also { mPathword = it }, intent.getStringExtra(BaseStrings.UUID) ?: return))
    }

    override fun initView(savedInstanceState: Bundle?) {

        // 以便在刘海屏上使用刘海区域并适应窗口布局。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // 初始化BadgeView
        mBadgeView = PageBadgeView(this, mBinding)
        mBadgeView!!.mBadgeBinding.root.isInvisible = true

        // 显示漫画页
        onShowComicPage(mComicVM.mComicPage ?: return)
    }

    override fun initListener() {
        mBinding.comicRv.setOnScrollChangeListener { _, _, _, _, _ ->
            if (mBadgeView  != null) {
                val layoutManager = mBinding.comicRv.layoutManager
                if(layoutManager is LinearLayoutManager)  mBadgeView!!.updateCurrentPos(layoutManager.findLastVisibleItemPosition() + 1)
            }
        }
    }

    override fun initObserver(savedInstanceState: Bundle?) {
        mComicVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicPage -> {
                    intent.mBaseViewState
                        .doOnLoading { showLoadingAnim(object : LoadingAnimDialog.LoadingAnimConfig {
                                override fun isNoInitStyle(): Boolean = true
                                override fun doOnConfig(window: Window) { window.setMaskAmount(0.2f) }
                            }) }
                        .doOnError { _, _ -> dismissLoadingAnim { onErrorComicPage() } }
                        .doOnResult { dismissLoadingAnim { onShowComicPage(intent.comicPage!!) } }
                }
                else -> {}
            }
        }
    }

    private fun onErrorComicPage() {
        toast(getString(baseR.string.BaseLoadingError))
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mComicVM.clearAllData()
        mBadgeView = null
    }
}
