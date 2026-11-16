package ru.gohasoft.wanderingtable.feature.auth.utils

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun StyleStatusBarIntoLight() {
    val context = LocalContext.current
    LaunchedEffect(context) {
        context.styleStatusBarIntoLight()
    }
}

internal fun Context.styleStatusBarIntoLight() {
    val activity = this as? ComponentActivity
    activity?.enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
        )
    )
}