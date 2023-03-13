package com.crow.base.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
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



class LoadingAnimDialog : DialogFragment() {

    init {
        setStyle(STYLE_NO_TITLE, R.style.Base_LibBase_LoadingAnim)
    }
    fun interface LoadingAnimCallBack {
        fun onAnimEnd()
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
        fun dismiss(fragmentManager: FragmentManager, animCallBack: LoadingAnimCallBack? = null) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? LoadingAnimDialog ?: return
            val consumeTime = System.currentTimeMillis() - showTime
            if (dismissFlagTime > consumeTime) {
                dialog.doAfterDelay(dismissFlagTime - consumeTime) {
                    if (dialog.isVisible) {
                        dialog.dismissAllowingStateLoss()
                        animCallBack?.onAnimEnd()
                    }
                }
            } else if (dialog.isVisible) {
                val view = dialog.requireView()
                view.alpha = 1f
                view.visibility = View.VISIBLE
                view.animate().alpha(0f).withEndAction {
                    dialog.dismissAllowingStateLoss()
                    animCallBack?.onAnimEnd()
                }.duration = 250L
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.base_dialog_loading, container, false)
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
        val window = dialog!!.window!!
        window.setBackgroundTransparent()
        window.setMaskAmount(0f)
        window.decorView.alpha = 0f
        window.decorView.visibility = View.VISIBLE
        window.decorView.animate().alpha(1f).duration = 200L
    }
}