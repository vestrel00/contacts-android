package com.vestrel00.contacts.entities

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Name internal constructor(

    override val id: Long,

    override val rawContactId: Long,

    override val contactId: Long,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The name that should be used to display the contact.
     *
     * Unstructured component of the name should be consistent with its structured representation.
     *
     * Note! This is automatically set the the Contacts Provider if null!
     */
    val displayName: String?,

    /**
     * The given name for the contact.
     */
    val givenName: String?,

    /**
     * The contact's middle name.
     */
    val middleName: String?,

    /**
     * The family name for the contact.
     */
    val familyName: String?,

    /**
     * The contact's honorific prefix, e.g. "Sir".
     */
    val prefix: String?,

    /**
     * The contact's honorific suffix, e.g. "Jr".
     */
    val suffix: String?,

    /**
     * The phonetic version of the [givenName].
     */
    val phoneticGivenName: String?,

    /**
     * The phonetic version of the [middleName].
     */
    val phoneticMiddleName: String?,

    /**
     * The phonetic version of the [familyName].
     */
    val phoneticFamilyName: String?

) : DataEntity, Parcelable {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.NAME

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        displayName,
        givenName, middleName, familyName,
        prefix, suffix,
        phoneticGivenName, phoneticMiddleName, phoneticFamilyName
    )

    fun toMutableName() = MutableName(
        id = id,
        rawContactId = rawContactId,
        contactId = contactId,

        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,

        displayName = displayName,

        givenName = givenName,
        middleName = middleName,
        familyName = familyName,

        prefix = prefix,
        suffix = suffix,

        phoneticGivenName = phoneticGivenName,
        phoneticMiddleName = phoneticMiddleName,
        phoneticFamilyName = phoneticFamilyName
    )
}

@Parcelize
data class MutableName internal constructor(

    override val id: Long,

    override val rawContactId: Long,

    override val contactId: Long,

    override var isPrimary: Boolean,

    override var isSuperPrimary: Boolean,

    /**
     * See [Name.displayName].
     */
    var displayName: String?,

    /**
     * See [Name.givenName].
     */
    var givenName: String?,

    /**
     * See [Name.middleName].
     */
    var middleName: String?,

    /**
     * See [Name.familyName].
     */
    var familyName: String?,

    /**
     * See [Name.prefix].
     */
    var prefix: String?,

    /**
     * See [Name.suffix].
     */
    var suffix: String?,

    /**
     * See [Name.phoneticGivenName].
     */
    var phoneticGivenName: String?,

    /**
     * See [Name.phoneticMiddleName].
     */
    var phoneticMiddleName: String?,

    /**
     * See [Name.phoneticFamilyName].
     */
    var phoneticFamilyName: String?

) : DataEntity, Parcelable {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.NAME

    constructor() : this(
        INVALID_ID, INVALID_ID, INVALID_ID, false, false, null, null,
        null, null, null, null, null, null, null
    )

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        displayName,
        givenName, middleName, familyName,
        prefix, suffix,
        phoneticGivenName, phoneticMiddleName, phoneticFamilyName
    )
}