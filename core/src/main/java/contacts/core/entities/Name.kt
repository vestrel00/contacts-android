package contacts.core.entities

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

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from NameEntity, there is only one interface that extends it; MutableNameEntity.
 *
 * The MutableNameEntity interface is used for library constructs that require an NameEntity
 * that can be mutated whether it is already inserted in the database or not. There are two
 * variants of this; MutableName and NewName. With this, we can create constructs that can
 * keep a reference to MutableName(s) or NewName(s) through the MutableNameEntity
 * abstraction/facade.
 *
 * This is why there are no interfaces for NewNameEntity, ExistingNameEntity, and
 * ImmutableNameEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A mutable [NameEntity]. `
 */
sealed interface MutableNameEntity : NameEntity, MutableDataEntity {

    override var displayName: String?

    override var givenName: String?
    override var middleName: String?
    override var familyName: String?

    override var prefix: String?
    override var suffix: String?

    override var phoneticGivenName: String?
    override var phoneticMiddleName: String?
    override var phoneticFamilyName: String?

    // Delegated properties are not allowed on interfaces =(
    // override var primaryValue: String? by this::displayName
    override var primaryValue: String?
        get() = displayName
        set(value) {
            displayName = value
        }
}

/**
 * An existing immutable [NameEntity].
 */
@Parcelize
data class Name internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

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

) : NameEntity, ExistingDataEntity, ImmutableDataEntityWithMutableType<MutableName> {

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
 * An existing mutable [NameEntity].
 */
@Parcelize
data class MutableName internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

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

) : NameEntity, ExistingDataEntity, MutableNameEntity

/**
 * A new mutable [NameEntity].
 */
// Intentionally expose primary constructor to consumers. Useful for Kotlin users.
@Parcelize
data class NewName(

    override var displayName: String?,

    override var givenName: String?,
    override var middleName: String?,
    override var familyName: String?,

    override var prefix: String?,
    override var suffix: String?,

    override var phoneticGivenName: String?,
    override var phoneticMiddleName: String?,
    override var phoneticFamilyName: String?

) : NameEntity, NewDataEntity, MutableNameEntity {

    // An empty constructor for consumer use. Useful for both Kotlin and Java users.
    constructor() : this(null, null, null, null, null, null, null, null, null)
}