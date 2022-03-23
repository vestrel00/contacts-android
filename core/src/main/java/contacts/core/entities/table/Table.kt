package contacts.core.entities.table

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import contacts.core.*

/**
 * Defines all of the database tables used by this library.
 */
internal sealed interface Table<out T : Field> {

    val uri: Uri

    /**
     * See [ContactsContract.Data].
     */
    object Data : Table<AbstractDataField> {
        override val uri: Uri = ContactsContract.Data.CONTENT_URI
    }

    /**
     * See [ContactsContract.RawContacts].
     */
    object RawContacts : Table<RawContactsField> {
        override val uri: Uri = ContactsContract.RawContacts.CONTENT_URI
    }

    /**
     * See [ContactsContract.Contacts].
     */
    object Contacts : Table<ContactsField> {
        override val uri: Uri = ContactsContract.Contacts.CONTENT_URI
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

    /**
     * See [BlockedNumberContract.BlockedNumbers].
     */
    // [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
    @TargetApi(Build.VERSION_CODES.N)
    object BlockedNumbers : Table<BlockedNumbersField> {
        override val uri: Uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
    }

    object SimContacts : Table<SimContactField> {
        override val uri: Uri = Uri.parse("content://icc/adn")
    }
}