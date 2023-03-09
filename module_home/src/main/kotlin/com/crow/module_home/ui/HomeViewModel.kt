package com.crow.module_home.ui

import com.crow.base.viewmodel.mvi.BaseMviViewModel
import com.crow.module_home.model.HomeEvent
import com.crow.module_home.model.HomeRepository

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/view
 * @Time: 2023/3/6 0:11
 * @Author: CrowForKotlin
 * @Description: HomeViewModel
 * @formatter:on
 **************************/
class HomeViewModel(private val repository: HomeRepository) : BaseMviViewModel<HomeEvent>() {

    private fun getHomePage(event: HomeEvent.GetHomePage) {
        flowResult(repository.getHomePage(), event) { value -> event.copy(homePageData = value) }
    }

    override fun dispatcher(event: HomeEvent) {
        when(event) {
            is HomeEvent.GetHomePage -> getHomePage(event)
        }
    }
}