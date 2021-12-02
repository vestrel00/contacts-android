package contacts.core.entities.cursor

/**
 * Provides the Data Id.
 *
 * This inherits from [RawContactIdCursor] because these cursors also have access to the Contact Id
 * and RawContact Id.
 */
internal sealed interface DataIdCursor : RawContactIdCursor {
    val dataId: Long?
}