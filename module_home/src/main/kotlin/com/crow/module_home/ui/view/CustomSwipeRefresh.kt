package com.crow.module_home.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.Animation.AnimationListener
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.lang.reflect.Method
import kotlin.math.abs

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/crow/module_home/view/customviews
 * @Time: 2022/11/20 20:02
 * @Author: CrowForKotlin
 * @Description: CustomSwiper 使用时全部反射属性仅作一次延迟初始化，减少在启动时的开销
 * @formatter:off
 **************************/
class CustomSwipeRefresh : SwipeRefreshLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    // 通过反射获取父类
    private val superClazz by lazy { javaClass.superclass }

    // 反射获取父类进度条
    private val mProgress: CircularProgressDrawable by lazy {
        superClazz.getDeclaredField("mProgress").apply { isAccessible = true }.get(this) as CircularProgressDrawable
    }

    // 反射获取ScaleDownMethod
    private val mScaleDownFunc: Method by lazy {
        superClazz.getDeclaredMethod("startScaleDownAnimation", AnimationListener::class.java).apply { isAccessible = true }
    }

    // 反射获取动画监听
    private val mRefreshListener: AnimationListener by lazy {
        superClazz.getDeclaredField("mRefreshListener").apply { isAccessible = true }.get(this) as AnimationListener
    }

    private var mDownX = 0f
    private var mDownY = 0f
    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop * 2

    // 因为Swipe滑动中止会保留指示器， 所以反射修复强制取消指示器， 其次做一个和ViewPager左右滑动不冲突的实现
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when(ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = ev.rawX
                mDownY = ev.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                // -0.125f是进度条不拖动但以显示出来的一个临界值
                val dx = abs(ev.rawX - mDownX)
                val dy = abs(ev.rawY - mDownY)
                if (dx > dy && dx > mTouchSlop) { return false }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (mProgress.progressRotation == -0.125f) mScaleDownFunc.invoke(this, mRefreshListener)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}