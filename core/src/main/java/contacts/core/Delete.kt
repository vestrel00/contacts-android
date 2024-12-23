package contacts.core

import android.content.ContentProviderOperation
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.operation.RawContactsOperation
import contacts.core.util.*

/**
 * Deletes one or more raw contacts or contacts from the raw contacts and contacts tables
 * respectively. All associated data rows are also deleted.
 *
 * ## RawContact deletion is not guaranteed to be immediate
 *
 * **RawContacts may not immediately be deleted**. They are marked for deletion and get deleted in
 * the background by the Contacts Provider depending on sync settings and network availability.
 *
 * Contacts of RawContacts that are marked for deletion are immediately deleted!
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * ## Usage
 *
 * To delete a [ExistingContactEntity] and all associated [ExistingRawContactEntity]s;
 *
 * ```kotlin
 * val result = delete.contacts(contact).commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * Delete.Result result = delete.contacts(contact).commit();
 * ```
 */
interface Delete : CrudApi {

    /**
     * Adds the given [rawContacts] to the delete queue, which will be deleted on [commit].
     *
     * ## Note
     *
     * Deleting all [ExistingRawContactEntity]s of a [ExistingContactEntity] will result in the
     * deletion of the associated [ExistingContactEntity]! However, the [ExistingContactEntity]
     * will remain as long as it has at least has one associated [ExistingRawContactEntity].
     */
    fun rawContacts(vararg rawContacts: ExistingRawContactEntity): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): Delete

    /**
     * Adds the given [rawContactsIds] to the delete queue, which will be deleted on [commit].
     *
     * ## Note
     *
     * Deleting all RawContacts of an existing Contact will result in the deletion of the
     * associated Contact! However, the Contact will remain as long as it has at least has one
     * associated RawContact.
     */
    fun rawContactsWithId(vararg rawContactsIds: Long): Delete

    /**
     * See [Delete.rawContactsWithId].
     */
    fun rawContactsWithId(rawContactsIds: Collection<Long>): Delete

    /**
     * See [Delete.rawContactsWithId].
     */
    fun rawContactsWithId(rawContactsIds: Sequence<Long>): Delete

    /**
     * Deletes all of the RawContacts that match the given [where].
     */
    fun rawContactsWhere(where: Where<RawContactsField>?): Delete

    /**
     * Same as [Delete.rawContactsWhere] except you have direct access to all properties of
     * [RawContactsFields] in the function parameter. Use this to shorten your code.
     */
    fun rawContactsWhere(where: RawContactsFields.() -> Where<RawContactsField>?): Delete

    /**
     * Deletes all of the RawContacts that have data that match the given [where].
     */
    fun rawContactsWhereData(where: Where<AbstractDataField>?): Delete

    /**
     * Same as [Delete.rawContactsWhereData] except you have direct access to all properties of
     * [Fields] in the function parameter. Use this to shorten your code.
     */
    fun rawContactsWhereData(where: Fields.() -> Where<AbstractDataField>?): Delete

    /**
     * Adds the given [contacts] to the delete queue, which will be deleted on [commit].
     *
     * ## Note
     *
     * Deleting a [ExistingContactEntity] will result in the deletion of all associated
     * [ExistingRawContactEntity]s even those that are not in the
     * [ExistingContactEntity.rawContacts] set!
     */
    fun contacts(vararg contacts: ExistingContactEntity): Delete

    /**
     * See [Delete.contacts].
     */
    fun contacts(contacts: Collection<ExistingContactEntity>): Delete

    /**
     * See [Delete.contacts].
     */
    fun contacts(contacts: Sequence<ExistingContactEntity>): Delete

    /**
     * Adds the given [contactsIds] to the delete queue, which will be deleted on [commit].
     *
     * ## Note
     *
     * Deleting a Contact will result in the deletion of all associated RawContacts!
     */
    fun contactsWithId(vararg contactsIds: Long): Delete

    /**
     * See [Delete.contactsWithId].
     */
    fun contactsWithId(contactsIds: Collection<Long>): Delete

    /**
     * See [Delete.contactsWithId].
     */
    fun contactsWithId(contactsIds: Sequence<Long>): Delete

    /**
     * Deletes all of the Contacts that match the given [where].
     *
     * Note that this will make an internal query when [commit] or [commitInOneTransaction] is
     * invoked, which may affect performance slightly.
     */
    fun contactsWhere(where: Where<ContactsField>?): Delete

    /**
     * Same as [Delete.contactsWhere] except you have direct access to all properties of
     * [ContactsFields] in the function parameter. Use this to shorten your code.
     */
    fun contactsWhere(where: ContactsFields.() -> Where<ContactsField>?): Delete

    /**
     * Deletes all of the Contacts that have data that match the given [where].
     */
    fun contactsWhereData(where: Where<AbstractDataField>?): Delete

    /**
     * Same as [Delete.contactsWhereData] except you have direct access to all properties of
     * [Fields] in the function parameter. Use this to shorten your code.
     */
    fun contactsWhereData(where: Fields.() -> Where<AbstractDataField>?): Delete

    /**
     * Deletes the [ExistingContactEntity]s and [ExistingRawContactEntity]s in the queue specified
     * via;
     *
     * - [rawContacts]
     * - [rawContactsWithId]
     * - [rawContactsWhere]
     * - [rawContactsWhereData]
     * - [contacts]
     * - [contactsWithId]
     * - [contactsWhere]
     * - [contactsWhereData]
     *
     * and returns the [Result].
     *
     * ## Permissions
     *
     * Requires the [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Deletes the [ExistingContactEntity]s and [ExistingRawContactEntity]s in the queue
     * (added via [contacts]/[contactsWithId] and [rawContacts]/[rawContactsWithId]) in one
     * transaction. Either ALL deletes succeed or ALL fail.
     *
     * ## Permissions
     *
     * Requires the [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commitInOneTransaction(): Result

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
    override fun redactedCopy(): Delete

    interface Result : CrudApi.Result {

        /**
         * True if all specified or matching Contacts and RawContacts have successfully been
         * deleted. False if even one delete failed.
         *
         * ## [commit] vs [commitInOneTransaction]
         *
         * If you used several of the following in one call,
         *
         * - [rawContacts]
         * - [rawContactsWithId]
         * - [rawContactsWhere]
         * - [rawContactsWhereData]
         * - [contacts]
         * - [contactsWithId]
         * - [contactsWhere]
         * - [contactsWhereData]
         *
         * then this value may be false even if the Contact and RawContacts were actually deleted if
         * you used [commit]. Using [commitInOneTransaction] does not have this "issue".
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully deleted. False otherwise.
         *
         * This does not indicate whether the parent [ExistingContactEntity] has been deleted or
         * not. This may return false even if the parent [ExistingContactEntity] has been deleted.
         *
         * This is used in conjunction with [Delete.rawContacts].
         */
        fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean

        /**
         * True if the RawContact with the given [rawContactId] has been successfully deleted.
         * False otherwise.
         *
         * This does not indicate whether the parent [ExistingContactEntity] has been deleted or
         * not. This may return false even if the parent [ExistingContactEntity] has been deleted.
         *
         * This is used in conjunction with [Delete.rawContactsWithId].
         */
        fun isRawContactDeleteSuccessful(rawContactId: Long): Boolean

        /**
         * True the [ExistingContactEntity] (and all of its associated [ExistingRawContactEntity]s)
         * has been successfully deleted. False otherwise.
         *
         * This does not indicate whether child [ExistingRawContactEntity]s have been deleted or
         * not. This may return false even if all associated [ExistingRawContactEntity]s have been
         * deleted.
         *
         * This is used in conjunction with [Delete.contacts].
         */
        fun isSuccessful(contact: ExistingContactEntity): Boolean

        /**
         * True the Contact with the given [contactId] (and all of its associated RawContacts) has
         * been successfully deleted. False otherwise.
         *
         * This does not indicate whether child RawContacts have been deleted or not. This may
         * return false even if all associated RawContacts have been deleted.
         *
         * This is used in conjunction with [Delete.contactsWithId].
         */
        fun isContactDeleteSuccessful(contactId: Long): Boolean

        /**
         * True if the delete operation using the given [where] was successful.
         *
         * This is used in conjunction with [Delete.rawContactsWhere],
         * [Delete.rawContactsWhereData], [Delete.contactsWhere], and [Delete.contactsWhereData].
         */
        fun isSuccessful(where: Where<*>): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

