package contacts.debug

import android.util.Log

private const val TAG = "ContactsDebug"

internal fun log(message: String) {
    Log.d(TAG, message)
}