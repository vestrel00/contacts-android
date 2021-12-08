package contacts.core.entities

import android.net.Uri
import contacts.core.util.isProfileId
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * [Entity] that holds data modeling columns in the Contacts table.
 *
 * ## Contact, RawContact, and Data
 *
 * A Contact may consist of one or more RawContact. A RawContact is an association between a Contact
 * and an [android.accounts.Account]. Each RawContact is associated with several pieces of Data such
 * as name, emails, phone, address, and more.
 *
 * The Contacts Provider may combine RawContacts from several different Accounts. The same effect
 * is achieved when merging / linking multiple contacts.
 *
 * It is possible for a RawContact to not be associated with an Account. Such RawContacts are local
 * to the device and are not synced.
 */
sealed interface ContactEntity : Entity {

    /**
     * A list of [RawContactEntity]s that are associated with this contact.
     *
     * This list is sorted by [RawContactEntity.id], which seems to be the sort order used by the
     * native Contacts app when displaying the linked RawContacts and when inserting new data for a
     * Contact with multiple linked RawContacts.
     */
    val rawContacts: List<RawContactEntity>

    /**
     * The standard text shown as the contact's display name, based on the best available
     * information for the contact (for example, it might be the email address if the name is not
     * available). This may be null if the Contacts Provider cannot find a suitable display name
     * source to use.
     *
     * This is the contact name displayed by the native Contacts app when viewing a contact. If a
     * Contact consists of more than one RawContact, then it is up to the Contacts Provider to
     * choose the display name to from the associated RawContacts.
     *
     * The contacts provider is free to choose whatever representation makes most sense for its
     * target market. For example in the default Android Open Source Project implementation, if the
     * display name is based on the [Name] and the [Name] follows the Western full-name style, then
     * this field contains the "given name first" version of the full name.
     *
     * This is a read-only attribute as the Contacts Provider automatically sets this value.
     * This is ignored for insert, update, and delete functions.
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
     * This is a read-only attribute as the Contacts Provider automatically sets this value.
     * This is ignored for insert, update, and delete functions.
     */
    val displayNamePrimary: String?

    /**
     * An alternative representation of the display name, such as "family name first" instead of
     * "given name first" for Western names. If an alternative is not available, the values should
     * be the same as [displayNamePrimary].
     *
     * This is a read-only attribute as the Contacts Provider automatically sets this value.
     * This is ignored for insert, update, and delete functions.
     */
    val displayNameAlt: String?

    /**
     * Timestamp of when this contact was last updated. This includes updates to all data associated
     * with this contact including raw contacts. Any modification (including deletes and inserts) of
     * underlying contact data are also reflected in this timestamp.
     *
     * This is a read-only attribute as the Contacts Provider automatically sets this value.
     * This is ignored for insert, update, and delete functions.
     */
    val lastUpdatedTimestamp: Date?

    /**
     * Contains options for this contact and all of the [RawContact]s associated with it (not
     * limited to the [rawContacts] in this instance).
     *
     * Changes to the options of a RawContact may affect the options of the parent Contact. On the
     * other hand, changes to the options of the parent Contact will be propagated to all child
     * RawContact options.
     *
     * This options instance will be ignored for update operations. Use the ContactOptions extension
     * functions to modify options or get the most up-to-date options.
     */
    val options: Options?

    /**
     * The uri to the full-sized image of this contact. This full sized image is from the associated
     * [RawContact] of the ContactsProvider's choosing. This may be the same as the
     * [photoThumbnailUri] if a full sized photo is not available.
     *
     * To get the latest photo as an InputStream/Bytes/Bitmap/BitmapDrawable or set or remove photo,
     * use the ContactPhoto extensions.
     */
    val photoUri: Uri?

    /**
     * The uri to the thumbnail-sized version of the [photoUri]. This thumbnail image is from the
     * associated [RawContact] of the ContactsProvider's choosing.
     *
     * To get the latest photo thumbnail as an InputStream/Bytes/Bitmap/BitmapDrawable or set or
     * remove photo thumbnail, use the ContactPhoto extensions.
     */
    val photoThumbnailUri: Uri?

