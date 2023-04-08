package com.crow.base.tools.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

/*************************
 * @ProjectName: JetpackApp
 * @Dir_Path: lib_base/src/main/java/cn/barry/base
 * @Time: 2022/3/8 10:37
 * @Author: CrowForKotlin
 * @Description: Activity Ext
 **************************/
inline fun <reified T> Context.startActivity() = startActivity(Intent(this, T::class.java))

inline fun <reified T> Context.startActivity(lambda: Intent.() -> Unit) {
    val intent = Intent(this, T::class.java)
    intent.lambda()
    startActivity(intent)
}

fun AppCompatActivity.repeatOnLifecycle(state: Lifecycle.State = Lifecycle.State.STARTED, lifecycleCallBack: LifecycleCallBack) {
    lifecycleScope.launch { repeatOnLifecycle(state) { lifecycleCallBack.onLifeCycle(this) } }
}
