package com.crow.module_book.ui.view.comic

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.crow.module_book.databinding.BookPageBadgeViewBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")
class PageBadgeView(private val mLayoutInflater: LayoutInflater, private val mLifecycleOwner: LifecycleOwner) {

    val mBadgeBinding = BookPageBadgeViewBinding.inflate(mLayoutInflater)

    private val mFormatTime = SimpleDateFormat("HH:mm:ss")

    init {
        mLifecycleOwner.lifecycleScope.launch {
            repeat(Int.MAX_VALUE) {
                mBadgeBinding.badgeTime.text = getTime()
                delay(1000L)
            }
        }
    }

    fun getTime(): String = " ${mFormatTime.format(Date())}"

    fun updateTotalCount(count: Int) { mBadgeBinding.badgeTotal.text = "$count" }

    fun updateCurrentPos(pos: Int) { mBadgeBinding.badgeCurrent.text = pos.toString() }

}