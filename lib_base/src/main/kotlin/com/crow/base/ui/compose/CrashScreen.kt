package com.crow.base.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.crow.base.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@Composable
fun CrashScreen(exception: Throwable?, onRestartClick: () -> Unit, onAcceptClick: () -> Unit) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    DisposableEffect(systemUiController, useDarkIcons) {
        // Update all of the system bar colors to be transparent, and use
        // dark icons if we're in light theme
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        // setStatusBarColor() and setNavigationBarColor() also exist

        onDispose {}
    }
    InfoScreen(
        icon = Icons.Outlined.BugReport,
        headingText = stringResource(R.string.base_crash_screen_title),
        subtitleText = stringResource(
            R.string.base_crash_screen_description,
            stringResource(R.string.base_app_name)
        ),
        acceptText = stringResource(R.string.base_crash_screen_shared),
        onAcceptClick = {
            scope.launch {
                CrashLogUtil(context).dumpLogs()
            }
        },
        rejectText = stringResource(R.string.base_crash_screen_restart_application),
        onRejectClick = onRestartClick,
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = MaterialTheme.padding.small)
                .clip(MaterialTheme.shapes.small)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Text(
                text = exception?.stackTraceToString() ?: exception.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(all = MaterialTheme.padding.small)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun CrashScreenPreview() {
    CopyMangaXTheme {
        CrashScreen(
            exception = RuntimeException("Dummy"),
            onRestartClick = {

            },
            onAcceptClick = {

            }
        )
    }
}