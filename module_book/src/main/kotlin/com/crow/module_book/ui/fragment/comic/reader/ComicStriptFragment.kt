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
import com.crow.mangax.copymanga.okhttp.AppProgressFactory
import com.crow.module_book.databinding.BookFragmentComicBinding
import com.crow.module_book.model.database.model.BookChapterEntity
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.entity.comic.reader.ReaderEvent
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.ui.activity.ComicActivity
import com.crow.module_book.ui.adapter.comic.reader.ComicStriptRvAdapter
import com.crow.module_book.ui.fragment.InfoFragment
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.crow.module_book.ui.viewmodel.comic.StriptLoader
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

    @SuppressLint("SourceLockedOrientationActivity")
    override fun initListener() {

        parentFragmentManager.setFragmentResultListener(ComicActivity.ROTATE, viewLifecycleOwner) { key, bundle ->
            if (mAdapter?.itemCount == 0) return@setFragmentResultListener
            requireActivity().apply {
                intent.putExtra(ComicActivity.ROTATE, true)
                getPosItem { index, pagePos, pageId, _ ->
                    val layoutManager = mBinding.list.layoutManager as LinearLayoutManager

                    when (requestedOrientation) {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                            val offset = layoutManager.findViewByPosition(mBinding.list.findCenterViewPosition())?.top
                                ?.run { this * (mBinding.list.width.toFloat() / mBinding.list.height) } ?: 0
                            updateUiState (pagePos, offset.toInt() , pageId)
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                            val offset = layoutManager.findViewByPosition(mBinding.list.findCenterViewPosition())?.top
                                ?.run { this * (mBinding.list.height.toFloat() / mBinding.list.width) } ?: 0
                            updateUiState (pagePos, offset.toInt() , pageId)
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                        else -> {
                            val offset = layoutManager.findViewByPosition(mBinding.list.findCenterViewPosition())?.top
                                ?.run { this * (mBinding.list.width.toFloat() / mBinding.list.height) } ?: 0
                            updateUiState (pagePos, offset.toInt() , pageId)
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }
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
//                    "Detached : $isDetached \t POSITION : $position \t OFFSET : $positionOffset".log()
                    if (isDetached || position >= (mAdapter?.itemCount ?: 0)) return
                    if (position == -1) {
                        mBinding.list.post {
                            mBinding.list.post {
                                mBinding.list.scrollBy(0, resources.getDimensionPixelSize(baseR.dimen.base_dp192))
                                getPosItem { index, pagePos, pageId, itemPos -> updateUiState(-1, positionOffset, pageId) }
                            }
                        }
                        return
                    }
                    if (mBinding.list.tag == null) {
                        mBinding.list.tag = mBinding.list
                        mBinding.list.post(object : Runnable {
                            override fun run() {
                                val layoutManager = (mBinding.list.layoutManager as LinearLayoutManager)
                                if (!layoutManager.isAttachedToWindow) return
                                layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition())?.apply {
                                    post {
                                        if (isDetached) return@post
                                        val realPos = StriptLoader.obtaintCurrentPos(mVM.mCurrentChapterPageKey, mVM.mChapterPageList, position)
                                        layoutManager.scrollToPositionWithOffset(realPos, positionOffset)
                                        mBinding.list.post(object : Runnable {
                                            override fun run() {
                                                if (isDetached) return
                                                getPosItem(realPos) { index, pagePos, pageId, itemPos ->
                                                    updateUiState(pagePos, positionOffset, pageId)
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        })
                    } else {
                        mBinding.list.post(object : Runnable {
                            override fun run() {
                                val realPos = StriptLoader.obtaintCurrentPos(mVM.mCurrentChapterPageKey, mVM.mChapterPageList, position)
                                mBinding.list.scrollToPosition(position)
                                mBinding.list.post {
                                    if (isDetached) return@post
                                    getPosItem(realPos) { index, pagePos, pageId, itemPos ->
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
            mBinding.list.post(object : Runnable {
                override fun run() {
                    val pos = bundle.getInt(key)
                    if (mAdapter?.itemCount == 0) return
                    getPosItem { index, pagePos, pageId, itemPos ->
                        mBinding.list.scrollToPosition(index - pagePos + pos)
                        mBinding.list.post {
                            if (isDetached) return@post
                            val view = (mBinding.list.layoutManager as LinearLayoutManager).findViewByPosition(index)
                            updateUiState(pos, view?.top ?: 0, pageId)
                        }
                    }
                }
            })
        }

        parentFragmentManager.setFragmentResultListener(ComicActivity.FRAGMENT_OPTION, viewLifecycleOwner) { key, bundle ->
            when(bundle.getInt(ComicActivity.EVENT, -1)) {
                ReaderEvent.OPEN_DRAWER -> {
                    mBinding.list.stopScroll()
                }
            }
        }

        mBinding.list.setPreScrollListener { dx, dy, position ->
            mVM.onScroll(dy, position, 2)
        }

        mBinding.list.setNestedPreScrollListener { dx, dy, position ->
            if (position < 0) return@setNestedPreScrollListener
            getPosItem(position) { _, pagePos, pageId, _ ->
                val top = (mBinding.list.layoutManager as LinearLayoutManager).findViewByPosition(position)?.top ?: 0
                updateUiState(pagePos, top, pageId)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mBinding.list.stopScroll()
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
                            mAdapter?.submitList(StriptLoader.obtainErrorPages((mAdapter ?: return@doOnError).getCurrentList().toMutableList(), intent.isNext) ?: return@doOnError) { }
                        }
                        .doOnResult {
                            if (intent.comicpage == null) mVM.mLoadingJob?.cancel()
                        }
                }
            }
        }

        mVM.mUnitPages.onCollect(this) {
            if (it == null) return@onCollect
            mAdapter?.submitList(StriptLoader.obtaintStriptPages(mContext, mVM.mChapterPageList)) {
                mVM.mLoadingJob?.cancel()
            }
        }

//        mVM.mContent.onCollect(this) {
//            mAdapter?.submitList(it.mPages.toMutableList()) {
//                mVM.mLoadingJob?.cancel()
//            }
//        }
    }

    private inline fun getPosItem(itemPos: Int? = null, invoke: (Int, Int, Int, Int?) -> Unit) {
        val list = (mAdapter ?: error("Adapter is null!")).getCurrentList()
        if (list.size <= 4) return
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
            is ReaderLoading -> {
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
                mReaderMode = ComicCategories.Type.STRIPT,
                mReaderContent =  reader,
                mChapterID = chapterPageID,
                mTotalPages = reader.mPages.size,
                mCurrentPagePos = pos,
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