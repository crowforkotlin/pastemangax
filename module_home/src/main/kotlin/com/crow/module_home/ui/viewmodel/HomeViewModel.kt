package com.crow.module_home.ui.viewmodel

import com.crow.base.viewmodel.mvi.BaseMviViewModel
import com.crow.module_home.model.factory.HomeRepository
import com.crow.module_home.model.intent.HomeEvent

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/view
 * @Time: 2023/3/6 0:11
 * @Author: CrowForKotlin
 * @Description: HomeViewModel
 * @formatter:on
 **************************/
class HomeViewModel(private val repository: HomeRepository) : BaseMviViewModel<HomeEvent>() {

    var mRefreshStartIndex = 3
        private set

    private fun getHomePage(event: HomeEvent.GetHomePage) {
        flowResult(repository.getHomePage(), event) { value -> event.copy(homePageData = value) }
    }

    private fun getRecPageByRefresh(event: HomeEvent.GetRecPageByRefresh) {
        flowResult(repository.getRecPageByRefresh(3, mRefreshStartIndex), event) { value ->
            mRefreshStartIndex += 3
            event.copy(recPageData = value)
        }
    }

    override fun dispatcher(event: HomeEvent) {
        when (event) {
            is HomeEvent.GetHomePage -> getHomePage(event)
            is HomeEvent.GetRecPageByRefresh -> getRecPageByRefresh(event)
        }
    }
}