package com.crow.mangax.ui.text

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

data class AttrTextFrameConfig(
    val mLeft: Boolean,
    val mTop: Boolean,
    val mRight: Boolean,
    val mBottom: Boolean,
    val mLineWidth: Float = 1f,
    val mType: FrameType = FrameType.SOLID,
    val mAnimationEnable: Boolean = false,
    val mAnimationSpeed: Int = 0,
    val mColor: Int = Color.RED,
    val mAntiAliasEnable: Boolean = false,
    val mGradient: Byte? = null
) {

    val mPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
        isAntiAlias = mAntiAliasEnable
        color = mColor
        strokeWidth = mLineWidth
    }
    enum class FrameType { SOLID }
}
