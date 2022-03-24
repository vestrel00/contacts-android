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
     * This is subject to the SIM card's maximum character limit, which is typically around 14-16
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

    // FIXME We'll eventually support emails in SIM when the system level APIs support it reliably.
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
 * Apart from SimContactEntity, there is only one interface that extends it; MutableSimContactEntity.
 *
 * The MutableSimContactEntity interface is used for library constructs that require an SimContactEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableSimContact and NewSimContact. With this, we can create constructs that can
 * keep a reference to MutableSimContact(s) or NewSimContact(s) through the MutableSimContactEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewSimContactEntity, ExistingSimContactEntity, and
 * ImmutableSimContactEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [SimContactEntity]. `
 */
sealed interface MutableSimContactEntity : SimContactEntity, MutableEntity {

    override var name: String?
    override var number: String?
    // override var emails: String?

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableSimContactEntity
}

/**
 * An existing immutable [SimContactEntity].
 */
@Parcelize
data class SimContact internal constructor(

    /**
     * The row ID in the SIM table.
     *
     * The contact this is pointing to may change if this contact is deleted in the database and
     * another contact is inserted. The inserted contact may be assigned the ID of the deleted
     * contact.
     *
     * DO NOT RELY ON THIS TO MATCH VALUES IN THE DATABASE! The SIM table does not support selection
     * by ID so you can't use this for anything anyways.
     */
    override val id: Long,

    override val name: String?,
    override val number: String?,
    // override val emails: String?,

    override val isRedacted: Boolean

) : SimContactEntity, ExistingEntity, ImmutableEntityWithMutableType<MutableSimContact> {

    override fun mutableCopy() = MutableSimContact(
        id = id,

        name = name,
        number = number,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        number = number?.redact()
        // emails = emails?.redact()
    )
}

/**
 * An existing mutable [SimContactEntity].
 */
@Parcelize
data class MutableSimContact internal constructor(

    override val id: Long,

    override var name: String?,
    override var number: String?,
    // override var emails: String?,

    override val isRedacted: Boolean

) : SimContactEntity, ExistingEntity, MutableSimContactEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        number = number?.redact()
        // emails = emails?.redact()
    )
}

/**
 * A new mutable [SimContactEntity].
 */
@Parcelize
data class NewSimContact @JvmOverloads constructor(

    override var name: String? = null,
    override var number: String? = null,
    // override var emails: String? = null,

    override val isRedacted: Boolean = false

) : SimContactEntity, NewEntity, MutableSimContactEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        number = number?.redact()
        // emails = emails?.redact()
    )
}