package com.vestrel00.contacts.debug

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process

// Intentionally not reusing the ContactsPermission to avoid a dependency on the contacts module.
internal fun Context.hasReadPermission(): Boolean = hasPermission(Manifest.permission.READ_CONTACTS)

private fun Context.hasPermission(permission: String) = checkPermission(
    permission, Process.myPid(), Process.myUid()
) == PackageManager.PERMISSION_GRANTED