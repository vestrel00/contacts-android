package contacts.debug

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

fun Context.logDataTable() {
    if (!hasReadPermission()) {
        log("#### Data table - read contacts permission not granted")
        return
    }

    log("#### Data table")

    logDataTable(ContactsContract.Data.CONTENT_URI)
}

internal fun Context.logDataTable(contentUri: Uri) {
    val cursor = contentResolver.query(
        contentUri,
        arrayOf(
            ContactsContract.Data._ID,
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.IS_PRIMARY,
            ContactsContract.Data.IS_SUPER_PRIMARY,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.DATA2,
            ContactsContract.Data.DATA3,
            ContactsContract.Data.DATA4,
            ContactsContract.Data.DATA5,
            ContactsContract.Data.DATA6,
            ContactsContract.Data.DATA7,
            ContactsContract.Data.DATA8,
            ContactsContract.Data.DATA9,
            ContactsContract.Data.DATA10,
            ContactsContract.Data.DATA11,
            ContactsContract.Data.DATA12,
            ContactsContract.Data.DATA13,
            ContactsContract.Data.DATA14
        ),
        null,
        null,
        null
    )

    cursor ?: return

    while (cursor.moveToNext()) {
        // Use getString instead of getLong, getInt, etc so that the value could be null.
        val id = cursor.getString(0)
        val rawContactId = cursor.getString(1)
        val contactId = cursor.getString(2)
        val mimeType = cursor.getString(3)
        val isPrimary = cursor.getString(4)
        val isSuperPrimary = cursor.getString(5)

        val data1 = cursor.getString(6)
        val data2 = cursor.getString(7)
        val data3 = cursor.getString(8)
        val data4 = cursor.getString(9)
        val data5 = cursor.getString(10)
        val data6 = cursor.getString(11)
        val data7 = cursor.getString(12)
        val data8 = cursor.getString(13)
        val data9 = cursor.getString(14)
        val data10 = cursor.getString(15)
        val data11 = cursor.getString(16)
        val data12 = cursor.getString(17)
        val data13 = cursor.getString(18)
        val data14 = cursor.getString(19)

        log(
            """
                Data id: $id, rawContactId: $rawContactId, contactId: $contactId,
                 mimeType: $mimeType, isPrimary: $isPrimary, isSuperPrimary: $isSuperPrimary,
                 data1: $data1, data2: $data2, data3: $data3, data4: $data4, data5: $data5,
                 data6: $data6, data7: $data7, data8: $data8, data9: $data9, data10: $data10,
                 data11: $data11, data12: $data12, data13: $data13, data14: $data14
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}