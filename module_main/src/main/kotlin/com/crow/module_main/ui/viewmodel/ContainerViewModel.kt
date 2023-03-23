package com.crow.module_main.ui.viewmodel

import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_comic.model.intent.ComicIntent
import com.crow.module_main.network.ContainerRepository

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/ui
 * @Time: 2023/3/7 23:56
 * @Author: CrowForKotlin
 * @Description: ContainerViewModel
 * @formatter:on
 **************************/
class ContainerViewModel(val repository: ContainerRepository) : BaseMviViewModel<ComicIntent>() {

    override fun dispatcher(intent: ComicIntent) { }
}