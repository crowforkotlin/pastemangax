package com.crow.base.ui.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.crow.base.R
import com.crow.base.tools.extensions.*

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

    fun interface LoadingAnimCallBack {
        fun onAnimEnd()
    }

    interface LoadingAnimConfig {

        fun isNoInitStyle() : Boolean

        fun doOnConfig(window: Window)
    }

    companion object {

        private val TAG: String = this::class.java.simpleName
        private var mShowTime = 0L
        private const val mDismissFlagTime = 1000L
        private const val mAnimateDuration = 200L

        @JvmStatic
        @Synchronized
        fun show(fragmentManager: FragmentManager,loadingAnimConfig: LoadingAnimConfig? = null) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? LoadingAnimDialog ?: LoadingAnimDialog()
            if (dialog.isAdded) { return }
            if (!dialog.isVisible) {
                mShowTime = System.currentTimeMillis()
                if (!fragmentManager.isStateSaved) {
                    dialog.show(fragmentManager, TAG)
                    if (loadingAnimConfig != null) {
                        if (!loadingAnimConfig.isNoInitStyle()) dialog.lifecycleScope.launchWhenCreated {
                            dialog.setStyle(STYLE_NO_TITLE,  R.style.Base_LoadingAnim_Dark)
                        }
                        dialog.lifecycleScope.launchWhenStarted { loadingAnimConfig.doOnConfig(dialog.dialog?.window ?: return@launchWhenStarted) }
                    } else {
                        dialog.setStyle(STYLE_NO_TITLE,  R.style.Base_LoadingAnim_Dark)
                    }
                }
            }
        }

        @JvmStatic
        @Synchronized
        fun dismiss(fragmentManager: FragmentManager, animCallBack: LoadingAnimCallBack? = null) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? LoadingAnimDialog ?: return
            val consumeTime = System.currentTimeMillis() - mShowTime
            // 判断 取消时间是否大于 显示超1S 时间
            if (mDismissFlagTime > consumeTime) dialog.doAfterDelay(mDismissFlagTime - consumeTime) { dismissWithAnim(dialog, animCallBack) } else dismissWithAnim(dialog, animCallBack)
        }

        private fun dismissWithAnim(dialog: LoadingAnimDialog, animCallBack: LoadingAnimCallBack?) {
            dialog.requireView().animateFadeOut(mAnimateDuration).withEndAction {
                dialog.dismissAllowingStateLoss()
                animCallBack?.onAnimEnd()
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
        view?.animateFadeIn()
    }
}