internal fun Delete(contacts: Contacts): Delete = DeleteImpl(contacts)

private class DeleteImpl(
    override val contactsApi: Contacts,

    private val rawContactIds: MutableSet<Long> = mutableSetOf(),
    private val contactIds: MutableSet<Long> = mutableSetOf(),

    private var rawContactsWhere: Where<RawContactsField>? = null,
    private var rawContactsWhereData: Where<AbstractDataField>? = null,

    private var contactsWhere: Where<ContactsField>? = null,
    private var contactsWhereData: Where<AbstractDataField>? = null,

    override val isRedacted: Boolean = false
) : Delete {

    private val hasNothingToCommit: Boolean
        get() = rawContactIds.isEmpty()
                && contactIds.isEmpty()
                && rawContactsWhere == null
                && rawContactsWhereData == null
                && contactsWhere == null
                && contactsWhereData == null

    override fun toString(): String =
        """
            Delete {
                rawContactIds: $rawContactIds
                contactIds: $contactIds
                rawContactsWhere: $rawContactsWhere
                rawContactsWhereData: $rawContactsWhereData
                contactsWhere: $contactsWhere
                contactsWhereData: $contactsWhereData
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    // There isn't really anything to redact =)
    override fun redactedCopy(): Delete = DeleteImpl(
        contactsApi,

        rawContactIds = rawContactIds,
        contactIds = contactIds,

        rawContactsWhere = rawContactsWhere?.redactedCopy(),
        rawContactsWhereData = rawContactsWhereData?.redactedCopy(),

        contactsWhere = contactsWhere?.redactedCopy(),
        contactsWhereData = contactsWhereData?.redactedCopy(),

        isRedacted = true
    )

    override fun rawContacts(vararg rawContacts: ExistingRawContactEntity) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>) =
        rawContactsWithId(rawContacts.map { it.id })

    override fun rawContactsWithId(vararg rawContactsIds: Long) =
        rawContactsWithId(rawContactsIds.asSequence())

    override fun rawContactsWithId(rawContactsIds: Collection<Long>) =
        rawContactsWithId(rawContactsIds.asSequence())

    override fun rawContactsWithId(rawContactsIds: Sequence<Long>): Delete = apply {
        this.rawContactIds.addAll(rawContactsIds)
    }

    override fun rawContactsWhere(where: Where<RawContactsField>?): Delete = apply {
        rawContactsWhere = where?.redactedCopyOrThis(isRedacted)
    }

    override fun rawContactsWhere(where: RawContactsFields.() -> Where<RawContactsField>?) =
        rawContactsWhere(where(RawContactsFields))

    override fun rawContactsWhereData(where: Where<AbstractDataField>?): Delete = apply {
        rawContactsWhereData = where?.redactedCopyOrThis(isRedacted)
    }

    override fun rawContactsWhereData(where: Fields.() -> Where<AbstractDataField>?) =
        rawContactsWhereData(where(Fields))

    override fun contacts(vararg contacts: ExistingContactEntity) = contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<ExistingContactEntity>) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<ExistingContactEntity>) =
        contactsWithId(contacts.map { it.id })

    override fun contactsWithId(vararg contactsIds: Long) = contactsWithId(contactsIds.asSequence())

    override fun contactsWithId(contactsIds: Collection<Long>) =
        contactsWithId(contactsIds.asSequence())

    override fun contactsWithId(contactsIds: Sequence<Long>): Delete = apply {
        this.contactIds.addAll(contactsIds)
    }

    override fun contactsWhere(where: Where<ContactsField>?): Delete = apply {
        contactsWhere = where?.redactedCopyOrThis(isRedacted)
    }

    override fun contactsWhere(where: ContactsFields.() -> Where<ContactsField>?) =
        contactsWhere(where(ContactsFields))

    override fun contactsWhereData(where: Where<AbstractDataField>?): Delete = apply {
        contactsWhereData = where?.redactedCopyOrThis(isRedacted)
    }

    override fun contactsWhereData(where: Fields.() -> Where<AbstractDataField>?) =
        contactsWhereData(where(Fields))

    override fun commit(): Delete.Result {
        onPreExecute()

        return if (!permissions.canUpdateDelete() || hasNothingToCommit) {
            DeleteAllResult(isSuccessful = false)
        } else {
            val rawContactsResult = mutableMapOf<Long, Boolean>()
            for (rawContactId in rawContactIds) {
                rawContactsResult[rawContactId] =
                    if (rawContactId.isProfileId) {
                        // Intentionally fail the operation to ensure that this is only used for
                        // non-profile updates. Otherwise, operation can succeed. This is only done
                        // to enforce API design.
                        false
                    } else {
                        contactsApi.deleteRawContactsWhere(
                            RawContactsFields.Id equalTo rawContactId
                        )
                    }
            }

            val contactsResults = mutableMapOf<Long, Boolean>()
            for (contactId in contactIds) {
                contactsResults[contactId] = if (contactId.isProfileId) {
                    // Intentionally fail the operation to ensure that this is only used for
                    // non-profile deletes. Otherwise, operation can succeed. This is only done to
                    // enforce API design.
                    false
                } else {
                    contactsApi.deleteRawContactsWhere(RawContactsFields.ContactId equalTo contactId)
                }
            }

            val whereResultMap = mutableMapOf<String, Boolean>()
            rawContactsWhere?.let {
                whereResultMap[it.toString()] = contactsApi.deleteRawContactsWhere(it)
            }
            rawContactsWhereData?.let {
                val reducedWhere = contactsApi.reduceDataTableWhereForMatchingRawContactIds(it)
                whereResultMap[it.toString()] = contactsApi.deleteRawContactsWhere(
                    RawContactsFields.Id
                            `in` contactsApi.findRawContactIdsInDataTable(reducedWhere)
                )
            }
            contactsWhere?.let {
                whereResultMap[it.toString()] = contactsApi.deleteRawContactsWhere(
                    RawContactsFields.ContactId
                            `in` contactsApi.findContactIdsInContactsTable(it)
                )
            }
            contactsWhereData?.let {
                val reducedWhere = contactsApi.reduceDataTableWhereForMatchingContactIds(it)
                whereResultMap[it.toString()] = contactsApi.deleteRawContactsWhere(
                    RawContactsFields.ContactId
                            `in` contactsApi.findContactIdsInDataTable(reducedWhere)
                )
            }

            DeleteResult(rawContactsResult, contactsResults, whereResultMap)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    override fun commitInOneTransaction(): Delete.Result {
        onPreExecute()

        return if (!permissions.canUpdateDelete() || hasNothingToCommit) {
            DeleteAllResult(isSuccessful = false)
        } else {
            val nonProfileRawContactIds = rawContactIds.filter { !it.isProfileId }
            val nonProfileContactIds = contactIds.filter { !it.isProfileId }

            if (rawContactIds.size != nonProfileRawContactIds.size ||
                contactIds.size != nonProfileContactIds.size
            ) {
                // There are some invalid ids or profile RawContacts, fail no-op.
                DeleteAllResult(isSuccessful = false)
            } else {
                val operations = arrayListOf<ContentProviderOperation>()

                if (nonProfileRawContactIds.isNotEmpty()) {
                    contactsApi.deleteOperationFor(RawContactsFields.Id `in` nonProfileRawContactIds)
                        .let(operations::add)
                }

                if (nonProfileContactIds.isNotEmpty()) {
                    contactsApi.deleteOperationFor(RawContactsFields.ContactId `in` nonProfileContactIds)
                        .let(operations::add)
                }

                rawContactsWhere?.let {
                    contactsApi.deleteOperationFor(it).let(operations::add)
                }
                rawContactsWhereData?.let {
                    val reducedWhere = contactsApi.reduceDataTableWhereForMatchingRawContactIds(it)
                    contactsApi.deleteOperationFor(
                        RawContactsFields.Id
                                `in` contactsApi.findRawContactIdsInDataTable(reducedWhere)
                    ).let(operations::add)
                }
                contactsWhere?.let {
                    contactsApi.deleteOperationFor(
                        RawContactsFields.ContactId
                                `in` contactsApi.findContactIdsInContactsTable(it)
                    ).let(operations::add)
                }
                contactsWhereData?.let {
                    val reducedWhere = contactsApi.reduceDataTableWhereForMatchingContactIds(it)
                    contactsApi.deleteOperationFor(
                        RawContactsFields.ContactId
                                `in` contactsApi.findContactIdsInDataTable(reducedWhere)
                    ).let(operations::add)
                }

                DeleteAllResult(isSuccessful = contentResolver.applyBatch(operations).deleteSuccess)
            }
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

internal fun Contacts.deleteRawContactsWhere(where: Where<RawContactsField>): Boolean =
    contentResolver.applyBatch(deleteOperationFor(where)).deleteSuccess

private fun Contacts.deleteOperationFor(where: Where<RawContactsField>): ContentProviderOperation =
    RawContactsOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = false
    ).deleteRawContactsWhere(where)

private class DeleteResult private constructor(
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    private val contactIdsResultMap: Map<Long, Boolean>,
    private var whereResultMap: Map<String, Boolean>,
    override val isRedacted: Boolean
) : Delete.Result {

    constructor(
        rawContactIdsResultMap: Map<Long, Boolean>,
        contactIdsResultMap: Map<Long, Boolean>,
        whereResultMap: Map<String, Boolean>
    ) : this(rawContactIdsResultMap, contactIdsResultMap, whereResultMap, false)

    override fun toString(): String =
        """
            Delete.Result {
                isSuccessful: $isSuccessful
                rawContactIdsResultMap: $rawContactIdsResultMap
                contactIdsResultMap: $contactIdsResultMap
                whereResultMap: $whereResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Delete.Result = DeleteResult(
        rawContactIdsResultMap = rawContactIdsResultMap,
        contactIdsResultMap = contactIdsResultMap,
        whereResultMap = whereResultMap.redactedStringKeys(),
        isRedacted = true
    )

    override val isSuccessful: Boolean by lazy {
        if (rawContactIdsResultMap.isEmpty()
            && contactIdsResultMap.isEmpty()
            && whereResultMap.isEmpty()
        ) {
            // Deleting nothing is NOT successful.
            false
        } else {
            // A set has failure if it is NOT empty and one of its entries is false.
            val hasRawContactFailure = rawContactIdsResultMap.any { !it.value }
            val hasContactFailure = contactIdsResultMap.any { !it.value }
            val hasWhereFailure = whereResultMap.any { !it.value }
            !hasRawContactFailure && !hasContactFailure && !hasWhereFailure
        }
    }

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean =
        isRawContactDeleteSuccessful(rawContact.id)

    override fun isRawContactDeleteSuccessful(rawContactId: Long): Boolean =
        rawContactIdsResultMap.getOrElse(rawContactId) { false }

    override fun isSuccessful(contact: ExistingContactEntity): Boolean =
        isContactDeleteSuccessful(contact.id)

    override fun isContactDeleteSuccessful(contactId: Long): Boolean =
        contactIdsResultMap.getOrElse(contactId) { false }

    override fun isSuccessful(where: Where<*>): Boolean =
        whereResultMap.getOrElse(where.toString()) { false }
}

private class DeleteAllResult private constructor(
    override val isSuccessful: Boolean,
    override val isRedacted: Boolean
) : Delete.Result {

    constructor(isSuccessful: Boolean) : this(
        isSuccessful = isSuccessful,
        isRedacted = false
    )

    override fun toString(): String =
        """
            Delete.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Delete.Result = DeleteAllResult(
        isSuccessful = isSuccessful,
        isRedacted = true
    )

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean = isSuccessful

    override fun isRawContactDeleteSuccessful(rawContactId: Long): Boolean = isSuccessful

    override fun isSuccessful(contact: ExistingContactEntity): Boolean = isSuccessful

    override fun isContactDeleteSuccessful(contactId: Long): Boolean = isSuccessful

    override fun isSuccessful(where: Where<*>): Boolean = isSuccessful
}