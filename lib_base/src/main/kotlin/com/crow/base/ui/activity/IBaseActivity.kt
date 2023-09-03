package com.crow.base.ui.activity

import android.os.Handler
import androidx.annotation.StyleRes
import com.crow.base.R
import com.crow.base.ui.dialog.LoadingAnimDialog

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/barry/base/activity
 * @Time: 2022/10/4 16:55
 * @Author: CrowForKotlin
 * @Description: IBaseActivity 接口基类
 * @formatter:on
 **************************/
interface IBaseActivity {

    /* Handler异步 */
    var mHandler: Handler?

    /* 是否需要延时加载数据 */
    fun isNeedLazyData(): Boolean

    /* 延时加载数据的时间 */
    fun doLazyDataDelayTime(): Long

    /* 执行延时逻辑 */
    fun doLazyData()

    // 显示加载动画
    fun showLoadingAnim(@StyleRes theme: Int = R.style.Base_LoadingAnim)

    // 显示加载动画
    fun showLoadingAnim(@StyleRes theme: Int = R.style.Base_LoadingAnim, loadingAnimConfig: LoadingAnimDialog.LoadingAnimConfig? = null)

    // 隐藏加载动画
    fun dismissLoadingAnim()

    // 隐藏加载动画 With 动画回调
    fun dismissLoadingAnim(animCallBack: LoadingAnimDialog.LoadingAnimCallBack?)

}