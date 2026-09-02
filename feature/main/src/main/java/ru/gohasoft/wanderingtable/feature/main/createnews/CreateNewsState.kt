package ru.gohasoft.wanderingtable.feature.main.createnews

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

internal data class CreateNewsState(
    val title: String = "",
    val content: String = "",
    val isSubmitting: Boolean = false,
    val formError: TextResource? = null,
) {
    val canSubmit: Boolean get() = title.isNotBlank() && !isSubmitting
}
