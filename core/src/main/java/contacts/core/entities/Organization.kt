package contacts.core.entities

import kotlinx.parcelize.IgnoredOnParcel
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

    override val mimeType: MimeType
        get() = MimeType.Organization

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            company, title, department, jobDescription, officeLocation, symbol, phoneticName
        )
}

/**
 * An immutable [OrganizationEntity].
 */
@Parcelize
data class Organization internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val company: String?,
    override val title: String?,
    override val department: String?,
    override val jobDescription: String?,
    override val officeLocation: String?,
    override val symbol: String?,
    override val phoneticName: String?

) : OrganizationEntity, ImmutableDataEntityWithMutableType<MutableOrganization> {

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
        phoneticName = phoneticName
    )
}

/**
 * A mutable [OrganizationEntity].
 */
@Parcelize
data class MutableOrganization internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var company: String?,
    override var title: String?,
    override var department: String?,
    override var jobDescription: String?,
    override var officeLocation: String?,
    override var symbol: String?,
    override var phoneticName: String?

) : OrganizationEntity, MutableDataEntity {

    constructor() : this(
        null, null, null, false, false, null, null,
        null, null, null, null, null
    )

    @IgnoredOnParcel
    override var primaryValue: String? by this::company
}
