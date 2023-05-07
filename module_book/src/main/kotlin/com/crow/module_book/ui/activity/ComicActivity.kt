package com.crow.module_book.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import com.crow.base.copymanga.BaseStrings
import com.crow.base.tools.extensions.startActivity
import com.crow.base.ui.activity.BaseMviActivity
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.databinding.BookComicFragmentBinding
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.ui.adapter.ComicRvAdapter
import com.crow.module_book.ui.viewmodel.BookInfoViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ComicActivity : BaseMviActivity<BookComicFragmentBinding>() {

    companion object {
        fun newInstance(context: Context, comicPathWord: String, uuid: String) {
            context.startActivity<ComicActivity> {
                putExtra(BaseStrings.PATH_WORD, comicPathWord)
                putExtra("uuid", uuid   )
            }
        }
    }

    private lateinit var mComicRvAdapter: ComicRvAdapter

    private val mComicVM by viewModel<BookInfoViewModel>()

    private fun showComicPage(comicPageResp: ComicPageResp) {
        mComicRvAdapter = ComicRvAdapter(comicPageResp.mChapter.mContents.toMutableList())
        mBinding.comicRv.adapter = mComicRvAdapter
    }

    override fun getViewBinding() = BookComicFragmentBinding.inflate(layoutInflater)

    override fun onDestroy() {
        super.onDestroy()
        mComicVM.clearAllData()
    }

    override fun initData() {
        if (mComicVM.comicPage != null) return
        val pathword = intent?.getStringExtra(BaseStrings.PATH_WORD) ?: return
        val uuid = intent?.getStringExtra("uuid") ?: return
        mComicVM.input(BookIntent.GetComicPage(pathword, uuid))
    }

    override fun initView(savedInstanceState: Bundle?) {

        // 全屏布局
        WindowCompat.setDecorFitsSystemWindows(window, true)

        showComicPage(mComicVM.comicPage ?: return)
    }

    override fun initObserver(savedInstanceState: Bundle?) {
        mComicVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicPage -> {
                    intent.mViewState
                        .doOnLoading { showLoadingAnim() }
                        .doOnError { _, _ -> dismissLoadingAnim() }
                        .doOnResult { dismissLoadingAnim { showComicPage(intent.comicPage!!) } }
                }
                else -> {}
            }
        }
    }

    override fun onStart() {
        super.onStart()
        onBackPressedDispatcher.addCallback(this) {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
