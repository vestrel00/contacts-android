package contacts.core.entities

import android.accounts.Account
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
     * - The Contacts system group is the default group in which all raw contacts of an account
     *   belongs to. Therefore, it is typically hidden when showing the list of groups in the UI.
     * - The starred (favorites) group is not a system group as it has null system id. However,
     *   it behaves like one in that it is read only and it comes with most (if not all) copies of
     *   the native Contacts app.
     */
    val systemId: String?

    /**
     * The display title of this group.
     */
    val title: String

    /**
     * If true, this group cannot be modified. All system groups are read-only.
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
     * #### Notes
     *
     * When there are no available accounts, no group may exist. The native Contacts app does not
     * display the groups field when creating or updating raw contacts when there are no available
     * accounts present.
     */
    val account: Account

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
sealed interface ExistingGroupEntity : GroupEntity, ExistingEntity

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
    override val account: Account

) : ExistingGroupEntity, ImmutableEntityWithNullableMutableType<MutableGroup> {

    /**
     * Returns a [MutableGroup]. If [readOnly] is true, this returns null instead.
     */
    override fun mutableCopy(): MutableGroup? = if (readOnly) {
        null
    } else {
        MutableGroup(id, systemId, title, readOnly, favorites, autoAdd, account)
    }
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
    override val account: Account

) : ExistingGroupEntity, MutableEntity

/**
 * A new mutable [GroupEntity].
 */
@Parcelize
data class NewGroup(
    override var title: String,
    override var account: Account
) : GroupEntity, NewEntity, MutableEntity {

    override val systemId: String?
        get() = null

    override val readOnly: Boolean
        get() = false

    override val favorites: Boolean
        get() = false

    override val autoAdd: Boolean
        get() = false
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

