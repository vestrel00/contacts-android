package com.vestrel00.contacts.entities

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Name internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The name that should be used to display the (raw) contact.
     *
     * Unstructured component of the name should be consistent with its structured representation.
     *
     * This is automatically set by the Contacts Provider from the other name components (given,
     * middle, family, etc) if inserted as null.
     *
     * ## [ContactEntity.displayNamePrimary] vs [Name.displayName]
     *
     * The [ContactEntity.displayNamePrimary] may be different than [Name.displayName]. If a [Name]
     * in the Data table is not provided, then other kinds of data will be used as the Contact's
     * display name. For example, if an [Email] is provided but no [Name] then the display name will
     * be the email. When a [Name] is inserted, the Contacts Provider automatically updates the
     * [ContactEntity.displayNamePrimary].
     *
     * If data rows suitable to be a [ContactEntity.displayNamePrimary] are not available, it will
     * be null.
     *
     * Data suitable to be a Contacts row display name are;
     *
     * - [Organization]
     * - [Email]
     * - [Name]
     * - [Nickname]
     * - [Phone]
     *
     * The [ContactEntity.displayNamePrimary] is automatically resolved by the Contacts Provider. It
     * may not be manually modified.
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

) : DataEntity {

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

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

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

) : MutableDataEntity {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.NAME

    constructor() : this(
        null, null, null, false, false, null, null,
        null, null, null, null, null, null, null
    )

    override fun isBlank(): Boolean = propertiesAreAllNullOrBlank(
        displayName,
        givenName, middleName, familyName,
        prefix, suffix,
        phoneticGivenName, phoneticMiddleName, phoneticFamilyName
    )
}