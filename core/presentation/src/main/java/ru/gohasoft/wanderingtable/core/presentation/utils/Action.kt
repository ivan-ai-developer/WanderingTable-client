package ru.gohasoft.wanderingtable.core.presentation.utils

import android.app.Activity

abstract class Action<T> {
    abstract operator fun invoke(
        activity: Activity,
        marker: T
    )
}