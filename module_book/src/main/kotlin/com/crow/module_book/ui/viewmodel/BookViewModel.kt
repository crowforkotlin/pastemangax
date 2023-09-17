package com.crow.module_book.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.crow.base.app.appContext
import com.crow.base.tools.coroutine.baseCoroutineException
import com.crow.base.tools.extensions.DBNameSpace
import com.crow.base.tools.extensions.buildDatabase
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_book.R
import com.crow.module_book.model.database.BookChapterDB
import com.crow.module_book.model.entity.BookChapterEntity
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicChapterResp
import com.crow.module_book.model.resp.ComicInfoResp
import com.crow.module_book.model.resp.NovelChapterResp
import com.crow.module_book.model.resp.NovelInfoResp
import com.crow.module_book.network.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/viewmodel
 * @Time: 2023/3/15 0:20
 * @Author: CrowForKotlin
 * @Description: ComicViewModel
 * @formatter:on
 **************************/
class BookViewModel(val repository: BookRepository) : BaseMviViewModel<BookIntent>() {

    /**
     * ● 检索章节 起始位置
     *
     * ● 2023-06-28 22:05:36 周三 下午
     */
    private var mChapterStartIndex = 0

    /**
     * ● 页面UUID (Comic,  Novel)
     *
     * ● 2023-06-28 22:05:47 周三 下午
     */
    var mUuid: String? = null
        private set

    /** ● 漫画信息页 */
    var mComicInfoPage: ComicInfoResp? = null
        private set

    /** ● 轻小说信息页 */
    var mNovelInfoPage: NovelInfoResp? = null
        private set

    /**
     * ● 章节 数据库 DAO
     *
     * ● 2023-06-28 22:23:19 周三 下午
     */
    private val mChapterDBDao by lazy { buildDatabase<BookChapterDB>(DBNameSpace.app).bookChapterDao() }


    /**
     * ● 书页章节实体数据
     *
     * ● 2023-06-28 22:23:46 周三 下午
     */
    private var _bookChapterEntity = MutableStateFlow<BookChapterEntity?>(null)
    val bookChapterEntity: StateFlow<BookChapterEntity?> get() = _bookChapterEntity

    /**
     * ● 通过检查意图的类型并执行相应的代码来处理意图
     *
     * ● 2023-06-28 22:08:41 周三 下午
     */
    override fun dispatcher(intent: BookIntent) {
        when (intent) {
            is BookIntent.GetComicInfoPage -> getComicInfoPage(intent)
            is BookIntent.GetComicChapter -> getComicChapter(intent)
            is BookIntent.GetComicBrowserHistory -> getComicBrowserHistory(intent)
            is BookIntent.GetNovelInfoPage -> getNovelInfoPage(intent)
            is BookIntent.GetNovelChapter -> getNovelChapter(intent)
            is BookIntent.GetNovelPage -> getNovelPage(intent)
            is BookIntent.GetNovelBrowserHistory -> getNovelBrowserHistory(intent)
            is BookIntent.GetNovelCatelogue -> getNovelCatelogue(intent)
            is BookIntent.AddComicToBookshelf -> addComicToBookshelf(intent)
            is BookIntent.AddNovelToBookshelf -> addNovelToBookshelf(intent)
        }
    }


    /**
     * ● 更新数据库中书页章节
     *
     * ● 2023-06-28 22:24:38 周三 下午
     */
    fun updateBookChapterOnDB(bookName: String, chapterName: String, bookType: Int) {
        viewModelScope.launch(Dispatchers.IO + baseCoroutineException) {
            val bookChapterEntity = mChapterDBDao.find(bookName, bookType)
            if (bookChapterEntity != null) {
                mChapterDBDao.update(bookChapterEntity.copy(mChapterName = chapterName).also {
                    _bookChapterEntity.value = it
                })
            } else {
                mChapterDBDao.insertAll(
                    BookChapterEntity(
                        mBookName = bookName,
                        mChapterType = bookType,
                        mChapterName = chapterName
                    ).also {
                        _bookChapterEntity.value = it
                    })
            }
        }
    }

