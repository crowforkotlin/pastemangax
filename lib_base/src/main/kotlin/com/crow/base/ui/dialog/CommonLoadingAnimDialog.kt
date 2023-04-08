package com.crow.base.ui.dialog

import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.crow.base.R
import com.crow.base.databinding.BaseDialogOriginalLoadingBinding
import com.crow.base.tools.extensions.setBackgroundTransparent
import com.crow.base.tools.extensions.setMaskAmount
import com.crow.base.ui.viewmodel.BaseViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/dialog
 * @Time: 2022/7/10 13:53
 * @Author: CrowForKotlin
 * @Description: Original LoadingAnim Dialog
 * @formatter:off
 **************************/
class CommonLoadingAnimDialog: BaseVBDialogFragment<BaseDialogOriginalLoadingBinding,BaseViewModel>() {

    init {
        setStyle(STYLE_NO_TITLE, R.style.Base_LoadingAnim_Dark)
    }

    companion object {

        @JvmStatic
        @Synchronized
        fun show(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? CommonLoadingAnimDialog ?: CommonLoadingAnimDialog()
            if (!dialog.isVisible) dialog.show(fragmentManager, TAG)
        }

        @JvmStatic
        @Synchronized
        fun dismiss(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? CommonLoadingAnimDialog
            if (dialog?.isVisible == true) dialog.dismiss()
        }
    }

    /* onStart 回调 */
    override fun onStart(){
        super.onStart()
        dialog!!.window!!.apply {
            setBackgroundTransparent()
            setMaskAmount(0f)
            isCancelable = false
        }
    }
    override fun getViewBinding(layoutInflater: LayoutInflater) = BaseDialogOriginalLoadingBinding.inflate(layoutInflater)
    override fun getViewModel(): Lazy<BaseViewModel> = viewModels()


}