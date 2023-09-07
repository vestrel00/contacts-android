package contacts.ui.util

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager

/**
 * Returns true if this application is the default dialer app. Otherwise, returns false.
 *
 * This will always return false for versions prior to M (API 23).
 */
// [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
fun Context.isDefaultDialerApp(): Boolean = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
    false
} else {
    // Requires API 21+
    val telecomManager = getSystemService(Activity.TELECOM_SERVICE) as TelecomManager
    // Requires API 23+
    packageName == telecomManager.defaultDialerPackage
}

/**
 * If this is not the default dialer / phone app, then this will call
 * [requestToBeTheDefaultDialerApp]. Otherwise, this will invoke the given [onBeingDefaultDialerApp]
 * function.
 */
// [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun Activity.requestToBeTheDefaultDialerAppIfNeeded(onBeingDefaultDialerApp: () -> Unit = {}) {
    if (isDefaultDialerApp()) {
        onBeingDefaultDialerApp()
    } else {
        requestToBeTheDefaultDialerApp()
    }
}

/**
 * Starts an activity to prompt the user to set this app as the default dialer / phone app.
 *
 * This (sub)activity is the same one used in the AOSP Contacts app and typically looks and
 * behaves like an alert dialog that floats on top of the current activity with a translucent dimmed
 * overlay.
 *
 * This is used in conjunction with [onRequestToBeDefaultDialerAppResult] to process the results.
 *
 * If this is already the default dialer app, this will do nothing.
 *
 * ## Manifest
 *
 * As per https://developer.android.com/reference/androidx/core/role/RoleManagerCompat#ROLE_DIALER(),
 * your app must have an activity with following intent filters in your manifest. Otherwise, this
 * will do nothing.
 *
 * ```
 * <activity>
 *      <intent-filter>
 *          <action android:name="android.intent.action.DIAL" />
 *          <category android:name="android.intent.category.DEFAULT"/>
 *      </intent-filter>
 *      <intent-filter>
 *          <action android:name="android.intent.action.DIAL" />
 *          <category android:name="android.intent.category.DEFAULT"/>
 *          <data android:scheme="tel" />
 *      </intent-filter>
 * </activity>
 * ```
 *
 * The above intent filters does NOT need to be added to the activity where this function is called.
 * It can be placed in any activity within the application.
 *
 * Additionally, starting with API 33 (Tiramisu), to qualify for this role, an application needs to
 * handle the intent to dial, and implement an [android.telecom.InCallService]...
 *
 * ```kotlin
 * @TargetApi(Build.VERSION_CODES.M)
 * class SampleInCallServiceForDialerRoleRequest: InCallService()
 * ```
 *
 * ```
 * <service
 *      android:name=".SampleInCallServiceForDialerRoleRequest"
 *      android:exported="true"
 *      android:permission="android.permission.BIND_INCALL_SERVICE">
 *      <meta-data
 *          android:name="android.telecom.IN_CALL_SERVICE_UI"
 *          android:value="true" />
 *      <meta-data
 *          android:name="android.telecom.IN_CALL_SERVICE_CAR_MODE_UI"
 *          android:value="false" />
 *
 *      <intent-filter>
 *          <action android:name="android.telecom.InCallService" />
 *      </intent-filter>
 * </service>
```
 */
// [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
fun Activity.requestToBeTheDefaultDialerApp() {
    if (isDefaultDialerApp()) {
        // Even if we don't have this check, nothing will happen by executing the following code
        // if this is already the default dialer app.
        return
    }

    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
    ) {
        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
        startActivityForResult(intent, REQUEST_CODE_SET_AS_DEFAULT_DIALER)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = getSystemService(Activity.ROLE_SERVICE) as RoleManager
        startActivityForResult(
            roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER),
            REQUEST_CODE_SET_AS_DEFAULT_DIALER
        )
    }
    // else do nothing. We should really instead use @RequiresApi lol
}

/**
 * Call this in [Activity.onActivityResult] to process the result of the request to be the default
 * dialer. This is used in conjunction with [requestToBeTheDefaultDialerAppIfNeeded] or
 * [requestToBeTheDefaultDialerApp].
 *
 * The given [onBeingDefaultDialerApp] function will be invoked if the user agreed to make this
 * the default dialer / phone app. Otherwise, this will do nothing.
 */
fun onRequestToBeDefaultDialerAppResult(
    requestCode: Int, resultCode: Int, onBeingDefaultDialerApp: () -> Unit
) {
    if (requestCode == REQUEST_CODE_SET_AS_DEFAULT_DIALER && resultCode == Activity.RESULT_OK) {
        onBeingDefaultDialerApp()
    }
}

private const val REQUEST_CODE_SET_AS_DEFAULT_DIALER = 63635