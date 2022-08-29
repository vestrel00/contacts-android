package contacts.permissions

import com.gun0912.tedpermission.coroutine.TedPermission

// FIXME Refactor to take in one or more permissions? Use case is not that wide so this is not that important.
internal suspend fun requestPermission(
    permission: String,
    // [ANDROID X] @StringRes (not using annotation to avoid dependency on androidx.annotation)
    permissionDeniedTitleRes: Int,
    permissionDeniedDescriptionRes: Int
): Boolean = TedPermission.create()
    .setPermissions(permission)
    .setDeniedTitle(permissionDeniedTitleRes)
    .setDeniedMessage(permissionDeniedDescriptionRes)
    .check()
    .isGranted