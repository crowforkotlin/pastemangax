package com.crow.module_book.ui.fragment.comic

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.R
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.tools.coroutine.FlowBus
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

    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentComicBinding.inflate(inflater)

    private fun showComicPage(comicPageResp: ComicPageResp) {
        FlowBus.with<String>(BaseEventEnum.UpdateChapter.name).post(lifecycleScope, comicPageResp.mChapter.mName)

        val comicContents = comicPageResp.mChapter.mWords.zip(comicPageResp.mChapter.mContents).sortedBy { it.first }.map { it.second }.toMutableList().also { it.add(null) }
        mComicRvAdapter = ComicRvAdapter(comicContents, comicPageResp.mChapter.mNext != null, comicPageResp. mChapter.mPrev != null) {
            mComicVM.input(BookIntent.GetComicPage(comicPageResp.mChapter.mComicPathWord, comicPageResp.mChapter.mNext ?: return@ComicRvAdapter))
        }
        mBinding.comicRv.layoutManager = WebtoonLayoutManager(requireActivity() as ComicActivity)
        mBinding.comicRv.adapter = mComicRvAdapter
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
            mComicVM.onPageScrollChanged((mBinding.comicRv.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 1)
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mComicVM.mComicPage == null) { mComicVM.input(BookIntent.GetComicPage(mComicVM.mPathword ?: return, mComicVM.mUuid ?: return)) }
    }

    override fun initObserver(savedInstanceState: Bundle?) {
        mComicVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicPage -> {
                    intent.mBaseViewState
                        .doOnLoading { showLoadingAnim { dialog ->
                            dialog.applyWindow(dimAmount = 0.3f)
                        } }
                        .doOnError { _, _ -> dismissLoadingAnim { onErrorComicPage() } }
                        .doOnResult { dismissLoadingAnim { showComicPage(intent.comicPage!!) } }
                }
                else -> {}
            }
        }
    }
}