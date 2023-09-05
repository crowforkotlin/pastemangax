package com.crow.base.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.crow.base.ui.dialog.LoadingAnimDialog

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/activity
 * @Time: 2022/11/14 19:20
 * @Author: CrowForKotlin
 * @Description: BaseViewBindingActivityImpl 实现
 * @formatter:on
 **************************/
abstract class BaseActivityImpl : AppCompatActivity(), IBaseActivity {

    override var mHandler: Handler? = Handler(Looper.getMainLooper())

    override fun isNeedLazyData(): Boolean = false
    override fun doLazyDataDelayTime(): Long = 300L
    override fun doLazyData() {}

    open fun initData() {}

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun initListener()

    // 获取加载动画 是否需要延时执行数据
    override fun onResume() {
        super.onResume()
        if (isNeedLazyData()) mHandler?.postDelayed({ doLazyData() }, doLazyDataDelayTime())
    }

    // 置空 mHandler
    override fun onDestroy() {
        super.onDestroy()
        mHandler = null
    }

    override fun showLoadingAnim() {
        LoadingAnimDialog.show(supportFragmentManager)
    }

    override fun showLoadingAnim(loadingAnimConfig: LoadingAnimDialog.LoadingAnimConfig?) { LoadingAnimDialog.show(supportFragmentManager, loadingAnimConfig = loadingAnimConfig) }

    override fun dismissLoadingAnim() {
        LoadingAnimDialog.dismiss(supportFragmentManager)
    }

    override fun dismissLoadingAnim(animCallBack: LoadingAnimDialog.LoadingAnimCallBack?) {
        LoadingAnimDialog.dismiss(supportFragmentManager, animCallBack)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView(savedInstanceState)
        initData()
        initListener()
    }
}