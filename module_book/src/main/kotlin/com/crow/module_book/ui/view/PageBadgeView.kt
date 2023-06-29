package com.crow.module_book.ui.view

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.view.setMargins
import androidx.lifecycle.lifecycleScope
import com.crow.module_book.databinding.BookActivityComicBinding
import com.crow.module_book.databinding.BookPageBadgeViewBinding
import com.crow.module_book.ui.activity.ComicActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import com.crow.base.R as baseR

@SuppressLint("SimpleDateFormat")
class PageBadgeView(val mActivity: ComicActivity, val mBinding: BookActivityComicBinding) {

    val mBadgeBinding = BookPageBadgeViewBinding.inflate(mActivity.layoutInflater)


    private val mFormatTime = SimpleDateFormat("HH:mm:ss")

    init {
        mBinding.comicConstraint.addView(mBadgeBinding.root)
        mBadgeBinding.root.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            endToEnd = PARENT_ID
            topToBottom = mBinding.mangaReaderToolbar.id
            setMargins(mActivity.resources.getDimensionPixelSize(baseR.dimen.base_dp10))
        }
        mActivity.lifecycleScope.launch {
            repeat(Int.MAX_VALUE) {
                mBadgeBinding.badgeTime.text = getTime()
                delay(1000L)
            }
        }
    }

    fun getTime(): String {
        return " ${mFormatTime.format(Date())}"
//        val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
//        return "  ${time.hour}:${time.minute}"
    }

    fun updateTotalCount(count: Int) { mBadgeBinding.badgeTotal.text = "$count" }

    fun updateCurrentPos(pos: Int) { mBadgeBinding.badgeCurrent.text = pos.toString() }

}