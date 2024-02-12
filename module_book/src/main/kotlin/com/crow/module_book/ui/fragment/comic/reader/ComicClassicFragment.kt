package com.crow.module_book.ui.fragment.comic.reader

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.module_book.R
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.comic_page.Chapter
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicClassicRvAdapter
import com.crow.module_book.ui.fragment.InfoFragment
import com.crow.module_book.ui.view.comic.rv.ComicLayoutManager
import com.crow.module_book.ui.viewmodel.ComicViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.crow.base.R as baseR
import com.crow.mangax.R as mangaR

class ComicClassicFragment : BaseMviFragment<BookFragmentComicBinding>() {

    /**
     * ● 漫画VM
     *
     * ● 2023-09-01 22:22:54 周五 下午
     */
    private val mVM by activityViewModel<ComicViewModel>()

    /**
     * ● 漫画RV
     *
     * ● 2023-09-04 21:56:28 周一 下午
     */
    private val mAdapter: ComicClassicRvAdapter = ComicClassicRvAdapter { reader ->
        val isUUIDEmpty = reader.mUUID.isNullOrEmpty()
        val message = when {
            reader.mIsNext && isUUIDEmpty -> getString(R.string.book_no_next)
            !reader.mIsNext && isUUIDEmpty -> getString(R.string.book_no_prev)
            else -> null
        }
        if (reader.mUUID == null || message != null) {
            return@ComicClassicRvAdapter toast(message ?: getString(mangaR.string.mangax_error, "uuid is null !"))
        }
        mVM.input(BookIntent.GetComicPage(mVM.mPathword, reader.mUUID, enableLoading = true))
    }

    /**
     * ● 获取VB
     *
     * ● 2023-09-04 21:56:47 周一 下午
     */
    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentComicBinding.inflate(inflater)

    /**
     * ● 初始化视图
     *
     * ● 2023-09-04 21:56:53 周一 下午
     */
    override fun initView(savedInstanceState: Bundle?) {

        mBinding.list.adapter = mAdapter
        mBinding.list.layoutManager = ComicLayoutManager(requireActivity() as ComicActivity)

        // 显示漫画页
        // showComicPage(mComicVM.mComicPage ?: return)
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-09-04 21:56:59 周一 下午
     */
    override fun initListener() {
        mBinding.list.setPreScrollListener { _, _, _ -> updateUiState() }
    }

    override fun onPause() {
        super.onPause()
        mBinding.list.stopScroll()
    }

    override fun initObserver(saveInstanceState: Bundle?) {
        mVM.onOutput { intent ->
            when(intent) {
                is BookIntent.GetComicPage -> {
                    intent.mViewState
                        .doOnResult {
                            intent.comicpage?.let { resp ->
                                launchDelay(BASE_ANIM_300L) { updateUiState() }
                            }
                        }
                }
            }
        }
        mVM.mPages.onCollect(this) { pages ->
            if(pages != null) {
                mAdapter.submitList(processedReaderPages(pages))
            }
        }
    }

    private fun processedReaderPages(chapter: Chapter): MutableList<Any> {
        val prevUUID = chapter.mPrev
        val nextUUID = chapter.mNext
        val prevInfo = if (prevUUID == null) getString(R.string.book_no_prev) else getString(R.string.book_prev)
        val nextInfo = if (nextUUID == null) getString(R.string.book_no_next) else getString(R.string.book_next)
        val pages = mutableListOf<Any>()
        pages.addAll(chapter.mContents.toMutableList())
        pages.add(0, ReaderPrevNextInfo(
            mUUID = prevUUID,
            mInfo = prevInfo,
            mIsNext = false
        ))
        pages.add(ReaderPrevNextInfo(
            mUUID = nextUUID,
            mInfo = nextInfo,
            mIsNext = true
        ))
        return pages
    }

    private fun onErrorComicPage() {
        toast(getString(baseR.string.base_loading_error))
        BaseEvent.getSIngleInstance().setBoolean(InfoFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun updateUiState() {
        val reader: ReaderContent = mVM.mContent.value
        mVM.updateUiState(ReaderUiState(
            mReaderContent = reader,
            mTotalPages = reader.mPages.size + 1,
            mCurrentPage = (mBinding.list.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 1
        ))
    }
}