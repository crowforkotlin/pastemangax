package com.crow.module_book.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.crow.base.app.app
import com.crow.base.kt.BaseNotNullVar
import com.crow.mangax.copymanga.BaseEventEnum
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.createCoroutineExceptionHandler
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_book.R
import com.crow.module_book.model.entity.BookChapterEntity
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.entity.comic.ComicActivityInfo
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderInfo
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.model.resp.comic_page.Chapter
import com.crow.module_book.network.BookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
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
     * ● 漫画信息
     *
     * ● 2024-01-28 00:25:53 周日 上午
     * @author crowforkotlin
     */
    var mComicInfo: ComicActivityInfo by BaseNotNullVar(true)
    val mPathword: String get() = mComicInfo.mPathword
    val mUuid: String get() = mComicInfo.mUuid
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

    private val _mPages = MutableStateFlow<Chapter?>(null)
    val mPages: StateFlow<Chapter?> get() = _mPages

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
    private val _uiState = MutableStateFlow<ReaderUiState?>(null)
    val uiState : StateFlow<ReaderUiState?> get() = _uiState

    /**
     * ● 页面大小列表
     *
     * ● 2024-01-14 23:46:06 周日 下午
     * @author crowforkotlin
     */
    private val _mPageSizeMapper = hashMapOf<Int, Int>()
    val mPageSizeMapper: Map<Int, Int> get() = _mPageSizeMapper

    /**
     * ● 章节页面ID
     *
     * ● 2024-01-15 23:45:55 周一 下午
     * @author crowforkotlin
     */
    private var mIncrementPageID: Int = 0

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
        return suspendCancellableCoroutine { continuation ->
            viewModelScope.launch {
                runCatching {
                    flowResult(repository.getComicPage(intent.pathword, intent.uuid), intent) { value ->
                        if (value.mCode == 210) {
                            toast("\\d+".toRegex().find(value.mMessage)?.value?.let { "请求频率太快了，请等待${it}秒后重试" } ?: value.mMessage)
                            intent.copy(comicpage = null)
                        } else {
                            val result = toTypeEntity<ComicPageResp>(value.mResults) ?: error(app.getString(baseR.string.base_unknow_error))
                            val readerContent = getReaderContent(result)
                            val chapter = result.mChapter
                            FlowBus.with<BookChapterEntity>(BaseEventEnum.UpdateChapter.name).post(
                                BookChapterEntity(
                                    mBookName = result.mComic.mName,
                                    mChapterType = BookType.COMIC,
                                    mChapterName = chapter.mName,
                                    mChapterUUID = chapter.mUuid,
                                    mChapterNextUUID = chapter.mNext,
                                    mChapterPrevUUID = chapter.mPrev
                                )
                            )
                            _mContent.value = readerContent
                            _mPages.value = chapter
                            intent.copy(comicpage = result)
                        }
                    }
                }
                    .onFailure { continuation.resume(Unit) }
            }
        }
    }

    /**
     * ● 获取阅读内容实体
     *
     * ● 2023-09-02 19:51:53 周六 下午
     */
    private fun getReaderContent(resp: ComicPageResp): ReaderContent {
        return with(resp) {
            val loadingPages = getLoadingPages()
            val reader = ReaderContent(
                mComicName = mComic.mName,
                mComicUUID = mComic.mPathWord,
                mComicPathword = mComic.mPathWord,
                mPages = loadingPages,
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
            _mReaderContents[mIncrementPageID] = reader
            mIncrementPageID ++
            reader
        }
    }

    private fun ComicPageResp.getLoadingPages(): MutableList<Any> {
        var currentPages: MutableList<Any> = mContent.value.mPages.toMutableList()
        val nextChapter = app.getString(R.string.book_next_val, mChapter.mName)
        val lastChapter = app.getString(R.string.book_prev_val, mChapter.mName)
        val noNextChapter = app.getString(R.string.book_no_next)
        val noLastChapter = app.getString(R.string.book_no_prev)
        val loading = app.getString(baseR.string.base_loading)
        val pageID = mIncrementPageID
        val pageTotalSize = if (currentPages.isEmpty()) {
            val pagesSize: Int
            var pageSizeTemp = 4
            val next = mNextUuid
            val prev = mPrevUuid
            if (prev == null) {
                createChapterPages(3).apply {
                     pagesSize = size
                     currentPages = this
                     currentPages.add(0, ReaderLoading(pageID, 2, nextChapter, null, next))
                     currentPages.add(ReaderLoading(pageID, pagesSize + 3, lastChapter, null, next))
                     currentPages.add(0, ReaderLoading(pageID, 1, noLastChapter, null, next))
                 }
            } else {
                createChapterPages(2).apply {
                    pagesSize = size
                    pageSizeTemp --
                    currentPages = this
                    currentPages.add(0, ReaderLoading(pageID, 1, nextChapter, prev, next))
                    currentPages.add(ReaderLoading(pageID, pagesSize + 2, lastChapter, prev, next))
                    currentPages.add(0, ReaderLoading(pageID, 1, loading, prev, next, false))
                }
            }
            if (next == null) {
                currentPages.add(ReaderLoading(pageID, pagesSize + 4, noNextChapter, prev, null))
            } else {
                pageSizeTemp --
                currentPages.add(ReaderLoading(pageID, pagesSize + 4, loading, prev, next, true))
            }
            pagesSize + pageSizeTemp
        } else {
            if (mIsNext) {
                mNextUuid = mChapter.mNext
                val next = mNextUuid
                val prev = mPrevUuid
                if (next == null) {
                    val pages = createChapterPages(3)
                    val pagesSize = pages.size
                    val last = currentPages.last()
                    if (last is ReaderLoading) {
                        currentPages.removeLast()
                        currentPages.add(ReaderLoading(pageID, 1, nextChapter, prev, null))
                        currentPages.addAll(pages)
                        currentPages.add(ReaderLoading(pageID, pagesSize + 2, lastChapter, prev, null))
                        currentPages.add(ReaderLoading(pageID, pagesSize + 3, noNextChapter, prev, null, true))
                    }
                    pagesSize + 3
                } else {
                    val pages = createChapterPages(2)
                    val pagesSize = pages.size
                    val last = currentPages.last()
                    if (last is ReaderLoading) {
                        currentPages.removeLast()
                        currentPages.add(ReaderLoading(pageID, 1, nextChapter, prev, next))
                        currentPages.addAll(pages)
                        currentPages.add(ReaderLoading(pageID, pagesSize + 2, lastChapter, prev, next))
                        currentPages.add(ReaderLoading(pageID, pagesSize + 2, loading, prev, next, true))
                    }
                    pagesSize + 2
                }
            } else {
                mPrevUuid = mChapter.mPrev
                val next = mNextUuid
                val prev = mPrevUuid
                if (prev == null) {
                    val pages = createChapterPages(3)
                    val pagesSize = pages.size
                    val first = currentPages.first()
                    if (first is ReaderLoading) {
                        currentPages.removeFirst()
                        currentPages.add(0, ReaderLoading(pageID, pagesSize + 3, lastChapter, null, next))
                        currentPages.addAll(0, pages)
                        currentPages.add(0, ReaderLoading(pageID, 2, nextChapter, null, next))
                        currentPages.add(0, ReaderLoading(pageID, 1, noLastChapter, null, next, false))
                    }
                    pagesSize + 3
                } else {
                    val pages = createChapterPages(2)
                    val pagesSize = pages.size
                    val first = currentPages.first()
                    if (first is ReaderLoading) {
                        currentPages.removeFirst()
                        currentPages.add(0, ReaderLoading(pageID, pagesSize + 2, lastChapter, prev, next))
                        currentPages.addAll(0, pages)
                        currentPages.add(0, ReaderLoading(pageID,1, nextChapter, prev, next))
                        currentPages.add(0, ReaderLoading(pageID, 1, loading, prev, next, false))
                    }
                    pagesSize + 2
                }
            }
        }
        _mPageSizeMapper[pageID] = pageTotalSize
        return currentPages
    }

    private fun ComicPageResp.createChapterPages(incrementIndex: Int): MutableList<Any> {
        return mChapter.mWords
            .zip(mChapter.mContents)
            .sortedBy { it.first }
            .mapIndexed { index, pair ->
                val content = pair.second
                content.mChapterPagePos = index + incrementIndex
                content.mChapterID = mIncrementPageID
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

    fun updateUiState(readerUiState: ReaderUiState) { _uiState.value = readerUiState }

    /**
     * ● 处理Comic滚动
     *
     * ● 2024-02-06 20:29:30 周二 下午
     * @author crowforkotlin
     */
    fun onScroll(dy: Int, position: Int) {
        if (dy < 0 && position - 4 < CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = false)
        }
        else if (dy > 0 && position + 4 > mContent.value.mPages.size - CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = true)
        }
    }

    private fun loadPrevNextChapter(isNext: Boolean) {
        val prevJob = mLoadingJob
        mLoadingJob = launchJob {
            prevJob?.join()
            if (isActive) {
                mIsNext = isNext
                val uuid = if (isNext) {
                    mNextUuid
                } else {
                    mPrevUuid
                }
                getComicPage(BookIntent.GetComicPage(mPathword, uuid ?: return@launchJob, isNext, null), isNext)
            }
        }
    }

    fun processErrorRequestPage(isNext: Boolean?) {
        val content = mContent.value
        val pages = content.mPages.toMutableList()
        if (pages.isEmpty()) return
        if (isNext == true) {
            val last = content.mPages.last()
            if (last is ReaderLoading) {
                pages.removeLast()
                pages.add(last.copy(mMessage = null, mLoadNext = true, mStateComplete = !last.mStateComplete))
                _mContent.value = content.copy(mPages = pages)
            }
        } else {
           val first = content.mPages.first()
            if (first is ReaderLoading) {
                pages.removeFirst()
                pages.add(0, first.copy(mMessage = null, mLoadNext = false, mStateComplete = !first.mStateComplete))
                _mContent.value = content.copy(mPages = pages)
            }
        }
    }
}
