package ru.gohasoft.wanderingtable.core.presentation.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.IconResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

sealed class ButtonConfig<T> : Parcelable {
    abstract val action: Action<T>

    @Parcelize
    class TextButton<T>(
        val text: TextResource,
        override val action: Action<T>
    ) : ButtonConfig<T>()

    @Parcelize
    class IconButton<T>(
        val icon: IconResource,
        override val action: Action<T>
    ) : ButtonConfig<T>()
}