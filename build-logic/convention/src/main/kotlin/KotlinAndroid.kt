import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal const val COMPILE_SDK = 36
internal const val MIN_SDK = 28
internal const val TARGET_SDK = 36

internal fun Project.configureKotlinAndroid(
    extension: CommonExtension,
) {
    extension.apply {
        compileSdk = COMPILE_SDK
        compileSdkMinor = 1

        defaultConfig.minSdk = MIN_SDK

        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
}
