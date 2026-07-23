package ru.gohasoft.wanderingtable.welcome

import ru.gohasoft.wanderingtable.R
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource.StringResource

data class WelcomeState(
    val title: TextResource = StringResource(R.string.welcome_title),
    val subtitle: TextResource = StringResource(R.string.welcome_subtitle),
    val buttonText: TextResource = StringResource(R.string.welcome_show_snackbar),
)