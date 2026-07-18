import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class DomainModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")

            extensions.getByType<KotlinJvmProjectExtension>().jvmToolchain(11)

            dependencies.add("implementation", versionCatalog.findLibrary("kotlinx-coroutines-core").get())
        }
    }
}
