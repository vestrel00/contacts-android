package contacts.core.entities.table

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import contacts.core.*
import contacts.core.util.forSyncAdapter

/**
 * Defines all of the database tables used by this library.
 */
internal sealed class Table<out T : Field>(protected val uri: Uri) {

    sealed class ContactsContractTable<out T : Field>(uri: Uri) : Table<T>(uri) {
        fun uri(callerIsSyncAdapter: Boolean): Uri = uri.forSyncAdapter(callerIsSyncAdapter)
    }

    // Expose the protected uri. We could just remove the protected modifier from uri but making
    // the refactors for https://github.com/vestrel00/contacts-android/issues/308 would have been
    // more difficult do without missing anything.
    fun uri(): Uri = uri

    /**
     * See [ContactsContract.Data].
     */
    object Data : ContactsContractTable<AbstractDataField>(ContactsContract.Data.CONTENT_URI)

    /**
     * See [ContactsContract.RawContacts].
     */
    object RawContacts : ContactsContractTable<RawContactsField>(
        ContactsContract.RawContacts.CONTENT_URI
    )

    /**
     * See [ContactsContract.Contacts].
     */
    object Contacts : ContactsContractTable<ContactsField>(ContactsContract.Contacts.CONTENT_URI)

    /**
     * See [ContactsContract.Groups].
     */
    object Groups : ContactsContractTable<GroupsField>(ContactsContract.Groups.CONTENT_URI)

    /**
     * See [ContactsContract.AggregationExceptions].
     */
    object AggregationExceptions : ContactsContractTable<AggregationExceptionsField>(
        ContactsContract.AggregationExceptions.CONTENT_URI
    )

    /**
     * See [BlockedNumberContract.BlockedNumbers].
     */
    // [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
    @TargetApi(Build.VERSION_CODES.N)
    object BlockedNumbers :
        Table<BlockedNumbersField>(BlockedNumberContract.BlockedNumbers.CONTENT_URI)

    object SimContacts : Table<SimContactsField>(Uri.parse("content://icc/adn"))
}