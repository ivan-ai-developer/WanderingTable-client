plugins {
    alias(libs.plugins.wanderingtable.android.application)
    alias(libs.plugins.wanderingtable.compose)
    alias(libs.plugins.wanderingtable.hilt)
    alias(libs.plugins.kotlin.serialization)
    // Brought onto the classpath but not applied: the plugin fails the build outright when
    // google-services.json is missing, and this app must still build and run without push.
    alias(libs.plugins.google.services) apply false
}

// Push is opt-in per checkout: drop your google-services.json into app/ and Firebase wires
// itself up on the next build. Without it the messaging SDK stays inert and
// FirebasePushTokenProvider reports no token, which every caller already handles.
if (project.file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

android {
    namespace = "ru.gohasoft.wanderingtable"

    defaultConfig {
        applicationId = "ru.gohasoft.wanderingtable"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":core:uikit"))
    implementation(project(":core:presentation"))
    implementation(project(":core:domain"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:main"))
    implementation(project(":data:auth"))
    implementation(project(":data:main"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.play.services)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.assertk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<Test> {
    useJUnitPlatform()
}