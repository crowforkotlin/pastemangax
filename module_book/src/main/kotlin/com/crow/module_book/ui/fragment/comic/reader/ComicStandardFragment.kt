@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.crow.module_book.ui.fragment.comic.reader

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.extensions.findCenterViewPosition
import com.crow.base.tools.extensions.findFisrtVisibleViewPosition
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.module_book.R
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.database.model.BookChapterEntity
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.entity.comic.reader.ReaderEvent
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicStandardRvAdapter
import com.crow.module_book.ui.fragment.InfoFragment
import com.crow.module_book.ui.fragment.comic.BaseComicFragment
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.crow.module_book.ui.viewmodel.comic.StandardLoader
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.crow.base.R as baseR
import com.crow.mangax.R as mangaR

class ComicStandardFragment : BaseComicFragment<BookFragmentComicBinding>() {

    /**
     * ⦁ 漫画VM
     *
     * ⦁ 2023-09-01 22:22:54 周五 下午
     */
    private val mVM by activityViewModel<ComicViewModel>()

    /**
     * ⦁ 漫画RV
     *
     * ⦁ 2023-09-04 21:56:28 周一 下午
     */
    private var mAdapter: ComicStandardRvAdapter? = null

    /**
     * ⦁ 获取VB
     *
     * ⦁ 2023-09-04 21:56:47 周一 下午
     */
    override fun getViewBinding(inflater: LayoutInflater) : BookFragmentComicBinding {

        return BookFragmentComicBinding.inflate(inflater)
    }

