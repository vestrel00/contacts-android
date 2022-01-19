package contacts.ui.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import contacts.ui.R

/**
 * Launches an external activity to pick a ringtone.
 *
 * The [currentRingtoneUri] determines the initially selected ringtone in the picker.
 *
 * This (sub)activity is the same one used in the native Contacts app and typically looks and
 * behaves like an alert dialog that floats on top of the current activity with a translucent dimmed
 * overlay.
 *
 * This is used in conjunction with [onRingtoneSelected] to process the results.
 *
 * #### Manifest
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
 */
fun onRingtoneSelected(
    requestCode: Int, resultCode: Int, intent: Intent?,
    ringtoneSelected: (ringtoneUri: Uri?) -> Unit
) {
    if (requestCode == REQUEST_SELECT_RINGTONE && resultCode == Activity.RESULT_OK) {
        val ringtoneUri = intent?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        ringtoneSelected(ringtoneUri)
    }
}

private const val REQUEST_SELECT_RINGTONE = 9100