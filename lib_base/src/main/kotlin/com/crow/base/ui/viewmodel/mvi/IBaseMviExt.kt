package com.crow.base.ui.viewmodel.mvi

import androidx.lifecycle.Lifecycle
import com.crow.base.tools.extensions.repeatOnLifecycle

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/ui/viewmodel/mvi
 * @Time: 2023/4/6 14:41
 * @Author: CrowForKotlin
 * @Description: IBaseMviExt
 * @formatter:on
 **************************/
interface IBaseMviExt {
    fun <I : BaseMviIntent> BaseMviViewModel<I>.onOutput(state: Lifecycle.State = Lifecycle.State.CREATED, baseMviSuspendResult: BaseMviViewModel.BaseMviSuspendResult<I>)
}