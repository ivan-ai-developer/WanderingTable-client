import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal const val COMPILE_SDK = 36
internal const val MIN_SDK = 28
internal const val TARGET_SDK = 36

internal fun Project.configureKotlinAndroid(
    extension: CommonExtension,
) {
    // Built-in Kotlin is disabled project-wide (see gradle.properties), so the
    // classic KGP android plugin is applied to keep compiler plugins (parcelize,
    // serialization) working.
    pluginManager.apply("org.jetbrains.kotlin.android")

    extension.apply {
        compileSdk = COMPILE_SDK
        compileSdkMinor = 1

        defaultConfig.minSdk = MIN_SDK

        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
}
