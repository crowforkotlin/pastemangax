package com.crow.module_book.ui.fragment.comic

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.setMargins
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.R
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.animateFadeOutWithEndInVisibility
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnLoading
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.ComicRvAdapter
import com.crow.module_book.ui.fragment.BookFragment
import com.crow.module_book.ui.view.PageBadgeView
import com.crow.module_book.ui.view.WebtoonLayoutManager
import com.crow.module_book.ui.viewmodel.ComicViewModel
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

    private lateinit var mComicRvAdapter: ComicRvAdapter

    private val mBadgeView: PageBadgeView by lazy {
        PageBadgeView(layoutInflater, viewLifecycleOwner).also {  view ->
            mBinding.root.addView(view.mBadgeBinding.root)
            view.mBadgeBinding.root.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.END or Gravity.TOP
                setMargins(resources.getDimensionPixelSize(R.dimen.base_dp10))
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentComicBinding.inflate(inflater)

    private fun showComicPage(comicPageResp: ComicPageResp) {
        FlowBus.with<String>(BaseEventEnum.UpdateChapter.name).post(lifecycleScope, comicPageResp.mChapter.mName)
        mComicRvAdapter = ComicRvAdapter(
            mComicContent =  comicPageResp.mChapter.mWords.zip(comicPageResp.mChapter.mContents).sortedBy { it.first }.map { it.second }.toMutableList().also { it.add(null) },
            mHasNext = !comicPageResp.mChapter.mNext.isNullOrEmpty(),
            mHasPrev = !comicPageResp. mChapter.mPrev.isNullOrEmpty()
        ) {
            showLoadingAnim()
            mBinding.comicRv.animateFadeOutWithEndInVisibility()
            mBadgeView.mBadgeBinding.root.animateFadeOut().withEndAction {
                mComicVM.input(BookIntent.GetComicPage(comicPageResp.mChapter.mComicPathWord, comicPageResp.mChapter.mNext ?: return@withEndAction))
                mBadgeView.mBadgeBinding.root.isInvisible = true
            }
        }
        if (mBadgeView.mBadgeBinding.root.isInvisible) mBadgeView.mBadgeBinding.root.animateFadeIn()
        if (mBinding.comicRv.isInvisible) mBinding.comicRv.animateFadeIn()
        mBinding.comicRv.layoutManager = WebtoonLayoutManager(requireActivity() as ComicActivity)
        mBinding.comicRv.adapter = mComicRvAdapter
        mBadgeView.updateTotalCount(comicPageResp.mChapter.mContents.size)
    }

    private fun onErrorComicPage() {
        toast(getString(R.string.BaseLoadingError))
        BaseEvent.getSIngleInstance().setBoolean(BookFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun initView(savedInstanceState: Bundle?) {

        // 显示漫画页
        showComicPage(mComicVM.mComicPage ?: return)
    }

    override fun initListener() {
        mBinding.comicRv.setOnScrollChangeListener { _, _, _, _, _ ->
            val pos = (mBinding.comicRv.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 1
            if (pos <= (mComicVM.mComicPage?.mChapter?.mContents?.size ?: 0)) mBadgeView.updateCurrentPos(pos)
            if (mBadgeView.mBadgeBinding.root.isGone) {
                mBadgeView.mBadgeBinding.root.animateFadeIn()
                mBadgeView.updateTotalCount(mComicVM.mComicPage?.mChapter?.mContents?.size ?: -1)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.comicRv.stopScroll()
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mComicVM.mComicPage == null) { mComicVM.input(BookIntent.GetComicPage(mComicVM.mPathword ?: return, mComicVM.mUuid ?: return)) }
    }

    override fun initObserver(savedInstanceState: Bundle?) {
        mComicVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicPage -> {
                    intent.mBaseViewState
                        .doOnLoading { showLoadingAnim { dialog -> dialog.applyWindow(dimAmount = 0.3f) } }
                        .doOnError { _, _ -> dismissLoadingAnim { onErrorComicPage() } }
                        .doOnResult { dismissLoadingAnim { showComicPage(intent.comicPage!!) } }
                }
            }
        }
    }
}