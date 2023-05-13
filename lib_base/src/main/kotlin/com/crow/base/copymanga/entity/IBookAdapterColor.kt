package com.crow.base.copymanga.entity

import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.crow.base.R
import com.crow.base.app.appContext

interface IBookAdapterColor<T: ViewHolder> {
    private val mRed: Int get() = ContextCompat.getColor(appContext, R.color.base_book_red)
    private val mPurple: Int get() = ContextCompat.getColor(appContext, R.color.base_book_purple)
    private val mIndigo: Int get() = ContextCompat.getColor(appContext, R.color.base_book_indigo)
    private val mGreen: Int get() = ContextCompat.getColor(appContext, R.color.base_book_green)
    private val mGrey: Int get() = ContextCompat.getColor(appContext, R.color.base_book_grey)

    fun setColor(vh: T, @ColorInt color: Int)

    fun toSetColor(vh: T, popular: Int) {
        when {
            popular > 1000_0000 -> setColor(vh, mRed)
            popular > 100_0000 -> setColor(vh, mPurple)
            popular > 10_0000 -> setColor(vh, mIndigo)
            popular > 1_0000 -> setColor(vh, mGreen)
            else -> setColor(vh, mGrey)
        }
    }
}