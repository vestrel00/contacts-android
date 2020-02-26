package com.vestrel00.contacts.util

import android.content.ContentProviderOperation
import android.content.Context
import android.os.Build
import android.provider.ContactsContract.*
import android.provider.ContactsContract.Contacts
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.INVALID_ID
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableName
import com.vestrel00.contacts.entities.cursor.NameCursor
import com.vestrel00.contacts.entities.mapper.NameMapper
import com.vestrel00.contacts.entities.table.Table

/**
 * Links [this] Contact with the given [contact]. This will aggregate all RawContacts belonging to
 * [this] Contact and the given [contact] into a single Contact. Aggregation is done by the
 * Contacts Provider. For example,
 *
 * - Contact (id: 1, display name: A)
 *     - RawContact A
 * - Contact (id: 2, display name: B)
 *     - RawContact B
 *     - RawContact C
 *
 * Linking Contact 1 with Contact 2 results in;
 *
 * - Contact (id: 1, display name: A)
 *     - RawContact A
 *     - RawContact B
 *     - RawContact C
 *
 * Contact 2 no longer exists and all of the Data belonging to RawContact B and C are now associated
 * with Contact 1.
 *
 * If instead Contact 2 is linked with Contact 1;
 *
 * - Contact (id: 1, display name: B)
 *     - RawContact A
 *     - RawContact B
 *     - RawContact C
 *
 * The same thing occurs except the display name has been set to the display name of RawContact B.
 *
 * This function only instructs the Contacts Provider which RawContacts should be aggregated to a
 * single Contact. Details on how RawContacts are aggregated into a single Contact are left to the
 * Contacts Provider.
 *
 * ## Contact Display Name Resolution
 *
 * There is one thing that the native Contacts app manually does that the Contacts Provider does not
 * do automatically; setting the display name for the aggregated Contact. The native Contacts app
 * sets the name of [this] as the "default" (if available) and clears the default status of all
 * other names belonging to the other RawContacts. If [this] does not have any names available,
 * then a name belonging to the other [contact] will be set as default.
 *
 * The Contacts Provider automatically sets the Contact display name to the default name that
 * belongs to any associated RawContact.
 *
 * The native Contacts app also sets the most recently updated name as the default at every update
 * (and new Contact creation). This results in the Contact display name changing to the most
 * recently updated name from one of the associated RawContacts.
 *
 * ## Permissions
 *
 * The [com.vestrel00.contacts.ContactsPermissions.WRITE_PERMISSION] is required.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// TODO vararg, collection functions
// TODO ContactLinksAsync and LinkResultAsync
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun Contact.link(context: Context, contacts: Sequence<Contact>): ContactLinkResult {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || id == INVALID_ID) {
        return ContactLinkFailed
    }

    val sortedContactIds = contacts
        .map { it.id }
        .filter { it != this.id && it != INVALID_ID }
        .sortedBy { it }
        .toMutableList()

    // Insert [this] as the first contact in the list to ensure it is the first choice in display
    // name resolution.
    sortedContactIds.add(0, this.id)

    if (sortedContactIds.size < 2) {
        // At least 2 Contacts is required to link.
        return ContactLinkFailed
    }

    val prioritizedContactIds = sortedContactIds.toSet()

    val sortedRawContactIds = sortedRawContactIds(context, prioritizedContactIds)

    if (sortedRawContactIds.size < 2) {
        // At least 2 RawContacts is required to link.
        return ContactLinkFailed
    }

    val nameRowIdToUseAsDefault = nameRowIdToUseAsDefault(context, prioritizedContactIds)

    try {
        // Note that the result uri is null. There is no meaningful information we can get here.
        context.contentResolver.applyBatch(
            AUTHORITY,
            linkRawContactsOperations(sortedRawContactIds)
        )
    } catch (exception: Exception) {
        return ContactLinkFailed
    }

    // Link succeeded. Set the default name.
    val name = nameWithId(context, nameRowIdToUseAsDefault)
    name?.setAsDefault(context)

    // Get the new Contact id of the RawContacts from the queried name. If no name is found,
    // query it.
    val contactId = name?.contactId ?: contactIdOfRawContact(context, sortedRawContactIds.first())

    return ContactLinkSuccess(contactId)
}

interface ContactLinkResult {

    /**
     * The parent [Contact.id] for all of the linked RawContacts. Null if [isSuccessful] is false.
     */
    val contactId: Long?

    /**
     * True if the link succeeded.
     */
    val isSuccessful: Boolean
}

private class ContactLinkSuccess(override val contactId: Long?) : ContactLinkResult {

    override val isSuccessful: Boolean = true
}

private object ContactLinkFailed : ContactLinkResult {

    override val contactId: Long? = null

    override val isSuccessful: Boolean = false
}

/**
 * Provides the operations to ensure that all or the given raw contacts are linked.
 *
 * See DEV_NOTES "AggregationExceptions table" section.
 */
private fun linkRawContactsOperations(sortedRawContactIds: List<Long>):
        ArrayList<ContentProviderOperation> = arrayListOf<ContentProviderOperation>().apply {

    for (i in 0 until (sortedRawContactIds.size - 1)) {
        for (j in (i + 1) until sortedRawContactIds.size) {

            val rawContactId1 = sortedRawContactIds[i]
            val rawContactId2 = sortedRawContactIds[j]

            val operation = ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI)
                .withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER)
                .withValue(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1)
                .withValue(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2)
                .build()

            add(operation)
        }
    }
}

/**
 * Returns the name row id pre-link of the name that will be used as the default post-link. This
 * goes through the set of [contactIds] in order. For each contact, this attempts to find the name
 * row of the raw contact specified by NAME_RAW_CONTACT_ID. If not found, the default or most
 * recently updated name is used. Repeat this process for all subsequent contacts until a name
 * row is found.
 *
 * Returns [INVALID_ID] if no name row is found.
 */
