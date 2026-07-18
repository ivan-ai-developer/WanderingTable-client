import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.getByType<CommonExtension>().apply {
                buildFeatures.compose = true
            }

            val bom = versionCatalog.findLibrary("androidx-compose-bom").get()
            dependencies.apply {
                add("implementation", platform(bom))
                add("implementation", versionCatalog.findLibrary("androidx-compose-ui").get())
                add("implementation", versionCatalog.findLibrary("androidx-compose-ui-graphics").get())
                add("implementation", versionCatalog.findLibrary("androidx-compose-ui-tooling-preview").get())
                add("implementation", versionCatalog.findLibrary("androidx-compose-foundation").get())
                add("implementation", versionCatalog.findLibrary("androidx-compose-material3").get())
                add("implementation", versionCatalog.findLibrary("androidx-compose-material-icons-core").get())
                add("debugImplementation", versionCatalog.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}
