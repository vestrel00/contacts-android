package contacts.core.entities

import kotlinx.parcelize.Parcelize

/**
 * A data kind representing an organization.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 */
sealed interface OrganizationEntity : DataEntity {

    // Type and Label are also available. However, both keep getting set to null automatically by
    // the Contacts Provider...

    /**
     * The company as the user entered it.
     */
    val company: String?

    /**
     * The position title at this company as the user entered it.
     */
    val title: String?

    /**
     * The department at this company as the user entered it.
     */
    val department: String?

    /**
     * The job description at this company as the user entered it.
     */
    val jobDescription: String?

    /**
     * The office location of this organization.
     */
    val officeLocation: String?

    /**
     * The symbol of this company as the user entered it.
     */
    val symbol: String?

    /**
     * The phonetic name of this company as the user entered it.
     */
    val phoneticName: String?

    /**
     * The [company].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::company
    override val primaryValue: String?
        get() = company

    override val mimeType: MimeType
        get() = MimeType.Organization

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            company, title, department, jobDescription, officeLocation, symbol, phoneticName
        )

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): OrganizationEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from OrganizationEntity, there is only one interface that extends it; MutableOrganizationEntity.
 *
 * The MutableOrganizationEntity interface is used for library constructs that require an OrganizationEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableOrganization and NewOrganization. With this, we can create constructs that can
 * keep a reference to MutableOrganization(s) or NewOrganization(s) through the MutableOrganizationEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewOrganizationEntity, ExistingOrganizationEntity, and
 * ImmutableOrganizationEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [OrganizationEntity]. `
 */
sealed interface MutableOrganizationEntity : OrganizationEntity, MutableDataEntity {

    override var company: String?
    override var title: String?
    override var department: String?
    override var jobDescription: String?
    override var officeLocation: String?
    override var symbol: String?
    override var phoneticName: String?

    /**
     * The [company].
     */
    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::company
    override var primaryValue: String?
        get() = company
        set(value) {
            company = value
        }

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableOrganizationEntity
}

/**
 * An existing immutable [OrganizationEntity].
 */
@ConsistentCopyVisibility
@Parcelize
data class Organization internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val company: String?,
    override val title: String?,
    override val department: String?,
    override val jobDescription: String?,
    override val officeLocation: String?,
    override val symbol: String?,
    override val phoneticName: String?,

    override val isRedacted: Boolean

) : OrganizationEntity, ExistingDataEntity,
    ImmutableDataEntityWithMutableType<MutableOrganization> {

    override fun mutableCopy() = MutableOrganization(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        company = company,
        title = title,
        department = department,
        jobDescription = jobDescription,
        officeLocation = officeLocation,
        symbol = symbol,
        phoneticName = phoneticName,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        company = company?.redact(),
        title = title?.redact(),
        department = department?.redact(),
        jobDescription = jobDescription?.redact(),
        officeLocation = officeLocation?.redact(),
        symbol = symbol?.redact(),
        phoneticName = phoneticName?.redact(),
    )
}

/**
 * An existing mutable [OrganizationEntity].
 */
@ConsistentCopyVisibility
@Parcelize
data class MutableOrganization internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var company: String?,
    override var title: String?,
    override var department: String?,
    override var jobDescription: String?,
    override var officeLocation: String?,
    override var symbol: String?,
    override var phoneticName: String?,

    override val isRedacted: Boolean

) : OrganizationEntity, ExistingDataEntity, MutableOrganizationEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        company = company?.redact(),
        title = title?.redact(),
        department = department?.redact(),
        jobDescription = jobDescription?.redact(),
        officeLocation = officeLocation?.redact(),
        symbol = symbol?.redact(),
        phoneticName = phoneticName?.redact(),
    )
}

/**
 * A new mutable [OrganizationEntity].
 */
@Parcelize
data class NewOrganization @JvmOverloads constructor(

    override var company: String? = null,
    override var title: String? = null,
    override var department: String? = null,
    override var jobDescription: String? = null,
    override var officeLocation: String? = null,
    override var symbol: String? = null,
    override var phoneticName: String? = null,

    override var isReadOnly: Boolean = false,
    override val isRedacted: Boolean = false

) : OrganizationEntity, NewDataEntity, MutableOrganizationEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        company = company?.redact(),
        title = title?.redact(),
        department = department?.redact(),
        jobDescription = jobDescription?.redact(),
        officeLocation = officeLocation?.redact(),
        symbol = symbol?.redact(),
        phoneticName = phoneticName?.redact(),
    )
}