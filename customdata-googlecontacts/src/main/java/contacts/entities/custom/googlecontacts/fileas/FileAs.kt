package contacts.entities.custom.googlecontacts.fileas

import contacts.core.entities.*
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType
import kotlinx.parcelize.Parcelize

/**
 * The "File as" field used in the Google Contacts app for a RawContact.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 */
sealed interface FileAsEntity : CustomDataEntity {

    /**
     * The "File as" value.
     */
    val name: String?

    /**
     * The [name].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::name
    override val primaryValue: String?
        get() = name

    override val mimeType: MimeType.Custom
        get() = GoogleContactsMimeType.FileAs

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(name)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): FileAsEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from FileAsEntity, there is only one interface that extends it; MutableFileAsEntity.
 *
 * The MutableFileAsEntity interface is used for library constructs that require an FileAsEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableFileAs and NewFileAs. With this, we can create constructs that can
 * keep a reference to MutableFileAs(s) or NewFileAs(s) through the MutableFileAsEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewFileAsEntity, ExistingFileAsEntity, and
 * ImmutableFileAsEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [FileAsEntity]. `
 */
sealed interface MutableFileAsEntity : FileAsEntity, MutableCustomDataEntity {

    override var name: String?

    /**
     * The [name].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::name
    override var primaryValue: String?
        get() = name
        set(value) {
            name = value
        }

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableFileAsEntity
}

/**
 * An existing immutable [FileAsEntity].
 */
@Parcelize
data class FileAs internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val name: String?,

    override val isRedacted: Boolean

) : FileAsEntity, ExistingCustomDataEntity,
    ImmutableCustomDataEntityWithMutableType<MutableFileAs> {

    override fun mutableCopy() = MutableFileAs(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        name = name,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact()
    )
}

/**
 * An existing mutable [FileAsEntity].
 */
@Parcelize
data class MutableFileAs internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var name: String?,

    override val isRedacted: Boolean

) : FileAsEntity, ExistingCustomDataEntity, MutableFileAsEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact()
    )
}

/**
 * A new mutable [FileAsEntity].
 */
@Parcelize
data class NewFileAs @JvmOverloads constructor(

    override var name: String? = null,

    override val isRedacted: Boolean = false

) : FileAsEntity, NewCustomDataEntity, MutableFileAsEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact()
    )
}
