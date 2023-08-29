package com.crow.module_book.ui.fragment.comic

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.R
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.logger
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.base.ui.viewmodel.doOnSuccess
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.ComicRvAdapter
import com.crow.module_book.ui.fragment.BookFragment
import com.crow.module_book.ui.view.comic.PageBadgeView
import com.crow.module_book.ui.view.comic.rv.ComicLayoutManager
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.fragment.comic
 * @Time: 2023/6/28 0:41
 * @Author: CrowForKotlin
 * @Description: BookStripComicFragment
 * @formatter:on
 **************************/
class BookStriptComicFragment : BaseMviFragment<BookFragmentComicBinding>() {

    private val mComicVM by sharedViewModel<ComicViewModel>()

    private val mComicRvAdapter: ComicRvAdapter by lazy { ComicRvAdapter() }

    private val mWindowInsetsControllerCompat by lazy {
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        )
    }

    private val mBadgeView: PageBadgeView by lazy {
        PageBadgeView(layoutInflater, viewLifecycleOwner).also { view ->
            mBinding.root.addView(view.mBadgeBinding.root)
            view.mBadgeBinding.root.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END or Gravity.TOP
                marginEnd = resources.getDimensionPixelSize(R.dimen.base_dp10)
                topMargin = (requireActivity() as ComicActivity).findViewById<MaterialToolbar>(com.crow.module_book.R.id.comic_toolbar).measuredHeight + resources.getDimensionPixelSize(R.dimen.base_dp10)
            }
        }
    }

    private val mBaseEvent  = BaseEvent.newInstance()

    override fun getViewBinding(inflater: LayoutInflater) =
        BookFragmentComicBinding.inflate(inflater)

    private fun showComicPage(contents: MutableList<Any>) {
        // FlowBus.with<String>(BaseEventEnum.UpdateChapter.name).post(lifecycleScope, comicPageResp.mChapter.mName)
        contents.logger()
        mComicRvAdapter.submitList(contents)
        if (mBadgeView.mBadgeBinding.root.isInvisible) mBadgeView.mBadgeBinding.root.animateFadeIn()
        if (mBinding.comicRv.isInvisible) mBinding.comicRv.animateFadeIn()
        mBadgeView.updateTotalCount(mComicVM.mContents.size)
    }

    private fun onErrorComicPage() {
        toast(getString(R.string.BaseLoadingError))
        BaseEvent.getSIngleInstance().setBoolean(BookFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun initData(savedInstanceState: Bundle?) {
        mComicVM.input(BookIntent.GetComicPage(mComicVM.mPathword ?: return, mComicVM.mUuid ?: return))
    }

    override fun initView(savedInstanceState: Bundle?) {

        // Set LayoutManager support zoom
        mBinding.comicRv.layoutManager = ComicLayoutManager(requireActivity() as ComicActivity)

        // Set RvAdapter
        mBinding.comicRv.adapter = mComicRvAdapter

        // Show ComicPage
        // showComicPage(mComicVM.mContents)
    }

    override fun initListener() {
        mBinding.comicRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy < 0) {
                    mComicVM.onScrollUp((mBinding.comicRv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
                } else {
                    mComicVM.onScrollDown((mBinding.comicRv.layoutManager as LinearLayoutManager).findLastVisibleItemPosition())
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mBinding.comicRv.stopScroll()
    }

    override fun initObserver(savedInstanceState: Bundle?) {

        mComicVM.mLoadedState.observe(this) {
            showComicPage(it)
        }
        mComicVM.onOutput { intent ->
            when (intent) {
                is BookIntent.GetComicPage -> {
                    intent.mBaseViewState
                        .doOnError { _, _ ->
                            onErrorComicPage()
                            mBaseEvent.setBoolean("loaded", false)
                        }
                        .doOnSuccess { mWindowInsetsControllerCompat.isAppearanceLightStatusBars = true }
                        .doOnResult {
                            intent.contents?.let {
                                showComicPage(it)
                                mBinding.comicRv.scrollBy(0, mContext.resources.getDimensionPixelSize(R.dimen.base_dp156))
                            }
                        }
                }
            }
        }
    }
}