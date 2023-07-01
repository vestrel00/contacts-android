package contacts.ui.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.widget.Toast
import contacts.ui.R

/**
 * Starts an activity to pick a ringtone.
 *
 * The [currentRingtoneUri] determines the initially selected ringtone in the picker.
 *
 * This (sub)activity is the same one used in the AOSP Contacts app and typically looks and
 * behaves like an alert dialog that floats on top of the current activity with a translucent dimmed
 * overlay.
 *
 * This is used in conjunction with [onRingtoneSelected] to process the results.
 *
 * ## Manifest
 *
 * Starting with Android 11 (API 30), you must include the following to your manifest in order to
 * successfully use this function.
 *
 * ```
 * <queries>
 *     <intent>
 *         <action android:name="android.intent.action.RINGTONE_PICKER" />
 *     </intent>
 * </queries>
 * ```
 */
@JvmOverloads
fun Activity.selectRingtone(currentRingtoneUri: Uri? = null) {
    val selectRingtoneIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentRingtoneUri)
    }

    @SuppressLint("QueryPermissionsNeeded")
    val component = selectRingtoneIntent.resolveActivity(packageManager)
    if (component != null) {
        startActivityForResult(selectRingtoneIntent, REQUEST_SELECT_RINGTONE)
    } else {
        Toast.makeText(this, R.string.contacts_ui_ringtone_select_error, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Call this in [Activity.onActivityResult] to get the selected ringtone uri after the ringtone
 * select activity has finished. This is used in conjunction with [selectRingtone].
 *
 * The given [ringtoneSelected] function will be invoked if the user selected a ringtone. Otherwise,
 * this will do nothing.
 */
fun onRingtoneSelected(
    requestCode: Int, resultCode: Int, intent: Intent?,
    ringtoneSelected: (ringtoneUri: Uri?) -> Unit
) {
    if (requestCode == REQUEST_SELECT_RINGTONE && resultCode == Activity.RESULT_OK) {
        val ringtoneUri = intent?.getParcelableExtraCompat<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        ringtoneSelected(ringtoneUri)
    }
}

private const val REQUEST_SELECT_RINGTONE = 9100

private inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("Deprecation")
        getParcelableExtra(name)
    }