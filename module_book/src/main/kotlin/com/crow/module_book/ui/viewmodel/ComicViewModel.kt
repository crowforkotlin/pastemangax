package com.crow.module_book.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.crow.base.app.app
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.createCoroutineExceptionHandler
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_book.R
import com.crow.module_book.model.entity.BookChapterEntity
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderInfo
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.network.BookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.crow.base.R as baseR

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.viewmodel
 * @Time: 2023/6/28 22:06
 * @Author: CrowForKotlin
 * @Description: ComicViewModel
 * @formatter:on
 **************************/
class ComicViewModel(val repository: BookRepository) : BaseMviViewModel<BookIntent>() {

    companion object {
        const val UUID = "uuid"
        const val PREV_UUID = "prev_uuid"
        const val NEXT_UUID = "next_uuid"
        const val CHAPTER_LOADED_THRESHOLD = 4
        const val CHAPTER_PRELOADED_INDEX = 4
    }

    /**
     * ● 漫画关键字
     *
     * ● 2023-06-28 22:04:54 周三 下午
     */
    lateinit var mPathword: String

    /**
     * ● 漫画UID
     *
     * ● 2023-06-28 22:04:58 周三 下午
     */
    lateinit var mUuid: String



    /**
     * ● 下一章和上一章漫画UID
     *
     * ● 2023-08-29 23:16:17 周二 下午
     */
    var mPrevUuid: String? = null
    var mNextUuid: String? = null
    var mLoadingJob: Job? = null
    private var mIsNext: Boolean = false


    /**
     * ● 漫画内容
     *
     * ● 2023-09-01 00:51:58 周五 上午
     */
    private val _mContent = MutableStateFlow(ReaderContent("", "", "", emptyList(), null))
    val mContent: StateFlow<ReaderContent> get() = _mContent

    /**
     * ● ChapterPageID to ReaderContent
     *
     * ● 2024-01-15 23:46:34 周一 下午
     * @author crowforkotlin
     */
    private val _mReaderContents: HashMap<Int, ReaderContent> = hashMapOf()
    val mReaderContents: Map<Int, ReaderContent> get() = _mReaderContents

    /**
     * ● UI State For InfoBar
     *
     * ● 2023-09-02 22:03:53 周六 下午
     */
    private val _uiState = MutableStateFlow<ReaderState?>(null)
    val uiState : StateFlow<ReaderState?> get() = _uiState

    /**
     * ● 旋转角度
     *
     * ● 2023-09-04 01:33:44 周一 上午
     */
    var mOrientation = app.resources.configuration.orientation

    /**
     * ● 页面大小列表
     *
     * ● 2024-01-14 23:46:06 周日 下午
     * @author crowforkotlin
     */
    var mPagesSizeList = mutableListOf<Int>()

    /**
     * ● 章节页面ID
     *
     * ● 2024-01-15 23:45:55 周一 下午
     * @author crowforkotlin
     */
    private var mChapterPageID: Int = 0

    /**
     * ● 通过检查意图的类型并执行相应的代码来处理意图
     *
     * ● 2023-06-28 22:08:41 周三 下午
     */
    override fun dispatcher(intent: BookIntent) {
        when(intent) {
            is BookIntent.GetComicPage -> {
                viewModelScope.launch(createCoroutineExceptionHandler("GetComicPage Catch")) {
                    getComicPage(intent)
                }
            }
        }
    }

    /**
     * ● 获取漫画页
     *
     * ● 2023-06-28 22:17:41 周三 下午
     */
    private suspend fun getComicPage(intent: BookIntent.GetComicPage, isNext: Boolean? = null) {
        val result = flowResult(repository.getComicPage(intent.pathword, intent.uuid), intent) { value ->
            val readerContent = getReaderContent(value.mResults)
            val _intent = if (isNext == null) {
                intent.copy(comicpage = value.mResults)
            } else {
                intent.copy(comicpage = value.mResults)
            }
            _mContent.value = readerContent
            _intent
        }
        val chapter = result.mResults.mChapter
        FlowBus.with<BookChapterEntity>(BaseEventEnum.UpdateChapter.name).post(
            BookChapterEntity(
                mBookName = result.mResults.mComic.mName,
                mChapterType = BookType.COMIC,
                mChapterName = chapter.mName,
                mChapterUUID = chapter.mUuid,
                mChapterNextUUID = chapter.mNext,
                mChapterPrevUUID = chapter.mPrev
            )
        )
    }

