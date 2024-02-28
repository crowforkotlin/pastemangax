package com.crow.module_book.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.crow.base.app.app
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.coroutine.createCoroutineExceptionHandler
import com.crow.base.tools.extensions.DBNameSpace
import com.crow.base.tools.extensions.buildDatabase
import com.crow.base.tools.extensions.info
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.module_book.R
import com.crow.module_book.model.database.ComicDB
import com.crow.module_book.model.database.model.MineReaderComicEntity
import com.crow.module_book.model.database.model.MineReaderSettingEntity
import com.crow.module_book.model.entity.comic.ComicActivityInfo
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderInfo
import com.crow.module_book.model.entity.comic.reader.ReaderLoading
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.model.resp.comic_page.Chapter
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.network.BookRepository
import com.crow.module_book.ui.fragment.comic.reader.ComicCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import kotlin.coroutines.resume
import kotlin.math.max
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
        const val CHAPTER_LOADED_THRESHOLD = 100
        const val CHAPTER_PRELOADED_INDEX = 3
    }

    /**
     * ● 漫画信息
     *
     * ● 2024-01-28 00:25:53 周日 上午
     * @author crowforkotlin
     */
    private var mUpdateComicInfoListener: (() -> Unit)? = null
    var mComicInfo: ComicActivityInfo by BaseNotNullVar(false)
    val mPathword: String get() = mComicInfo.mPathword
    val mComicUuid: String get() = mComicInfo.mComicUuid
    val mCurrentChapterUuid: String get() = mComicInfo.mChapterCurrentUuid
    var mChapterPrevUuid: String? = null
    var mChapterNextUuid: String? = null
    var mLoadingJob: Job? = null
    private var mIsNext: Boolean = false

    var mScrollPos = 0
    var mScrollPosOffset = 0

    fun setUpdateComicInfoListener(listener: () -> Unit) {
        mUpdateComicInfoListener =  listener
    }

    /**
     * ● 漫画 阅读器内容，存储着当前章节页面的数据，以及其他已加载章节的页面内容并在其内容增加了分割提示
     *
     * ● 2023-09-01 00:51:58 周五 上午
     */
    private val _mContent = MutableStateFlow(ReaderContent("", "", "", emptyList(), null))
    val mContent: StateFlow<ReaderContent> get() = _mContent

    /**
     * ● 存储当前阅读的章节数据
     *
     * ● 2024-02-14 20:33:09 周三 下午
     * @author crowforkotlin
     */
    private val _mPages = MutableStateFlow<Chapter?>(null)
    val mPages: StateFlow<Chapter?> get() = _mPages

    /**
     * ● UI State For InfoBar
     *
     * ● 2023-09-02 22:03:53 周六 下午
     */
    private val _uiState = MutableStateFlow<ReaderUiState?>(null)
    val uiState : StateFlow<ReaderUiState?> get() = _uiState

    /**
     * ● 章节页面ID 对应 当前阅读器的内容
     *
     * ● 2024-01-15 23:46:34 周一 下午
     * @author crowforkotlin
     */
    private val _mPageContentMapper: HashMap<Int, ReaderContent> = hashMapOf()
    val mPageContentMapper: Map<Int, ReaderContent> get() = _mPageContentMapper

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
     * ● 漫画阅读信息、配置 数据库
     *
     * ● 2024-02-14 20:40:16 周三 下午
     * @author crowforkotlin
     */
    private val mComicDBDao by lazy { buildDatabase<ComicDB>(DBNameSpace.READER_COMIC_DB).comicDao() }

    var mReaderSetting: MineReaderSettingEntity? = null
    var mReaderComic: MineReaderComicEntity? = null
        private set

    suspend fun getSetting(): MineReaderSettingEntity? {
        return viewModelScope.async(Dispatchers.IO) { mComicDBDao.findSetting(MangaXAccountConfig.mAccount).also { mReaderSetting = it } }.await()
    }

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
                            toast("\\d+".toRegex().find(value.mMessage)?.value?.let { app.getString(baseR.string.base_request_time_limit, it) } ?: value.mMessage)
                            intent.copy(comicpage = null)
                        } else {
                            intent.copy(comicpage = (toTypeEntity<ComicPageResp>(value.mResults) ?: error(app.getString(baseR.string.base_unknow_error))).apply {
                                val loadingPages = getLoadingPages()
                                val reader = ReaderContent(
                                    mComicName = mComic.mName,
                                    mComicUuid = mComic.mPathWord,
                                    mComicPathword = mComic.mPathWord,
                                    mPages = loadingPages.second,
                                    mChapterInfo =  ReaderInfo(
                                        mChapterIndex = mChapter.mIndex,
                                        mChapterUuid = mChapter.mUuid,
                                        mChapterName = mChapter.mName,
                                        mChapterCount = mChapter.mCount,
                                        mChapterUpdate = mChapter.mDatetimeCreated,
                                        mPrevUUID = mChapter.mPrev,
                                        mNextUUID = mChapter.mNext
                                    )
                                )
                                _mPageContentMapper[mIncrementPageID] = reader
                                mIncrementPageID ++
                                _mContent.value = reader.copy(mPages = loadingPages.first)
                                if (intent.isReloadEnable || _mPages.value == null) {
                                    _mPages.value = mChapter
                                }
                            })
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
    private fun ComicPageResp.getLoadingPages(): Pair<MutableList<Any>, MutableList<Any>> {
        var currentPages: MutableList<Any> = mContent.value.mPages.toMutableList()
        var pages: MutableList<Any>
        val nextChapter = app.getString(R.string.book_next_val, mChapter.mName)
        val lastChapter = app.getString(R.string.book_prev_val, mChapter.mName)
        val noNextChapter = app.getString(R.string.book_no_next)
        val noLastChapter = app.getString(R.string.book_no_prev)
        val loading = app.getString(baseR.string.base_loading)
        val pageID = mIncrementPageID
        val currentUuid = mChapter.mUuid
        val pageTotalSize = if (currentPages.isEmpty()) {
            val pagesSize: Int
            var pageSizeTemp = 4
            val next = mChapterNextUuid
            val prev = mChapterPrevUuid
            if (prev == null) {
                createChapterPages(3).apply {
                    pagesSize = size
                    pages = this
                    pages.add(0, ReaderLoading(pageID, 2, nextChapter, null, next, currentUuid))
                    pages.add(ReaderLoading(pageID, pagesSize + 3, lastChapter, null, next, currentUuid))
                    pages.add(0, ReaderLoading(pageID, 1, noLastChapter, null, next, currentUuid))
                }
            } else {
                createChapterPages(2).apply {
                    pagesSize = size
                    pageSizeTemp --
                    pages = this
                    pages.add(0, ReaderLoading(pageID, 1, nextChapter, prev, next, currentUuid))
                    pages.add(ReaderLoading(pageID, pagesSize + 2, lastChapter, prev, next, currentUuid))
                    pages.add(0, ReaderLoading(pageID, 1, loading, prev, next, currentUuid, false))
                }
            }
            if (next == null) {
                pages.add(ReaderLoading(pageID, pagesSize + 4, noNextChapter, prev, null, currentUuid))
            } else {
                pageSizeTemp --
                pages.add(ReaderLoading(pageID, pagesSize + 4, loading, prev, next, currentUuid, true))
            }
            currentPages.addAll(pages)
            pagesSize + pageSizeTemp
        } else {
            if (mIsNext) {
                mChapterNextUuid = mChapter.mNext
                val next = mChapterNextUuid
                val prev = mChapterPrevUuid
                if (next == null) {
                    pages = createChapterPages(3)
                    val pagesSize = pages.size
                    val last = currentPages.last()
                    if (last is ReaderLoading) {
                        pages.add(ReaderLoading(pageID, pagesSize + 2, lastChapter, prev, null, currentUuid))
                        pages.add(ReaderLoading(pageID, pagesSize + 3, noNextChapter, prev, null, currentUuid, true))
                        pages.add(0, ReaderLoading(pageID, 1, nextChapter, prev, null, currentUuid))
                        currentPages.removeLast()
                        currentPages.addAll(pages)
                    }
                    pagesSize + 3
                } else {
                    pages = createChapterPages(2)
                    val pagesSize = pages.size
                    val last = currentPages.last()
                    if (last is ReaderLoading) {
                        pages.add(ReaderLoading(pageID, pagesSize + 2, lastChapter, prev, next, currentUuid))
                        pages.add(ReaderLoading(pageID, pagesSize + 2, loading, prev, next, currentUuid, true))
                        pages.add(0, ReaderLoading(pageID, 1, nextChapter, prev, next, currentUuid))
                        currentPages.removeLast()
                        currentPages.addAll(pages)
                    }
                    pagesSize + 2
                }
            } else {
                mChapterPrevUuid = mChapter.mPrev
                val next = mChapterNextUuid
                val prev = mChapterPrevUuid
                if (prev == null) {
                    pages = createChapterPages(3)
                    val pagesSize = pages.size
                    val first = currentPages.first()
                    if (first is ReaderLoading) {
                        pages.add(0, ReaderLoading(pageID, 2, nextChapter, null, next, currentUuid))
                        pages.add(0, ReaderLoading(pageID, 1, noLastChapter, null, next, currentUuid, false))
                        pages.add(ReaderLoading(pageID, pagesSize + 3, lastChapter, null, next, currentUuid))
                        currentPages.removeFirst()
                        currentPages.addAll(0, pages)
                    }
                    pagesSize + 3
                } else {
                    pages = createChapterPages(2)
                    val pagesSize = pages.size
                    val first = currentPages.first()
                    if (first is ReaderLoading) {
                        pages.add(0, ReaderLoading(pageID,1, nextChapter, prev, next, currentUuid))
                        pages.add(0, ReaderLoading(pageID, 1, loading, prev, next, currentUuid, false))
                        pages.add(ReaderLoading(pageID, pagesSize + 2, lastChapter, prev, next, currentUuid))
                        currentPages.removeFirst()
                        currentPages.addAll(0, pages)
                    }
                    pagesSize + 2
                }
            }
        }
        _mPageSizeMapper[pageID] = pageTotalSize
        if (_mPageSizeMapper.size > 2 && currentPages.size > CHAPTER_LOADED_THRESHOLD) {
            currentPages = removeLoadingPages(loading, currentPages, mIsNext)
        }
        return currentPages to pages
    }

    private fun removeLoadingPages(loading: String, pages: MutableList<Any>, isNext: Boolean): MutableList<Any> {
        val pageSize = pages.size
        return if (isNext) {
            val chapterId = (pages.first() as ReaderLoading).mChapterID
            val totalSize =  (_mPageSizeMapper[chapterId] ?: error("sizemapper is null!"))
            val pageList: MutableList<Any>
            var prev = pages[totalSize - 1]
            val next: ReaderLoading
            if (prev is ReaderLoading) {
                next = pages[totalSize] as ReaderLoading
                pageList = pages.subList(totalSize, pageSize).toMutableList()
            } else {
                val nextIndex = totalSize + 1
                prev = pages[totalSize] as ReaderLoading
                next = pages[nextIndex] as ReaderLoading
                pageList = pages.subList(nextIndex, pageSize).toMutableList()
            }
            _mPageContentMapper.remove(chapterId)
            _mPageSizeMapper.remove(chapterId)
            mChapterPrevUuid = prev.mCurrentUuid
            pageList.add(0, next.copy(mChapterID = next.mChapterID,mMessage = loading))
            pageList
        } else {
            val chapterId = (pages.last() as ReaderLoading).mChapterID
            val totalSize =  (_mPageSizeMapper[chapterId] ?: error("sizemapper is null!"))
            val pageList: MutableList<Any>
            val nextIndex = pageSize - totalSize
            var next = pages[nextIndex]
            val prev: ReaderLoading
            if (next is ReaderLoading) {
                prev = pages[nextIndex - 1] as ReaderLoading
                pageList = pages.subList(0, nextIndex).toMutableList()
            } else {
                prev = pages[nextIndex - 2] as ReaderLoading
                next = pages[nextIndex - 1] as ReaderLoading
                pageList = pages.subList(0, nextIndex - 1).toMutableList()
            }
            _mPageContentMapper.remove(chapterId)
            _mPageSizeMapper.remove(chapterId)
            mChapterNextUuid = next.mCurrentUuid
            pageList.add(prev.copy(mChapterID = prev.mChapterID,mMessage = loading))
            pageList
        }
    }

    private fun ComicPageResp.createChapterPages(incrementIndex: Int): MutableList<Any> {
        val pages: MutableList<Any> = mChapter.mWords
            .zip(mChapter.mContents)
            .sortedBy { it.first }
            .mapIndexed { index, pair ->
                val content = pair.second
                content.mChapterPagePos = index + incrementIndex
                content.mChapterID = mIncrementPageID
                content
            }
            .also { mChapter.mContents = it.toMutableList() }
//            .also { = mChapter.copy(mContents = it.toMutableList()) }
            .toMutableList()
        return pages
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

    fun updateUiState(readerUiState: ReaderUiState) {
        _uiState.value = readerUiState
    }

    /**
     * ● 处理Comic滚动
     *
     * ● 2024-02-06 20:29:30 周二 下午
     * @author crowforkotlin
     */
    fun onScroll(dy: Int, position: Int) {
//        "position $position \t ${mContent.value.mPages.size}".log()
        if (dy < 0 && position - 2 < CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = false)
        }
        else if (dy > 0 && position + 2 > mContent.value.mPages.size - CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = true)
        }
    }

    private fun loadPrevNextChapter(isNext: Boolean) {
        val prevJob = mLoadingJob
        if (prevJob?.isActive == true) return
        mLoadingJob = launchJob {
            prevJob?.join()
            if (isActive) {
                mIsNext = isNext
                val uuid = if (isNext) {
                    mChapterNextUuid
                } else {
                    mChapterPrevUuid
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

    fun initComicReader(complete: (MineReaderComicEntity) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            mReaderComic= mComicDBDao.getComic(MangaXAccountConfig.mAccount, mComicUuid, mCurrentChapterUuid)
            mReaderComic?.let(complete)
        }
    }

    fun updateLight(light: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            var setting = mReaderSetting
            setting = if (setting == null) {
                val time = Date(System.currentTimeMillis())
                MineReaderSettingEntity(
                    mAccount = MangaXAccountConfig.mAccount,
                    mLight = light,
                    mReadMode = ComicCategories.Type.STANDARD,
                    mCreatedAt = time,
                    mUpdatedAt = time
                )
            } else {
                setting.copy(mLight = light, mUpdatedAt = Date(System.currentTimeMillis()))
            }
            mReaderSetting = setting
            mComicDBDao.upSertSetting(setting)
        }
    }

    fun updatePos(position: Int, offset: Int, chapterID: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            var comic = mReaderComic
            comic = if (comic == null) {
                val time = Date(System.currentTimeMillis())
                MineReaderComicEntity(
                    mAccount = MangaXAccountConfig.mAccount,
                    mComicUUID = mComicUuid,
                    mChapterUUID = mCurrentChapterUuid,
                    mChapterId = chapterID,
                    mChapterPosition = position,
                    mChapterPositionOffset = offset,
                    mCreatedAt = time,
                    mUpdatedAt = time
                )
            } else {
                comic.copy(mChapterPosition = position, mChapterPositionOffset = offset, mChapterId = chapterID, mUpdatedAt = Date(System.currentTimeMillis()))
            }
            mReaderComic = comic
            mComicDBDao.upSertReaderComic(comic)
        }
    }

    fun getPos() = mReaderComic?.mChapterPosition ?: 0
    fun getPosByChapterId(): Int {
        val reader = mReaderComic ?: return 0
        val pages = mPageContentMapper[reader.mChapterId]?.mPages
        val first = pages?.first()
        var pos =  _mContent.value.mPages.indexOf(pages?.get(max(0, reader.mChapterPosition)))
        if (first is ReaderLoading) {
            if (first.mMessage == app.getString(com.crow.base.R.string.base_loading)) {
                pos ++
            }
        }
        return pos
    }
    fun getPosOffset() = mReaderComic?.mChapterPositionOffset ?: 0

    inline fun tryUpdateReaderComicrInfo(position: Int, offset: Int, chapterID: Int, readerInfo: ReaderInfo, update: (ComicActivityInfo) -> Unit) {
        if (readerInfo.mChapterUuid != mCurrentChapterUuid) {
            mComicInfo = mComicInfo.copy(mChapterCurrentUuid = readerInfo.mChapterUuid, mChapterNextUuid = readerInfo.mNextUUID, mChapterPrevUuid = readerInfo.mPrevUUID)
            update(mComicInfo)
        } else {
            updatePos(position, offset, chapterID)
        }
    }

    suspend fun updateReaderMode(readerMode: ComicCategories.Type) {
        viewModelScope.async(Dispatchers.IO) {
            var setting = mReaderSetting
            setting = if (setting == null) {
                val time = Date(System.currentTimeMillis())
                MineReaderSettingEntity(
                    mAccount = MangaXAccountConfig.mAccount,
                    mLight = 0,
                    mReadMode = readerMode,
                    mCreatedAt = time,
                    mUpdatedAt = time
                )
            } else {
                setting.copy(mReadMode = readerMode, mUpdatedAt = Date(System.currentTimeMillis()))
            }
            mReaderSetting = setting
            mComicDBDao.upSertSetting(setting)
        }.await()
    }

    fun updateOriginChapterPage(chapterPageID: Int) {
        val pages = (_mPages.value ?: return)
        val pageContentMapper = _mPageContentMapper[chapterPageID] ?: return
        var index = 0
        val list = pageContentMapper.mPages.toMutableList()
        if (list[index] is ReaderLoading) { list.removeFirst() } else { index ++ }
        if (list[index] is ReaderLoading) { list.removeFirst() }
        index = list.size - 2
        if (list[index] is ReaderLoading) { list.removeAt(index) } else { index ++ }
        if (list[index] is ReaderLoading) { list.removeLast() }
        _mPages.value = pages.copy(mContents = list as MutableList<Content>)
    }
}