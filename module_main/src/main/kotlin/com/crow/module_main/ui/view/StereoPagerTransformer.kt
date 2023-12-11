package com.crow.module_main.ui.view

import android.animation.TimeInterpolator
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.pow

/**
 * @author wupanjie
 * Use : setPageTransformer(StereoPagerTransformer(mContext.resources.displayMetrics.widthPixels.toFloat()))
 */
class StereoPagerTransformer() : ViewPager2.PageTransformer {

    companion object {
        private const val MAX_ROTATE_Y = 90f
        private val sInterpolator = TimeInterpolator { input ->
            if (input < 0.7) {
                input * 0.7.pow(3.0).toFloat() * MAX_ROTATE_Y
            } else {
                input.toDouble().pow(4.0).toFloat() * MAX_ROTATE_Y
            }
        }
    }

    override fun transformPage(view: View, position: Float) {

        view.pivotY = (view.height / 2).toFloat()

        // [-Infinity,-1) This page is way off-screen to the left.
        when {
            position < -1 -> {
                view.pivotX = 0f
                view.rotationY = MAX_ROTATE_Y
            }

            // [-1,0]
            position <= 0 -> {
                view.pivotX = view.width.toFloat()
                view.rotationY = -sInterpolator.getInterpolation(-position)
            }

            // (0,1]
            position <= 1 -> {
                view.pivotX = 0f
                view.rotationY = sInterpolator.getInterpolation(position)
            }

            // (1,+Infinity] This page is way off-screen to the right.
            else -> {
                view.pivotX = 0f
                view.rotationY = MAX_ROTATE_Y
            }
        }
    }
}