    /**
     * ⦁ INIT DATA
     *
     * ⦁ 2024-02-16 23:30:09 周五 下午
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
            mVM.input(BookIntent.GetComicPage(mVM.mPathword, reader.mUuid, isNext = reader.mIsNext, isReloadEnable = true))
        }
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * ⦁ 初始化视图
     *
     * ⦁ 2023-09-04 21:56:53 周一 下午
     */
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.list.adapter = mAdapter
        mBinding.list.layoutManager = LinearLayoutManager(requireActivity() as ComicActivity)
    }

    /**
     * ⦁ 初始化监听器
     *
     * ⦁ 2023-09-04 21:56:59 周一 下午
     */
    @SuppressLint("SourceLockedOrientationActivity")
    override fun initListener() {

        parentFragmentManager.setFragmentResultListener(ComicActivity.ROTATE, viewLifecycleOwner) { key, bundle ->
            if (mAdapter?.itemCount == 0) {
                toast(getString(R.string.book_prev_val))
                return@setFragmentResultListener
            }
            requireActivity().apply {
                intent.putExtra(ComicActivity.ROTATE, true)
                when (requestedOrientation) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                        updateUiState(isRotate = true, directionY = true)
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                        updateUiState(isRotate = true, directionY = false)
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                    else -> {
                        updateUiState(isRotate = true, directionY = true)
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
            }
        }

        parentFragmentManager.setFragmentResultListener(ComicActivity.CHAPTER_POSITION, viewLifecycleOwner) { key, bundle ->
            val position = bundle.getInt(key)
            val positionOffset = bundle.getInt(ComicActivity.CHAPTER_POSITION_OFFSET)
            mBinding.list.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewDetachedFromWindow(view: View) { }
                override fun onChildViewAttachedToWindow(view: View) {
                    mBinding.list.removeOnChildAttachStateChangeListener(this)
                    "Detached : $isDetached \t POSITION : $position \t OFFSET : $positionOffset".log()
                    if (isDetached || position >= (mAdapter?.itemCount ?: 0)) return
                    if (position == -1) {
                        mBinding.list.post {
                            (mBinding.list.layoutManager as LinearLayoutManager).apply {
                                if (!isAttachedToWindow) return@post
                                findViewByPosition(mBinding.list.findFisrtVisibleViewPosition())?.post {
                                    if (!isAttachedToWindow) return@post
                                    scrollToPosition(1)
                                    mBinding.list.post {
                                        getPosItem { index, pagePos, pageId, itemPos -> updateUiState(-1, positionOffset, pageId) }
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
                                findViewByPosition(mBinding.list.findFisrtVisibleViewPosition())?.post {
                                    if (!isAttachedToWindow) return@post
                                    scrollToPositionWithOffset(position + 1, positionOffset)
                                    mBinding.list.post {
                                        if (!isAttachedToWindow) return@post
                                        getPosItem(position) { index, pagePos, pageId, itemPos ->
                                            updateUiState(pagePos + 1, positionOffset, pageId)
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
                                getPosItem(position) { index, pagePos, pageId, itemPos ->
                                    updateUiState(pagePos, positionOffset, pageId)
                                }
                            }
                        }
                    }
                }
            })
        }

        parentFragmentManager.setFragmentResultListener(ComicActivity.SLIDE, viewLifecycleOwner) { key, bundle ->
            mBinding.list.post {
                if (mAdapter?.itemCount == 0) return@post
                mBinding.list.scrollToPosition(bundle.getInt(key))
                mBinding.list.post {
                    if (isDetached) return@post
                    updateUiState()
                }
            }
        }

        parentFragmentManager.setFragmentResultListener(ComicActivity.FRAGMENT_OPTION, viewLifecycleOwner) { key, bundle ->
            when(bundle.getInt(ComicActivity.EVENT, -1)) {
                ReaderEvent.OPEN_DRAWER -> {
                    mBinding.list.stopScroll()
                }
            }
        }

        mBinding.list.setNestedPreScrollListener { _, _, position ->
            getPosItem(position) { index, pagePos, pageId, itemPos ->
                val top = (mBinding.list.layoutManager as LinearLayoutManager).findViewByPosition(position)?.top ?: 0
                updateUiState(pagePos, top, pageId)
            }
//            updateUiState(pos = position)
        }
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
                            /*viewLifecycleScope {
                                withStarted {
                                    mBinding.list.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                                        override fun onChildViewDetachedFromWindow(view: View) { }
                                        override fun onChildViewAttachedToWindow(view: View) {
                                            mBinding.list.post { updateUiState() }
                                        }
                                    })
                                }
                            }*/
                            viewLifecycleScope {
                                val resp = intent.comicpage ?: return@viewLifecycleScope
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

        mVM.mPages.onCollect(this) { pages ->
            pages?.let {
                mAdapter?.submitList(StandardLoader.obtaintStandrdPages(mContext, mVM.mChapterPageMapper[mVM.mCurrentChapterPageKey] ?: return@let))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAdapter = null
    }


    private fun onErrorComicPage() {
        toast(getString(baseR.string.base_loading_error))
        BaseEvent.getSIngleInstance().setBoolean(InfoFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun updateUiState(offset: Int = -1, pos: Int = -1, isRotate: Boolean = false, directionY: Boolean = false) {
        val list = (mAdapter ?: return).getCurrentList()
        val centerViewPos = if(pos == -1) mBinding.list.findCenterViewPosition() else pos
        val chapterId : Int = when(val item = list.first()) {
            is ReaderPrevNextInfo -> item.mChapterID
            is Content -> item.mChapterID
            else -> error("unknow view type!")
        }
        var positionOffset: Float = offset.toFloat()
        if (offset == -1) {
            positionOffset = (mBinding.list.layoutManager as LinearLayoutManager).findViewByPosition(centerViewPos)?.top?.toFloat() ?: 0f
            if (isRotate) {
                positionOffset = if (directionY) {
                    positionOffset * (mBinding.list.width / mBinding.list.height.toFloat())
                } else {
                    positionOffset * (mBinding.list.height / mBinding.list.width.toFloat())
                }
            }
        }
        val offsetInt = positionOffset.toInt()
        mVM.mScrollPos = centerViewPos
        mVM.mScrollPosOffset = offsetInt
        mVM.updateUiState(ReaderUiState(
            mReaderMode = ComicCategories.Type.STANDARD,
            mReaderContent = mVM.mChapterPageMapper[chapterId] ?: return,
            mChapterID = chapterId,
            mTotalPages = list.size,
            mCurrentPagePos = centerViewPos,
            mCurrentPagePosOffset = offsetInt
        ))
    }

    private inline fun getPosItem(itemPos: Int? = null, invoke: (Int, Int, Int, Int?) -> Unit) {
        val list = (mAdapter ?: error("Adapter is null!")).getCurrentList()
        if (list.size <= 2) return
        val item: Any
        var index: Int? = null
        var itemCenterPos: Int? = null
        if (itemPos == null) {
            itemCenterPos = mBinding.list.findCenterViewPosition()
            item = list[itemCenterPos]
            index = list.indexOf(item)
        } else {
            item = list[itemPos]
        }
        val chapterPageID: Int
        val chapterPagePos: Int
        when (item) {
            is ReaderPrevNextInfo -> {
                chapterPageID = item.mChapterID
                chapterPagePos = item.mChapterPagePos
            }
            is Content -> {
                chapterPageID = item.mChapterID
                chapterPagePos = item.mChapterPagePos + 1
            }
            else -> {
                error("unknow item type!")
            }
        }
        mVM.mScrollPos = index ?: 0
        invoke(index ?: 0, chapterPagePos, chapterPageID, itemCenterPos)
    }

    private fun updateUiState(pos: Int, offset: Int, chapterPageID: Int) {
        mVM.mScrollPosOffset = offset
        val reader = mVM.mChapterPageMapper[chapterPageID] ?: return
        mVM.updateUiState(
            ReaderUiState(
                mReaderMode = ComicCategories.Type.STRIPT,
                mReaderContent =  reader,
                mChapterID = chapterPageID,
                mTotalPages = reader.mPages.size,
                mCurrentPagePos = pos,
                mCurrentPagePosOffset = offset
            )
        )
    }
}