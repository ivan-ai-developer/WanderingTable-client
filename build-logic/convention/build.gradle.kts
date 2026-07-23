plugins {
    `kotlin-dsl`
}

group = "ru.gohasoft.wanderingtable.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "wanderingtable.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "wanderingtable.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("compose") {
            id = "wanderingtable.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("domainModule") {
            id = "wanderingtable.domain.module"
            implementationClass = "DomainModuleConventionPlugin"
        }
        register("hilt") {
            id = "wanderingtable.hilt"
            implementationClass = "HiltConventionPlugin"
        }
    }
}
