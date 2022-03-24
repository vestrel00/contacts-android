package contacts.core.entities

import kotlinx.parcelize.Parcelize

/**
 * [Entity] that holds data modeling columns in the SIM table.
 */
sealed interface SimContactEntity : Entity {

    /**
     * The name of the person.
     *
     * ## Character limit
     *
     * This is subject to the SIM card's maximum character limit, which is typically around 14
     * characters. This may vary per SIM card. When inserting, any characters over the limit
     * will automatically be truncated.
     *
     * See https://www.google.com/search?q=sim+card+name+character+limit
     */
    val name: String?

    /**
     * The phone number.
     *
     * ## Character limit
     *
     * SIM cards typically support phone numbers of all lengths. You shouldn't need to worry about
     * this as long as you put in a valid number.
     */
    val number: String?

    // TODO We'll eventually support emails in SIM when the system level APIs support it correctly and reliably.
    // Support for CRUD operations for emails in SIM cards was implemented in Android 12 (API 31).
    // Given that this support is very new and IMO unstable and still incomplete, this library will
    // not yet support emails in SIM cards.
    /**
     * The email addresses in CSV format (comma separated values).
     *
     * - when there is no email, this value may be ","
     * - when there is one email, this value is "one@gmail.com,"
     * - when there are two emails, this value is "one@gmail.com,two@gmail.com,"
     *
     * There seems to always be a trailing ",". This may or may not vary between SIM cards and OEMs.
     */
    // val emails: String?

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(name, number)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): SimContactEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * This is why there are no interfaces for NewSimContactEntity, ExistingSimContactEntity,
 * ImmutableSimContactEntity, and MutableNewSimContactEntity. There are currently no
 * library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * An existing immutable [SimContactEntity].
 */
@Parcelize
data class SimContact internal constructor(

    /**
     * The "volatile" row ID in the SIM table. This in-memory value may be different than the value
     * in the SIM table. It may change.
     *
     * DO NOT RELY ON THIS TO MATCH VALUES IN THE DATABASE!
     *
     * ## Developer notes
     *
     * The _id in the SIM table always starts at 0. When there are 10 contacts, there are 10 rows
     * with IDs from 0 to 9. When deleting a contact at row 0, all remaining contacts will have
     * their ID's shifted down by one.
     *
     * I guess this is due to the memory restrictions in SIM cards. Perhaps ints are supported but
     * not longs, therefore the row IDs behave like this.
     */
    override val id: Long,

    override val name: String?,
    override val number: String?,

    override val isRedacted: Boolean

) : SimContactEntity, ExistingEntity, ImmutableEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        number = number?.redact()
    )
}

/**
 * A new mutable [SimContactEntity].
 */
@Parcelize
data class NewSimContact @JvmOverloads constructor(

    override var name: String? = null,
    override var number: String? = null,

    override val isRedacted: Boolean = false

) : SimContactEntity, NewEntity, MutableEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        number = number?.redact()
    )
}