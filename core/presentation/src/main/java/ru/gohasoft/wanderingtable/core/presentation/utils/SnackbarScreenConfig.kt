package ru.gohasoft.wanderingtable.core.presentation.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.gohasoft.wanderingtable.core.presentation.utils.ButtonConfig.TextButton
import ru.gohasoft.wanderingtable.core.presentation.utils.SnackbarScreenConfig.Marker.ActionButton
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

@Parcelize
data class SnackbarScreenConfig(
    val message: TextResource,
    val actionButton: TextButton<ActionButton>?,
    val duration: Duration,
    val withDismissAction: Boolean
) : Parcelable {

    fun rebuild(): Builder = Builder(this)

    fun rebuild(block: Builder.() -> Unit) = Builder(this).apply(block).build()

    sealed class Marker {
        data object ActionButton
    }

    enum class Duration {
        SHORT,
        LONG,
        INDEFINITE
    }

    class Builder {
        var message: TextResource? = null
            private set

        var actionButton: TextButton<ActionButton>? = null
            private set

        var duration: Duration = Duration.SHORT
            private set

        var withDismissAction: Boolean = false
            private set

        constructor()

        constructor(block: Builder.() -> Unit) { apply(block) }

        constructor(config: SnackbarScreenConfig) {
            this.message = config.message
            this.actionButton = config.actionButton
            this.duration = config.duration
            this.withDismissAction = config.withDismissAction
        }

        fun message(message: TextResource) = apply { this.message = message }

        fun actionButton(config: TextButton<ActionButton>) = apply { this.actionButton = config }

        fun duration(duration: Duration) = apply { this.duration = duration }

        fun withDismissAction(withDismissAction: Boolean) = apply { this.withDismissAction = withDismissAction }

        fun build(): SnackbarScreenConfig {
            return SnackbarScreenConfig(
                message = requireNotNull(message) { "SnackbarScreenConfig requires a message" },
                actionButton = actionButton,
                duration = duration,
                withDismissAction = withDismissAction
            )
        }
    }
}

inline fun SnackbarScreenConfig(
    block: SnackbarScreenConfig.Builder.() -> Unit
): SnackbarScreenConfig = SnackbarScreenConfig.Builder().apply(block).build()
