package com.crow.base.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crow.base.ui.dialog.LoadingAnimDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/fragment
 * @Time: 2022/11/14 21:26
 * @Author: CrowForKotlin
 * @Description: BaseVBBottomSheetDfImpl
 * @formatter:on
 **************************/
abstract class BaseBottomSheetDFImpl : BottomSheetDialogFragment(), IBaseFragment {

    override fun initData(savedInstanceState: Bundle?) {}

    override fun showLoadingAnim(loadingAnimConfig: LoadingAnimDialog.LoadingAnimConfig?) {
        LoadingAnimDialog.show(parentFragmentManager, loadingAnimConfig)
    }

    override fun dismissLoadingAnim() { LoadingAnimDialog.dismiss(parentFragmentManager) }

    override fun dismissLoadingAnim(loadingAnimCallBack: LoadingAnimDialog.LoadingAnimCallBack) { LoadingAnimDialog.dismiss(parentFragmentManager, loadingAnimCallBack) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return getView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initData(savedInstanceState)
        initListener()
    }
}