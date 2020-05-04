package com.vestrel00.contacts.entities

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Organization internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    // Type and Label are also available. However, both keep getting set to null automatically by
    // the Contacts Provider...

    /**
     * The company as the user entered it.
     */
    val company: String?,

    /**
     * The position title at this company as the user entered it.
     */
    val title: String?,

    /**
     * The department at this company as the user entered it.
     */
    val department: String?,

    /**
     * The job description at this company as the user entered it.
     */
    val jobDescription: String?,

    /**
     * The office location of this organization.
     */
    val officeLocation: String?,

    /**
     * The symbol of this company as the user entered it.
     */
    val symbol: String?,

    /**
     * The phonetic name of this company as the user entered it.
     */
    val phoneticName: String?

) : DataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.ORGANIZATION

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        company, title, department, jobDescription, officeLocation, symbol, phoneticName
    )

    fun toMutableOrganization() = MutableOrganization(
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

@Parcelize
data class MutableOrganization internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Organization.company].
     */
    var company: String?,

    /**
     * See [Organization.title].
     */
    var title: String?,

    /**
     * See [Organization.department].
     */
    var department: String?,

    /**
     * See [Organization.jobDescription].
     */
    var jobDescription: String?,

    /**
     * See [Organization.officeLocation].
     */
    var officeLocation: String?,

    /**
     * See [Organization.symbol].
     */
    var symbol: String?,

    /**
     * See [Organization.phoneticName].
     */
    var phoneticName: String?

) : DataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.ORGANIZATION

    constructor() : this(
        null, null, null, false, false, null, null,
        null, null, null, null, null
    )

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        company, title, department, jobDescription, officeLocation, symbol, phoneticName
    )
}
