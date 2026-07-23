package ru.gohasoft.wanderingtable.core.presentation.utils

import ru.gohasoft.wanderingtable.core.domain.exception.AppException
import ru.gohasoft.wanderingtable.core.domain.exception.LocalException
import ru.gohasoft.wanderingtable.core.domain.exception.NetworkException
import ru.gohasoft.wanderingtable.core.presentation.R
import ru.gohasoft.wanderingtable.core.presentation.utils.resource.TextResource

val AppException.infoScreenConfig: InfoScreenConfig
    get() = when (this) {
        is NetworkException.NoInternet -> InfoScreenConfig {
            title(TextResource.StringResource(R.string.error_no_internet_title))
            description(TextResource.StringResource(R.string.error_no_internet_description))
        }

        is NetworkException.Unauthorized -> InfoScreenConfig {
            title(TextResource.StringResource(R.string.error_unauthorized_title))
            description(TextResource.StringResource(R.string.error_unauthorized_description))
        }

        is NetworkException -> InfoScreenConfig {
            title(TextResource.StringResource(R.string.error_server_title))
            description(TextResource.StringResource(R.string.error_server_description))
        }

        is LocalException.DiskFull -> InfoScreenConfig {
            title(TextResource.StringResource(R.string.error_disk_full_title))
            description(TextResource.StringResource(R.string.error_disk_full_description))
        }

        else -> InfoScreenConfig {
            title(TextResource.StringResource(R.string.error_unknown_title))
            description(TextResource.StringResource(R.string.error_unknown_description))
        }
    }

val AppException.snackbarConfig: SnackbarScreenConfig
    get() = SnackbarScreenConfig {
        message(
            when (this@snackbarConfig) {
                is NetworkException.NoInternet ->
                    TextResource.StringResource(R.string.error_no_internet_title)

                is NetworkException.Unauthorized ->
                    TextResource.StringResource(R.string.error_unauthorized_title)

                is NetworkException ->
                    TextResource.StringResource(R.string.error_server_title)

                is LocalException.DiskFull ->
                    TextResource.StringResource(R.string.error_disk_full_title)

                else ->
                    TextResource.StringResource(R.string.error_unknown_title)
            }
        )
    }
