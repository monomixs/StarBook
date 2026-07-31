plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.androidPluginForGradle)
  implementation(libs.kotlin.pluginForGradle)
  implementation(libs.kotlin.powerAssert)
  implementation(libs.compose.compiler.gradle.plugin)
  implementation(libs.ktlint.gradlePlugin)
}

gradlePlugin {
  plugins {
    create("library") {
      id = "com.starbook.library"
      implementationClass = "LibraryPlugin"
    }
    create("app") {
      id = "com.starbook.app"
      implementationClass = "AppPlugin"
    }
    create("compose") {
      id = "com.starbook.compose"
      implementationClass = "ComposePlugin"
    }
    create("ktlint") {
      id = "com.starbook.ktlint"
      implementationClass = "KtlintPlugin"
    }
  }
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.toolchain.get().toInt()))
  }
}
