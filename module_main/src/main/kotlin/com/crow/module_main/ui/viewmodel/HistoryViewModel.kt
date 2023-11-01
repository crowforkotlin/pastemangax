package com.crow.module_main.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_main.model.intent.MainIntent
import com.crow.module_main.model.resp.comic_history.ComicHistoryResult
import com.crow.module_main.model.resp.novel_history.NovelHistoryResult
import com.crow.module_main.model.source.ComicHistoryDataSource
import com.crow.module_main.model.source.NovelHistoryDataSource
import com.crow.module_main.network.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.viewmodel
 * @Time: 2023/10/3 19:00
 * @Author: CrowForKotlin
 * @Description: HistoryViewModel
 * @formatter:on
 **************************/
class HistoryViewModel(val repository: MainRepository) : BaseMviViewModel<MainIntent>() {


    private var mOrder: String = ""


    // 暴露的 漫画流
    var mComicHistoryFlowPager: Flow<PagingData<ComicHistoryResult>>? = null
        private set

    // 暴露的 轻小说流
    var mNovelHistoryFlowPager: Flow<PagingData<NovelHistoryResult>>? = null
        private set


    private fun getComicHistory(intent: MainIntent.GetComicHistory) {
        mComicHistoryFlowPager = Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                ComicHistoryDataSource { position, pageSize ->
                    flowResult(
                        repository.getComicHistory(
                            offset = position,
                            limit = pageSize,
                            order = mOrder
                        ), intent
                    ) { value -> intent.copy(comic = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    private fun getNovelHistory(intent: MainIntent.GetNovelHistory) {
        mNovelHistoryFlowPager = Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                NovelHistoryDataSource { position, pageSize ->
                    flowResult(
                        repository.getNovelHistory(
                            offset = position,
                            limit = pageSize,
                            order = mOrder
                        ), intent
                    ) { value -> intent.copy(novel = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    override fun dispatcher(intent: MainIntent, onEndAction: Runnable) {
        when(intent) {
            is MainIntent.GetComicHistory -> getComicHistory(intent = intent)
            is MainIntent.GetNovelHistory -> getNovelHistory(intent = intent)
        }
        onEndAction.run()
    }
}