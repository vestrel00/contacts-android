package contacts.core.entities

import kotlinx.parcelize.Parcelize

/**
 * [Entity] that holds data modeling columns in the SIM table.
 */
sealed interface SimContactEntity : Entity {

    /**
     * The name of the person.
     */
    val name: String?

    /**
     * The phone number.
     */
    val number: String?

    /**
     * The email address.
     */
    val email: String?

    // type and label are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(name, number, email)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): SimContactEntity
}

// TODO Update this if SIM contacts cannot be updated!
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
// TODO Remove this if SIM contacts cannot be updated!
sealed interface MutableSimContactEntity : SimContactEntity, MutableEntity {

    override var name: String?
    override var number: String?
    override var email: String?

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableSimContactEntity
}

/**
 * An existing immutable [SimContactEntity].
 */
@Parcelize
data class SimContact internal constructor(

    override val id: Long,

    override val name: String?,
    override val number: String?,
    override val email: String?,

    override val isRedacted: Boolean

) : SimContactEntity, ExistingEntity, ImmutableEntityWithMutableType<MutableSimContact> {

    override fun mutableCopy() = MutableSimContact(
        id = id,

        name = name,
        number = number,
        email = email,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        number = number?.redact(),
        email = email?.redact()
    )
}

/**
 * An existing mutable [EmailEntity].
 */
// TODO Remove this if SIM contacts cannot be updated!
@Parcelize
data class MutableSimContact internal constructor(

    override val id: Long,

    override var name: String?,
    override var number: String?,
    override var email: String?,

    override val isRedacted: Boolean

) : SimContactEntity, ExistingEntity, MutableSimContactEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        number = number?.redact(),
        email = email?.redact()
    )
}


/**
 * A new mutable [SimContactEntity].
 */
@Parcelize
data class NewSimContact @JvmOverloads constructor(

    override var name: String? = null,
    override var number: String? = null,
    override var email: String? = null,

    override val isRedacted: Boolean = false

) : SimContactEntity, NewEntity, MutableSimContactEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        number = number?.redact(),
        email = email?.redact()
    )
}