    /**
     * ● 获取阅读内容实体
     *
     * ● 2023-09-02 19:51:53 周六 下午
     */
    private fun getReaderContent(resp: ComicPageResp): ReaderContent {
        return with(resp) {
            val pages = getPages()
            val reader = ReaderContent(
                mComicName = mComic.mName,
                mComicUUID = mComic.mPathWord,
                mComicPathword = mComic.mPathWord,
                mPages = pages,
                mChapterInfo =  ReaderInfo(
                    mChapterIndex = mChapter.mIndex,
                    mChapterID = mChapter.mUuid,
                    mChapterName = mChapter.mName,
                    mChapterCount = mChapter.mCount,
                    mChapterUpdate = mChapter.mDatetimeCreated,
                    mPrevUUID = mChapter.mPrev,
                    mNextUUID = mChapter.mNext
                )
            )
            _mReaderContents[mChapterPageID] = reader
            reader
        }
    }

    private fun ComicPageResp.getPages(): MutableList<Any> {
        var currentPages: MutableList<Any> = mContent.value.mPages.toMutableList()
        val nextChapter = app.getString(R.string.book_next_val, mChapter.mName)
        val lastChapter = app.getString(R.string.book_prev_val, mChapter.mName)
        val noNextChapter = app.getString(R.string.book_no_next)
        val noLastChapter = app.getString(R.string.book_no_prev)
        val loading = app.getString(baseR.string.base_loading)
        if (currentPages.isEmpty()) {
            val pageID = mChapterPageID
            val pages = createChapterPages()
            val pagesSize = pages.size
            currentPages = pages
            currentPages.add(0, ReaderLoading(pageID, 1, nextChapter, mPrevUuid, mNextUuid))
            currentPages.add(ReaderLoading(pageID, pagesSize + 2, lastChapter, mPrevUuid, mNextUuid))
            var pageSizeTemp = 0
            if (mPrevUuid == null) {
                pageSizeTemp = 3
                currentPages.add(0, ReaderLoading(pageID, 1, noLastChapter, null, mNextUuid))
            } else {
                pageSizeTemp = 2
                currentPages.add(0, ReaderLoading(pageID, 1, loading, mPrevUuid, mNextUuid))
            }
            if (mNextUuid == null) {
                pageSizeTemp ++
                currentPages.add(ReaderLoading(pageID, pagesSize + 3, noNextChapter, mPrevUuid, null))
            } else {
                currentPages.add(ReaderLoading(pageID, pagesSize + 3, loading, mPrevUuid, mNextUuid))
            }
            mPagesSizeList.add(pagesSize + pageSizeTemp)
        } else {
            if (mIsNext) {
                mNextUuid = mChapter.mNext
                mChapterPageID += 1
                val pageID = mChapterPageID
                val pages = createChapterPages()
                val pagesSize = pages.size
                if (mNextUuid == null) {
                    mPagesSizeList.add(pagesSize + 3)
                    val last = currentPages.last()
                    if (last is ReaderLoading) {
                        currentPages.removeLast()
                        currentPages.add(ReaderLoading(pageID, 1, nextChapter, mPrevUuid, mNextUuid))
                        currentPages.addAll(pages)
                        currentPages.add(ReaderLoading(pageID, pagesSize + 2, lastChapter, mPrevUuid, mNextUuid))
                        currentPages.add(ReaderLoading(pageID, pagesSize + 3, noNextChapter, mPrevUuid, mNextUuid))
                    }
                } else {
                    mPagesSizeList.add(pagesSize + 2)
                    val last = currentPages.last()
                    if (last is ReaderLoading) {
                        currentPages.removeLast()
                        currentPages.add(ReaderLoading(pageID, 1, nextChapter, mPrevUuid, mNextUuid))
                        currentPages.addAll(pages)
                        currentPages.add(ReaderLoading(pageID, pagesSize + 1, lastChapter, mPrevUuid, mNextUuid))
                        currentPages.add(ReaderLoading(pageID, pagesSize + 2, loading, mPrevUuid, mNextUuid))
                    }
                }
            } else {
                mPrevUuid = mChapter.mPrev
                mChapterPageID += 1
                val pageID = mChapterPageID
                val pages = createChapterPages()
                val pagesSize = pages.size
                if (mPrevUuid == null) {
                    mPagesSizeList.add(pagesSize + 3)
                    val first = currentPages.first()
                    if (first is ReaderLoading) {
                        currentPages.removeFirst()
                        currentPages.add(0, ReaderLoading(pageID, pagesSize + 2,  lastChapter, mPrevUuid, mNextUuid))
                        currentPages.addAll(0, pages)
                        currentPages.add(0, ReaderLoading(pageID, 2, nextChapter, mPrevUuid, mNextUuid))
                        currentPages.add(0, ReaderLoading(pageID, 1, noLastChapter, mPrevUuid, mNextUuid))
                    }
                } else {
                    mPagesSizeList.add(pagesSize + 2)
                    val first = currentPages.first()
                    if (first is ReaderLoading) {
                        currentPages.removeFirst()
                        currentPages.add(0, ReaderLoading(pageID, pagesSize + 2, lastChapter, mPrevUuid, mNextUuid))
                        currentPages.addAll(0, pages)
                        currentPages.add(0, ReaderLoading(pageID,2, nextChapter, mPrevUuid, mNextUuid))
                        currentPages.add(0, ReaderLoading(pageID, 1, loading, mPrevUuid, mNextUuid))
                    }
                }
            }
        }
        return currentPages
    }

