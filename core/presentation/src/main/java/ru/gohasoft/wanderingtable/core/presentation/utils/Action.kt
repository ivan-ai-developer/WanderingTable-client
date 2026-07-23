package ru.gohasoft.wanderingtable.core.presentation.utils

import android.app.Activity
import android.os.Parcelable

abstract class Action<T> : Parcelable {
    abstract operator fun invoke(
        activity: Activity,
        marker: T
    )
}