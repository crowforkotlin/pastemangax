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

class FadePageTransformer : ViewPager2.PageTransformer {

    private val MIN_SCALE = 0.75f
    override fun transformPage(view: View, position: Float) {

        view.translationX = -position*view.width

        view.setAlpha(1- abs(position))

    }
}