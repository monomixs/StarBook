plugins {
  id("com.starbook.library")
  alias(libs.plugins.metro)
}

dependencies {
  implementation(projects.core.remoteconfig.api)
}
