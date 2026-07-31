plugins {
  alias(libs.plugins.compose.compiler) apply false
  id("com.starbook.ktlint")
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}
