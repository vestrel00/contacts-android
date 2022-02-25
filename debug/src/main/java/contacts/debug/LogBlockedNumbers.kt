package contacts.debug

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.Telephony
import android.telecom.TelecomManager

fun Context.logBlockedNumbersTable() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        log("#### Blocked numbers table - not available prior to Android 7.0 (N / API 24)")
    } else if (!canReadWriteBlockedNumbers()) {
        log("#### Blocked numbers table - only available for system, default dialer, or default SMS app")
    } else {
        log("#### Blocked numbers table")

        try {
            val cursor: Cursor? = contentResolver.query(
                BlockedNumberContract.BlockedNumbers.CONTENT_URI, arrayOf(
                    BlockedNumberContract.BlockedNumbers.COLUMN_ID,
                    BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                    BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER
                ), null, null, null
            )

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val originalNumber = cursor.getString(1)
                    val e164Number = cursor.getString(2)

                    log("Blocked number id: $id, originalNumber: $originalNumber, e164Number: $e164Number")
                }
                cursor.close()
            }
        } catch (se: SecurityException) {
            log("#### Blocked numbers table - error")
            log(se.toString())
        }
    }
}

// [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
private fun Context.canReadWriteBlockedNumbers(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        return false
    }

    val defaultDialerPackage =
        (getSystemService(Context.TELECOM_SERVICE) as TelecomManager).defaultDialerPackage
    val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)

    val canCurrentUserBlockNumbers = BlockedNumberContract.canCurrentUserBlockNumbers(this)
    val isDefaultDialer = packageName == defaultDialerPackage
    val isDefaultSms = packageName == defaultSmsPackage

    // A check that is omitted here is if this is a system app. We are just assuming that it is not.
    return canCurrentUserBlockNumbers && (isDefaultDialer || isDefaultSms)
}