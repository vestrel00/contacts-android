package contacts.entities

import contacts.util.isProfileId
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * [Entity] in the Contacts table.
 */
sealed class ContactEntity : Entity {
    /**
     * The id of the Contacts row this represents.
     *
     * This is the value of Contacts._ID / RawContacts.CONTACT_ID / Data.CONTACT_ID
     */
    abstract override val id: Long?

    /**
     * A list of [RawContactEntity]s that are associated with this contact.
     *
     * This list is sorted by [RawContactEntity.id], which seems to be the sort order used by the
     * native Contacts app when displaying the linked RawContacts and when inserting new data for a
     * Contact with multiple linked RawContacts.
     */
    abstract val rawContacts: List<RawContactEntity>

    /**
     * The standard text shown as the contact's display name, based on the best available
     * information for the contact (for example, it might be the email address if the name is not
     * available). This may be null if the Contacts Provider cannot find a suitable display name
     * source to use.
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
    abstract val displayNamePrimary: String?

    /**
     * An alternative representation of the display name, such as "family name first" instead of
     * "given name first" for Western names. If an alternative is not available, the values should
     * be the same as [displayNamePrimary].
     *
     * This is a read-only attribute as the Contacts Provider automatically sets this value.
     * This is ignored for insert, update, and delete functions.
     */
    abstract val displayNameAlt: String?

    /**
     * Timestamp of when this contact was last updated. This includes updates to all data associated
     * with this contact including raw contacts. Any modification (including deletes and inserts) of
     * underlying contact data are also reflected in this timestamp.
     *
     * This is a read-only attribute as the Contacts Provider automatically sets this value.
     * This is ignored for insert, update, and delete functions.
     */
    abstract val lastUpdatedTimestamp: Date?

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
    abstract val options: Options?

    /* Intentionally not including these to ensure consumers obtain Contact photos the same way that
     * RawContact photos are obtained. The ContactPhoto extension functions ensures that only the
     * most up-to-date photos are exposed to consumers.

     * The uri to the full-sized image of this contact. This full sized image is from the associated
     * [RawContact] of the ContactsProvider's choosing. Note that the [RawContact] this photo
     * belongs to is not guaranteed to be in the [rawContacts] list depending on query filters.
    val photoUri: Uri?,

     * The uri to the thumbnail-sized version of the [photoUri]. This thumbnail image is from the
     * associated [RawContact] of the ContactsProvider's choosing. Note that the [RawContact] this
     * photo belongs to is not guaranteed to be in the [rawContacts] list depending on query
     * filters.
    val photoThumbnailUri: Uri?
     */

    /**
     * True if this contact represents the user's personal profile entry.
     */
    val isProfile: Boolean
        get() = id.isProfileId
}

/**
 * Contains contact data and [rawContacts] that are associated with this contact.
 *
 * ## [RawContact]
 *
 * A Contact may consist of one or more [RawContact]. A [RawContact] is an association between a
 * Contact and an [android.accounts.Account]. Each [RawContact] is associated with several pieces of
 * Data such as emails.
 *
 * The Contacts Provider may combine [RawContact] from several different Accounts. The same effect
 * is achieved when merging / linking multiple contacts. Instances of this class also provides
 * aggregate data from all [RawContact]s in the set of [rawContacts].
 */
@Parcelize
data class Contact internal constructor(

    /**
     * See [ContactEntity.id].
     */
    override val id: Long?,

    /**
     * See [ContactEntity.rawContacts].
     *
     * Notice that the type is [RawContact] instead of [RawContactEntity].
     */
    override val rawContacts: List<RawContact>,

    /**
     * See [ContactEntity.displayNamePrimary].
     */
    override val displayNamePrimary: String?,

    /**
     * See [ContactEntity.displayNameAlt].
     */
    override val displayNameAlt: String?,

    /**
     * See [ContactEntity.lastUpdatedTimestamp].
     */
    override val lastUpdatedTimestamp: Date?,

    /**
     * See [ContactEntity.options].
     */
    override val options: Options?

) : ContactEntity() {

    // Blank Contacts only have RawContact(s) that are blank. Blank RawContacts do not have any rows
    // in the Data table. The attributes in this class (e.g. displayNamePrimary) are not columns of
    // the Data table, which is why they are not part of the blank check.
    override val isBlank: Boolean
        get() = entitiesAreAllBlank(rawContacts)

    fun toMutableContact() = MutableContact(
        id = id,

        rawContacts = rawContacts.map { it.toMutableRawContact() },

        displayNamePrimary = displayNamePrimary,
        displayNameAlt = displayNameAlt,
        lastUpdatedTimestamp = lastUpdatedTimestamp,
        options = options
    )
}

/**
 * A mutable [Contact]. Well, nothing is really mutable here except for the [MutableRawContact] in
 * the immutable [rawContacts] list.
 */
@Parcelize
data class MutableContact internal constructor(

    /**
     * See [ContactEntity.id].
     */
    override val id: Long?,

    /**
     * See [ContactEntity.rawContacts].
     *
     * Notice that the type is [MutableRawContact] instead of [RawContactEntity].
     */
    override val rawContacts: List<MutableRawContact>,

    /**
     * See [ContactEntity.displayNamePrimary].
     */
    override val displayNamePrimary: String?,

    /**
     * See [ContactEntity.displayNameAlt].
     */
    override val displayNameAlt: String?,

    /**
     * See [ContactEntity.lastUpdatedTimestamp].
     */
    override val lastUpdatedTimestamp: Date?,

    /**
     * See [ContactEntity.options].
     */
    override val options: Options?

) : ContactEntity() {

    // Blank Contacts only have RawContact(s) that are blank. Blank RawContacts do not have any rows
    // in the Data table. The attributes in this class (e.g. displayNamePrimary) are not columns of
    // the Data table, which is why they are not part of the blank check.
    override val isBlank: Boolean
        get() = entitiesAreAllBlank(rawContacts)
}
