package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Contains contact data and [rawContacts] that are associated with this contact.
 *
 * ## [RawContact]
 *
 * A Contact may consist of one or more [RawContact]. A [RawContact] is an association between a
 * Contact and an [android.accounts.Account]. Each [RawContact] is associated with several pieces of
 * Data such as emails.
 *
 * The Contacts Provider may combine [RawContact] from several different Accounts. The same effect
 * is achieved when merging / linking multiple contacts. Instances of this class also provides
 * aggregate data from all [RawContact]s in the set of [rawContacts].
 */
@Parcelize
data class Contact internal constructor(

    /**
     * The unique ID of this [Contact].
     *
     * This is the value of Contacts._ID / RawContacts.CONTACT_ID / Data.CONTACT_ID
     */
    override val id: Long,

    /**
     * A list of [RawContact]s that are associated with this contact.
     *
     * Note that this list may not include all raw contacts that are actually associated with this
     * contact depending on query filters.
     */
    val rawContacts: List<RawContact>,

    /**
     * The standard text shown as the contact's display name, based on the best available
     * information for the contact (for example, it might be the email address if the name is not
     * available). This may be null if the Contacts Provider cannot find a suitable display name
     * source to use.
     *
     * This is a read-only attribute as the Contacts Provider automatically sets this value.
     * This is ignored for insert, update, and delete functions.
     */
    val displayName: String?,

    // FIXME Add `ContactsColumns.NAME_RAW_CONTACT_ID` when minSdkVersion is at least 21.

    /**
     * Timestamp of when this contact was last updated. This includes updates to all data associated
     * with this contact including raw contacts. Any modification (including deletes and inserts) of
     * underlying contact data are also reflected in this timestamp.
     *
     * This is a read-only attribute as the Contacts Provider automatically sets this value.
     * This is ignored for insert, update, and delete functions.
     */
    val lastUpdatedTimestamp: Date?,

    /**
     * Contains options for this contact and all of the [RawContact]s associated with it (not
     * limited to the [rawContacts] in this instance).
     *
     * Changes to the options of a RawContact may affect the options of the parent Contact. On the
     * other hand, changes to the options of the parent Contact will be propagated to all child
     * RawContact options.
     *
     * Use the ContactOptions extension functions to modify options.
     */
    val options: Options?

) : Entity, Parcelable {

    fun toMutableContact() = MutableContact(
        id = id,

        rawContacts = rawContacts.map { it.toMutableRawContact() },

        displayName = displayName,
        lastUpdatedTimestamp = lastUpdatedTimestamp,
        options = options
    )
}

/**
 * A mutable [Contact].
 */
@Parcelize
data class MutableContact internal constructor(

    /**
     * See [Contact.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * Contains a list of **mutable** raw contacts though the list containing them is immutable.
     *
     * See [Contact.rawContacts].
     */
    val rawContacts: List<MutableRawContact>,

    /**
     * See [Contact.displayName].
     */
    val displayName: String?,

    /**
     * See [Contact.lastUpdatedTimestamp].
     */
    val lastUpdatedTimestamp: Date?,

    /**
     * See [Contact.options].
     */
    val options: Options?

) : Entity, Parcelable