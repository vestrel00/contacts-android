package contacts.entities.custom.googlecontacts.userdefined

import contacts.core.entities.*
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType
import kotlinx.parcelize.Parcelize

/**
 * The "Custom field" and "Custom label" pair used in the Google Contacts app for a RawContact.
 *
 * A RawContact may have 0, 1, or more entry of this data kind.
 *
 * ## Google Contacts app data integrity
 *
 * When inserting or updating this data kind, the Google Contacts app enforces [field] and [label]
 * to both be non-null and non-blank. Otherwise, the insert or update operation fails. To protect
 * the data integrity that the Google Contacts app imposes, this library is silently not performing
 * insert or update operations for these instances. Consumers are informed via documentation (this).
 *
 * Both [field] and [label] must be non-null and non-blank strings in order for insert and update
 * operations to be performed on them. The corresponding fields must also be included in the insert
 * or update operation. Otherwise, the update and insert operation will silently NOT be performed.
 *
 * We might change the way we handle this in the future. Maybe we'll throw an exception instead or
 * fail the entire insert/update and bubble up the reason. For now, to avoid complicating the API
 * in these early stages, we'll go with silent but documented. We'll see what the community thinks!
 */
sealed interface UserDefinedEntity : CustomDataEntity {

    /**
     * The "Custom field" value.
     *
     * ## Data integrity
     *
     * Both [field] and [label] must be non-null and non-blank strings in order for insert and
     * update operations to be performed on them. The corresponding fields must also be included
     * in the insert or update operation. Otherwise, the update and insert operation will silently
     * NOT be performed.
     *
     * For more info, read the class documentation.
     */
    val field: String?

    /**
     * The "Custom label" value.
     *
     * ## Data integrity
     *
     * Both [field] and [label] must be non-null and non-blank strings in order for insert and
     * update operations to be performed on them. The corresponding fields must also be included
     * in the insert or update operation. Otherwise, the update and insert operation will silently
     * NOT be performed.
     *
     * For more info, read the class documentation.
     */
    val label: String?

    // The primary value is the combination of both the field and label. So, this does nothing to
    // avoid complicating the API implementation. It is unused and will always return null.
    override val primaryValue: String?
        get() = null

    override val mimeType: MimeType.Custom
        get() = GoogleContactsMimeType.UserDefined

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(this.field, label)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): UserDefinedEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from UserDefinedEntity, there is only one interface that extends it;
 * MutableUserDefinedEntity.
 *
 * The MutableUserDefinedEntity interface is used for library constructs that require an
 * UserDefinedEntity that can be mutated whether it is already inserted in the database or not.
 * There are two variants of this; MutableUserDefined and NewUserDefined. With this, we can create
 * constructs that can keep a reference to MutableUserDefined(s) or NewUserDefined(s) through the
 * MutableUserDefinedEntity abstraction/facade.
 *
 * This is why there are no interfaces for NewUserDefinedEntity, ExistingUserDefinedEntity, and
 * ImmutableUserDefinedEntity. There are currently no library functions or constructs that require
 * them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [UserDefinedEntity].
 */
sealed interface MutableUserDefinedEntity : UserDefinedEntity, MutableCustomDataEntity {

    override var field: String?
    override var label: String?

    // The primary value is the combination of both the field and label. So, this does nothing to
    // avoid complicating the API implementation. It is unused and will always return null.
    override var primaryValue: String?
        get() = null
        set(_) {}

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableUserDefinedEntity
}

/**
 * An existing immutable [UserDefinedEntity].
 */
@Parcelize
data class UserDefined internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val field: String?,
    override val label: String?,

    override val isRedacted: Boolean

) : UserDefinedEntity, ExistingCustomDataEntity,
    ImmutableCustomDataEntityWithMutableType<MutableUserDefined> {

    override fun mutableCopy() = MutableUserDefined(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        field = field,
        label = label,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        field = field?.redact(),
        label = label?.redact()
    )
}

/**
 * An existing mutable [UserDefinedEntity].
 */
@Parcelize
data class MutableUserDefined internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var field: String?,
    override var label: String?,

    override val isRedacted: Boolean

) : UserDefinedEntity, ExistingCustomDataEntity, MutableUserDefinedEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        field = field?.redact(),
        label = label?.redact()
    )
}

/**
 * A new mutable [UserDefinedEntity].
 */
@Parcelize
data class NewUserDefined @JvmOverloads constructor(

    override var field: String? = null,
    override var label: String? = null,

    override var isReadOnly: Boolean = false,
    override val isRedacted: Boolean = false

) : UserDefinedEntity, NewCustomDataEntity, MutableUserDefinedEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        field = field?.redact(),
        label = label?.redact()
    )
}