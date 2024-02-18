package com.crow.module_book.ui.fragment.comic.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.findCenterViewPosition
import com.crow.base.tools.extensions.findFisrtVisibleViewPosition
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.module_book.R
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.database.model.BookChapterEntity
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.comic_page.Chapter
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicStandardRvAdapter
import com.crow.module_book.ui.fragment.InfoFragment
import com.crow.module_book.ui.viewmodel.ComicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.crow.base.R as baseR
import com.crow.mangax.R as mangaR

class ComicStandardFragment : BaseMviFragment<BookFragmentComicBinding>() {

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
    private var mAdapter: ComicStandardRvAdapter? = null

    /**
     * ● 获取VB
     *
     * ● 2023-09-04 21:56:47 周一 下午
     */
    override fun getViewBinding(inflater: LayoutInflater) : BookFragmentComicBinding {

        return BookFragmentComicBinding.inflate(inflater)
    }

    /**
     * ● INIT DATA
     *
     * ● 2024-02-16 23:30:09 周五 下午
     * @author crowforkotlin
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAdapter = ComicStandardRvAdapter { reader ->
            val isUUIDEmpty = reader.mUuid.isNullOrEmpty()
            val message = when {
                reader.mIsNext && isUUIDEmpty -> getString(R.string.book_no_next)
                !reader.mIsNext && isUUIDEmpty -> getString(R.string.book_no_prev)
                else -> null
            }
            if (reader.mUuid == null || message != null) { return@ComicStandardRvAdapter toast(message ?: getString(mangaR.string.mangax_error, "uuid is null !")) }
            mVM.input(BookIntent.GetComicPage(mVM.mPathword, reader.mUuid, enableLoading = true))
        }
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * ● 初始化视图
     *
     * ● 2023-09-04 21:56:53 周一 下午
     */
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.list.adapter = mAdapter
        mBinding.list.layoutManager = LinearLayoutManager(requireActivity() as ComicActivity)
    }

    /**
     * ● 初始化监听器
     *
     * ● 2023-09-04 21:56:59 周一 下午
     */
    override fun initListener() {

        parentFragmentManager.setFragmentResultListener(ComicActivity.CHAPTER_POSITION, viewLifecycleOwner) { key, bundle ->
            val position = bundle.getInt(key)
            val positionOffset = bundle.getInt(ComicActivity.CHAPTER_POSITION_OFFSET)
            mBinding.list.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewDetachedFromWindow(view: View) { }
                override fun onChildViewAttachedToWindow(view: View) {
                    mBinding.list.removeOnChildAttachStateChangeListener(this)
                    if (position == -1) {
                        mBinding.list.post { updateUiState(positionOffset) }
                        return
                    }
                    mBinding.list.post {
                        (mBinding.list.layoutManager as LinearLayoutManager).apply {
                            findViewByPosition(mBinding.list.findFisrtVisibleViewPosition())?.post {
                                scrollToPositionWithOffset(position, positionOffset)
                                mBinding.list.post { updateUiState(positionOffset) }
                            }
                        }
                    }
                }
            })
        }

        parentFragmentManager.setFragmentResultListener(ComicActivity.SLIDE, this) { key, bundle ->
            mBinding.list.scrollToPosition(bundle.getInt(key))
            updateUiState()
        }

        mBinding.list.setNestedPreScrollListener { _, _, _ -> updateUiState() }
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
                            mBinding.list.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                                override fun onChildViewDetachedFromWindow(view: View) { }
                                override fun onChildViewAttachedToWindow(view: View) {
                                    mBinding.list.removeOnChildAttachStateChangeListener(this)
                                    mBinding.list.post { updateUiState() }
                                }
                            })
                            lifecycleScope.launch {
                                val resp = intent.comicpage ?: return@launch
                                val comic = resp.mComic
                                val chapter = resp.mChapter
                                FlowBus.with<BookChapterEntity>(BaseEventEnum.UpdateChapter.name).post(
                                    BookChapterEntity(
                                        mBookName = comic.mName,
                                        mBookUuid = comic.mUuid,
                                        mChapterType = BookType.COMIC,
                                        mChapterName = chapter.mName,
                                        mChapterCurrentUuid = chapter.mUuid,
                                        mChapterNextUuid = chapter.mNext,
                                        mChapterPrevUuid = chapter.mPrev
                                    )
                                )
                            }
                        }
                }
            }
        }

        viewLifecycleScope {
            mVM.mPages.collect { pages ->
                pages?.let {
                    mAdapter?.submitList(processedReaderPages(it))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdapter = null
    }

    private fun processedReaderPages(chapter: Chapter): MutableList<Any> {
        val prevUUID = chapter.mPrev
        val nextUUID = chapter.mNext
        val prevInfo = if (prevUUID == null) getString(R.string.book_no_prev) else getString(R.string.book_prev)
        val nextInfo = if (nextUUID == null) getString(R.string.book_no_next) else getString(R.string.book_next)
        val pages: MutableList<Any> = chapter.mContents.toMutableList()
        val chapterID = (pages.first() as Content).mChapterID
        pages.add(0, ReaderPrevNextInfo(
            mChapterID = chapterID,
            mUuid = prevUUID,
            mInfo = prevInfo,
            mIsNext = false
        ))
        pages.add(ReaderPrevNextInfo(
            mChapterID = chapterID,
            mUuid = nextUUID,
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

    private fun updateUiState(offset: Int = -1) {
        val list = (mAdapter ?: return).getCurrentList()
        val centerViewPos = mBinding.list.findCenterViewPosition()
        val chapterId : Int = when(val item = list.first()) {
            is ReaderPrevNextInfo -> item.mChapterID
            is Content -> item.mChapterID
            else -> error("unknow view type!")
        }
        var positionOffset = offset
        if (offset == -1) {
            positionOffset = (mBinding.list.layoutManager as LinearLayoutManager).findViewByPosition(centerViewPos)?.top ?: 0
        }
        mVM.updateUiState(ReaderUiState(
            mReaderContent = mVM.mPageContentMapper[chapterId] ?: return,
            mChapterID = chapterId,
            mTotalPages = list.size,
            mCurrentPagePos = centerViewPos,
            mCurrentPagePosOffset = positionOffset
        ))
    }
}