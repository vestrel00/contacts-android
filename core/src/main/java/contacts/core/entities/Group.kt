package contacts.core.entities

import android.accounts.Account
import contacts.core.util.redactedCopy
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * [Entity] that holds data modeling columns in the Groups table.
 */
sealed interface GroupEntity : Entity {

    /**
     * The id of this group if it is a System Group, i.e. a group that has a special meaning to the
     * sync adapter, null otherwise.
     *
     * System ids are typically Contacts, Friends, Family, and Coworkers, which are typically the
     * same across all versions of Android.
     *
     * #### Notes
     *
     * - The "Contacts" system group is the default group in which all raw contacts of an account
     *   belongs to. Therefore, it is typically hidden when showing the list of groups in the UI.
     * - The starred (favorites) group is not a system group as it has null system id. However,
     *   it behaves like one in that it is read only and it comes with most (if not all) copies of
     *   the AOSP Contacts app.
     */
    val systemId: String?

    /**
     * The display title of this group.
     */
    val title: String

    /**
     * If true, this group cannot be modified except by sync adapters. Note that all system groups
     * are read-only.
     *
     * This is a read-only flag! The Contacts Provider routinely sets this to false for all
     * user-created groups. System groups has this set to true.
     */
    val readOnly: Boolean

    /**
     * When a contacts is marked as a favorites it will be automatically added to the groups that
     * have this flag set, and when it is removed from favorites it will be removed from these
     * groups.
     *
     * This is a read-only flag! The Contacts Provider routinely sets this to false for all
     * user-created groups. System groups has this set to true.
     *
     * Contacts that are "starred" belong to this favorites group.
     */
    val favorites: Boolean

    /**
     * Any newly created contacts will automatically be added to groups that have this flag set to
     * true.
     *
     * This is a read-only flag! The Contacts Provider routinely sets this to false for all
     * user-created groups.  System groups has this set to true.
     */
    val autoAdd: Boolean

    /**
     * The Account that this group is associated with.
     *
     * This must be a valid Account.
     *
     * ## Samsung and Xiaomi devices
     *
     * Samsung and Xiaomi devices use non-null for the account name and type of local RawContacts
     * in the RawContacts table instead of null. This will be null for [Account] instances created
     * with this name and type.
     *
     * #### Notes
     *
     * In the AOSP Contacts app, when there are no available accounts, no group may exist. As a
     * result, the AOSP Contacts app does not display the groups field when creating or updating
     * raw contacts when there are no available accounts present.
     *
     * On the other hand, other Contacts applications such as Google Contacts allows groups to exist
     * without an association to an account.
     *
     * Therefore, this library will allow groups to exist without an account. The responsibility of
     * allowing or disallowing groups to exist without an account is up to the application.
     *
     * More info in https://github.com/vestrel00/contacts-android/issues/167
     */
    val account: Account?

    /**
     * Similar to [RawContactEntity.sourceId], this is a String that uniquely identifies this row to
     * its source account.
     *
     * The source ID will be null if the RawContact is not associated/managed by an [account] that
     * has a sync adapter that assigns a non-null value to it.
     *
     * ## Not guaranteed to be immediate!
     *
     * When a [NewGroup] is inserted with a [NewGroup.account] that has a sync adapter, this
     * property may be set to a non-null value. The final value of this property may be assigned at
     * a later time, when the sync adapter performs a sync. This means that the value of this may be
     * null or assigned a non-null temporary value right after insertion but may change
     * once the sync as occurred.
     *
     * ## For sync adapter use only!
     *
     * Applications should NOT set/modify the value of this property!
     *
     * Setting the value for this property at the time of insertion or updating its value afterwards
     * is typically only done in the context of sync adapters. This is not for general app use!
     *
     * Do NOT mess with this unless you know exactly what you are doing. Otherwise, it WILL cause
     * issues with syncing with respect to the Account's sync adapter and remote servers/databases.
     *
     * ## Other things to note
     *
     * Surprisingly, setting/modifying this value does not require
     * [android.provider.ContactsContract.CALLER_IS_SYNCADAPTER] to be set to true. This means that
     * regular applications can set/modify it... The best we can do is document this.
     */
    val sourceId: String?

    /**
     * A group that is created and managed by the system and cannot be modified.
     *
     * See [systemId] for more info.
     */
    @IgnoredOnParcel
    val isSystemGroup: Boolean
        get() = systemId != null

    /**
     * The default group of an Account is a system group that has [autoAdd] set to true.
     *
     * #### Notes
     *
     * Usually, only one of these system groups have [autoAdd] set to true and that is typically the
     * "default" group as it is not shown in any UI as a selectable group. All raw contacts for an
     * Account belong to the Account's default group.
     */
    @IgnoredOnParcel
    val isDefaultGroup: Boolean
        // FIXME? Should we instead check if the systemId is "Contacts"? Do we hard code that string
        // or is it defined as a constant somewhere?
        get() = isSystemGroup && autoAdd

