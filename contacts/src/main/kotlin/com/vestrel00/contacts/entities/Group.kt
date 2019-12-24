package com.vestrel00.contacts.entities

import android.accounts.Account
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group internal constructor(

    /**
     * The id of this row in the Groups table.
     */
    override val id: Long,

    /**
     * The display title of this group.
     */
    val title: String,

    /**
     * If true, this group is a system group and cannot be modified. All system groups are read
     * only.
     *
     * All custom groups created by this library have this flag set to false.
     *
     * IMPORTANT! The [toMutableGroup] function will return null if this flag is true.
     *
     * Please do not set this to true using the data class [copy] function because it would then
     * be treated by this library as an unmodifiable group like system groups.
     */
    val readOnly: Boolean,

    /**
     * When a contacts is marked as a favorites it will be automatically added to the groups that
     * have this flag set, and when it is removed from favorites it will be removed from these
     * groups.
     *
     * This is a read-only flag! The Contacts Provider routinely sets this to false for all
     * user-created groups. Most, if not all, Android systems will only have one group marked as
     * the favorites group. That is typically the following system group;
     *
     * ```
     * Group id: 2, systemId: null, title: Starred in Android, readOnly: 1, favorites: 1
     * ```
     *
     * Contacts that are "starred" belong to this favorites group.
     */
    val favorites: Boolean,

    /**
     * Any newly created contacts will automatically be added to groups that have this flag set to
     * true.
     *
     * This is a read-only flag! The Contacts Provider routinely sets this to false for all
     * user-created groups. Most, if not all, Android systems will only have one group marked as
     * the favorites group. That is typically the following system group;
     *
     * ```
     * Group id: 2, systemId: null, title: Starred in Android, readOnly: 1, favorites: 1
     * ```
     */
    val autoAdd: Boolean,

    /**
     * The account that this group is associated with.
     *
     * If an incorrect account is provided, the first account returned by the system will be used
     * (if any).
     *
     * When there are no available accounts, no group may exist. The native Contacts app does not
     * display the groups field when creating or updating contacts when there are no available
     * accounts present.
     */
    val account: Account

) : Entity, Parcelable {

    /**
     * The default group is a system group that has [autoAdd] set to true. A system group is a
     * [readOnly] group that belongs to an [Account]. An account has several system groups that are
     * tied to it.
     *
     * Usually, only one of these system groups have [autoAdd] set to true and that is typically the
     * "default" group as it is not shown in any UI as a selectable group. All contacts for an
     * account belong to the default group.
     */
    @IgnoredOnParcel
    val isDefaultGroup: Boolean = readOnly && autoAdd

    /**
     * The favorites group is a system group that has [favorites] set to true. A system group is a
     * [readOnly] group that belongs to an [Account]. An account has several system groups that are
     * tied to it.
     *
     * Usually, only one of these system groups have [favorites] set to true and that is typically
     * the "favorites" group as it is shown in the UI as a special group.
     */
    @IgnoredOnParcel
    val isFavoritesGroup: Boolean = readOnly && favorites

    override fun isBlank(): Boolean = false

    /**
     * Returns a [MutableGroup]. If [readOnly] is true, this returns null instead.
     */
    fun toMutableGroup(): MutableGroup? = if (readOnly) {
        null
    } else {
        MutableGroup(
            id = id,

            title = title,

            readOnly = readOnly,
            favorites = favorites,
            autoAdd = autoAdd,

            account = account
        )
    }
}

@Parcelize
data class MutableGroup internal constructor(

    /**
     * See [Group.id].
     *
     * This may be an INVALID_ID if not retrieved from the DB via a query.
     */
    override val id: Long,

    /**
     * See [Group.title].
     */
    var title: String,

    /**
     * See [Group.readOnly].
     *
     * This library only allows the creation of non-read-only groups. Therefore, this value is
     * always false (to consumers).
     *
     * Please do not set this to true using the data class [copy] function because it would then
     * be treated by this library as an unmodifiable group like system groups.
     */
    val readOnly: Boolean,

    /**
     * See [Group.favorites].
     */
    val favorites: Boolean,

    /**
     * See [Group.autoAdd].
     */
    val autoAdd: Boolean,

    /**
     * See [Group.account].
     */
    val account: Account

) : Entity, Parcelable {

    constructor(title: String, account: Account) : this(
        INVALID_ID, title, false, false, false, account
    )

    /**
     * The default group is a system group that has [autoAdd] set to true. A system group is a
     * [readOnly] group that belongs to an [Account]. An account has several system groups that are
     * tied to it.
     *
     * Usually, only one of these system groups have [autoAdd] set to true and that is typically the
     * "default" group as it is not shown in any UI as a selectable group. All contacts for an
     * account belong to the default group.
     */
    @IgnoredOnParcel
    val isDefaultGroup: Boolean = readOnly && autoAdd

    /**
     * The favorites group is a system group that has [favorites] set to true. A system group is a
     * [readOnly] group that belongs to an [Account]. An account has several system groups that are
     * tied to it.
     *
     * Usually, only one of these system groups have [favorites] set to true and that is typically
     * the "favorites" group as it is shown in the UI as a special group.
     */
    @IgnoredOnParcel
    val isFavoritesGroup: Boolean = readOnly && favorites

    override fun isBlank(): Boolean = false

    internal fun toGroup() = Group(
        id = id,

        title = title,

        readOnly = readOnly,
        favorites = favorites,
        autoAdd = autoAdd,

        account = account
    )
}
