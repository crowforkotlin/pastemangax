package com.crow.base.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.crow.base.R
import com.crow.base.extensions.doAfterDelay
import com.crow.base.extensions.setBackgroundTransparent
import com.crow.base.extensions.setMaskAmount

/*************************
 * @Machine: RedmiBook Pro 15
 * @RelativePath: cn\barry\base\dialog\LoadingAnimDialog.kt
 * @Path: D:\Barry\B_study\Android\Android_Project\JetpackApp\lib_base\src\main\java\cn\barry\base\dialog\LoadingAnimDialog.kt
 * @Author: CrowForKotlin
 * @Time: 2022/5/2 12:23 周一 下午
 * @Description: 加载动画弹窗
 * @formatter:off
 *************************/



class LoadingAnimDialog : BaseVBDialogFragmentImpl() {

    init {
        setStyle(STYLE_NO_TITLE, R.style.Base_LibBase_LoadingAnim)
    }

    companion object {

        private val TAG: String = this::class.java.simpleName
        private var showTime = 0L
        private const val dismissFlagTime = 1000L

        @JvmStatic
        @Synchronized
        fun show(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? LoadingAnimDialog ?: LoadingAnimDialog()
            if (!dialog.isVisible) {
                showTime = System.currentTimeMillis()
                if (!fragmentManager.isStateSaved) dialog.show(fragmentManager, TAG)
            }
        }

        @JvmStatic
        @Synchronized
        fun dismiss(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? LoadingAnimDialog ?: return
            val consumeTime = System.currentTimeMillis() - showTime
            if (dismissFlagTime > consumeTime) dialog.doAfterDelay(dismissFlagTime - consumeTime) { if (dialog.isVisible) dialog.dismiss() }
            else if (dialog.isVisible) dialog.dismissAllowingStateLoss()
        }
    }

    override fun getView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.base_dialog_loading, container, false)
    }
    override fun onStart() {
        super.onStart()
        dialog!!.window!!.apply {
            setBackgroundTransparent()
            setMaskAmount(0f)
            isCancelable = false
        }
    }

}