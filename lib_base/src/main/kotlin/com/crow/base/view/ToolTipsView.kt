package com.crow.base.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.crow.base.databinding.BaseTextviewBinding

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/view
 * @Time: 2022/11/20 10:43
 * @Author: BarryAllen
 * @Description: ToolTipsView
 * @formatter:on
 **************************/
class ToolTipsView(context: Context, layoutInflater: LayoutInflater) {

    private val binding by lazy { BaseTextviewBinding.inflate(layoutInflater) }
    private val popupWindow by lazy {
        PopupWindow(context).apply {
            contentView = binding.root
            setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent))
        }
    }

    fun View.showToolTips(content: String, offsetX: Int = 0, offsetY: Int = 0) {
        binding.textview.text = content
        popupWindow.isFocusable = false          // 聚焦取消
        popupWindow.isOutsideTouchable = true    // 允许外部触摸
        popupWindow.update()                     // 更新后就可以点击提示视图外部取消提示
        popupWindow.showAsDropDown(this, offsetX, offsetY)
    }
}