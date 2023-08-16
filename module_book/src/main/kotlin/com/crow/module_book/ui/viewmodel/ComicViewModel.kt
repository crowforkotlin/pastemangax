package com.crow.module_book.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import com.crow.base.tools.extensions.logMsg
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.model.resp.comic_page.Content
import com.crow.module_book.network.BookRepository

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_book.ui.viewmodel
 * @Time: 2023/6/28 22:06
 * @Author: CrowForKotlin
 * @Description: ComicViewModel
 * @formatter:on
 **************************/
class ComicViewModel(val repository: BookRepository) : BaseMviViewModel<BookIntent>() {


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
     * ● 漫画内容
     *
     * ● 2023-06-28 22:08:26 周三 下午
     */
    var mComicPage: ComicPageResp? = null
        private set

    val mContents = mutableListOf<Content>()

    var mLoadedState = MutableLiveData<MutableList<Content>>()

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
        if (intent.uuid == null) {
            mComicPage?.mChapter?.apply {
                when {
                    intent.loadPrev -> {
                        val content = this@ComicViewModel.mContents.first().copy(mTips = "已经没有上一章啦...",mPrev = "", mNext = "当前章节：$mName", mIsLoading = false, mImageUrl = "")
                        this@ComicViewModel.mContents.removeFirst()
                        this@ComicViewModel.mContents.add(0, content)
                        mLoadedState.value = this@ComicViewModel.mContents
                    }
                    intent.loadNext -> {
                        val content = this@ComicViewModel.mContents.first().copy(mTips = "已经没有上一章啦...", mNext = "当前章节：$mName", mIsLoading = false, mImageUrl = "")
                        this@ComicViewModel.mContents.removeFirst()
                        this@ComicViewModel.mContents.add(0, content)
                        mLoadedState.value = this@ComicViewModel.mContents
                    }
                }
            }
            return
        }
        flowResult(intent, repository.getComicPage(intent.pathword, intent.uuid)) { value ->
            mComicPage = value.mResults
            mComicPage?.mChapter?.apply {
                when {
                    intent.loadPrev -> {
                        val content = this@ComicViewModel.mContents.first().copy(mPrev = "上一章节：$mName", mIsLoading = false)
                        this@ComicViewModel.mContents.removeFirst()
                        this@ComicViewModel.mContents.add(0, content)
                        this@ComicViewModel.mContents.addAll(0, mWords.zip(mContents).sortedBy { it.first }.map { it.second }.toMutableList())
                        if (mPrev.isNullOrEmpty()) this@ComicViewModel.mContents.add(0, Content(mTips = "已经没有上一章啦...", mNext = "当前章节：$mName",mIsLoading = false, mImageUrl = ""))
                        else this@ComicViewModel.mContents.add(0, Content(mTips = "正在加载中...", mNext = "下一章节：$mName", mIsLoading = true, mImageUrl = ""))
                    }
                    intent.loadNext -> {
                        val content = this@ComicViewModel.mContents.last().copy(mNext = "下一章节：$mName", mIsLoading = false)
                        content.logMsg()
                        this@ComicViewModel.mContents.removeLast()
                        this@ComicViewModel.mContents.add(content)
                        this@ComicViewModel.mContents.addAll(mWords.zip(mContents).sortedBy { it.first }.map { it.second }.toMutableList())
                        if (mNext.isNullOrEmpty()) this@ComicViewModel.mContents.add(Content(mTips = "已经到最后一章啦...", mPrev = "当前章节：$mName", mIsLoading = false, mImageUrl = ""))
                        else this@ComicViewModel.mContents.add(Content(mTips = "正在加载中...", mPrev = "上一章节：$mName", mIsLoading = true, mImageUrl = ""))
                    }
                    else -> {
                        this@ComicViewModel.mContents.addAll(mWords.zip(mContents).sortedBy { it.first }.map { it.second }.toMutableList())
                        if (mPrev.isNullOrEmpty()) this@ComicViewModel.mContents.add(0, Content(mTips = "已经没有上一章节了...", mNext = "当前章节：$mName", mIsLoading = false, mImageUrl = ""))
                        else this@ComicViewModel.mContents.add(0, Content(mTips = "", mIsLoading = true, mNext = "下一章节：$mName", mPrev= "", mImageUrl = ""))
                        if (mNext.isNullOrEmpty()) this@ComicViewModel.mContents.add(Content(mTips = "已经没有下一章节了...", mPrev = "当前章节：$mName", mIsLoading = false, mImageUrl = ""))
                        else this@ComicViewModel.mContents.add(Content(mTips = "", mIsLoading = true, mPrev = "上一章节：$mName", mNext = "", mImageUrl = ""))
                    }
                }
            }
            mLoadedState.value = this@ComicViewModel.mContents
            intent.copy(comicPage = value.mResults)
        }
    }

}