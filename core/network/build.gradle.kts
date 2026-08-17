plugins {
    alias(libs.plugins.wanderingtable.android.library)
    alias(libs.plugins.wanderingtable.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.gohasoft.wanderingtable.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val baseUrl = providers.gradleProperty("wt.baseUrl").get()
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }
}

dependencies {
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)
    api(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.assertk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
