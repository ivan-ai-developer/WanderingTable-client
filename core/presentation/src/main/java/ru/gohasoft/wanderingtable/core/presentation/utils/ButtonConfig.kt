package ru.gohasoft.wanderingtable.core.presentation.utils

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.IconResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

sealed class ButtonConfig<T> {
    abstract val action: Action<T>

    class TextButton<T>(
        val text: TextResource,
        override val action: Action<T>
    ) : ButtonConfig<T>()
    
    class IconButton<T>(
        val icon: IconResource,
        override val action: Action<T>
    ) : ButtonConfig<T>()
}