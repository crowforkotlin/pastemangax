package com.crow.module_book.ui.fragment.comic.reader

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import com.crow.base.R
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.findCenterViewPosition
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicStriptRvAdapter
import com.crow.module_book.ui.fragment.InfoFragment
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

    private val mAdapter: ComicStriptRvAdapter by lazy {
        ComicStriptRvAdapter { uuid, isNext ->
            launchDelay(BASE_ANIM_300L) { mVM.input(BookIntent.GetComicPage(mVM.mPathword, uuid, isNext)) }
        }
    }

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
        BaseEvent.getSIngleInstance().setBoolean(InfoFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }


    override fun initView(savedInstanceState: Bundle?) {

        // Set LayoutManager support zoom
        mBinding.list.layoutManager = ComicLayoutManager(requireActivity() as ComicActivity)

        // Set RvAdapter
        mBinding.list.adapter = mAdapter
    }

    override fun initListener() {

        parentFragmentManager.setFragmentResultListener(ComicActivity.SLIDE, this) { key, bundle ->
            val pos = bundle.getInt(key)
            val itemCenterPos = mBinding.list.findCenterViewPosition()
            val list = mAdapter.getCurrentList()
            val item = list[itemCenterPos]
            list[itemCenterPos]
            val index = list.indexOf(item)
            val chapterPageID: Int
            val chapterPagePos: Int
            when (item) {
                is ReaderLoading -> {
                    chapterPageID = item.mChapterID
                    chapterPagePos = item.mChapterPagePos
                }
                is Content -> {
                    chapterPageID = item.mChapterID
                    chapterPagePos = item.mChapterPagePos
                }
                else -> {
                    error("unknow item type!")
                }
            }
            mBinding.list.scrollToPosition(index - chapterPagePos + pos)
            mVM.updateUiState(
                ReaderUiState(
                    mReaderContent = mVM.mReaderContents[chapterPageID] ?: return@setFragmentResultListener,
                    mTotalPages = mVM.mPageSizeMapper[chapterPageID] ?: return@setFragmentResultListener,
                    mCurrentPage = chapterPagePos
                )
            )
        }

        mBinding.list.setPreScrollListener { dx, dy, position ->
            val item = mAdapter.getCurrentList()[position]
            val chapterPageID: Int
            val chapterPagePos: Int
            when (item) {
                is ReaderLoading -> {
                    chapterPageID = item.mChapterID
                    chapterPagePos = item.mChapterPagePos
                }
                is Content -> {
                    chapterPageID = item.mChapterID
                    chapterPagePos = item.mChapterPagePos
                }
                else -> {
                    error("unknow item type!")
                }
            }
            mVM.onScroll(dy, position)
            mVM.updateUiState(
                ReaderUiState(
                    mReaderContent = mVM.mReaderContents[chapterPageID] ?: return@setPreScrollListener,
                    mTotalPages = mVM.mPageSizeMapper[chapterPageID] ?: return@setPreScrollListener,
                    mCurrentPage = chapterPagePos
                )
            )
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.list.stopScroll()
    }

    override fun initObserver(savedInstanceState: Bundle?) {
        mVM.onOutput { intent ->
            when (intent) {
                is BookIntent.GetComicPage -> {
                    intent.mViewState
                        .doOnError { _, _ -> mVM.processErrorRequestPage(intent.isNext) }
                }
            }
        }

        lifecycleScope.launch {
            mVM.mContent.collect {
                mAdapter.submitList(it.mPages.toMutableList()) {
                    mVM.mLoadingJob?.cancel()
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