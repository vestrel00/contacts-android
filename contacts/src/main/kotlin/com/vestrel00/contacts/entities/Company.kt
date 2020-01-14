package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Company internal constructor(

    override val id: Long,

    override val rawContactId: Long,

    override val contactId: Long,

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

) : DataEntity, Parcelable {

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        company, title, department, jobDescription, officeLocation, symbol, phoneticName
    )

    fun toMutableCompany() = MutableCompany(
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
data class MutableCompany internal constructor(

    override val id: Long,

    override val rawContactId: Long,

    override val contactId: Long,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

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
        INVALID_ID, INVALID_ID, INVALID_ID, false, false, null, null,
        null, null, null, null, null
    )

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        company, title, department, jobDescription, officeLocation, symbol, phoneticName
    )

    internal fun toCompany() = Company(
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
