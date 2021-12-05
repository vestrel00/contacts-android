package contacts.core.util

import contacts.core.*
import contacts.core.entities.MutableOptions
import contacts.core.entities.Options
import contacts.core.entities.RawContactEntity
import contacts.core.entities.mapper.rawContactsOptionsMapper
import contacts.core.entities.operation.OptionsOperation
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table

/**
 * Returns the [Options] of this [RawContactEntity].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContactEntity.options(contacts: Contacts): Options {
    val rawContactId = id

    if (!contacts.permissions.canQuery() || rawContactId == null) {
        return Options()
    }

    return contacts.applicationContext.contentResolver.query(
        if (isProfile) ProfileUris.RAW_CONTACTS.uri else Table.RawContacts.uri,
        Include(RawContactsFields.Options),
        RawContactsFields.Id equalTo rawContactId
    ) {
        it.getNextOrNull { it.rawContactsOptionsMapper().value }
    } ?: Options()
}

/**
 * Updates this [RawContactEntity.options] with the given [options].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Changes are immediate
 *
 * This function will make the changes to the Contacts Provider database immediately. You do not
 * need to use update APIs to commit the changes.
 *
 * ## Changes are not applied to the receiver
 *
 * This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
 * query APIs or refresh extensions or process the result of this function call to get the most
 * up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
 * Provider database.
 *
 * ## Starred in Android and group membership to the favorites group
 *
 * When a Contact is starred, the Contacts Provider automatically adds a group membership to the
 * favorites group for all RawContacts linked to the Contact. Setting starred to false removes all
 * group memberships to the favorites group.
 *
 * If the RawContact is not associated with an Account, then no group memberships that are created.
 *
 * The starred option is interdependent with group memberships to the favorites group. Adding a
 * group membership to the favorites group results in starred being set to true. Removing the
 * membership sets it to false.
 *
 * RawContacts that are not associated with an account do not have any group memberships. Even
 * though these raw contacts may not have a membership to the favorites group, they may still be
 * "starred" (favorited), which is not dependent on the existence of a favorites group membership.
 *
 * **Refresh RawContact instances after changing the starred value.** Otherwise, performing an
 * update on the RawContact with a stale set of group memberships may revert the star/unstar
 * operation. For example, query returns a starred RawContact -> set starred to false -> update
 * RawContact (still containing a group membership to the favorites group) -> starred will be set
 * back to true.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContactEntity.setOptions(contacts: Contacts, options: MutableOptions): Boolean {
    val rawContactId = id

    if (!contacts.permissions.canUpdateDelete() || rawContactId == null) {
        return false
    }

    return contacts.applicationContext.contentResolver.applyBatch(
        OptionsOperation().updateRawContactOptions(rawContactId, options)
    ) != null
}

/**
 * Updates this [RawContactEntity.options] in [update]. If this contact has null options, a new
 * blank options will be used in [update].
 *
 * Note that changes to the options of a RawContact may affect the options of the parent Contact.
 * On the other hand, changes to the options of the parent Contact will be propagated to all child
 * RawContact options.
 *
 * Supports profile and non-profile RawContacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
 *
 * ## Changes are immediate
 *
 * This function will make the changes to the Contacts Provider database immediately. You do not
 * need to use update APIs to commit the changes.
 *
 * ## Changes are not applied to the receiver
 *
 * This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
 * query APIs or refresh extensions or process the result of this function call to get the most
 * up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
 * Provider database.
 *
 * ## Starred in Android and group membership to the favorites group
 *
 * When a Contact is starred, the Contacts Provider automatically adds a group membership to the
 * favorites group for all RawContacts linked to the Contact. Setting starred to false removes all
 * group memberships to the favorites group.
 *
 * If the RawContact is not associated with an Account, then no group memberships that are created.
 *
 * The starred option is interdependent with group memberships to the favorites group. Adding a
 * group membership to the favorites group results in starred being set to true. Removing the
 * membership sets it to false.
 *
 * RawContacts that are not associated with an account do not have any group memberships. Even
 * though these raw contacts may not have a membership to the favorites group, they may still be
 * "starred" (favorited), which is not dependent on the existence of a favorites group membership.
 *
 * **Refresh RawContact instances after changing the starred value.** Otherwise, performing an
 * update on the RawContact with a stale set of group memberships may revert the star/unstar
 * operation. For example, query returns a starred RawContact -> set starred to false -> update
 * RawContact (still containing a group membership to the favorites group) -> starred will be set
 * back to true.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun RawContactEntity.updateOptions(contacts: Contacts, update: MutableOptions.() -> Unit): Boolean {
    val mutableOptions = options(contacts).mutableCopy()
    mutableOptions.update()
    return setOptions(contacts, mutableOptions)
}