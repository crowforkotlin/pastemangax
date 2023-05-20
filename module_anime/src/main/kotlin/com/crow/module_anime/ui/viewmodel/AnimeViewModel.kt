package com.crow.module_anime.ui.viewmodel

import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_anime.model.AnimeIntent
import com.crow.module_anime.network.AnimeRepository

class AnimeViewModel(val repository: AnimeRepository) : BaseMviViewModel<AnimeIntent>(){

    override fun dispatcher(intent: AnimeIntent) {
        when(intent) {

        }
    }
}