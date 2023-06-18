package com.crow.base.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.crow.base.tools.extensions.permissionext.IBasePerEvent
import com.crow.base.tools.extensions.permissionext.IBasePermission
import com.crow.base.ui.fragment.IBaseFragment

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/dialog
 * @Time: 2022/11/14 20:57
 * @Author: CrowForKotlin
 * @Description: BaseVBDialogFragmentImpl
 * @formatter:on
 **************************/
abstract class BaseVBDialogFragmentImpl : DialogFragment(), IBaseFragment, IBasePermission {

    private val mPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.containsValue(false)) iBasePerEvent?.onFailure()
        else iBasePerEvent?.onSccess()
    }

    // 初始化View
    override fun initView(bundle: Bundle?) {}

    // 初始化监听事件
    override fun initListener() {}

    // 初始化数据
    override fun initData(savedInstanceState: Bundle?) {}

    // 初始化观察者
    override fun initObserver(saveInstanceState: Bundle?) {}

    override fun showLoadingAnim(loadingAnimConfig: LoadingAnimDialog.LoadingAnimConfig?) {
        LoadingAnimDialog.show(parentFragmentManager, loadingAnimConfig)
    }

    override fun dismissLoadingAnim() {
        LoadingAnimDialog.dismiss(parentFragmentManager)
    }

    override fun dismissLoadingAnim(loadingAnimCallBack: LoadingAnimDialog.LoadingAnimCallBack) {
        LoadingAnimDialog.dismiss(parentFragmentManager, loadingAnimCallBack)
    }

    override var iBasePerEvent: IBasePerEvent? = null

    override fun requestPermission(permissions: Array<String>, iBasePerEvent: IBasePerEvent) {
        this.iBasePerEvent = iBasePerEvent
        mPermission.launch(permissions)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return getView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initData(savedInstanceState)
        initListener()
    }
}