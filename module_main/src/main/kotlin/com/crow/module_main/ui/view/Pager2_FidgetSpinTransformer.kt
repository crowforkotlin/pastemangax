package com.crow.module_main.ui.view

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class Pager2_FidgetSpinTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.translationX = -position * page.width
        if (abs(position.toDouble()) < 0.5) {
            page.visibility = View.VISIBLE
            page.scaleX = (1 - abs(position.toDouble())).toFloat()
            page.scaleY = (1 - abs(position.toDouble())).toFloat()
        } else if (abs(position.toDouble()) > 0.5) {
            page.visibility = View.GONE
        }
        if (position < -1) {     // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0f)
        } else if (position <= 0) {    // [-1,0]
            page.setAlpha(1f)
            page.rotation = (36000 * (abs(position.toDouble()) * abs(position.toDouble()) * abs(
                position.toDouble()
            ) * abs(position.toDouble()) * abs(position.toDouble()) * abs(
                position.toDouble()
            ) * abs(position.toDouble()))).toFloat()
        } else if (position <= 1) {    // (0,1]
            page.setAlpha(1f)
            page.rotation = (-36000 * (abs(position.toDouble()) * abs(position.toDouble()) * abs(
                position.toDouble()
            ) * abs(position.toDouble()) * abs(position.toDouble()) * abs(
                position.toDouble()
            ) * abs(position.toDouble()))).toFloat()
        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.setAlpha(0f)
        }
    }
}