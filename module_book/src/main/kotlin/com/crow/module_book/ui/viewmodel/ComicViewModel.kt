package com.crow.module_book.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.crow.base.app.app
import com.crow.base.copymanga.BaseEventEnum
import com.crow.base.tools.coroutine.FlowBus
import com.crow.base.tools.coroutine.createCoroutineExceptionHandler
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_book.model.entity.BookChapterEntity
import com.crow.module_book.model.entity.BookType
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderInfo
import com.crow.module_book.model.entity.comic.reader.ReaderState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.comic_page.Chapter
import com.crow.module_book.network.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
        const val CHAPTER_LOADED_THRESHOLD = 2
        const val CHAPTER_PRELOADED_INDEX = 2
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
     * ● 上一章漫画UID
     *
     * ● 2023-08-29 23:16:44 周二 下午
     */
    var mPrevUuid: String? = null

    /**
     * ● 下一章漫画UID
     *
     * ● 2023-08-29 23:16:17 周二 下午
     */
    var mNextUuid: String? = null

    /**
     * ● 漫画内容
     *
     * ● 2023-09-01 00:51:58 周五 上午
     */
    private val _mContent = MutableStateFlow(ReaderContent(emptyList(), null))
    val mContent: StateFlow<ReaderContent> get() = _mContent

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
            val readerContent = getReaderContent(value.mResults.mChapter)
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
    private fun getReaderContent(chapter: Chapter): ReaderContent {
        return ReaderContent(
            mPages = chapter.mWords
                .zip(chapter.mContents)
                .sortedBy { it.first }
                .map { it.second }
                .toMutableList(),
            mInfo =  ReaderInfo(
                mChapterIndex = chapter.mIndex,
                mChapterID = chapter.mUuid,
                mChapterName = chapter.mName,
                mChapterCount = chapter.mCount,
                mChapterUpdate = chapter.mDatetimeCreated,
                mPrevUUID = chapter.mPrev,
                mNextUUID = chapter.mNext
            ))
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
    fun onScroll(position: Int) {
        // TODO Future
/*
        if (position < CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = false)
        }

        if (position > mContent.value.mPages.size - CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = true)
        }
*/
    }

/*
    private fun loadPrevNextChapter(isNext: Boolean) {
        val prevJob = mLoadingJob
        mLoadingJob = launchJob {
            prevJob?.cancelAndJoin()
            ensureActive()

            val uuid = if (isNext) mNextUuid else mPrevUuid

            getComicPage(BookIntent.GetComicPage(mPathword!!, uuid ?: return@launchJob,null), isNext)
        }
    }
*/
}
