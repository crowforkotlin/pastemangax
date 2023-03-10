package com.crow.copymanga

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.core.view.WindowCompat
import com.crow.base.extensions.getNavigationBarHeight
import com.crow.base.extensions.getStatusBarHeight
import com.crow.copymanga.databinding.AppActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val mBinding by lazy { AppActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {

        // 启动动画
        installSplashScreen().apply {

            // 可以监听动画是否展示完毕来延长launch的展示
            setKeepOnScreenCondition { false }
            setOnExitAnimationListener { provider -> provider.animateFadeOut_1() }
        }

        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        // 全屏布局
        WindowCompat.setDecorFitsSystemWindows(window, true)
        mBinding.root.setPadding(0, getStatusBarHeight(), 0, getNavigationBarHeight())

        // supportFragmentManager.beginTransaction().replace(R.id.main_header, HomeHeaderFragment()).commit()
        // supportFragmentManager.beginTransaction().replace(R.id.main_body, HomeBodyFragment()).commit()

    }

    // 启动动画淡出方式1
    private fun SplashScreenViewProvider.animateFadeOut_1() {
        view.apply {
            alpha = 1f
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .setDuration(500L)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) { remove() }
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
        }
    }

    // 启动动画淡出方式1
    private fun SplashScreenViewProvider.animateFadeOut_2() {
        AnimationUtils.loadAnimation(this@MainActivity, android.R.anim.fade_out).also {
            view.animation = it
            it.start()
            it.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    remove()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
    }

    // 启动动画向上滑动1
    private fun SplashScreenViewProvider.animateSlideUp_1() {
        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0F, -(view.height.toFloat())).apply {
            interpolator = AnticipateInterpolator()
            duration = 500L
            doOnEnd {
                remove()
            }
            start()
        }
    }

    // 启动动画向上滑动并旋转1
    private fun SplashScreenViewProvider.animateSlideUpAndRotation_1() {
        AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    view,
                    View.TRANSLATION_Y,
                    0F,
                    -(view.height.toFloat())
                )
            ).apply {
                with(ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 180f))
            }
            interpolator = AnticipateInterpolator()
            duration = 500L
            doOnEnd {
                remove()
            }
            start()
        }
    }

    // 启动动画淡出方式 3
    private fun SplashScreenViewProvider.animateFadeOut_3() {
        ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 90f).apply {
            interpolator = AnticipateInterpolator()
            duration = 500L
            doOnEnd {
                remove()
            }
            start()
        }
    }
}