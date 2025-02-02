package contacts.core.entities

import android.net.Uri
import contacts.core.redactedCopies
import contacts.core.util.isProfileId
import kotlinx.parcelize.Parcelize
import java.util.Date

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
     * This list is sorted by RawContact id, which seems to be the sort order used by the AOSP
     * Contacts app when displaying the linked RawContacts and when inserting new data for a Contact
     * with multiple linked RawContacts.
     */
    val rawContacts: List<RawContactEntity>

    /**
     * The standard text shown as the contact's display name, based on the best available
     * information for the contact (for example, it might be the email address if the name is not
     * available). This may be null if the Contacts Provider cannot find a suitable display name
     * source to use.
     *
     * This is the contact name displayed by the AOSP Contacts app when viewing a contact. If a
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
     * Display name sources are specified in `ContactsContract.DisplayNameSources`. In order of
     * increasing priority; [Email], [Phone], [Organization], [Nickname], and [Name].
     *
     * ## Display name may change upon data updates
     *
     * The AOSP Contacts app also sets the most recently updated name as the default at every update
     * (and new Contact creation). This results in the Contact display name changing to the most
     * recently updated name from one of the associated RawContacts. The "most recently updated name"
     * is the name field that was last updated by the user when editing in the Contacts app, which is
     * irrelevant to its value. It does not matter if the user deleted the last character of the name,
     * added back the same character (undo), and then saved. It still counts as the most recently
     * updated. This logic is not implemented in this library. It is up to the consumers to implement it
     * or not, or do it differently.
     *
     * ## [ContactEntity.displayNamePrimary] vs [ExistingRawContactEntity.displayNamePrimary]
     *
     * The [ContactEntity.displayNamePrimary] holds the same value as **one of its** constituent
     * RawContacts.
     */
    val displayNamePrimary: String?

    /**
     * An alterAOSP representation of the display name, such as "family name first" instead of
     * "given name first" for Western names. If an alterAOSP is not available, the values should
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
     * The [OptionsEntity] for this contact.
     *
     * ## [ContactEntity.options] vs [RawContactEntity.options]
     *
     * Changes to the options of a RawContact may affect the options of the parent Contact. On the
     * other hand, changes to the options of the parent Contact will be propagated to all child
     * RawContact options.
     */
    val options: OptionsEntity?

    /**
     * The uri to the full-sized image of this contact. This full sized image is from the associated
     * [RawContact] of the ContactsProvider's choosing. This may be the same as the
     * [photoThumbnailUri] if a full sized photo is not available.
     */
    val photoUri: Uri?

    /**
     * The uri to the thumbnail-sized version of the [photoUri]. This thumbnail image is from the
     * associated [RawContact] of the ContactsProvider's choosing.
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
        get() = propertiesAreAllNullOrBlank(rawContacts)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ContactEntity

    /**
     * True if this contact represents the user's personal profile entry.
     */
    val isProfile: Boolean
        get() = false
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from ContactEntity, there is only one interface that extends it; ExistingContactEntity.
 * This interface is used for library functions that require a ContactEntity with an ID, which means
 * that it exists in the database. There are two variants of this; Contact and MutableContact.
 * With this, we can create functions (or extensions) that can take in (or have as the receiver)
 * either Contact or MutableContact through the ExistingContactEntity abstraction/facade.
 *
 * This is why there are no interfaces for NewContactEntity, ImmutableContactEntity, and
 * MutableContactEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 * 
 * DISCLAIMER: Okay, so technically ExistingContactEntity is not necessary. The ContactEntity only
 * has one inheritor, which is ExistingContactEntity. Currently, implementations are "exising".
 * We'll make an exception here as it provides a standard structure that matches RawContactEntity 
 * and ExistingRawContactEntity. Furthermore, it gives us flexibility to create other entities such 
 * as NewContactEntity and MockContactEntity in the future if we ever need it. This may be violating 
 * YAGNI but it also does not hurt at all to have things that we way they are currently. It just 
 * gives us options and protects consumers from big refactors.
 */

/**
 * A [ContactEntity] that has already been inserted into the database.
 */
sealed interface ExistingContactEntity : ContactEntity, ExistingEntity {

    /**
     * A list of [ExistingRawContactEntity]s that are associated with this contact.
     *
     * This list is sorted by [ExistingRawContactEntity.id], which seems to be the sort order used
     * by the AOSP Contacts app when displaying the linked RawContacts and when inserting new data
     * for a Contact with multiple linked RawContacts.
     */
    override val rawContacts: List<ExistingRawContactEntity>

    /**
     * The id of the Contacts row this represents.
     *
     * This is the value of Contacts._ID / RawContacts.CONTACT_ID / Data.CONTACT_ID
     */
    // Override for documentation purposes.
    override val id: Long