    /**
     * ● 查找数据库中德章节
     *
     * ● 2023-06-28 22:26:51 周三 下午
     */
    fun findReadedBookChapterOnDB(bookName: String, bookType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _bookChapterEntity.value = mChapterDBDao.find(bookName, bookType)
        }
    }

    /**
     * ● 重新计数（位置）
     *
     * ● 2023-06-28 22:27:27 周三 下午
     */
    fun reCountPos(pos: Int) {
        mChapterStartIndex = pos * 100
    }

    private fun addNovelToBookshelf(intent: BookIntent.AddNovelToBookshelf) {
        flowResult(
            intent,
            repository.addNovelToBookshelf(intent.novelId, intent.isCollect)
        ) { value -> intent.copy(baseResultResp = value) }
    }

    private fun addComicToBookshelf(intent: BookIntent.AddComicToBookshelf) {
        flowResult(
            intent,
            repository.addComicToBookshelf(intent.comicId, intent.isCollect)
        ) { value -> intent.copy(baseResultResp = value) }
    }

    private fun getComicBrowserHistory(intent: BookIntent.GetComicBrowserHistory) {
        flowResult(
            intent,
            repository.getComicBrowserHistory(intent.pathword)
        ) { value -> intent.copy(comicBrowser = value.mResults) }
    }

    private fun getComicInfoPage(intent: BookIntent.GetComicInfoPage) {
        flowResult(intent, repository.getComicInfo(intent.pathword)) { value ->
            mComicInfoPage = value.mResults
            mUuid = mComicInfoPage?.mComic?.mUuid
            intent.copy(comicInfo = value.mResults)
        }
    }

    private fun getComicChapter(intent: BookIntent.GetComicChapter) {
        flowResult(intent, repository.getComicChapter(intent.pathword, mChapterStartIndex, 100)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) {
                val comicChapterPage = toTypeEntity<ComicChapterResp>(value.mResults)
                intent.copy(comicChapter = comicChapterPage)
            } else {
                intent.copy(
                    invalidResp = appContext.getString(
                        R.string.BookComicRequestThrottled,
                        Regex("\\d+").find(value.mMessage)?.value ?: "0"
                    )
                )
            }
        }
    }


    private fun getNovelInfoPage(intent: BookIntent.GetNovelInfoPage) {
        flowResult(intent, repository.getNovelInfo(intent.pathword)) { value ->
            mNovelInfoPage = value.mResults
            mUuid = mNovelInfoPage!!.mNovel.mUuid
            intent.copy(novelInfo = value.mResults)
        }
    }

    private fun getNovelChapter(intent: BookIntent.GetNovelChapter) {
        flowResult(intent, repository.getNovelChapter(intent.pathword)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) {
                val novelChapterResp = toTypeEntity<NovelChapterResp>(value.mResults)
                intent.copy(novelChapter = novelChapterResp)
            } else {
                intent.copy(invalidResp = value.mMessage)
            }
        }
    }

    private fun getNovelBrowserHistory(intent: BookIntent.GetNovelBrowserHistory) {
        flowResult(intent, repository.getNovelBrowserHistory(intent.pathword)) { value ->
            intent.copy(novelBrowser = value.mResults)
        }
    }

    private fun getNovelPage(intent: BookIntent.GetNovelPage) {
        flowResult(intent, repository.getNovelPage(intent.pathword)) { value ->
            intent.copy(novelPage = value)
        }
    }

    private fun getNovelCatelogue(intent: BookIntent.GetNovelCatelogue) {
        flowResult(intent, repository.getNovelCatelogue(intent.pathword)) { value ->
            intent.copy(novelCatelogue = value.mResults)
        }
    }
}