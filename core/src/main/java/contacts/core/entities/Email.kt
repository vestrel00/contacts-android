package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds
import contacts.core.entities.EmailEntity.Type
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing an email address.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 */
sealed interface EmailEntity : DataEntityWithTypeAndLabel<Type> {

    /**
     * The email address.
     */
    val address: String?

    /**
     * The [address].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::address
    override val primaryValue: String?
        get() = address

    override val mimeType: MimeType
        get() = MimeType.Email

    // type and label are intentionally excluded as per documentation
    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(address)

    enum class Type(override val value: Int) : DataEntity.Type {

        // Order of declaration is the same as seen in the native contacts app
        HOME(CommonDataKinds.Email.TYPE_HOME), // Default
        WORK(CommonDataKinds.Email.TYPE_WORK),
        OTHER(CommonDataKinds.Email.TYPE_OTHER),
        CUSTOM(CommonDataKinds.Email.TYPE_CUSTOM);

        // For some reason, the native contacts app does not show this type in the UI. Thus, we will
        // do the same =)
        // MOBILE(CommonDataKinds.Email.TYPE_MOBILE)

        override fun labelStr(resources: Resources, label: String?): String =
            CommonDataKinds.Email.getTypeLabel(resources, value, label).toString()

        internal companion object {

            fun fromValue(value: Int?): Type? = values().find { it.value == value }
        }
    }
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from EmailEntity, there is only one interface that extends it; MutableEmailEntity.
 *
 * The MutableEmailEntity interface is used for library constructs that require an EmailEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableEmail and NewEmail. With this, we can create constructs that can
 * keep a reference to MutableEmail(s) or NewEmail(s) through the MutableEmailEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewEmailEntity, ExistingEmailEntity, and
 * ImmutableEmailEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [EmailEntity]. `
 */
sealed interface MutableEmailEntity : EmailEntity, MutableDataEntityWithTypeAndLabel<Type> {

    override var address: String?

    /**
     * The [address].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::address
    override var primaryValue: String?
        get() = address
        set(value) {
            address = value
        }
}

/**
 * An existing immutable [EmailEntity].
 */
@Parcelize
data class Email internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val type: Type?,
    override val label: String?,

    override val address: String?

) : EmailEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableEmail> {

    override fun mutableCopy() = MutableEmail(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        type = type,
        label = label,

        address = address
    )
}

/**
 * An existing mutable [EmailEntity].
 */
@Parcelize
data class MutableEmail internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var type: Type?,
    override var label: String?,
    override var address: String?

) : EmailEntity, ExistingDataEntity, MutableEmailEntity

/**
 * An new mutable [EmailEntity].
 */
@Parcelize
data class NewEmail @JvmOverloads constructor(

    override var type: Type? = null,
    override var label: String? = null,
    override var address: String? = null

) : EmailEntity, NewDataEntity, MutableEmailEntity