    /**
     * The unique identifier for an aggregate contact in the **local and remote** databases_.
     * These look like randomly generated or hashed strings. For example; `2059i4a27289d88a0a4e7`,
     * `0r62-2A2C2E`, ...
     *
     * Unlike the Contact ID, the lookup key is the same across devices (for contacts that are
     * associated with an Account and are synced). The lookup key points to a person entity rather
     * than just a row in a table. It is the unique identifier used by local and remote sync
     * adapters to identify an aggregate contact.
     *
     * ## When to use Contact lookup key vs Contact ID?
     *
     * Use the **Contact lookup key** when you need to save a reference to a Contact that you want
     * to fetch after some period of time.
     *
     * - Creating and loading shortcuts.
     * - Saving/restoring activity/fragment instance state.
     * - Saving to an external database, preferences, or files.
     *
     * Use the **Contact ID** for everything else.
     *
     * - Performing read/write operations in the same function call or session in your app.
     * - Performing read/write operations that require ID (e.g. Contact photo and options).
     *
     * The reason why lookup keys are used as long-term links to contacts is because Contact IDs
     * and the lookup keys themselves may change over time due to linking/unlinking, local contact
     * updates, and syncing adapter operations. Lookup keys provide the ability to retrieve contacts
     * even when its ID or lookup key itself changes.
     *
     * ## The [lookupKey] vs [RawContactEntity.sourceId]
     *
     * RawContacts do not have a lookup key. It is exclusive to Contacts. However, RawContacts
     *  associated with an Account that have a SyncAdapter typically have a non-null value in the
     * [RawContactEntity.sourceId], which is typically used as **part** of the
     * parent [ExistingContactEntity.lookupKey]. For example, a RawContact that has a sourceId of
     * 6f5de8460f7f227e belongs to a Contact that has a lookup key of 2059i6f5de8460f7f227e. Notice
     * that the value of the sourceId is not exactly the same as the value of the lookup key!
     */
    val lookupKey: String?

    /**
     * Photo file ID of the full-size photo.
     *
     * This is for advanced usage only. Used to resolve the [primaryPhotoHolder].
     */
    val photoFileId: Long?

    /**
     * The RawContact that owns the photo referenced by [photoUri] and [photoThumbnailUri].
     *
     * This is useful if you are showing the Contact photo and the primary photo holder (a
     * RawContact) photo in the same screen.
     *
     * To properly resolve this value, this Contact instance must have come from a query that
     * included all fields in [contacts.core.Fields.PrimaryPhotoHolder]. Otherwise, may return
     * the incorrect [ExistingRawContactEntity].
     */
    val primaryPhotoHolder: ExistingRawContactEntity?

    /**
     * True if this contact represents the user's personal profile entry.
     */
    override val isProfile: Boolean
        get() = id.isProfileId

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ExistingContactEntity
}

/**
 * An existing immutable [ContactEntity].
 *
 * This contains an immutable list of existing immutable [RawContact]s.
 *
 * To get a mutable copy of this instance, use [mutableCopy].
 */
@ConsistentCopyVisibility
@Parcelize
data class Contact internal constructor(

    override val id: Long,
    override val lookupKey: String?,

    override val rawContacts: List<RawContact>,

    override val displayNamePrimary: String?,
    override val displayNameAlt: String?,

    override val lastUpdatedTimestamp: Date?,

    override val options: Options?,

    override val photoFileId: Long?,
    override val photoUri: Uri?,
    override val photoThumbnailUri: Uri?,

    override val hasPhoneNumber: Boolean?,

    override val isRedacted: Boolean

) : ExistingContactEntity, ImmutableEntityWithMutableType<MutableContact> {

    override val primaryPhotoHolder: RawContact?
        get() = rawContacts.find { photoFileId == it.photo?.fileId } ?: rawContacts.firstOrNull()

    override fun mutableCopy() = MutableContact(
        id = id,
        lookupKey = lookupKey,

        rawContacts = rawContacts.asSequence().mutableCopies().toMutableList(),

        displayNamePrimary = displayNamePrimary,
        displayNameAlt = displayNameAlt,

        lastUpdatedTimestamp = lastUpdatedTimestamp,

        options = options?.mutableCopy(),

        photoFileId = photoFileId,
        photoUri = photoUri,
        photoThumbnailUri = photoThumbnailUri,

        hasPhoneNumber = hasPhoneNumber,

        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        rawContacts = rawContacts.redactedCopies(),

        displayNamePrimary = displayNamePrimary?.redact(),
        displayNameAlt = displayNameAlt?.redact(),
        options = options?.redactedCopy(),
    )
}

/**
 * An existing mutable [ContactEntity].
 *
 * This contains an immutable list of existing [MutableRawContact]s.
 */
@ConsistentCopyVisibility
@Parcelize
data class MutableContact internal constructor(

    override val id: Long,
    override val lookupKey: String?,

    override val rawContacts: List<MutableRawContact>,

    override val displayNamePrimary: String?,
    override val displayNameAlt: String?,

    override val lastUpdatedTimestamp: Date?,

    override var options: MutableOptionsEntity?,

    override val photoFileId: Long?,
    override val photoUri: Uri?,
    override val photoThumbnailUri: Uri?,

    override val hasPhoneNumber: Boolean?,

    override val isRedacted: Boolean

) : ExistingContactEntity, MutableEntity {

    override val primaryPhotoHolder: MutableRawContact?
        get() = rawContacts.find { photoFileId == it.photo?.fileId } ?: rawContacts.firstOrNull()

    override fun redactedCopy() = copy(
        isRedacted = true,

        rawContacts = rawContacts.redactedCopies(),

        displayNamePrimary = displayNamePrimary?.redact(),
        displayNameAlt = displayNameAlt?.redact(),
        options = options?.redactedCopy(),
    )
}

// Note that there is no "NewContact". A new "Contact" is created automatically by the Contacts
// Provider when a "NewRawContact" is inserted.
