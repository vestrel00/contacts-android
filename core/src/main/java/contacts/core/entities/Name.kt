package contacts.core.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A data kind representing the contact's proper name.
 *
 * A RawContact may have 0 or 1 entry of this data kind.
 */
sealed interface NameEntity : DataEntity {

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
    val displayName: String?

    /**
     * The given name for the contact.
     */
    val givenName: String?

    /**
     * The contact's middle name.
     */
    val middleName: String?

    /**
     * The family name for the contact.
     */
    val familyName: String?

    /**
     * The contact's honorific prefix, e.g. "Sir".
     */
    val prefix: String?

    /**
     * The contact's honorific suffix, e.g. "Jr".
     */
    val suffix: String?

    /**
     * The phonetic version of the [givenName].
     */
    val phoneticGivenName: String?

    /**
     * The phonetic version of the [middleName].
     */
    val phoneticMiddleName: String?

    /**
     * The phonetic version of the [familyName].
     */
    val phoneticFamilyName: String?

    override val mimeType: MimeType
        get() = MimeType.Name

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(
            displayName,
            givenName, middleName, familyName,
            prefix, suffix,
            phoneticGivenName, phoneticMiddleName, phoneticFamilyName
        )
}

/**
 * An immutable [NameEntity].
 */
@Parcelize
data class Name internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val displayName: String?,

    override val givenName: String?,
    override val middleName: String?,
    override val familyName: String?,

    override val prefix: String?,
    override val suffix: String?,

    override val phoneticGivenName: String?,
    override val phoneticMiddleName: String?,
    override val phoneticFamilyName: String?

) : NameEntity, ImmutableDataEntityWithMutableType<MutableName> {

    override fun mutableCopy() = MutableName(
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

/**
 * A mutable [NameEntity].
 */
@Parcelize
data class MutableName internal constructor(

    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override var displayName: String?,

    override var givenName: String?,
    override var middleName: String?,
    override var familyName: String?,

    override var prefix: String?,
    override var suffix: String?,

    override var phoneticGivenName: String?,
    override var phoneticMiddleName: String?,
    override var phoneticFamilyName: String?

) : NameEntity, MutableDataEntity {

    constructor() : this(
        null, null, null, false, false, null, null,
        null, null, null, null, null, null, null
    )

    @IgnoredOnParcel
    override var primaryValue: String? by this::displayName
}