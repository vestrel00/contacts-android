package contacts.test.entities

import contacts.core.entities.MimeType
import contacts.core.entities.MutableCustomData
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

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
    val value: String = VALUE

) : MutableCustomData {
    // MutableCustomDataEntity are also CustomDataEntity so this serves as both the mutable and
    // immutable implementations. This is okay because this isn't really mutable.

    @IgnoredOnParcel
    override val mimeType: MimeType.Custom = TestDataMimeType

    // This needs to be false so that it is able to be included in query results, inserted, and not
    // deleted upon update.
    @IgnoredOnParcel
    override val isBlank: Boolean = false

    override var primaryValue: String?
        get() = value
        set(_) {}

    companion object {
        const val VALUE = "This data and the RawContact it belongs to are for test purposes only"
    }
}