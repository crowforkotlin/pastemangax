package com.crow.base.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.google.accompanist.themeadapter.material3.createMdc3Theme

@Composable
fun CopyMangaXTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    val (colorScheme, typography, shape) = createMdc3Theme(
        context = context,
        layoutDirection = layoutDirection,
    )

    MaterialTheme(
        colorScheme = colorScheme!!,
        typography = typography!!,
        shapes = shape!!,
        content = content
    )
}