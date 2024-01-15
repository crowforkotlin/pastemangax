package com.crow.module_book.ui.fragment.comic.reader

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.crow.base.R
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.findCenterViewPosition
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderState
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicStriptRvAdapter
import com.crow.module_book.ui.fragment.BookFragment
import com.crow.module_book.ui.view.comic.rv.ComicLayoutManager
import com.crow.module_book.ui.viewmodel.ComicViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.fragment.comic
 * @Time: 2023/6/28 0:41
 * @Author: CrowForKotlin
 * @Description: BookStripComicFragment
 * @formatter:on
 **************************/
class ComicStriptFragment : BaseMviFragment<BookFragmentComicBinding>() {

    private val mVM by activityViewModel<ComicViewModel>()

    private val mAdapter: ComicStriptRvAdapter by lazy { ComicStriptRvAdapter() }

    private val mWindowInsetsControllerCompat by lazy {
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        )
    }

    private val mBaseEvent  = BaseEvent.newInstance()

    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentComicBinding.inflate(inflater)

    private suspend fun showComicPage(reader: ReaderContent) = coroutineScope {
        val setItemTask = async {
//             mAdapter.submitList(reader.mPages)
            yield()
        }
        if (mBinding.list.isInvisible) mBinding.list.animateFadeIn()
        setItemTask.await()
    }

    private fun onErrorComicPage() {
        toast(getString(R.string.base_loading_error))
        BaseEvent.getSIngleInstance().setBoolean(BookFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }


    override fun initView(savedInstanceState: Bundle?) {

        // Set LayoutManager support zoom
        mBinding.list.layoutManager = ComicLayoutManager(requireActivity() as ComicActivity)

        // Set RvAdapter
        mBinding.list.adapter = mAdapter

        // Show ComicPage
        // showComicPage(mComicVM.mContents)
    }

    override fun initListener() {
        mBinding.list.setPreScrollListener { position ->
            val item = mAdapter.getCurrentList()[position]
            mVM.onScroll(position, item)
            val reader = mVM.mContent.value
            val pageID: Int
            val pagePos: Int
            if(item is ReaderLoading) {
                pageID = item.mID
                pagePos = item.mPos
            } else if (item is Content) {
                pagePos = item.mPos
                pageID = item.mID
            } else {
                error("unknow item type!")
            }
            mVM.updateUiState(
                ReaderState(
                    mReaderContent = reader,
                    mTotalPages = mVM.mPagesSizeList[pageID],
                    mCurrentPage = pagePos
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.list.stopScroll()
    }

    override fun initObserver(savedInstanceState: Bundle?) {
        /*mVM.onOutput { intent ->
            when (intent) {
                is BookIntent.GetComicPage -> {
                    intent.mViewState
                        .doOnSuccess { mWindowInsetsControllerCompat.isAppearanceLightStatusBars = true }
                        .doOnError { _, _ ->
                            onErrorComicPage()
                            mBaseEvent.setBoolean("loaded", false)
                        }
                }
            }
        }*/
        lifecycleScope.launch {
            mVM.mContent.collect {
                mAdapter.submitList(it.mPages.toMutableList()) {
                    if (mAdapter.itemCount != 0) {
                        if (mBinding.list.tag == null) {
                            mBinding.list.scrollBy(0, resources.getDimensionPixelSize(baseR.dimen.base_dp96))
                        } else {
                            mBinding.list.tag = Unit
                        }
                    }
                }
            }
        }
    }
}