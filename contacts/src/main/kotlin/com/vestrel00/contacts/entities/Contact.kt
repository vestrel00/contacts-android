package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

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
    val rawContacts: List<RawContact>

) : Entity, Parcelable {

    fun toMutableContact() = MutableContact(
        id = id,

        rawContacts = rawContacts.map { it.toMutableRawContact() }
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
    val rawContacts: List<MutableRawContact>

) : Entity, Parcelable
