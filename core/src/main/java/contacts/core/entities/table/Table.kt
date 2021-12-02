package contacts.core.entities.table

import android.net.Uri
import android.provider.ContactsContract
import contacts.core.*

/**
 * Defines all of the main [ContactsContract] tables, types with [Field]s =)
 */
internal sealed interface Table<out T : Field> {

    val uri: Uri

    /**
     * See [ContactsContract.Contacts].
     */
    object Contacts : Table<ContactsField> {
        override val uri: Uri = ContactsContract.Contacts.CONTENT_URI
    }

    /**
     * See [ContactsContract.RawContacts].
     */
    object RawContacts : Table<RawContactsField> {
        override val uri: Uri = ContactsContract.RawContacts.CONTENT_URI
    }

    /**
     * See [ContactsContract.Data].
     */
    object Data : Table<AbstractDataField> {
        override val uri: Uri = ContactsContract.Data.CONTENT_URI
    }

    /**
     * See [ContactsContract.Groups].
     */
    object Groups : Table<GroupsField> {
        override val uri: Uri = ContactsContract.Groups.CONTENT_URI
    }

    /**
     * See [ContactsContract.AggregationExceptions].
     */
    object AggregationExceptions : Table<AggregationExceptionsField> {
        override val uri: Uri = ContactsContract.AggregationExceptions.CONTENT_URI
    }
}