package com.crow.module_home.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.search.comic_reuslt.SearchComicResult
import com.crow.module_home.model.resp.search.novel_result.SearchNovelResult
import com.crow.module_home.model.source.ComicSearchDataSource
import com.crow.module_home.model.source.NovelSearchDataSource
import com.crow.module_home.network.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/view
 * @Time: 2023/3/6 0:11
 * @Author: CrowForKotlin
 * @Description: HomeViewModel
 * @formatter:on
 **************************/
class HomeViewModel(private val repository: HomeRepository) : BaseMviViewModel<HomeIntent>() {

    private var mRefreshStartIndex = 3

    var mComicSearchFlowPage : Flow<PagingData<SearchComicResult>>? = null
    var mNovelSearchFlowPage : Flow<PagingData<SearchNovelResult>>? = null


    // 获取主页 （返回数据量很多）
    private fun getHomePage(intent: HomeIntent.GetHomePage) {
        flowResult(intent, repository.getHomePage()) { value ->
            intent.copy(homePageData = value)
        }
    }

    // 通过刷新的方式 获取推荐
    private fun getRecPageByRefresh(intent: HomeIntent.GetRecPageByRefresh) {
        flowResult(intent, repository.getRecPageByRefresh(3, mRefreshStartIndex)) { value ->
            mRefreshStartIndex += 3
            intent.copy(recPageData = value)
        }
    }

    private fun doSearchComic(intent: HomeIntent.SearchComic) {
        mComicSearchFlowPage = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                ComicSearchDataSource { position, pagesize ->
                    flowResult(repository.doSearchComic(intent.keyword, intent.type, position, pagesize), intent) { value -> intent.copy(searchComicResp = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    private fun doSearchNovel(intent: HomeIntent.SearchNovel) {
        mNovelSearchFlowPage = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                NovelSearchDataSource { position, pagesize ->
                    flowResult(repository.doSearchNovel(intent.keyword, intent.type, position, pagesize), intent) { value -> intent.copy(searchNovelResp = value.mResults) }.mResults
                }
            }
        ).flow.flowOn(Dispatchers.IO).cachedIn(viewModelScope)
    }

    override fun dispatcher(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.GetHomePage -> getHomePage(intent)
            is HomeIntent.GetRecPageByRefresh -> getRecPageByRefresh(intent)
            is HomeIntent.SearchComic -> doSearchComic(intent)
            is HomeIntent.SearchNovel -> doSearchNovel(intent)
        }
    }
}