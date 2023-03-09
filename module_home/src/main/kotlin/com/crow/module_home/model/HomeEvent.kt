package com.crow.module_home.model

import com.crow.base.viewmodel.mvi.BaseMviEvent
import com.crow.module_home.model.resp.HomePageResp

sealed class HomeEvent : BaseMviEvent() {

    data class GetHomePage(val homePageData: HomePageResp? = null) : HomeEvent()

}