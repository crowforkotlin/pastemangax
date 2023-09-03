package com.crow.base.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.crow.base.ui.dialog.LoadingAnimDialog

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/fragment
 * @Time: 2022/11/14 20:15
 * @Author: CrowForKotlin
 * @Description: BaseVBFragmentImpl
 * @formatter:off
 **************************/
abstract class BaseFragmentImpl : Fragment(), IBaseFragment {

    /** UI Handler */
    protected val mHandler by lazy { Handler(Looper.getMainLooper()) }

    // 初始化View
    override fun initView(savedInstanceState: Bundle?) {}

    // 初始化监听事件
    override fun initListener() {}

    // 初始化数据
    override fun initData(savedInstanceState: Bundle?) { }

    override fun showLoadingAnim(loadingAnimConfig: LoadingAnimDialog.LoadingAnimConfig?) { LoadingAnimDialog.show(childFragmentManager, loadingAnimConfig = loadingAnimConfig) }

    override fun dismissLoadingAnim() { LoadingAnimDialog.dismiss(childFragmentManager) }

    override fun dismissLoadingAnim(loadingAnimCallBack: LoadingAnimDialog.LoadingAnimCallBack) {
        LoadingAnimDialog.dismiss(childFragmentManager) { loadingAnimCallBack.onAnimEnd()  }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return getView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initData(savedInstanceState)
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy(){
        super.onDestroy()
        dismissLoadingAnim()
    }
}
