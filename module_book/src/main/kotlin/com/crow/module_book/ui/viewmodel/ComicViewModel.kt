package com.crow.module_book.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_book.model.entity.ComicLoadMorePage
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.model.resp.comic_page.getSortedContets
import com.crow.module_book.model.resp.requireContentsSize
import com.crow.module_book.network.BookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.LinkedList

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

    enum class  ComicState {
        PREV,
        NEXT
    }

    /**
     * ● 漫画关键字
     *
     * ● 2023-06-28 22:04:54 周三 下午
     */
    var mPathword: String? = null

    /**
     * ● 漫画UID
     *
     * ● 2023-06-28 22:04:58 周三 下午
     */
    var mUuid: String? = null

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
     * ● 2023-06-28 22:08:26 周三 下午
     */
    var mComicPage: ComicPageResp? = null
        private set

    private val mChapters: LinkedList<ComicPageResp> = LinkedList()

    val mContents = mutableListOf<Any>()

    var mLoadedState = MutableLiveData<MutableList<Any>>()

    /**
     * ● 通过检查意图的类型并执行相应的代码来处理意图
     *
     * ● 2023-06-28 22:08:41 周三 下午
     */
    override fun dispatcher(intent: BookIntent) {
        when(intent) {
            is BookIntent.GetComicPage -> getComicPage(intent)
        }
    }

    /**
     * ● 获取漫画页
     *
     * ● 2023-06-28 22:17:41 周三 下午
     */
    private fun getComicPage(intent: BookIntent.GetComicPage) {
        flowResult(intent, repository.getComicPage(intent.pathword, intent.uuid)) { value ->
            mComicPage = value.mResults
            val snapshot = if (mChapters.isEmpty()) {
                mContents.addAll(value.mResults.mChapter.getSortedContets())
                mContents.add(
                        ComicLoadMorePage(
                            mIsNext = true,
                            mHasNext = mNextUuid == null,
                            mIsLoading = true,
                            mPrevContent = "",
                            mNextContent = "",
                        )
                )
                mContents.add(0,
                        ComicLoadMorePage(
                            mIsNext = false,
                            mHasNext = mPrevUuid == null,
                            mIsLoading = true,
                            mPrevContent = "",
                            mNextContent = ""
                        )
                )
                intent.copy(contents = mContents)
            } else{
                intent.copy(contents = null)
            }
            mChapters.add(value.mResults)
            snapshot
        }
    }

    /**
     * ● 加载上一章
     *
     * ● 2023-08-29 22:19:57 周二 下午
     */
    fun onScrollUp(position: Int) {
        CoroutineScope(coroutineContext).launch {
            mutex.tryLock()
            if (position < CHAPTER_PRELOADED_INDEX) {
                loadPrevNextChapter(isNext = false)
            }
            mutex.unlock()
        }
    }

    /**
     * ● 加载下一章
     *
     * ● 2023-08-29 22:19:57 周二 下午
     */
    fun onScrollDown(position: Int) {
        CoroutineScope(coroutineContext).launch {
            mutex.tryLock()
            if (position > mComicPage.requireContentsSize() - CHAPTER_PRELOADED_INDEX) {
                loadPrevNextChapter(isNext = true)
            }
            mutex.unlock()
        }
    }

    val mutex = Mutex(locked = true)
    val coroutineContext = SupervisorJob() + Dispatchers.Main.immediate

    private suspend fun loadPrevNextChapter(isNext: Boolean) {
        if (mChapters.size > CHAPTER_LOADED_THRESHOLD) {
            if(isNext) {
                mChapters.removeFirst()
            } else {
                mChapters.removeLast()
            }
        }
        mChapters.map {
            val contents = it.mChapter.getSortedContets()
            mContents.addAll(contents)
            if(isNext) {
                mContents.add(
                    ComicLoadMorePage(
                        mIsNext = true,
                        mHasNext = mNextUuid == null,
                        mIsLoading = true,
                        mPrevContent = "",
                        mNextContent = "",
                    )
                )
            } else {
                mContents.add(0,
                    ComicLoadMorePage(
                        mIsNext = false,
                        mHasNext = mPrevUuid == null,
                        mIsLoading = true,
                        mPrevContent = "",
                        mNextContent = "",
                    )
                )
            }
        }
        mLoadedState.value = mContents
    }
}