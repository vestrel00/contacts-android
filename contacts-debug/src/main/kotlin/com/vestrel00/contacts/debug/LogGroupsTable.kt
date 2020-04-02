package com.vestrel00.contacts.debug

import android.content.Context
import android.provider.ContactsContract

fun Context.logGroupsTable() {
    if (!hasReadPermission()) {
        log("#### Groups table - read contacts permission not granted")
        return
    }

    log("#### Groups table")

    val cursor = contentResolver.query(
        ContactsContract.Groups.CONTENT_URI,
        arrayOf(
            ContactsContract.Groups._ID,
            ContactsContract.Groups.SYSTEM_ID,
            ContactsContract.Groups.TITLE,
            ContactsContract.Groups.GROUP_IS_READ_ONLY,
            ContactsContract.Groups.FAVORITES,
            ContactsContract.Groups.AUTO_ADD,
            ContactsContract.Groups.SHOULD_SYNC,
            ContactsContract.Groups.ACCOUNT_NAME,
            ContactsContract.Groups.ACCOUNT_TYPE
        ),
        null,
        null,
        null
    )

    cursor ?: return

    while (cursor.moveToNext()) {
        // Use getString instead of getLong, getInt, etc so that the value could be null.
        val id = cursor.getString(0)
        val systemId = cursor.getString(1)
        val title = cursor.getString(2)
        val readOnly = cursor.getString(3)
        val favorites = cursor.getString(4)
        val autoAdd = cursor.getString(5)
        val shouldSync = cursor.getString(6)
        val accountName = cursor.getString(7)
        val accountType = cursor.getString(8)

        log(
            """
                Group id: $id, systemId: $systemId, title: $title,
                 readOnly: $readOnly, favorites: $favorites, autoAdd: $autoAdd,
                 shouldSync: $shouldSync, accountName: $accountName, accountType: $accountType
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}