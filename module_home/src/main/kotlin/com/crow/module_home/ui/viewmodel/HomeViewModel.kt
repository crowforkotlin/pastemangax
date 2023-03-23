package com.crow.module_home.ui.viewmodel

import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_home.network.HomeRepository
import com.crow.module_home.model.intent.HomeIntent
import com.crow.module_home.model.resp.homepage.results.Results

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

    private var mResult: Results? = null

    fun getResult() = mResult

    // 获取主页 （返回数据量很多）
    private fun getHomePage(intent: HomeIntent.GetHomePage) {
        flowResult(intent, repository.getHomePage()) { value ->
            mResult = value.mResults
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

    override fun dispatcher(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.GetHomePage -> getHomePage(intent)
            is HomeIntent.GetRecPageByRefresh -> getRecPageByRefresh(intent)
        }
    }
}