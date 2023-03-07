package com.crow.module_home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crow.base.viewmodel.BaseViewModel
import com.crow.module_home.model.HomeRepository
import com.crow.module_home.model.resp.HomePageResp

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/view
 * @Time: 2023/3/6 0:11
 * @Author: BarryAllen
 * @Description: HomeViewModel
 * @formatter:on
 **************************/
class HomeViewModel(private val repository: HomeRepository) : BaseViewModel() {

    private val _mHomePageData = MutableLiveData<HomePageResp>()
    val mHomePageResp: LiveData<HomePageResp> get() = _mHomePageData

    fun getHomePage() {
        flowLaunch(repository.getHomePage()) {
            _mHomePageData.value = it
        }
    }
}