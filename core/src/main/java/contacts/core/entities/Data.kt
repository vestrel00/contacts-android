package contacts.core.entities

import android.content.res.Resources
import android.provider.ContactsContract
import contacts.core.util.isProfileId

/**
 * [Entity] that holds data modeling columns in the Data table.
 *
 * ## Contact, RawContact, and Data
 *
 * A Contact may consist of one or more RawContact. A RawContact is an association between a Contact
 * and an [android.accounts.Account]. Each RawContact is associated with several pieces of Data such
 * as name, emails, phone, address, and more.
 *
 * The Contacts Provider may combine RawContacts from several different Accounts. The same effect
 * is achieved when merging / linking multiple contacts.
 *
 * It is possible for a RawContact to not be associated with an Account. Such RawContacts are local
 * to the device and are not synced.
 *
 * ## Data count restrictions
 *
 * A RawContact may either have...
 *
 * - only 0 or 1
 * - 0, 1, or more
 *
 * of this type of entity
 */
sealed interface DataEntity : Entity {

    /**
     * The main value encapsulated by this entity as a string.
     */
    val primaryValue: String?

    /**
     * The type of data.
     */
    val mimeType: MimeType

    /**
     * Whether this is the primary entry of its kind for the [RawContact] it belongs to.
     *
     * ## Developer Notes
     *
     * This is immutable to prevent consumers from setting multiple data entities of the same
     * mimetype as primary. Consumers should use the DefaultContactData extension functions to
     * modify these values.
     */
    val isPrimary: Boolean

    /**
     * Whether this is the primary entry of its kind for the aggregate [Contact] it belongs to. Any
     * data record that is "super primary" must also be [isPrimary].
     *
     * ## Developer Notes
     *
     * This is immutable to prevent consumers from setting multiple data entities of the same
     * mimetype as primary. Consumers should use the DefaultContactData extension functions to
     * modify these values.
     */
    val isSuperPrimary: Boolean

    /**
     * True if [isSuperPrimary] is true.
     *
     * "Default" is the terminology used by the AOSP Contacts app. Consumers should use the
     * DefaultContactData extension functions to set a data entity as default or not.
     */
    val isDefault: Boolean
        get() = isSuperPrimary

    /**
     * Returns true if the underlying data contains at least one non-null and non-empty piece of
     * information.
     *
     * Blank data are typically;
     *
     * - Not returned in any query results
     * - Not inserted
     * - Deleted upon update
     */
    // Overridden for documentation purposes.
    override val isBlank: Boolean

    /**
     * True if this data belongs to the user's personal profile entry.
     */
    val isProfile: Boolean
        get() = false

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): DataEntity

    /**
     * A type of data. Used by data that may have several types.
     */
    // Not sealed intentionally.
    interface Type {
        /**
         * The actual value that represents the type.
         */
        val value: Int

        /**
         * True if this type is the custom type.
         */
        val isCustomType: Boolean
            get() = value == ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM

        /**
         * The string representation of this type.
         *
         * If this [isCustomType] then the [label] is used (if not null). Otherwise, the
         * Android default string for this type is used.
         */
        fun labelStr(resources: Resources, label: String?): String
    }
}

sealed interface DataEntityWithTypeAndLabel<out T : DataEntity.Type> : DataEntity {

    /**
     * The [DataEntity.Type].
     */
    val type: T?

    /**
     * Used as the string representation of the [type] if this is not null and the [type] is custom.
     * Otherwise, the system's string representation of the type is used.
     *
     * This is the string value displayed in the UI for user-created custom types. This is only used
     * when the [type] is custom.
     */
    val label: String?

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): DataEntityWithTypeAndLabel<T>
}

/**
 * A [DataEntity] that has NOT yet been inserted into the database.
 */
sealed interface NewDataEntity : DataEntity, NewEntity {

    /**
     * When true, only sync adapters are allowed to update this data, though deletion may still be
     * possible regardless.
     *
     * Note that this property is not at the [DataEntity] because including the
     * [android.provider.ContactsContract.DataColumns.IS_READ_ONLY] column in query projection
     * array causes an exception to be thrown. In order to check if an [ExistingDataEntity] is
     * read-only, use the read-only field in query selection/WHERE clauses in conjunction with the
     * ID. Or, use the extension functions provided in [contacts.core.util].
     *
     * ## For sync adapter use only!
     *
     * Do NOT set this to true unless you know what you are doing. Typically, you only set this to
     * true in the context of sync adapters. If you are working on a regular application, keep this
     * false.
     *
     * ## Update API results are non-indicative of actual success or failure.
     *
     * Update API operations from those that are not sync adapters for read-only data may still
     * indicate success even though the data was not actually updated. This is a limitation/behavior
     * that is also present in other Contacts apps such as AOSP Contacts, Google Contacts, Samsung
     * Contacts, etc.
     */
    var isReadOnly: Boolean

    override val isPrimary: Boolean
        get() = false

    override val isSuperPrimary: Boolean
        get() = false

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): NewDataEntity
}

/**
 * A [DataEntity] that has already been inserted into the database.
 */
sealed interface ExistingDataEntity : DataEntity, ExistingEntity {
    /**
     * The id of the Data row this represents.
     */
    // Overridden for documentation purposes.
    override val id: Long

    /**
     * The id of the [RawContact] that this data entity is associated with.
     */
    val rawContactId: Long

    /**
     * The id of the [Contact] that this data entity is associated with.
     */
    val contactId: Long

    override val isProfile: Boolean
        get() = id.isProfileId

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ExistingDataEntity
}


/**
 * An immutable [DataEntity].
 */
sealed interface ImmutableDataEntity : DataEntity, ImmutableEntity {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ImmutableDataEntity
}

/**
 * An [ImmutableDataEntity] that has a mutable type [T].
 */
sealed interface ImmutableDataEntityWithMutableType<T : MutableDataEntity> : ImmutableDataEntity,
    ImmutableEntityWithMutableType<T> {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ImmutableDataEntityWithMutableType<T>
}

/**
 * A mutable [DataEntity], with a mutable [primaryValue].
 */
sealed interface MutableDataEntity : DataEntity, MutableEntity {

    override var primaryValue: String?

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableDataEntity
}

/**
 * A [MutableDataEntity], with a mutable [type] and [label].
 */
sealed interface MutableDataEntityWithTypeAndLabel<T : DataEntity.Type> : MutableDataEntity,
    DataEntityWithTypeAndLabel<T> {

    override var type: T?
    override var label: String?

    /**
     * Sets the [type] to the given [unsafeType], which MUST be a subclass of [T]. Otherwise, an
     * exception will be thrown.
     *
     * This function is useful at times when the type [T] is erased but you still need to be able to
     * set the [type]. Use with caution! If you don't know what you are doing, don't use this!
     */
    @Suppress("UNCHECKED_CAST")
    fun setTypeUnsafe(unsafeType: DataEntity.Type?) {
        type = unsafeType as T?
    }

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableDataEntityWithTypeAndLabel<T>
}