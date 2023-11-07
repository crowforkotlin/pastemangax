package com.crow.module_book.ui.fragment.comic.reader

import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.module_book.R
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo
import com.crow.module_book.model.entity.comic.reader.ReaderState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicClassicRvAdapter
import com.crow.module_book.ui.fragment.BookFragment
import com.crow.module_book.ui.view.comic.rv.ComicLayoutManager
import com.crow.module_book.ui.viewmodel.ComicViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.crow.base.R as baseR

class PageComicFragment : BaseMviFragment<BookFragmentComicBinding>() {

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
    private val mComicRvAdapter: ComicClassicRvAdapter = ComicClassicRvAdapter { reader ->
        val isUUIDEmpty = reader.mUUID.isNullOrEmpty()
        val message = when {
            reader.mIsNext && isUUIDEmpty -> getString(R.string.book_no_next)
            !reader.mIsNext && isUUIDEmpty -> getString(R.string.book_no_prev)
            else -> null
        }
        if (reader.mUUID == null || message != null) {
            return@ComicClassicRvAdapter toast(message ?: getString(baseR.string.BaseError, "uuid is null !"))
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

        mBinding.comicRv.adapter = mComicRvAdapter
        mBinding.comicRv.layoutManager = ComicLayoutManager(requireActivity() as ComicActivity)

        // 显示漫画页
        // showComicPage(mComicVM.mComicPage ?: return)
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-09-04 21:56:59 周一 下午
     */
    override fun initListener() {
        mBinding.comicRv.setOnScrollChangeListener { _, _, _, _, _ ->

            val reader = mVM.mContent.value
            mVM.updateUiState(ReaderState(
                mReaderContent = reader,
                mTotalPages = reader.mPages.size + 2,
                mCurrentPage = (mBinding.comicRv.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 1
            ))
        }
    }

    /**
     * ● Lifecycle OnPause Stop Rv Scroll
     *
     * ● 2023-11-05 02:28:14 周日 上午
     * @author crowforkotlin
     */
    override fun onPause() {
        super.onPause()
        mBinding.comicRv.stopScroll()
    }

    /**
     * ● 初始化观察者
     *
     * ● 2023-11-05 02:28:34 周日 上午
     * @author crowforkotlin
     */
    override fun initObserver(saveInstanceState: Bundle?) {

        mVM.mContent.onCollect(this) { reader ->
            if(reader.mPages.isNotEmpty()) {
                showComicPage(reader)
            }
        }
    }

    private suspend fun showComicPage(readerContent: ReaderContent) = coroutineScope {

        // wait util complete
        async {
            mComicRvAdapter.submitList(processedReaderPages(readerContent))
            yield()
        }.await()

    }

    private fun processedReaderPages(reader: ReaderContent): MutableList<Any> {
        if (reader.mInfo == null) return mutableListOf()
        val prevUUID = reader.mInfo.mPrevUUID
        val nextUUID = reader.mInfo.mNextUUID
        val prevInfo = if (prevUUID == null) getString(R.string.book_no_prev) else getString(R.string.book_prev)
        val nextInfo = if (nextUUID == null) getString(R.string.book_no_next) else getString(R.string.book_next)
        val pages = reader.mPages.toMutableList()
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
        toast(getString(baseR.string.BaseLoadingError))
        BaseEvent.getSIngleInstance().setBoolean(BookFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}