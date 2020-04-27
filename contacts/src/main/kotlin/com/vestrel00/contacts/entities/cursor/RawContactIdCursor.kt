package com.vestrel00.contacts.entities.cursor

/**
 * Provides the RawContact Id. This abstraction is useful because the RawContact Id column name
 * differs across tables;
 *
 * - RawContacts table: "_id"
 * - Data table: "raw_contact_id"
 *
 * This should be used when using a cursor that could have been from the any of the above tables.
 *
 * This inherits from [ContactIdCursor] because these cursors also have access to the Contact Id.
 */
internal interface RawContactIdCursor : ContactIdCursor {
    val rawContactId: Long?
}