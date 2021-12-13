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
     * "Default" is the terminology used by the native Contacts app. Consumers should use the
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

sealed interface DataEntityWithTypeAndLabel<T : DataEntity.Type> : DataEntity {

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
}

/**
 * A [DataEntity] that has NOT yet been inserted into the database.
 */
sealed interface NewDataEntity : DataEntity, NewEntity {

    override val isPrimary: Boolean
        get() = false

    override val isSuperPrimary: Boolean
        get() = false
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

    /**
     * True if this data belongs to the user's personal profile entry.
     */
    val isProfile: Boolean
        get() = id.isProfileId
}


/**
 * An immutable [DataEntity].
 */
sealed interface ImmutableDataEntity : DataEntity, ImmutableEntity

/**
 * An [ImmutableDataEntity] that has a mutable type [T].
 */
sealed interface ImmutableDataEntityWithMutableType<T : MutableDataEntity> : ImmutableDataEntity,
    ImmutableEntityWithMutableType<T>

/**
 * An [ImmutableDataEntity] that has a mutable type [T] that may or may not be null.
 */
sealed interface ImmutableDataEntityWithNullableMutableType<T : MutableDataEntity> :
    ImmutableDataEntity,
    ImmutableEntityWithNullableMutableType<T>

/**
 * A mutable [DataEntity], with a mutable [primaryValue].
 */
sealed interface MutableDataEntity : DataEntity, MutableEntity {

    override var primaryValue: String?
}

/**
 * A [MutableDataEntity], with a mutable [type] and [label].
 */
sealed interface MutableDataEntityWithTypeAndLabel<T : DataEntity.Type> : MutableDataEntity,
    DataEntityWithTypeAndLabel<T> {

    override var type: T?
    override var label: String?
}