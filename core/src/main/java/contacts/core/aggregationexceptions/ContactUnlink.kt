package contacts.core.aggregationexceptions

import contacts.core.*
import contacts.core.entities.ExistingContactEntity
import contacts.core.util.unlinkDirect

/**
 * Unlinks (keep separate) a Contacts' RawContacts, resulting in one [ExistingContactEntity]
 * for each [ExistingContactEntity.rawContacts].
 *
 * This does nothing / fails if there is only one RawContact associated with a given Contact.
 *
 * **Profile Contact & RawContacts are not supported!** This operation will fail if a given
 * Contact is a Profile Contact.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. Unlinking will do nothing if these permissions are not granted.
 *
 * ## Usage
 *
 * To unlink an [ExistingContactEntity];
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = contactUnlink.contact(contact).commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * Result result = contactUnlink.contact(contact).commit();
 * ```
 */
interface ContactUnlink : CrudApi {

    /**
     * Sets the given [contact] to be unlinked on [commit].
     */
    fun contact(contact: ExistingContactEntity): ContactUnlink

    /**
     * Unlinks the given [ExistingContactEntity] (added via [contact]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Returns a redacted instance where all private user data are redacted.
     *
     * ## Redacted instances may produce invalid results!
     *
     * Redacted instance may have critical information redacted, which is required to make
     * the operation work properly.
     *
     * **Redacted operations should typically only be used for logging in production!**
     */
    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ContactUnlink

    interface Result : CrudApi.Result {

        /**
         * The list of RawContacts' IDs that have been unlinked. Empty if [isSuccessful] is false.
         */
        val rawContactIds: List<Long>

        /**
         * True if the unlink succeeded.
         */
        val isSuccessful: Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

internal fun ContactUnlink(contacts: Contacts): ContactUnlink = ContactUnlinkImpl(contacts)

private class ContactUnlinkImpl(
    override val contactsApi: Contacts,

    private var contact: ExistingContactEntity? = null,

    override val isRedacted: Boolean = false
) : ContactUnlink {

    override fun toString(): String =
        """
            ContactUnlink {
                contact: $contact
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ContactUnlink = ContactUnlinkImpl(
        contactsApi,

        // Redact contact.
        contact?.redactedCopy(),

        isRedacted = true
    )

    override fun contact(contact: ExistingContactEntity): ContactUnlink = apply {
        this.contact = contact
    }

    override fun commit(): ContactUnlink.Result {
        onPreExecute()

        return contact?.run {
            unlinkDirect(contactsApi)
                .redactedCopyOrThis(isRedacted)
                .also { onPostExecute(contactsApi, it) }
        } ?: ContactUnlinkFailed()
    }
}

internal class ContactUnlinkSuccess private constructor(
    override val rawContactIds: List<Long>,
    override val isRedacted: Boolean
) : ContactUnlink.Result {

    constructor(rawContactIds: List<Long>) : this(rawContactIds, false)

    override fun toString(): String =
        """
            ContactUnlink.Result {
                isSuccessful: $isSuccessful
                rawContactIds: $rawContactIds
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override val isSuccessful: Boolean = true

    override fun redactedCopy() = ContactUnlinkSuccess(
        rawContactIds = rawContactIds,
        isRedacted = true
    )
}

internal class ContactUnlinkFailed private constructor(override val isRedacted: Boolean) :
    ContactUnlink.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            ContactUnlink.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override val rawContactIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun redactedCopy() = ContactUnlinkFailed(true)
}