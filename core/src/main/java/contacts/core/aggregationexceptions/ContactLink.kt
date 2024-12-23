package contacts.core.aggregationexceptions

import contacts.core.*
import contacts.core.entities.ExistingContactEntity
import contacts.core.util.linkDirect

/**
 * Links (keep together) Contacts. This will aggregate all RawContacts belonging to the given
 * Contacts into a single Contact.
 *
 * Aggregation is done by the Contacts Provider. For example,
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
 * Contact 2 no longer exists and all of the RawContacts associated with Contact 2 are now
 * associated with Contact 1.
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
 * This API only instructs the Contacts Provider which RawContacts should be aggregated to a
 * single Contact. Details on how RawContacts are aggregated into a single Contact are left to the
 * Contacts Provider.
 *
 * **Profile Contact/RawContacts are not supported!** This operation will fail if there are any
 * profile Contact/RawContacts in [contacts].
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. Linking will do nothing if these permissions are not granted.
 *
 * ## Usage
 *
 * To link a set of [ExistingContactEntity];
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = contactLink.contacts(contacts).commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * Result result = contactLink.contacts(contacts).commit();
 * ```
 */
interface ContactLink : CrudApi {

    /**
     * Adds the given [contacts] to the link queue, which will be linked on [commit].
     */
    fun contacts(vararg contacts: ExistingContactEntity): ContactLink

    /**
     * See [ContactLink.contacts].
     */
    fun contacts(contacts: Collection<ExistingContactEntity>): ContactLink

    /**
     * See [ContactLink.contacts].
     */
    fun contacts(contacts: Sequence<ExistingContactEntity>): ContactLink

    /**
     * Links all of the [ExistingContactEntity]s in the queue (added via [contacts]) and returns
     * the [Result].
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
    override fun redactedCopy(): ContactLink

    interface Result : CrudApi.Result {

        /**
         * The parent [ExistingContactEntity.id] for all of the linked RawContacts. Null if
         * [isSuccessful] is false.
         */
        val contactId: Long?

        /**
         * True if the link succeeded.
         */
        val isSuccessful: Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

internal fun ContactLink(contacts: Contacts): ContactLink = ContactLinkImpl(contacts)

private class ContactLinkImpl(
    override val contactsApi: Contacts,

    private val contacts: MutableSet<ExistingContactEntity> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : ContactLink {

    override fun toString(): String =
        """
            ContactLink {
                contacts: $contacts
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ContactLink = ContactLinkImpl(
        contactsApi,

        // Redact contacts.
        contacts.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun contacts(vararg contacts: ExistingContactEntity) = contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<ExistingContactEntity>) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<ExistingContactEntity>): ContactLink = apply {
        this.contacts.addAll(contacts.redactedCopiesOrThis(isRedacted))
    }

    override fun commit(): ContactLink.Result {
        onPreExecute()

        return contacts
            .linkDirect(contactsApi)
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

internal class ContactLinkSuccess private constructor(
    override val contactId: Long,
    override val isRedacted: Boolean
) : ContactLink.Result {

    constructor(contactId: Long) : this(contactId, false)

    override fun toString(): String =
        """
            ContactLink.Result {
                isSuccessful: $isSuccessful
                contactId: $contactId
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override val isSuccessful: Boolean = true

    override fun redactedCopy() = ContactLinkSuccess(
        contactId = contactId,
        isRedacted = true
    )
}

internal class ContactLinkFailed private constructor(override val isRedacted: Boolean) :
    ContactLink.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            ContactLink.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override val contactId: Long? = null

    override val isSuccessful: Boolean = false

    override fun redactedCopy() = ContactLinkFailed(true)
}