    private fun ComicPageResp.createChapterPages(): MutableList<Any> {
        return mChapter.mWords
            .zip(mChapter.mContents)
            .sortedBy { it.first }
            .mapIndexed { index, pair ->
                val content = pair.second
                content.mPos = index + 2
                content.mID = mChapterPageID
                content
            }
            .toMutableList()
    }

    /**
     * ● 计算章节、页数累计和 的 百分比
     *
     * ● 2023-09-02 21:49:09 周六 下午
     */
    fun computePercent(pageIndex: Int, totalPage: Int, info: ReaderInfo): Float {
        val ppc = 1f / info.mChapterCount
        return ppc * info.mChapterIndex + ppc * (pageIndex / totalPage.toFloat())
    }

    fun updateUiState(readerState: ReaderState) {
        _uiState.value = readerState
    }

    /**
     * ● 加载上一章
     *
     * ● 2023-08-29 22:19:57 周二 下午
     */
    fun onScroll(position: Int, any: Any? = null) {

        if (any != null) { updatePageID(position, any) }

        if (position < CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = false)
        }

        if (position > mContent.value.mPages.size - CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = true)
        }
    }

    private fun updatePageID(position: Int, any: Any) {
        if (any is Content) {
            mChapterPageID = any.mID
        }
         else if (any is ReaderLoading) {
             mChapterPageID = any.mID
         }
    }

    private fun loadPrevNextChapter(isNext: Boolean) {
        val prevJob = mLoadingJob
        mLoadingJob = launchJob {
            prevJob?.cancelAndJoin()
            if (isActive) {
                mIsNext = isNext
                val uuid = if (isNext) mNextUuid else mPrevUuid
                getComicPage(BookIntent.GetComicPage(mPathword, uuid ?: return@launchJob,null), isNext)
            }
        }
    }
}
