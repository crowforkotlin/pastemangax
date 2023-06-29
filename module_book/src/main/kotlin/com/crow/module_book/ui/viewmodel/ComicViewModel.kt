package com.crow.module_book.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.ComicPageResp
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

    /**
     * ● 页面指示器（页码）
     *
     * ● 2023-06-28 22:18:35 周三 下午
     */
    private val _mPageIndicator = MutableLiveData<Int>()
    val mPageIndicator: LiveData<Int> get() = _mPageIndicator
    
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
            intent.copy(comicPage = value.mResults)
        }
    }


    /**
     * ● 页面滚动发生改变
     *
     * ● 2023-06-28 22:19:25 周三 下午
     */
    fun onPageScrollChanged(pos: Int) {
        _mPageIndicator.value = pos
    }
}