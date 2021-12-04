package contacts.test.entities

import contacts.core.entities.CustomDataEntity
import contacts.core.entities.ImmutableCustomDataEntity
import contacts.core.entities.MimeType
import kotlinx.parcelize.Parcelize

internal sealed interface TestDataEntity : CustomDataEntity {

    override val isPrimary: Boolean
        get() = false

    override val isSuperPrimary: Boolean
        get() = false

    /**
     * The value of this string does not matter. However, it does need to be non-null and non blank
     * so that it does not get mistakenly cleaned up by the Contacts Provider.
     */
    val value: String
        get() = VALUE

    override val mimeType: MimeType.Custom
        get() = TestDataMimeType

    // This needs to be false so that it is able to be included in query results, inserted, and not
    // deleted upon update.
    override val isBlank: Boolean
        get() = false

    companion object {
        const val VALUE = "This data and the RawContact it belongs to are for test purposes only"
    }
}

/**
 * Indicates that a RawContact exist for test purposes only.
 */
@Parcelize
internal data class TestData(
    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    /**
     * The value of this string does not matter. However, it does need to be non-null and non blank
     * so that it does not get mistakenly cleaned up by the Contacts Provider.
     */
    override val value: String

) : TestDataEntity, ImmutableCustomDataEntity