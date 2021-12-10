package contacts.core.entities.cursor

/**
 * Provides the Contact Id. This abstraction is useful because the Contact Id column name differs
 * across tables;
 *
 * - Contacts table: "_id"
 * - RawContacts table: "contact_id"
 * - Data table: "contact_id"
 *
 * This should be used when using a cursor that could have been from the any of the above tables.
 */
internal sealed interface ContactIdCursor {
    val contactId: Long
}