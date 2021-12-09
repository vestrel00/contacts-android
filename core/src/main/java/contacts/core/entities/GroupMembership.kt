package contacts.core.entities

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
 */
// I know this interface is not necessary because there is only one implementation. Still, it does
// not hurt to have it. It follows the setup like everything else, so it's cool.
sealed interface GroupMembershipEntity : DataEntity {

    /**
     * The id of the Group in the Groups table that this membership refers to, which must share
     * the same account as the contact.
     *
     * This is a read-only attribute, which is ignored for insert, update, and delete functions.
     */
    val groupId: Long?

    override val mimeType: MimeType
        get() = MimeType.GroupMembership

    override val isBlank: Boolean
        get() = propertiesAreAllNullOrBlank(groupId)
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * This is why there are no interfaces for NewGroupMembershipEntity, ExistingGroupMembershipEntity,
 * ImmutableGroupMembershipEntity, and MutableNewGroupMembershipEntity. There are currently no
 * library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * An existing immutable [GroupMembership].
 */
@Parcelize
data class GroupMembership internal constructor(

    override val id: Long,
    override val rawContactId: Long,
    override val contactId: Long,

    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean,

    override val groupId: Long?

) : GroupMembershipEntity, ExistingDataEntity, ImmutableDataEntity