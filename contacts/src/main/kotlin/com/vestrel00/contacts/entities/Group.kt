package com.vestrel00.contacts.entities

import android.accounts.Account
import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Group internal constructor(

    override val id: Long,

    val title: String,

    val readOnly: Boolean,

    val favorites: Boolean,

    val autoAdd: Boolean,

    val account: Account

) : Entity, Parcelable {

    @IgnoredOnParcel
    val isDefaultGroup: Boolean = readOnly && autoAdd

    @IgnoredOnParcel
    val isFavoritesGroup: Boolean = readOnly && favorites

    override fun isBlank(): Boolean = false

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

    override val id: Long,

    var title: String,

    val readOnly: Boolean,

    val favorites: Boolean,

    val autoAdd: Boolean,

    val account: Account

) : Entity, Parcelable {

    constructor(title: String, account: Account) : this(
        INVALID_ID, title, false, false, false, account
    )
    
    @IgnoredOnParcel
    val isDefaultGroup: Boolean = readOnly && autoAdd

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
