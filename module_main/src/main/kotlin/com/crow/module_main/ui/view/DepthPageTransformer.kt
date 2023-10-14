package com.crow.module_main.ui.view

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.ui.view
 * @Time: 2023/10/10 22:58
 * @Author: CrowForKotlin
 * @Description: 深度页面转换
 * @formatter:on
 **************************/

class DepthPageTransformer : ViewPager2.PageTransformer {

    private val MIN_SCALE = 0.75f
    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 0 -> { // [-1,0]
                    // Use the default slide transition when moving to the left page.
                    alpha = 1f
                    translationX = 0f
                    translationZ = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> { // (0,1]
                    // Fade the page out.
                    alpha = 1 - position

                    // Counteract the default slide transition.
                    translationX = pageWidth * -position
                    // Move it behind the left page.
                    translationZ = -1f

                    // Scale the page down (between MIN_SCALE and 1).
                    val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position)))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}