import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class LibraryPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    target.pluginManager.run {
      apply("com.starbook.ktlint")
      apply("com.android.library")
      withPlugin("com.android.library") {
        target.baseSetup()
        target.tasks.withType(Test::class.java).configureEach {
          // We want all modules to be configured for tests, for the
          // starbookUnitTest to work
          failOnNoDiscoveredTests.set(false)
        }
        target.tasks.register("starbookUnitTest") {
          dependsOn("testDebugUnitTest")
        }
      }
    }
  }
}
