package contacts.test.entities

import contacts.core.entities.*
import kotlinx.parcelize.Parcelize

/**
 * Indicates that a RawContact exist for test purposes only.
 */
internal sealed interface TestDataEntity : CustomDataEntity {

    /**
     * A test value.
     */
    val value: String?

    /**
     * The [value].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::value
    override val primaryValue: String?
        get() = value

    override val mimeType: MimeType.Custom
        get() = TestDataMimeType

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(value)
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from TestDataEntity, there is only one interface that extends it; MutableTestDataEntity.
 *
 * The MutableTestDataEntity interface is used for library constructs that require an TestDataEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableTestData and NewTestData. With this, we can create constructs that can
 * keep a reference to MutableTestData(s) or NewTestData(s) through the MutableTestDataEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewTestDataEntity, ExistingTestDataEntity, and
 * ImmutableTestDataEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [TestDataEntity]. `
 */
internal sealed interface MutableTestDataEntity : TestDataEntity, MutableCustomDataEntity {

    override var value: String?

    /**
     * The [value].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::value
    override var primaryValue: String?
        get() = value
        set(value) {
            this.value = value
        }
}

/**
 * An existing immutable [TestDataEntity].
 */
@Parcelize
internal data class TestData(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val value: String?

) : TestDataEntity, ExistingCustomDataEntity, ImmutableCustomDataEntity

/**
 * An existing mutable [TestDataEntity].
 */
@Parcelize
internal data class MutableTestData(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var value: String?

) : TestDataEntity, ExistingCustomDataEntity, MutableTestDataEntity

/**
 * A new mutable [TestDataEntity].
 */
@Parcelize
internal data class NewTestData @JvmOverloads constructor(

    override var value: String? = null

) : TestDataEntity, NewCustomDataEntity, MutableTestDataEntity