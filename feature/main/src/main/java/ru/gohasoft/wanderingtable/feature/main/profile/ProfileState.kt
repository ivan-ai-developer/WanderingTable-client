package ru.gohasoft.wanderingtable.feature.main.profile

import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.feature.main.model.ProfileUi

internal data class ProfileState(
    val isLoading: Boolean = true,
    val profile: ProfileUi? = null,
    val isLoggingOut: Boolean = false,
    val error: TextResource? = null,
)
