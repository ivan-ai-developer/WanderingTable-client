plugins {
    alias(libs.plugins.wanderingtable.android.library)
    alias(libs.plugins.wanderingtable.compose)
}

android {
    namespace = "ru.gohasoft.wanderingtable.core.presentation"
}

dependencies {
    implementation(project(":core:domain"))
}