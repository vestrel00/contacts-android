package contacts.debug

import android.content.Context
import android.database.Cursor
import android.net.Uri

fun Context.logSimContactsTable() {
    if (!hasReadPermission()) {
        log("#### SIM Contacts table - read contacts permission not granted")
        return
    }

    log("#### SIM Contacts table")

    val cursor: Cursor? = contentResolver.query(
        Uri.parse("content://icc/adn"), null, null, null, null
    )

    if (cursor != null) {
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("_id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val number = cursor.getString(cursor.getColumnIndexOrThrow("number"))

            log("SIM Contact id: $id, name: $name, number: $number")
        }
        cursor.close()
    }
}