package contacts.debug

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
            ContactsContract.Groups.SOURCE_ID,
            ContactsContract.Groups.SYSTEM_ID,
            ContactsContract.Groups.TITLE,
            ContactsContract.Groups.GROUP_IS_READ_ONLY,
            ContactsContract.Groups.FAVORITES,
            ContactsContract.Groups.AUTO_ADD,
            ContactsContract.Groups.SHOULD_SYNC,
            ContactsContract.Groups.ACCOUNT_NAME,
            ContactsContract.Groups.ACCOUNT_TYPE,
            ContactsContract.Groups.DELETED
        ),
        null,
        null,
        null
    )

    cursor ?: return

    while (cursor.moveToNext()) {
        // Use getString instead of getLong, getInt, etc so that the value could be null.
        val id = cursor.getString(0)
        val sourceId = cursor.getString(1)
        val systemId = cursor.getString(2)
        val title = cursor.getString(3)
        val isReadOnly = cursor.getString(4)
        val favorites = cursor.getString(5)
        val autoAdd = cursor.getString(6)
        val shouldSync = cursor.getString(7)
        val accountName = cursor.getString(8)
        val accountType = cursor.getString(9)
        val deleted = cursor.getString(10)

        log(
            """
                Group id: $id, sourceId: $sourceId, systemId: $systemId, title: $title,
                 isReadOnly: $isReadOnly, favorites: $favorites, autoAdd: $autoAdd,
                 shouldSync: $shouldSync, accountName: $accountName, accountType: $accountType,
                 deleted: $deleted
            """.trimIndent().replace("\n", "")
        )
    }

    cursor.close()
}