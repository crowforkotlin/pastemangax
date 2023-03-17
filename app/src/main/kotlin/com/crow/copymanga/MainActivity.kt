package com.crow.copymanga

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.crow.base.extensions.animateFadeOut
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
            setOnExitAnimationListener { provider -> provider.view.animateFadeOut(500L).withEndAction { provider.remove() } }
        }

        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        // 设置屏幕方向
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        // 全屏布局
        WindowCompat.setDecorFitsSystemWindows(window, true)
        mBinding.root.setPadding(0, getStatusBarHeight(), 0, getNavigationBarHeight())
    }
}