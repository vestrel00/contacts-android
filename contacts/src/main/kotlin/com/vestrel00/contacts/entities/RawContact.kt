package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Contact data specific to an [android.accounts.Account].
 *
 * ## Note
 *
 * A [Contact] may consist of one or more [RawContact]. A [RawContact] is an association between a
 * Contact and an [android.accounts.Account]. Each [RawContact] is associated with several pieces of
 * Data such as emails.
 *
 * The Contacts Provider may combine [RawContact] from several different Accounts. The same effect
 * is achieved when merging / linking multiple contacts.
 *
 * ## Developer notes
 *
 * MutableLists are used instead of MutableSets to allow for duplicates, which are allowed in the
 * native Contacts app. Technically, sets could be used here because each data has different row id.
 * However, [MutableRawContact] data all have invalid ids, which disallows duplicates for consumer-
 * created data instances. Therefore, lists are also used here for parity. Besides, lists are more
 * commonly used in Android development and are more supported than sets.
 */
@Parcelize
data class RawContact internal constructor(

    /**
     * The unique ID of this [RawContact].
     *
     * The value of RawContacts._ID / Data.RAW_CONTACT_ID.
     */
    override val id: Long,

    /**
     * The ID of the [Contact] that this [RawContact] is associated with.
     *
     * The value of RawContacts.CONTACT_ID / Data.CONTACT_ID.
     */
    val contactId: Long,

    /**
     * An immutable list of addresses.
     */
    val addresses: List<Address>,

    val company: Company?,

    /**
     * An immutable list of emails.
     */
    val emails: List<Email>

) : Entity, Parcelable {

    fun toMutableRawContact() = MutableRawContact(
        id = id,
        contactId = contactId,

        addresses = addresses.asSequence().map { it.toMutableAddress() }.toMutableList(),

        company = company?.toMutableCompany(),

        emails = emails.asSequence().map { it.toMutableEmail() }.toMutableList()
    )
}

/**
 * A mutable [RawContact].
 */
@Parcelize
data class MutableRawContact internal constructor(

    /**
     * See [RawContact.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [RawContact.contactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    val contactId: Long,

    /**
     * Mutable version of [RawContact.addresses].
     */
    var addresses: MutableList<MutableAddress>,

    /**
     * Mutable version of [RawContact.company].
     */
    var company: MutableCompany?,

    /**
     * Mutable version of [RawContact.emails].
     */
    var emails: MutableList<MutableEmail>

) : Entity, Parcelable {

    constructor() : this(INVALID_ID, INVALID_ID, mutableListOf(), null, mutableListOf())

    internal fun toRawContact() = RawContact(
        id = id,
        contactId = contactId,

        addresses = addresses.asSequence().map { it.toAddress() }.toList(),

        company = company?.toCompany(),

        emails = emails.asSequence().map { it.toEmail() }.toList()
    )
}