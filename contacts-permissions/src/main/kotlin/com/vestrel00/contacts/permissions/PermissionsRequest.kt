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
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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