package com.crow.module_book.ui.view

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.view.setMargins
import com.crow.module_book.databinding.BookActivityComicBinding
import com.crow.module_book.databinding.BookPageBadgeViewBinding
import com.crow.module_book.ui.activity.ComicActivity
import java.text.SimpleDateFormat
import java.util.Date
import com.crow.base.R as baseR

class PageBadgeView(val mActivity: ComicActivity, val mBinding: BookActivityComicBinding) {

    val mBadgeBinding = BookPageBadgeViewBinding.inflate(mActivity.layoutInflater)

    init {
        mBinding.root.addView(mBadgeBinding.root)
        mBadgeBinding.root.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            endToEnd = PARENT_ID
            topToTop = PARENT_ID
            setMargins(mActivity.resources.getDimensionPixelSize(baseR.dimen.base_dp10))
        }
        mBadgeBinding.badgeTime.text = getTime()
    }

    fun getTime(): String {
        return " ${SimpleDateFormat("HH:mm:ss").format(Date())}"
//        val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
//        return "  ${time.hour}:${time.minute}"
    }

    fun updateTotalCount(count: Int) { mBadgeBinding.badgeTotal.text = "$count" }

    fun updateCurrentPos(pos: Int) { mBadgeBinding.badgeCurrent.text = pos.toString() }

}