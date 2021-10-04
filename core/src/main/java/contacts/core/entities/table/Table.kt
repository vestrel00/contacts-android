package contacts.core.entities.table

import android.net.Uri
import android.provider.ContactsContract
import contacts.core.*

/**
 * Defines all of the main [ContactsContract] tables, types with [Field]s =)
 */
internal sealed class Table<out T : Field>(val uri: Uri) {

    /**
     * See [ContactsContract.Contacts].
     */
    object Contacts : Table<ContactsField>(ContactsContract.Contacts.CONTENT_URI)

    /**
     * See [ContactsContract.RawContacts].
     */
    object RawContacts : Table<RawContactsField>(ContactsContract.RawContacts.CONTENT_URI)

    /**
     * See [ContactsContract.Data].
     */
    object Data : Table<AbstractDataField>(ContactsContract.Data.CONTENT_URI)

    /**
     * See [ContactsContract.Groups].
     */
    object Groups : Table<GroupsField>(ContactsContract.Groups.CONTENT_URI)

    /**
     * See [ContactsContract.AggregationExceptions].
     */
    object AggregationExceptions :
        Table<AggregationExceptionsField>(ContactsContract.AggregationExceptions.CONTENT_URI)
}