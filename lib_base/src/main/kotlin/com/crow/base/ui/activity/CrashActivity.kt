package com.crow.base.ui.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.crow.base.app.BaseAppExceptionHandler
import com.crow.base.ui.compose.CopyMangaXTheme

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.base.ui.activity
 * @Time: 2023/8/2 0:18
 * @Author: CrowForKotlin
 * @Description: CrashActivity
 * @formatter:on
 **************************/
class CrashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val exception = BaseAppExceptionHandler.getThrowableFromIntent(intent)

        setContent {
            CopyMangaXTheme(

            ) {
            }
        }
    }
}