package ru.gohasoft.wanderingtable.core.presentation.utils.resource

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import kotlinx.parcelize.Parcelize

sealed interface IconResource : Parcelable {

    @Parcelize
    data class DrawableResource(@param:DrawableRes val id: Int) : IconResource
}

@Composable
fun IconResource.asPainter(): Painter = when (this) {
    is IconResource.DrawableResource -> painterResource(id)
}
