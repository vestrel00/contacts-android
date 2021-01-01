package contacts.entities

import contacts.util.unsafeLazy
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Name internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The name that should be used to display the (raw) contact. This is the unstructured component
     * of the name should be consistent with its structured representation.
     *
     * The [displayName] is the unstructured representation of the name. It is made up of structured
     * components; [prefix], [givenName], [middleName], [familyName], and [suffix].
     *
     * When updating or inserting;
     *
     * - If the [displayName] is null and there are non-null structured components provided (e.g.
     *   [givenName] and [familyName]), the Contacts Provider will automatically set the
     *   [displayName] by combining the structured components.
     *
     * - If the [displayName] is not null and all structured components are null, the Contacts
     *   Provider automatically (to the best of its ability) sets the values for all the structured
     *   components.
     *
     * - If the [displayName] and structured components are not null, the Contacts Provider does
     *   nothing automatically.
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

) : CommonDataEntity {

    override val mimeType: MimeType = MimeType.Name

    override val isBlank: Boolean by unsafeLazy {
        propertiesAreAllNullOrBlank(
            displayName,
            givenName, middleName, familyName,
            prefix, suffix,
            phoneticGivenName, phoneticMiddleName, phoneticFamilyName
        )
    }

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

) : MutableCommonDataEntity {

    constructor() : this(
        null, null, null, false, false, null, null,
        null, null, null, null, null, null, null
    )

    override val mimeType: MimeType = MimeType.Name

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            displayName,
            givenName, middleName, familyName,
            prefix, suffix,
            phoneticGivenName, phoneticMiddleName, phoneticFamilyName
        )
}