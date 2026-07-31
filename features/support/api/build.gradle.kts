plugins {
  id("com.starbook.library")
  id("com.starbook.compose")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.strings)
  implementation(projects.core.ui)
  implementation(projects.navigation)

  testImplementation(libs.molecule)
  testImplementation(libs.turbine)
}
