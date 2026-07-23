plugins {
    alias(libs.plugins.wanderingtable.android.library)
    alias(libs.plugins.wanderingtable.compose)
    alias(libs.plugins.wanderingtable.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "ru.gohasoft.wanderingtable.core.presentation"
}

dependencies {
    api(project(":core:domain"))
    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.lifecycle.viewmodel.navigation3)
    api(libs.androidx.hilt.navigation.compose)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.kotlinx.serialization.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
