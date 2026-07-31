package com.starbook.core.data.folders

import android.net.Uri

public interface PersistedUriPermissions {
  public fun persistedUris(): Set<Uri>
}

