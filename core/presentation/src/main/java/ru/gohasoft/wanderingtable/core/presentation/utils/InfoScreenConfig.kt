package ru.gohasoft.wanderingtable.core.presentation.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.gohasoft.wanderingtable.core.presentation.utils.ButtonConfig.IconButton
import ru.gohasoft.wanderingtable.core.presentation.utils.ButtonConfig.TextButton
import ru.gohasoft.wanderingtable.core.presentation.utils.InfoScreenConfig.Marker.PrimaryButton
import ru.gohasoft.wanderingtable.core.presentation.utils.InfoScreenConfig.Marker.SecondaryButton
import ru.gohasoft.wanderingtable.core.presentation.utils.InfoScreenConfig.Marker.TextButton1
import ru.gohasoft.wanderingtable.core.presentation.utils.InfoScreenConfig.Marker.TextButton2
import ru.gohasoft.wanderingtable.core.presentation.utils.InfoScreenConfig.Marker.ToolbarNavigationButton
import ru.gohasoft.wanderingtable.core.presentation.utils.InfoScreenConfig.Marker.ToolbarTextButton
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.IconResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

@Parcelize
data class InfoScreenConfig(
    val title: TextResource?,
    val description: TextResource?,
    val icon: IconResource?,
    val primaryButton: TextButton<PrimaryButton>?,
    val secondaryButton: TextButton<SecondaryButton>?,
    val textButton1: TextButton<TextButton1>?,
    val textButton2: TextButton<TextButton2>?,
    val toolbarNavigationButton: IconButton<ToolbarNavigationButton>?,
    val toolbarTitle: TextResource?,
    val toolbarTextButton: TextButton<ToolbarTextButton>?
) : Parcelable {
    fun rebuild(): Builder = Builder(this)

    fun rebuild(block: Builder.() -> Unit) = Builder(this).apply(block).build()

    sealed class Marker {
        data object PrimaryButton
        data object SecondaryButton
        data object TextButton1
        data object TextButton2
        data object ToolbarNavigationButton
        data object ToolbarTextButton
    }

    class Builder {
        var title: TextResource? = null
            private set
        
        var description: TextResource? = null
            private set
        
        var icon: IconResource? = null
            private set
        
        var primaryButton: TextButton<PrimaryButton>? = null
            private set
        
        var secondaryButton: TextButton<SecondaryButton>? = null
            private set
        
        var textButton1: TextButton<TextButton1>? = null
            private set
        
        var textButton2: TextButton<TextButton2>? = null
            private set
        
        var toolbarNavigationButton: IconButton<ToolbarNavigationButton>? = null
            private set
        
        var toolbarTitle: TextResource? = null
            private set
        
        var toolbarTextButton: TextButton<ToolbarTextButton>? = null
            private set

        constructor()

        constructor(block: Builder.() -> Unit) { apply(block) }

        constructor(info: InfoScreenConfig) {
            this.title = info.title
            this.description = info.description
            this.icon = info.icon
            this.primaryButton = info.primaryButton
            this.secondaryButton = info.secondaryButton
            this.textButton1 = info.textButton1
            this.textButton2 = info.textButton2
            this.toolbarNavigationButton = info.toolbarNavigationButton
            this.toolbarTitle = info.toolbarTitle
            this.toolbarTextButton = info.toolbarTextButton
        }

        fun title(title: TextResource) = apply { this.title = title }
        
        fun description(description: TextResource) = apply { this.description = description }
        
        fun icon(icon: IconResource) = apply { this.icon = icon }
        
        fun primaryButton(config: TextButton<PrimaryButton>) = apply { this.primaryButton = config }
        
        fun secondaryButton(config: TextButton<SecondaryButton>) = apply { this.secondaryButton = config }
        
        fun textButton1(config: TextButton<TextButton1>) = apply { this.textButton1 = config }
        
        fun textButton2(config: TextButton<TextButton2>) = apply { this.textButton2 = config }
        
        fun toolbarNavigationButton(config: IconButton<ToolbarNavigationButton>) = apply { this.toolbarNavigationButton = config }
        
        fun toolbarTitle(title: TextResource) = apply { this.toolbarTitle = title }
        
        fun toolbarTextButton(config: TextButton<ToolbarTextButton>) = apply { this.toolbarTextButton = config }

        fun build(): InfoScreenConfig {
            return InfoScreenConfig(
                title = title,
                description = description,
                icon = icon,
                primaryButton = primaryButton,
                secondaryButton = secondaryButton,
                textButton1 = textButton1,
                textButton2 = textButton2,
                toolbarNavigationButton = toolbarNavigationButton,
                toolbarTitle = toolbarTitle,
                toolbarTextButton = toolbarTextButton
            )
        }
    }
}

inline fun InfoScreenConfig(
    block: InfoScreenConfig.Builder.() -> Unit
): InfoScreenConfig = InfoScreenConfig.Builder().apply(block).build()