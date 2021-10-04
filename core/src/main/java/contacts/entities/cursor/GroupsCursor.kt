package contacts.entities.cursor

import android.accounts.Account
import android.database.Cursor
import contacts.GroupsField
import contacts.GroupsFields

/**
 * Retrieves [GroupsFields] data from the given [cursor].
 *
 * This does not modify the [cursor] position. Moving the cursor may result in different attribute
 * values.
 */
internal class GroupsCursor(cursor: Cursor) : AbstractEntityCursor<GroupsField>(cursor) {

    val id: Long? by long(GroupsFields.Id)

    val systemId: String? by string(GroupsFields.SystemId)

    val title: String by nonNullString(GroupsFields.Title, "Null")

    val readOnly: Boolean by nonNullBoolean(GroupsFields.ReadOnly)

    val favorites: Boolean by nonNullBoolean(GroupsFields.Favorites)

    val autoAdd: Boolean by nonNullBoolean(GroupsFields.AutoAdd)

    val account: Account
        get() {
            // There should never be null account name or type...
            val accountName = getNonNullString(GroupsFields.AccountName, "null")
            val accountType = getNonNullString(GroupsFields.AccountType, "null")

            return Account(accountName, accountType)
        }
}
