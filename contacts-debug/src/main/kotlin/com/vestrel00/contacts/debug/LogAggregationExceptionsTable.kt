package com.vestrel00.contacts.debug

import android.content.Context
import android.provider.ContactsContract

fun Context.logAggregationExceptions() {
    if (!hasReadPermission()) {
        log("#### Aggregation exceptions table - read contacts permission not granted")
        return
    }

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

    log("#### Aggregation exceptions table")
    cursor.moveToPosition(-1)
    while (cursor.moveToNext()) {
        val id = cursor.getLong(0)
        val type = cursor.getString(1)
        val rawContactId1 = cursor.getLong(2)
        val rawContactId2 = cursor.getLong(3)

        log(
            """
                Aggregation exception id: $id, type: $type,
                 rawContactId1: $rawContactId1, rawContactId2: $rawContactId2
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}