package contacts.debug

import android.content.Context
import android.provider.ContactsContract

fun Context.logAggregationExceptions() {
    if (!hasReadPermission()) {
        log("#### Aggregation exceptions table - read contacts permission not granted")
        return
    }

    log("#### Aggregation exceptions table")

    val cursor = contentResolver.query(
        ContactsContract.AggregationExceptions.CONTENT_URI,
        arrayOf(
            ContactsContract.AggregationExceptions._ID,
            ContactsContract.AggregationExceptions.TYPE,
            ContactsContract.AggregationExceptions.RAW_CONTACT_ID1,
            ContactsContract.AggregationExceptions.RAW_CONTACT_ID2
        ),
        null,
        null,
        null
    )

    cursor ?: return

    while (cursor.moveToNext()) {
        // Use getString instead of getLong, getInt, etc so that the value could be null.
        val id = cursor.getString(0)
        val type = cursor.getString(1)
        val rawContactId1 = cursor.getString(2)
        val rawContactId2 = cursor.getString(3)

        log(
            """
                Aggregation exception id: $id, type: $type,
                 rawContactId1: $rawContactId1, rawContactId2: $rawContactId2
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}