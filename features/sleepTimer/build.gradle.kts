plugins {
  id("com.starbook.library")
  id("com.starbook.compose")
}

dependencies {
  implementation(projects.core.strings)
  implementation(projects.core.ui)

  testImplementation(libs.junit)
  testImplementation(libs.androidX.test.core)
  testImplementation(libs.androidX.test.junit)
  testImplementation(libs.androidX.test.runner)
  testImplementation(libs.robolectric)
}
