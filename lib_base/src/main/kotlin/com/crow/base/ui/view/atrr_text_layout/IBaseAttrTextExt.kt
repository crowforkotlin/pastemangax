package com.crow.base.ui.view.atrr_text_layout

import android.graphics.Paint
import kotlin.math.abs

interface IBaseAttrTextExt {

    /**
     * ● 获取文本高度：ascent绝对值 + descent
     *
     * ● 2023-11-29 17:01:15 周三 下午
     * @author crowforkotlin
     */
    fun getTextHeight(fontMetrics: Paint.FontMetrics) : Float {
        return abs(fontMetrics.ascent) + fontMetrics.descent
    }
}