import org.gradle.api.Plugin
import org.gradle.api.Project

class AppPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    target.pluginManager.run {
      apply("com.starbook.ktlint")
      apply("com.android.application")
      withPlugin("com.android.application") {
        target.baseSetup()
        target.tasks.register("starbookUnitTest") {
          dependsOn("testDebugUnitTest")
        }
      }
    }
  }
}
