package com.starbook.core.remoteconfig.api

interface FmcTokenProvider {

  suspend fun token(): String?
}

