package com.crow.module_main.ui.viewmodel

import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_comic.model.intent.BookIntent
import com.crow.module_main.network.ContainerRepository

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/ui
 * @Time: 2023/3/7 23:56
 * @Author: CrowForKotlin
 * @Description: ContainerViewModel
 * @formatter:on
 **************************/
class ContainerViewModel(val repository: ContainerRepository) : BaseMviViewModel<BookIntent>() {

    override fun dispatcher(intent: BookIntent) { }
}