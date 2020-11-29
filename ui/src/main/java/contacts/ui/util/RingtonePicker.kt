package contacts.ui.util

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import contacts.ui.R

/**
 * Launches an external activity to pick a ringtone.
 *
 * This (sub)activity is the same one used in the native Contacts app and typically looks and
 * behaves like an alert dialog that floats on top of the current activity with a translucent dimmed
 * overlay.
 *
 * This is used in conjunction with [onRingtoneSelected] to process the results.
 */
fun Activity.selectRingtone(currentRingtoneUri: Uri?) {
    val selectRingtoneIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentRingtoneUri)
    }

    val component = selectRingtoneIntent.resolveActivity(packageManager)
    if (component != null) {
        startActivityForResult(selectRingtoneIntent, REQUEST_SELECT_RINGTONE)
    } else {
        Toast.makeText(this, R.string.contact_ringtone_select_error, Toast.LENGTH_SHORT).show()
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