package com.crow.base.ui.view


import android.view.View
import android.view.ViewStub
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.crow.base.tools.extensions.animateFadeIn
import com.crow.base.tools.extensions.animateFadeOut

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.base.ui.view
 * @Time: 2023/10/29 21:02
 * @Author: CrowForKotlin
 * @Description: BaseViewStub
 * @formatter:on
 **************************/
abstract class BaseViewStub<VB : ViewBinding>(
    private val mViewStub: ViewStub,
    val mLifecycle: Lifecycle,
    val mOnBinding: IBaseViewStub<VB>
) : LifecycleEventObserver {

    /**
     * ● 错误视图
     *
     * ● 2023-10-29 20:59:42 周日 下午
     * @author crowforkotlin
     */
    var mView: View? = null

    /**
     * ● 为当前Lifecycle 添加观察此对象
     *
     * ● 2023-10-29 21:17:33 周日 下午
     * @author crowforkotlin
     */
    init {
        mLifecycle.addObserver(this)
    }

    abstract fun bindViewBinding(view: View): VB

    /**
     * ● 当声明周期处于销毁状态时移除ErrorView 防止内存泄漏
     *
     * ● 2023-10-29 21:16:55 周日 下午
     * @author crowforkotlin
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                mView = null
                mLifecycle.removeObserver(this)
            }

            else -> {}
        }
    }

    /**
     * ● 加载错误视图
     *
     * ● 2023-10-29 21:07:58 周日 下午
     * @author crowforkotlin
     */
    fun loadLayout(visible: Boolean, animation: Boolean = false) {
        if (mView == null) {
            mView = mViewStub.inflate()
            mView?.let { view -> mOnBinding.onBinding(view, bindViewBinding(view)) }
            if (animation) {
                if (isVisible()) mView?.animateFadeIn()
                else {
                    mView?.let {  view ->
                        view.animateFadeOut().withEndAction {
                            view.isGone = true
                            mViewStub.isGone = true
                        }
                    }
                }
            } else {
                mView?.isGone = true
                mViewStub.isGone = true
            }
        } else {
            mView?.let { view ->
                when {
                    visible -> {
                        if (animation) {
                            mViewStub.isVisible = true
                            view.animateFadeIn()
                        } else mViewStub.isVisible = true
                    }

                    animation -> {
                        view.animateFadeOut().withEndAction {
                                view.isGone = true
                                mViewStub.isGone = true
                            }
                    }

                    else -> {
                        view.isGone = true
                        mViewStub.isGone = true
                    }
                }
            }
        }
    }

    /**
     * ● ErrorView 和 ViewStub 是否可见
     *
     * ● 2023-10-29 21:16:42 周日 下午
     * @author crowforkotlin
     */
    fun isVisible() = mView?.isVisible == true || mViewStub.isVisible

    /**
     * ● ErrorView 和 ViewStub 是否消失
     *
     * ● 2023-10-29 21:29:27 周日 下午
     * @author crowforkotlin
     */
    fun isGone() = mView?.isGone == true || mViewStub.isGone
}