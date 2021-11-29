package contacts.core.entities

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * A membership to a group.
 *
 * A RawContact may have 0, 1, or more entries of this data kind.
 *
 * Local RawContacts (those that are not associated with an Account) **should not** have any entries
 * of this data kind.
 *
 * ## Notes
 *
 * In more recent versions of Android, the native Contacts app presents group memberships
 * in the UI as "labels".
 *
 * There is no mutable version of a group membership. To make modifications to group memberships,
 * set the group memberships in the MutableRawContact. To select a set of group memberships, use
 * the [contacts.core.groups.GroupsQuery] with the same account as the RawContact and convert the
 * desired groups to group memberships via the functions in [contacts.core.util.toGroupMembership].
 * Then, perform an update operation on the MutableRawContact.
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class GroupMembership internal constructor(

    override val id: Long?,

    override val rawContactId: Long?,

    override val contactId: Long?,

    override val isPrimary: Boolean,

    override val isSuperPrimary: Boolean,

    /**
     * The id of the Group in the Groups table that this membership refers to, which must share
     * the same account as the contact.
     *
     * This is a read-only attribute, which is ignored for insert, update, and delete functions.
     */
    val groupId: Long?

) : ImmutableData {

    @IgnoredOnParcel
    override val mimeType: MimeType = MimeType.GroupMembership

    @IgnoredOnParcel
    override val isBlank: Boolean = propertiesAreAllNullOrBlank(groupId)
}