private fun nameRowIdToUseAsDefault(context: Context, contactIds: Set<Long>): Long {

    var nameRowIdToUseAsDefault = INVALID_ID

    for (contactId in contactIds) {
        nameRowIdToUseAsDefault = nameRawContactIdStructuredNameId(context, contactId)
        if (nameRowIdToUseAsDefault == INVALID_ID) {
            nameRowIdToUseAsDefault = defaultOrMostRecentlyUpdatedNameId(context, contactId)
        }

        if (nameRowIdToUseAsDefault != INVALID_ID) {
            break
        }
    }

    return nameRowIdToUseAsDefault
}

/**
 * Returns the structured name row ID of the RawContact referenced by the
 * [Contacts.NAME_RAW_CONTACT_ID] of the Contact with the given [contactId].
 *
 * Returns [INVALID_ID] if the [Contacts.DISPLAY_NAME_SOURCE] is not
 * [DisplayNameSources.STRUCTURED_NAME] or if the name row is not found.
 */
private fun nameRawContactIdStructuredNameId(context: Context, contactId: Long): Long {
    val nameRawContactId = nameRawContactId(context, contactId)

    if (nameRawContactId == INVALID_ID) {
        return INVALID_ID
    }

    val cursor = context.contentResolver.query(
        Table.DATA.uri,
        arrayOf(Fields.Id.columnName),
        "${(Fields.RawContactId equalTo nameRawContactId)
                and (Fields.MimeType equalTo MimeType.NAME)}",
        null,
        null
    )

    var nameRowId: Long = INVALID_ID
    if (cursor != null && cursor.moveToNext()) {
        nameRowId = cursor.getLong(0)

        cursor.close()
    }

    return nameRowId
}

/**
 * Returns the [Contacts.NAME_RAW_CONTACT_ID] of the Contact with the given [contactId].
 *
 * Returns [INVALID_ID] if the [Contacts.DISPLAY_NAME_SOURCE] is not
 * [DisplayNameSources.STRUCTURED_NAME].
 */
private fun nameRawContactId(context: Context, contactId: Long): Long {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        // Contacts.NAME_RAW_CONTACT_ID is not available
        // TODO Check behavior in API 19
        return INVALID_ID
    }

    val cursor = context.contentResolver.query(
        Table.CONTACTS.uri,
        arrayOf(Contacts.DISPLAY_NAME_SOURCE, Contacts.NAME_RAW_CONTACT_ID),
        "${Fields.Contacts.Id equalTo contactId}",
        null,
        null
    )

    var displayNameSource: Int = DisplayNameSources.UNDEFINED
    var nameRawContactId: Long = INVALID_ID

    if (cursor != null && cursor.moveToNext()) {
        displayNameSource = cursor.getInt(0)
        nameRawContactId = cursor.getLong(1)

        cursor.close()
    }

    return if (displayNameSource != DisplayNameSources.STRUCTURED_NAME) {
        INVALID_ID
    } else {
        nameRawContactId
    }
}

/**
 * Returns the default name row ID of the Contact with the given [contactId]. If there is no default
 * name, then the most recently updated name is returned. Otherwise, [INVALID_ID].
 */
private fun defaultOrMostRecentlyUpdatedNameId(context: Context, contactId: Long): Long {
    val cursor = context.contentResolver.query(
        Table.DATA.uri,
        arrayOf(Fields.Id.columnName),
        "${(Fields.Contact.Id equalTo contactId)
                and (Fields.MimeType equalTo MimeType.NAME)}",
        null,
        "${Data.IS_SUPER_PRIMARY} DESC, ${Data.DATA_VERSION} DESC LIMIT 1"
    )

    var nameRowId: Long = INVALID_ID
    if (cursor != null && cursor.moveToNext()) {
        nameRowId = cursor.getLong(0)

        cursor.close()
    }

    return nameRowId
}

/**
 * Returns the RawContact IDs of the Contacts with the given [contactIds] in ascending order.
 */
private fun sortedRawContactIds(context: Context, contactIds: Set<Long>): List<Long> {
    val cursor = context.contentResolver.query(
        Table.RAW_CONTACTS.uri,
        arrayOf(Fields.RawContact.Id.columnName),
        "${Fields.RawContact.ContactId `in` contactIds}",
        null,
        Fields.RawContact.Id.columnName
    )

    return mutableListOf<Long>().apply {
        if (cursor != null) {
            while (cursor.moveToNext()) {
                add(cursor.getLong(0))
            }

            cursor.close()
        }
    }
}

private fun nameWithId(context: Context, nameRowId: Long): MutableName? {
    if (nameRowId == INVALID_ID) {
        return null
    }

    val cursor = context.contentResolver.query(
        Table.DATA.uri,
        Include(Fields.Required).columnNames,
        "${Fields.Id equalTo nameRowId}",
        null,
        null
    )

    var name: MutableName? = null
    if (cursor != null && cursor.moveToNext()) {
        name = NameMapper(NameCursor(cursor)).name

        cursor.close()
    }

    return name
}

private fun contactIdOfRawContact(context: Context, rawContactId: Long): Long? {
    val cursor = context.contentResolver.query(
        Table.RAW_CONTACTS.uri,
        arrayOf(Fields.RawContact.ContactId.columnName),
        "${Fields.RawContact.Id equalTo rawContactId}",
        null,
        null
    )

    var contactId: Long? = null
    if (cursor != null) {
        contactId = cursor.getLong(0)

        cursor.close()
    }

    return contactId
}

// TODO MutableContact link
// TODO unlink