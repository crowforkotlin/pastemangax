@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.crow.module_book.ui.fragment.comic.reader

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.launchDelay
import com.crow.base.tools.extensions.BASE_ANIM_300L
import com.crow.base.tools.extensions.error
import com.crow.base.tools.extensions.findCenterViewPosition
import com.crow.base.tools.extensions.onCollect
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.fragment.BaseMviFragment
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.viewmodel.doOnError
import com.crow.base.ui.viewmodel.doOnResult
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.module_book.databinding.BookFragmentComicPageBinding
import com.crow.module_book.model.database.model.BookChapterEntity
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.entity.comic.reader.ReaderEvent
import com.crow.module_book.model.entity.comic.reader.ReaderPageLoading
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicPageVerticalRvAdapter
import com.crow.module_book.ui.fragment.InfoFragment
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.crow.module_book.ui.viewmodel.comic.PagerLoader
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
class ComicPageVerticalFragment : BaseMviFragment<BookFragmentComicPageBinding>() {

    private val mVM by activityViewModel<ComicViewModel>()

    private var mAdapter: ComicPageVerticalRvAdapter?  = null

    private val mWindowInsetsControllerCompat by lazy {
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        )
    }

    private val mBaseEvent  = BaseEvent.newInstance()

    private var mCurrentChapterPageID = -1

    private var mReverse: Boolean = false

    override fun getViewBinding(inflater: LayoutInflater) = BookFragmentComicPageBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAdapter = ComicPageVerticalRvAdapter(viewLifecycleOwner) { uuid, isNext ->
            launchDelay(BASE_ANIM_300L) { mVM.input(BookIntent.GetComicPage(mVM.mPathword, uuid, isNext)) }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let { bundle -> mReverse = bundle.getBoolean("REVERSE", false) }
        super.onCreate(savedInstanceState)
    }

    override fun initView(savedInstanceState: Bundle?) {

        mBinding.pager.layoutManager = LinearLayoutManager((requireActivity() as ComicActivity), LinearLayoutManager.VERTICAL, mReverse)

        PagerSnapHelper().attachToRecyclerView(mBinding.pager)

        // Set RvAdapter
        mBinding.pager.adapter = mAdapter
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun initListener() {

        parentFragmentManager.setFragmentResultListener(ComicActivity.ROTATE, viewLifecycleOwner) { key, bundle ->
            if (mAdapter?.itemCount == 0) return@setFragmentResultListener
            requireActivity().apply {
                intent.putExtra(ComicActivity.ROTATE, true)
                getPosItem { index, pagePos, pageId, _ ->
                    when (requestedOrientation) {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                            updateUiState (pagePos, 0, pageId)
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                            updateUiState (pagePos, 0, pageId)
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                        else -> {
                            updateUiState (pagePos, 0, pageId)
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }
                    }
                }
            }
        }
        parentFragmentManager.setFragmentResultListener(ComicActivity.CHAPTER_POSITION, viewLifecycleOwner) { key, bundle ->
            val position = bundle.getInt(key)
            val positionOffset = bundle.getInt(ComicActivity.CHAPTER_POSITION_OFFSET)
            mBinding.pager.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewDetachedFromWindow(view: View) { }
                override fun onChildViewAttachedToWindow(view: View) {
                    mBinding.pager.removeOnChildAttachStateChangeListener(this)
//                    "Detached : $isDetached \t POSITION : $position \t OFFSET : $positionOffset".log()
                    if (isDetached || position >= (mAdapter?.itemCount ?: 0)) return
                    if (position == -1) {
                        mBinding.pager.post {
                            mBinding.pager.post {
                                mBinding.pager.scrollBy(0, if(mReverse) -resources.displayMetrics.heightPixels else resources.displayMetrics.heightPixels)
                                getPosItem { index, pagePos, pageId, itemPos -> updateUiState(-1, positionOffset, pageId) }
                            }
                        }
                        return
                    }
                    if (mBinding.pager.tag == null) {
                        mBinding.pager.tag = mBinding.pager
                        mBinding.pager.post(object : Runnable {
                            override fun run() {
                                val layoutManager = (mBinding.pager.layoutManager as LinearLayoutManager)
                                if (!layoutManager.isAttachedToWindow) return
                                layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition())?.apply {
                                    post {
                                        if (isDetached) return@post
                                        val realPosition = PagerLoader.obtainPagerPosition(mVM.mCurrentChapterPageKey, mVM.mChapterPageList, position)
                                        layoutManager.scrollToPosition(realPosition)
                                        mBinding.pager.post(object : Runnable {
                                            override fun run() {
                                                if (isDetached) return
                                                getPosItem(realPosition) { index, pagePos, pageId, itemPos ->
                                                    updateUiState(pagePos, positionOffset, pageId)
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        })
                    } else {
                        mBinding.pager.post(object : Runnable {
                            override fun run() {
                                mBinding.pager.scrollToPosition(position)
                                mBinding.pager.post {
                                    if (isDetached) return@post
                                    getPosItem(position) { index, pagePos, pageId, itemPos ->
                                        updateUiState(pagePos, positionOffset, pageId)
                                    }
                                }
                            }
                        })
                    }
                }
            })
        }

        parentFragmentManager.setFragmentResultListener(ComicActivity.SLIDE, this) { key, bundle ->
            if (isDetached) return@setFragmentResultListener
            mBinding.pager.post(object : Runnable {
                override fun run() {
                    val pos = bundle.getInt(key)
                    if (mAdapter?.itemCount == 0) return
                    getPosItem { index, pagePos, pageId, itemPos ->
                        mBinding.pager.scrollToPosition(index - pagePos + pos)
                        mBinding.pager.post {
                            if (isDetached) return@post
                            updateUiState(pos, 0, pageId)
                        }
                    }
                }
            })
        }

        parentFragmentManager.setFragmentResultListener(ComicActivity.FRAGMENT_OPTION, viewLifecycleOwner) { key, bundle ->
            when(bundle.getInt(ComicActivity.EVENT, -1)) {
                ReaderEvent.OPEN_DRAWER -> {
                    mBinding.pager.stopScroll()
                }
            }
        }

        mBinding.pager.setPreScrollListener { dx, dy, position ->
            mVM.onScroll(dy, position, 1)
        }

        mBinding.pager.setNestedPreScrollListener { dx, dy, position ->
            if (position < 0) return@setNestedPreScrollListener
            getPosItem(position) { _, pagePos, pageId, _ ->
                updateUiState(pagePos, 0, pageId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AppProgressFactory.clear()
        mAdapter = null
    }

    override fun initObserver(saveInstanceState: Bundle?) {
        mVM.onOutput { intent ->
            when (intent) {
                is BookIntent.GetComicPage -> {
                    intent.mViewState
                        .doOnError { _, _ ->
                            mAdapter?.submitList(PagerLoader.obtainErrorPages((mAdapter ?: return@doOnError).getCurrentList().toMutableList(), intent.isNext, mReverse) ?: return@doOnError) { }
                        }
                        .doOnResult {
                            if (intent.comicpage == null) mVM.mLoadingJob?.cancel()
                        }
                }
            }
        }

        mVM.mUnitPages.onCollect(this) {
            if (it == null) return@onCollect
            mAdapter?.submitList(PagerLoader.obtainPagerPages(
                mContext,
                mVM.mChapterPageList,
                mReverse
            ).toMutableList()) {
                mVM.mLoadingJob?.cancel()
            }
        }
    }

    private inline fun getPosItem(itemPos: Int? = null, invoke: (Int, Int, Int, Int?) -> Unit) {
        val pager = (mAdapter ?: error("Adapter is null!")).getCurrentList()
        if (pager.size <= 4) return
        val item: Any
        var index: Int? = null
        var itemCenterPos: Int? = null
        if (itemPos == null) {
            itemCenterPos = mBinding.pager.findCenterViewPosition()
            item = pager[itemCenterPos]
            index = pager.indexOf(item)
        } else {
            item = pager[itemPos]
        }
        val chapterPageID: Int
        val chapterPagePos: Int
        when (item) {
            is ReaderPageLoading -> {
                chapterPageID = item.mChapterID
                chapterPagePos = item.mChapterPagePos
            }
            is Content -> {
                chapterPageID = item.mChapterID
                chapterPagePos = item.mChapterPagePos + 1
            }
            else -> { error("unknow item type!") }
        }
        invoke(index ?: 0, chapterPagePos, chapterPageID, itemCenterPos)
    }

    data class Item(
        val mIndex: Int,
        val mPagePosition: Int,
        val mPageId: Int,
        val mItemPosition: Int
    )

    private fun updateUiState(pos: Int, offset: Int, chapterPageID: Int) {
        val reader = mVM.mChapterPageMapper[chapterPageID] ?: return
        if (mCurrentChapterPageID != chapterPageID) {
            mCurrentChapterPageID = chapterPageID
            lifecycleScope.launch {
                val chapter = reader.mChapterInfo
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
                mReaderMode = ComicCategories.Type.PAGE_VERTICAL_TTB,
                mReaderContent =  reader,
                mChapterID = chapterPageID,
                mTotalPages = reader.mPages.size,
                mCurrentPagePos = pos,
                mCurrentPagePosOffset = offset
            )
        )
    }

    private fun onErrorComicPage() {
        toast(getString(baseR.string.base_loading_error))
        BaseEvent.getSIngleInstance().setBoolean(InfoFragment.LOGIN_CHAPTER_HAS_BEEN_SETED, true)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}