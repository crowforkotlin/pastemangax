package com.crow.copymanga.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crow.base.tools.extensions.logger
import com.crow.copymanga.databinding.AppActivityTestBinding
import kotlin.concurrent.timer

class TestActivity : AppCompatActivity() {

    val binding by lazy { AppActivityTestBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var progress = 0
        timer("PROGRESS", period = 3000L) {
            progress += 25
            logger(progress)
        }
    }
}