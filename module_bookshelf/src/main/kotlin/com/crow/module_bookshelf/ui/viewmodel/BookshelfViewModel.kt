package com.crow.module_bookshelf.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.current_project.BaseUser
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.clear
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_bookshelf.model.intent.BookShelfIntent
import com.crow.module_bookshelf.model.resp.book_shelf.BookshelfResults
import com.crow.module_bookshelf.model.source.BookshelfDataSource
import com.crow.module_bookshelf.network.BookShelfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_bookshelf/src/main/java/com/crow/module_bookshelf
 * @Time: 2023/3/7 22:34
 * @Author: CrowForKotlin
 * @Description: BookShelfViewModel
 * @formatter:on
 **************************/
class BookshelfViewModel(val repository: BookShelfRepository) : BaseMviViewModel<BookShelfIntent>() {

    var mBookshelfFlowPager : Flow<PagingData<BookshelfResults>>? = null

    var mOrder = "-datetime_modifier"

    // 默认加载20页 第一次初始化加载的大小默认为 （PageSize * 3）这里也设置成20
    fun getBookShelf(intent: BookShelfIntent.GetBookShelf): Flow<PagingData<BookshelfResults>>? {
        mBookshelfFlowPager = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                BookshelfDataSource { position, pagesize ->
                    flowResult(repository.getBookShelf(position, pagesize, mOrder), intent) { value -> intent.copy(bookshelfResp = value.mResults) }?.mResults?.mList
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
        return mBookshelfFlowPager
    }

    fun clearUserAllData() {
        DataStoreAgent.DATA_USER.clear()
        BaseUser.CURRENT_USER_TOKEN = ""
    }

    override fun dispatcher(intent: BookShelfIntent) {
        when (intent) {
            is BookShelfIntent.GetBookShelf -> getBookShelf(intent)
        }
    }
}