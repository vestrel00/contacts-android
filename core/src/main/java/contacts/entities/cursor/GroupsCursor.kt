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
internal class GroupsCursor(cursor: Cursor) : AbstractCursor<GroupsField>(cursor) {

    val id: Long?
        get() = getLong(GroupsFields.Id)

    val title: String
        get() = getString(GroupsFields.Title) ?: "Null"

    val readOnly: Boolean
        get() = getBoolean(GroupsFields.ReadOnly) ?: false

    val favorites: Boolean
        get() = getBoolean(GroupsFields.Favorites) ?: false

    val autoAdd: Boolean
        get() = getBoolean(GroupsFields.AutoAdd) ?: false

    val account: Account
        get() {
            // There should never be null account name or type...
            val accountName = getString(GroupsFields.AccountName) ?: "null"
            val accountType = getString(GroupsFields.AccountType) ?: "null"

            return Account(accountName, accountType)
        }
}
