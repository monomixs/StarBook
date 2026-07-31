plugins {
  id("com.starbook.library")
  alias(libs.plugins.metro)
}

dependencies {
  api(projects.core.analytics.api)
}
