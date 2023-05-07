package com.crow.copymanga.model.di

import android.graphics.drawable.Drawable
import com.bumptech.glide.GenericTransitionOptions
import org.koin.dsl.module

val singleModule = module {

    single {
        GenericTransitionOptions<Drawable>()
    }
}