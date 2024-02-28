package com.crow.module_book.ui.fragment.comic.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.R
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.error
import com.crow.base.tools.extensions.findCenterViewPosition
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.database.model.BookChapterEntity
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicStriptRvAdapter
import com.crow.module_book.ui.fragment.InfoFragment
import com.crow.module_book.ui.viewmodel.ComicViewModel
import kotlinx.coroutines.launch
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

    private var mAdapter: ComicStriptRvAdapter?  = null

    private val mWindowInsetsControllerCompat by lazy {
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        )
    }

    private val mBaseEvent  = BaseEvent.newInstance()

    private var mCurrentChapterPageID = -1

    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentComicBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAdapter = ComicStriptRvAdapter { uuid, isNext ->
            launchDelay(BASE_ANIM_300L) { mVM.input(BookIntent.GetComicPage(mVM.mPathword, uuid, isNext)) }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView(savedInstanceState: Bundle?) {

        // Set LayoutManager support zoom
        mBinding.list.layoutManager = LinearLayoutManager(requireActivity() as ComicActivity)

        // Set RvAdapter
        mBinding.list.adapter = mAdapter
    }

    override fun initListener() {

        parentFragmentManager.setFragmentResultListener(ComicActivity.CHAPTER_POSITION, viewLifecycleOwner) { key, bundle ->
            val position = bundle.getInt(key)
            val positionOffset = bundle.getInt(ComicActivity.CHAPTER_POSITION_OFFSET)
            mBinding.list.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewDetachedFromWindow(view: View) { }
                override fun onChildViewAttachedToWindow(view: View) {
                    mBinding.list.removeOnChildAttachStateChangeListener(this)
                    "Detached : $isDetached \t POSITION : $position \t OFFSET : $positionOffset".log()
                    if (isDetached) return
                    if (position == -1) {
                        mBinding.list.post {
                            mBinding.list.post {
                                mBinding.list.scrollBy(0, resources.getDimensionPixelSize(baseR.dimen.base_dp192))
                                mBinding.list.post {
                                    getPosItem().apply {
                                        updateUiState(second, positionOffset, third)
                                    }
                                }
                            }
                        }
                        return
                    }
                    if (mBinding.list.tag == null) {
                        mBinding.list.tag = mBinding.list
                        mBinding.list.post {
                            (mBinding.list.layoutManager as LinearLayoutManager).apply {
                                if (!isAttachedToWindow) return@post
                                findViewByPosition(findFirstVisibleItemPosition())?.apply {
                                    post {
                                        if (isDetached) return@post
                                        scrollToPositionWithOffset(position, positionOffset)
                                        mBinding.list.post {
                                            if (isDetached) return@post
                                            val triple = getPosItem(position)
                                            updateUiState(triple.second, positionOffset, triple.third)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        mBinding.list.post {
                            mBinding.list.scrollToPosition(position)
                            mBinding.list.post {
                                if (isDetached) return@post
                                val triple = getPosItem(position)
                                updateUiState(triple.second, positionOffset, triple.third)
                            }
                        }
                    }
                }
            })
        }

        parentFragmentManager.setFragmentResultListener(ComicActivity.SLIDE, this) { key, bundle ->
            if (isDetached) return@setFragmentResultListener
            mBinding.list.post {
                val pos = bundle.getInt(key)
                val triple = getPosItem()
                mBinding.list.scrollToPosition(triple.first - triple.second + pos)
                mBinding.list.post {
                    if (isDetached) return@post
                    updateUiState(triple.second, (mBinding.list.layoutManager as LinearLayoutManager).findViewByPosition(triple.first)?.top ?: 0, triple.third)
                }
            }
        }

        mBinding.list.setPreScrollListener { dx, dy, position ->
            mVM.onScroll(dy, position)
        }
        mBinding.list.setNestedPreScrollListener { dx, dy, position ->
            if (position < 0) return@setNestedPreScrollListener
            val triple = getPosItem(position)
            val top = (mBinding.list.layoutManager as LinearLayoutManager).findViewByPosition(position)?.top ?: 0
            updateUiState(triple.second, top, triple.third)
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.list.stopScroll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdapter = null
    }

    override fun initObserver(saveInstanceState: Bundle?) {
        mVM.onOutput { intent ->
            when (intent) {
                is BookIntent.GetComicPage -> {
                    intent.mViewState
                        .doOnError { _, _ -> mVM.processErrorRequestPage(intent.isNext) }
                        .doOnResult {
                            if (intent.comicpage == null) mVM.mLoadingJob?.cancel()
                        }
                }
            }
        }

        mVM.mContent.onCollect(this) {
            mAdapter?.submitList(it.mPages.toMutableList()) {
                mVM.mLoadingJob?.cancel()
            }
        }
    }

    private fun getPosItem(itemPos: Int? = null): Triple<Int, Int, Int> {
        val list = (mAdapter ?: error("Adapter is null!")).getCurrentList()
        val item: Any
        var index: Int? = null
        if (itemPos == null) {
            val itemCenterPos = mBinding.list.findCenterViewPosition()
            item = list[itemCenterPos]
            index = list.indexOf(item)
        } else {
            item = list[itemPos]
        }
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
            else -> { error("unknow item type!") }
        }

        return Triple(index ?: 0, chapterPagePos, chapterPageID)
    }

    private fun updateUiState(currentPage: Int, offset: Int, chapterPageID: Int) {
        val reader = mVM.mPageContentMapper[chapterPageID] ?: return
        if (mCurrentChapterPageID != chapterPageID) {
            mCurrentChapterPageID = chapterPageID
            mVM.updateOriginChapterPage(chapterPageID)
            lifecycleScope.launch {
                val chapter = reader.mChapterInfo ?: return@launch
                FlowBus.with<BookChapterEntity>(BaseEventEnum.UpdateChapter.name).post(
                    BookChapterEntity(
                        mBookName = reader.mComicName,
                        mBookUuid = reader.mComicUuid,
                        mChapterType = BookType.COMIC,
                        mChapterName = chapter.mChapterName,
                        mChapterCurrentUuid = chapter.mChapterUuid,
                        mChapterNextUuid = chapter.mNextUUID,
                        mChapterPrevUuid = chapter.mPrevUUID
                    )
                )
            }
        }

        mVM.updateUiState(
            ReaderUiState(
                mReaderMode = ComicCategories.Type.STRIPT,
                mReaderContent =  reader,
                mChapterID = chapterPageID,
                mTotalPages = mVM.mPageSizeMapper[chapterPageID] ?: return,
                mCurrentPagePos = currentPage,
                mCurrentPagePosOffset = offset
            )
        )
    }

    private fun onErrorComicPage() {
        toast(getString(R.string.base_loading_error))
        BaseEvent.getSIngleInstance().setBoolean(InfoFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}