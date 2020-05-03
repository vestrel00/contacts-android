package com.vestrel00.contacts.permissions

import android.app.Activity
import android.content.Context
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.permissions.accounts.requestGetAccountsPermission
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Requests the [ContactsPermissions.READ_PERMISSION]. The current coroutine is suspended until the
 * user either grants or denies the permission request.
 *
 * Returns true if permission is granted. False otherwise.
 */
suspend fun requestQueryPermission(activity: Activity): Boolean =
    requestContactsPermission(ContactsPermissions.READ_PERMISSION, activity)

/**
 * Requests the [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION]. The current
 * coroutine is suspended until the user either grants or denies the permissions request.
 *
 * Returns true if permissions are granted. False otherwise.
 */
suspend fun requestInsertUpdateDeletePermission(activity: Activity): Boolean =
    requestContactsPermission(ContactsPermissions.WRITE_PERMISSION, activity)
            && requestGetAccountsPermission(activity)

private suspend fun requestContactsPermission(permission: String, activity: Activity): Boolean =
    requestPermission(
        permission,
        activity,
        R.string.contacts_request_permission_title,
        R.string.contacts_request_permission_description
    )

internal suspend fun requestPermission(
    permission: String,
    activity: Activity,
    // [ANDROID X] @StringRes (not using annotation to avoid dependency on androidx.annotation)
    permissionDeniedTitleRes: Int,
    permissionDeniedDescriptionRes: Int
): Boolean = suspendCoroutine { continuation ->

    Dexter.withActivity(activity)
        .withPermission(permission)
        .withListener(
            CompositePermissionListener(
                CustomDialogOnDeniedPermissionListener(
                    activity,
                    permissionDeniedTitleRes,
                    permissionDeniedDescriptionRes
                ),
                CustomPermissionListener(continuation)
            )
        )
        .check()
}

private class CustomPermissionListener(private val continuation: Continuation<Boolean>) :
    PermissionListener {
    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        continuation.resume(true)
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        continuation.resume(false)
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?
    ) {
        token?.continuePermissionRequest()
    }
}

@Suppress("FunctionName")
private fun CustomDialogOnDeniedPermissionListener(
    context: Context,
    // [ANDROID X] @StringRes (not using annotation to avoid dependency on androidx.annotation)
    permissionDeniedTitleRes: Int,
    permissionDeniedDescriptionRes: Int
) = DialogOnDeniedPermissionListener.Builder
    .withContext(context)
    .withTitle(permissionDeniedTitleRes)
    .withMessage(permissionDeniedDescriptionRes)
    .withButtonText(android.R.string.ok)
    .build()