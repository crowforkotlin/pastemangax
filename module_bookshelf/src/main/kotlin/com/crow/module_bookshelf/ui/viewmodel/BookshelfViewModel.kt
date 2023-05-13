package com.crow.module_bookshelf.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_bookshelf.model.intent.BookshelfIntent
import com.crow.module_bookshelf.model.resp.bookshelf_comic.BookshelfComicResults
import com.crow.module_bookshelf.model.resp.bookshelf_novel.BookshelfNovelResults
import com.crow.module_bookshelf.model.source.BookshelfComicDataSource
import com.crow.module_bookshelf.model.source.BookshelfNovelDataSource
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
class BookshelfViewModel(val repository: BookShelfRepository) : BaseMviViewModel<BookshelfIntent>() {

    var mBookshelfComicFlowPager : Flow<PagingData<BookshelfComicResults>>? = null
    var mBookshelfNovelFlowPager : Flow<PagingData<BookshelfNovelResults>>? = null

    var mOrder = "-datetime_modifier"

    // 默认加载20页 第一次初始化加载的大小默认为 （PageSize * 3）这里也设置成20
    fun getBookshelfComic(intent: BookshelfIntent.GetBookshelfComic) {
        mBookshelfComicFlowPager = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                BookshelfComicDataSource { position, pagesize ->
                    flowResult(repository.getBookshelfComic(position, pagesize, mOrder), intent) { value -> intent.copy(bookshelfComicResp = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    fun getBookshelfNovel(intent: BookshelfIntent.GetBookshelfNovel) {
        mBookshelfNovelFlowPager = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                BookshelfNovelDataSource { position, pagesize ->
                    flowResult(repository.getBookshelfNovel(position, pagesize, mOrder), intent) { value -> intent.copy(bookshelfNovelResp = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    override fun dispatcher(intent: BookshelfIntent) {
        when (intent) {
            is BookshelfIntent.GetBookshelfComic -> getBookshelfComic(intent)
            is BookshelfIntent.GetBookshelfNovel -> getBookshelfNovel(intent)
        }
    }
}