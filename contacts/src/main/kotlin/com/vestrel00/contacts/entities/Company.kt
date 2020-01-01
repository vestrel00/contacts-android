package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Company internal constructor(

    /**
     * The id of this row in the Data table.
     */
    override val id: Long,

    /**
     * The id of the [RawContact] this data belongs to.
     */
    override val rawContactId: Long,

    /**
     * The id of the [Contact] that this data entity is associated with.
     */
    override val contactId: Long,

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

) : DataEntity, Parcelable {

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        company, title, department, jobDescription, officeLocation, symbol, phoneticName
    )

    fun toMutableCompany() = MutableCompany(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

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
data class MutableCompany internal constructor(

    /**
     * See [Company.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [Company.rawContactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val rawContactId: Long,

    /**
     * See [Company.contactId].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val contactId: Long,

    /**
     * See [Company.company].
     */
    var company: String?,

    /**
     * See [Company.title].
     */
    var title: String?,

    /**
     * See [Company.department].
     */
    var department: String?,

    /**
     * See [Company.jobDescription].
     */
    var jobDescription: String?,

    /**
     * See [Company.officeLocation].
     */
    var officeLocation: String?,

    /**
     * See [Company.symbol].
     */
    var symbol: String?,

    /**
     * See [Company.phoneticName].
     */
    var phoneticName: String?

) : DataEntity, Parcelable {

    constructor() : this(
        INVALID_ID, INVALID_ID, INVALID_ID, null, null, null, null,
        null, null, null
    )

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        company, title, department, jobDescription, officeLocation, symbol, phoneticName
    )

    internal fun toCompany() = Company(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        company = company,
        title = title,
        department = department,
        jobDescription = jobDescription,
        officeLocation = officeLocation,
        symbol = symbol,

        phoneticName = phoneticName
    )
}
