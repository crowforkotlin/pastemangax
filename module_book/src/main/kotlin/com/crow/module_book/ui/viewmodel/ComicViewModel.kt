package com.crow.module_book.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.app.app
import com.crow.base.kt.BaseNotNullVar
import com.crow.base.tools.extensions.DBNameSpace
import com.crow.base.tools.extensions.buildDatabase
import com.crow.base.tools.extensions.log
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.module_book.model.database.ComicDB
import com.crow.module_book.model.database.model.MineReaderComicEntity
import com.crow.module_book.model.database.model.MineReaderSettingEntity
import com.crow.module_book.model.entity.comic.ComicActivityInfo
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderInfo
import com.crow.module_book.model.entity.comic.reader.ReaderUiState
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.model.resp.comic_comment.ComicCommentListResult
import com.crow.module_book.model.source.ComicCommentDataSource
import com.crow.module_book.network.BookRepository
import com.crow.module_book.ui.fragment.comic.reader.ComicCategories
import com.crow.module_book.ui.viewmodel.comic.ComicChapterLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
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
     * ⦁ 漫画信息
     *
     * ⦁ 2024-01-28 00:25:53 周日 上午
     * @author crowforkotlin
     */
    var mComicInfo: ComicActivityInfo by BaseNotNullVar(false)
    val mPathword: String get() = mComicInfo.mPathword
    val mComicUuid: String get() = mComicInfo.mComicUuid
    val mCurrentChapterUuid: String get() = mComicInfo.mChapterCurrentUuid
    var mChapterPrevUuid: String? = null
    var mChapterNextUuid: String? = null
    var mLoadingJob: Job? = null
    var mScrollPos = 0
    var mScrollPosOffset = 0


    private var mIsNext: Boolean = false

    private val mChapterLoader = ComicChapterLoader()

    /**
     * ⦁ UI State For InfoBar
     *
     * ⦁ 2023-09-02 22:03:53 周六 下午
     */
    private val _uiState = MutableStateFlow<ReaderUiState?>(null)
    val uiState : StateFlow<ReaderUiState?> get() = _uiState

    /**
     * ⦁ 当接收到Comic时 UnitPage粘性状态回馈给视图，用于更新页面
     *
     * ⦁ 2024-02-14 20:33:09 周三 下午
     * @author crowforkotlin
     */
    private val _mUnitPages = MutableStateFlow<Boolean?>(null)
    val mUnitPages: StateFlow<Boolean?> get() = _mUnitPages
    val mChapterPageList: List<Pair<Int, ReaderContent>> get() = mChapterLoader._mChapterPageList
    val mChapterPageMapper: Map<Int, ReaderContent> get() = mChapterLoader._mChapterPageMapper
    var mCurrentChapterPageKey: Int = 0

    /**
     * ⦁ 漫画阅读信息、配置 数据库
     *
     * ⦁ 2024-02-14 20:40:16 周三 下午
     * @author crowforkotlin
     */
    private val mComicDBDao by lazy { buildDatabase<ComicDB>(DBNameSpace.READER_COMIC_DB).comicDao() }

    var mReaderSetting: MineReaderSettingEntity? = null
    var mReaderComic: MineReaderComicEntity? = null
        private set

    var mComicCommentFlowPage : Flow<PagingData<ComicCommentListResult>>? = null

    suspend fun getSetting(): MineReaderSettingEntity? {
        return viewModelScope.async(Dispatchers.IO) { mComicDBDao.findSetting(MangaXAccountConfig.mAccount).also { mReaderSetting = it } }.await()
    }

    /**
     * ⦁ 通过检查意图的类型并执行相应的代码来处理意图
     *
     * ⦁ 2023-06-28 22:08:41 周三 下午
     */
    override fun dispatcher(intent: BookIntent) {
        when(intent) {
            is BookIntent.GetComicPage -> { viewModelScope.launch { getComicPage(intent) } }
            is BookIntent.GetComicComment -> getComment(intent)
            is BookIntent.SubmitComment -> submitComment(intent)
        }
    }

    private fun submitComment(intent: BookIntent.SubmitComment) {
        flowResult(intent, repository.submitComment(mCurrentChapterUuid, intent.content)) { value -> intent.copy(resp = value) }
    }

    private fun getComment(intent: BookIntent.GetComicComment) {
        mComicCommentFlowPage = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                ComicCommentDataSource { position, pagesize ->
                    flowResult(repository.getComicComment(mCurrentChapterUuid, position, pagesize), intent) { value -> intent.copy(commentResp = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    /**
     * ⦁ 获取漫画页
     *
     * ⦁ 2023-06-28 22:17:41 周三 下午
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
                                mChapterLoader.obtainReaderContent(isNext, this)
                                mChapterPrevUuid = mChapterLoader.mLoadPrevUuid
                                mChapterNextUuid = mChapterLoader.mLoadNextUuid
                                _mUnitPages.value = !(mUnitPages.value ?: false)
                                return@apply
                            })
                        }
                    }
                }
                    .onFailure { continuation.resume(Unit) }
                    .onSuccess { continuation.resume(Unit) }
            }
        }
    }

    /**
     * ⦁ 计算章节、页数累计和 的 百分比
     *
     * ⦁ 2023-09-02 21:49:09 周六 下午
     */
    fun computePercent(pageIndex: Int, totalPage: Int, info: ReaderInfo): Float {
        val ppc = 1f / info.mChapterCount
        return ppc * info.mChapterIndex + ppc * (pageIndex / totalPage.toFloat())
    }

    /**
     * ⦁ 更新UiState同时设置当前章节的KEY ID，当阅读模式切换时能起到作用
     *
     * ⦁ 2024-03-10 22:48:44 周日 下午
     * @author crowforkotlin
     */
    fun updateUiState(readerUiState: ReaderUiState) {
        mCurrentChapterPageKey = readerUiState.mChapterID
        _uiState.value = readerUiState
    }

    /**
     * ⦁ 处理Comic滚动
     *
     * ⦁ 2024-02-06 20:29:30 周二 下午
     * @author crowforkotlin
     */
    fun onScroll(dy: Int, position: Int, fraction: Int) {
//        "position $position \t $dy \t ${mChapterLoader.mPageTotalSize} \t ${mChapterLoader.mPageTotalSize + mChapterPageList.size * fraction - CHAPTER_PRELOADED_INDEX}".log()

        if (dy < 0 && position - 2 < CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = false)
        }
        else if (dy > 0 && position + 2 > mChapterLoader.mPageTotalSize + mChapterPageList.size * fraction - CHAPTER_PRELOADED_INDEX) {
            loadPrevNextChapter(isNext = true)
        }
    }

    /**
     * ⦁ 加载上一章或者下一章
     *
     * ⦁ 2024-03-10 22:48:06 周日 下午
     * @author crowforkotlin
     */
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

    /**
     * ⦁ 初始化漫画阅读信息、用于初始化工作，可恢复已阅读位置
     *
     * ⦁ 2024-03-10 22:44:32 周日 下午
     * @author crowforkotlin
     */
    fun initComicReader(complete: (MineReaderComicEntity) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            mReaderComic = mComicDBDao.getComic(MangaXAccountConfig.mAccount, mComicUuid, mCurrentChapterUuid)
            mReaderComic?.let(complete)
        }
    }

    /**
     * ⦁ 更新亮度
     *
     * ⦁ 2024-03-10 22:44:20 周日 下午
     * @author crowforkotlin
     */
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

    /**
     * ⦁ 更新当前漫画页面位置以及章节信息、偏移量
     *
     * ⦁ 2024-03-10 22:41:51 周日 下午
     * @author crowforkotlin
     */
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
                    mChapterPagePosition = position,
                    mChapterPagePositionOffset = offset,
                    mCreatedAt = time,
                    mUpdatedAt = time
                )
            } else {
                comic.copy(
                    mChapterPagePosition = position,
                    mChapterPagePositionOffset = offset,
                    mChapterId = chapterID,
                    mUpdatedAt = Date(System.currentTimeMillis()))
            }
            mReaderComic = comic
            mComicDBDao.upSertReaderComic(comic)
        }
    }

    /**
     * ⦁ 条漫获取位置通过ChapterId，这里计算有点麻烦是因为要计算额外的ReaderLoading
     *
     * ⦁ 2024-03-10 22:34:46 周日 下午
     * @author crowforkotlin
     */
    fun getStriptChapterPagePosById(): Int {
        val reader = mReaderComic ?: return 0
        val chapterId = reader.mChapterId
        val pages = mChapterPageMapper[chapterId]?.mPages
        var totalSize = 0
        var index = 0
        for (chapter in mChapterPageList) {
            index++
            totalSize += 1
            if (chapter.first == chapterId) {
                index = chapter.second.mPages.indexOf(pages?.get(max(0, reader.mChapterPagePosition - 1)))
                index ++
                break
            }
            if (index > 1) { totalSize ++ }
            totalSize += chapter.second.mPages.size
        }
        index += totalSize
        return index
    }
    fun getPageChapterPagePosById(): Int {
        val reader = mReaderComic ?: return 1
        val chapterId = reader.mChapterId
        val pages = mChapterPageMapper[chapterId]?.mPages
        var totalSize = 1
        var index = 1
        for (chapter in mChapterPageList) {
            if (chapter.first == chapterId) {
                index += chapter.second.mPages.indexOf(pages?.get(max(0, reader.mChapterPagePosition - 1)))
                index.log()
                break
            }
            totalSize += chapter.second.mPages.size
        }
        index += totalSize
        "getPageChapterPagePosById = $index".log()
        return index
    }
    fun getChapterPagePos() = mReaderComic?.mChapterPagePosition ?: 0
    fun getPosOffset() = mReaderComic?.mChapterPagePositionOffset ?: 0
    fun getRealPosition(): Int {
        val comic = mReaderComic ?: return 0
        val current = ComicCategories.CURRENT_TYPE
        return when(current) {
            ComicCategories.Type.STANDARD -> { comic.mChapterPagePosition }
            ComicCategories.Type.STRIPT -> {
                val chapterId = comic.mChapterId
                val pages = mChapterPageMapper[chapterId]?.mPages
                var totalSize = 0
                var index = 0
                for (chapter in mChapterPageList) {
                    index ++
                    totalSize ++
                    if (chapter.first == chapterId) {
                        index += chapter.second.mPages.indexOf(pages?.get(max(0, comic.mChapterPagePosition - 1)))
                        break
                    }
                    if (index > 1) { totalSize + }
                    totalSize += chapter.second.mPages.size
                }
                index += totalSize
                return index
            }
            ComicCategories.Type.PAGE -> {
                0
            }
        }
    }

    /**
     * ⦁ 尝试更新当前漫画信息如果uuid不同则更新到数据库
     *
     * ⦁ 2024-03-10 22:40:11 周日 下午
     * @author crowforkotlin
     */
    inline fun tryUpdateReaderComicrInfo(position: Int, offset: Int, chapterID: Int, readerInfo: ReaderInfo, update: (ComicActivityInfo) -> Unit) {
        if (readerInfo.mChapterUuid != mCurrentChapterUuid) {
            mComicInfo = mComicInfo.copy(
                mChapterCurrentUuid = readerInfo.mChapterUuid,
                mChapterNextUuid = readerInfo.mNextUUID,
                mChapterPrevUuid = readerInfo.mPrevUUID,
                mSubTitle = readerInfo.mChapterName
            )
            update(mComicInfo)
        } else {
            updatePos(position, offset, chapterID)
        }
    }

    /**
     * ⦁ 更新阅读模式
     *
     * ⦁ 2024-03-10 22:39:47 周日 下午
     * @author crowforkotlin
     */
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
}