plugins {
    alias(libs.plugins.wanderingtable.domain.module)
}


dependencies {
    implementation(project(":core:domain"))
}