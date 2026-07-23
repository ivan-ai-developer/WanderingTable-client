package ru.gohasoft.wanderingtable.core.presentation.utils.resource

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.parcelize.Parcelize

sealed interface TextResource : Parcelable {

    fun getText(context: Context): String

    @Parcelize
    data class DynamicString(val value: String) : TextResource {
        override fun getText(context: Context): String {
            return value
        }
    }

    @Parcelize
    data class StringResource(
        @param:StringRes val id: Int,
        val args: List<String> = emptyList(),
    ) : TextResource {
        override fun getText(context: Context): String {
            return context.getString(id, *args.toTypedArray())
        }
    }
}

@Composable
fun TextResource.getText(): String = getText(LocalContext.current)
