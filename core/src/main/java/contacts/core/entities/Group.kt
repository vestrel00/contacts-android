package contacts.core.entities

import android.accounts.Account
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * [Entity] in the Groups table.
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
sealed class GroupsEntity : Entity {

    /**
     * The id of this row in the Groups table.
     */
    abstract override val id: Long?

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
    abstract val systemId: String?

    /**
     * If true, this group cannot be modified. All system groups are read-only.
     *
     * This is a read-only flag! The Contacts Provider routinely sets this to false for all
     * user-created groups. System groups has this set to true.
     */
    abstract val readOnly: Boolean

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
    abstract val favorites: Boolean

    /**
     * Any newly created contacts will automatically be added to groups that have this flag set to
     * true.
     *
     * This is a read-only flag! The Contacts Provider routinely sets this to false for all
     * user-created groups.  System groups has this set to true.
     */
    abstract val autoAdd: Boolean

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
    abstract val account: Account

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

    @IgnoredOnParcel
    override val isBlank: Boolean = false
}

/**
 * See [GroupsEntity].
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class Group internal constructor(

    /**
     * See [GroupsEntity.id].
     */
    override val id: Long?,

    /**
     * See [GroupsEntity.systemId].
     */
    override val systemId: String?,

    /**
     * The display title of this group.
     */
    val title: String,

    /**
     * See [GroupsEntity.readOnly].
     */
    override val readOnly: Boolean,

    /**
     * See [GroupsEntity.favorites].
     */
    override val favorites: Boolean,

    /**
     * See [GroupsEntity.autoAdd].
     */
    override val autoAdd: Boolean,

    /**
     * See [GroupsEntity.account].
     */
    override val account: Account

) : GroupsEntity() {

    /**
     * Returns a [MutableGroup]. If [readOnly] is true, this returns null instead.
     */
    fun toMutableGroup(): MutableGroup? = if (readOnly) {
        null
    } else {
        MutableGroup(id, systemId, title, readOnly, favorites, autoAdd, account)
    }
}

/**
 * A mutable [Group].
 *
 * ## Dev notes
 *
 * See DEV_NOTES sections "Creating Entities" and "Immutable vs Mutable Entities".
 */
@Parcelize
data class MutableGroup internal constructor(

    /**
     * See [Group.id].
     */
    override val id: Long?,

    /**
     * See [Group.systemId].
     */
    override val systemId: String?,

    /**
     * See [Group.title].
     */
    var title: String,

    /**
     * See [Group.readOnly].
     */
    override val readOnly: Boolean,

    /**
     * See [Group.favorites].
     */
    override val favorites: Boolean,

    /**
     * See [Group.autoAdd].
     */
    override val autoAdd: Boolean,

    /**
     * See [Group.account].
     */
    override val account: Account

) : GroupsEntity() {

    constructor(title: String, account: Account) : this(
        null, null, title, false, false, false, account
    )
}

/**
 * Returns the list of system groups found in [this] collection of groups.
 */
fun <T : GroupsEntity> Collection<T>.systemGroups(): List<T> = filter { it.isSystemGroup }

/**
 * Returns the first default group found in [this] collection of groups.
 */
fun <T : GroupsEntity> Collection<T>.defaultGroup(): T? = firstOrNull { it.isDefaultGroup }

/**
 * Returns the first favorites group found in [this] collection of groups.
 */
fun <T : GroupsEntity> Collection<T>.favoritesGroup(): T? = firstOrNull { it.isFavoritesGroup }

/**
 * Returns the sequence of system groups found in [this] sequence of groups.
 */
fun <T : GroupsEntity> Sequence<T>.systemGroups(): Sequence<T> = filter { it.isSystemGroup }

/**
 * Returns the first default group found in [this] sequence of groups.
 */
fun <T : GroupsEntity> Sequence<T>.defaultGroup(): T? = firstOrNull { it.isDefaultGroup }

/**
 * Returns the first favorites group found in [this] sequence of groups.
 */
fun <T : GroupsEntity> Sequence<T>.favoritesGroup(): T? = firstOrNull { it.isFavoritesGroup }

