package com.crow.base.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.FloatRange
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.crow.base.R
import com.crow.base.copymanga.appIsDarkMode
import com.crow.base.databinding.BaseDialogLoadingBinding
import com.crow.base.tools.coroutine.baseCoroutineException
import com.crow.base.tools.extensions.BASE_ANIM_200L
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut
import com.crow.base.tools.extensions.doOnInterval
import com.crow.base.tools.extensions.doResetEventFlagTime
import com.crow.base.ui.view.event.BaseEvent
import com.crow.base.ui.view.event.BaseEventEntity
import com.crow.base.ui.view.event.click.BaseIEventIntervalExt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*************************
 * @Machine: RedmiBook Pro 15
 * @RelativePath: cn\barry\base\dialog\LoadingAnimDialog.kt
 * @Path: D:\Barry\B_study\Android\Android_Project\JetpackApp\lib_base\src\main\java\cn\barry\base\dialog\LoadingAnimDialog.kt
 * @Author: CrowForKotlin
 * @Time: 2022/5/2 12:23 周一 下午
 * @Description: 加载动画弹窗
 * @formatter:off
 *************************/
class LoadingAnimDialog() : DialogFragment() {

    init {
        setStyle(STYLE_NO_TITLE, R.style.Base_LoadingAnim)
    }

    val mBinding: BaseDialogLoadingBinding get() = _mBinding!!
    private var _mBinding: BaseDialogLoadingBinding? = null
    private var mIsApplyConfig: Boolean = false

    private val mWindowInsetsControllerCompat by lazy { WindowInsetsControllerCompat(dialog!!.window!!, mBinding.root) }

    fun interface LoadingAnimCallBack {
        fun onAnimEnd()
    }

    fun interface LoadingAnimConfig {

        fun initConfig(dialog: LoadingAnimDialog)
    }

    companion object {

        private val TAG: String = this::class.java.simpleName
        private var mShowTime = 0L
        private val mBaseEvent = BaseEvent.newInstance(1000L)

        @JvmStatic
        fun show(fragmentManager: FragmentManager, loadingAnimConfig: LoadingAnimConfig? = null) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? LoadingAnimDialog ?: LoadingAnimDialog()
            if (dialog.isAdded || dialog.isVisible || fragmentManager.isStateSaved) return
            mBaseEvent.doResetEventFlagTime(0L)
            mBaseEvent.doOnInterval { dialog.show(fragmentManager, TAG) }
            mBaseEvent.doResetEventFlagTime(1000L)
            if (loadingAnimConfig != null) {
                dialog.mIsApplyConfig = true
                dialog.lifecycleScope.launchWhenStarted { loadingAnimConfig.initConfig(dialog) }
            }

        }

        @JvmStatic
        fun dismiss(fragmentManager: FragmentManager, animCallBack: LoadingAnimCallBack? = null) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? LoadingAnimDialog ?: return
            mBaseEvent.doOnIntervalResult(dialog, mBaseEvent, object : BaseIEventIntervalExt<LoadingAnimDialog> {
                override fun onIntervalOk(baseEventEntity: BaseEventEntity<LoadingAnimDialog>) {
                    dismissWithAnim(dialog, animCallBack)
                }
                override fun onIntervalFailure(gapTime: Long) {
                    dialog.lifecycleScope.launch(baseCoroutineException) {
                        delay(gapTime)
                        dismissWithAnim(dialog, animCallBack)
                    }
                }
            })
        }

        private fun dismissWithAnim(dialog: LoadingAnimDialog, animCallBack: LoadingAnimCallBack?) {
            dialog.mBinding.root.animateFadeOut(BASE_ANIM_200L).withEndAction {
                dialog.dismissAllowingStateLoss()
                animCallBack?.onAnimEnd()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BaseDialogLoadingBinding.inflate(inflater).also { _mBinding = it }.root
    }

    override fun onStart() {
        super.onStart()
        isCancelable = false
        if (!mIsApplyConfig) { applyWindow() }
        mBinding.root.animateFadeIn()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    fun applyWindow(lightStatusbar: Boolean = appIsDarkMode, @FloatRange(from = 0.0, to = 1.0) dimAmount: Float = 0f, isFullScreen: Boolean = false) {
        dialog?.window?.apply {
            if (isFullScreen) addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setWindowAnimations(com.google.android.material.R.style.Animation_AppCompat_Dialog)
            setDimAmount(dimAmount)
            mWindowInsetsControllerCompat.isAppearanceLightStatusBars = !lightStatusbar
        }
    }

}