    /**
     * The favorites group is a read-only group that has [favorites] set to true.
     *
     * #### Notes
     *
     * Usually, an Account only has one group that have [favorites] set to true and that is
     * typically THE "favorites" group as it is shown in the UI as a special group.
     */
    @IgnoredOnParcel
    val isFavoritesGroup: Boolean
        get() = readOnly && favorites

    // This is never blank because of the non-nullable attributes such as readOnly, favorites, ...
    @IgnoredOnParcel
    override val isBlank: Boolean
        get() = false

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): GroupEntity
}

/* DEV NOTES: Necessary Abstractions
 *
 * We only create abstractions when they are necessary!
 *
 * Apart from GroupEntity, there is only one interface that extends it; ExistingGroupEntity.
 * This interface is used for library functions that require a GroupEntity with an ID, which means
 * that it exists in the database. There are two variants of this; Group and MutableGroup.
 * With this, we can create functions (or extensions) that can take in (or have as the receiver)
 * either Group or MutableGroup through the ExistingGroupEntity abstraction/facade.
 *
 * This is why there are no interfaces for NewGroupEntity, ImmutableGroupEntity, and
 * MutableGroupEntity. There are currently no library functions or constructs that require them.
 *
 * Please update this documentation if new abstractions are created.
 */

/**
 * A [GroupEntity] that has already been inserted into the database.
 */
sealed interface ExistingGroupEntity : GroupEntity, ExistingEntity {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ExistingGroupEntity
}

/**
 * An existing immutable [GroupEntity].
 */
@Parcelize
data class Group internal constructor(

    override val id: Long,
    override val systemId: String?,

    override val title: String,

    override val readOnly: Boolean,
    override val favorites: Boolean,
    override val autoAdd: Boolean,
    override val account: Account?,
    override val sourceId: String?,

    override val isRedacted: Boolean

) : ExistingGroupEntity, ImmutableEntityWithMutableType<MutableGroup> {

    override fun mutableCopy() = MutableGroup(
        id = id,
        systemId = systemId,
        title = title,
        readOnly = readOnly,
        favorites = favorites,
        autoAdd = autoAdd,
        account = account,
        sourceId = sourceId,
        isRedacted = isRedacted
    )

    override fun redactedCopy() = copy(
        isRedacted = true,

        title = title.redact(),
        account = account?.redactedCopy()
    )
}

/**
 * An existing mutable [GroupEntity].
 */
@Parcelize
data class MutableGroup internal constructor(

    override val id: Long,

    override val systemId: String?,

    override var title: String,

    override val readOnly: Boolean,
    override val favorites: Boolean,
    override val autoAdd: Boolean,
    override val account: Account?,
    override var sourceId: String?,

    override val isRedacted: Boolean

) : ExistingGroupEntity, MutableEntity {

    override fun redactedCopy() = copy(
        isRedacted = true,

        title = title.redact(),
        account = account?.redactedCopy()
    )
}

/**
 * A new mutable [GroupEntity].
 */
@Parcelize
data class NewGroup @JvmOverloads constructor(

    override var title: String,
    override var account: Account?,

    override var sourceId: String? = null,

    override val isRedacted: Boolean = false

) : GroupEntity, NewEntity, MutableEntity {

    override val systemId: String?
        get() = null

    override val readOnly: Boolean
        get() = false

    override val favorites: Boolean
        get() = false

    override val autoAdd: Boolean
        get() = false

    override fun redactedCopy() = copy(
        isRedacted = true,

        title = title.redact(),
        account = account?.redactedCopy()
    )
}

/**
 * Returns the list of system groups found in [this] collection of groups.
 */
fun <T : GroupEntity> Collection<T>.systemGroups(): List<T> = filter { it.isSystemGroup }

/**
 * Returns the first default group found in [this] collection of groups.
 */
fun <T : GroupEntity> Collection<T>.defaultGroup(): T? = firstOrNull { it.isDefaultGroup }

/**
 * Returns the first favorites group found in [this] collection of groups.
 */
fun <T : GroupEntity> Collection<T>.favoritesGroup(): T? = firstOrNull { it.isFavoritesGroup }

/**
 * Returns the sequence of system groups found in [this] sequence of groups.
 */
fun <T : GroupEntity> Sequence<T>.systemGroups(): Sequence<T> = filter { it.isSystemGroup }

/**
 * Returns the first default group found in [this] sequence of groups.
 */
fun <T : GroupEntity> Sequence<T>.defaultGroup(): T? = firstOrNull { it.isDefaultGroup }

/**
 * Returns the first favorites group found in [this] sequence of groups.
 */
fun <T : GroupEntity> Sequence<T>.favoritesGroup(): T? = firstOrNull { it.isFavoritesGroup }