    /**
     * True if this contact has at least one RawContact that has at least one phone number
     * **in the database Data table**.
     *
     * ## Note
     *
     * The phone number is the only kind of data that the ContactsContract provides with an indexed
     * value such as this. The ContactsContract does NOT provide things like "hasEmail",
     * "hasWebsite", etc.
     *
     * Regardless, this library provide functions to match contacts that "has at least one instance
     * of a kind of data". This [hasPhoneNumber] is not necessary to get contacts that have a phone
     * number. However, this does provide an easy way to get contacts that have no phone numbers
     * without having to make two queries.
     */
    val hasPhoneNumber: Boolean?

    // Blank Contacts only have RawContact(s) that are blank. Blank RawContacts do not have any rows
    // in the Data table. The attributes in this class (e.g. displayNamePrimary) are not columns of
    // the Data table, which is why they are not part of the blank check.
    override val isBlank: Boolean
        get() = entitiesAreAllBlank(rawContacts)
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary! That is when there are two separate concrete
 * types that we want to perform an operation on.
 *
 * Apart from ContactEntity, there is only one interface that extends it; ExistingContactEntity.
 * This interface is used for library functions that require a ContactEntity with an ID, which means
 * that it exists in the database. There are two variants of this; Contact and MutableContact.
 * With this, we can create functions (or extensions) that can take in (or have as the receiver)
 * either Contact or MutableContact through the ExistingContactEntity abstraction/facade.
 *
 * This is why there are no interfaces for NewContactEntity, ImmutableContactEntity, and
 * MutableContactEntity. There are currently no library functions that exist that need them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A [ContactEntity] that has already been inserted into the database.
 */
sealed interface ExistingContactEntity: ContactEntity, ExistingEntity {
    /**
     * The id of the Contacts row this represents.
     *
     * This is the value of Contacts._ID / RawContacts.CONTACT_ID / Data.CONTACT_ID
     */
    // Override for documentation purposes.
    override val id: Long

    /**
     * True if this contact represents the user's personal profile entry.
     */
    val isProfile: Boolean
        get() = id.isProfileId
}

/**
 * An existing immutable [ContactEntity].
 *
 * This contains an immutable list of existing immutable [RawContact]s.
 *
 * To get a mutable copy of this instance, use [mutableCopy].
 */
@Parcelize
data class Contact internal constructor(

    override val id: Long,

    override val rawContacts: List<RawContact>,

    override val displayNamePrimary: String?,
    override val displayNameAlt: String?,

    override val lastUpdatedTimestamp: Date?,

    override val options: Options?,

    override val photoUri: Uri?,
    override val photoThumbnailUri: Uri?,

    override val hasPhoneNumber: Boolean?

) : ExistingContactEntity, ImmutableEntityWithMutableType<MutableContact> {

    override fun mutableCopy() = MutableContact(
        id = id,

        rawContacts = rawContacts.asSequence().mutableCopies().toMutableList(),

        displayNamePrimary = displayNamePrimary,
        displayNameAlt = displayNameAlt,
        lastUpdatedTimestamp = lastUpdatedTimestamp,
        options = options,
        photoUri = photoUri,
        photoThumbnailUri = photoThumbnailUri,
        hasPhoneNumber = hasPhoneNumber
    )
}

/**
 * An existing mutable [ContactEntity].
 *
 * This contains an immutable list of existing [MutableRawContact]s.
 */
@Parcelize
data class MutableContact internal constructor(

    override val id: Long,

    override val rawContacts: List<MutableRawContact>,

    override val displayNamePrimary: String?,
    override val displayNameAlt: String?,

    override val lastUpdatedTimestamp: Date?,

    override val options: Options?,

    override val photoUri: Uri?,
    override val photoThumbnailUri: Uri?,

    override val hasPhoneNumber: Boolean?

) : ExistingContactEntity, MutableEntity

// Note that there is no "NewContact". A new "Contact" is created automatically by the Contacts
// Provider when a "NewRawContact" is inserted.
