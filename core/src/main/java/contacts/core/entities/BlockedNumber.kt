package contacts.core.entities

import kotlinx.parcelize.Parcelize

/**
 * [Entity] that holds data modeling columns in the Blocked Numbers table.
 */
sealed interface BlockedNumberEntity : Entity {

    /**
     * The phone number to block as the user entered it.
     *
     * This may or may not be formatted (e.g. (012) 345-6789).
     *
     * ## Email data
     *
     * Other than regular phone numbers, the blocked number provider can also store addresses (such
     * as email) from which a user can receive messages, and calls.
     */
    val number: String?

    /**
     * The [number]'s E164 representation. This value can be omitted in which case the provider
     * will try to automatically infer it. (It'll be left null if the provider fails to infer.)
     *
     * If present, [number] has to be set as well (it will be ignored otherwise).
     *
     * E.G. +10123456789
     *
     * If you want to set this value yourself, you may want to look at
     * [android.telephony.PhoneNumberUtils].
     *
     * ## Email data
     *
     * This may contain an email if [number] is an email.
     */
    val normalizedNumber: String?

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(number, normalizedNumber)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): BlockedNumberEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * This is why there are no interfaces for NewBlockedNumberEntity, ExistingBlockedNumberEntity,
 * ImmutableBlockedNumberEntity, and MutableNewBlockedNumberEntity. There are currently no
 * library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * An existing immutable [BlockedNumberEntity].
 */
@ConsistentCopyVisibility
@Parcelize
data class BlockedNumber internal constructor(

    override val id: Long,

    override val number: String?,
    override val normalizedNumber: String?,

    override val isRedacted: Boolean

) : BlockedNumberEntity, ExistingEntity, ImmutableEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        number = number?.redact(),
        normalizedNumber = normalizedNumber?.redact()
    )
}

/**
 * A new mutable [BlockedNumberEntity].
 */
@Parcelize
data class NewBlockedNumber @JvmOverloads constructor(

    override var number: String? = null,
    override var normalizedNumber: String? = null,

    override val isRedacted: Boolean = false

) : BlockedNumberEntity, NewEntity, MutableEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        number = number?.redact(),
        normalizedNumber = normalizedNumber?.redact()
    )
}